var assert = require('assert');

describe('dataManagersTests', function() {
    before(function(done) {
        browser.url('/test/utilities/persistantData/specificManager/assignmentDataManagerTest.html', function() {
            client.saveScreenshot('screenshotoftest.pnd').then(done);
        });
    });

    it('tests a feature', function(done) {
        browser
            .getTitle(function(err, title) {
                assert.equal(title, 'Example');
            })
            .call(done);
    });
});
