// JavaScript source code
/*function showOptions(optionNumber) {
    alert(optionNumber);
    var element = scope.createElement("p");
    element.textContent = optionNumber;
    scope.body.appendChild(element);


}*/

function createBox() {


    var scope = document;

    var di1 = scope.createElement("div");
    var inp = scope.createElement("input");
    var di2 = scope.createElement("div");
    var bu1 = scope.createElement("button");
    var bu2 = scope.createElement("button");
    var bu3 = scope.createElement("button");
    var bu4 = scope.createElement("button");
    var bu5 = scope.createElement("button");
    var bu6 = scope.createElement("button");

    di1.setAttribute("class", "colored_box");



    bu1.setAttribute("class", "button_subject");
    bu1.setAttribute("data-col", "1");
    bu2.setAttribute("class", "button_subject");
    bu2.setAttribute("data-col", "2");
    bu3.setAttribute("class", "button_subject");
    bu3.setAttribute("data-col", "1");
    bu4.setAttribute("class", "button_subject");
    bu4.setAttribute("data-col", "2");
    bu5.setAttribute("class", "button_subject");
    bu5.setAttribute("data-col", "1");
    bu6.setAttribute("class", "button_subject");
    bu6.setAttribute("data-col", "2");

    bu1.setAttribute("name", "Math");
    bu2.setAttribute("name", "English");
    bu3.setAttribute("name", "Phsyics");
    bu4.setAttribute("name", "Chemistry");
    bu5.setAttribute("name", "Computer");
    bu6.setAttribute("name", "Arts");

    bu1.textContent = "Math";
    bu2.textContent = "English";
    bu3.textContent = "Physics";
    bu4.textContent = "Chemistry";
    bu5.textContent = "Computer";
    bu6.textContent = "Arts";

    di1.appendChild(inp);
    di1.appendChild(di2);
    di2.appendChild(bu1);
    di2.appendChild(bu2);
    di2.appendChild(scope.createElement("br"));
    di2.appendChild(bu3);
    di2.appendChild(bu4);
    di2.appendChild(scope.createElement("br"));
    di2.appendChild(bu5);
    di2.appendChild(bu6);


    di1.style.position = "relative";
    di1.style.left = "200px";

    scope.body.appendChild(di1);

    function disableButtons()
    {
        bu1.disabled = true;
        bu2.disabled = true;
        bu3.disabled = true;
        bu4.disabled = true;
        bu5.disabled = true;
        bu6.disabled = true;
        
    }

    var disable = function () {
        showOptions1(this.name, this.dataset.col)
        disableButtons();
    }

    bu1.onclick = disable;
    bu2.onclick = disable;
    bu3.onclick = disable;
    bu4.onclick = disable;
    bu5.onclick = disable;
    bu6.onclick = disable;



}

function showOptions(name) {

    var scope = document;

    var di1 = scope.createElement("div");
    var inp = scope.createElement("input");
    var di2 = scope.createElement("div");
    var bu1 = scope.createElement("button");
    var bu2 = scope.createElement("button");
    var bu3 = scope.createElement("button");
    var bu4 = scope.createElement("button");
    var bu5 = scope.createElement("button");
    var bu6 = scope.createElement("button");

    di1.setAttribute("class", "colored_box");

    switch (name) {
        case 1:
            option1 = Angle;
            option2 = Cosine;
            option3 = Sine;
            option4 = Tangent;
            option5 = Factorial;
            option6 = Exponent;
            break;
        case 2:
            option1 = Angle;
            option2 = Cosine;
            option3 = Sine;
            option4 = Tangent;
            option5 = Factorial;
            option6 = Exponent;
            break;
        case 3:
            option1 = Angle;
            option2 = Cosine;
            option3 = Sine;
            option4 = Tangent;
            option5 = Factorial;
            option6 = Exponent;
            break;
        case 4:
            option1 = Angle;
            option2 = Cosine;
            option3 = Sine;
            option4 = Tangent;
            option5 = Factorial;
            option6 = Exponent;
            break;
        case 5:
            option1 = Angle;
            option2 = Cosine;
            option3 = Sine;
            option4 = Tangent;
            option5 = Factorial;
            option6 = Exponent;
            break;
        case 6:
            option1 = Angle;
            option2 = Cosine;
            option3 = Sine;
            option4 = Tangent;
            option5 = Factorial;
            option6 = Exponent;
            break;
        default:

    }

    bu1.setAttribute("class", "button_subject");
    bu1.setAttribute("class", "button_subject");
    bu2.setAttribute("class", "button_subject");
    bu3.setAttribute("class", "button_subject");
    bu4.setAttribute("class", "button_subject");
    bu5.setAttribute("class", "button_subject");
    bu6.setAttribute("class", "button_subject");

    bu1.setAttribute("name", this.option1);
    bu2.setAttribute("name", this.option2);
    bu3.setAttribute("name", this.option3);
    bu4.setAttribute("name", this.option4);
    bu5.setAttribute("name", this.option5);
    bu6.setAttribute("name", this.option6);

    bu2.textContent = this.option1;
    bu2.textContent = this.option2;
    bu2.textContent = this.option3;
    bu2.textContent = this.option4;
    bu2.textContent = this.option5;
    bu2.textContent = this.option6;

    bu1.setAttribute("onclick", "showOptions(this.name)");
    bu2.setAttribute("onclick", "showOptions(this.name)");
    bu3.setAttribute("onclick", "showOptions(this.name)");
    bu4.setAttribute("onclick", "showOptions(this.name)");
    bu5.setAttribute("onclick", "showOptions(this.name)");
    bu6.setAttribute("onclick", "showOptions(this.name)");


    di1.appendChild(inp);
    di1.appendChild(di2);
    di2.appendChild(bu1);
    di2.appendChild(bu2);
    di2.appendChild(scope.createElement("br"));
    di2.appendChild(bu3);
    di2.appendChild(bu4);
    di2.appendChild(scope.createElement("br"));
    di2.appendChild(bu5);
    di2.appendChild(bu6);


    scope.body.appendChild(di1);


}

