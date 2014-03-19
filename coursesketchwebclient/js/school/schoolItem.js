
/**
 * Takes in a school item object (which is a course, assignment ... etc) and creates an item card as a result.
 */
function SchoolItemBuilder() {
	/**
	 * resets the values in the school builder so that the same build object can be used again.
	 *
	 * NOTE: ONLY DATA SHOULD GO HERE.
	 */
	this.resetValues = function resetValues() {
		this.list = 0;
		this.isSimpleList = false; // displays everything as text
		this.width = 'large'; // the options are small, medium and larg
		this.centerInDiv = false; // true if the card will be centered
		this.instructorCard = false; // true if the card is for an instructor and will be editable as such

		//items to show
		this.listTitle = false; // true if we want to show the title
		this.showImage = true; // true if we want to show an image
		this.showBox = true; // if we want the card to be boxed shape
		this.showDescription = true; // true to show the description
		this.showDate = true; // true to show the show date
		this.showState = false; //changed from this.showCompletionStatus
		this.limitSize = false; // this will limit the description of the card to a certain height and add an expanding button

		// function
		this.boxClickFunction = false;
		this.endEditFunction = false; // when in instructor mode this can be used to edit the box. and the item is saved

		//custom text for an empty list.
		this.emptyListMessage = false;
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

	this.centerItem = this.setCenterInDiv;
	
	/********
	 * LOCAL VARIABLES
	 ******/
	var COURSE = "Course";
	var ASSIGNMENT = "Assignment";
	var PROBLEM = "Problem";
	var BANK_PROBLEM = "BankProblem";
	var localScope = this;
	
	/*****************
	 * CREATING LIST LOGIC
	 ***************/

	this.build = function(id) {

		var hostElement = document.getElementById(id);
		hostElement.innerHTML = '';
		
		// if there is no list add the empty message and then exit
		if (!this.list || this.list.length <= 0) {
			var message = 'There are no items in this list!';
			if (this.emptyListMessage) {
				message = '' + this.emptyListMessage;
			}
			var element = document.createElement('h1');
			element.textContent = message;
			hostElement.appendChild(element);
			return;
		}

		// adds a title
		if (this.listTitle) {
			var element = document.createElement('h1');
			element.textContent = this.listTitle;
			hostElement.appendChild(element);
		}

		this.createSchoolList(hostElement);
	};

	/**
	 * Creates a list of school items with the parameters set by the builder.
	 */
	this.createSchoolList = function(parentElement) {
		var uList = document.createElement('ul');
		uList.setAttribute('class', 'school_list');
		parentElement.appendChild(uList);

		var currentDate = new Date();//getCurrentDate(); // we use one date for the creation of the cards

		for (var i = 0; i< this.list.length; i++) {
			var srlSchoolItem = this.list[i];
			var type = findType(srlSchoolItem); // We establish the type of this specific card

			var listItem = document.createElement('li');
			if (this.centerInDiv) {
				listItem.setAttribute('class', 'child_center');
			}

			if (this.isSimpleList) {
				listItem.appendChild(this.createSimpleSchoolItem(srlSchoolItem));
			} else {
				listItem.appendChild(this.createFancySchoolItem(srlSchoolItem, currentDate, type, i));
			}
			
			uList.appendChild(listItem);
		}
	};

	/**
	 * Returns the HTML for a very basic school item.
	 * This only consist of the title
	 */
	this.createSimpleSchoolItem = function createSimpleSchoolItem(srlSchoolItem) {
		var simpleItem = document.createElement('div');
		simpleItem.setAttribute('class', srlSchoolItem.id);
		simpleItem.textContent = srlSchoolItem.name;
		return simpleItem;
	};

	/**
	 * Builds up a school item that is fully customizable
	 */
	this.createFancySchoolItem = function createFancySchoolItem(srlSchoolItem, currentDate, type, index) {
		// Required Items
		var box = document.createElement('div');
		box.setAttribute('id', srlSchoolItem.id);

		this.createBoxClass(box);

		box.setAttribute('data-item_number', index);

		this.addClickFunction(box, this.boxClickFunction, srlSchoolItem.id);

		this.setBoxState(srlSchoolItem);

		// add the text component
		box.appendChild(this.writeTextData(srlSchoolItem, currentDate, type));

		/*
		if (this.showImage && srlSchoolItem.imageUrl) {
			document.createElement('');
			// remove link
			//html += this.replaceLink(this.imageClicked, srlSchoolItem);
			// image link
			html += '<img src="images/' + srlSchoolItem.imageUrl + '" width="128" height="128"></a>\n';
		}
		html += '</div>\n';
		*/
		return box;
	};

	/**
	 * Writes out the data that composes the text portion of the box.
	 * (Title,subTitle, date, description)
	 */
	this.writeTextData = function writeTextData(srlSchoolItem, currentDate, type) {

		var divElement = document.createElement('div');
		divElement.setAttribute('class', 'text');

		divElement.appendChild(this.createTitleElement(srlSchoolItem));

		if (type == ASSIGNMENT && this.showDate) {
			divElement.appendChild(this.showFormattedDate(srlSchoolItem, currentDate, type));
		}

		divElement.appendChild(this.createDescriptionElement(srlSchoolItem));

		return divElement;
	};

	/**
	 * Creates a title element that can be edited in instructor mode.
	 */
	this.createTitleElement = function(srlSchoolItem) {
		var div = document.createElement('div');
		div.setAttribute('class', 'title');
		var nameElement = document.createElement('h3');
		nameElement.setAttribute('class', 'name');
		nameElement.textContent = srlSchoolItem.name;
		this.addEditCapabilities(nameElement, srlSchoolItem.id, 'title');

		div.appendChild(nameElement);
		return div;
	};

	/**
	 * Creates the formatted date that appears on cards.  These only appear if the date object exist.
	 */
	this.showFormattedDate = function(srlSchoolItem, currentDate, type) {

		// TODO: show the earliest date that has not happened.
		// I.E if it has not opened yet show that date and the due date
		// if the due date has passed show the close date
		// if the close date has passed 
		var dueDate = srlSchoolItem.dueDate;
		var div = document.createElement('div');
		div.setAttribute('class', 'date');
		if (dueDate) {
			var element = document.createElement('h4');
			element.setAttribute('class', 'dueDate subDate');
			element.textContent = 'Due: ' + getFormattedDateTime(new Date(dueDate.millisecond.toNumber()));
			this.addEditCapabilities(element, srlSchoolItem.id, 'date', 'dueDate');
			div.appendChild(element);
		}

		var accessDate = srlSchoolItem.accessDate;
		if (accessDate) {
			var element = document.createElement('h4');
			element.setAttribute('class', 'accessDate subDate');
			element.textContent = 'Open: ' + getFormattedDateTime(new Date(dueDate.millisecond.toNumber()));
			this.addEditCapabilities(element, srlSchoolItem.id, 'date', 'accessDate');
			div.appendChild(element);
		}

		return div;
	};

	this.createDescriptionElement = function(srlSchoolItem) {
		var div = document.createElement('div');
		div.setAttribute('class', 'description');
		var nameElement = document.createElement('p');
		nameElement.setAttribute('class', this.width);
		div.appendChild(nameElement);

		var text = srlSchoolItem.description;
		var cutPoint = 140;
		if (text != null && text.length > cutPoint) {
			var shortText = text.substring(0, cutPoint) + '...';
			nameElement.textContent = shortText;
			var expandButton = document.createElement('p');
			expandButton.setAttribute('class', 'expand');
			var click = false;
			expandButton.onclick = function(event) {
				event.stopPropagation();
				click = ! click;
				if (click) {
					expandButton.setAttribute('class', 'contract');

					// expand
					nameElement.textContent = text;
					localScope.addEditCapabilities(nameElement, srlSchoolItem.id, 'description');
				} else {
					expandButton.setAttribute('class', 'expand');

					// contract
					nameElement.textContent = shortText;
					localScope.addEditCapabilities(nameElement, srlSchoolItem.id, 'description');
				}
			};
			div.appendChild(expandButton);
		} else {
			nameElement.textContent = text;
		}
		
		this.addEditCapabilities(nameElement, srlSchoolItem.id, 'description');

		
		return div;
	};

	/***********
	 * UTILITY METHODS
	 **********/

	/**
	 * Gives an element the ability to be edited.
	 */
	this.addEditCapabilities = function(element, id, section, subSection) {
		if (!this.instructorCard) {
			return;
		}
		var span = document.createElement('span');
		span.setAttribute('class', 'editButton');
		span.onclick = function(event) {
			event.stopPropagation();
			localScope.editSchoolItem(this.parentNode, id, section, subSection);
		};
		span.innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;';
		element.appendChild(span);
	};

	/**
	 * Sets the state if it exist and if showState is true.
	 */
	this.setBoxState = function(srlSchoolItem) {
		if (!this.showState) {
			return;
		}
		var itemState = srlSchoolItem.state;
		var span = document.createElement('span');
		var classType = 'school_item_state ';
		if (itemState != null && ! isUndefined(itemState)) {
			// TODO: add state for an assignment that has been graded.
			if (!completionStatus.accessible && completionStatus.pastDue) {
				classType += 'assignment_closed';
			} else if (completionStatus.completed) {
				classType += 'completed';
			} else if (completionStatus.started) {
				classType += 'in_progress';
			} else if (!completionStatus.accessible) {
				classType += 'not_open';
			}
			span.setAttribute('class', classType);
		}
		return html;
	};

	/**
	 * Creates the box options based on whether we want a box and it is clickable.
	 */
	this.createBoxClass = function createBoxClass(element) {
		if (!this.showBox && !this.boxClickFunction) {
			return;
		}
		var classes = '';
		if (this.showBox) {
			classes += 'school_item ';
		}
		if (!this.instructorCard && this.boxClickFunction) {
			classes += ' hover_box ';
		}
		element.setAttribute('class', classes);
	};

	/**
	 * Returns a string of the object's type (SrlCourse, SrlAssignment, or SrlProblem
	 */
	function findType(object) {
		if (!isUndefined(object.assignmentList)) {
			return COURSE;
		} else if (!isUndefined(object.problemList)) {
			return ASSIGNMENT;
		} else if (!isUndefined(object.questionText)){
			return PROBLEM;
		} else {
			return BANK_PROBLEM;
		}
	}

	/**
	 * Adds an onclick function to the element if the function exists, does nothing otherwise.
	 */
	this.addClickFunction = function addClickFunction(element, functionToAdd, id) {
		if (functionToAdd) {
			element.addEventListener('click',function() {
				functionToAdd(id);
			},false);
			// GET THE ONCLICK LISTENTER TO DO THE CLICKING THING CORRECTLY!
		}
	};

	/**************
	 * EDIT METHODS
	 *************/

	/**
	 * When the edit button is clicked it allows the specific school item to be edited.
	 *
	 * This function replaces the clicked element with an input dialog.
	 */
	this.editSchoolItem = function(element, cardId, specificId, subSection) {
		var specifiedElement = element.parentNode;//document.getElementById(cardId).getElementsByClassName(specificId)[0];
		var input = false;
		var value = this.cut(element.innerHTML, '<span');
		if (specificId == "title") {
			input = document.createElement("input");
			input.type = "text";
			input.value = value[0];
		} else if (specificId == "description") {
			input = document.createElement("textarea");
			input.cols = "45"; // needs to change based on size of object.
			input.value = value[0];
		} else if (specificId == "date") {
			input = document.createElement("input");
			input.type = "date";
			var text = this.cut(value[0], ':')[1];
			// TODO: convert text into a valid date input object;
			input.value = text;
		} else if (specificId == "image") {
			input = document.createElement("input");
			input.type = "image";
			input.onblur = function() {localScope.endEditSchoolItem(cardId, specificId);};
			specifiedElement.appendChild(input);
			// special image dialog!
			return;
		}
		input.className = "school_item_input";
		input.onkeydown = function() {localScope.checkEnter(this);};
		input.onclick = function(event) {
			event.stopPropagation();
		};
		input.onblur = function() {localScope.endEditSchoolItem(this, element, cardId, specificId, subSection);};

		specifiedElement.replaceChild(input, element);
		input.focus();
	};

	/**
	 * Gets the value from the edit element and sets it to the previous element.
	 *
	 * if the callback is set it is called with the value and then the id, type, and subtype.
	 */
	this.endEditSchoolItem = function(oldElement, newElement, cardId, specificId, subSection) {
		var result = oldElement.value;
		if (specificId == "image") {
			specifiedElement.removeChild(oldElement);
			newElement.src = oldElement.value;
			return;
		} else if (specificId == "date") {
			var text = this.cut(this.cut(newElement.innerHTML, '<span')[0], ':')[0]; // gets the left hand of the date text
			result = text + ': ' + oldElement.value;
		}

		oldElement.parentNode.replaceChild(newElement, oldElement);
		newElement.textContent = result;
		this.addEditCapabilities(newElement, cardId, specificId, subSection);

		if (localScope.endEditFunction) {
			localScope.endEditFunction(oldElement.value, cardId, specificId, subSection);
		}
	};

	this.cut = function(input, cutoff) {
		var index = input.indexOf(cutoff);
		result = [];
		result[0] = input.substring(0,index).trim();
		result[1] = input.substring(index).trim();
		return result;
	};

	this.checkEnter = function checkEnter(element) {
		if (event.which == 13 || event.keyCode == 13) {
			element.blur();
		}
	};
}

function clickSelectionManager() {
	this.selectedItems = [];
	this.selectionClassName = ' selected_box';

	this.addSelectedItem = function(id) {
		this.selectedItems.push(id);
		this.selectItem(id);
	};

	this.selectItem = function(id) {
		document.getElementById(id).className+=this.selectionClassName;
	};

	this.clearItem = function(id) {
		var toSelect = document.getElementById(id);
		if(!toSelect) {
			return;
		}	
		var className = toSelect.className;
		var index = className.indexOf(this.selectionClassName);
		var firstHalf = className.substring(0,index);
		var secondHalf = className.substring(index + this.selectionClassName.length);
		toSelect.className = firstHalf + secondHalf;
	};

	this.clearAllSelectedItems = function() {
		for(var i = 0; i < this.selectedItems.length; i++) {
			this.clearItem(this.selectedItems[i]);
		}
		// Clears array.
		this.selectedItems = [];
	};

	this.isItemSelected = function(id) {
		return this.selectedItems.indexOf(id) > -1;
	};
}