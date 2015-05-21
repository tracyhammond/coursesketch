// Requires protobuf and connection library
/*jshint scripturl:true*/
/*jshint quotmark:false*/

/**
 * A class that allows a user to login.
 * @class LoginSystem
 */
function LoginSystem() {
    // note these are shared accross all instances of the login element.
    var connection = undefined;
    var shadowRoot = undefined;
    var successLoginCallback = undefined;
    var formSubmitFunction = undefined;
    var registerCallback = undefined;

    /**
     * @returns {Connection} the connection that was created by this login system.
     */
    this.getConnection = function() {
        return connection;
    };

    /**
     * Creates a new connection object and stores it locally.
     * @param {String} location Url to connect to.
     * @param {Boolean} encrypted True if the connection should occur over ssl
     * @param {Boolean} attemptReconnections True if the connection should be reattempted till success.
     */
    this.createConnection = function(location, encrypted, attemptReconnections) {
        connection = new Connection(location, encrypted, attemptReconnections);
        connection.setOnCloseListener(function(evt, attemptingToReconnect) {
            if (isUndefined(connection)) {
                // If this became undefined then we should stop trying to connect.
                throw 'this connection object is no longer valid';
            }
            if (evt.code === connection.CONNECTION_LOST) {
                if (!attemptingToReconnect) {
                    alert('can not connect to the server');
                }
            } else if (evt.code === connection.SERVER_FULL) {
                if (!attemptingToReconnect) {
                    alert(evt.reason); // Here we can try to connect to other servers.
                }
            } else {
                if (!attemptingToReconnect) {
                    alert('SERVER CLOSED CONNECTION');
                }
            }
        });
        connection.setOnOpenListener(function(evt) {
            // Do something on opening?
            alert('You are now able to log in');
            connected = true;
        });
        connection.reconnect();
    };

    /**
     * @param {Document} document The document in which the node is being imported to.
     * @param {Element} templateClone An element representing the data inside tag,
     *                  its content has already been imported and then added to this element.
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
         * @param {Event} evt the event that caused the successful login
         * @param {Message} message The protobuf message sent from the server.
         */
        function onLogin(evt, message) {
            var userId = undefined;
            var isInstructor = undefined;
            if (!isUndefined(message.getLogin) && !isUndefined(message.getLogin())) {
                if (message.login && message.login.isLoggedIn) {
                    // Successful login here
                    isInstructor = message.login.isInstructor;
                    userId = message.login.userId;
                }
            } else {
                if (message.otherData) {
                    var loginInfo = CourseSketch.PROTOBUF_UTIL.getLoginInformationClass().decode(message.otherData);
                    console.log(loginInfo);
                    if (loginInfo.isLoggedIn) {
                        console.log('successfully login!');
                        // Successful login here
                        isInstructor = loginInfo.isInstructor;
                        userId = loginInfo.userId;
                    }
                }
            }

            if (!isUndefined(userId) && !isUndefined(isInstructor)) {
                console.log('Sucessful login!!!');
                // Successful login here
                makeValueReadOnly(connection, 'isInstructor', isInstructor);
                makeValueReadOnly(connection, 'userId', userId);
                makeValueReadOnly(connection, 'userName', shadowRoot.querySelector('#username').value);

                // Remove oneself from being able to respond to login attempts
                connection.setLoginListener(undefined);
                successLoginCallback(connection);
            } else {
                alert('not able to login: ' + message.responseText);
            }
        }
        connection.setLoginListener(onLogin);
    }

    /**
     * @access private
     * @memberof LoginSystem
     * @function formSubmit
     * the function used for submitting login information.
     * Also the only difference between login.js and register.js
     */
    function formSubmit() {
        console.log('Submitting something?');
        /**
         * Called to send the login.
         * @param {String} arg1 username
         * @param {String} arg2 hashed password
         */
        function sendLogin(arg1, arg2) {
            if (!connection.isConnected()) {
                alert('You are unable to login at the moment. Please be sure to VPN / connected to tamulink or that you are using' +
                        ' \n the newest version of chrome. If you are still unable to login please email' +
                        ' \n server@coursesketch.com with your device, and web browser');
                return;
            }
            var loginInfo = CourseSketch.PROTOBUF_UTIL.LoginInformation();

            loginInfo.username = arg1;
            loginInfo.password = '' + arg2;

            var request = CourseSketch.PROTOBUF_UTIL.Request();
            request.setRequestType(CourseSketch.PROTOBUF_UTIL.getRequestClass().MessageType.LOGIN);
            if (!isUndefined(request.setLogin)) {
                request.login = loginInfo;
            }
            request.otherData = loginInfo.toArrayBuffer();
            console.log('Sending login information');
            connection.sendRequest(request);
            console.log('login information sent successfully');
        }
        sendLogin(shadowRoot.querySelector('#username').value, CryptoJS.SHA3(shadowRoot.querySelector('#password').value));
    }

    /**
     * Sets up the form action when the submit button is pressed.
     */
    function setupFormScript() {
        formSubmitFunction = formSubmit;

        var formElement = shadowRoot.querySelector('#submitForm');

        formElement.action = "Javascript:(function() { document.querySelector('login-system').getFormSubmitFunction()();})()";
    }

    /**
     * @function setupCallbacks
     * Setups up the callback for the register button and the lost password button.
     */
    function setupCallbacks() {
        /**
         * Called when the register button is clicked.
         */
        shadowRoot.querySelector('#registerButton').onclick = function() {
            if (registerCallback) {
                registerCallback();
            }
        };
    }

    /**
     * @function setOnSuccessLogin
     * The callback is called with one parameter.
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
     * @function setRegisterCallback
     * The callback is called when the register button is pressed.
     */
    this.setRegisterCallback = function(callback) {
        registerCallback = callback;
    };

    /**
     * Removes all stored variables. so that hopefully most of this object can
     * be garbage collected
     */
    this.finalize = function() {
        connection = undefined;
        shadowRoot = undefined;
        successLoginCallback = undefined;
    };
}

LoginSystem.prototype = Object.create(HTMLElement.prototype);
