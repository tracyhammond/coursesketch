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
    };
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
    };
  }
}

/**
 * A class that handles the dynamic loading of files.
 */
function DynamicFileLoader(givenScope) {
	this.scope = givenScope || document; // the givenScope has to be first in case it does exist
	/**
	 * Dynamically loads a file with a filename, filetype and an onload function.
	 *
	 * @param filename The url of where the file is located.
	 * @param filetype The only supported file types are 'js' and 'css'.
	 * @param onload Called when the file is loaded.
	 */
	this.loadFile = function loadFile(filename, filetype, onload, onerror, fileId) {
		var fileref = this.createFile(filename, filetype, onload, onerror, fileId);
		if (fileref) {
			this.scope.querySelector("head").appendChild(fileref);
		}
	};

	/**
	 * creates a file reference with a filename, filetype and an onload event.
	 *
	 * @param filename The url of where the file is located.
	 * @param filetype The only supported file types are 'js' and 'css'.
	 * @param onload Called when the file is loaded.
	 */
	this.createFile = function createFile(filename, filetype, onload, onerror, fileId) {
		if (onload && typeof onload !== 'function') {
			throw new Error('Not a valid callBack for file: ' + filename + ' type is ' + (typeof onload));  
		}
		var fileref = false;
		if (filetype == "js") { //if filename is a external JavaScript file
			fileref = this.scope.createElement('script');
			fileref.setAttribute("type","text/javascript");
			fileref.setAttribute("src", filename);
		}
		else if (filetype == "css") { //if filename is an external CSS file
			fileref = this.scope.createElement("link");
			fileref.setAttribute("rel", "stylesheet");
			fileref.setAttribute("type", "text/css");
			fileref.setAttribute("href", filename);
		} else if (filetype == "html") { // if filename is an external HTML file
			fileref = this.scope.createElement('link');
			fileref.setAttribute("rel", "import");
			fileref.setAttribute("href", filename);
		}

		if (fileref && onload) {
			fileref.onload = onload;
		}

		if (fileref && onerror) {
			fileref.onerror = onerror;
		}

		if (fileref && fileId) {
			fileref.setAttribute("id", fileId);
		}
		return fileref;
	};

	/**
	 * Removes the given file.  We assume that no Id exists
	 *
	 * @param filename The url of where the file is located that you want removed you only need the ending part of the name.
	 * @param filetype The type of file you want removed. The only supported file types are 'js' and 'css'.
	 */
	this.removeFile = function removefile(filename, filetype) {
		var targetelement = (filetype=="js") ? "script" : (filetype == "css" || filetype == "html") ? "link" : "none"; //determine element type to create nodelist from
		var targetattr = (filetype == "js") ? "src" : (filetype == "css" || filetype == "html") ? "href" : "none"; //determine corresponding attribute to test for
		var allSuspects = this.scope.getElementsByTagName(targetelement);

		for (var i=allSuspects.length; i>=0; i--) { //search backwards within nodelist for matching elements to remove
			if (allSuspects[i] && allSuspects[i].getAttribute(targetattr)!=null && allSuspects[i].getAttribute(targetattr).indexOf(filename)!=-1) {
				allSuspects[i].parentNode.removeChild(allSuspects[i]); //remove element by calling parentNode.removeChild()
			}
		}
	};

	/**
	 * Removes the given file and replaces it with a new file.
	 *
	 * @param filename The url of where the file is located that you want removed you only need the ending part of the name.
	 * @param filetype The type of file you want removed. The only supported file types are 'js' and 'css'.
	 * @param onload Called when the file is loaded.
	 */
	this.replaceFile = function replaceFile(oldfilename, newfilename, filetype, onload, onerror, oldFileId, newfileId) {
		if (!oldFileId && oldfilename) {
			var targetelement = (filetype=="js") ? "script" : (filetype == "css" || filetype == "html") ? "link" : "none"; //determine element type to create nodelist from
			var targetattr = (filetype == "js") ? "src" : (filetype == "css" || filetype == "html") ? "href" : "none"; //determine corresponding attribute to test for
			var allSuspects = this.scope.getElementsByTagName(targetelement);

			for (var i = allSuspects.length; i>=0; i--) { //search backwards within nodelist for matching elements to remove
				if (allSuspects[i] && allSuspects[i].getAttribute(targetattr) != null && allSuspects[i].getAttribute(targetattr).indexOf(oldfilename) != -1) {
					var newElement = this.createFile(newfilename, filetype, onload, onerror, newfileId);
					allSuspects[i].parentNode.replaceChild(newElement, allSuspects[i]);
				}
			}
		} else {
			var newElement = this.createFile(newfilename, filetype, onload, onerror, newfileId);
			var oldElement = this.scope.querySelector('#' + oldFileId);
			oldElement.parentNode.replaceChild(newElement, oldElement);
		}
	};
}
/*
EXAMPLE USES

loader.loadFile("css/jquery/jquery.mobile-1.0rc2.min.css",'css');
loader.loadFile("css/main.css",'css');
*/

/*

EXAMPLE USES

addLoadEvent(nameOfSomeFunctionToRunOnPageLoad);
addLoadEvent(function() {
   more code to run on page load
});
*/