package handlers.subhandlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.institution.Institution;
import protobuf.srl.grading.Grading;
import protobuf.srl.query.Data;

/**
 * Used to handle grade policy data upserts to the server.
 *
 * Created by matt on 5/24/15.
 */
public final class GradingPolicyUpsertHandler {
    /**
     * Private constructor.
     */
    private GradingPolicyUpsertHandler() {
    }

    /**
     * Handles grading policy upsert requests to the server.
     *
     * @param institution The database interface.
     * @param itemSet The upsert object being sent.
     * @param authId The id of the user upserting the policy.
     * @throws AuthenticationException Thrown if user does not have correct permission to upsert policy.
     * @throws DatabaseAccessException Thrown if there is something not found in the database.
     * @throws InvalidProtocolBufferException Thrown if a protobuf object is not correctly formatted.
     */
    public static void gradingPolicyUpsertHandler(final Institution institution, final Data.ItemSend itemSet, final String authId)
            throws AuthenticationException, DatabaseAccessException, InvalidProtocolBufferException {
        final Grading.ProtoGradingPolicy policy = Grading.ProtoGradingPolicy.parseFrom(itemSet.getData());
        institution.upsertGradingPolicy(authId, policy);
    }
}
