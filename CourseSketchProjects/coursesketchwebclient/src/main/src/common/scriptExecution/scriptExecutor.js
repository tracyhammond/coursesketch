validateFirstRun(document.currentScript);

/**
 * This function merges two existing api objects into one api object
 * @param originalApi
 *        {Object}  The base api object
 * @param objectApi
 *        {Object}  The second api object, usually used for passing objects into an api
 * @return result
 *        {Object}   An api object that contains the contents of both api objects
 */

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

/**
 * This function adds the ability to call console.log() to the api
 * @param text
 *        {string}
 */

function debugLog(text) {
    console.log(text);
}

/**
 * This function builds an object that holds an api for manipulating the problem panel
 * @param panel
 *        {Object}  The problem panel from the student experiment
 */

function panelEditApi(panel){

    /**
     * This function allows scripts to create a text area object to the right of the sketch surface in an experiment
     * @param textAreaObj
     *        {Object} The object that defines the text area parameters (width, className, textContent)
     */

    this.addTextArea = function(textAreaObj) {
        //builds a text area from a passed in object
        var textArea = document.createElement('TEXTAREA');
        // gets the sketch surface object
        var sketchSurface = panel.getElementsByTagName('sketch-surface')[0];
        textArea.className = textAreaObj.className + ' sub-panel';
        if (textAreaObj.location === 'top' || textAreaObj.location === 'bottom') {
            sketchSurface.style.height = 'calc(' + (100 - textAreaObj.height - 1) + '% - 110px)';
            textArea.style.width = 'calc(100% - 110px)';
            textArea.style.height = 'calc(' + (textAreaObj.height) + '% - 110px)';

        }
        else {
            sketchSurface.style.width = '' + (100 - textAreaObj.width - 1) + '%';
            textArea.style.width = '' + (textAreaObj.width - 1) + '%';
            textArea.style.height = 'calc(100% - 110px)';
        }
        textArea.value = textAreaObj.textContent;
        textArea.disabled = true;
        //if (textAreaObj.location === 'top' || textAreaObj.location === 'left') {
        //    panel.insertBefore(textArea, sketchSurface);
        //}
        //else{
            panel.appendChild(textArea);
        //}
            sketchSurface.resizeSurface();
        }

    /**
     * This function allows scripts to change the background of the sketch surface to any supported type
     * @param className
     *        {string} A string containing the className that corresponds to the background type
     */

     this.setSketchSurfaceBG = function(className) {
        //sets the className of the sketch surface and adds sub-panel
        panel.childNodes[1].className = className + ' sub-panel' + ' submittable';
     }

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
    var panelApi = new panelEditApi(panel);
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