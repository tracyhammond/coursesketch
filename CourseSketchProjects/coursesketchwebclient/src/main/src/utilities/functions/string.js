/* depends on base.js */

/**
 **************************************************************
 *
 * String Functions
 * @author gigemjt
 * 
 **************************************************************
 */

/**
 * Replaces all instances of {@code find} with {@code replace}.
 */
if (isUndefined(String.prototype.replaceAll)) {
	String.prototype.replaceAll = function(find, replace) {
		return this.replace(new RegExp(find, 'g'), replace);
	};
}

if (isUndefined(replaceAll)) {
	function replaceAll(find, replace, src) {
		return src.replace(new RegExp(find, 'g'), replace);
	}
	;
}

/**
 * Replaces the character or a string at the specific index
 */
if (isUndefined(String.prototype.replaceAt)) {
	String.prototype.replaceAt = function(index, string) {
		return this.substr(0, index) + character
				+ this.substr(index + character.length);
	};
}