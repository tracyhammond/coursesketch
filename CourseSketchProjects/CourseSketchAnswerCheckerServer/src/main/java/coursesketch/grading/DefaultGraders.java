package coursesketch.grading;

import com.google.protobuf.GeneratedMessage;
import protobuf.srl.grading.Rubric;
import protobuf.srl.submission.Feedback;

import static coursesketch.grading.AbstractGrader.createBasicFeedback;
import static coursesketch.grading.AbstractGrader.createGrade;

/**
 * A set of useful graders when a specific one does not work.
 */
@SuppressWarnings("PMD.UncommentedEmptyMethod")
final class DefaultGraders {

    /**
     * private constructor.
     */
    private DefaultGraders() { }

    /**
     * @return a grader for invalid data.
     */
    static AbstractGrader buildMissingExperimentDataGrader() {
        return new AbstractGrader() {
            @Override
            public void setExperiment(GeneratedMessage experiment) {

            }

            @Override
            public void setSolution(GeneratedMessage solution) {

            }

            @Override
            protected Feedback.FeedbackData.Builder gradeProblem(Rubric.GradingRubric rubric) throws GradingException {
                return buildMissingExperimentData();
            }
        };
    }

    /**
     * @return a grader for invalid data.
     * @param throwable A random exception that occurred while building the grader.
     */
    static AbstractGrader buildThrowExceptionGrader(Throwable throwable) {
        return new AbstractGrader() {
            @Override
            public void setExperiment(GeneratedMessage experiment) {

            }

            @Override
            public void setSolution(GeneratedMessage solution) {

            }

            @Override
            protected Feedback.FeedbackData.Builder gradeProblem(Rubric.GradingRubric rubric) throws GradingException {
                throw new GradingException(throwable);
            }
        };
    }


    /**
     * @return feedback for invalid data.
     *
     * @throws GradingException Should not be thrown by this method.
     */
    private static Feedback.FeedbackData.Builder buildMissingExperimentData() throws GradingException {
        return Feedback.FeedbackData.newBuilder()
                .setGrade(createGrade(0))
                .setFeedbackState(Feedback.FeedbackState.INCORRECT)
                .setBasicFeedback(createBasicFeedback("No experiment data submitted"));
    }

}
