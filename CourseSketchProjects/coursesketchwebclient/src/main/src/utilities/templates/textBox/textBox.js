/**
 * Creates the text box dialog
 * The dialog is moveable and allows the creator to enter text to be displayed
 */
function TextBox() {
    var saveCallback;
 
    /**
     * This is for making the dialog moveable with the interact.js library
     * It selects the created dialog and makes it draggable with no inertia
     * It also ignores click and drag from textareas and buttons within the dialog
     */
    function enableDragging(localscope) {
        console.log(localscope);
        interact(shadowRoot.querySelector("#textBoxDialog"))
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
                drag: localscope.parentNode,
                endOnly: false,
                elementRect: { top: 0, left: 0, bottom: 1, right: 1 }
            });
    }
    
    /**
     * @param {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        
        // Click action for the "X" that closes the dialog
        shadowRoot.querySelector("#closeButton").onclick = function() {
            localScope.parentNode.removeChild(localScope);
        };
        enableDragging(localScope);
    };
    
    this.setSaveListener = function(listener) {
        saveCallback = listener;
    };
    
    this.saveData = function() {
        var textBoxProto = CourseSketch.PROTOBUF_UTIL.ActionCreateTextBox();
        textBoxProto.setText(shadowRoot.querySelector('#creatorText').value);
        textBoxProto.setHeight($(shadowRoot.querySelector('#creatorText')).height());
        textBoxProto.setWidth($(shadowRoot.querySelector('#creatorText')).width());
        textBoxProto.setX(shadowRoot.querySelector('#textBoxDialog').getAttribute('data-x'));
        textBoxProto.setY(shadowRoot.querySelector('#textBoxDialog').getAttribute('data-y'));
        var command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_TEXTBOX,true);
        command.setCommandData(textBoxProto.toArrayBuffer());
        saveCallback(command);
    };
}

TextBox.prototype = Object.create(HTMLDialogElement.prototype);

