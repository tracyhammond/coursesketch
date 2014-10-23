// requires protobuf and connection library

/**
 * A class that allows a user to register.
 */
function RegisterSystem() {
    var connection = undefined;
    var shadowRoot = undefined;
    var successLoginCallback = undefined;
    var formSubmitFunction = undefined;
    var cancelCallback = undefined;

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
                if (isUndefined(connection)) {
                    // if this became undefined then we should stop trying to connect.
                    throw "this connection object is no longer valid";
                }
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
        setupCallbacks();
    };

    /**
     * Sets up what happens upon the server return the result of attempting to
     * login.
     */
    function setupLoginScript() {
        function onLogin(evt, message) {
            var userId = undefined;
            var isInstructor = undefined;
            if (!isUndefined(message.getLogin) && !isUndefined(message.getLogin())) {
                if (message.login && message.login.isLoggedIn) {
                    // successful login here
                    isInstructor = message.login.isInstructor;
                    userId = message.login.userId;
                }
            } else {
                if (message.otherData) {
                    var loginInfo = CourseSketch.PROTOBUF_UTIL.getLoginInformationClass().decode(message.otherData);
                    console.log(loginInfo);
                    if (loginInfo.isLoggedIn) {
                        console.log("successfully login!");
                        // successful login here
                        isInstructor = loginInfo.isInstructor;
                        userId = loginInfo.userId;
                    }
                }
            }

            if (!isUndefined(userId) && !isUndefined(isInstructor)) {
                console.log("Sucessful login!!!");
                // successful login here
                makeValueReadOnly(connection, 'isInstructor', isInstructor);
                makeValueReadOnly(connection, 'userId', userId);

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
     * @Method the function used for submitting register information.
     * Also the only difference between login.js and register.js
     */
    function formSubmit() {
        function sendLogin(arg1, arg2, email , isInstructor) {
            if (!connection.isConnected()) {
                alert("You are unable to login at the moment. Please be sure to VPN / connected to tamulink or that you are using"
                        + " \n the newest version of chrome. If you are still unable to login please email"
                        + " \n server@coursesketch.com with your device, and web browser");
                return;
            }
            var loginInfo = CourseSketch.PROTOBUF_UTIL.LoginInformation();

            loginInfo.username = arg1;
            loginInfo.password = "" + arg2;
            loginInfo.email = email;
            loginInfo.isRegistering = true;
            var request = CourseSketch.PROTOBUF_UTIL.Request();

            if (!isUndefined(request.setLogin)) {
                request.login = loginInfo;
            }

            request.otherData = loginInfo.toArrayBuffer();
            connection.sendRequest(request);
            console.log("Sending register information");
        }

        var p1 = shadowRoot.querySelector("#password1").value;
        var p2 = shadowRoot.querySelector("#password2").value;
        if (p1 != p2) {
            alert("The passwords must match");
            return;
        }
        sendLogin(shadowRoot.querySelector("#username").value, CryptoJS.SHA3(p1),
                document.getElementById("email").value, document.getElementById("myonoffswitch").checked);
    }

    /**
     * Sets up the form action when the submit button is pressed.
     */
    function setupFormScript() {
        formSubmitFunction = formSubmit;

        var formElement = shadowRoot.querySelector("#submitForm");

        formElement.action = "Javascript:(function() { document.querySelector('login-system').getFormSubmitFunction()();})()";
    }

    /**
     * @Method
     * Setups up the callback for the register button and the lost password button.
     */
    function setupCallbacks() {
        shadowRoot.querySelector("#cancel").onclick = function() {
            if (cancelCallback) {
                cancelCallback();
            }
        };
    }
    /**
     * @Method The callback is called with one parameter.
     * @callbackParam {Connection} An instance of the connection object object.
     */
    this.setOnSuccessLogin = function(callback) {
        successLoginCallback = callback;
    }

    this.getFormSubmitFunction = function() {
        return formSubmitFunction;
    }

    /**
     * @Method
     * The callback is called when the register button is pressed.
     */
    this.setCancelCallback = function(callback) {
        cancelCallback = callback;
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

RegisterSystem.prototype = Object.create(HTMLElement.prototype);
