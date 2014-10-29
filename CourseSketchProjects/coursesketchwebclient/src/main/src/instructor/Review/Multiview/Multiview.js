

<script>
    (function(importDoc) {
        $(document).ready(function() {
            var link = importDoc.querySelector('#schoolItemTemplateImport');
            var template = link.import.querySelector('#schoolItemTemplate');
            var clone = document.importNode(template.content, true);

            var newElement = new SchoolItem();

            /**
             * @Method
             * Called when the element is created, using document.create()(but does nothing else)
             */
            SchoolItem.prototype.createdCallback = function() {
            };

            /**
             * Creates the shadow root and adds that into the system.
             */
            SchoolItem.prototype.attachedCallback = function() {
                this.initializeElement(clone.cloneNode(true));
            };

            SchoolItem.prototype.detachedCallback = function() {
                //this.finalize();
            };

            document.registerElement('school-item', {
                prototype : newElement
            });
        });
    })(document.currentScript.ownerDocument);
</script>




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
