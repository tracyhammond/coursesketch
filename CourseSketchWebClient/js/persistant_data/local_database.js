
/**
 * Stores data to the given locations
 * long_storage does not expire.
 * cookie_storage is for cookie related data
 * short_storage will only last one session
 */
function database(long_storage, cookie_storage, short_storage) {
	this.long_storage = long_storage;
	this.cookie_storage = cookie_storage;
	this.short_storage = short_storage;
}