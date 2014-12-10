/* depends on base.js */

/**
 **************************************************************
 *
 * Placement utility Functions
 * @author gigemjt
 * 
 **************************************************************
 */

/**
 * Takes the height and width of this element and expands it to fill the size of
 * the screen.
 */
if (isUndefined(fillScreen)) {
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

/**
 * Takes the height of this element and expands it to fill the size of the
 * screen.
 */
if (isUndefined(fillHeight)) {
	function fillHeight(scope, id) {
		var height = scope.window.innerHeight - 4;
		var element = scope.document.getElementById(id);
		element.height = height - element.offsetTop;
		element.style.height = element.height + "px";
	}
}

/**
 * Takes the width of this element and expands it to fill the size of the
 * screen.
 */
if (isUndefined(fillWidth)) {
	function fillWidth(scope, id) {
		var width = scope.window.innerWidth - 4;
		var element = scope.document.getElementById(id);
		element.width = width - element.offsetLeft;
		element.style.width = element.width;
	}
}

/**
 * Sets the height of the iFrame to the height of the content.
 */
if (isUndefined(setHeightToContent)) {
	function setHeightToContent(scope, iframeId, contentId, offset) {
		if (!offset) {
			offset = 0;
		}
		var frameScope = scope.document.getElementById(iframeId).contentWindow.document;
		var iFrame = scope.document.getElementById(iframeId);

		var totalHeight = frameScope.getElementById(contentId).offsetHeight
				+ offset;
		iFrame.height = totalHeight + 1;
		iFrame.style.height = iFrame.height;
	}
}

/**
 * Sets the height of the iFrame to the height of the content.
 */
if (isUndefined(setWidthToContent)) {
	function setWidthToContent(scope, iframeId, contentId, offset) {
		if (!offset) {
			offset = 0;
		}
		var frameScope = scope.document.getElementById(iframeId).contentWindow.document;
		var iFrame = scope.document.getElementById(iframeId);
		var totalWidth = frameScope.getElementById(contentId).offsetWidth
				+ offset;
		iFrame.width = totalWidth + 1;
		iFrame.style.width = iFrame.width;
	}
}

/**
 * Sets the height of the iFrame to the height of the content.
 */
if (isUndefined(setSizeToContent)) {
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

/**
 * Returns the iframe given the scope and the iframeId.
 */
if (isUndefined(getIframeScope)) {
	function getIframeScope(scope, iframeId) {
		return scope.getElementById(iframeId).contentWindow.document;
	}
}

/**
 * Makes an element fullscreen.
 */
if (isUndefined(makeFullScreen)) {
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