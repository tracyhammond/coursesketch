/**
 * An exception that is used to represent problems with the database.
 *
 * @class DatabaseException
 * @extends BaseException
 * @param {String} message - The message to show for the exception.
 * @param {Request | BaseException | String} request - The request associated with the exception.
 * @param {BaseException} [cause] - The cause of the exception.
 */
function DatabaseException(message, request, cause) {

    this.name = 'DatabaseException';
    this.message = '';
    this.setMessage(message);
    this.createStackTrace();

    if (!isUndefined(request) && (typeof request !== 'string')) {
        this.setCause(request);
    } else {
        this.setCause(cause);
    }

    if (typeof request === 'string') {
        this.setMessage(': [' + message  + '] for request [' + request + ']');
    }
}

DatabaseException.prototype = new BaseException();
