/**
 * Created by David Windows on 5/13/2016.
 */
(function () {

    /**
     * A plugin used to send updates to the server.
     *
     * @class RecognitionPlugin
     */
    function RecognitionPlugin(updateManager, sketchId) {

        /**
         * The id of this plugin.
         *
         * @type {String}
         */
        var pluginId = 'Recognition Plugin';

        /**
         * @returns {String} The plugin id of this plugin.
         */
        this.getPluginId = function () {
          return pluginId;
        };

        /**
         * Holds the list of updates that are waiting to be sent to the server.
         *
         * This list should almost always be near empty.
         */
        var queuedServerUpdates = [];

        /**
         * Called when the {@link UpdateManager} adds an update.
         *
         * @param {SrlUpdate} update - The update to be sent to thee recognition server.
         * @param {Boolean} toRemote - True if this update is destined to the remote server.
         */
        this.addUpdate = function(update, redraw, updateIndex, updateType, updatePluginId) {
            console.log('adding update!');
            var cleanUpdate = CourseSketch.prutil.cleanProtobuf(update, CourseSketch.prutil.getSrlUpdateClass());
            if (updatePluginId !== pluginId) {
                console.log('submitting an update to a remote computer for recognition');
                CourseSketch.recognition.addUpdate(sketchId, cleanUpdate, function(err, msg) {
                    console.log('It worked@!!!', err, msg);
                    if ((!isUndefined(err) && err !== null) || isUndefined(msg)) {
                        console.log('problems with the response');
                        return;
                    }
                    var updateList = msg.changes;
                    var updates = updateList.list;
                    for (var i = 0; i < updates.length; i++) {
                        var recognition_update = updates[i];
                        console.log('add update', recognition_update);
                        updateManager.addUpdate(recognition_update, pluginId);
                    }
                });
            }
        };
    }

    /**
     * Creates a recognition plugin for this specific recognition manager and sketchId.
     *
     * @param {UpdateManager} updateManager The manager of the update that results are added to.
     * @param {UUID} sketchId The id of the sketch that this recognition plugin is being created for.
     * @returns {RecognitionPlugin}
     */
    CourseSketch.createRecognitionPlugin = function(updateManager, sketchId) {
        return new RecognitionPlugin(updateManager, sketchId);
    };
})();
