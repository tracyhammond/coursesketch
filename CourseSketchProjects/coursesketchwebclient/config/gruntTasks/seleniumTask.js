var selenium = require('selenium-standalone');

module.exports = function(grunt) {

    grunt.util.hooker.hook(grunt.fail, function() {
        // Clean up selenium if we left it running after a failure.
        grunt.log.writeln('Attempting to clean up running selenium server.');
        var seleniumChildProcesses = global['seleniumChildProcesses'];
        if (seleniumChildProcesses && seleniumChildProcesses.kill) {
            grunt.log.writeln('Child is killable');
            seleniumChildProcesses.kill();
            grunt.log.writeln('Child is dead');
        }
        grunt.log.writeln('Server Gas been cleaned');
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
            version: '2.47.1',
            baseURL: 'http://selenium-release.storage.googleapis.com',
            drivers: {
                chrome: {
                    // check for more recent versions of chrome driver here:
                    // http://chromedriver.storage.googleapis.com/index.html
                    version: '2.9',
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
            selenium.start(
                // Use as an example for the drivers.
          /*{
                spawnOptions: {
                    stdio: 'inherit'
                },
                drivers: {
                    chrome: {
                        // check for more recent versions of chrome driver here:
                        // http://chromedriver.storage.googleapis.com/index.html
                        version: '2.9',
                        arch: process.arch,
                        baseURL: 'http://chromedriver.storage.googleapis.com'
                    }
                }
            },*/
            function(err, child) {
                if (err) return done(err);
                grunt.log.writeln('Server should hopefully be running unless there is an error');
                global['seleniumChildProcesses'] = child;
                child.stderr.on('data', function(data) {
                    grunt.log.write(data.toString());
                });
                done();
            });
        });
    });
};
