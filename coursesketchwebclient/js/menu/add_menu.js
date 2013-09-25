function placeMenu() {
	var menuElement = document.getElementById('menuBar');
	if(menuElement) {
		menuElement.innerHTML = getMenu();
		var loader = new DynamicFileLoader();
		loader.loadFile('js/menu/sliding_menu.js', 'js', swipeCheck);
		loader.loadFile('css/menu/menu.css', 'css', false);
	}
}

function swipeCheck() {
	if(!is_touch) {
		disable_menu_swiping();
	}
}

function getMenu() {
	return	'<div id="menu">' +
			'	<h1>' +
			'		<a href="home.html" data-ajax="false"><img src="images/smallTitle.svg"></a>' +
			'	</h1>' +
			'	<ul id="menu">' +
			'		<li><a href="home.html" data-ajax="false"' +
			'			class="contentLink">Home</a></li>' +
			'		<li class="header"><h3>Classes I\'m In</h3></li>' +
			'		<!-- listofClasses is appended by the js in add_menu.js . It uses class_data.js to get the classes -->' +
			'		<li onclick="list_menu_classes('+"0"+')">' +
			'			<div class="expandable_button">' +
			'				<img id="expandable_arow_button0" src="images/menu/triangle_right.png" width="15" height="15">' +
			'				<a href="javascript:void(0)">View All Classes</a>' +
			'			</div>' +
			'		</li>' +
			'		<li class="inner_list"><div id="list_of_classes" class="inner_menu_list" style="display: none;"></div></li>' +
			'		<li><a href="addAClass.php" data-ajax="false" class="contentLink">Add New Class</a></li>' +
			'		<li><a href="myGrades.php" data-ajax="false" class="contentLink">My Grades</a></li>' +
			'		<li><a href="toggleClasses.php" data-ajax="false" class="contentLink">Hide Class</a></li>' +
			'		<li class="header"><h3>Classes I\'m Teaching</h3></li>' +
			'		<li><a href="professorGradebook.php" data-ajax="false" class="contentLink">Grades</a></li>' +
			'		<li><a href="course_management.html" data-ajax="false" class="contentLink">Course Management</a></li>' +
			'		<li><a href="viewClassKeys.php" data-ajax="false" class="contentLink">View Class Keys</a></li>' +
			'		<li class="header"><h3>Account</h3></li>' +
			'		<li><a href="changePassword.php" data-ajax="false" class="contentLink">Change Password</a></li>' +
			'		<li><a href="logOut.php" data-ajax="false" class="contentLink">Sign Out</a></li>' +
			'	</ul>' +
			'</div>';
}

function list_menu_classes(id) {
	var html = '';
	var class_list = document.getElementById('list_of_classes');
	var expanding_button = document.getElementById('expandable_arow_button' + id);
	if(class_list_showing) {
		class_list_showing = false;
		class_list.style.display = "none";
		expanding_button.src = "images/menu/triangle_right.png";
	} else {
		class_list_showing = true;
		var builder = new schoolItemBuilder();
		builder.setList(user_classes);
		builder.showAsSimpleList(true);
		html = builder.createSchoolList();
		class_list.style.display = "block";
		expanding_button.src = "images/menu/triangle_down.png";
		// ADD CLASSES HERE
	}
	class_list.innerHTML = html;

}

var class_list_showing = false;