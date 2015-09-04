package database.auth;

/**
 * Checks if the user has valid access to the data in the time range specified.
 * Created by gigemjt on 9/4/15.
 */
public interface AuthenticationDateChecker {
    public void authenticateDate(long checkTime);
}
