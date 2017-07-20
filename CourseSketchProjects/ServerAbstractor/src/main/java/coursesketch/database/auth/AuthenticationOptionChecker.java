package coursesketch.database.auth;

import database.DatabaseAccessException;
import protobuf.srl.utils.Util;

/**
 * Checks a bunch of miscellaneous parts for authentication.  These are things that do not require the authenticationId.
 *
 * Created by gigemjt on 9/4/15.
 */
public interface AuthenticationOptionChecker {

    /**
     * Authenticates if the course is open with the given time.
     *
     * @param dataCreator Where the data is coming from.
     * @param checkTime The time we want to check for validity.
     * @return True if the course is valid, false otherwise.
     * @throws DatabaseAccessException Thrown if there is a problem authenticating the date.
     */
    boolean authenticateDate(final AuthenticationDataCreator dataCreator, long checkTime) throws DatabaseAccessException;

    /**
     * Returns true if the item requires registration to view it.
     *
     * @param dataCreator Where the data is coming from.
     * @return true if the item requires registration to view it.
     * @throws DatabaseAccessException Thrown if there is a problem grabbing the data about registration.
     */
    boolean isItemRegistrationRequired(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException;

    /**
     * True if the item is published.
     *
     * An item can't be published without certain requirements being met.
     * These requirements are item dependent.
     * @param dataCreator Where the data is coming from.
     * @return True if the item is published
     * @throws DatabaseAccessException Thrown if there is a problem grabbing the data about registration.
     */
    boolean isItemPublished(final AuthenticationDataCreator dataCreator) throws DatabaseAccessException;

    /**
     * Creates a data grabber based on the {@link Util.ItemType} using the itemId to find the data.
     *
     * @param collectionType The type of item it is ex: Course, Assignment, Problem.
     * @param itemId The id of the item used for looking it up in the database.
     * @return A {@link AuthenticationDataCreator} that grabs the data for any other uses by the option checker.
     * @throws DatabaseAccessException Thrown if there is a problem creating the data from the database.
     */
    AuthenticationDataCreator createDataGrabber(final Util.ItemType collectionType, final String itemId) throws DatabaseAccessException;
}
