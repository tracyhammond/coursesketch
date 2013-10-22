/**
 * Creates an SRL prtobuf version of a point
 */
SRL_Point.prototype.sendToProtobuf = function(scope) {
	var PointProto = scope ? scope.ProtoSrlPoint : ProtoSrlPoint;
	var proto = new PointProto();
	proto.id = this.getId();
	proto.setTime(Long.fromString('' + this.getTime()));
	proto.name = this.getName();
	proto.x = this.getX();
	proto.y = this.getY();
	proto.pressure = this.getPressure();
	proto.size = this.getSize();
	proto.speed = this.getSpeed();
	return proto;
}

/**
 * Creates an SRL protobuf version of a stroke
 */
SRL_Stroke.prototype.sendToProtobuf = function(scope) {
	var StrokeProto = scope ? scope.ProtoSrlStroke : ProtoSrlStroke;
	var proto = new StrokeProto();
	proto.id = this.getId();
	var n = this.getTime();
	proto.setTime(n);
	proto.name = this.getName();
	var array = new Array();
	var points = this.getPoints();
	for (var i=0; i<points.length; i++) {
		array.push(points[i].sendToProtobuf(scope));
	}
	console.error(array);
	proto.setPoints(array); // THIS FUNCTION SUCKS!
	return proto;
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
