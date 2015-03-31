validateFirstRun(document.currentScript);

/**
 * This function parses and executes the script that is passed in.
 * @param script
 *        {string}
 */

function executeScript(script) {

    var scriptWorker = new jailed.DynamicPlugin(script, api);
}

/**
 * This var is passed to the jailed plugin to define what functions from this
 *  script the plugin can call when executing 3rd party code.
 */

var api = {
    debugLog: debugLog
}

/**
 *This function adds the ability to call console.log() to the api
 * @param text
 *        {string}
 */

function debugLog(text) {
    console.log(text);
}