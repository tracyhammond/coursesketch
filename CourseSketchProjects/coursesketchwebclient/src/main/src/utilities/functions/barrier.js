/**
 * A class that allows callbacks to be a barrier.
 */
function CallbackBarrier() {
    this.callbackHandle = null;
    this.asyncCount = 0;
    this.finalized = false;
}

/**
 * Creates a function that is called to decrement the barrier.
 */
CallbackBarrier.prototype.getCallback = function() {
    if (this.finalized) {
        throw "You can't add a callback after finalizing";
    }
    this.asyncCount++;
    return function(){
        this.asyncCount--;
        if (this.asyncCount === 0 && this.finalized) {
            this.callbackHandle();
        }
    }.bind(this);
};

/**
 * Creates a function that is called to decrement the barrier but is only called once.
 * @param amount {Number} the number of times the result is called before the callback is called.
 */
CallbackBarrier.prototype.getCallbackAmount = function(amount) {
    if (this.finalized) {
        throw "You can't add a callback after finalizing";
    }
    this.asyncCount = amount;
    return function(){
        this.asyncCount--;
        if (this.asyncCount === 0 && this.finalized) {
            this.callbackHandle();
        }
    }.bind(this);
};
/**
 * Sends in the call that is called when all of the callbacks are created.
 */
CallbackBarrier.prototype.finalize = function(callback) {
    this.callbackHandle = callback;
    this.finalized = true;
    if (this.asyncCount === 0) {
        this.callbackHandle();
    }
}

/**
 * Creates a barrier with the specific amount and a callback.
 * What is returned is the function that is called a number of times before callback is called.
 */
function createBarrier(amount, callback) {
    var barrier = new CallbackBarrier();
    var result = barrier.getCallbackAmount(amount);
    barrier.finalize(callback);
    return result;
}
