/**
 * This class creates the highlightText dialog
 * The dialog is moveable with a color selector and checkbox for turning highlight mode on and off
 * The highlighting is done by adding span tags and will not break any formatting across other tags
 */
function HighlightText() {
    var loadedData = undefined; // Utilized if the element does not exist when loadData() is called
    var shadowRoot = undefined; // Used only to tell if the data is ready to be loaded.
    /**
     * This is for making the dialog moveable with the interact.js library
     * It selects the created dialog and makes it draggable with no inertia
     * It also ignores click and drag from textareas and buttons within the dialog
     */
    function enableDragging() {
        interact(shadowRoot.querySelector("#highlightTextDialog"))
            .ignoreFrom("button")
            .draggable({
                onmove: function (event) {
                    var target = event.target,
                        x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx,
                        y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;

                    target.style.webkitTransform =
                    target.style.transform =
                        'translate(' + x + 'px, ' + y + 'px)';

                    target.setAttribute('data-x', x);
                    target.setAttribute('data-y', y);
                },
                
            })
            .inertia(false)
            .restrict({
                drag: "parent",
                endOnly: true,
                elementRect: { top: 0, left: 0, bottom: 1, right: 1 }
        });
    }
    
    /**
     * @param children {array} represents the childNodes in the selected text.
     * @return {boolean} false if children contains nodes that are something other than #text or SPAN. True otherwise
     * If false then the text selected will not be highlighted
     * It will not be highlighted because it contains node types such as H2 and adding span tags will ruin the formatting of the text
     */
    function checkChildrenNodes(children) {
        for (i = 0; i < children.length; i++) {
            if (children[i].nodeName !== "#text" && children[i].nodeName !== "SPAN") {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Wraps the selected text in span tags with the color of the color selector
     * If the selection crosses elements, there is an alert to notify the user of an invalid selection
     * The selection must also contain characters (no alert for this)
     */
    function highlightText() {
        if (window.getSelection().type !== "None") {
            var myText = window.getSelection();
            var range = myText.getRangeAt();
            children = range.cloneContents().childNodes;
            if (myText.toString().length > 0) { // Makes sure the selection contains characters so blank span tags are not added
                if (checkChildrenNodes(children)) { // Makes sure adding span tags will not ruin the selected text formatting
                    var newNode = document.createElement('span'); // This node is created to encase the text in the highlighted color
                    newNode.setAttribute('class', 'highlightedText');
                    newNode.setAttribute('style', 'background:' + this.backgroundColor + '; color:' + this.textColor);
                    
                    // These values are used for saving data
                    this.startPath = getXPath(range.startContainer);
                    this.startOffset = range.startOffset;
                    this.endPath = getXPath(range.endContainer);
                    this.endOffset = range.endOffset;

                    newNode.appendChild(range.extractContents()); // Makes the new span tags have the contents of the selection
                    range.insertNode(newNode); // Inserts the new node to where the old range was
                    this.saveData();
                } else {
                    alert("Please make a valid selection."); // Message for invalid selections
                }
            }
        }
    }
    
    /**
     * @param node {node} is the node whose path we are getting
     * @param currentPath {string} is used within the function to append the previous sibling to the path
     * @return currentPath {string} is the XML Path of the input node
     */
    function getXPath (node, currentPath) {
        currentPath = currentPath || '';
        switch (node.nodeType) {
            case 3:
            case 4:
                return getXPath(node.parentNode, 'text()[' + (document.evaluate('preceding-sibling::text()', node, null,
                        XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null).snapshotLength + 1) + ']');
            case 1:
                return getXPath(node.parentNode, node.nodeName + '[' + (document.evaluate('preceding-sibling::' + node.nodeName, node, null,
                        XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null).snapshotLength + 1) + ']' + (currentPath ? '/' + currentPath : ''));
            case 9:
                return '/' + currentPath;
            default:
                return '';
        }
    }

    /**
     * @param {node} is a clone of the custom HTML template for highlighting text
     * This creates the element in the shadowRoot and turns highlight mode on by default
     * It tracks changes to the color selector and to the highlight mode checkbox
     * When color selector is changed, the color variable updates. 
     * When highlight checkbox is changed, the function for highlighting is bound or unbound to mouseup
     * Also enables dragging and sets the exit button to close the dialog
     */
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        this.backgroundColor = shadowRoot.querySelector("#backgroundColor").value;
        this.textColor = shadowRoot.querySelector("#textColor").value;
        this.startPath = undefined;
        this.startOffset = undefined;
        this.endPath = undefined;
        this.endOffset = undefined;
        this.highlightProto = undefined;

        // Binds or unbinds mouseup and the highlightText function based on the state of the highlightMode checkbox
        shadowRoot.querySelector("#highlightMode").onchange = function() {
            if (shadowRoot.querySelector("#highlightMode").checked) {
                $(document).on("mouseup", highlightText.bind(this));
            } else {
                $(document).off("mouseup", highlightText.bind(this));
            }
        }.bind(this); // Binds this so the highlightText function can write to variables (this is to define the correct scope)
            
        // Click action for the "X" that closes the dialog
        shadowRoot.querySelector("#closeButton").onclick = function() {
            if (confirm("You are about to permanently remove the highlighting from this step.")) {
                $(document).off("mouseup", highlightText); // Removes the bound mouseup event
                localScope.getFinishedCallback()(localScope.command, event, localScope.currentUpdate); // Gets and calls finishedCallback
            }
        };
        
        // Updates value of backgroundColor when the color selector value is changed by the user
        shadowRoot.querySelector("#backgroundColor").onchange = function() {
            localScope.backgroundColor = shadowRoot.querySelector("#backgroundColor").value;
        };
        
        // Updates value of textColor when the color selecor value is changed by the user
        shadowRoot.querySelector("#textColor").onchange = function() {
            localScope.textColor = shadowRoot.querySelector("#textColor").value;
        };
        
        enableDragging();
    };
    
    this.setFinishedListener = function(listener) {
        this.finishedCallback = listener;
    };
    
    /**
     * This saves data for the highlight tool
     * The highlight tool only appears once per tutorial step, but multiple selections is allowed
     * highlightProto corresponds with the tool, and thus only exists once per tutorial step
     * nodePathProto is the data needed to define a selection with its highlighted color. There can be multiple per tutorial step
     */
    this.saveData = function() {
        var nodePathProto = CourseSketch.PROTOBUF_UTIL.SelectedNodePath();
        if (isUndefined(this.highlightProto)) { // Defines highlightProto if it does not already exist
            this.highlightProto = CourseSketch.PROTOBUF_UTIL.ActionCreateHighlightText();
        }
        if (!isUndefined(this.startPath)) { // If startPath is defined, then saving will occur
            nodePathProto.setStartPath(this.startPath);
            nodePathProto.setStartOffset(this.startOffset);
            nodePathProto.setEndPath(this.endPath);
            nodePathProto.setEndOffset(this.endOffset);
            nodePathProto.setBackgroundColor(this.backgroundColor);
            nodePathProto.setTextColor(this.textColor);
            this.highlightProto.add('selectedNodePath', nodePathProto); // Adds nodePath to the end of the current highlightProto list
        }

        // If the highlightText does not have an id, then a command has not been created for the highlightText
        if ((isUndefined(this.id) || this.id == null || this.id == "")) {
            this.command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_HIGHLIGHT_TEXT, true);
        }
        this.command.setCommandData(this.highlightProto.toArrayBuffer()); // Sets commandData for commandlist
        this.createdCommand = this.command;
        this.id = this.command.commandId; // Sets highlightText id to the same as the commandId
        this.getFinishedCallback()(this.command, undefined, this.currentUpdate); // Gets finishedCallback and calls it with command as parameter
    };
    
    /**
     * This function loads data by recreating the node and then insert it into the webpage
     * @param protoData {protoCommand} is the CommandData to be loaded
     */
    this.loadData = function(protoData) {
        if (isUndefined(shadowRoot)) {
            loadedData = protoData;
            return;
        }
        if (isUndefined(protoData)) {
            return;
        }
        var nodes = protoData.getSelectedNodePath(); // This is a list of all the nodes to recreate
        this.highlightProto = protoData; // This sets highlightProto to the previous list so that you can add new selections in edit mode
        for (var i=0; i < nodes.length; i++) { // Goes through the list of nodes to recreate and recreates them
            var loadNode = nodes[i]; // The current node to be loaded
            var rangeStartNode = loadNode.getStartPath();
            var rangeStartOffset = loadNode.getStartOffset();
            var rangeEndNode = loadNode.getEndPath();
            var rangeEndOffset = loadNode.getEndOffset();
            var backgroundColor = loadNode.getBackgroundColor();
            var textColor = loadNode.getTextColor();

            // This recreates the node based on selection start/end node/offset and text/background colors
            if (!isUndefined(window.getSelection)) {
                var selection = window.getSelection();
                selection.removeAllRanges();
                var range = document.createRange();
                range.setStart(document.evaluate(rangeStartNode, document, null,
                        XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue, Number(rangeStartOffset));
                range.setEnd(document.evaluate(rangeEndNode, document, null,
                        XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue, Number(rangeEndOffset));
                selection.addRange(range);
            
                var newNode = document.createElement('span');
                newNode.setAttribute('class', 'highlightedText');
                newNode.setAttribute('style', 'background:' + backgroundColor + '; color:' + textColor);
                newNode.appendChild(range.extractContents());
                range.insertNode(newNode);
            }
        }
        
        // This will set the text/background color to the last used combination
        shadowRoot.querySelector('#textColor').value = textColor;
        shadowRoot.querySelector('#backgroundColor').value = backgroundColor;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    };
    
    this.getFinishedCallback = function() {
        return this.finishedCallback;
    };
}

HighlightText.prototype.finishedCallback = undefined; // Defined by whoever implements this by using setFinishedListener().
HighlightText.prototype.createdCommand = undefined;
HighlightText.prototype = Object.create(HTMLDialogElement.prototype);
