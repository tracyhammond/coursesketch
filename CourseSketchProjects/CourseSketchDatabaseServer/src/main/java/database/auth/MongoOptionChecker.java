package database.auth;

import database.DatabaseAccessException;
import protobuf.srl.school.School;

/**
 * Created by gigemjt on 9/4/15.
 */
public class MongoOptionChecker implements AuthenticationOptionChecker {

    @Override public boolean authenticateDate(final AuthenticationDataCreator dataCreator, final long checkTime) {
        return false;
    }

    @Override public boolean isItemRegistrationRequired(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
        return false;
    }

    @Override public boolean isItemPublished(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException {
        return false;
    }

    /**
     * @param collectionType
     * @param itemId
     * @return a data creator that grabs the data for any other uses by the option checker.
     */
    @Override public AuthenticationDataCreator createDataGrabber(final School.ItemType collectionType, final String itemId) {
        return null;
    }
}
