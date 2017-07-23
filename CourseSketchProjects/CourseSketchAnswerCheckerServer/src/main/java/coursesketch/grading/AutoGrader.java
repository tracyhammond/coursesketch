package coursesketch.grading;

import coursesketch.database.AnswerCheckerDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.question.QuestionDataOuterClass;
import protobuf.srl.submission.Feedback;
import protobuf.srl.submission.Submission;
import protobuf.srl.utils.Util;

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

        final Feedback.FeedbackData.Builder builder = gradeProblem(experimentQuestionData, solutionQuestionData, solution.getProblemDomain());
        builder.setGrade(builder.getGradeBuilder()
                .setUserId(experiment.getUserId())
                .setCourseId(experiment.getCourseId())
                .setAssignmentId(experiment.getAssignmentId())
                .setProblemId(experiment.getProblemId())
                .setPartId(experiment.getPartId()));

        return Feedback.SubmissionFeedback.newBuilder().setFeedbackData(builder).build();
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment.
     * @param solutionQuestionData The solution.
     * @param rubricId The id of the rubric to be grabbed from the database.
     * @return Feedback Feedback from grading.
     */
    private Feedback.FeedbackData.Builder gradeProblem(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData, final Util.DomainId rubricId) {
        Feedback.FeedbackData.Builder data;
        switch (rubricId.getQuestionType()) {
            case SKETCH:
                data = gradeSketch(experimentQuestionData, solutionQuestionData, rubricId);
                break;
            case MULT_CHOICE:
                data = gradeMultipleChoice(experimentQuestionData, solutionQuestionData, rubricId);
                break;
            case FREE_RESP:
                data = gradeFreeResponse(experimentQuestionData, solutionQuestionData, rubricId);
                break;
            case CHECK_BOX:
                data = gradeCheckbox(experimentQuestionData, solutionQuestionData, rubricId);
                break;
            default:
                data = Feedback.FeedbackData.newBuilder();
                break;
        }
        return data;
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment
     * @param solutionQuestionData The solution
     * @param rubricId The id of the rubric to be grabbed from the database.
     * @return Feedback Feedback from grading.
     */
    private Feedback.FeedbackData.Builder gradeCheckbox(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData, final Util.DomainId rubricId) {
        LOG.info("Grade info {}{} {}", experimentQuestionData, solutionQuestionData, rubricId);
        return null;
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment
     * @param solutionQuestionData The solution
     * @param rubricId The id of the rubric to be grabbed from the database.
     * @return Feedback Feedback from grading.
     */
    private Feedback.FeedbackData.Builder gradeFreeResponse(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData, final Util.DomainId rubricId) {
        LOG.info("Grade info {} {} {}", experimentQuestionData, solutionQuestionData, rubricId);
        return null;
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment
     * @param solutionQuestionData The solution
     * @param rubricId The id of the rubric to be grabbed from the database.
     * @return Feedback Feedback from grading.
     */
    private Feedback.FeedbackData.Builder gradeMultipleChoice(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData, final Util.DomainId rubricId) {
        LOG.info("Grade info {} {} {} {}", experimentQuestionData, solutionQuestionData, rubricId, database);
        return null;
    }

    /**
     * Grades a problem and returns feedback.
     *
     * @param experimentQuestionData The student experiment
     * @param solutionQuestionData The solution
     * @param rubricId The id of the rubric to be grabbed from the database.
     * @return Feedback Feedback from grading.
     */
    private Feedback.FeedbackData.Builder gradeSketch(final QuestionDataOuterClass.QuestionData experimentQuestionData,
            final QuestionDataOuterClass.QuestionData solutionQuestionData, final Util.DomainId rubricId) {
        LOG.info("Grade info {} {}  {}", experimentQuestionData, solutionQuestionData, rubricId);
        return null;
    }
}
