/*
 * The MvSketch function handles all the action that can take place
 * in the multiview units.
 * Attributes:
 * data-binary: if set then the button will be disabled.
 * data-max_points: if set then this is the max number of points that can be input
 * data-max_percent: if set then this is the max percent of score that can be used
 */
function MvSketch() {
    var maxValue = 100;
    var shadowRoot = undefined;
    var gradeValue = undefined;


    /**
     * sets the update list.
     * @param updateList  a list that contains all the changes made in sketch.
     */
    this.setUpdateList = function(updateList)  {
        shadowRoot.querySelector("sketch-surface").loadUpdateList(updateList, undefined);
    };

    /*
     * This creates the shadow root and attaches it to the object in question.
     */
    this.initializeElement = function(templateClone) {
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        shadowRoot.querySelector(".correctButton").onclick = correct;
        shadowRoot.querySelector(".wrongButton").onclick = wrong;
        shadowRoot.querySelector("input").addEventListener("click",
            function(event) {event.stopPropagation()}, false);
        this.setupAttributes();
    };

    this.setupAttributes = function() {
        if (!isUndefined(this.dataset) && this.dataset.binary == "true" || this.dataset.binary == "") {
            shadowRoot.querySelector("#gradeInput").disabled = true;
        }
        if (!isUndefined(this.dataset) && this.dataset.max_points != "") {
            shadowRoot.querySelector("#gradeInput").max = this.dataset.max_points;
            shadowRoot.querySelector("#gradeInput").className = "point";
            maxValue = parseFloat(this.dataset.max_points);
        }
        if (!isUndefined(this.dataset) && this.dataset.max_percent != "") {
            shadowRoot.querySelector("#gradeInput").max = this.dataset.max_percent;
            shadowRoot.querySelector("#gradeInput").className = "percent";
        }
    };

    /*
     * Marks the sketch at correct and changes the background to outercorrect.
     */
    function correct(event) {
        event.stopPropagation();
        gradeValue = maxValue;
        shadowRoot.querySelector("#outer").className='outerCorrect';
    }

    /*
     * Marks the sketch as wrong and changes the background to outerwrong.
     */
    function wrong(event) {
        event.stopPropagation();
        gradeValue = 0;
        shadowRoot.querySelector("#outer").className='outerWrong';
    }
}

MvSketch.prototype = Object.create(HTMLElement.prototype);
