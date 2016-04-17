validateFirstGlobalRun(document.currentScript, CourseSketch);

/*******************************************************************************
 * METHODS FOR THE REDO AND UNDO ARE BELOW.
 *
 * Each method is a prototype of the command or the update
 *
 * @overview This file holds the redo and undo methods for the default sketch command.
 ******************************************************************************/
(function() {

    /**
     * Removes all elements of the sketch.
     *
     * @returns {Boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.CLEAR, function() {
        var sketch = this.getLocalSketchSurface();
        var objects = sketch.resetSketch();
        this.decodedData = objects;
        return true;
    });

    /**
     * Adds all of the sketch data back.
     *
     * @returns {Boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.CLEAR, function() {
        var sketch = this.getLocalSketchSurface();
        sketch.addAllSubObjects(this.decodedData);
        this.decodedData = undefined;
        return true;
    });

    /**
     * Do nothing.
     *
     * @returns {Boolean} true.  because if we switch sketch we should probably do something about it.
     */
    CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.CREATE_SKETCH, function() {
        return true;
    });

    /**
     * Do nothing.
     *
     * @returns {Boolean} true.  because if we switch sketch we should probably do something about it.
     */
    CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.SWITCH_SKETCH, function() {
        return true;
    });

    /**
     * Do nothing.
     *
     * @returns {Boolean} true.  because if we switch sketch we should probably do something about it.
     */
    CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.CREATE_SKETCH, function() {
        return true;
    });

    /**
     * Do nothing.
     *
     * @returns {Boolean} true.  because if we switch sketch we should probably do something about it.
     */
    CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.SWITCH_SKETCH, function() {
        return true;
    });

    /**
     * Adds a stroke to this local sketch object.
     *
     * @returns {Boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.ADD_STROKE, function() {
        if (!this.decodedData) {
            var stroke = CourseSketch.prutil.decodeProtobuf(this.commandData, CourseSketch.prutil.getProtoSrlStrokeClass());
            this.decodedData = SRL_Stroke.createFromProtobuf(stroke);
        }
        this.getLocalSketchSurface().addObject(this.decodedData);
        return true;
    });

    /**
     * The undo method associated with adding a stroke to the sketch.
     *
     * @returns {Boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.ADD_STROKE, function() {
        if (!this.decodedData) {
            var stroke = CourseSketch.prutil.decodeProtobuf(this.commandData, CourseSketch.prutil.getProtoSrlStrokeClass());
            this.decodedData = SRL_Stroke.createFromProtobuf(stroke);
        }
        this.getLocalSketchSurface().removeSubObjectById(this.decodedData.getId());
        return true;
    });

    /**
     * Adds a shape to this local sketch object.
     *
     * @returns {Boolean} false. This will never ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.ADD_SHAPE, function() {
        if (!this.decodedData) {
            var shape = CourseSketch.prutil.decodeProtobuf(this.commandData, CourseSketch.prutil.getProtoSrlShapeClass());
            this.decodedData = SRL_Shape.createFromProtobuf(shape);
        }
        this.getLocalSketchSurface().addObject(this.decodedData);
        return false;
    });

    /**
     * Undoes adding a shape command which basically means it removes the shape.
     *
     * @returns {Boolean} false. This will never ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.ADD_SHAPE, function() {
        if (!this.decodedData) {
            var shape = CourseSketch.prutil.decodeProtobuf(this.commandData, CourseSketch.prutil.getProtoSrlShapeClass());
            this.decodedData = SRL_Shape.createFromProtobuf(shape);
        }
        this.getLocalSketchSurface().removeSubObjectById(this.decodedData.getId());
        this.getLocalSketchSurface().addObject(this.decodedData);
        return false;
    });

    /**
     * Removes an object from the {@code this.getLocalSketchSurface()}.
     *
     * @returns {Boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.REMOVE_OBJECT, function() {
        if (!this.decodedData || !isArray(this.decodedData)) {
            this.decodedData = [];
            var idChain = CourseSketch.prutil.decodeProtobuf(this.commandData, CourseSketch.prutil.getIdChainClass());
            this.decodedData[0] = idChain;
        }
        this.decodedData[1] = this.getLocalSketchSurface().removeSubObjectByIdChain(this.decodedData[0].idChain);
        return true;
    });

    /**
     * Undoes removing an object from the sketch Removes an object from the
     * {@code this.getLocalSketchSurface()}.
     *
     * @returns {Boolean} true. This will always ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.REMOVE_OBJECT, function() {
        if (!this.decodedData || !isArray(this.decodedData)) {
            this.decodedData = [];
            var idChain = CourseSketch.prutil.decodeProtobuf(this.commandData, CourseSketch.prutil.getIdChainClass());
            this.decodedData[0] = idChain;
        }
        this.getLocalSketchSurface().addObject(this.decodedData[1]);
        // this.decodedData[1];
        return true;
    });

    /**
     * Moves shapes from one shape to another shape.
     *
     * @returns {Boolean} false. This will never ask for the sketch to be
     *          redrawn. TODO: change it so that it knows what sketch it is
     *          associated with.
     */
    CourseSketch.prutil.getSrlCommandClass().addRedoMethod(CourseSketch.prutil.CommandType.PACKAGE_SHAPE, function() {
        if (isUndefined(this.decodedData) || (!this.decodedData)) {
            this.decodedData = CourseSketch.prutil.decodeProtobuf(this.commandData, CourseSketch.prutil.getActionPackageShapeClass());
        }
        this.decodedData.redo(this.getLocalSketchSurface());
        return false;
    });

    /**
     * Moves shapes from one shape to another shape. But does the opposite as the redo package shape.
     *
     * @returns {Boolean} false. This will never ask for the sketch to be
     *          redrawn.
     */
    CourseSketch.prutil.getSrlCommandClass().addUndoMethod(CourseSketch.prutil.CommandType.PACKAGE_SHAPE, function() {
        if (isUndefined(this.decodedData) || (!this.decodedData)) {
            this.decodedData = CourseSketch.prutil.decodeProtobuf(this.commandData, CourseSketch.prutil.getActionPackageShapeClass());
        }
        this.decodedData.undo(this.getLocalSketchSurface());
        return false;
    });

    /***************************************************************************
     * MARKER SPECIFIC UPDATES.
     **************************************************************************/

    /**
     * @returns {String} the human readable name of the given marker type.
     */
    CourseSketch.prutil.getMarkerClass().prototype.getCommandTypeName = function() {
        switch (this.getType()) {
            case this.MarkerType.SUBMISSION:
                return 'SUBMISSION';
            case CourseSketch.prutil.CommandType.FEEDBACK:
                return 'FEEDBACK';
            case CourseSketch.prutil.CommandType.SAVE:
                return 'SAVE';
            case CourseSketch.prutil.CommandType.SPLIT:
                return 'SPLIT';
            case CourseSketch.prutil.CommandType.CLEAR:
                return 'CLEAR';
        }
        return 'NO_NAME # is: ' + this.getCommandType();
    };

    /***************************************************************************
     * Specific commands and their actions.
     **************************************************************************/

    /**
     * Moves the shapes from the old container to the new container.
     *
     * @param {SrlSketch} sketch - The sketch object that is being affected by these changes.
     */
    CourseSketch.prutil.getActionPackageShapeClass().prototype.redo = function(sketch) {

        if (this.newContainerId) {
            console.log('SHAPE ID: ', this.newContainerId.getIdChain());
        }

        var oldContainingObject = !(this.oldContainerId) ? sketch : sketch.getSubObjectByIdChain(this.oldContainerId.getIdChain());
        var newContainingObject = !(this.newContainerId) ? sketch : sketch.getSubObjectByIdChain(this.newContainerId.getIdChain());

        if (oldContainingObject === newContainingObject) {
            // done moving to same place.
            return;
        }
        for (var shapeIndex = 0; shapeIndex < this.shapesToBeContained.length; shapeIndex++) {
            var shapeId = this.shapesToBeContained[shapeIndex];
            console.log('SHAPE BEING CONTAINED ID ', shapeId);
            var object = oldContainingObject.removeSubObjectById(shapeId);
            newContainingObject.addSubObject(object);
        }
    };

    /**
     * Moves the shapes from the new container to the old container.
     *
     * This is a reverse of the process used in redo.
     *
     * @param {SrlSketch} sketch - The sketch object that is being affected by these changes.
     */
    CourseSketch.prutil.getActionPackageShapeClass().prototype.undo = function(sketch) {
        var oldContainingObject = !(this.newContainerId) ? sketch : sketch.getSubObjectByIdChain(this.newContainerId.getIdChain());
        var newContainingObject = !(this.oldContainerId) ? sketch : sketch.getSubObjectByIdChain(this.oldContainerId.getIdChain());

        if (!!this.newContainerId) {
            console.log('NEW CONTAINER ID ID ID ID ', this.newContainerId.getIdChain());
        }

        if (oldContainingObject === newContainingObject) {
            // done moving to same place.
            return;
        }

        for (var shapeId in this.shapesToBeContained) {
            if (this.shapesToBeContained.hasOwnProperty(shapeId)) {
                var object = oldContainingObject.removeSubObjectById(shapeId);
                if (newContainerId) {
                    newContainingObject.addSubObject(object);
                } else {
                    newContainingObject.addObject(object);
                }
            }
        }
    };

})();
