// Code taken from MatthewCrumley (http://stackoverflow.com/a/934925/298479)
function getBase64Image(imgTag) {
    if (imgTag.src.endsWith(".svg")) {
        console.log("ENDInG WITH SVG OH NOES!");
    }
    console.log(imgTag);
    // Create an empty canvas element
    var canvas = document.createElement("canvas");
    canvas.width = imgTag.width;
    canvas.height = imgTag.height;

    // Copy the image contents to the canvas
    var ctx = canvas.getContext("2d");
    ctx.drawImage(imgTag, 0, 0);

    // Get the data-URL formatted image
    // Firefox supports PNG and JPEG. You could check img.src to guess the
    // original format, but be aware the using "image/jpg" will re-encode the image.
    var dataURL = canvas.toDataURL("image/png");

    return dataURL;//dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
}

// Code taken from MatthewCrumley (http://stackoverflow.com/a/934925/298479)
function getBase64Svg(imgTag) {
    return dataURL;//dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
}

function setBase64Image(imgTag, data) {
    imgTag.src = /*"data:image/png;base64," + */data;
}
