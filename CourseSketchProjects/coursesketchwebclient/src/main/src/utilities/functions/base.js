// jshint undef:false
// jshint latedef:false


if (typeof isUndefined === 'undefined') {
    /**
     * Returns true if an object is not defined
     * @param {Object} object the object that is being tested.
     * @returns {Boolean} true if the object is not defined.  (Only not defined being null will return false)
     */
    function isUndefined(object) {
        return typeof object === 'undefined';
    }
}

/**
 * *************************************************************
 *
 * Sketch Function
 *
 * @author gigemjt
 *
 * *************************************************************
 */

// jshint bitwise:false
if (isUndefined(generateUUID)) {
    /**
     * Generates an rfc4122 version 4 compliant solution.
     *
     * found at http://stackoverflow.com/a/2117523/2187510 and further improved at
     * http://stackoverflow.com/a/8809472/2187510
     * @returns {String} A unique id.
     */
    function generateUUID() {
        var d = new Date().getTime();
        var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = (d + Math.random() * 16) % 16 | 0;
            d = Math.floor(d / 16);
            return (c === 'x' ? r : (r & 0x7 | 0x8)).toString(16);
        });
        return uuid;
    }
}

// jshint bitwise:true
if (isUndefined(createTimeStamp)) {
    /**
     * Creates a number that represents the current time in milliseconds since jan 1st 1970.
     *
     * @return {Number} milliseconds since jan 1st 1970
     */
    function createTimeStamp() {
        return new Date().getTime();
    }
}

/**
 * *************************************************************
 *
 * Utility utility Functions
 *
 * @author gigemjt
 *
 * *************************************************************
 */

if (isUndefined(isFunction)) {
    /**
     * Checks to see if the given object is a function.
     *
     * @param {*} object Object that is being checked if it is a function.
     * @returns {Boolean} true if the input object is a function, false otherwise.
     */
    function isFunction(object) {
        return typeof (object) === 'function';
    }
}

/**
 * *************************************************************
 *
 * Date Functions
 *
 * @author gigemjt
 *
 * *************************************************************
 */

if (isUndefined(make2Digits)) {
    /**
     * Given a number that could be 1 or 2 digits returns a 2 digit version of that number.
     *
     * ex: 1 -> 01, or 15 -> 15
     * @param {Number} num the number that is needed to be forced into 2 digits.
     */
    function make2Digits(num) {
        return ('0' + Number(num)).slice(-2);
    }
}

if (isUndefined(getMilitaryFormattedDateTime)) {
    /**
     * Returns the date formatted as Military Time.
     *
     * @param {Date} dateTime the date that is being formatted.
     * @returns {String} The formatted result.
     */
    function getMilitaryFormattedDateTime(dateTime) {
        var date = make2Digits(dateTime.getMonth() + 1) + '-' + make2Digits(dateTime.getDate()) + '-' + dateTime.getFullYear();
        var time = make2Digits(dateTime.getHours()) + ':' + make2Digits(dateTime.getMinutes());
        return date + ' ' + time;
    }
}

if (isUndefined(getFormattedDateTime)) {
    /**
     * Creates and return a formatted date time.
     *
     * ex: 01-18-1994 10:50PM
     *
     * @param {Date} dateTime uses the default Date object in the browser to return.
     * @return {String} A formatted date time.
     */
    function getFormattedDateTime(dateTime) {
        var date = make2Digits(dateTime.getMonth() + 1) + '-' + make2Digits(dateTime.getDate()) + '-' + dateTime.getFullYear();
        var hours = dateTime.getHours();
        var timeType = 'AM';
        if (dateTime.getHours() > 12) {
            hours -= 12;
        }
        if (dateTime.getHours() >= 12) {
            timeType = 'PM';
        }
        if (dateTime.getHours() === 0) {
            hours = 12;
        }
        var time = make2Digits(hours) + ':' + make2Digits(dateTime.getMinutes()) + timeType;
        return date + ' ' + time;
    }
}

if (isUndefined(BaseException)) {
    /**
     * @class BaseException
     * Defines the base exception class that can be extended by all other exceptions.
     */
    function BaseException() {
        this.name = 'BaseException';
        /**
         * The level defines how bad it is. level 5 is the okayest exception
         * (with 6+ typically being ignored completely) and level 0 is the worst
         * exception (with <0 being treated as 0).
         */
        this.message = 'BaseException Thrown.\n Please subclass this to create a better exception.';
        this.stackTrace = undefined;
        this.cause = undefined;

        /**
         * @returns {String} A string representation of the exception.
         */
        this.toString = function() {
            return this.name + ': ' + this.message + (this.specificMessage ? '\n' + this.specificMessage : '\n') + this.stackTrace.join('\n\n');
        };

        /**
         * Sets the message of the Exception.
         * @param {messageValue} messageValue
         *          is a string that contains the description
         *          of the the exception that occurred.
         */
        this.setMessage = function(messageValue) {
            this.specificMessage = messageValue;
        };

        /**
         * Used to access the stacktrace of the exception without modifying it.
         * @return {stackTrace} Returns a string that contains the entire stacktrace of the exception.
         */
        this.getStackTrace = function() {
            return this.stackTrace;
        };

        /**
         * Used to log the stacktrace object in BaseException.
         */
        this.printStackTrace = function() {
            console.log(printStackTrace().join('\n\n'));
        };

        /**
         * Assigns the stacktrace object to an existing stacktrace.
         */
        this.createStackTrace = function() {
            this.stackTrace = CourseSketch.printStackTrace();
        };

        /**
         * Sets the cause of baseException to the causeValue passed in.
         * @param {causeValue} causeValue
         *          is the cause of the exception.
         */
        this.setCause = function(causeValue) {
            if (!isUndefined(causeValue)) {
                this.cause = causeValue;
            }
        };

        /**
         *  A getter function used to access the cause of the stacktrace without the risk of manipulating it.
         */
        this.getCause = function() {
            return this.cause;
        };
    }

    if (CourseSketch && isUndefined(CourseSketch.BaseException)) {
        CourseSketch.BaseException = CourseSketch.BaseException;
    }
}

