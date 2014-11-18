/**
 * Creates the toolbar of tutorial tools
 */
function Toolbar() {
    this.initializeElement = function(templateClone) {
        var localScope = this;
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);
    };
}

Toolbar.prototype = Object.create(HTMLElement.prototype);

