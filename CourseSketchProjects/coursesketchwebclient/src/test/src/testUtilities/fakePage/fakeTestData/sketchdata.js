(function() {

	CourseSketch.fakeSketches = [];
	var currentTime = CourseSketch.getCurrentTime();

	/*
	 * a simple function to convert a degree into radians
	 */

	function  toRadians(num){
		return num*Math.PI/180;
	}

	/*
	 * createing the sketches to make possible submissions or other test data
	 */

	var sketch1 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
	var sketch2 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
	var sketch3 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
	var sketch4 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
	var sketch5 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();

	/*
	 * making updates to be used in a sketches update list
	 */

	var update1 = CourseSketch.PROTOBUF_UTIL.SrlUpdate();
	var update2 = CourseSketch.PROTOBUF_UTIL.SrlUpdate();
	var update3 = CourseSketch.PROTOBUF_UTIL.SrlUpdate();
	var update4 = CourseSketch.PROTOBUF_UTIL.SrlUpdate();
	var update5 = CourseSketch.PROTOBUF_UTIL.SrlUpdate();

	var update6 = CourseSketch.PROTOBUF_UTIL.SrlUpdate();

	/*
	 * commands for adding strokes to updates, false is because the strokes are created in code
	 * not from a user.
	 */

	var command1 = CourseSketch.PROTOBUF_UTIL.createBaseCommand(
		CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, false);
	var command2 = CourseSketch.PROTOBUF_UTIL.createBaseCommand(
		CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, false);
	var command3 = CourseSketch.PROTOBUF_UTIL.createBaseCommand(
		CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, false);
	var command4 = CourseSketch.PROTOBUF_UTIL.createBaseCommand(
		CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, false);
	var command5 = CourseSketch.PROTOBUF_UTIL.createBaseCommand(
		CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE, false);

	/*
	 * creating strokes to be used for the command ADD_STROKE
	 */

	var stroke1 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();
	var stroke2 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();
	var stroke3 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();
	var stroke4 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();
	var stroke5 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();

	/*
	 * initaliszing the arrays of each stroke to hold their points
	 */

	stroke1.points = new Array();
	stroke2.points = new Array();
	stroke3.points = new Array();
	stroke4.points = new Array();
	stroke5.points = new Array();

	/*
	 * creating some shapes or lines and their points are placed into each strokes array of points
	 */

	//a circle
	for(var i = 0; i <= 360; i += 5) {
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point" + i;
		point.time = currentTime.add(i * 10);
		point.x = (Math.cos(toRadians(i)) * 50) + 100;
		point.y = (Math.sin(toRadians(i)) * 50) + 100;
		stroke1.points.push(point)
	}
	// a slightly larger circle
	for(var i = 0; i <= 360; i += 5){
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point" + i;
		point.time = currentTime.add(i * 10);
		point.x = (Math.cos(toRadians(i)) * 50) + 250;
		point.y = (Math.sin(toRadians(i)) * 50) + 250;
		stroke2.points.push(point)
	}
	// a line
	for(var i = 0; i < 300; i++){
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point" + i;
		point.time = currentTime.add(i * 10);
		point.x = i * 2;
		point.y = i * 4;
		stroke3.points.push(point);
	}
	// a steeper line
	for(var i = 0; i < 400; i++){
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point" + i;
		point.time = currentTime.add(i * 10);
		point.x = i;
		point.y = i * 5;
		stroke4.points.push(point);
	}
	// a flat line
	for(var i = 0; i < 400; i++){
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point" + i;
		point.time = currentTime.add(i * 10);
		point.x = i;
		point.y = 300;
		stroke5.points.push(point);
	}

	/*
	 * giving each stroke an ID and a time stamp
	 */

	stroke1.id = "stroke1-of-sketch1";
	stroke1.time = currentTime.add(3600);

	stroke2.id = "stroke2-of-sketch2"
	stroke2.time = currentTime.add(3600);

	stroke3.id = "stroke3-of-sketch3"
	stroke3.time = currentTime.add(3000);

	stroke4.id = "stroke4-of-sketch4"
	stroke4.time = currentTime.add(4000);

	stroke5.id = "stroke5-of-sketch5"
	stroke5.time = currentTime.add(4000);

	command1.setCommandData(stroke1.toArrayBuffer());
	command2.setCommandData(stroke2.toArrayBuffer());
	command3.setCommandData(stroke3.toArrayBuffer());
	command4.setCommandData(stroke4.toArrayBuffer());
	command5.setCommandData(stroke5.toArrayBuffer());

	/*
	 * initalizing the updates array of commands and pushing a command to the array
	 * also giving each update an ID
	 */

	update1.updateId = "update1-of-sketch1";
	update1.commands = new Array();
	update1.commands.push(command1);

	update2.updateId = "update1-of-sketch2";
	update2.commands = new Array();
	update2.commands.push(CourseSketch.PROTOBUF_UTIL.createNewSketch("sketch2"));
	update2.commands.push(command2);

	update3.updateId = "update1-of-sketch3";
	update3.commands = new Array();
	//update3.commands.push(CourseSketch.PROTOBUF_UTIL.createNewSketch("sketch3"));
	update3.commands.push(command3);

	update4.updateId = "update1-of-sketch4";
	update4.commands = new Array();
	//update4.commands.push(CourseSketch.PROTOBUF_UTIL.createNewSketch("sketch4"));
	update4.commands.push(command4);

	update5.updateId = "update1-of-sketch5";
	update5.commands = new Array();
	//update5.commands.push(CourseSketch.PROTOBUF_UTIL.createNewSketch("sketch5"));
	update5.commands.push(command5);

	update6.updateId = "undo-update";
	update6.commands = new Array();
	var undoMarkerObject = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.UNDO, false);
	var undoUpdate = CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands([ undoMarkerObject ]);
	update6.commands.push(undoMarkerObject);

	/*
	 * initalizing the array of updates for the sketchs list of updates
	 */

	sketch1.setList([CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands(
			[ CourseSketch.PROTOBUF_UTIL.createNewSketch("sketch1") ])
		]);
	sketch1.list.push(update1);
	sketch1.list.push(update2);
	for (var i = 0; i < 10; i++) {
		sketch1.list.push(update3);
		sketch1.list.push(update4);
		sketch1.list.push(update5);
		sketch1.list.push(update6);
	}
	sketch1.setList(sketch1.list);

	sketch2.setList([CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands(
			[ CourseSketch.PROTOBUF_UTIL.createNewSketch("sketch2") ])
		]);
	sketch2.list.push(update2);
	sketch2.setList(sketch2.list);

	sketch3.setList([CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands(
			[ CourseSketch.PROTOBUF_UTIL.createNewSketch("sketch3") ])
		]);
	sketch3.list.push(update3);
	sketch3.setList(sketch3.list);

	sketch4.setList([CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands(
			[ CourseSketch.PROTOBUF_UTIL.createNewSketch("sketch4") ])
		]);
	sketch4.list.push(update4);
	sketch4.setList(sketch4.list);

	sketch5.setList([CourseSketch.PROTOBUF_UTIL.createUpdateFromCommands(
			[ CourseSketch.PROTOBUF_UTIL.createNewSketch("sketch5") ])
		]);
	sketch5.list.push(update5);
	sketch5.setList(sketch5.list);

	/*
	 * pushing the sketches
	 */
	CourseSketch.fakeSketches.push(sketch1);
	CourseSketch.fakeSketches.push(sketch2);
	CourseSketch.fakeSketches.push(sketch3);
	CourseSketch.fakeSketches.push(sketch4);
	CourseSketch.fakeSketches.push(sketch5);

}
)();

/**
 * cleans the update to make sure it is the same as all new versions
 */
function cleanUpdate(update) {
	return CourseSketch.PROTOBUF_UTIL.decodeProtobuf(update.toArrayBuffer(), CourseSketch.PROTOBUF_UTIL.getSrlUpdateClass());
}

/**
 * cleans the command to make sure it is the same as all new versions
 */
function cleanCommand(command) {
	return CourseSketch.PROTOBUF_UTIL.decodeProtobuf(command.toArrayBuffer(), CourseSketch.PROTOBUF_UTIL.getSrlCommandClass());
}
