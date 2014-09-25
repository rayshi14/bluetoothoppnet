package ch.fshi.btoppnet.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class BTOppNetDBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "btoppnet";

	// Database table names;
	public static final String TABLE_DEVICES = "devices";  
	public static final String TABLE_COMMUNICATION_LOGS = "logs";
	public static final String TABLE_SCAN_LOGS = "scans";

	public static final String COL_ROW_ID = BaseColumns._ID;

	private static final int DATABASE_VERSION = 16;

	// table devices
	private static final String KEY_DEVICE_NAME = "key_device_name";
	private static final String KEY_DEVICE_MAC = "key_device_mac";
	private static final String KEY_DEVICE_RSSI = "key_device_rssi";
	private static final String KEY_DEVICE_STATE = "key_device_state";
	private static final String KEY_DEVICE_COUNTER = "key_device_counter";

	// table comm logs
	private static final String KEY_LOG_TIMESTAMP = "key_log_timestamp";
	private static final String KEY_LOG_RSSI = "key_log_rssi";
	private static final String KEY_LOG_FK_DEVICE = "key_log_fk_device";
	private static final String KEY_LOG_DELAY = "key_log_delay";

	// table scan log
	private static final String KEY_SCAN_TIMESTAMP = "key_scan_timestamp";
	private static final String KEY_SCAN_RSSI = "key_scan_rssi";
	private static final String KEY_SCAN_FK_DEVICE = "key_scan_fk_device";
	private static final String KEY_SCAN_DURATION = "key_scan_duration";
	private static final String KEY_SCAN_BASELINE = "key_scan_baseline";

	public BTOppNetDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_DEVICES_TABLE = "CREATE TABLE " + TABLE_DEVICES + "("
				+ COL_ROW_ID + " INTEGER PRIMARY KEY," + KEY_DEVICE_NAME + " TEXT,"
				+ KEY_DEVICE_MAC + " TEXT," + KEY_DEVICE_RSSI + " INTEGER," + KEY_DEVICE_STATE + " INTEGER," + KEY_DEVICE_COUNTER + " INTEGER" + ")";

		String CREATE_LOGS_TABLE = "CREATE TABLE " + TABLE_COMMUNICATION_LOGS + "("
				+ COL_ROW_ID + " INTEGER PRIMARY KEY," + KEY_LOG_TIMESTAMP + " INTEGER,"
				+ KEY_LOG_RSSI + " INTEGER," + KEY_LOG_FK_DEVICE + " INTEGER," + KEY_LOG_DELAY + " INTEGER" + ")";
		String CREATE_SCANS_TABLE = "CREATE TABLE " + TABLE_SCAN_LOGS + "("
				+ COL_ROW_ID + " INTEGER PRIMARY KEY," + KEY_SCAN_TIMESTAMP + " INTEGER,"
				+ KEY_SCAN_RSSI + " INTEGER," + KEY_SCAN_FK_DEVICE + " INTEGER," + KEY_SCAN_DURATION + " INTEGER," + KEY_SCAN_BASELINE + " INTEGER" + ")";
		db.execSQL(CREATE_DEVICES_TABLE);
		db.execSQL(CREATE_LOGS_TABLE);
		db.execSQL(CREATE_SCANS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMUNICATION_LOGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCAN_LOGS);
		// Create tables again
		onCreate(db);
	}

	// Adding new device
	public void addDevice(MyBluetoothDevice device) {
		SQLiteDatabase db = this.getWritableDatabase();

		MyBluetoothDevice btDevice = getDevice(device.getMac());
		if (btDevice != null){
			updateDevice(device);
		}
		else{
			ContentValues values = new ContentValues();
			values.put(KEY_DEVICE_NAME, device.getName());
			values.put(KEY_DEVICE_MAC, device.getMac());
			values.put(KEY_DEVICE_RSSI, device.getRssi());
			values.put(KEY_DEVICE_COUNTER, device.getCounter());
			values.put(KEY_DEVICE_STATE, device.getState());
			// Inserting Row
			db.insert(TABLE_DEVICES, null, values);
		}
		db.close(); // Closing database connection
	}

	// Adding new log
	public void addLog(CommunicationLog log) {
		SQLiteDatabase db = this.getWritableDatabase();

		CommunicationLog tmpLog = getLogByTimestamp(log.getTimestamp());
		if(tmpLog!=null){
			updateLog(log);
		}else{
			ContentValues values = new ContentValues();
			values.put(KEY_LOG_TIMESTAMP, log.getTimestamp());
			values.put(KEY_LOG_RSSI, log.getRssi());
			values.put(KEY_LOG_FK_DEVICE, log.getDeviceId());
			values.put(KEY_LOG_DELAY, log.getDelay());
			// Inserting Row
			db.insert(TABLE_COMMUNICATION_LOGS, null, values);
		}
		db.close(); // Closing database connection
	}

	// Adding new log
	public void addScanLog(ScanResultLog log) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_SCAN_TIMESTAMP, log.getTimestamp());
		values.put(KEY_SCAN_RSSI, log.getRssi());
		values.put(KEY_SCAN_FK_DEVICE, log.getDeviceId());
		values.put(KEY_SCAN_DURATION, log.getScanDuration());
		values.put(KEY_SCAN_BASELINE, log.getBaselineTimestamp());
		// Inserting Row
		db.insert(TABLE_SCAN_LOGS, null, values);

		db.close(); // Closing database connection
	}

	// Getting single device
	public MyBluetoothDevice getDevice(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_DEVICES, new String[] { COL_ROW_ID,
				KEY_DEVICE_NAME, KEY_DEVICE_MAC, KEY_DEVICE_RSSI, KEY_DEVICE_COUNTER, KEY_DEVICE_STATE }, COL_ROW_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor.getCount() > 0 && cursor != null){
			cursor.moveToFirst();

			MyBluetoothDevice device = new MyBluetoothDevice(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
					cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)), cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)), 
					cursor.getShort(cursor.getColumnIndex(KEY_DEVICE_RSSI)), cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_STATE)), 
					cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_COUNTER)));
			return device;
		}
		else{
			return null;
		}
	}
	// Getting single device
	public MyBluetoothDevice getDevice(String mac) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_DEVICES, new String[] { COL_ROW_ID,
				KEY_DEVICE_NAME, KEY_DEVICE_MAC, KEY_DEVICE_RSSI, KEY_DEVICE_STATE, KEY_DEVICE_COUNTER }, KEY_DEVICE_MAC + "=?",
				new String[] { mac }, null, null, null, null);
		if (cursor.getCount() > 0 && cursor != null){
			cursor.moveToFirst();
			MyBluetoothDevice device = new MyBluetoothDevice(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
					cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)), cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)), 
					cursor.getShort(cursor.getColumnIndex(KEY_DEVICE_RSSI)), cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_STATE)), 
					cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_COUNTER)));
			return device;
		}
		else{
			return null;
		}
	}

	// Getting single log
	public CommunicationLog getLogById(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_COMMUNICATION_LOGS, new String[] { COL_ROW_ID,
				KEY_LOG_TIMESTAMP, KEY_LOG_RSSI, KEY_LOG_FK_DEVICE, KEY_LOG_DELAY }, COL_ROW_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor.getCount() > 0 && cursor != null){
			cursor.moveToFirst();

			CommunicationLog log = new CommunicationLog(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
					cursor.getLong(cursor.getColumnIndex(KEY_LOG_TIMESTAMP)), cursor.getShort(cursor.getColumnIndex(KEY_LOG_RSSI)), 
					cursor.getInt(cursor.getColumnIndex(KEY_LOG_FK_DEVICE)), cursor.getLong(cursor.getColumnIndex(KEY_LOG_DELAY)));
			// return log
			return log;
		}
		else{
			return null;
		}
	}
	// Getting single log
	public CommunicationLog getLogByTimestamp(long timestamp) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_COMMUNICATION_LOGS, new String[] { COL_ROW_ID,
				KEY_LOG_TIMESTAMP, KEY_LOG_RSSI, KEY_LOG_FK_DEVICE, KEY_LOG_DELAY }, KEY_LOG_TIMESTAMP + "=?",
				new String[] { String.valueOf(timestamp) }, null, null, null, null);
		if (cursor.getCount() > 0 && cursor != null){
			cursor.moveToFirst();

			CommunicationLog log = new CommunicationLog(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
					cursor.getLong(cursor.getColumnIndex(KEY_LOG_TIMESTAMP)), cursor.getShort(cursor.getColumnIndex(KEY_LOG_RSSI)), 
					cursor.getInt(cursor.getColumnIndex(KEY_LOG_FK_DEVICE)), cursor.getLong(cursor.getColumnIndex(KEY_LOG_DELAY)));
			// return log
			return log;
		}
		else{
			return null;
		}
	}

	// Getting single log
	public ScanResultLog getScanLogById(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_SCAN_LOGS, new String[] { COL_ROW_ID,
				KEY_SCAN_TIMESTAMP, KEY_SCAN_RSSI, KEY_SCAN_FK_DEVICE, KEY_SCAN_DURATION, KEY_SCAN_BASELINE }, COL_ROW_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor.getCount() > 0 && cursor != null){
			cursor.moveToFirst();

			ScanResultLog log = new ScanResultLog(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
					cursor.getLong(cursor.getColumnIndex(KEY_SCAN_TIMESTAMP)), cursor.getShort(cursor.getColumnIndex(KEY_SCAN_RSSI)), 
					cursor.getInt(cursor.getColumnIndex(KEY_SCAN_FK_DEVICE)), cursor.getInt(cursor.getColumnIndex(KEY_SCAN_DURATION)),
					cursor.getLong(cursor.getColumnIndex(KEY_SCAN_BASELINE)));
			// return log
			return log;
		}
		else{
			return null;
		}
	}

	// Getting All devices
	public List<MyBluetoothDevice> getAllDevices() {
		List<MyBluetoothDevice> deviceList = new ArrayList<MyBluetoothDevice>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_DEVICES;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if(cursor.getCount() > 0 && cursor != null){
			if (cursor.moveToFirst()) {
				do {
					MyBluetoothDevice device = new MyBluetoothDevice(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
							cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)), cursor.getString(cursor.getColumnIndex(KEY_DEVICE_MAC)), 
							cursor.getShort(cursor.getColumnIndex(KEY_DEVICE_RSSI)), cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_STATE)), 
							cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_COUNTER)));
					// Adding contact to list
					deviceList.add(device);
				} while (cursor.moveToNext());
			}
		}

		// return device list
		return deviceList;
	}
	// Getting All logs
	public List<CommunicationLog> getAllLogs() {
		List<CommunicationLog> logList = new ArrayList<CommunicationLog>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_COMMUNICATION_LOGS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if(cursor.getCount() > 0 && cursor != null){
			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					CommunicationLog log = new CommunicationLog(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
							cursor.getLong(cursor.getColumnIndex(KEY_LOG_TIMESTAMP)), cursor.getShort(cursor.getColumnIndex(KEY_LOG_RSSI)), 
							cursor.getInt(cursor.getColumnIndex(KEY_LOG_FK_DEVICE)), cursor.getLong(cursor.getColumnIndex(KEY_LOG_DELAY)));
					// Adding contact to list
					logList.add(log);
				} while (cursor.moveToNext());
			}
		}

		// return log list
		return logList;
	}
	// Getting All logs
	public List<CommunicationLog> getAllLogsByDeviceId(long id) {
		List<CommunicationLog> logList = new ArrayList<CommunicationLog>();

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query(TABLE_COMMUNICATION_LOGS, new String[] { COL_ROW_ID,
				KEY_LOG_TIMESTAMP, KEY_LOG_RSSI, KEY_LOG_FK_DEVICE, KEY_LOG_DELAY }, KEY_LOG_FK_DEVICE + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if(cursor.getCount() > 0 && cursor != null){
			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					CommunicationLog log = new CommunicationLog(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
							cursor.getLong(cursor.getColumnIndex(KEY_LOG_TIMESTAMP)), cursor.getShort(cursor.getColumnIndex(KEY_LOG_RSSI)), 
							cursor.getInt(cursor.getColumnIndex(KEY_LOG_FK_DEVICE)), cursor.getLong(cursor.getColumnIndex(KEY_LOG_DELAY)));
					// Adding contact to list
					logList.add(log);
				} while (cursor.moveToNext());
			}
		}

		// return log list
		return logList;
	}
	// Getting All logs
	public List<ScanResultLog> getAllScanLogs() {
		List<ScanResultLog> logList = new ArrayList<ScanResultLog>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_SCAN_LOGS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if(cursor.getCount() > 0 && cursor != null){
			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					ScanResultLog log = new ScanResultLog(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
							cursor.getLong(cursor.getColumnIndex(KEY_SCAN_TIMESTAMP)), cursor.getShort(cursor.getColumnIndex(KEY_SCAN_RSSI)), 
							cursor.getInt(cursor.getColumnIndex(KEY_SCAN_FK_DEVICE)), cursor.getInt(cursor.getColumnIndex(KEY_SCAN_DURATION)),
							cursor.getLong(cursor.getColumnIndex(KEY_SCAN_BASELINE)));
					// Adding contact to list
					logList.add(log);
				} while (cursor.moveToNext());
			}
		}

		// return log list
		return logList;
	}
	// Getting All logs
	public List<ScanResultLog> getAllScanLogsByDeviceId(long id) {
		List<ScanResultLog> logList = new ArrayList<ScanResultLog>();
		// Select All Query

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query(TABLE_SCAN_LOGS, new String[] { COL_ROW_ID,
				KEY_SCAN_TIMESTAMP, KEY_SCAN_RSSI, KEY_SCAN_FK_DEVICE, KEY_SCAN_DURATION, KEY_SCAN_BASELINE }, KEY_SCAN_FK_DEVICE + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if(cursor.getCount() > 0 && cursor != null){
			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					ScanResultLog log = new ScanResultLog(cursor.getLong(cursor.getColumnIndex(COL_ROW_ID)),
							cursor.getLong(cursor.getColumnIndex(KEY_SCAN_TIMESTAMP)), cursor.getShort(cursor.getColumnIndex(KEY_SCAN_RSSI)), 
							cursor.getInt(cursor.getColumnIndex(KEY_SCAN_FK_DEVICE)), cursor.getInt(cursor.getColumnIndex(KEY_SCAN_DURATION)),
							cursor.getLong(cursor.getColumnIndex(KEY_SCAN_BASELINE)));
					// Adding contact to list
					logList.add(log);
				} while (cursor.moveToNext());
			}
		}

		// return log list
		return logList;
	}

	// Getting devices Count
	public int getDevicesCount() {
		String countQuery = "SELECT  * FROM " + TABLE_DEVICES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		// return count
		return count;
	}

	// Getting logs Count
	public int getLogsCount() {
		String countQuery = "SELECT  * FROM " + TABLE_COMMUNICATION_LOGS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		// return count
		return count;
	}

	// Getting logs Count
	public int getScanLogsCount() {
		String countQuery = "SELECT  * FROM " + TABLE_SCAN_LOGS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		// return count
		return count;
	}
	
	// Updating single device
	public int updateDevice(MyBluetoothDevice device) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_DEVICE_NAME, device.getName());
		values.put(KEY_DEVICE_MAC, device.getMac());
		values.put(KEY_DEVICE_RSSI, device.getRssi());
		values.put(KEY_DEVICE_STATE, device.getState());
		values.put(KEY_DEVICE_COUNTER, device.getCounter());

		// updating row
		return db.update(TABLE_DEVICES, values, COL_ROW_ID + " = ?",
				new String[] { String.valueOf(device.getId()) });
	}
	// Updating single device
	public int updateLog(CommunicationLog log) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_LOG_TIMESTAMP, log.getTimestamp());
		values.put(KEY_LOG_RSSI, log.getRssi());
		values.put(KEY_LOG_FK_DEVICE, log.getDeviceId());
		values.put(KEY_LOG_DELAY, log.getDelay());

		// updating row
		return db.update(TABLE_COMMUNICATION_LOGS, values, COL_ROW_ID + " = ?",
				new String[] { String.valueOf(log.getId()) });
	}
	// Updating single device
	public int updateScanLog(ScanResultLog log) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_SCAN_TIMESTAMP, log.getTimestamp());
		values.put(KEY_SCAN_RSSI, log.getRssi());
		values.put(KEY_SCAN_FK_DEVICE, log.getDeviceId());
		values.put(KEY_SCAN_BASELINE, log.getBaselineTimestamp());
		// updating row
		return db.update(TABLE_SCAN_LOGS, values, COL_ROW_ID + " = ?",
				new String[] { String.valueOf(log.getId()) });
	}
	// Deleting single device
	public void deleteDevice(MyBluetoothDevice device) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_DEVICES, COL_ROW_ID + " = ?",
				new String[] { String.valueOf(device.getId()) });
		db.close();
	}
	// Deleting single log
	public void deleteLog(CommunicationLog log) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_COMMUNICATION_LOGS, COL_ROW_ID + " = ?",
				new String[] { String.valueOf(log.getId()) });
		db.close();
	}
	// Deleting single log
	public void deleteScanLog(ScanResultLog log) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SCAN_LOGS, COL_ROW_ID + " = ?",
				new String[] { String.valueOf(log.getId()) });
		db.close();
	}
}
