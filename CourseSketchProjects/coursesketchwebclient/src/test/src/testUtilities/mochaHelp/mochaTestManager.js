/**
 * Created by dtracers on 11/14/2015.
 */
var assert = require('assert');

var failedElement = 'span.failed';

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
            before(function(done) {
                browser.url(fileUrl).then(function () {
                    browser.waitForExist(failedElement, timeout).then(function (result) {
                        console.log('done waiting for element to exist was it found? ' + result);
                        unitTestsRan = result;
                        if (result) {
                            createTests(browser, describe, filePath, fileName);
                        }
                        done();
                    });
                });
            });

            it('unit tests should be created', function (done) {
                assert.equal(true, unitTestsRan, 'the browser was able to unit test');
                done();
            });
        });
    },
    createTests: function(browser, descrive, filePath, fileName) {
        console.log('creating tests from failures');
        return;
        browser.getHTML(failedElement, false).then(function (html) {
            console.log('the number of failed tests ' + html);
            /*
            describe('live test!', function() {
                it('should have no problem creating this live test', function() {
                    var failed = parseInt(html);
                    console.log('number of failed tests ' + failed);
                    assert.equal(0, failed);
                    if (failed > 0) {
                        browser.getHTML('span.failed', false).then(function (html) {

                        });
                    }
                });
            });
            */
        });
    }
};
