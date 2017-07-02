// jscs:disable jsDoc
(function () {

    /**
     * @param {ShadowRoot} parent - the root of the parent.
     * @return {Map} A map of the data mapped to the element.
     */
    function getInput(parent) {
        var inputList = parent.querySelectorAll('.need-saving');
        var mappedInput = new Map();
        for (var i = 0; i < inputList.length; i++) {
            var value = inputList[i].value;
            mappedInput.set(inputList[i].dataset.prop, value);
        }
        return mappedInput;
    }

    /**
     * Holds the object with the specific functions that loads data.
     */
    var loaderObject = {};

    /**
     * Yes I know these functions have an underscore.
     * This is so that you don't have to dynamically capitalize the first letter.
     * Each one returns a value if it exist otherwise undefined is returned.
     * <br>
     * loads a functiontype object from the schoolItem.
     *
     * @return {Null|Undefined|Enum} either undefined or the value of the enum.
     */
    loaderObject.load_functionType = function (schoolItemElement, schoolItemData, nodeToFill) {
        var index = -1;
        if (!isUndefined(schoolItemData)) {
            try {
                index = schoolItemData.latePolicy.functionType;
            } catch (exception) {
                console.log('Ignoring exception while setting function type of element');
                console.log(exception);
                return null;
            }
        }
        if (index > 0 || index === 0) {
            nodeToFill.options[index].selected = true;
            return nodeToFill.value;
        }
        return undefined;
    };

    /**
     * Yes I know these functions have an underscore.
     * This is so that you don't have to dynamically capitalize the first letter.
     * Each one returns a value if it exist otherwise undefined is returned.
     * <br>
     * loads a time frame object from the schoolItem.
     *
     * @return {Null|Undefined|Enum} either undefined or the value of the enum.
     */
    loaderObject.load_timeFrameType = function (schoolItemElement, schoolItemData, nodeToFill) {
        var index = -1;
        if (!isUndefined(schoolItemData)) {
            try {
                index = schoolItemData.latePolicy.timeFrameType;
            } catch (exception) {
                console.log('Ignoring exception while setting timeFrame type of element');
                console.log(exception);
                return null;
            }
        }
        if (index > 0 || index === 0) {
            nodeToFill.options[index].selected = true;
            return nodeToFill.value;
        }
        return undefined;
    };

    /**
     * Yes I know these functions have an underscore.
     * This is so that you don't have to dynamically capitalize the first letter.
     * Each one returns a value if it exist otherwise undefined is returned.
     * <br>
     * loads a subtraction object from the schoolItem.
     *
     * @return {Null|Undefined|Enum} either undefined or the value of the enum.
     */
    loaderObject.load_subtractionType = function (schoolItemElement, schoolItemData, nodeToFill) {
        var index = -1;
        if (!isUndefined(schoolItemData)) {
            try {
                index = schoolItemData.latePolicy.subtractionType;
            } catch (exception) {
                console.log('Ignoring exception while setting subtraction type of element');
                console.log(exception);
                return null;
            }
        }
        if (index > 0 || index === 0) {
            nodeToFill.options[index].selected = true;
            return nodeToFill.value;
        }
        return undefined;
    };

    function loadIntoElement(elementData, schoolItemData) {
        if (elementData.tagName === 'SPAN') {
            elementData.textContent = schoolItemData;
            if (isUndefined(schoolItemData)) {
                elementData.textContent = "no information for this field";
            }
            return schoolItemData;
        }
        if (elementData.tagName === 'DIV' && elementData.hasAttribute('data-date')) {
            var dateInput = elementData.querySelector('.date');
            var timeInput = elementData.querySelector('.time');

            var result = undefined;
            var date;
            if (!isUndefined(schoolItemData) && !isUndefined(schoolItemData.millisecond) && schoolItemData.millisecond !== null) {
                var milliseconds = '' + schoolItemData.millisecond;
                result = schoolItemData.millisecond;
                date = new Date(milliseconds);
                if ('' + date === 'Invalid Date') {
                    date = new Date();
                }
            }

            // format date for date input
            var day = ('0' + date.getDate()).slice(-2);
            var month = ('0' + (date.getMonth() + 1)).slice(-2);
            var today = date.getFullYear() + '-' + (month) + '-' + (day);

            dateInput.value = today;

            // format hour for date input
            var hours = ('0' + date.getHours()).slice(-2);
            var minutes = ('0' + date.getMinutes()).slice(-2);
            var seconds = ('0' + date.getSeconds()).slice(-2);

            var time = hours + ':' + minutes + ':' + seconds;

            timeInput.value = time;

            return result;
        }
        if (elementData.tagName === 'TEXTAREA' || (elementData.tagName === 'INPUT' && elementData.type === 'text')) {
            if (!isUndefined(schoolItemData)) {
                elementData.value = schoolItemData;
            }
            return schoolItemData;
        }
        if (elementData.tagName === 'SELECT') {
            if (!isUndefined(schoolItemData)) {
                elementData.options[schoolItemData].selected = true;
                elementData.style.display = 'inherit';
            }
            return schoolItemData;
        }
    }

    /**
     * Loads the data from the school item into the edit panel.
     *
     * @param {Element} schoolItemElement - the school Item that is currently being edited.
     * @param {Map} schoolItemData - A mpa containing the school item.
     * @param {Element} editPanel - a panel that displays the editable material.
     * @returns {Map} A map that contains the field of the school proto boject and the loaded value of the proto object.
     */
    function loadData(schoolItemElement, schoolItemData, editPanel) {
        var inputList = editPanel.querySelectorAll('.need-loading');
        var mappedInput = new Map();
        for (var property in schoolItemData) {
            if (schoolItemData.hasOwnProperty(property)) {
                /*
                 var element = inputList.filter(function (element) {
                 return element.getAttribute('data-prop') === property;
                 });
                 */
                var element = editPanel.querySelectorAll('.need-loading[data-prop="' + property + '"]')[0];
                if (!isUndefined(element)) {
                    var elementData = element.querySelectorAll('.data')[0];
                    var result = loadIntoElement(elementData, schoolItemData[property]);
                    element.style.display = 'inherit';
                    mappedInput.set(property, result);
                } else {
                    mappedInput.set(property, undefined);
                }
            }
        }
        return mappedInput;
    }

    /**
     * Removes the advance edit panel if the school item is removed.
     */
    SchoolItem.prototype.finalize = function () {
        if (!isUndefined(this.advanceEditPanel)) {
            if (this.advanceEditPanel.parentNode !== null) {
                this.advanceEditPanel.parentNode.removeChild(this.advanceEditPanel);
            }
        }
    };

    /**
     * Sets up the advance edit panel for editing advance data.
     *
     * @param {SchoolItem} localElement -  The school item that this advance panel is associated with.
     * @param {Node} parentNode - The node that is a parent to the button.  This is used to get the school item after saving.
     */
    SchoolItem.prototype.createAdvanceEditPanel = function (localElement, parentNode, saveCallback) {
        // create host and position it
        var host = document.createElement('div');
        host.className = 'advanceEditHost';
        host.style.height = '100%';
        host.style.display = 'none';

        // add html to host
        var shadow = host.createShadowRoot();
        var clone = localElement.getAdvanceEditPanel();
        clone = combineLists(clone, localElement);
        shadow.appendChild(clone);
        localElement.advanceEditPanel = host;
        var currentData;
        // add our loaded element to the page.
        parentNode.appendChild(host);

        // save data
        //NOT WORKING, NEEDS TO BE MODIFIED
        var saveButton = shadow.querySelector('button.save');
        /**
         * Called to save all of the input.
         */
        saveButton.onclick = function () {
            var newData = getInput(shadow);
            var schoolItem = localElement;
            console.log(schoolItem);
            console.log(localElement);
            saveCallback('advance', currentData, newData, schoolItem);
        };

        // cancel!
        /**
         * Called to cancel the editing process.
         *
         * @param {Element} closeEvent -  On Click event.
         * @returns {Boolean} false if the element clicked is the host dialog.
         */
        function close(closeEvent) {
            if (closeEvent.toElement === host) {
                return false;
            }
            closeEvent.stopPropagation();
            document.body.removeEventListener('click', close);
            try {
                document.body.removeChild(host);
            } catch (exception) {
                // ignored if this throws an error.
            }
        }

        shadow.querySelector('button.closeButton').onclick = close;
        document.body.addEventListener('click', close);

        // select the target node
        var target = host;

        // create an observer instance
        var observer = new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                if (mutation.type === 'attributes') {
                    currentData = loadData(localElement, localElement.schoolItemData, shadow);
                    var accordion = host.shadowRoot.querySelectorAll('.collapsible')[0];
                    $(accordion).collapsible();
                }
            });
        });

        // configuration of the observer:
        var config = {attributes: true, childList: true};

        // pass in the target node, as well as the observer options
        observer.observe(target, config);

        return host;
    };

    /**
     *
     * @param {Element} clone
     * @param schoolItemElement
     */
    function combineLists(clone, schoolItemElement) {
        var type = schoolItemElement.getAttribute('data-type');
        var listOfTemplates = clone.querySelectorAll('.' + type);
        var parent = clone.querySelector('ul.collapsible');
        for (var i = 0; i < listOfTemplates.length; i++) {
            parent.appendChild(listOfTemplates[i].content);
        }
        return clone;
    }

    function ignore() {
        /**
         * Opens scripting window on click.
         *
         * open scripting window and sketch saving/loading
         */
        var scriptButton = shadow.querySelector('button.scripting');
        /**
         * Called to open a script editor.
         */
        scriptButton.onclick = function () {
            var data = getInput(shadow);
            location.href = '/src/instructor/problemCreation/scriptEditor/scriptEditor.html';
        };
    }
})();
