
CourseSketch.fakeExperiments = [];

/*
 * tansfering the sketches into submissions to be used to make experiments
 * for testing purposes
 */
var submission1 = CourseSketch.PROTOBUF_UTIL.SrlSubmission();
var submission2 = CourseSketch.PROTOBUF_UTIL.SrlSubmission();
var submission3 = CourseSketch.PROTOBUF_UTIL.SrlSubmission();
var submission4 = CourseSketch.PROTOBUF_UTIL.SrlSubmission();
var submission5 = CourseSketch.PROTOBUF_UTIL.SrlSubmission();

submission1.setUpdateList(CourseSketch.fakeSketches[0]);
submission2.setUpdateList(CourseSketch.fakeSketches[1]);
submission3.setUpdateList(CourseSketch.fakeSketches[2]);
submission4.setUpdateList(CourseSketch.fakeSketches[3]);
submission5.setUpdateList(CourseSketch.fakeSketches[4]);

/*
 * setting the submissions to experiments to be used as test data
 */

var experiment1 = CourseSketch.PROTOBUF_UTIL.SrlExperiment();
var experiment2 = CourseSketch.PROTOBUF_UTIL.SrlExperiment();
var experiment3 = CourseSketch.PROTOBUF_UTIL.SrlExperiment();
var experiment4 = CourseSketch.PROTOBUF_UTIL.SrlExperiment();
var experiment5 = CourseSketch.PROTOBUF_UTIL.SrlExperiment();

experiment1.setSubmission(submission1);
experiment2.setSubmission(submission2);
experiment3.setSubmission(submission3);
experiment4.setSubmission(submission4);
experiment5.setSubmission(submission5);

/*
 * assinging details to the experiments so we can seach for specific problems
 */

experiment1.assignmentId = "1"
experiment2.assignmentId = "1"
experiment3.assignmentId = "1"
experiment4.assignmentId = "1"
experiment5.assignmentId = "1"

experiment1.problemId = "1"
experiment2.problemId = "1"
experiment3.problemId = "1"
experiment4.problemId = "2"
experiment5.problemId = "2"

experiment1.userId = "tony"
experiment2.userId = "Dtracer"
experiment3.userId = "Chrome"
experiment4.userId = "Chrome"
experiment5.userId = "Dtracer"

CourseSketch.fakeExperiments.push(experiment1);
CourseSketch.fakeExperiments.push(experiment2);
CourseSketch.fakeExperiments.push(experiment3);
CourseSketch.fakeExperiments.push(experiment4);
CourseSketch.fakeExperiments.push(experiment5);
