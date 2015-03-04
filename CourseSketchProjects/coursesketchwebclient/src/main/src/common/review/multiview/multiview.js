/*
 * The MvSketch function handles all the action that can take place
 * in the multiview units.
 * Attributes:
 * data-binary: if set then the button will be disabled.
 * data-max_points: if set then this is the max number of points that can be input
 * data-max_percent: if set then this is the max percent of score that can be used
 */
function MvSketch() {
    this.maxValue = 100;
    this.gradeValue = undefined;

    /**
     * sets the update list.
     * after the update list is done loading
     * @param updateList  a list that contains all the changes made in sketch.
     */
    this.setUpdateList = function(updateList)  {
        this.shadowRoot.querySelector("sketch-surface").loadUpdateList(updateList, undefined, function() {
            console.log("Resizing the canvas");
            this.shadowRoot.querySelector("sketch-surface").fillCanvas();
        }.bind(this));
    };

    /*
     * This creates the shadow root and attaches it to the object in question.
     */
    this.initializeElement = function(templateClone) {
        this.createShadowRoot();
        this.shadowRoot.appendChild(templateClone);

        this.shadowRoot.querySelector(".correctButton").onclick = correct.bind(this);
        this.shadowRoot.querySelector(".wrongButton").onclick = wrong.bind(this);
        this.shadowRoot.querySelector("input").addEventListener("click",
            function(event) {event.stopPropagation();}, false);
        this.setupAttributes();
    };

    /**
     * Looks at the data attributes of this element and configures the element appropriately.
     */
    this.setupAttributes = function() {
        if (!isUndefined(this.dataset) && this.dataset.binary === "true" || this.dataset.binary === "") {
            this.shadowRoot.querySelector("#gradeInput").disabled = true;
        }
        if (!isUndefined(this.dataset) && !isUndefined(this.dataset.max_points) && this.dataset.max_points !== "") {
            this.shadowRoot.querySelector("#gradeInput").max = this.dataset.max_points;
            this.shadowRoot.querySelector("#gradeInput").className = "point";
            this.maxValue = parseFloat(this.dataset.max_points);
        }
        if (!isUndefined(this.dataset) && !isUndefined(this.dataset.max_percent) && this.dataset.max_percent !== "") {
            this.shadowRoot.querySelector("#gradeInput").max = this.dataset.max_percent;
            this.shadowRoot.querySelector("#gradeInput").className = "percent";
        }
    };

    /*
     * Marks the sketch at correct and changes the background to outercorrect.
     */
    function correct(event) {
        event.stopPropagation();
        this.gradeValue = this.maxValue;
        this.shadowRoot.querySelector("#outer").className='outerCorrect';
        this.shadowRoot.querySelector("#gradeInput").value = parseFloat(this.gradeValue);
    }

    /*
     * Marks the sketch as wrong and changes the background to outerwrong.
     */
    function wrong(event) {
        event.stopPropagation();
        this.gradeValue = 0;
        this.shadowRoot.querySelector("#outer").className='outerWrong';
        this.shadowRoot.querySelector("#gradeInput").value = parseFloat(this.gradeValue);
    }

    /**
     * Sets the callback that is called when the sketch is clicked.
     */
    this.setSketchClickedFunction = function(sketchClickedFunction) {
        this.shadowRoot.querySelector("sketch-surface").onclick = sketchClickedFunction;
    };
}

MvSketch.prototype = Object.create(HTMLElement.prototype);
