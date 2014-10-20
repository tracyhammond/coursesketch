function SchoolItem() {
    var shadowRoot = undefined;
    var isOverflow = false;
    var editFunction = undefined;

    /**
     * @param templateClone
     *            {Element} an element representing the data inside tag, its
     *            content has already been imported and then added to this
     *            element.
     */
    this.initializeElement = function(templateClone) {
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
        if (!nodes || nodes == null || nodes.length == 0) {
            isOverflow = false;
            return false;
        }
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
        if (!isOverflow) {
            return; // do nothing if there is no overflow
        }

        var paragraph = shadowRoot.querySelector('.description p');
        $(paragraph).addClass("overflow");

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
        var localScope = this;
        if (isUndefined(this.dataset.instructor)) {
            return;
        }

        var editingClass = 'currentlyEditing';
        // calls the function for ever instance of the editButton
        [].forEach.call(shadowRoot.querySelectorAll('.editButton'), function(element) {
            var parentNode = element.parentNode;
            var content = parentNode.querySelector('content');
            var nodes = content.getDistributedNodes();
            var contentElement = nodes[0];
            var editorElement = getEditorElement(parentNode);
            var finishEditing = function() {
                $(parentNode).removeClass(editingClass);
                $(contentElement).removeClass(editingClass);
                parentNode.removeChild(editorElement);
                var oldContent = contentElement.textContent;
                contentElement.textContent = editorElement.value;
                if (editFunction) {
                    editFunction(element.dataset.type, oldContent, contentElement.textContent);
                }
            }
            $(element).click(function(event) {
                event.stopPropagation();
                if ($(parentNode).hasClass(editingClass)) {
                    finishEditing();
                } else {
                    $(parentNode).addClass(editingClass);

                    if (isUndefined(contentElement)) {
                        contentElement = document.createElement("div");
                        $(contentElement).addClass(element.dataset.type);
                        localScope.appendChild(contentElement);
                    }
                    // makes the display = none
                    $(contentElement).addClass(editingClass);
                    editorElement.value = contentElement.textContent;
                    parentNode.insertBefore(editorElement, element);
                }
                return false;
            });
        });
    };

    /**
     * Should create a special editor element based on its state.
     */
    function getEditorElement(parentNode) {
        return document.createElement('input');
    }

    /**
     * Returns true if the description has larger text size than is allowed.
     */
    this.isDescriptionOverflow = function() {
        return isOverflow;
    };

    /**
     * @Method
     * @param func
     *            A function that is called at the end of an edit.
     * @callbackParam type {string} this is the class of the item that was
     *                edited (description, name, accessDate, dueDate,
     *                closedDate)
     * @callbackParam oldValue {string} the old value.
     * @callbackParam newValue {string} the value that the element was changed
     *                to.
     */
    this.setEditCallback = function(func) {
        editFunction = func;
    };
}

SchoolItem.prototype = Object.create(HTMLElement.prototype);
