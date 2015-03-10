// http://stackoverflow.com/a/6158050/2187510
// example simulate(document.getElementById("btn"), "click", { pointerX: 123, pointerY: 321 })

// supported events:
// HTMLEVENTS: load, unload, about, error, select, change, submit, reset, focus, blur, resize, scroll
// MOUSEEVENTS: click, dblclick, mousedown, mouseup, mouseover, mousemove, mouseout
// TouchEvents: touchend, touchstart, touchmove

function extend(destination, source) {
    for ( var property in source)
        destination[property] = source[property];
    return destination;
}

eventMatchers = {
    'HTMLEvents' : /^(?:load|unload|abort|error|select|change|submit|reset|focus|blur|resize|scroll)$/,
    'MouseEvents' : /^(?:click|dblclick|mouse(?:down|up|over|move|out))$/,
    'TouchEvents' : /^(?:touch(?:end|start|move))$/
};

/**
 * The list of default options
 */
defaultOptions = {
    pointerX : 0,
    pointerY : 0,
    button : 0,
    ctrlKey : false,
    altKey : false,
    shiftKey : false,
    metaKey : false,
    bubbles : true,
    cancelable : true
};

/**
 * Simulates an event.
 *
 * Supported events:
 * HTMLEVENTS:
 *              load, unload, about, error, select, change, submit, reset, focus, blur, resize, scroll
 * MOUSEEVENTS:
 *              click, dblclick, mousedown, mouseup, mouseover, mousemove, mouseout
 * TouchEvents:
 *              touchend, touchstart, touchmove
 *
 * @param element {Element}
 *            The element that is affected by the event. It is returned at the
 *            end.
 * @param eventName {String}
 *            The name of the event ex: "click" or "mousedown".  A list of supported events is above.
 * @param inputOptions {Object}
 *            a list of options that extend the default list of
 *            options.
 * @returns The element that the event is simulated on
 */
function simulate(element, eventName, inputOptions) {
    var options = extend(defaultOptions, inputOptions || {});
    var oEvent, eventType = null;

    for ( var name in eventMatchers) {
        if (eventMatchers[name].test(eventName)) {
            eventType = name;
            break;
        }
    }

    if (!eventType) throw new SyntaxError('Only HTMLEvents and MouseEvents interfaces are supported');

    if (document.createEvent) {
        oEvent = document.createEvent(eventType);
        if (eventType == 'HTMLEvents') {
            oEvent.initEvent(eventName, options.bubbles, options.cancelable);
        } else if (eventType == 'MouseEvents') {
            oEvent.initMouseEvent(eventName, options.bubbles, options.cancelable, document.defaultView, options.button, options.pointerX,
                    options.pointerY, options.pointerX, options.pointerY, options.ctrlKey, options.altKey, options.shiftKey, options.metaKey,
                    options.button, element);
        } else if (eventType == 'TouchEvents') {
            oEvent.initTouchEvent(eventName, options.bubbles, options.cancelable, document.defaultView, options.button, options.pointerX,
                    options.pointerY, options.pointerX, options.pointerY, options.ctrlKey, options.altKey, options.shiftKey, options.metaKey,
                    options.button, element);
        }
        element.dispatchEvent(oEvent);
    } else {
        options.clientX = options.pointerX;
        options.clientY = options.pointerY;
        var evt = document.createEventObject();
        oEvent = extend(evt, options);
        element.fireEvent('on' + eventName, oEvent);
    }
    return element;
}
