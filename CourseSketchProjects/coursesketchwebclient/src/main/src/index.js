if (isUndefined(CourseSketch)) {
    makeValueReadOnly(this, 'CourseSketch', {});
}
CourseSketch.connection = false;
CourseSketch.redirector = {};

/**
 * @namespace Index
 */
$(document).ready(
function() {
    /**
     * A local instance of the document object used to maintain the correct instance when potentially called from IFrames.
     * @type {document}
     */
    var localDoc = document;

    /**
     * @returns {Element} The element that encapsulates the exception.
     */
    CourseSketch.getExceptionParentElement = function() {
        return document.body;
    };

    /**
     * Refreshes the page without adding a browser event.
     *
     * @memberof CourseSketch
     */
    CourseSketch.reloadContent = function() {
        var value = CourseSketch.redirector.getRedirect();
        CourseSketch.redirector.changeSourceNoEvent(value);
    };

    /**
     * Changes the page to point at a different location.
     *
     * @param {URL} url - a url that points to a different page
     * @param {String} title - the title that appears on the top of course sketch.
     * @memberof CourseSketch
     */
    CourseSketch.redirectContent = function(url, title) {
        CourseSketch.redirector.changeSourceNoEvent(url);

        /* document.getElementById('iframeContent').src = url; */
        CourseSketch.redirector.setRedirect(url);

        if (title && CourseSketch.headerManager) {
            CourseSketch.headerManager.changeText(title);
        }
    };

    window.addEventListener('beforeunload', function(e) {
        var r = CourseSketch.prutil.Request();
        r.setRequestType(Request.MessageType.CLOSE);
        connection.sendRequest(r);
        return 'you can close this window';
    });

    var element = document.querySelector('#loginLocation');

    /**
     * Called when the user logs in correctly.
     *
     * @param {ConnectionLibrary} loggedInConnection - a valid connection of the user being logged in.
     * @memberof Index
     */
    var successLogin = function(loggedInConnection) {
        CourseSketch.connection = loggedInConnection;
        $(element).empty();
        var importPage = document.createElement('link');
        importPage.rel = 'import';
        importPage.href = '/src/main.html';
        /**
         * Imports {@code main.html}.
         *
         * @memberof Index
         */
        importPage.onload = function() {
            var content = importPage.import;
            document.querySelector('#mainPageContent').appendChild(content.querySelector('#mainPage'));
            loadMenu(content);
            $('#mainPageContent').show();

            CourseSketch.redirector = new Redirector(window, document.querySelector('#iframeContent'));

            loadHomePage();
        };
        localDoc.head.appendChild(importPage);
        element.style.display = 'none';
    };

    /**
     * Creates a login element and a function so that when the register button is clicked the register is created.
     *
     * This is called on the Register element when cancel is pressed.
     * They each call the other when clicked.
     * This forms an infinite loop with {@link Index.createRegister}.
     *
     * @param {Function} register - The createRegister function
     * @param {Function} successLoginCallback - called when the user log ins successfully.
     * @see {@link Index.createRegister}
     * @memberof Index
     */
    function createLogin(register, successLoginCallback) {
        $(element).empty();
        var login = document.createElement('login-system');
        login.setOnSuccessLogin(successLoginCallback);
        login.setRegisterCallback(function() {
            register(createLogin, successLoginCallback);
        });
        element.appendChild(login);
    }

    /**
     * Creates a register element and a function so that when the cancel button is clicked the login is created.
     *
     * This is called on the Login element when register is pressed.
     * They each call the other when clicked.
     * This forms an infinite loop with {@link Index.createLogin}.
     *
     * @param {Function} login - The createLogin function
     * @param {Function} successLoginCallback - called when the user log ins successfully.
     * @see {@link Index.createLogin}
     * @memberof Index
     */
    function createRegister(login, successLoginCallback) {
        $(element).empty();
        var register = document.createElement('register-system');
        register.setOnSuccessLogin(successLoginCallback);
        register.setCancelCallback(function() {
            login(createRegister, successLoginCallback);
        });
        element.appendChild(register);
    }

    /**
     * A public function that creates a login element.
     */
    CourseSketch.createLoginElement = function() {
        createLogin(createRegister, successLogin);
    };

    CourseSketch.createLoginElement();

    /**
     * A public function that is used to display the login element anywhere.
     */
    CourseSketch.createReconnection = function() {
        createLogin(createRegister, CourseSketch.successfulReconnection);
        element.className = 'reconnectLogin';
        element.style.display = 'initial';
    };

    /**
     * Called when a reconnection occurs.
     *
     * @param {Connection} loggedInConnection - The object that handles the connection to the database.
     */
    CourseSketch.successfulReconnection = function(loggedInConnection) {
        console.log('The user relogged in correctly');
        CourseSketch.connection = loggedInConnection;
        CourseSketch.dataListener.setupConnectionListeners();
        $(element).empty();
        element.className = '';

        // Note that this function may be defined dynamically
        CourseSketch.onSuccessfulReconnection();
    };

    /**
     * Creates and loads the menu.
     *
     * @param {Element} importDoc - The link element that contains the menu template.
     * @memberof Index
     */
    function loadMenu(importDoc) {
        var menuToLoad;
        if (CourseSketch.connection.isInstructor) {
            menuToLoad = 'instructorMenu';
        } else {
            menuToLoad = 'studentMenu';
        }
        var clone = safeImport(importDoc, document, 'menubarTemplate', menuToLoad);
        document.querySelector('.nav-wrapper').appendChild(clone);
        var element = document.querySelectorAll('.button-collapse')[0];
        element.style.display = 'inline-flex';

        $(element).sideNav({
            closeOnClick: true, // Closes side-nav on <a> clicks
            edge: 'left'
        });
    }

    /**
     * Loads the homepage.
     *
     * This loads a different page depending on if the user is currently an instructor or a user.
     *
     * @memberof Index
     */
    function loadHomePage() {
        if (CourseSketch.connection.isInstructor) {
            CourseSketch.redirectContent('/src/instructor/homepage/homePage.html', 'Welcome Instructor');
        } else {
            CourseSketch.redirectContent('/src/student/homepage/homePage.html', 'Welcome Student');
        }

        CourseSketch.dataListener = new AdvanceDataListener(
                CourseSketch.prutil.getRequestClass(), function(evt, item) {
                    console.log('default listener');
                });
        CourseSketch.dataManager = new SchoolDataManager(CourseSketch.connection.userId, CourseSketch.dataListener, CourseSketch.connection,
                CourseSketch.prutil.getRequestClass(), dcodeIO.ByteBuffer);
        CourseSketch.DatabaseException = DatabaseException;
    }
});
