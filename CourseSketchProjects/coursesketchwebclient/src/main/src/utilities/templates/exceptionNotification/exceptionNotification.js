(function() {
    /**
     * Creates a notification from the exception that is passed in.
     *
     * @param {ProtoException} protoEx - is a ProtoException passed is so the contents can be displayed.
     */
    CourseSketch.showShallowException = function notifyMe(protoEx) {

        // Let's check if the browser supports notifications
        if (!('Notification' in window)) {
            console.log('this browser does not support desktop notification');
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
     *
     * User can click on the notification to see the full stack trace or the notification will disappear after 7 seconds.
     *
     * @param {ProtoException} protoEx - is a ProtoException passed is so the contents can be displayed.
     */
    function createShallowNotification(protoEx) {
        try {
            var imageUrl = 'http://www.spilmanlaw.com/media%20content/media-content/Stock%20Photos/Alert.jpg?width=2218&height=2216&ext=.jpg';
            var notification = new Notification(protoEx.getExceptionType(), {
                body: protoEx.getMssg(),
                icon: imageUrl
            });

            /**
             * Called when the html5 notification is clicked.
             *
             * @param {Event} event - On Click event.
             */
            notification.onclick = function(event) {
                console.log(event);
                createDeepNotification(protoEx, CourseSketch.getExceptionParentElement());
            };
            setTimeout(function() {
                notification.close();
            }, 5501);
        } catch (exception) {
            console.log('Unable to create exception', protoEx);
        }
    }

    /**
     * Creates the element 'exception-notification' and appends it to the parent element.
     *
     * Then calls loadProtoException() to load the StackTrace on 'exception-notification'.
     *
     * @param {ProtoException} protoEx - is a ProtoException passed is so the contents can be displayed.
     * @param {Element} parentElement - is the element to which the element we create will be appended to.
     */
    function createDeepNotification(protoEx, parentElement) {
        var detailedNotification = document.createElement('exception-notification');
        parentElement.appendChild(detailedNotification);
        detailedNotification.loadProtoException(protoEx);
    }

    if (!window.errorListenerSet) {
        if (isUndefined(CourseSketch.clientException)) {
            /**
             * Handles an exception or error then shows it on the client.
             *
             * @param {BaseException|Error} exception - The exception that was thrown.
             */
            function showClientSideException(exception) {
                console.log(exception);
                var protoException = CourseSketch.prutil.createProtoException(exception);
                createShallowNotification(protoException);
            }

            CourseSketch.clientException = showClientSideException;
        }
        window.addEventListener('error', function(evt) {
            if (evt.error.ignoreError) {
                console.log('just validating a script only ran once!');
                return;
            }
            showClientSideException(evt);
        });
        window.errorListenerSet = true;
    }
})();

/**
 * Creates an custom element ExceptionNotification.
 */
function ExceptionNotification() {
    /**
     * Makes the exit button close the box and enables dragging.
     *
     * @param {Node} templateClone - is a clone of the custom HTML Element for the text box.
     */
    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        this.createShadowRoot();
        this.shadowRoot.appendChild(templateClone);
        var modal_id = $(this.shadowRoot.querySelector('#closeButton')).attr('href');
        var elements = this.shadowRoot.querySelectorAll('#notificationInformation');
        $(elements[0]).modal({
            dismissible: true, // Modal can be dismissed by clicking outside of the modal
            opacity: 0.5, // Opacity of modal background
            inDuration: 300, // Transition in duration
            outDuration: 200, // Transition out duration
            startingTop: '4%', // Starting top style attribute
            endingTop: '10%' // Ending top style attribute
        });
        $(elements[0]).modal('open');
        /**
         * Removes the element when clicked.
         *
         * @param {Event} event - On Click event.
         * @returns {Boolean} false.
         */

        /**
         * Removes the element when clicked.
         *
         * @param {Event} event - On Click event.
         */
        this.shadowRoot.querySelectorAll('#closeButton')[0].onclick = function(event) {
            $(elements[0]).modal('close');
        };
        Waves.attach(this.shadowRoot.querySelector('#closeButton'));
    };

    /**
     * Creates a root to which everything else is appended to.
     *
     * Loads the type of the ProtoException as the title.
     * Loads the message of the ProtoException as the content.
     * Then displays the entire StackTrace of the ProtoException.
     * Lastly displays the cause if it exists.
     *
     * @param {ProtoException} protoEx - is a ProtoException passed is so the contents can be displayed.
     */
    this.loadProtoException = function(protoEx) {
        var header = document.createElement('h4');
        header.className = 'header';

        var title = document.createElement('div');
        title.textContent = protoEx.getExceptionType();
        title.className = 'exceptionTitle';
        header.appendChild(title);

        var message = document.createElement('div');
        message.textContent = protoEx.getMssg();
        message.className = 'exceptionMessage';
        header.appendChild(message);

        this.appendChild(header);

        var stack = document.createElement('div');
        var exceptionStackTrace = protoEx.getStackTrace();
        for (var i = 0; i < exceptionStackTrace.length; i++) {
            var singleTrace = document.createElement('p');
            singleTrace.textContent = exceptionStackTrace[i];
            stack.appendChild(singleTrace);
        }
        stack.className = 'stacktrace';
        this.appendChild(stack);

        var cause = document.createElement('div');
        cause.textContent = protoEx.getCause();
        cause.className = 'cause';
        this.appendChild(cause);
    };
}
ExceptionNotification.prototype = Object.create(HTMLDialogElement.prototype);
