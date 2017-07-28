/* Depends on base.js */
// jshint undef:false
// jshint latedef:false

/**
 * *************************************************************
 *
 * Inheritance Functions
 *
 * @author gigemjt
 *
 * *************************************************************
 */

if (isUndefined(inheritsParent)) {
    /**
     * sets up inheritance for functions
     *
     * inheritsParent(this, SuperClass); // super call inside object AND
     * inheritsParent(SubClass, SuperClass);
     *
     * @param {*} Child - The child class.
     * @param {*} Parent - The parent class.
     */
    function inheritsParent(Child, Parent) {
        var child = Child;
        child.prototype = new Parent();
        child.prototype.constructor = child;
        if (!isUndefined(Parent.prototype.superConstructor)) {
            var parentConstructor = Parent.prototype.superConstructor;
            var localConstructor = undefined;
            /**
             * Super constructor.
             *
             * @type {Function}
             */
            localConstructor = child.prototype.superConstructor = function() {
                // special setting
                this.superConstructor = parentConstructor;
                // console.log('Setting parent constructor' + parent);
                if (arguments.length >= 1) {
                    Parent.apply(this, Array.prototype.slice.call(arguments, 0));
                } else {
                    Parent.apply(this);
                }
                // console.log('Setting back to current constructor' +
                // localConstructor);
                this.superConstructor = localConstructor;
            };
        } else {
            /**
             * SuperConstructor.
             */
            child.prototype.superConstructor = function() {
                if (arguments.length >= 1) {
                    Parent.apply(this, Array.prototype.slice.call(arguments, 0));
                } else {
                    Parent.apply(this);
                }
            };
        }
    }

}
