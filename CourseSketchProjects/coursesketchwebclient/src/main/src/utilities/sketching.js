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
		return "#"
				+ ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
	}
}

if (isUndefined(convertHexToRgb)) {
	function convertHexToRgb(hex) {
		// Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
		var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
		hex = hex.replace(shorthandRegex, function(m, r, g, b) {
			return r + r + g + g + b + b;
		});

		var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
		return result ? {
			r : parseInt(result[1], 16),
			g : parseInt(result[2], 16),
			b : parseInt(result[3], 16)
		} : null;
	}
}

/**
 * *************************************************************
 * Protobuf Utility functions
 * @author gigemjt 
 * *************************************************************
 */

/**
 * Decodes the data and perserves the bytebuffer for later use
 * 
 * @param data
 *            the data to decode.
 * @param proto
 *            what you are using to decode.
 * @return decoded protobuf object.
 */
if (isUndefined(decodeProtobuf)) {
	function decodeProtobuf(data, proto) {
		try {
			data.mark();
		} catch (exception) {
		}
		var decoded = proto.decode(data);
		try {
			data.reset();
		} catch (exception) {
		}
		return decoded;
	}
}