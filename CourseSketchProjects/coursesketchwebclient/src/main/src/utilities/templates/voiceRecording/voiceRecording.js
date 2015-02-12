function VoiceRecording() {
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        this.shadowRoot.querySelector("#recordBtn").onclick = function() {
            if (this.voiceBtnBool == true) {
                clearInterval(this.voiceBtnTimer);
                this.voiceBtnBool = false;
                this.shadowRoot.querySelector('#recordBtn').value = " ";
            }
            else {
                this.blinking(this.shadowRoot.querySelector("#recordBtn"));
                this.voiceBtnBool = true;
            }
        }.bind(this);

        this.blinking = function(elm) {
            this.blink = function() {
                this.shadowRoot.querySelector('#recordBtn').value = "REC";
                elm.fadeOut(400, function() {
                    elm.fadeIn(400);
                });
            }
            this.voiceBtnTimer = setInterval(this.blink, 10);
        }
    }
}

VoiceRecording.prototype = Object.create(HTMLElement.prototype);

