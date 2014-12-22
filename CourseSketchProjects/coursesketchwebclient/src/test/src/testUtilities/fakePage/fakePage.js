(function() {
    $(document).ready(function() {
        var fakePage = document.querySelector("link[data-fake]");
        if (!fakePage) {
            return;
        }
        var cloneBody = document.importNode(fakePage.import.body, true);
        var cloneHead = document.importNode(fakePage.import.head, true);
        document.head.appendChild(cloneHead);
        document.body.appendChild(cloneBody);
        var style = document.createElement("style");
        style.innerHTML = "body {border:5px solid #cceeee; width:calc(100% - 10px); height:calc(100% - 10px);}";
        document.head.appendChild(style);

    });
})();
