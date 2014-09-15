/* depends on the protobuf library, base.js, objectAndInheritance.js */

function ProtobufException(message) {
    this.name = "ProtobufException";
    this.setMessage(message);
    this.message = "";
    this.htmlMessage = "";
};
ProtobufException.prototype = BaseException;

/**
 * @Class Has utilities for protobufs and is a convient accessor to create new
 *        instances of protobuf files (and prevents the modification of a
 *        protobuf object creator before it is created)
 */
function ProtobufSetup() {

    var localScope = this;
    var PROTOBUF_PACKAGE = 'protobuf';
    var protobufDirectory = "/other/protobuf/";

    var objectList = new Array();
    var enumList = new Array();
    var ready = false;

    /**
     * @returns {ProtobufSetup} an instance of itself.
     */
    this.initializeBuf = function() {

        buildMessage();
        buildSchool();
        buildSketch();
        buildUpdateList();
        buildDataQuery();
        buildSubmissions();
        ready = true;
        return localScope;
    };

    function buildMessage() {
        var builder = dcodeIO.ProtoBuf.protoFromFile(protobufDirectory + "message.proto");
        var requestPackage = builder.build(PROTOBUF_PACKAGE).srl.request;
        assignValues(requestPackage);
    }

    function buildDataQuery() {
        var builder = dcodeIO.ProtoBuf.protoFromFile(protobufDirectory + "data.proto");
        var QueryBuilder = builder.build(PROTOBUF_PACKAGE).srl.query;
        assignValues(QueryBuilder);
    }

    function buildSchool() {
        var builder = dcodeIO.ProtoBuf.protoFromFile(protobufDirectory + "school.proto");
        var SchoolBuilder = builder.build(PROTOBUF_PACKAGE).srl.school;
        assignValues(SchoolBuilder);
    }

    function buildSketch() {
        var builder = dcodeIO.ProtoBuf.protoFromFile(protobufDirectory + "sketch.proto");
        var sketchBuilder = builder.build(PROTOBUF_PACKAGE).srl.sketch;
        assignValues(sketchBuilder, 'Proto');
    }

    function buildUpdateList() {
        var builder = dcodeIO.ProtoBuf.protoFromFile(protobufDirectory + "commands.proto");
        var ProtoUpdateCommandBuilder = builder.build(PROTOBUF_PACKAGE).srl.commands;
        assignValues(ProtoUpdateCommandBuilder);
    }

    function buildSubmissions() {
        var builder = dcodeIO.ProtoBuf.protoFromFile(protobufDirectory + "submission.proto");
        var ProtoSubmissionBuilder = builder.build(PROTOBUF_PACKAGE).srl.submission;
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
        // safe checking
        if (isUndefined(preString)) {
            preString = '';
        }
        for (object in protoPackage) {
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
        if (!isArray(commands)) {
            throw new TypeError('Invalid Type Error: Input is not an Array');
        }

        var update = this.SrlUpdate();
        update.setCommands(commands);
        var n = createTimeStamp();
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
     *            PROTOBUF_UTIL.CommandType).
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
     * @Method
     * @returns {Array} A cloned version of the array that contains all of the
     *          current protobuf objects.
     */
    this.getSupportedObjects = function getSupportedObjects() {
        return objectList;// JSON.parse(JSON.stringify(objectList)); // why is
                            // this
        // always so fast?
    };

    /**
     * @Method
     * @returns {Array} A cloned version of the array that contains all of the
     *          current protobuf enums.
     */
    this.getSupportedEnums = function getSupportedObjects() {
        return enumList;// JSON.parse(JSON.stringify(enumList)); // why is this
        // always so fast?
    };

    makeValueReadOnly(localScope, "ProtobufException", ProtobufException);
};

(function(scope) {
    if (!isUndefined(scope.PROTOBUF_UTIL)) {
        return;
    }
    makeValueReadOnly(scope, "PROTOBUF_UTIL", new ProtobufSetup().initializeBuf());
})(this);
