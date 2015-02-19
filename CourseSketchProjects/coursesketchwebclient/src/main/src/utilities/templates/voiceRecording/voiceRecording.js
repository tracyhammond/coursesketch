function VoiceRecording() {
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        this.shadowRoot.querySelector("#recordBtn").onclick = function() {
            if (this.isBlinking) {
                clearInterval(this.voiceBtnTimer);
                this.isBlinking = false;
                this.shadowRoot.querySelector('#recordBtn').value = " ";
            }
            else {
                this.blinking($(this.shadowRoot.querySelector("#recordBtn")));
                this.isBlinking = true;
            }
        }.bind(this);

        this.blinking = function(elm) {
            this.blink = function() {
                this.shadowRoot.querySelector('#recordBtn').value = "REC";
                elm.fadeOut(400, function() { console.log(this.voiceBtnTimer);
                    elm.fadeIn(400);
                });
            }.bind(this);
            this.voiceBtnTimer = setInterval(this.blink, 800);
        }.bind(this);
    }
}

VoiceRecording.prototype = Object.create(HTMLElement.prototype);

