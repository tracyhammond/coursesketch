function mvsketch(){
    var shadowRoot = undefined;
    var gradevalue = undefined;

}

this.initializeElement = function(templateClone){
    shadowRoot = this.createShadowRoot();
    shadowRoot.appendChild(templateClone);
    
}
function correct(){
    gradevalue = 'correct';
}
function wrong(){
    gradevalue = 'wrong';
}
function redo(){
    gradevalue = 'redo';
    //later will have a function to call that brings up a text box for commments
}






/*//this should activate the template "mvSketch" from the CSS
var t = document.querySelector('mvSketch');
var clone = document.importNode(t.content, true);
document.appendChild(clone);

//just a function that whem called should create a template
function multiSketch(){
	var content = document.querySelector('template').content;
	document.importNode(content, true);
}
/*var content = document.querySelector('template').content;
var link = importDoc.querySelector('#mvsketch');
var template = link.import.querySelector('template');
var clone = document.importNode(template.content, true);

function makeSketchview(elem) {
	var shadow = elem.createShadowRoot();
	var template = document.querySelector('#mvsketch');
	shadow.appendChild(clone.cloneNode(true));
}
 
 

document.querySelector(mvsketch).appendChild(
	document.importNode(content, true));*/
