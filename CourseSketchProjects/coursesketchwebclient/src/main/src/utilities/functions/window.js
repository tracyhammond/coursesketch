/* depends on base.js */
// jshint undef:false
// jshint latedef:false
/* eslint-disable valid-jsdoc */

/**
 **************************************************************
 *
 * Placement utility Functions
 * @author gigemjt
 *
 **************************************************************
 */


if (isUndefined(fillScreen)) {
    /**
     * Takes the height and width of this element and expands it to fill the size of the screen.
     */
    function fillScreen(scope, id) {
        var height = scope.window.innerHeight - 4;
        var width = scope.window.innerWidth - 4;
        var element = scope.document.getElementById(id);
        element.height = height - element.offsetTop;
        element.width = width - element.offsetLeft;
        element.style.width = element.width;
        element.style.height = element.height;
    }
}


if (isUndefined(fillHeight)) {
    /**
     * Takes the height of this element and expands it to fill the size of the screen.
     */
    function fillHeight(scope, id) {
        var height = scope.window.innerHeight - 4;
        var element = scope.document.getElementById(id);
        element.height = height - element.offsetTop;
        element.style.height = element.height + 'px';
    }
}


if (isUndefined(fillWidth)) {
    /**
     * Takes the width of this element and expands it to fill the size of the screen.
     */
    function fillWidth(scope, id) {
        var width = scope.window.innerWidth - 4;
        var element = scope.document.getElementById(id);
        element.width = width - element.offsetLeft;
        element.style.width = element.width;
    }
}


if (isUndefined(setHeightToContent)) {
    /**
     * Sets the height of the iFrame to the height of the content.
     */
    function setHeightToContent(scope, iframeId, contentId, offset) {
        if (!offset) {
            offset = 0;
        }
        var frameScope = scope.document.getElementById(iframeId).contentWindow.document;
        var iFrame = scope.document.getElementById(iframeId);

        var totalHeight = frameScope.getElementById(contentId).offsetHeight +
                offset;
        iFrame.height = totalHeight + 1;
        iFrame.style.height = iFrame.height;
    }
}


if (isUndefined(setWidthToContent)) {
    /**
     * Sets the width of the iFrame to the width of the content.
     */
    function setWidthToContent(scope, iframeId, contentId, offset) {
        if (!offset) {
            offset = 0;
        }
        var frameScope = scope.document.getElementById(iframeId).contentWindow.document;
        var iFrame = scope.document.getElementById(iframeId);
        var totalWidth = frameScope.getElementById(contentId).offsetWidth +
                offset;
        iFrame.width = totalWidth + 1;
        iFrame.style.width = iFrame.width;
    }
}


if (isUndefined(setSizeToContent)) {
    /**
     * Sets the size of the iFrame to the size of the content.
     */
    function setSizeToContent(scope, iframeId, contentId, offsetX, offsetY) {
        if (!offsetX) {
            offsetX = 0;
        }
        if (!offsetY) {
            offsetY = 0;
        }
        var frameScope = scope.document.getElementById(iframeId).contentWindow.document;
        var iFrame = scope.document.getElementById(iframeId);
        var scopedValue = frameScope.getElementById(contentId);

        var totalWidth = scopedValue.offsetWidth + offsetX;
        iFrame.width = totalWidth + 1;
        iFrame.style.width = iFrame.width;

        var totalHeight = scopedValue.offsetHeight + offsetY;
        iFrame.height = totalHeight + 1;
        iFrame.style.height = iFrame.height;
    }
}


if (isUndefined(getIframeScope)) {
    /**
     * Returns the iframe given the scope and the iframeId.
     */
    function getIframeScope(scope, iframeId) {
        return scope.getElementById(iframeId).contentWindow.document;
    }
}

if (isUndefined(makeFullScreen)) {
    /**
     * Makes an element fullscreen.
     */
    function makeFullScreen(element) {
        if (elem.requestFullscreen) {
            elem.requestFullscreen();
        } else if (elem.msRequestFullscreen) {
            elem.msRequestFullscreen();
        } else if (elem.mozRequestFullScreen) {
            elem.mozRequestFullScreen();
        } else if (elem.webkitRequestFullscreen) {
            elem.webkitRequestFullscreen();
        }
    }
}

if (isUndefined(makeNotFullScreen)) {
    /**
     * Stops the element from being full screen.
     *
     * @param {Element} element - The element that full screen is being removed from.
     */
    function makeNotFullScreen(element) {
        if (element.cancelFullscreen) {
            element.cancelFullscreen();
        } else if (element.exitFullscreen) {
            element.exitFullscreen();
        } else if (element.webkitExitFullscreen) {
            element.webkitExitFullscreen();
        }
    }
}
