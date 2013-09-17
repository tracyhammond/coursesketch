 *******************************
 *
 *
 * Overload data class
 * @author Daniel Tan
 *
 *
 *
 *******************************
 */

function Overloads() {
	/**
	 * This function returns a string with the value of the original object's type
	 * (e.g. SRL_Object, SRL_Shape, SRL_Point, etc.)
	 @return a string class_type
	 */
	this.check_type = function(){
		var class_type;
		if (this instanceof SRL_Object 
			&& this instanceof SRL_Shape 
			&& this instanceof SRL_Stroke) {
			class_type = "SRL_Stroke";
			//console.log("I am SRL_Stroke");
		} else if (this instanceof SRL_Object 
				&& this instanceof SRL_Shape 
				&& this instanceof SRL_Point) {
			class_type = "SRL_Point";
			//console.log("I am SRL_Point");
		} else if (this instanceof SRL_Object 
				&& this instanceof SRL_Shape 
				&& this instanceof SRL_Line) {
			class_type = "SRL_Line";
			//console.log("I am SRL_Line");
		} else if (this instanceof SRL_Object 
				&& this instanceof SRL_Shape) {
			class_type = "SRL_Shape";
			//console.log("I am SRL_Shape");
		} else if (this instanceof SRL_Object) {
			class_type = "SRL_Object";
			//console.log("I am SRL_Object");
		}
		return class_type;
	}
	/*
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Shape.getArea() and SRL_Line.getArea() for more details
	 * @return the return value of the function it calls
	 */
	this.getArea = function () {
		if (this.check_type() == "SRL_Shape") {
			return(this.getArea.SRL_Shape.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Line") {
			return(this.getArea.SRL_Line.apply( this, arguments ));
		}
	}
	/*
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Stroke.getMinX(), SRL_Line.getMinX() SRL_Point.getMinX() for more details
	 * @return the return value of the function it calls
	 */
	this.getMinX = function () {
		if (this.check_type() == "SRL_Stroke") {
			return(this.getMinX.SRL_Stroke.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Point") {
			return(this.getMinX.SRL_Point.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Line") {
			return(this.getMinX.SRL_Line.apply( this, arguments ));
		}
	}
	/*
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Stroke.getMinY(), SRL_Line.getMinY() SRL_Point.getMinY() for more details
	 * @return the return value of the function it calls
	 */
	this.getMinY = function () {
		if (this.check_type() == "SRL_Stroke") {
			return(this.getMinY.SRL_Stroke.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Point") {
			return(this.getMinY.SRL_Point.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Line") {
			return(this.getMinY.SRL_Line.apply( this, arguments ));
		}
	}
	/*
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Stroke.getMaxX(), SRL_Line.getMaxX() SRL_Point.getMinX() for more details
	 * @return the return value of the function it calls
	 */
	this.getMaxX = function () {
		if (this.check_type() == "SRL_Stroke") {
			return(this.getMaxX.SRL_Stroke.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Point") {
			return(this.getMaxX.SRL_Point.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Line") {
			return(this.getMaxX.SRL_Line.apply( this, arguments ));
		}
	}
	/*
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Stroke.getMaxY(), SRL_Line.getMaxY() SRL_Point.getMaxY() for more details
	 * @return the return value of the function it calls
	 */
	this.getMaxY = function () {
		if (this.check_type() == "SRL_Stroke") {
			return(this.getMaxY.SRL_Stroke.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Point") {
			return(this.getMaxY.SRL_Point.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Line") {
			return(this.getMaxY.SRL_Line.apply( this, arguments ));
		}
	}
	/*
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Point.distance() SRL_Line.distance() for more details
	 * @return the return value of the function it calls
	 */
	this.distance = function () {
		if (this.check_type() == "SRL_Point") {
			return(this.distance.SRL_Point.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Line") {
			return(this.distance.SRL_Line.apply( this, arguments ));
		}
	}
};

/**
 *******************************
 *
 *
 * Object data class
 * @author hammond; Daniel Tan
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 *
 *
 *******************************
 */
function SRL_Object() {
	/**
	 * Each object has a unique ID associated with it.
	 */
	var m_id; // = guid();
	/**
	 * The name of the object, such as "triangle1"
	 */
	var m_name = "";
	/**
	 * The creation time of the object.
	 */
	//TODO have Avid fill in the functions for time
	//var m_time = this.getTime();
	/**
	 * An object can be created by a user 
	 * (like drawing a shape, or speaking a phrase)
	 * or it can be created by a system
	 * (like a recognition of a higher level shape)
	 */
	var m_isUserCreated = false;
	/**
	 * A list of possible interpretations for an object
	 */
	var m_interpretations = new Array();
	/**
	 * Was this object made up from a collection of subObjects? 
	 * If so they are in this list.
	 * This list usually gets filled in through recognition.
	 * This list can be examined hierarchically.
	 * e.g., an arrow might have three lines inside, and each line might have a stroke.
	 */
	var m_subObjects = new Array();
	/**
	 * Adds a subobject to this object. 
	 * This usually happens during recognition, when a new object
	 * is made up from one or more objects
	 * @param subObject
	 */
	this.addSubObject = function(subObject){
		if (subObject instanceof SRL_Object){
			m_subObjects.push(subObject);
		}
	}
	/**
	 * Gets the list of subobjects
	 * @return list of objects that make up this object
	 */
	this.getSubObjects = function(){
		return m_subObjects;
	}
	/**
	 * Gets a list of all of the objects that make up this object.
	 * This is a recursive search through all of the subobjects.
	 * This objects is also included on the list.
	 * @return
	 */
	this.getRecursiveSubObjectList = function() {
		var completeList = new Array();
		completeList.push(this);
		for (var i=0; i<m_subObjects.length; i++) {
			for (var j=0; j<m_subObjects[i].length; j++) {
				completeList.push(m_subObjects[i].getRecursiveSubObjectList()[j]);
			}
		}
		return completeList;
	}
	/**
	 * add an interpretation for an object
	 * @param interpretation a string name representing the interpretation
	 * @param confidence a double representing the confidence
	 * @param complexity a double representing the complexity
	 */
	this.addInterpretation = function(interpretation, confidence, complexity) {
		//m_interpretations.push(new SRL_Interpretation(interpretation, confidence, complexity));
		console.log("Implement SRL_Object.addInterpretation later");
		console.log("This may not actually be needed");
	}
	/**
	 * @return unique UUID for an object
	 */
	this.getId = function() {
		return m_id;
	}
	/**
	 * An object can have a name, such as "triangle1". 
	 * @return the string name of the object
	 */
	this.getName = function() {
		return m_name;
	}
	/**
	 * An object can have a name, such as "triangle1". 
	 * @param name object name
	 */
	this.setName = function(name) {
		m_name = name;
	}
	/**
	 * Gets the time associated with the object. 
	 * The default time is the time it was created
	 * @return the time the object was created.
	 */
	this.getTime = function() {
		return m_time;
	}
	/**
	 * Sets the time the object was created. This probably should 
	 * only be used when loading in pre-existing objects.
	 * @param time the time the object was created.
	 */
	this.setTime = function(time) {
		m_time = time;
	}
	/**
	 * An object can be created by a user 
	 * (like drawing a shape, or speaking a phrase)
	 * or it can be created by a system
	 * (like a recognition of a higher level shape)
	 * default is false if not explicitly set
	 * @return true if a user created the shape
	 */
	this.isUserCreated = function() {
		return m_isUserCreated;
	}
	/**
	 * An object can be created by a user 
	 * (like drawing a shape, or speaking a phrase)
	 * or it can be created by a system
	 * (like a recognition of a higher level shape)
	 * @param isUserCreated true if the user created the shape, else false
	 */
	this.setUserCreated = function(isUserCreated) {
		m_isUserCreated = isUserCreated;
	}
};

/**
 *******************************
 *
 *
 * Shape data class
 * @author hammond; Daniel Tan
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 *
 *
 *******************************
 */

function SRL_Shape(type) {
	/**
	 * Gets a list of all of the strokes that make up this object.
	 * It searches recursively to get all of the strokes of this object.
	 * If it does not have any strokes, the list will be empty.
	 * @return
	 */
	this.getStrokes = function(){
		var completeList = new Array();
		console.log("TODO - need to implement a .getRecursiveSubObjectList()");
		/*
		for(SRL_Object o : getRecursiveSubObjectList()){
			try {
				if(this.getClass() == Class.forName("SRL_Stroke")){
					completeList.add((SRL_Stroke)o);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		//*/
		return completeList;
	}
	/**
	 * Returns the center x of a shape.
	 * @return center x of a shape
	 */
	this.getCenterX = function(){
		return (getMinX() + getMaxX())/2.0;
	}
	/**
	 * Returns the center y of a shape
	 * @return center y of a shape
	 */
	this.getCenterY = function(){
		return (this.getMinY() + this.getMaxY())/2.0;
	}
	/**
	 * Get the bounding box of the stroke
	 * This returns an awt shape. 
	 * Use getBoundingSRLRectangle to get the SRL shape
	 * @return the bounding box of the stroke
	 */
	this.getBoundingBox = function() {
		//var r = new Rectangle();
		//r.setRect(getMinX(), getMinY(), getWidth(), getHeight());
		//return r;
	}
	/**
	 * Returns the width of the object
	 * @return the width of the object
	 */
	this.getWidth = function(){
		return getMaxX() - getMinX();
	}
	/**
	 * Returns the height of the object
	 * @return the height of the object
	 */
	this.getHeight = function(){
		return getMaxY() - getMinY();
	}
	
	/**
	 * Returns the length times the height
	 * See also getLengthOfDiagonal()
	 * return area of shape
	 */
	this.getArea.SRL_Shape = function(){
		return getHeight() * getWidth();
	}
	/**
	 * This returns the length of the diagonal of the bounding box. 
	 * This might be a better measure of perceptual size than area
	 * @return Euclidean distance of bounding box diagonal
	 */
	this.getLengthOfDiagonal = function(){
		return Math.sqrt(getHeight() * getHeight() + getWidth() * getWidth());
	}
	
	/**
	 * This function just returns the same thing as the length of the diagonal
	 * as it is a good measure of size.
	 * @return size of the object.
	 */
	this.getSize = function(){
		return getLengthOfDiagonal();
	}
	
	/**
	 * Returns the angle of the diagonal of the bounding box of the shape
	 * @return angle of the diagonal of the bounding box of the shape
	 */
	this.getBoundingBoxDiagonalAngle = function() {
		return Math.atan(getHeight()/getWidth());
	}
};

/**
 *******************************
 *
 *
 * Stroke data class
 * @author hammond; Daniel Tan
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 *
 *
 *******************************
 */

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
		console.log("IGNORE AWT");
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
	this.getMinX.SRL_Stroke = function() {
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
	this.getMinY.SRL_Stroke = function() {
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
	this.getMaxX.SRL_Stroke = function() {
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
	this.getMaxY.SRL_Stroke = function() {
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

/**
 *******************************
 *
 *
 * Point data class
 * @author hammond; Daniel Tan
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 *
 *
 *******************************
 */

function SRL_Point(x, y) {
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
		this.setP(x,y);
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


	this.distance.SRL_Point = function(arg1, arg2, arg3, arg4) {
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
	 * Return an object drawable by AWT
	 * return awt point
	 */
	this.getAWT = function(){
		console.log("IGNORE AWT");
		//return new Point((int)getX(),(int)getY());
	}
	/**
	 * Just returns the x value with is obviously the same as the min
	 * return x value
	 */
	this.getMinX.SRL_Point = function() {
		return this.getX();
	}
	/**
	 * Just returns the y value with is obviously the same as the min
	 * return y value
	 */
	this.getMinY.SRL_Point = function() {
		return this.getY();
	}
	/**
	 * Just returns the x value with is obviously the same as the max
	 * return x value
	 */
	this.getMaxX.SRL_Point = function() {
		return this.getX();
	}
	/**
	 * Just returns the y value with is obviously the same as the max
	 * return y value
	 */
	this.getMaxY.SRL_Point = function() {
		return this.getY();
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

/**
 *******************************
 *
 *
 * Line data class
 * @author hammond, Daniel Tan
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 *
 *
 *******************************
 */

function SRL_Line(p1, p2, p3, p4) {
	/**
	 * Starting value of the line
	 */
	var m_p1 = new SRL_Point();
	/**
	 * Ending value of the line
	 */
	var m_p2 = new SRL_Point();
	/**
	 * Provide an object drawable through Graphics2D
	 */
	this.getAWT = function(){
		console.log("IGNORE AWT");
		//return new Line2D.Double(m_p1.getAWT(), m_p2.getAWT());
	}
	/**
	 * Get the starting point of the line
	 * @return starting point of the line
	 */
	this.getP1 = function() {
		return m_p1;
	}
	/**
	 * Set the starting point of the line
	 * @param p1 starting point of the line
	 */
	this.setP1 = function(p1) {
		if (p1 instanceof SRL_Point){
			m_p1 = p1;
		}
	}
	/**
	 * Get the ending point of the line
	 * @return ending point of the line
	 */
	this.getP2 = function() {
		return m_p2;
	}
	/**
	 * Set the ending point of the line
	 * @param p2 ending point of the line
	 */
	this.setP2 = function(p2) {
		if (p2 instanceof SRL_Point){
			m_p2 = p2;
		}
	}
	/**
	 * Constructor that takes two srlpoints
	 * @param p1 start point
	 * @param p2 end point
	 */
	if (p1 instanceof SRL_Point && p2 instanceof SRL_Point) {
		this.setP1(p1);
		this.setP2(p2);

		/**
		 * Create a new line from the end point values
		 * @param x1 x value of endpoint 1
		 * @param y1 y value of endpoint 1
		 * @param x2 x value of endpoint 2
		 * @param y2 y value of endpoint 2
		 */
	} else if (typeof p1 === "number" && 
				typeof p2 === "number" && 
				typeof p3 === "number" && 
				typeof p4 === "number") {
	    var arg1 = new SRL_Point(p1, p2);
	    var arg2 = new SRL_Point(p3, p4);
	    this.setP1(arg1);
	    this.setP2(arg2);
	} else {
		//do nothing;
	}
	/**
	 * Returns the slope of the line. Note that 
	 * if this line is vertical, this will cause an error.
	 * This is the m in the line equation in y = mx + b.
	 * @return slope of the line
	 */
	this.getSlope = function(x1, y1, x2, y2){
	 	if (x1 === undefined && 
	 		y1 === undefined && 
	 		x2 === undefined && 
	 		y2 === undefined) {
			return (this.getP2().getY() - this.getP1().getY())/
		        (this.getP2().getX() - this.getP1().getX());
	 	} else if (typeof x1 === "number" && 
	 				typeof y1 === "number" && 
	 				typeof x2 === "number" && 
	 				typeof y2 === "number") {
	    	return (y2 - y1)/ (x2 - x1);
	    }
	}
	/**
	 * Returns the y-intercept of the line.  (Where the 
	 * line crosses the y axis.) This is the b in 
	 * the equation for a line y = mx + b. Note that this will 
	 * cause an error if this line is vertical.
	 * @return the y-intercept
	 */
	this.getYIntercept = function(x1, y1, x2, y2){
	 	if (x1 === undefined && 
	 		y1 === undefined && 
	 		x2 === undefined && 
	 		y2 === undefined) {
	    	return this.getP1().getY() - this.getSlope() * this.getP1().getX();
	    } else if (typeof x1 === "number" && 
	    			typeof y1 === "number" && 
	    			typeof x2 === "number" && 
	    			typeof y2 === "number") {
	    	return y1 - this.getSlope(x1, y1, x2, y2) * x1;
	    }
	}
	/**
	 * Returns a vector of doubles A,B,C representing the 
	 * line in the equations Ax + BY = C;
	 * @return the vector, [A,B,C]
	 */
	this.getABCArray = function(x1, y1, x2, y2){
	    var A = 0;
	    var B = 0;
	    var C = 0;
	    if(Math.abs(x2-x1) < .001){
	      A = 1;
	      B = 0;
	      C = x1;
	    } else if (Math.abs(y2-y1) < .001){
	      A = 0;
	      B = 1;
	      C = y1;
	    } else {
	      A = - this.getSlope(x1, y1, x2, y2);
	      B = 1;
	      C = this.getYIntercept(x1, y1, x2, y2);
	    }
	    var array = new Array();
	    array[0] = A;
	    array[1] = B;
	    array[2] = C;
	    //confirm
	    if((Math.abs(A*x1 +  B*y1 - C) > .001) || (Math.abs(A*x2 + B * y2 - C) > .001)){
	      throw ("getABCArray FAILED! A:" + A + ",B:" + B + "C:" + C + " (" + x1 + "," + y1 + "),(" + x2 + "," + y2 + ")");
	    }
	    return array;
	}
	/**
	 * Returns the intersection point between this 
	 * line and the given line as if they are infinite.
	 * This function returns null if there is no intersection point
	 * (i.e., the lines are parallel).
	 * @param l the other line
	 * @return The intersection point between the two lines
	 */
	this.getIntersection = function(l1_x1, l1_y1, l1_x2, l1_y2, l2_x1, l2_y1, l2_x2, l2_y2) { 
		if (l1_x1 instanceof SRL_Line){
			var iPoint = this.getIntersection(
						this.getP1().getX(), this.getP1().getY(), 
						this.getP2().getX(), this.getP2().getY(),
						l1_x1.getP1().getX(), l1_x1.getP1().getY(), 
						l1_x1.getP2().getX(), l1_x1.getP2().getY()
					);
			return new SRL_Point(iPoint[0], iPoint[1]);
		} else if (typeof l1_x1 === "number" && 
					typeof l1_y1 === "number" && 
					typeof l1_x2 === "number" && 
					typeof l1_y2 === "number" && 
					typeof l2_x1 === "number" && 
					typeof l2_y1 === "number" && 
					typeof l2_x2 === "number" && 
					typeof l2_y2 === "number") {
		    var array1 = this.getABCArray(l1_x1, l1_y1, l1_x2, l1_y2);
		    var array2 = this.getABCArray(l2_x1, l2_y1, l2_x2, l2_y2);
		  
		    var a1 = array1[0];
		    var b1 = array1[1];
		    var c1 = array1[2];
		    var a2 = array2[0];
		    var b2 = array2[1];
		    var c2 = array2[2];
		    var x = 0;
		    var y = 0;
		    var done = false;

		    while(!done){
		    	done = true;
				//fix problems from floating point errors
				if(Math.abs(a1) < .001){a1 = 0;}
				if(Math.abs(a2) < .001){a2 = 0;}
				if(Math.abs(b1) < .001){b1 = 0;}
				if(Math.abs(b2) < .001){b2 = 0;}
				if(Math.abs(c1) < .001){c1 = 0;}
				if(Math.abs(c2) < .001){c2 = 0;}
				if(a1 == 0 && b1 == 0 && a2 == 0 && b2 == 0){done = true;}
				else if(a1 == 0 && b1 == 0 && c1 != 0){done = true;}
				else if(a2 == 0 && b2 == 0 && c2 != 0){done = true;}
				else if(a2 == 0 && b2 == 0 && c2 == 0){
			        //can pick any point on other line
			        if(b1 == 0){
						x = c1/a1;
						y = 0;
			        } else {
						y = c1/b1;
						x = 0;        
			        }  
			        done = true;
				} else if(a1 == 0 && b1 == 0 && c1 == 0){
			        //can pick any point on other line
			        if(b2 == 0){
						x = c2/a2;
						y = 0;
			        } else {
						y = c2/b2;
						x = 0;        
			        }  
			        done = true;
				} else if (a1 == 0 && a2 == 0){        
					y = c1/b1;
					x = 0;
					done = true;
				} else if (a1 == 0 && b2 == 0){
			        y = c1/b1;
			        x = c2/a2;
			        done = true;
				} else if (a1 == 0){
			        y = c1/b1;
			        x = (c2 - y*b2) / a2;
			        done = true;
				} else if (b1 == 0 && b2 == 0){
			        x = c1/a1;
			        y = 0;
			        done = true;
				} else if (b1 == 0 && a2 == 0){
			        x = c1/a1;
			        y = c2/b2;
			        done = true; 
				} else if (b1 == 0){
			        x = c1/a1;
			        y = (c2 - x*a2)/b2;
			        done = true;
				} else if (a2 == 0){
			        y = c2/b2;
			        x = (c1 - y*b1)/a1;
			        done = true;
				} else if (b2 == 0){
			        x = c2/a2;
			        y = (c1 - x*a1)/b1;
				} else if (b2 * a2 != 0){
			        var fraction = a1/a2;
			        a2 *= fraction;
			        b2 *= fraction;
			        c2 *= fraction;
			        a2 -= a1;
			        b2 -= b1;
			        c2 -= c1;
			        done = false;
			    }
			}
		    if(Math.abs(a1 * x + b1 * y - c1) > .001 || 
		    	Math.abs(a2 * x + b2 * y - c2) > .001){
		    	throw ("["+a1+","+b1+","+c1+"]["+a2+","+b2+","+c2+"]");
		    	throw ("Failed Intersection! " + x + "," + y);
		    	throw (Math.abs(a1 * x + b1 * y - c1));
		    	throw (Math.abs((a2 * x) + (b2 * y) - c2) + " " +  ((a2 * x) + (b2 * y) - c2) + " a2 * x = " + (a2 * x) + "  b2 * y = " + (b2 * y) + " c2 = " + c2 );
		    	throw ("Failed Intersection! " + x + "," + y);
		    	throw ("Initial values were: [(" + l1_x1 + "," + l1_y1 + "),(" + l1_x2 + "," + l1_y2 + ")] [(" + l2_x1 + "," + l2_y1 + "),(" + l2_x2 + "," + l2_y2 + ")]");
		    	return null;
		    }
		    var iPoint = new Array(x,y);
		    return iPoint;
		} else {
			throw "argument(s) of .getIntersection is(are) wrong";
		}
	}
	/**
	 * Returns the euclidean length of the line between the two endpoints
	 * @return the length
	 */
	this.getLength = function(){
	    return m_p1.distance(m_p2);
	}
	/**
	 * Returns the euclidean length of the line.  The area of the line
	 * is assumed to be the length times the width, where the width is 1.
	 * return area of line, which equals the euclidean length
	 */ 
	this.getArea.SRL_Line = function(){
		return this.getLength();
	}
	/**
	 * Returns a new line in the opposite direction.
	 * Note that the points inside the line are not cloned, 
	 * but rather the same original points.
	 * @return a line with the endpoints swapped.
	 */
	this.getFlippedLine = function() {
		return new SRL_Line(this.getP2(), this.getP1());
	}
	/**
	 * Make this line to be permanently going in the other direction.
	 * Note that the timing of the points will not be changed.
	 * This just swaps the first and the last point.
	 */
	this.flip = function() {
		var temp = new SRL_Point();
		temp = m_p1;
	    m_p1 = m_p2;
	    m_p2 = temp;    
	}
	/**
	 * Returns the x value of the leftmost point.
	 * return min x of line
	 */
	this.getMinX.SRL_Line = function() {
		return Math.min(this.getP1().getX(), this.getP2().getX());
	}
	/**
	 * Returns the y value of the lower point 
	 * return min y of line
	 */
	this.getMinY.SRL_Line = function() {
		return Math.min(this.getP1().getY(), this.getP2().getY());
	}
	/**
	 * Returns the x value of the rightmost point
	 * return max x of line
	 */
	this.getMaxX.SRL_Line = function() {
		return Math.max(this.getP1().getX(), this.getP2().getX());
	}
	/**
	 * Returns the y value of the highest point
	 * return max y of line
	 */
	this.getMaxY.SRL_Line = function() {
		return Math.max(this.getP1().getY(), this.getP2().getY());
	}
	/**
	 * This function creates a line segment of the same
	 * length as this line but is perpendicular to this line
	 * and has an endpoint at point p.
	 * @param p endpoint of perpendicular line
	 * @return the perpendicular line segment.
	 */
	this.getPerpendicularLine = function(arg1, arg2, arg3, arg4, arg5, arg6, arg7) {
		if (arg1 instanceof SRL_Point) {
			var p = arg1;
			var len = this.getLength();
			var perpline = this.getPerpendicularLine(p.getX(), p.getY(), len, 
				this.getP1().getX(), this.getP1().getY(), this.getP2().getX(), this.getP2().getY());
			return new SRL_Line(perpline[0], perpline[1], perpline[2], perpline[3]);
		} else if (typeof arg1 === "number" &&
					typeof arg2 === "number" &&
					typeof arg3 === "number" &&
					typeof arg4 === "number" &&
					typeof arg5 === "number" &&
					typeof arg6 === "number" &&
					typeof arg7 === "number") {
			var newx = arg1;
			var newy = arg2;
			var newlength = arg3;
			var oldx1 = arg4;
			var oldy1 = arg5;
			var oldx2 = arg6; 
			var oldy2 = arg7;

			var newangle = Math.atan2(oldy2 - oldy1, oldx2 - oldx1) + Math.PI/2;
		    var perpline = new Array (newx,newy, newx + Math.cos(newangle) * newlength, 
		    		newy + Math.sin(newangle) * newlength);
		    return perpline;
		} else {
			throw "argument(s) of .getPerpendicularLine is(are) wrong";
		}
	}
	/**
	 * Is this point on the bounding box of the point
	 * @param p the point on the bounding box
	 * @return true if the point and line share the bounding box
	 */
	this.overBoundingBox = function(arg1, arg2, arg3, arg4, arg5, arg6) {
		if (arg1 instanceof SRL_Point) {
			var p = arg1;
			if(p.getX() > this.getP1().getX() && p.getX() > this.getP2().getX()){return false;}
			if(p.getX() < this.getP1().getX() && p.getX() < this.getP2().getX()){return false;}
			if(p.getY() > this.getP1().getY() && p.getY() > this.getP2().getY()){return false;}
			if(p.getY() < this.getP1().getY() && p.getY() < this.getP2().getY()){return false;}
			return true;
		} else if (typeof arg1 === "number" &&  // px
					typeof arg2 === "number" && // py
					typeof arg3 === "number" && // lx1
					typeof arg4 === "number" && // ly1
					typeof arg5 === "number" && // lx2
					typeof arg6 === "number") { // ly2
			var px = arg1;
			var py = arg2;
			var lx1 = arg3;
			var ly1 = arg4;
			var lx2 = arg5;
			var ly2 = arg6;

			if(px > lx1 && px > lx2){return false;}
			if(px < lx1 && px < lx2){return false;}
			if(py > ly1 && py > ly2){return false;}
			if(py < ly1 && py < ly2){return false;}
			return true;
		}
	}
	/**
	 * returns angle in radians
	 * @return angle in radians
	 */
	this.getAngleInRadians = function(x1, y1, x2, y2){
		if (x1 === undefined &&
			y1 === undefined &&
			x2 === undefined &&
			y2 === undefined) {
			return Math.atan2(this.getP2().getY() - this.getP1().getY(), 
				this.getP2().getX() - this.getP1().getX());

		/**
		 * returns angle in radians of two separate lines
		 * @param x1
		 * @param y1
		 * @param x2
		 * @param y2
		 * @return angle in radians of two separate lines
		 */
		} else if (typeof x1 === "number" &&
					typeof y1 === "number" &&
					typeof x2 === "number" &&
					typeof y2 === "number") {
			return Math.atan2(y2 - y1, x2 - x1);
		} else {
			throw "arguments of .getAngleInRadians is wrong";
		}
	}
	/**
	 * Returns the angle of the line in degrees
	 * @return angle in degrees from 1-360
	 */
	this.getAngleInDegrees = function(){
		var angle = 360 - this.getAngleInRadians() * 180/Math.PI;
		while(angle < 0){angle += 360;}
		while(angle >=360){angle -=360;}
		return angle;
	}
	/**
	 * returns the angle in degrees within 180
	 * @return angle in degrees
	 */
	this.getAngleInDegreesUndirected = function(){
		var angle = 360 - this.getAngleInRadians() * 180/Math.PI;
	    while(angle < 0){angle += 180;}
	    while(angle >=180){angle -=180;}
	    return angle;
	}
	/**
	 * Returns true if the lines are parallel
	 * within given threshold. If threshold is 0,
	 * the lines have to be perfectly parallel.
	 * If the threshold is 1, all lines are parallel
	 * If the threshold is .5, lines with a difference of  less than 45 
	 * degrees are parallel.
	 * @param line 
	 * @param percent_threshold
	 * @return true if parallel
	 */
	this.isParallel = function(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9) {
		if (arg1 instanceof SRL_Line) {
			var line = arg1;
			var percent_threshold = arg2;

		    var threshold = percent_threshold * Math.PI/2;
		    var diff = this.getAngleInRadians() - line.getAngleInRadians();
		    while(diff < 0){ diff += Math.PI;}
		    while(diff > Math.PI){diff -= Math.PI;}
		    if(diff <= threshold){
		    	return true;
		    }
		    if(diff >= Math.PI - threshold){
		    	return true;
		    }
		    return false;
		} else if (typeof arg1 === "number" &&  // l1_x1
					typeof arg2 === "number" && // l1_y1
					typeof arg3 === "number" && // l1_x2
					typeof arg4 === "number" && // l1_y2
					typeof arg5 === "number" && // l2_x1
					typeof arg6 === "number" && // l2_y1
					typeof arg7 === "number" && // l2_x2
					typeof arg8 === "number" && // l2_y2
					typeof arg9 === "number") { // percent_threshold
			var l1_x1 = arg1;
			var l1_y1 = arg2;
			var l1_x2 = arg3;
			var l1_y2 = arg4;
			var l2_x1 = arg5;
			var l2_y1 = arg6;
			var l2_x2 = arg7;
			var l2_y2 = arg8;
			var percent_threshold = arg9;

			var threshold = percent_threshold * Math.PI/2;
	    	var diff = this.getAngleInRadians(l1_x1, l1_y1, l1_x2, l1_y2) - this.getAngleInRadians(l2_x1, l2_y1, l2_x2, l2_y2);
	    	while(diff < 0){ diff += Math.PI;}
	    	while(diff > Math.PI){diff -= Math.PI;}
	    	if(diff <= threshold){
	    		return true;
	    	}
	    	if(diff >= Math.PI - threshold){
	    		return true;
	    	}
	    	return false;
		} else {
			throw "arguments of .isParallel are wrong";
		}
	}
	/**
	 * Compute the distance from this line to 
	 * the closest point on the line.
	 * @param p the point to compare
	 * @return the distance
	 */
	this.distance.SRL_Line = function(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8) { 
		if (arg1 instanceof SRL_Point) {
			var p = arg1;
			var perp = this.getPerpendicularLine(p);
			var intersectionPoint = this.getIntersection(perp);
			if (this.overBoundingBox(intersectionPoint)) {
				return intersectionPoint.distance(p);
			}
			return Math.min(p.distance(getP1()), p.distance(getP2()));
		} else if (typeof arg1 === "number" &&  // px
					typeof arg2 === "number" && // py
					typeof arg3 === "number" && // l1_x1
					typeof arg4 === "number" && // l1_y1
					typeof arg5 === "number" && // l1_x2
					typeof arg6 === "number" && // l1_y2
					arg7 === undefined &&
					arg8 === undefined) {
			var px = arg1;
			var py = arg2;
			var l1_x1 = arg3;
			var l1_y1 = arg4;
			var l1_x2 = arg5;
			var l1_y2 = arg6;
			var temp = new SRL_Point();

			if (l1_x1 == l1_x2 && l1_y1 == l1_y2) {
				return temp.distance(l1_x1, l1_y1, px, py);
			}
			var perp = this.getPerpendicularLine(px, py, 10, l1_x1, l1_y1, l1_x2, l1_y2);
			var iPoint = this.getIntersection(l1_x1, l1_y1, l1_x2, l1_y2,
				perp[0], perp[1], perp[2], perp[3]);
			if (iPoint != null && this.overBoundingBox(iPoint[0], iPoint[1], l1_x1, l1_y1, l1_x2, l1_y2)){
				return temp.distance(px, py, iPoint[0], iPoint[1]);
			}
			return Math.min(temp.distance(l1_x1, l1_y1, px, py),
				temp.distance(l1_x2, l1_y2, px, py));
		} else if (arg1 instanceof SRL_Line) {
			var l = arg1;
			var intersectionPoint;
		    if (this.isParallel(l, .001)) {
		    	intersectionPoint = l.getP1();
		    } else {
		    	intersectionPoint = this.getIntersection(l);
		    }
		    if (intersectionPoint == null) {
		    	throw "intersection point is null!";
		    	return 0;
		    }
		    var di = Math.max(this.distance(intersectionPoint), l.distance(intersectionPoint));
		    var d1 = this.distance(l.getP1());
		    var d2 = this.distance(l.getP2());
		    return Math.min(di, Math.min(d1, d2));
		} else if (typeof arg1 === "number" && 
					typeof arg2 === "number" && 
					typeof arg3 === "number" && 
					typeof arg4 === "number" && 
					typeof arg5 === "number" && 
					typeof arg6 === "number" && 
					typeof arg7 === "number" && 
					typeof arg8 === "number") {
			var l1_x1 = arg1;
			var l1_y1 = arg2;
			var l1_x2 = arg3;
			var l1_y2 = arg4;
			var l2_x1 = arg5;
			var l2_y1 = arg6;
			var l2_x2 = arg7;
			var l2_y2 = arg8;

		    var iPoint = new Array(l1_x1, l1_y1);
		    if (l1_x1 == l1_x2 && l1_y1 == l1_y2) {
		    	return this.distance(l1_x1, l1_y1, l2_x1, l2_y1, l2_x2, l2_y2);
		    }
		    if (l2_x1 == l2_x2 && l2_y1 == l2_y2) {
		    	return this.distance(l2_x1, l2_y1, l1_x1, l1_y1, l1_x2, l1_y2);
		    }
		    if (!this.isParallel(l1_x1, l1_y1, l1_x2, l1_y2, 
		    	l2_x1, l2_y1, l2_x2, l2_y2, .001)){
		    	iPoint = this.getIntersection(l1_x1, l1_y1, l1_x2, l1_y2, 
		    		l2_x1, l2_y1,l2_x2, l2_y2);
		    }
		    if (iPoint == null) {
		    	throw "intersection point is null!";
		    	return 0;
		    }
		    var di = Math.max(this.distance(iPoint[0], iPoint[1], l2_x1, l2_y1, l2_x2, l2_y2), 
		    	this.distance(iPoint[0], iPoint[1], l1_x1, l1_y1, l1_x2, l1_y2)) ;
		    var d1 = this.distance(l2_x1, l2_y1, l1_x1, l1_y1, l1_x2, l1_y2);
		    var d2 = this.distance(l2_x2, l2_y2, l1_x1, l1_y1, l1_x2, l1_y2);    
		    return Math.min(di, Math.min(d1, d2));
		} else {
			throw "argument(s) of .getIntersection is(are) wrong";
		}
	}
	/**
	 * Do the two lines intersect?
	 * @param line second line that might intersect with this one
	 * @return yes if they intersect
	 */
	this.intersects = function(line){
		//System.debug.println("distance between lines is " + distance(line));
		if(this.distance(line) < .1){return true;}
		return false;
	}

	this.temp_print = function(){
		console.log("printing m_p1");
		console.log(m_p1.temp_print());
		console.log("printing m_p2");
		console.log(m_p2.temp_print());
	}
};

/**
 **************************************************************
 *
 *
 * Test Functions for SRL_Library
 * @author Daniel Tan
 * 
 *
 *
 **************************************************************
 */

// Set up the object inheritance:
SRL_Object.prototype = new Overloads();
SRL_Shape.prototype = new SRL_Object();
SRL_Stroke.prototype = new SRL_Shape();
SRL_Point.prototype = new SRL_Shape();
SRL_Line.prototype = new SRL_Shape();

//************************************************************************
//
//
//Test Functions for SRL_Stroke()
//
//
//************************************************************************

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