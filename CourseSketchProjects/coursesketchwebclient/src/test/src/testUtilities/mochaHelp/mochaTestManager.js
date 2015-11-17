/**
 * Created by dtracers on 11/14/2015.
 */
var assert = require('chai').assert;
var qunitFileParser = require('./qunitFileParser');
var fs = require('fs');

var mainPath = require.main.filename;
var home = mainPath.substring(0, mainPath.indexOf('node_modules'));
var output = home + '/target/unitTest';

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
                this.timeout(1000 + timeout);
                browser.url(fileUrl).then(function () {
                    browser.waitForExist(failedElement, timeout).then(function (result) {
                        console.log('done waiting for element to exist was it found? ' + result);
                        unitTestsRan = result;
                        if (result) {
                            createTests(browser, describe, filePath, fileName, done);
                        }
                        assert.equal(true, unitTestsRan, 'the browser was able to unit test');
                    }).catch(function(result) {
                        console.log('Writing timeout result!!!! ', result);
                        var writeStream = fs.createWriteStream(output + '/' + fileName + 'on');
                        writeStream.write('[\n');
                        var timeoutMessage = {
                            passed: false,
                            message: 'Test timed out after ' + timeout,
                            moduleName: 'global',
                            testName: 'global',
                            runtime: timeout + 'ms',
                            stacktrace: []
                        };
                        writeStream.write(JSON.stringify(timeoutMessage, null, '    '));
                        writeStream.write(']');
                        writeStream.end();
                        assert.ok(false, '' + result);
                        done();
                    });
                });
            });
        });
    },
    createTests: function(browser, descrive, filePath, fileName, done) {
        console.log('creating tests from failures');
        browser.getHTML(failedElement, false).then(function (html) {
            // console.log('the number of failed tests ' + html);
            var writeStream;
            browser.getHTML(codeCoverage).then(function(codeCoverage) {
                console.log('getting test results');
                browser.getHTML(testResults).then(function (results) {
                    // console.log('the test results', results);
                    qunitFileParser.parseFile(results, function(resultList) {
                        if (html > 0) {
                            writeStream = fs.createWriteStream(output + '/' + fileName + 'on');
                            writeStream.write('[\n');
                        }
                        for (index in resultList) {
                            var testData = resultList[index];
                            if (!testData.passing) {
                                writeStream.write(JSON.stringify(testData, null, '    '));
                                writeStream.write(',\n');
                            }
                        }
                        if (html > 0) {
                            writeStream.write(']');
                            writeStream.end();
                        }
                        for (index in resultList) {
                            var testData = resultList[index];
                            if (!testData.passing) {
                                assert.ok(testData.passing, testData.message);
                            }
                        }
                        done();
                    });
                });
            });
        });
    }
};
