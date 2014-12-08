function NavigationPanel() {

var scope = document;
		parent.problemNavigator.addCallback(function(nav) {
			scope.getElementById("selectionBoxNumber").innerHTML = nav.getCurrentNumber();
			// set span state
			var button = scope.getElementById("buttonNext");
			if (nav.hasNext()) {

				button.onclick = function(){parent.problemNavigator.gotoNext();};
			} else {
				button.onclick = function(){};
			}
			button = scope.getElementById("buttonPrev");
			if (nav.hasPrevious()) {
				button.onclick = function(){parent.problemNavigator.gotoPrevious();};
			} else {
				button.onclick = function(){};
			}
			var totalNumber = nav.getLength();
			if (totalNumber) {
				scope.getElementById("totalNumber").innerHTML = "out of " + totalNumber;
			}
			// TODO: change this to strip out bad HTML code
			scope.getElementById("problemPanel").innerHTML = '<p>' + parent.problemNavigator.getProblemText() + '</p>';
		});

		/*
		window.onresize = function() {
			var navWidth = document.getElementById("navPanel").offsetWidth;
			var navHeight = document.getElementById("navPanel").offsetHeight;
			var textWidth = document.getElementById("panelWrapper").offsetWidth - navWidth;
			document.getElementById("problemPanel").style.width = (textWidth - 15) +"px";
			document.getElementById("problemPanel").style.height = (navHeight -15) +"px";
		}
		*/

	/**
	 * @param templateClone {node} is a clone of the custom HTML Element for the text box
	 * Makes the exit button close the box and enables dragging
	 */
	this.initializeElement = function(templateClone) {
		localScope = this; // This sets the variable to the level of the custom element tag
		shadowRoot = this.createShadowRoot();
		shadowRoot.appendChild(templateClone);

		if (isUndefined(this.navigator)) {
			this.navigator = new ProblemNavigator
		}
	}

	this.setNavigator = function(navPanel) {
		this.navigator = navPanel;
	}
}

NavigationPanel.prototype = Object.create(HTMLElement.prototype);
