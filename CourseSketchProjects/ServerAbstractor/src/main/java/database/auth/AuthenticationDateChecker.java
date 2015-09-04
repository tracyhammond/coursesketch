package database.auth;

import protobuf.srl.school.School;

/**
 * Checks if the user has valid access to the data in the time range specified.
 * Created by gigemjt on 9/4/15.
 */
public interface AuthenticationDateChecker {
    public boolean authenticateDate(final School.ItemType collectionType, final String itemId, long checkTime);
}
