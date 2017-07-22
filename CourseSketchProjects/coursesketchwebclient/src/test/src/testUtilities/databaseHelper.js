/**
 * A helper function for testing that waits for the coursesketch.util.util to be loaded before calling a callback.
 * @param dataManager The coursesketch.util.util we are waiting to stop.
 * @param callback called when the coursesketch.util.util is ready.
 */
function waitForDatabase(dataManager, callback) {
    var interval = setRealInterval(function() {
        if (dataManager.isDatabaseReady()) {
            clearRealInterval(interval);
            // ACTUAL TEST HERE
            callback();
        } // endif
    }, 20);
}
