/********************************************************************************
*  launch.js                                                                    *
*  Author: Denes Solti                                                          *
********************************************************************************/
var exec = require('cordova/exec');

module.exports = {
  launch: function(packageName) {
	return new Promise(function(resolve, reject){
		exec(resolve, reject, 'LauncherPlugin', 'launch', [packageName]);
	});   
  }
};
