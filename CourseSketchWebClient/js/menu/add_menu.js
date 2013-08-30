function placeMenu() {
	document.getElementById('menuBar').innerHTML = getMenu();
}

function getMenu() {
	return	'<link rel="stylesheet" href="css/menu/menu.css">' +
			'<div id="menu">' +
			'	<h1>' +
			'		<a href="home.html" data-ajax="false"><img src="images/smallTitle.svg"></a>' +
			'	</h1>' +
			'	<ul id="menu">' +
			'		<li><a href="home.html" data-ajax="false"' +
			'			class="contentLink">Home</a></li>' +
			'		<li class="header"><h3>Classes I\'m In</h3></li>' +
			'		<!-- listofClasses is appended by the js in add_menu.js . It uses class_data.js to get the classes -->' +
			'		<li onclick="list_menu_classes()">' +
			'			<div class="class_button">' +
			'				<img id="expandable_arow_button" src="images/menu/triangle_right.png" width="15" height="15">' +
			'				<a href="javascript:void(0)">View All Classes</a>' +
			'			</div>' +
			'		</li>' +
			'		<li class="inner_list"><div id="list_of_classes" class="inner_menu_list" style="display: none;"></div></li>' +
			'		<li><a href="addAClass.php" data-ajax="false" class="contentLink">Add New Class</a></li>' +
			'		<li><a href="myGrades.php" data-ajax="false" class="contentLink">My Grades</a></li>' +
			'		<li><a href="toggleClasses.php" data-ajax="false" class="contentLink">Hide Class</a></li>' +
			'		<li class="header"><h3>Classes I\'m Teaching</h3></li>' +
			'		<li><a href="professorGradebook.php" data-ajax="false" class="contentLink">Grades</a></li>' +
			'		<li><a href="viewClassKeys.php" data-ajax="false" class="contentLink">View Class Keys</a></li>' +
			'		<li class="header"><h3>Account</h3></li>' +
			'		<li><a href="changePassword.php" data-ajax="false" class="contentLink">Change Password</a></li>' +
			'		<li><a href="logOut.php" data-ajax="false" class="contentLink">Sign Out</a></li>' +
			'	</ul>' +
			'</div>';
}

function list_menu_classes() {
	var html = '';
	var class_list = document.getElementById('list_of_classes');
	if(class_list_showing) {
		class_list_showing = false;
		class_list.style.display = "none";
	} else {
		class_list_showing = true;
		html = createClassList(false, false);
		class_list.style.display = "block";
		// ADD CLASSES HERE
	}
	class_list.innerHTML = html;

}

var class_list_showing = false;