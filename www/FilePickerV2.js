var exec = require('cordova/exec');

module.exports.open = function(arg0, success, error) {
    exec(success, error, 'FilePickerV2', 'open', [arg0]);
};