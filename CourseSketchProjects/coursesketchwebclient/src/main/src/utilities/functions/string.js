/* depends on base.js */

/**
 * *************************************************************
 * 
 * String Functions
 * 
 * @author gigemjt
 * 
 * *************************************************************
 */

/**
 * Replaces all instances of {@code find} with {@code replace}.
 */
if (isUndefined(String.prototype.replaceAll)) {
    /*
	String.prototype.replaceAll = function(find, replace) {
		return this.replace(new RegExp(find, 'g'), replace);
	};
	*/
}

if (isUndefined(replaceAll)) {
	function replaceAll(find, replace, src) {
		return src.replace(new RegExp(find, 'g'), replace);
	}
}

/**
 * Replaces the character or a string at the specific index
 */
if (isUndefined(String.prototype.replaceAt)) {
    /*
	String.prototype.replaceAt = function(index, string) {
		return this.substr(0, index) + character
				+ this.substr(index + character.length);
	};
	*/
}

/**
 * Uses canvas.measureText to compute and return the width of the given text of
 * given font in pixels.
 * 
 * @param {String}
 *            text The text to be rendered.
 * @param {String}
 *            font The css font descriptor that text is to be rendered with
 *            (e.g. "bold 14px verdana").
 * 
 * @see http://stackoverflow.com/questions/118241/calculate-text-width-with-javascript/21015393#21015393
 */
if (isUndefined(getTextWidth)) {
    function getTextWidth(text, font) {
        // re-use canvas object for better performance
        var canvas = getTextWidth.canvas || (getTextWidth.canvas = document.createElement("canvas"));
        var context = canvas.getContext("2d");
        context.font = font;
        var metrics = context.measureText(text);
        return metrics.width;
    }
}