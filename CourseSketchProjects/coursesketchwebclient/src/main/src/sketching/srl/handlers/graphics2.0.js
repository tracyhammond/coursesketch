function Graphics(canvasElement, sketch) {
    paper.install(window);
    var ps = undefined;
    var livePath = undefined;
    var canvasElement = $(canvasElement)[0];

    /**
     * the last stroke the user drew.  Used to prevent the creation of two paths.
     */
    var lastStroke = undefined;

    ps = new paper.PaperScope(canvasElement);
    ps.setup(canvasElement);
    //ps.view.viewSize = [300, 200];

    // ghetoo rigging
    this.correctSize = function correctSize() {
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
            this.correctSize();
        }
        ps.view.update();
    }.bind(this);

    this.createNewPath = function(point) {
        livePath = new ps.Path({strokeWidth: 2, strokeCap:'round', selected:false, strokeColor: 'black'});
        livePath.add(point);
    };

    this.updatePath = function(point) {
        livePath.add(point);
    };

    this.endPath = function(point, stroke) {
        livePath.add(point);
        livePath.simplify();
        lastStroke = stroke;
    };

    var queue = new ps.Path.Circle({center: [50, 50], radius: [20, 20], strokeColor: 'black'});

    this.getPaper = function() {
        return ps;
    };

    this.loadSketch = function() {
        lastStroke = undefined;
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
        if (lastStroke == stroke) {
            return; // we do not need to double path.
        }
        path = new ps.Path({strokeWidth: 2, strokeCap:'round', selected:false, strokeColor: 'black'});
        var pointList = stroke.getPoints();
        for (var i = 0; i < pointList.length; i++) {
            path.add(new ps.Point(pointList[i].getX(), pointList[i].getY()));
        }
        path.simplify();
    }

    /**
     * Adds ability to draw the command as it is added to the update list.
     */
    this.addUpdate = function addUpdate(update, redraw, updateIndex) {
        var commandList = update.commands;
        for (var i = 0; i < commandList.length; i++) {
            var command = commandList[i];
            if (command.commandType == CourseSketch.PROTOBUF_UTIL.CommandType.ADD_STROKE) {
                var stroke = command.decodedData;
                loadStroke(stroke);
            }
        }
        if (redraw) {
            ps.view.update();
        }
    }
}