function showOptions1(name, col) {

    var scope = document;
    var numButtons = 6;
    var di1 = scope.createElement("div");
    var di2 = scope.createElement("div");
    var bu = new Array();
    var spfc = new Array();

    
    di1.setAttribute("class", "colored_box");
    di1.style.position = "relative";
    di1.style.left = "200px";

    ButtonObject(name, spfc);

    for (i = 0; i < 6; i++)
    {
        bu[i] = scope.createElement("button");
        bu[i].setAttribute("class", "button_subject");
        bu[i].setAttribute("name", "spfc[i]");
        bu[i].textContent = "test";
        di1.appendChild(di2);
        di2.appendChild(bu[i]);
        if (i % 2 == 1)
        {
            di2.appendChild(scope.createElement("br"));

        }
        scope.body.appendChild(di1);
    }











   /* var bu1 = scope.createElement("button");
    var bu2 = scope.createElement("button");
    var bu3 = scope.createElement("button");
    var bu4 = scope.createElement("button");
    var bu5 = scope.createElement("button");
    var bu6 = scope.createElement("button");

    di1.setAttribute("class", "colored_box");
    di1.style.position = "relative";
    di1.style.left = "200px";

    bu1.setAttribute("class", "button_subject");
    bu2.setAttribute("class", "button_subject");
    bu3.setAttribute("class", "button_subject");
    bu4.setAttribute("class", "button_subject");
    bu5.setAttribute("class", "button_subject");
    bu6.setAttribute("class", "button_subject");

    bu1.setAttribute("name", "Angle");
    bu2.setAttribute("name", "Cosine");
    bu3.setAttribute("name", "Sine");
    bu4.setAttribute("name", "Tangent");
    bu5.setAttribute("name", "Factorial");
    bu6.setAttribute("name", "Exponent");

    bu1.textContent = "Angle";
    bu2.textContent = "Cosine";
    bu3.textContent = "Sine";
    bu4.textContent = "Tangent";
    bu5.textContent = "Factorial";
    bu6.textContent = "Exponent";

    di1.appendChild(di2);
    di2.appendChild(bu1);
    di2.appendChild(bu2);
    di2.appendChild(scope.createElement("br"));
    di2.appendChild(bu3);
    di2.appendChild(bu4);
    di2.appendChild(scope.createElement("br"));
    di2.appendChild(bu5);
    di2.appendChild(bu6);

    if (col == 1)
    {
        di1.style.position = "relative";
        di1.style.left = "-178px"
    }
    
    if (col == 2) {
        di1.style.position = "relative";
        di1.style.left = "185px"
    }

    scope.body.appendChild(di1);*/
}

function testFunction() {

    // setup test data

    // setup text box using test data
    createTextBox(position, buttons, inputType, server);
}

function myFunction(x) {
    var whichSelected = x.selectedIndex;
    var posVal = x.options[whichSelected].text;
    var elem = document.getElementById("myDiv");
    elem.style.position = posVal;
}

function createSmallBox() {

    var scope = document;

    var di1 = scope.createElement("div");
    var inp = scope.createElement("input");
    var di2 = scope.createElement("div");
    var bu1 = scope.createElement("button");
    var bu2 = scope.createElement("button");


    di1.setAttribute("class", "colored_box");



    bu1.setAttribute("class", "showOptions1(this.name)");
    bu2.setAttribute("class", "showOptions1()");


    bu1.setAttribute("name", "Math");
    bu2.setAttribute("name", "English");


    bu1.textContent = "Math";
    bu2.textContent = "English";



    /*
    bu2.setAttribute("class", "button_subject");
    bu3.setAttribute("class", "button_subject");
    bu4.setAttribute("class", "button_subject");
    bu5.setAttribute("class", "button_subject");
    bu6.setAttribute("class", "button_subject");*/


    di1.appendChild(inp);
    di1.appendChild(di2);
    di2.appendChild(bu1);
    di2.appendChild(bu2);



    //  di2.onclick = showOptions();

    scope.body.appendChild(di1);
}

function ButtonObject(name, array) {
    this.buttonName = name;
    this.subButtons = array;

    switch(name)
    {
        case Math:
            subButtons[0].name="Angle";
            subButtons[1].name="Cosine";
            subButtons[2].name="Sine";
            subButtons[3].name="Tangent";
            subButtons[4].name="Factorial";
            subButtons[5].name="Exponent";

    }

}

//new ButtonObject("Math", ["Angle", "Cosine", .......);