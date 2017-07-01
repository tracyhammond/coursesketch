package handlers.subhandlers;

import coursesketch.database.auth.AuthenticationException;
import database.DatabaseAccessException;
import database.institution.Institution;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;
import protobuf.srl.query.Data.ItemRequest;

/**
 * Used to handle grading policy data requests to the server.
 *
 * Created by matt on 5/24/15.
 */
public final class GradingPolicyRequestHandler {
    /**
     * Private constructor.
     */
    private GradingPolicyRequestHandler() {
    }

    /**
     * Handles grading policy requests to the server.
     *
     * @param institution The database interface.
     * @param request The request being sent. Should have an ItemId with courseId as the 0th index.
     * @param userId The Id of the user who sent the request.
     * @return ProtoObject of the grading policy.
     * @throws AuthenticationException Thrown if user does not have correct permission to retrieve policy.
     * @throws DatabaseAccessException Thrown if the policy is not found in the database.
     */
    public static ProtoGradingPolicy gradingPolicyRequestHandler(final Institution institution, final ItemRequest request, final String userId)
            throws AuthenticationException, DatabaseAccessException {
        return institution.getGradingPolicy(request.getItemId(0), userId);
    }
}
