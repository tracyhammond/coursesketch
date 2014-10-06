
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
function SRL_IntersectionHandler(parentObjectHandler) {
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
	var parentObject = parentObjectHandler;

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
	};

	/**
	 * Tells the handler that we are no longer adding any more items and to finish off the list.
	 */
	this.finish = function() {
		if (currentBounds) {
			currentBounds.scale(EXPANDING_CONSTANT);
			subBounds.push(currentBounds);
		}
	};
	
	this.addSubObject = function addSubObject(object, index) {
		subBounds.add(object.getBoundingBox());
		object.getBoundingBox().setIndexes(index, index);
	};

	this.isIntersecting = function(x,y) {
		if (!parentObject.getBoundingBox().containsCoordinate(x,y)) {
			return false;
		}

		for (var i = 0; i < subBounds.length; i++) {
			var box = subBounds[i];
			if (box.containsCoordinate(x,y)) {
				return true; // fixme: add a queue so that all of the items are checked in the queue at once.
			};
		};
	};

	function isIntersectingStroke(x,y) {
		throw "isIntersectingStroke is not yet implemented";
	}

	function isIntersectingShape(x,y) {
		throw "isIntersectingShape is not yet implemented";
	}

	this.getSubBounds = function() {
		return subBounds;
	};

	//checking!
	//check every boudning box.  have a queue for every intersection.  If an intersection is closer than X choose than one instead.
}
