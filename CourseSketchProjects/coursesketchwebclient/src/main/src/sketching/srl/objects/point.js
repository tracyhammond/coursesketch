/* Depends on objectAndInheritance.js */
//jscs:disable

/**
 * ******************************
 *
 *
 * Point data class
 *
 * @author hammond; Daniel Tan
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 *
 *
 * ******************************
 */

function SRL_Point(x, y) {
    // this.Inherits(SRL_Shape);
    this.superConstructor();

    /**
     * Points can have pressure depending on the input device
     */
    var pressure = 1;
    /**
     * Points can have size depending on the input device
     */
    var size = 1;
    /**
     * Gives the instantaneous speed calculated from this and the previous
     * point.
     */
    var speed = 0;
    /**
     * Computes the thickness for the stroke based off of a number of factors.
     *
     * Used to create more natural lines that vary in thickness.
     */
    var thickness = 0;
    /**
     * Holds an history list of the x points Purpose is so that we can redo and
     * undo and go back to the original points
     */
    var m_xList = new Array();
    /**
     * Holds a history list of the y points Purpose is so that we can redo and
     * undo and go back to the original points
     */
    var m_yList = new Array();
    /**
     * A counter that keeps track of where you are in the history of points
     */
    var m_currentElement = -1;

    /**
     * Points can have pressure depending on the input device
     *
     * @return the pressure of the point
     */
    this.getPressure = function() {
        return pressure;
    };

    /**
     * Points can have pressure depending on the input device
     *
     * @param pressure
     */
    this.setPressure = function(pointPressure) {
        if (typeof pressure === "number") {
            pressure = pointPressure;
        } else {
            throw "argument of .setPressure must be a 'number'";
        }
    };

    /**
     * Points can have pressure depending on the input device
     *
     * @param pressure
     */
    this.setSize = function(pointSize) {
        if (typeof size === "number") {
            size = pointSize;
        } else {
            throw "argument of .setPressure must be a 'number'";
        }
    };

    this.getSize = function() {
        return size;
    };

    /**
     * Updates the location of the point Also add this point to the history of
     * the points so this can be undone.
     *
     * @param x
     *            the new x location for the point
     * @param y
     *            the new y location for the point
     */
    this.setP = function(x, y) {
        if (typeof x === "number" && typeof y === "number") {
            m_xList.push(x);
            m_yList.push(y);
            m_currentElement = m_xList.length - 1;
        } else {
            throw "arguments of .setP must be 'number'";
        }
    };

    /**
     * Creates a point with the initial points at x,y
     *
     * @param x
     *            the initial x point
     * @param y
     *            the initial y point
     */
    if (x != undefined && y != undefined) {
        this.setP(x, y);
        // addInterpretation("Point", 1, 1);
    } else {
        // do nothing;
    }

    /**
     * Get the current x value of the point
     *
     * @return current x value of the point
     */
    this.getX = function() {
        return m_xList[m_currentElement];
    };

    /**
     * Get the current y value of the point
     *
     * @return current y value of the point
     */
    this.getY = function() {
        return m_yList[m_currentElement];
    };

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
    };

    this.getSpeed = function() {
        return speed;
    };

    this.distance.SRL_Point = function(arg1, arg2, arg3, arg4) {
        /**
         * Return the distance from point rp to this point.
         *
         * @param rp
         *            the other point
         * @return the distance
         */
        if (arg1 instanceof SRL_Point) {
            return this.distance(arg1.getX(), arg1.getY());

            /**
             * Return the distance from the point specified by (x,y) to this
             * point
             *
             * @param x
             *            the x value of the other point
             * @param y
             *            the y value of the other point
             * @return the distance
             */
        } else if (typeof arg1 === "number" && typeof arg2 === "number" && arg3 === undefined && arg4 === undefined) {
            var xdiff = Math.abs(arg1 - this.getX());
            var ydiff = Math.abs(arg2 - this.getY());
            return Math.sqrt(xdiff * xdiff + ydiff * ydiff);

            /**
             * Return the distance from the point specified by (x,y) to this
             * point
             *
             * @param x
             *            the x value of the other point
             * @param y
             *            the y value of the other point
             * @return the distance
             */
        } else if (typeof arg1 === "number" && typeof arg2 === "number" && typeof arg3 === "number" && typeof arg4 === "number") {
            var xdiff = arg1 - arg3;
            var ydiff = arg2 - arg4;
            return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
        } else {
            throw "arguments of .distance are wrong";
        }
    };

    /**
     * Delete the entire point history and use these values as the starting
     * point
     *
     * @param x
     *            new initial x location
     * @param y
     *            new initial y location
     */
    this.setOrigP = function(x, y) {
        if (typeof x === "number" && typeof y === "number") {
            m_xList = [];
            m_yList = [];
            this.setP(x, y);
        } else {
            throw "arguments of .setP must be 'number'";
        }
    };

    /**
     * Remove last point update If there is only one x,y value in the history,
     * then it does nothing Returns the updated shape (this)
     */
    this.undoLastChange = function() {
        if (m_xList.length < 2) {
            return this;
        }
        if (m_yList.length < 2) {
            return this;
        }
        m_xList.pop();
        m_yList.pop();
        m_currentElement -= 1;
        return this;
    };

    /**
     * Get the original value of the point
     *
     * @return a point where getx and gety return the first values that were
     *         added to the history
     */
    this.goBackToInitial = function() {
        if (m_currentElement >= 0) {
            m_currentElement = 0;
        }
        return this;
    };

    /**
     * Get the x value for the first point in the history
     *
     * @return {Number} the first x point.
     */
    this.getInitialX = function() {
        if (m_xList.length === 0) {
            return Number.NaN;
        }
        return m_xList[0];
    };

    /**
     * Get the y value for the first point in the history
     *
     * @return {Number} the first y point.
     */
    this.getInitialY = function() {
        if (m_yList.length === 0) {
            return Number.NaN;
        }
        return m_yList[0];
    };

    /**
     * Just returns the x value with is obviously the same as the min return x
     * value
     */
    this.getMinX.SRL_Point = function() {
        return this.getX();
    };

    /**
     * Just returns the y value with is obviously the same as the min return y
     * value
     */
    this.getMinY.SRL_Point = function() {
        return this.getY();
    };

    /**
     * Just returns the x value with is obviously the same as the max return x
     * value
     */
    this.getMaxX.SRL_Point = function() {
        return this.getX();
    };

    /**
     * Just returns the y value with is obviously the same as the max return y
     * value
     */
    this.getMaxY.SRL_Point = function() {
        return this.getY();
    };

    this.test_functions = function() {
        console.log("testing .getPressure");
        console.log(this.getPressure());

        console.log("testing .setPressure");
        console.log("setting pressure = 2");
        this.setPressure(2);
        console.log(this.getPressure());
        /*
         * // throws an error console.log("setting pressure = \"string\"");
         * test_point.setPressure("string");
         * console.log(test_point.getPressure()); //
         */

        console.log("testing .setP");
        console.log("show status of arrays");
        this.temp_print();
        console.log("adding a point");
        this.setP(10, 10);
        this.temp_print();
        console.log("adding a point");
        this.setP(2, 5);
        this.temp_print();

    };

    this.temp_print = function() {
        console.log("printing m_xList");
        console.log(m_xList);
        console.log("printing m_yList");
        console.log(m_yList);
        console.log("printing m_currentElement");
        console.log(m_currentElement);
    };
};

SRL_Point.Inherits(SRL_Shape);
