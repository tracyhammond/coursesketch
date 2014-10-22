package database.institution;

import java.util.List;

import multiconnection.MultiConnectionManager;
import protobuf.srl.request.Message.Request;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;

import com.google.protobuf.ByteString;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;

/**
 * A wrapper around the database that contains institution data.
 * @author gigemjt
 *
 */
public interface Institution {

    /**
     * Sets up any indexes that need to be set up or have not yet been set up.
     */
    void setUpIndexes();

    /**
     * @param courseIds A list of ids for a specific course
     * @param userId The user requesting these courses.
     * @return A list of courses given a list of Ids for the courses.
     * @throws AuthenticationException Thrown if the user does not have permissions for the courses requested.
     */
    List<SrlCourse> getCourses(List<String> courseIds, String userId) throws AuthenticationException;

    /**
     * @param problemID A list of ids for a specific course problem.
     * @param userId The user requesting these courses.
     * @return A list of course problems given a list of Ids for the course problems.
     * @throws AuthenticationException Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException Thrown if the data does not exist.
     */
    List<SrlProblem> getCourseProblem(List<String> problemID, String userId) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param assignementID A list of ids for a specific assignment.
     * @param userId The user requesting these courses.
     * @return A list of assignments given a list of Ids for the assignments.
     * @throws AuthenticationException Thrown if the user does not have permissions for the courses requested.
     * @throws DatabaseAccessException Thrown if the data does not exist.
     */
    List<SrlAssignment> getAssignment(List<String> assignementID, String userId) throws AuthenticationException,
            DatabaseAccessException;

    /**
     * @param problemID A list of ids for a specific bank problem.
     * @param userId The user requesting these courses.
     * @return A list of course problems given a list of Ids for the course problems.
     * @throws AuthenticationException Thrown if the user does not have permissions for the courses requested.
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
     *            The credentials used to authenticate the insertion.  All users can create a course.
     * @param course
     *            The object being inserted
     * @return The Id of the object that was inserted
     * @throws DatabaseAccessException
     *             Thrown if the course is not able to be inserted.
     *
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
     *            The credentials used to authenticate the insertion
     * @param assignment
     *            The object being inserted
     * @throws AuthenticationException
     *             Thrown if the user does not have permission to insert an
     *             Assignment.
     * @throws DatabaseAccessException
     *             Thrown if there is a problem inserting the assignment.
     * @return The Id of the object that was inserted
     */
    String insertAssignment(String userId, SrlAssignment assignment) throws AuthenticationException, DatabaseAccessException;

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
     *            The credentials used to authenticate the insertion
     * @param problem
     *            The object being inserted
     * @throws AuthenticationException
     *             Thrown if the user does not have permission to insert a
     *             Course Problem.
     * @throws DatabaseAccessException
     *             Thrown if there is a problem inserting the assignment.
     * @return The Id of the object that was inserted
     */
    String insertCourseProblem(String userId, SrlProblem problem) throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts the {@link SrlBankProblem} into the the database.
     *
     * Upon insertion a bank problem is created within the problem bank.
     *
     * @param userId
     *            The credentials used to authenticate the insertion
     * @param problem
     *            The object being inserted
     * @return The Id of the object that was inserted
     *
     * @throws AuthenticationException if the user does not have permission to insert this bank problem.
     */
    String insertBankProblem(String userId, SrlBankProblem problem) throws AuthenticationException;

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
     *            The credentials user to be put into the course.
     * @param courseId
     *            The course that the user is being inserted into
     * @return The Id of the object that was inserted
     * @throws DatabaseAccessException
     *             Only thrown if the user is already registered for the course.
     *
     * @throws AuthenticationException
     *             Thrown if the user does not have permission to be inserted into the course.
     */
    boolean putUserInCourse(String courseId, String userId) throws DatabaseAccessException, AuthenticationException;

    /**
     * Gets all of the courses of a specific user.
     * @param userId The user asking for their courses.
     * @return A list of all courses for a specific user.
     * @throws AuthenticationException Thrown if the user does not have authentication to some of the courses.
     * @throws DatabaseAccessException Thrown if there is a problem accessing the course.
     */
    List<SrlCourse> getUserCourses(String userId) throws AuthenticationException, DatabaseAccessException;

    /**
     * A message sent from the submission server that allows the insertion of
     * the message.
     *
     * @param req Submission being inserted.
     * @throws DatabaseAccessException Thrown if there is data missing.
     */
    void insertSubmission(Request req) throws DatabaseAccessException;

    /**
     * A message sent from the submission server that allows the insertion of
     * the submission.
     * @param problemId The bank problem that is related
     * @param userId The user that the submission is associated.
     * @param submissionId The submission that is being inserted.
     * @param experiment True if the submission is an experiment.
     * @throws DatabaseAccessException Thrown if there is an issue accessing data.
     */
    void insertSubmission(String problemId, String userId, String submissionId, boolean experiment)
            throws DatabaseAccessException;

    /**
     * Calls the submission server for a specific experiment from a specific user.
     * @param userId User requesting the experiment.
     * @param problemId The problemId that the experiment is associated with.
     * @param sessionInfo The session information of this query.
     * @param internalConnections The connection manager to other servers.
     * @throws DatabaseAccessException Thrown if there is an issue accessing data.
     */
    void getExperimentAsUser(String userId, String problemId, String sessionInfo, MultiConnectionManager internalConnections)
            throws DatabaseAccessException;

    /**
     * Calls the submission server for a specific experiment from a specific user.
     * @param userId User requesting the experiment.
     * @param problemId The problemId that the experiment is associated with.
     * @param sessionInfo The session information of this query.
     * @param internalConnections The connection manager to other servers.
     * @param review data about review the sketch.
     * @throws DatabaseAccessException Thrown if there is an issue accessing data.
     * @throws AuthenticationException Thrown if the instructor does not have authentication to the experiments.
     */
    void getExperimentAsInstructor(String userId, String problemId, String sessionInfo,
            MultiConnectionManager internalConnections, ByteString review) throws DatabaseAccessException, AuthenticationException;

}
