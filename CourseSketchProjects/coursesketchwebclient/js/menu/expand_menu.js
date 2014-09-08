/**
 * Creates the ability to expand menus.
 *
 * Assume creation before/during page getting ready.
 * numberOfElements: this is the total number of panels that you want to expand.
 * containerID: the id of the container containing expandable menu parts.
 * scrollPage: if true then the page will scroll.
 */
function expandingMenu(numberOfElements, containerID, scrollPage) {
	var totalPanels = numberOfElements;
	var panelSpeed = 500; //panel animate speed in milliseconds
	var scrollPage = false; // set to true to attempt to scroll page as elements are expanded.
    var defaultOpenPanel = 0; //leave 0 for no panel open   
    var accordian = accordian; //set panels to behave like an accordian, with one panel only ever open at once      
  
    var panelHeight = new Array();
    var currentPanel = defaultOpenPanel;
    var highlightOpen = true;

	$(document).ready(function () { 
		var $containerScope = $('#' + containerID);
		//Initialise collapsible panels
        function panelinit() {
            for (var i=1; i<=totalPanels; i++) {
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

    	$containerScope.find('.expandable-panel-heading').click(function() {
            var obj = $(this).next();
            // Gets the objects ID.
            var objid = parseInt($(this).parent().attr('ID').substr(3,2));  
            currentPanel = objid;
            if (accordian == true) {
                resetPanels();
            }

            // Check to see if we need to open panel.
         //   alert('height: '+ parseInt(obj.css('margin-top')));
            if (parseInt(obj.css('margin-top')) < 0) {
                obj.clearQueue();
                obj.stop();
                openPanel($(this).parent());
            } else {
                obj.clearQueue();
                obj.stop();
                closePanel($(this).parent(),panelHeight[objid]);
            }
        }); // END CLICK

		$(window).load(function() {
		    panelinit();
		}); //END LOAD
	}); // END READY
	
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
		$panel.find('.expandable-panel-content').animate({'margin-top':-panelHeight}, panelSpeed);
		// TODO: set display to none after closing.
		// TODO: add scrolling.
        if (highlightOpen == true) {
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
		$panel.find('.expandable-panel-content').animate({'margin-top':0}, panelSpeed);
		// TODO: add scrolling.
        if (highlightOpen == true) {
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
