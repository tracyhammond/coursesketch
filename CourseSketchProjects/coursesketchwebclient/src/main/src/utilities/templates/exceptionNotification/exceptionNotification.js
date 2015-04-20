(function() {
    /**
     * Creates a notification from the exception that is passed in.
     * @param {ProtoException} protoEx is a ProtoException passed is so the contents can be displayed.
     */

    CourseSketch.showShallowException = function notifyMe(protoEx) {
        // Let's check if the browser supports notifications
        if (!('Notification' in window)) {
            alert('This browser does not support desktop notification');
        } else if (Notification.permission === 'granted') {
            // If it's okay let's create a notification
            createShallowNotification(protoEx);
        } else if (Notification.permission !== 'denied') {
            Notification.requestPermission(function(permission) {
            // If the user is okay, let's create a notification
                if (permission === 'granted') {
                    createShallowNotification(protoEx);
                }
            });
        }
    };

    /**
     * Creates a small notification displaying the exception that occurred.
     * User can click on the notification to see the full stack trace or the notification will disappear after 7 seconds.
     * @param {ProtoException} protoEx is a ProtoException passed is so the contents can be displayed.
     */

    function createShallowNotification(protoEx) {
        var imageUrl = 'http://www.spilmanlaw.com/media%20content/media-content/Stock%20Photos/Alert.jpg?width=2218&height=2216&ext=.jpg';
        var notification = new Notification(protoEx.getExceptionType(), {
            body: protoEx.getMssg(),
            icon: imageUrl
        });
        notification.onclick = function(event) {
            console.log(event);
            createDeepNotification(protoEx, CourseSketch.getExceptionParentElement());
        };
        setTimeout(function() {
            notification.close();
        }, 7001);
    }

    /**
     * Creates the element 'exception-notification' and appends it to the parent element.
     * Then calls loadProtoException() to load the StackTrace on 'exception-notification'.
     * @param {ProtoException} protoEx is a ProtoException passed is so the contents can be displayed.
     * @param {Element} parentElement is the element to which the element we create will be appended to.
     */

    function createDeepNotification(protoEx, parentElement) {
        var detailedNotification = document.createElement('exception-notification');
        parentElement.appendChild(detailedNotification);
        detailedNotification.loadProtoException(protoEx);
    }
})();

/**
 * Creates an custom element ExceptionNotification.
 */

function ExceptionNotification() {
    /**
     * @param {Node} templateClone is a clone of the custom HTML Element for the text box.
     * Makes the exit button close the box and enables dragging.
     */

    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        this.createShadowRoot();
        this.shadowRoot.appendChild(templateClone);
        this.shadowRoot.querySelector('#closeButton').onclick = function(event) {
            localScope.parentNode.removeChild(localScope);
        };
    };

    /**
     * Creates a root to which everything else is appended to.
     */

    /**
     * Loads the type of the ProtoException as the title.
     * Loads the message of the ProtoException as the content.
     * Then displays the entire StackTrace of the ProtoException.
     * Lastly displays the cause if it exists.
     * @param {ProtoException} protoEx is a ProtoException passed is so the contents can be displayed.
     */

    this.loadProtoException = function(protoEx) {
        var title = document.createElement('p');
        title.textContent = protoEx.getExceptionType();
        title.className = 'title';
        this.appendChild(title);

        var message = document.createElement('div');
        message.textContent = protoEx.getMssg();
        message.className = 'message';
        this.appendChild(message);

        var stack = document.createElement('div');
        var exceptionStackTrace = protoEx.getStackTrace();
        var stackTraceWithNewLine = '';
        for (var i = 0; i < exceptionStackTrace.length; i++){
            stackTraceWithNewLine += exceptionStackTrace[i] + ' ';
        }
        stack.textContent = stackTraceWithNewLine;
        stack.className = 'stacktrace';
        this.appendChild(stack);

        var cause = document.createElement('div');
        cause.textContent = protoEx.getCause();
        cause.className = 'cause';
        this.appendChild(cause);
    };
}
ExceptionNotification.prototype = Object.create(HTMLDialogElement.prototype);
