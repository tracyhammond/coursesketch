/**
 * Created by David Windows on 4/15/2016.
 */

(function () {
    CourseSketch.dataListener.addRequestType(CourseSketch.prutil.getRequestClass().MessageType.RECOGNITION);
    CourseSketch.connection.setRecognitionListener(CourseSketch.dataListener.getListenerHook());
    var recognitionRpcDefinition = function(method, req, callback) {
        var generalRequest = CourseSketch.prutil.GeneralRecognitionRequest();
        var returnType = CourseSketch.prutil.RecognitionResponseClass();
        if (method === '.RecognitionService.addUpdate') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.ADD_UPDATE);
            generalRequest.setAddUpdate(req);
        } else if (method === '.RecognitionService.createUpdateList') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.SET_NEW_LIST);
            generalRequest.setSetUpdateList(req);
        } else if (method === '.RecognitionService.addTemplate') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.ADD_TEMPLATE);
            generalRequest.setTemplate(req);
            returnType = CourseSketch.prutil.DefaultResponseClass();
        } else if (method === '.RecognitionService.recognize') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.SET_NEW_LIST);
            generalRequest.setTemplate(req);
        }

        var request = CourseSketch.prutil.createRequestFromData(generalRequest, CourseSketch.prutil.getRequestClass().MessageType.RECOGNITION);
        CourseSketch.dataListener.sendRequestWithTimeout(request, function (evt, msg) {
            // TODO: add exception checking
            // if (msg instanceof CourseSketch.)
            callback(undefined, msg);
        }, 1, returnType);
    };

    CourseSketch.recognition = CourseSketch.prutil.RecognitionService(recognitionRpcDefinition);
});
