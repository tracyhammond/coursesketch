// jscs:disable jsDoc
(function() {

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
     * loads a name object from the schoolItem.
     *
     * @return {Undefined|String} the name or undefined.
     */
    loaderObject.load_name = function(schoolItemElement, schoolItemData, nodeToFill) {
        var name = '';
        if (isUndefined(schoolItemData)) {
            try {
                // grabs the content element then gets the first inserted node.
                name = schoolItemElement.shadowRoot.querySelector('.name content').getDistributedNodes()[0].textContent;
            } catch (exception) {
                console.log('Ignoring exception while setting name of element');
                console.log(exception);
            }
        } else {
            name = schoolItemData.name;
        }
        if (name !== '') {
            nodeToFill.value = name;
            return name;
        }
        return undefined;
    };

    /**
     * Yes I know these functions have an underscore.
     * This is so that you don't have to dynamically capitalize the first letter.
     * Each one returns a value if it exist otherwise undefined is returned.
     * <br>
     * loads a name object from the schoolItem.
     *
     * @return {Undefined|String} undefined or the description.
     */
    loaderObject.load_description = function(schoolItemElement, schoolItemData, nodeToFill) {
        var description = '';
        if (isUndefined(schoolItemData)) {
            try {
                // grabs the content element then gets the first inserted node.
                description = schoolItemElement.shadowRoot.querySelector('.description content').getDistributedNodes()[0].textContent;
            } catch (exception) {
                console.log('Ignoring exception while setting description of element');
                console.log(exception);
            }
        } else {
            description = schoolItemData.description;
        }
        if (description !== '') {
            nodeToFill.value = description;
            return description;
        }
        return undefined;
    };

    /**
     * Yes I know these functions have an underscore.
     * This is so that you don't have to dynamically capitalize the first letter.
     * Each one returns a value if it exist otherwise undefined is returned.
     * <br>
     * loads a name object from the schoolItem.
     *
     * @return {Null} This returns null to differentiate it from other possible values as this is not saveable.
     */
    loaderObject.load_id = function(schoolItemElement, schoolItemData, nodeToFill) {
        var id = '';
        if (isUndefined(schoolItemData)) {
            console.log(schoolItemElement.id);
        } else {
            id = schoolItemData.id;
        }
        if (id !== '') {
            nodeToFill.textContent = id;
        } else {
            nodeToFill.textContent = 'No Id assigned yet';
        }
        return null;
    };

    /**
     * Yes I know these functions have an underscore.
     * This is so that you don't have to dynamically capitalize the first letter.
     * Each one returns a value if it exist otherwise undefined is returned.
     * <br>
     * loads a functiontype object from the schoolItem.
     *
     * @return {Null|Undefined|Enum} either undefined or the value of the enum.
     */
    loaderObject.load_functionType = function(schoolItemElement, schoolItemData, nodeToFill) {
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
    loaderObject.load_timeFrameType = function(schoolItemElement, schoolItemData, nodeToFill) {
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
    loaderObject.load_subtractionType = function(schoolItemElement, schoolItemData, nodeToFill) {
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

    /**
     * Yes I know these functions have an underscore.
     * This is so that you don't have to dynamically capitalize the first letter.
     * Each one returns a value if it exist otherwise undefined is returned.
     * <br>
     * loads an Assignment type object from the schoolItem.
     *
     * @return {Undefined|Enum} either undefined or the value of the enum.
     */
    loaderObject.load_assignmentType = function(schoolItemElement, schoolItemData, nodeToFill) {
        var index = -1;
        if (!isUndefined(schoolItemData)) {
            index = schoolItemData.assignmentType;
        }
        if (index > 0 || index === 0) {
            if (index > 6) {
                index = 6;
            }
            nodeToFill.options[index].selected = true;
            return nodeToFill.value;
        }
        return undefined;
    };

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
        for (var i = 0; i < inputList.length; i++) {
            var result = loaderObject['load_' + inputList[i].dataset.prop](schoolItemElement, schoolItemData, inputList[i]);
            if (result !== null) {
                mappedInput.set(inputList[i].dataset.prop, result);
            }
        }
        return mappedInput;
    }

    /**
     * Removes the advance edit panel if the school item is removed.
     */
    SchoolItem.prototype.finalize = function() {
        if (!isUndefined(this.advanceEditPanel)) {
            if (this.advanceEditPanel.parentNode !== null) {
                this.advanceEditPanel.parentNode.removeChild(this.advanceEditPanel);
            }
        }
    };

    /**
     * Sets up the advance edit panel for editing advance data.
     *
     * @param {Element} element - The edit button that opens up the panel when clicked.
     * @param {SchoolItem} localElement -  The school item that this advance panel is associated with.
     * @param {Node} parentNode - The node that is a parent to the button.  This is used to get the school item after saving.
     */
    SchoolItem.prototype.createAdvanceEditPanel = function(element, localElement, parentNode) {
        $(element).click(function(event) {
            event.stopPropagation();

            // create host and position it
            var host = document.createElement('dialog');
            host.className = 'advanceEditHost';
            var pos = $(localElement).offset();
            var leftPos = (pos.left + $(localElement).width());
            $(host).offset({ top: pos.top, left: leftPos });

            // add html to host
            var shadow = host.createShadowRoot();
            var clone = localElement.getAdvanceEditPanel();
            shadow.appendChild(clone);
            localElement.advanceEditPanel = host;

            var currentData = loadData(localElement, localElement.schoolItemData, shadow);

            // add our loaded element to the page.
            document.body.appendChild(host);
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
                location.href = '/src/instructor/problemCreation/scriptEditor/scriptEditor.html';
            };

            // save data
            //NOT WORKING, NEEDS TO BE MODIFIED
            var saveButton = shadow.querySelector('button.save');
            /**
             * Called to save all of the input.
             */
            saveButton.onclick = function() {
                var newData = getInput(shadow);
                var schoolItem = getHostElement(parentNode);
                document.body.removeChild(host);
                console.log(schoolItem);
                console.log(localElement);
                schoolItem.editFunction('advance', currentData, newData, schoolItem);
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
        });
    };
})();
