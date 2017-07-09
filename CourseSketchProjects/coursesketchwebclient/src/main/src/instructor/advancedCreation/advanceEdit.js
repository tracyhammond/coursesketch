CourseSketch.AdvanceEditPanel = function() {

    /**
     * Holds the object with the specific functions that loads data.
     */
    var protoTypes = {};
    var actions = {};
    var actionFunctions = {};

    var IGNORE_FIELD = {special: 'N/A'};

    /**
     * Loads the data from the school item into the edit panel.
     *
     * @param {Protobuf} schoolItemData - A proto object containing the school item.
     * @param {Element} parentElement - a panel that displays the editable material.
     * @returns {Map} A map that contains the field of the school proto boject and the loaded value of the proto object.
     */
    function loadData(schoolItemData, parentElement) {
        var mappedInput = new Map();
        for (var property in schoolItemData) {
            if (schoolItemData.hasOwnProperty(property)) {
                var result;
                var element = parentElement.querySelectorAll('.need-loading[data-prop="' + property + '"]')[0];
                loadAction(schoolItemData, parentElement, property);
                if (!isUndefined(element)) {
                    var elementData = element.querySelectorAll('.data')[0];
                    result = loadIntoElement(elementData, schoolItemData[property], property);
                    if (!element.hasAttribute('data-hidden')) {
                        element.style.display = 'inherit';
                    }
                } else {
                    result = IGNORE_FIELD;
                }
                mappedInput.set(property, result);
            }
        }
        return mappedInput;
    }

    this.loadData = loadData;

    function loadAction(schoolItemData, parentElement, property) {
        var actionElement = parentElement.querySelectorAll('.need-action[data-actionProp="' + property + '"]')[0];
        if (isUndefined(actionElement)) {
            return;
        }
        actionElement.onclick = function() {
            actionFunctions[actionElement.getAttribute('data-action')](schoolItemData, actionElement, property);
        };
    }

    /**
     * Converts date and time elements to be merged into milliseconds
     *
     * @param dateInput
     * @param timeInput
     * @returns {Number}
     */
    function convertElementsToDateTime(dateInput, timeInput) {
        var milliseconds = new Date(dateInput.value + ' ' + timeInput.value).getTime();
        var date = CourseSketch.prutil.DateTime();
        date.setMillisecond('' + milliseconds);
        return date;
    }

    /**
     * Loads the data into a specific element.
     *
     * @param {Element} elementData - The element that the data needs to be set on
     * @param {*} schoolItemData - The value of the property that needs to be set
     * @param {String} property - The name of the field
     * @returns {*}
     */
    function loadIntoElement(elementData, schoolItemData, property) {
        /*jshint maxcomplexity:18 */
        if (protoTypes.hasOwnProperty(property + 'ProtoType')) {
            return loadSubObject(elementData, schoolItemData, property);
        }
        if (elementData.tagName === 'SPAN') {
            elementData.textContent = schoolItemData;
            if (isUndefined(schoolItemData) || schoolItemData === null) {
                // This is considered write only
                elementData.textContent = 'no information for this field';
            }
        } else if (elementData.tagName === 'DIV' && elementData.hasAttribute('data-date')) {
            schoolItemData = loadDate(elementData, schoolItemData);
        } else if (elementData.tagName === 'TEXTAREA' ||
            (elementData.tagName === 'INPUT' && elementData.type === 'text')) {
            if (!isUndefined(schoolItemData) && schoolItemData !== null) {
                elementData.value = schoolItemData;
            } else {
                elementData.value = '';
                schoolItemData = '';
            }
        } else if (elementData.tagName === 'INPUT' && elementData.type === 'number') {
            if (!isUndefined(schoolItemData) && schoolItemData !== null && !isNaN(schoolItemData)) {
                elementData.value = schoolItemData;
            } else {
                elementData.value = '';
            }
        } else if (elementData.tagName === 'INPUT' && elementData.type === 'checkbox') {
            if (isUndefined(schoolItemData) || schoolItemData === null) {
                schoolItemData = false;
            }
            elementData.checked = schoolItemData;
        } else if (elementData.tagName === 'SELECT') {
            if (!isUndefined(schoolItemData) && schoolItemData !== null) {
                elementData.options[schoolItemData].selected = true;
            } else {
                schoolItemData = elementData.selectedIndex;
            }
            elementData.style.display = 'inherit';
        } else if (elementData.tagName === 'DIV' && elementData.hasAttribute('data-list')) {
            loadListIntoElement(elementData, schoolItemData, property);
        }
        return schoolItemData;
    }

    this.loadDataIntoElement = loadIntoElement;

    function loadListIntoElement(elementData, schoolItemData, property) {
        var template = elementData.querySelector('.template');
        var templateNode = document.importNode(template.content, true);
        var templateDataNode = templateNode.querySelector('.templateData');
        for (var i = 0; i < schoolItemData.length; i++) {
            createListElement(i, schoolItemData[i], templateDataNode.cloneNode(true), elementData);
        }
    }

    function createListElement(index, schoolItemData, newNode, parent) {
        newNode.className = 'listItem' + index;
        newNode.setAttribute('data-list-item', index);
        parent.appendChild(newNode);
        newNode.querySelector('.listNumber').textContent = index + 1;
        loadData(schoolItemData, newNode);
    }

    function loadDate(elementData, schoolItemData) {
        var dateInput = elementData.querySelector('.date');
        var timeInput = elementData.querySelector('.time');

        var date;
        if (!isUndefined(schoolItemData) && schoolItemData !== null &&
            !isUndefined(schoolItemData.millisecond) && schoolItemData.millisecond !== null) {
            var milliseconds = '' + schoolItemData.millisecond;
            date = new Date(parseInt(milliseconds, 10));
            if ('' + date === 'Invalid Date') {
                date = new Date();
            }
        } else {
            date = new Date();
        }

        // format date for date input
        var day = ('0' + date.getDate()).slice(-2);
        var month = ('0' + (date.getMonth() + 1)).slice(-2);
        dateInput.value = date.getFullYear() + '-' + (month) + '-' + (day);

        // format hour for date input
        var hours = ('0' + date.getHours()).slice(-2);
        var minutes = ('0' + date.getMinutes()).slice(-2);
        var seconds = ('0' + date.getSeconds()).slice(-2);

        timeInput.value = hours + ':' + minutes + ':' + seconds;

        // The absolute date might actually be off by some milliseconds this fixes that potential offset
        return convertElementsToDateTime(dateInput, timeInput);
    }

    /**
     * Decodes and runs everything again on a sub object.
     *
     * @param {Element} elementData - The element that the data needs to be set on
     * @param {*} schoolItemData - The value of the property that needs to be set
     * @param {String} property - The name of the field
     * @returns {Map} a map of the original elements of the sub object
     */
    function loadSubObject(elementData, schoolItemData, property) {
        if (schoolItemData === null || isUndefined(schoolItemData)) {
            schoolItemData = protoTypes[property + 'ProtoType']();
        }
        return loadData(schoolItemData, elementData);
    }

    /**
     * Decodes the policy.
     *
     * @returns {LatePolicy} A protobuf object
     */
    protoTypes.latePolicyProtoType = function() {
        var latePolicy = CourseSketch.prutil.LatePolicy();
        latePolicy.subtractionType = 0;
        latePolicy.timeFrameType = 3;
        latePolicy.functionType = 0;
        return latePolicy;
    };

    /**
     * Decodes the policy.
     *
     * @returns {Problem} A protobuf object
     */
    protoTypes.problemProtoType = function() {
        var bankProblem = CourseSketch.prutil.SrlBankProblem();
        return bankProblem;
    };

    actions.createPart = function(srlProblem, buttonElement, property) {
        var buttonParent = buttonElement.parentNode;
        var elementsToRemove = buttonParent.querySelectorAll('[data-list-item]');
        for (var i = 0; i < elementsToRemove.length; i++) {
            elementsToRemove[i].parentNode.removeChild(elementsToRemove[i]);
        }
        loadData(srlProblem, buttonParent.parentNode);
    };

    /**
     * Does some special comparison.
     *
     * For example if the schoolItemData is null/undefined but the default element map contains something
     * Then if the result map is the same as the default element map we do not change anything.
     * @param {ProtobufObject} schoolItemData - A mpa containing the school item.
     * @param {Element} parentElement - a panel that displays the editable material.
     * @param {Map} originalData - A map of the data mapped to the element.
     * @return {Map} A map representing the modified data.
     */
    function getInput(schoolItemData, parentElement, originalData) {
        var result;
        var mappedOutput = new Map();
        for (var property in schoolItemData) {
            if (schoolItemData.hasOwnProperty(property)) {
                var element = parentElement.querySelectorAll('.need-saving[data-prop="' + property + '"]')[0];
                if (!isUndefined(element)) {
                    var elementData = element.querySelectorAll('.data')[0];

                    result = getDataFromElement(elementData, schoolItemData[property], property, originalData.get(property));
                } else {
                    if (originalData.get(property) === IGNORE_FIELD) {
                        result = IGNORE_FIELD;
                    } else {
                        result = schoolItemData[property];
                    }
                }
                mappedOutput.set(property, result);
                if (compareElements(property, schoolItemData, originalData.get(property), result)) {
                    setProtoData(schoolItemData, property, result);
                }
            }
        }
        return mappedOutput;
    }

    this.getInput = getInput;

    /**
     * Gets data from the element if they are the same.
     *
     * @param {Element} elementData
     * @param {*} schoolItemData
     * @param {String} property
     * @param {*} [originalData]
     */
    function getDataFromElement(elementData, schoolItemData, property, originalData) {
        if (protoTypes.hasOwnProperty(property + 'ProtoType')) {
            return saveSubObject(elementData, schoolItemData, property, originalData);
        } else if (elementData.tagName === 'DIV' && elementData.hasAttribute('data-date')) {
            var dateInput = elementData.querySelector('.date');
            var timeInput = elementData.querySelector('.time');

            // The absolute date might actually be off by some milliseconds this fixes that potential offset
            schoolItemData = convertElementsToDateTime(dateInput, timeInput);
        } else if (elementData.tagName === 'TEXTAREA' || (elementData.tagName === 'INPUT' &&
            (elementData.type === 'text'))) {
            schoolItemData = elementData.value;
        } else if (elementData.tagName === 'INPUT' && elementData.type === 'number') {
            schoolItemData = parseFloat(elementData.value);
            if (isNaN(schoolItemData)) {
                schoolItemData = null;
            }
        } else if (elementData.tagName === 'INPUT' && elementData.type === 'checkbox') {
            schoolItemData = elementData.checked;
        } else if (elementData.tagName === 'SELECT') {
            schoolItemData = elementData.selectedIndex;
        }
        return schoolItemData;
    }

    this.getDataFromElement = getDataFromElement;

    /**
     * @param elementData
     * @param schoolItemData
     * @param property
     * @param originalData
     */
    function saveSubObject(elementData, schoolItemData, property, originalData) {
        // Return a map if the result and the initial is the same
        // Otherwise return a protofbuf object
        if (schoolItemData === null || isUndefined(schoolItemData)) {
            schoolItemData = protoTypes[property + 'ProtoType']();
        }
        var result = getInput(schoolItemData, elementData, originalData);
        if (compareMaps(originalData, result)) {
            return result;
        } else {
            return schoolItemData;
        }
    }

    /**
     *
     * @param original
     * @param result
     * @returns {boolean} True if the maps are the same otherwise this will return false
     */
    function compareMaps(original, result) {
        var resultValue;
        if (original === result) {
            return true;
        }
        if (isUndefined(original) || isUndefined(result)) {
            return false;
        }
        if (original.size !== result.size) {
            return false;
        }
        var mapIter = original.entries();
        for (var i = 0; i < original.size; i++) {
            var entry = mapIter.next().value;
            var key = entry[0];
            var originalValue = entry[1];
            resultValue = result.get(key);
            if (resultValue === IGNORE_FIELD || originalValue === IGNORE_FIELD) {
                continue;
            }
            // in cases of an undefined value, make sure the key
            // actually exists on the object so there are no false positives
            if (resultValue === undefined && !result.has(key) || !compareValues(originalValue, resultValue)) {
                return false;
            }
        }
        return true;
    }

    this.compareMaps = compareMaps;

    /**
     * @param originalValue
     * @param newValue
     * @returns {boolean} True if the values are the same otherwise this will return false;
     */
    function compareValues(originalValue, newValue) {
        /*jshint maxcomplexity:16 */
        if (originalValue === newValue) {
            return true;
        }
        if (originalValue === null || newValue === null || isUndefined(originalValue || isUndefined(newValue))) {
            return false;
        }
        if (isFunction(originalValue.encode) && isFunction(newValue.encode)) {
            for (var property in originalValue) {
                if (originalValue.hasOwnProperty(property)) {
                    if (!compareValues(originalValue[property], newValue[property])) {
                        return false;
                    }
                }
            }
            return true;
        }
        if (getTypeName(originalValue) === 'Long' && getTypeName(newValue) === 'Long') {
            return ('' + originalValue) === ('' + newValue);
        }
        if (originalValue instanceof Map && newValue instanceof Map) {
            return compareMaps(originalValue, newValue);
        }
        if ((getTypeName(originalValue) === 'number' && getTypeName(newValue) === 'string') ||
            (getTypeName(originalValue) === 'string' && getTypeName(newValue) === 'number')) {
            return ('' + originalValue) === ('' + newValue);
        }
        if (isNaN(originalValue) && isNaN(newValue)) {
            return true;
        }
        return false;
    }

    /**
     * Does a comparison and then sets the result on the protobuf object if needed.
     *
     * @param property
     * @param schoolItemData
     * @param originalData
     * @param result
     */
    function compareElements(property, schoolItemData, originalData, result) {
        if (originalData === IGNORE_FIELD || result === IGNORE_FIELD) {
            return false;
        }
        if (schoolItemData[property] !== originalData) {
            if (!compareValues(originalData, result)) {
                return true;
            }
            return;
        }
        if (schoolItemData[property] !== result) {
            return true;
        }
    }

    function setProtoData(schoolItemData, property, result) {
        schoolItemData[property] = result;
    }

    /**
     * Removes the advance edit panel if the school item is removed.
     */
    if (!isUndefined(SchoolItem)) {
        SchoolItem.prototype.finalize = function() {
            if (!isUndefined(this.advanceEditPanel)) {
                if (this.advanceEditPanel.parentNode !== null) {
                    this.advanceEditPanel.parentNode.removeChild(this.advanceEditPanel);
                }
            }
        };
    }
    var getAdvanceEditPanel = this.getAdvanceEditPanel;

    function createAdvanceEditPanelElements(localElement, parentNode) {
        var host = document.createElement('div');
        host.className = 'advanceEditHost';
        host.style.height = '100%';
        host.style.display = 'none';

        // add html to host
        var shadow = host.createShadowRoot();
        var clone = getAdvanceEditPanel();
        clone = combineLists(clone, localElement);
        shadow.appendChild(clone);
        localElement.advanceEditPanel = host;

        // add our loaded element to the page.
        parentNode.appendChild(host);
        return [ host, shadow ];
    }

    this.createAdvanceEditPanelElements = createAdvanceEditPanelElements;

    /**
     * Adds actions from the outside to this.
     *
     * If there is an overlapping function then it creates a new function.
     * This new function calls in external function with an extra argument of a callback
     * This call back is the original function.
     *
     * @param sourceActions {Object} A map of function names to functions for actions.
     * @param destinationObject {Object} A map of function names to functions for actions.
     */
    function addActions(destinationObject, sourceActions) {
        for (var property in sourceActions) {
            if (destinationObject.hasOwnProperty(property)) {
                destinationObject[property] = (function(oldFunc, newFunc) {
                    return function() {
                        var args = Array.prototype.slice.call(arguments);
                        args.push(function() {
                            oldFunc.apply({}, arguments);
                        });
                        newFunc.apply({}, args);
                    };
                })(destinationObject[property], sourceActions[property]);
            } else {
                destinationObject[property] = sourceActions[property];
            }
        }
        return destinationObject;
    }

    /**
     * Sets up the advance edit panel for editing advance data.
     *
     * @param {SchoolItem} localElement -  The school item that this advance panel is associated with.
     * @param {Node} parentNode - The node that is a parent to the button.  This is used to get the school item after saving.
     * @param {Function} saveCallback - Called when the data is saved.
     * @param {Function} closeCallback - Called when the panel is closed.
     * @param {Object} externalActions - A list of actions mapped to function names.
     */

    this.createAdvanceEditPanel = function(localElement, parentNode,
                                           saveCallback, closeCallback, externalActions) {
        actionFunctions = addActions(addActions({}, actions), externalActions);

        // create host and position it
        var currentData;

        var result = createAdvanceEditPanelElements(localElement, parentNode);
        var host = result[0];
        var shadow = result[1];

        // save data
        //NOT WORKING, NEEDS TO BE MODIFIED
        var saveButton = host.shadowRoot.querySelector('.saveButton');
        Waves.attach(saveButton);
        /**
         * Called to save all of the input.
         */

        var schoolItem = localElement.schoolItemData;

        function saveData() {
            getInput(schoolItem, shadow, currentData);
            localElement.schoolItemData = schoolItem;
            console.log(schoolItem);
            saveCallback(localElement, schoolItem);
        }

        saveButton.onclick = function() {
            saveData();
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
            saveData();
            closeCallback();
        }

        var closeButton = shadow.querySelector('.closeButton');
        closeButton.onclick = close;
        Waves.attach(closeButton);
        document.body.addEventListener('click', close);

        // select the target node
        var target = host;

        // create an observer instance
        var observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.type === 'attributes') {
                    currentData = loadData(localElement.schoolItemData, host.shadowRoot);
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
    this.combineLists = combineLists;

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
        scriptButton.onclick = function() {
            var data = getInput(shadow);
            location.href = '/instructor/problemCreation/scriptEditor/scriptEditor.html';
        };
    }
};
