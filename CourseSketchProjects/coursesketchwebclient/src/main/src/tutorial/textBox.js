function TextBox() {
    // This makes the dialog moveable using the interact.js library
    function enableDragging() {
        interact(shadowRoot.querySelector("#textBoxDialog"))
            .ignoreFrom("textarea, button")
            .draggable({
                onmove: function (event) {
                    var target = event.target,
                        x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx,
                        y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;

                    target.style.webkitTransform =
                    target.style.transform =
                        'translate(' + x + 'px, ' + y + 'px)';

                    target.setAttribute('data-x', x);
                    target.setAttribute('data-y', y);
                },
                
            })
            .inertia(false)
            .restrict({
                drag: "parent",
                endOnly: true,
                elementRect: { top: 0, left: 0, bottom: 1, right: 1 }
        });
    }
    
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        
        // Click action for the "X" that closes the dialog
        shadowRoot.querySelector("#closeButton").onclick = function() {
            localScope.parentNode.removeChild(localScope);
        };
        enableDragging();
    };
}

TextBox.prototype = Object.create(HTMLDialogElement.prototype);

