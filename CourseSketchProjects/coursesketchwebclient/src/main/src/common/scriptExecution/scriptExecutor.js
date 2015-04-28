validateFirstRun(document.currentScript);

/**
 * This function merges two existing api objects into one api object.
 *
 * @param {Object} originalApi The base api object.
 * @param {Object} objectApi The second api object, usually used for passing objects into an api.
 * @return {Object} result An api object that contains the contents of both api objects.
 */
function mergeApi(originalApi, objectApi){
    var result = {};
    for (var key in originalApi) {
        if (originalApi.hasOwnProperty(key)) {
            result[key] = originalApi[key];
        }
    }
    for (var key in objectApi) {
        if (objectApi.hasOwnProperty(key)) {
            result[key] = objectApi[key];
        }
    }
    return result;
}

/**
 * This function adds the ability to call console.log() to the api.
 *
 * @param {String} text The string that will be printed to console.
 */
function debugLog(text) {
    console.log(text);
}

/**
 * This function builds an object that holds an api for manipulating the problem panel.
 *
 * @param {Object} panel The problem panel from the student experiment.
 * @param {Object} problemInfo The bank problem info object.
 * @param {Boolean} hasSubmission Flag that shows if this problem has a submission or not.
 */
function PanelEditApi(panel, problemInfo, hasSubmission){

    /**
     * This function allows scripts to create a text area object next to the sketch surface in an experiment.
     *
     * @param {Object} textAreaObj The object that defines the text area parameters (width, height, location, className, textContent).
     */
    this.addTextArea = function(textAreaObj) {
        // Builds a text area from a passed in object.
        var textArea = document.createElement('TEXTAREA');
        // Gets the sketch surface object by finding it through the .submittable class. Works with any object with that class.
        var sketchSurface = panel.querySelector('.submittable');
        textArea.className = textAreaObj.className + ' sub-panel';
        if (textAreaObj.location === 'top' || textAreaObj.location === 'bottom') {
            // parseInt in the line below is used to strip the % from the size
            sketchSurface.style.height = 'calc(' + (parseInt(sketchSurface.style.height, 10) - textAreaObj.height - 1) + '% - 110px)';
            textArea.style.width = 'calc(100% - 110px)';
            textArea.style.height = 'calc(' + (textAreaObj.height) + '% - 110px)';

        } else {
            sketchSurface.style.width = '' + (parseInt(sketchSurface.style.width, 10) - textAreaObj.width - 1) + '%';
            textArea.style.width = '' + (textAreaObj.width - 1) + '%';
            textArea.style.height = 'calc(100% - 110px)';
        }
        textArea.value = textAreaObj.textContent;
        textArea.disabled = true;
        if (textAreaObj.location === 'top' || textAreaObj.location === 'left') {
            panel.insertBefore(textArea, sketchSurface);
        } else {
            panel.appendChild(textArea);
        }
        sketchSurface.resizeSurface();
    };

    /**
     * This function allows scripts to change the background of the sketch surface to any supported type.
     *
     * @param {String} bgClass A string containing the className that corresponds to the background type.
     */
    this.setSketchSurfaceBg = function(bgClass) {
        // Sets the className of the sketch surface and adds .sub-panel + .submittable
        panel.querySelector('.submittable').className = bgClass + ' sub-panel submittable';
    };

    /**
     * This function allows scripts to set the image in the image-background class type.
     *
     * @param {String} backgroundUrl A string containing the Url of the desired background image.
     */
    this.setSketchBgImage = function(backgroundUrl) {
        // Sets the Url of backgroundImage
        var urlString = 'url(http://' + backgroundUrl + ')';
        panel.querySelector('.submittable').style.backgroundImage = urlString;
    };

    /**
     * This function allows scripts to create an embedded-html object next to the sketch surface in an experiment.
     *
     * @param {Object} htmlObj The object that defines the embedded-html parameters (width, height, location, htmlCode).
     */
    this.addEmbeddedHtml = function(htmlObj) {
        var sketchSurface = panel.querySelector('.submittable');
        var embeddedHtml = document.createElement('embedded-html');
        embeddedHtml.className = 'sub-panel';
        embeddedHtml.style.position = 'relative';
        if (htmlObj.location === 'top' || htmlObj.location === 'bottom') {
            sketchSurface.style.height = 'calc(' + (parseInt(sketchSurface.style.height, 10) - htmlObj.height - 1) + '% - 110px)';
            embeddedHtml.style.width = 'calc(100%)';
            embeddedHtml.style.height = 'calc(' + (htmlObj.height) + '% - 110px)';
            embeddedHtml.width = 'calc(100%)';
            embeddedHtml.height = 'calc(' + (htmlObj.height) + '% - 110px)';
        } else {
            sketchSurface.style.width = '' + (parseInt(sketchSurface.style.width, 10) - htmlObj.width - 1) + '%';
            embeddedHtml.style.width = '' + (htmlObj.width - 1) + '%';
            embeddedHtml.style.height = 'calc(100% - 110px)';
            embeddedHtml.width = '' + (htmlObj.width - 1) + '%';
            embeddedHtml.height = 'calc(100% - 110px)';
        }
        if (htmlObj.location === 'top' || htmlObj.location === 'left') {
            panel.insertBefore(embeddedHtml, sketchSurface);
        } else {
            panel.appendChild(embeddedHtml);
        }
        sketchSurface.resizeSurface();
        var builtHtml = '<iframe src=\"' + htmlObj.htmlCode + '\" frameborder=\"0\" allowfullscreen=\"1\"' +
               'style= "position: inline-flex; left: 0px; top: 0px; width: 100%; height: 100%;"></iframe>';
        embeddedHtml.setHtml(builtHtml);
    };

    /**
     * This function allows scripts to load and draw a sketch onto the sketch surface
     */
    this.loadBasisSketch = function() {
        if (!hasSubmission) {
            var sketchSurface = panel.querySelector('.submittable');
            var updateList = problemInfo.getBasisSketch();
            sketchSurface.loadUpdateList(updateList.getList(), undefined);
        }
    };
}

/**
 * This var is passed to the jailed plugin to define what functions from this
 * script the plugin can call when executing 3rd party code.
 */
var api = {
    debugLog: debugLog
};

/**
 * This function parses and executes the script that is passed in.
 *
 * @param {Object} problemInfo The object containing the bank problem info.
 * @param {Node} panel The submission surface DOM node that contains the sketch surface and will be passed to PanelEditApi.
 * @param {Boolean} hasSubmission Flag that shows if this problem has a submission or not.
 * @param {Function} callback A function to call when the script is done executing to finish experiment setup.
 */
function executeScript(problemInfo, panel, hasSubmission, callback) {
    var script = problemInfo.getProblemScript();
    console.log('executing script: ' + script);
    var panelApi = new PanelEditApi(panel, problemInfo, hasSubmission);
    var totalApi = mergeApi(api, panelApi);
    console.log(totalApi);
    var scriptWorker = new jailed.DynamicPlugin(script, totalApi);
    var timer = setTimeout(function() {
        scriptWorker.disconnect();
    }, 1000); // This time value is arbitrary. It serves as a cutoff time to force script execution to stop.
    scriptWorker.whenDisconnected(function() {
        clearTimeout(timer);
        callback();
    });
}
