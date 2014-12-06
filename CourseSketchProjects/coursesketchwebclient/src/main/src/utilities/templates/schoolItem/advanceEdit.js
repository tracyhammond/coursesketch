(function() {

    /**
     * @param parent {ShadowRoot} the root of the parent.
     * @return {Map} A map of the data mapped to the element.
     */
    function getInput(parent) {
        var inputList = parent.querySelectorAll(".need-saving");
        var mappedInput = new Map();
        for (var i = 0; i < inputList.length; i++) {
            var value = inputList[i].value;
            mappedInput.set(inputList.dataset.prop, value);
        }
        return mappedInput;
    }

    /**
     * yes I know these functions have an underscore.
     * This is so that you don't have to dynamically capitalize the first letter.
     * Each one returns a value if it exist otherwise undefined is returned.
     */
    var loaderObject = {};
    loaderObject.load_name = function(schoolItemElement, schoolItemData, nodeToFill) {
        var name = "";
        if (isUndefined(schoolItemData)) {
            try {
                // grabs the content element then gets the first inserted node.
                name = schoolItemElement.shadowRoot.querySelector(".name content").getDistributedNodes()[0].textContent;
            } catch(exception) {
                console.log("Ignoring exception while setting name of element");
                console.log(exception);
            }
        } else {
            name = schoolItemData.name;
        }
        if (name != "") {
            nodeToFill.value = name;
            return name;
        }
        return undefined;
    };

    loaderObject.load_description = function(schoolItemElement, schoolItemData, nodeToFill) {
        var description = "";
        if (isUndefined(schoolItemData)) {
            try {
                // grabs the content element then gets the first inserted node.
                description = schoolItemElement.shadowRoot.querySelector(".description content").getDistributedNodes()[0].textContent;
            } catch(exception) {
                console.log("Ignoring exception while setting description of element");
                console.log(exception);
            }
        } else {
            description = schoolItemData.description;
        }
        if (description != "") {
            nodeToFill.value = description;
            return description;
        }
        return undefined;
    };

    /**
     * @return null. This returns null to differentiate it from other possible values as this is not saveable.
     */
    loaderObject.load_id = function(schoolItemElement, schoolItemData, nodeToFill) {
        var id = "";
        if (isUndefined(schoolItemData)) {
            //console.log(schoolItemElement.id);
        }  else {
            id = schoolItemData.id;
        }
        if (id != "") {
            nodeToFill.textContent = id;
        } else {
            nodeToFill.textContent = "No Id assigned yet";
        }
        return null;
    };

    loaderObject.load_functionType = function(schoolItemElement, schoolItemData, nodeToFill) {
        var index = -1;
        if (isUndefined(schoolItemData)) {
        }  else {
            try {
                index = schoolItemData.latePolicy.functionType;
            } catch(exception) {
                console.log("Ignoring exception while setting function type of element");
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

    loaderObject.load_timeFrameType = function(schoolItemElement, schoolItemData, nodeToFill) {
        var index = -1;
        if (isUndefined(schoolItemData)) {
        }  else {
            try {
                index = schoolItemData.latePolicy.timeFrameType;
            } catch(exception) {
                console.log("Ignoring exception while setting timeFrame type of element");
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

    loaderObject.load_subtractionType = function(schoolItemElement, schoolItemData, nodeToFill) {
        var index = -1;
        if (isUndefined(schoolItemData)) {
        }  else {
            try {
                index = schoolItemData.latePolicy.subtractionType;
            } catch(exception) {
                console.log("Ignoring exception while setting subtraction type of element");
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
     * @param parent {ShadowRoot} the root of the parent.
     * @return {Map} A map of the data mapped to the element.
     */
    function loadData(schoolItemElement, schoolItemData, editPanel) {
        var inputList = editPanel.querySelectorAll(".need-loading");
        var mappedInput = new Map();
        for (var i = 0; i < inputList.length; i++) {
            var result = loaderObject['load_' + inputList[i].dataset.prop](schoolItemElement, schoolItemData, inputList[i]);
            if (result != null) {
                mappedInput.set(inputList[i].dataset.prop, result);
            }
        }
        return mappedInput;
    }

    SchoolItem.prototype.finalize = function() {
        if (!isUndefined(this.advanceEditPanel)) {
            if (this.advanceEditPanel.parentNode != null) {
                this.advanceEditPanel.parentNode.removeChild(this.advanceEditPanel);
            }
        }
    };

    /**
     * Sets up the advance edit panel for editing advance data.
     * @param element {Element} The edit button that opens up the panel when clicked.
     * @param localScope {SchoolItem} The school item that this advance panel is associated with.
     * @param parentNode {Node} The node that is a parent to the button.  This is used to get the school item after saving.
     */
    SchoolItem.prototype.createAdvanceEditPanel = function(element, localScope, parentNode) {
        $(element).click(function(event) {
            event.stopPropagation();

            // create host and position it
            var host = document.createElement("dialog");
            host.className = "advanceEditHost";
            var pos = $(localScope).offset();
            var leftPos = (pos.left + $(localScope).width());
            $(host).offset({top:pos.top, left:leftPos});

            // add html to host
            var shadow = host.createShadowRoot();
            var clone = localScope.getAdvanceEditPanel();
            shadow.appendChild(clone);
            localScope.advanceEditPanel = host;

            var currentData = loadData(localScope, localScope.schoolItemData, shadow);

            // add our loaded element to the page.
            document.body.appendChild(host);

            // save data
            var saveButton = shadow.querySelector("button.save");
            saveButton.onclick = function() {
                var newData = getInput(shadow);
                var schoolItem = localScope.getParentParent(parentNode);
                document.body.removeChild(host);
                localScope.editFunction("advance", currentData, newData, schoolItem);
                alert("Saving data!");
            };

            // cancel!
            function close(event) {
                event.stopPropagation();
                document.body.removeEventListener("click", close);
                try {
                    document.body.removeChild(host);
                } catch(exception) {
                    // ignored if this throws an error.
                }
            };

            shadow.querySelector("button.closeButton").onclick = close;
            document.body.addEventListener("click", close);
        });
    }
})();
