/* Depends on the protobuf library, base.js, objectAndInheritance.js */

function ProtobufException(message) {
    this.name = 'ProtobufException';
    this.setMessage(message);
    this.message = '';
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
 * @class
 * @classdesc
 * Has utilities for protobufs and is a convient accessor to create new
 *        instances of protobuf files (and prevents the modification of a
 *        protobuf object creator before it is created)
 *
 */
function ProtobufSetup() {
    // Sets it locally and only uses the local version from now on.
    this.dcodeIO = dcodeIO;

    var localDcodeIo = this.dcodeIO;

    var localScope = this;
    var PROTOBUF_PACKAGE = 'protobuf';
    var protobufDirectory = '/other/protobuf/';

    var objectList = [];
    var enumList = [];

    /**
     * @returns {ProtobufSetup} an instance of itself.
     */
    this.initializeBuf = function() {
        buildUtil();
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

    function buildUtil() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'util.proto');
        var utilBuilder = builder.build(PROTOBUF_PACKAGE).srl.utils;
        assignValues(utilBuilder);
    }

    function buildMessage() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'message.proto');
        var requestPackage = builder.build(PROTOBUF_PACKAGE).srl.request;
        assignValues(requestPackage);
    }

    function buildDataQuery() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'data.proto');
        var QueryBuilder = builder.build(PROTOBUF_PACKAGE).srl.query;
        assignValues(QueryBuilder);
    }

    function buildSchool() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'school.proto');
        var SchoolBuilder = builder.build(PROTOBUF_PACKAGE).srl.school;
        assignValues(SchoolBuilder);
    }

    function buildSketch() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'sketch.proto');
        var sketchBuilder = builder.build(PROTOBUF_PACKAGE).srl.sketch;
        assignValues(sketchBuilder, 'Proto');
    }

    function buildUpdateList() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'commands.proto');
        var ProtoUpdateCommandBuilder = builder.build(PROTOBUF_PACKAGE).srl.commands;
        assignValues(ProtoUpdateCommandBuilder);
    }

    function buildTutorial() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'tutorial.proto');
        var ProtoTutorialBuilder = builder.build(PROTOBUF_PACKAGE).srl.tutorial;
        assignValues(ProtoTutorialBuilder);
    }

    function buildSubmissions() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'submission.proto');
        var ProtoSubmissionBuilder = builder.build(PROTOBUF_PACKAGE).srl.submission;
        assignValues(ProtoSubmissionBuilder);
    }

    function buildLectures() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'lecturedata.proto');
        var ProtoSubmissionBuilder = builder.build(PROTOBUF_PACKAGE).srl.lecturedata;
        assignValues(ProtoSubmissionBuilder);
    }

    /**
     * @function assignValues
     * @param {String} protoPackage
     *            the package that the protofiles live in (this should basically
     *            hold a list of protoObjects)
     * @param {String} namePrefix
     *            allows a string to be precede the name of the function being
     *            created.
     */
    function assignValues(protoPackage, namePrefix) {
        var preString = namePrefix;
        // safe checking.
        if (isUndefined(preString)) {
            preString = '';
        }
        for (var messageName in protoPackage) {
            if (protoPackage.hasOwnProperty(messageName)) {
                createProtoMethod(protoPackage[messageName], messageName, preString);
            }
        }
    }

    /**
     * Creates a method that becomes a part of the protobuf singleton object.
     * It creates two method:
     * <ul>
     * <li>The first method is the name of the message.  This creates an instance of the message.
     * An example is Request calling <code>PROTOBUF_UTIL.Request()</code> creates an instance of the Request class.
     * If the message is an enum then you can call it without the () an example is: <code>PROTOBUF_UTIL.CommandType.ADD_STROKE</code>
     * </li>
     * <li> The second method is the class of the message.  This can be useful to get access to messages inside messages.
     * This second method only exist for messages and does not exist for enums.
     * an example is: <code>PROTOBUF_UTIL.getRequestClass()</code></li>
     *
     * @param {Function|Enum} ClassType the actual data that represents the protobuf data.
     * If the classType is not a function then we treat it like an enum.
     * @param {String} messageName the name of the message.
     * @param {String} preString a string that is used to preprend the messageName.
     * This can be used to prevent conflicts. The value must not be undefined.
     */
    function createProtoMethod(ClassType, messageName, preString) {
        var objectName = preString + messageName;
        if (isFunction(ClassType)) {
            objectList.push(objectName);
            Object.defineProperty(localScope, objectName, {
                value: function() {
                    if (arguments.length > 0) {
                        throw new ProtobufException('you can not create this object with arguments.');
                    }
                    return new ClassType();
                },
                writable: false
            });

            Object.defineProperty(localScope, 'get' + objectName + 'Class', {
                value: function() {
                    // somehow change it to make this read only?
                    return ClassType;
                },
                writable: false
            });
        } else {
            enumList.push(objectName);
            Object.defineProperty(localScope, objectName, {
                get: function() {
                    return ClassType;
                }
            });
        }

    }

    /**
     * Given a protobuf Command array a Request is created with a single
     * SrlUpdate.
     *
     * It is important to node that an SrlUpdate implies that the commands
     * happened at the same time.
     *
     * @param {Array<SrlCommand>} commands
     *            a list of commands stored as an array.
     * @param {MessageType} requestType
     *            the type that the request is.
     * @return {Request}
     */
    this.createRequestFromCommands = function createRequestFromCommands(commands, requestType) {
        return this.createRequestFromUpdate(this.createUpdateFromCommands(commands), requestType);
    };

    /**
     * Given a protobuf object compile it to other data and return a request.
     *
     * @param {Protobuf} data
     *            An uncompiled protobuf object.
     * @param {MessageType} requestType
     *            The message type of the request.
     * @return {Request}
     */
    this.createRequestFromData = function(data, requestType) {
        var request = this.Request();
        request.requestType = requestType;
        var buffer = data.toArrayBuffer();
        request.setOtherData(buffer);
        return request;
    };

    this.createProtoException = function (exception) {
        var pException = CourseSketch.PROTOBUF_UTIL.ProtoException();
        pException.setMssg(exception.specificMessage);

        for (StackTraceElement element : exception.getStackTrace()) {
            pException.addStackTrace(element);
        }
        if (!isUndefined(exception.getCause())) {
            pException.setCause(this.createProtoException(exception.getCause()));
        }
        pException.setExceptionType(exception.name);
        return pException;
    }

    /**
     * Given a protobuf Command array an SrlUpdate is created.
     *
     * It is important to node that an SrlUpdate implies that the commands
     * happened at the same time.
     *
     * @param {Array<SrlCommand>} commands
     *            a list of commands stored as an array.
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
        update.setTime('' + n);
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
        update.setTime('' + n);
        update.setUpdateId(generateUUID());
        return update;
    };

    /**
     * @function
     * Given an SrlUpdate a Request is created.
     * @param {SrlUpdate} update
     *            a valid and complete object.
     * @param {MessageType} requestType
     *            the type that the request is.
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
     * @param {CommandType} commandType
     *            the enum object of the commandType (found at
     *            CourseSketch.PROTOBUF_UTIL.CommandType).
     * @param {Boolean} userCreated
     *            true if the user created this command, false if the
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
     * @param {Number|Date|Long} inputDateTime representing the time that this object should be created with.
     * @return {DateTime} A protobuf date time objct that can be used for date stuff.
     */
    this.createProtoDateTime = function(inputDateTime) {
        var preConvertedDate = inputDateTime;
        if (inputDateTime instanceof Date) {
            preConvertedDate = inputDateTime.getTime();
        }
        var longVersion = localDcodeIo.Long.fromString('' + preConvertedDate);
        var dateTime = this.DateTime();
        // Long object does not play nice with iframes so parsing as string instead.
        dateTime.setMillisecond('' + longVersion);
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
     * @param {String} id the id of the sketch, undefined if you want a random id given.
     * @param {Number} x the x location of the sketch as an offset of its parent sketch.
     * @param {Number} y the y location of the sketch as an offset of its parent sketch.
     * @param {Number} width the width of the sketch.
     * @param {Number} height the height of the sketch.
     *
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
     * @function getSupportedObjects
     * @returns {Array} A cloned version of the array that contains all of the
     *          current protobuf objects.
     */
    this.getSupportedObjects = function getSupportedObjects() {
        // The quickest way to clone.
        return JSON.parse(JSON.stringify(objectList));
    };

    /**
     * @function getSupportedEnums
     * @returns {Array} A cloned version of the array that contains all of the
     *          current protobuf enums.
     */
    this.getSupportedEnums = function getSupportedEnums() {
        // The quickest way to clone.
        return JSON.parse(JSON.stringify(enumList));
    };

    /**
     * Decodes the data and preserves the bytebuffer for later use
     *
     * @param {ArrayBuffer} data
     *            a compiled set of data in the protobuf object.
     * @param {ProtobufClass} proto
     *            The protobuf object that is being decoded.
     *            This can be grabbed by using CourseSketch.PROTOBUF_UTIL.get<objectName>Class();
     * @param {Function} [onError]
     *            A callback that is called when an error occurs
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
            throw new ProtobufException('Data type is not supported:' + typeof data);
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
        if (localScope.hasOwnProperty(obj)) {
            makeValueReadOnly(localScope, obj, localScope[obj]);
        }
    }
    // making ProtobufException read only
    makeValueReadOnly(localScope, 'ProtobufException', ProtobufException);
}

(function(scope) {
    if (isUndefined(scope.CourseSketch)) {
        makeValueReadOnly(scope, 'CourseSketch', {});
    }
    if (!isUndefined(scope.CourseSketch.PROTOBUF_UTIL)) {
        return;
    }
    makeValueReadOnly(scope, 'dcodeIO', dcodeIO);
    makeValueReadOnly(scope.CourseSketch, 'PROTOBUF_UTIL', new ProtobufSetup().initializeBuf());
})(window);
