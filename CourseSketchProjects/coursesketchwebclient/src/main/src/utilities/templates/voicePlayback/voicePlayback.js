/**
 * Sets up and saves recordings. 
 * Calls playback.js and starts music when play button is pushed.
 */
function VoicePlayback() {
    var localScope = undefined;

	/**
     * Initialize microphone on client
     */
    this.initRecorder = function() {
        try {
            window.AudioContext = window.AudioContext || window.webkitAudioContext;
            navigator.getUserMedia = (navigator.getUserMedia ||
                    navigator.webkitGetUserMedia ||
                    navigator.mozgetUserMedia ||
                    navigator.msGetUserMedia);
            window.URL = window.URL || window.webkitURL;
        } catch (e) {
            alert('Web audio is not supported in this browser.');
        }

		/**
		 * Create the recorder and check to see if failed or not
		 */
        navigator.getUserMedia({ audio: true }, function(stream) {
            localScope.recorder = new Recorder(stream);
        }, function(e) {
        });
    };

	/**
     * Start recording voice
     */
    this.startRecording = function() {
        localScope.recorder.record();
    };

	/**
     * Stop recording voice
     */
    this.stopRecording = function() {
        localScope.recorder.stop();
        localScope.saveFile();
    };

	/**
     * Save the file to the database
     * NOTE: CURRENTLY SAVES LOCALLY
     */
    this.saveFile = function() {
        localScope.recorder.exportMP3(function(blob, mp3name) {
            localScope.vid.src = URL.createObjectURL(blob);
            localScope.vid.type = 'audio/mp3';
        });
    };

	/**
     * Blink the elem passed in
	 * @param {Element} The element that needs to blink
     */
    this.blink = function(elm) {
        localScope.voiceBtnTimer = setInterval(function() {
            elm.fadeOut(400, function() {
                elm.fadeIn(400);
            });
        }, 800);
        elm.val('REC');
    };

    /**
     * Playback the drawn sketch
     */
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
    };

    /**
     * Pause the drawn sketch
     */
    this.pauseMe = function() {
        localScope.pauseIndex = localScope.playBack.pauseNext();
        localScope.isPaused = true;
    };

	/**
     * Initialize the passed in element.
	 * Used for initializing the video
	 * @param {Node} templateClone is a clone of the custom HTML Element for the voicePlayback
     */
    this.initializeElement = function(templateClone) {
        localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        localScope.recorder = undefined;
        localScope.initRecorder();

        localScope.vid = this.shadowRoot.querySelector('#myaudio');
        localScope.vid.src = '/src/utilities/templates/voicePlayback/test.mp3';
        localScope.playBack = undefined;
        localScope.isPaused = false;
        localScope.pauseIndex = 0;
        localScope.startTime = 0;

		/**
		 * Calls playMe when the play button is bushed to start playback
		 */
        localScope.vid.onplay = function() {
            localScope.playMe();
        };
		/**
		 * Calls pauseMe when the play button is bushed to pause playback
		 */
        localScope.vid.onpause = function() {
            localScope.pauseMe();
        };

        setTimeout(function() {
            localScope.surface = localScope.shadowRoot.querySelector('sketch-surface');
            localScope.graphics = localScope.surface.graphics;
            localScope.updateManager = localScope.surface.getUpdateManager();
			
			/**
			 * Calls blink if the button is blinking or starts blink if it is not blinking
			 */
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
        }, 2000);
    };

}

VoicePlayback.prototype = Object.create(HTMLElement.prototype);

