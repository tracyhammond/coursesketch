/**
 * Created by dtracers on 11/14/2015.
 */
var assert = require('assert');

module.exports = {
    failedElement: 'span.failed',
    run: function(browser, describe, filePath, timeout) {
        var filePath = filePath.replace(/[\\\/]/g, '/');
        var fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        var testPath = 'src/test/src';
        var fileUrl = '/test' + filePath.substring(filePath.indexOf(testPath) + testPath.length);
        fileUrl = fileUrl.replace('.js', '.html?coverage=true');

        var createTests = this.createTests.bind(this);

        describe(fileName, function () {
            var unitTestsRan = false;
            browser.url(fileUrl, function () {
                browser.waitForExist(failedElement, timeout).then(function (result) {
                    console.log('done waiting for element to exist was it found? ' + result);
                    unitTestsRan = result;
                    if (result) {
                        createTests(browser, describe, filePath, fileName);
                    }
                });
            });

            it('all test should pass!', function (done) {
                assert.equal(true, unitTestsRan, 'the browser was able to unit test');
            });
        });
    },
    createTests: function(browser, descrive, filePath, fileName) {
        console.log('creating tests from failures');
        browser.getHTML(failedElement, false).then(function (html) {
            var failed = parseInt(html);
            if (failed > 0) {
                browser.getHTML('span.failed', false).then(function (html) {

                }
            }
        });
    }
};
