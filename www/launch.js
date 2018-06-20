/********************************************************************************
*  launch.js                                                                    *
*  Author: Denes Solti                                                          *
********************************************************************************/
var exec = require('cordova/exec');

module.exports = {
  launch: function(opts) {
	return new Promise(function(resolve, reject){
		exec(function(data){
			if (data.isActivityDone) resolve(data);
		}, reject, 'LauncherPlugin', 'launch', [opts]);
	});   
  }
};
