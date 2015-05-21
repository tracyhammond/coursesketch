/* depends on base.js */
// jshint undef:false
// jshint latedef:false

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
    /**
     * Replaces all strings with a different value.
     *
     * @param {String} src The string that the replace is happening in.
     * @param {RegularExpression} find the expression that is being looked for.
     * @param {String} replace what is being replaced with.
     *
     * @returns {String} A string with the replaced values.
     */
    function replaceAll(src, find, replace) {
        return src.replace(new RegExp(find, 'g'), replace);
    }
}


if (isUndefined(getTextWidth)) {
    /**
     * Uses canvas.measureText to compute and return the width of the given text of given font in pixels.
     *
     * @param {String} text
     *            The text to be rendered.
     * @param {String} font
     *            The css font descriptor that text is to be rendered with
     *            (e.g. 'bold 14px verdana').
     *
     * @see http://stackoverflow.com/questions/118241/calculate-text-width-with-javascript/21015393#21015393
     */
    function getTextWidth(text, font) {
        // re-use canvas object for better performance
        var canvas = getTextWidth.canvas || (getTextWidth.canvas = document.createElement('canvas'));
        var context = canvas.getContext('2d');
        context.font = font;
        var metrics = context.measureText(text);
        return metrics.width;
    }
}


if (isUndefined(cssEscapeId)) {
    /**
     * Does a very simple escaping of the id for css purposes.
     * A more complicated version is found here: https://mothereff.in/css-escapes
     * @param {String} inputId The id we want escaped.
     * @return {String} An escaped value is returned.
     *
     * Example:
     * Input: 12a2b3c
     * Output: #\31 2a2b3c
     */
    function cssEscapeId(inputId) {
        var output = inputId;
        var firstChar = inputId.charAt(0);
        if (/\d/.test(firstChar)) {
            output = '\\3' + firstChar + ' ' + output.slice(1);
        }
        return '#' + output;
    }
}
