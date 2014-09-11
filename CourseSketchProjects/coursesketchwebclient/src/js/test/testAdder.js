

var course_title;
var course_desc;

function addNewCourse() { // Functionality to allow for adding of courses by instructors
	
	var courseEdit1 = document.getElementById('addNewCourse_title');
	courseEdit1.innerHTML = '<textarea rows="1" cols="36">Enter your course&rsquo;s title here! </textarea>';

	var courseEdit2 = document.getElementById('addNewCourse_desc');
	courseEdit2.innerHTML = '<textarea rows="6" cols="36">Enter your course&rsquo;s description here! </textarea>';
	
	var courseEdit3 = document.getElementById('newCourseForm');
	courseEdit3.innerHTML = '<form onsubmit="testCourseAdder()" action=""><input id="newCourseTitle" type="text" name="newCourseTitle" maxlength="100"><br/><input id="newCourseDesc" type="text" name="newCourseDesc" maxlength="1000" style="width: 300px;"><br/><input type="submit" value="Submit"></form>';
	
	/**var courseEdit3 = document.getElementById('newCourseTitle');
	courseEdit3.innerHTML = '<textarea rows="1" cols="36"></textarea>';

	var courseEdit4 = document.getElementById('newCourseDesc');
	courseEdit4.innerHTML = '<textarea rows="6" cols="36"></textarea>';**/

}

function testCourseAdder(){
								
	/**var**/ course_title = document.getElementById('newCourseTitle').value;
	/**var**/ course_desc = document.getElementById('newCourseDesc').value;
	
	alert(course_title + '<br>' + course_desc);
	/**
	return course_title;
	return course_desc;
	**/
													
	}