package coursesketch.database.submission;

import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.Authenticator;
import database.DatabaseAccessException;
import protobuf.srl.submission.Submission;

import java.util.List;

/**
 * Created by dtracers on 12/17/2015.
 */
public interface SubmissionManagerInterface {
    /**
     * Returns the submissions based on the submission ids.
     * @param authId Used to authenticate the user asking for the submissions
     * @param authenticator Used to authenticate the user and validate permissions.
     * @param problemId This should related with the submission and is used to validate permissions
     * @param submissionIds
     * @return
     * @throws DatabaseAccessException
     * @throws AuthenticationException
     */
    List<Submission.SrlExperiment> getSubmission(String authId, final Authenticator authenticator, final String problemId, String... submissionIds)
            throws DatabaseAccessException, AuthenticationException;

    String insertExperiment(final String authId, final Authenticator authenticator, final Submission.SrlExperiment submission,
            final long submissionTime)
            throws AuthenticationException, DatabaseAccessException;

    String insertSolution(final String authId, final Authenticator authenticator, final Submission.SrlSolution submission)
            throws AuthenticationException, DatabaseAccessException;
}
