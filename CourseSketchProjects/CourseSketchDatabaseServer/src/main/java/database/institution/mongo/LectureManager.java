package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import protobuf.srl.lecturedata.Lecturedata;

import java.util.List;

import static database.DatabaseStringConstants.ACCESS_DATE;
import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.CLOSE_DATE;
import static database.DatabaseStringConstants.COURSE_COLLECTION;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.DESCRIPTION;
import static database.DatabaseStringConstants.LECTURE_COLLECTION;
import static database.DatabaseStringConstants.MOD;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SLIDES;
import static database.DatabaseStringConstants.USERS;

/**
 * Manages lectures for mongo.
 * @author Devin Tuchsen
 */
public final class LectureManager {

    /**
     * Private constructor.
     */
    private LectureManager() {
    }

    /**
     * Inserts a lecture into the mongo database.
     *
     * @param authenticator the object that is performing authenticaton.
     * @param dbs           The database where the assignment is being stored.
     * @param userId        The id of the user that asking to insert the assignment.
     * @param lecture       The lecture that is being inserted.
     * @return The mongo database id of the assignment.
     * @throws database.auth.AuthenticationException Thrown if the user did not have the authentication to perform the authentication.
     * @throws database.DatabaseAccessException      Thrown if there are problems inserting the assignment.
     */
    public static String mongoInsertLecture(final Authenticator authenticator, final DB dbs, final String userId, final Lecturedata.Lecture lecture)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection newUser = dbs.getCollection(LECTURE_COLLECTION);
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, lecture.getCourseId(), userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        final BasicDBObject query = new BasicDBObject(COURSE_ID, lecture.getCourseId())
                .append(NAME, lecture.getName())
                .append(DESCRIPTION, lecture.getDescription())
                .append(ADMIN, lecture.getAccessPermission().getAdminPermissionList())
                .append(MOD, lecture.getAccessPermission().getModeratorPermissionList())
                .append(USERS, lecture.getAccessPermission().getUserPermissionList());
        if (lecture.hasAccessDate()) {
            query.append(ACCESS_DATE, lecture.getAccessDate().getMillisecond());
        }
        if (lecture.hasCloseDate()) {
            query.append(CLOSE_DATE, lecture.getCloseDate().getMillisecond());
        }
        if (lecture.getSlidesList() != null) {
            query.append(SLIDES, lecture.getSlidesList());
        }
        newUser.insert(query);
        final DBObject cursor = newUser.findOne(query);

        // inserts the id into the previous the course
        CourseManager.mongoInsertLectureIntoCourse(dbs, lecture.getCourseId(), cursor.get(SELF_ID).toString());

        return cursor.get(SELF_ID).toString();
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     * <p/>
     * This is used to copy permissions from the parent course into the current
     * lecture.
     *
     * @param dbs       the database where the data is stored.
     * @param lectureId the id of the assignment that is getting permissions.
     * @param ids       the list of list of permissions that is getting added.
     */
    /*package-private*/ static void mongoInsertDefaultGroupId(final DB dbs, final String lectureId, final List<String>[] ids) {
        final DBRef myDbRef = new DBRef(dbs, LECTURE_COLLECTION, new ObjectId(lectureId));
        final DBObject cursor = myDbRef.fetch();
        final DBCollection lectures = dbs.getCollection(LECTURE_COLLECTION);

        final BasicDBObject updateQuery = MongoAuthenticator.createMongoCopyPermissionQeuery(ids);

        System.out.println(updateQuery);
        lectures.update(cursor, updateQuery);
    }
}
