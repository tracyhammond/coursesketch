// jscs:disable
/**
 * Sets up and saves recordings.
 */
function VoiceRecording() {

    /**
     * Initialize the VoiceRecording
     * @param {Node} templateClone - is a clone of the custom HTML Element for the voice Recording
     */
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        /**
         * Calls blink if the button is blinking or starts blink if it is not blinking
         */
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

        /**
         * Blink the elem passed in
         * @param {Element} element - The element that needs to blink
         */
        this.blink = function(element) {
            this.voiceBtnTimer = setInterval(function() {
                element.fadeOut(400, function() {
                    element.fadeIn(400);
                });
            }, 800);
            element.val('REC');
        }.bind(this);

        /**
         * Start recording voice
         */
        this.startRecording = function() {
            this.recorder.record();
        }.bind(this);

        /**
         * Stop recording voice
         */
        this.stopRecording = function() {
            this.recorder.stop();

            this.saveFile();
        }.bind(this);

        /**
         * Create the recorder
         */
        this.startUserMedia = function(stream) {
            this.recorder = new Recorder(stream);
        }.bind(this);

        /**
         * Save the file to the database
         * NOTE: CURRENTLY SAVES LOCALLY
         */
        this.saveFile = function() {
            this.recorder.exportMP3(function(blob, mp3name) {

            });
        }.bind(this);

        /**
         * Initialize the recorder
         */
        init = function() {
            try {
                window.AudioContext = window.AudioContext || window.webkitAudioContext;
                navigator.getUserMedia = (navigator.getUserMedia ||
<<<<<<< HEAD
                                          navigator.webkitGetUserMedia ||
                                          navigator.mozgetUserMedia ||
                                          navigator.msGetUserMedia);
                window.URL = window.URL || window.webkitURL;

                console.log('Audio context set up');
                console.log('navigator.getUserMedia ' + (navigator.getUserMedia ? 'available.' : 'not available'));
            } catch (e) {
                alert('No web audio support in this browser');
=======
                navigator.webkitGetUserMedia ||
                navigator.mozgetUserMedia ||
                navigator.msGetUserMedia);
                window.URL = window.URL || window.webkitURL;
            } catch (e) {
                alert('Web audio is not supported in this browser.');
>>>>>>> master
            }

            navigator.getUserMedia({ audio: true }, function(stream) {
                localScope.recorder = new Recorder(stream);
            }, function(e) {
            });
        }.bind(localScope);
    };
}

VoiceRecording.prototype = Object.create(HTMLElement.prototype);

