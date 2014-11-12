function Mvsketch(){
    var shadowRoot = undefined;
    var gradevalue = undefined;



    this.initializeElement = function(templateClone){
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

    };
    function correct(){
        gradevalue = 'correct';
        var changing = document.getElementById("outer");
        changing.className="outercorrect";
    }
    function wrong(){
        gradevalue = 'wrong';
        var changing = document.getElementById("outer");
        changing.className="outerwrong";
    }
    function redo(){
        gradevalue = 'redo';
        var changing = document.getElementById("outer");
        changing.className="outerredo";
        //later will have a function to call that brings up a text box for commments
    }
    document.getElementById("correct").onClick = correct();
    document.getElementById("wrong").onClick =  wrong();
    
    document.getElementById("redo").onClick = redo();


   // function getData(){
       // var sketches = new Array();

};

Mvsketch.prototype = Object.create(HTMLElement.prototype);