package coursesketch.database.grading;

import coursesketch.database.util.DatabaseAccessException;
import protobuf.srl.grading.Grading;
import protobuf.srl.services.authentication.Authentication;

public interface GradingManagerInterface {
    /**
     * @param authRequest The request used to authenticate the grade being added.
     * @param grade The grading being added.
     */
    void addGrade(final Authentication.AuthRequest authRequest, Grading.ProtoGrade grade) throws DatabaseAccessException;
}
