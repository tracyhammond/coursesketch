package handlers.subhandlers;

import com.google.protobuf.InvalidProtocolBufferException;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;
import protobuf.srl.grading.Grading;
import protobuf.srl.query.Data;

/**
 * Used to handle grade and grading policy data upserts to the server.
 *
 * Created by matt on 4/22/15.
 */
public final class GradingUpsertHandler {
    /**
     * Private constructor.
     */
    private GradingUpsertHandler() {
    }

    /**
     *
     * @param institution The database interface.
     * @param itemSet The upsert object being sent.
     * @param userId The id of the user upserting the grade.
     * @throws AuthenticationException Thrown if user does not have correct permission to upsert grade.
     * @throws DatabaseAccessException Thrown if there is something not found in the database.
     * @throws InvalidProtocolBufferException Thrown if a protobuf object is not correctly formatted.
     */
    public static void gradingUpsertHandler(final Institution institution, final Data.ItemSend itemSet, final String userId)
            throws AuthenticationException, DatabaseAccessException, InvalidProtocolBufferException {
        final Grading.ProtoGrade grade = Grading.ProtoGrade.parseFrom(itemSet.getData());
        institution.addGrade(userId, grade);
    }
}
