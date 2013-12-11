/**
 * Creates a database with a specific name, and has a callback after being opened.
 *
 * It will also create all the functions needed for the specific database.
 */
function protoDatabase = function(databaseName, version, openCallback) {
	if (!window.indexedDB) {
		return;
		window.alert("Your browser doesn't support a stable version of IndexedDB. So storing your data will not be possible");
	}
	var self = this;
	var courseSketch = null;
	courseSketch.indexedDB = null;
	var updgradeTables = null;
	this.setTables(tables) {
		updgradeTables = tables;
	}

	/**
	 * Returns an object that can be converted to a table.
	 *
	 * The table contains an addingFunction (other) and a generic callback.<br>
	 * example addingFunction<br>
	 * <code>
	 * function adding(store, todoText) {
	 * 		return store.put({
	 * 			"text": todoText,
	 * 			"timeStamp" : new Date().getTime()
	 * 		});
	 * }
	 * </code>
	 * @param tableName The name of the specific table to be created.
	 * @param keyValue This is the key for that specific table
	 * @param addingFunction Takes in a store and then creates and returns a request see sample above.
	 * @param gettingFunction
	 * @param callback Is called upon success of a data change (either adding or 
	 */
	this.createTable(tableName, keyValue, addingFunction) {
		return {
			name: tableName,
			key: keyValue,
			add: addingFunction
		};
	}

	this.open = function() {
		// lets do browser checking for compatability.
		var request = indexedDB.open(databaseName, version);

		// We can only create Object stores in a versionchange transaction.
		request.onupgradeneeded = function(e) {
			var db = e.target.result;
			// A versionchange transaction is started automatically.
			e.target.transaction.onerror = courseSketch.indexedDB.onerror;
			for (table in updgradeTables) {
				// delete existing table
				if (db.objectStoreNames.contains(table.name)) {
					db.deleteObjectStore(table.name);
				}
				var store = db.createObjectStore(table.name, {keyPath: table.key});
			}
		};
		request.onsuccess = function(e) {
			courseSketch.indexedDB.db = e.target.result;
			createTableFunctions();
		};
		request.onerror = courseSketch.indexedDB.onerror;
	}

	/**
	 * creates a bunch of functions for the table which are created upon successful database creation.
	 */
	function createTableFunctions() {
		for (table in updgradeTables) {
			(function(localTable) {
				/**
				 * Creates a function for adding items to the database.
				 */
				self['putIn' + localTable.name] = function(objectId, objectToAdd, callback) {
					var db = courseSketch.indexedDB.db;
					var trans = db.transaction([localTable.name], "readwrite");
					var store = trans.objectStore(localTable.name);
					var request = localTable.add(store, objectId, objectToAdd);
					request.onsuccess = function(e) {
						callback(e, request);
					}

					request.onerror = function(e) {
						console.log(e.value);
					};
				};

				/**
				 * Creates a function for deleting items from the database.
				 */
				self['deleteFrom' + localTable.name] = function(objectId, callback) {
					var db = courseSketch.indexedDB.db;
					var trans = db.transaction([localTable.name], "readwrite");
					var store = trans.objectStore(localTable.name);
					var request = store.delete(objectId);
					request.onsuccess = function(e) {
						callback(e, request);
					}

					request.onerror = function(e) {
						console.log(e.value);
					};
				};

				/**
				 * Creates a function for deleting items from the database.
				 */
				self['getFrom' + localTable.name] = function(objectId, callback) {
					var db = courseSketch.indexedDB.db;
					var trans = db.transaction([localTable.name]);
					var store = trans.objectStore(localTable.name);
					var request = store.get(objectId);
					request.onsuccess = function(e) {
						callback(e, request, request.result);
					}

					request.onerror = function(e) {
						console.log(e.value);
					};
				};
			})(table);
		}
		openCallback();
	}
}