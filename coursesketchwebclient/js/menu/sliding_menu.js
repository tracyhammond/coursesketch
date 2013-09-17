$(function () {
    var menuStatus;

    $("menu").collapsible({
        effect: 'slide',
    });

    // Show menu
    $("a.showMenu").click(function () {
        if (menuStatus != true) {
            $(".ui-page-active").animate({
                marginLeft: "200px",
            }, 300, function () {
                menuStatus = true
            });
            return false;
        } else {
            $(".ui-page-active").animate({
                marginLeft: "0px",
            }, 300, function () {
                menuStatus = false
            });
            return false;
        }
    });

//Swiping motion for phone
    $('#menu, .pages').live("swipeleft", function () {
        if (menuStatus && enable_menu_swiping) {
            $(".ui-page-active").animate({
                marginLeft: "0px",
            }, 300, function () {
                menuStatus = false
            });
        }
    });

    $('.pages').live("swiperight", function () {
        if (!menuStatus && enable_menu_swiping) {
            $(".ui-page-active").animate({
                marginLeft: "200px",
            }, 300, function () {
                menuStatus = true
            });
        }
    });

    $('div[data-role="page"]').live('pagebeforeshow', function (event, ui) {
        menuStatus = false;
        $(".pages").css("margin-left", "0");
    });

    // Menu behaviour
    $("#menu li a").click(function () {
        var p = $(this).parent();
        if ($(p).hasClass('active')) {
            $("#menu li").removeClass('active');
        } else {
            $("#menu li").removeClass('active');
            $(p).addClass('active');
        }
    });
});

var enable_menu_swiping = true;
function enable_menu_swiping() {
	enable_menu_swiping = true;
}

function disable_menu_swiping() {
	enable_menu_swiping = false;
}