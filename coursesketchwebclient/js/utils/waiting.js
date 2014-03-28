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
		var element = document.createElement("div");
		element.setAttribute("class", "waitingBox");
		if (this.waitType == this.TYPE_PERCENT) {
			buildPercent(element);
		} else if (this.waitType == this.TYPE_WIOD) {
			buildWaitIcon(element);
		}

		element.startWaiting = function() {
			if (element.parentNode) {
				element.style.display = "initial"; // default 
			} else {
				throw "Element must be added before it can start waiting";
			}
		};

		element.finishWaiting = function() {
			if (element.parentNode) {
				element.parentNode.removeChild(this);
			}  else {
				throw "Element must be added before it can finish waiting";
			}
		};
		return element;
	}

	function buildPercent(element) {
		var outer = document.createElement("outerDiv");
		outer.setAttribute("class", "outerPercent");
		var bar = document.createElement("innerDiv");
		outer.setAttribute("class", "innerPercent");
		bar.style.display = "inline";
		bar.style.height = "100%";
		bar.style.minHeight = "10px";
		bar.style.width = "0%";
		out.appendChild(bar);
		out.style.height = "100%";

		element.appendChild(outer);
		element.updatePercentBar = function(current, total) {
			
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
	}
	
	function buildWaitIcon(element) {
		var outer = document.createElement("img");
		outer.setAttribute("class", "waitingIcon");
		if (this.customIcon) {
			outer.src = customIcon;
		} else {
			outer.src = "images/loading/000000_large_loader.gif";
		}
		element.appendChild(outer);
	}
}