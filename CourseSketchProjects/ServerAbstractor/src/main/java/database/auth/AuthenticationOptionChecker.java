package database.auth;

import protobuf.srl.school.School;

/**
 * CHecks a bunch of millacious things
 * Checks if the user has valid access to the data in the time range specified.
 * Created by gigemjt on 9/4/15.
 */
public interface AuthenticationOptionChecker {
    boolean authenticateDate(final School.ItemType collectionType, final String itemId, long checkTime);

    boolean isItemRegistrationRequired(final School.ItemType collectionType, final String itemId);

    boolean isItemPublished(final School.ItemType collectionType, final String itemId);
}
