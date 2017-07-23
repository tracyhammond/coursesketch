package coursesketch;

import protobuf.srl.grading.Grading;
import protobuf.srl.submission.Feedback;
import protobuf.srl.submission.Feedback.SubmissionFeedback;

/**
 * Some utilities for filtering feedback.
 */
public final class FeedbackFilterUtils {

    /**
     * private constructor.
     */
    private FeedbackFilterUtils() { }

    /**
     * Creates a new builder from the existing feedback data.
     *
     * @param submissionFeedback Initial data.
     * @return A builder representing initial data..
     */
    private static SubmissionFeedback.Builder createNewInstance(SubmissionFeedback submissionFeedback) {
        return SubmissionFeedback.newBuilder(submissionFeedback);
    }

    /**
     * Creates a new builder from the existing feedback data.
     *
     * @param submissionFeedback Initial data.
     * @return A builder representing initial data.
     */
    public static SubmissionFeedback createFeedbackForUser(SubmissionFeedback submissionFeedback) {
        final SubmissionFeedback.Builder newInstance = createNewInstance(submissionFeedback);
        filterGrade(newInstance);
        filterWithLimitations(newInstance);
        removeLimitations(newInstance);
        return newInstance.build();
    }

    /**
     * Removes the limitation field.
     * @param feedbackBuilder Builder.
     */
    private static void removeLimitations(SubmissionFeedback.Builder feedbackBuilder) {
        feedbackBuilder.clearLimitations();
    }

    /**
     * Removes personal grade data.
     * @param feedbackBuilder Builder.
     */
    private static void filterGrade(SubmissionFeedback.Builder feedbackBuilder) {
        final Grading.ProtoGrade.Builder gradeBuilder = feedbackBuilder.getFeedbackDataBuilder().getGradeBuilder();
        final float unscaledGrade = gradeBuilder.getUnscaledGrade();
        gradeBuilder.clear().setUnscaledGrade(unscaledGrade);
    }

    /**
     * Removes feedback data based on the limitations.
     * @param feedbackBuilder Builder.
     */
    private static void filterWithLimitations(SubmissionFeedback.Builder feedbackBuilder) {
        final Feedback.FeedbackData.Builder feedbackDataBuilder = feedbackBuilder.getFeedbackDataBuilder();
        final Feedback.FeedbackLimitations limitations = feedbackBuilder.getLimitations();
        if (limitations.getOnlyState()) {
            final Feedback.FeedbackState feedbackState = feedbackDataBuilder.getFeedbackState();
            feedbackDataBuilder.clear().setFeedbackState(feedbackState);
        }
        if (limitations.getShowNothing()) {
            feedbackDataBuilder.clear().setFeedbackState(Feedback.FeedbackState.UNKOWN);
        }
        // More to be done as requirements become clear
    }
}
