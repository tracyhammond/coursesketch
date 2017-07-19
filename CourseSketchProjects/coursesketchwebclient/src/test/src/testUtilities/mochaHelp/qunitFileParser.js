

var jsdom = require('jsdom');
var Barrier = require('barrier');
var localHostReg = /http:\/\/localhost:\d+\//;

var exports = {};

module.exports = exports;

exports.parseFile = function(testResults, callback) {
    var testObject = [];
    var barrier = Barrier.createBarrier(testResults.length, function() {
        callback(testObject);
    });
    for (var index in testResults) {
        var html = testResults[index];
        jsdom.env(html,[], function(err, window) {
            var $ = require('jquery')(window);
            var failingTests = $('.fail');
            var html = $(failingTests).html();
            if (typeof html !== "undefined") {
                // Tests that failed
                testObject.push(createFailingTest(getModuleName($), getTestName($), getTestTime($), $));
            } else {
                // Tests that passed
                testObject.push(createPassingTest(getModuleName($), getTestName($), getTestTime($)));
            }
            barrier();
        });
    }
};

function getModuleName($) {
    return $('.module-name').html();
}

function getTestName($) {
    return $('.test-name').html();
}

function getTestTime($) {
    return $('.runtime').html();
}

function createPassingTest(moduleName, testName, runtime) {
    return {
        passing: true,
        message: moduleName + ': [' + testName + '] ran in ' + runtime,
        moduleName: moduleName,
        testName: testName,
        runtime: runtime
    }
}


function createFailingTest(moduleName, testName, runtime, $) {
    var globalTest = false;
    if (typeof moduleName == 'undefined') {
        moduleName = 'global';
    }

    if (typeof testName == 'undefined') {
        testName = 'global';
        runtime = 'N/A';
        globalTest = true;
    }
    var message = $('.test-message').first().html();
    var result = {
        passing: false,
        message: message,
        moduleName: moduleName,
        testName: testName,
        runtime: runtime,
        stackTrace: getFailedLineNumbers($)
    };
    console.log('failed test', result);
    return result;
}

/**
 * Gets the line numbers of where the tests failed.
 *
 * @param $
 * @returns {Array}
 */
function getFailedLineNumbers($) {
    var source = $('.test-source pre').html();
    if (typeof source == 'undefined') {
        return [];
    }
    var stackTrace = source.split(/\sat\s/g).map(function(string) {
        // removes "http://localhost:PORT/" and trims extra spaces
        var stringResult = string.trim();
        if (stringResult == '') {
            return;
        }
        if (stringResult.indexOf(/Error:/) >= 0) {
            return string;
        }
        var lineNumberIndex = stringResult.indexOf(' ');
        stringResult = stringResult.replace(localHostReg, '');
        var object = stringResult.substring(0, lineNumberIndex).trim();
        // gets everything after the localhost, removes the parenthesis then splits by colons
        var lineInfo = stringResult.substring(lineNumberIndex).replace(/[()]/g,'').split(':');
        return {
            object: object,
            // gets the file path and removes any ? from the url
            file: lineInfo[0].replace(/[?].*/,'').trim(),
            line: lineInfo[1],
            column: lineInfo[2]
        };
    });
    return cleanArray(stackTrace);
}

/**
 * Removes an undefined objects from the array.
 *
 * @param dirtyArray An array that may contain undefined objects.
 * @returns {Array} An array that does not contain undefined objects.
 */
function cleanArray(dirtyArray) {
    var newArray = [];
    for (var i = 0; i < dirtyArray.length; i++) {
        if (dirtyArray[i]) {
            newArray.push(dirtyArray[i]);
        }
    }
    return newArray;
}
