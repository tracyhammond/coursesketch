/*
package edu.tamu.srl.object.shape.stroke;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import edu.tamu.srl.object.shape.SRL_Shape;
import edu.tamu.srl.object.shape.primitive.SRL_Point;
*/
/**
 * Stroke data class
 * @author hammond
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 */


/* Ignored the following functions:
	~ public abstract SRL_Object clone();
	~ protected SRL_Object clone(SRL_Object cloned)
	~ public void addInterpretation(String interpretation, double confidence, double complexity)
	public void setId(UUID id)
	public int compareTo(SRL_Object o)
	public SRL_Interpretation getBestInterpretation()
	public SRL_Interpretation getBestInterpretation()
	public SRL_Interpretation getInterpretation(String interpretation)
	public double getInterpretationConfidence(String interpretation)
	public double getInterpretationComplexity(String interpretation)
//*/

function SRL_Object() {
	addInterpretation = function(interpretation, confidence, complexity) {
		//m_interpretations.push(new SRL_Interpretation(interpretation, confidence, complexity));
		console.log("Implement SRL_Object.addInterpretation later");
	}
};

function SRL_Shape() {};

function SRL_Point(x, y, time) {
	/**
	 * Points can have pressure depending on the input device
	 */
	var m_pressure = 1;
	/**
	 * Holds an history list of the x points 
	 * Purpose is so that we can redo and undo and go back to the original points
	 */
	var m_xList = new Array();
	/**
	 * Holds a history list of the y points 
	 * Purpose is so that we can redo and undo and go back to the original points
	 */
	var m_yList = new Array();
	/**
	 * Holds a history list of the timestamps
	 * Purpose is so that we can redo and undo and go back to the original points
	 */
	var m_timeList = new Array();
	/**
	 * A counter that keeps track of where you are in the history of points
	 */
	var m_currentElement = -1;
	/**
	 * Points can have pressure depending on the input device
	 * @return the pressure of the point
	 */
	this.getPressure = function() {
		return m_pressure;
	}
	/**
	 * Points can have pressure depending on the input device
	 * @param pressure
	 */
	this.setPressure = function(pressure) {
		if (typeof pressure === "number") {
			m_pressure = pressure;
		} else {
			throw "argument of .setPressure must be a 'number'";
		}
	}
	/**
	 * Updates the location of the point
	 * Also add this point to the history of the points 
	 * so this can be undone.
	 * @param x the new x location for the point
	 * @param y the new y location for the point
	 */
	this.setP = function(x,y) {
		if (typeof x === "number" && typeof y === "number") {
			m_xList.push(x);
		    m_yList.push(y);
		    m_timeList.push(time);
		    m_currentElement = m_xList.length - 1;
		} else {
			throw "arguments of .setP must be 'number'";
		}
	}
	/**
	 * Creates a point with the initial points at x,y
	 * @param x the initial x point
	 * @param y the initial y point
	 */
	if (x != undefined && y != undefined) {
		this.setP(x,y, time);
		//addInterpretation("Point", 1, 1);
	} else {
		//do nothing;
	}
	/**
	 * Get the current x value of the point
	 * @return current x value of the point
	 */
	this.getX = function() {
		return m_xList[m_currentElement];
	}
	/**
	 * Get the current y value of the point
	 * @return current y value of the point
	 */
	this.getY = function() {
		return m_yList[m_currentElement];
	}
	/**
	 * Get the current time value of the point
	 * @return current time value of the point
	 */
	this.getTime = function() {
		return m_timeList[m_currentElement];
	}


	this.distance = function(arg1, arg2, arg3, arg4) {
		/**
	 	 * Return the distance from point rp to this point.
	 	 * @param rp the other point
	 	 * @return the distance
	 	 */
		if (arg1 instanceof SRL_Point){
	 		return this.distance(arg1.getX(), arg1.getY());

	 	/**
	 	 * Return the distance from the point specified by (x,y) to this point
	 	 * @param x the x value of the other point
	 	 * @param y the y value of the other point
	 	 * @return the distance
	 	 */
		} else if (typeof arg1 === "number" && 
					typeof arg2 === "number" && 
					arg3 === undefined && 
					arg4 === undefined) {
			var xdiff = Math.abs(arg1 - this.getX());
	    	var ydiff = Math.abs(arg2 - this.getY());
	    	return Math.sqrt(xdiff*xdiff + ydiff*ydiff);
		
		/**
	   	 * Return the distance from the point specified by (x,y) to this point
	   	 * @param x the x value of the other point
	   	 * @param y the y value of the other point
	   	 * @return the distance
	   	 */
		} else if (typeof arg1 === "number" && 
					typeof arg2 === "number" && 
					typeof arg3 === "number" && 
					typeof arg4 === "number") {
			var xdiff = arg1 - arg3;
			var ydiff = arg2 - arg4;
	    	return Math.sqrt(xdiff*xdiff + ydiff*ydiff);
		} else {
			throw "arguments of .distance are wrong";
		}
	}
	/**
	 * Delete the entire point history and 
	 * use these values as the starting point
	 * @param x new initial x location
	 * @param y new initial y location 
	 */
	this.setOrigP = function(x, y){
		if (typeof x === "number" && typeof y === "number") {
			m_xList = [];
	  		m_yList = [];
	  		this.setP(x, y);
		} else {
			throw "arguments of .setP must be 'number'";
		}
	}
	/**
	 * Remove last point update
	 * If there is only one x,y value in the history,
	 * then it does nothing
	 * Returns the updated shape (this)
	 */
	this.undoLastChange = function() {
		if (m_xList.length < 2) { return this; }
		if (m_yList.length < 2) { return this; }
		m_xList.pop();
		m_yList.pop();
		m_currentElement -= 1;
		return this;
	}
	/**
	 * Get the original value of the point
	 * @return a point where getx and gety return the first values that were added to the history
	 */
	this.goBackToInitial = function () {
		if(m_currentElement >= 0){
			m_currentElement = 0;
		}
		return this;
	}
	/**
	 * Get the x value for the first point in the history
	 * @return
	 */
	this.getInitialX = function() {
		if(m_xList.length === 0){
			return Number.NaN;
		}
		return m_xList[0];
	}
	/**
	 * Get the y value for the first point in the history
	 * @return
	 */
	this.getInitialY = function(){
		if(m_yList.length === 0){
			return Number.NaN;
		}
		return m_yList[0];
	}
	/**
	 * Get the y value for the first point in the history
	 * @return
	 */
	this.getInitialTime = function(){
		if(m_timeList.length === 0){
			return Number.NaN;
		}
		return m_timeList[0];
	}
	/**
	 * Return an object drawable by AWT
	 * return awt point
	 */
	this.getAWT = function(){
		// TODO - figure out how to link to AWT in javascript
		console.log("TODO - figure out how to link to AWT in javascript");
		//return new Point((int)getX(),(int)getY());
	}
	/**
	 * Just returns the x value with is obviously the same as the min
	 * return x value
	 */
	this.getMinX = function() {
		return this.getX();
	}
	/**
	 * Just returns the y value with is obviously the same as the min
	 * return y value
	 */
	this.getMinY = function() {
		return this.getY();
	}
	/**
	 * Just returns the time value with is obviously the same as the min
	 * return time value
	 */
	 this.getMinTime = function() {
	 	return this.getTime();
	 }
	/**
	 * Just returns the x value with is obviously the same as the max
	 * return x value
	 */
	this.getMaxX = function() {
		return this.getX();
	}
	/**
	 * Just returns the y value with is obviously the same as the max
	 * return y value
	 */
	this.getMaxY = function() {
		return this.getY();
	}
	/**
	 * Just returns the time value with is obviously the same as the max
	 * return time value
	 */
	 this.getMaxTime = function() {
	 	return this.getTime();
	}
	this.test_functions = function() {
		console.log("testing .getPressure");
		console.log(this.getPressure());

		console.log("testing .setPressure");
		console.log("setting pressure = 2");
		this.setPressure(2);
		console.log(this.getPressure());
		/* // throws an error
		console.log("setting pressure = \"string\"");
		test_point.setPressure("string");
		console.log(test_point.getPressure());
		//*/

		console.log("testing .setP");
		console.log("show status of arrays");
		this.temp_print();
		console.log("adding a point");
		this.setP(10,10);
		this.temp_print();
		console.log("adding a point");
		this.setP(2,5);
		this.temp_print();

	}
	this.temp_print = function(){
		console.log("printing m_xList");
		console.log(m_xList);
		console.log("printing m_yList");
		console.log(m_yList);
		console.log("printing m_currentElement");
		console.log(m_currentElement);
	}
};

