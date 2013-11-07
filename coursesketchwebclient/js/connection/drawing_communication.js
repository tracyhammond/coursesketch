/**
 * Creates an SRL prtobuf version of a point
 */
SRL_Point.prototype.sendToProtobuf = function(scope) {
	var PointProto = scope ? scope.ProtoSrlPoint : ProtoSrlPoint;
	var proto = new PointProto();
	proto.id = this.getId();
	var n = this.getTime();
	var longVersion = parent.Long.fromString("" + n);
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
	var proto = new StrokeProto();
	proto.id = this.getId();
	var n = this.getTime();
	var longVersion = parent.Long.fromString("" + n);
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
	// LIST
	var srlStroke = false;
	//alert("listsize " + pointList.length);
	for(i in pointList) {
		var point = pointList[i];
		var currentPoint = SRL_Point.createFromProtobuf(point);
		if (!srlStroke) {
			srlStroke = new SRL_Stroke(currentPoint);
		} else {
			srlStroke.addPoint(currentPoint);
		}
	}
	if (!srlStroke) {
		srlStroke = new SRL_Stroke();
	}
	srlStroke.setId(stroke.getId());
	return srlStroke;
}

/**
 * Creates an SRL protobuf version of a shape.
 *
 * TODO: finish this method
 */
SRL_Shape.prototype.sendToProtobuf = function(scope) {
	var StrokeProto = scope ? scope.ProtoSrlShape : ProtoSrlShape;
	var interpretations = shape.getInterpretations();
	var newShape = new SRL_Shape();
	for(i in interpretations) {
		var protoInter = interpretations[i];
		newShape.addInterpretation(protoInter.name, protoInter.confidence, protoInter.complexity);
	}
	return newShape;
}

/**
 * Static function that returns an {@link SRL_Shape}.
 */
SRL_Shape.createFromProtobuf = function(shape) {
	var interpretations = shape.interpretations;
	var newShape = new SRL_Shape();
	for(i in interpretations) {
		var protoInter = interpretations[i];
		newShape.addInterpretation(protoInter.name, protoInter.confidence, protoInter.complexity);
	}
	return newShape;
}

/*
var testProtobuf = function(scope) {
	var OuterProto = scope ? scope.Outer : Outer;
	var InnerProto = scope ? scope.Inner : Inner;
	console.log("WORKING")
    var inners = new Array();

    // Array of repeated messages
    inners.push(new InnerProto("a"), new InnerProto("b"), new InnerProto("c"));
    var outer = new OuterProto();
    outer.setInners(inners);

    // Array of repeated message objects
    inners = new Array();
    inners.push({ str: 'a' }, { str: 'b' }, { str: 'c' });
    console.log("WORKING");
    outer.setInners(inners); // Converts
    console.log("FINISHED WORKING");
}
*/
