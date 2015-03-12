/* depends on base.js */

/*******************************************************************************
 *
 * Color Functions
 *
 * @author gigemjt
 *
 * ******************************
 */
if (isUndefined(convertRGBtoHex)) {
    function convertRGBtoHex(a, r, g, b) {
        return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
    }
}

if (isUndefined(convertHexToRgb)) {
    function convertHexToRgb(hex) {
        var localHex = hex;
        // Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
        var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
        localHex = localHex.replace(shorthandRegex, function(m, r, g, b) {
            return r + r + g + g + b + b;
        });

        var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(localHex);
        return result ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16)
        } : null;
    }
}
