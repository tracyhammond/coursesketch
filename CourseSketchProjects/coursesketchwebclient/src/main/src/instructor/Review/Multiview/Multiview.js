function Mvsketch(){
    var shadowRoot = undefined;
    var gradevalue = undefined;



    this.initializeElement = function(templateClone){
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        shadowRoot.querySelector(".correctButton").onclick = correct;
        shadowRoot.querySelector(".wrongButton").onclick = wrong;
    };
    function correct(){
        gradevalue = 'correct';
        shadowRoot.querySelector("#outer").className='outerCorrect';
    }
    function wrong(){
        gradevalue = 'wrong';
        shadowRoot.querySelector("#outer").className='outerWrong';
    }
  //  function redo(){
       // gradevalue = 'redo';
       // shadowRoot.querySelector(".outer").backgroundColor='green';
        //later will have a function to call that brings up a text box for commments
   // }
    

};

Mvsketch.prototype = Object.create(HTMLElement.prototype);