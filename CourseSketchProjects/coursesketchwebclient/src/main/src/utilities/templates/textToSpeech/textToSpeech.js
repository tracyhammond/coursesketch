function TextToSpeech() {
    /** @param textToRead {string} contains the text to be read
     * This function speaks the text using the meSpeak library
     */
    this.speakText = function(textToRead) {
        meSpeak.speak(textToRead);
        };
        
    // This makes the dialog moveable using the interact.js library
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

