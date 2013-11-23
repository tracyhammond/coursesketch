/*******************************
 *
 *
 * Overload data class
 * @author Daniel Tan
 *
 *
 *
 *******************************
 */

const SRL_ObjectType = "SRL_Object";
const SRL_SketchType = "SRL_Sketch";
const SRL_ShapeType = "SRL_Shape";
const SRL_StrokeType = "SRL_Stroke"; 
const SRL_PointType = "SRL_Point";

function Overloads() {
	/**
	 * This function returns a string with the value of the original object's type
	 * (e.g. {@link SRL_Object}, {@link SRL_Shape}, {@link SRL_Point}, {@link SRL_Stroke}, {@link SRL_Line})
	 @return a string class_type
	 */
	var classType = false;
	this.check_type = function() {
		if (!classType) {
			var class_type = "No Known Type";
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
					&& false && this instanceof SRL_Line) {
				class_type = "SRL_Line";
				//console.log("I am SRL_Line");
			} else if (this instanceof SRL_Object 
					&& this instanceof SRL_Shape) {
				class_type = "SRL_Shape";
				//console.log("I am SRL_Shape");
			} else if (this instanceof SRL_Object) {
				class_type = "SRL_Object";
				//console.log("I am SRL_Object");
			}  else if (this instanceof SRL_Sketch) {
				class_type = "SRL_Sketch";
				//console.log("I am SRL_Sketch");
			}
			classType = class_type;
		}
		return classType;
	}
	/**
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Shape.getArea() and SRL_Line.getArea() for more details.
	 *
	 * @return the return value of the function it calls
	 */
	this.getArea = function () {
		if (this.check_type() == "SRL_Shape") {
			return(this.getArea.SRL_Shape.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Line") {
			return(this.getArea.SRL_Line.apply( this, arguments ));
		}
	}
	/**
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Stroke.getMinX(), SRL_Line.getMinX() SRL_Point.getMinX() for more details.
	 *
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

	/**
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Stroke.getMinY(), SRL_Line.getMinY() SRL_Point.getMinY() for more details.
	 *
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

	/**
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Stroke.getMaxX(), SRL_Line.getMaxX() SRL_Point.getMinX() for more details.
	 *
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

	/**
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Stroke.getMaxY(), SRL_Line.getMaxY() SRL_Point.getMaxY() for more details.
	 *
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

	/**
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Point.distance() SRL_Line.distance() for more details.
	 *
	 * @return the return value of the function it calls
	 */
	this.distance = function () {
		if (this.check_type() == "SRL_Point") {
			return(this.distance.SRL_Point.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Line") {
			return(this.distance.SRL_Line.apply( this, arguments ));
		}
	}

	/**
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Point.size() SRL_Shape.size() for more details.
	 *
	 * @return the return value of the function it calls
	 */
	this.getSize = function () {
		if (this.check_type() == "SRL_Point") {
			return(this.getSize.SRL_Point.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Shape") {
			return(this.getSize.SRL_Shape.apply( this, arguments ));
		}
	}

	/**
	 * Takes the original object's type and returns the overloaded function associated with that object
	 * See SRL_Object.getObjectById(), SRL_Shape.getObjectById(), SRL_Stroke.getObjectById(),
	 * and SRL_Point.getObjectById() for more details.
	 *
	 * @return the return value of the function it calls
	 */
	this.getSubObjectById = function () {
		if (this.check_type() == "SRL_Stroke") {
			return(this.getSubObjectById.SRL_Stroke.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Sketch") {
			return(this.getSubObjectById.SRL_Sketch.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Point") {
			throw 'No such method error: SRL_Point does not have method "getSubObjectById"';
		}  else if (this.check_type() == "SRL_Line") {
			throw 'No such method error: SRL_Line does not have method "getSubObjectById"';
		}
		return(this.getSubObjectById.SRL_Object.apply( this, arguments ));
	}

	this.removeSubObjectById = function () {
		if (this.check_type() == "SRL_Stroke") {
			return(this.removeSubObjectById.SRL_Stroke.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Sketch") {
			return(this.removeSubObjectById.SRL_Sketch.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Point") {
			throw 'No such method error: SRL_Point does not have method "removeSubObjectById"';
		} else if (this.check_type() == "SRL_Line") {
			throw 'No such method error: SRL_Line does not have method "removeSubObjectById"';
		}
		return (this.removeSubObjectById.SRL_Object.apply( this, arguments ));
	}

	this.removeSubObject = function () {
		if (this.check_type() == "SRL_Stroke") {
			return(this.removeSubObject.SRL_Stroke.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Sketch") {
			return(this.removeSubObject.SRL_Sketch.apply( this, arguments ));
		}  else if (this.check_type() == "SRL_Point") {
			throw 'No such method error: SRL_Point does not have method "removeSubObject"';
		} else if (this.check_type() == "SRL_Line") {
			throw 'No such method error: SRL_Line does not have method "removeSubObject"';
		}
		return(this.removeSubObject.SRL_Object.apply( this, arguments ));
	}

	this.getSubObjectAtIndex = function() {
		if (this.check_type() == "SRL_Stroke") {
			return(this.getSubObjectAtIndex.SRL_Stroke.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Sketch") {
			return(this.getSubObjectAtIndex.SRL_Sketch.apply( this, arguments ));
		}  else if (this.check_type() == "SRL_Point") {
			throw 'No such method error: SRL_Point does not have method "removeSubObject"';
		} else if (this.check_type() == "SRL_Line") {
			throw 'No such method error: SRL_Line does not have method "removeSubObject"';
		}
		return (this.getSubObjectAtIndex.SRL_Object.apply( this, arguments ));
	}

	this.removeSubObjectAtIndex = function(index) {
		if (this.check_type() == "SRL_Stroke") {
			return(this.removeSubObjectAtIndex.SRL_Stroke.apply( this, arguments ));
		} else if (this.check_type() == "SRL_Sketch") {
			return(this.removeSubObjectAtIndex.SRL_Sketch.apply( this, arguments ));
		}  else if (this.check_type() == "SRL_Point") {
			throw 'No such method error: SRL_Point does not have method "removeSubObject"';
		} else if (this.check_type() == "SRL_Line") {
			throw 'No such method error: SRL_Line does not have method "removeSubObject"';
		}
		return(this.removeSubObjectAtIndex.SRL_Object.apply( this, arguments ));
	}
};

/*******************************
 *
 *
 * SRL_Sketch data class
 * @author gigemjt
 *
 *
 *
 *******************************
 */

function SRL_Sketch() {
	this.Inherits(Overloads); // super call

	var objectList = [];
	var objectIdMap = [];
	var boundingBox = new SRL_BoundingBox();

	var objectMap = {};
	this.addObject = function(srlObject) {
		objectList.push(srlObject);
		objectIdMap[srlObject.getId()] = srlObject;
	}

	this.addSubObject = this.addObject; // backwards comaptiablity

	/**
	 * Given an object, remove this instance of the object.
	 */
	this.removeSubObject = function(srlObject) {
		var result = objectList.removeObject(srlObject);
		if (result) {
			delete objectMap[result.getId()];
		}
		return result;
	}

	/**
	 * Given an objectId, remove the object. (Slower than if you already have an instance of the object)
	 */
	this.removeSubObjectById = function(objectId) {
		var object = this.getSubObjectById(objectId);
		this.removeSubObject(object);
		return object;
	}

	this.getList = function() {
		return objectList;
	}

	/**
	 * Returns the object based off of its id.
	 */
	this.getSubObjectById = function(objectId) {
		return objectIdMap[objectId];
	}

	this.getSubObjectAtIndex = function(index) {
		return objectList[index];
	}

	this.removeSubObjectAtIndex = function(index) {
		this.removeSubObject(objectList[index]);
	}

	/**
	 * Returns the object that is a result of the given IdChain
	 */
	this.getSubObjectByIdChain = function(idList) {
		if (idList.length <= 0) {
			throw "input list is empty";
		}
		var returnShape = this.getSubObjectById(idList[0]);
		for (var i = 1; i < idList.length; i++) {
			returnShape = returnShape.getSubObjectById(idList[i]);
		}
		return returnShape;
	}

	/**
	 * Removes an object by the given IdChain.
	 *
	 * The last id in the chain is the object that is removed.
	 * The second to last id in the chain is where it is removed from.
	 *
	 * If there is only one item in the list then the item is removed from the sketch.
	 * In all cases except exceptions the item that is removed is returned from this method.
	 */
	this.removeSubObjectByIdChain = function(idList) {
		if (!isArray(idList)) {
			throw "input list is not an array: ";
		}

		if (idList.length <= 0) {
			throw "input list is empty";
		}
		// there is only 1 item in the list so remove from top level
		if (idList.length == 1) {
			return this.removeSubObjectById(idList[0]);
		}

		var parentShape = this.getSubObjectById(idList[0]);
		for (var i = 1; i < idList.length - 1; i++) {
			parentShape = parentShape.getSubObjectById(idList[i]);
		}
		var returnShape = parentShape.removeSubObjectById(idList[idList.length - 1]);
		return returnShape;
	}
}


	
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
	this.Inherits(Overloads);

	/**
	 * Each object has a unique ID associated with it.
	 */
	var id = generateUUID(); // = guid();

	/**
	 * The name of the object, such as "triangle1"
	 */
	var name = "";

	/**
	 * The creation time of the object.
	 */
	var time;

	/**
	 * An object can be created by a user 
	 * (like drawing a shape, or speaking a phrase)
	 * or it can be created by a system
	 * (like a recognition of a higher level shape)
	 */
	var isUserCreated = false;

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
	 * Contains the bounds of this shape.
	 *
	 * The bounding box is the farthest left, right, top and bottom points in this shape;
	 */
	var boundingBox = new SRL_BoundingBox();
	/**
	 * Adds a subobject to this object. 
	 * This usually happens during recognition, when a new object
	 * is made up from one or more objects
	 * @param subObject
	 */
	this.addSubObject = function(subObject) {
		if (subObject instanceof SRL_Object) {
			boundingBox.addSubObject(subObject);
			m_subObjects.push(subObject);
		}
	}

	/**
	 * Goes through every object in this list of objects. (Brute force).
	 *
	 * @return the object if it exist, returns false otherwise.
	 */
	this.getSubObjectById = function(objectId) {
		for (object in m_subObjects) {
			if (object.getId == objectId) {
				return object;
			}
		}
		return false;
	}

	/**
	 * Goes through every object in this list of objects. (Brute force).
	 *
	 * @return the object if it exist, returns false otherwise.
	 */
	this.removeSubObjectById = function(objectId) {
		for (var i = 0; i < m_subObjects.length; i++) {
			var object = m_subObjects[i];
			if (object.getId() == objectId) {
				return m_subObjects.removeObjectAtIndex(i);
			}
		}
	}

	/**
	 * Given an object, remove this instance of the object.
	 */
	this.removeSubObject = function(srlObject) {
		return m_subObjects.removeObject(srlObject);
	}

	/**
	 * Gets the list of subobjects
	 * @return list of objects that make up this object
	 */
	this.getSubObjects = function() {
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
		m_interpretations.push(new SRL_Interpretation(interpretation, confidence, complexity));
	}

	/**
	 * @return the list of interpretations for this shape.
	 */
	this.getInterpretations = function() {
		return m_interpretations;
	}

	/**
	 * sets unique UUID for an object
	 */
	this.setId = function(newId) {
		id = newId;
	}

	/**
	 * @return unique UUID for an object
	 */
	this.getId = function() {
		return id;
	}

	/**
	 * An object can have a name, such as "triangle1". 
	 * @return the string name of the object
	 */
	this.getName = function() {
		return name;
	}

	/**
	 * An object can have a name, such as "triangle1". 
	 * @param name object name
	 */
	this.setName = function(name) {
		name = name;
	}

	/**
	 * Gets the time associated with the object. 
	 * The default time is the time it was created
	 * @return the time the object was created.
	 */
	this.getTime = function() {
		return time;
	}

	/**
	 * Sets the time the object was created. This probably should 
	 * only be used when loading in pre-existing objects.
	 * @param time the time the object was created.
	 */
	this.setTime = function(inputTime) {
		if (typeof inputTime === "number") {
			time = inputTime;
		} else {
			time = null;
		}
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
		return isUserCreated;
	}
	/**
	 * An object can be created by a user 
	 * (like drawing a shape, or speaking a phrase)
	 * or it can be created by a system
	 * (like a recognition of a higher level shape)
	 * @param isUserCreated true if the user created the shape, else false
	 */
	this.setUserCreated = function(isUserCreated) {
		isUserCreated = isUserCreated;
	}
	
	/**
	 * Gets the bounding box of the object.
	 * @return the bounding box of the object
	 */
	this.getBoundingBox = function() {
		return boundingBox;
	}

	/**
	 * returns the minimum x value in an object
	 */
	this.getMinX = function() {
		return boundingBox.getLeft();//minx;
	}

	/**
	 * return minimum y value in an object
	 */
	this.getMinY = function() {
		return boundingBox.getTop();//miny;
	}

	/**
	 * return maximum x value in an object
	 */
	this.getMaxX = function() {
		return boundingBox.getRight();//maxx;
	}

	/**
	 * return maximum x value in an object
	 */
	this.getMaxY = function() {
		return boundingBox.getBottom();
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

function SRL_Shape() {

	this.Inherits(SRL_Object);
	/**
	 * Gets a list of all of the strokes that make up this object.
	 * It searches recursively to get all of the strokes of this object.
	 * If it does not have any strokes, the list will be empty.
	 * @return
	 */
	this.getRecursiveStrokes = function() {
		var completeList = new Array();
		console.log("TODO - need to implement a .getRecursiveSubObjectList()");
		throw 'Function not supported: getRecursiveStrokes';
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
	 * Returns the width of the object
	 * @return the width of the object
	 */
	this.getWidth = function(){
		return this.getBoundingBox().getWidth();//getMaxX() - getMinX();
	}
	/**
	 * Returns the height of the object
	 * @return the height of the object
	 */
	this.getHeight = function(){
		return this.bondingBox().getHeight();//getMaxY() - getMinY();
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
	this.Inherits(SRL_Shape);
	/**
	 * List of points in the stroke.
	 */
	var points = new Array();

	var intersector = new SRL_IntersectionHandler(this);
	/**
	 * Adding another point to the stroke
	 * @param point
	 */
	this.addPoint = function(point){
		if (point instanceof SRL_Point) {
			points.push(point);
			this.getBoundingBox().addPoint(point);
			intersector.addPoint(point, points.length-1);
		}
	}

	/**
	 * Constructor setting the initial point in the stroke
	 * @param startPoint
	 */
	if (startPoint instanceof SRL_Point) {
		this.addPoint(startPoint);
	} else {
	}

	/**
	 * Gets the complete list of points in the stroke
	 * @return list of points in the stroke
	 */
	this.getPoints = function() {
		return points;
	}

	this.finish = function() {
		intersector.finish();
	}

	/**
	 * Get the i'th point in the stroke 
	 * The first point has index i = 0
	 * @param i the index of the stroke
	 * @return the point at index i
	 */
	this.getPoint = function(i){
		if (typeof i === "number") {
			if(i >= points.length){
				return null;
			}
			return points[i];
		}
	}

	/**
	 * Goes through every object in this list of objects. (Brute force).
	 *
	 * @return the object if it exist, returns false otherwise.
	 */
	this.getSubObjectById = function(objectId) {
		for (object in points) {
			if (object.getId == objectId) {
				return object;
			}
		}
		return false;
	}

	/**
	 * Goes through every object in this list of objects. (Brute force).
	 *
	 * @return the object if it exist, returns false otherwise.
	 */
	this.removeSubObjectById = function(objectId) {
		for (var i = 0; i < points.length; i++) {
			var object = points[i];
			if (object.getId() == objectId) {
				return points.removeObjectAtIndex(i);
			}
		}
	}

	/**
	 * Given an object, remove this instance of the object.
	 */
	this.removeSubObject = function(srlObject) {
		return points.removeObject(srlObject);
	}

	/**
	 * Gets the list of subobjects
	 * @return list of objects that make up this object
	 */
	this.getSubObjects = function(){
		return points;
	}

	/**
	 * Gets the number of points in the stroke
	 * @return number of points in the stroke
	 */
	this.getNumPoints = function(){
		return points.length;
	}

	/**
	 * Returns the first point in the stroke.
	 * if the stroke has no points, it returns null.
	 * @return first point in the stroke
	 */
	this.getFirstPoint = function(){
		if (points.length == 0){
			return null;
		}
		return points[0];
	}

	/**
	 * Returns the last point in the stroke
	 * If the stroke has no points, it returns null.
	 * @return last point in the stroke.
	 */
	this.getLastPoint = function(){
		if (points.length == 0){
			return null;
		}
		return points[points.length-1];
	}

	this.getStrokeIntersector = function() {
		return intersector;
	}
	
	/** returns the minimum x value in a stroke
	 * return minimum x value in a stroke
	 */
	this.getMinX.SRL_Stroke = function() {
		/*
		var minx = this.getFirstPoint().getX();
		for(var i=0; i<points.length; i++){
			if(points[i].getX() < minx){
				minx = points[i].getX();
			}
		}
		*/
		return boundingBox.getLeft();//minx;
	}

	/** returns the minimum y value in a stroke
	 * return minimum y value in a stroke
	 */
	this.getMinY.SRL_Stroke = function() {
		/*
		var miny = this.getFirstPoint().getY();
		for(var i=0; i<points.length; i++){
			if(points[i].getY() < miny){
				miny = points[i].getY();
			}
		}
		*/
		return boundingBox.getTop();//miny;
	}

	/** returns the maximum x value in a stroke
	 * return maximum x value in a stroke
	 */
	this.getMaxX.SRL_Stroke = function() {
		/*
	
		var maxx = this.getFirstPoint().getX();
		for(var i=0; i<points.length; i++){
			if(points[i].getX() > maxx){
				maxx = points[i].getX();
			}
		}
		*/
		return boundingBox.getRight();//maxx;
	}

	/** returns the maximum x value in a stroke
	 * return maximum x value in a stroke
	 */
	this.getMaxY.SRL_Stroke = function() {
		/*
		var maxy = this.getFirstPoint().getY();
		for(var i=0; i<points.length; i++){
			if(points[i].getY() > maxy){
				maxy = points[i].getY();
			}
		}
		*/
		return boundingBox.getBottom();//maxy;
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
		throw 'unspoorted function call: "getTotalTime"';
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
		throw 'unspoorted function call: "removeTimeDuplicates"';
		var points = new Array();
		for(var i=0; i<points.length; i++){
			if(points.length > 0){
				/*
				if(points[points.size()-1].getTime() == p.getTime()){
					continue;
				}
				//*/
			}
			points.push(points[i]);
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
		throw 'unspoorted function call: "getMaximumSpeed"';
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
	this.rotationAtAPoint = function(startP){
		if (points[0] instanceof SRL_Point && typeof startP === "number") {
			if(points.length < startP+2){
				return Number.NaN;
			}
			var mx = points.get[startP+1].getX() - points.get[startP].getX();
			var my = points.get[startP+1].getY() - points.get[startP].getY();
		    return Math.atan2(my, mx);
		} else {
			throw 'and error occured! (probably because the argument was not a number)';
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
		for (var i=0; i<points.length; i++) {
			points[i].temp_print();
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
	this.Inherits(SRL_Shape);

	/**
	 * Points can have pressure depending on the input device
	 */
	var pressure = 1;
	/**
	 * Points can have size depending on the input device
	 */
	var size = 1;
	/**
	 * Gives the instantaneous speed calculated from this and the previous point.
	 */
	var speed = 0;
	/**
	 * Computes the thickness for the stroke based off of a number of factors.
	 *
	 * Used to create more natural lines that vary in thickness.
	 */
	var thickness = 0;
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
		return pressure;
	}

	/**
	 * Points can have pressure depending on the input device
	 * @param pressure
	 */
	this.setPressure = function(pressure) {
		if (typeof pressure === "number") {
			pressure = pressure;
		} else {
			throw "argument of .setPressure must be a 'number'";
		}
	}

	/**
	 * Points can have pressure depending on the input device
	 * @param pressure
	 */
	this.setSize = function(size) {
		if (typeof size === "number") {
			size = size;
		} else {
			throw "argument of .setPressure must be a 'number'";
		}
	}

	this.getSize = function() {
		return size;
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

	this.setSpeed = function(point) {
		if (point instanceof SRL_Point) {
			var distance = this.distance(point.getX(), point.getY());
			var timeDiff = point.getTime() - this.getTime();
			if (timeDiff == 0) {
				return false;
			}
			speed = distance / timeDiff;
			return true;
		}
	}

	this.getSpeed = function() {
		return speed;
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
 **************************************************************
 *
 *
 * SRL_Interpretation SRL_Library
 * @author Daniel Tan
 * 
 *
 *
 **************************************************************
 */

function SRL_Interpretation(label, confidence, complexity) {
	this.label = label;
	this.confidence = confidence;
	this.complexity = complexity;
}

/**
 **************************************************************
 *
 *
 * SRL_BoundingBox SRL_Library
 * @author Daniel Tan
 * 
 *
 *
 **************************************************************
 */
function SRL_BoundingBox() {
	var internalLeft, internalRight, internalTop, internalBottom;
	var internalX, internalY, internalWidth, internalHeight;

	var firstCoordinate = true;
	var firstIndex, lastIndex;
	/**
	 * @see SRL_BoundingBox#addCoordinate(x,y)
	 */
	this.addPoint = function(point) {
		this.addCoordinate(point.getX(), point.getY());
	}

	/**
	 * expands the box if the given coordinate is outside of the current bounds;
	 */
	this.addCoordinate = function(newX, newY) {
		if (firstCoordinate) {
			internalLeft = internalRight = newX;
			internalTop = internalBottom = newY;
			firstCoordinate = false;
		}

		internalLeft = Math.min(newX, internalLeft);
		internalRight = Math.max(newX, internalRight);
		internalTop = Math.min(newY,	internalTop);
		internalBottom = Math.max(newY, internalBottom);
		sync();
	}

	this.setIndexes = function(firstI, lastI) {
		firstIndex = firstI;
		lastIndex = lastI;
	}

	/**
	 * @see SRL_BoundingBox#containsCoordinate(x,y)
	 */
	this.containsPoint = function(point) {
		return this.containsCoordinate(point.getX(), point.getY());
	}

	/**
	 * Returns true if the bounding box contains the given point, false otherwise.
	 *
	 * containment is detirmined by being in or on the border of the bounding box.
	 */
	this.containsCoordinate = function(checkX, checkY) {
		return internalLeft <= checkX && checkX <= internalRight &&
				internalTop <= checkY && checkY <= internalBottom;
	}

	this.union = function(other) {
		var extremes = other.getExtremeValues();
		if (firstCoordinate) {
			internalLeft = internalRight = extremes.left;
			internalTop = internalBottom = extremes.top;
			firstCoordinate = false;
		}

		internalLeft = Math.min(extremes.left, internalLeft);
		internalRight = Math.max(extremes.right, internalRight);
		internalTop = Math.min(extremes.top, internalTop);
		internalBottom = Math.max(extremes.bottom, internalBottom);
		sync();
	}

	/**
	 * Moves every egdge of the bounding box away from the center by this many pixels.
	 */
	this.scale = function(pixels) {
		internalLeft -= pixels;
		internalRight += pixels;
		internalTop -= pixels;
		internalBottom += pixels;
		sync();
	}
	
	/**
	 * Makes the rectangle coordinates the same as the extreme coordinates.
	 */
	function sync() {
		internalX = internalLeft;
		internalY = internalTop;
		internalWidth = internalRight - internalLeft;
		internalHeight = internalBottom - internalTop;
	}

	this.addSubObject = function(subObject) {
		console.log('combing ');
		console.log(subObject);
		this.union(subObject.getBoundingBox());
	}

	/**
	 * Returns the extreme values that make up this {@code SRL_BoundingBox}.
	 *
	 * returns left, right, top, and bottom from this instance.
	 */
	this.getExtremeValues = function() {
		return {
			left : internalLeft,
			right : internalRight,
			top : internalTop,
			bottom : internalBottom
		}
	}

	/**
	 * Returns the extreme values that make up this {@code SRL_BoundingBox}.
	 *
	 * returns left, right, top, and bottom from this instance.
	 */
	this.getRectangle = function() {
		return {
			x : internalX,
			y : internalY,
			width : internalWidth,
			height : internalHeight
		}
	}

	this.getArea = function() {
		return internalWidth * internalHeight;
	}

	this.toString = function() {
		return "SRL_BoundingBox: (" + internalX + ', ' + internalY + ') Width: ' + internalWidth + ' Height: ' + internalHeight; 
	}

}

/**
 **************************************************************
 *
 *
 * SRL_IntersectionHandler SRL_Library
 * @author Daniel Tan
 * 
 *
 *
 **************************************************************
 */

/**
 * Handles the intersection of a stroke or a shape.
 */
function SRL_IntersectionHandler(parentObject) {
// FIXME: change this to a layered level so that there are no more than 10 boxes to be checked at a time.  (yay)

	/**
	 * Holds sub bounding boxes for the stroke.
	 *
	 * They are confined to a certain area to maintain to the size of the stroke and a maximum number of points.
	 * They can also be used to guess certain features to the stroke at that point.
	 *
	 * For example a box with equal width and height means that the line is appromately a square.
	 * An area smaller than the maximum area means the points are very dense.
	 * A small number of points means the density is very low.
	 */
	var subBounds = new Array();
	var currentBounds = new SRL_BoundingBox();
	var currentBoundsBackup = new SRL_BoundingBox();
	var minIndex = 0;
	const MAX_POINTS = 50;
	const MAX_AREA = 100;
	const EXPANDING_CONSTANT = 3;
	var parentObject = parentObject;

	/**
	 * Adds the point and makes sure that every bounding box maintains a certain size and number of points.
	 *
	 * There will be no bounding boxes that are larger than MAX_AREA or has more points than MAX_POINTS
	 */
	this.addPoint = function addPoint(point, index) {
		currentBoundsBackup.addPoint(point);
		if (currentBoundsBackup.getArea() > MAX_AREA || index - minIndex > MAX_POINTS) {	
			currentBounds.setIndexes(minIndex, index);
			currentBounds.addPoint(point);
			currentBounds.scale(EXPANDING_CONSTANT);
			subBounds.push(currentBounds);
			currentBounds = new SRL_BoundingBox();
			currentBoundsBackup = new SRL_BoundingBox();
			minIndex = index;
			currentBoundsBackup.addPoint(point);
		}
		currentBounds.addPoint(point);
	}

	/**
	 * Tells the handler that we are no longer adding any more items and to finish off the list.
	 */
	this.finish = function() {
		if (currentBounds) {
			currentBounds.scale(EXPANDING_CONSTANT);
			subBounds.push(currentBounds);
		}
	}
	
	this.addSubObject = function addSubObject(object, index) {
		subBounds.add(object.getBoundingBox());
		object.getBoundingBox().setIndexes(index, index);
	}

	this.isIntersecting = function(x,y) {
		if (!parentObject.getBoundingBox().containsCoordinate(x,y)) {
			return false;
		}

		for(var i = 0; i < subBounds.length; i++) {
			var box = subBounds[i];
			if (box.containsCoordinate(x,y)) {
				return true; // fixme: add a queue so that all of the items are checked in the queue at once.
			}
		}
	}

	function isIntersectingStroke(x,y) {
	}

	function isIntersectingShape(x,y) {
	}

	this.getSubBounds = function() {
		return subBounds;
	}
	
	//checking!
	//check every boudning box.  have a queue for every intersection.  If an intersection is closer than X choose than one instead.
}

/**
 **************************************************************
 *
 *
 * Inheritence SRL_Library
 * @author Daniel Tan
 * 
 *
 *
 **************************************************************
 */

SRL_Sketch.Inherits(Overloads);
SRL_Object.Inherits(Overloads);
SRL_Shape.Inherits(SRL_Object);
SRL_Stroke.Inherits(SRL_Shape);
SRL_Point.Inherits(SRL_Shape);