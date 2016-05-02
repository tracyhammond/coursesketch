/* jshint camelcase: false */

(function(localScope) {
    /**
     * Creates an SRL prtobuf version of a point.
     *
     * @memberof SRL_Point
     */
    SRL_Point.prototype.sendToProtobuf = function() {
        var proto = CourseSketch.prutil.ProtoSrlPoint();
        proto.id = this.getId();
        var n = this.getTime();
        proto.setTime('' + n);
        proto.name = this.getName();
        proto.x = this.getX();
        proto.y = this.getY();
        proto.pressure = this.getPressure();
        proto.size = this.getSize();
        proto.speed = this.getSpeed();
        return proto;
    };

    /**
     * Static function that returns an {@link SRL_Point}.
     *
     * @param {SrlPoint} proto - The proto object that is being turned into a sketch object.
     * @memberof SRL_Point
     */
    SRL_Point.createFromProtobuf = function(proto) {
        var point = new SRL_Point(proto.x, proto.y);
        point.setId(proto.id);

        if (proto.time) {
            point.setTime(parseInt(proto.time.toString(), 10));
        }
        if (proto.name) {
            point.setName(proto.name);
        }
        if (proto.size) {
            point.setSize(proto.size);
        }
        if (proto.pressure) {
            point.setPressure(proto.pressure);
        }
        if (proto.speed) {
            point.setSpeed(proto.speed);
        }
        return point;
    };

    /**
     * Creates an SRL protobuf version of a stroke.
     *
     * @memberof SRL_Stroke
     */
    SRL_Stroke.prototype.sendToProtobuf = function() {
        var proto = CourseSketch.prutil.ProtoSrlStroke();
        proto.id = this.getId();
        var n = this.getTime();
        proto.setTime('' + n);
        proto.name = this.getName();
        var array = [];
        var points = this.getPoints();
        for (var i = 0; i < points.length; i++) {
            array.push(points[i].sendToProtobuf());
        }
        proto.setPoints(array); // THIS FUNCTION SUCKS!
        return proto;
    };

    /**
     * Static function that returns an {@link SRL_Stroke}.
     *
     * @param {SrlStroke} stroke - The proto object that is being turned into a sketch object.
     * @memberof SRL_Stroke
     */
    SRL_Stroke.createFromProtobuf = function(stroke) {
        var pointList = stroke.getPoints();
        var srlStroke = new SRL_Stroke();
        for (var i in pointList) {
            if (pointList.hasOwnProperty(i)) {
                var point = pointList[i];
                var currentPoint = SRL_Point.createFromProtobuf(point);
                srlStroke.addPoint(currentPoint);
            }
        }
        if (!srlStroke) {
            srlStroke = new SRL_Stroke();
        }
        srlStroke.finish();
        srlStroke.setId(stroke.getId());
        return srlStroke;
    };

    /**
     * Creates an SRL protobuf version of a shape.
     *
     * @memberof SRL_Shape
     */
    SRL_Shape.prototype.sendToProtobuf = function() {
        var proto = CourseSketch.prutil.ProtoSrlShape();

        var interpretations = this.getInterpretations();
        var protoInterp = [];
        for (var i = 0; i < interpretations.length; i++) {
            var protoInter = interpretations[i];
            protoInterp = protoInter.sendToProtobuf();
        }
        proto.setInterpretations(protoInterp);

        var protoSubShapes = [];
        var subShapeList = this.getSubObjects();
        for (i = 0; i < subShapeList.length; i++) {
            protoSubShapes.push(encodeSrlObject(subShapeList[i]));
        }
        proto.setSubComponents(protoSubShapes);

        proto.setId(this.getId());
        var n = this.getTime();
        proto.setTime('' + n);
        proto.setName = this.getName();
        return proto;
    };

    /**
     * Static function that returns an {@link SRL_Shape}.
     *
     * @param {ProtoSrlShape} shape - The proto object that is being turned into a sketch object.
     * @memberof SRL_Shape
     */
    SRL_Shape.createFromProtobuf = function(shape) {
        var interpretations = shape.interpretations;
        var subObjects = shape.subComponents;
        var newShape = new SRL_Shape();
        for (var i = 0; i < interpretations.length; i++) {
            var protoInter = interpretations[i];
            newShape.addInterpretation(protoInter.label, protoInter.confidence, protoInter.complexity);
        }

        for (i = 0; i < subObjects.length; i++) {
            var protoObject = subObjects[i];
            newShape.addSubObject(decodeSrlObject(protoObject));
        }
        newShape.setId(shape.getId());
        newShape.setName(shape.getName());

        return newShape;
    };

    /**
     * Creates an SRL protobuf version of an Interpretation.
     *
     * @memberof SRL_Interpretation
     */
    SRL_Interpretation.prototype.sendToProtobuf = function() {
        var proto = CourseSketch.prutil.ProtoSrlInterpretation();
        proto.label = this.label;
        proto.confidence = this.confidence;
        proto.complexity = this.complexity;
        return proto;
    };

    SRL_Sketch.prototype.sendToProtobuf = function() {
        var protoSketch = CourseSketch.prutil.ProtoSrlSketch();

        var subObjects = this.getList();
        var protoSubObjects = [];

        for (var i = 0; i < subObjects.length; i++) {
            protoSubObjects.push(encodeSrlObject(subObjects[i]));
        }
        protoSketch.sketch = protoSubObjects;
        return protoSketch;
    };

    /**
     * Used locally to decode the srl object.
     *
     * @param {SRL_Object} object - the object that is being turned into its proto type.
     * @returns {ProtoSrlObject} SRL_Object or its subclass.
     */
    function decodeSrlObject(object) {
        var proto = CourseSketch.prutil.getProtoSrlObjectClass();
        var objectType = object.type; // FIXME: change this to objectType
        switch (objectType) {
            case proto.ObjectType.SHAPE:
                return SRL_Shape.createFromProtobuf(CourseSketch.prutil.decodeProtobuf(object.object, 'ProtoSrlShape'));
            case proto.ObjectType.STROKE:
                return SRL_Stroke.createFromProtobuf(CourseSketch.prutil.decodeProtobuf(object.object, 'ProtoSrlStroke'));
            case proto.ObjectType.POINT:
                return SRL_Point.createFromProtobuf(CourseSketch.prutil.decodeProtobuf(object.object, 'ProtoSrlPoint'));
        }
    }

    /**
     * Used locally to encode an SRL_Object into its protobuf type.
     *
     * @param {SRL_Object} object - the object that is being turned into its proto type.
     * @return {ProtoSrlObject} The protobuf form of an SRL_Object.
     */
    function encodeSrlObject(object) {
        var proto = CourseSketch.prutil.ProtoSrlObject();
        var SrlObject = CourseSketch.prutil.getProtoSrlObjectClass();

        if (object.check_type() === SRL_ShapeType) {
            proto.type = SrlObject.ObjectType.SHAPE;
        } else if (object.check_type() === SRL_StrokeType) {
            proto.type = SrlObject.ObjectType.STROKE;
        } else if (object.check_type() === SRL_PointType) {
            proto.type = SrlObject.ObjectType.POINT;
        }

        proto.object = object.sendToProtobuf().toArrayBuffer();
        return proto;
    }

})(this);
