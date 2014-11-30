(function() {

    /**
     * @param parent {ShadowRoot} the root of the parent.
     * @return {Map} A map of the data mapped to the element.
     */
    function getInput(parent) {
        var inputList = parent.querySelectorAll(".need-saving");
        console.log(inputList);
        for (var i = 0; i < inputList.length; i++) {
            var value = inputList[i].value;
            console.log(value);
        }
    }

    /**
     * yes I know these functions have an underscore.
     * This is so that you don't have to dynamically capitalize the first letter.
     */
    var loaderObject = {};
    loaderObject.load_name = function(schoolItemElement, schoolItemData, NodeToFill) {
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
            NodeToFill.value = name;
        }
    };

    loaderObject.load_description = function(schoolItemElement, schoolItemData, NodeToFill) {
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
            NodeToFill.value = description;
        }
    };

    loaderObject.load_id = function(schoolItemElement, schoolItemData, NodeToFill) {
        var id = "";
        if (isUndefined(schoolItemData)) {
            //console.log(schoolItemElement.id);
        }  else {
            console.log(schoolItemData.id);
            id = schoolItemData.id;
        }
        if (id != "") {
            NodeToFill.textContent = id;
        } else {
            NodeToFill.textContent = "No Id assigned yet";
        }
    };

    loaderObject.load_functionType = function(schoolItemElement, schoolItemData, NodeToFill) {
        var index = -1;
        if (isUndefined(schoolItemData)) {
        }  else {
            try {
                index = schoolItemData.latePolicy.functionType;
            } catch(exception) {
                console.log("Ignoring exception while setting function type of element");
                console.log(exception);
            }
        }
        if (index > 0 || index === 0) {
            NodeToFill.options[index].selected = true;
        }
    };

    loaderObject.load_timeFrameType = function(schoolItemElement, schoolItemData, NodeToFill) {
        var index = -1;
        if (isUndefined(schoolItemData)) {
        }  else {
            try {
                index = schoolItemData.latePolicy.timeFrameType;
            } catch(exception) {
                console.log("Ignoring exception while setting timeFrame type of element");
                console.log(exception);
            }
        }
        if (index > 0 || index === 0) {
            NodeToFill.options[index].selected = true;
        }
    };

    loaderObject.load_subtractionType = function(schoolItemElement, schoolItemData, NodeToFill) {
        var index = -1;
        if (isUndefined(schoolItemData)) {
        }  else {
            try {
                index = schoolItemData.latePolicy.subtractionType;
            } catch(exception) {
                console.log("Ignoring exception while setting subtraction type of element");
                console.log(exception);
            }
        }
        if (index > 0 || index === 0) {
            NodeToFill.options[index].selected = true;
        }
    };

    /**
     * @param parent {ShadowRoot} the root of the parent.
     * @return {Map} A map of the data mapped to the element.
     */
    function loadData(schoolItemElement, schoolItemData, editPanel) {
        var inputList = editPanel.querySelectorAll(".need-loading");
        console.log(inputList);
        for (var i = 0; i < inputList.length; i++) {
            console.log(inputList[i].dataset.prop);
            loaderObject['load_' + inputList[i].dataset.prop](schoolItemElement, schoolItemData, inputList[i]);
        }
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

            loadData(localScope, localScope.schoolItemData, shadow);

            // add our loaded element to the page.
            document.body.appendChild(host);

            // save data
            var saveButton = shadow.querySelector("button.save");
            saveButton.onclick = function() {
                getInput(shadow);
                var schoolItem = localScope.getParentParent(parentNode);
                document.body.removeChild(host);
                localScope.editFunction("advance", [], [], schoolItem);
                alert("Saving data!");
            };
        });
    }
})();
