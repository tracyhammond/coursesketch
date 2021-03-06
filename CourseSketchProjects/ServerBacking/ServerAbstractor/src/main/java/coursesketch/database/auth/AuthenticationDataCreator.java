package coursesketch.database.auth;

/**
 * An interface that implements where data for authentication actually comes from.
 * @author gigemjt
 *
 */
public interface AuthenticationDataCreator {

    /**
     * @return The value that the database found for authentication.
     */
    Object getDatabaseResult();
}
