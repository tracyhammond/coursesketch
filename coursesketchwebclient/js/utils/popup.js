/**
 * Toggles the popup with the given ID (it can either appear or dissapear)
 * The popup needs to be built before this works.
 */
function togglePopup(boxId) {
	var popupBox = document.getElementById(boxId);
	if (popupBox.style.display == "block") {
		popupBox.style.display = "none";
	} else {
		popupBox.style.display = "block";
	}
}

function PopupBoxBuilder() {
	this.hasFooter = false;
	this.hasHeader = false;
	this.headerText = false;
	this.bodyHTML = false;
	this.bodySrc = false;
	this.isIframe = false;
	this.footerHTML = false;
	this.bodyHeight = false;

	this.togglePopup = togglePopup;

	/**
	 * builds the HTML for the popup.
	 * This only needs to be called once.
	 * This will not display the popup.
	 */
	this.build = function build(boxId, toggleId) {
		loader.loadFile('css/utility/popup.css', 'css');
		var html = '<div id="'+ toggleId + '" class="popupBox">';
		if (this.hasHeader) {
			html += this.getHeader();
		}

		html += this.getBody();

		if (this.hasFooter) {
			html += this.getFooter();
		}
		html += '</div>';
		document.getElementById(boxId).innerHTML = html;
	}

	this.getHeader = function getHeader() {
		return '<div class="boxheader">\n' + (this.headerText ? this.headerText:'Default Header') +
				'\n</div>';
	}

	this.getBody = function getBody() {
		if(this.isIframe) {
			return '<div class="boxbody">\n<Iframe id="edit_frame_id" src="' +
					(this.bodySrc ? this.bodySrc:'404.html') + '"' +
					' height= "' + (this.bodyHeight ? this.bodyHeight:'100%') + '"' +
					'width = "100%" ' +
					'sanbox = "allow-same-origin allow-scripts"' +
					'seamless = "seamless">' +
					'\n</Iframe>\n</div>';
		} else {
			return '<div class="boxbody">\n' + (this.bodyHTML ? this.bodyHTML:'Default Body') +
					'\n</div>';
		}
	}

	this.getFooter = function getFooter() {
		return '<div class="boxfooter">\n' + (this.footerHTML ? this.footerHTML:'Default Footer') +
				'\n</div>';
	}

	this.setFooter = function setFooter(hasFooter, footerHTML) {
		this.hasFooter = hasFooter;
		if(hasFooter) {
			this.footerHTML = footerHTML;
		}
	}

	this.setHeader = function setHeader(hasHeader, headerText) {
		this.hasHeader = hasHeader;
		if(hasHeader) {
			this.headerText = headerText;
		}
	}

	/**
	 * Sets the information for the body.
	 * If isIframe is true then bodyInformation is the src.
	 * If isIframe is false then bodyInfomration is the HTML to go in the body.
	 */
	this.setBody = function setBody(isIframe, bodyInformation) {
		this.isIframe = isIframe;
		if(isIframe) {
			this.bodySrc = bodyInformation;
		} else {
			this.bodyHTML = bodyInformation;
		}
	}
	
	this.setBodyHeight = function setBodyHeight(height) {
		this.bodyHeight = height;
	}
	
}
/*
  <div class="boxheader">

    Confirm Getting Kicked In the Teeth

  </div>

  <div class="boxbody">

    Are you sure that you want Adam to kick you in your teeth? <br /><br />

    Resize your browser window horizontally and vertically to

    see how the box has dynamic width and dynamic placement.

  </div>

  <div class="boxfooter">

    <button onmousedown="alert('Ajax request now!')">Confirm</button>

    <button onmousedown="togglePopup('popbox1')">Cancel</button>

  </div>
  */