function VoicePlayback() {
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        var vid = this.shadowRoot.querySelector('#myaudio');
        vid.src = '/src/utilities/templates/voicePlayback/test.mp3';
        var playBack;
        var isPaused = false;
        var pauseIndex= 0 ;
        vid.ontimeupdate = function() {myFunction()};
        vid.onplay = function() {
                                    playMe();
                                }
        vid.onpause = function() {
                                    pauseMe();
                                 }

        var surface = document.body.querySelector('sketch-surface');
        var graphics = surface.graphics;
        var updateManager = surface.getUpdateManager();

        function playMe() {
            if (!isPaused){
                var graphics = surface.graphics;
                var updateList = surface.getUpdateList();
                var copyList = [];
                for (var i = 0; i < updateList.length; i++) {
                    copyList.push(updateList[i]);
                }
                var updateManager = surface.getUpdateManager();
                updateManager.clearUpdates(false);

                playBack = new Playback(copyList, updateManager, graphics);
                updateManager.addPlugin(playBack);
                playBack.playNext();
            } else {
                playBack.playNext();
            }
            //localScope.shadowRoot.querySelector("#play-btn").style.display = "block";
            //localScope.shadowRoot.querySelector("#pause-btn").style.display = "none";

        }

        function pauseMe() {
            pauseIndex = playBack.pauseNext();
            isPaused = true;
        }

        function myFunction() {
            // Display the current position of the video in a p element with id="demo"
            this.shadowRoot.querySelector('#demo').innerHTML = vid.currentTime;
        }

        document.ready(function() {
                localScope.shadowRoot.querySelector('#audio-player').mediaelementplayer({
                    alwaysShowControls: true,
                    features: ['playpause','volume','progress'],
                    audioVolume: 'horizontal',
                    audioWidth: 364,
                    audioHeight: 70
                });
            });

    };

}

VoicePlayback.prototype = Object.create(HTMLElement.prototype);

