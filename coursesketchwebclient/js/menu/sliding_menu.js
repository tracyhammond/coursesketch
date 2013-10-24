$(function () {
    var menuStatus = false;

    /*$("menu").collapsible({
        effect: 'slide',
    });*/

    $("#menu").find("a").click(function() {
    	animateMenu(true); // close menu if a link has been clicked.
    });

    function animateMenu(value) {
    	if (value) { // close menu
			$(".ui-page-active").animate({
                marginLeft: "0px",
            	}, 300, function () {
            		menuStatus = false;
            });
            return false;
		} else { // open menu
			$(".ui-page-active").animate({
				marginLeft: "200px",
            	}, 300, function () {
	            	menuStatus = true;
            });
            return false;
		}
    }

    // Show menu
    $("a.showMenu").click(function () {
    	return animateMenu(menuStatus);
    });

    $(document).on("swipeleft", '#menu, .pages', function () {
        if (menuStatus && enable_menu_swiping) {
        	animateMenu(menuStatus);
        }
    });

    $(document).on("swiperight", '.pages', function () {
        if (!menuStatus && enable_menu_swiping) {
        	animateMenu(menuStatus);
        }
    });
    // Menu behaviour
});

var enable_menu_swiping = true;
function enable_menu_swiping() {
	enable_menu_swiping = true;
}

function disable_menu_swiping() {
	enable_menu_swiping = false;
}