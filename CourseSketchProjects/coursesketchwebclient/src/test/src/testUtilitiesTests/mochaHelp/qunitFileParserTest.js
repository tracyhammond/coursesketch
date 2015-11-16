var assert = require('assert');
var fs = require('fs');
var qunitFileParser = require('./../../testUtilities/mochaHelp/qunitFileParser');


describe('Tests qunitFileParser', function() {
    var testList = [];
    before(function(done) {
        fs.readFile('./src/test/src/testUtilitiesTests/mochaHelp/qUnitHtml', 'utf8', function(err, data) {
            if (err) throw err;
            console.log(data);
            // we split by any data that contains exactly two new lines
            testList = data.split(/(?:\r?\n){2}/).map(function(string) {
                // capturing group is currently ignored in node for split kinda
                // so this replaces the values it inserts with empty string
                return string.replace(/(\r?\n)/g, '');
            });
            done();
        });
    });
    it('qunitFileParser Parses correctly', function(done) {
        qunitFileParser.parseFile(testList, function(resultList) {
            console.log(resultList);
            done();
        });
    });
});
