/**
 *
 * The Sketch Surface is actually used as part of an element. but can be used
 * without actually being an element if you spoof some methods.
 *
 * Supported attributes.
 * <ul>
 * <li>data-existingList: This is meant to tell the surface that the update
 * list will be provided for it.</li>
 *
 * <li>data-existingManager: This is meant to tell the surface that the update
 * manager will be provided for it. and to not bind an update manager</li>
 *
 * <li>data-customId: This is meant to tell the surface that the Id of the
 * element will be provided to it and to not assign a random id to it.</li>
 *
 * <li>data-readOnly: This tells the sketch surface to ignore any input and it
 * will only display sketches.</li>
 *
 * <li>data-autoResize: This is meant to tell the sketch surface that it
 * should resize itself every time the window changes size.</li>
 * </ul>
 *
 * @Class
 */
function SketchSurface() {
    this.bindUpdateListCalled = false;

    /**
     * Does some manual GC. TODO: unlink some circles manually.
     */
    this.finalize = function() {
        this.updateManager.clearUpdates(false, true);
        this.updateManager = undefined;
        this.localInputListener = undefined;
        this.sketchEventConverter = undefined;
        this.sketch = undefined;
        this.sketchManager = undefined;
        this.graphics.finalize();
    };

    /**
     * Creates a sketch update in the update list if it is empty so the update
     * list knows what Id to assign to subsequent events.
     */
    this.createSketchUpdate = function() {
        if (isUndefined(this.id)) {
            this.id = generateUUID();
        }
        if (!isUndefined(this.updateManager) && this.updateManager.getListLength() <= 0) {
            var command = CourseSketch.PROTOBUF_UTIL.createNewSketch(this.id);
            var update = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([ command ]);
            this.updateManager.addUpdate(update);
        }
    };

    /**
     * Sets the listener that is called when an error occurs.
     */
    this.setErrorListener = function(error) {
        this.errorListener = error;
    };

    /**
     * Returns the sketch object used by this sketch surface.
     */
    this.getCurrentSketch = function() {
        return this.sketchManager.getCurrentSketch();
    };

    /**
     * binds the sketch surface to an update manager.
     * @param UpdateManagerClass {UpdateManager instance | UpdateManager class} this takes an either an instance of an update manager.
     * Or a update manager class that is then constructed.
     * You can only bind an update list to a sketch once.
     */
    this.bindToUpdateManager = function(UpdateManagerClass) {
        if (this.bindUpdateListCalled === false) {
            this.updateManager = undefined;
        }

        if (isUndefined(this.updateManager)) {
            if (UpdateManagerClass instanceof UpdateManager) {
                this.updateManager = UpdateManagerClass;
            } else {
                this.updateManager = new UpdateManagerClass(this.sketchManager, this.errorListener);
            }
            this.bindUpdateListCalled = true;
            // sets up the plugin that draws the strokes as they are added to the update list.
            this.updateManager.addPlugin(this.graphics);
        } else {
            throw new Error("Update list is already defined");
        }
    };

    /**
     * Draws the stroke then creates an update that is added to the update
     * manager, given a stroke.
     *
     * @param stroke
     *            {SRL_Stroke} a stroke that is added to the sketch.
     */
    function addStrokeCallback(stroke) {

        var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, true);
        var protoStroke = stroke.sendToProtobuf(parent);
        command.commandData = protoStroke.toArrayBuffer();
        command.decodedData = stroke;
        var update = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([ command ]);
        this.updateManager.addUpdate(update);
    }

    /**
     * @param InputListener
     *            {InputListenerClass} a class that represents an input listener.
     */
    this.initializeInput = function(InputListener) {
        this.localInputListener = new InputListener();

        this.localInputListener.initializeCanvas(this, addStrokeCallback.bind(this), this.graphics);

        this.eventListenerElement = this.sketchCanvas;

        this.resizeSurface();
    };

    /**
     * resize the canvas so that its dimensions are the same as the css dimensions.  (this makes it a 1-1 ratio).
     */
    this.resizeSurface = function() {
        this.sketchCanvas.height = $(this.sketchCanvas).height();
        this.sketchCanvas.width = $(this.sketchCanvas).width();
    };

    /**
     * Binds a function that resizes the surface every time the size of the window changes.
     */
    this.makeResizeable = function() {
        $(window).resize(function() {
            this.resizeSurface();
            this.graphics.correctSize();
        }.bind(this));
    };

    /**
     * Initializes the sketch and resets all values.
     */
    this.initializeSketch = function() {
        this.sketchManager = new SketchSurfaceManager(this);
        this.updateManager = undefined;
        bindUpdateListCalled = false;
        this.sketchManager.setParentSketch(new SRL_Sketch());
        this.eventListenerElement = undefined;
    };

    /**
     * Initializes the graphics for the sketch surface.
     */
    this.initializeGraphics = function() {
        this.graphics = new Graphics(this.sketchCanvas, this.sketchManager);
    };

    /**
     * Returns the element that listens to the input events.
     */
    this.getElementForEvents = function() {
        return this.eventListenerElement;
    };

    /**
     * returns the element where the sketch is drawn to.
     */
    this.getElementForDrawing = function() {
        return this.sketchCanvas;
    };

    /**
     * returns the update list of the element.
     */
    this.getUpdateList = function() {
        return this.updateManager.getUpdateList();
    };

    /**
     * Returns the manager for this sketch surface.
     */
    this.getUpdateManager = function() {
        return this.updateManager;
    };

    /**
     * @return SrlUpdateList proto object.
     * This is a cleaned version of the list and modifying this list will not affect the update manager list.
     */
    this.getSrlUpdateListProto = function() {
        var updateProto = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
        updateProto.list = this.updateManager.getUpdateList();
        return CourseSketch.PROTOBUF_UTIL.decodeProtobuf(updateProto.toArrayBuffer(), CourseSketch.PROTOBUF_UTIL.getSrlUpdateListClass());
    };

    /**
     * Redraws the sketch so it is visible on the screen.
     */
    this.refreshSketch = function() {
        this.graphics.loadSketch();
    };

    /**
     * Extracts the canvas id from the sketch list.
     */
    this.extractIdFromList = function(updateList) {
        var update = updateList[0];
        if (!isUndefined(update)) {
            var firstCommand = update.commands[0];
            if (firstCommand.commandType === CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_SKETCH) {
                var sketch = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(firstCommand.commandData,
                    CourseSketch.PROTOBUF_UTIL.getActionCreateSketchClass());
                this.id = sketch.sketchId.idChain[0];
                this.sketchManager.setParentSketchId(this.id);
            }
        }
    };

    /**
     * Loads all of the updates into the sketch.
     * This should only be done after the sketch surface is inserted into the dom.
     * @param updateList {Array<SrlUpdate>}
     */
    this.loadUpdateList = function(updateList, percentBar, finishedCallback) {
        try {
            this.extractIdFromList(updateList);
        } catch(exception) {
            console.error(exception);
            throw exception;
        }
        this.updateManager.setUpdateList(updateList, percentBar, finishedCallback);
    };

    /**
     * Tells the sketch surface to fill the screen so it is completely visible.
     * This currently is only allowed on read-only canvases
     */
    this.fillCanvas = function() {
        if (isUndefined(this.dataset) || isUndefined(this.dataset.readonly)) {
            throw new BaseException("This can only be performed on read only sketch surfaces");
        }
    };
}

