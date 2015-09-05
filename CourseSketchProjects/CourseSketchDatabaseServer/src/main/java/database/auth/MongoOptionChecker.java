package database.auth;

/**
 * Created by gigemjt on 9/4/15.
 */
public class MongoOptionChecker implements AuthenticationOptionChecker {

    @Override public boolean authenticateDate(final AuthenticationDataCreator dataCreator, final long checkTime) {
        return false;
    }
}
