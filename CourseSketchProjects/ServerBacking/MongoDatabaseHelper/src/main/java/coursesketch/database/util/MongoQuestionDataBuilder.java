package coursesketch.database.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.bson.Document;
import org.bson.types.Binary;
import protobuf.srl.commands.Commands;
import protobuf.srl.question.QuestionDataOuterClass;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.ANSWER_CHOICES;
import static coursesketch.database.util.DatabaseStringConstants.EMBEDDED_HTML;
import static coursesketch.database.util.DatabaseStringConstants.ITEM_ID;
import static coursesketch.database.util.DatabaseStringConstants.QUESTION_TEXT;
import static coursesketch.database.util.DatabaseStringConstants.SELECTED_ANSWER_CHOICES;
import static coursesketch.database.util.DatabaseStringConstants.TEXT_ANSWER;
import static coursesketch.database.util.DatabaseStringConstants.UPDATELIST;
import static coursesketch.database.util.MongoUtilities.appendQuestionTypeToDocument;
import static coursesketch.database.util.MongoUtilities.getQuestionType;

public class MongoQuestionDataBuilder {

    /**
     * Retrieves the questionData portion of the document
     *
     * @param questionDocument The database pointer to the data.
     * @return {@link protobuf.srl.submission.Submission.SrlSubmission} the resulting submission.
     * @throws DatabaseAccessException Thrown if there are issues getting the questionData.
     */
    public QuestionDataOuterClass.QuestionData buildQuestionDataProto(final Document questionDocument) throws DatabaseAccessException {
        final QuestionDataOuterClass.QuestionData.Builder questionData = QuestionDataOuterClass.QuestionData.newBuilder();

        switch (getQuestionType(questionDocument)) {
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
                questionData.setMultipleChoice(buildMultipleChoiceProto(questionDocument));
                break;
            case EMBEDDEDHTML:
                final Object embeddedHtmlBinary = questionDocument.get(EMBEDDED_HTML);
                if (embeddedHtmlBinary == null) {
                    throw new DatabaseAccessException("Embedded html did not contain any data", null);
                }
                try {
                    questionData.setEmbeddedHtml(QuestionDataOuterClass.EmbeddedHtml.parseFrom(
                            ByteString.copyFrom(((Binary) embeddedHtmlBinary).getData())));
                } catch (InvalidProtocolBufferException e) {
                    throw new DatabaseAccessException("Error decoding embedded html", e);
                }
                break;
            default:
                throw new DatabaseAccessException("question data is not supported type or does not exist", null);
        }
        return questionData.build();
    }

    private QuestionDataOuterClass.MultipleChoice buildMultipleChoiceProto(Document questionDocument) throws DatabaseAccessException {
        final List<String> selectedChoices = questionDocument.get(SELECTED_ANSWER_CHOICES, new ArrayList<String>());
        final List<Document> listOfAnswerChoices = questionDocument.get(ANSWER_CHOICES, new ArrayList<Document>());
        if (selectedChoices == null && listOfAnswerChoices == null) {
            throw new DatabaseAccessException("MultipleChoice answer did not contain any data", null);
        }

        final QuestionDataOuterClass.MultipleChoice.Builder multipleChoice = QuestionDataOuterClass.MultipleChoice.newBuilder();

        if (listOfAnswerChoices != null && listOfAnswerChoices.size() > 0) {
            multipleChoice.addAllAnswerChoices(getAnswerChoices(listOfAnswerChoices));
        }

        if (selectedChoices != null) {
            multipleChoice.addAllSelectedIds(selectedChoices);
        }
        return multipleChoice.build();
    }

    private Iterable<? extends QuestionDataOuterClass.AnswerChoice> getAnswerChoices(List<Document> answerChoices) {
        List<QuestionDataOuterClass.AnswerChoice> protoAnswerChoices = new ArrayList<>();
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
     */
    public Document createSubmission(final QuestionDataOuterClass.QuestionData questionData)
            throws DatabaseAccessException {
        Document document;

        switch (questionData.getElementTypeCase()) {
            case SKETCHAREA:
                document = createUpdateList(questionData.getSketchArea());
                break;
            case FREERESPONSE:
                document = createTextSubmission(questionData.getFreeResponse());
                break;
            case MULTIPLECHOICE:
                document = createMultipleChoiceSolution(questionData.getMultipleChoice());
                break;
            case EMBEDDEDHTML:
                document = new Document(EMBEDDED_HTML, questionData.getEmbeddedHtml().toByteArray());
                break;
            default:
                throw new DatabaseAccessException("Tried to save invalid Question Data", null);
        }
        appendQuestionTypeToDocument(questionData.getElementTypeCase(), document);
        return document;
    }

    protected Document createUpdateList(QuestionDataOuterClass.SketchArea sketchArea) throws DatabaseAccessException {
        return new Document(UPDATELIST, new Binary(sketchArea.getRecordedSketch().toByteArray()));
    }

    /**
     * Creates a database object for the text submission.
     *
     * @param submission The submission that is being inserted.
     * @return An object that represents how it would be stored in the database.
     */
    private Document createTextSubmission(final QuestionDataOuterClass.FreeResponse submission) {
        // don't store it as changes for right now.
        return new Document(TEXT_ANSWER, submission.getStartingText());
    }

    /**
     * Creates a database object for the multiple choice multipleChoice.
     *
     * @param multipleChoice The multipleChoice that is being inserted.
     * @return An object that represents how it would be stored in the database.
     */
    private Document createMultipleChoiceSolution(final QuestionDataOuterClass.MultipleChoice multipleChoice) {
        // don't store it as changes for right now.
        Document document = new Document();
        if (multipleChoice.getAnswerChoicesCount() > 0) {
            document.append(SELECTED_ANSWER_CHOICES, multipleChoice.getSelectedIdsList());
        }
        if (multipleChoice.getAnswerChoicesCount() > 0) {
            List<Document> answerChoices = new ArrayList<>();
            for (QuestionDataOuterClass.AnswerChoice choice : multipleChoice.getAnswerChoicesList()) {
                answerChoices.add(new Document(ITEM_ID, choice.getId()).append(QUESTION_TEXT, choice.getText()));
            }
            document.append(ANSWER_CHOICES, answerChoices);
        }
        return document;
    }
}
