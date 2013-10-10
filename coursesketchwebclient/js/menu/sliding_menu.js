$(function () {
    var menuStatus = false;

    $("menu").collapsible({
        effect: 'slide',
    });

    $("#menu").find("a").click(function() {
    	animateMenu(true);
    });

    function animateMenu(value) {
		if(value != true) { // value = false;
			document.getElementById("menuBar").style.display = "block";
			 $(".ui-page-active").animate({
	                marginLeft: "200px",
	            }, 300, function () {
	                menuStatus = true;
	            });
	            return false;
		} else {
			$(".ui-page-active").animate({
                marginLeft: "0px",
            }, 300, function () {
                menuStatus = false;
    			document.getElementById("menuBar").style.display = "none";
            });
            return false;
		}
    }
    
    // Show menu
    $("a.showMenu").click(function () {
    	return animateMenu(menuStatus);
    });

//Swiping motion for phone
    $('#menu, .pages').live("swipeleft", function () {
        if (menuStatus && enable_menu_swiping) {
        	animateMenu(menuStatus);
        }
    });

    $('.pages').live("swiperight", function () {
        if (!menuStatus && enable_menu_swiping) {
        	animateMenu(menuStatus);
        }
    });

    $('div[data-role="page"]').live('pagebeforeshow', function (event, ui) {
        menuStatus = false;
        $(".pages").css("margin-left", "0");
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