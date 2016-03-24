/* jshint camelcase: false */
/* depends on objectAndInheritance.js */
//jscs:disable

/**
 * ******************************
 *
 *
 * Shape data class
 *
 * @author hammond; Daniel Tan
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 *
 *
 * ******************************
 */

function SRL_Shape() {

    // this.Inherits(SRL_Object);
    this.superConstructor();
    /**
     * Gets a list of all of the strokes that make up this object. It searches
     * recursively to get all of the strokes of this object. If it does not have
     * any strokes, the list will be empty.
     *
     * @return {List<Stroke>} a list of all strokes contains in the shape
     */
    this.getRecursiveStrokes = function() {
        var completeList = [];
        console.log("TODO - need to implement a .getRecursiveSubObjectList()");
        throw 'Function not supported: getRecursiveStrokes';
        /*
         * for(SRL_Object o : getRecursiveSubObjectList()){ try {
         * if(this.getClass() == Class.forName("SRL_Stroke")){
         * completeList.add((SRL_Stroke)o); } } catch (ClassNotFoundException e) {
         * e.printStackTrace(); } } //
         *
         * return completeList;
         */
    };

    /**
     * Returns the center x of a shape.
     *
     * @return center x of a shape
     */
    this.getCenterX = function() {
        return (getMinX() + getMaxX()) / 2.0;
    };

    /**
     * Returns the center y of a shape
     *
     * @return center y of a shape
     */
    this.getCenterY = function() {
        return (this.getMinY() + this.getMaxY()) / 2.0;
    };

    /**
     * Returns the width of the object
     *
     * @return the width of the object
     */
    this.getWidth = function() {
        return this.getBoundingBox().getWidth();// getMaxX() - getMinX();
    };

    /**
     * Returns the height of the object
     *
     * @return the height of the object
     */
    this.getHeight = function() {
        return this.bondingBox().getHeight();// getMaxY() - getMinY();
    };

    /**
     * Returns the length times the height See also getLengthOfDiagonal() return
     * area of shape
     */
    this.getArea.SRL_Shape = function() {
        return getHeight() * getWidth();
    };

    /**
     * This returns the length of the diagonal of the bounding box. This might
     * be a better measure of perceptual size than area
     *
     * @return Euclidean distance of bounding box diagonal
     */
    this.getLengthOfDiagonal = function() {
        return Math.sqrt(getHeight() * getHeight() + getWidth() * getWidth());
    };

    /**
     * This function just returns the same thing as the length of the diagonal
     * as it is a good measure of size.
     *
     * @return size of the object.
     */
    this.getSize = function() {
        return getLengthOfDiagonal();
    };

    /**
     * Returns the angle of the diagonal of the bounding box of the shape
     *
     * @return angle of the diagonal of the bounding box of the shape
     */
    this.getBoundingBoxDiagonalAngle = function() {
        return Math.atan(getHeight() / getWidth());
    };
}

SRL_Shape.Inherits(SRL_Object);
