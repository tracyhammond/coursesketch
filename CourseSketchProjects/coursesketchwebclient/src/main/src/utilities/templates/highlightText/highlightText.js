function HighlightText() {
    // This makes the dialog moveable using the interact.js library
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
    
    // Wraps the selected text in span tags with color of the color selector
    function highlightText() {
        if (window.getSelection().type !== "None") {
            var myText = window.getSelection();
            var range = myText.getRangeAt();
            children = range.cloneContents().childNodes;
            
            // Makes sure the selection contains characters so blank span tags are not added
            if (myText.toString().length > 0) {
                // Makes sure adding span tags will not ruin the selected text formatting
                if (checkChildrenNodes(children)) {
                    var newNode = document.createElement('span');
                    newNode.setAttribute('class', 'highlightedText');
                    newNode.setAttribute('style', 'color:' + highlightColor);
                    newNode.appendChild(range.extractContents());
                    range.insertNode(newNode);
                } else {
                    // Message for a selection that is not valid
                    alert("Please make a valid selection.")
                }
            }
        }
    }
    
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        highlightColor = shadowRoot.querySelector("#highlightColor").value;
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
            localScope.parentNode.removeChild(localScope);
        };
        
        // Updates value of highlightColor when the color selector value is changed by the user
        shadowRoot.querySelector("#highlightColor").onchange = function() {
            highlightColor = shadowRoot.querySelector("#highlightColor").value;
        };
        
        enableDragging();
    };
}

HighlightText.prototype = Object.create(HTMLDialogElement.prototype);
