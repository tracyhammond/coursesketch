/**
 *
 * The Sketch Surface is actually used as part of an element. but can be used
 * without actually being an element if you spoof some methods.
 *
 * Supported attributes.
 * <ul>
 * <li>data-existingList: This is meant to tell the surface that the update
 * list will be provided for it and to not automatically bind an UpdateManager
 * to it</li>
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

    this.registerSketchInManager = function() {
        if (isUndefined(this.id)) {
            this.id = generateUUID();
        }
        CourseSketch.SKETCHING_SURFACE_HANDLER.addElement(this);
    };

    /**
     * Does some manual GC. TODO: unlink some circles manually.
     */
    this.finalize = function() {
        this.updateList = undefined;
        this.localInputListener = undefined;
        this.sketchEventConverter = undefined;
        this.sketch = undefined;
        CourseSketch.SKETCHING_SURFACE_HANDLER.deleteSketch(this.id);
    };

    /**
     * Creates a sketch update in the update list if it is empty so the update
     * list knows what Id to assign to subsequent events.
     */
    this.createSketchUpdate = function(preDefinedUpdate) {
        if (isUndefined(this.id)) {
            this.id = generateUUID();
        }
        if (!isUndefined(this.updateList) && this.updateList.getListLength() <= 0) {
            var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_SKETCH, false);
            var idChain = CourseSketch.PROTOBUF_UTIL.IdChain();
            idChain.idChain = [ this.id ];
            var createSketchAction = CourseSketch.PROTOBUF_UTIL.ActionCreateSketch();
            if (!isUndefined(preDefinedUpdate)) {
                createSketchAction = preDefinedUpdate;
            } else {
                createSketchAction.sketchId = idChain;
                createSketchAction.x = -1;
                createSketchAction.y = -1;
                createSketchAction.width = -1;
                createSketchAction.height = -1;
            }
            command.setCommandData(createSketchAction.toArrayBuffer());
            var update = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([ command ]);
            this.updateList.addUpdate(update);
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
    this.getSrlSketch = function() {
        return this.sketch;
    };

    /**
     * binds the sketch surface to an update list.
     * @param UpdateManagerClass {UpdateManager instance | UpdateManager class} this takes an either an instance of an update manager.
     * Or a update manager class that is then constructed.
     * You can only bind an update list to a sketch once.
     */
    this.bindToUpdateList = function(UpdateManagerClass) {
        if (this.bindUpdateListCalled === false) {
            this.updateList = undefined;
        }

        if (isUndefined(this.updateList)) {
            if (UpdateManagerClass instanceof UpdateManager) {
                this.updateList = UpdateManagerClass;
            } else {
                this.updateList = new UpdateManagerClass(this.sketch, this.errorListener, CourseSketch.SKETCHING_SURFACE_HANDLER);
            }
            this.bindUpdateListCalled = true;
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
        stroke.draw( this.localInputListener.canvasContext);

        var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, true);

        command.commandData = stroke.sendToProtobuf(parent).toArrayBuffer();
        command.decodedData = stroke;
        var update = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([ command ]);
        this.updateList.addUpdate(update);
    }

    /**
     * @param InputListener
     *            {InputListenerClass} a class that represents an input listener
     * @param SketchEventConverter
     *            {SketchEventConverterClass} a class that represents
     */
    this.initializeInput = function(InputListener, SketchEventConverter) {
        this.localInputListener = new InputListener();
        this.localInputListener.initializeCanvas(this.sketchCanvas);
        var canvasContext = this.localInputListener.canvasContext;
        this.sketch.canvasContext = canvasContext;
        this.sketchEventConverter = new SketchEventConverter(this.localInputListener, addStrokeCallback.bind(this), canvasContext);
        this.eventListenerElement = this.sketchCanvas;

        this.resizeSurface();
    };

    /**
     * resize the canvas so that its dimensions are the same as the css dimensions.  (this makes it a 1-1 ratio).
     */
    this.resizeSurface = function() {
        console.log(this.sketchCanvas);
        this.sketchCanvas.height = $(this.sketchCanvas).height();
        this.sketchCanvas.width = $(this.sketchCanvas).width();
        this.sketch.drawEntireSketch();
    };

    /**
     * Binds a function that resizes the surface every time the size of the window changes.
     */
    this.makeResizeable = function() {
        $(window).resize(this.resizeSurface.bind(this));
    };

    /**
     * Initializes the sketch and resets all values.
     */
    this.initializeSketch = function() {
        this.updateList = undefined;
        bindUpdateListCalled = false;
        this.sketch = new SRL_Sketch();
        this.eventListenerElement = undefined;
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
        return this.updateList;
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
    this.sketchCanvas = this.shadowRoot.querySelector("#drawingCanvas");
};

SketchSurface.prototype.initializeSurface = function(InputListenerClass, SketchEventConverterClass, UpdateManagerClass) {
    this.initializeSketch();

    if (isUndefined(this.dataset) || isUndefined(this.dataset.readonly)) {
        this.initializeInput(InputListenerClass, SketchEventConverterClass);
    }

    if (isUndefined(this.dataset) || isUndefined(this.dataset.customid) || isUndefined(this.id) || this.id == null || this.id == "") {
        this.id = generateUUID();
    }

    if (isUndefined(this.dataset) || isUndefined(this.dataset.existinglist)) {
        this.bindToUpdateList(UpdateManagerClass);
    }

    this.registerSketchInManager();

    if (isUndefined(this.dataset) || (isUndefined(this.dataset.existinglist) && isUndefined(this.dataset.customid))) {
        this.createSketchUpdate();
    }

    if (!isUndefined(this.dataset) && !(isUndefined(this.dataset.autoresize))) {
        this.makeResizeable();
    }
};
