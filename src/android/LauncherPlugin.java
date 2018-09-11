/********************************************************************************
*  LauncherPlugin.java                                                          *
*  Author: Denes Solti                                                          *
********************************************************************************/
package org.solti.cordova.plugin.launch;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

public final class LauncherPlugin extends CordovaPlugin {
	private static final int LAUNCH_REQUEST = 0;
	private static final String 
	    PACKAGE_NAME = "packageName",
	    EXTRAS       = "extras",
		IS_LAUNCHED  = "isLaunched",
		IS_DONE      = "isActivityDone";
	
	private CallbackContext callback;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (!"launch".equals(action)) return false;

		callback = callbackContext;
        
		final JSONObject opts = args.getJSONObject(0);
		
		final String 
		    packageName = opts.getString(PACKAGE_NAME),
			extras      = opts.has(EXTRAS) ? opts.getString(EXTRAS) : null;

		launch(packageName, extras);
		return true;
	}

	private void launch(String packageName, String strExtras) {
		final LauncherPlugin self = this;
		
		cordova.getThreadPool().execute(() -> {
            final Intent launchIntent = self
                .webView
                .getContext()
                .getPackageManager()
                .getLaunchIntentForPackage(packageName);

            if (launchIntent != null) {
				//
				// Clear flags [set by getLaunchIntentForPackage()]
				//
				
				launchIntent.setFlags(0);
				
				//
				// Add extended data to the intent (if needed).
				//
				
				if (strExtras != null) { 
					try {
						final JSONObject extras = new JSONObject(strExtras);
						for (String key : (Iterable<String>)() -> extras.keys()) {
							launchIntent.putExtra(key, extras.getString(key));  // TODO: convert
						}
					}
					catch (JSONException e){
						launchIntent.putExtra(EXTRAS, strExtras);
					}
				}
				
				//
				// Try to start activity
				//		
				
				try {							
					self.cordova.startActivityForResult(self, launchIntent, LAUNCH_REQUEST);

					//
					// Activity starting successful
					//

					triggerLaunched();
					return;
				} catch (ActivityNotFoundException e) {}
            }

            //
            // Activity starting failed
            //

            self.callback.error("Activity not found for package name.");
        });
	}

	private void triggerLaunched(){
		final JSONObject json = new JSONObject();
		try {
			json.put(IS_LAUNCHED, true);
		} catch (JSONException e){}

		final PluginResult result = new PluginResult(PluginResult.Status.OK, json);
		result.setKeepCallback(true);

		callback.sendPluginResult(result);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);	
		if (requestCode != LAUNCH_REQUEST) return;
		
		if (resultCode != Activity.RESULT_OK) {
			callback.error(String.format("Activity failed (%d)", resultCode));
			return;
		}
		
		final JSONObject json = new JSONObject();
		try {
			json.put(IS_DONE, true);

			if (intent != null) {
				final Bundle extras = intent.getExtras();

				if (extras != null) {
					final JSONObject jsonExtras = new JSONObject();

					for (String key : extras.keySet()) {
						jsonExtras.put(key, JSONObject.wrap(extras.get(key)));
					}

					json.put(EXTRAS, jsonExtras);
				}
			}
		} catch (JSONException e){}

		callback.success(json);
	}
}