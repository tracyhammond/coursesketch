module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-qunit');
    grunt.initConfig({
        jshint: {
            options: {
                jshintrc: 'config/jshint.conf.js',
                ignores : ['src/main/src/utilities/libraries/**/*.js', 'src/test/src/testUtilities/**/*.js'],
                globals: {
                  module: true
                }
            },
            //files: ['Gruntfile.js', 'src/main/src/**/*.js','src/test/src/**/*.js'],
            files: ['Gruntfile.js']
        },
        qunit: {
            files: ['src/test/src/**/*.html']
        }
    });
    grunt.registerTask('default', ['jshint', 'qunit']);
};
