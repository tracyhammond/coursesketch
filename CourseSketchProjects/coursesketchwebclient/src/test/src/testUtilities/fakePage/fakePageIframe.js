(function() {
    $(document).ready(function() {
        var fakePage = document.querySelector("link[data-fake]");
        if (!fakePage) {
            return;
        }
        var iframe = document.createElement("iframe");
        iframe.src = fakePage.src;
        iframe.height = window.innerHeight;
        iframe.width = window.innerWidth;
        document.body.appendChild(iframe);
    });
})();
