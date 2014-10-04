/**
 * @Class Handles the window state.
 * 
 * Right now this is missing a couple of items but ill describe them here.
 * <ol>
 * <li>A way to add and pop off states (you can not undo redo though so dont
 * worry about that).</li>
 * <li>when moving through peer states the most previous state should be
 * replaced</li>
 * <li>each state should probably have a url or template/document id that
 * points to the html code to be displayed</li>
 * <li>when a state is popped off forcibly delete as much as possible to make
 * it easier to reclaim memory</li>
 * <li> States are described through the url. There are a couple ways to do this
 * though.
 * <ul>
 * <li>One is to have a mechanism that unviersally knows how to handle all
 * possible states</li>
 * <li>Another is to have a state map file in json that describes how to handle
 * each state</li>
 * <li>The final way I thought of is to have each level know how to handle the
 * state and then pass the state down to the level below it</li>
 * </ul>
 * </li>
 * </ol>
 */
function Redirector(scope, affectedWindow) {
    var scope = scope;
    var activeState = false;
    /**
     * Sets the hash url to the file location.
     * 
     * This allows us to find the url if something were to happen.
     */
    this.setRedirect = function setRedirect(url) {
        var shortUrl = replaceAll('html', '', url); // Remove html from file
        // ending
        var hashUrl = replaceAll('/', '.', shortUrl);
        var trimmedHash = hashUrl.substring(1, hashUrl.length - 1); // Trim
        // ending
        var replacedHash = ('' + window.location).split('#')[0] + '#' + trimmedHash;
        scope.location.replace(replacedHash);
    }

    function replaceAll(find, replace, str) {
        return str.replace(new RegExp(find, 'g'), replace);
    }

    this.getRedirect = function getRedirect() {
        var starting = scope.location.hash.substring(1);
        var addedSlashes = 'html/' + replaceAll('\\.', '/', starting) + '.html';
        return addedSlashes;
    }

    this.moveWindow = function(url) {
        affectedWindow.src = url;
    }

    /**
     * Redirects the window but does not actually add an event.
     */
    this.changeSourceNoEvent = function(url) {
        var docWindow = affectedWindow.documentWindow || affectedWindow.contentDocument;
        docWindow.location.replace(url); // it allows the url to change
        // without adding an event to the
        // history!
    }
}
