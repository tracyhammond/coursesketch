/**
 * This class creates the highlightText dialog
 * The dialog is moveable with a color selector and checkbox for turning highlight mode on and off
 * The highlighting is done by adding span tags and will not break any formatting across other tags
 */
function HighlightText() {
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
                    var newNode = document.createElement('span');
                    newNode.setAttribute('class', 'highlightedText');
                    newNode.setAttribute('style', 'background:' + backgroundColor + '; color:' + textColor);
                    
                    startXPath = getXPath(range.startContainer);
                    startOffset = range.startOffset;
                    endXPath = getXPath(range.endContainer);
                    endOffset = range.endOffset;

                    newNode.appendChild(range.extractContents());
                    range.insertNode(newNode);
                    
                } else {
                    alert("Please make a valid selection.") // Message for invalid selections
                }
            }
        }
    }
    
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
    
    
    function restoreSelection() {
            if (typeof window.getSelection != 'undefined') {
                var selection = window.getSelection();
                selection.removeAllRanges();
                var range = document.createRange();
                range.setStart(document.evaluate(startXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue, Number(startOffset));
                range.setEnd(document.evaluate(endXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue, Number(endOffset));
                selection.addRange(range);
                
                var newNode = document.createElement('span');
                newNode.setAttribute('class', 'highlightedText');
                newNode.setAttribute('style', 'background:' + backgroundColor + '; color:' + textColor);
                newNode.appendChild(range.extractContents());
                range.insertNode(newNode);
            }
    }
    
    this.unhighlightAll = function() {
        $(".highlightedText").contents().unwrap();
        document.normalize(); //Try to find a way to do this with node.normalize() (in case somebody highlights a bajillion things)
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
        backgroundColor = shadowRoot.querySelector("#backgroundColor").value;
        textColor = shadowRoot.querySelector("#textColor").value;
        startXPath, startOffset, endXPath, endOffset = undefined;
        
        $(document).on("mouseup", highlightText);
        
        // Binds or unbinds mouseup and the highlightText function based on the state of the highlightMode checkbox
        shadowRoot.querySelector("#highlightMode").onchange = function() {
            if (shadowRoot.querySelector("#highlightMode").checked) {
                $(document).on("mouseup", highlightText)
            } else {
                $(document).off("mouseup", highlightText);
            }
        };
            
        // Click action for the "X" that closes the dialog
        shadowRoot.querySelector("#closeButton").onclick = function() {
            $(document).off("mouseup", highlightText);
            localScope.parentNode.removeChild(localScope);
        };
        
        // Updates value of backgroundColor when the color selector value is changed by the user
        shadowRoot.querySelector("#backgroundColor").onchange = function() {
            backgroundColor = shadowRoot.querySelector("#backgroundColor").value;
        };
        
        // Updates value of textColor when the color selecor value is changed by the user
        shadowRoot.querySelector("#textColor").onchange = function() {
            textColor = shadowRoot.querySelector("#textColor").value;
        };
        
        enableDragging();
    };
    
    this.setFinishedListener = function(listener) {
        this.finishedCallback = listener;
    };

    // Saves Data for the proto message based on the position, height, width, and value of the text box
    this.saveData = function(event) {
        var highlightTextProto = CourseSketch.PROTOBUF_UTIL.ActionCreateHighlightText();
        highlightTextProto.setStartXPath(startXPath);
        highlightTextProto.setStartOffset(startOffset);
        highlightTextProto.setEndXPath(endXPath);
        highlightTextProto.setEndOffset(endOffset);
        highlightTextProto.setBackgroundColor(backgroundColor);
        highlightTextProto.setTextColor(textColor);

        // If the highlightText does not have an id, then a command has not been created for the highlightText
        if ((isUndefined(this.id) || this.id == null || this.id == "")) {
            this.command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_HIGHLIGHT_TEXT, true);
        }
        
        this.command.setCommandData(highlightTextProto.toArrayBuffer()); // Sets commandData for commandlist
        this.createdCommand = this.command;
        this.id = this.command.commandId;
        this.getFinishedCallback()(this.command, event, this.currentUpdate); // Gets finishedCallback and calls it with command as parameter
    };
    
    this.loadData = function(highlightTextProto) {
        if (isUndefined(shadowRoot)) {
            loadedData = highlightTextProto;
            return;
        }
        if (isUndefined(highlightTextProto)) {
            return;
        }
        
        var rangeStartNode = highlightTextProto.getStartXPath();
        var rangeStartOffset = highlightTextProto.getStartOffset();
        var rangeEndNode = highlightTextProto.getEndXPath();
        var rangeEndOffset = highlightTextProto.getEndOffset();
        var bgColor = highlightTextProto.getBackgroundColor();
        var fontColor = highlightTextProto.getTextColor();
        
        if (typeof window.getSelection != 'undefined') {
            var selection = window.getSelection();
            selection.removeAllRanges();
            var range = document.createRange();
            range.setStart(document.evaluate(rangeStartNode, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue, Number(rangeStartOffset));
            range.setEnd(document.evaluate(rangeEndNode, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue, Number(rangeEndOffset));
            selection.addRange(range);
            
            var newNode = document.createElement('span');
            newNode.setAttribute('class', 'highlightedText');
            newNode.setAttribute('style', 'background:' + bgColor + '; color:' + fontColor);
            newNode.appendChild(range.extractContents());
            range.insertNode(newNode);
        }
        
    };
    
    this.getFinishedCallback = function() {
        return this.finishedCallback;
    };
}

HighlightText.prototype.finishedCallback = undefined; // Defined by whoever implements this by using setFinishedListener().
HighlightText.prototype.createdCommand = undefined;
HighlightText.prototype = Object.create(HTMLDialogElement.prototype);
