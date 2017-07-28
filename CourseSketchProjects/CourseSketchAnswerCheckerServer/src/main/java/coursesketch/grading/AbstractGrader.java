package coursesketch.grading;

import com.google.protobuf.GeneratedMessage;
import coursesketch.database.RubricDataHandler;
import protobuf.srl.grading.Grading;
import protobuf.srl.grading.Rubric;
import protobuf.srl.submission.Feedback;
import protobuf.srl.utils.Util;

/**
 * A parent class to all graders.
 * Used to grade special types of problems.
 *
 * @param <T> The type of element that is being graded
 */
@SuppressWarnings("checkstyle:designforextension")
public abstract class AbstractGrader<T extends GeneratedMessage> {

    /**
     * Loads a rubric for building for internal use.
     *
     * @param rubricId The domain id of the rubric
     * @param database Where the rubric is stored.
     * @return A builder representing a loaded rubric.
     */
    protected Rubric.GradingRubric.Builder loadRubricBuilder(Util.DomainId rubricId, RubricDataHandler database) {
        return database.loadRubric(rubricId);
    }

    /**
     * Sets the experiment locally.
     * @param experiment Students submission.
     */
    public abstract void setExperiment(T experiment);
    /**
     * Sets the solution locally.
     * @param solution Instructors submission.
     */
    public abstract void setSolution(T solution);

    /**
     * Builds a rubric for for grading.
     *
     * @param rubricId The domain id of the rubric
     * @param database Where the rubric is stored.
     * @return A loaded rubric.
     */
    final Rubric.GradingRubric buildRubric(Util.DomainId rubricId, RubricDataHandler database) {
        return database.loadRubric(rubricId).build();
    }

    /**
     * Grades the problem according to the rubric and responds with feedback.
     *
     * @param rubric How the feedback should be constructed.
     * @return {@link Feedback.FeedbackData}
     * @throws GradingException Thrown if there are problems while grading
     */
    protected abstract Feedback.FeedbackData.Builder gradeProblem(Rubric.GradingRubric rubric) throws GradingException;

    /**
     * Grades the problem according to the rubric and responds with feedback.
     *
     * @return {@link Feedback.FeedbackData}
     * @throws GradingException Thrown if there are problems while creating feedback
     */
    static Feedback.FeedbackData.Builder createCorrectFeedback() throws GradingException {
        return Feedback.FeedbackData.newBuilder()
                .setBasicFeedback(createBasicFeedback("Correct!"))
                .setGrade(createGrade(1.0f))
                .setFeedbackState(Feedback.FeedbackState.CORRECT);
    }

    /**
     * All grades from the answer checker server are not scaled.
     *
     * @param grade The raw grade percentage.
     * @return A grade.
     * @throws GradingException Thrown if there are problems while creating the grade
     */
    static Grading.ProtoGrade.Builder createGrade(final float grade) throws GradingException {
        if (grade > 1) {
            throw new GradingException("Cant submit a grade larger than 1");
        }
        if (grade < 0) {
            throw new GradingException("Cant submit a grade less than 0");
        }
        return Grading.ProtoGrade.newBuilder().setUnscaledGrade(grade);
    }

    /**
     * @param simpleFeedback simple feedback.
     * @return Feedback used for simple displaying.
     */
    static Feedback.BasicFeedback createBasicFeedback(final String simpleFeedback) {
        return Feedback.BasicFeedback.newBuilder().setFeedbackMessage(simpleFeedback).build();
    }

    /**
     * Checks if the experiment and solution is valid.
     * @param experiment Experiment Data
     * @param solution Solution Data
     * @throws GradingException Thrown if data is not valid.
     */
    protected final void checkValidGradeData(T experiment, T solution) throws GradingException {
        if (solution == null || experiment == null) {
            throw new GradingException("Invalid solution = " + (solution == null) + " or experiment = " + (experiment == null));
        }
    }
}
