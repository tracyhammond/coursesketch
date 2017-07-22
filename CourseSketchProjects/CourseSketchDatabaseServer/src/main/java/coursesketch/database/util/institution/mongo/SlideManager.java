package coursesketch.database.util.institution.mongo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import coursesketch.database.util.DatabaseAccessException;
import coursesketch.database.util.DatabaseStringConstants;
import org.bson.Document;
import org.bson.types.Binary;
import protobuf.srl.question.QuestionDataOuterClass;
import protobuf.srl.school.Problem;
import protobuf.srl.school.Problem.LectureSlide;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.tutorial.TutorialOuterClass;
import protobuf.srl.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.ASSIGNMENT_ID;
import static coursesketch.database.util.DatabaseStringConstants.ELEMENT_LIST;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.util.DatabaseStringConstants.SET_COMMAND;
import static coursesketch.database.util.DatabaseStringConstants.SLIDE_BLOB;
import static coursesketch.database.util.DatabaseStringConstants.SLIDE_BLOB_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.X_DIMENSION;
import static coursesketch.database.util.DatabaseStringConstants.X_POSITION;
import static coursesketch.database.util.DatabaseStringConstants.Y_DIMENSION;
import static coursesketch.database.util.DatabaseStringConstants.Y_POSITION;
import static coursesketch.database.util.DbSchoolUtility.getCollectionFromType;
import static coursesketch.database.util.utilities.MongoUtilities.convertStringToObjectId;

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
    private SlideManager() { }

    /**
     * Inserts a lecture into the mongo coursesketch.util.util.
     *
     * @param authenticator the object that is performing authenticaton.
     * @param dbs The coursesketch.util.util where the slide is being stored.
     * @param authId The id of the user that asking to insert the slide.
     * @param slide The slide that is being inserted.
     * @return The mongo coursesketch.util.util id of the assignment.
     * @throws AuthenticationException Thrown if the user did not have the authentication to perform the authentication.
     * @throws DatabaseAccessException Thrown if there are problems inserting the assignment.
     */
    public static String mongoInsertSlide(final Authenticator authenticator, final MongoDatabase dbs, final String authId, final LectureSlide slide)
            throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> newUser = dbs.getCollection(getCollectionFromType(Util.ItemType.SLIDE));

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckDate(true)
                .setCheckingAdmin(true)
                .build();

        // Checks the course problem if the user has permission to insert a slide
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE_PROBLEM, slide.getCourseProblemId(), authId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        final Document query = new Document(DatabaseStringConstants.ASSIGNMENT_ID, slide.getAssignmentId());
        final ArrayList list = new ArrayList();
        for (Problem.ProblemElement element : slide.getElementsList()) {
            list.add(createQueryFromElement(element));
        }
        query.append(ELEMENT_LIST, list);
        newUser.insertOne(query);
        final Document cursor = newUser.find(query).first();

        return cursor.get(SELF_ID).toString();
    }

    /**
     * NOTE: This function is only used internally and should not be made public.
     *
     * @param lectureElement an element that belongs on a lecture
     * @return a Document of the element
     */
    public static Document createQueryFromElement(final Problem.ProblemElement lectureElement) {
        final Document query = new Document(SELF_ID, lectureElement.getId())
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
     * @param authenticator the object that is performing authentication.
     * @param dbs The coursesketch.util.util where the assignment is being stored.
     * @param userId The id of the user that asking to insert the lecture.
     * @param slideId the id of the lecture that is being grabbed.
     * @param checkTime The time that the assignment was asked to be grabbed. (used to
     * check if the slide is valid)
     * @return The slide from the coursesketch.util.util.
     * @throws AuthenticationException Thrown if the user did not have the authentication to get the
     * slide.
     * @throws DatabaseAccessException Thrown if there are problems retrieving the slide.
     */
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity" })
    public static LectureSlide mongoGetLectureSlide(final Authenticator authenticator, final MongoDatabase dbs,
            final String userId, final String slideId, final long checkTime) throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> collection = dbs.getCollection(getCollectionFromType(Util.ItemType.SLIDE));
        final Document cursor = collection.find(convertStringToObjectId(slideId)).first();
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
                .checkAuthentication(Util.ItemType.ASSIGNMENT, (String) cursor.get(ASSIGNMENT_ID), userId, 0, authType);

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
        final Problem.LectureSlide.Builder exactSlide = Problem.LectureSlide.newBuilder();

        exactSlide.setId(slideId);

        // sets the majority of the slide data
        setSlideData(exactSlide, cursor);

        return exactSlide.build();
    }

    /**
     * Updates data from an assignment.
     *
     * @param authenticator the object that is performing authentication.
     * @param dbs The coursesketch.util.util where the lecture slide is being stored.
     * @param assignmentId the id of the lecture slide that is being updated.
     * @param userId The id of the user that asking to update the lecture slide.
     * @param lectureSlide The lecture slide that is being updated.
     * @return true if the lecture slide was updated successfully.
     * @throws AuthenticationException The user does not have permission to update the lecture slide.
     * @throws DatabaseAccessException The lecture does not exist.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static boolean mongoUpdateLectureSlide(final Authenticator authenticator, final MongoDatabase dbs,
            final String assignmentId, final String userId,
            final LectureSlide lectureSlide) throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final MongoCollection<Document> collection = dbs.getCollection(getCollectionFromType(Util.ItemType.SLIDE));
        final Document cursor = collection.find(convertStringToObjectId(lectureSlide.getId())).first();

        final Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.ASSIGNMENT, assignmentId, userId, 0, authType);

        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }
        // TODO make a way to clear out a lecture slide so it is empty?
        if (lectureSlide.getElementsCount() > 0) {
            final List<Document> list = new ArrayList<>();
            for (Problem.ProblemElement element : lectureSlide.getElementsList()) {
                list.add(createQueryFromElement(element));
            }
            collection.updateOne(cursor, new Document(SET_COMMAND, new Document(ELEMENT_LIST, list)));
            update = true;
        }
        return update;
    }

    /**
     * sets data of the slide from the given cursor.
     *
     * @param exactSlide The lecture that the data is being set to.
     * @param cursor The coursesketch.util.util cursor pointing to a specific slide.
     * @throws coursesketch.database.util.DatabaseAccessException passes exception through to createElementFromQuery
     */
    public static void setSlideData(final Problem.LectureSlide.Builder exactSlide, final Document cursor) throws DatabaseAccessException {
        exactSlide.setAssignmentId(cursor.get(DatabaseStringConstants.ASSIGNMENT_ID).toString());
        exactSlide.setId(cursor.get(SELF_ID).toString());
        if (cursor.get(ELEMENT_LIST) != null) {
            final ArrayList<Problem.ProblemElement> objects = new ArrayList<>();
            for (Document element : (List<Document>) cursor.get(ELEMENT_LIST)) {
                objects.add(createElementFromQuery(element));
            }
            exactSlide.addAllElements(objects);
        }
    }

    /**
     * NOTE: This function is only used internally and should not be made public.
     *
     * @param query a Document from the mongo coursesketch.util.util that is a slide
     * @return a Problem.ProblemElement of the Document that was passed in
     * @throws coursesketch.database.util.DatabaseAccessException a DatabaseAccessException if something goes wrong parsing a blob of a LectureElement
     */
    public static Problem.ProblemElement createElementFromQuery(final Document query) throws DatabaseAccessException {
        final Problem.ProblemElement.Builder element = Problem.ProblemElement.newBuilder();
        final String lectureElementId = (String) query.get(SELF_ID);
        final int xPos = (int) query.get(X_POSITION);
        final int yPos = (int) query.get(Y_POSITION);
        final int xDim = (int) query.get(X_DIMENSION);
        final int yDim = (int) query.get(Y_DIMENSION);
        final Problem.ProblemElement.ElementTypeCase blobType =
                Problem.ProblemElement.ElementTypeCase.valueOf((int) query.get(SLIDE_BLOB_TYPE));
        final byte[] blob = ((Binary) query.get(SLIDE_BLOB)).getData();
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
                    element.setImage(Problem.Image.parseFrom(blob));
                    break;
                case SKETCHAREA:
                    element.setSketchArea(QuestionDataOuterClass.SketchArea.parseFrom(blob));
                    break;
                case EMBEDDEDHTML:
                    element.setEmbeddedHtml(QuestionDataOuterClass.EmbeddedHtml.parseFrom(blob));
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
