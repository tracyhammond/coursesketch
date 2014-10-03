makeValueReadOnly(this, "CourseSketch", {});
CourseSketch.connection = false;
CourseSketch.redirector = {};

$(document).ready(function() {
    CourseSketch.redirector = new Redirector(window, document.querySelector("#iframeContent"));
    window.addEventListener("beforeunload", function(e) {
        var r = PROTOBUF_UITL.Request();
        r.setRequestType(Request.MessageType.CLOSE);
        connection.sendRequest(r);
        return "you can close this window";
    });

    var login = document.createElement("login-system");
    login.setOnSuccessLogin(function(loggedInConnection) {
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
        };
        document.head.appendChild(importPage);
    });

    var element = document.querySelector("#loginLocation");
    element.appendChild(login);

    element.style.display = "block";

    function loadMenu(importDoc) {
        console.log("is this working?");
        var content = importDoc.querySelector("#menubarTemplate").import;
        var template = undefined;
        if (CourseSketch.connection.isInstructor) {
            template = content.querySelector("#instructorMenu");
        } else {
            template = content.querySelector("#studentMenu");
        }
        var clone = document.importNode(template.content, true);
        console.log(document);
        console.log(document.querySelector('#menuBar'));
        console.log(document.querySelector('#mainPage'));
        console.log(document.body);
        document.querySelector('#menuBar').appendChild(clone);
        startMenuSliding();
    }

    function startMenuSliding() {
        var menuStatus = false;

        $("#menu").find("a").click(function() {
            console.log("MENU CLICKING!");
            animateMenu(true); // close menu if a link has been clicked.
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
            console.log("MENU CLICKING!");
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
});

CourseSketch.isMenuSwipeable = true;
function enableMenuSwiping() {
    CourseSketch.isMenuSwipeable = true;
}

function disableMenuSwiping() {
    CourseSketch.isMenuSwipeable = false;
}
