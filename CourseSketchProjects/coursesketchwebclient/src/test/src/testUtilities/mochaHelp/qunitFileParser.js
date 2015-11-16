var mainPath = require.main.filename;
var home = mainPath.substring(0, mainPath.indexOf('node_modules'));
var output = home + '/target/unitTests';
var fs = require('fs');

var $ = require('jquery');
var jsdom = require("jsdom");

module.exports = {
    parseFile: function(testResults, callback) {
        console.log(testResults);
        for (var index in testResults) {
            var html = testResults[index];
            jsdom.env(html,[], function(err, window) {
                var passingTests = window.$('.pass');
                console.log(passingTests);
            });
        }
    }
};
