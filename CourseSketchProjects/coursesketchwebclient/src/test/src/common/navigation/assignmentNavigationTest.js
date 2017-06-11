var mainPath = require.main.filename;
var helper = require(mainPath.substring(0, mainPath.indexOf('node_modules')) + '/src/test/src/testUtilities/mochaHelp/mochaTestManager.js');

var assert = require('assert');
global['currentFile'] = __filename;

helper.run(browser, describe, __filename, 6000);
