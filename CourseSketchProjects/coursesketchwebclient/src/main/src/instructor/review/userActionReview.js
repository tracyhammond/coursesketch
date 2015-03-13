/**
 *
 * The userActionReview is a custom element that can analyze a user's actions on a problem
 *
 * @Class
 */
function UserActionGraph() {

    /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        var localScope = this; // This sets the variable to the level of the custom element tag
        var shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        this.loadData(this.loadedData); // Loads data if data exists. This should allow for editing of the element after it is created and saved.
    };

    /**
     * @param textBoxProto {protoCommand} is the data to be loaded from the proto
     * If shadowRoot does not exist, saves the protoCommand locally and returns so the element can be initialized
     * If the protoCommand does not exist, returns because data cannot be loaded
     */
    this.loadData = function(importData) {
        if (isUndefined(this.shadowRoot) || this.shadowRoot == null) {
            this.loadedData = importData;
            return;
        }
        if (isUndefined(importData)) {
            return;
        }
        var graph = this.shadowRoot.querySelector('#userActionGraph');

        // If creatorText element does not exist, make the selected node the viewText element
/*
        $(dialog).height(textBoxProto.getHeight()); // Sets dialog height
        $(dialog).width(textBoxProto.getWidth()); // Sets dialog width
        $(node).width(textBoxProto.getWidth()); // Sets node width
        $(node).height(textBoxProto.getHeight() - 16); // Sets node height minus 16px to account for default padding
        $(dialog).css({ top: textBoxProto.getY(), left: textBoxProto.getX() }); // Sets dialog x and y positions
        node.textContent = textBoxProto.getText(); // Sets selected node (creatorText or viewTexet) text value
*/
        var margin = {top: 30, right: 20, bottom: 30, left: 20},
            width = 960 - margin.left - margin.right,
            barHeight = 20,
            barWidth = width * .8;

        var i = 0,
            duration = 400,
            root;

        var tree = d3.layout.tree()
            .nodeSize([0, 20]);

        var diagonal =function(d, i) {
            return "M" + d.source.y + "," + d.source.x
                + "V" + d.target.x + "H" + d.target.y;
        }

        var svg = d3.select(graph).append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", "100%")
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");


        function Node(data){
            this.name = data;
            this.parent = this;
            this.children = [];
        }

        Node.prototype.AddChild=function(child){
            child.parent = this;
            this.children.push(child);
        }

        var updateData = importData;
        //start putting data into tree structure
        var base = new Node("User Action History");
        var currentParent = base;
        var markerObject;
        for(var u = 0; u < updateData.list.length; u++){
            //checks to see if current command corresponds to previous lowest marker
            //CourseSketch.PROTOBUF_UTIL.decodeProtobuf(updateData.list[u].time, CourseSketch.PROTOBUF_UTIL.getUpdateClass());
            if(updateData.list[u].commands[0].getCommandTypeName()=="MARKER") {
                markerObject = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(updateData.list[u].commands[0].commandData, CourseSketch.PROTOBUF_UTIL.getMarkerClass());
                if(markerObject.otherData>0){
                    currentParent=currentParent.children[currentParent.children.length-1];
                    currentParent.AddChild(new Node(updateData.list[u].commands[0].getCommandTypeName()));
                } else {
                    currentParent.AddChild(new Node(updateData.list[u].commands[0].getCommandTypeName()));
                    currentParent = currentParent.parent;
                }
            } else {
                currentParent.AddChild(new Node(updateData.list[u].commands[0].getCommandTypeName()
                    + updateData.list[u].time));
            }
        }

        d3.json(base, function(error) {
            base.x0 = 0;
            base.y0 = 0;
            update(root = base);
        });

        function update(source) {
            // Compute the flattened node list. TODO use d3.layout.hierarchy.
            var nodes = tree.nodes(root);

            var height = Math.max(500, nodes.length * barHeight + margin.top + margin.bottom);

            d3.select("svg").transition()
                .duration(duration)
                .attr("height", height);

            d3.select(self.frameElement).transition()
                .duration(duration)
                .style("height", height + "px");

            // Compute the "layout".
            nodes.forEach(function(n, i) {
                n.x = i * barHeight;
            });

            // Update the nodes…
            var node = svg.selectAll("g.node")
                .data(nodes, function(d) { return d.id || (d.id = ++i); });

            var nodeEnter = node.enter().append("g")
                .attr("class", "node")
                .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
                .style("opacity", 1e-6);

            // Enter any new nodes at the parent's previous position.
            nodeEnter.append("rect")
                .attr("y", -barHeight / 2)
                .attr("height", barHeight)
                .attr("width", barWidth)
                .style("fill", color)
                .on("click", click);

            nodeEnter.append("text")
                .attr("dy", 3.5)
                .attr("dx", 5.5)
                .text(function(d) { return d.name; });

            // Transition nodes to their new position.
            nodeEnter.transition()
                .duration(duration)
                .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
                .style("opacity", 1);

            node.transition()
                .duration(duration)
                .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
                .style("opacity", 1)
                .select("rect")
                .style("fill", color);

            // Transition exiting nodes to the parent's new position.
            node.exit().transition()
                .duration(duration)
                .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
                .style("opacity", 1e-6)
                .remove();

            // Update the links…
            var link = svg.selectAll("path.link")
                .data(tree.links(nodes), function(d) { return d.target.id; });

            // Enter any new links at the parent's previous position.
            link.enter().insert("path", "g")
                .attr("class", "link")
                .attr("d", function(d) {
                    var o = {x: source.x0, y: source.y0};
                    return diagonal({source: o, target: o});
                })
                .transition()
                    .duration(duration)
                    .attr("d", diagonal);

            // Transition links to their new position.
            link.transition()
                .duration(duration)
                .attr("d", diagonal);

            // Transition exiting nodes to the parent's new position.
            link.exit().transition()
                .duration(duration)
                .attr("d", function(d) {
                    var o = {x: source.x, y: source.y};
                    return diagonal({source: o, target: o});
                })
                .remove();

            // Stash the old positions for transition.
            nodes.forEach(function(d) {
                d.x0 = d.x;
                d.y0 = d.y;
            });
        }

        // Toggle children on click.
        function click(d) {
            if (d.children) {
                d._children = d.children;
                d.children = null;
            } else {
                d.children = d._children;
                d._children = null;
            }
            update(d);
        }

        function color(d) {
            return d._children ? "#3182bd" : d.children ? "#c6dbef" : "#fd8d3c";
        }

    };

    /**
     * @return finishedCallback {function} is the callback set at implementation.
     * The callback can be called immediately using .getFinishedCallback()(argument) with argument being optional
     */
    this.getFinishedCallback = function() {
        return this.finishedCallback;
    };
}
UserActionGraph.prototype.finishedCallback = undefined; // Defined by whoever implements this by using setFinishedListener().
UserActionGraph.prototype.createdCommand = undefined;
UserActionGraph.prototype = Object.create(HTMLDialogElement.prototype);
