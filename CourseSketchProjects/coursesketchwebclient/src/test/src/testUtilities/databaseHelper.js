/**
 * A helper function for testing that waits for the database to be loaded before calling a callback.
 * @param dataManager The database we are waiting to stop.
 * @param callback called when the database is ready.
 */
function waitForDatabase(dataManager, callback) {
    var interval = setInterval(function() {
        if (dataManager.isDatabaseReady()) {
            clearInterval(interval);
            // ACTUAL TEST HERE
            callback();
        } // endif
    }, 20);
}