if (isUndefined(getTypeName)) {
    /**
     * Gets the name of the object.  Also gets the name of the constructor if the constructor.
     *
     * @param {*} value The object that we want to get the type of.
     * @returns {String} The name of the type of the object sent in.
     */
    function getTypeName(value) {
        if (value === null) {
            return 'null';
        }
        var t = typeof value;
        switch (t) {
            case 'function':
                /* falls through */
            case 'object':
                if (value.constructor) {
                    if (value.constructor.name) {
                        return value.constructor.name;
                    }
                    // Internet Explorer
                    // Anonymous functions are stringified as follows:
                    // 'function () {}'
                    // => the regex below does not match
                    var match = value.constructor.toString().match(/^function (.+)\(.*$/);
                    if (match) {
                        return match[1];
                    }
                }
                // fallback, for nameless constructors etc.
                return Object.prototype.toString.call(value).match(/^\[object (.+)\]$/)[1];

            default:
                return t;
        }
    }
}

if (isUndefined(loadJs)) {
    /**
     * Loads the javascript file given its src.
     * @param {String} src the address of the script to load.
     */
    function loadJs(src) {
        var head = document.getElementsByTagName('head')[0];
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = src;
        head.appendChild(script);
    }
}

if (isUndefined(validateFirstRun)) {
    /**
     * Allows the script to continue if it is only being run once otherwise it will throw an exception (that it hides)
     * And prevents further execution of the script.  This is limited to the document it is contained in.
     *
     * @param {Script} scriptObject
     *                  the object of the script being run. often it is called as <code>"validateFirstRun(document.currentScript);"</code>
     */
    function validateFirstRun(scriptObject) {
        try {
            // no var on purpose.  (global object)
            scriptBay = scriptBay || {};
        } catch (exception) {
            scriptBay = {};
        }
        if (!isUndefined(scriptBay[scriptObject.src])) {
            var errorEvent = { src: scriptObject.src };
            /**
             * The listener that ignores the event.
             *
             * @param {Event} event the error event that was thrown.
             * @memberof validateFirstRun
             */
            var listener = function(event) {
                if (typeof event.error === 'object' && !isUndefined(event.error.src) && event.error.src === scriptObject.src) {
                    event.preventDefault();event.stopPropagation();
                    window.removeEventListener('error', listener, true);
                }
            };
            window.addEventListener('error', listener, true);
            throw errorEvent;
        }
        scriptBay[scriptObject.src] = {};
    }
}

if (isUndefined(validateFirstGlobalRun)) {
    /**
     * Allows the script to continue if it is only being run once otherwise it will throw an exception (that it hides)
     * And prevents further execution of the script. This is limited to CourseSketch running.
     *
     * @param {Script} scriptObject
     *                  the object of the script being run. often it is called as <code>"validateFirstRun(document.currentScript);"</code>
     */
    function validateFirstGlobalRun(scriptObject) {
        // no var on purpose.
        try {
            CourseSketch.scriptBay = CourseSketch.scriptBay || {};
        } catch (exception) {
            CourseSketch.scriptBay = {};
        }
        if (!isUndefined(CourseSketch.scriptBay[scriptObject.src])) {
            var errorEvent = { src: scriptObject.src };
            /**
             * The listener that ignores the event.
             *
             * @param {Event} event the error event that was thrown.
             * @memberof validateFirstGlobalRun
             */
            var listener = function(event) {
                if (typeof event.error === 'object' && !isUndefined(event.error.src) && event.error.src === scriptObject.src) {
                    event.preventDefault();event.stopPropagation();
                    window.removeEventListener('error', listener, true);
                }
            };
            window.addEventListener('error', listener, true);
            throw errorEvent;
        }
        CourseSketch.scriptBay[scriptObject.src] = {};
        validateFirstRun(scriptObject); // look locally too!
    }
}

if (isUndefined(safeLoad)) {
    /**
     * Loads a script but only once.
     *
     * @param {String} url The url that is being loaded.
     * @param {Object} uniqueGlobalObject a global object that is unique to the script that will be undefined if the script does not exist.
     */
    function safeLoad(url, uniqueGlobalObject) {
        if (typeof window[uniqueGlobalObject] === 'undefined') {
            // load jquery
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.src = url;
            document.getElementsByTagName('head')[0].appendChild(script);
        }
    }
}
