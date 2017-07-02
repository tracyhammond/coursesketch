/* jshint camelcase: false */
/* depends on objectAndInheritance.js */
//jscs:disable

/**
 * ******************************
 *
 *
 * Object data class
 *
 * @author hammond; Daniel Tan
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 *
 *
 * ******************************
 */
function SRL_Object() {
    // this.Inherits(Overloads);
    this.superConstructor();

    /**
     * Each object has a unique ID associated with it.
     */
    var id = generateUUID();

    /**
     * The name of the object, such as "triangle1"
     */
    var name = '';

    /**
     * The creation time of the object.
     */
    var time = -1;

    /**
     * An object can be created by a user (like drawing a shape, or speaking a
     * phrase) or it can be created by a system (like a recognition of a higher
     * level shape)
     */
    var isUserCreated = false;

    /**
     * A list of possible interpretations for an object
     */
    var m_interpretations = [];

    /**
     * Was this object made up from a collection of subObjects? If so they are
     * in this list. This list usually gets filled in through recognition. This
     * list can be examined hierarchically. e.g., an arrow might have three
     * lines inside, and each line might have a stroke.
     */
    var m_subObjects = [];

    /**
     * Contains the bounds of this shape.
     *
     * The bounding box is the farthest left, right, top and bottom points in
     * this shape;
     */
    var boundingBox = new SRL_BoundingBox();

    /**
     * Adds a subobject to this object. This usually happens during recognition,
     * when a new object is made up from one or more objects
     *
     * @param subObject
     */
    this.addSubObject = function(subObject) {
        if (subObject instanceof SRL_Object) {
            boundingBox.addSubObject(subObject);
            m_subObjects.push(subObject);
        }
    };

    /**
     * Goes through every object in this list of objects. (Brute force).
     *
     * @return the object if it exist, returns false otherwise.
     */
    this.getSubObjectById = function(objectId) {
        for (var i = 0; i < m_subObjects.length; i++) {
            var object = m_subObjects[i];
            if (object.getId === objectId) {
                return object;
            }
        }
        return false;
    };

    /**
     * Goes through every object in this list of objects. (Brute force).
     *
     * @return the object if it exist, returns false otherwise.
     */
    this.removeSubObjectById = function(objectId) {
        for (var i = 0; i < m_subObjects.length; i++) {
            var object = m_subObjects[i];
            if (object.getId() === objectId) {
                return removeObjectByIndex(m_subObjects, i);
            }
        }
    };

    /**
     * Given an object, remove this instance of the object.
     */
    this.removeSubObject = function(srlObject) {
        return removeObjectFromArray(m_subObjects, srlObject);
    };

    /**
     * Gets the list of subobjects
     *
     * @return {Array<SRL_Object>} list of objects that make up this object
     */
    this.getSubObjects = function() {
        return m_subObjects;
    };

    /**
     * Gets a list of all of the objects that make up this object. This is a
     * recursive search through all of the subobjects. This objects is also
     * included on the list.
     *
     * @return {List<SrlObject>} a list of objects.
     */
    this.getRecursiveSubObjectList = function() {
        var completeList = [];
        completeList.push(this);
        for (var i = 0; i < m_subObjects.length; i++) {
            for (var j = 0; j < m_subObjects[i].length; j++) {
                completeList.push(m_subObjects[i].getRecursiveSubObjectList()[j]);
            }
        }
        return completeList;
    };

    /**
     * add an interpretation for an object
     *
     * @param interpretation
     *            a string name representing the interpretation
     * @param confidence
     *            a double representing the confidence
     * @param complexity
     *            a double representing the complexity
     */
    this.addInterpretation = function(interpretation, confidence, complexity) {
        m_interpretations.push(new SRL_Interpretation(interpretation, confidence, complexity));
    };

    /**
     * @return the list of interpretations for this shape.
     */
    this.getInterpretations = function() {
        return m_interpretations;
    };

    /**
     * sets unique UUID for an object
     */
    this.setId = function(newId) {
        id = newId;
    };

    /**
     * @return unique UUID for an object
     */
    this.getId = function() {
        return id;
    };

    /**
     * An object can have a name, such as "triangle1".
     *
     * @return the string name of the object
     */
    this.getName = function() {
        return name;
    };

    /**
     * An object can have a name, such as "triangle1".
     *
     * @param name
     *            object name
     */
    this.setName = function(objectName) {
        name = objectName;
    };

    /**
     * Gets the time associated with the object. The default time is the time it
     * was created
     *
     * @return the time the object was created.
     */
    this.getTime = function() {
        return time;
    };

    /**
     * Sets the time the object was created. This probably should only be used
     * when loading in pre-existing objects.
     *
     * @param time
     *            the time the object was created.
     */
    this.setTime = function(inputTime) {
        if (typeof inputTime === "number") {
            time = inputTime;
        } else {
            time = null;
        }
    };

    /**
     * An object can be created by a user (like drawing a shape, or speaking a
     * phrase) or it can be created by a system (like a recognition of a higher
     * level shape) default is false if not explicitly set
     *
     * @return true if a user created the shape
     */
    this.isUserCreated = function() {
        return isUserCreated;
    };

    /**
     * An object can be created by a user (like drawing a shape, or speaking a
     * phrase) or it can be created by a system (like a recognition of a higher
     * level shape)
     *
     * @param isUserCreated
     *            true if the user created the shape, else false
     */
    this.setUserCreated = function(isUserCreatedObject) {
        isUserCreated = isUserCreatedObject;
    };

    /**
     * Gets the bounding box of the object.
     *
     * @return the bounding box of the object
     */
    this.getBoundingBox = function() {
        return boundingBox;
    };

    /**
     * @returns the minimum x value in an object
     */
    this.getMinX = function() {
        return boundingBox.getLeft();// minx;
    };

    /**
     * @return minimum y value in an object
     */
    this.getMinY = function() {
        return boundingBox.getTop();// miny;
    };

    /**
     * @return maximum x value in an object
     */
    this.getMaxX = function() {
        return boundingBox.getRight();// maxx;
    };

    /**
     * @return maximum x value in an object
     */
    this.getMaxY = function() {
        return boundingBox.getBottom();
    };
}

SRL_Object.Inherits(Overloads);
