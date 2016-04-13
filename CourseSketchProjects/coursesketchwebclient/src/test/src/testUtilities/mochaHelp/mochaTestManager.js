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
var passedAssertions = '.result span.passed';
var totalTestCases = 'span.total';
var testResults = '#qunit-tests li';
var codeCoverage = '#blanket-main';

module.exports = {

    /**
     * Runs the generic unit test for the given file.
     *
     * @param browser {Object} The browser the html tests will run in.
     * @param describe {Object} The mocha instance.
     * @param filePath {String} The specific file that is being tested.
     * @param timeout {Long} The amount of time to wait for the html tests to finish.
     */
    run: function(browser, describe, filePath, timeout) {
        var filePath = filePath.replace(/[\\\/]/g, '/');
        var fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        var testPath = 'src/test/src';
        var postTestPath = filePath.substring(filePath.indexOf(testPath) + testPath.length);
        var fileUrl = '/test' + postTestPath;
        var outputFileName = postTestPath.substring(1).replace(/\//g, '-');
        console.log('output file name!!!', outputFileName);
        fileUrl = fileUrl.replace('.js', '.html?coverage=true');
        console.log('creating tests for this url!' + fileUrl);

        var createTests = this.createTests.bind(this);

        describe(fileName, function (done) {
            var unitTestsRan = false;

            it('running test for ' + fileUrl, function (done) {
                browser.on('error', function(e) {
                    console.log('error occured while running tests for ' + filePath, JSON.stringify(e));
                    assert.isOk(false, 'a browser error occured when running test [' + filePath + ']\n' + JSON.stringify(e));
                });

                this.timeout(1000 + timeout);
                browser.url(fileUrl).then(function () {
                    browser.waitForExist(failedElement, timeout).then(function (result) {
                        console.log('done waiting for element to exist was it found? ' + result);
                        unitTestsRan = result;
                        if (result) {
                            createTests(browser, filePath, outputFileName, done);
                        }
                        assert.equal(true, unitTestsRan, 'the browser was able to unit test');
                    }).catch(function(result) {
                        console.log('Writing timeout result!!!! ', result);
                        var writeStream = fs.createWriteStream(output + '/' + outputFileName + 'on');
                        writeStream.write('// ' + filePath);
                        writeStream.write('\n[\n');
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
    createTests: function(browser, filePath, fileName, done) {
        console.log('creating information about the test that just occurred.');

        var decodeTests = this.decodeTests.bind(this);
        browser.getHTML(failedElement, false).then(function (failedAssertions) {
            console.log('there were at least ', failedAssertions, 'failed assertions that occured');
            browser.getHTML(passedAssertions, false).then(function(passedAssertions) {
                var writeStream;
                if (passedAssertions == 0 && failedAssertions == 0) {
                    writeStream = fs.createWriteStream(output + '/' + fileName + 'on');
                    writeStream.write('// ' + filePath);
                    writeStream.write('\n[\n');
                    var noAssertionMessage = {
                        passed: false,
                        message: 'No assertions were run each test requires at least 1 assertion',
                        moduleName: 'global',
                        testName: 'global',
                        runtime: 0 + 'ms',
                        stacktrace: []
                    };
                    writeStream.write(JSON.stringify(noAssertionMessage, null, '    '));
                    writeStream.write(']');
                    writeStream.end();
                    console.log('test failed!!!');
                    done();
                } else {
                    decodeTests(browser, filePath, fileName, failedAssertions, done)
                }
            });
        });
    },

    decodeTests: function(browser, filePath, fileName, failedAssertions, done) {
        console.log('decoding tests');
        var writeStream;
        browser.getHTML(codeCoverage).then(function (codeCoverage) {
            console.log('getting test results');
            browser.getHTML(testResults).then(function (results) {
                qunitFileParser.parseFile(results, function (resultList) {
                    if (failedAssertions > 0) {
                        console.log('There were at least [', failedAssertions, '] failed assertions');
                        writeStream = fs.createWriteStream(output + '/' + fileName + 'on');
                        writeStream.write('// ' + filePath);
                        writeStream.write('\n[\n');
                    }
                    for (index in resultList) {
                        var testData = resultList[index];
                        if (!testData.passing) {
                            writeStream.write(JSON.stringify(testData, null, '    '));
                            writeStream.write(',\n');
                        }
                    }
                    if (failedAssertions > 0) {
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
    }

};
