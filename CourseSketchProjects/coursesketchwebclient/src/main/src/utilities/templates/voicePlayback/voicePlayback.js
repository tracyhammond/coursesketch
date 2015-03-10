function VoicePlayback() {
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        var surface = document.body.querySelector("sketch-surface");
        var graphics = surface.graphics;
        var updateManager = surface.getUpdateManager();

        this.shadowRoot.querySelector("#play-btn").onclick = function(){
            localScope.shadowRoot.querySelector("#pause-btn").style.display = "block";
            localScope.shadowRoot.querySelector("#play-btn").style.display = "none";
            playMe();
        }
        this.shadowRoot.querySelector("#pause-btn").onclick = function(){
            localScope.shadowRoot.querySelector("#play-btn").style.display = "block";
            localScope.shadowRoot.querySelector("#pause-btn").style.display = "none";
        }

        function playMe() {
            var graphics = surface.graphics;
            var updateList = surface.getUpdateList();
            var copyList = [];
            for (var i = 0; i < updateList.length; i++) {
                copyList.push(updateList[i]);
            }
            var updateManager = surface.getUpdateManager();
            updateManager.clearUpdates(false);

            var playBack = new Playback(copyList, updateManager, graphics);
            updateManager.addPlugin(playBack);
            playBack.playNext();
        }
    }

}

VoicePlayback.prototype = Object.create(HTMLElement.prototype);

