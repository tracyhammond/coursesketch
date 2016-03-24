/* Depends on the protobuf library, base.js, objectAndInheritance.js */

/**
 * Any exception that occurs relating to protobufs.
 *
 * @param {String} message - A custom message for the user.
 * @param {BaseException|Error} [cause] - Optional exception that caused this instance.
 * @constructor
 */
function ProtobufException(message, cause) {
    this.name = 'ProtobufException';
    this.setMessage(message);
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}
ProtobufException.prototype = new BaseException();

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
     * Builds the Utility protobuf files.
     *
     * These can be used by all other protobuf files.
     */
    function buildUtil() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'util.proto');
        var utilBuilder = builder.build(PROTOBUF_PACKAGE).srl.utils;
        assignValues(utilBuilder);
    }

    /**
     * Builds the Grade protobuf files.
     *
     * This is the base for storing and retrieving grades.
     */
    function buildGrade() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'grading.proto');
        var gradingBuilder = builder.build(PROTOBUF_PACKAGE).srl.grading;
        assignValues(gradingBuilder);
    }

    /**
     * Builds the Message protobuf files.
     *
     * This is the base for talking to the server.
     */
    function buildMessage() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'message.proto');
        var requestPackage = builder.build(PROTOBUF_PACKAGE).srl.request;
        assignValues(requestPackage);
    }

    /**
     * Builds the Data protobuf files.
     *
     * These ares used to talk with the database.
     */
    function buildDataQuery() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'data.proto');
        var QueryBuilder = builder.build(PROTOBUF_PACKAGE).srl.query;
        assignValues(QueryBuilder);
    }

    /**
     * Builds the School protobuf files.
     *
     * These contain data about courses, assignments, and problems
     */
    function buildSchool() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'school.proto');
        var SchoolBuilder = builder.build(PROTOBUF_PACKAGE).srl.school;
        assignValues(SchoolBuilder);
    }

    /**
     * Builds the Sketch protobuf files.
     *
     * This contains the sketchml format kinda.  It holds points, strokes, and shapes
     */
    function buildSketch() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'sketch.proto');
        var sketchBuilder = builder.build(PROTOBUF_PACKAGE).srl.sketch;
        assignValues(sketchBuilder, 'Proto');
    }

    /**
     * Builds the UpdateList protobuf files.
     *
     * These contain all of the little actions that can occur
     */
    function buildUpdateList() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'commands.proto');
        var ProtoUpdateCommandBuilder = builder.build(PROTOBUF_PACKAGE).srl.commands;
        assignValues(ProtoUpdateCommandBuilder);
    }

    /**
     * Builds the UpdateList protobuf files.
     *
     * These contain all of the little actions that can occur
     */
    function buildIdentity() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'identity.proto');
        var ProtoIdentityServiceBuilder = builder.build(PROTOBUF_PACKAGE).srl.services.identity;
        assignValues(ProtoIdentityServiceBuilder);
    }

    /**
     * Builds the Tutorial protobuf files.
     *
     * These ares used for the tutorials
     */
    function buildTutorial() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'tutorial.proto');
        var ProtoTutorialBuilder = builder.build(PROTOBUF_PACKAGE).srl.tutorial;
        assignValues(ProtoTutorialBuilder);
    }

    /**
     * Builds the Submission protobuf files.
     *
     * These ares for submitting experiments or solutions.
     */
    function buildSubmissions() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'submission.proto');
        var ProtoSubmissionBuilder = builder.build(PROTOBUF_PACKAGE).srl.submission;
        assignValues(ProtoSubmissionBuilder);
    }

    /**
     * Builds the Lecture protobuf files.
     *
     * These ares used for lecture data.
     */
    function buildLectures() {
        var builder = localDcodeIo.ProtoBuf.protoFromFile(protobufDirectory + 'lecturedata.proto');
        var ProtoSubmissionBuilder = builder.build(PROTOBUF_PACKAGE).srl.lecturedata;
        assignValues(ProtoSubmissionBuilder);
    }

    /**
     * @returns {ProtobufSetup} an instance of itself.
     */
    this.initializeBuf = function() {
        buildUtil();
        buildMessage();
        buildSchool();
        buildSketch();
        buildUpdateList();
        buildIdentity();
        buildDataQuery();
        buildTutorial();
        buildSubmissions();
        buildLectures();
        buildGrade();
        return localScope;
    };

    /**
     * @function assignValues
     * @param {String} protoPackage - The package that the protofiles live in (this should basically hold a list of protoObjects)
     * @param {String} namePrefix - Allows a string to be precede the name of the function being created.
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
     * An example is Request calling <code>prutil.Request()</code> creates an instance of the Request class.
     * If the message is an enum then you can call it without the () an example is: <code>prutil.CommandType.ADD_STROKE</code>
     * </li>
     * <li> The second method is the class of the message.  This can be useful to get access to messages inside messages.
     * This second method only exist for messages and does not exist for enums.
     * an example is: <code>prutil.getRequestClass()</code></li>
     *
     * @param {Function|Enum} ClassType - the actual data that represents the protobuf data.
     * If the classType is not a function then we treat it like an enum.
     * @param {String} messageName - the name of the message.
     * @param {String} preString - a string that is used to preprend the messageName.
     * This can be used to prevent conflicts. The value must not be undefined.
     */
    function createProtoMethod(ClassType, messageName, preString) {
        var objectName = preString + messageName;
        if (isFunction(ClassType)) {
            objectList.push(objectName);
            Object.defineProperty(localScope, objectName, {
                /**
                 * @returns {Object} An instance a protobuf object.
                 */
                value: function() {
                    if (arguments.length > 0) {
                        throw new ProtobufException('you can not create this object with arguments.');
                    }
                    return new ClassType();
                },
                writable: false
            });

            Object.defineProperty(localScope, 'get' + objectName + 'Class', {
                /**
                 * @returns {Function|Enum} A class representing a protobuf object.
                 */
                value: function() {
                    // somehow change it to make this read only?
                    return ClassType;
                },
                writable: false
            });
        } else {
            enumList.push(objectName);
            Object.defineProperty(localScope, objectName, {
                /**
                 * @returns {Enum} An enum defined in protobuf.
                 */
                get: function() {
                    return ClassType;
                }
            });
        }
    }

    /**
     * Given a protobuf Command array a Request is created with a single {@code SrlUpdate}.
     * it is important to node that an SrlUpdate implies that the commands happened at the same time.
     *
     * @param {Array<SrlCommand>} commands
     *            a list of commands stored as an array.
     * @param {MessageType} requestType
     *            the type that the request is.
     * @return {Request} A request that holds the list of commands.
     */
    this.createRequestFromCommands = function createRequestFromCommands(commands, requestType) {
        return this.createRequestFromUpdate(this.createUpdateFromCommands(commands), requestType);
    };

    /**
     * Given a protobuf object compile it to other data and return a request.
     *
     * @param {Protobuf} data
     *              An uncompiled protobuf object.
     * @param {MessageType} requestType
     *              The message type of the request.
     * @param {String} [requestId]
     *              An id that is required for every request.
     * @return {Request} Creates a request from the binary data given.
     */
    this.createRequestFromData = function(data, requestType, requestId) {
        var request = this.Request();
        request.requestType = requestType;
        var buffer = data.toArrayBuffer();
        request.setOtherData(buffer);

        if (!isUndefined(requestId)) {
            request.requestId = requestId;
        } else {
            request.requestId = generateUUID();
        }
        return request;
    };

    /**
     * Given an custom exception, a ProtoException Object will be created.
     *
     * @param {Exception} exception
     *              An custom exception that extends BaseException.
     * @return {ProtoException} A protobuf exception.
     */
    this.createProtoException = function(exception) {
        if (!(exception instanceof BaseException) && !(exception instanceof CourseSketch.prutil.getProtoExceptionClass()) &&
            !(exception instanceof CourseSketch.BaseException)) {
            return this.errorToProtoException(exception);
        }
        var pException = CourseSketch.prutil.ProtoException();
        pException.setMssg(exception.specificMessage);

        pException.stackTrace = exception.getStackTrace();

        if (!isUndefined(exception.getCause())) {
            pException.setCause(this.createProtoException(exception.getCause()));
        }
        pException.setExceptionType(exception.name);
        return pException;
    };

    /**
     * Given an javascript error, a ProtoException Object will be created.
     *
     * @param {error} anError
     *              An JS error that has occurred or been defined.
     * @return {ProtoException} A protobuf exception.
     */
    this.errorToProtoException = function(anError) {
        var pException = CourseSketch.prutil.ProtoException();
        if (typeof anError === 'string') {
            pException.setMssg(anError);
            pException.setExceptionType('String');
            pException.setName('String Error');
            return pException;
        }
        pException.setMssg(anError.message);

        var stack = anError.stack;
        if (!isArray(stack)) {
            pException.stackTrace = [ stack ];
        } else {
            pException.stackTrace = anError.stack;
        }

        pException.setExceptionType('Error');
        return pException;
    };

    /**
     * Given a protobuf Command array an SrlUpdate is created.
     *
     * It is important to node that an SrlUpdate implies that the commands
     * happened at the same time.
     *
     * @param {Array<SrlCommand>} commands - A list of commands stored as an array.
     * @return {SrlUpdate} An update that holds the list of given commands.
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
     * @return {SrlUpdate} An empty update.
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
     * @param {SrlUpdate} update - A valid and complete object.
     * @param {MessageType} requestType - The type that the request is.
     * @return {Request} used for all requesting needs
     */
    this.createRequestFromUpdate = function createRequestFromUpdate(update, requestType) {
        if (!(update instanceof localScope.getSrlUpdateClass())) {
            throw new TypeError('Invalid Type Error: Input must be an instanceof SrlUpdate');
        }

        return this.createRequestFromData(update, requestType);
    };

    /**
     * Creates a command given the commandType and if the user created.
     *
     * @param {CommandType} commandType - The enum object of the commandType (found at
     *            CourseSketch.prutil.CommandType).
     * @param {Boolean} userCreated - True if the user created this command, false if the
     *            command is system created.
     * @returns {SrlCommand} Creates a command with basic data.
     */
    this.createBaseCommand = function createBaseCommand(commandType, userCreated) {
        var command = this.SrlCommand();
        command.setCommandType(commandType);
        command.setIsUserCreated(userCreated);
        command.commandId = generateUUID(); // unique ID
        return command;
    };

    /**
     * Creates an itemRequest from the given data.
     *
     * @param {ItemQuery} queryType - The query type of the object.
     * @param {String | List<String>} [idList] - A list of ids used for retrieving data from the database.
     * @param {ByteArray} [advanceQuery] - A protobuf object used to represent more complex queries.
     * @returns {ItemRequest} An item request from the data.
     */
    this.createItemRequest = function createItemRequest(queryType, idList, advanceQuery) {
        var itemRequest = CourseSketch.prutil.ItemRequest();
        itemRequest.setQuery(queryType);

        if (!isUndefined(idList)) {
            itemRequest.setItemId(idList);
        }
        if (!isUndefined(advanceQuery)) {
            itemRequest.setAdvanceQuery(advanceQuery.toArrayBuffer());
        }
        return itemRequest;
    };

    /**
     * Creates a protobuf date time object.
     *
     * @param {Number|Date|Long} inputDateTime - representing the time that this object should be created with.
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
     *
     * @param {String} id - the id of the sketch, undefined if you want a random id given.
     * @param {Number} x - the x location of the sketch as an offset of its parent sketch.
     * @param {Number} y - the y location of the sketch as an offset of its parent sketch.
     * @param {Number} width - the width of the sketch.
     * @param {Number} height - the height of the sketch.
     *
     * @return {SrlCommand} a create sketch command
     */
    this.createNewSketch = function createNewSketch(id, x, y, width, height) {
        var command = CourseSketch.prutil.createBaseCommand(CourseSketch.prutil.CommandType.CREATE_SKETCH, false);
        var idChain = CourseSketch.prutil.IdChain();
        if (!isUndefined(id)) {
            idChain.idChain = [ id ];
        } else {
            idChain.idChain = [ generateUUID() ];
        }
        var createSketchAction = CourseSketch.prutil.ActionCreateSketch();
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
     * @param {ProtobufClass} proto - The protobuf object that is being decoded.
     *            This can be grabbed by using CourseSketch.prutil.get<objectName>Class();
     * @param {Function} [onError] - A callback that is called when an error occurs regarding marking and resetting.
     *            (optional). This will be called before the result is returned
     *
     * @return {ProyobufObject} decoded protobuf object.  (This will not return undefined)
     * @throws {ProtobufException} Thrown is there are problems decoding the data.
     */
    this.decodeProtobuf = function(data, proto, onError) {
        if (isUndefined(data) || data === null || typeof data !== 'object') {
            throw new ProtobufException('Data type is not supported:' + typeof data);
        }
        try {
            data.mark();
        } catch (exception) {
            if (onError) {
                onError(exception);
            }
        }
        var decoded = undefined;
        try {
            decoded = proto.decode(data);
        } catch (exception) {
            throw new ProtobufException('data was not decoded successfully');
        }
        if (isUndefined(decoded)) {
            throw new ProtobufException('data was not decoded successfully or input was empty');
        }
        try {
            data.reset();
        } catch (exception) {
            if (onError) {
                onError(exception);
            }
        }
        return decoded;
    };

    /**
     * Returns a "clean" version of the protobuf files that can be considered a clone.
     *
     * "clean" means that the protobuf object is compiled into a byteArray then immediately decompiled.
     * The purpose of cleaning is in case you want to prototype a protobuf object but the object was created on an old version of objects.
     * Then the protubf would not correctly apply to this new object.
     *
     * @param {ProtobufObject} protobuf - An object that we want to "clean".
     * @param {ProtobufMessage} protobufType - A class representing the object we want to "clean".
     * @returns {ProyobufObject} A clean version of the object we sent in.
     */
    this.cleanProtobuf = function(protobuf, protobufType) {
        // TODO: check to see if we can extract the type from the protobuf object.
        return CourseSketch.prutil.decodeProtobuf(protobuf.toArrayBuffer(), protobufType);
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
    if (!isUndefined(scope.CourseSketch.prutil)) {
        return;
    }
    makeValueReadOnly(scope, 'dcodeIO', dcodeIO);
    var protobuf = new ProtobufSetup().initializeBuf();
    makeValueReadOnly(scope.CourseSketch, 'prutil', protobuf);

    // For old existing code that has not mage the switch.
    Object.defineProperty(scope.CourseSketch, 'PROTOBUF_UTIL', {
        /**
         * @returns {Enum} An existing protobuf
         */
        get: function() {
            // prints stack trace
            console.warn('USING OLD TYPE OF PROTOBUF USE prutil INSTEAD [trace of possible use below]');
            console.trace();
            console.warn('USING OLD TYPE OF PROTOBUF USE prutil INSTEAD [trace of possible use above]');
            return protobuf;
        }
    });
})(window);
