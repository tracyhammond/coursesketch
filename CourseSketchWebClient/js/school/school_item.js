
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
		
		// functions
		this.imageClicked = false;
		this.titleClicked = false;
		this.subClicked = false;
		this.entireBoxClicked = false;
		this.descriptionClicked = false;
	}

	this.resetValues();
	
	this.setList = function setList(list) {
		this.dataList = list;
		return this;
	}
	
	this.setTitle = function setTitle(title) {
		this.listTitle = title;
	}

	this.build = function build(id) {
		document.getElementById(id).innerHTML = this.createSchoolList();
	}
	
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
	}
	
	this.setWidth = function setWidth(width) {
		if (width == 'small' || width == 'medium' || width == 'large') {
			this.width = width;
		}
		return this;
	}

	this.centerItem = function centerItem(center) {
		this.centerInDiv = center;
		return this;
	}

	/**
	 * This overrides all other options to make it show a simple list.
	 *
	 * Shows the title and a link. If a titleClickFunction is set then it will still be used.
	 */
	this.showAsSimpleList = function showAsSimpleList(isSimpleList) {
		this.isSimpleList = isSimpleList;
	}

	/**
	 * Returns the HTML for a school_item based off of the specified school builder
	 */
	this.createFancySchoolItem = function createFancySchoolItem(list, currentDate) {
		// Required Items
		var html = '';
		
		//<div class = "school_item hoverbox";
		html+='	<div ' + this.createBoxClass(this.showBox,this.entireBoxClicked);
		html+= this.addClickFunction(this.entireBoxClicked, list) + '>';
		

		html+='		<div class="text">';
		html+=			this.writeTextData(list, currentDate);
		html+='	</div>';
		if (this.showImage) {
			html+= this.replaceLink(this.imageClicked, list, list[0][1]);
			html+='<img src="images/' + list[2] + '" width="128" height="128"></a>';
		}
		html+='</div>';
		return html;
	}

	/**
	 * Returns the HTML for a very basic school item.
	 * This only consist of the title
	 */
	this.createSimpleSchoolItem = function createSimpleSchoolItem(list) {
		var html = '';
		html+='<div class="text">';
		html += this.replaceLink(this.titleClicked, list, list[0][1]);
		html += list[0][0] + '</a>';
		html+='</div>';
		return html;
	}
	
	/**
	 * Creates a very 
	 */
	this.createSchoolList = function createSchoolList() {
		var html = "";
		if(this.listTitle) {
			html += '<h1>' + this.listTitle + '</h1>\n';
		}
		html += '<ul class = "school_list">';
		var currentDate = new Date();
		for(var i = 0; i< this.dataList.length; i++) {
			var list = this.dataList[i];

			html+='<li ' + (this.centerInDiv?'class = "child_center"':'') + '>';
			if (this.isSimpleList) {
				html += this.createSimpleSchoolItem(list);
			} else {
				html += this.createFancySchoolItem(list, currentDate);
			}
			html+='</li>';
		}
		html+='</ul>';
		return html;
	}
	
	/**
	 * Writes out the data that composes the text portion of the box.
	 * (Title,subTitle, date, description)
	 */
	this.writeTextData = function writeTextData(list, currentDate) {
		var html = '';
		html+= '		<h3 class="name">';
		html += this.replaceLink(this.titleClicked, list, list[0][1]);
		html += list[0][0] + '</a></h3>';

		if (list.length > 3 && this.showDate) {
			var dueDate = list[4];
			html+='		<h1 class="' + getDateType() + '">' + get_formatted_date(currentDate, dueDate) + '</h1>';
		}
		if (list.length > 3 && this.showItemSubTitle) {
			html += '		<h1 class="class">';
			html += this.replaceLink(this.subClicked, list, list[3][1]);
			html += list[3][0] + '</a></h1>';
		}

		html+='		<p class="' + this.width + '" ' + this.addClickFunction(this.descriptionClicked, list) + '>' + list[1] + '</p>';
		return html;
	}
	
	this.createBoxClass = function createBoxClass(showBox, entireBoxClicked) {
		if (!showBox && !entireBoxClicked) {
			return '';
		}
		var html = 'class = "';
		if (showBox) {
			html += 'school_item ';
		}
		if (entireBoxClicked) {
			html+= ' hoverBox ';
		}
		return html + '"';
	}
	

	/**
	 * Returns: <a href="link or function">
	 */
	this.replaceLink = function replaceLink(replacingFunction, list, link) {
		var html = ' <a href="';
		if (replacingFunction) {
			// <a href="javascript:void(0)" onclick = "FUNCTIONNAME(listdata)"
			html+= 'javascript:void(0)"';
			html+= ' onclick ="' + replacingFunction;
			html+= '(' + list + ')';
		} else {
			// <a href="LINKLOCATION"
			html+= link;
		}
		html+='"> ';
		return html;
	}

	/**
	 * Adds a function to the html if it exist.
	 *
	 * If the function does not exist then empty string is returned.
	 */
	this.addClickFunction = function addClickFunction(functionToAdd, list) {
		if (functionToAdd) {
			return ' onclick = "' + functionToAdd + '(' + list + ')"';
		} else {
			return '';
		}
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