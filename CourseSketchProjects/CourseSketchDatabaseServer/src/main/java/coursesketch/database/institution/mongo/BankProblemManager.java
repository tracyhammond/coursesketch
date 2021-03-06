package coursesketch.database.institution.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import coursesketch.database.util.MongoQuestionDataBuilder;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.question.QuestionDataOuterClass.QuestionData;
import protobuf.srl.school.Problem.SrlBankProblem;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.COURSE_ACCESS;
import static coursesketch.database.util.DatabaseStringConstants.COURSE_TOPIC;
import static coursesketch.database.util.DatabaseStringConstants.DOMAIN_ID;
import static coursesketch.database.util.DatabaseStringConstants.KEYWORDS;
import static coursesketch.database.util.DatabaseStringConstants.QUESTION_TEXT;
import static coursesketch.database.util.DatabaseStringConstants.QUESTION_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.REGISTRATION_KEY;
import static coursesketch.database.util.DatabaseStringConstants.SCRIPT;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.util.DatabaseStringConstants.SET_COMMAND;
import static coursesketch.database.util.DatabaseStringConstants.SOLUTION_ID;
import static coursesketch.database.util.DatabaseStringConstants.SOURCE;
import static coursesketch.database.util.DatabaseStringConstants.STATE_PUBLISHED;
import static coursesketch.database.util.DatabaseStringConstants.SUB_TOPIC;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.MongoUtilities.appendQuestionTypeToDocument;
import static coursesketch.database.util.MongoUtilities.convertStringToObjectId;
import static coursesketch.database.util.MongoUtilities.createDomainIdFromDocument;
import static coursesketch.database.util.MongoUtilities.createDomainIdFromProto;
import static coursesketch.database.util.MongoUtilities.getQuestionType;

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
     * Builds the {@link QuestionData}.
     */
    private static final MongoQuestionDataBuilder QUESTION_DATA_BUILDER = new MongoQuestionDataBuilder();

    /**
     * Private constructor.
     */
    private BankProblemManager() {
    }

    /**
     * Inserts a problem bank into the mongo database.
     *
     * @param dbs     the database into which the bank is being inserted.
     * @param problem the problem data that is being inserted.
     * @return The mongo id of the problem bank.
     * @throws AuthenticationException Not currently thrown but may be thrown in the future.
     */
    static String mongoInsertBankProblem(final MongoDatabase dbs, final SrlBankProblem problem) throws AuthenticationException {
        final MongoCollection<Document> problemBankCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final Document insertObject = new Document(QUESTION_TEXT, problem.getQuestionText())
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

        if (!problem.hasProblemDomain()) {
            // We can at least set the question type.
            insertObject.append(DOMAIN_ID, createDomainIdFromProto(
                    Util.DomainId.newBuilder().setQuestionType(problem.getQuestionType()).build()));
        } else {
            insertObject.append(DOMAIN_ID, createDomainIdFromProto(problem.getProblemDomain()));
        }

        if (problem.hasSpecialQuestionData()) {
            insertObject.append(DatabaseStringConstants.SPECIAL_QUESTION_DATA,
                    createQueryFromElement(problem.getSpecialQuestionData()));
        }

        problemBankCollection.insertOne(insertObject);
        return insertObject.get(SELF_ID).toString();
    }

    /**
     * @param questionData an element that belongs on a problem
     * @return a Document of the element
     */
    private static Document createQueryFromElement(final QuestionData questionData) {
        if (questionData.getElementTypeCase() == QuestionData.ElementTypeCase.ELEMENTTYPE_NOT_SET) {
            return appendQuestionTypeToDocument(questionData.getElementTypeCase(), new Document());
        }
        try {
            return QUESTION_DATA_BUILDER.createSubmission(questionData);
        } catch (DatabaseAccessException exception) {
            LOG.error("Error creating question data {}", exception);
            return appendQuestionTypeToDocument(questionData.getElementTypeCase(), new Document());
        }
    }

    /**
     * Gets a mongo bank problem (this is usually grabbed through a course id instead of a specific user unless the user is the admin).
     *
     * @param authenticator The object that is authenticating the user.
     * @param dbs           the database where the problem is stored.
     * @param authId        the id of the user (typically a course unless they are an admin)
     * @param problemBankId the id of the problem that is being grabbed.
     * @return the SrlBank problem data if it past all tests.
     * @throws AuthenticationException thrown if the user does not have access to the permissions.
     * @throws DatabaseAccessException thrown if there is a problem finding the bank problem in the database.
     */
    static SrlBankProblem mongoGetBankProblem(final Authenticator authenticator, final MongoDatabase dbs, final String authId,
                                              final String problemBankId)
            throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> bankProblemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final Document mongoBankProblem = bankProblemCollection.find(convertStringToObjectId(problemBankId)).first();
        if (mongoBankProblem == null) {
            throw new DatabaseAccessException("bank problem can not be found with id: " + problemBankId);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingAdmin(true)
                .setCheckingOwner(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.BANK_PROBLEM, problemBankId, authId, 0, authType);

        // if registration is not required for bank problem any course can use it!
        if (!responder.hasStudentPermission() && responder.isRegistrationRequired()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        return extractBankProblem(mongoBankProblem, problemBankId);

    }

    /**
     * Creates an SrlBankProblem out of the database object.
     *
     * @param mongoBankProblem a pointer to an object in the mongo database.
     * @param problemBankId    The id of problem bank
     * @return {@link protobuf.srl.school.Problem.SrlBankProblem}.
     */
    private static SrlBankProblem extractBankProblem(final Document mongoBankProblem, final String problemBankId) {

        final SrlBankProblem.Builder exactProblem = SrlBankProblem.newBuilder();

        exactProblem.setId(problemBankId);
        exactProblem.setQuestionText((String) mongoBankProblem.get(QUESTION_TEXT));
        exactProblem.setSolutionId((String) mongoBankProblem.get(SOLUTION_ID));
        exactProblem.setCourseTopic((String) mongoBankProblem.get(COURSE_TOPIC));
        exactProblem.setSubTopic((String) mongoBankProblem.get(SUB_TOPIC));
        exactProblem.setSource((String) mongoBankProblem.get(SOURCE));
        exactProblem.setQuestionType(Util.QuestionType.valueOf((Integer) mongoBankProblem.get(QUESTION_TYPE)));
        exactProblem.setProblemDomain(createDomainIdFromDocument(mongoBankProblem.get(DOMAIN_ID, new Document())));
        try {
            if (mongoBankProblem.containsKey(DatabaseStringConstants.SPECIAL_QUESTION_DATA)) {
                exactProblem.setSpecialQuestionData(
                        createElementFromQuery((Document) mongoBankProblem.get(DatabaseStringConstants.SPECIAL_QUESTION_DATA)));
            }
        } catch (DatabaseAccessException e) {
            LOG.error("Error parsing lecture element", e);
        }
        exactProblem.addAllOtherKeywords(mongoBankProblem.get(KEYWORDS, new ArrayList<>())); // change
        if (mongoBankProblem.get(SCRIPT) != null) {
            exactProblem.setScript((String) mongoBankProblem.get(SCRIPT));
        }
        return exactProblem.build();
    }


    /**
     * NOTE: This function is only used internally and should not be made public.
     *
     * @param query a Document from the mongo database that is a slide
     * @return a Problem.ProblemElement of the Document that was passed in
     * @throws DatabaseAccessException a DatabaseAccessException if something goes wrong parsing a blob of a LectureElement
     */
    static QuestionData createElementFromQuery(final Document query) throws DatabaseAccessException {
        final QuestionData.Builder element = QuestionData.newBuilder();
        final QuestionData.ElementTypeCase type = getQuestionType(query);
        if (type == QuestionData.ElementTypeCase.ELEMENTTYPE_NOT_SET) {
            return element.build();
        }
        return QUESTION_DATA_BUILDER.buildQuestionDataProto(query);
    }

    /**
     * Updates a bank problem.
     *
     * @param authenticator the object that is performing authentication.
     * @param dbs           The database where the assignment is being stored.
     * @param authId        the user updating the bank problem.
     * @param problemBankId the id of the problem getting updated.
     * @param problem       the bank problem data that is being updated.
     * @return true if the update is successful
     * @throws AuthenticationException Thrown if the user does not have permission to update the bank problem.
     * @throws DatabaseAccessException Thrown if there is an issue updating the problem.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity",
            "PMD.NPathComplexity", "PMD.AvoidDeeplyNestedIfStmts" })
    static boolean mongoUpdateBankProblem(final Authenticator authenticator, final MongoDatabase dbs, final String authId,
                                          final String problemBankId,
                                          final SrlBankProblem problem) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final MongoCollection<Document> bankProblemCollection = dbs.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final Document cursor = bankProblemCollection.find(convertStringToObjectId(problemBankId)).first();

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
        final Document updateObj = new Document();
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
            updateObj.append(DatabaseStringConstants.SPECIAL_QUESTION_DATA, createQueryFromElement(problem.getSpecialQuestionData()));
            update = true;
        }
        if (problem.getOtherKeywordsCount() > 0) {
            updateObj.append(KEYWORDS, problem.getOtherKeywordsList());
            update = true;
        }
        // Optimization: have something to do with pulling values of an
        // array and pushing values to an array

        if (update) {
            bankProblemCollection.updateOne(cursor, new Document(SET_COMMAND, updateObj));
        }
        return true;
    }

    /**
     * Returns all bank problems.  The user must be an instructor of a course.
     *
     * @param authenticator The object that is performing authentication.
     * @param database      The database where the assignment is being stored.
     * @param authId        The user asking for the bank problems.
     * @param courseId      The course the user is wanting to possibly be associated with the bank problem.
     * @param page          The bank problems are limited to ensure that the database is not overwhelmed.
     * @return a list of {@link protobuf.srl.school.Problem.SrlBankProblem}.
     * @throws AuthenticationException Thrown if the user does not have permission to retrieve any bank problems.
     * @throws DatabaseAccessException Thrown if there are fields missing that make the problem inaccessible.
     */
    static List<SrlBankProblem> mongoGetAllBankProblems(final Authenticator authenticator, final MongoDatabase database, final String authId,
            final String courseId, final int page) throws AuthenticationException, DatabaseAccessException {
        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE, courseId, authId, 0, authType);
        if (!responder.hasTeacherPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final MongoCollection<Document> bankProblemCollection = database.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final MongoCursor<Document> dbCursor = bankProblemCollection.find().limit(PAGE_LENGTH).skip(page * PAGE_LENGTH).iterator();

        final List<SrlBankProblem> results = new ArrayList<>();
        while (dbCursor.hasNext()) {
            final Document bankProblem = dbCursor.next();
            // no one is an admin when getting problems in this way.
            results.add(extractBankProblem(bankProblem, bankProblem.get(SELF_ID).toString()));
        }
        return results;
    }

    /**
     * Returns the registration key of the given bank problem if the constraints are met, null is returned in all other cases.
     *
     * @param authenticator Used to ensure the user has access to the registration key.
     * @param database      The database that contains the registration key.
     * @param authId        The user wanting to view the registration key.
     * @param bankProblemId The id of the bank problem that contains the registration key.
     * @return The registration key of the given course if the constraints are met, null is returned in all other cases.
     * @throws AuthenticationException Thrown if there are problems checking the users authentication.
     * @throws DatabaseAccessException Thrown if the bank problem does not exist.
     */
    @SuppressWarnings("PMD.UselessParentheses")
    static String mongoGetRegistrationKey(final Authenticator authenticator, final MongoDatabase database,
                                          final String authId, final String bankProblemId)
            throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> bankProblemCollection = database.getCollection(getCollectionFromType(Util.ItemType.BANK_PROBLEM));
        final Document cursor = bankProblemCollection.find(convertStringToObjectId(bankProblemId)).first();
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
            return (String) cursor.get(REGISTRATION_KEY);
        }
        return null;
    }
}
