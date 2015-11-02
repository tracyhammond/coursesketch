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
     * Gets tutorial and appends it to the document.
     */
    CourseSketch.loadCreateTutorial = function() {
        document.getElementById('tutorial').innerHTML = '';
        var element = document.createElement('entire-timeline');
        document.getElementById('tutorial').appendChild(element);
        element.loadExistingTutorials();
    };

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
        $('#loginLocation').empty();
        var importPage = document.createElement('link');
        importPage.rel = 'import';
        importPage.href = '/src/main.html';
        /**
         * Imports main.html.
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
        document.head.appendChild(importPage);
        element.style.display = 'none';
    };

    /**
     * Creates a login element and a function so that when the register button is clicked the register is created.
     *
     * This is called on the Register element when cancel is pressed.  This forms an infinite loop with {@link Index.createRegister}.
     * They each call the other when clicked.
     * @param {Function} register - the createRegister function
     * @see {@link Index.createRegister}
     * @memberof Index
     */
    function createLogin(register) {
        $('#loginLocation').empty();
        var login = document.createElement('login-system');
        login.setOnSuccessLogin(successLogin);
        login.setRegisterCallback(function() {
            register(createLogin);
        });
        element.appendChild(login);
    }

    /**
     * Creates a register element and a function so that when the cancel button is clicked the login is created.
     *
     * This is called on the Login element when register is pressed.  This forms an infinite loop with {@link Index.createLogin}.
     * They each call the other when clicked.
     * @param {Function} login - the createLogin function
     * @see {@link Index.createLogin}
     * @memberof Index
     */
    function createRegister(login) {
        $('#loginLocation').empty();
        var register = document.createElement('register-system');
        register.setOnSuccessLogin(successLogin);
        register.setCancelCallback(function() {
            login(createRegister);
        });
        element.appendChild(register);
    }
    createLogin(createRegister);

    /**
     * Creates and loads the menu.
     *
     * @param {Link} importDoc The link element that contains the menu template.
     * @memberof Index
     */
    function loadMenu(importDoc) {
        var content = importDoc.querySelector('#menubarTemplate').import;
        var template = undefined;
        if (CourseSketch.connection.isInstructor) {
            template = content.querySelector('#instructorMenu');
        } else {
            template = content.querySelector('#studentMenu');
        }
        var clone = document.importNode(template.content, true);
        document.querySelector('.nav-wrapper').appendChild(clone);

        $('.button-collapse').sideNav({
            closeOnClick: true // Closes side-nav on <a> clicks
        });
    }

    /**
     * loads the homepage.
     *
     * This loads a different page depending on if the user is currently an instructor or a user.
     * @memberof Index
     */
    function loadHomePage() {
        if (CourseSketch.connection.isInstructor) {
            CourseSketch.redirectContent('/src/instructor/homepage/homePage.html', 'Welcome Instructor');
        } else {
            CourseSketch.redirectContent('/src/student/homepage/homePage.html', 'Welcome Student');
        }

        CourseSketch.dataListener = new AdvanceDataListener(CourseSketch.connection,
                CourseSketch.prutil.getRequestClass(), function(evt, item) {
            console.log('default listener');
        });
        CourseSketch.dataManager = new SchoolDataManager(CourseSketch.connection.userId, CourseSketch.dataListener, CourseSketch.connection,
                CourseSketch.prutil.getRequestClass(), dcodeIO.ByteBuffer);
        CourseSketch.DatabaseException = DatabaseException;
    }
});
