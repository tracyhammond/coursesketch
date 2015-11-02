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



    });
})();
