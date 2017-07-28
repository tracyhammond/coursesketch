package coursesketch.database.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.bson.Document;
import org.bson.types.Binary;
import protobuf.srl.commands.Commands;
import protobuf.srl.question.QuestionDataOuterClass;
import protobuf.srl.question.QuestionDataOuterClass.EmbeddedHtml;
import protobuf.srl.question.QuestionDataOuterClass.FreeResponse;
import protobuf.srl.question.QuestionDataOuterClass.MultipleChoice;
import protobuf.srl.question.QuestionDataOuterClass.QuestionData;
import protobuf.srl.question.QuestionDataOuterClass.SketchArea;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.ANSWER_CHOICES;
import static coursesketch.database.util.DatabaseStringConstants.EMBEDDED_HTML;
import static coursesketch.database.util.DatabaseStringConstants.ITEM_ID;
import static coursesketch.database.util.DatabaseStringConstants.MULTIPLE_CHOICE_DISPLAY_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.QUESTION_TEXT;
import static coursesketch.database.util.DatabaseStringConstants.SELECTED_ANSWER_CHOICES;
import static coursesketch.database.util.DatabaseStringConstants.TEXT_ANSWER;
import static coursesketch.database.util.DatabaseStringConstants.UPDATELIST;
import static coursesketch.database.util.MongoUtilities.appendQuestionTypeToDocument;
import static coursesketch.database.util.MongoUtilities.getNonNullList;
import static coursesketch.database.util.MongoUtilities.getQuestionType;

/**
 * Builds question data for the servers.
 */
@SuppressWarnings("checkstyle:designforextension")
public class MongoQuestionDataBuilder {

    /**
     * Retrieves the questionData portion of the document.
     *
     * @param questionDocument The database pointer to the data.
     * @return {@link protobuf.srl.submission.Submission.SrlSubmission} the resulting submission.
     * @throws DatabaseAccessException Thrown if there are issues getting the questionData.
     */
    public QuestionData buildQuestionDataProto(final Document questionDocument) throws DatabaseAccessException {
        final QuestionData.Builder questionData = QuestionData.newBuilder();

        switch (getQuestionType(questionDocument)) {
            case SKETCHAREA:
                questionData.setSketchArea(buildSketchAreaProto(questionDocument));
                break;
            case FREERESPONSE:
                questionData.setFreeResponse(buildFreeResponseProto(questionDocument));
                break;
            case MULTIPLECHOICE:
                questionData.setMultipleChoice(buildMultipleChoiceProto(questionDocument));
                break;
            case EMBEDDEDHTML:
                questionData.setEmbeddedHtml(buildEmbeddedHtmlProto(questionDocument));
                break;
            default:
                throw new DatabaseAccessException("question data is not supported type or does not exist", null);
        }
        return questionData.build();
    }

    /**
     * Builds a multiple choice proto from the server data.
     *
     * @param document A {@link Document} that contains free response data.
     * @return A proto built by the server.
     * @throws DatabaseAccessException Throw if the data does not exist.
     */
    private FreeResponse buildFreeResponseProto(Document document) throws DatabaseAccessException {
        final Object text = document.get(TEXT_ANSWER);
        if (text == null) {
            throw new DatabaseAccessException("Text answer did not contain any data", null);
        }
        return FreeResponse.newBuilder().setStartingText(text.toString()).build();
    }

    /**
     * Builds a multiple choice proto from the server data.
     *
     * @param document A {@link Document} that contains embeddedhtml data.
     * @return A proto built by the server.
     * @throws DatabaseAccessException Throw if there is a problem parsing the embedded html
     */
    private EmbeddedHtml buildEmbeddedHtmlProto(Document document) throws DatabaseAccessException {
        final Object embeddedHtmlBinary = document.get(EMBEDDED_HTML);
        if (embeddedHtmlBinary == null) {
            throw new DatabaseAccessException("Embedded html did not contain any data", null);
        }
        try {
            return EmbeddedHtml.parseFrom(
                    ByteString.copyFrom(((Binary) embeddedHtmlBinary).getData()));
        } catch (InvalidProtocolBufferException e) {
            throw new DatabaseAccessException("Error decoding embedded html", e);
        }
    }

    /**
     * Builds a multiple choice proto from the server data.
     *
     * @param document A {@link Document} that contains sketch area data.
     * @return A proto built by the server.
     * @throws DatabaseAccessException Throw if there is no sketch area data in the document.
     */
    private SketchArea buildSketchAreaProto(Document document) throws DatabaseAccessException {
        final Object binary = document.get(UPDATELIST);
        if (binary == null) {
            throw new DatabaseAccessException("UpdateList did not contain any data", null);
        }
        try {
            final Commands.SrlUpdateList updateList = Commands.SrlUpdateList.parseFrom(ByteString.copyFrom(((Binary) binary).getData()));
            return SketchArea.newBuilder().setRecordedSketch(updateList).build();
        } catch (InvalidProtocolBufferException e) {
            throw new DatabaseAccessException("Error decoding update list", e);
        }
    }

