/**
*Identifies a specific sketch component and prompts the user for action via mouse click (brief; singular)
*/

/**
*Ideation: 
*	Utilize highlighted callback in order to pinpoint/identify sketch component warranting action
*	allow multiple options for user interface:
*		copy
*		move
*		erase
*		remove(?)
*/

/**
*STILL IN DEVELOPMENT!!!
*/

function Onclick(externalInputListener, externalSketchContainer, highlightedCallback, graphics) {
	var inputListener = externalInputListener;
	var sketchContainer = externalSketchContainer;
	var currentPoint;
	var pastPoint;
	var currentStroke;
	var graphics = graphics;
	var pastObject = false;
