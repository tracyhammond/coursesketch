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
/*

EXAMPLE USES

addLoadEvent(nameOfSomeFunctionToRunOnPageLoad);
addLoadEvent(function() {
   more code to run on page load
});
*/

// Used to detirmine if the browser is touch capable
var is_touch = 'ontouchstart' in document.documentElement;