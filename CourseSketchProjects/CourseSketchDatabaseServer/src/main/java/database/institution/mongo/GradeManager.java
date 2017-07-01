package database.institution.mongo;

import com.google.common.base.Strings;
import com.mongodb.BasicDBList;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.server.authentication.HashManager;
import database.DatabaseAccessException;
import database.RequestConverter;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.grading.Grading.GradeHistory;
import protobuf.srl.grading.Grading.ProtoGrade;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ADD_SET_COMMAND;
import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COMMENT;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.COURSE_PROBLEM_ID;
import static database.DatabaseStringConstants.CURRENT_GRADE;
import static database.DatabaseStringConstants.EXISTS;
import static database.DatabaseStringConstants.EXTERNAL_GRADE;
import static database.DatabaseStringConstants.GRADED_DATE;
import static database.DatabaseStringConstants.GRADE_COLLECTION;
import static database.DatabaseStringConstants.GRADE_HISTORY;
import static database.DatabaseStringConstants.GRADE_VALUE;
import static database.DatabaseStringConstants.USER_ID;
import static database.DatabaseStringConstants.WHO_CHANGED;

/**
 * Interfaces with mongo database to manage grades. Used for gradebook functionality mostly.
 *
 * In the mongo database, a grade has the following structure.
 * <pre>
 * Grade
 *  {
 *      courseId: ID,
 *      userId: ID,
 *      assignmentId: ID,
 *      problemId: ID,
 *      currentGrade: float,
 *      gradeHistory: [
 *          {
 *              gradeValue: float,
 *              comment: String,
 *              gradedDate: Date,
 *              whoChanged: ID      // ID of the user who changed the grade
 *          },
 *          {
 *              More of the same
 *          }
 *      ]
 *      externalGrade: bool // True if the currentGrade was not generated by auto grading
 *  }
 *  </pre>
 *
 * Created by Matt on 3/29/2015.
 */
