package coursesketch.grading;

import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import coursesketch.database.RubricDataHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.grading.Grading;
import protobuf.srl.grading.Rubric;
import protobuf.srl.question.QuestionDataOuterClass;
import protobuf.srl.submission.Feedback;
import protobuf.srl.submission.Submission;
import protobuf.srl.utils.Util;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutoGraderTest {

    @Mock
    private RubricDataHandler database;

    private static final String PROBLEM_ID = "problem1";
    private static final String USER_ID = "user1";
    private static final String COURSE_ID = "user1";
    private static final String ASSIGNMENT_ID = "user1";
    private static final String BANKPROBLEM_ID = "user1";
    private static final String CORRECT_ID = "CORRECT";
    private static final String INCORRECT_ID = "WRONG";

    private QuestionDataOuterClass.MultipleChoice experiment;
    private Submission.SrlExperiment.Builder srlExperiment;
    private Submission.SrlSolution.Builder srlSolution;
    private QuestionDataOuterClass.MultipleChoice solution;
    private Rubric.GradingRubric rubric;
    private Feedback.FeedbackData.Builder correctFeedback;
    private AutoGrader grader;

    @Before
    public void setup() throws GradingException {
        experiment = QuestionDataOuterClass.MultipleChoice.newBuilder()
                .addSelectedIds(CORRECT_ID).build();
        solution = QuestionDataOuterClass.MultipleChoice.newBuilder()
                .addSelectedIds(CORRECT_ID).build();
        rubric = Rubric.GradingRubric.newBuilder().build();
        correctFeedback = AbstractGrader.createCorrectFeedback();
        grader = new AutoGrader(database);

        srlExperiment = Submission.SrlExperiment.newBuilder()
                .setProblemId(PROBLEM_ID)
                .setUserId(USER_ID)
                .setAssignmentId(ASSIGNMENT_ID)
                .setCourseId(COURSE_ID)
                .setProblemBankId(BANKPROBLEM_ID);
        srlSolution = Submission.SrlSolution.newBuilder()
                .setProblemBankId(PROBLEM_ID)
                .setProblemDomain(Util.DomainId.getDefaultInstance());

        when(database.loadRubric(any(Util.DomainId.class))).thenReturn(rubric.toBuilder());
    }

    @Test
    public void gradingEmptyDataSetsExceptionFeedback() {
        final Feedback.SubmissionFeedback submissionFeedback = grader.gradeProblem(srlExperiment.build(), srlSolution.build());
        Assert.assertEquals(submissionFeedback.hasException(), true);
    }

    @Test
    public void gradingMultipleChoiceFeedback() {
        srlSolution.setProblemDomain(Util.DomainId.newBuilder().setQuestionType(Util.QuestionType.MULT_CHOICE).build());
        srlSolution.setSubmission(Submission.SrlSubmission.newBuilder()
                .setSubmissionData(QuestionDataOuterClass.QuestionData.newBuilder()
                        .setMultipleChoice(solution).build()).build());

        srlExperiment.setSubmission(Submission.SrlSubmission.newBuilder()
                .setSubmissionData(QuestionDataOuterClass.QuestionData.newBuilder()
                        .setMultipleChoice(experiment).build()).build());
        final Feedback.SubmissionFeedback submissionFeedback = grader.gradeProblem(srlExperiment.build(), srlSolution.build());
        Assert.assertEquals(submissionFeedback.hasException(), false);
        new ProtobufComparisonBuilder()
                .setIsDeepEquals(true)
                .setIgnoreListOrder(true).build()
                .equals(correctFeedback.setGrade(Grading.ProtoGrade.newBuilder()
                        .setUserId(USER_ID)
                        .setAssignmentId(ASSIGNMENT_ID)
                        .setCourseId(COURSE_ID)
                        .setProblemId(PROBLEM_ID)
                        .setUnscaledGrade(1.0f).build()).build(), submissionFeedback.getFeedbackData());
    }
}
