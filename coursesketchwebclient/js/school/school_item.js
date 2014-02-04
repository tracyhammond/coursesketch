
/**
 * The data value is preset.
 * Title = [0] (has two values, a label, and a link)
 * Description [1] is just text
 * Image [2] this is just a link
 * SubTitle[3] (has two values, a label, and a link)
 * Date [4] either due date or some other type of date
 */
function SchoolItemBuilder() {

	this.resetValues = function resetValues() {
		this.dataList = 0;
		this.isSimpleList = false;
		this.width = 'large';
		this.centerInDiv = false;

		//items to show
		this.listTitle = false;
		this.showImage = true;
		this.showItemSubTitle = true;
		this.showBox = true;
		this.showDescription = true;
		this.showDate = true;
		this.showState = false; //changed from this.showCompletionStatus

		// functions
		this.imageClicked = false;
		this.titleClicked = false;
		this.subClicked = false;
		this.entireBoxClicked = false;
		this.descriptionClicked = false;

		//custom text.
		this.noItemMessage = false;
	};

	this.resetValues();

	this.setEmptyListMessage = function(message) {
		this.noItemMessage = message;
	};

	this.setList = function setList(list) {
		this.dataList = list;
		return this;
	};

	this.setTitle = function setTitle(title) {
		this.listTitle = title;
	};

	this.build = function build(id) {
		document.getElementById(id).innerHTML = this.createSchoolList();
	};

	/**
	 * Sets all of the show options at one time.
	 *
	 * If this is too much then we can have them set all independently.
	 *
	 */
	this.setShowOptions = function setShowOptions(showImage, showItemSubTitle, showDate, showListTitle, showBox) {
		this.showBox = showBox;
		this.showListTitle = showListTitle;
		this.showImage = showImage;
		this.showItemSubTitle = showItemSubTitle;
		this.showDate = showDate;
		return this;
	};

	this.setWidth = function setWidth(width) {
		if (width == 'small' || width == 'medium' || width == 'large') {
			this.width = width;
		}
		return this;
	};

	this.centerItem = function centerItem(center) {
		this.centerInDiv = center;
		return this;
	};

	/**
	 * This overrides all other options to make it show a simple list.
	 *
	 * Shows the title and a link. If a titleClickFunction is set then it will still be used.
	 */
	this.showAsSimpleList = function showAsSimpleList(isSimpleList) {
		this.isSimpleList = isSimpleList;
	};

	/**
	 * sets the function for clicking the entire box.
	 *
	 * This overrides all other functions.
	 * So if you want other function based on different things, then you must set those afterwards.
	 * The input is json of the current list of elements
	 */	
	this.setOnBoxClick = function onBoxClick(clickFunction) {
		this.entireBoxClicked =  clickFunction;
	};
	//*********
	// CREATION METHODS BELOW
	//*********

	/**
	 * Returns a string of the object's type (SrlCourse, SrlAssignment, or SrlProblem
	 */
	function findType(object) {
		if (!isUndefined(object.assignmentList)) {
			return "Course";
		} else if (!isUndefined(object.problemList)) {
			return "Assignment";
		} else {
			return "Problem";
		}
	}
	
//	function getSchoolItem(id){
//		return
//	}
	
	/**
	 * Creates a list of school items with the parameters set by the builder.
	 */
	this.createSchoolList = function createSchoolList() {
		var html = "";
		if (!this.dataList) {
			if (this.noItemMessage) {
				return '<h1>'+this.noItemMessage+'</h1>\n';
			} else {
				return '<h1>There are no items in this list!</h1>\n';
			}
		}

		if (this.listTitle) {
			html += '<h1>' + this.listTitle + '</h1>\n';
		}
		html += '<ul class = "school_list">';
		var currentDate = new Date();
		for (var i = 0; i< this.dataList.length; i++) {
			var srlSchoolItem = this.dataList[i];
			var type = findType(srlSchoolItem); // We establish the type that it is.
			html += '<li ' + (this.centerInDiv?'class = "child_center"':'') + '>';
			if (this.isSimpleList) {
				html += this.createSimpleSchoolItem(srlSchoolItem);
			} else {
				html += this.createFancySchoolItem(srlSchoolItem, currentDate, type, i);
			}
			html += '</li>';
		}
		html += '</ul>';
		return html;
	};

	/**
	 * Returns the HTML for a very basic school item.
	 * This only consist of the title
	 */
	this.createSimpleSchoolItem = function createSimpleSchoolItem(srlSchoolItem) {
		var html = '';
		// name
		html+='<div class="text" id = "'+srlSchoolItem.id+'">';
//		html += this.replaceLink(this.titleClicked, srlSchoolItem, srlSchoolItem[1][1]);
		html += srlSchoolItem.name; //+ '</a>';
		html+='</div>';
		return html;
	};

	/**
	 * Returns the HTML for a school_item based off of the specified school builder
	 */
	this.createFancySchoolItem = function createFancySchoolItem(srlSchoolItem, currentDate, type, index) {
		// Required Items
		var html = '';

		//objctId
		html += '	<div id = "' + srlSchoolItem.id + '"' + this.createBoxClass(this.showBox,this.entireBoxClicked);
		html += 'data-item_number="' + index + '"'; // the number of the school item

		html += this.addClickFunction(this.entireBoxClicked, srlSchoolItem.id) + '>\n';

		html += this.createCompletionStatus(srlSchoolItem);

		html += '		<div class="text">\n';
		html +=	this.writeTextData(srlSchoolItem, currentDate, type);
		html += '	</div>\n';

		if (this.showImage && srlSchoolItem.imageUrl) {
			// remove link
			html += this.replaceLink(this.imageClicked, srlSchoolItem);
			// image link
			html += '<img src="images/' + srlSchoolItem.imageUrl + '" width="128" height="128"></a>\n';
		}
		html += '</div>\n';
		return html;
	};	

	/**
	 * Writes out the data that composes the text portion of the box.
	 * (Title,subTitle, date, description)
	 */
	this.writeTextData = function writeTextData(srlSchoolItem, currentDate, type) {
		var html = '';
		html+= '		<h3 class="name">';
														// remove link
		//html += this.replaceLink(this.titleClicked, list, list[1][1]);
		html += srlSchoolItem.name; // + '</a></h3>\n';
		html += '</h3>\n';

		if (type == 'Assignment' && this.showDate) {
			var dueDate = srlSchoolItem.dueDate;
			if (dueDate) {
				console.log("server date");
				console.log(dueDate);
				html+='		<h1>' + getFormattedDateTime(new Date(dueDate.millisecond.toNumber())) + '</h1>\n';
			}
		}
/*		if (type == 'Assignment' && this.showItemSubTitle) {
			html += '		<h1 class="class">';
			//html += this.replaceLink(this.subClicked, list, list[4][1]);
			html += list[4][0];// + '</a>
			//html += srlSchoolItem.getSchoolItem(srlSchoolItem.courseId).name;
			//??? ^ Need to create a 'getSchoolItem' function that returns the object with the specified ID
			html += '</h1>\n';
		}
*/
		html+='		<p class="' + this.width + '" ' + this.addClickFunction(this.descriptionClicked, srlSchoolItem) + '>' + srlSchoolItem.description + '</p>\n';
		return html;
	};

	this.createBoxClass = function createBoxClass(showBox, entireBoxClicked) {
		if (!showBox && !entireBoxClicked) {
			return '';
		}
		var html = 'class = "';
		if (showBox) {
			html += 'school_item ';
		}
		if (entireBoxClicked) {
			html+= ' hover_box ';
		}
		return html + '"';
	};

	this.createCompletionStatus = function(srlSchoolItem) {
		var html = '';
		var completionStatus = srlSchoolItem.state;
		if (completionStatus != null) {
			 if (!completionStatus.accessible && completionStatus.pastDue) {
				html = '<span class="school_item_state assignment_closed"></span>';
				//The assignment was accessible, but is now closed
			} else if (completionStatus.completed) {
				html = '<span class="school_item_state completed"></span>';
			} else if (completionStatus.started) {
				html = '<span class="school_item_state in_progress"></span>';
			} else if (!completionStatus.accessible) {
				html = '<span class="school_item_state not_open"></span>';
				// the assignment is not able to be worked on!
			}
		}
		return html;
	}

	/**
	 * Returns: <a href="link or function">
	 */
	this.replaceLink = function replaceLink(replacingFunction, srlSchoolItem, link) {
		var html = ' <a href="';
		if (replacingFunction) {
			// <a href="javascript:void(0)" onclick = "FUNCTIONNAME(listdata)"
			html+= 'javascript:void(0)"';
			html+= addClickFunction(replacingFunction, srlSchoolItem);
		} else {
			// <a href="LINKLOCATION"
			html+= link + '"';
		}
		html+='> ';
		return html;
	};

	/**
	 * Adds a function to the html if it exist.
	 *
	 * If the function does not exist then empty string is returned.
	 */
	this.addClickFunction = function addClickFunction(functionToAdd, id) {
		if (functionToAdd) {
			//replaceAll('"', '\'', JSON.stringify(list))
			var escapedString = '\'' + id + '\'';
			return ' onclick = "' + functionToAdd + '(' + escapedString + ')"'; 
			//??? Josh: Unsure how to convert from [array] to .protobuf format
		} else {
			return '';
		}
	};
}

function clickSelectionManager() {
	this.selectedItems = [];
	this.selectionClassName = ' selected_box';

	this.addSelectedItem = function(id) {
		this.selectedItems.push(id);
		this.selectItem(id);
	}

	this.selectItem = function(id) {
		document.getElementById(id).className+=this.selectionClassName;
	}

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
	}

	this.clearAllSelectedItems = function() {
		for(var i = 0; i < this.selectedItems.length; i++) {
			this.clearItem(this.selectedItems[i]);
		}
		// Clears array.
		this.selectedItems = [];
	}

	this.isItemSelected = function(id) {
		return this.selectedItems.indexOf(id) > -1;
	}
}
/*
function getFormattedDate(currentDate, dueDate) {
		var curr_date = dueDate.getDate();
		var curr_month = dueDate.getMonth() + 1; //Months are zero based
		var curr_year = dueDate.getFullYear();
		return (curr_date + "-" + curr_month + "-" + curr_year);
}
*/

function getFormattedDateTime(dateTime) {
	console.log(dateTime);
	return ((dateTime.getMonth() +1) + "/" + dateTime.getDate() + "/" + dateTime.getFullYear());
}