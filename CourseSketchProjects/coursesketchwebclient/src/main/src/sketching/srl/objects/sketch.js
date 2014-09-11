
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
	this.superConstructor();//(Overloads); // super call

	var objectList = new Array();
	var objectIdMap = new Array();
	var boundingBox = new SRL_BoundingBox();
	var objectMap = {};

	this.addObject = function(srlObject) {
		objectList.push(srlObject);
		objectIdMap[srlObject.getId()] = srlObject;
	};

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
	};

	/**
	 * Given an objectId, remove the object. (Slower than if you already have an instance of the object)
	 */
	this.removeSubObjectById = function(objectId) {
		var object = this.getSubObjectById(objectId);
		this.removeSubObject(object);
		return object;
	};

	this.getList = function() {
		return objectList;
	};

	/**
	 * Returns the object based off of its id.
	 */
	this.getSubObjectById = function(objectId) {
		return objectIdMap[objectId];
	};

	this.getSubObjectAtIndex = function(index) {
		return objectList[index];
	};

	this.removeSubObjectAtIndex = function(index) {
		this.removeSubObject(objectList[index]);
	};

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
	};

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
	};

	/**
	 * Resets the sketch to its starting state.
	 * 
	 */
	this.resetSketch = function() {
		objectList = [];
		objectIdMap = [];
		boundingBox = new SRL_BoundingBox();
		objectMap = {};
	};
}

SRL_Sketch.Inherits(Overloads);