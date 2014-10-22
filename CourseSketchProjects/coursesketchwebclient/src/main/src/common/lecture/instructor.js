var foo = [
	{
		"title":"this is a title",
		"summary":"this is a summary"
	}
];
var courseSelected = function(evt) {
	var item = $(evt.currentTarget);
	var title = item[0].children[0];
	$("#placeholder").text($(title).text());
	$.each($(".list-item"), function() {
		if($(this) != item)
			$(this).removeClass("selected");
	});
	$(item).addClass("selected");
}

var addLecture = function(evt) {
	$("#col2").append("<span>HELLO WORLD</span>");
}

$(document).ready(function() {
	$.each($(".list-item"), function() {
		var children = $(this).children();
		if(typeof children[0] != "undefined")
		$(this).bind("click", children, courseSelected);
	});
	$("#add").bind("click", addLecture);
})
