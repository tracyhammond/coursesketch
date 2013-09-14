var upcomming_assignments =
	[
	 	[
	 	 	['Truss Assignment1','Truss_Link1'],
	 	 	'In this assignment we will draw a truss trussty truss.' +
	 	 	' This truss trussty truss will allow us to save sience.',
	 	 	'test/truss_thumb.png',
	 	 	['Phyics','PhysicsLink'],
	 	 	new Date('Thu Aug 29 2013 11:24:27 GMT-0500 (CDT)')
	 	],
	 	[
	 	 	['Chemistry Assignment1','Chemistry_Link1'],
	 	 	'n this assignment we will do a chemistry diagram.',
	 	 	'test/chemistry_thumb.png',
	 	 	['Chemistry','ChemistryLink'],
	 	 	new Date('Sun Sep 1 2013 11:24:27 GMT-0500 (CDT)'),
 	 	],
	 	[
	 	 	['Truss Assignment2','Truss_Link2'],
	 	 	'In this second assignment we will make something cool. YAYYYYYY',
	 	 	'test/truss_thumb.png',
	 	 	['Phyics','PhysicsLink'],
	 	 	new Date('Thu Aug 29 2013 11:24:27 GMT-0500 (CDT)')
 	 	],
	];

var user_classes =
	[
	 	[['Physics','PhysicsLink'],'Description for me class blah blah blabity blah',
	 	 	'test/truss_thumb.png'],
	 	[['Chemistry','ChemistryLink'],'Description for the chemistry class!',
	 	 	'test/chemistry_thumb.png'],
	 	[['Class 3','Class3Link'],'Description for the 3rd class!', 'test/no_image.png'],
	];
	
var questions =
	[
	 	[['TrussProblem1','TrussProblem1Link'],'Question Text for problem one is blahbity blah blah',
	 	 	'test/truss_thumb.png']
	];

var classAssignments = new listMap();	
var assignmentQuestions = new listMap();	
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
	classAssignments.addObject('Physics',upcomming_assignments[0]);
	classAssignments.addObject('Physics',upcomming_assignments[2]);
	classAssignments.addObject('Chemistry',upcomming_assignments[1]);
	assignmentQuestions.addObject('Truss Assignment2',questions[0]);
}


if(typeof(Storage)!=="undefined")
  {
  }
else
  {
  // Sorry! No web storage support..
  }