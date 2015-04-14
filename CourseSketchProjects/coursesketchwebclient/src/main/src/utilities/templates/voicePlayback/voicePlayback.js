function VoicePlayback() {
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        var vid = this.shadowRoot.querySelector('#myaudio');
        vid.src = '/src/utilities/templates/voicePlayback/test.mp3';
        vid.ontimeupdate = function() { 
            myFunction()
        }

        vid.onplay = function() {
                                    playMe();
                                    playVoice();
        }

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
            this.stopRecording();
            //localScope.audio.play();
        }

        function pauseVoice() {
            localScope.audio.pause();
        }

        this.startRecording = function() {
            this.recorder.record();
            console.log('Recording...');
        }

        this.stopRecording = function() {
            this.recorder.stop();
            console.log('Stopped recording.');

            this.saveFile();
        }

        this.startUserMedia = function(stream) {
            this.recorder = new Recorder(stream);
            console.log('Recorder initialized.');

            this.startRecording();
        }

        this.saveFile = function() {
            this.recorder.exportMP3(function(blob, mp3name) {
                vid.src = webkitURL.createObjectURL(blob);
            });
        }
        init = function() {
            try {
                window.AudioContext = window.AudioContext || window.webkitAudioContext;
                navigator.getUserMedia = (navigator.getUserMedia ||
                                          navigator.webkitGetUserMedia ||
                                          navigator.mozgetUserMedia ||
                                          navigator.msGetUserMedia);
                window.URL = window.URL || window.webkitURL;

                console.log('Audio context set up');
                console.log('navigator.getUserMedia ' + (navigator.getUserMedia ? 'available.' : 'not available'));
            } catch (e) {
                alert('No web audio support in this browser');
            }

            navigator.getUserMedia({ audio: true }, function(stream) {
                localScope.recorder = new Recorder(stream);
                console.log('Recorder initialized.');
            }, function(e) {
                console.log('No live audio input: ' + e);
            });
        }

    };

}

VoicePlayback.prototype = Object.create(HTMLElement.prototype);

