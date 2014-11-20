/*
 * The MvSketch function handles all the action that can take place
 * in the multiview units.
 */
function MvSketch() {
    var shadowRoot = undefined;
    var gradevalue = undefined;


    /*
     * This creates the shadow root and attches it to the object in question.
     */
    this.initializeElement = function(templateClone) {
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        shadowRoot.querySelector(".correctButton").onclick = correct;
        shadowRoot.querySelector(".wrongButton").onclick = wrong;
    };

    /*
     * Marks the sketch at correct and changes the background to outercorrect.
     */
    function correct() {
        gradevalue = 'correct';
        shadowRoot.querySelector("#outer").className='outerCorrect';
    }
    /*
     * Marks the sketch as wrong and changes the background to outerwrong.
     */
    function wrong() {
        gradevalue = 'wrong';
        shadowRoot.querySelector("#outer").className='outerWrong';
    }

}

MvSketch.prototype = Object.create(HTMLElement.prototype);