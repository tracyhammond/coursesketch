function WaitScreenManager() {
	/**
	 * resets the values in the school builder so that the same build object can be used again.
	 *
	 * NOTE: ONLY DATA SHOULD GO HERE.
	 */
	this.resetValues = function resetValues() {
		this.waitType = 0; // possible type TYPE_PERCENT, TYPE_WIOD
		this.customIcon = false; // if a custom icon is chosen (this should be a url)
		this.parentElement = false; // if added we will use javascript to place our items
		this.total = false; // creates a step function
		this.exitAtTotal = false; // when the percent bar reaches 100% then it will close itself (this is a boolean value)
		this.exitAtTotalCallback = false; // if exitAtTotal
		this.waitIconText = false; // sets text for the waiting icon
		this.haveOverlay = false; // if overlay is true then the overlay element must be set upon building
		this.overlayElement = false;
	};

	this.resetValues();

	/****************
	 * NOTHING ELSE GOES ABOVE THIS!!!!!
	 * SETTER METHODS
	 *
	 * Creates a set method for every variable in the current object.
	 * EX: setWidth, setImageClicked ...
	 ***************/
	for (obj in this) {
		if (obj != this.resetValues && ('' + obj) != 'resetValues') {
			var objectName = '' + obj;
			// scopes the loop so that the memory of the object stays
			(function(objectName, scope) {
				// capitalizes only the first letter.
				var capitalName = objectName.charAt(0).toUpperCase() + objectName.slice(1);
				var setName = 'set' + capitalName;
				scope[setName] = function(value) {
					scope[objectName] = value;
					return scope;
				};
			})(objectName, this);
		}
	}

	makeValueReadOnly(this, 'TYPE_PERCENT', 0);
	makeValueReadOnly(this, 'TYPE_WIOD', 1); // waiting icon of death
	makeValueReadOnly(this, 'TYPE_WAITING_ICON_OF_DEATH', 1); // waiting circle of death
	makeValueReadOnly(this, 'TYPE_WAITING_ICON', 1); // waiting icon

	/**
	 * Returns an element with some added functions:
	 * startWaiting: pops up the bar on the screen. (must already be added to the parent)
	 * finishWaiting: removes the bar from the screen. (must already be added to the parent)
	 */
	this.build = function build() {
		var running = false;
		var element = document.createElement("div");
		element.setAttribute("class", "waiting_box");
		if (this.waitType == this.TYPE_PERCENT) {
			this.buildPercent(element);
		} else if (this.waitType == this.TYPE_WIOD) {
			this.buildWaitIcon(element);
		}

		element.startWaiting = function() {
			running = true;
			if (element.parentNode) {
				element.style.display = "initial"; // default
			} else {
				throw new Error("Element must be added before it can start waiting");
			}
		};

		element.finishWaiting = function(delay) {
			if (!running) {
				throw new Error("You can only call this while running");
			}
			running = false;
			function remove() {
				if (element.parentNode) {
					element.parentNode.removeChild(element);
				}  else {
					// throw new Error("Element must be added before it can finish waiting");
				}
			}
			if (delay) {
				setTimeout(remove, delay);
			} else {
				remove();
			}
		};

		element.isRunning = function() {
			return running;
		};
		return element;
	};

	/**
	 * Builds a percent element that changes over time as data is loaded.
	 */
	this.buildPercent = function buildPercent(element) {
		var outer = document.createElement("div");
		outer.setAttribute("class", "outer_percent");
		var bar = document.createElement("div");
		bar.setAttribute("class", "inner_percent");
		bar.style.height = "100%";
		bar.style.minHeight = "10px";
		bar.style.width = "0%";
		outer.appendChild(bar);
		outer.style.height = "100%";

		element.appendChild(outer);
		element.updatePercentBar = function(current, total) {
			var percent = current / total * 100;
			bar.style.width = percent + "%";
		};
		if (this.total) {
			(function(total) {
				var elementStepCounter = 0;
				element.step = function() {
					elementStepCounter++;
					element.updatePercentBar(elementStepCounter, total);
				};
			})(this.total);
		}
	};

	/**
	 * Adds a semi-transparent overlay to the specified element, preventing user interaction.
	 *
	 * @param element element to which the overlay will be added
	 */
	this.buildOverlay = function(element) {
		var overlay = document.createElement("dialog");
		overlay.style.background = "#000";
		overlay.style.opacity = 0.5;
		overlay.style.position = "fixed";
		overlay.style.left = 0;
		overlay.style.top = 0;
		overlay.style.width = "100%";
		overlay.style.height = "100%";
		overlay.style.zIndex = 2147483647;
		overlay.open = true;
		overlay.id = "overlay";
		element.appendChild(overlay);
	};

	/**
	 * Adds a wait icon to the specified element.
	 *
	 * @param element element to which the wait icon will be added
	 */
	this.buildWaitIcon = function buildWaitIcon(element) {
		var outer = document.createElement('div');
		outer.setAttribute("class", "outerWaitingIcon");

		var img = document.createElement("img");
		img.setAttribute("class", "waitingIcon");
		if (this.customIcon) {
			img.src = customIcon;
		} else {
			img.src = "/images/loading/000000_large_loader.gif";
		}
		outer.appendChild(img);
		if (this.waitIconText) {
			var text = document.createElement("h1");
			text.textContent = this.waitIconText;
			text.setAttribute("class", "waitingIconText");
			outer.appendChild(text);
		}
		element.appendChild(outer);
	};
}

makeValueReadOnly(WaitScreenManager, 'TYPE_PERCENT', 0);
makeValueReadOnly(WaitScreenManager, 'TYPE_WIOD', 1); // waiting icon of death
makeValueReadOnly(WaitScreenManager, 'TYPE_WAITING_ICON_OF_DEATH', 1); // waiting circle of death
makeValueReadOnly(WaitScreenManager, 'TYPE_WAITING_ICON', 1); // waiting icon
