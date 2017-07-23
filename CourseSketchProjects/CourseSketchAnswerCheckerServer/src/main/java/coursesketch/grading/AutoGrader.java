package coursesketch.grading;

import coursesketch.database.AnswerCheckerDatabase;
import coursesketch.database.util.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.grading.Rubric;
import protobuf.srl.question.QuestionDataOuterClass;
import protobuf.srl.submission.Feedback;
import protobuf.srl.submission.Submission;
import protobuf.srl.utils.Util;

import static coursesketch.grading.DefaultGraders.buildMissingExperimentDataGrader;
import static coursesketch.grading.DefaultGraders.buildThrowExceptionGrader;
import static coursesketch.utilities.ExceptionUtilities.createProtoException;

/**
 * Automatically grades submissions.
 */
public class AutoGrader {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AutoGrader.class);

    /**
     * Used to get rubrics.
     */
    private final AnswerCheckerDatabase database;

    /**
     * Creates a new instance of the autograder with the database.
     *
     * @param database The database that holds the rubric.
     */
    public AutoGrader(final AnswerCheckerDatabase database) {
        this.database = database;
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experiment The student experiment.
     * @param solution The solution.
     * @return Feedback Feedback from grading.
     */
    public final Feedback.SubmissionFeedback gradeProblem(final Submission.SrlExperiment experiment, final Submission.SrlSolution solution) {
        final QuestionDataOuterClass.QuestionData experimentQuestionData = experiment.getSubmission().getSubmissionData();
        final QuestionDataOuterClass.QuestionData solutionQuestionData = solution.getSubmission().getSubmissionData();

        final Feedback.SubmissionFeedback.Builder submissionFeedback = Feedback.SubmissionFeedback.newBuilder();
        final AbstractGrader grader = createGrader(experimentQuestionData, solutionQuestionData, solution.getProblemDomain());
        final Rubric.GradingRubric gradingRubric = grader.buildRubric(solution.getProblemDomain(), database);
        try {
            final Feedback.FeedbackData.Builder builder = grader.gradeProblem(gradingRubric);

            // We don't want this to happen if an exception is thrown
            builder.setGrade(builder.getGradeBuilder()
                    .setUserId(experiment.getUserId())
                    .setCourseId(experiment.getCourseId())
                    .setAssignmentId(experiment.getAssignmentId())
                    .setProblemId(experiment.getProblemId())
                    .setPartId(experiment.getPartId()));
        } catch (GradingException exception) {
            if (exception.hasFeedbackData()) {
                submissionFeedback.setFeedbackData(exception.getFeedbackData());
            }
            // Can not be correct must be unknown
            submissionFeedback.getFeedbackDataBuilder().setFeedbackState(Feedback.FeedbackState.EXCEPTION);
            submissionFeedback.setException(createProtoException(exception));
        }
        submissionFeedback.setLimitations(gradingRubric.getLimitations());

        return submissionFeedback.build();
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment.
     * @param solutionQuestionData The solution.
     * @param rubricId The id of the rubric to be grabbed from the database.
     * @return Feedback Feedback from grading.
     */
    private AbstractGrader createGrader(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData, final Util.DomainId rubricId) {
        AbstractGrader grader;
        switch (rubricId.getQuestionType()) {
            case SKETCH:
                grader = getSketchGrader(experimentQuestionData, solutionQuestionData);
                break;
            case MULT_CHOICE:
                grader = getMultipleChoiceGrader(experimentQuestionData, solutionQuestionData);
                break;
            case FREE_RESP:
                grader = getFreeResponseGrader(experimentQuestionData, solutionQuestionData);
                break;
            case CHECK_BOX:
                grader = getCheckboxGrader(experimentQuestionData, solutionQuestionData);
                break;
            default:
                grader = buildMissingExperimentDataGrader();
                break;
        }
        return grader;
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment
     * @param solutionQuestionData The solution
     * @return Feedback Feedback from grading.
     */
    private AbstractGrader getCheckboxGrader(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData) {
        if (!experimentQuestionData.hasCheckBox()) {
            return buildMissingExperimentDataGrader();
        }
        LOG.info("Grade info {}{} {}", experimentQuestionData, solutionQuestionData);
        return buildThrowExceptionGrader(new UnsupportedOperationException("cant grade sketches"));
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment
     * @param solutionQuestionData The solution
     * @return Feedback Feedback from grading.
     */
    private AbstractGrader getFreeResponseGrader(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData) {
        if (!experimentQuestionData.hasMultipleChoice()) {
            return buildMissingExperimentDataGrader();
        }
        LOG.info("Grade info {} {} {}", experimentQuestionData, solutionQuestionData);
        return buildThrowExceptionGrader(new UnsupportedOperationException("cant grade sketches"));
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment
     * @param solutionQuestionData The solution
     * @return Feedback Feedback from grading.
     */
    private AbstractGrader getMultipleChoiceGrader(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData) {
        if (!experimentQuestionData.hasMultipleChoice()) {
            return buildMissingExperimentDataGrader();
        }
        if (!solutionQuestionData.hasMultipleChoice()) {
            return buildThrowExceptionGrader(new DatabaseAccessException("Solution has no multiple choice data"));
        }
        final MultipleChoiceGrader multipleChoiceGrader =
                new MultipleChoiceGrader();
        multipleChoiceGrader.setExperiment(experimentQuestionData.getMultipleChoice());
        multipleChoiceGrader.setSolution(solutionQuestionData.getMultipleChoice());
        return multipleChoiceGrader;
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment
     * @param solutionQuestionData The solution
     * @return Feedback Feedback from grading.
     */
    private AbstractGrader getSketchGrader(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData) {
        if (!experimentQuestionData.hasSketchArea()) {
            return buildMissingExperimentDataGrader();
        }
        LOG.info("Grade info {} {}  {}", experimentQuestionData, solutionQuestionData);
        return buildThrowExceptionGrader(new UnsupportedOperationException("cant grade sketches"));
    }
}
