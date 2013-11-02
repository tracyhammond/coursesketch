
/**
 * The data value is preset.
 * Title = [0] (has two values, a label, and a link)
 * Description [1] is just text
 * Image [2] this is just a link
 * SubTitle[3] (has two values, a label, and a link)
 * Date [4] either due date or some other type of date
 */
function schoolItemBuilder() {
	
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
		this.showCompletionStatus = false;
		
		// functions
		this.imageClicked = false;
		this.titleClicked = false;
		this.subClicked = false;
		this.entireBoxClicked = false;
		this.descriptionClicked = false;

		//custom text.
		this.noItemMessage = false;
	}

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
	 * Creates a list of school items with the parameters set by the builder.
	 */
	this.createSchoolList = function createSchoolList() {
		var html = "";
		if(!this.dataList) {
			if(this.noItemMessage) {
				return '<h1>'+this.noItemMessage+'</h1>\n';
			} else {
				return '<h1>There are no items in this list!</h1>\n';
			}
		}

		if(this.listTitle) {
			html += '<h1>' + this.listTitle + '</h1>\n';
		}
		html += '<ul class = "school_list">';
		var currentDate = new Date();
		for(var i = 0; i< this.dataList.length; i++) {
			var list = this.dataList[i];
			var type = list[0]; // We establish the type that it is.
			html+='<li ' + (this.centerInDiv?'class = "child_center"':'') + '>';
			if (this.isSimpleList) {
				html += this.createSimpleSchoolItem(list);
			} else {
				html += this.createFancySchoolItem(list, currentDate, type);
			}
			html+='</li>';
		}
		html+='</ul>';
		return html;
	};

	/**
	 * Returns the HTML for a very basic school item.
	 * This only consist of the title
	 */
	this.createSimpleSchoolItem = function createSimpleSchoolItem(list) {
		var html = '';
		html+='<div class="text" id = "'+list[1][2]+'">';
		html += this.replaceLink(this.titleClicked, list, list[1][1]);
		html += list[1][0] + '</a>';
		html+='</div>';
		return html;
	};

	/**
	 * Returns the HTML for a school_item based off of the specified school builder
	 */
	this.createFancySchoolItem = function createFancySchoolItem(list, currentDate, type) {
		// Required Items
		var html = '';
		
		//<div class = "school_item hoverbox";
		html+='	<div id = "' + list[1][2] + '"' + this.createBoxClass(this.showBox,this.entireBoxClicked);
		html+= this.addClickFunction(this.entireBoxClicked, list) + '>\n';
		
		html+= this.createCompletionStatus(list,type);

		html+='		<div class="text">\n';
		html+=			this.writeTextData(list, currentDate, type);
		html+='	</div>\n';
		if (this.showImage) {
			html+= this.replaceLink(this.imageClicked, list, list[1][1]);
			html+='<img src="images/' + list[3] + '" width="128" height="128"></a>\n';
		}
		html+='</div>\n';
		return html;
	};	

	/**
	 * Writes out the data that composes the text portion of the box.
	 * (Title,subTitle, date, description)
	 */
	this.writeTextData = function writeTextData(list, currentDate, type) {
		var html = '';
		html+= '		<h3 class="name">';
		html += this.replaceLink(this.titleClicked, list, list[1][1]);
		html += list[1][0] + '</a></h3>\n';

		if (type == 'assignment' && this.showDate) {
			var dueDate = list[5];
			html+='		<h1 class="' + getDateType() + '">' + get_formatted_date(currentDate, dueDate) + '</h1>\n';
		}
		if (type == 'assignment' && this.showItemSubTitle) {
			html += '		<h1 class="class">';
			html += this.replaceLink(this.subClicked, list, list[4][1]);
			html += list[4][0] + '</a></h1>\n';
		}

		html+='		<p class="' + this.width + '" ' + this.addClickFunction(this.descriptionClicked, list) + '>' + list[2] + '</p>\n';
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

	this.createCompletionStatus = function(list, type) {
		var index = type == 'class'?
				4 : type == 'assignment'?
						6 : type == 'problem'?
								4 : list.length -1
		var html = '';
		var completionStatus = list[index];
		if (completionStatus == 'completed') {
			html = '<span class="school_item_state checkered_flag"></span>';
		} else if (completionStatus == 'started') {
			html = '<span class="school_item_state construction_tape"></span>';
		} else if (completionStatus == 'unaccessible') {
			html = '<span class="school_item_state not_enter"></span>';
			// the assignment is not able to be worked on!
		} else if (completionStatus == 'closed') {
			html = '<span class="school_item_state closed_sign"></span>';
		}
		return html;
	}

	/**
	 * Returns: <a href="link or function">
	 */
	this.replaceLink = function replaceLink(replacingFunction, list, link) {
		var html = ' <a href="';
		if (replacingFunction) {
			// <a href="javascript:void(0)" onclick = "FUNCTIONNAME(listdata)"
			html+= 'javascript:void(0)"';
			html+= addClickFunction(replacingFunction, list);
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
	this.addClickFunction = function addClickFunction(functionToAdd, list) {
		if (functionToAdd) {
			return ' onclick = "' + functionToAdd + '(' + replaceAll('"', '\'', JSON.stringify(list)) + ')"';
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
}


function getDateType() {
		return 'late';
}

function get_formatted_date(currentDate, dueDate) {
		var curr_date = dueDate.getDate();
		var curr_month = dueDate.getMonth() + 1; //Months are zero based
		var curr_year = dueDate.getFullYear();
		return (curr_date + "-" + curr_month + "-" + curr_year);
}

function replaceAll(find, replace, str) {
  return str.replace(new RegExp(find, 'g'), replace);
}