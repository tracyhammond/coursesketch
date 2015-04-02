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

        this.startRecording = function() {
            this.recorder.record();
            console.log('Recording...');
        }.bind(this);

        this.stopRecording = function() {
            this.recorder.stop();
            console.log('Stopped recording.');

            this.saveFile();
        }.bind(this);

        this.startUserMedia = function(stream) {
            this.recorder = new Recorder(stream);
            console.log('Recorder initialized.');
        }.bind(this);

        this.saveFile = function() {
            this.recorder.exportMP3(function(blob, mp3name) {

            });
        }.bind(this);

        init = function() {
            try {
                window.AudioContext = window.AudioContext || window.webkitAudioContext;
                navigator.getUserMedia = ( navigator.getUserMedia ||
                                           navigator.webkitGetUserMedia ||
                                           navigator.mozgetUserMedia ||
                                           navigator.msGetUserMedia);
                window.URL = window.URL || window.webkitURL;

                console.log('Audio context set up');
                console.log('navigator.getUserMedia ' + (navigator.getUserMedia ? 'available.' : 'not available'));
            } catch(e) {
                alert('No web audio support in this browser');
            }

            navigator.getUserMedia({audio: true}, function(stream) {
                localScope.recorder = new Recorder(stream);
                console.log('Recorder initialized.');
            }, function(e) {
                console.log('No live audio input: ' + e);
            });
        }.bind(localScope);
    }
}

VoiceRecording.prototype = Object.create(HTMLElement.prototype);

