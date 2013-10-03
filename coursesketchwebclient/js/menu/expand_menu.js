/**
 * Creates the ability to expand menus.
 *
 * 
 * Assume creation before/during page getting ready.
 */
function expandingMenu(numberOfElements, containerID, scrollPage) {
	var totalPanels = numberOfElements;
	var panelSpeed = 500; //panel animate speed in milliseconds
	var scrollPage = false; // set to true to attempt to scroll page as elements are expanded.
    var defaultOpenPanel = 0; //leave 0 for no panel open   
    var accordian = false; //set panels to behave like an accordian, with one panel only ever open at once      
  
    var panelHeight = new Array();
    var currentPanel = defaultOpenPanel;
    var iconCloseHeight = parseInt($('.icon-close').css('height'));
    var iconOpenHeight = parseInt($('.icon-open').css('height'));
    var highlightOpen = true;
    
    var $containerScope = $(containerID);
	$(document).ready(function () { 
		//Initialise collapsible panels
        function panelinit() {
            for (var i=1; i<=totalpanels; i++) {
            	var $specificPanel = $containerScope.find('#cp-'+i);
            	// Gets the total height of the content
                panelHeight[i] = parseInt($specificPanel.find('.expandable-panel-content').css('height'));
                // Sets the margin-top to move the panel above height of the panel
                quickClosePanel($specificPanel, panelHeight[i]);
                if (defaultOpenPanel == i) {
                	quickOpenPanel($specificPanel);
                }
            }
        }

		$(window).load(function() {
	        panelinit();
	    }); //END LOAD
		
    	$containerScope.find('.expandable-panel-heading').click(function() {           
            var obj = $(this).next();
            // Gets the objects ID.
            var objid = parseInt($(this).parent().attr('ID').substr(3,2));  
            currentPanel = objid;
            if (accordian == true) {
                resetPanels();
            }

            // Check to see if we need to open panel.
            if (parseInt(obj.css('margin-top')) <= (panelheight[objid]*-1)) {
                obj.clearQueue();
                obj.stop();
                openPanel($(this).parent());
            } else {
                obj.clearQueue();
                obj.stop();
                closePanel($(this).parent());
            }
        });
	}
	
	/**
	 * Opens without an animation.
	 */
	function quickOpenPanel($panel) {
		var $iconElement = $panel.find('#expandable-panel-icon');
		$iconElement.removeClass('icon-close');
		$iconElement.addClass('icon-open');
		$panel.find('.expandable-panel-content').css('margin-top', 0);
	}

	/**
	 * Closes without an animation.
	 */
	function quickClosePanel($panel, panelHeight) {
		var $iconElement = $panel.find('#expandable-panel-icon');
		$iconElement.removeClass('icon-open');
		$iconElement.addClass('icon-close');
		$panel.find('.expandable-panel-content').css('margin-top', -panelHeight);
	}

	/**
	 * Closes panel with an animation.
	 */
	function closePanel($panel,panelHeight) {
		var $iconElement = $panel.find('#expandable-panel-icon');
		$iconElement.removeClass('icon-open');
		$iconElement.addClass('icon-close');
		$panel.find('.expandable-panel-content').animate({'margin-top':-panelHeight}, panelspeed);
		// TODO: add scrolling.
        if (highlightopen == true) {
        	$panel.find('.expandable-panel-heading').removeClass('header-active');
        }
	}
	
	/**
	 * Opens panel with an animation.
	 */
	function openPanel($panel) {
		var $iconElement = $panel.find('#expandable-panel-icon');
		$iconElement.removeClass('icon-close');
		$iconElement.addClass('icon-open');
		$panel.find('.expandable-panel-content').animate({'margin-top':0}, panelspeed);
		// TODO: add scrolling.
        if (highlightopen == true) {
        	$panel.find('.expandable-panel-heading').addClass('header-active');
        }
	}

	/**
	 * Closes panel with an animation.
	 */

	function resetPanels() {
        for (var i=1; i<=totalPanels; i++) {
            if (currentPanel != i) {
            	openPanel($containerScope.find('#cp-'+i));
            }
        }
    }
}
