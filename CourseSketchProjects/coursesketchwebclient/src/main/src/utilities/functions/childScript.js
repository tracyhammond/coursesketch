/**
 * This is used in all pages that are contained in an iFrame and setup simple
 * items for them.
 */

var CourseSketch = parent.CourseSketch;

(function() {
    if (typeof CourseSketch === "undefined") {
        CourseSketch = {};
    }

    var namespaceList = [];
    CourseSketch.addNamespace = function(namespaceName) {
        CourseSketch[namespaceName] = {};
        namespaceList.push(namespaceName);
    };

    /**
     * Empties out all added name spaces. Including the addNamespace function.
     */
    $(window).unload(function() {
        for (var i = 0; i < namespaceList.length; i++) {
            CourseSketch[namespaceList[i]] = undefined;
        }
        CourseSketch.addNamespace = undefined;
    });

    /**
     * Looks at the script tag to get the script
     * <script data-namespace="name1, name2,name3 , name4" src="childScript.js"></script>
     */
    var scriptElement = document.currentScript;

    // prevents multiple additions of the name space.
    if (scriptElement.ownerDocument.URL.indexOf("FakePage.html") > -1 ) {
        return;
    }
    if (typeof scriptElement.dataset.namespace !== "undefined") {
        var dataNamespaceList = scriptElement.dataset.namespace.split(",");
        for (var i = 0; i < dataNamespaceList.length; i++) {
            CourseSketch.addNamespace(dataNamespaceList[i].trim());
        }
    }
})();
