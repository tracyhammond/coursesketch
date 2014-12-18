function Graphics(canvasElement, sketch) {
    var ps = undefined;

    ps = new paper.PaperScope(canvasElement);
    ps.setup(canvasElement);
    ps.view.viewSize = [300, 200];

    ps.view.onFrame = function(event) {
        ps.view.update();
    };

    var queue = new Path.Circle({center: [50, 50], radius: [20, 20], strokeColor: 'black'});
}