function SRL_Stroke(startPoint) {
	/**
	 * List of points in the stroke
	 */
	var m_points = new Array();
	/**
	 * Constructor setting the initial point in the stroke
	 * @param startPoint
	 */
	if (startPoint instanceof SRL_Point) {
		m_points.push(startPoint);
		//addInterpretation("Stroke", 1, 1);

	/**
	 * Constructor creating an empty stroke
	 */
	} else {
		//addInterpretation("Stroke", 1, 1);
	}
	/**
	 * Adding another point to the stroke
	 * @param point
	 */
	this.addPoint = function(point){
		if (point instanceof SRL_Point) {
			m_points.push(point);
		}
	}
	/**
	 * Gets the complete list of points in the stroke
	 * @return list of points in the stroke
	 */
	this.getPoints = function(){
		return m_points;
	}
	/**
	 * Get the i'th point in the stroke 
	 * The first point has index i = 0
	 * @param i the index of the stroke
	 * @return the point at index i
	 */
	this.getPoint = function(i){
		if (typeof i === "number") {
			if(i >= m_points.length){
				return null;
			}
			return m_points[i];
		}
	}
	/**
	 * Gets the number of points in the stroke
	 * @return number of points in the stroke
	 */
	this.getNumPoints = function(){
		return m_points.length;
	}
	/**
	 * Returns the first point in the stroke.
	 * if the stroke has no points, it returns null.
	 * @return first point in the stroke
	 */
	this.getFirstPoint = function(){
		if (m_points.length == 0){
			return null;
		}
		return m_points[0];
	}
	/**
	 * Returns the last point in the stroke
	 * If the stroke has no points, it returns null.
	 * @return last point in the stroke.
	 */
	this.getLastPoint = function(){
		if (m_points.length == 0){
			return null;
		}
		return m_points[m_points.length-1];
	}
	/**
	 * Returns a Graphics2Ddrawable awt object
	 * return a GeneralPath objects that contains the list of points.
	 */
	this.getAWT = function(){
		// "TODO - figure out how to link to AWT in javascript"
		console.log("TODO - figure out how to link to AWT in javascript");
		/*
		GeneralPath path = new GeneralPath();
		for(SRL_Point p : m_points){
			path.lineTo(p.getX(), p.getY());
		}
		return path;
		//*/
	}
	/** returns the minimum x value in a stroke
	 * return minimum x value in a stroke
	 */
	this.getMinX = function() {
		var minx = this.getFirstPoint().getX();
		for(var i=0; i<m_points.length; i++){
			if(m_points[i].getX() < minx){
				minx = m_points[i].getX();
			}
		}
		return minx;
	}
	/** returns the minimum y value in a stroke
	 * return minimum y value in a stroke
	 */
	this.getMinY = function() {
		var miny = this.getFirstPoint().getY();
		for(var i=0; i<m_points.length; i++){
			if(m_points[i].getY() < miny){
				miny = m_points[i].getY();
			}
		}
		return miny;
	}
	/** returns the maximum x value in a stroke
	 * return maximum x value in a stroke
	 */
	this.getMaxX = function() {
		var maxx = this.getFirstPoint().getX();
		for(var i=0; i<m_points.length; i++){
			if(m_points[i].getX() > maxx){
				maxx = m_points[i].getX();
			}
		}
		return maxx;
	}
	/** returns the maximum x value in a stroke
	 * return maximum x value in a stroke
	 */
	this.getMaxY = function() {
		var maxy = this.getFirstPoint().getY();
		for(var i=0; i<m_points.length; i++){
			if(m_points[i].getY() > maxy){
				maxy = m_points[i].getY();
			}
		}
		return maxy;
	}
	/**
	 * Return the cosine of the starting angle of the stroke
	 * This takes the angle between the initial point and the point specified as the secondPoint
	 * If there are fewer than that many points, it uses the last point.
	 * If there are only 0 or 1 points, this returns NaN.
	 * Note that this is also feature 1 of Rubine.
	 * @param secondPoint which number point should be used for the second point
	 * @return cosine of the starting angle of the stroke
	 */
	this.getStartAngleCosine = function (secondPoint) {
		if (typeof secondPoint === "number") {
			if(this.getNumPoints() <= 1) return Number.NaN;
			if(this.getNumPoints() <= secondPoint){
				secondPoint = this.getNumPoints() - 1;
			}
			
			var xStart, xEnd, yStart, yEnd;		
			xStart = this.getPoint(0).getX();
			yStart = this.getPoint(0).getY();
			xEnd = this.getPoint(secondPoint).getX();
			yEnd = this.getPoint(secondPoint).getY();
			
			if(xStart == xEnd && yStart == yEnd){
				return Number.NaN;
			}
				
			var sectionWidth = xEnd - xStart;
			var sectionHeight = yEnd - yStart;
			var hypotenuse = Math.sqrt(sectionWidth * sectionWidth + sectionHeight * sectionHeight);
			return sectionWidth / hypotenuse;
		} else {
			throw ".getStartAngleCosine needs an int argument";
		}
	}
	/**
	 * Return the sine of the starting angle of the stroke
	 * This takes the angle between the initial point and the point specified as the secondPoint
	 * If there are fewer than that many points, it uses the last point.
	 * If there are only 0 or 1 points, this returns NaN.
	 * Note that this is also feature 1 of Rubine.
	 * @param secondPoint which number point should be used for the second point
	 * @return cosine of the starting angle of the stroke
	 */
	this.getStartAngleSine = function(secondPoint) {
		if (typeof secondPoint === "number") {
			if(this.getNumPoints() <= 1) return Number.NaN;
			if(this.getNumPoints() <= secondPoint){
				secondPoint = this.getNumPoints() - 1;
			}
			
			var xStart, xEnd, yStart, yEnd;		
			xStart = this.getPoint(0).getX();
			yStart = this.getPoint(0).getY();
			xEnd = this.getPoint(secondPoint).getX();
			yEnd = this.getPoint(secondPoint).getY();
			
			if(xStart == xEnd && yStart == yEnd){
				return Number.NaN;
			}
				
			var sectionWidth = xEnd - xStart;
			var sectionHeight = yEnd - yStart;
			var hypotenuse = Math.sqrt(sectionWidth * sectionWidth + sectionHeight * sectionHeight);
			return sectionHeight / hypotenuse;
		}
	}
	/**
	 * Return the Euclidean distance from the starting point 
	 * to the ending point of the stroke
	 * @return the distance between the starting and ending points of the stroke
	 */
	this.getEuclideanDistance = function() {
		var x0, xn, y0, yn;
		if (this.getPoints().length == 0)
			return 0;
		x0 = this.getFirstPoint().getX();
		y0 = this.getFirstPoint().getY();
		xn = this.getLastPoint().getX();
		yn = this.getLastPoint().getY();
		return Math.sqrt(Math.pow(xn-x0,2)+Math.pow(yn-y0,2));
	}
	/**
	 * Return the cosine of the angle between the start and end point
	 * @return cosine of the ending angle
	 */
	this.getEndAngleCosine = function() {
		if(this.getNumPoints() <= 1) return Number.NaN;
		if (this.getEuclideanDistance()==0)
			return Number.NaN;
		var xDistance = this.getLastPoint().getX() - this.getFirstPoint().getX();
		return xDistance/this.getEuclideanDistance();
	}
	/**
	 * Return the cosine of the angle between the start and end point
	 * @return cosine of the ending angle
	 */
	this.getEndAngleSine = function() {
		if(this.getNumPoints() <= 1) return Number.NaN;
		if (this.getEuclideanDistance()==0)
			return Number.NaN;
		var yDistance = this.getLastPoint().getY() - this.getFirstPoint().getY();
		return yDistance/this.getEuclideanDistance();
	}
	/**
	 * Returns the length of the stroke, 
	 * complete with all of its turns
	 * @return length of the stroke
	 */
	this.getStrokeLength = function() {
		var sum = 0;
		var deltaX, deltaY;
		for (var i=0; i < this.getPoints().length-1; i++) {
			deltaX = this.getPoint(i+1).getX()-this.getPoint(i).getX();
			deltaY = this.getPoint(i+1).getY()-this.getPoint(i).getY();
			sum += Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2));
		}
		return sum;
	}
	/**
	 * Return the total stroke time
	 * @return total time of the stroke
	 */
	this.getTotalTime = function() {
		console.log("TODO - need to implement a .getTime()");
		if (this.getPoints().length == 0)
			return Number.NaN;
		//return this.getLastPoint().getTime()-this.getFirstPoint().getTime();
	}
	/**
	 * Auxiliary method used to return a list containing all points
	 * but with duplicated (based on time) removed
	 * @return list of points with duplicates (based on time) removed
	 */
	this.removeTimeDuplicates = function() {
		console.log("TODO - need to implement a .getTime()");
		var points = new Array();
		for(var i=0; i<m_points.length; i++){
			if(points.length > 0){
				/*
				if(points[points.size()-1].getTime() == p.getTime()){
					continue;
				}
				//*/
			}
			points.push(m_points[i]);
		}
		return points;
	}
	/**
	 * Auxiliary method used to return a list containing all points
	 * but with duplicated (based on X,Y coordinates) removed
	 * @return list of points with duplicates (based on coordinates) removed
	 */
	this.removeCoordinateDuplicates = function() {
		var p = new Array();
		var x1, x2, y1, y2;
		p.push(this.getPoint(0));
		for (var i = 0; i < this.getPoints().length-1; i++) {
			x1 = this.getPoint(i).getX();
			y1 = this.getPoint(i).getY();
			x2 = this.getPoint(i+1).getX();
			y2 = this.getPoint(i+1).getY();
			if (x1 != x2 || y1 != y2)
				p.push(this.getPoint(i+1));
		}
		return p;
	}
	/**
	 * Return the maximum stroke speed reached
	 * @return maximum stroke speed reached
	 */
	this.getMaximumSpeed = function() {
		console.log("TODO - need to implement a .getTime()");
		if (this.getPoints().length == 0)
			return Number.NaN;
		var max = 0;
		var deltaX, deltaY;
		var deltaT;
		var p = this.removeTimeDuplicates();
		for (var i = 0; i < p.length-1; i++) {
			deltaX = p[i+1].getX()-p[i].getX();
			deltaY = p[i+1].getY()-p[i].getY();
			//deltaT = p.get(i+1).getTime()-p.get(i).getTime();
			var speed = (Math.pow(deltaX,2)+Math.pow(deltaY,2))/Math.pow(deltaT,2);
			if (speed > max)
				max = speed;
		}
		return max;
	}
	/**
	 * Calculates the rotation from point startP to two points further.
	 * Calculates the line between startP and the next point,
	 * and then the next two points,
	 * and then returns the angle between the two.
	 * @param points
	 * @param startP
	 * @return
	 */
	this.rotationAtAPoint = function(points, startP){
		if (points[0] instanceof SRL_Point && typeof startP === "number") {
			if(points.length < startP+2){
				return Number.NaN;
			}
			var mx = points.get[startP+1].getX()-points.get[startP].getX();
			var my = points.get[startP+1].getY()-points.get[startP].getY();
		    return Math.atan2(my, mx);
		}
	}
	/**
	 * Return the total rotation of the stroke from start to end points
	 * @return total rotation of the stroke
	 */
	this.getRotationSum = function() {
		var p = this.removeCoordinateDuplicates();
		var sum = 0;
		var lastrot = Number.NaN;
		for (var i = 1; i < p.length-2; i++) {
			var rot = this.rotationAtAPoint(p, i);
			if(lastrot == Number.NaN) lastrot = rot;
			while((i > 0) && (rot - lastrot > Math.PI)){
				rot = rot - 2*Math.PI;
			}
		    while((i > 0) && (lastrot - rot > Math.PI)){
		    	rot = rot + 2*Math.PI;
			}  
		    sum += rot;
		}
		return sum;
	}
	/**
	 * Return the absolute rotation of the stroke from start to end points
	 * @return total absolute rotation of the stroke
	 */
	this.getRotationAbsolute = function() {
		var p = this.removeCoordinateDuplicates();
		var sum = 0;
		var lastrot = Number.NaN;
		for (var i = 1; i < p.length-2; i++) {
			var rot = this.rotationAtAPoint(p, i);
			if(lastrot == Number.NaN) lastrot = rot;
			while((i > 0) && (rot - lastrot > Math.PI)){
				rot = rot - 2*Math.PI;
			}
		    while((i > 0) && (lastrot - rot > Math.PI)){
		    	rot = rot + 2*Math.PI;
			}  
		    sum += Math.abs(rot);
		}
		return sum;
	}
	/**
	 * Return the squared rotation of the stroke from start to end points
	 * @return total squared rotation of the stroke
	 */
	this.getRotationSquared = function() {
		var p = this.removeCoordinateDuplicates();
		var sum = 0;
		var lastrot = Number.NaN;
		for (var i = 1; i < p.length-2; i++) {
			var rot = this.rotationAtAPoint(p, i);
			if(lastrot == Number.NaN) lastrot = rot;
			while((i > 0) && (rot - lastrot > Math.PI)){
				rot = rot - 2*Math.PI;
			}
		    while((i > 0) && (lastrot - rot > Math.PI)){
		    	rot = rot + 2*Math.PI;
			}  
		    sum += rot * rot;
		}
		return sum;
	}




	this.temp_print = function() {
		for (var i=0; i<m_points.length; i++) {
			m_points[i].temp_print();
		}
	}

};

/*
//Test Functions for SRL_Stroke()
SRL_Shape.prototype = new SRL_Object();
SRL_Stroke.prototype = new SRL_Shape();
SRL_Point.prototype = new SRL_Shape();

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
*/