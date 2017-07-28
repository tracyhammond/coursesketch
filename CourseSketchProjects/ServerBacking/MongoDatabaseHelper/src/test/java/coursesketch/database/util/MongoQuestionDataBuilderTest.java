package coursesketch.database.util;

import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.google.protobuf.LazyStringArrayList;
import com.google.protobuf.UnmodifiableLazyStringList;
import org.bson.Document;
import org.bson.types.Binary;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import protobuf.srl.commands.Commands;
import protobuf.srl.question.QuestionDataOuterClass;

import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.ANSWER_CHOICES;
import static coursesketch.database.util.DatabaseStringConstants.EMBEDDED_HTML;
import static coursesketch.database.util.DatabaseStringConstants.ITEM_ID;
import static coursesketch.database.util.DatabaseStringConstants.MULTIPLE_CHOICE_DISPLAY_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.QUESTION_TEXT;
import static coursesketch.database.util.DatabaseStringConstants.SELECTED_ANSWER_CHOICES;
import static coursesketch.database.util.DatabaseStringConstants.TEXT_ANSWER;
import static coursesketch.database.util.DatabaseStringConstants.UPDATELIST;
import static coursesketch.database.util.MongoUtilities.getNonNullList;

@RunWith(JUnit4.class)
public class MongoQuestionDataBuilderTest {

    private QuestionDataOuterClass.QuestionData.Builder expectedQuestionData;
    private static final String FREE_RESPONSE_TEXT = "Hello";
    private static final String ANSWER_ID = "Hello2";

    @Before
    public void setup() {
        expectedQuestionData = QuestionDataOuterClass.QuestionData.newBuilder();
    }

    @Test
    public void buildFreeResponseDocument() throws DatabaseAccessException {
        expectedQuestionData.setFreeResponse(QuestionDataOuterClass.FreeResponse.newBuilder()
                .setStartingText(FREE_RESPONSE_TEXT).build());
        final Document submission = new MongoQuestionDataBuilder().createSubmission(expectedQuestionData.build());
        Assert.assertEquals(submission.get(TEXT_ANSWER), FREE_RESPONSE_TEXT);
    }

    @Test
    public void buildFreeResponseProto() throws DatabaseAccessException {
        expectedQuestionData.setFreeResponse(QuestionDataOuterClass.FreeResponse.newBuilder()
                .setStartingText(FREE_RESPONSE_TEXT).build());
        final QuestionDataOuterClass.QuestionData questionDataProto =
                new MongoQuestionDataBuilder().buildQuestionDataProto(
                        new MongoQuestionDataBuilder().createSubmission(this.expectedQuestionData.build()));
        new ProtobufComparisonBuilder().build().equals(expectedQuestionData.build(), questionDataProto);
    }

    @Test
    public void buildMultipleChoiceDocument() throws DatabaseAccessException {
        expectedQuestionData.setMultipleChoice(
                QuestionDataOuterClass.MultipleChoice.newBuilder()
                        .setDisplayType(QuestionDataOuterClass.MultipleChoiceDisplayType.MULTIPLE_CHOICE)
                        .addSelectedIds(ANSWER_ID)
                        .addAnswerChoices(QuestionDataOuterClass.AnswerChoice.newBuilder()
                                .setText(FREE_RESPONSE_TEXT)
                                .setId(ANSWER_ID).build())
                        .build());
        final Document submission = new MongoQuestionDataBuilder().createSubmission(expectedQuestionData.build());
        Assert.assertEquals((int) submission.getInteger(MULTIPLE_CHOICE_DISPLAY_TYPE),
                QuestionDataOuterClass.MultipleChoiceDisplayType.MULTIPLE_CHOICE.getNumber());
        Assert.assertEquals(submission.get(SELECTED_ANSWER_CHOICES,
                new UnmodifiableLazyStringList(LazyStringArrayList.EMPTY)).get(0), ANSWER_ID);
        final List<Document> documentList = getNonNullList(submission, ANSWER_CHOICES);
        Assert.assertEquals(documentList.get(0)
                .getString(ITEM_ID), ANSWER_ID);
        Assert.assertEquals(documentList.get(0)
                .getString(QUESTION_TEXT), FREE_RESPONSE_TEXT);
    }

    @Test
    public void buildMultipleChoiceProto() throws DatabaseAccessException {
        expectedQuestionData.setMultipleChoice(
                QuestionDataOuterClass.MultipleChoice.newBuilder()
                        .addSelectedIds(ANSWER_ID)
                        .setDisplayType(QuestionDataOuterClass.MultipleChoiceDisplayType.MULTIPLE_CHOICE)
                        .addAnswerChoices(QuestionDataOuterClass.AnswerChoice.newBuilder()
                                .setText(FREE_RESPONSE_TEXT)
                                .setId(ANSWER_ID).build())
                        .build());
        final QuestionDataOuterClass.QuestionData questionDataProto =
                new MongoQuestionDataBuilder().buildQuestionDataProto(
                        new MongoQuestionDataBuilder().createSubmission(this.expectedQuestionData.build()));
        new ProtobufComparisonBuilder().build().equals(expectedQuestionData.build(), questionDataProto);
    }

