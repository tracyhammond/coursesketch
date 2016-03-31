/**
 * Created by David Windows on 3/31/2016.
 */

/**
 * Creates an exception comparator for the given exception type.
 *
 * @param {BaseException} exceptionType - The exception that is being expected.
 * @param {String} [message] - A specific message the exception needs to have.
 * @returns {Function} A function that compares the exceptions
 */
function createExceptionComparator(exceptionType, message) {
    return function(error) {
        console.log(error);
        if (isUndefined(error)) {
            return false;
        }
        var sameExceptionType = false;
        if (error instanceof exceptionType) {
            sameExceptionType = true;
        } else if (error instanceof CourseSketch.BaseException && exceptionType instanceof CourseSketch.BaseException) {
            sameExceptionType = error.name === exceptionType.name;
        }

        // if the exception type is not the same deeper checks are not needed.
        if (!sameExceptionType) {
            return false;
        }

        if (!isUndefined(message)) {
            // return what the specific message required is.
            return error.specificMessage === message;
        } else {
            // no need to check deeper the exception types are the same and that is all that matters.
            return true;
        }
    };
}
