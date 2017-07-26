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
     * Inherits(SubClass, SuperClass);
     *
     * @param {*} Parent - The parent class.
     */
     function inheritsParent(Child, Parent) {
        var localScope = Child;
        localScope.prototype = new Parent();
        localScope.prototype.constructor = localScope;
        if (!isUndefined(Parent.prototype.superConstructor)) {
            var parentConstructor = Parent.prototype.superConstructor;
            var localConstructor = undefined;
            /**
             * Super constructor.
             *
             * @type {Function}
             */
            localConstructor = localScope.prototype.superConstructor = function () {
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
            localScope.prototype.superConstructor = function () {
                if (arguments.length >= 1) {
                    Parent.apply(this, Array.prototype.slice.call(arguments, 0));
                } else {
                    Parent.apply(this);
                }
            };
        }
    };

}