function addLoadEvent(func) {
  var oldonload = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = func;
  } else {
    window.onload = function() {
      if (oldonload) {
        oldonload();
      }
      func();
    }
  }
}

function loadDynamicFile(filename, filetype, callBack) {
	if(callBack && typeof callBack !== 'function') {
		throw 'Not a valid callBack';  
	}
	var fileref = false;
	if (filetype=="js"){ //if filename is a external JavaScript file
		fileref=document.createElement('script');
		fileref.setAttribute("type","text/javascript");
		fileref.setAttribute("src", filename);
	}
	else if (filetype=="css"){ //if filename is an external CSS file
		fileref=document.createElement("link");
		fileref.setAttribute("rel", "stylesheet");
		fileref.setAttribute("type", "text/css");
		fileref.setAttribute("href", filename);
	}
	
	if (fileref) {
		if(callBack) {
			fileref.onload = callBack; 
		}
		document.getElementsByTagName("head")[0].appendChild(fileref);
	}
}

addLoadEvent(function () {
	loadDynamicFile("js/menu/add_menu.js",'js',function() {
		placeMenu();	
	}
	);
});

loadDynamicFile("css/jquery/jquery.mobile-1.0rc2.min.css",'css');
loadDynamicFile("css/home.css",'css');

/*

EXAMPLE USES

addLoadEvent(nameOfSomeFunctionToRunOnPageLoad);
addLoadEvent(function() {
   more code to run on page load
});
*/

// Used to detirmine if the browser is touch capable
var is_touch = 'ontouchstart' in document.documentElement;