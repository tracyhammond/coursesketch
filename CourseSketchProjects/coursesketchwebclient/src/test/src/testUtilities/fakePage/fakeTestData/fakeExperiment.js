(function() {
    CourseSketch.fakeExperiments = [];

    function createSubmissionFromSketch(updateList) {
        var questionData = CourseSketch.prutil.QuestionData();
        questionData.sketchArea = CourseSketch.prutil.SketchArea();
        questionData.sketchArea.setRecordedSketch(updateList);
        return questionData;
    }

    function createSubmissionFromText(textData) {
        var questionData = CourseSketch.prutil.QuestionData();
        questionData.freeResponse = CourseSketch.prutil.FreeResponse();
        questionData.freeResponse.startingText = textData;
        return questionData;
    }

    /*
     * tansfering the sketches into submissions to be used to make experiments
     * for testing purposes
     */
    var submission1 = CourseSketch.prutil.SrlSubmission();
    var submission2 = CourseSketch.prutil.SrlSubmission();
    var submission3 = CourseSketch.prutil.SrlSubmission();
    var submission4 = CourseSketch.prutil.SrlSubmission();
    var submission5 = CourseSketch.prutil.SrlSubmission();

    submission1.setSubmissionData(createSubmissionFromSketch(CourseSketch.fakeSketches[0]));
    submission2.setSubmissionData(createSubmissionFromSketch(CourseSketch.fakeSketches[1]));
    submission3.setSubmissionData(createSubmissionFromSketch(CourseSketch.fakeSketches[2]));
    submission4.setSubmissionData(createSubmissionFromText('This is a text answer I think I answered it correctly'));
    submission5.setSubmissionData(createSubmissionFromText('Here is a bad text answer I suck at answering questions'));
  //  submission4.setUpdateList(CourseSketch.fakeSketches[3]);
  //  submission5.setUpdateList(CourseSketch.fakeSketches[4]);

    /*
     * setting the submissions to experiments to be used as test data
     */

    var experiment1 = CourseSketch.prutil.SrlExperiment();
    var experiment2 = CourseSketch.prutil.SrlExperiment();
    var experiment3 = CourseSketch.prutil.SrlExperiment();
    var experiment4 = CourseSketch.prutil.SrlExperiment();
    var experiment5 = CourseSketch.prutil.SrlExperiment();

    experiment1.setSubmission(submission1);
    experiment2.setSubmission(submission2);
    experiment3.setSubmission(submission3);
    experiment4.setSubmission(submission4);
    experiment5.setSubmission(submission5);

    /*
     * assinging details to the experiments so we can seach for specific problems
     */

    experiment2.courseId = '1';
    experiment3.courseId = '1';
    experiment4.courseId = '1';
    experiment5.courseId = '1';
    experiment1.courseId = '1';

    experiment1.assignmentId = '100';
    experiment2.assignmentId = '100';
    experiment3.assignmentId = '100';
    experiment4.assignmentId = '100';
    experiment5.assignmentId = '100';

    experiment1.problemId = '10';
    experiment2.problemId = '10';
    experiment3.problemId = '10';
    experiment4.problemId = '20';
    experiment5.problemId = '20';

    experiment1.userId = 'tony';
    experiment2.userId = 'Dtracer';
    experiment3.userId = 'Chrome';
    experiment4.userId = 'Chrome';
    experiment5.userId = 'Dtracer';

    CourseSketch.fakeExperiments.push(experiment1);
    CourseSketch.fakeExperiments.push(experiment2);
    CourseSketch.fakeExperiments.push(experiment3);
    CourseSketch.fakeExperiments.push(experiment4);
    CourseSketch.fakeExperiments.push(experiment5);

})();

/**
 * cleans the update to make sure it is the same as all new versions
 */
function cleanSubmission(submisson) {
    return CourseSketch.prutil.decodeProtobuf(submisson.toArrayBuffer(), CourseSketch.prutil.getSrlSubmissionClass());
}