public final class GradeManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GradeManager.class);

    /**
     * Private constructor.
     */
    private GradeManager() {
    }

    /**
     * Adds the specified grade if it does not exist. If it does exist, updates the grade value in the database.
     * The code block is an example of what happens when a new problem grade is added.
     * <pre><code>
     * coll.update(
     *  { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId },
     *  {   $addToSet: { gradeHistory: { $each: [gradeToInsertDBObject], $sort: { GRADED_DATE: -1 }}}
     *      $set: { CURRENT_GRADE: currentGrade }
     *      $setOnInsert: { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId }
     *  },
     *  { upsert: true }
     * )
     * </code></pre>
     *
     * @param authenticator The object that is performing authentication.
     * @param dbs The database that the grade is being added to.
     * @param authId The id used to authenticate the user adding the grade to ensure the user has valid permission.
     * @param grade The ProtoObject representing the grade to be added.
     * Assumptions:
     * The userId is hashed
     * @throws AuthenticationException Thrown if the user did not have the authentication to add the grade.
     * @throws DatabaseAccessException Thrown if grades are not found in the database.
     * Package-private
     */
    static void addGrade(final Authenticator authenticator, final MongoDatabase dbs, final String authId, final ProtoGrade grade)
            throws AuthenticationException, DatabaseAccessException {
        // Check authentication so only teachers of the course can add grades
        final Authentication.AuthType.Builder auth = Authentication.AuthType.newBuilder();
        auth.setCheckingAdmin(true);
        final AuthenticationResponder responder = checkUserExistsForGrade(authenticator, authId, auth.build(), grade);
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("User does not have permission to change this grade.", AuthenticationException.INVALID_PERMISSION);
        }

        final MongoCollection<Document> gradeCollection = dbs.getCollection(GRADE_COLLECTION);

        final Document query = new Document(COURSE_ID, grade.getCourseId())
                .append(USER_ID, HashManager.toHex(grade.getUserId()));
        final Document setOnInsertFields = new Document(query); // Initially setOnInsertFields and query are the same.

        // If the protoGrade has an assignmentId, add it to the query and setOnInsertFields.
        // Else we are looking for a course grade probably which doesn't have an assignmentId.
        if (grade.hasAssignmentId()) {
            query.append(ASSIGNMENT_ID, grade.getAssignmentId());
            setOnInsertFields.append(ASSIGNMENT_ID, grade.getAssignmentId());
        } else {
            query.append(ASSIGNMENT_ID, new Document(EXISTS, false));
        }

        // If the protoGrade has a problemId, add it to the query and setOnInsertFields.
        // Else we are looking for something that isn't a problem grade so it does not have a problemId.
        if (grade.hasProblemId()) {
            query.append(COURSE_PROBLEM_ID, grade.getProblemId());
            setOnInsertFields.append(COURSE_PROBLEM_ID, grade.getProblemId());
        } else {
            query.append(COURSE_PROBLEM_ID, new Document(EXISTS, false));
        }

        if (grade.hasExternalGrade()) {
            setOnInsertFields.append(EXTERNAL_GRADE, grade.getExternalGrade());
        }

        if (grade.getGradeHistoryCount() <= 0) {
            throw new DatabaseAccessException("A grade history is required to insert a grade.");
        }

        int mostRecentHistoryIndex = -1;
        long mostRecentHistoryTime = -1;
        final BasicDBList gradeHistoryList = new BasicDBList();
        for (int i = 0; i < grade.getGradeHistoryCount(); i++) {
            final Document gradeHistoryObject = buildMongoGradeHistory(grade.getGradeHistory(i));
            gradeHistoryList.add(gradeHistoryObject);

            if ((long) gradeHistoryObject.get(GRADED_DATE) > mostRecentHistoryTime) {
                mostRecentHistoryTime = (long) gradeHistoryObject.get(GRADED_DATE);
                mostRecentHistoryIndex = i;
            }
        }
        final Document updateObject = new Document(ADD_SET_COMMAND, new Document(GRADE_HISTORY,
                new Document("$each", gradeHistoryList).append("$sort", new Document(GRADED_DATE, -1))));

        // Sets currentGrade if the ProtoGrade has the field. Else sets currentGrade base on gradeHistory if the ProtoGrade has a gradeHistory.
        updateObject.append("$set", new Document(CURRENT_GRADE, grade.getGradeHistory(mostRecentHistoryIndex).getGradeValue()));

        updateObject.append("$setOnInsert", setOnInsertFields);

        gradeCollection.updateOne(query, updateObject, new UpdateOptions().upsert(true));

    }

    /**
     * Checks if the user is in the corresponding collection for the grade type.
     * If it is a problem grade, checks problem collection. Assignment grade checks assignment collection.
     * Course grade or external grade checks course collection.
     *
     * @param authenticator The object that is performing authentication.
     * @param authId The id used to authenticate the person checking if the user exist for the specific grade.
     * @param auth The authentication being performed.
     * @param grade The grade object the authentication is being performed on.
     * @return {@link AuthenticationResponder} that contains the authentication of the user.
     * @throws DatabaseAccessException if the user is not in the respective collection for the grade type being checked.
     * @throws AuthenticationException Thrown if there are problems checking the users permission.
     */
    private static AuthenticationResponder checkUserExistsForGrade(final Authenticator authenticator,
            final String authId, final Authentication.AuthType auth, final ProtoGrade grade)
            throws DatabaseAccessException, AuthenticationException {

        String itemId;
        Util.ItemType itemType;
        if (grade.getExternalGrade()) {
            itemId = grade.getCourseId();
            itemType = Util.ItemType.COURSE;
        } else if (grade.hasProblemId()) {
            itemId = grade.getProblemId();
            itemType = Util.ItemType.COURSE_PROBLEM;
        } else if (grade.hasAssignmentId()) {
            itemId = grade.getAssignmentId();
            itemType = Util.ItemType.ASSIGNMENT;
        } else {
            itemId = grade.getCourseId();
            itemType = Util.ItemType.COURSE;
        }
        LOG.debug("Checking grade permission for type: {}", itemType);
        return authenticator.checkAuthentication(itemType, itemId, authId, 0, auth);
    }

    /**
     * Builds a mongo Document for a single grade history value.
     *
     * @param gradeHistory ProtoObject that grade history is being set from.
     * @return Document representing the single grade history value in mongo.
     *
     * Package-private
     */
    static Document buildMongoGradeHistory(final GradeHistory gradeHistory) {
        final Document gradeHistoryDocument = new Document();

        if (gradeHistory.hasGradeValue()) {
            gradeHistoryDocument.append(GRADE_VALUE, gradeHistory.getGradeValue());
        }

        if (gradeHistory.hasComment()) {
            gradeHistoryDocument.append(COMMENT, gradeHistory.getComment());
        }

        if (gradeHistory.hasGradedDate()) {
            gradeHistoryDocument.append(GRADED_DATE, gradeHistory.getGradedDate().getMillisecond());
        }

        if (gradeHistory.hasWhoChanged()) {
            gradeHistoryDocument.append(WHO_CHANGED, gradeHistory.getWhoChanged());
        }

        return gradeHistoryDocument;
    }

    /**
     * Finds a single grade for a student in a course.
     *
     * If fields are not required in the search, pass in null.
     * For example, if looking for a particular assignment grade, pass in null for the problemId parameter.
     * If looking for a specific problem grade, you must pass in the assignmentId as well as the problemId.
     *
     * @param authenticator The object that is performing authentication.
     * @param dbs The database that the grades are being retrieved from.
     * @param authId The id used to authenticate the user getting the grade to ensure the user has valid permission.
     * @param userId The id of the user requesting the grade.
     * This is a check so that users can get their own grade but other users can not get other's grade.
     * @param grade A grade object that contains the data needed to get a grade for similar parameters.
     * @return ProtoGrade object representing the grade requested.
     * @throws AuthenticationException Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException Thrown if a grade is not found in the database matching the requested parameters.
     * Package-private
     */
    static ProtoGrade getGrade(final Authenticator authenticator, final MongoDatabase dbs, final String authId, final String userId,
            final ProtoGrade grade)
            throws AuthenticationException, DatabaseAccessException {
        final Authentication.AuthType.Builder auth = Authentication.AuthType.newBuilder();

        // If requester is the user for the grade, check if they are in the course.
        // If requester is not the user for the grade, check if they are an admin for the course.
        auth.setCheckingAdmin(true);

        final AuthenticationResponder responder = checkUserExistsForGrade(authenticator, authId, auth.build(), grade);
        if (!responder.hasStudentPermission()) {
            throw new AuthenticationException("User does not have permission to see this grade", AuthenticationException.INVALID_PERMISSION);
        }

        String hashedUserId = null;
        try {
            hashedUserId = HashManager.createHash(userId, HashManager.generateUnSecureSalt(grade.getCourseId()));
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Exception trying to hash userid while getting all of the assignment grades for a student", e);
        }

        if (!responder.hasModeratorPermission() && !grade.getUserId().equals(hashedUserId)) {
            throw new AuthenticationException("User does not have permission to see this grade", AuthenticationException.INVALID_PERMISSION);
        }

        final MongoCollection<Document> gradeCollection = dbs.getCollection(GRADE_COLLECTION);
        final Document query = new Document(COURSE_ID, grade.getCourseId())
                .append(USER_ID, HashManager.toHex(grade.getUserId()));

        // Adds to query to look for documents without assignmentId field if assignmentId is not given.
        if (!Strings.isNullOrEmpty(grade.getAssignmentId())) {
            query.append(ASSIGNMENT_ID, grade.getAssignmentId());
        } else {
            query.append(ASSIGNMENT_ID, new Document(EXISTS, false));
        }

        // Adds to query to look for documents without problemId field if problemId is not given.
        if (!Strings.isNullOrEmpty(grade.getProblemId())) {
            query.append(COURSE_PROBLEM_ID, grade.getProblemId());
        } else {
            query.append(COURSE_PROBLEM_ID, new Document(EXISTS, false));
        }

        final MongoCursor<Document> cursor = gradeCollection.find(query).iterator();
        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("Did not find a grade matching those parameters for that student in course: " + grade.getCourseId());
        }

        return buildProtoGrade(cursor.next());
    }

    /**
     * Gets all assignment grades for a certain course.
     *
     * It may not contain grades for all assignments for all users.
     * If an assignment does not have any assignment grades yet, it will not appear in the result.
     * Sorted in ascending order by assignmentId and then userId.
     * This does not mean the list will be in chronological or alphabetical order.
     * The query and sort is shown below.
     *
     * <pre><code>
     *     coll.find( { COURSE_ID: courseId, COURSE_PROBLEM_ID: { "exists", false } } )
     *     .sort( { ASSIGNMENT_ID: 1, USER_ID: 1 } )
     * </code></pre>
     *
     * @param authenticator The object that is performing authentication.
     * @param dbs The database that the grades are being retrieved from.
     * @param courseId The course that the grades are being retrieved for.
     * @param authId The id used to authenticate the user getting the grade to ensure the user has valid permission.  Only admin can get all grades.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     * More sorting should be done by whoever implements this method.
     * @throws AuthenticationException Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException Thrown if grades are not found in the database.
     */
    public static List<ProtoGrade> getAllAssignmentGradesInstructor(final Authenticator authenticator, final MongoDatabase dbs, final String courseId,
            final String authId) throws AuthenticationException, DatabaseAccessException {
        // Check authentication so only teachers of the course can retrieve all grades
        final Authentication.AuthType.Builder auth = Authentication.AuthType.newBuilder();
        auth.setCheckingAdmin(true);
        final AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, courseId, authId, 0, auth.build());
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("User does not have permissions to get grades for this course",
                    AuthenticationException.INVALID_PERMISSION);
        }

        final MongoCollection<Document> gradeCollection = dbs.getCollection(GRADE_COLLECTION);
        final Document query = new Document(COURSE_ID, courseId)
                .append(COURSE_PROBLEM_ID, new Document(EXISTS, false));
        final Document sortMethod = new Document(ASSIGNMENT_ID, 1).append(USER_ID, 1); // Sort by assignmentId then userId
        final MongoCursor<Document> cursor = gradeCollection.find(query).sort(sortMethod).iterator();

        final List<ProtoGrade> grades = new ArrayList<>();

        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("Grades were not found for course with ID " + courseId);
        }

        while (cursor.hasNext()) {
            grades.add(buildProtoGrade(cursor.next()));
        }

        return grades;
    }

    /**
     * Gets all grades for a certain student in a certain course.
     *
     * It may not contain grades for all assignments.
     * If an assignment does not have an assignment grade yet, it will not appear in the result.
     * Sorted in ascending order by assignmentId.
     * This does not mean the list will be in chronological or alphabetical order.
     * The query and sort is shown below.
     *
     * <pre><code>
     *      coll.find( { COURSE_ID: courseId, USER_ID: userId, COURSE_PROBLEM_ID: { "exists", false } } )
     *     .sort( { ASSIGNMENT_ID: 1 } )
     * </code></pre>
     *
     * @param authenticator The object that is performing authentication.
     * @param dbs The database that the grades are being retrieved from.
     * @param courseId The course that the grades are being retrieved for.
     * @param authId The id used to authenticate the user getting the all of the grades
     * @param userId The user that is requesting the grades.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     * More sorting should be done by whoever implements this method.
     * @throws AuthenticationException Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException Thrown if grades are not found in the database.
     */
    public static List<ProtoGrade> getAllAssignmentGradesStudent(final Authenticator authenticator, final MongoDatabase dbs, final String courseId,
            final String authId, final String userId) throws AuthenticationException, DatabaseAccessException {
        // Check authentication to make sure the user is in the course
        final Authentication.AuthType.Builder auth = Authentication.AuthType.newBuilder();
        auth.setCheckingAdmin(true);
        final AuthenticationResponder responder = authenticator.checkAuthentication(Util.ItemType.COURSE, courseId, authId, 0, auth.build());
        if (!responder.hasStudentPermission()) {
            throw new AuthenticationException("User does not have permissions to get grades for this course",
                    AuthenticationException.INVALID_PERMISSION);
        }

        String hashedUserId = null;
        try {
            hashedUserId = HashManager.createHash(userId, HashManager.generateUnSecureSalt(courseId));
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Exception trying to hash userid while getting all of the assignment grades for a student", e);
        }

        final MongoCollection<Document> gradeCollection = dbs.getCollection(GRADE_COLLECTION);
        final Document query = new Document(COURSE_ID, courseId)
                .append(USER_ID, HashManager.toHex(hashedUserId))
                .append(COURSE_PROBLEM_ID, new Document(EXISTS, false));
        final Document sortMethod = new Document(ASSIGNMENT_ID, 1); // Sort by assignmentId
        final MongoCursor<Document> cursor = gradeCollection.find(query).sort(sortMethod).iterator();
        if (!cursor.hasNext()) {
            throw new DatabaseAccessException("Grades were not found for specific user in course " + courseId);
        }

        final List<ProtoGrade> grades = new ArrayList<>();
        while (cursor.hasNext()) {
            grades.add(buildProtoGrade(cursor.next()));
        }

        return grades;
    }

    /**
     * Takes in a grade mongo Document and returns a ProtoObject matching the data in the Document.
     *
     * @param grade Mongo Document representing one assignment grade for one student.
     * @return ProtoObject representing one assignment grade for one student.
     * @throws DatabaseAccessException Thrown if courseId or userId are not found in the Document.
     *
     * Package-private
     */
    static ProtoGrade buildProtoGrade(final Document grade) throws DatabaseAccessException {
        if (!grade.containsKey(COURSE_ID)) {
            throw new DatabaseAccessException("Missing required field: courseId");
        }
        if (!grade.containsKey(USER_ID)) {
            throw new DatabaseAccessException("Missing required field: userId");
        }

        final ProtoGrade.Builder protoGrade = ProtoGrade.newBuilder();
        protoGrade.setCourseId(grade.get(COURSE_ID).toString());
        final String decodedVersion = HashManager.fromHexString(grade.get(USER_ID).toString());
        protoGrade.setUserId(decodedVersion);

        if (grade.containsKey(ASSIGNMENT_ID)) {
            protoGrade.setAssignmentId(grade.get(ASSIGNMENT_ID).toString());
        }

        if (grade.containsKey(COURSE_PROBLEM_ID)) {
            protoGrade.setProblemId(grade.get(COURSE_PROBLEM_ID).toString());
        }

        if (grade.containsKey(CURRENT_GRADE)) {
            // The reason appears that some instances are returing a double and others return a float.
            // So we convert it to a string and parse it.
            protoGrade.setCurrentGrade(Float.parseFloat(grade.get(CURRENT_GRADE).toString()));
        }

        if (grade.containsKey(GRADE_HISTORY)) {
            final List<Document> history = (List<Document>) grade.get(GRADE_HISTORY);
            for (int i = 0; i < history.size(); i++) {
                protoGrade.addGradeHistory(buildProtoGradeHistory(history.get(i)));
            }
        }

        if (grade.containsKey(EXTERNAL_GRADE)) {
            protoGrade.setExternalGrade((boolean) grade.get(EXTERNAL_GRADE));
        }

        return protoGrade.build();
    }

    /**
     * Takes in a gradeHistory mongo Document and returns a ProtoObject matching the data in the Document.
     *
     * @param history Mongo Document representing grade history for one assignment grade for one user.
     * @return ProtoObject representing the grade history for one assignment grade for one user.
     * @throws DatabaseAccessException Thrown if no grade history fields are found.
     *
     * Package-private
     */
    static GradeHistory buildProtoGradeHistory(final Document history) throws DatabaseAccessException {
        final GradeHistory.Builder protoHistory = GradeHistory.newBuilder();
        if (history.containsKey(GRADE_VALUE)) {
            // The reason appears that some instances are returing a double and others return a float.
            // So we convert it to a string and parse it.
            protoHistory.setGradeValue(Float.parseFloat(history.get(GRADE_VALUE).toString()));
        }

        if (history.containsKey(COMMENT)) {
            protoHistory.setComment(history.get(COMMENT).toString());
        }

        if (history.containsKey(GRADED_DATE)) {
            protoHistory.setGradedDate(RequestConverter.getProtoFromMilliseconds(((Number) history.get(GRADED_DATE)).longValue()));
        }

        if (history.containsKey(WHO_CHANGED)) {
            protoHistory.setWhoChanged(history.get(WHO_CHANGED).toString());
        }

        // GetAllFields returns a map of fields that have values in the ProtoObject
        // If the size of the map is 0, then no fields exist
        if (protoHistory.getAllFields().size() == 0) {
            throw new DatabaseAccessException("No fields found for gradeHistory object");
        }

        return protoHistory.build();
    }
}
