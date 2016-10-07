var exec = require('cordova/exec');

exports.coolMethod = function(arg0, success, error) {
    exec(success, error, "PPCInAppBrowser", "coolMethod", [arg0]);
};
exports.openBrowser = function(arg0, success, error) {
    exec(success, error, "PPCInAppBrowser", "openBrowser", [arg0]);
};