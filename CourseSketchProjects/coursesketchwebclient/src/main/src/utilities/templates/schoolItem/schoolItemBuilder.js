/**
 * Takes in a school item object (which is a course, assignment ... etc) and
 * creates an item card as a result.
 */
function SchoolItemBuilder() {
    /**
     * resets the values in the school builder so that the same build object can
     * be used again.
     *
     * NOTE: ONLY DATA SHOULD GO HERE.
     */
    this.resetValues = function resetValues() {
        this.list = 0;
        this.instructorCard = false; // true if the card is for an instructor
        // and will be editable as such

        // items to show
        this.listTitle = false; // true if we want to show the title
        this.showImage = true; // true if we want to show an image
        this.showBox = true; // if we want the card to be boxed shape
        this.showDescription = true; // true to show the description
        this.showState = true; // changed from this.showCompletionStatus
        this.showDate = true; // true to show the date.

        this.clearList = true; // true if you want to empty the previous list.

        // function
        this.boxClickFunction = false; // calls a function with the entire
        // schoolItem object
        this.editCallback = false; // when in instructor mode this can be
        // used to edit the box. and the item is
        // saved

        // custom text for an empty list.
        this.emptyListMessage = false;
    };

    this.resetValues();

    /***************************************************************************
     * NOTHING ELSE GOES ABOVE THIS!!!!! SETTER METHODS
     *
     * Creates a set method for every variable in the current object. EX:
     * setWidth, setImageClicked ...
     **************************************************************************/
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

    /***************************************************************************
     * LOCAL VARIABLES
     **************************************************************************/
    var COURSE = "Course";
    var ASSIGNMENT = "Assignment";
    var PROBLEM = "Problem";
    var BANK_PROBLEM = "BankProblem";
    var localScope = this;

    /***************************************************************************
     * CREATING LIST LOGIC
     **************************************************************************/

    this.build = function(id) {

        var hostElement = undefined;
        if (typeof id == "string") {
            hostElement = document.getElementById(id);
        } else {
            hostElement = id;
        }

        if (this.clearList) {
            // TODO: make this use the faster way of clearing
            hostElement.innerHTML = "";
        }

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
        // only get the date once.
        var currentDate = CourseSketch.getCurrentTime();

        for (var i = 0; i < this.list.length; i++) {
            var srlSchoolItem = this.list[i];
            var type = findType(srlSchoolItem); // We establish the type of this
            // specific card

            parentElement.appendChild(this.createFancySchoolItem(srlSchoolItem, currentDate, type, i));
        }
    };

    /**
     * Returns a string of the object's type (SrlCourse, SrlAssignment, or
     * SrlProblem
     */
    function findType(object) {
        if (!isUndefined(object.assignmentList)) {
            return COURSE;
        } else if (!isUndefined(object.problemList)) {
            return ASSIGNMENT;
        } else if (!isUndefined(object.questionText)) {
            return PROBLEM;
        } else {
            return BANK_PROBLEM;
        }
    }

    /**
     * Builds up a school item that is fully customizable
     */
    this.createFancySchoolItem = function createFancySchoolItem(srlSchoolItem, currentDate, type, index) {
        // Required Items
        var box = document.createElement('school-item');
        box.setAttribute('id', srlSchoolItem.id);
        box.schoolItemData = srlSchoolItem;

        if (!this.instructorCard && this.boxClickFunction) {
            box.dataset.clickable = "";
        }

        if (this.instructorCard) {
            box.dataset.instructor = "";
        }

        box.setAttribute('data-item_number', index);

        this.addClickFunction(box, this.boxClickFunction, this.editCallback, srlSchoolItem);

        this.setBoxState(box, srlSchoolItem);

        // add the text component
        this.writeTextData(box, srlSchoolItem, currentDate, type);

        return box;
    };

    /**
     * Sets the state if it exist and if showState is true.
     */
    this.setBoxState = function(box, srlSchoolItem) {
        if (!this.showState) {
            return;
        }
        var itemState = srlSchoolItem.state;
        if (itemState != null && !isUndefined(itemState)) {
            // TODO: add state for an assignment that has been graded.
            if (itemState.graded) {
                $(box).addClass("graded");
                box.dataset.state = "graded";
            } else if (!itemState.accessible && itemState.pastDue) {
                $(box).addClass("closed");
                box.dataset.state = "closed";
            } else if (itemState.completed) {
                box.dataset.state = "completed";
            } else if (itemState.started) {
                box.dataset.state = "inProgress";
            } else if (!itemState.accessible) {
                box.dataset.state = "notOpen";
            }
        }
    };

    /**
     * Adds an onclick function to the element if the function exists, does
     * nothing otherwise.
     */
    this.addClickFunction = function addClickFunction(element, boxFunction, editFunction, srlSchoolItem) {
        if (boxFunction) {
            element.addEventListener('click', function() {
                boxFunction(srlSchoolItem);
            }, false);
            // GET THE ONCLICK LISTENTER TO DO THE CLICKING THING CORRECTLY!
        }

        if (editFunction) {
            element.setEditCallback(function(type, oldValue, newValue, realElement) {
                editFunction(type, oldValue, newValue, realElement.schoolItemData);
            });
        }
    };

    /**
     * Writes out the data that composes the text portion of the box.
     * (Title,subTitle, date, description)
     */
    this.writeTextData = function writeTextData(box, srlSchoolItem, currentDate, type) {

        if (srlSchoolItem.name) {
            var name = document.createElement('span');
            name.className = "name";
            name.textContent = srlSchoolItem.name;
            box.appendChild(name);
        }

        if (srlSchoolItem.description) {
            var description = document.createElement('div');
            description.className = "description";
            description.textContent = srlSchoolItem.description;
            box.appendChild(description);
        }

        if (type == ASSIGNMENT && this.showDate) {
            this.showFormattedDate(box, srlSchoolItem, currentDate, type);
        }
    };

    /**
     * Creates the formatted date that appears on cards. These only appear if
     * the date object exist.
     */
    this.showFormattedDate = function(box, srlSchoolItem, currentDate, type) {

        // TODO: show the earliest date that has not happened.
        // I.E if it has not opened yet show that date and the due date
        // if the due date has passed show the close date
        // if the close date has passed show only the close date

        var accessDate = srlSchoolItem.accessDate;
        if (accessDate) {
            var element = document.createElement('span');
            element.setAttribute('class', 'accessDate');
            element.textContent = getFormattedDateTime(new Date(accessDate.millisecond.toNumber()));
            box.appendChild(element);
        }

        var dueDate = srlSchoolItem.dueDate;
        if (dueDate) {
            var element = document.createElement('span');
            element.setAttribute('class', 'dueDate');
            element.textContent = getFormattedDateTime(new Date(dueDate.millisecond.toNumber()));
            box.appendChild(element);
        }

        var closeDate = srlSchoolItem.closeDate;
        var showCloseDate = true;
        if (dueDate && closeDate) {
            showCloseDate = closeDate.millisecond.toNumber() != dueDate.millisecond.toNumber();
        }
        if (closeDate && showCloseDate) {
            var element = document.createElement('span');
            element.setAttribute('class', 'closeDate');
            element.textContent = getFormattedDateTime(new Date(closeDate.millisecond.toNumber()));
            box.appendChild(element);
        }

        // if there is a single date element involved.
        if (accessDate || dueDate || (closeDate && showCloseDate)) {
            $(box).addClass("validDate");
        }
    };
}
