package coursesketch.grading;

import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import coursesketch.database.RubricDataHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.grading.Grading;
import protobuf.srl.grading.Rubric;
import protobuf.srl.question.QuestionDataOuterClass;
import protobuf.srl.submission.Feedback;
import protobuf.srl.utils.Util;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MultipleChoiceGraderTest {

    private MultipleChoiceGrader multChoice;
    @Mock
    private RubricDataHandler database;

    private static final String CORRECT_ID = "CORRECT";
    private static final String INCORRECT_ID = "WRONG";

    private QuestionDataOuterClass.MultipleChoice experiment;
    private QuestionDataOuterClass.MultipleChoice solution;
    private Rubric.GradingRubric rubric;
    private Feedback.FeedbackData.Builder correctFeedback;

    @Before
    public void setup() throws GradingException {
        multChoice = new MultipleChoiceGrader();
        experiment = QuestionDataOuterClass.MultipleChoice.newBuilder()
                .addSelectedIds(CORRECT_ID).build();
        solution = QuestionDataOuterClass.MultipleChoice.newBuilder()
                .addSelectedIds(CORRECT_ID).build();
        rubric = Rubric.GradingRubric.newBuilder().build();
        correctFeedback = multChoice.createCorrectFeedback();
    }

    @Test
    public void testDatabaseCalledCorrectly() {
        Util.DomainId rubricId = Util.DomainId.newBuilder().build();
        multChoice.loadRubricBuilder(rubricId, database);
        verify(database).loadRubric(eq(rubricId));
    }

    @Test
    public void testCorrectGradeGivesValidFeedback() throws GradingException {
        multChoice.setSolution(solution);
        multChoice.setExperiment(experiment);
        final Feedback.FeedbackData.Builder builder = multChoice .gradeProblem(rubric);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(true)
                .setIgnoreListOrder(true).build()
                .equals(correctFeedback.build(), builder.build());
    }

    @Test(expected = GradingException.class)
    public void testMissingSolutionThrowsException() throws GradingException {
        multChoice.setExperiment(experiment);
        multChoice .gradeProblem(rubric);
    }
    @Test(expected = GradingException.class)
    public void testMissingExperimentThrowsException() throws GradingException {
        multChoice.setSolution(solution);
        multChoice .gradeProblem(rubric);
    }

    @Test
    public void testWrongAnswerGivesFeedback() throws GradingException {
        multChoice.setSolution(solution);

        experiment = QuestionDataOuterClass.MultipleChoice.newBuilder()
                .addSelectedIds(INCORRECT_ID).build();
        multChoice.setExperiment(experiment);
        Feedback.FeedbackData feedbackData = Feedback.FeedbackData.newBuilder()
                .setFeedbackState(Feedback.FeedbackState.INCORRECT)
                .setGrade(Grading.ProtoGrade.newBuilder().setUnscaledGrade(0).build())
                .setMultipleChoice(Feedback.MultipleChoiceFeedback.newBuilder()
                        .setMultipleChoice(QuestionDataOuterClass.MultipleChoice.newBuilder()
                                .addSelectedIds(CORRECT_ID).build()).build())
                .build();
        final Feedback.FeedbackData.Builder builder = multChoice.gradeProblem(rubric);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(true)
                .ignoreField(Feedback.FeedbackData.getDescriptor(),
                        Feedback.FeedbackData.BASICFEEDBACK_FIELD_NUMBER)
                .setIgnoreListOrder(true).build()
                .equals(feedbackData, builder.build());
    }
}
