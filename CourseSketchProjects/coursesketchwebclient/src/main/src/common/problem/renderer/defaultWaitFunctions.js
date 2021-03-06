/**
 * Creates a default set of wait functions.
 * @param {WaitScreenManager} waitScreenManager Manages the wait screen.
 * @param {Element} percentBarElement Manages a percent bar.
 * @constructor
 */
function DefaultWaiter(waitScreenManager, percentBarElement) {
    var waitingElement = undefined;
    var overlayElement;

    /**
     * @param {Element} element - An element being checked
     * @returns {Boolean} True if the element is valid.
     */
    function isValidElement(element) {
        return !isUndefined(element) && element !== null;
    }

    /**
     * Starts a waiting screen.
     */
    this.startWaiting = function startWaiting() {
        try {
            if (isValidElement(percentBarElement)) {
                percentBarElement.innerHTML = '';
            }
            waitingElement = new WaitScreenManager().setWaitType(WaitScreenManager.TYPE_PERCENT).build();
            addWaitOverlay();
            if (isValidElement(percentBarElement)) {
                percentBarElement.appendChild(waitingElement);
            }
            waitingElement.startWaiting();
            var realWaiting = waitingElement.finishWaiting.bind(waitingElement);

            /**
             * Called when the sketch surface is done loading to remove the overlay.
             */
            waitingElement.finishWaiting = function() {
                realWaiting();
                removeWaitOverlay();
            };
        } catch (exception) {
            console.log(exception);
        }
    };

    /**
     * Ends a waiting screen.
     */
    this.finishWaiting = function finishWaiting() {
        try {
            if (!isUndefined(waitingElement) && waitingElement.isRunning()) {
                waitingElement.finishWaiting();
                removeWaitOverlay();
            }
        } catch (exception) {
            console.log(exception);
        }
    };

    /**
     * Adds a wait overlay, preventing the user from interacting with the page until it is removed.
     */
    function addWaitOverlay() {
        overlayElement = waitScreenManager.buildOverlay(document.querySelector('body'));
        if (isValidElement(overlayElement)) {
            waitScreenManager.buildWaitIcon(overlayElement);
        }
    }

    /**
     * Removes the wait overlay from the DOM if it exists.
     */
    function removeWaitOverlay() {
        try {
            if (isValidElement(overlayElement)) {
                overlayElement.parentNode.removeChild(overlayElement);
            }
        } catch (exception) {
            var newOverlay = document.getElementById(overlayElement.id);
            if (isValidElement(newOverlay)) {
                newOverlay.parentNode.removeChild(newOverlay);
            }
        }
    }
}
