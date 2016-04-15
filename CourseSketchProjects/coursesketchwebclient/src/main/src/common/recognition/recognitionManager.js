/**
 * Created by David Windows on 4/15/2016.
 */

(function () {
    CourseSketch.dataListener.addRequestType(CourseSketch.prutil.getRequestClass().MessageType.RECOGNITION);
    CourseSketch.connection.setRecognitionListener(CourseSketch.dataListener.getListenerHook());
    var recognitionRpcDefinition = function(method, req, callback) {
        var generalRequest = CourseSketch.prutil.GeneralRecognitionRequest();
        if (method === '.RecognitionService.addUpdate') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.ADD_UPDATE);
            generalRequest.setAddUpdate(req);
        } else if (method === '.RecognitionService.createUpdateList') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.SET_NEW_LIST);
            generalRequest.setSetUpdateList(req);
        } else if (method === '.RecognitionService.addTemplate') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.ADD_TEMPLATE);
            generalRequest.setTemplate(req);
        } else if (method === '.RecognitionService.recognize') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.SET_NEW_LIST);
            generalRequest.setTemplate(req);
        }

        var request = CourseSketch.prutil.createRequestFromData(generalRequest, CourseSketch.prutil.getRequestClass().MessageType.RECOGNITION);
        CourseSketch.dataListener.sendRequestWithTimeout(request, function (msg) {
            
        });
    };

    CourseSketch.recognitionService = CourseSketch.prutil.RecognitionService(recognitionRpcDefinition);
    
    function addUpdate(recognitionId, update, callback) {
        var protoAddUpdate = CourseSketch.prutil.AddUpdateRequest();
        protoAddUpdate.setRecognitionId(recognitionId);
        protoAddUpdate.setUpdate(update);
        CourseSketch.recognitionService.addUpdate(protoAddUpdate, callback);
    }

    function setUpdateList(recognitionId, updateList, callback) {
        var recogUpdateList = CourseSketch.prutil.RecognitionUpdateList();
        recogUpdateList.setRecognitionId(recognitionId);
        recogUpdateList.setUpdateList(updateList);
        CourseSketch.recognitionService.createUpdateList(recogUpdateList, callback);
    }

    function addSketchTemplate(recognitionId, sketch, callback) {
        var recogTemplate = CourseSketch.prutil.RecognitionTemplate();
        recogTemplate.setTemplateId(recognitionId);
        recogTemplate.setTemplateType(sketch);
        CourseSketch.recognitionService.addTemplate(recogTemplate, callback);
    }

    function addShapeTemplate(recognitionId, shape, callback) {
        var recogTemplate = CourseSketch.prutil.RecognitionTemplate();
        recogTemplate.setTemplateId(recognitionId);
        recogTemplate.setTemplateType(shape);
        CourseSketch.recognitionService.addTemplate(recogTemplate, callback);
    }

    function addStrokeTemplate(recognitionId, stroke, callback) {
        var recogTemplate = CourseSketch.prutil.RecognitionTemplate();
        recogTemplate.setTemplateId(recognitionId);
        recogTemplate.setTemplateType(stroke);
        CourseSketch.recognitionService.addTemplate(recogTemplate, callback);
    }

    function recognize(recognitionId, updateList, callback) {
        var recogUpdateList = CourseSketch.prutil.RecognitionUpdateList();
        recogUpdateList.setRecognitionId(recognitionId);
        protoAddUpdate.setUpdate(updateList);
        CourseSketch.recognitionService.createUpdateList(recogUpdateList, callback);
    }
});
