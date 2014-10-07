(function() {
    $(document).ready(function() {
        var fakePage = document.querySelector("link[data-fake]");
        if (!fakePage) {
            return;
        }
        var cloneBody = document.importNode(fakePage.import.body, true);
        var cloneHead = document.importNode(fakePage.import.head, true);
        createStyleSheet(cloneHead);
        var root = document.querySelector("#fakePage").createShadowRoot();
        window.FAKE_PAGE_DOCUMENT = root;
        root.appendChild(cloneHead);
        root.appendChild(cloneBody);
    });

    function createStyleSheet(head) {
        var nodeList = head.querySelectorAll('link[rel="stylesheet"');
        var styleElement = document.createElement("style");
        var style = "";
        for (var i = 0; i < nodeList.length; i++) {
            style += '@import "' + nodeList[i].href + '";\n';
            head.removeChild(nodeList[i]);
        }
        styleElement.innerHTML = style;
        head.appendChild(styleElement);
    }
})();
