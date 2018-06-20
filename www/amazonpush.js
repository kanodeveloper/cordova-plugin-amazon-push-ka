var exec = require('cordova/exec');

var AmazonPushPlugin = function() {};

AmazonPushPlugin.register = function(success, error) {
    exec(success, error, "AmazonPushPlugin", "register");
};

module.exports = AmazonPushPlugin;