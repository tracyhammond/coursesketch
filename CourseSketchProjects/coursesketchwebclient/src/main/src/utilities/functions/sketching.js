/* depends on base.js */
// jshint undef:false
// jshint latedef:false

// jshint bitwise:false
/*******************************************************************************
 *
 * Color Functions
 *
 * @author gigemjt
 *
 * ******************************
 */
if (isUndefined(convertRGBtoHex)) {
    /**
     * Converts RGB to a hex value.
     *
     * @returns {String} A hexcode string representing the color.
     */
    function convertRGBtoHex(a, r, g, b) {
        return '#' + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
    }
}

if (isUndefined(convertHexToRgb)) {
    /**
     * Given a hex value convert it to rgb values.
     *
     * @param {String} hex - A hexcode string representing the color.
     * @returns {{r: (*|Number), g: (*|Number), b: (*|Number)}} An object that contains rgb values.
     */
    function convertHexToRgb(hex) {
        var localHex = hex;
        // Expand shorthand form (e.g. '03F') to full form (e.g. '0033FF')
        var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
        localHex = localHex.replace(shorthandRegex, function(m, r, g, b) {
            return r + r + g + g + b + b;
        });

        var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(localHex);
        return result ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16)
        } : undefined;
    }
}
// jshint bitwise:true
