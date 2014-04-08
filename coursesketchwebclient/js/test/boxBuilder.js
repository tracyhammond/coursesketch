/* 
Default Menu Type - Shows the default options for a text box
Set option Type - Enter options to use in text box 
Essay Type - Takes up Right half of the screen

*/

function BoxBuilder() {
    /**
	 * resets the values in the school builder so that the same build object can be used again.
	 *
	 * NOTE: ONLY DATA SHOULD GO HERE.
	 */

    this.resetValues = function resetValues() {
        this.hasMainBox = true; // has a main box which we navigate from
        this.hasMiniTextInput = true; // has an text input box
        this.isEssayBox = false; // is of essay type if true
        this.hasNoTextInput = false; // has no text input if true
        this.hasPredictions = false; // has prediction options if true
        this.numOfTabs = '0'; //has the number of essay tabs for essay boxes
        this.autoClose = false; //if true will close automatically
        this.buttonList = new Array();
        this.mainButtonList = new Array();
        this.subButtonList = new Array();




    };
    this.resetValues();

    var scope = document;
    

    this.build = function () {

        var localButtonList;  // either copies from this.buttonList or filled from predictions
        var entireBox = scope.createElement("div");


        if (this.hasMainBox) {
            console.log("Main box is true!");

            this.copyButtonList();
            
            entireBox.setAttribute("class", "colored_box");

            if (this.hasNoTextInput) {
                entireBox.appendChild(this.buildPredictions());
            }

            if (this.hasMiniTextInput) {
                var inp = scope.createElement("input"); // creates input box
                inp.setAttribute("class", "input_box"); 
                entireBox.appendChild(inp);
                
                if (this.hasUnits) {
                    var inp2 = scope.createElement("input"); // creates unit box
                    inp2.setAttribute("class", "unit_input_box");
                    entireBox.appendChild(inp2);
                    entireBox.appendChild(this.buildUnitsBox());
                }
                else if (this.hasPredictions)
                    entireBox.appendChild(this.buildPredictions((["2", "3", "4", "5", "7"])));
            }


            entireBox.style.position = "absolute";

           // scope.body.appendChild(entireBox);
           // alert(entireBox);
            
        }
        return entireBox;
        };



        this.buildUnitsBox = function () { 
            if (this.hasUnits) {
                var div = scope.createElement("div");
                
                if(this.hasPredictions)
                    div.appendChild(this.buildPredictions((["2", "3", "4", "5", "7"])));// How do I
                                                                                     // set the strings
                return div;
            }

        };

        this.createButtons = function () {
            var div = scope.createElement("div");
            var b = scope.createElement("button");
            div.appendChild(b);
            return div;
        }

        this.buildPredictions = function (array) {
            //if (this.hasUnits) {
                var div = scope.createElement("div");
                var bu = new Array();
                var al = array.length;
                
                for (i = 0; i < al; i++) {

                    bu[i] = scope.createElement("button");
                    bu[i].setAttribute("class", "button_subject");
                    bu[i].textContent = array[i];
                    div.appendChild(bu[i]);// adds text in buttons

                    if (((i + 1) % Math.floor(Math.sqrt(al))) == 0)// spacing of the buttons 
                        //(every sqrt of number of buttons add space)
                        div.appendChild(scope.createElement("br"));


                }
                return div;
            
        };
        
        

        

        this.copyButtonList = function(array){
           // for (i = 0; i < array.length; i++) {
             //   this.ButtonList[i] = array[i];
            //}
        };

        this.setButtonList = function (array) {
            array = this.ButtonList;
            this.copyButtonList(array)
        };
        

        this.setMainButtonList = function (array) {
            array = this.mainButtonList;
            this.copyButtonList(array)
        };


        this.setSubButtonList = function (array) {
            array = this.subButtonList;
            this.copyButtonList(array)
        };


}



