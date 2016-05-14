/**
 * Created by David Windows on 4/15/2016.
 */

(function () {
    CourseSketch.dataListener.addRequestType(CourseSketch.prutil.getRequestClass().MessageType.RECOGNITION);
    CourseSketch.connection.setRecognitionListener(CourseSketch.dataListener.getListenerHook());
    var recognitionRpcDefinition = function(method, req, callback) {
        console.log('RPC METHOD CALLED', method, req);
        var shortenedMethodName = method.substring(method.lastIndexOf('.') + 1, method.length);
        console.log('short name', shortenedMethodName);
        var generalRequest = CourseSketch.prutil.GeneralRecognitionRequest();
        var returnType = CourseSketch.prutil.getRecognitionResponseClass();
        if (shortenedMethodName === 'addUpdate') {
            console.log('add Update called!');
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.ADD_UPDATE);
            generalRequest.setAddUpdate(req);
        } else if (shortenedMethodName === 'createUpdateList') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.SET_NEW_LIST);
            generalRequest.setSetUpdateList(req);
        } else if (shortenedMethodName === 'addTemplate') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.ADD_TEMPLATE);
            generalRequest.setTemplate(req);
            returnType = CourseSketch.prutil.getDefaultResponseClass();
        } else if (shortenedMethodName === 'recognize') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.RECOGNIZE);
            generalRequest.setSetUpdateList(req);
        } else if (shortenedMethodName === 'generateTemplates') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.GENERATE_SHAPES);
            generalRequest.setTemplate(req);
            returnType = CourseSketch.prutil.getGeneratedTemplatesClass();
        } else {
            throw 'Recognition service method is not recognized: ' + shortenedMethodName;
        }

        console.log('rpc data is set!');
        var request = CourseSketch.prutil.createRequestFromData(generalRequest, CourseSketch.prutil.getRequestClass().MessageType.RECOGNITION);
        console.log('rpc data is added and being sent: ', generalRequest);
        CourseSketch.dataListener.sendRequestWithTimeout(request, function (evt, msg) {
            console.log('we got info back from the recognition server!!', msg);
            // TODO: add exception checking
            // if (msg instanceof CourseSketch.)
            try {
                // if callback is called with error then the protobuf implementation does not call it with msg.
                callback(undefined, msg);
            } catch(exception) {
                console.log(exception);
            }
        }, 1, returnType);
    };

    CourseSketch.recognitionService = CourseSketch.prutil.RecognitionService(recognitionRpcDefinition);

    function addUpdate(recognitionId, update, callback) {
        console.log('Adding the update into the server');
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

    function setTemplateData(label, recognitionId, protoRecognitionTemplate) {
        var interpretationTemplate = CourseSketch.prutil.ProtoSrlInterpretation();
        interpretationTemplate.setLabel('' + label);
        interpretationTemplate.setConfidence(1);
        interpretationTemplate.setComplexity(1);
        protoRecognitionTemplate.setTemplateId('' + recognitionId);
        protoRecognitionTemplate.setInterpretation(interpretationTemplate);
    }

    function addTemplate(label, recognitionId, protoRecognitionTemplate, callback, templateType) {
        setTemplateData(label, recognitionId, protoRecognitionTemplate);
        if (!isUndefined(templateType)) {
            protoRecognitionTemplate.type = templateType;
        }
        console.log(protoRecognitionTemplate);
        CourseSketch.recognitionService.addTemplate(protoRecognitionTemplate, callback);
    }

    function addSketchTemplate(label, recognitionId, sketch, callback, templateType) {
        var recogTemplate = CourseSketch.prutil.ProtoRecognitionTemplate();
        recogTemplate.setSketch(sketch);
        addTemplate(label, recognitionId, recogTemplate, callback, templateType);
    }

    function addShapeTemplate(label, recognitionId, shape, callback, templateType) {
        var recogTemplate = CourseSketch.prutil.ProtoRecognitionTemplate();
        recogTemplate.setShape(shape);
        addTemplate(label, recognitionId, recogTemplate, callback, templateType);
    }

    function addStrokeTemplate(label, recognitionId, stroke, callback, templateType) {
        var recogTemplate = CourseSketch.prutil.ProtoRecognitionTemplate();
        recogTemplate.setStroke(stroke);
        addTemplate(label, recognitionId, recogTemplate, callback, templateType);
    }

    function recognize(recognitionId, updateList, callback) {
        var recogUpdateList = CourseSketch.prutil.RecognitionUpdateList();
        recogUpdateList.setRecognitionId(recognitionId);
        recogUpdateList.setUpdateList(updateList);
        CourseSketch.recognitionService.recognize(recogUpdateList, callback);
    }

    CourseSketch.recognition = {};
    CourseSketch.recognition.addUpdate = addUpdate;
    CourseSketch.recognition.setUpdateList = setUpdateList;
    CourseSketch.recognition.addSketchTemplate = addSketchTemplate;
    CourseSketch.recognition.addShapeTemplate = addShapeTemplate;
    CourseSketch.recognition.addStrokeTemplate = addStrokeTemplate;
    CourseSketch.recognition.recognize = recognize;
    CourseSketch.recognition.setTemplateData = setTemplateData;
    CourseSketch.recognition.generateTemplates = CourseSketch.recognitionService.generateTemplates.bind(CourseSketch.recognitionService);
})();