SketchSurface.prototype = Object.create(HTMLElement.prototype);

/**
 * @param document
 *            {document} The document in which the node is being imported to.
 * @param templateClone
 *            {Element} an element representing the data inside tag, its content
 *            has already been imported and then added to this element.
 */
SketchSurface.prototype.initializeElement = function(templateClone) {
    var root = this.createShadowRoot();
    root.appendChild(templateClone);
    this.shadowRoot = this;
    document.body.appendChild(templateClone);
    this.sketchCanvas = this.shadowRoot.querySelector("#drawingCanvas");
};


SketchSurface.prototype.initializeSurface = function(InputListenerClass, UpdateManagerClass) {
    this.initializeSketch();
    this.initializeGraphics();

    if (isUndefined(this.dataset) || isUndefined(this.dataset.readonly)) {
        this.initializeInput(InputListenerClass);
    }

    if (isUndefined(this.dataset) || isUndefined(this.dataset.customid) || isUndefined(this.id) || this.id === null || this.id === "") {
        this.id = generateUUID();
    }

    if (isUndefined(this.dataset) || isUndefined(this.dataset.existingManager)) {
        this.bindToUpdateManager(UpdateManagerClass);
    }

    if (isUndefined(this.dataset) || (isUndefined(this.dataset.existinglist) && isUndefined(this.dataset.customid))) {
        this.createSketchUpdate();
    }

    if (!isUndefined(this.dataset) && !(isUndefined(this.dataset.autoresize))) {
        this.makeResizeable();
    }
};
