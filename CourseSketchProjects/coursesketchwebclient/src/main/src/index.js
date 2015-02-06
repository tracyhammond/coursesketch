if (isUndefined(CourseSketch)) {
    makeValueReadOnly(this, "CourseSketch", {});
}
CourseSketch.connection = false;
CourseSketch.redirector = {};

$(document).ready(
function() {

    CourseSketch.reloadContent = function() {
        var value = CourseSketch.redirector.getRedirect();
        CourseSketch.redirector.changeSourceNoEvent(value);
    };

    CourseSketch.redirectContent = function(url, title) {
        CourseSketch.redirector.changeSourceNoEvent(url);

        // document.getElementById('iframeContent').src = url;
        CourseSketch.redirector.setRedirect(url);

        if (title && CourseSketch.headerManager) {
            CourseSketch.headerManager.changeText(title);
        }
    };

    window.addEventListener("beforeunload", function(e) {
        var r = PROTOBUF_UITL.Request();
        r.setRequestType(Request.MessageType.CLOSE);
        connection.sendRequest(r);
        return "you can close this window";
    });

    var element = document.querySelector("#loginLocation");

    // creates the scuccess funciton.
    var successLogin = function(loggedInConnection) {
        CourseSketch.connection = loggedInConnection;
        $("#loginLocation").empty();
        var importPage = document.createElement("link");
        importPage.rel = "import";
        importPage.href = "/src/main.html";
        importPage.onload = function() {
            var content = importPage.import;
            document.querySelector("#mainPageContent").appendChild(content.querySelector("#mainPage"));
            loadMenu(content);
            $("#mainPageContent").show();

            CourseSketch.redirector = new Redirector(window, document.querySelector("#iframeContent"));

            loadHomePage();
        };
        document.head.appendChild(importPage);
        element.style.display = "none";
    };

    // This section creates a cross so that the login links to the register and register links to login.
    function createLogin(register) {
        $("#loginLocation").empty();
        var login = document.createElement("login-system");
        login.setOnSuccessLogin(successLogin);
        login.setRegisterCallback(function() {
            register(createLogin);
        });
        element.appendChild(login);
    }

    function createRegister(login) {
        console.log("creating new register element");
        $("#loginLocation").empty();
        var register = document.createElement("register-system");
        register.setOnSuccessLogin(successLogin);
        console.log(register);
        register.setCancelCallback(function() {
            login(createRegister);
        });
        element.appendChild(register);
    }
    createLogin(createRegister);
    element.style.display = "flex";

    // create the load menu function.
    function loadMenu(importDoc) {
        var content = importDoc.querySelector("#menubarTemplate").import;
        var template = undefined;
        if (CourseSketch.connection.isInstructor) {
            template = content.querySelector("#instructorMenu");
        } else {
            template = content.querySelector("#studentMenu");
        }
        var clone = document.importNode(template.content, true);
        document.querySelector('#menuBar').appendChild(clone);
        startMenuSliding();
    }

    function startMenuSliding() {
        var menuStatus = false;

        $("#menu").find("a").click(function() {
            animateMenu(true); // close menu if a link has been
            // clicked.
        });

        function animateMenu(value) {
            if (value) { // close menu
                $("#content").animate({
                    marginLeft : "0px",
                }, 300, function() {
                    menuStatus = false;
                });
                return false;
            } else { // open menu
                $("#content").animate({
                    marginLeft : "200px",
                }, 300, function() {
                    menuStatus = true;
                });
                return false;
            }
        }

        // Show menu
        $("a.showMenu").click(function() {
            return animateMenu(menuStatus);
        });

        $(document).on("swipeleft", '#menu, .pages', function() {
            if (menuStatus && CourseSketch.isMenuSwipeable) {
                animateMenu(menuStatus);
            }
        });

        $(document).on("swiperight", '.pages', function() {
            if (!menuStatus && CourseSketch.isMenuSwipeable) {
                animateMenu(menuStatus);
            }
        });
        // Menu behaviour
    }

    function loadHomePage() {
        console.log("LOADING HOMEPAGE");
        if (CourseSketch.connection.isInstructor) {
            CourseSketch.redirectContent("/src/instructor/homepage/homePage.html", "Welcome Instructor");
        } else {
            CourseSketch.redirectContent("/src/student/homepage/homePage.html", "Welcome Student");
        }

        CourseSketch.dataListener = new AdvanceDataListener(CourseSketch.connection, CourseSketch.PROTOBUF_UTIL.getRequestClass(), function(evt, item) {
            console.log("default listener");
        });
        CourseSketch.dataManager = new SchoolDataManager(CourseSketch.connection.userId, CourseSketch.dataListener, CourseSketch.connection,
                CourseSketch.PROTOBUF_UTIL.getRequestClass(), dcodeIO.ByteBuffer);
        CourseSketch.DatabaseException = DatabaseException;
    }
});

CourseSketch.isMenuSwipeable = true;
CourseSketch.enableMenuSwiping = function() {
    CourseSketch.isMenuSwipeable = true;
}

CourseSketch.disableMenuSwiping = function() {
    CourseSketch.isMenuSwipeable = false;
}
