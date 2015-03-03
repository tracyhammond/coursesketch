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
function UserActionGraph() {
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
     *            {InputListenerClass} a class that represents an input listener
     * @param SketchEventConverter
     *            {SketchEventConverterClass} a class that represents
     */
    this.initializeInput = function(InputListener, SketchEventConverter) {
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
            if (firstCommand.commandType == CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_SKETCH) {
                var sketch = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(firstCommand.commandData,
                    CourseSketch.PROTOBUF_UTIL.getActionCreateSketchClass());
                this.id = sketch.sketchId.idChain[0];
                this.sketchManager.setParentSketchId(this.id);
            }
        }
    }

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

UserActionGraph.prototype = Object.create(HTMLElement.prototype);

/**
 * @param document
 *            {document} The document in which the node is being imported to.
 * @param templateClone
 *            {Element} an element representing the data inside tag, its content
 *            has already been imported and then added to this element.
 */
UserActionGraph.prototype.initializeElement = function(templateClone) {
    var root = this.createShadowRoot();
    root.appendChild(templateClone);
    this.shadowRoot = this;
    document.body.appendChild(templateClone);
    this.sketchCanvas = this.shadowRoot.querySelector("#userActionGraph");
};


UserActionGraph.prototype.initializeSurface = function(InputListenerClass, UpdateManagerClass) {
    this.initializeSketch();
    this.initializeGraphics();

    if (isUndefined(this.dataset) || isUndefined(this.dataset.readonly)) {
        this.initializeInput(InputListenerClass);
    }

    if (isUndefined(this.dataset) || isUndefined(this.dataset.customid) || isUndefined(this.id) || this.id == null || this.id == "") {
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

UserActionGraph.prototype.initializeGraph = function() {

    var margin = {top: 30, right: 20, bottom: 30, left: 20},
        width = 960 - margin.left - margin.right,
        barHeight = 20,
        barWidth = width * .8;

    var i = 0,
        duration = 400,
        root;

    var tree = d3.layout.tree()
        .nodeSize([0, 20]);

    var diagonal =function(d, i) {
        return "M" + d.source.y + "," + d.source.x
            + "V" + d.target.x + "H" + d.target.y;
    }

    var svg = d3.select(this.getElementForDrawing()).append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", "100%")
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");


    function Node(data){
        this.name = data;
        this.parent = this;
        this.children = [];
    }

    Node.prototype.AddChild=function(child){
        child.parent = this;
        this.children.push(child);
    }

    var updateList = new UpdateManager(undefined, function(error) {
        console.log(error);
    });

    //begin creating test data
    var stubs = function(){
        return false;
    }

    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.ASSIGN_ATTRIBUTE, stubs);
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.ASSIGN_ATTRIBUTE, stubs);

    var assignAt = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.ASSIGN_ATTRIBUTE, true);
    var assignUpdate = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([ assignAt ]);

    updateList.addUpdate(assignUpdate);
    updateList.addUpdate(assignUpdate);
    updateList.addUpdate(assignUpdate);
    updateList.undoAction(false);
    updateList.undoAction(false);
    updateList.redoAction(false);
    updateList.undoAction(false);
    updateList.redoAction(false);
    updateList.addUpdate(assignUpdate);
    updateList.undoAction(false);
    updateList.redoAction(false);
    updateList.addUpdate(assignUpdate);

    var updateData = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
    updateData.list=(updateList.getUpdateList());
    console.log(updateData);
    //end creating fake data

    //start putting data into tree structure
    var base = new Node("User Action History");
    var currentParent = base;
    var markerObject;
    for(var u = 0; u < updateData.list.length; u++){
        //checks to see if current command corresponds to previous lowest marker
        if(updateData.list[u].commands[0].getCommandTypeName()=="MARKER") {
            markerObject = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(updateData.list[u].commands[0].commandData, CourseSketch.PROTOBUF_UTIL.getMarkerClass());
            if(markerObject.otherData>0){
                currentParent=currentParent.children[currentParent.children.length-1];
                currentParent.AddChild(new Node(updateData.list[u].commands[0].getCommandTypeName()));
            } else {
                currentParent.AddChild(new Node(updateData.list[u].commands[0].getCommandTypeName()));
                currentParent = currentParent.parent;
            }
        } else {
            currentParent.AddChild(new Node(updateData.list[u].commands[0].getCommandTypeName()));
        }
    }

    d3.json(base, function(error) {
        base.x0 = 0;
        base.y0 = 0;
        update(root = base);
    });

    function update(source) {
        // Compute the flattened node list. TODO use d3.layout.hierarchy.
        var nodes = tree.nodes(root);

        var height = Math.max(500, nodes.length * barHeight + margin.top + margin.bottom);

        d3.select("svg").transition()
            .duration(duration)
            .attr("height", height);

        d3.select(self.frameElement).transition()
            .duration(duration)
            .style("height", height + "px");

        // Compute the "layout".
        nodes.forEach(function(n, i) {
            n.x = i * barHeight;
        });

        // Update the nodes…
        var node = svg.selectAll("g.node")
            .data(nodes, function(d) { return d.id || (d.id = ++i); });

        var nodeEnter = node.enter().append("g")
            .attr("class", "node")
            .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
            .style("opacity", 1e-6);

        // Enter any new nodes at the parent's previous position.
        nodeEnter.append("rect")
            .attr("y", -barHeight / 2)
            .attr("height", barHeight)
            .attr("width", barWidth)
            .style("fill", color)
            .on("click", click);

        nodeEnter.append("text")
            .attr("dy", 3.5)
            .attr("dx", 5.5)
            .text(function(d) { return d.name; });

        // Transition nodes to their new position.
        nodeEnter.transition()
            .duration(duration)
            .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
            .style("opacity", 1);

        node.transition()
            .duration(duration)
            .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
            .style("opacity", 1)
            .select("rect")
            .style("fill", color);

        // Transition exiting nodes to the parent's new position.
        node.exit().transition()
            .duration(duration)
            .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
            .style("opacity", 1e-6)
            .remove();

        // Update the links…
        var link = svg.selectAll("path.link")
            .data(tree.links(nodes), function(d) { return d.target.id; });

        // Enter any new links at the parent's previous position.
        link.enter().insert("path", "g")
            .attr("class", "link")
            .attr("d", function(d) {
                var o = {x: source.x0, y: source.y0};
                return diagonal({source: o, target: o});
            })
            .transition()
                .duration(duration)
                .attr("d", diagonal);

        // Transition links to their new position.
        link.transition()
            .duration(duration)
            .attr("d", diagonal);

        // Transition exiting nodes to the parent's new position.
        link.exit().transition()
            .duration(duration)
            .attr("d", function(d) {
                var o = {x: source.x, y: source.y};
                return diagonal({source: o, target: o});
            })
            .remove();

        // Stash the old positions for transition.
        nodes.forEach(function(d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });
    }

    // Toggle children on click.
    function click(d) {
        if (d.children) {
            d._children = d.children;
            d.children = null;
        } else {
            d.children = d._children;
            d._children = null;
        }
        update(d);
    }

    function color(d) {
        return d._children ? "#3182bd" : d.children ? "#c6dbef" : "#fd8d3c";
    }

};
