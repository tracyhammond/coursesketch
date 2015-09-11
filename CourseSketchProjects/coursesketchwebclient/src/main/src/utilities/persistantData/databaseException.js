/**
 * An exception that is used to represent problems with the database.
 *
 * @class DatabaseException
 * @extends BaseException
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
