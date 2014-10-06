// requires protobuf and connection library

/**
 * A class that allows a user to login.
 */
function LoginSystem() {
    var connection = undefined;
    var shadowRoot = undefined;
    var successLoginCallback = undefined;
    var formSubmitFunction = undefined;

    /**
     * @returns the connection that was created by this login system.
     */
    this.getConnection = function() {
        return connection;
    };

    this.createConnection = function(location, encrytped, attemptReconnections) {
        connection = new Connection(location, encrytped, attemptReconnections);
        connection.setOnCloseListener(function(evt, attemptingToReconnect) {
            if (evt.code == connection.CONNECTION_LOST) {
                if (!attemptingToReconnect) {
                    alert('can not connect to the server');
                }
            } else if (evt.code == connection.SERVER_FULL) {
                if (!attemptingToReconnect) {
                    alert(evt.reason); // Here we can try to connect to other
                                        // servers.
                }
            } else {
                if (!attemptingToReconnect) {
                    alert("SERVER CLOSED CONNECTION");
                }
            }
        });
        connection.setOnOpenListener(function(evt) {
            // do something on opening?
            alert("You are now able to log in");
            connected = true;
        });
        connection.reconnect();
    };

    /**
     * @param document
     *            {document} The document in which the node is being imported
     *            to.
     * @param templateClone
     *            {Element} an element representing the data inside tag, its
     *            content has already been imported and then added to this
     *            element.
     */
    this.initializeElement = function(document, templateClone) {
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        setupLoginScript();
        setupFormScript();
    };

    /**
     * Sets up what happens upon the server return the result of attempting to
     * login.
     */
    function setupLoginScript() {
        function onLogin(evt, message) {
            var userId = undefined;
            var isInstructor = undefined;
            if (!isUndefined(message.getLogin())) {
                if (message.login && message.login.isLoggedIn) {
                    // successful login here
                    isInstructor = message.login.isInstructor;
                    userId = message.login.userId;
                }
            } else {
                if (message.otherData) {
                    var loginInfo = PROTOBUF_UTILgetLoginClass().decode(message.otherData);
                    if (loginInfo.isLoggedIn) {
                        // successful login here
                        isInstructor = loginInfo.isInstructor;
                        userId = loginInfo.userId;
                    }
                }
            }

            if (!isUndefined(userId) && !isUndefined(isInstructor)) {
                // successful login here
                connection.isInstructor = isInstructor;
                connection.userId = userId;

                // remove oneself from being able to respond to login attempts
                connection.setLoginListener(undefined);
                successLoginCallback(connection);
            } else {
                alert("not able to login: " + message.responseText);
            }
        }
        connection.setLoginListener(onLogin);
    }

    /**
     * Sets up the form action when the submit button is pressed.
     */
    function setupFormScript() {
        function formSubmit() {
            console.log("Submitting something?");
            function sendLogin(arg1, arg2) {
                if (!connection.isConnected()) {
                    alert("You are unable to login at the moment. Please be sure to VPN / connected to tamulink or that you are using"
                            + " \n the newest version of chrome. If you are still unable to login please email"
                            + " \n server@coursesketch.com with your device, and web browser");
                    return;
                }
                var loginInfo = PROTOBUF_UTIL.LoginInformation();

                loginInfo.username = arg1;
                loginInfo.password = "" + arg2;

                var request = PROTOBUF_UTIL.Request();
                request.setRequestType(PROTOBUF_UTIL.getRequestClass().MessageType.LOGIN);
                if (!isUndefined(request.setLogin)) {
                    request.login = loginInfo;
                }
                request.otherData = loginInfo.toArrayBuffer();
                console.log("Sending login information");
                connection.sendRequest(request);
                console.log("login information sent successfully");
            }
            sendLogin(shadowRoot.querySelector("#username").value, CryptoJS.SHA3(shadowRoot.querySelector("#password").value));
        }
        formSubmitFunction = formSubmit;

        var formElement = shadowRoot.querySelector("#loginForm");

        formElement.action = "Javascript:(function() { document.querySelector('login-system').getFormSubmitFunction()();})()";
    }

    /**
     * @Method The clallback is called with one parameter.
     * @callbackParam {Connection} An instance of the connection object object.
     */
    this.setOnSuccessLogin = function(callback) {
        successLoginCallback = callback;
    }

    this.getFormSubmitFunction = function() {
        return formSubmitFunction;
    }

    /**
     * Removes all stored variables. so that hopefully most of this object can
     * be garbe collected
     */
    this.finalize = function() {
        connection = undefined;
        shadowRoot = undefined;
        successLoginCallback = undefined;
    }
}

LoginSystem.prototype = Object.create(HTMLElement.prototype);
