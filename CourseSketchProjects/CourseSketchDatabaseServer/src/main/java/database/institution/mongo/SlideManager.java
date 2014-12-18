package database.institution.mongo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import org.bson.types.ObjectId;
import protobuf.srl.lecturedata.Lecturedata;
import protobuf.srl.lecturedata.Lecturedata.LectureSlide;
import protobuf.srl.school.School;
import protobuf.srl.tutorial.TutorialOuterClass;

import java.util.ArrayList;
import java.util.List;

import static database.DatabaseStringConstants.ADMIN;
import static database.DatabaseStringConstants.ELEMENT_LIST;
import static database.DatabaseStringConstants.LECTURE_COLLECTION;
import static database.DatabaseStringConstants.LECTURE_ID;
import static database.DatabaseStringConstants.MOD;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.SLIDE_BLOB;
import static database.DatabaseStringConstants.SLIDE_BLOB_TYPE;
import static database.DatabaseStringConstants.SLIDE_COLLECTION;
import static database.DatabaseStringConstants.STATE_PUBLISHED;
import static database.DatabaseStringConstants.X_DIMENSION;
import static database.DatabaseStringConstants.X_POSITION;
import static database.DatabaseStringConstants.Y_DIMENSION;
import static database.DatabaseStringConstants.Y_POSITION;

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
     *
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
            case EMBEDDEDHTML:
                query.append(SLIDE_BLOB, e.getEmbeddedHtml().toByteArray());
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
     * @param slideId
     *         the id of the lecture that is being grabbed.
     * @param userId
     *         The id of the user that asking to insert the lecture.
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
    public static LectureSlide mongoGetLectureSlide(final Authenticator authenticator, final DB dbs, final String slideId, final String userId,
            final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, SLIDE_COLLECTION, new ObjectId(slideId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("Slide was not found with the following ID " + slideId, true);
        }

        boolean isAdmin, isMod, isUsers;
        isAdmin = authenticator.checkAuthentication(userId, (List<String>) corsor.get(ADMIN));
        isMod = authenticator.checkAuthentication(userId, (List<String>) corsor.get(MOD));

        // TODO: Fix this
        isUsers = true; // authenticator.checkAuthentication(userId, (List<String>) corsor.get(USERS));

        if (!isAdmin && !isMod && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        // check to make sure the slide is within the time period that the
        // course is open and the user is in the course
        // FUTURE: maybe not make this necessary if the insertion of lecture prevents this.
        final Authenticator.AuthType auth = new Authenticator.AuthType();
        auth.setCheckDate(true);
        auth.setUser(true);
        if (isUsers && !authenticator.isAuthenticated(LECTURE_COLLECTION, (String) corsor.get(LECTURE_ID), userId, checkTime, auth)) {
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
                    throw new DatabaseAccessException("The specific slide is not published yet", true);
                }
                stateBuilder.setPublished(false);
            }
        }

        // now all possible exceptions have already been thrown.
        final Lecturedata.LectureSlide.Builder exactSlide = Lecturedata.LectureSlide.newBuilder();

        exactSlide.setId(slideId);

        // sets the majority of the slide data
        setSlideData(exactSlide, corsor);

        return exactSlide.build();
    }

    /**
     * updates data from an assignment.
     *
     * @param authenticator
     *         the object that is performing authentication.
     * @param dbs
     *         The database where the lecture slide is being stored.
     * @param lectureSlideId
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
    public static boolean mongoUpdateLectureSlide(final Authenticator authenticator, final DB dbs, final String lectureSlideId, final String userId,
            final LectureSlide lectureSlide) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, SLIDE_COLLECTION, new ObjectId(lectureSlideId));
        final DBObject corsor = myDbRef.fetch();
        final DBCollection lectureSlides = dbs.getCollection(SLIDE_COLLECTION);

        //final ArrayList adminList = (ArrayList<Object>) corsor.get("Admin");
        //final ArrayList modList = (ArrayList<Object>) corsor.get("Mod");
        boolean isAdmin, isMod;
        isAdmin = true; //authenticator.checkAuthentication(userId, adminList);
        isMod = true; //authenticator.checkAuthentication(userId, modList);

        if (!isAdmin && !isMod) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        final BasicDBObject updated = new BasicDBObject();
        if (isAdmin || isMod) {
            if (lectureSlide.getElementsCount() > 0) {
                final ArrayList list = new ArrayList();
                for (Lecturedata.LectureElement element : lectureSlide.getElementsList()) {
                    list.add(createQueryFromElement(element));
                }
                lectureSlides.update(corsor, new BasicDBObject(SET_COMMAND, new BasicDBObject(ELEMENT_LIST, list)));
                update = true;
            }
        }
        /*if (update) {
            UserUpdateHandler.insertUpdates(dbs, ((List) corsor.get(USERS)), lectureSlideId, UserUpdateHandler.ASSIGNMENT_CLASSIFICATION);
        }*/
        return true;
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
    private static void setSlideData(final Lecturedata.LectureSlide.Builder exactSlide, final DBObject cursor) throws DatabaseAccessException {
        exactSlide.setLectureId((String) cursor.get(LECTURE_ID));
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
    private static Lecturedata.LectureElement createElementFromQuery(final BasicDBObject query) throws DatabaseAccessException {
        final Lecturedata.LectureElement.Builder element = Lecturedata.LectureElement.newBuilder();
        final String id = (String) query.get(SELF_ID);
        final int xPos = (int) query.get(X_POSITION);
        final int yPos = (int) query.get(Y_POSITION);
        final int xDim = (int) query.get(X_DIMENSION);
        final int yDim = (int) query.get(Y_DIMENSION);
        final Lecturedata.LectureElement.ElementTypeCase blobType =
                Lecturedata.LectureElement.ElementTypeCase.valueOf((int) query.get(SLIDE_BLOB_TYPE));
        final byte[] blob = (byte[]) query.get(SLIDE_BLOB);
        element.setId(id);
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
                case QUESTION:
                    element.setQuestion(Lecturedata.SrlQuestion.parseFrom(blob));
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
