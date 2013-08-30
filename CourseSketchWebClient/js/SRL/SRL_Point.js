/*
package edu.tamu.srl.object.shape.primitive;

import java.awt.Point;
import java.util.ArrayList;

import edu.tamu.srl.object.shape.SRL_Shape;
*/
/**
 * Point data class
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
	this.setP = function(x,y,time) {
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
			//console.log("run with 1 param");
	 		return this.distance(arg1.getX(), arg1.getY());

	 	/**
	 	 * Return the distance from the point specified by (x,y) to this point
	 	 * @param x the x value of the other point
	 	 * @param y the y value of the other point
	 	 * @return the distance
	 	 */
		} else if (typeof arg1 === "number" && typeof arg2 === "number" && arg3 === undefined && arg4 === undefined) {
			//console.log("run with 2 params");
			var xdiff = Math.abs(arg1 - this.getX());
	    	var ydiff = Math.abs(arg2 - this.getY());
	    	return Math.sqrt(xdiff*xdiff + ydiff*ydiff);
		
		/**
	   	 * Return the distance from the point specified by (x,y) to this point
	   	 * @param x the x value of the other point
	   	 * @param y the y value of the other point
	   	 * @return the distance
	   	 */
		} else if (typeof arg1 === "number" && typeof arg2 === "number" && typeof arg3 === "number" && typeof arg4 === "number") {
			//console.log("run with 4 params");
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
	this.setOrigP = function(x, y, time){
		if (typeof x === "number" && typeof y === "number") {
			m_xList = [];
	  		m_yList = [];
	  		m_timeList = [];
	  		this.setP(x, y, time);
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
		if (m_timeList.length < 2) { return this; }
		m_xList.pop();
		m_yList.pop();
		m_timeList.pop();
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

/*
 //Test Functions for SRL_Point()
SRL_Shape.prototype = new SRL_Object();
SRL_Point.prototype = new SRL_Shape();

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

//*/