var rewriteRulesSnippet = require('grunt-connect-rewrite/lib/utils').rewriteRequest;
module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-jscs');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-connect-rewrite');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-qunit');
    grunt.loadNpmTasks('grunt-jsdoc');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.initConfig({
        jshint: {
            options: {
                jshintrc: 'config/.jshintrc',
                ignores: [ 'src/main/src/utilities/libraries/**/*.js', 'src/test/src/testUtilities/**/*.js' ],
                globals: {
                    module: true
                },
                reporter:'jslint',
                reporterOutput: 'target/jshint.xml'
            },
            files: [ 'Gruntfile.js', 'src/main/src/**/*.js', 'src/test/src/**/*.js', '!src/main/src/utilities/libraries/**/*.js',
                    '!src/test/src/testUtilities/**/*.js', '!src/main/src/sketching/srl/objects/**/*.js' ]
        },
        jscs: {
            src: '<%= jshint.files %>',
            options: {
                config: 'config/jscs.conf.jscsrc',
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
               { from: '^/test(.*)$', to: '/src/test/src$1', redirect: 'permanent' },
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
        jsdoc: {
            dist: {
                src: [ 'Gruntfile.js', 'src/main/src/**/*.js', 'src/test/src/**/*.js', '!src/main/src/utilities/libraries/**/*.js',
                        '!src/test/src/testUtilities/**/*.js', '!src/main/src/sketching/srl/objects/**/*.js' ],
                options: {
                    destination: 'doc'
                }
            }
        },
        copy: {
            main: {
                files: [
                    {
                        // copies the files used in production for prod use
                        expand: true,
                        src: [ 'src/**', '!src/test/**',
                            // these are ignored as they are legacy.
                            '!src/html/**', '!src/js/**' ],
                        dest: 'target/website/'
                    },
                    {
                        // copies other html files that appear in the top level directory
                        expand: true,
                        src: [ 'index.html', 'favicon.ico' ],
                        dest: 'target/website/'
                    },
                    {
                        // copies the bower components to target
                        expand: true,
                        src: 'bower_components/**',
                        dest: 'target/website/bower_components/'
                    },
                    {
                        // copies the google app engine directory file
                        expand: true,
                        src: 'app.yaml',
                        dest: 'target/website/',
                        options: {
                            process: function(content, srcpath) {
                                return content.replace('dev-coursesketch', 'prod-coursesketch');
                            }
                        }
                    },
                    {
                        // copies the rest of the google app engine files
                        expand: true,
                        src: [ 'testFiles.py', 'main.py' ],
                        dest: 'target/website/'
                    }
                ]
            }
        }
    });
    // target is a parameter to all registration functions, it is not used.

    // sets up tasks relating to starting the server
    grunt.registerTask('server', function() {
        grunt.task.run([
            'configureRewriteRules',
            'connect:development'
        ]);
    });

    // sets up tasks related to testing
    grunt.registerTask('test', function() {
        grunt.task.run([
            'server',
            'qunit'
        ]);
    });

    // sets up tasks related to checkstyle
    grunt.registerTask('checkstyle', function() {
        grunt.task.run([
            'jscs',
            'jshint'
        ]);
    });

    // sets up tasks related to building the production website
    grunt.registerTask('build', function() {
        grunt.task.run([
            'copy'
        ]);
    });

    // 'test'  wait till browsers are better supported
    grunt.registerTask('default', [ 'checkstyle', 'jsdoc', 'build' ]);
};
