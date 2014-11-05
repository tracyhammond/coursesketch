/**
 * Creates the text to speech box dialog
 * The dialog is moveable and allows the user to enter and playback text to be spoken
 */
function TextToSpeech() {
    /** 
     * @param textToRead {string} contains the text to be read
     * This function speaks the text using the meSpeak library
     */
    this.speakText = function(textToRead) {
        meSpeak.speak(textToRead);
        };
    /**
     * This is for making the dialog moveable with the interact.js library
     * It selects the created dialog and makes it draggable with no inertia
     * It also ignores click and drag from textareas and buttons within the dialog
     */
    function enableDragging() {
        interact(shadowRoot.querySelector("#textToSpeechDialog"))
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
                drag: "parent",
                endOnly: true,
                elementRect: { top: 0, left: 0, bottom: 1, right: 1 }
        });
    }
    /**
     * @param {node} is a clone of the custom HTML Element for the text to speech box
     * This makes the "speak text" button call the speakText function with the user text as the parameter
     * Also makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        
        // Click action for the "Speak Text" button
        shadowRoot.querySelector("#speakText").onclick = function() {
            localScope.speakText(shadowRoot.querySelector("#creatorText").value);
        };
        
        // Click action for the "X" that closes the dialog
        shadowRoot.querySelector("#closeButton").onclick = function() {
            localScope.parentNode.removeChild(localScope);
        };
        enableDragging();
    };
}

TextToSpeech.prototype = Object.create(HTMLDialogElement.prototype);

