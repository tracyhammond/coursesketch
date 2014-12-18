function Graphics(canvasElement, sketch) {
    paper.install(window);
    var ps = undefined;
    var livePath = undefined;
    var canvasElement = $(canvasElement)[0];

    ps = new paper.PaperScope(canvasElement);
    ps.setup(canvasElement);
    //ps.view.viewSize = [300, 200];

    // ghetoo rigging
    function correctSize() {
        console.log(canvasElement);
        var oldHeight = canvasElement.height;
        var oldWidth = canvasElement.width;
        canvasElement.height = $(canvasElement).parent().height();
        canvasElement.width = $(canvasElement).parent().width();
        if (oldHeight != canvasElement.height || oldWidth != canvasElement.width) {
                console.log(canvasElement);

            ps.view.viewSize = [canvasElement.height, canvasElement.width];
        }
    }

    ps.view.onFrame = function(event) {
        if (event.count <= 1) {
            correctSize();
        }
        ps.view.update();
    };

    this.createNewPath = function(point) {
        livePath = new ps.Path({strokeWidth: 2, strokeCap:'round', selected:false, strokeColor: 'black'});
        livePath.add(point);
    };

    this.updatePath = function(point) {
        livePath.add(point);
    };

    this.endPath = function(point) {
        livePath.add(point);
        livePath.simplify();
    };

    var queue = new ps.Path.Circle({center: [50, 50], radius: [20, 20], strokeColor: 'black'});

    this.getPaper = function() {
        return ps;
    };

    this.loadSketch = function() {
        ps.project.activeLayer.removeChildren();
        ps.view.update();
        console.log(sketch);
        var objectList = sketch.getList();
        console.log(objectList);
        for (var i = 0; i < objectList.length; i++) {
            var object = objectList[i];
            if (object instanceof SRL_Stroke) {
                loadStroke(object);
            }
        }
        ps.view.update();
    };

    function loadStroke(stroke) {
        path = new ps.Path({strokeWidth: 2, strokeCap:'round', selected:false, strokeColor: 'black'});
        var pointList = stroke.getPoints();
        for (var i = 0; i < pointList.length; i++) {
            path.add(new ps.Point(pointList[i].getX(), pointList[i].getY()));
        }
    }
}
