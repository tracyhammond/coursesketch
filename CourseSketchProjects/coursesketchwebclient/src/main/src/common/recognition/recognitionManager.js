/**
 * Created by David Windows on 4/15/2016.
 */

(function () {
    CourseSketch.dataListener.addRequestType(CourseSketch.prutil.getRequestClass().MessageType.RECOGNITION);
    CourseSketch.connection.setRecognitionListener(CourseSketch.dataListener.getListenerHook());
    var recognitionRpcDefinition = function(method, req, callback) {
        var generalRequest = CourseSketch.prutil.GeneralRecognitionRequest();
        if (method === '.RecognitionService.addUpdate') {

        }

        var request = CourseSketch.prutil.createRequestFromData(generalRequest, CourseSketch.prutil.getRequestClass().MessageType.RECOGNITION);
        CourseSketch.dataListener.sendRequestWithTimeout(request, callback);
    }
});
