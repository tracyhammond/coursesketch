// jshint undef:false
// jshint latedef:false

/*
 * Ironically check to see if a function that checks to see if objects are
 * undefined is undefined
 */
if (typeof isUndefined === 'undefined') {
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
/**
 *
 * Generates an rfc4122 version 4 compliant solution.
 *
 * found at http://stackoverflow.com/a/2117523/2187510 and further improved at
 * http://stackoverflow.com/a/8809472/2187510
 */
if (isUndefined(generateUUID)) {
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
/**
 * Creates a number that represents the current time in milliseconds since jan
 * 1st 1970.
 */
if (isUndefined(createTimeStamp)) {
    // Creates a time stamp every time this method is called.
    function createTimeStamp() {
        return new Date().getTime();
    }
}

/**
 * *************************************************************
 *
 * onLoad utility Functions
 *
 * @author gigemjt
 *
 * *************************************************************
 */

/**
 * Creates a recursive set of functions that are all called onload
 *
 * The scope is the target for the onload
 */
if (isUndefined(addScopedLoadEvent)) {
    function addScopedLoadEvent(scope, func) {
        var oldonload = scope.onload;
        if (typeof scope.onload !== 'function') {
            scope.onload = func;
        } else {
            scope.onload = function() {
                if (oldonload) {
                    oldonload();
                }
                func();
            };
        }
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

/**
 * Checks to see if the given object is a function.
 *
 * returns true if the object is a function.
 */
if (isUndefined(isFunction)) {
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

/**
 * Given a number that could be 1 or 2 digits returns a 2 digit version of that number
 * ex: 1 -> 01, or 15 -> 15
 */
if (isUndefined(make2Digits)) {
    function make2Digits(num) {
        return ('0' + Number(num)).slice(-2);
    }
}

if (isUndefined(getMillitaryFormattedDateTime)) {
    function getMillitaryFormattedDateTime(dateTime) {
        var date = make2Digits(dateTime.getMonth() + 1) + '-' + make2Digits(dateTime.getDate()) + '-' + dateTime.getFullYear();
        var time = make2Digits(dateTime.getHours()) + ':' + make2Digits(dateTime.getMinutes());
        return date + ' ' + time;
    }
}

/**
 * @param dateTime {Date} uses the default Date object in the browser to return
 */
if (isUndefined(getFormattedDateTime)) {
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
    var BaseException = {
        name:           'BaseException',
        /**
         * The level defines how bad it is. level 5 is the okayest exception
         * (with 6+ typically being ignored completely) and level 0 is the worst
         * exception (with <0 being treated as 0).
         */
        level:          5,
        message:        'BaseException Thrown.\n Please subclass this to create a better exception.',
        htmlMessage:    'BaseException Thrown<br> Please subclass this to create a better exception.',
        toString: function() {
            return this.name + ': ' + this.message + (this.specificMessage ? '\n' + this.specificMessage : '');
        },
        setMessage: function(messageValue) {
            this.specificMessage = messageValue;
        }
    };
}

if (isUndefined(getTypeName)) {
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
    function loadJs(src) {
        var head = document.getElementsByTagName('head')[0];
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = src;
        head.appendChild(script);
    }
}

/**
 * Allows the script to continue if it is only being run once otherwise it will throw an exception (that it hides)
 * And prevents further execution of the script.
 */
if (isUndefined(validateFirstRun)) {
    function validateFirstRun(scriptObject) {
        // no var on purpose.
        try {
            scriptBay = scriptBay || {};
        } catch (exception) {
            scriptBay = {};
        }
        if (!isUndefined(scriptBay[scriptObject.src])) {
            var errorEvent = { src: scriptObject.src };
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


/**
 * Allows the script to continue if it is only being run once otherwise it will throw an exception (that it hides)
 * And prevents further execution of the script.
 */
if (isUndefined(validateFirstGlobalRun)) {
    function validateFirstGlobalRun(scriptObject) {
        // no var on purpose.
        try {
            CourseSketch.scriptBay = CourseSketch.scriptBay || {};
        } catch (exception) {
            CourseSketch.scriptBay = {};
        }
        if (!isUndefined(CourseSketch.scriptBay[scriptObject.src])) {
            var errorEvent = { src: scriptObject.src };
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
