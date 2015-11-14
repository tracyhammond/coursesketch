var assert = require('assert');


versionNumber = 1;

describe('dataManagersTests', function() {
    it('should not call creation or initialize till open is called', function() {
        new ProtoDatabase("id1", versionNumber, function() {
            assert.fail(true, "Result function is called");
        });
    });

    it('should call creation and initialize', function(done) {
        var database = new ProtoDatabase("id1", versionNumber, function() {
            assert.ok(true, "Result function is called");
            done();
        });
        database.open();
    });

    it('should create tables', function() {
        var database = new ProtoDatabase("id1", versionNumber, function() {
        });
        var tableName = "Table";
        var keyName = "key";
        var addFunction = function() {
        };
        var table = database.createTable(tableName, keyName, addFunction);
        assert.equal(tableName, table.name, "Table name should be equals");
        assert.equal(keyName, table.key, "Table key should be equals");
        assert.equal(addFunction, table.add, "Table add function should be equals");
    });

    it('should create and add tables', function(done) {
        var database = new ProtoDatabase("id1", versionNumber, function() {
            assert.ok(true, "Result function is called");
            QUnit.start();
        });
        var tableName = "Table";
        var keyName = "key";
        var addFunction = function() {
        };
        var tables = [];
        tables.push(database.createTable(tableName, keyName, addFunction));
        database.setTables(tables);
        database.open();
        assert.notEqual(database.putInTable, undefined, "table should exist");
    });
});


QUnit.asyncTest("creation and addition of tables", function(assert) {
    expect(2);
    var database = new ProtoDatabase("id1", versionNumber, function() {
        assert.ok(true, "Result function is called");
        QUnit.start();
    });
    var tableName = "Table";
    var keyName = "key";
    var addFunction = function() {
    };
    var tables = [];
    tables.push(database.createTable(tableName, keyName, addFunction));
    database.setTables(tables);
    database.open();
    assert.notEqual(database.putInTable, undefined, "table should exist");
});

QUnit.module("exception handling", {
    setup: function() {
        this.db = window.indexedDB;
    },
    teardown: function() {
        window.indexedDB = this.db;
    }
});

QUnit.asyncTest("database still creates even with a version of 0", function(assert) {
    expect(1);
    database = new ProtoDatabase("id1", 0, function() {
        assert.ok(true, "Result function is called");
        QUnit.start();
    });
    database.open();
});

QUnit.asyncTest("database still creates even if indexedDB is undefined (this test may not be valid though)", function(assert) {
    window.indexedDB = undefined;
    expect(1);
    database = new ProtoDatabase("id1", 1, function() {
        assert.ok(true, "Result function is called");
        QUnit.start();
    });
    database.open();
});
