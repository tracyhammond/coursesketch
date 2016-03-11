package database.institution.mongo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import database.DatabaseAccessException;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import database.DatabaseStringConstants;
import protobuf.srl.lecturedata.Lecturedata;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.tutorial.TutorialOuterClass;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.ELEMENT_LIST;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.SLIDE_BLOB;
import static database.DatabaseStringConstants.SLIDE_BLOB_TYPE;
import static database.DatabaseStringConstants.X_DIMENSION;
import static database.DatabaseStringConstants.X_POSITION;
import static database.DatabaseStringConstants.Y_DIMENSION;
import static database.DatabaseStringConstants.Y_POSITION;
import static database.DbSchoolUtility.getCollectionFromType;
import static database.utilities.MongoUtilities.convertStringToObjectId;

/**
 * Manages slides for mongo.
 *
 * @author Antonio Sanchez
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
     * @param authId
     *         The id of the user that asking to insert the slide.
     * @param slide
     *         The slide that is being inserted.
     * @return The mongo database id of the assignment.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to perform the authentication.
     * @throws DatabaseAccessException
     *         Thrown if there are problems inserting the assignment.
     */
    public static String mongoInsertSlide(final Authenticator authenticator, final DB dbs, final String authId, final LectureSlide slide)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection newUser = dbs.getCollection(getCollectionFromType(School.ItemType.SLIDE));

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .setCheckingAdmin(true)
                .build();

        // Checks the course problem if the user has permission to insert a slide
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.COURSE_PROBLEM, slide.getCourseProblemId(), authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        final BasicDBObject query = new BasicDBObject(DatabaseStringConstants.ASSIGNMENT_ID, slide.getAssignmentId());
        final ArrayList list = new ArrayList();
        for (Lecturedata.LectureElement element : slide.getElementsList()) {
            list.add(createQueryFromElement(element));
        }
        query.append(ELEMENT_LIST, list);
        newUser.insert(query);
        final DBObject cursor = newUser.findOne(query);

        return cursor.get(SELF_ID).toString();
    }

    /**
     * NOTE: This function is only used internally and should not be made public.
     *
     * @param lectureElement
     *         an element that belongs on a lecture
     * @return a BasicDBObject of the element
     */
    public static BasicDBObject createQueryFromElement(final Lecturedata.LectureElement lectureElement) {
        final BasicDBObject query = new BasicDBObject(SELF_ID, lectureElement.getId())
                .append(X_POSITION, lectureElement.getXPosition())
                .append(Y_POSITION, lectureElement.getYPosition())
                .append(X_DIMENSION, lectureElement.getXDimension())
                .append(Y_DIMENSION, lectureElement.getYDimension())
                .append(SLIDE_BLOB_TYPE, lectureElement.getElementTypeCase().getNumber());
        switch (lectureElement.getElementTypeCase()) {
            case IMAGE:
                query.append(SLIDE_BLOB, lectureElement.getImage().toByteArray());
                break;
            case TEXTBOX:
                query.append(SLIDE_BLOB, lectureElement.getTextBox().toByteArray());
                break;
            case SKETCHAREA:
                query.append(SLIDE_BLOB, lectureElement.getSketchArea().toByteArray());
                break;
            case EMBEDDEDHTML:
                query.append(SLIDE_BLOB, lectureElement.getEmbeddedHtml().toByteArray());
                break;
            case ELEMENTTYPE_NOT_SET:
            default:
                break;
        }
        return query;
    }

    /**
     * Grabs the slide from mongo and performs checks making sure the user is valid before returning the slide.
     *
     * @param authenticator
     *         the object that is performing authentication.
     * @param dbs
     *         The database where the assignment is being stored.
     * @param userId
     *         The id of the user that asking to insert the lecture.
     * @param slideId
     *         the id of the lecture that is being grabbed.
     * @param checkTime
     *         The time that the assignment was asked to be grabbed. (used to
     *         check if the slide is valid)
     * @return The slide from the database.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the
     *         slide.
     * @throws DatabaseAccessException
     *         Thrown if there are problems retrieving the slide.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
    public static LectureSlide mongoGetLectureSlide(final Authenticator authenticator, final DB dbs, final String userId, final String slideId,
            final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final DBCollection collection = dbs.getCollection(getCollectionFromType(School.ItemType.SLIDE));
        final DBObject cursor = collection.findOne(convertStringToObjectId(slideId));
        if (cursor == null) {
            throw new DatabaseAccessException("Slide was not found with the following ID " + slideId, true);
        }

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .setCheckingAdmin(true)
                .build();
        // FUTURE: figure out lecture slide permissions
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.ASSIGNMENT, (String) cursor.get(ASSIGNMENT_ID), userId, 0, authType);

        // FUTURE Fix this! maybe make the lecture a user? not really sure for now everyone is a user.
        // authenticator.checkAuthentication(userId, (List<String>) cursor.get(USERS));

        if (!responder.hasAccess()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        // check to make sure the slide is within the time period that the
        // course is open and the user is in the course
        // FUTURE: maybe not make this necessary if the insertion of lecture prevents this.
        if (responder.hasAccess() && !responder.hasPeerTeacherPermission() && !responder.isItemOpen()) {
            throw new AuthenticationException(AuthenticationException.INVALID_DATE);
        }

        // FUTURE: add this to all fields!
        // An assignment is only publishable after a certain criteria is met
        if (!responder.isItemPublished() && !responder.hasModeratorPermission()) {
            throw new DatabaseAccessException("The specific lecture is not published yet: " + slideId, true);
        }

        // now all possible exceptions have already been thrown.
        final Lecturedata.LectureSlide.Builder exactSlide = Lecturedata.LectureSlide.newBuilder();

        exactSlide.setId(slideId);

        // sets the majority of the slide data
        setSlideData(exactSlide, cursor);

        return exactSlide.build();
    }

    /**
     * updates data from an assignment.
     *
     * @param authenticator
     *         the object that is performing authentication.
     * @param dbs
     *         The database where the lecture slide is being stored.
     * @param assignmentId
     *         the id of the lecture slide that is being updated.
     * @param userId
     *         The id of the user that asking to update the lecture slide.
     * @param lectureSlide
     *         The lecture slide that is being updated.
     * @return true if the lecture slide was updated successfully.
     * @throws AuthenticationException
     *         The user does not have permission to update the lecture slide.
     * @throws DatabaseAccessException
     *         The lecture does not exist.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static boolean mongoUpdateLectureSlide(final Authenticator authenticator, final DB dbs, final String assignmentId, final String userId,
            final LectureSlide lectureSlide) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBCollection collection = dbs.getCollection(getCollectionFromType(School.ItemType.SLIDE));
        final DBObject cursor = collection.findOne(convertStringToObjectId(lectureSlide.getId()));

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(School.ItemType.ASSIGNMENT, assignmentId, userId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        // TODO make a way to clear out a lecture slide so it is empty?
        if (lectureSlide.getElementsCount() > 0) {
            final List<BasicDBObject> list = new ArrayList<>();
            for (Lecturedata.LectureElement element : lectureSlide.getElementsList()) {
                list.add(createQueryFromElement(element));
            }
            collection.update(cursor, new BasicDBObject(SET_COMMAND, new BasicDBObject(ELEMENT_LIST, list)));
            update = true;
        }
        return update;
    }

    /**
     * sets data of the slide from the given cursor.
     *
     * @param exactSlide
     *         The lecture that the data is being set to.
     * @param cursor
     *         The database cursor pointing to a specific slide.
     * @throws database.DatabaseAccessException
     *         passes exception through to createElementFromQuery
     */
    public static void setSlideData(final Lecturedata.LectureSlide.Builder exactSlide, final DBObject cursor) throws DatabaseAccessException {
        exactSlide.setAssignmentId(cursor.get(DatabaseStringConstants.ASSIGNMENT_ID).toString());
        exactSlide.setId(cursor.get(SELF_ID).toString());
        if (cursor.get(ELEMENT_LIST) != null) {
            final ArrayList<Lecturedata.LectureElement> objects = new ArrayList<>();
            for (BasicDBObject element : (List<BasicDBObject>) cursor.get(ELEMENT_LIST)) {
                objects.add(createElementFromQuery(element));
            }
            exactSlide.addAllElements(objects);
        }
    }

    /**
     * NOTE: This function is only used internally and should not be made public.
     *
     * @param query
     *         a BasicDBObject from the mongo database that is a slide
     * @return a Lecturedata.LectureElement of the BasicDBObject that was passed in
     * @throws database.DatabaseAccessException
     *         a DatabaseAccessException if something goes wrong parsing a blob of a LectureElement
     */
    public static Lecturedata.LectureElement createElementFromQuery(final DBObject query) throws DatabaseAccessException {
        final Lecturedata.LectureElement.Builder element = Lecturedata.LectureElement.newBuilder();
        final String lectureElementId = (String) query.get(SELF_ID);
        final int xPos = (int) query.get(X_POSITION);
        final int yPos = (int) query.get(Y_POSITION);
        final int xDim = (int) query.get(X_DIMENSION);
        final int yDim = (int) query.get(Y_DIMENSION);
        final Lecturedata.LectureElement.ElementTypeCase blobType =
                Lecturedata.LectureElement.ElementTypeCase.valueOf((int) query.get(SLIDE_BLOB_TYPE));
        final byte[] blob = (byte[]) query.get(SLIDE_BLOB);
        element.setId(lectureElementId);
        element.setXPosition(xPos);
        element.setYPosition(yPos);
        element.setXDimension(xDim);
        element.setYDimension(yDim);
        try {
            switch (blobType) {
                case TEXTBOX:
                    element.setTextBox(TutorialOuterClass.ActionCreateTextBox.parseFrom(blob));
                    break;
                case IMAGE:
                    element.setImage(Lecturedata.Image.parseFrom(blob));
                    break;
                case SKETCHAREA:
                    element.setSketchArea(Lecturedata.SketchArea.parseFrom(blob));
                    break;
                case EMBEDDEDHTML:
                    element.setEmbeddedHtml(Lecturedata.EmbeddedHtml.parseFrom(blob));
                    break;
                default:
                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            throw new DatabaseAccessException("Error while parsing the blob of a LectureElement", e);
        }
        return element.build();
    }
}
