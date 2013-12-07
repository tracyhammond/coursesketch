/**
 * Creates a database with a specific name, and has a callback after being opened.
 *
 * It will also create all the functions needed for the specific database.
 */
function protoDatabase = function(databaseName, openCallback) {
	var courseSketch = null;
	courseSketch.indexedDB = null;
	var version = 1;
	var updgradeTables = null;
	this.setUpgradeTables(tables) {
		updgradeTables = tables;
	}

	/**
	 * Returns an object that can be converted to a table.
	 *
	 * The table contains an addingFunction (other) and a generic callback.<br>
	 * example addingFunction<br>
	 * <code>
	 * function adding(store) {
	 * 		return store.put({
	 * 			"text": todoText,
	 * 			"timeStamp" : new Date().getTime()
	 * 		});
	 * }
	 * </code>
	 * @param tableName The name of the specific table to be created.
	 * @param keyValue This is the key for that specific table
	 * @param addingFunction Takes in a store and then creates and returns a request see sample above.
	 * @param callback Is called upon success of a data change (either adding or 
	 */
	this.getUpgradeTable(tableName, keyValue, addingFunction, callback) {
		return {
			name: tableName,
			key: keyValue,
			add: addingFunction,
			addCall: callback
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
				this['addTo' + localTable.name] = function() {
					var db = courseSketch.indexedDB.db;
					var trans = db.transaction([localTable.name], "readwrite");
					  var store = trans.objectStore(localTable.name);
					  var request = localTable.add(store);
					  request.onsuccess = function(e) {
						  addCall(e,"add");
					  }

					  request.onerror = function(e) {
					    console.log(e.value);
					  };
				}
			})(table);
		}
		openCallback();
	}
}