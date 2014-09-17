var Preview = {
  delay: 150,        // delay after keystroke before updating

  preview: null,     // filled in by Init below
  buffer: null,      // filled in by Init below

  timeout: null,     // store setTimout id
  mjRunning: false,  // true when MathJax is processing
  oldText: null,     // used to check if an update is needed
  
  previewId: null,
  bufferId: null,
  inputId: null,
  //
  //  Get the preview and buffer DIV's
  //
  Init: function (preview, buffer, input) {
	  this.previewId = preview;
	  this.bufferId = buffer;
	  this.inputId = input;
	  this.preview = document.getElementById(preview);
	  this.buffer = document.getElementById(buffer);
  },

  //
  //  Switch the buffer and preview, and display the right one.
  //  (We use visibility:hidden rather than display:none since
  //  the results of running MathJax are more accurate that way.)
  //
  SwapBuffers: function () {
    var buffer = this.preview, preview = this.buffer;
    this.buffer = buffer; this.preview = preview;
    buffer.style.visibility = "hidden"; buffer.style.position = "absolute";
    preview.style.position = ""; preview.style.visibility = "";
  },

  //
  //  This gets called when a key is pressed in the textarea.
  //  We check if there is already a pending update and clear it if so.
  //  Then set up an update to occur after a small delay (so if more keys
  //    are pressed, the update won't occur until after there has been 
  //    a pause in the typing).
  //  The callback function is set up below, after the Preview object is set up.
  //
  Update: function () {
    if (this.timeout) {clearTimeout(this.timeout)}
    this.timeout = setTimeout(this.callback,this.delay);
  },

  //
  //  Creates the preview and runs MathJax on it.
  //  If MathJax is already trying to render the code, return
  //  If the text hasn't changed, return
  //  Otherwise, indicate that MathJax is running, and start the
  //    typesetting.  After it is done, call PreviewDone.
  //  
  CreatePreview: function () {
    Preview.timeout = null;
    if (this.mjRunning) return;
    var text = document.getElementById(this.inputId).value;
    if (text === this.oldtext) return;
    // should replace \n with <br>
    if (text) {
    	text = text.replace(new RegExp("[\n]", 'g'), "<br>");
    }
    this.buffer.innerHTML = this.oldtext = text;
    this.mjRunning = true;
    MathJax.Hub.Queue(
      ["Typeset",MathJax.Hub,this.buffer],
      ["PreviewDone",this]
    );
  },

  //
  //  Indicate that MathJax is no longer running,
  //  and swap the buffers to show the results.
  //
  PreviewDone: function () {
    this.mjRunning = false;
    this.SwapBuffers();
  }

};

//
//  Cache a callback to the CreatePreview action
//
Preview.callback = MathJax.Callback(["CreatePreview", Preview]);
Preview.callback.autoReset = true;  // make sure it can run more than once


/*
<!-- Sample use!  (copy and paste) -->
<div id="MathJax_Message" style="display: none;"></div>
<textarea id="MathInput" cols="60" rows="10" onkeyup="Preview.Update()" style="margin-top:5px"></textarea>
<br><br>
Preview is shown here:
<div id="MathPreview" style="border:1px solid; padding: 3px; width:50%; margin-top:5px"></div>
<div id="MathBuffer" style="border:1px solid; padding: 3px; width:50%; margin-top:5px; 
visibility:hidden; position:absolute; top:0; left: 0">
	<div>
		<script>
			Preview.Init("MathPreview", "MathBuffer", "MathInput"); // the Id of the three items that are used
		</script>
	</div>
</div>
*/