/**
 *
 * @constructor AdvanceEditPanel
 */
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
     * @param {Protobuf} protobufObject - A proto object containing the school item.
     * @param {Element} parentElement - a panel that displays the editable material.
     * @returns {Map} A map that contains the field of the school proto boject and the loaded value of the proto object.
     */
    function loadData(protobufObject, parentElement) {
        var mappedInput = new Map();
        for (var property in protobufObject) {
            if (protobufObject.hasOwnProperty(property)) {
                var result;
                var element = parentElement.querySelectorAll('.need-loading[data-prop="' + property + '"]')[0];
                loadAction(protobufObject, parentElement, property);
                if (!isUndefined(element)) {
                    var elementData = element.querySelectorAll(':scope > .data')[0];
                    result = loadIntoElement(elementData, protobufObject[property], property);
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

    /**
     * Returns the top level parent that matches this selector.
     *
     * @param {Element} node - The node that is looking for its parent.
     * @param {String} selector - The selector used to match the parent.
     * @returns {Element} - An element that is a parent of node.
     */
    function getMatchingParent(node, selector) {
        var compareNode = node;
        while (compareNode.parentNode !== null &&
            compareNode.parentNode !== document.body &&
            !compareNode.matches(selector)) {
            compareNode = compareNode.parentNode;
        }
        return compareNode;
    }
    this.getMatchingParent = getMatchingParent;

    /**
     * Loads an action into the element.
     *
     * @param {ProtobufObject} protobufObject - Data that is being attached to the action.
     * @param {Element} parentElement - The parent element.
     * @param {String} property - A property of schoolItemData.
     */
    function loadAction(protobufObject, parentElement, property) {
        var actionElement = parentElement.querySelectorAll('.need-action[data-actionProp="' + property + '"]')[0];
        if (isUndefined(actionElement)) {
            return;
        }
        var superParent = getMatchingParent(parentElement, 'collapsible-body');
        actionElement.onclick = function() {
            actionFunctions[actionElement.getAttribute('data-action')](protobufObject, actionElement, [ property,
                parentElement, superParent ]);
        };
    }

    /**
     * Converts date and time elements to be merged into milliseconds
     *
     * @param {Element} dateInput - A date Input element.
     * @param {Element} timeInput - A time Input element.
     * @returns {Number} The date in milliseconds.
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
     * @param {Element} elementData - The element that the data needs to be set on.
     * @param {*} schoolItemData - The value of the property that needs to be set.
     * @param {String} property - The name of the field.
     * @returns {*} The equivalent of what would be grabbed by the field if not edited.
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
            return loadListIntoElement(elementData, schoolItemData, property);
        } else if (elementData.tagName === 'DIV' && elementData.hasAttribute('data-lower')) {
            // Says this should look lower into this element
            schoolItemData = loadIntoElement(elementData.querySelector('.data'), schoolItemData, property);
        }
        return schoolItemData;
    }

    this.loadDataIntoElement = loadIntoElement;

    /**
     * Loads a list proto into the html.
     *
     * @param {Element} elementData - The parent element.
     * @param {ProtobufObject} schoolItemData - Data that is being attached to the action.
     * @param {String} property - A property of schoolItemData.
     * @returns {Array} a list of how the data was loaded into the element.
     */
    function loadListIntoElement(elementData, schoolItemData, property) {
        var template = elementData.querySelector('.template');
        var templateNode = document.importNode(template.content, true);
        var templateDataNode = templateNode.querySelector('.templateData');
        var returnedList = [];
        for (var i = 0; i < schoolItemData.length; i++) {
            var result = createListElement(i, schoolItemData[i], templateDataNode.cloneNode(true), elementData);
            returnedList.push(result);
        }
        return returnedList;
    }

    /**
     * Creates a new list element and loads the data for it.
     *
     * @param {Number} index - The index of the element in the list.
     * @param {ProtobufObject} protobufData - Data that is being attached to the action.
     * @param {Element} newNode - The existing node
     * @param {Element} parent - The parent element.
     * @returns {*} The equivalent of what would be grabbed by the field if not edited.
     */
    function createListElement(index, protobufData, newNode, parent) {
        newNode.classList.remove('templateData');
        newNode.className += ' listItem' + index;
        newNode.setAttribute('data-list-item', index);
        parent.appendChild(newNode);
        newNode.querySelector('.listNumber').textContent = index + 1;
        var elements = newNode.querySelectorAll('[data-list-item-id');
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            var field = element.getAttribute('data-list-item-id');
            var prefix = '';
            if (element.hasAttribute('data-list-item-prefix')) {
                prefix = element.getAttribute('data-list-item-prefix');
            }
            element[field] = prefix + (index);
        }
        return loadData(protobufData, newNode);
    }

    /**
     * Loads date into an element
     * @param {Element} elementData - The parent element.
     * @param {DateTime} schoolItemData - Data that is being attached to the action.
     * @returns {Number} The date in milliseconds.
     */
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
     *
     * @param {ProtobufObject} schoolItemData - A mpa containing the school item.
     * @param {Element} parentElement - a panel that displays the editable material.
     * @param {Map} originalData - A map of the data mapped to the element.
     * @returns {Map} A map representing the modified data.
     */
    function getInput(schoolItemData, parentElement, originalData) {
        var result;
        var mappedOutput = new Map();
        for (var property in schoolItemData) {
            if (schoolItemData.hasOwnProperty(property)) {
                var element = parentElement.querySelectorAll('.need-saving[data-prop="' + property + '"]')[0];
                if (!isUndefined(element)) {
                    var elementData = element.querySelectorAll('.data')[0];
                    var subMapData = new Map();
                    if (!isUndefined(originalData)) {
                        subMapData = originalData.get(property);
                    }

                    result = getDataFromElement(elementData, schoolItemData[property], property, subMapData);
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
     * @param {Element} elementData - The element the data is being loaded from.
     * @param {*} schoolItemData - Data that is being loaded into.
     * @param {String} property - A property of schoolItemData.
     * @param {*} [originalData] - A map of the original data.
     * @returns {*} The data loaded from the element.
     */
    function getDataFromElement(elementData, schoolItemData, property, originalData) {
        /*jshint maxcomplexity:13 */
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
        } else if (elementData.tagName === 'DIV' && elementData.hasAttribute('data-lower')) {
            // Says this should look lower into this element
            schoolItemData = getDataFromElement(elementData.querySelector('.data'), schoolItemData, property, originalData);
        } else if (elementData.tagName === 'DIV' && elementData.hasAttribute('data-list')) {
            schoolItemData = getDataFromList(elementData, schoolItemData, property, originalData);
        }
        return schoolItemData;
    }

    this.getDataFromElement = getDataFromElement;

    /**
     * Gets data from the element if they are the same.
     *
     * It is expecting the data to be in a list though.
     * @param {Element} elementData - The element the data is being loaded from.
     * @param {*} schoolItemData - Data that is being loaded into.
     * @param {String} property - A property of schoolItemData.
     * @param {*} [originalData] - A map of the original data.
     * @returns {*} The data loaded from the element.
     */
    function getDataFromList(elementData, schoolItemData, property, originalData) {
        var elements = elementData.querySelectorAll(':scope > [data-list-item]');
        var resultData = [];
        for (var i = 0; i < elements.length; i++) {
            var arraySchoolItemData;
            if (schoolItemData.length <= i && i > 0) {
                arraySchoolItemData = CourseSketch.prutil.createNewProtobufInstanceFromInstance(schoolItemData[i]);
            } else {
                arraySchoolItemData = schoolItemData[i];
            }
            resultData.push(getInput(arraySchoolItemData, elements[i], originalData[i]));
        }

        if (compareValues(originalData, resultData)) {
            return resultData;
        } else {
            return schoolItemData;
        }
    }

    /**
     * @param {Element} elementData - The element the data is being loaded from.
     * @param {*} schoolItemData - Data that is being split into sub objects.
     * @param {String} property - A property of schoolItemData.
     * @param {*} originalData - A map of the original data.
     * @returns {Map | *} A map or a protobuf object if they are different.
     */
    function saveSubObject(elementData, schoolItemData, property, originalData) {
        // Return a map if the result and the initial is the same
        // Otherwise return a protofbuf object
        if (schoolItemData === null || isUndefined(schoolItemData)) {
            schoolItemData = protoTypes[property + 'ProtoType']();
        }
        var result = getInput(schoolItemData, elementData, originalData);
        if (compareValues(originalData, result)) {
            return result;
        } else {
            return schoolItemData;
        }
    }

    /**
     * Compares two maps. Returns true if they are equal.
     *
     * @param {Map} original - The original map.
     * @param {Map} result - The new map.
     * @returns {Boolean} True if the maps are the same otherwise this will return false
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
     * @param {*} originalValue - The original value.
     * @param {*} newValue - The new value.
     * @returns {Boolean} True if the values are the same otherwise this will return false;
     */
    function compareValues(originalValue, newValue) {
        /*jshint maxcomplexity:21 */
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
        if (getTypeName(originalValue) === 'string' && getTypeName(newValue) === 'string') {
            return false;
        }
        if (isArray(originalValue) && isArray(newValue)) {
            if (originalValue.length !== newValue.length) {
                return false;
            }
            for (var i = 0; i < originalValue.length; i++) {
                if (!compareValues(originalValue[i], newValue[i])) {
                    return false;
                }
            }
            return true;
        }

        if (getTypeName(originalValue) === getTypeName(newValue)) {
            return false;
        }

        // Should always be the very last one!
        if (isNaN(originalValue) && isNaN(newValue)) {
            return true;
        }
        return false;
    }

    /**
     * Does a comparison and then sets the result on the protobuf object if needed.
     *
     * @param {String} property - The property where the elements are being compared.
     * @param {*} schoolItemData - The data contained within the school item.
     * @param {*} originalData - The data from the original Map.
     * @param {*} result - The data from the result Map.
     * @returns {Boolean} True if the values are the same otherwise this will return false;
     */
    function compareElements(property, schoolItemData, originalData, result) {
        if (originalData === IGNORE_FIELD || result === IGNORE_FIELD) {
            return false;
        }
        if (schoolItemData[property] !== originalData) {
            return !compareValues(originalData, result);
        }
        if (schoolItemData[property] !== result) {
            return true;
        }
    }

    /**
     * Sets the proto data to the result.
     *
     * @param {*} schoolItemData - The item the data is being set on.
     * @param {String} property - The property at which the data is being set.
     * @param {*} result - The data that is being set.
     */
    function setProtoData(schoolItemData, property, result) {
        schoolItemData[property] = result;
    }

    var getAdvanceEditPanel = this.getAdvanceEditPanel;

    /**
     * Creates the panel elements and adds them to the DOM.
     *
     * @param {Element} localElement - An element that holds the list of elements where the templates are.
     * @param {Element} parentNode - The node where the panel is added to.
     * @returns {{Element, Node}} The host and its shadow.
     */
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
     * @param {Object} destinationObject - A map of function names to functions for actions.
     * @param {Object} sourceActions - A map of function names to functions for actions.
     * @returns {Object} The merged object.
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
     * Sets the actions locally.
     *
     * @param {Object} externalActions - Contains a map of functions to act upon.
     */
    function setActions(externalActions) {
        actionFunctions = addActions(addActions({}, actions), externalActions);
    }
    this.setActions = setActions;

    /**
     * Sets up the advance edit panel for editing advance data.
     *
     * @param {SchoolItem} localElement -  The school item that this advance panel is associated with.
     * @param {Node} parentNode - The node that is a parent to the button.  This is used to get the school item after saving.
     * @param {Function} saveCallback - Called when the data is saved.
     * @param {Function} closeCallback - Called when the panel is closed.
     * @param {Object} externalActions - A list of actions mapped to function names.
     * @returns {Element} The host element.
     */
    this.createAdvanceEditPanel = function(localElement, parentNode,
                                           saveCallback, closeCallback, externalActions) {

        setActions(externalActions);
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

        /**
         * Saves the data when clicked.
         */
        function saveData() {
            var resultData = getInput(schoolItem, shadow, currentData);
            localElement.schoolItemData = schoolItem;
            console.log(schoolItem);
            saveCallback(localElement, schoolItem, compareMaps(currentData, resultData));
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
                    var idList = host.shadowRoot.querySelectorAll('span.data[data-id]');
                    for (var i = 0; i < idList.length; i++) {
                        idList[i].textContent = localElement.schoolItemData.id;
                    }
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
     * Merges templates into the main element.
     *
     * @param {Element} clone - A list of the templates.
     * @param {Element} schoolItemElement - An element that represents a school item.
     * @returns {Element} The clone element.
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

    /**
     * Ignore
     */
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
            location.href = '/instructor/problem/scriptEditor/scriptEditor.html';
        };
    }
};
