package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import protobuf.srl.lecturedata.Lecturedata;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.school.School;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ACCESS_DATE;
import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.CLOSE_DATE;
import static database.DatabaseStringConstants.COURSE_COLLECTION;
import static database.DatabaseStringConstants.COURSE_ID;
import static database.DatabaseStringConstants.DESCRIPTION;
import static database.DatabaseStringConstants.ELEMENT_LIST;
import static database.DatabaseStringConstants.LECTURE_COLLECTION;
import static database.DatabaseStringConstants.LECTURE_ID;
import static database.DatabaseStringConstants.MOD;
import static database.DatabaseStringConstants.NAME;
import static database.DatabaseStringConstants.PROBLEM_LIST;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SLIDES;
import static database.DatabaseStringConstants.SLIDE_BLOB;
import static database.DatabaseStringConstants.SLIDE_BLOB_TYPE;
import static database.DatabaseStringConstants.SLIDE_COLLECTION;
import static database.DatabaseStringConstants.STATE_PUBLISHED;
import static database.DatabaseStringConstants.USERS;
import static database.DatabaseStringConstants.X_DIMENSION;
import static database.DatabaseStringConstants.X_POSITION;
import static database.DatabaseStringConstants.Y_DIMENSION;
import static database.DatabaseStringConstants.Y_POSITION;

