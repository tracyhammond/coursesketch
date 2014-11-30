function DatabaseException(message, request) {
    if (message) {
        this.message = message;
    }
    if (request) {
        this.request = request;
    }
};

DatabaseException.prototype.message = "Generic database message";
DatabaseException.prototype.request = "Generic request";
DatabaseException.prototype.name = "DatabaseException";
DatabaseException.prototype.toString = function() {return this.name + ": [" + this.message  + "] for request [" + this.request + "]";};
