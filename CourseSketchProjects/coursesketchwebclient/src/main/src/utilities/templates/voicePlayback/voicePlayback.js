function VoicePlayback() {
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        this.shadowRoot.querySelector("#play-btn").onclick = function(){
            localScope.shadowRoot.querySelector("#pause-btn").style.display = "block";
            localScope.shadowRoot.querySelector("#play-btn").style.display = "none";
        }
        this.shadowRoot.querySelector("#pause-btn").onclick = function(){
            localScope.shadowRoot.querySelector("#play-btn").style.display = "block";
            localScope.shadowRoot.querySelector("#pause-btn").style.display = "none";
        }
    }

}

VoicePlayback.prototype = Object.create(HTMLElement.prototype);

