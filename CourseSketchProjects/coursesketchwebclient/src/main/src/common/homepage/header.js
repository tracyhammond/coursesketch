function HeaderHandler() {
    var open;
    var handler = this;
    this.animateHeader = function(value) {
        var height = $(".ui-header").height();
        if (value) { // close header
            $(".ui-header").animate({
                top: '-'+(height+2)+'px',
                }, 300, function () {
                    open = false;
            });
            return false;
        } else { // open header
            $(".ui-header").animate({
                top: "0px",
                }, 300, function () {
                    open = true;
            });
            return false;
        }
    };

    this.changeText = function(titleText) {
        document.getElementById("nameBlock").textContent = titleText;
    };

    this.getMenuObject = function() {
        return false;
    };

    this.isOpen = function() {
        return open;
    };
}
