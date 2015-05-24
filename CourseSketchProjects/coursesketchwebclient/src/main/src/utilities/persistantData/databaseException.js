/**
 * An exception that is used to represent problems with the database.
 *
 * @class DatabaseException
 * @extends BaseException
 */
function DatabaseException(message, cause) {
    this.name = 'DatabaseException';
    this.setMessage(message);
    this.message = '';
    this.setCause(cause);
    this.createStackTrace();
}

DatabaseException.prototype = new BaseException();
