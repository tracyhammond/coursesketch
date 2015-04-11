(function(window){

  var WORKER_PATH = '/src/utilities/templates/voiceRecording/cdn/recorderWorker.js';

  var Recorder = function(stream){
    var context, audioInput, processor, gain, gainFunction, processorFunction, blob, mp3Name;
    var recording = false, currCallback;

    // http://typedarray.org/from-microphone-to-wav-with-getusermedia-and-web-audio/
    context = new AudioContext;
    gainFunction = context.createGain || context.createGainNode;
    gain = gainFunction.call(context);
    audioInput = context.createMediaStreamSource(stream);
    console.log('Media stream created.' );
    console.log("input sample rate " +context.sampleRate);

    audioInput.connect(gain);
    console.log('Input connected to audio context destination.');

    processorFunction = context.createScriptProcessor || context.createJavaScriptNode;
    processor = processorFunction.call(context, 4096, 2, 2);

    var worker = new Worker(WORKER_PATH);
    worker.postMessage({
      command: 'init',
      sampleRate: context.sampleRate
    });

    processor.onaudioprocess = function(e){
      if (!recording) return;
      worker.postMessage({
        command: 'record',
        buffer: e.inputBuffer.getChannelData(0)
      });
    }

    this.record = function(){
      recording = true;
    }

    this.stop = function(){
      recording = false;
    }

    this.exportMP3 = function(cb){
      currCallback = cb;
      if (!currCallback) throw new Error('Callback not set');
      worker.postMessage({ command: 'exportMP3' });
    }

    worker.onmessage = function(e){
      blob = e.data;
      mp3Name = encodeURIComponent('audio_recording_' + new Date().getTime() + '.mp3');
      uploadAudio(blob, mp3Name);
      currCallback(blob, mp3Name);
    }

    function uploadAudio(mp3Data, mp3Name){
      var reader = new FileReader();
      reader.onload = function(event){
        var fd = new FormData();
        fd.append('fname', mp3Name);
        fd.append('data', event.target.result);
        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/src/utilities/templates/voiceRecording/cdn/upload.php', true);
        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4) {
                console.log("MP3 Uploaded.");
            }
        };
        xhr.send(fd);
      };
      reader.readAsDataURL(mp3Data);
    }

    gain.connect(processor);
    processor.connect(context.destination);
  };

  window.Recorder = Recorder;

})(window);
