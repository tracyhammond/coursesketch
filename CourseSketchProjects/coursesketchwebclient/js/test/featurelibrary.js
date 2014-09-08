// JavaScript source code

function FeatureLibrary() {

    this.screenWidth = function() {
        console.log("Available Width: " + screen.availWidth);
        //var dpi_x = document.getElementById('testdiv').offsetWidth;
        //console.log("Dots Per Inch: " + dpi_x);
        var widthPPI=document.getElementById(“div1?).offsetWidth;
        return screen.availWidth
    }

    this.screenHeight = function() {
        console.log("Available Height: " + screen.availHeight);
        //var dpi_y = document.getElementById('testdiv').offsetHeight;
        //console.log("Dots Per Inch: " + dpi_y);
        var heightPPI=document.getElementById(“div1?).offsetHeight;
        return screen.availHeight
    }

    this.touchScreen = function() {

        var is_touch_device = 'ontouchstart' in document.documentElement;
        console.log("Touch Device Result: " + is_touch_device);
        return is_touch_device;

    }


    function multiTouch() {


    }


    function webSocket() {


    }


    function indexDB() {


    }





}


