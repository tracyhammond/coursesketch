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
        vid.ontimeupdate = function() {
            myFunction()
        }
        vid.onplay = function() {
            playMe();
            //localScope.stopRecording();
        }
        vid.onpause = function() {
            pauseMe();
        }
        var surface = document.body.querySelector('sketch-surface');
        var graphics = surface.graphics;
        var updateManager = surface.getUpdateManager();

        this.shadowRoot.querySelector('#recordBtn').onclick = function() {
            if (this.isRecording === true) {
                this.stopRecording();
                clearInterval(this.voiceBtnTimer);
                this.isRecording = false;
                $(this.shadowRoot.querySelector('#recordBtn')).val(null);
            } else {
                this.blink($(this.shadowRoot.querySelector('#recordBtn')));
                this.startRecording();
                this.isRecording = true;
            }
        }.bind(this);

        this.blink = function(elm) {
            this.voiceBtnTimer = setInterval(function() {
                elm.fadeOut(400, function() {
                    elm.fadeIn(400);
                });
            }, 800);
            elm.val('REC');
        }.bind(this);

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

        this.initRecorder = function() {
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
                this.recorder = new Recorder(stream);
                console.log('Recorder initialized.');
            }, function(e) {
                console.log('No live audio input: ' + e);
            });
        }
        this.initRecorder();
    };

}

VoicePlayback.prototype = Object.create(HTMLElement.prototype);

