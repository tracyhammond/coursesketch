/**
 * Shape data class
 * @author hammond
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 */


 /* Ignored the following functions:
	~ public abstract SRL_Object clone();
	~ protected SRL_Object clone(SRL_Object cloned)
	~ public void addInterpretation(String interpretation, double confidence, double complexity)
	public void setId(UUID id)
	public int compareTo(SRL_Object o)
	public SRL_Interpretation getBestInterpretation()
	public SRL_Interpretation getBestInterpretation()
	public SRL_Interpretation getInterpretation(String interpretation)
	public double getInterpretationConfidence(String interpretation)
	public double getInterpretationComplexity(String interpretation)
//*/

function SRL_Object() {
	addInterpretation = function(interpretation, confidence, complexity) {
		//m_interpretations.push(new SRL_Interpretation(interpretation, confidence, complexity));
		console.log("Implement SRL_Object.addInterpretation later");
	}
};

function SRL_Shape(type) {

	//TODO - figure out how to handle function overrides by subclasses in javascript
	/*
	this.prototype.getAWT = function() {};
	this.prototype.getMinX = function() {};
	this.prototype.getMinY = function() {};
	this.prototype.getMaxX = function() {};
	this.prototype.getMaxY = function() {};
	//*/

	/**
	 * Gets a list of all of the strokes that make up this object.
	 * It searches recursively to get all of the strokes of this object.
	 * If it does not have any strokes, the list will be empty.
	 * @return
	 */
	this.getStrokes = function(){
		var completeList = new Array();
		console.log("TODO - need to implement a .getRecursiveSubObjectList()");
		/*
		for(SRL_Object o : getRecursiveSubObjectList()){
			try {
				if(this.getClass() == Class.forName("SRL_Stroke")){
					completeList.add((SRL_Stroke)o);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		//*/
		return completeList;
	}
	/**
	 * Returns the center x of a shape.
	 * @return center x of a shape
	 */
	this.getCenterX = function(){
		return (getMinX() + getMaxX())/2.0;
	}
	/**
	 * Returns the center y of a shape
	 * @return center y of a shape
	 */
	this.getCenterY = function(){
		return (this.getMinY() + this.getMaxY())/2.0;
	}
	/**
	 * Get the bounding box of the stroke
	 * This returns an awt shape. 
	 * Use getBoundingSRLRectangle to get the SRL shape
	 * @return the bounding box of the stroke
	 */
	this.getBoundingBox = function() {
		var r = new Rectangle();
		r.setRect(getMinX(), getMinY(), getWidth(), getHeight());
		return r;
	}
	/**
	 * Returns the width of the object
	 * @return the width of the object
	 */
	this.getWidth = function(){
		return getMaxX() - getMinX();
	}
	/**
	 * Returns the height of the object
	 * @return the height of the object
	 */
	this.getHeight = function(){
		return getMaxY() - getMinY();
	}
	
	/**
	 * Returns the length times the height
	 * See also getLengthOfDiagonal()
	 * return area of shape
	 */
	this.getArea = function(){
		return getHeight() * getWidth();
	}
	/**
	 * This returns the length of the diagonal of the bounding box. 
	 * This might be a better measure of perceptual size than area
	 * @return Euclidean distance of bounding box diagonal
	 */
	this.getLengthOfDiagonal = function(){
		return Math.sqrt(getHeight() * getHeight() + getWidth() * getWidth());
	}
	
	/**
	 * This function just returns the same thing as the length of the diagonal
	 * as it is a good measure of size.
	 * @return size of the object.
	 */
	this.getSize = function(){
		return getLengthOfDiagonal();
	}
	
	/**
	 * Returns the angle of the diagonal of the bounding box of the shape
	 * @return angle of the diagonal of the bounding box of the shape
	 */
	this.getBoundingBoxDiagonalAngle = function() {
		return Math.atan(getHeight()/getWidth());
	}
};

//Test Functions for SRL_Shape()
SRL_Shape.prototype = new SRL_Object();
SRL_Point.prototype = new SRL_Shape();

console.log("****************SRL_Shape()****************");