    /**
     * Builds a multiple choice proto from the server data.
     *
     * @param questionDocument A {@link Document} that contains multiple choice data.
     * @return A proto built by the server.
     * @throws DatabaseAccessException Throw if there is no multiple choice data in the document.
     */
    private MultipleChoice buildMultipleChoiceProto(Document questionDocument) throws DatabaseAccessException {

        final List<String> selectedChoices = getNonNullList(questionDocument, SELECTED_ANSWER_CHOICES);
        final List<Document> listOfAnswerChoices = getNonNullList(questionDocument, ANSWER_CHOICES);
        if (selectedChoices.isEmpty() && listOfAnswerChoices.isEmpty()) {
            throw new DatabaseAccessException("MultipleChoice answer did not contain any data", null);
        }

        final MultipleChoice.Builder multipleChoice = MultipleChoice.newBuilder();

        if (questionDocument.containsKey(MULTIPLE_CHOICE_DISPLAY_TYPE)) {
            multipleChoice.setDisplayType(
                    QuestionDataOuterClass.MultipleChoiceDisplayType
                            .valueOf(questionDocument.getInteger(MULTIPLE_CHOICE_DISPLAY_TYPE)));
        }

        if (!listOfAnswerChoices.isEmpty()) {
            multipleChoice.addAllAnswerChoices(getAnswerChoices(listOfAnswerChoices));
        }

        if (!selectedChoices.isEmpty()) {
            multipleChoice.addAllSelectedIds(selectedChoices);
        }
        return multipleChoice.build();
    }

    /**
     * Creates an answer choice from an answer choice document.
     *
     * @param answerChoices A list of answer choice documents
     * @return A list of {@link QuestionDataOuterClass.AnswerChoice} objects.
     */
    private Iterable<? extends QuestionDataOuterClass.AnswerChoice> getAnswerChoices(List<Document> answerChoices) {
        final List<QuestionDataOuterClass.AnswerChoice> protoAnswerChoices = new ArrayList<>();
        for (Document answerChoice : answerChoices) {
            protoAnswerChoices.add(QuestionDataOuterClass.AnswerChoice.newBuilder()
                    .setId(answerChoice.getString(ITEM_ID))
                    .setText(answerChoice.getString(QUESTION_TEXT))
                    .build());
        }
        return protoAnswerChoices;
    }

    /**
     * Creates a database object for the submission object.  Handles certain values in certain ways if they exist.
     *
     * @param questionData The questionData that is being inserted.
     * @return An object that represents how it would be stored in the database.
     * @throws DatabaseAccessException Thrown if there are problems creating the document.
     */
    public Document createSubmission(final QuestionDataOuterClass.QuestionData questionData)
            throws DatabaseAccessException {
        Document document;

        switch (questionData.getElementTypeCase()) {
            case SKETCHAREA:
                document = createUpdateList(questionData.getSketchArea());
                break;
            case FREERESPONSE:
                document = createFreeResponseDocument(questionData.getFreeResponse());
                break;
            case MULTIPLECHOICE:
                document = createMultipleChoiceDocument(questionData.getMultipleChoice());
                break;
            case EMBEDDEDHTML:
                document = new Document(EMBEDDED_HTML, new Binary(questionData.getEmbeddedHtml().toByteArray()));
                break;
            default:
                throw new DatabaseAccessException("Tried to save invalid Question Data", null);
        }
        appendQuestionTypeToDocument(questionData.getElementTypeCase(), document);
        return document;
    }

    /**
     * Creates an update list document from the {@link SketchArea}.
     *
     * @param sketchArea The sketch area that contains the sketch data.
     * @return A document represented by this data.
     * @throws DatabaseAccessException Thrown if there are problems creating the update list.
     */
    protected Document createUpdateList(SketchArea sketchArea) throws DatabaseAccessException {
        return new Document(UPDATELIST, new Binary(sketchArea.getRecordedSketch().toByteArray()));
    }

    /**
     * Creates a database object for the text submission.
     *
     * @param submission The submission that is being inserted.
     * @return An object that represents how it would be stored in the database.
     */
    private Document createFreeResponseDocument(final QuestionDataOuterClass.FreeResponse submission) {
        // don't store it as changes for right now.
        return new Document(TEXT_ANSWER, submission.getStartingText());
    }

    /**
     * Creates a database object for the multiple choice multipleChoice.
     *
     * @param multipleChoice The multipleChoice that is being inserted.
     * @return An object that represents how it would be stored in the database.
     */
    private Document createMultipleChoiceDocument(final MultipleChoice multipleChoice) {
        // don't store it as changes for right now.
        final Document document = new Document();
        if (multipleChoice.hasDisplayType()) {
            document.append(MULTIPLE_CHOICE_DISPLAY_TYPE, multipleChoice.getDisplayType().getNumber());
        }
        if (multipleChoice.getSelectedIdsCount() > 0) {
            document.append(SELECTED_ANSWER_CHOICES, multipleChoice.getSelectedIdsList());
        }
        if (multipleChoice.getAnswerChoicesCount() > 0) {
            final List<Document> answerChoices = new ArrayList<>();
            for (QuestionDataOuterClass.AnswerChoice choice : multipleChoice.getAnswerChoicesList()) {
                answerChoices.add(new Document(ITEM_ID, choice.getId()).append(QUESTION_TEXT, choice.getText()));
            }
            document.append(ANSWER_CHOICES, answerChoices);
        }
        return document;
    }
}
