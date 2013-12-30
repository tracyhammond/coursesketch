function addScrollEvent(element, eventFunction) {
	var handleFunc = function(e) {
		var delta = getScrollDelta(e);
		eventFunction(delta, e); //pass the event just in case people are interested.
	};
	if (element.addEventListener) {
		// IE9, Chrome, Safari, Opera
		element.addEventListener("mousewheel", handleFunc, false);
		// Firefox
		element.addEventListener("DOMMouseScroll", handleFunc, false);
	}// IE 6/7/8
	else element.attachEvent("onmousewheel", handleFunc);
}

function getScrollDelta(e) {
		// cross-browser wheel delta
		var e = window.event || e; // old IE support
		var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
		return delta;
}