var rewriteRulesSnippet = require('grunt-connect-rewrite/lib/utils').rewriteRequest;
module.exports = function(grunt) {
    grunt.loadNpmTasks("grunt-jscs");
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-connect-rewrite');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-qunit');
    grunt.initConfig({
        jshint: {
            options: {
                jshintrc: 'config/jshint.conf.json',
                ignores: [ 'src/main/src/utilities/libraries/**/*.js', 'src/test/src/testUtilities/**/*.js' ],
                globals: {
                    module: true
                },
                reporter:'jslint',
                reporterOutput: 'target/jshint.xml'
            },
            files: [ 'Gruntfile.js', 'src/main/src/**/*.js', 'src/test/src/**/*.js', '!src/main/src/utilities/libraries/**/*.js',
                    '!src/test/src/testUtilities/**/*.js' ]
        },
        jscs: {
            src: '<%= jshint.files %>',
            options: {
                config: 'config/jscs.conf.json',
                reporterOutput: 'target/jscsReport.txt'
            }
        },
        connect: {
            options: {
                port: 9001,
                hostname: 'localhost',
                debug: true
            },
            rules: [
               { from: '^/src/(?!test)(.*)$', to: '/src/main/src/$1' },
               { from: '^/test(.*)$', to: '/src/test/src$1', redirect: "permanent" },
               { from: '^/other(.*)$', to: 'src/main/resources/other/$1' }
            ],
            development: {
                options: {
                    middleware: function(connect, options) {
                        var middlewares = [];

                        // RewriteRules support
                        middlewares.push(rewriteRulesSnippet);

                        if (!Array.isArray(options.base)) {
                            options.base = [ options.base ];
                        }

                        var directory = options.directory || options.base[options.base.length - 1];
                        options.base.forEach(function(base) {
                            // Serve static files.
                            middlewares.push(connect.static(base));
                        });

                        // Make directory browse-able.
                        middlewares.push(connect.directory(directory));

                        return middlewares;
                    }
                }
            }
        },
        qunit: {
            options: {
                httpBase: 'http://localhost:9001',
                timeout: 2000
            },
            all: [ 'src/test/src/**/*Test.html' ]
        },
    });

    // sets up tasks relating to starting the server
    grunt.registerTask('server', function(target) {
        grunt.task.run([
            'configureRewriteRules',
            'connect:development'
        ]);
    });

    // sets up tasks related to testing
    grunt.registerTask('test', function(target) {
        grunt.task.run([
            'server',
            'qunit'
        ]);
    });

    // sets up tasks related to checkstyle
    grunt.registerTask('checkstyle', function(target) {
        grunt.task.run([
            'jscs',
            'jshint'
        ]);
    });
    grunt.registerTask('default', [ 'checkstyle', 'test' ]);
};
