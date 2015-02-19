function VoicePlayback() {
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
    }
}

VoicePlayback.prototype = Object.create(HTMLElement.prototype);

