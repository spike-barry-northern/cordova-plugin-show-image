var exec = require("cordova/exec");

var PhotoViewer = function () {
  this.name = "Photo Viewer";
};

PhotoViewer.prototype.showImageURL = function (url) {

  exec(null, null, "PhotoViewer", "showImage", [{"url":url}]);

};

module.exports = new PhotoViewer();