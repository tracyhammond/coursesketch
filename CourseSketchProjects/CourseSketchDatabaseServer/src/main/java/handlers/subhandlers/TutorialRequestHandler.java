package handlers.subhandlers;

import com.google.protobuf.InvalidProtocolBufferException;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;
import protobuf.srl.query.Data;
import protobuf.srl.tutorial.TutorialOuterClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigemjt on 5/14/15.
 */
public class TutorialRequestHandler {
    /**
     * Private constructor
     */
    private TutorialRequestHandler() {

    }

    /**
     * Returns a list of tutorials based on the request
     * @param institution
     * @param request
     * @param userId
     * @return
     * @throws AuthenticationException
     * @throws DatabaseAccessException
     * @throws InvalidProtocolBufferException
     */
    public static List<TutorialOuterClass.Tutorial> handleTutorialRequest(final Institution institution, final Data.ItemRequest request,
            final String userId) throws AuthenticationException, DatabaseAccessException, InvalidProtocolBufferException {
        List<TutorialOuterClass.Tutorial> result = new ArrayList<>();
        if (!request.hasPage()) {
            result.add(institution.getTutorial(request.getItemId(0), userId));
        } else {
            result = institution.getTutorialList(userId, request.getItemId(0), request.getPage());
        }
        return result;
    }
}
