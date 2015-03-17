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
     * refreshes the page without adding a browser event.
     * @memberof CourseSketch
     */
    CourseSketch.reloadContent = function() {
        var value = CourseSketch.redirector.getRedirect();
        CourseSketch.redirector.changeSourceNoEvent(value);
    };

    /**
     * changes the page to point at a different location.
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
        var r = PROTOBUF_UITL.Request();
        r.setRequestType(Request.MessageType.CLOSE);
        connection.sendRequest(r);
        return 'you can close this window';
    });

    var element = document.querySelector('#loginLocation');

    /**
     * Called when the user logs in correctly.
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
     * Creates a login element and a function so that when the register button is clinked the register is created.
     * This is called on the Register element when cancel is pressed.
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
     * Creates a register element and a function so that when the cancel button is clinked the login is created.
     * This is called on the Login element when register is pressed.
     * @param {Function} login - the createLogin function
     * @see {@link Index.createLogin}
     * @memberof Index
     */
    function createRegister(login) {
        console.log('creating new register element');
        $('#loginLocation').empty();
        var register = document.createElement('register-system');
        register.setOnSuccessLogin(successLogin);
        console.log(register);
        register.setCancelCallback(function() {
            login(createRegister);
        });
        element.appendChild(register);
    }
    createLogin(createRegister);
    element.style.display = 'flex';

    /**
     * Creates and loads the menu.
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
        document.querySelector('#menuBar').appendChild(clone);
        startMenuSliding();
    }

    /**
     * Sets up the sliding for the menu.
     * TODO look up the Header.js and see what is happening there.
     * @memberof Index
     */
    function startMenuSliding() {
        var menuStatus = false;

        $('#menu').find('a').click(function() {
            animateMenu(true); // close menu if a link has been
            // clicked.
        });

        function animateMenu(value) {
            if (value) { // close menu
                $('#content').animate({
                    marginLeft: '0px',
                }, 300, function() {
                    menuStatus = false;
                });
                return false;
            } else { // open menu
                $('#content').animate({
                    marginLeft: '200px',
                }, 300, function() {
                    menuStatus = true;
                });
                return false;
            }
        }

        // Show menu
        $('a.showMenu').click(function() {
            return animateMenu(menuStatus);
        });

        $(document).on('swipeleft', '#menu, .pages', function() {
            if (menuStatus && CourseSketch.isMenuSwipeable) {
                animateMenu(menuStatus);
            }
        });

        $(document).on('swiperight', '.pages', function() {
            if (!menuStatus && CourseSketch.isMenuSwipeable) {
                animateMenu(menuStatus);
            }
        });
        // Menu behaviour
    }

    /**
     * loads the homepage.  This loads a different page depending on if the user is currently an instructor or a user.
     * @memberof Index
     */
    function loadHomePage() {
        console.log('LOADING HOMEPAGE');
        if (CourseSketch.connection.isInstructor) {
            CourseSketch.redirectContent('/src/instructor/homepage/homePage.html', 'Welcome Instructor');
        } else {
            CourseSketch.redirectContent('/src/student/homepage/homePage.html', 'Welcome Student');
        }

        CourseSketch.dataListener = new AdvanceDataListener(CourseSketch.connection,
                CourseSketch.PROTOBUF_UTIL.getRequestClass(), function(evt, item) {
            console.log('default listener');
        });
        CourseSketch.dataManager = new SchoolDataManager(CourseSketch.connection.userId, CourseSketch.dataListener, CourseSketch.connection,
                CourseSketch.PROTOBUF_UTIL.getRequestClass(), dcodeIO.ByteBuffer);
        CourseSketch.DatabaseException = DatabaseException;
    }
});

CourseSketch.isMenuSwipeable = true;
/**
 * turns on menu swiping.
 * @memberof CourseSketch
 */
CourseSketch.enableMenuSwiping = function() {
    CourseSketch.isMenuSwipeable = true;
};
/**
 * turns off menu swiping.
 * @memberof CourseSketch
 */
CourseSketch.disableMenuSwiping = function() {
    CourseSketch.isMenuSwipeable = false;
};
