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
            returnType = CourseSketch.prutil.DefaultResponseClass();
        } else if (shortenedMethodName === 'recognize') {
            generalRequest.setRequestType(CourseSketch.prutil.RecognitionRequestType.SET_NEW_LIST);
            generalRequest.setTemplate(req);
        }

        console.log('rpc data is set!');
        var request = CourseSketch.prutil.createRequestFromData(generalRequest, CourseSketch.prutil.getRequestClass().MessageType.RECOGNITION);
        console.log('rpc data is added!');
        CourseSketch.dataListener.sendRequestWithTimeout(request, function (evt, msg) {
            console.log('we got info back from the recognition server!!', msg);
            // TODO: add exception checking
            // if (msg instanceof CourseSketch.)
            callback(undefined, msg);
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


    CourseSketch.recognition = {};
    CourseSketch.recognition.addUpdate = addUpdate;
    CourseSketch.recognition.setUpdateList = setUpdateList;
    CourseSketch.recognition.addSketchTemplate = addSketchTemplate;
    CourseSketch.recognition.addShapeTemplate = addStrokeTemplate;
    CourseSketch.recognition.recognize = recognize;

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
            console.log("adding update!");
            var cleanUpdate = CourseSketch.prutil.cleanProtobuf(update, CourseSketch.prutil.getSrlUpdateClass());
            if (!isUndefined(toRemote) && toRemote) {
                CourseSketch.recognition.addUpdate(sketchId, cleanUpdate, function(err, msg) {
                    console.log('It worked@!!!', err, msg);
                    if (!isUndefined(err) || isUndefined(msg)) {
                        console.log('problems with the response')
                        return;
                    }
                    var changes = msg.changes;
                    for (var i = 0; i < changes)
                });
            }
        };
    }
    CourseSketch.createRecognitionPlugin = function(updateManager, sketchId) {
        return new RecognitionPlugin(updateManager, sketchId);
    }
})();
