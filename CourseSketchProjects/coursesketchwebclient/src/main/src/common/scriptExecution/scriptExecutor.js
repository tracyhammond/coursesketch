validateFirstRun(document.currentScript);

/**
 *This function adds the ability to call console.log() to the api
 * @param text
 *        {string}
 */

function debugLog(text) {
    console.log(text);
}

function panelEditor(panel){

}

/**
 * This var is passed to the jailed plugin to define what functions from this
 *  script the plugin can call when executing 3rd party code.
 */

var api = {
    debugLog: debugLog
};

/**
 * This function parses and executes the script that is passed in.
 * @param script
 *        {string}
 */

function executeScript(script, panel, callback) {
    console.log('executing script: ' + script);
    var panelApi = new panelEditor(panel);
    var totalApi = mergeApi(api, panelApi);
    console.log(totalApi);
    var scriptWorker = new jailed.DynamicPlugin(script, totalApi);
    var timer = setTimeout(function(){
        scriptWorker.disconnect()
    }, 2000)
    scriptWorker.whenDisconnected(function() {
        clearTimeout(timer);
        callback();
    });
}


function mergeApi(originalApi, objectApi){
    var result = {};
    for (var key in originalApi) {
        result[key] = originalApi[key];
    }
    for (var key in objectApi) {
        result[key] = objectApi[key];
    }
    return result;
}