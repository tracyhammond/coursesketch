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
 * <li>data-readOnly: This tells the sketch surface to ignore any input and it
 * will only display sketches.</li>
 * </ul>
 *
 * @Class
 */
function SketchSurface() {
    var sketch = undefined;
    var updateList = undefined;
    var localInputListener = undefined;
    var sketchEventConverter = undefined;
    var shadowRoot = undefined;
    var errorListener = undefined;
    this.bindUpdateListCalled = false;

    this.registerSketchInManager = function() {
        if (isUndefined(this.id)) {
            this.id = generateUUID();
        }
        SKETCHING_SURFACE_HANDLER.addElement(this);
    };

    /**
     * Does some manual GC. TODO: unlink some circles manually.
     */
    this.finalize = function() {
        updateList = undefined;
        localInputListener = undefined;
        sketchEventConverter = undefined;
        sketchEventConverter = undefined;
        sketch = undefined;
        SKETCHING_SURFACE_HANDLER.deleteSketch(this.id);
    };

    /**
     * Creates a sketch update in the update list if it is empty so the update
     * list knows what Id to assign to subsequent events.
     */
    this.createSketchUpdate = function() {
        if (isUndefined(this.id)) {
            this.id = generateUUID();
        }
        if (!isUndefined(updateList) && updateList.getListLength() <= 0) {
            var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_SKETCH, false);
            var idChain = CourseSketch.PROTOBUF_UTIL.IdChain();
            idChain.idChain = [ this.id ];
            command.setCommandData(idChain.toArrayBuffer());
            var update = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([ command ]);
            updateList.addUpdate(update);
        }
    };

    this.setErrorListener = function(error) {
        errorListener = error;
    };

    this.setRoot = function(root) {
        shadowRoot = root;
    };

    this.getSrlSketch = function() {
        return sketch;
    };

    this.bindToUpdateList = function(UpdateManagerClass) {
        if (this.bindUpdateListCalled === false) {
            updateList = undefined;
        }

        if (isUndefined(updateList)) {
            if (UpdateManagerClass instanceof UpdateManager) {
                updateList = UpdateManagerClass;
            } else {
                updateList = new UpdateManagerClass(sketch, errorListener, SKETCHING_SURFACE_HANDLER);
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
        stroke.draw(sketch.canvasContext);

        var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, true);

        command.commandData = stroke.sendToProtobuf(parent).toArrayBuffer();
        command.decodedData = stroke;
        var update = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([ command ]);
        updateList.addUpdate(update);
    }

    /**
     * @param InputListener
     *            {InputListenerClass} a class that represents an input listener
     * @param SketchEventConverter
     *            {SketchEventConverterClass} a class that represents
     */
    this.initializeInput = function(InputListener, SketchEventConverter) {
        localInputListener = new InputListener();
        var canvas = shadowRoot.querySelector("#drawingCanvas");
        localInputListener.initializeCanvas(canvas);
        var canvasContext = localInputListener.canvasContext;
        sketch.canvasContext = canvasContext;
        sketchEventConverter = new SketchEventConverter(localInputListener, addStrokeCallback, canvasContext);

        this.eventListenerElement = canvas;
        this.sketchCanvas = canvas;

        this.resizeSurface();
    };

    this.resizeSurface = function() {
        this.sketchCanvas.height = $(this.sketchCanvas).height();
        this.sketchCanvas.width = $(this.sketchCanvas).width();
        sketch.drawEntireSketch();
    };

    this.makeResizeable = function() {
        $(window).resize(this.resizeSurface);
    };

    /**
     * Initializes the sketch and resets all values.
     */
    this.initializeSketch = function() {
        updateList = undefined;
        bindUpdateListCalled = false;
        sketch = new SRL_Sketch();
        this.eventListenerElement = undefined;
        this.sketchCanvas = undefined;
    };

    this.getElementForEvents = function() {
        return this.eventListenerElement;
    };

    this.getElementForDrawing = function() {
        return this.eventListenerElement;
    };

    this.getUpdateList = function() {
        return updateList;
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
SketchSurface.prototype.initializeElement = function(document, templateClone) {
    var root = this.createShadowRoot();
    this.setRoot(root);
    root.appendChild(templateClone);
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
};
