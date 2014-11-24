/**
 * Creates the text box dialog
 * The dialog is moveable and allows the creator to enter text to be displayed
 */
function TextBox() {
    var localScope; // This is set to the level of the custom element tag
    var finishedCallback;
    var loadedData = undefined;
    var shadowRoot = undefined;
    
    /**
     * @param textToRead {string} contains the text to be read
     * This function speaks the text using the meSpeak library
     */
    this.speakText = function(textToRead, callback) {
        meSpeak.speak(textToRead, callback);
    };
        
    /**
     * This is for making the dialog moveable with the interact.js library
     * It selects the created dialog and makes it draggable with no inertia
     * It also ignores click and drag from textareas and buttons within the dialog
     * The dragging is restricted to the area of the parentNode the dialog is created in
     */
    function enableDragging() {
        interact(shadowRoot.querySelector(".draggable"))
            .ignoreFrom("textarea, button")
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
                drag: localScope.parentNode,
                endOnly: false,
                elementRect: { top: 0, left: 0, bottom: 1, right: 1 }
            });
    }

    /**
     * @param {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        localScope = this; // This sets the variable to the level of the custom element tag
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        // Click action for the "X" that closes the dialog
        shadowRoot.querySelector("#closeButton").onclick = function() {
            if (shadowRoot.querySelector('#creatorText') != null) {
                localScope.saveData();
            }
            localScope.parentNode.removeChild(localScope);
        };
        
        if (shadowRoot.querySelector("#continueButton") != null) {
            shadowRoot.querySelector('#continueButton').onclick = shadowRoot.querySelector('#closeButton').onclick;
        }
        
        if (shadowRoot.querySelector("#speakText") != null) {
            shadowRoot.querySelector("#speakText").onclick = function() {
                localScope.speakText(shadowRoot.querySelector("#creatorText").value);
            };
        }
        enableDragging();
        
        this.loadData(loadedData);
    };

    this.setFinishedListener = function(listener) {
        finishedCallback = listener;
    };

    this.saveData = function() {
        var textBoxProto = CourseSketch.PROTOBUF_UTIL.ActionCreateTextBox();
        textBoxProto.setText(shadowRoot.querySelector('#creatorText').value);
        textBoxProto.setHeight($(shadowRoot.querySelector('#textBoxDialog')).height());
        textBoxProto.setWidth($(shadowRoot.querySelector('#textBoxDialog')).width());
        var dialog = shadowRoot.querySelector('#textBoxDialog');
        var x = $(shadowRoot.querySelector('#textBoxDialog').parentNode.host).position().left;
        var y = $(shadowRoot.querySelector('#textBoxDialog').parentNode.host).position().top;
        if (dialog.getAttribute('data-x') == null || dialog.getAttribute('data-y') == null) {
            textBoxProto.setX(x);
            textBoxProto.setY(y);
        } else {
            textBoxProto.setX(x + parseInt(shadowRoot.querySelector('#textBoxDialog').getAttribute('data-x')));
            textBoxProto.setY(y + parseInt(shadowRoot.querySelector('#textBoxDialog').getAttribute('data-y')));
        }
        var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX,true);
        command.setCommandData(textBoxProto.toArrayBuffer());
        this.getFinishedCallback()(command); // Gets finishedCallback and calls it with command as parameter
    };
    
    this.loadData = function(textBoxProto) {
        if (isUndefined(shadowRoot)) {
            loadedData = textBoxProto;
            return;
        }
        if (isUndefined(textBoxProto)) {
            return;
        }
        var node = shadowRoot.querySelector('#creatorText');
        var dialog = shadowRoot.querySelector('#textBoxDialog');
        if (node == null) {
            node = shadowRoot.querySelector('#viewText');
        }
        $(dialog).height(textBoxProto.getHeight());
        $(dialog).width(textBoxProto.getWidth());
        $(node).width(textBoxProto.getWidth());
        $(node).height(textBoxProto.getHeight() - 16);
        $(dialog).offset({ top: textBoxProto.getY(), left: textBoxProto.getX() });
        node.textContent = textBoxProto.getText();
        if (shadowRoot.querySelector("#textBoxDialog").style.display == "none") {
            localScope.speakText(textBoxProto.getText(), localScope.getFinishedCallback());
            localScope.parentNode.removeChild(localScope);
        }
    };

    this.getFinishedCallback = function() {
        return finishedCallback;
    };
}
TextBox.prototype = Object.create(HTMLDialogElement.prototype);
