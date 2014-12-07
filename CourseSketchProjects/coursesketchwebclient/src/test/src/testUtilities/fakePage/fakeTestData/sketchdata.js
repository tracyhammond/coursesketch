(function() {

	CourseSketch.fakeSketches = [];
	var currentTime = CourseSketch.getCurrentTime();


	function  toRadians(num){
		return num*Math.PI/180;
	}



	var sketch1 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
	var sketch2 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
	var sketch3 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
	var sketch4 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();
	var sketch5 = CourseSketch.PROTOBUF_UTIL.SrlUpdateList();

	var update1 = CourseSketch.PROTOBUF_UTIL.SrlUpdate(); 
	var update2 = CourseSketch.PROTOBUF_UTIL.SrlUpdate(); 
	var update3 = CourseSketch.PROTOBUF_UTIL.SrlUpdate(); 
	var update4 = CourseSketch.PROTOBUF_UTIL.SrlUpdate(); 
	var update5 = CourseSketch.PROTOBUF_UTIL.SrlUpdate(); 

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

	var stroke1 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();
	var stroke2 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();
	var stroke3 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();
	var stroke4 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();
	var stroke5 = CourseSketch.PROTOBUF_UTIL.ProtoSrlStroke();

	stroke1.points = new Array();
	stroke2.points = new Array();
	stroke3.points = new Array();
	stroke4.points = new Array();
	stroke5.points = new Array();

	for(var i = 0; i <=360; i+=5){
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point"+i;
		point.time = currentTime.add(i*10);
		point.x = (Math.cos(toRadians(i))*50)+200;
		point.y = (Math.sin(toRadians(i))*50)+200;
		stroke1.points.push(point)
	}

	for(var i = 0; i <=360; i+=5){
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point"+i;
		point.time = currentTime.add(i*10);
		point.x = (Math.cos(toRadians(i))*50)+250;
		point.y = (Math.sin(toRadians(i))*50)+250;
		stroke2.points.push(point)
	}

	for(var i = 0; i <300; i++){
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point" + i;
		point.time = currentTime.add(i*10);
		point.x = i*2;
		point.y = i*4;
		stroke3.points.push(point);
	}

	for(var i = 0; i <400; i++){
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point" + i;
		point.time = currentTime.add(i*10);
		point.x = i;
		point.y = i*5;
		stroke4.points.push(point);
	}

	for(var i = 0; i <400; i++){
		var point = CourseSketch.PROTOBUF_UTIL.ProtoSrlPoint();
		point.id = "point" + i;
		point.time = currentTime.add(i*10);
		point.x = i;
		point.y = 300;
		stroke5.points.push(point);
	}

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


	update1.updateId = "update1-of-sketch1";
	update1.commands = new Array();
	update1.commands.push(command1);

	update2.updateId = "update2-of-sketch2";
	update2.commands = new Array();
	update2.commands.push(command2);

	update3.updateId = "update3-of-sketch3";
	update3.commands = new Array();
	update3.commands.push(command3);

	update4.updateId = "update4-of-sketch4";
	update4.commands = new Array();
	update4.commands.push(command4);

	update5.updateId = "update5-of-sketch5";
	update5.commands = new Array();
	update5.commands.push(command5);

	
	sketch1.list = new Array();
	sketch1.list.push(update1);

	sketch2.list = new Array();
	sketch2.list.push(update2);

	sketch3.list = new Array();
	sketch3.list.push(update3);

	sketch4.list = new Array();
	sketch4.list.push(update4);

	sketch5.list = new Array();
	sketch5.list.push(update5);


	CourseSketch.fakeSketches.push(sketch1);
	CourseSketch.fakeSketches.push(sketch2);
	CourseSketch.fakeSketches.push(sketch3);
	CourseSketch.fakeSketches.push(sketch4);
	CourseSketch.fakeSketches.push(sketch5);
}
)();