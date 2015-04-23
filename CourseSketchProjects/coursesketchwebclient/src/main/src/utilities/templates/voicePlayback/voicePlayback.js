function VoicePlayback() {
    var localScope = undefined;

    // Initialize microphone on client
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
            localScope.recorder = new Recorder(stream);
            console.log('Recorder initialized.');
        }, function(e) {
            console.log('No live audio input: ' + e);
        });
    }

    // Start recording voice
    this.startRecording = function() {
        localScope.recorder.record();
        console.log('Recording...');
    }

    // Stop recording voice
    this.stopRecording = function() {
        localScope.recorder.stop();
        console.log('Stopped recording.');

        localScope.saveFile();
    }

    // Save the file to the database
    // NOTE: CURRENTLY SETS LOCALLY
    this.saveFile = function() {
        localScope.recorder.exportMP3(function(blob, mp3name) {
            localScope.vid.src = URL.createObjectURL(blob);
            localScope.vid.type = "audio/mp3";
        });
    }

    // Blink the red record button
    this.blink = function(elm) {
        localScope.voiceBtnTimer = setInterval(function() {
            elm.fadeOut(400, function() {
                elm.fadeIn(400);
            });
        }, 800);
        elm.val('REC');
    }

    // Playback the drawn sketch
    this.playMe = function() {
        localScope.surface.resizeSurface();
        if (!localScope.isPaused){
            var updateList = localScope.surface.getUpdateList();
            var copyList = [];
            for (var i = 0; i < updateList.length; i++) {
                copyList.push(updateList[i]);
            }
            localScope.updateManager = localScope.surface.getUpdateManager();
            localScope.updateManager.clearUpdates(false);

            localScope.playBack = new Playback(copyList, localScope.updateManager, localScope.graphics);
            localScope.updateManager.addPlugin(localScope.playBack);
            localScope.playBack.playNext(new Date().getTime());
        } else {
            localScope.playBack.playNext(new Date().getTime());
        }
    }

    // Pause the sketch 
    this.pauseMe = function() {
        localScope.pauseIndex = localScope.playBack.pauseNext();
        localScope.isPaused = true;
    }

    this.initializeElement = function(templateClone) {
        localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        localScope.recorder = undefined;
        localScope.initRecorder();

        localScope.vid            = this.shadowRoot.querySelector('#myaudio');
        localScope.vid.src        = '/src/utilities/templates/voicePlayback/test.mp3';
        localScope.playBack       = undefined;
        localScope.isPaused       = false;
        localScope.pauseIndex     = 0;
        localScope.startTime      = 0;

        localScope.vid.onplay           = function() {
            localScope.playMe();
        }
        localScope.vid.onpause          = function() {
            localScope.pauseMe();
        }

        setTimeout(function() {
            localScope.surface        = localScope.shadowRoot.querySelector('sketch-surface');
            localScope.graphics       = localScope.surface.graphics;
            localScope.updateManager  = localScope.surface.getUpdateManager();

            this.shadowRoot.querySelector('#recordBtn').onclick = function() {
                if (localScope.isRecording === true) {
                    localScope.stopRecording();
                    clearInterval(localScope.voiceBtnTimer);
                    localScope.isRecording = false;
                    $(localScope.shadowRoot.querySelector('#recordBtn')).val(null);
                } else {
                    localScope.blink($(localScope.shadowRoot.querySelector('#recordBtn')));
                    localScope.startRecording();
                    localScope.isRecording = true;
                }
            }.bind(this);

            // document.ready(function() {
            //     localScope.shadowRoot.querySelector('#myaudio') {
            //         alwaysShowControls: true,
            //         features: ['playpause','volume','progress'],
            //         audioVolume: 'horizontal',
            //         audioWidth: 364,
            //         audioHeight: 70
            //     };
            // });
        }, 2000);
    };

}

VoicePlayback.prototype = Object.create(HTMLElement.prototype);

