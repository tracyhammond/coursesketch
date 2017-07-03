// http://stackoverflow.com/a/6158050/2187510
// example simulate(document.getElementById("btn"), "click", { pointerX: 123, pointerY: 321 })

// supported events:
// HTMLEVENTS: load, unload, about, error, select, change, submit, reset, focus, blur, resize, scroll
// MOUSEEVENTS: click, dblclick, mousedown, mouseup, mouseover, mousemove, mouseout
// TouchEvents: touchend, touchstart, touchmove

var simulate = (function () {

    /**
     * Creates a new object that extends destination
     * @param destination
     * @param source
     * @returns {{}}
     */
    function merge(destination, source) {
        var newDestination = {};
        for (var destinationProperty in destination)
            newDestination[destinationProperty] = destination[destinationProperty];
        for (var sourceProperty in source)
            newDestination[sourceProperty] = source[sourceProperty];
        return newDestination;
    }

    eventMatchers = {
        'HTMLEvents': /^(?:load|unload|abort|error|select|change|submit|reset|focus|blur|resize|scroll)$/,
        'MouseEvents': /^(?:click|dblclick|mouse(?:down|up|over|move|out))$/,
        'TouchEvents': /^(?:touch(?:end|start|move))$/
    };

    /**
     * The list of default options
     */
    defaultOptions = {
        pointerX: 0,
        pointerY: 0,
        button: 0,
        buttons: 0,
        ctrlKey: false,
        altKey: false,
        shiftKey: false,
        metaKey: false,
        bubbles: true,
        cancelable: true
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
        var options = merge(defaultOptions, inputOptions || {});
        var oEvent, eventType = null;

        for (var name in eventMatchers) {
            if (eventMatchers[name].test(eventName)) {
                eventType = name;
                break;
            }
        }

        if (!eventType) throw new SyntaxError('Only HTMLEvents and MouseEvents interfaces are supported');

        var mouseEventClassSupported = false;
        try {
            var mouseEvent = new MouseEvent('click');
            var fakeElement = document.createElement('div');
            fakeElement.dispatchEvent(mouseEvent);
            mouseEventClassSupported = true; // No need to polyfill
        } catch (e) {
            mouseEventClassSupported = false;
            // Need to polyfill - fall through
        }

        if (mouseEventClassSupported) {
            var mouseOptions = convertOptions(options, element);
            var oEvent = new MouseEvent(eventName, mouseOptions);
            element.dispatchEvent(oEvent);
        } else if (document.createEvent) {
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
            if (options.buttons) {
                oEvent.buttons = options.buttons;
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

    function convertOptions(options, element) {
        return merge(options, {
            screenX: options.pointerX,
            screenY: options.pointerY,
            clientX: options.pointerX,
            clientY: options.pointerY,
            view: window,
            relatedTarget: element
        });
    }

    return simulate;
})();