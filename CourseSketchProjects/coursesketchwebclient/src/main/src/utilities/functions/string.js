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


if (isUndefined(replaceAll)) {
	function replaceAll(find, replace, src) {
		return src.replace(new RegExp(find, 'g'), replace);
	}
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

/**
 * Does a very simple escaping of the id for css purposes.
 * A more complicated version is found here: https://mothereff.in/css-escapes
 * @param inputId The id we want escaped.
 * @return escaped value
 *
 * Example:
 * Input: 12a2b3c
 * Output: #\31 2a2b3c
 */
if (isUndefined(cssEscapeId)) {
    function cssEscapeId(inputId) {
        var output = inputId;
        var firstChar = inputId.charAt(0);
        if (/\d/.test(firstChar)) {
            output = '\\3' + firstChar + ' ' + output.slice(1);
        }
        return '#' + output;
    }
}
