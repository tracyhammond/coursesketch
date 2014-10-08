function SchoolItem() {

    var shadowRoot = undefined;
    var isOverflow = false;

    /**
     * @param document
     *            {document} The document in which the node is being imported
     *            to.
     * @param templateClone
     *            {Element} an element representing the data inside tag, its
     *            content has already been imported and then added to this
     *            element.
     */
    this.initializeElement = function(document, templateClone) {
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
        this.checkOverflow();
        this.buildExpandEvent();
        this.setUpEditButtons();
    };

    /**
     * Checks for overflow in an element.
     */
    this.checkOverflow = function() {
        var descriptionHolder = shadowRoot.querySelector('.description');
        var descriptionContent = shadowRoot.querySelector('.description content');

        var nodes = descriptionContent.getDistributedNodes();
        var element = nodes[0];
        var text = element.textContent;

        var widths = [ descriptionHolder.clientWidth, descriptionHolder.offsetWidth, descriptionHolder.scrollWidth, this.style.width,
                this.style.maxwidth ];
        var usedWidth = $(window).width();
        for (var i = 0; i < widths.length; i++) {
            var width = widths[i];
            if (typeof width == "string") {
                if (width.endsWith("px")) {
                    width = width.substring(0, width.indexOf("px"));
                    width = parseInt(width);
                } else {
                    break;
                }
            }
            if (width && width < usedWidth) {
                usedWidth = width;
            }
        }
        var padding = 20; // left right padding is 10
        isOverflow = checkTextOverflow(usedWidth - padding, text, $(element).css('font'));
        return isOverflow;
    };

    /**
     * Given an allowed width, text and font it returns true if the element is
     * over 3 lines long.
     */
    function checkTextOverflow(widthAllowed, text, font) {
        var totalWidth = getTextWidth(text, font);
        var lines = totalWidth / widthAllowed;
        var numberOfLines = Math.round(lines);
        if (numberOfLines > 3) {
            return true;
        }
        return false;
    }

    /**
     * Builds the event for what happens when expanding content.
     */
    this.buildExpandEvent = function() {
        var paragraph = shadowRoot.querySelector('.description p');
        if (isOverflow) {
            $(paragraph).addClass("overflow");
        }

        var descriptionContent = shadowRoot.querySelector('.description content');

        var nodes = descriptionContent.getDistributedNodes();
        var contentElement = nodes[0];

        var button = shadowRoot.querySelector('.description p + p');
        var expanded = true;
        $(button).click(function() {
            if ($(button).hasClass('expand')) {
                $(button).removeClass('expand');
                $(button).addClass('contract');
                $(contentElement).addClass('expandedContent');
                $(paragraph).addClass('expandedContent');
            } else {
                $(button).removeClass('contract');
                $(button).addClass('expand');
                $(contentElement).removeClass('expandedContent');
                $(paragraph).removeClass('expandedContent');
            }
        });
    };

    /**
     * Sets up what happens when an edit button is clicked.
     */
    this.setUpEditButtons = function() {
        if (isUndefined(this.dataset.instructor)) {
            return;
        }

        var editingClass = 'currentlyEditing';
        [].forEach.call(shadowRoot.querySelectorAll('.editButton'), function(element) {
            var parentNode = element.parentNode;
            var content = parentNode.querySelector('content');
            var nodes = content.getDistributedNodes();
            var contentElement = nodes[0];
            var editorElement = getEditorElement(parentNode);
            $(element).click(function() {
                if ($(parentNode).hasClass(editingClass)) {
                    $(parentNode).removeClass(editingClass);
                    $(contentElement).removeClass(editingClass);
                    parentNode.removeChild(editorElement);
                    contentElement.textContent = editorElement.value;
                } else {
                    $(parentNode).addClass(editingClass);
                    // makes the display = none
                    $(contentElement).addClass(editingClass);
                    editorElement.value = contentElement.textContent;
                    parentNode.insertBefore(editorElement, element);
                }
            });
        });
    };

    function getEditorElement(parentNode) {
        return document.createElement('input');
    }
}

SchoolItem.prototype = Object.create(HTMLElement.prototype);
