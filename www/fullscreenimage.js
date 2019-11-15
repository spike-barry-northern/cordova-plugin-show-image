
var exec = require("cordova/exec");

var FullScreenImage = function () {
  this.name = "FullScreenImage";
};

/*
 * Show image from url
 *
 * Parameters:
 * url: url to show
 *
 */

FullScreenImage.prototype.showImageURL = function (url) {

  exec(null, null, "FullScreenImage", "showImageURL", [{"url":url}]);


};

module.exports = new FullScreenImage();