/**
 * Manages lectures for mongo.
 *
 * @author Devin Tuchsen
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity",
        "PMD.UselessParentheses" })
public final class SlideManager {

    /**
     * Private constructor.
     */
    private SlideManager() {
    }

    /**
     * Inserts a lecture into the mongo database.
     *
     * @param authenticator
     *         the object that is performing authenticaton.
     * @param dbs
     *         The database where the slide is being stored.
     * @param userId
     *         The id of the user that asking to insert the slide.
     * @param slide
     *         The slide that is being inserted.
     * @return The mongo database id of the assignment.
     * @throws database.auth.AuthenticationException
     *         Thrown if the user did not have the authentication to perform the authentication.
     * @throws database.DatabaseAccessException
     *         Thrown if there are problems inserting the assignment.
     */
    public static String mongoInsertSlide(final Authenticator authenticator, final DB dbs, final String userId, final LectureSlide slide)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection newUser = dbs.getCollection(SLIDE_COLLECTION);
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(LECTURE_COLLECTION, slide.getLectureId(), userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        final BasicDBObject query = new BasicDBObject(LECTURE_ID, slide.getLectureId());
        final ArrayList list = new ArrayList();
        for (Lecturedata.LectureElement element : slide.getElementsList()) {
            list.add(createQueryFromElement(element));
        }
        query.append(ELEMENT_LIST, list);
        newUser.insert(query);
        final DBObject cursor = newUser.findOne(query);

        // inserts the id into the previous the course
        LectureManager.mongoInsertSlideIntoLecture(dbs, slide.getLectureId(), cursor.get(SELF_ID).toString(), true);

        return cursor.get(SELF_ID).toString();
    }

    /**
     * NOTE: This function is only used internally and should not be made public.
     * @param e
     *         an element that belongs on a lecture
     * @return a BasicDBObject of the element
     */
    private static BasicDBObject createQueryFromElement(final Lecturedata.LectureElement e) {
        final BasicDBObject query = new BasicDBObject(SELF_ID, e.getId())
                .append(X_POSITION, e.getXPosition())
                .append(Y_POSITION, e.getYPosition())
                .append(X_DIMENSION, e.getXDimension())
                .append(Y_DIMENSION, e.getYDimension())
                .append(SLIDE_BLOB_TYPE, e.getElementTypeCase().getNumber());
        switch (e.getElementTypeCase()) {
            case IMAGE:
                query.append(SLIDE_BLOB, e.getImage().toByteArray());
                break;
            case TEXTBOX:
                query.append(SLIDE_BLOB, e.getTextBox().toByteArray());
                break;
            case SKETCHAREA:
                query.append(SLIDE_BLOB, e.getSketchArea().toByteArray());
                break;
            case QUESTION:
                query.append(SLIDE_BLOB, e.getQuestion().toByteArray());
                break;
            case ELEMENTTYPE_NOT_SET:
            default:
                break;
        }
        return query;
    }

    /**
     * Grabs the lecture from mongo and performs checks making sure the user is valid before returning the lecture.
     *
     * @param authenticator
     *         the object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param lectureId
     *         the id of the lecture that is being grabbed.
     * @param userId
     *         The id of the user that asking to insert the lecture.
     * @param checkTime
     *         The time that the assignment was asked to be grabbed. (used to
     *         check if the lecture is valid)
     * @return The lecture from the database.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the
     *         assignment.
     * @throws DatabaseAccessException
     *         Thrown if there are problems retrieving the lecture.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
    public static Lecture mongoGetLecture(final Authenticator authenticator, final DB dbs, final String lectureId, final String userId,
            final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, LECTURE_COLLECTION, new ObjectId(lectureId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("Lecture was not found with the following ID " + lectureId, true);
        }

        boolean isAdmin, isMod, isUsers;
        isAdmin = authenticator.checkAuthentication(userId, (List<String>) corsor.get(ADMIN));
        isMod = authenticator.checkAuthentication(userId, (List<String>) corsor.get(MOD));
        isUsers = authenticator.checkAuthentication(userId, (List<String>) corsor.get(USERS));

        if (!isAdmin && !isMod && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        // check to make sure the lecture is within the time period that the
        // course is open and the user is in the course
        // FUTURE: maybe not make this necessarry if the insertion of lecture prevents this.
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckDate(true);
        auth.setUser(true);
        if (isUsers && !authenticator.isAuthenticated(COURSE_COLLECTION, (String) corsor.get(COURSE_ID), userId, checkTime, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_DATE);
        }

        final School.State.Builder stateBuilder = School.State.newBuilder();
        // FUTURE: add this to all fields!
        // An assignment is only publishable after a certain criteria is met
        if (corsor.containsField(STATE_PUBLISHED)) {
            final boolean published = (Boolean) corsor.get(STATE_PUBLISHED);
            if (published) {
                stateBuilder.setPublished(true);
            } else {
                if (!isAdmin || !isMod) {
                    throw new DatabaseAccessException("The specific lecture is not published yet", true);
                }
                stateBuilder.setPublished(false);
            }
        }

        // now all possible exceptions have already been thrown.
        final Lecture.Builder exactLecture = Lecture.newBuilder();

        exactLecture.setId(lectureId);

        // sets the majority of the assignment data
        setLectureData(exactLecture, corsor);

        setLectureStateAndDate(exactLecture, corsor);

        // if you are a user, the assignment must be open to view the problems
        if (isAdmin || isMod || (isUsers
                && Authenticator.isTimeValid(checkTime, exactLecture.getAccessDate(), exactLecture.getCloseDate()))) {
            if (corsor.get(PROBLEM_LIST) != null) {
                exactLecture.addAllSlides((List) corsor.get(SLIDES));
            }

            stateBuilder.setAccessible(true);
        } else if (isUsers && !Authenticator.isTimeValid(checkTime, exactLecture.getAccessDate(), exactLecture.getCloseDate())) {
            stateBuilder.setAccessible(false);
            System.err.println("USER LECTURE TIME IS CLOSED SO THE COURSE LIST HAS BEEN PREVENTED FROM BEING USED!");
            System.err.println(exactLecture.getAccessDate().getMillisecond() + " < " + checkTime + " < "
                    + exactLecture.getCloseDate().getMillisecond());
        }

        exactLecture.setState(stateBuilder);

        final School.SrlPermission.Builder permissions = School.SrlPermission.newBuilder();
        if (isAdmin) {
            permissions.addAllAdminPermission((ArrayList) corsor.get(ADMIN)); // admin
            permissions.addAllModeratorPermission((ArrayList) corsor.get(MOD)); // admin
        }
        if (isAdmin || isMod) {
            permissions.addAllUserPermission((ArrayList) corsor.get(USERS)); // mod
            exactLecture.setAccessPermission(permissions.build());
        }
        return exactLecture.build();
    }

    /**
     * sets data of the lecture from the given cursor.
     *
     * @param exactLecture
     *         The lecture that the data is being set to.
     * @param cursor
     *         The database cursor pointing to a specific lecture.
     */
    private static void setLectureData(final Lecture.Builder exactLecture, final DBObject cursor) {
        exactLecture.setCourseId((String) cursor.get(COURSE_ID));
        exactLecture.setName((String) cursor.get(NAME));
        exactLecture.setDescription((String) cursor.get(DESCRIPTION));
    }

    /**
     * Sets data about the state of the lecture and its date.
     *
     * @param exactLecture
     *         a protobuf lecture builder.
     * @param corsor
     *         the current database pointer for the lecture.
     */
    private static void setLectureStateAndDate(final Lecture.Builder exactLecture, final DBObject corsor) {
        final Object accessDate = corsor.get(ACCESS_DATE);
        final Object closeDate = corsor.get(CLOSE_DATE);
        if (accessDate == null) {
            exactLecture.setAccessDate(RequestConverter.getProtoFromMilliseconds(0));
        } else {
            exactLecture.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) accessDate).longValue()));
        }
        if (closeDate == null) {
            exactLecture.setCloseDate(RequestConverter.getProtoFromMilliseconds(RequestConverter.getMaxTime()));
        } else {
            exactLecture.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) closeDate).longValue()));
        }
    }
}
