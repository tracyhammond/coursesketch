function protobufUtils(postLoadedFunction) {
	function loadFiles() {
		loader.loadFile("js/connection/libraries/Long.min.js",'js',loadBarrier.bind(this));
		loader.loadFile("js/connection/libraries/ByteBuffer.min.js",'js',load2.bind(this));
	}
	function load2() {
		barrierCount++;
		loader.loadFile("js/connection/libraries/ProtoBuf.min.js",'js',loadBarrier.bind(this));
	}
	
	var barrierCount = 0;
	function loadBarrier() {
		barrierCount++;
		if(barrierCount == 3) {
			//Next Function.
			initializeBuf();
		}
	}
	var builder;
	var testBuilded;
	var Message;
	var postFunction = postLoadedFunction;
	function initializeBuf() {
		ProtoBuf = dcodeIO.ProtoBuf;
		builder = ProtoBuf.protoFromFile("other/test.proto");
		Message = builder.build("Message");
		postFunction();
	}

	
	this.makeMessage = function(text) {
		var message = new Message(text);
		alert(' result ' + message);
		alert(' resultingText ' + message.text);
		return message;
	}

	this.createBuffer = function(message) {
		var buffer = message.toArrayBuffer();
		return buffer;
	}

	loadFiles();
}

var ProtoBuf;