/**
 * Creates a database with a specific name, and has a callback after being opened.
 *
 * It will also create all the functions needed for the specific database.
 */
function protoDatabase(databaseName, version, openCallback) {
	var databaseSupported = true;
	if (!window.indexedDB) {
		databaseSupported = false;
		console.log("Your browser doesn't support a stable version of IndexedDB. So storing your data will not be possible");
		//window.alert("Your browser doesn't support a stable version of IndexedDB. So storing your data will not be possible");
	}
	var self = this;
	var courseSketch = {};
	if (databaseSupported) {
		courseSketch.indexedDB = window.indexedDB;
	} else {
		courseSketch.indexedDB = undefined;
	}

	var updgradeTables = null;
	this.setTables = function(tables) {
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
	this.createTable = function(tableName, keyValue, addingFunction) {
		return {
			name: tableName,
			key: keyValue,
			add: addingFunction
		};
	}

	this.open = function() {
		try {
			// lets do browser checking for compatability.
			var request = indexedDB.open(databaseName, version);
	
			// We can only create Object stores in a versionchange transaction.
			request.onupgradeneeded = function(e) {
				var db = e.target.result;
				// A versionchange transaction is started automatically.
				e.target.transaction.onerror = courseSketch.indexedDB.onerror;
				for (var i = 0; i < updgradeTables.length; i++) {
					table = updgradeTables[i];
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
		} catch(exception) {
			// if there is an exception then we should continue
			courseSketch.indexedDB = null;
			createTableFunctions();
		}
	}

	/**
	 * creates a bunch of functions for the table which are created upon successful database creation.
	 */
	function createTableFunctions() {
		for (var i = 0; i < updgradeTables.length; i++) {
			table = updgradeTables[i];
			(function(localTable) {
				/**
				 * Creates a function for adding items to the database.
				 */
				self['putIn' + localTable.name] = function(objectId, objectToAdd, callback) {
					if (!databaseSupported || !courseSketch.indexedDB) {
						return; // fail silently
					}

					var db = courseSketch.indexedDB.db;
					var trans = db.transaction([localTable.name], "readwrite");
					var store = trans.objectStore(localTable.name);
					var request = localTable.add(store, objectId, objectToAdd);
					request.onsuccess = function(e) {
						// add objectId to some sort of id list so we know what data is contained
						// this data is only to be used for the local deletion of all items in the database
						if (callback)
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
					if (!databaseSupported || !courseSketch.indexedDB) {
						return; // fail silently
					}

					var db = courseSketch.indexedDB.db;
					var trans = db.transaction([localTable.name], "readwrite");
					var store = trans.objectStore(localTable.name);
					var request = store.delete(objectId);
					request.onsuccess = function(e) {
						if (callback)
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
					if (!databaseSupported || !courseSketch.indexedDB) {
						// return undefined
						callback(undefined, undefined, undefined);
						return;
					}

					var db = courseSketch.indexedDB.db;
					var trans = db.transaction([localTable.name]);
					var store = trans.objectStore(localTable.name);
					var request = store.get(objectId);
					request.onsuccess = function(e) {
						if (callback)
							callback(e, request, request.result);
					}

					request.onerror = function(e) {
						console.log(e.value);
					};
				};
			})(table);
		}
		if (openCallback)
			openCallback();
	}

	this.emptySelf = function() {
		emptyDB(databaseName);
	}

	function emptyDB(databaseName) {
		try {
			var result = confirm("Do you want to empty all of the local data?");
			if(result == true) {
				var dbreq = courseSketch.indexedDB.deleteDatabase(databaseName);
				dbreq.onsuccess = function (event) {
					var db = event.result;
					output_trace("indexedDB: " + databaseName + " deleted");
				}
				dbreq.onerror = function (event) {
					output_trace("indexedDB.delete Error: " + event.message);
				}
			} else {
				alert("The local data was not emptied");
			}
		}
		catch (e) {
			output_trace("Error: " + e.message);
		}
	}
}