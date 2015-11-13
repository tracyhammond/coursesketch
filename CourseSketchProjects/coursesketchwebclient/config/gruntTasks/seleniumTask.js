var selenium = require('selenium-standalone');

module.exports = function(grunt) {

    var seleniumChildProcesses = {};

    function kill() {
        grunt.log.write('Killing child process');
        for (target in seleniumChildProcesses)
            seleniumChildProcesses[target].kill('SIGINT');
    }

    grunt.registerMultiTask('seleniumStandalone', 'Run and installs selenium', function() {
        grunt.log.write('Installing chrome driver\n');
        var done = this.async();
        var target = this.target;
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
                seleniumChildProcesses[target] = child;
                child.stderr.on('data', function(data) {
                    grunt.log.write(data.toString());
                });
                done();
            });
        });
    });
};