    @Test
    public void buildMultipleChoiceProtoMissingId() throws DatabaseAccessException {
        expectedQuestionData.setMultipleChoice(
                QuestionDataOuterClass.MultipleChoice.newBuilder()
                        .setDisplayType(QuestionDataOuterClass.MultipleChoiceDisplayType.MULTIPLE_CHOICE)
                        .addAnswerChoices(QuestionDataOuterClass.AnswerChoice.newBuilder()
                                .setText(FREE_RESPONSE_TEXT)
                                .setId(ANSWER_ID).build())
                        .build());
        final QuestionDataOuterClass.QuestionData questionDataProto =
                new MongoQuestionDataBuilder().buildQuestionDataProto(
                        new MongoQuestionDataBuilder().createSubmission(this.expectedQuestionData.build()));
        new ProtobufComparisonBuilder().build().equals(expectedQuestionData.build(), questionDataProto);
    }

    @Test
    public void buildMultipleChoiceProtoMissingAnswerChoices() throws DatabaseAccessException {
        expectedQuestionData.setMultipleChoice(
                QuestionDataOuterClass.MultipleChoice.newBuilder()
                        .setDisplayType(QuestionDataOuterClass.MultipleChoiceDisplayType.CHECKBOX)
                        .addSelectedIds(ANSWER_ID)
                        .build());
        final QuestionDataOuterClass.QuestionData questionDataProto =
                new MongoQuestionDataBuilder().buildQuestionDataProto(
                        new MongoQuestionDataBuilder().createSubmission(this.expectedQuestionData.build()));
        new ProtobufComparisonBuilder().build().equals(expectedQuestionData.build(), questionDataProto);
    }

    @Test(expected = DatabaseAccessException.class)
    public void buildMultipleChoiceProtoNoData() throws DatabaseAccessException {
        expectedQuestionData.setMultipleChoice(
                QuestionDataOuterClass.MultipleChoice.newBuilder()
                        .setDisplayType(QuestionDataOuterClass.MultipleChoiceDisplayType.CHECKBOX)
                        .build());
        final QuestionDataOuterClass.QuestionData questionDataProto =
                new MongoQuestionDataBuilder().buildQuestionDataProto(
                        new MongoQuestionDataBuilder().createSubmission(this.expectedQuestionData.build()));
        new ProtobufComparisonBuilder().build().equals(expectedQuestionData.build(), questionDataProto);
    }

    @Test
    public void buildSketchAreaDocument() throws DatabaseAccessException {
        final Commands.SrlUpdateList defaultInstance = Commands.SrlUpdateList.getDefaultInstance();
        expectedQuestionData.setSketchArea(QuestionDataOuterClass.SketchArea.newBuilder()
                .setRecordedSketch(defaultInstance).build());
        final Document submission = new MongoQuestionDataBuilder().createSubmission(expectedQuestionData.build());
        Assert.assertArrayEquals(submission.get(UPDATELIST, Binary.class).getData(), defaultInstance.toByteArray());
    }

    @Test
    public void buildSketchAreaProto() throws DatabaseAccessException {
        final Commands.SrlUpdateList defaultInstance = Commands.SrlUpdateList.getDefaultInstance();
        expectedQuestionData.setSketchArea(QuestionDataOuterClass.SketchArea.newBuilder()
                .setRecordedSketch(defaultInstance).build());
        final QuestionDataOuterClass.QuestionData questionDataProto =
                new MongoQuestionDataBuilder().buildQuestionDataProto(
                        new MongoQuestionDataBuilder().createSubmission(this.expectedQuestionData.build()));
        new ProtobufComparisonBuilder().build().equals(expectedQuestionData.build(), questionDataProto);
    }

    @Test
    public void buildEmbeddedHtmlDocument() throws DatabaseAccessException {
        final QuestionDataOuterClass.EmbeddedHtml defaultInstance = QuestionDataOuterClass.EmbeddedHtml.getDefaultInstance();
        expectedQuestionData.setEmbeddedHtml(defaultInstance);
        final Document submission = new MongoQuestionDataBuilder().createSubmission(expectedQuestionData.build());
        Assert.assertArrayEquals(submission.get(EMBEDDED_HTML, Binary.class).getData(), defaultInstance.toByteArray());
    }

    @Test
    public void buildEmbeddedHtmlProto() throws DatabaseAccessException {
        final QuestionDataOuterClass.EmbeddedHtml defaultInstance = QuestionDataOuterClass.EmbeddedHtml.getDefaultInstance();
        expectedQuestionData.setEmbeddedHtml(defaultInstance);
        final QuestionDataOuterClass.QuestionData questionDataProto =
                new MongoQuestionDataBuilder().buildQuestionDataProto(
                        new MongoQuestionDataBuilder().createSubmission(this.expectedQuestionData.build()));
        new ProtobufComparisonBuilder().build().equals(expectedQuestionData.build(), questionDataProto);
    }
}
