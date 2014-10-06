 
//************************************************************************
//
//
//Test Functions for SRL_Stroke()
//
//
//************************************************************************
///*
console.log("****************SRL_Stroke()****************");
var test_stroke = new SRL_Stroke();
test_stroke.temp_print();

console.log("****************SRL_Stroke() Points: add, get, get first, get last****************");
var test_point = new SRL_Point(-2,-2);
test_stroke.addPoint(test_point);
var test_point2 = new SRL_Point(2,2);
test_stroke.addPoint(test_point2);
test_stroke.temp_print();


test_stroke.getPoints();
test_stroke.getPoint(0).temp_print();
console.log(test_stroke.getNumPoints());
test_stroke.getFirstPoint().temp_print();
test_stroke.getLastPoint().temp_print();

console.log("****************SRL_Stroke().getMin/MaxX/Y()****************");
console.log(test_stroke.getMinX());
console.log(test_stroke.getMinY());
console.log(test_stroke.getMaxX());
console.log(test_stroke.getMaxY());

console.log("****************SRL_Stroke()this.getStartAngleCosine/Sine()****************");
console.log(test_stroke.getStartAngleCosine(1));
console.log(test_stroke.getStartAngleSine(1));

console.log("****************SRL_Stroke()this.getEndAngleCosine/Sine()****************");
console.log(test_stroke.getEndAngleCosine());
console.log(test_stroke.getEndAngleSine());

console.log("****************SRL_Stroke()this.getStrokeLength()****************");
console.log(test_stroke.getStrokeLength());

console.log("****************SRL_Stroke()this Time functions****************");
console.log(test_stroke.getTotalTime());
console.log(test_stroke.getMaximumSpeed());

console.log("****************SRL_Stroke()this Rotation functions****************");
console.log(test_stroke.getRotationSum());
console.log(test_stroke.getRotationAbsolute());
console.log(test_stroke.getRotationSquared());

//************************************************************************
//
//
//Test Functions for SRL_Point()
//
//
//************************************************************************

console.log("****************SRL_Point()****************");
var test_point = new SRL_Point();
test_point.test_functions();

console.log("****************SRL_Point(50,50)****************");
var test_point2 = new SRL_Point(40,50);
test_point2.test_functions();
console.log("setting a point");
test_point2.setP(40,30);
test_point2.temp_print();

console.log("****************SRL_Point.distance()****************");
console.log(test_point2.distance(test_point));
console.log(test_point.distance(test_point2));
console.log(test_point2.distance(2,5));
console.log(test_point.distance(40,30));
console.log(test_point2.distance(2,5,40,30));
console.log(test_point2.distance(40,30,2,5));

console.log("****************SRL_Point.setOrigP()****************");
test_point.setOrigP(25,20);
test_point2.setOrigP(75,70);
test_point.temp_print();
test_point2.temp_print();

console.log("****************SRL_Point.undoLastChange()****************");
console.log("setting a point");
test_point2.setP(40,30);
test_point2.temp_print();
test_point2.undoLastChange();
test_point2.temp_print();

console.log("****************SRL_Point.goBackToInitial()****************");
console.log("setting a point");
test_point2.setP(40,30);
test_point2.temp_print();
console.log("go back to initial");
console.log(test_point2.goBackToInitial().getX());
console.log(test_point2.goBackToInitial().getY());

console.log("****************SRL_Point.getInitialX & Y()****************");
test_point.temp_print();
console.log(test_point.getInitialX());
console.log(test_point.getInitialY());
test_point2.temp_print();
console.log(test_point2.getInitialX());
console.log(test_point2.getInitialY());

console.log("****************SRL_Point.get min and max****************");

//************************************************************************
//
//
//Test Functions for SRL_Line()
//
//
//************************************************************************

console.log("****************SRL_Line()****************");
var test_line = new SRL_Line();
test_line.temp_print();

console.log("****************SRL_Line().setP1 & 2****************");
var test_point = new SRL_Point(-2,-2);
test_line.setP1(test_point);
var test_point2 = new SRL_Point(2,2);
test_line.setP2(test_point2);
test_line.temp_print();

var test_point3 = new SRL_Point(0,4);
var test_point4 = new SRL_Point(4,0);

console.log("****************SRL_Line(p1, p2)****************");
var test_line2 = new SRL_Line(test_point3, test_point4); // 40,30 -> 2,5
test_line2.temp_print();

console.log("****************SRL_Line(x1,y1,x2,y2)****************");
var test_line3 = new SRL_Line(4,-2,0,4);
test_line3.temp_print();

console.log("****************SRL_Line().getSlope()****************");
console.log(test_line2.getSlope());

console.log("****************SRL_Line().getSlope(x1, y1, x2, y2)****************");
console.log(test_line2.getSlope(3,5,70,10));

console.log("****************SRL_Line().getYIntercept()****************");
console.log(test_line2.getYIntercept());

console.log("****************SRL_Line().getYIntercept(x1,y1,x2,y2)****************");
console.log(test_line2.getYIntercept(3,5,70,10));

console.log("****************SRL_Line().getABCArray(x1,y1,x2,y2)****************");
console.log(test_line2.getABCArray(0,4,4,0));
console.log(test_line2.getABCArray(-2,-2,2,2));

console.log("****************SRL_Line().getIntersection()****************");
test_line2.getIntersection(test_line).temp_print();

console.log("****************SRL_Line().getLength() & .getArea()****************");
console.log(test_line2.getLength());
console.log(test_line2.getArea());

console.log("****************SRL_Line().getFlippedLine & .flip()****************");
test_line2.getFlippedLine().temp_print();
test_line2.flip();
test_line2.temp_print();

console.log("****************SRL_Line().getMins & .getMaxs()****************");
console.log(test_line2.getMinX());
console.log(test_line2.getMinY());
console.log(test_line2.getMaxX());
console.log(test_line2.getMaxY());

console.log("****************SRL_Line().getPerpendicularLine()****************");
test_line2.getPerpendicularLine(test_point2).temp_print();

console.log("****************SRL_Line().isParallel()****************");
test_line2.isParallel(test_line2); // doesn't seem to work; double check with Hammond that the algorithm works

console.log("****************SRL_Line().distance()****************");
console.log(test_line2.distance(test_point));
console.log(test_line2.distance(0,0,0,4,4,0));
console.log(test_line2.distance(test_line));
console.log(test_line2.distance(-2,-2,2,2,0,4,4,0));

//*/