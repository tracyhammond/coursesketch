package database.auth;

import protobuf.srl.school.School;

/**
 * CHecks a bunch of millacious things
 * Checks if the user has valid access to the data in the time range specified.
 * Created by gigemjt on 9/4/15.
 */
public interface AuthenticationOptionChecker {
    boolean authenticateDate(final AuthenticationDataCreator dataCreator, long checkTime);

    boolean isItemRegistrationRequired(final AuthenticationDataCreator dataCreator);

    boolean isItemPublished(final AuthenticationDataCreator dataCreator);

    /**
     * @return a data creator that grabs the data for any other uses by the option checker.
     */
    AuthenticationDataCreator createDataGrabber(final School.ItemType collectionType, final String itemId);
}
