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
    var sketch = new SRL_Sketch();
    var updateList = undefined;
    var localInputListener = undefined;
    var sketchEventConverter = undefined;
    var shadowRoot = undefined;
    var errorListener = undefined;

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
            var command = PROTOBUF_UTIL.createBaseCommand(PROTOBUF_UTIL.CommandType.CREATE_SKETCH, false);
            var idChain = PROTOBUF_UTIL.IdChain();
            idChain.idChain = [ this.id ];
            command.setCommandData(idChain.toArrayBuffer());
            var update = PROTOBUF_UTIL.createUpdateFromCommands([ command ]);
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
        console.log("Creating update list");
        console.log(updateList);
        console.log(this.id);
        if (isUndefined(updateList)) {
            if (UpdateManagerClass instanceof UpdateManager) {
                updateList = UpdateManagerClass;
            } else {
                updateList = new UpdateManagerClass(sketch, errorListener, SKETCHING_SURFACE_HANDLER);
            }
            // We define the surface to have a list.
            this.dataset.existingList = "";
            this.updateListTEMP = updateList;
        } else {
            this.updateListTEMP = updateList;
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

        var command = PROTOBUF_UTIL.createBaseCommand(PROTOBUF_UTIL.CommandType.ADD_STROKE, true);

        command.commandData = stroke.sendToProtobuf(parent).toArrayBuffer();
        command.decodedData = stroke;
        var update = PROTOBUF_UTIL.createUpdateFromCommands([ command ]);
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

        canvasSize(canvas);
    };

    /**
     * 
     */
    function canvasSize(canvas) {
        canvas.height = $(canvas).height();
        canvas.width = $(canvas).width();
        $(window).resize(function() {
            canvas.height = $(canvas).height();
            canvas.width = $(canvas).width();

            sketch.drawEntireSketch();
        });
    }
}

SketchSurface.prototype = Object.create(HTMLElement.prototype);

/**
 * @param document
 *            {document} The document in which the node is being imported to.
 * @param template
 *            {Element} an element representing a template tag, its content is
 *            imported and then added to this element.
 */
SketchSurface.prototype.initializeElement = function(document, template) {
    var clone = document.importNode(template.content, true);

    var root = this.createShadowRoot();
    this.setRoot(root);
    root.appendChild(clone);
};

SketchSurface.prototype.initializeSurface = function(InputListenerClass, SketchEventConverterClass, UpdateManagerClass) {

    if (isUndefined(this.dataset.readOnly)) {
        this.initializeInput(InputListenerClass, SketchEventConverterClass);
    }

    if (isUndefined(this.dataset.customId) || isUndefined(this.id) || this.id == null || this.id == "") {
        console.log("CREATING ID");
        console.log("[" + this.id + "]");
        console.log("ID");
        this.id = generateUUID();
    }

    if (isUndefined(this.dataset.existingList)) {
        this.bindToUpdateList(UpdateManagerClass);
    }

    this.registerSketchInManager();

    if (isUndefined(this.dataset.existingList) && isUndefined(this.dataset.customId)) {
        this.createSketchUpdate();
    }
};
