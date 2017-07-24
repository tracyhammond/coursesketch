package coursesketch.database.util;

import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import protobuf.srl.commands.Commands;
import protobuf.srl.question.QuestionDataOuterClass;
import protobuf.srl.submission.Submission;
import protobuf.srl.utils.Util;

import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.*;

/**
 * Contains various Utilities for working with mongo databases.
 * Created by gigemjt on 9/6/15.
 */
public final class MongoUtilities {

    /**
     * Empty constructor.
     */
    private MongoUtilities() {
    }

    /**
     * Tries to convert a string into a mongo ObjectId.
     *
     * Throws a {@link DatabaseAccessException} if a valid id can not be created.
     * An id is valid if it is not null and conforms to the ObjectId Format.  It does not mean the id actually exist.
     * @param objectId The string id that we want convert to a mongo ObjectId.
     * @return {@link ObjectId} if it is successfully created.
     * @throws DatabaseAccessException Thrown if a valid id can not be created.
     */
    public static Document convertStringToObjectId(final String objectId) throws DatabaseAccessException {
        if (objectId == null) {
            throw new DatabaseAccessException(new IllegalArgumentException("Object Id was given a null parameter"), false);
        }
        try {
            return new Document(SELF_ID, new ObjectId(objectId.trim()));
        } catch (IllegalArgumentException e) {
            throw new DatabaseAccessException(e, false);
        }
    }

    /**
     * Creates the document for a domain id.
     *
     * @param domainId The domain id to be converted into recognition data.
     * @return {@link Document} containing a domainId
     */
    public static Document createDomainId(final Util.DomainId domainId) {
        return new Document();
    }

    /**
     * Gets the user group from the database.
     *
     * @param databaseResult The result from the database
     * @return The list of users.
     */
    public static List<String> getUserGroup(final Document databaseResult) {
        return (List<String>) databaseResult.get(DatabaseStringConstants.USER_LIST);
    }

    /**
     * Retrieves the questionData portion of the document
     *
     * @param questionDocument
     *         The database pointer to the data.
     * @param questionType
     *         The type of question it is.
     * @return {@link protobuf.srl.submission.Submission.SrlSubmission} the resulting submission.
     * @throws DatabaseAccessException
     *         Thrown if there are issues getting the questionData.
     */
    private static QuestionDataOuterClass.QuestionData getQuestionData(final Document questionDocument,
                                                            final QuestionDataOuterClass.QuestionData.ElementTypeCase questionType) throws DatabaseAccessException {
        final QuestionDataOuterClass.QuestionData.Builder questionData = QuestionDataOuterClass.QuestionData.newBuilder();
        switch (questionType) {
            case SKETCHAREA:
                final Object binary = questionDocument.get(UPDATELIST);
                if (binary == null) {
                    throw new DatabaseAccessException("UpdateList did not contain any data", null);
                }
                try {
                    final Commands.SrlUpdateList updateList = Commands.SrlUpdateList.parseFrom(ByteString.copyFrom(((Binary) binary).getData()));
                    questionData.setSketchArea(QuestionDataOuterClass.SketchArea.newBuilder().setRecordedSketch(updateList));
                } catch (InvalidProtocolBufferException e) {
                    throw new DatabaseAccessException("Error decoding update list", e);
                }
                break;
            case FREERESPONSE:
                final Object text = questionDocument.get(TEXT_ANSWER);
                if (text == null) {
                    throw new DatabaseAccessException("Text answer did not contain any data", null);
                }
                questionData.setFreeResponse(QuestionDataOuterClass.FreeResponse.newBuilder().setStartingText(text.toString()));
                break;
            case MULTIPLECHOICE:
                final Object answerChoice = questionDocument.get(ANSWER_CHOICE);
                if (answerChoice == null) {
                    throw new DatabaseAccessException("Text answer did not contain any data", null);
                }
                questionData.setMultipleChoice(QuestionDataOuterClass.MultipleChoice.newBuilder().setSelectedId(answerChoice.toString()));
                break;
            default:
                throw new DatabaseAccessException("Submission data is not supported type or does not exist", null);
        }
        return questionData.build();
    }
}
