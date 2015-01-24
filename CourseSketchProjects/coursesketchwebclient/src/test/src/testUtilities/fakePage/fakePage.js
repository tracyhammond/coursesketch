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
        // basically removes the duplicate body.
        $("body > body").replaceWith(function() {
            return this.children;
        });

        var style = document.createElement("style");
        style.innerHTML = ":root > body { position: relative; border:5px solid #cceeee; width:calc(100% - 12px); height:calc(100% - 12px); "
            + "margin:0; padding:0;}"
            + "html { padding:0px; margin:0px; border:0px;}";
        document.head.appendChild(style);

    });
})();
