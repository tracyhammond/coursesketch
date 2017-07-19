/* eslint-disable require-jsdoc, valid-jsdoc */
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
        throw 'You can not add a callback after finalizing';
    }
    this.asyncCount++;
    return function() {
        this.asyncCount--;
        if (this.asyncCount === 0 && this.finalized) {
            this.callbackHandle();
        }
    }.bind(this);
};

/**
 * Creates a function that is called to decrement the barrier but is only called once.
 *
 * @param {Number} amount - The number of times the result is called before the callback is called.
 */
CallbackBarrier.prototype.getCallbackAmount = function(amount) {
    if (this.finalized) {
        throw 'You can not add a callback after finalizing';
    }
    this.asyncCount = amount;
    return function() {
        this.asyncCount--;
        if (this.asyncCount === 0 && this.finalized) {
            this.callbackHandle();
        }
    }.bind(this);
};

/**
 * Sends in the call that is called when all of the callbacks are created.
 *
 * @param {Function} callback - The callback to be called when the barrier is finished.
 */
CallbackBarrier.prototype.finalize = function(callback) {
    this.callbackHandle = callback;
    this.finalized = true;
    if (this.asyncCount === 0) {
        this.callbackHandle();
    }
};

/**
 * Creates a callback that is triggered once complete is called.
 * <code>
 * <pre>
 *     Example Use:
 *     var b = new Barrier();
 *
 *     b.once(function() {
 *       // do some stuff
 *     });
 *
 *     fs.doSomethingAsync(function () {
 *       b.complete();
 *     });
 * </pre>
 * </code>
 *
 * @param {Function} callback - called after complete is called
 */
CallbackBarrier.prototype.once = function(callback) {
    var result = this.getCallbackAmount(1);
    this.finalize(callback);
    this.complete = result;
};

/**
 * Creates a barrier with the specific amount and a callback.
 * What is returned is the function that is called a number of times before callback is called.
 *
 * @param {Number} amount - The number of times the barrier can be called.
 * @param {Function} callback - called after complete is called
 */
function createBarrier(amount, callback) {
    var barrier = new CallbackBarrier();
    var result = barrier.getCallbackAmount(amount);
    barrier.finalize(callback);
    return result;
}

CallbackBarrier.createBarrier = createBarrier;
