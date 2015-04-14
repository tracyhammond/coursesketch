function VoicePlayback() {
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        var vid = this.shadowRoot.querySelector('#myaudio');
        vid.ontimeupdate = function() {myFunction()};
        vid.onplay = function() {playMe();}
        this.shadowRoot.querySelector('#play-btn').onclick = function() {
            localScope.shadowRoot.querySelector('#pause-btn').style.display = 'block';
            localScope.shadowRoot.querySelector('#play-btn').style.display = 'none';
            playMe();
            playVoice();
        };
        this.shadowRoot.querySelector('#pause-btn').onclick = function() {
            localScope.shadowRoot.querySelector('#play-btn').style.display = 'block';
            localScope.shadowRoot.querySelector('#pause-btn').style.display = 'none';
            pauseVoice();
        };

        var surface = document.body.querySelector('sketch-surface');
        var graphics = surface.graphics;
        var updateManager = surface.getUpdateManager();

        /*localScope.shadowRoot.querySelector('#slider').slider({
            value: 100,
            min: 0,
            max: 500,
            step: 50,
        });*/

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
            //localScope.shadowRoot.querySelector("#play-btn").style.display = "block";
            //localScope.shadowRoot.querySelector("#pause-btn").style.display = "none";

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

        function playVoice() {
            localScope.audio.play();
        }

        function pauseVoice() {
            localScope.audio.pause();
        }

        init = function() {
            localScope.audio = document.createElement('audio');
            localScope.audio.src = '/src/utilities/templates/voicePlayback/test.mp3';
        }

    };

}

VoicePlayback.prototype = Object.create(HTMLElement.prototype);

