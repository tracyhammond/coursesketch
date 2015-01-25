validateFirstGlobalRun(document.currentScript);

/*******************************************************************************
 * METHODS FOR THE REDO AND UNDO ARE BELOW.
 *
 * Each method is a prototype of the command or the update
 ******************************************************************************/
(function() {

    /**
     * Removes all elements of the sketch.
     * @returns {boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CLEAR, function() {
        var sketch = this.getLocalSketchSurface();
        var objects = sketch.resetSketch();
        this.decodedData = objects;
        return true;
    });

    /**
     * Adds all of the sketch data back.
     *
     * @returns {boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CLEAR, function() {
        var sketch = this.getLocalSketchSurface();
        sketch.addAllSubObjects(this.decodedData);
        this.decodedData = undefined;
        return true;
    });

    /**
     * Do nothing
     *
     * @returns {boolean} true.  because if we switch sketch we should probably do something about it.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_SKETCH, function() {
        return true;
    });

    /**
     * Do nothing
     *
     * @returns {boolean} true.  because if we switch sketch we should probably do something about it.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.SWITCH_SKETCH, function() {
        return true;
    });

    /**
     * Do nothing
     *
     * @returns {boolean} true.  because if we switch sketch we should probably do something about it.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_SKETCH, function() {
        return true;
    });

    /**
     * Do nothing
     *
     * @returns {boolean} true.  because if we switch sketch we should probably do something about it.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.SWITCH_SKETCH, function() {
        return true;
    });

    /**
     * Adds a stroke to this local sketch object.
     *
     * @returns {boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, function() {
        if (!this.decodedData) {
            var stroke = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData, CourseSketch.PROTOBUF_UTIL.getProtoSrlStrokeClass());
            this.decodedData = SRL_Stroke.createFromProtobuf(stroke);
        }
        this.getLocalSketchSurface().addObject(this.decodedData);
        return true;
    });

    /**
     * The undo method associated with adding a stroke to the sketch
     *
     * @returns {boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, function() {
        if (!this.decodedData) {
            var stroke = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData, CourseSketch.PROTOBUF_UTIL.getProtoSrlStrokeClass());
            this.decodedData = SRL_Stroke.createFromProtobuf(stroke);
        }
        this.getLocalSketchSurface().removeSubObjectById(this.decodedData.getId());
        return true;
    });

    /**
     * Adds a shape to this local sketch object.
     *
     * @returns {boolean} false. This will never ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.ADD_SHAPE, function() {
        if (!this.decodedData) {
            var shape = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData, CourseSketch.PROTOBUF_UTIL.getProtoSrlShapeClass());
            this.decodedData = SRL_Shape.createFromProtobuf(shape);
        }
        this.getLocalSketchSurface().addObject(this.decodedData);
        return false;
    });

    /**
     * Undoes adding a shape command which basically means it removes the shape
     *
     * @returns {boolean} false. This will never ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.ADD_SHAPE, function() {
        if (!this.decodedData) {
            var shape = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData, CourseSketch.PROTOBUF_UTIL.getProtoSrlShapeClass());
            this.decodedData = SRL_Shape.createFromProtobuf(shape);
        }
        this.getLocalSketchSurface().removeSubObjectById(this.decodedData.getId());
        this.getLocalSketchSurface().addObject(this.decodedData);
        return false;
    });

    /**
     * Removes an object from the this.getLocalSketchSurface().
     *
     * @returns {boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.REMOVE_OBJECT, function() {
        if (!this.decodedData || !isArray(this.decodedData)) {
            this.decodedData = new Array();
            var idChain = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData, CourseSketch.PROTOBUF_UTIL.getIdChainClass());
            this.decodedData[0] = idChain;
        }
        this.decodedData[1] = this.getLocalSketchSurface().removeSubObjectByIdChain(this.decodedData[0].idChain);
        return true;
    });

    /**
     * Undoes removing an object from the sketch Removes an object from the
     * this.getLocalSketchSurface().
     *
     * @returns {boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.REMOVE_OBJECT, function() {
        if (!this.decodedData || !isArray(this.decodedData)) {
            this.decodedData = new Array();
            var idChain = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData, CourseSketch.PROTOBUF_UTIL.getIdChainClass());
            this.decodedData[0] = idChain;
        }
        this.getLocalSketchSurface().addObject(this.decodedData[1]);
        // this.decodedData[1];
        return true;
    });

    /**
     * Moves shapes from one shape to another shape.
     *
     * @returns {boolean} false. This will never ask for the sketch to be
     *          redrawn. TODO: change it so that it knows what sketch it is
     *          associated with.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addRedoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.PACKAGE_SHAPE, function() {
        if (isUndefined(this.decodedData) || (!this.decodedData)) {
            this.decodedData = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData, CourseSketch.PROTOBUF_UTIL.getActionPackageShapeClass());
        }
        this.decodedData.redo(this.getLocalSketchSurface());
        return false;
    });

    /**
     * Moves shapes from one shape to another shape. But does the opposite as
     * the redo package shape
     *
     * @returns {boolean} false. This will never ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.PROTOBUF_UTIL.getSrlCommandClass().addUndoMethod(CourseSketch.PROTOBUF_UTIL.CommandType.PACKAGE_SHAPE, function() {
        if (isUndefined(this.decodedData) || (!this.decodedData)) {
            this.decodedData = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(this.commandData, CourseSketch.PROTOBUF_UTIL.getActionPackageShapeClass());
        }
        this.decodedData.undo(this.getLocalSketchSurface());
        return false;
    });

    /***************************************************************************
     * MARKER SPECIFIC UPDATES.
     **************************************************************************/

    /**
     * @returns the human readable name of the given marker type
     */

    CourseSketch.PROTOBUF_UTIL.getMarkerClass().prototype.getCommandTypeName = function() {
        switch (this.getType()) {
            case this.MarkerType.SUBMISSION:
                return 'SUBMISSION';
            case CourseSketch.PROTOBUF_UTIL.CommandType.FEEDBACK:
                return 'FEEDBACK';
            case CourseSketch.PROTOBUF_UTIL.CommandType.SAVE:
                return 'SAVE';
            case CourseSketch.PROTOBUF_UTIL.CommandType.SPLIT:
                return 'SPLIT';
            case CourseSketch.PROTOBUF_UTIL.CommandType.CLEAR:
                return 'CLEAR';
        }
        return "NO_NAME # is: " + this.getCommandType();
    };

    /***************************************************************************
     * Specific commands and their actions.
     **************************************************************************/

    /**
     * Moves the shapes from the old container to the new container.
     *
     * @param sketch
     *            {SrlSketch} the sketch object that is being affected by these
     *            changes.
     */
    CourseSketch.PROTOBUF_UTIL.getActionPackageShapeClass().prototype.redo = function(sketch) {
        var oldContainingObject = !(this.oldContainerId) ? sketch : sketch.getSubObjectByIdChain(this.oldContainerId.getIdChain());
        var newContainingObject = !(this.newContainerId) ? sketch : sketch.getSubObjectByIdChain(this.newContainerId.getIdChain());

        if (oldContainingObject == newContainingObject)
        // done moving to same place.
        return;
        for (var shapeIndex = 0; shapeIndex < this.shapesToBeContained.length; shapeIndex++) {
            var shapeId = this.shapesToBeContained[shapeIndex];
            var object = oldContainingObject.removeSubObjectById(shapeId);
            newContainingObject.addSubObject(object);
        }
    };

    /**
     * Moves the shapes from the new container to the old container.
     *
     * This is a reverse of the process used in redo.
     *
     * @param sketch
     *            {SrlSketch} the sketch object that is being affected by these
     *            changes.
     */
    CourseSketch.PROTOBUF_UTIL.getActionPackageShapeClass().prototype.undo = function(sketch) {
        var oldContainingObject = !(this.newContainerId) ? sketch : sketch.getSubObjectByIdChain(this.newContainerId.getIdChain());
        var newContainingObject = !(this.oldContainerId) ? sketch : sketch.getSubObjectByIdChain(this.oldContainerId.getIdChain());

        if (oldContainingObject == newContainingObject)
        // done moving to same place.
        return;

        for (shapeId in this.shapesToBeContained) {
            var object = oldContainingObject.removeSubObjectById(shapeId);
            if (newContainerId) {
                newContainingObject.addSubObject(object);
            } else {
                newContainingObject.addObject(object);
            }
        }
    };

})();// (CourseSketch.PROTOBUF_UTIL.SrlUpdate, CourseSketch.PROTOBUF_UTIL.SrlCommand, CourseSketch.PROTOBUF_UTIL);
