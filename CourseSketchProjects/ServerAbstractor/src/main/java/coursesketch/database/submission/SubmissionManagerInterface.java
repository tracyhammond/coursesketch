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
    List<Submission.SrlExperiment> getSubmission(String authId, final Authenticator authenticator, final String problemId, String... submissionIds)
            throws DatabaseAccessException, AuthenticationException;

    String insertExperiment(final String authId, final Authenticator authenticator, final Submission.SrlExperiment submission,
            final long submissionTime)
            throws AuthenticationException, DatabaseAccessException;

    String insertSolution(final String authId, final Authenticator authenticator, final Submission.SrlSolution submission)
            throws AuthenticationException, DatabaseAccessException;
}
