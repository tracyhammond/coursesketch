/**
 * Creates a recursive set of functions that are all called onload
 */
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

/**
 * Creates a recursive set of functions that are all called onload
 *
 * This one is given a loading scope
 */
function addScopedLoadEvent(scope, func) {
  var oldonload = scope.onload;
  if (typeof scope.onload != 'function') {
	  scope.onload = func;
  } else {
	  scope.onload = function() {
      if (oldonload) {
        oldonload();
      }
      func();
    }
  }
}

/**
 * A class that handles the dynamic loading of files.
 */
function DynamicFileLoader() {
	this.scope = document;
	this.loadFile = function loadFile(filename, filetype, callBack) {
		var fileref = this.createFile(filename, filetype, callBack);

		if (fileref) {
			this.scope.getElementsByTagName("head")[0].appendChild(fileref);
		}
	}

	this.createFile = function createFile(filename, filetype, callBack) {
		if(callBack && typeof callBack !== 'function') {
			throw 'Not a valid callBack';  
		}
		var fileref = false;
		if (filetype=="js"){ //if filename is a external JavaScript file
			fileref=this.scope.createElement('script');
			fileref.setAttribute("type","text/javascript");
			fileref.setAttribute("src", filename);
		}
		else if (filetype=="css"){ //if filename is an external CSS file
			fileref=this.scope.createElement("link");
			fileref.setAttribute("rel", "stylesheet");
			fileref.setAttribute("type", "text/css");
			fileref.setAttribute("href", filename);
		}

		if(fileref && callBack) {
			fileref.onload = callBack;
		}
		return fileref;
	}	

	/**
	 * Removes the given javascript file.
	 */
	this.removeFile = function removefile(filename, filetype){
		var targetelement=(filetype=="js")? "script" : (filetype=="css")? "link" : "none"; //determine element type to create nodelist from
		var targetattr=(filetype=="js")? "src" : (filetype=="css")? "href" : "none"; //determine corresponding attribute to test for
		var allsuspects=this.scope.getElementsByTagName(targetelement);
		for (var i=allsuspects.length; i>=0; i--) { //search backwards within nodelist for matching elements to remove
			if (allsuspects[i] && allsuspects[i].getAttribute(targetattr)!=null && allsuspects[i].getAttribute(targetattr).indexOf(filename)!=-1) {
				allsuspects[i].parentNode.removeChild(allsuspects[i]); //remove element by calling parentNode.removeChild()
			}
		}
	}

	/**
	 * Replaces the old filename with the new filename.
	 * They must be the same filetype.
	 */
	this.replaceFile = function replaceFile(oldfilename, newfilename, filetype){
		var targetelement=(filetype=="js")? "script" : (filetype=="css")? "link" : "none"; //determine element type to create nodelist using
		var targetattr=(filetype=="js")? "src" : (filetype=="css")? "href" : "none"; //determine corresponding attribute to test for
		var allsuspects=this.scope.getElementsByTagName(targetelement);
		for (var i=allsuspects.length; i>=0; i--){ //search backwards within nodelist for matching elements to remove
			if (allsuspects[i] && allsuspects[i].getAttribute(targetattr)!=null && allsuspects[i].getAttribute(targetattr).indexOf(oldfilename)!=-1){
				var newelement=createjscssfile(newfilename, filetype);
				allsuspects[i].parentNode.replaceChild(newelement, allsuspects[i]);
			}
		}
	}
}

var loader = new DynamicFileLoader();
loader.loadFile("css/jquery/jquery.mobile-1.0rc2.min.css",'css');
loader.loadFile("css/main.css",'css');

/*

EXAMPLE USES

addLoadEvent(nameOfSomeFunctionToRunOnPageLoad);
addLoadEvent(function() {
   more code to run on page load
});
*/

// Used to detirmine if the browser is touch capable
var is_touch = 'ontouchstart' in document.documentElement;