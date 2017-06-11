/* jshint camelcase: false */
/* depends on objectAndInheritance.js */
//jscs:disable

/**
 * ******************************
 *
 *
 * Stroke data class
 *
 * @author hammond; Daniel Tan
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 *
 *
 * ******************************
 */

function SRL_Stroke(startPoint) {
    // this.Inherits(SRL_Shape);
    this.superConstructor();
    /**
     * List of points in the stroke.
     */
    var points = [];

    /**
     * Adding another point to the stroke
     *
     * @param point
     */
    this.addPoint = function(point) {
        if (point instanceof SRL_Point) {
            points.push(point);
            this.getBoundingBox().addPoint(point);
        }
    };

    /**
     * Constructor setting the initial point in the stroke
     *
     * @param startPoint
     */
    if (startPoint instanceof SRL_Point) {
        this.addPoint(startPoint);
    }

    /**
     * Gets the complete list of points in the stroke
     *
     * @return list of points in the stroke
     */
    this.getPoints = function() {
        return points;
    };

    this.finish = function() {
    };

    /**
     * Get the i'th point in the stroke The first point has index i = 0
     *
     * @param i
     *            the index of the stroke
     * @return the point at index i
     */
    this.getPoint = function(i) {
        if (typeof i === 'number') {
            if (i >= points.length) {
                return null;
            }
            return points[i];
        }
    };

    /**
     * Goes through every object in this list of objects. (Brute force).
     *
     * @return the object if it exist, returns false otherwise.
     */
    this.getSubObjectById = function(objectId) {
        for (var object in points) {
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
        for (var i = 0; i < points.length; i++) {
            var object = points[i];
            if (object.getId() === objectId) {
                return removeObjectByIndex(points, i);
            }
        }
    };

    /**
     * Given an object, remove this instance of the object.
     */
    this.removeSubObject = function(srlObject) {
        return removeObjectFromArray(points, srlObject);
    };

    /**
     * Gets the list of subobjects
     *
     * @return list of objects that make up this object
     */
    this.getSubObjects = function() {
        return points;
    };

    /**
     * Gets the number of points in the stroke
     *
     * @return number of points in the stroke
     */
    this.getNumPoints = function() {
        return points.length;
    };

    /**
     * Returns the first point in the stroke. if the stroke has no points, it
     * returns null.
     *
     * @return first point in the stroke
     */
    this.getFirstPoint = function() {
        if (points.length === 0) {
            return null;
        }
        return points[0];
    };

    /**
     * Returns the last point in the stroke If the stroke has no points, it
     * returns null.
     *
     * @return last point in the stroke.
     */
    this.getLastPoint = function() {
        if (points.length === 0) {
            return null;
        }
        return points[points.length - 1];
    };

    /**
     * returns the minimum x value in a stroke return minimum x value in a
     * stroke
     */
    this.getMinX.SRL_Stroke = function() {
        /*
         * var minx = this.getFirstPoint().getX(); for(var i=0; i<points.length;
         * i++){ if(points[i].getX() < minx){ minx = points[i].getX(); } }
         */
        return boundingBox.getLeft();// minx;
    };

    /**
     * returns the minimum y value in a stroke return minimum y value in a
     * stroke
     */
    this.getMinY.SRL_Stroke = function() {
        /*
         * var miny = this.getFirstPoint().getY(); for(var i=0; i<points.length;
         * i++){ if(points[i].getY() < miny){ miny = points[i].getY(); } }
         */
        return boundingBox.getTop();// miny;
    };

    /**
     * returns the maximum x value in a stroke return maximum x value in a
     * stroke
     */
    this.getMaxX.SRL_Stroke = function() {
        /*
         *
         * var maxx = this.getFirstPoint().getX(); for(var i=0; i<points.length;
         * i++){ if(points[i].getX() > maxx){ maxx = points[i].getX(); } }
         */
        return boundingBox.getRight();// maxx;
    };

    /**
     * returns the maximum x value in a stroke return maximum x value in a
     * stroke
     */
    this.getMaxY.SRL_Stroke = function() {
        /*
         * var maxy = this.getFirstPoint().getY(); for(var i=0; i<points.length;
         * i++){ if(points[i].getY() > maxy){ maxy = points[i].getY(); } }
         */
        return boundingBox.getBottom();// maxy;
    };

    /**
     * Return the cosine of the starting angle of the stroke This takes the
     * angle between the initial point and the point specified as the
     * secondPoint If there are fewer than that many points, it uses the last
     * point. If there are only 0 or 1 points, this returns NaN. Note that this
     * is also feature 1 of Rubine.
     *
     * @param secondPoint
     *            which number point should be used for the second point
     * @return cosine of the starting angle of the stroke
     */
    this.getStartAngleCosine = function(inputSecondPoint) {
        var secondPoint = inputSecondPoint;
        if (typeof secondPoint === 'number') {
            if (this.getNumPoints() <= 1) return Number.NaN;
            if (this.getNumPoints() <= secondPoint) {
                secondPoint = this.getNumPoints() - 1;
            }

            var xStart, xEnd, yStart, yEnd;
            xStart = this.getPoint(0).getX();
            yStart = this.getPoint(0).getY();
            xEnd = this.getPoint(secondPoint).getX();
            yEnd = this.getPoint(secondPoint).getY();

            if (xStart === xEnd && yStart === yEnd) {
                return Number.NaN;
            }

            var sectionWidth = xEnd - xStart;
            var sectionHeight = yEnd - yStart;
            var hypotenuse = Math.sqrt(sectionWidth * sectionWidth + sectionHeight * sectionHeight);
            return sectionWidth / hypotenuse;
        }
        throw '.getStartAngleCosine needs an int argument';
    };

    /**
     * Return the sine of the starting angle of the stroke This takes the angle
     * between the initial point and the point specified as the secondPoint If
     * there are fewer than that many points, it uses the last point. If there
     * are only 0 or 1 points, this returns NaN. Note that this is also feature
     * 1 of Rubine.
     *
     * @param secondPoint
     *            which number point should be used for the second point
     * @return cosine of the starting angle of the stroke
     */
    this.getStartAngleSine = function(inputSecondPoint) {
        var secondPoint = inputSecondPoint;
        if (typeof secondPoint === 'number') {
            if (this.getNumPoints() <= 1) return Number.NaN;
            if (this.getNumPoints() <= secondPoint) {
                secondPoint = this.getNumPoints() - 1;
            }

            var xStart, xEnd, yStart, yEnd;
            xStart = this.getPoint(0).getX();
            yStart = this.getPoint(0).getY();
            xEnd = this.getPoint(secondPoint).getX();
            yEnd = this.getPoint(secondPoint).getY();

            if (xStart === xEnd && yStart === yEnd) {
                return Number.NaN;
            }

            var sectionWidth = xEnd - xStart;
            var sectionHeight = yEnd - yStart;
            var hypotenuse = Math.sqrt(sectionWidth * sectionWidth + sectionHeight * sectionHeight);
            return sectionHeight / hypotenuse;
        }
    };

    /**
     * Return the Euclidean distance from the starting point to the ending point
     * of the stroke
     *
     * @return the distance between the starting and ending points of the stroke
     */
    this.getEuclideanDistance = function() {
        var x0, xn, y0, yn;
        if (this.getPoints().length === 0) return 0;
        x0 = this.getFirstPoint().getX();
        y0 = this.getFirstPoint().getY();
        xn = this.getLastPoint().getX();
        yn = this.getLastPoint().getY();
        return Math.sqrt(Math.pow(xn - x0, 2) + Math.pow(yn - y0, 2));
    };

    /**
     * Return the cosine of the angle between the start and end point
     *
     * @return cosine of the ending angle
     */
    this.getEndAngleCosine = function() {
        if (this.getNumPoints() <= 1) return Number.NaN;
        if (this.getEuclideanDistance() === 0) return Number.NaN;
        var xDistance = this.getLastPoint().getX() - this.getFirstPoint().getX();
        return xDistance / this.getEuclideanDistance();
    };

    /**
     * Return the cosine of the angle between the start and end point
     *
     * @return cosine of the ending angle
     */
    this.getEndAngleSine = function() {
        if (this.getNumPoints() <= 1) return Number.NaN;
        if (this.getEuclideanDistance() === 0) return Number.NaN;
        var yDistance = this.getLastPoint().getY() - this.getFirstPoint().getY();
        return yDistance / this.getEuclideanDistance();
    };

    /**
     * Returns the length of the stroke, complete with all of its turns
     *
     * @return length of the stroke
     */
    this.getStrokeLength = function() {
        var sum = 0;
        var deltaX, deltaY;
        for (var i = 0; i < this.getPoints().length - 1; i++) {
            deltaX = this.getPoint(i + 1).getX() - this.getPoint(i).getX();
            deltaY = this.getPoint(i + 1).getY() - this.getPoint(i).getY();
            sum += Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
        }
        return sum;
    };

    /**
     * Return the total stroke time
     *
     * @return total time of the stroke
     */
    this.getTotalTime = function() {
        console.log('TODO - need to implement a .getTime()');
        throw 'unspoorted function call: "getTotalTime"';
        // if (this.getPoints().length === 0) return Number.NaN;
        // return this.getLastPoint().getTime()-this.getFirstPoint().getTime();
    };

    /**
     * Auxiliary method used to return a list containing all points but with
     * duplicated (based on time) removed
     *
     * @return list of points with duplicates (based on time) removed
     */
    this.removeTimeDuplicates = function() {
        console.log('TODO - need to implement a .getTime()');
        throw 'unspoorted function call: "removeTimeDuplicates"';
        /*
        var points = [];
        for (var i = 0; i < points.length; i++) {
            if (points.length > 0) {
                if(points[points.size()-1].getTime() === p.getTime()){
                continue; } //
            }
            points.push(points[i]);
        }
        return points;
        */
    };

    /**
     * Auxiliary method used to return a list containing all points but with
     * duplicated (based on X,Y coordinates) removed
     *
     * @return list of points with duplicates (based on coordinates) removed
     */
    this.removeCoordinateDuplicates = function() {
        var p = [];
        var x1, x2, y1, y2;
        p.push(this.getPoint(0));
        for (var i = 0; i < this.getPoints().length - 1; i++) {
            x1 = this.getPoint(i).getX();
            y1 = this.getPoint(i).getY();
            x2 = this.getPoint(i + 1).getX();
            y2 = this.getPoint(i + 1).getY();
            if (x1 !== x2 || y1 !== y2) p.push(this.getPoint(i + 1));
        }
        return p;
    };

    /**
     * Return the maximum stroke speed reached
     *
     * @return maximum stroke speed reached
     */
    this.getMaximumSpeed = function() {
        console.log('TODO - need to implement a .getTime()');
        throw 'unspoorted function call: "getMaximumSpeed"';
        /*
        if (this.getPoints().length === 0) return Number.NaN;
        var max = 0;
        var deltaX, deltaY;
        var deltaT;
        var p = this.removeTimeDuplicates();
        for (var i = 0; i < p.length - 1; i++) {
            deltaX = p[i + 1].getX() - p[i].getX();
            deltaY = p[i + 1].getY() - p[i].getY();
            // deltaT = p.get(i+1).getTime()-p.get(i).getTime();
            var speed = (Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) / Math.pow(deltaT, 2);
            if (speed > max) max = speed;
        }
        return max;
        */
    };

    /**
     * Calculates the rotation from point startP to two points further.
     * Calculates the line between startP and the next point, and then the next
     * two points, and then returns the angle between the two.
     *
     * @param points
     * @param startP
     * @return
     */
    this.rotationAtAPoint = function(startP) {
        if (points[0] instanceof SRL_Point && typeof startP === 'number') {
            if (points.length < startP + 2) {
                return Number.NaN;
            }
            var mx = points.get[startP + 1].getX() - points.get[startP].getX();
            var my = points.get[startP + 1].getY() - points.get[startP].getY();
            return Math.atan2(my, mx);
        }
        throw 'and error occured! (probably because the argument was not a number)';
    };

    /**
     * Return the total rotation of the stroke from start to end points
     *
     * @return total rotation of the stroke
     */
    this.getRotationSum = function() {
        var p = this.removeCoordinateDuplicates();
        var sum = 0;
        var lastrot = Number.NaN;
        for (var i = 1; i < p.length - 2; i++) {
            var rot = this.rotationAtAPoint(p, i);
            if (lastrot === Number.NaN) lastrot = rot;
            while ((i > 0) && (rot - lastrot > Math.PI)) {
                rot = rot - 2 * Math.PI;
            }
            while ((i > 0) && (lastrot - rot > Math.PI)) {
                rot = rot + 2 * Math.PI;
            }
            sum += rot;
        }
        return sum;
    };

    /**
     * Return the absolute rotation of the stroke from start to end points
     *
     * @return total absolute rotation of the stroke
     */
    this.getRotationAbsolute = function() {
        var p = this.removeCoordinateDuplicates();
        var sum = 0;
        var lastrot = Number.NaN;
        for (var i = 1; i < p.length - 2; i++) {
            var rot = this.rotationAtAPoint(p, i);
            if (lastrot === Number.NaN) lastrot = rot;
            while ((i > 0) && (rot - lastrot > Math.PI)) {
                rot = rot - 2 * Math.PI;
            }
            while ((i > 0) && (lastrot - rot > Math.PI)) {
                rot = rot + 2 * Math.PI;
            }
            sum += Math.abs(rot);
        }
        return sum;
    };

    /**
     * Return the squared rotation of the stroke from start to end points
     *
     * @return total squared rotation of the stroke
     */
    this.getRotationSquared = function() {
        var p = this.removeCoordinateDuplicates();
        var sum = 0;
        var lastrot = Number.NaN;
        for (var i = 1; i < p.length - 2; i++) {
            var rot = this.rotationAtAPoint(p, i);
            if (lastrot === Number.NaN) lastrot = rot;
            while ((i > 0) && (rot - lastrot > Math.PI)) {
                rot = rot - 2 * Math.PI;
            }
            while ((i > 0) && (lastrot - rot > Math.PI)) {
                rot = rot + 2 * Math.PI;
            }
            sum += rot * rot;
        }
        return sum;
    };

    this.temp_print = function() {
        for (var i = 0; i < points.length; i++) {
            points[i].temp_print();
        }
    };
}

SRL_Stroke.Inherits(SRL_Shape);
