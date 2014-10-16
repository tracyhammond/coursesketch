/*
 * For line use
 */

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
	this.Inherits(SRL_Shape);

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



SRL_Line.Inherits(SRL_Shape);