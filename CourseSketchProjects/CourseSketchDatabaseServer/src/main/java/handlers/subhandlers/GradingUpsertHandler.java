package handlers.subhandlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.database.auth.AuthenticationException;
import database.DatabaseAccessException;
import database.institution.Institution;
import protobuf.srl.grading.Grading;
import protobuf.srl.query.Data;
import protobuf.srl.utils.Util;

import java.util.List;

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
     * Handles grading upsert requests to the server.
     *
     * @param institution The database interface.
     * @param itemSet The upsert object being sent.
     * @param authId The id of the user upserting the grade.
     * @param gradedTime The time of the grade submission.
     * @throws AuthenticationException Thrown if user does not have correct permission to upsert grade.
     * @throws DatabaseAccessException Thrown if there is something not found in the database.
     * @throws InvalidProtocolBufferException Thrown if a protobuf object is not correctly formatted.
     */
    public static void gradingUpsertHandler(final Institution institution, final Data.ItemSend itemSet, final String authId, final long gradedTime)
            throws AuthenticationException, DatabaseAccessException, InvalidProtocolBufferException {
        final Grading.ProtoGrade grade = Grading.ProtoGrade.parseFrom(itemSet.getData());
        final Grading.ProtoGrade.Builder clone =  Grading.ProtoGrade.newBuilder(grade);
        final List<Grading.GradeHistory.Builder> gradeHistroy = clone.getGradeHistoryBuilderList();
        if (gradeHistroy.size() == 1) {
            final Grading.GradeHistory.Builder newestHistroy = gradeHistroy.get(0);
            final Util.DateTime.Builder date = Util.DateTime.newBuilder();
            date.setMillisecond(gradedTime);
            newestHistroy.setGradedDate(date);
            newestHistroy.setWhoChanged(authId);
        }
        institution.addGrade(authId, clone.build());
    }
}
