(function(localScope) {
	/**
	 * Creates an SRL prtobuf version of a point
	 */
	SRL_Point.prototype.sendToProtobuf = function(scope) {
		var PointProto = scope ? scope.ProtoSrlPoint : ProtoSrlPoint;
		var proto = new PointProto();
		proto.id = this.getId();
		var n = this.getTime();
		var longVersion = scope.Long.fromString("" + n);
		proto.setTime(longVersion);
		proto.name = this.getName();
		proto.x = this.getX();
		proto.y = this.getY();
		proto.pressure = this.getPressure();
		proto.size = this.getSize();
		proto.speed = this.getSpeed();
		return proto;
	}

	/**
	 * Static function that returns an {@link SRL_Point}.
	 */
	SRL_Point.createFromProtobuf = function(proto) {
		var point = new SRL_Point(proto.x, proto.y);
		point.setId(proto.id);
		if (proto.time)
			point.setTime(proto.time);
		if (proto.name)
			point.setName(proto.name);
		if (proto.size)
			point.setSize(proto.size);
		if (proto.pressure)
			point.setPressure(proto.pressure);
		if (proto.speed)
			point.setSpeed(proto.speed);
		return point;
	}

	/**
	 * Creates an SRL protobuf version of a stroke
	 */
	SRL_Stroke.prototype.sendToProtobuf = function(scope) {
		var StrokeProto = scope ? scope.ProtoSrlStroke : ProtoSrlStroke;
		var long = scope.Long || Long;
		var proto = new StrokeProto();
		proto.id = this.getId();
		var n = this.getTime();
		var longVersion = long.fromString("" + n);
		proto.setTime(longVersion);
		proto.name = this.getName();
		var array = new Array();
		var points = this.getPoints();
		for (var i=0; i<points.length; i++) {
			array.push(points[i].sendToProtobuf(scope));
		}
		proto.setPoints(array); // THIS FUNCTION SUCKS!
		return proto;
	}

	/**
	 * Static function that returns an {@link SRL_Stroke}.
	 */
	SRL_Stroke.createFromProtobuf = function(stroke) {
		var pointList = stroke.getPoints();
		var srlStroke = new SRL_Stroke();
		for(i in pointList) {
			var point = pointList[i];
			var currentPoint = SRL_Point.createFromProtobuf(point);
			srlStroke.addPoint(currentPoint);
		}
		if (!srlStroke) {
			srlStroke = new SRL_Stroke();
		}
		srlStroke.finish();
		srlStroke.setId(stroke.getId());
		return srlStroke;
	}

	/**
	 * Creates an SRL protobuf version of a shape.
	 */
	SRL_Shape.prototype.sendToProtobuf = function(scope) {
		var ShapeProto = scope ? scope.ProtoSrlShape : ProtoSrlShape;
		var proto = new ShapeProto();

		var interpretations = this.getInterpretations();
		var protoInterp = new Array();
		for(var i = 0; i < interpretations.length; i ++) {
			var protoInter = interpretations[i];
			protoInterp = i.sendToProtobuf(scope);
		}
		proto.setInterpretations(protoInterp);
		
		var protoSubShapes = new Array();
		var subShapeList = this.getSubObjects();
		for (var i = 0; i < subShapeList.length; i++) {
			protoSubShapes.push(encodeSrlObject(scope,subShapeList[i]));
		}
		proto.setSubComponents(protoSubShapes);

		proto.id = this.getId();
		var n = this.getTime();
		var longVersion = scope.Long.fromString("" + n);
		proto.setTime(longVersion);
		proto.name = this.getName();
		return proto;
	}

	/**
	 * Static function that returns an {@link SRL_Shape}.
	 */
	SRL_Shape.createFromProtobuf = function(shape) {
		var interpretations = shape.interpretations;
		var subObjects = shape.subComponents;
		var newShape = new SRL_Shape();
		for(var i = 0; i < interpretations.length; i ++) {
			var protoInter = interpretations[i];
			newShape.addInterpretation(protoInter.label, protoInter.confidence, protoInter.complexity);
		}

		for(var i = 0; i < subObjects.length; i ++) {
			var protoObject = subObjects[i];
			newShape.addSubObject(decodeSrlObject(protoObject));
		}

		return newShape;
	}

	/**
	 * Creates an SRL protobuf version of an Interpretation.
	 */
	SRL_Interpretation.prototype.sendToProtobuf = function(scope) {
		var Interpretation = scope ? scope.ProtoSrlInterpretation : ProtoSrlInterpretation;
		var proto = new Interpretation();
		proto.label = this.label;
		proto.confidence = this.confidence;
		proto.complexity = this.complexity;
		return proto;
	}

	function decodeSrlObject(object) {
		var proto = false;
		var scope = false;
		if (!isUndefined(ProtoSrlObject)) {
			proto = ProtoSrlObject;
			scope = localScope;
		} else {
			proto = parent.ProtoSrlObject;
			scope = parent;
		}

		var objectType = object.type; // FIXME: change this to objectType
		switch(objectType) {
			case proto.ObjectType.SHAPE:
				return SRL_Shape.createFromProtobuf(scope.ProtoSrlShape.decode(object.object));
			break;
				case proto.ObjectType.STROKE:
				return SRL_Stroke.createFromProtobuf(scope.ProtoSrlStroke.decode(object.object));
			break;
				case proto.ObjectType.POINT:
				return SRL_Point.createFromProtobuf(scope.ProtoSrlPoint.decode(object.object));
			break;
		}
	}

	function encodeSrlObject(scope, object) {
		var SrlObject = scope ? scope.ProtoSrlObject : ProtoSrlObject;
		var proto = new SrlObject();

		if (object.check_type() == SRL_ShapeType) {
			proto.type = SrlObject.ObjectType.SHAPE;
		} else if (object.check_type() == SRL_StrokeType) {
			proto.type = SrlObject.ObjectType.STROKE;
		} else if (object.check_type() == SRL_PointType) {
			proto.type = SrlObject.ObjectType.POINT;
		}

		proto.object = object.sendToProtobuf(scope).toArrayBuffer();
		return proto;
	}

})(this);