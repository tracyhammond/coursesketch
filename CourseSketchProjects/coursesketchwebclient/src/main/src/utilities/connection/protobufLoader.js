/* depends on the protobuf library, base.js, objectAndInheritance.js */

function ProtobufException(message) {
    this.name = "ProtobufException";
    this.setMessage(message);
    this.message = "";
    this.htmlMessage = "";
}
ProtobufException.prototype = BaseException;

/**
 * *************************************************************
 *
 * Protobuf Utility functions
 *
 * @author gigemjt
 *
 * *************************************************************
 */

/**
 * @Class Has utilities for protobufs and is a convient accessor to create new
 *        instances of protobuf files (and prevents the modification of a
 *        protobuf object creator before it is created)
 */
function ProtobufSetup() {
    // sets it locally and only uses the local version from now on.
    this.dcodeIO = dcodeIO;

    var localDcodeIo = this.dcodeIO;

    var localScope = this;
    var PROTOBUF_PACKAGE = 'protobuf';
    var protobufDirectory = "/other/protobuf/";

    var objectList = new Array();
    var enumList = new Array();

    /**
     * @returns {ProtobufSetup} an instance of itself.
     */
    this.initializeBuf = function() {
        buildMessage();
        buildSchool();
        buildSketch();
        buildUpdateList();
        buildDataQuery();
        buildTutorial();
        buildSubmissions();
        buildLectures();
        return localScope;
    };

    function buildMessage() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + "message.proto");
        var requestPackage = builder.build(PROTOBUF_PACKAGE).srl.request;
        assignValues(requestPackage);
    }

    function buildDataQuery() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + "data.proto");
        var QueryBuilder = builder.build(PROTOBUF_PACKAGE).srl.query;
        assignValues(QueryBuilder);
    }

    function buildSchool() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + "school.proto");
        var SchoolBuilder = builder.build(PROTOBUF_PACKAGE).srl.school;
        assignValues(SchoolBuilder);
    }

    function buildSketch() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + "sketch.proto");
        var sketchBuilder = builder.build(PROTOBUF_PACKAGE).srl.sketch;
        assignValues(sketchBuilder, 'Proto');
    }

    function buildUpdateList() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + "commands.proto");
        var ProtoUpdateCommandBuilder = builder.build(PROTOBUF_PACKAGE).srl.commands;
        assignValues(ProtoUpdateCommandBuilder);
    }

    function buildTutorial() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + "tutorial.proto");
        var ProtoTutorialBuilder = builder.build(PROTOBUF_PACKAGE).srl.tutorial;
        assignValues(ProtoTutorialBuilder);
    }

    function buildSubmissions() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + "submission.proto");
        var ProtoSubmissionBuilder = builder.build(PROTOBUF_PACKAGE).srl.submission;
        assignValues(ProtoSubmissionBuilder);
    }

    function buildLectures() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + "lecturedata.proto");
        var ProtoSubmissionBuilder = builder.build(PROTOBUF_PACKAGE).srl.lecturedata;
        assignValues(ProtoSubmissionBuilder);
    }

    /**
     * @Method
     * @param protoPackage
     *            the package that the protofiles live in (this should basically
     *            hold a list of protoObjects)
     * @param namePrefix
     *            allows a string to be precede the name of the function being
     *            created.
     */
    function assignValues(protoPackage, namePrefix) {
        var preString = namePrefix;
        // safe checking.
        if (isUndefined(preString)) {
            preString = '';
        }
        for (var object in protoPackage) {
            (function(classType) {
                var objectName = preString + object;
                if (isFunction(classType)) {
                    objectList.push(objectName);
                    Object.defineProperty(localScope, objectName, {
                        value : function() {
                            if (arguments.length > 0) {
                                throw new ProtobufException("you can not create this object with arguments.");
                            }
                            return new classType();
                        },
                        writable : false
                    });

                    Object.defineProperty(localScope, "get" + objectName + "Class", {
                        value : function() {
                            // somehow change it to make this read only?
                            return classType;
                        },
                        writable : false
                    });
                } else {
                    enumList.push(objectName);
                    Object.defineProperty(localScope, objectName, {
                        get : function() {
                            return classType;
                        }
                    });
                }

            })(protoPackage[object]);
        }
    }

    /**
     * Given a protobuf Command array a Request is created with a single
     * SrlUpdate.
     *
     * It is important to node that an SrlUpdate implies that the commands
     * happened at the same time.
     *
     * @param commands
     *            {Array<SrlCommand>} a list of commands stored as an array.
     * @param requestType
     *            {MessageType} the type that the request is.
     * @return {Request}
     */
    this.createRequestFromCommands = function createRequestFromCommands(commands, requestType) {
        return this.createRequestFromUpdate(this.createUpdateFromCommands(commands), requestType);
    };

    /**
     * Given a protobuf object compile it to other data and return a request.
     *
     * @param data
     *            {Protobuf} An uncompiled protobuf object.
     * @param requestType
     *            {MessageType} The message type of the request.
     * @return {Request}
     */
    this.createRequestFromData = function(data, requestType) {
        var request = this.Request();
        request.requestType = requestType;
        var buffer = data.toArrayBuffer();
        request.setOtherData(buffer);
        return request;
    };

    /**
     * Given a protobuf Command array an SrlUpdate is created.
     *
     * It is important to node that an SrlUpdate implies that the commands
     * happened at the same time.
     *
     * @param commands
     *            {Array<SrlCommand>} a list of commands stored as an array.
     * @return {SrlUpdate}
     */
    this.createUpdateFromCommands = function createUpdateFromCommands(commands) {
        /*
        if (!isArray(commands)) {
            throw new TypeError('Invalid Type Error: Input is not an Array');
        }
        */

        var update = this.SrlUpdate();
        update.setCommands(commands);
        var n = createTimeStamp();
        update.setTime("" + n);
        update.setUpdateId(generateUUID());
        return update;
    };

    /**
     * Given a protobuf Command array an SrlUpdate is created.
     *
     * It is important to node that an SrlUpdate implies that the commands
     * happened at the same time.
     *
     * @return {SrlUpdate}
     */
    this.createBaseUpdate = function createBaseUpdate() {
        var update = this.SrlUpdate();
        var n = createTimeStamp();
        update.commands = [];
        update.setTime("" + n);
        update.setUpdateId(generateUUID());
        return update;
    };

    /**
     * @Method Given an SrlUpdate a Request is created.
     * @param update
     *            {SrlUpdate} a valid and complete object.
     * @param requestType
     *            {MessageType} the type that the request is.
     * @return {Request} used for all requesting needs
     */
    this.createRequestFromUpdate = function createRequestFromUpdate(update, requestType) {
        if (!(update instanceof localScope.getSrlUpdateClass())) {
            throw new TypeError('Invalid Type Error: Input must be an instanceof SrlUpdate');
        }

        var request = this.Request();
        request.requestType = requestType;
        var buffer = update.toArrayBuffer();
        request.setOtherData(buffer);
        return request;
    };

    /**
     * Creates a command given the commandType and if the user created.
     *
     * @param commandType
     *            {CommandType} the enum object of the commandType (found at
     *            CourseSketch.PROTOBUF_UTIL.CommandType).
     * @param userCreated
     *            {boolean} true if the user created this command, false if the
     *            command is system created.
     * @returns {SrlCommand}
     */
    this.createBaseCommand = function createBaseCommand(commandType, userCreated) {
        var command = this.SrlCommand();
        command.setCommandType(commandType);
        command.setIsUserCreated(userCreated);
        command.commandId = generateUUID(); // unique ID
        return command;
    };

    /**
     * Creates a protobuf date time object.
     * @param inputDateTime {Number | Date | Long} representing the time that this object should be created with.
     * @return {DateTime} a protobuf date time objct that can be used for date stuff.
     */
    this.createProtoDateTime = function(inputDateTime) {
        var preConvertedDate = inputDateTime;
        if (inputDateTime instanceof Date) {
            preConvertedDate = inputDateTime.getTime();
        }
        var longVersion = localDcodeIo.Long.fromString("" + preConvertedDate);
        var dateTime = this.DateTime();
        // Long object does not play nice with iframes so parsing as string instead.
        dateTime.setMillisecond("" + longVersion);
        var date = new Date(preConvertedDate);
        dateTime.setYear(date.getFullYear());
        dateTime.setMonth(date.getMonth());
        dateTime.setDay(date.getDate());
        dateTime.setHour(date.getHours());
        dateTime.setMinute(date.getMinutes());
        return dateTime;
    };

    /**
     * Creates a new sketch command.
     * @param x the x location of the sketch as an offset of its parent sketch.
     * @param y the y location of the sketch as an offset of its parent sketch.
     * @param width the width of the sketch.
     * @param height the height of the sketch.
     * @param id the id of the sketch, undefined if you want a random id given.
     * @return {SrlCommand} a create sketch command
     */
    this.createNewSketch = function createNewSketch(id, x, y, width, height) {
        var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_SKETCH, false);
        var idChain = CourseSketch.PROTOBUF_UTIL.IdChain();
        if (!isUndefined(id)) {
            idChain.idChain = [ id ];
        } else {
            idChain.idChain = [ generateUUID() ];
        }
        var createSketchAction = CourseSketch.PROTOBUF_UTIL.ActionCreateSketch();
        createSketchAction.sketchId = idChain;
        createSketchAction.x = x || (x === 0 ? 0 : -1);
        createSketchAction.y = y || (y === 0 ? 0 : -1);
        createSketchAction.width = width || (width === 0 ? 0 : -1);
        createSketchAction.height = height || (height === 0 ? 0 : -1);
        command.setCommandData(createSketchAction.toArrayBuffer());
        return command;
    };
    /**
     * @Method
     * @returns {Array} A cloned version of the array that contains all of the
     *          current protobuf objects.
     */
    this.getSupportedObjects = function getSupportedObjects() {
        // The quickest way to clone.
        return JSON.parse(JSON.stringify(objectList));
    };

    /**
     * @Method
     * @returns {Array} A cloned version of the array that contains all of the
     *          current protobuf enums.
     */
    this.getSupportedEnums = function getSupportedObjects() {
        // The quickest way to clone.
        return JSON.parse(JSON.stringify(enumList));
    };

    /**
     * Decodes the data and preserves the bytebuffer for later use
     *
     * @param data
     *            {ArrayBuffer} a compiled set of data in the protobuf object.
     * @param proto
     *            {ProtobufClass} The protobuf object that is being decoded.
     *            This can be grabbed by using CourseSketch.PROTOBUF_UTIL.get<objectName>Class();
     * @param onError
     *            {Function} A callback that is called when an error occurs
     *            (optional). This will be called before the result is returned
     *            and may be called up to two times.
     * @return {ProyobufObject} decoded protobuf object.
     */
    this.decodeProtobuf = function(data, proto, onError) {
        try {
            data.mark();
        } catch (exception) {
            if (onError) {
                onError(exception);
            }
        }
        if (isUndefined(data) || data === null || typeof data !== 'object') {
            throw new ProtobufException("Data type is not supported:" + typeof data);
        }
        var decoded = proto.decode(data);
        try {
            data.reset();
        } catch (exception) {
            if (onError) {
                onError(exception);
            }
        }
        return decoded;
    };

    // makes all of the methods read only
    for (var obj in localScope) {
        makeValueReadOnly(localScope, obj, localScope[obj]);
    }
    // making ProtobufException read only
    makeValueReadOnly(localScope, "ProtobufException", ProtobufException);
}

(function(scope) {
    if (isUndefined(scope.CourseSketch)) {
        makeValueReadOnly(scope, "CourseSketch", {});
    }
    if (!isUndefined(scope.CourseSketch.PROTOBUF_UTIL)) {
        return;
    }
    makeValueReadOnly(scope, "dcodeIO", dcodeIO);
    makeValueReadOnly(scope.CourseSketch, "PROTOBUF_UTIL", new ProtobufSetup().initializeBuf());
})(window);
