package handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import coursesketch.server.interfaces.SocketSession;
import database.auth.AuthenticationException;
import database.institution.Institution;
import database.institution.mongo.MongoInstitution;
import protobuf.srl.lecturedata.Lecturedata.Lecture;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message.Request;

import java.util.ArrayList;

/**
 * Handles data being added or edited.
 *
 * In most cases insert returns the mongoId and the id that was taken in. This
 * allows the client to replace the old assignment id with the new assignment
 * id.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity" })
public final class DataUpdateHandler {
    /**
     * A message returned when the insert was successful.
     */
    private static final String SUCCESS_MESSAGE = "QUERY WAS SUCCESSFUL!";

    /**
     * Private constructor.
     */
    private DataUpdateHandler() {
    }

    /**
     * Takes in a request that has to deal with inserting data.
     *
     * decode request and pull correct information from {@link database.institution.Institution}
     * (courses, assignments, ...) then repackage everything and send it out.
     *
     * @param req
     *         The request that has data being inserted.
     * @param conn
     *         The connection where the result is sent to.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.ExcessiveMethodLength", "PMD.AvoidCatchingGenericException" })
    public static void handleData(final Request req, final SocketSession conn) {
        try {
            System.out.println("Receiving DATA UPDATE Request...");

            final String userId = req.getServersideId();
            final DataSend request = DataSend.parseFrom(req.getOtherData());
            if (userId == null || userId.equals("")) {
                throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
            }
            final ArrayList<ItemResult> results = new ArrayList<>();

            final Institution instance = MongoInstitution.getInstance();
            for (int p = 0; p < request.getItemsList().size(); p++) {
                final ItemSend itemSet = request.getItemsList().get(p);
                try {
                    switch (itemSet.getQuery()) {
                        // TODO Enable updates for other data
                        case LECTURE: {
                            final Lecture lecture = Lecture.parseFrom(itemSet.getData());
                            instance.updateLecture(userId, lecture);
                            results.add(ResultBuilder.buildResult("", itemSet.getQuery()));
                        }
                        break;
                        case LECTURESLIDE: {
                            final LectureSlide lectureSlide = LectureSlide.parseFrom(itemSet.getData());
                            instance.updateLectureSlide(userId, lectureSlide);
                            results.add(ResultBuilder.buildResult("", itemSet.getQuery()));
                        }
                        break;
                        default:
                            break;
                    }
                } catch (AuthenticationException e) {
                    if (e.getType() == AuthenticationException.INVALID_DATE) {
                        final ItemResult.Builder build = ItemResult.newBuilder();
                        build.setQuery(itemSet.getQuery());
                        results.add(ResultBuilder.buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
                    } else {
                        e.printStackTrace();
                        throw e;
                    }
                } catch (Exception e) {
                    final ItemResult.Builder build = ItemResult.newBuilder();
                    build.setQuery(itemSet.getQuery());
                    build.setData(itemSet.toByteString());
                    results.add(ResultBuilder.buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
                    e.printStackTrace();
                }
            }
            if (!results.isEmpty()) {
                conn.send(ResultBuilder.buildRequest(results, SUCCESS_MESSAGE, req));
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
            conn.send(ResultBuilder.buildRequest(null, "user was not authenticated to insert data " + e.getMessage(), req));
        } catch (InvalidProtocolBufferException | RuntimeException e) {
            e.printStackTrace();
            conn.send(ResultBuilder.buildRequest(null, e.getMessage(), req));
        }
    }
}
