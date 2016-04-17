var selenium = require('selenium-standalone');
var hooker = require('hooker');

module.exports = function(grunt) {

    /**
     * Used to censor circular references.
     *
     * @param circularObject
     * @returns {Function}
     */
    function filter(circularObject) {
        var i = 0;

        return function(key, value) {
            if(i !== 0 && typeof(circularObject) === 'object' && typeof(value) == 'object' && circularObject == value)
                return '[Circular]';

            if(i >= 29) // seems to be a harded maximum of 30 serialized objects?
                return '[Unknown]';

            ++i; // so we know we aren't using the original object anymore

            return value;
        };
    }

    /**
     * Performs a json stringify but removes circular references.
     *
     * @param item
     * @param censor
     * @param space
     */
    function stringify(item, censor, space) {
        return JSON.stringify(item, censor ? censor : filter(item), space);
    }

    hooker.hook(grunt.fail, function() {
        // Clean up selenium if we left it running after a failure.
        grunt.log.writeln('Attempting to clean up running selenium server. as the grun process has failed');
        var seleniumChildProcesses = global['seleniumChildProcesses'];
        if (seleniumChildProcesses && seleniumChildProcesses.kill) {
            grunt.log.writeln('Child is killable');
            seleniumChildProcesses.kill();
            grunt.log.writeln('Child is dead');
        }
        grunt.log.writeln('Server Has been cleaned');
    });

    grunt.registerMultiTask('seleniumKill', 'kills selenium', function() {
        grunt.log.writeln('killing the child using task');
        var seleniumChildProcesses = global['seleniumChildProcesses'];
        if (seleniumChildProcesses && seleniumChildProcesses.kill) {
            grunt.log.writeln('Child is killable');
            seleniumChildProcesses.kill();
            grunt.log.writeln('Child is dead');
        }
    });

    grunt.registerMultiTask('seleniumStandalone', 'Run and installs selenium', function() {
        grunt.log.write('Installing chrome driver\n');
        var done = this.async();

        selenium.install({
            // check for more recent versions of selenium here:
            // http://selenium-release.storage.googleapis.com/index.html
            version: '2.53.0',
            baseURL: 'http://selenium-release.storage.googleapis.com',
            drivers: {
                chrome: {
                    // check for more recent versions of chrome driver here:
                    // http://chromedriver.storage.googleapis.com/index.html
                    version: '2.21',
                    arch: process.arch,
                    baseURL: 'http://chromedriver.storage.googleapis.com'
                }
            },
            logger: function(message) {
                grunt.log.write(message + '\n');
            },
            progressCb: function(totalLength, progressLength, chunkLength) {
                grunt.log.writeln((progressLength / totalLength * 100) + '% completed');
            }
        }, function() {
            grunt.log.writeln('Running server');
            selenium.start({
                spawnOptions: {
                    stdio: 'inherit'
                },
                drivers: {
                    chrome: {
                        // check for more recent versions of chrome driver here:
                        // http://chromedriver.storage.googleapis.com/index.html
                        version: '2.21',
                        arch: process.arch,
                        baseURL: 'http://chromedriver.storage.googleapis.com'
                    }
                }
            },
            function(err, child) {
                if (err) {
                    grunt.log.writeln('error:' + err);
                    return done(err);
                }
                grunt.log.writeln('Server should hopefully be running unless there is an error');
                global['seleniumChildProcesses'] = child;
                grunt.log.writeln(stringify(child));
                done();
            });
        });
    });
};
