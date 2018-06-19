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
	
	private CallbackContext callback;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (!"launch".equals(action)) return false;

		callback = callbackContext;

		final String packageName = args
			.getJSONObject(0)
			.getString("packageName");

		launch(packageName);
		return true;
	}

	private void launch(String packageName) {
		final LauncherPlugin self = this;
		
		cordova.getThreadPool().execute(() -> {
            final Intent launchIntent = self
                .webView
                .getContext()
                .getPackageManager()
                .getLaunchIntentForPackage(packageName);

            if (launchIntent != null)
            try {
                launchIntent.setFlags(0); // getLaunchIntentForPackage() beallitasok torlese
                self.cordova.startActivityForResult(self, launchIntent, LAUNCH_REQUEST);

                //
                // App inditas sikeres.
                //

				triggerLaunched();
                return;
            } catch (ActivityNotFoundException e) {}

            //
            // App inditas sikertelen.
            //

            self.callback.error("Activity not found for package name.");
        });
	}

	private void triggerLaunched(){
		final JSONObject json = new JSONObject();
		try {
			json.put("isLaunched", true);
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
			json.put("isActivityDone", true);

			if (intent != null) {
				final Bundle extras = intent.getExtras();

				if (extras != null) {
					final JSONObject jsonExtras = new JSONObject();

					for (String key : extras.keySet()) {
						jsonExtras.put(key, JSONObject.wrap(extras.get(key)));
					}

					json.put("extras", jsonExtras);
				}
			}
		} catch (JSONException e){}

		callback.success(json);
	}
}