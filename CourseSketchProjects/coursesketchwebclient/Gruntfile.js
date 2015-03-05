module.exports = function(grunt) {
    grunt.loadNpmTasks("grunt-jscs");
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-connect-rewrite');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-qunit');
    grunt.initConfig({
        jshint: {
            options: {
                jshintrc: 'config/jshint.conf.js',
                ignores : ['src/main/src/utilities/libraries/**/*.js', 'src/test/src/testUtilities/**/*.js'],
                globals: {
                    module: true
                },
                reporter:'jslint',
                reporterOutput: 'target/jshint.xml'
            },
            files: ['Gruntfile.js', 'src/main/src/**/*.js','src/test/src/**/*.js']
        },
        jscs: {
            src: "src/main/src/**/*.js",
            options: {
                config: "config/jscs.conf.js",
            }
        },
        connect: {
            options: {
                port: 9001,
                hostname: 'localhost',
                debug: true
            },
            rules: [
                // Internal rewrite
                {from: '^/src/(?!test)(.*)$', to: '/src/main/src/$1'},
                // Internal rewrite
                {from: '^/test/(.*)$', to: 'src/test/src/$1'},
                // Internal rewrite
                {from: '^/other(.*)$', to: 'src/main/resources/other/$1'}
            ],
        },
        qunit: {
            options: {
                httpBase: 'http://localhost:9001',
                urls: [
                  'http://localhost:9001/test/instructor/homePage/courseManagementTest.html',
                  'http://localhost:9001/test/instructor/homePage/NOURL.html',
                ]
            },
            all: ['src/test/src/**/*.html']
        },
    });

    // sets up tasks relating to starting the server
    grunt.registerTask('server', function (target) {
        grunt.task.run([
            'configureRewriteRules',
            'connect'
        ]);
    });

    // sets up tasks related to testing
    grunt.registerTask('test', function (target) {
            grunt.task.run([
                'server',
                'qunit'
            ]);
        });
    grunt.registerTask('default', ['test', 'jshint']);
};
