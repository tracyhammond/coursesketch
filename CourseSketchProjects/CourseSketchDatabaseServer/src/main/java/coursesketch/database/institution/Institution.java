package coursesketch.database.institution;

import com.google.protobuf.ByteString;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.submission.SubmissionManagerInterface;
import coursesketch.server.interfaces.MultiConnectionManager;
import coursesketch.database.util.DatabaseAccessException;
import protobuf.srl.grading.Grading.ProtoGrade;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;
import protobuf.srl.school.Problem.LectureSlide;
import protobuf.srl.request.Message;
import protobuf.srl.school.Assignment.SrlAssignment;
import protobuf.srl.school.Problem.SrlBankProblem;
import protobuf.srl.school.Problem.SrlProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.services.identity.Identity;
import protobuf.srl.submission.Submission;

import java.util.List;

/**
 * A wrapper around the database that contains institution data.
 *
 * @author gigemjt
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface Institution {

    /**
     * Sets up any indexes that need to be set up or have not yet been set up.
     */
    void setUpIndexes();

    /**
     * @param authId The user requesting these courses.
     * @param courseIds A list of ids for a specific course
     * @return A list of courses given a list of Ids for the courses.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException Thrown if the user if the user is trying to access something non-existant.
     */
    List<SrlCourse> getCourses(String authId, List<String> courseIds) throws AuthenticationException, DatabaseAccessException;

    /**
     * @param authId The user requesting these courses.
     * @param problemID A list of ids for a specific course problem.
     * @return A list of course problems given a list of Ids for the course problems.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException
     *         Thrown if the data does not exist.
     */
    List<SrlProblem> getCourseProblem(String authId, List<String> problemID) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param authId The user requesting these courses.
     * @param assignmentID A list of ids for a specific assignment.
     * @return A list of assignments given a list of Ids for the assignments.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException
     *         Thrown if the data does not exist.
     */
    List<SrlAssignment> getAssignment(String authId, List<String> assignmentID) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param authId The user requesting these courses.
     * @param lectureId A list of ids for a specific lecture.
     * @return A list of lectures given a list of Ids for the lectures.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException
     *         Thrown if the data does not exist.
     */
    List<SrlAssignment> getLecture(String authId, List<String> lectureId) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param authId The user requesting these courses.
     * @param lectureSlideId A list of ids for a specific lecture slide.
     * @return A list of lecture slides given a list of Ids for the lecture slides.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException
     *         Thrown if the data does not exist.
     */
    List<LectureSlide> getLectureSlide(String authId, List<String> lectureSlideId) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param authId The user requesting these courses.
     * @param problemID A list of ids for a specific bank problem.
     * @return A list of course problems given a list of Ids for the course problems.
     * @throws AuthenticationException Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException Thrown if there are problems getting the problems.
     */
    List<SrlBankProblem> getProblem(String authId, List<String> problemID) throws AuthenticationException, DatabaseAccessException;

    /**
     * @return A list of courses that are public (used when registering problems)
     */
    List<SrlCourse> getAllPublicCourses();

    /**
     * Inserts a {@link SrlCourse} into the the database.
     *
     * Upon insertion 3 steps happen:
     * <ol>
     * <li>a default usergroup is created for this course for users, mods, and
     * admins</li>
     * <li>the course is created in the course collection</li>
     * <li>the course contains a reference to the Id of the userGroup and has
     * the groups in its access permission list</li>
     * </ol>
     *
     * @param userId
     *            The user that is inserting the course.  Used for identification purposes.
     * @param authId
     *            The credentials used to authenticate the insertion.  All users can create a course.
     * @param course
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws DatabaseAccessException
     *         Thrown if the course is not able to be inserted.
     */
    String insertCourse(final String userId, String authId, SrlCourse course) throws DatabaseAccessException;

    /**
     * Inserts the assignment into the the database.
     *
     * Upon insertion 3 steps happen:
     * <ol>
     * <li>the assignment is created in an assignment collection</li>
     * <li>the course assignment list now contains the assignment Id</li>
     * <li>the assignment has the same default permissions as the parent course</li>
     * </ol>
     *
     * @param userId
     *            The user that is inserting the assignment.  Used for identification purposes.
     * @param authId
     *            The credentials used to authenticate the insertion
     * @param assignment
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    String insertAssignment(final String userId, String authId, SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts the lecture into the the database.
     *
     * Upon insertion 3 steps happen:
     * <ol>
     * <li>the lecture is created in a lecture collection</li>
     * <li>the course lecture list now contains the lecture Id</li>
     * <li>the lecture has the same default permissions as the parent course</li>
     * </ol>
     *
     * @param userId
     *            The user that is inserting the lecture.  Used for identification purposes.
     * @param authId
     *            The credentials used to authenticate the insertion
     * @param lecture
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    String insertLecture(final String userId, String authId, SrlAssignment lecture) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts the lecture slide into the the database.
     *
     * Upon insertion 3 steps happen:
     * <ol>
     * <li>the lecture slide is created in a lecture slide collection</li>
     * <li>the lecture slide list now contains the lecture Id</li>
     * </ol>
     *
     * @param authId
     *            The credentials used to authenticate the insertion
     * @param lectureSlide
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    String insertLectureSlide(String authId, LectureSlide lectureSlide) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts the assignment into the the database.
     *
     * Upon insertion 3 steps happen:
     * <ol>
     * <li>the assignment is created in a problem collection</li>
     * <li>the assignment problems list now contains the problem Id</li>
     * <li>the problem has the same default permissions as the parent assignment
     * </li>
     * </ol>
     *
     * @param userId
     *            The user that is inserting the course problem.  Used for identification purposes.
     * @param authId
     *            The credentials used to authenticate the insertion
     * @param problem
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert a
     *         Course Problem.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    String insertCourseProblem(final String userId, String authId, SrlProblem problem) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts the {@link SrlBankProblem} into the the database.
     *
     * Upon insertion a bank problem is created within the problem bank.
     *
     * @param userId
     *            The user that is inserting bank problem.  Used for identification purposes.
     * @param authId
     *            The credentials used to authenticate the insertion
     * @param problem
     *         The object being inserted
     * @return The Id of the object that was inserted
     *
     * @throws AuthenticationException if the user does not have permission to insert this bank problem.
     * @throws DatabaseAccessException
     *             Thrown if there is a problem inserting the bank problem.
     */
    String insertBankProblem(final String userId, String authId, SrlBankProblem problem) throws AuthenticationException, DatabaseAccessException;

    /**
     * Updates an existing lecture in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>the lecture is updated in a lecture collection</li>
     * </ol>
     *
     * @param authId
     *            The credentials used to authenticate the update
     * @param lecture
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    void updateLecture(String authId, SrlAssignment lecture) throws AuthenticationException, DatabaseAccessException;

    /**
     * Updates an existing course in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>The course is updated in a course collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param authId
     *            The credentials used to authenticate the update
     * @param course
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update a
     *         Course.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem updating the course.
     */
    void updateCourse(final String authId, final SrlCourse course) throws AuthenticationException, DatabaseAccessException;

    /**
     * Updates an existing assignment in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>the assignment is updated in a assignment collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param authId
     *            The credentials used to authenticate the update
     * @param assignment
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem updating the assignment.
     */
    void updateAssignment(final String authId, final SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException;

    /**
     * Updates an existing courseProblem in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>the courseProblem is updated in a courseProblem collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param authId
     *            The credentials used to authenticate the update
     * @param problem
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update a
     *         Courseproblem.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem updating the courseproblem.
     */
    void updateCourseProblem(final String authId, final SrlProblem problem) throws AuthenticationException, DatabaseAccessException;

    /**
     * Updates an existing bankProblem in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>the bankProblem is updated in a bankProblem collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param authId
     *            The credentials used to authenticate the update
     * @param problem
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update a
     *         bankProblem.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem updating the bankProblem.
     */
    void updateBankProblem(final String authId, final SrlBankProblem problem) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts the lecture into the the database.
     *
     * Upon insertion 1 step happen:
     * <ol>
     * <li>the lecture slide is updated in a slide collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param authId
     *            The credentials used to authenticate the update
     * @param lectureSlide
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert a
     *         Lecture.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the lecture.
     */
    void updateLectureSlide(String authId, LectureSlide lectureSlide) throws AuthenticationException, DatabaseAccessException;

    /**
     * Registers a user for a course
     *
     * Upon registration 2 steps happen:
     * <ol>
     * <li>The user is added to the user permission list.</li>
     * <li>The user now has the course in its list of courses.</li>
     * </ol>
     *
     * @param userId
     *            The id of the user that is being added to the course.  Used for identification purposes.
     * @param authId
     *            The credentials user to be put into the course.
     * @param courseId
     *            The course that the user is being inserted into
     * @param registrationKey
     *            Used to ensure that the user has permission to be added to the course.
     * @return The Id of the object that was inserted
     * @throws DatabaseAccessException
     *             Thrown if there is data missing or the registration was not successful.
     * @throws AuthenticationException
     *             Thrown if the user does not have permission to be inserted into the course.
     */
    boolean putUserInCourse(final String userId, String authId, String courseId, String registrationKey)
            throws DatabaseAccessException, AuthenticationException;

    /**
     * Registers a course for a bank problem
     *
     * Upon registration 2 steps happen:
     * <ol>
     * <li>The registration key is checked to ensure that the the registration is valid.</li>
     * <li>The course is added to the bank problem user permission list.</li>
     * </ol>
     *
     * @param authId
     *            The credentials of the user trying to put the course into the bank problem
     * @param courseId
     *            The credentials course to be put into the bank problem.
     * @param bankProblemId
     *            The bankproblem that the course is being inserted into
     * @param clientRegistrationKey
     *            Used to ensure that the course has permission to be added to the bankproblem.
     * @return {@code true} if the registration was successful.
     * @throws DatabaseAccessException
     *             Thrown if there is data missing or the registration was not successful.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to be inserted into the course.
     */
    boolean putCourseInBankProblem(String authId, String courseId, String bankProblemId, String clientRegistrationKey)
            throws DatabaseAccessException, AuthenticationException;

    /**
     * Gets all of the courses of a specific user.
     *
     * @param authId The user asking for their courses.
     * @return A list of all courses for a specific user.
     * @throws AuthenticationException
     *         Thrown if the user does not have authentication to some of the courses.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem accessing the course.
     */
    List<SrlCourse> getUserCourses(String authId) throws AuthenticationException, DatabaseAccessException;

    /**
     * A message sent from the submission server that submits the submission information into the database.
     *
     * @param userId The user that the submission is associated.
     * @param authId The id that signifies the permissions of the user the submission is associated with.
     * @param problemId The bank problem that is related
     * @param problemPartIndex The id for a specific part of a problem.
     * @param submissionId The submission that is being inserted.
     * @param isExperiment True if the submission is an experiment.
     * @throws DatabaseAccessException Thrown if there is an issue accessing data.
     */
    void insertSubmission(final String userId, String authId, String problemId, String problemPartIndex, String submissionId,
            boolean isExperiment)
            throws DatabaseAccessException;

    /**
     * Calls the submission server for a specific experiment from a specific user.
     *
     * @param userId User requesting the experiment.
     * @param authId The authentication of the user requesting the experiment.
     * @param identifierList The list of ids that identify a set of submissions.
     * @param submissionManager The connection manager to other servers.
     * @throws DatabaseAccessException Thrown if there is an issue accessing data.
     * @throws AuthenticationException Thrown if the user does not have authentication to the experiment.
     * @return An {@link protobuf.srl.submission.Submission.SrlExperiment} for the experiment given by the info and the problemId.
     */
    Submission.SrlExperiment getExperimentAsUser(final String userId, String authId,
            List<String> identifierList, SubmissionManagerInterface submissionManager)
            throws DatabaseAccessException, AuthenticationException;

    /**
     * Calls the submission server for a specific experiment from a specific user.
     *
     * @param userId User requesting the experiment.
     * @param authId The authentication of the user requesting the experiment.
     * @param identifierList The list of ids that identify a set of submissions.
     * @param submissionManager The connection manager to other servers.
     * @throws DatabaseAccessException Thrown if there is an issue accessing data.
     * @throws AuthenticationException Thrown if the user does not have authentication to the experiment.
     * @return An {@link protobuf.srl.submission.Submission.SrlExperiment} for the experiment given by the info and the problemId.
     */
    Submission.SrlSolution getSolution(String userId, String authId, List<String> identifierList,
            SubmissionManagerInterface submissionManager) throws DatabaseAccessException, AuthenticationException;

    /**
     * Calls the submission server for a list of experiments based on user ids.
     *
     * @param authId Permissions of the user requesting the experiment.
     * @param identifier The list of ids that identify a set of submissions.
     * @param sessionInfo The session information of this query.
     * @param internalConnections The connection manager to other servers.
     * @param review Data about review the sketch.
     * @throws DatabaseAccessException Thrown if there is an issue accessing data.
     * @throws AuthenticationException Thrown if the instructor does not have authentication to the experiments.
     * @return The list of experiments grabbed by the instructor.
     */
    List<Submission.SrlExperiment> getExperimentAsInstructor(String authId, List<String> identifier, Message.Request sessionInfo,
            MultiConnectionManager internalConnections, ByteString review) throws DatabaseAccessException, AuthenticationException;

    /**
     * This method will set or insert the gradingPolicy in Mongo based on the proto object passed in.
     * As of now, it is up to the implementation to check if gradingPolicies are valid (ex: add to 100%) before calling this method.
     *
     * @param authId
     *         The id of the user asking for the state.
     * @param policy
     *         Proto object containing the gradingPolicy to be set or updated.
     * @throws DatabaseAccessException
     *         Thrown if connecting to database causes an error.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the course.
     */
    void upsertGradingPolicy(final String authId, final ProtoGradingPolicy policy) throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets the grading policy for a course from the mongoDb.
     *
     * @param authId
     *         The id of the user asking for the state.
     * @param courseId
     *         The gradingPolicy we will get is from this course.
     * @return The protoObject representing the gradingPolicy.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the course.
     * @throws DatabaseAccessException
     *         Thrown if connecting to database causes an error.
     */
    ProtoGradingPolicy getGradingPolicy(final String authId, final String courseId) throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets all bank problems in the database by a page.
     *
     * @param authId The user who is requesting all bank problems
     * @param courseId Must be admin of the course.
     * @param page The page number.
     * @return A list of all bank problems.
     * @throws DatabaseAccessException Thrown if there is an issue accessing data.
     * @throws AuthenticationException Thrown if the instructor does not have authentication to the experiments.
     */
      List<SrlBankProblem> getAllBankProblems(String authId, String courseId, int page) throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets all grades for a certain course. Sorted in ascending order by assignmentId and then userId.
     * This does not mean the list will be in chronological or alphabetical order.
     *
     * @param authId
     *         The user that is requesting the grades. Only users with admin access can get all grades.
     * @param courseId
     *         The course that the grades are being retrieved for.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     *         More sorting should be done by whoever implements this method.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    List<ProtoGrade> getAllAssignmentGradesInstructor(final String authId, final String courseId)
            throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets all grades for a certain student in a certain course. Sorted in ascending order by assignmentId and then authId.
     * This does not mean the list will be in chronological or alphabetical order.
     *
     * @param userId
     *         The user that is requesting the grades (used for identification purposes).
     * @param authId
     *         The id used to authenticate the student getting the grade.
     * @param courseId
     *         The course that the grades are being retrieved for.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     *         More sorting should be done by whoever implements this method.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    List<ProtoGrade> getAllAssignmentGradesStudent(final String userId, final String authId, final String courseId)
            throws AuthenticationException, DatabaseAccessException;

    /**
     * Adds the specified grade if it does not exist. If it does exist, updates the grade value in the database.
     * The code block is an example of what happens when a new problem grade is added.
     * <pre><code>
     * coll.update(
     *  { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId },
     *  {   $push: { gradeHistory: { $each: [gradeToInsertDocument], $sort: { GRADED_DATE: -1 }}}
     *      $set: { CURRENT_GRADE: currentGrade }
     *      $setOnInsert: { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId }
     *  },
     *  { upsert: true }
     * )
     * </code></pre>
     * @param authId
     *         The id used to authenticate the user adding the grade to ensure correct permissions.
     * @param grade
     *         The ProtoObject representing the grade to be added.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to add the grade.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    void addGrade(final String authId, final ProtoGrade grade) throws AuthenticationException, DatabaseAccessException;

    /**
     * Adds the specified grade if it does not exist. If it does exist, updates the grade value in the database.
     * The code block is an example of what happens when a new problem grade is added.
     * <pre><code>
     * coll.update(
     *  { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId },
     *  {   $push: { gradeHistory: { $each: [gradeToInsertDocument], $sort: { GRADED_DATE: -1 }}}
     *      $set: { CURRENT_GRADE: currentGrade }
     *      $setOnInsert: { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId }
     *  },
     *  { upsert: true }
     * )
     * </code></pre>
     *
     * @param authRequest
     * @param grade
     *         The ProtoObject representing the grade to be added.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to add the grade.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    void addGrade(Authentication.AuthRequest authRequest, String authId, final ProtoGrade grade) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * Finds a single grade for a student in a course. If fields are not required in the search, pass in null.
     * For example, if looking for a particular assignment grade, pass in null for the problemId parameter.
     * If looking for a specific problem grade, you must pass in the assignmentId as well as the problemId.
     *
     *
     * @param userId
     *         The id of the user requesting the grade. This is required.
     * @param authId
     *         The id used to authenticate the user getting the grade to ensure correct permissions.
     * @param gradeData
     *         Grade data that contains information about the grade that is wanted (courseId, userId, assignmentId, problemId).
     * @return ProtoGrade object representing the grade requested.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if a grade is not found in the database matching the requested parameters.
     */
    ProtoGrade getGrade(final String userId, final String authId, final ProtoGrade gradeData)
            throws AuthenticationException, DatabaseAccessException;

    /**
     * @param authId
     *         The id used to authenticate the user getting the course roster.
     * @param courseId
     *         The id of what courseRoster is being grabbed
     * @return a list of users in the course
     * @throws DatabaseAccessException
     *         Thrown if there are problems accessing the database.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the course.
     */
    Identity.UserNameResponse getCourseRoster(final String authId, final String courseId)
            throws DatabaseAccessException, AuthenticationException;

    /**
     * Gets the username for the userId.
     *
     * @param userId The userId that is being exchanged for the userName.
     * @param authId The authentication of the user that is exchanging the userId.
     * @param courseId The course the username is being asked for.
     * @return The username if the permissions are successful.
     * @throws DatabaseAccessException Thrown if the user does not exist or there are problems getting the username.
     * @throws AuthenticationException Thrown if the user does not have the authentication to get the username.
     */
    String getUserNameForIdentity(final String userId, final String authId, final String courseId)
            throws DatabaseAccessException, AuthenticationException;
}
