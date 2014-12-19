function Playback(updateList, updateManager, graphics) {
    var currentIndex = 0;
    this.addUpdate = function addUpdate(update, redraw, updateIndex) {
        if (redraw) {
            graphics.getPaper().view.update();
        }
        this.playNext();
    };

    this.playNext() {
        updateManager.addUpdate(updateList[currentIndex]);
        currentIndex++;
    }
}
