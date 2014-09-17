
box1.addEventListener('touchstart', function(e){
	var touchlist = e.touches //reference any contact made with box1
	for (var i=0; i<touchlist.length; i++){ // loop through all touch points currently in contact with surface
	//do something with each Touch object (point)
	//detects and returns number of points being made contact with
		if (i<=3){
			statusdiv.innerHTML = 'Status: touchstart<br /> Contact = ' + i + ' points'
		} else {
			statusdiv.innerHTML = 'Please remove some fingers!'
		}
			
	}

	switch(i) {
        /*	case 1:
		* 		???
		* 		break;
		*/
		case 2:
                
			break;

		case 3:
                
			break;

			
	}
		
	box1.addEventListener('touchmove', function(e){
		var touchobj = e.changedTouches[0] // reference first touch point for this event
		var dist = parseInt(touchobj.clientX) - startx
		statusdiv.innerHTML = 'Status: touchmove<br /> Horizontal distance traveled: ' + dist + 'px'
		e.preventDefault()
	}, false)
 
	box1.addEventListener('touchend', function(e){
		var touchobj = e.changedTouches[0] // reference first touch point for this event
		statusdiv.innerHTML = 'Status: touchend<br /> Resting x coordinate: ' + touchobj.clientX + 'px'
		e.preventDefault()
	}, false)
	
	
}, false)	