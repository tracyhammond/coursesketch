package database.auth;

import database.DatabaseAccessException;
import protobuf.srl.school.School;

/**
 * Checks the database for auxiliary permission data.
 *
 * For example this will be used for checking the dates or if the item requires registration but does not check if there is a user or an admin.
 * Created by gigemjt on 9/4/15.
 */
@SuppressWarnings("PMD.CommentRequired")
public final class MongoOptionChecker implements AuthenticationOptionChecker {

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
     * Creates a mongo database interface for grabbing specific pieces of data.
     *
     * @param collectionType The type of collection that is being checked.
     * @param itemId The id of the tiem that is being checked.
     * @return a data creator that grabs the data for any other uses by the option checker.
     */
    @Override public AuthenticationDataCreator createDataGrabber(final School.ItemType collectionType, final String itemId) {
        return null;
    }
}
