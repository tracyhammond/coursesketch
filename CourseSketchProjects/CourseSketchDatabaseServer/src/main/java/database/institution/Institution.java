package database.institution;

import com.google.protobuf.ByteString;
import coursesketch.server.interfaces.MultiConnectionManager;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import protobuf.srl.grading.Grading.ProtoGrade;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;
import protobuf.srl.lecturedata.Lecturedata.Lecture;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;

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
     * @param courseIds
     *         A list of ids for a specific course
     * @param userId
     *         The user requesting these courses.
     * @return A list of courses given a list of Ids for the courses.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException Thrown if the user if the user is trying to access something non-existant.
     */
    List<SrlCourse> getCourses(List<String> courseIds, String userId) throws AuthenticationException, DatabaseAccessException;

    /**
     * @param problemID
     *         A list of ids for a specific course problem.
     * @param userId
     *         The user requesting these courses.
     * @return A list of course problems given a list of Ids for the course problems.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException
     *         Thrown if the data does not exist.
     */
    List<SrlProblem> getCourseProblem(List<String> problemID, String userId) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param assignementID
     *         A list of ids for a specific assignment.
     * @param userId
     *         The user requesting these courses.
     * @return A list of assignments given a list of Ids for the assignments.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException
     *         Thrown if the data does not exist.
     */
    List<SrlAssignment> getAssignment(List<String> assignementID, String userId) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param lectureId
     *         A list of ids for a specific lecture.
     * @param userId
     *         The user requesting these courses.
     * @return A list of lectures given a list of Ids for the lectures.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException
     *         Thrown if the data does not exist.
     */
    List<Lecture> getLecture(List<String> lectureId, String userId) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param lectureSlideId
     *         A list of ids for a specific lecture slide.
     * @param userId
     *         The user requesting these courses.
     * @return A list of lecture slides given a list of Ids for the lecture slides.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException
     *         Thrown if the data does not exist.
     */
    List<LectureSlide> getLectureSlide(List<String> lectureSlideId, String userId) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param problemID
     *         A list of ids for a specific bank problem.
     * @param userId
     *         The user requesting these courses.
     * @return A list of course problems given a list of Ids for the course problems.
     * @throws AuthenticationException
     *         Thrown if the user does not have permissions for the courses requested.
     */
    List<SrlBankProblem> getProblem(List<String> problemID, String userId) throws AuthenticationException;

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
     *         The credentials used to authenticate the insertion.  All users can create a course.
     * @param course
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws DatabaseAccessException
     *         Thrown if the course is not able to be inserted.
     */
    String insertCourse(String userId, SrlCourse course) throws DatabaseAccessException;

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
     *         The credentials used to authenticate the insertion
     * @param assignment
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    String insertAssignment(String userId, SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException;

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
     *         The credentials used to authenticate the insertion
     * @param lecture
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    String insertLecture(String userId, Lecture lecture) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts the lecture slide into the the database.
     *
     * Upon insertion 3 steps happen:
     * <ol>
     * <li>the lecture slide is created in a lecture slide collection</li>
     * <li>the lecture slide list now contains the lecture Id</li>
     * </ol>
     *
     * @param userId
     *         The credentials used to authenticate the insertion
     * @param lectureSlide
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    String insertLectureSlide(String userId, LectureSlide lectureSlide) throws AuthenticationException, DatabaseAccessException;

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
     *         The credentials used to authenticate the insertion
     * @param problem
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert a
     *         Course Problem.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    String insertCourseProblem(String userId, SrlProblem problem) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts the {@link SrlBankProblem} into the the database.
     *
     * Upon insertion a bank problem is created within the problem bank.
     *
     * @param userId
     *         The credentials used to authenticate the insertion
     * @param problem
     *         The object being inserted
     * @return The Id of the object that was inserted
     * @throws AuthenticationException
     *         if the user does not have permission to insert this bank problem.
     */
    String insertBankProblem(String userId, SrlBankProblem problem) throws AuthenticationException;

    /**
     * Updates an existing lecture in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>the lecture is updated in a lecture collection</li>
     * </ol>
     *
     * @param userId
     *         The credentials used to authenticate the update
     * @param lecture
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the assignment.
     */
    void updateLecture(String userId, Lecture lecture) throws AuthenticationException, DatabaseAccessException;

    /**
     * Updates an existing course in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>The course is updated in a course collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param userId
     *         The credentials used to authenticate the update
     * @param course
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update a
     *         Course.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem updating the course.
     */
    void updateCourse(final String userId, final SrlCourse course) throws AuthenticationException, DatabaseAccessException;

    /**
     * Updates an existing assignment in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>the assignment is updated in a assignment collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param userId
     *         The credentials used to authenticate the update
     * @param assignment
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update an
     *         Assignment.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem updating the assignment.
     */
    void updateAssignment(final String userId, final SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException;

    /**
     * Updates an existing courseProblem in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>the courseProblem is updated in a courseProblem collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param userId
     *         The credentials used to authenticate the update
     * @param problem
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update a
     *         Courseproblem.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem updating the courseproblem.
     */
    void updateCourseProblem(final String userId, final SrlProblem problem) throws AuthenticationException, DatabaseAccessException;

    /**
     * Updates an existing bankProblem in the database.
     *
     * Upon updating 1 step happen:
     * <ol>
     * <li>the bankProblem is updated in a bankProblem collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param userId
     *         The credentials used to authenticate the update
     * @param problem
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to update a
     *         bankProblem.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem updating the bankProblem.
     */
    void updateBankProblem(final String userId, final SrlBankProblem problem) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts the lecture into the the database.
     *
     * Upon insertion 1 step happen:
     * <ol>
     * <li>the lecture slide is updated in a slide collection</li>
     * <li>After updating a user update is created.</li>
     * </ol>
     *
     * @param userId
     *         The credentials used to authenticate the update
     * @param lectureSlide
     *         The object being updated
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to insert a
     *         Lecture.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem inserting the lecture.
     */
    void updateLectureSlide(String userId, LectureSlide lectureSlide) throws AuthenticationException, DatabaseAccessException;

    /**
     * Registers a user for a course
     *
     * Upon registration 3 steps happen:
     * <ol>
     * <li>The user is checked to make sure that they already are not enrolled
     * in the course.
     * <li>The user is added to the user permission list.</li>
     * <li>The user now has the course in its list of courses.</li>
     * </ol>
     *
     * @param userId
     *         The credentials user to be put into the course.
     * @param courseId
     *         The course that the user is being inserted into
     * @return The Id of the object that was inserted
     * @throws DatabaseAccessException
     *         Only thrown if the user is already registered for the course.
     * @throws AuthenticationException
     *         Thrown if the user does not have permission to be inserted into the course.
     */
    boolean putUserInCourse(String courseId, String userId) throws DatabaseAccessException, AuthenticationException;

    /**
     * Gets all of the courses of a specific user.
     *
     * @param userId
     *         The user asking for their courses.
     * @return A list of all courses for a specific user.
     * @throws AuthenticationException
     *         Thrown if the user does not have authentication to some of the courses.
     * @throws DatabaseAccessException
     *         Thrown if there is a problem accessing the course.
     */
    List<SrlCourse> getUserCourses(String userId) throws AuthenticationException, DatabaseAccessException;

    /**
     * A message sent from the submission server that allows the insertion of
     * the submission.
     *
     * @param userId
     *         The user that the submission is associated.
     * @param problemId
     *         The bank problem that is related
     * @param submissionId
     *         The submission that is being inserted.
     * @param isExperiment
     *         True if the submission is an experiment.
     * @throws DatabaseAccessException
     *         Thrown if there is an issue accessing data.
     */
    void insertSubmission(String userId, String problemId, String submissionId, boolean isExperiment)
            throws DatabaseAccessException;

    /**
     * Calls the submission server for a specific experiment from a specific user.
     *
     * @param userId
     *         User requesting the experiment.
     * @param problemId
     *         The problemId that the experiment is associated with.
     * @param sessionInfo
     *         The session information of this query.
     * @param internalConnections
     *         The connection manager to other servers.
     * @throws DatabaseAccessException
     *         Thrown if there is an issue accessing data.
     */
    void getExperimentAsUser(String userId, String problemId, String sessionInfo, MultiConnectionManager internalConnections)
            throws DatabaseAccessException;

    /**
     * Calls the submission server for a specific experiment from a specific user.
     *
     * @param userId
     *         User requesting the experiment.
     * @param problemId
     *         The problemId that the experiment is associated with.
     * @param sessionInfo
     *         The session information of this query.
     * @param internalConnections
     *         The connection manager to other servers.
     * @param review
     *         data about review the sketch.
     * @throws DatabaseAccessException
     *         Thrown if there is an issue accessing data.
     * @throws AuthenticationException
     *         Thrown if the instructor does not have authentication to the experiments.
     */
    void getExperimentAsInstructor(String userId, String problemId, String sessionInfo,
            MultiConnectionManager internalConnections, ByteString review) throws DatabaseAccessException, AuthenticationException;

    /**
     * This method will set or insert the gradingPolicy in Mongo based on the proto object passed in.
     * As of now, it is up to the implementation to check if gradingPolicies are valid (ex: add to 100%) before calling this method.
     *
     * @param userId
     *         The id of the user asking for the state.
     * @param policy
     *         Proto object containing the gradingPolicy to be set or updated.
     * @throws DatabaseAccessException
     *         Thrown if connecting to database causes an error.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the course.
     */
    void insertGradingPolicy(final String userId, final ProtoGradingPolicy policy) throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets the grading policy for a course from the mongoDb.
     *
     * @param courseId
     *         The gradingPolicy we will get is from this course.
     * @param userId
     *         The id of the user asking for the state.
     * @return The protoObject representing the gradingPolicy.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the course.
     * @throws DatabaseAccessException
     *         Thrown if connecting to database causes an error.
     */
    ProtoGradingPolicy getGradingPolicy(final String courseId, final String userId) throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets all bank problems in the database by a page.
     * @param userId the user who is requesting all bank problems
     * @param courseId must be admin of the course.
     * @param page The page number.
     * @return A list of all bank problems.
     * @throws DatabaseAccessException Thrown if there is an issue accessing data.
     * @throws AuthenticationException Thrown if the instructor does not have authentication to the experiments.
     */
    List<SrlBankProblem> getAllBankProblems(String userId, String courseId, int page) throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets all grades for a certain course. Sorted in ascending order by assignmentId and then userId.
     * This does not mean the list will be in chronological or alphabetical order.
     *
     * @param courseId
     *         The course that the grades are being retrieved for.
     * @param userId
     *         The user that is requesting the grades. Only users with admin access can get all grades.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     *         More sorting should be done by whoever implements this method.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    List<ProtoGrade> getAllAssignmentGradesInstructor(final String courseId, final String userId)
            throws AuthenticationException, DatabaseAccessException;

    /**
     * Gets all grades for a certain student in a certain course. Sorted in ascending order by assignmentId and then userId.
     * This does not mean the list will be in chronological or alphabetical order.
     *
     * @param courseId
     *         The course that the grades are being retrieved for.
     * @param userId
     *         The user that is requesting the grades.
     * @return The list of ProtoGrades for the course. Each ProtoGrade is an individual assignment grade for an individual student.
     *         More sorting should be done by whoever implements this method.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    List<ProtoGrade> getAllAssignmentGradesStudent(final String courseId, final String userId)
            throws AuthenticationException, DatabaseAccessException;

    /**
     * Adds the specified grade if it does not exist. If it does exist, updates the grade value in the database.
     * The code block is an example of what happens when a new problem grade is added.
     * <pre><code>
     * coll.update(
     *  { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId },
     *  {   $push: { gradeHistory: { $each: [gradeToInsertDBObject], $sort: { GRADED_DATE: -1 }}}
     *      $set: { CURRENT_GRADE: currentGrade }
     *      $setOnInsert: { COURSE_ID: courseId, USER_ID, userId, ASSIGNMENT_ID: assignmentId, PROBLEM_ID: problemId }
     *  },
     *  { upsert: true }
     * )
     * </code></pre>
     * @param adderId
     *         The Id of the person trying to add the grade.
     * @param grade
     *         The ProtoObject representing the grade to be added.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to add the grade.
     * @throws DatabaseAccessException
     *         Thrown if grades are not found in the database.
     */
    void addGrade(final String adderId, final ProtoGrade grade) throws AuthenticationException, DatabaseAccessException;

    /**
     * Finds a single grade for a student in a course. If fields are not required in the search, pass in null.
     * For example, if looking for a particular assignment grade, pass in null for the problemId parameter.
     * If looking for a specific problem grade, you must pass in the assignmentId as well as the problemId.
     *
     * @param requesterId
     *         The id of the user requesting the grade. This is required.
     * @param userId
     *         The id of the user that the grade is for. This is required. This value is at itemId(3).
     * @param courseId
     *         The id of the course that the grade is for. This is required. This value is at itemId(0).
     * @param assignmentId
     *         The id of the assignment that the grade is for. This is optional. This value is at itemId(1).
     * @param problemId
     *         The id of the problem that the grade is for. This is optional. This value is at itemId(2).
     * @return ProtoGrade object representing the grade requested.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the grades.
     * @throws DatabaseAccessException
     *         Thrown if a grade is not found in the database matching the requested parameters.
     */
    ProtoGrade getGrade(final String requesterId, final String userId, final String courseId, final String assignmentId, final String problemId)
            throws AuthenticationException, DatabaseAccessException;
}
