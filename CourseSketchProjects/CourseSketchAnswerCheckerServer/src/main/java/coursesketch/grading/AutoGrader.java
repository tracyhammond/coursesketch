package coursesketch.grading;

import coursesketch.database.AnswerCheckerDatabase;
import protobuf.srl.question.QuestionDataOuterClass;
import protobuf.srl.submission.Feedback;
import protobuf.srl.submission.Submission;
import protobuf.srl.utils.Util;

public class AutoGrader {

    private AnswerCheckerDatabase database;

    public AutoGrader(AnswerCheckerDatabase database) {

        this.database = database;
    }
    public Feedback.SubmissionFeedback gradeProblem(Submission.SrlExperiment experiment, Submission.SrlSolution solution) {
        QuestionDataOuterClass.QuestionData experimentQuestionData = experiment.getSubmission().getSubmissionData();
        QuestionDataOuterClass.QuestionData solutionQuestionData = solution.getSubmission().getSubmissionData();

        Feedback.FeedbackData.Builder builder = gradeProblem(experimentQuestionData, solutionQuestionData, solution.getProblemDomain());
        builder.setGrade(builder.getGradeBuilder()
                .setUserId(experiment.getUserId())
                .setCourseId(experiment.getCourseId())
                .setAssignmentId(experiment.getAssignmentId())
                .setProblemId(experiment.getProblemId())
                .setPartId(experiment.getPartId()));

        return Feedback.SubmissionFeedback.newBuilder().setFeedbackData(builder).build();
    }

    private Feedback.FeedbackData.Builder gradeProblem(QuestionDataOuterClass.QuestionData experimentQuestionData,
            QuestionDataOuterClass.QuestionData solutionQuestionData, Util.DomainId rubricId) {
        Feedback.FeedbackData.Builder data;
        switch(rubricId.getQuestionType()) {
            case SKETCH:
                data = gradeSketch(experimentQuestionData, solutionQuestionData, rubricId);
                break;
            case MULT_CHOICE:
                data =  gradeMultipleChoice(experimentQuestionData, solutionQuestionData, rubricId);
                break;
            case FREE_RESP:
                data = gradeFreeResponse(experimentQuestionData, solutionQuestionData, rubricId);
                break;
            case CHECK_BOX:
                data = gradeCheckbox(experimentQuestionData, solutionQuestionData, rubricId);
                break;
            default:
                data = Feedback.FeedbackData.newBuilder();
        }
        return data;
    }

    private Feedback.FeedbackData.Builder gradeCheckbox(QuestionDataOuterClass.QuestionData experimentQuestionData,
            QuestionDataOuterClass.QuestionData solutionQuestionData, Util.DomainId rubricId) {
        return null;
    }

    private Feedback.FeedbackData.Builder gradeFreeResponse(QuestionDataOuterClass.QuestionData experimentQuestionData,
            QuestionDataOuterClass.QuestionData solutionQuestionData, Util.DomainId rubricId) {
        return null;
    }

    private Feedback.FeedbackData.Builder gradeMultipleChoice(QuestionDataOuterClass.QuestionData experimentQuestionData,
            QuestionDataOuterClass.QuestionData solutionQuestionData, Util.DomainId rubricId) {
        return null;
    }

    private Feedback.FeedbackData.Builder gradeSketch(QuestionDataOuterClass.QuestionData experimentQuestionData,
            QuestionDataOuterClass.QuestionData solutionQuestionData, Util.DomainId rubricId) {
        return null;
    }
}
