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

    function addTemplate(label, recognitionId, protoRecognitionTemplate, callback) {
        setTemplateData(label, recognitionId, protoRecognitionTemplate);
        console.log(protoRecognitionTemplate);
        CourseSketch.recognitionService.addTemplate(protoRecognitionTemplate, callback);
    }

    function addSketchTemplate(label, recognitionId, sketch, callback) {
        var recogTemplate = CourseSketch.prutil.ProtoRecognitionTemplate();
        recogTemplate.setSketch(sketch);
        addTemplate(label, recognitionId, recogTemplate, callback);
    }

    function addShapeTemplate(label, recognitionId, shape, callback) {
        var recogTemplate = CourseSketch.prutil.ProtoRecognitionTemplate();
        recogTemplate.setShape(shape);
        addTemplate(label, recognitionId, recogTemplate, callback);
    }

    function addStrokeTemplate(label, recognitionId, stroke, callback) {
        var recogTemplate = CourseSketch.prutil.ProtoRecognitionTemplate();
        recogTemplate.setStroke(stroke);
        addTemplate(label, recognitionId, recogTemplate, callback);
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

    /**
     * A plugin used to send updates to the server.
     *
     * @class RecognitionPlugin
     */
    function RecognitionPlugin(updateManager, sketchId) {
        /**
         * Holds the list of updates that are waiting to be sent to the server.
         *
         * This list should almost always be near empty.
         */
        var queuedServerUpdates = [];

        /**
         * Called when the updatemanager adds an update.
         *
         * @param {SrlUpdate} update - The update to be sent to thee recognition server.
         * @param {Boolean} toRemote - True if this update is destined to the remote server.
         */
        this.addUpdate = function(update, toRemote) {
            console.log('adding update!');
            var cleanUpdate = CourseSketch.prutil.cleanProtobuf(update, CourseSketch.prutil.getSrlUpdateClass());
            if (!isUndefined(toRemote) && toRemote) {
                CourseSketch.recognition.addUpdate(sketchId, cleanUpdate, function(err, msg) {
                    console.log('It worked@!!!', err, msg);
                    if ((!isUndefined(err) && err !== null) || isUndefined(msg)) {
                        console.log('problems with the response');
                        return;
                    }
                    var updateList = msg.changes;
                    var updates = updateList.list;
                    for (var i = 0; i < updates.length; i++) {
                        var update = updates[i];
                        console.log('add update', update);
                        updateManager.addUpdate(update);
                    }
                });
            }
        };
    }

    CourseSketch.createRecognitionPlugin = function(updateManager, sketchId) {
        return new RecognitionPlugin(updateManager, sketchId);
    };
})();
