var foo = [
	{
		"title":"this is a title",
		"summary":"this is a summary"
	}
];
var courseSelected = function(evt) {
	var item = $(evt.currentTarget);
	var title = item[0].children[0];
	$("#placeholder").css({
		display: "none"
	});
	$("#add").css({
		display: "inline-block"
	});
	$.each($(".list-item"), function() {
		if($(this) != item)
			$(this).removeClass("selected");
	});
	$(item).addClass("selected");
}

var addLecture = function(evt) {
	$("#col2>.content").append("<span class=\"lecture\"><div class=\"title\">TITLE</div><div class=\"summary\">HELLO WORLD</div></span>");
}

$(document).ready(function() {
	$.each($(".list-item"), function() {
		var children = $(this).children();
		if(typeof children[0] != "undefined")
		$(this).bind("click", children, courseSelected);
	});
	$("#add").bind("click", addLecture);
})
