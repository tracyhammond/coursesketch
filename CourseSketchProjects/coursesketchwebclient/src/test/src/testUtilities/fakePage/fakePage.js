(function() {
    $(document).ready(function() {
        var link = document.querySelectorAll("link[data-fake]")[0];
        if (!link) {
            return;
        }

        console.log('grabbed link with id: link[data-fake]', ' the resulting link is ', link);
        var cloneBody = document.importNode(link.import.body, true);
        console.log('grabbed body from id: link[data-fake]', ' the resulting body is ', cloneBody);
        var cloneHead = document.importNode(link.import.head, true);
        console.log('grabbed head from id: link[data-fake]', ' the resulting head is ', cloneHead);
        document.head.appendChild(cloneHead);
        document.body.appendChild(cloneBody);

        // basically removes the duplicate body.
        $("body > body").replaceWith(function() {
            return this.children;
        });



    });
})();
