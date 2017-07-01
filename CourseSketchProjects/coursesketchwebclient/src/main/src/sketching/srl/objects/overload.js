/* jshint camelcase: false */
//jscs:disable
/*******************************************************************************
 *
 *
 * Overload data class
 *
 * @author Daniel Tan
 *
 *
 *
 * ******************************
 */


var SRL_ObjectType = "SRL_Object";
var SRL_SketchType = "SRL_Sketch";
var SRL_ShapeType = "SRL_Shape";
var SRL_StrokeType = "SRL_Stroke";
var SRL_PointType = "SRL_Point";

function Overloads() {
    /**
     * This function returns a string with the value of the original object's
     * type (e.g. {@link SRL_Object}, {@link SRL_Shape}, {@link SRL_Point},
     * {@link SRL_Stroke}, {@link SRL_Line})
     *
     * @return a string class_type
     */
    var classType = false;
    this.check_type = function() {
        if (!classType) {
            var class_type = "No Known Type";
            if (this instanceof SRL_Object && this instanceof SRL_Shape && this instanceof SRL_Stroke) {
                class_type = "SRL_Stroke";
                // console.log("I am SRL_Stroke");
            } else if (this instanceof SRL_Object && this instanceof SRL_Shape && this instanceof SRL_Point) {
                class_type = "SRL_Point";
                // console.log("I am SRL_Point");
            } else if (this instanceof SRL_Object && this instanceof SRL_Shape && false && this instanceof SRL_Line) {
                class_type = "SRL_Line";
                // console.log("I am SRL_Line");
            } else if (this instanceof SRL_Object && this instanceof SRL_Shape) {
                class_type = "SRL_Shape";
                // console.log("I am SRL_Shape");
            } else if (this instanceof SRL_Object) {
                class_type = "SRL_Object";
                // console.log("I am SRL_Object");
            } else if (this instanceof SRL_Sketch) {
                class_type = "SRL_Sketch";
                // console.log("I am SRL_Sketch");
            }
            classType = class_type;
        }
        return classType;
    };

    /**
     * Takes the original object's type and returns the overloaded function
     * associated with that object See SRL_Shape.getArea() and
     * SRL_Line.getArea() for more details.
     *
     * @return the return value of the function it calls
     */
    this.getArea = function() {
        if (this.check_type() === "SRL_Shape") {
            return (this.getArea.SRL_Shape.apply(this, arguments));
        } else if (this.check_type() === "SRL_Line") {
            return (this.getArea.SRL_Line.apply(this, arguments));
        }
    };

    /**
     * Takes the original object's type and returns the overloaded function
     * associated with that object See SRL_Stroke.getMinX(), SRL_Line.getMinX()
     * SRL_Point.getMinX() for more details.
     *
     * @return the return value of the function it calls
     */
    this.getMinX = function() {
        if (this.check_type() === "SRL_Stroke") {
            return (this.getMinX.SRL_Stroke.apply(this, arguments));
        } else if (this.check_type() === "SRL_Point") {
            return (this.getMinX.SRL_Point.apply(this, arguments));
        } else if (this.check_type() === "SRL_Line") {
            return (this.getMinX.SRL_Line.apply(this, arguments));
        }
    };

    /**
     * Takes the original object's type and returns the overloaded function
     * associated with that object See SRL_Stroke.getMinY(), SRL_Line.getMinY()
     * SRL_Point.getMinY() for more details.
     *
     * @return the return value of the function it calls
     */
    this.getMinY = function() {
        if (this.check_type() === "SRL_Stroke") {
            return (this.getMinY.SRL_Stroke.apply(this, arguments));
        } else if (this.check_type() === "SRL_Point") {
            return (this.getMinY.SRL_Point.apply(this, arguments));
        } else if (this.check_type() === "SRL_Line") {
            return (this.getMinY.SRL_Line.apply(this, arguments));
        }
    };

    /**
     * Takes the original object's type and returns the overloaded function
     * associated with that object See SRL_Stroke.getMaxX(), SRL_Line.getMaxX()
     * SRL_Point.getMinX() for more details.
     *
     * @return the return value of the function it calls
     */
    this.getMaxX = function() {
        if (this.check_type() === "SRL_Stroke") {
            return (this.getMaxX.SRL_Stroke.apply(this, arguments));
        } else if (this.check_type() === "SRL_Point") {
            return (this.getMaxX.SRL_Point.apply(this, arguments));
        } else if (this.check_type() === "SRL_Line") {
            return (this.getMaxX.SRL_Line.apply(this, arguments));
        }
    };

    /**
     * Takes the original object's type and returns the overloaded function
     * associated with that object See SRL_Stroke.getMaxY(), SRL_Line.getMaxY()
     * SRL_Point.getMaxY() for more details.
     *
     * @return the return value of the function it calls
     */
    this.getMaxY = function() {
        if (this.check_type() === "SRL_Stroke") {
            return (this.getMaxY.SRL_Stroke.apply(this, arguments));
        } else if (this.check_type() === "SRL_Point") {
            return (this.getMaxY.SRL_Point.apply(this, arguments));
        } else if (this.check_type() === "SRL_Line") {
            return (this.getMaxY.SRL_Line.apply(this, arguments));
        }
    };

    /**
     * Takes the original object's type and returns the overloaded function
     * associated with that object See SRL_Point.distance() SRL_Line.distance()
     * for more details.
     *
     * @return the return value of the function it calls
     */
    this.distance = function() {
        if (this.check_type() === "SRL_Point") {
            return (this.distance.SRL_Point.apply(this, arguments));
        } else if (this.check_type() === "SRL_Line") {
            return (this.distance.SRL_Line.apply(this, arguments));
        }
    };

    /**
     * Takes the original object's type and returns the overloaded function
     * associated with that object See SRL_Point.size() SRL_Shape.size() for
     * more details.
     *
     * @return the return value of the function it calls
     */
    this.getSize = function() {
        if (this.check_type() === "SRL_Point") {
            return (this.getSize.SRL_Point.apply(this, arguments));
        } else if (this.check_type() === "SRL_Shape") {
            return (this.getSize.SRL_Shape.apply(this, arguments));
        }
    };

    /**
     * Takes the original object's type and returns the overloaded function
     * associated with that object See SRL_Object.getObjectById(),
     * SRL_Shape.getObjectById(), SRL_Stroke.getObjectById(), and
     * SRL_Point.getObjectById() for more details.
     *
     * @return the return value of the function it calls
     */
    this.getSubObjectById = function() {
        if (this.check_type() === "SRL_Stroke") {
            return (this.getSubObjectById.SRL_Stroke.apply(this, arguments));
        } else if (this.check_type() === "SRL_Sketch") {
            return (this.getSubObjectById.SRL_Sketch.apply(this, arguments));
        } else if (this.check_type() === "SRL_Point") {
            throw 'No such method error: SRL_Point does not have method "getSubObjectById"';
        } else if (this.check_type() === "SRL_Line") {
            throw 'No such method error: SRL_Line does not have method "getSubObjectById"';
        }
        return (this.getSubObjectById.SRL_Object.apply(this, arguments));
    };

    this.removeSubObjectById = function() {
        if (this.check_type() === "SRL_Stroke") {
            return (this.removeSubObjectById.SRL_Stroke.apply(this, arguments));
        } else if (this.check_type() === "SRL_Sketch") {
            return (this.removeSubObjectById.SRL_Sketch.apply(this, arguments));
        } else if (this.check_type() === "SRL_Point") {
            throw 'No such method error: SRL_Point does not have method "removeSubObjectById"';
        } else if (this.check_type() === "SRL_Line") {
            throw 'No such method error: SRL_Line does not have method "removeSubObjectById"';
        }
        return (this.removeSubObjectById.SRL_Object.apply(this, arguments));
    };

    this.removeSubObject = function() {
        if (this.check_type() === "SRL_Stroke") {
            return (this.removeSubObject.SRL_Stroke.apply(this, arguments));
        } else if (this.check_type() === "SRL_Sketch") {
            return (this.removeSubObject.SRL_Sketch.apply(this, arguments));
        } else if (this.check_type() === "SRL_Point") {
            throw 'No such method error: SRL_Point does not have method "removeSubObject"';
        } else if (this.check_type() === "SRL_Line") {
            throw 'No such method error: SRL_Line does not have method "removeSubObject"';
        }
        return (this.removeSubObject.SRL_Object.apply(this, arguments));
    };

    this.getSubObjectAtIndex = function() {
        if (this.check_type() === "SRL_Stroke") {
            return (this.getSubObjectAtIndex.SRL_Stroke.apply(this, arguments));
        } else if (this.check_type() === "SRL_Sketch") {
            return (this.getSubObjectAtIndex.SRL_Sketch.apply(this, arguments));
        } else if (this.check_type() === "SRL_Point") {
            throw 'No such method error: SRL_Point does not have method "removeSubObject"';
        } else if (this.check_type() === "SRL_Line") {
            throw 'No such method error: SRL_Line does not have method "removeSubObject"';
        }
        return (this.getSubObjectAtIndex.SRL_Object.apply(this, arguments));
    };

    this.removeSubObjectAtIndex = function(index) {
        if (this.check_type() === "SRL_Stroke") {
            return (this.removeSubObjectAtIndex.SRL_Stroke.apply(this, arguments));
        } else if (this.check_type() === "SRL_Sketch") {
            return (this.removeSubObjectAtIndex.SRL_Sketch.apply(this, arguments));
        } else if (this.check_type() === "SRL_Point") {
            throw 'No such method error: SRL_Point does not have method "removeSubObject"';
        } else if (this.check_type() === "SRL_Line") {
            throw 'No such method error: SRL_Line does not have method "removeSubObject"';
        }
        return (this.removeSubObjectAtIndex.SRL_Object.apply(this, arguments));
    };
};
