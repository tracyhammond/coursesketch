package coursesketch.grading;

import coursesketch.database.AnswerCheckerDatabase;
import protobuf.srl.grading.Rubric;
import protobuf.srl.question.QuestionDataOuterClass;
import protobuf.srl.submission.Feedback;
import protobuf.srl.utils.Util;

/**
 * A grader created for grading multiple choice problems.
 */
public class MultipleChoiceGrader extends AbstractGrader<QuestionDataOuterClass.MultipleChoice> {
    /**
     * The student multiple choice answer.
     */
    private QuestionDataOuterClass.MultipleChoice experiment;
    /**
     * The instructor multiple choice answer.
     */
    private QuestionDataOuterClass.MultipleChoice solution;

    @Override
    public final Rubric.GradingRubric.Builder loadRubricBuilder(Util.DomainId rubricId, AnswerCheckerDatabase database) {
        return super.loadRubricBuilder(rubricId, database);
    }

    @Override
    public final void setExperiment(QuestionDataOuterClass.MultipleChoice experiment) {
        this.experiment = experiment;
    }

    @Override
    public final void setSolution(QuestionDataOuterClass.MultipleChoice solution) {
        this.solution = solution;
    }

    @Override
    public final Feedback.FeedbackData.Builder gradeProblem(Rubric.GradingRubric gradingRubric) throws GradingException {
        final String studentAnswer = this.experiment.getCorrectId();
        final String correctAnswer = this.solution.getCorrectId();

        if (correctAnswer.equals(studentAnswer)) {
            return createCorrectFeedback();
        }
        final Feedback.MultipleChoiceFeedback incorrectFeedback = createIncorrectFeedback(studentAnswer, gradingRubric);
        return Feedback.FeedbackData.newBuilder()
                .setBasicFeedback(createBasicFeedback(createFeedbackString(studentAnswer, correctAnswer)))
                .setGrade(createGrade(0.0f))
                .setFeedbackState(Feedback.FeedbackState.INCORRECT)
                .setMultipleChoice(incorrectFeedback);
    }

    /**
     * Creates a feedback message to display for the user.
     *
     * @param studentAnswer The answer the student gave.
     * @param correctAnswer The answer the instructor gave.
     * @return A feedback message to display for the user.
     */
    private String createFeedbackString(String studentAnswer, String correctAnswer) {
        // Also need to do some data about what feedback can be sent back
        return "Student Put [" + studentAnswer + "] But Correct Answer was [" + correctAnswer + "]";
    }

    /**
     * Creates special feedback just for multiple choice questions.
     *
     * @param correctChoice The correct answer.
     * @param gradingRubric The rubric that is being graded with.
     * @return special feedback just for multiple choice questions.
     */
    private Feedback.MultipleChoiceFeedback createIncorrectFeedback(String correctChoice, Rubric.GradingRubric gradingRubric) {
        // Go through the rubric and find good feedback!
        // Also need to do some data about what feedback can be sent back

        if (gradingRubric.getLimitations().getNoCorrectInfo()) {
            return Feedback.MultipleChoiceFeedback.getDefaultInstance();
        }
        return Feedback.MultipleChoiceFeedback.newBuilder()
                .setMultipleChoice(QuestionDataOuterClass.MultipleChoice.newBuilder()
                        .setCorrectId(correctChoice).build())
                .build();
    }
}
