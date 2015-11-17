/**
 * Created by dtracers on 11/14/2015.
 */
var assert = require('assert');
var qunitFileParser = require('./qunitFileParser');
var fs = require('fs');

var mainPath = require.main.filename;
var home = mainPath.substring(0, mainPath.indexOf('node_modules'));
var output = home + '/target/unitTests';

var failedElement = 'span.failed';
var totalTestCases = 'span.total';
var testResults = '#qunit-tests li';
var codeCoverage = '#blanket-main';

module.exports = {

    /**
     * Runs the generic unit test for the given file.
     *
     * @param browser The browser the html tests will run in
     * @param describe The mocha instance
     * @param filePath The specific file that is being tested
     * @param timeout The amount of time to wait for the html tests to finish.
     */
    run: function(browser, describe, filePath, timeout) {
        var filePath = filePath.replace(/[\\\/]/g, '/');
        var fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        var testPath = 'src/test/src';
        var fileUrl = '/test' + filePath.substring(filePath.indexOf(testPath) + testPath.length);
        fileUrl = fileUrl.replace('.js', '.html?coverage=true');
        console.log('creating tests for this url!' + fileUrl);

        var createTests = this.createTests.bind(this);

        describe(fileName, function (done) {
            var unitTestsRan = false;

            it('running test for ' + fileUrl, function (done) {
                browser.url(fileUrl).then(function () {
                    browser.waitForExist(failedElement, timeout).then(function (result) {
                        console.log('done waiting for element to exist was it found? ' + result);
                        unitTestsRan = result;
                        if (result) {
                            createTests(browser, describe, filePath, fileName, done);
                        }
                        assert.equal(true, unitTestsRan, 'the browser was able to unit test');
                    });
                });
            });
        });
    },
    createTests: function(browser, descrive, filePath, fileName, done) {
        console.log('creating tests from failures');
        browser.getHTML(failedElement, false).then(function (html) {
            // console.log('the number of failed tests ' + html);
            browser.getHTML(codeCoverage).then(function(codeCoverage) {
                console.log('getting test results');
                browser.getHTML(testResults).then(function (results) {
                    // console.log('the test results', results);
                    qunitFileParser.parseFile(results, function(resultList) {
                        console.log(resultList);
                        for (index in resultList) {
                            var testData = resultList;
                            assert.ok(testData.passing, testData);
                        }
                        done();
                    });
                });
            });
        });
    }
};
