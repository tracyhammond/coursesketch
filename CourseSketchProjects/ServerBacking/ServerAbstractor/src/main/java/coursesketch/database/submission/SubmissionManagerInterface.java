package coursesketch.database.submission;

import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.util.DatabaseAccessException;
import protobuf.srl.submission.Submission;

import java.util.List;

/**
 * An interface for submissions.
 *
 * Created by dtracers on 12/17/2015.
 */
public interface SubmissionManagerInterface {
    /**
     * Returns the submissions based on the submission ids.
     *
     * @param authId Used to authenticate the user asking for the submissions
     * @param authenticator Used to authenticate the user and validate permissions.
     * @param problemId This should related with the submission and is used to validate permissions
     * @param submissionIds A list of submission ids that are used to identify the specific submissions wanted.
     * @return A {@link List<protobuf.srl.submission.Submission.SrlExperiment>} That matches the submissions requested.
     * @throws DatabaseAccessException Thrown if the submission can not be found.
     * @throws AuthenticationException Thrown if the user does not have permission to access the submission.
     */
    List<Submission.SrlExperiment> getSubmission(final String authId, final Authenticator authenticator, final String problemId, String...
            submissionIds)
            throws DatabaseAccessException, AuthenticationException;

    /**
     * It inserts an experiment.
     *
     * You must have permission to the problem and the specific experiment that you are inserting into.
     * @param authId The id used to authenticate the user.
     * @param authenticator The object that performs the authentication on the user.
     * @param submission The submission that is being inserted.
     * @param submissionTime The time at which the server received the submission.
     * @return The submission id that belongs to this experiment.
     * @throws AuthenticationException Thrown if the user does not have permission to insert the experiment.
     * @throws DatabaseAccessException Thrown if there are problems merging an existing experiment or creating a new experiment.
     */
    String insertExperiment(final String authId, final Authenticator authenticator, final Submission.SrlExperiment submission,
            final long submissionTime)
            throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts a solution.
     *
     * @param authId The id used to authenticate the user.
     * @param authenticator The object that performs the authentication on the user.
     * @param submission The submission that is being inserted.
     * @return The submission id that belongs to this solution.
     * @throws AuthenticationException Thrown if the user does not have permission to insert the solution.
     * @throws DatabaseAccessException Thrown if there are problems merging an existing solution or creating a new solution.
     */
    String insertSolution(final String authId, final Authenticator authenticator, final Submission.SrlSolution submission)
            throws AuthenticationException, DatabaseAccessException;

    /**
     * Inserts a solution.
     *
     * @param authId The id used to authenticate the user.
     * @param authenticator The object that performs the authentication on the user.
     * @param bankProblemId This should related with the submission and is used to validate permissions
     * @param submissionId An id that is used to identify the specific submission wanted.
     * @return {@link Submission.SrlSolution} The solution requested.
     * @throws AuthenticationException Thrown if the user does not have permission to get the solution.
     * @throws DatabaseAccessException Thrown if there are problems merging an existing solution or creating a new solution.
     */
    Submission.SrlSolution getSolution(final String authId, final Authenticator authenticator, final String bankProblemId, final String
            submissionId)
            throws DatabaseAccessException, AuthenticationException;
}
