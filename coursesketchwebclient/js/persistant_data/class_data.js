var upcomming_assignments =
	[
	 	[
	 	 	'assignment',
	 	 	['Truss Assignment1','Truss_Link1','Assignment_Truss_1_ID'],
	 	 	'In this assignment we will draw a truss trussty truss.' +
	 	 	' This truss trussty truss will allow us to save sience.',
	 	 	'test/truss_thumb.png',
	 	 	['Phyics','PhysicsLink','Class_Physics_ID'],
	 	 	new Date('Thu Aug 29 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'completed'
	 	],
		[
		 	'assignment',
		 	['Truss Assignment2','Truss_Link2', 'Assignment_Truss_2_ID'],
		 	'In this second assignment we will make something cool. YAYYYYYY',
		 	'test/truss_thumb.png',
		 	['Phyics','PhysicsLink','Class_Physics_ID'],
		 	new Date('Thu Aug 29 2013 11:24:27 GMT-0500 (CDT)'),
		 	'notStarted'
		],
	 	[
	 	 	'assignment',
	 	 	['Chemistry Assignment1','Chemistry_Link1', 'Assignment_Chemistry_1_ID'],
	 	 	'n this assignment we will do a chemistry diagram.',
	 	 	'test/chemistry_thumb.png',
	 	 	['Chemistry','ChemistryLink','Class_Chemistry_ID'],
	 	 	new Date('Sun Sep 1 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'started'
 	 	],
	 	[
	 	 	'assignment',
	 	 	['Chemistry Assignment2','Chemistry_Link2', 'Assignment_Chemistry_2_ID'],
	 	 	'n this assignment we will do a chemistry diagram.',
	 	 	'test/chemistry_thumb.png',
	 	 	['Chemistry','ChemistryLink','Class_Chemistry_ID'],
	 	 	new Date('Sun Sep 1 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'studentStarted'
	 	],
	 	[
	 	 	'assignment',
	 	 	['Chemistry Assignment3','Chemistry_Link3', 'Assignment_Chemistry_3_ID'],
	 	 	'n this assignment we will do a chemistry diagram.',
	 	 	'test/chemistry_thumb.png',
	 	 	['Chemistry','ChemistryLink','Class_Chemistry_ID'],
	 	 	new Date('Sun Sep 1 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'visible'
	 	],
	 	[
	 	 	'assignment',
	 	 	['Class Assignment1','Class_Link3', 'Assignment_Class_1_ID'],
	 	 	'n this assignment we will do a class item.',
	 	 	'test/class_thumb.png',
	 	 	['Class 3','Class3Link','Class_Class_3_ID'],
	 	 	new Date('Sun Sep 1 2013 11:24:27 GMT-0500 (CDT)'),
	 	 	'notVisible'
 	 	],
	];

var user_classes =
	[
	 	[
	 	 	'class',
		 	['Physics','PhysicsLink','Class_Physics_ID'],
		 	'Description for me class blah blah blabity blah',
	 	 	'test/truss_thumb.png'
	 	],
	 	[
	 	 	'class',
	 	 	['Chemistry','ChemistryLink','Class_Chemistry_ID'],
	 	 	'Description for the chemistry class!',
	 	 	'test/chemistry_thumb.png'
	 	],
	 	[
	 	 	'class',
	 	 	['Class 3','Class3Link','Class_Class_3_ID'],
	 	 	'Description for the 3rd class!',
	 	 	'test/no_image.png'
	 	 ],
	];
	
var questions =
	[
	 	[
	 	 	'problem',
	 	 	['TrussProblem1','TrussProblem1Link','Problem_Truss_1_ID'],
	 	 	'Question Text for problem one is blahbity blah blah',
	 	 	'test/truss_thumb.png'
	 	 ]
	];

var classAssignments = new listMap();	
var assignmentProblems = new listMap();	
initialize();
/**
 * This map will hold a map of classes to a list of assignments.
 */

function listMap() {
	this.map = {};
	this.addObject = function(key,object) {
		if(this.map[key]){
			this.map[key].push(object);
		} else {
			var array = [];
			array.push(object);
			this.map[key] = array;
		}
	}

	this.getList = function(key) {
		return this.map[key];
	}

}

// TODO change the keys to actually be unique
function initialize() {
	classAssignments.addObject('Class_Physics_ID',upcomming_assignments[0]);
	classAssignments.addObject('Class_Physics_ID',upcomming_assignments[1]);
	classAssignments.addObject('Class_Chemistry_ID',upcomming_assignments[2]);
	classAssignments.addObject('Class_Chemistry_ID',upcomming_assignments[3]);
	classAssignments.addObject('Class_Chemistry_ID',upcomming_assignments[4]);
	classAssignments.addObject('Class_Class_3_ID',upcomming_assignments[5]);
	assignmentProblems.addObject('Assignment_Truss_2_ID',questions[0]);
}


if(typeof(Storage)!=="undefined")
  {
  }
else
  {
  // Sorry! No web storage support..
  }