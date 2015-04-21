function VoicePlayback() {
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
            this.recorder = new Recorder(stream);
            console.log('Recorder initialized.');
        }, function(e) {
            console.log('No live audio input: ' + e);
        });
    }

    // Start recording voice
    this.startRecording = function() {
        this.recorder.record();
        console.log('Recording...');
    }

    // Stop recording voice
    this.stopRecording = function() {
        this.recorder.stop();
        console.log('Stopped recording.');

        this.saveFile();
    }

    // Initialize recorder stream, call to start voice recording
    this.startUserMedia = function(stream) {
        this.recorder = new Recorder(stream);
        console.log('Recorder initialized.');

        this.startRecording();
    }

    // Save the file to the database
    // NOTE: CURRENTLY SETS LOCALLY
    this.saveFile = function() {
        this.recorder.exportMP3(function(blob, mp3name) {
            vid.src = webkitURL.createObjectURL(blob);
        });
    }

    // Blink the red record button
    this.blink = function(elm) {
        this.voiceBtnTimer = setInterval(function() {
            elm.fadeOut(400, function() {
                elm.fadeIn(400);
            });
        }, 800);
        elm.val('REC');
    }

    // Playback the drawn sketch
    this.playMe = function(isPaused) {
        if (!isPaused){
            var surface = this.shadowRoot.querySelector('sketch-surface');
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

    // Pause the sketch 
    this.pauseMe = function(playBack) {
        pauseIndex = playBack.pauseNext();
        isPaused = true;
    }

    // TESTING FUNCTION
    this.myFunction = function(vid) {
        // Display the current position of the video in a p element with id="demo"
        this.shadowRoot.querySelector('#demo').innerHTML = vid.currentTime;
    }

    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        this.initRecorder();
        var vid = this.shadowRoot.querySelector('#myaudio');
        vid.src = '/src/utilities/templates/voicePlayback/test.mp3';
        var playBack;
        var isPaused = false;
        var pauseIndex= 0 ;
        vid.ontimeupdate = function() {
            localScope.myFunction(vid)
        }
        vid.onplay = function() {
            localScope.playMe(isPaused);
            //localScope.stopRecording();
        }
        vid.onpause = function() {
            localScope.pauseMe(playBack);
        }

        setTimeout(function() {
            var surface = this.shadowRoot.querySelector('sketch-surface');
            var graphics = surface.graphics;
            var updateManager = surface.getUpdateManager();

            this.shadowRoot.querySelector('#recordBtn').onclick = function() {
                if (localScope.isRecording === true) {
                    localScope.stopRecording();
                    clearInterval(localScope.voiceBtnTimer);
                    localScope.isRecording = false;
                    $(this.shadowRoot.querySelector('#recordBtn')).val(null);
                } else {
                    localScope.blink($(this.shadowRoot.querySelector('#recordBtn')));
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

