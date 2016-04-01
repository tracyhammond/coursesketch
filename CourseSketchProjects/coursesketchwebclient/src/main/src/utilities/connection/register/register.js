// requires protobuf and connection library
/*jshint scripturl:true*/
/*jshint quotmark:false*/

/**
 * A class that allows a user to register.
 *
 * @class RegisterSystem
 */
function RegisterSystem() {
    var connection = undefined;
    var shadowRoot = undefined;
    var successLoginCallback = undefined;
    var formSubmitFunction = undefined;
    var cancelCallback = undefined;

    /**
     * @returns {Connection} the connection that was created by this login system.
     */
    this.getConnection = function() {
        return connection;
    };

    /**
     * Creates a new connection object and stores it locally.
     *
     * @param {String} location - Url to connect to.
     * @param {Boolean} encrypted - True if the connection should occur over ssl
     * @param {Boolean} attemptReconnections - True if the connection should be reattempted till success.
     */
    this.createConnection = function(location, encrypted, attemptReconnections) {
        connection = new Connection(location, encrypted, attemptReconnections);
        connection.setOnCloseListener(function(evt, attemptingToReconnect) {
            if (evt.code === connection.CONNECTION_LOST) {
                if (isUndefined(connection)) {
                    // if this became undefined then we should stop trying to connect.
                    throw 'this connection object is no longer valid';
                }
                if (!attemptingToReconnect) {
                    console.log('can not connect to the server');
                }
            } else if (evt.code === connection.SERVER_FULL) {
                if (!attemptingToReconnect) {
                    console.log(evt.reason); // Here we can try to connect to other servers.
                }
            } else {
                if (!attemptingToReconnect) {
                    console.log('SERVER CLOSED CONNECTION');
                }
            }
        });
        connection.setOnOpenListener(function(evt) {
            // do something on opening?
            console.log('You are now able to log in');
            connected = true;
        });
        connection.reconnect();
    };

    /**
     * @param {document}document - The document in which the node is being imported to.
     * @param {Element} templateClone - An element representing the data inside tag, its
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
        /**
         * Called when the server responds to an attempt to login.
         *
         * @param {Event} evt - the event that caused the successful login
         * @param {Message} message - The protobuf message sent from the server.
         */
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
                    var loginInfo = CourseSketch.prutil.getLoginInformationClass().decode(message.otherData);
                    console.log(loginInfo);
                    if (loginInfo.isLoggedIn) {
                        console.log('successfully login!');
                        // successful login here
                        isInstructor = loginInfo.isInstructor;
                        userId = loginInfo.userId;
                    }
                }
            }

            if (!isUndefined(userId) && !isUndefined(isInstructor)) {
                console.log('Sucessful login!!!');
                // successful login here
                makeValueReadOnly(connection, 'isInstructor', isInstructor);
                makeValueReadOnly(connection, 'userId', userId);

                // remove oneself from being able to respond to login attempts
                connection.setLoginListener(undefined);
                successLoginCallback(connection);
            } else {
                console.log('not able to login: ' + message.responseText);
            }
        }
        connection.setLoginListener(onLogin);
    }

    /**
     * The function used for submitting register information.
     * Also the only difference between {@code login.js and register.js}
     *
     * @access private
     * @memberof LoginSystem
     * @function formSubmit
     */
    function formSubmit() {
        /**
         * Called to send the login.
         *
         * @param {String} arg1 - username
         * @param {String} arg2 - hashed password
         * @param {String} email - the users Email
         * @param {Boolean} isInstructor - true if the user wants to default to loggin in as an instructor.
         */
        function sendLogin(arg1, arg2, email, isInstructor) {
            if (!connection.isConnected()) {
                console.log('You are unable to login at the moment. Please be sure to VPN / connected to tamulink or that you are using' +
                        ' \n the newest version of chrome. If you are still unable to login please email' +
                        ' \n server@coursesketch.com with your device, and web browser');
                return;
            }
            var loginInfo = CourseSketch.prutil.LoginInformation();

            loginInfo.username = arg1;
            loginInfo.password = '' + arg2;
            loginInfo.email = email;
            loginInfo.isRegistering = true;
            loginInfo.isInstructor = isInstructor;
            var request = CourseSketch.prutil.createRequestFromData(loginInfo, CourseSketch.prutil.getRequestClass().MessageType.LOGIN);

            connection.sendRequest(request);
            console.log('Sending register information');
        }

        var p1 = shadowRoot.querySelector('#password1').value;
        var p2 = shadowRoot.querySelector('#password2').value;
        if (p1 !== p2) {
            console.log('The passwords must match');
            return;
        }
        sendLogin(shadowRoot.querySelector('#username').value, CryptoJS.SHA3(p1),
                shadowRoot.querySelector('#email').value, shadowRoot.querySelector('#myonoffswitch').checked);
    }

    /**
     * Sets up the form action when the submit button is pressed.
     */
    function setupFormScript() {
        formSubmitFunction = formSubmit;

        var formElement = shadowRoot.querySelector('#submitForm');

        formElement.action = "Javascript:(function() { document.querySelector('register-system').getFormSubmitFunction()();})()";
    }

    /**
     * Setups up the callback for the register button and the lost password button.
     *
     * @function setupCallbacks
     */
    function setupCallbacks() {
        /**
         * Called when the cancel button is clicked.
         */
        shadowRoot.querySelector('#cancel').onclick = function() {
            if (cancelCallback) {
                cancelCallback();
            }
        };
    }
    /**
     * The callback is called with one parameter.
     *
     * @param {Function} callback - Called when login is successful.
     * @function setOnSuccessLogin
     * @callbackParam {Connection} An instance of the connection object object.
     */
    this.setOnSuccessLogin = function(callback) {
        successLoginCallback = callback;
    };

    /**
     * @returns {Function} the function that occurs on form submit.
     */
    this.getFormSubmitFunction = function() {
        return formSubmitFunction;
    };

    /**
     * The callback is called when the register button is pressed.
     *
     * @param {Function} callback - Called when registration is canceled.
     * @function setCancelCallback
     */
    this.setCancelCallback = function(callback) {
        cancelCallback = callback;
    };
    /**
     * Removes all stored variables. So that hopefully most of this object can be garbage collected.
     */
    this.finalize = function() {
        connection = undefined;
        shadowRoot = undefined;
        successLoginCallback = undefined;
    };
}

RegisterSystem.prototype = Object.create(HTMLElement.prototype);
