package database.auth;

import protobuf.srl.school.School;

/**
 * Created by gigemjt on 9/4/15.
 */
public class MongoOptionChecker implements AuthenticationOptionChecker {

    @Override public boolean authenticateDate(final School.ItemType collectionType, final String itemId, final long checkTime) {
        return false;
    }
}
