package coursesketch.database.grading;

import coursesketch.database.util.DatabaseAccessException;
import protobuf.srl.grading.Grading;
import protobuf.srl.services.authentication.Authentication;

/**
 * Interface for grading managers.
 */
public interface GradingManagerInterface {
    /**
     * @param authRequest The request used to authenticate the grade being added.
     * @param grade The grading being added.
     * @throws DatabaseAccessException Thrown if there is a problem with the database
     */
    void addGrade(Authentication.AuthRequest authRequest, Grading.ProtoGrade grade) throws DatabaseAccessException;
}
