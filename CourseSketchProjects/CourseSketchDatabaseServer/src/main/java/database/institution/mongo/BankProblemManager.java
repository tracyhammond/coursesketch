package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.utils.Util;
import protobuf.srl.school.Problem.SrlBankProblem;
import protobuf.srl.services.authentication.Authentication;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.COURSE_ACCESS;
import static database.DatabaseStringConstants.COURSE_TOPIC;
import static database.DatabaseStringConstants.KEYWORDS;
import static database.DatabaseStringConstants.QUESTION_TEXT;
import static database.DatabaseStringConstants.QUESTION_TYPE;
import static database.DatabaseStringConstants.REGISTRATION_KEY;
import static database.DatabaseStringConstants.SCRIPT;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.SOLUTION_ID;
import static database.DatabaseStringConstants.SOURCE;
import static database.DatabaseStringConstants.STATE_PUBLISHED;
import static database.DatabaseStringConstants.SUB_TOPIC;
import static database.DbSchoolUtility.getCollectionFromType;
import static database.utilities.MongoUtilities.convertStringToObjectId;

/**
 * Interfaces with the mongo database to manage bank problems.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
public final class BankProblemManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MongoInstitution.class);

    /**
     * The amount of problems that are allowed to show at the same time.
     */
    private static final int PAGE_LENGTH = 20;

    /**
     * Private constructor.
     */
    private BankProblemManager() {
    }

    /**
     * Inserts a problem bank into the mongo database.
     *
     * @param dbs
     *         the database into which the bank is being inserted.
     * @param problem
     *         the problem data that is being inserted.
     * @return The mongo id of the problem bank.
     * @throws AuthenticationException
     *         Not currently thrown but may be thrown in the future.
     */
    public static String mongoInsertBankProblem(final DB dbs, final SrlBankProblem problem) throws AuthenticationException {
        final DBCollection problemBankCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final BasicDBObject insertObject = new BasicDBObject(QUESTION_TEXT, problem.getQuestionText())
                .append(SOLUTION_ID, problem.getSolutionId())
                .append(COURSE_TOPIC, problem.getCourseTopic())
                .append(SUB_TOPIC, problem.getSubTopic())
                .append(SOURCE, problem.getSource())
                .append(QUESTION_TYPE, problem.getQuestionType().getNumber())
                .append(SCRIPT, problem.getScript())
                .append(KEYWORDS, problem.getOtherKeywordsList())
                .append(REGISTRATION_KEY, problem.getRegistrationKey())
                .append(STATE_PUBLISHED, true)
                .append(COURSE_ACCESS, 0);

        if (problem.hasSpecialQuestionData()) {
            insertObject.append(DatabaseStringConstants.SPECIAL_QUESTION_DATA, SlideManager.createQueryFromElement(problem.getSpecialQuestionData()));
        }

        problemBankCollection.insert(insertObject);
        return insertObject.get(SELF_ID).toString();
    }

    /**
     * Gets a mongo bank problem (this is usually grabbed through a course id instead of a specific user unless the user is the admin).
     *
     * @param authenticator
     *         The object that is authenticating the user.
     * @param dbs
     *         the database where the problem is stored.
     * @param authId
     *         the id of the user (typically a course unless they are an admin)
     * @param problemBankId
     *         the id of the problem that is being grabbed.
     * @return the SrlBank problem data if it past all tests.
     * @throws AuthenticationException
     *         thrown if the user does not have access to the permissions.
     * @throws DatabaseAccessException
     *         thrown if there is a problem finding the bank problem in the database.
     */
    public static SrlBankProblem mongoGetBankProblem(final Authenticator authenticator, final DB dbs, final String authId, final String problemBankId)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection bankProblemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final DBObject mongoBankProblem = bankProblemCollection.findOne(convertStringToObjectId(problemBankId));
        if (mongoBankProblem == null) {
            throw new DatabaseAccessException("bank problem can not be found with id: " + problemBankId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.BANK_PROBLEM, problemBankId, authId, 0, authType);

        // if registration is not required for bank problem any course can use it!
        if (!responder.hasStudentPermission() && responder.isRegistrationRequired()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        return extractBankProblem(mongoBankProblem, problemBankId, responder.hasTeacherPermission());

    }

    /**
     * Creates an SrlBankProblem out of the database object.
     *
     * @param dbObject
     *         a pointer to an object in the mongo database.
     * @param problemBankId
     *         The id of problem bank
     * @param isAdmin
     *         true if the user is an admin
     * @return {@link protobuf.srl.school.Problem.SrlBankProblem}.
     */
    private static SrlBankProblem extractBankProblem(final DBObject dbObject, final String problemBankId, final boolean isAdmin) {

        final SrlBankProblem.Builder exactProblem = SrlBankProblem.newBuilder();

        exactProblem.setId(problemBankId);
        exactProblem.setQuestionText((String) dbObject.get(QUESTION_TEXT));
        if (isAdmin) {
            exactProblem.setSolutionId((String) dbObject.get(SOLUTION_ID));
        }
        exactProblem.setCourseTopic((String) dbObject.get(COURSE_TOPIC));
        exactProblem.setSubTopic((String) dbObject.get(SUB_TOPIC));
        exactProblem.setSource((String) dbObject.get(SOURCE));
        exactProblem.setQuestionType(Util.QuestionType.valueOf((Integer) dbObject.get(QUESTION_TYPE)));
        try {
            if (dbObject.containsField(DatabaseStringConstants.SPECIAL_QUESTION_DATA)) {
                exactProblem.setSpecialQuestionData(
                        SlideManager.createElementFromQuery((DBObject) dbObject.get(DatabaseStringConstants.SPECIAL_QUESTION_DATA)));
            }
        } catch (DatabaseAccessException e) {
            LOG.error("Error parsing lecture element", e);
            e.printStackTrace();
        }
        exactProblem.addAllOtherKeywords((ArrayList) dbObject.get(KEYWORDS)); // change
        if (dbObject.get(SCRIPT) != null) {
            exactProblem.setScript((String) dbObject.get(SCRIPT));
        }
        return exactProblem.build();
    }

    /**
     * Updates a bank problem.
     *
     * @param authenticator
     *         the object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param authId
     *         the user updating the bank problem.
     * @param problemBankId
     *         the id of the problem getting updated.
     * @param problem
     *         the bank problem data that is being updated.
     * @return true if the update is successful
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update the bank problem.
     * @throws DatabaseAccessException
     *         Thrown if there is an issue updating the problem.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity",
            "PMD.NPathComplexity", "PMD.AvoidDeeplyNestedIfStmts" })
    public static boolean mongoUpdateBankProblem(final Authenticator authenticator, final DB dbs, final String authId, final String problemBankId,
            final SrlBankProblem problem) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBCollection bankProblemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final DBObject cursor = bankProblemCollection.findOne(convertStringToObjectId(problemBankId));

        if (cursor == null) {
            throw new DatabaseAccessException("Bank Problem was not found with the following ID: " + problemBankId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.BANK_PROBLEM, problemBankId, authId, 0, authType);

        if (!responder.hasTeacherPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        final BasicDBObject updateObj = new BasicDBObject();
        if (problem.hasQuestionText()) {
            updateObj.append(QUESTION_TEXT, problem.getQuestionText());
            update = true;
        }

        // Optimization: have something to do with pulling values of an
        // array and pushing values to an array
        if (problem.hasSolutionId()) {
            updateObj.append(SOLUTION_ID, problem.getSolutionId());
            update = true;
        }
        if (problem.hasCourseTopic()) {
            updateObj.append(COURSE_TOPIC, problem.getCourseTopic());
            update = true;
        }
        if (problem.hasSubTopic()) {
            updateObj.append(SUB_TOPIC, problem.getSubTopic());
            update = true;
        }
        if (problem.hasSource()) {
            updateObj.append(SOURCE, problem.getSource());
            update = true;
        }
        if (problem.hasQuestionType()) {
            updateObj.append(QUESTION_TYPE, problem.getQuestionType().getNumber());
            update = true;
        }
        if (problem.hasScript()) {
            updateObj.append(SCRIPT, problem.getScript());
            update = true;
        }
        if (problem.hasSpecialQuestionData()) {
            updateObj.append(DatabaseStringConstants.SPECIAL_QUESTION_DATA, SlideManager.createQueryFromElement(problem.getSpecialQuestionData()));
            update = true;
        }
        if (problem.getOtherKeywordsCount() > 0) {
            updateObj.append(KEYWORDS, problem.getOtherKeywordsList());
            update = true;
        }
        // Optimization: have something to do with pulling values of an
        // array and pushing values to an array

        if (update) {
            bankProblemCollection.update(cursor, new BasicDBObject(SET_COMMAND, updateObj));
        }
        return true;
    }

    /**
     * Returns all bank problems.  The user must be an instructor of a course.
     *
     * @param authenticator
     *         the object that is performing authentication.
     * @param database
     *         The database where the assignment is being stored.
     * @param authId
     *         the user asking for the bank problems.
     * @param courseId
     *         The course the user is wanting to possibly be associated with the bank problem.
     * @param page
     *         the bank problems are limited to ensure that the database is not overwhelmed.
     * @return a list of {@link protobuf.srl.school.Problem.SrlBankProblem}.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to retrieve any bank problems.
     * @throws DatabaseAccessException
     *         Thrown if there are fields missing that make the problem inaccessible.
     */
    public static List<SrlBankProblem> mongoGetAllBankProblems(final Authenticator authenticator, final DB database, final String authId,
            final String courseId, final int page) throws AuthenticationException, DatabaseAccessException {
        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE, courseId, authId, 0, authType);
        if (!responder.hasTeacherPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final DBCollection bankProblemCollection = database.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final DBCursor dbCursor = bankProblemCollection.find().limit(PAGE_LENGTH).skip(page * PAGE_LENGTH);

        final List<SrlBankProblem> results = new ArrayList<>();
        while (dbCursor.hasNext()) {
            final DBObject dbObject = dbCursor.next();
            // no one is an admin when getting problems in this way.
            results.add(extractBankProblem(dbObject, dbObject.get(SELF_ID).toString(), false));
        }
        return results;
    }

    /**
     * Returns the registration key of the given bank problem if the constraints are met, null is returned in all other cases.
     *
     * @param authenticator
     *         Used to ensure the user has access to the registration key.
     * @param database
     *         The database that contains the registration key.
     * @param authId
     *         The user wanting to view the registration key.
     * @param bankProblemId
     *         The id of the bank problem that contains the registration key.
     * @return The registration key of the given course if the constraints are met, null is returned in all other cases.
     * @throws AuthenticationException
     *         Thrown if there are problems checking the users authentication.
     * @throws DatabaseAccessException
     *         Thrown if the bank problem does not exist.
     */
    @SuppressWarnings("PMD.UselessParentheses")
    public static String mongoGetRegistrationKey(final Authenticator authenticator, final DB database,
            final String authId, final String bankProblemId)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection bankProblemCollection = database.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final DBObject cursor = bankProblemCollection.findOne(convertStringToObjectId(bankProblemId));
        if (cursor == null) {
            throw new DatabaseAccessException("BankProblem was not found with the following ID " + bankProblemId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckIsRegistrationRequired(true)
                .setCheckingAdmin(true)
                .setCheckIsPublished(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.BANK_PROBLEM, bankProblemId.trim(), authId, 0, authType);

        if ((!responder.isRegistrationRequired() && responder.isItemPublished()) || responder.hasTeacherPermission()) {
            return (String) cursor.get(DatabaseStringConstants.REGISTRATION_KEY);
        }
        return null;
    }
}
