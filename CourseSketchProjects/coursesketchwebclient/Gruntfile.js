//jscs:disable jsDoc

var rewriteRulesSnippet = require('grunt-connect-rewrite/lib/utils').rewriteRequest;
var selenium = require('selenium-standalone');

module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-jscs');
    grunt.loadNpmTasks('grunt-regex-check');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-connect-rewrite');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-jsdoc');
    grunt.loadNpmTasks('grunt-babel');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-text-replace');
    grunt.loadNpmTasks('grunt-wiredep');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-webdriver');
    grunt.loadNpmTasks('grunt-selenium-server');
    grunt.loadNpmTasks('grunt-mkdir');
    grunt.loadTasks('config/gruntTasks/');

    var gruntOptions = {
        skipTests: (process.env.GRUNT_SKIP_TESTS || false) === 'true'
    };

    console.log(gruntOptions);

    /******************************************
     * GRUNT INIT
     ******************************************/

    grunt.initConfig({
        fileConfigOptions: {
            prodHtml: [ 'target/website/index.html', 'target/website/src/**/*.html', '!target/website/src/main/src/utilities/libraries/**/*.html' ],
            prodFiles: [ 'target/website/index.html', 'target/website/src/**/*.html', 'target/website/src/**/*.js',
                '!target/website/src/main/src/utilities/libraries/**/*.js', '!target/website/src/main/src/utilities/libraries/**/*.html' ]
        },
        /**
         * CHECKSTYLE
         */
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
            files: [ 'Gruntfile.js', 'src/main/src/**/*.js', 'src/test/src/**/*.js',
                '!src/main/src/utilities/libraries/**/*.js', '!src/test/src/**/*.js', '!src/main/src/sketching/srl/objects/**/*.js' ]
        },
        jscs: {
            src: '<%= jshint.files %>',
            options: {
                config: 'config/jscs.conf.jscsrc',
                reporterOutput: 'target/jscsReport.txt',
                maxErrors: 1000
            }
        },
        /*
         * This module is used to check the existence or the lack of existance of a pattern in the given files
         */
        'regex-check': {
            head: {
                files: {
                    src: [ 'src/main/src/**/*.html', 'src/test/src/**/*.html', '!src/main/src/utilities/libraries/**/*.html',
                        'src/main/src/utilities/libraries/**/*Include.html' ]
                },
                options: {
                    // This looks for the head tag <head>
                    pattern: /<head(\s|(.*lang=.*))*>/g,
                    failIfMissing: true
                }
            }
        },
        /**
         * JSDOC
         */
        jsdoc: {
            dist: {
                src: '<%= jshint.files %>',
                options: {
                    destination: 'doc'
                }
            }
        },
        /**
         * Directory Creation
         */
        mkdir: {
            all: {
                options: {
                    create: [ 'target/unitTest', 'target/screenshots' ]
                }
            }
        },
        /**
         * UNIT TESTS AND SERVER
         */
        connect: {
            options: {
                port: 9001,
                hostname: 'localhost',
                debug: false
            },
            rules: [
               { from: '^/src/(?!test)(.*)$', to: '/src/main/src/$1' },
               { from: '^/test(.*)$', to: '/src/test/src$1', redirect: 'permanent' },
               { from: '^/other(.*)$', to: '/src/main/resources/other/$1' },
               { from: '^/images(.*)$', to: '/src/main/resources/images/$1' },
            //   { from: '^/bower_components(.*)$', to: 'bower_components$1' }
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
        webdriver: {
            unit: {
                options: {
                    specs: [
                        'src/test/src/**/*Test.html'
                        // Test.html
                    ]
                },
                specs: [
                    'src/test/src/**/*Test.html'
                    // Test.html
                ],
                configFile: 'config/test/wdio.conf.js'
            }
        },
        'seleniumStandalone': {
            run: {

            }
        },
        'seleniumKill': {
            run: {

            }
        },
        /**
         * BUILDERS
         */
        babel: {
            options: {
                sourceMap: true
            },
            all: {
                files: [
                    {
                        expand: true,
                        src: [ 'target/website/src/main/src/**/*.js', '!target/website/src/main/src/utilities/libraries/**/*.js' ],
                        dest: '.'
                    }
                ]
            }
        },
        copy: {
            proto: {
                files: [
                    {
                        filter: 'isFile',
                        expand: true,
                        flatten: true,
                        cwd: '../ProtoFiles/src/main/',
                        src: [ 'proto/**/**.proto' ],
                        dest: 'src/main/resources/other/protobuf'
                    }
                ]
            },
            main: {
                files: [
                    {
                        // copies the website files used in production for prod use
                        expand: true,
                        src: [ 'src/**', '!src/test/**',
                            // we do not want these copied as they are legacy.
                            '!src/html/**', '!src/js/**',
                            // we do not want these copied as they are not meant for production.
                            '!src/**/debug/**' ],

                        dest: 'target/website/'
                    },
                    {
                        // copies other important files that appear in the top level directory
                        expand: true,
                        src: [ 'index.html', 'favicon.ico', 'bower.json' ],
                        dest: 'target/website/'
                    },
                    {
                        // copies the bower components to target
                        expand: true,
                        src: [ 'bower_components/**', '!bower_components/**/test/**', '!bower_components/**/tests/**',
                            '!bower_components/**/examples/**' ],
                        dest: 'target/website/'
                    },
                    {
                        // copies the google app engine directory file
                        expand: true,
                        src: 'app.yaml',
                        dest: 'target/website/'
                    },
                    {
                        // copies the rest of the google app engine files
                        expand: true,
                        src: [ 'testFiles.py', 'main.py' ],
                        dest: 'target/website/'
                    }
                ]
            },
            /**
             * copies the babel polyfill into the bower_components folder
             */
            babel: {
                files: [
                    {
                        expand: false,
                        src: [ 'node_modules/babel-core/browser-polyfill.js' ],
                        dest: 'bower_components/babel-polyfill/browser-polyfill.js',
                        filter: 'isFile'
                    },
                    {
                        expand: false,
                        src: [ 'bower_components/babel-polyfill/.bower.json' ],
                        dest: 'bower_components/babel-polyfill/bower.json',
                        filter: 'isFile'
                    }
                ]
            }
        },
        replace: {
            bowerLoad: {
                src: '<%= fileConfigOptions.prodHtml %>',
                overwrite: true,
                replacements: [
                    {
                        // looks for <head>
                        from: /(^|\s)<head>($|\s)/g,
                        to: '\n<head>\n<!-- bower:js -->\n<!-- endbower -->\n'
                    }
                ]
            },
            bowerSlash: {
                src: '<%= fileConfigOptions.prodHtml %>',
                overwrite: true,
                replacements: [
                    {
                        // looks for the bower_components url in scripts and replaces it with a /
                        from: /=['"].*bower_components/g,
                        to: '="/bower_components'
                    }
                ]
            },
            bowerRunOnce: {
                src: '<%= fileConfigOptions.prodHtml %>',
                overwrite: true,
                replacements: [
                    {
                        // <!-- bower: -->
                        from: /(([ \t]*)<!--\s*bower:*(\S*)\s*-->)/,
                        to: '<!-- bower: -->\n<script src="bower_components/validate-first-run/validateRunOnce.js"></script>'
                    }
                ]
            },
            // TODO: change this into a plugin
            runOncePlugins: {
                src: [ 'target/website/bower_components/jquery/dist/jquery.js', 'target/website/bower_components/babel-polyfill/browser-polyfill.js',
                        'target/website/bower_components/webcomponentsjs/webcomponents.js' ],
                overwrite: true,
                replacements: [
                    {
                        // looks for the very first character of the file.
                        from: /^/,
                        to: 'validateFirstRun(document.currentScript);'
                    }
                ]
            },
            appEngine: {
                src: [ 'target/website/app.yaml' ],
                overwrite: true,
                replacements: [
                    {
                        // starts with the different lettering because app engine gui cuts off some of the lettering.
                        from: 'dev-coursesketch',
                        to: 'prod-coursesketch'
                    }
                ]
            },
            isUndefined: {
                src: '<%= fileConfigOptions.prodFiles %>',
                overwrite: true,
                replacements: [
                    {
                        // looks for isUndefined(word).
                        from: /isUndefined\((\w+\b)\)/g,
                        to: '(typeof $1 === \'undefined\')'
                    },
                    {
                        from: 'function (typeof object === \'undefined\')',
                        to: 'function isUndefined(object)'
                    }
                ]
            }
        },
        /**
         * Inserts scripts loaded via bower into our website.
         */
        wiredep: {
            task: {

                // Point to the files that should be updated when you run `grunt wiredep`
                src: '<%= fileConfigOptions.prodHtml %>',

                options: {
                    // https://github.com/taptapship/wiredep#configuration
                    directory: 'target/website/bower_components',

                    html: {
                        // looks for:
                        // <!-- bower: -->
                        // <!-- endbower -->
                        block: /(([ \t]*)<!--\s*bower:*(\S*)\s*-->)(\n|\r|.)*?(<!--\s*endbower\s*-->)/gi,
                        detect: {
                            js: /<script.*src=['"]([^'"]+)/gi,
                            css: /<link.*href=['"]([^'"]+)/gi
                        },
                        replace: {
                            js: '<script src="{{filePath}}"></script>',
                            css: '<link rel="stylesheet" href="{{filePath}}" />'
                        }
                    },
                    overrides: {
                        'babel-polyfill': {
                            main: 'browser-polyfill.js'
                        }
                    }
                }
            }
        },
        /**
         * Minifies our code to make it smaller.
         */
        uglify: {
            options: {
                compress: {
                    global_defs: {
                        'DEBUG': false
                    },
                    dead_code: true
                }
            },
            main: {
                files: [
                    {
                        expand: true,
                        src: [ 'target/website/src/**/*.js', '!target/website/src/main/src/utilities/libraries/**/*.js' ],
                        dest: '.'
                    }
                ]
            }
        }

    });

    /******************************************
     * UTILITIES
     ******************************************/

    function printTaskGroup() {
        grunt.log.write('\n===========\n=========== Running task group ' + grunt.task.current.name + ' ===========\n===========\n');
    }

    /******************************************
     * TASK WORKFLOW SETUP
     ******************************************/

    // sets up tasks relating to starting the server
    grunt.registerTask('server', function() {
        printTaskGroup();
        grunt.task.run([
            'seleniumStandalone:run',
            'configureRewriteRules',
            'connect:development'
        ]);
    });

    // sets up tasks related to testing
    grunt.registerTask('test', function() {
        if (gruntOptions.skipTests) {
            grunt.log.write('\n===========\n=========== SKIPPING UNIT TESTS ===========\n===========\n');
            return;
        }
        printTaskGroup();
        grunt.task.run([
            'server',
            'webdriver:unit',
            'seleniumKill:run'
        ]);
    });

    // sets up tasks related to creating documentation
    grunt.registerTask('documentation', function() {
        printTaskGroup();
        grunt.task.run([
            'jsdoc'
        ]);
    });

    // Sets up tasks related to setting the system for the rest of the tasks.
    grunt.registerTask('setup', function() {
        printTaskGroup();
        grunt.task.run([
            'mkdir'
        ]);
    });

    // sets up tasks needed before any checking happens.  (which in this case is changing proto files)
    grunt.registerTask('install', function() {
        printTaskGroup();
        grunt.task.run([
            'copy:proto'
        ]);
    });

    // sets up tasks related to checkstyle
    grunt.registerTask('checkstyle', function() {
        printTaskGroup();
        grunt.task.run([
            'jscs',
            'jshint',
            'regex-check'
        ]);
    });

    // sets up tasks related to building the production website
    grunt.registerTask('build', function() {
        printTaskGroup();
        grunt.task.run([
            'preBuild',
            'setupProd',
            'bower',
            'polyfill',
            'obfuscate'
        ]);
    });

    // sets up tasks needed before building.
    // specifically this loads node_modules to bower components
    grunt.registerTask('preBuild', function() {
        printTaskGroup();
        grunt.task.run([
            'copy:babel'
        ]);
    });

    // Sets up tasks related to setting up the production website.
    grunt.registerTask('setupProd', function() {
        printTaskGroup();
        grunt.task.run([
            'copy:main',
            'replace:appEngine'
        ]);
    });

    // sets up tasks related to loading up bower
    grunt.registerTask('bower', function() {
        printTaskGroup();
        grunt.task.run([
            'replace:bowerLoad',
            'wiredep',
            'replace:bowerRunOnce',
            'replace:bowerSlash',
            'replace:runOncePlugins'
        ]);
    });

    // sets up tasks related to supporting older version of browsers
    grunt.registerTask('polyfill', function() {
        printTaskGroup();
        grunt.task.run([
            'replace:isUndefined'
            // babel is turned off because it is breaking things.
            //'babel'
        ]);
    });

    // sets up tasks related to minifying the code
    grunt.registerTask('obfuscate', function() {
        printTaskGroup();
        grunt.task.run([
            'uglify'
        ]);
    });

    /******************************************
     * TASK WORKFLOW RUNNING
     ******************************************/

    // 'test'  wait till browsers are better supported
    grunt.registerTask('default', [ 'install', 'checkstyle', 'documentation', 'setup', 'test', 'build' ]);
};
