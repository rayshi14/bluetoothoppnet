package ch.fshi.btoppnet.bluetooth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import ch.fshi.btoppnet.util.Constants;
import ch.fshi.btoppnet.util.SharedPreferencesUtil;

public class ScanningAlarm extends BroadcastReceiver {

	// Class constants
	static final String TAG = "ScanningAlarm"; /** for logging */   

	private static WakeLock wakeLock; 
	private static final String WAKE_LOCK = "ScanningAlarmWakeLock";

	private static PendingIntent alarmIntent;

	private static long interval;

	/**
	 * This constructor is called the alarm manager.
	 */
	public ScanningAlarm(){}

	/**
	 * Starts the alarm.
	 */

	public ScanningAlarm(Context context) {          

		if(BluetoothAdapter.getDefaultAdapter().isEnabled()){                           
			scheduleScanning(context, System.currentTimeMillis());
		}
	}

	/**
	 * Acquire the Wake Lock
	 * @param context
	 */
	public static void getWakeLock(Context context){

		releaseWakeLock();

		PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , WAKE_LOCK); 
		wakeLock.acquire();
	}

	public static void releaseWakeLock(){
		if(wakeLock != null)
			if(wakeLock.isHeld())
				wakeLock.release();
	}



	/**
	 * Stop the scheduled alarm
	 * @param context
	 */
	public static void stopScanning(Context context) {
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		if(alarmMgr != null){
			Log.d(TAG, "alarm cancelled");
			Intent intent = new Intent(context, ScanningAlarm.class);
			alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmMgr.cancel(alarmIntent);
		}
		releaseWakeLock();
	}

	/**
	 * Schedules a Scanning communication
	 * @param time after how many milliseconds (0 for immediately)?
	 */
	public void scheduleScanning(Context context, long time) {

		Log.d(TAG, "scheduling a new Bluetooth scanning in " + Long.toString( time ));
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, ScanningAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if(alarmMgr != null){
			Log.d(TAG, "alarm cancelled");
			alarmMgr.cancel(alarmIntent);
		}
		interval = SharedPreferencesUtil.loadSavedPreferences(context, Constants.SP_RADIO_SCAN_INTERVAL, Constants.DEFAULT_SCAN_INTERVAL);
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time, interval, alarmIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// start scan
		Log.d(TAG, "start a scan at " + String.valueOf(System.currentTimeMillis()));
		if(BluetoothComm.getObject() != null){
			BluetoothComm.getObject().startScanService(true);
		}
		long newInterval = SharedPreferencesUtil.loadSavedPreferences(context, Constants.SP_RADIO_SCAN_INTERVAL, Constants.DEFAULT_SCAN_INTERVAL);
		if(interval != newInterval){
			// schedule a new scan
			Log.d(TAG, "schedule a new scan");
			scheduleScanning(context, System.currentTimeMillis() + newInterval);
		}
	}
}
