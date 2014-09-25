package ch.fshi.btoppnet.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class SharedPreferencesUtil {
	
	static public int loadSavedPreferences(Context context, String key, int defaultInt) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int scanDuration = sharedPreferences.getInt(key, defaultInt);
		Log.d(Constants.TAG_ACT_TEST, key + ":" + String.valueOf(scanDuration));
		return scanDuration;
	}
	
	static public void savePreferences(Context context, String key, int value) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
		Log.d(Constants.TAG_ACT_TEST, "save " + String.valueOf(value) + " to " + key);
	}

}