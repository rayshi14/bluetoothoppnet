package ch.fshi.btoppnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import ch.fshi.btoppnet.bluetooth.BluetoothComm;
import ch.fshi.btoppnet.bluetooth.ScanningAlarm;
import ch.fshi.btoppnet.data.BTOppNetDBHelper;
import ch.fshi.btoppnet.data.CommunicationLog;
import ch.fshi.btoppnet.data.MyBluetoothDevice;
import ch.fshi.btoppnet.data.ScanResultLog;
import ch.fshi.btoppnet.util.Constants;
import ch.fshi.btoppnet.util.SharedPreferencesUtil;

public class BTDeviceListActivity extends Activity {

	// current activity
	public static Context context = null;

	/**
	 * List of request code
	 */
	private final int REQUEST_BT_ENABLE = 1;
	private final int REQUEST_BT_DISCOVERABLE = 11;

	private int RESULT_BT_DISCOVERABLE_DURATION = 300;

	private boolean mScanning;
	private boolean autoScanning;

	private long scanStartTimestamp = System.currentTimeMillis() - 100000;

	private ArrayList<BTDevice> deviceList = new ArrayList<BTDevice>();
	BTDeviceListAdapter deviceListAdapter;

	private BluetoothAdapter mBluetoothAdapter = null;
	String[] defaultDevices ={"BC:EE:7B:B0:7E:5A","F8:D0:BD:95:7E:28","D8:50:E6:34:15:4D"};
	ArrayList<String> goodDevices = new ArrayList<String>(Arrays.asList(defaultDevices));

	ArrayList<String> devicesFound = new ArrayList<String>();

	// database helper
	BTOppNetDBHelper dbHelper = null;
	
	private BluetoothComm bluetoothHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_btdevice_list);

		context = this;

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
		}
		else{			
			// start bluetooth utils
			bluetoothHelper = BluetoothComm.getObject(context, new Messenger(mHandler));
			bluetoothHelper.startServer();
			// clean up
			ScanningAlarm.stopScanning(context);
		}


		dbHelper = new BTOppNetDBHelper(context);
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		registerReceiver(BTFoundReceiver, filter);

		// update shared preference
		SharedPreferencesUtil.savePreferences(context, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_SCAN_DURATION);
		SharedPreferencesUtil.savePreferences(context, Constants.SP_RADIO_SCAN_DURATION_ID, Constants.DEFAULT_SCAN_DURATION_ID);
		SharedPreferencesUtil.savePreferences(context, Constants.SP_RADIO_SCAN_INTERVAL, Constants.DEFAULT_SCAN_INTERVAL);
		SharedPreferencesUtil.savePreferences(context, Constants.SP_RADIO_SCAN_INTERVAL_ID, Constants.DEFAULT_SCAN_INTERVAL_ID);

		//inflate the device list
		List<MyBluetoothDevice> dbDeviceList = dbHelper.getAllDevices();
		for(MyBluetoothDevice dbDevice : dbDeviceList){
			BluetoothDevice tmpBTDevice = mBluetoothAdapter.getRemoteDevice(dbDevice.getMac());
			BTDevice tmpDevice = new BTDevice(tmpBTDevice);
			tmpDevice.setConnState(dbDevice.getState());
			tmpDevice.setRssi(dbDevice.getRssi());
			deviceList.add(tmpDevice);
		}

		// adapters
		deviceListAdapter = new BTDeviceListAdapter(context, R.layout.devicelist_bt_device, deviceList);
		ListView lv = (ListView) findViewById(R.id.device_list);
		lv.setAdapter(deviceListAdapter);
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, BTDeviceDetailActivity.class);
				intent.putExtra(Constants.INTENT_EXTRA_DEVICE_MAC, deviceList.get(position).getMAC());
				startActivity(intent);
			}
		});

		// start wifi-direct utils
		/**
		 * ToDo: start wifi server
		 */
	}

	@Override
	protected void onDestroy() {
		Log.d(Constants.TAG_APPLICATION, "onDestroy()");
		ScanningAlarm.stopScanning(context);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.d(Constants.TAG_APPLICATION, "onStop()");
		super.onStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.d(Constants.TAG_APPLICATION, "onPause()");
		super.onPause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.d(Constants.TAG_APPLICATION, "onRestart()");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Log.d(Constants.TAG_APPLICATION, "onResume()");
		super.onResume();
	}

	/**
	 * The Handler that gets information back from the BTService
	 */
	class btServiceHandler extends Handler {

		@Override
		public void handleMessage(Message msg)
		{
			Bundle b = msg.getData();
			String MAC = b.getString(Constants.MESSAGE_DATA_DEVICE_MAC);
			switch(msg.what){
			case Constants.MESSAGE_WHAT_SCAN_STARTED:
				// set device state to outdated
				for (BTDevice device : deviceList){
					device.setConnState(Constants.STATE_CLIENT_OUTDATED);
					deviceListAdapter.notifyDataSetChanged();
				}
				break;
			case Constants.MESSAGE_WHAT_DATA:
				JSONObject json;
				int type;
				try {
					json = new JSONObject(b.getString(Constants.MESSAGE_DATA));
					type = json.getInt(Constants.PACKET_TYPE);
					switch(type){
					case Constants.PACKET_TYPE_TIMESTAMP_DATA:
						Log.d(Constants.TAG_ACT_TEST, "receive data");
						Toast.makeText(context, "received data:" + json.getString(Constants.PACKET_DATA),Toast.LENGTH_SHORT).show();
						JSONObject ack = new JSONObject();
						try {
							ack.put(Constants.PACKET_TYPE, Constants.PACKET_TYPE_TIMESTAMP_ACK);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.e(Constants.TAG_ACT_TEST, "json error");
							e.printStackTrace();
						}
						bluetoothHelper.writeObject(MAC, ack);
						break;
					case Constants.PACKET_TYPE_TIMESTAMP_ACK:
						Log.d(Constants.TAG_ACT_TEST, "receive ack");
						long timeAckReceived = System.currentTimeMillis();
						Toast.makeText(context, "received ack",Toast.LENGTH_SHORT).show();
						bluetoothHelper.stopConnection(MAC);
						// update deviceList adapter
						deviceListAdapter.setDeviceAction(MAC, Constants.PACKET_TYPE_TIMESTAMP_ACK);
						deviceListAdapter.notifyDataSetChanged();
						// update log adapter
						String name = null;
						long timeConnectionStart = timeAckReceived + 1;
						for(BTDevice device : deviceList){
							if (device.getMAC().equals(MAC)){
								name = device.getName();
								timeConnectionStart = device.getConnectionStartTime();
								break;
							}
						}
						if(name != null){
							CommunicationLog comLog = new CommunicationLog();
							MyBluetoothDevice myDevice = dbHelper.getDevice(MAC);
							comLog.setTimestamp(timeAckReceived);
							comLog.setDeviceId(myDevice.getId());
							comLog.setRssi(myDevice.getRssi());
							comLog.setDelay(timeAckReceived - timeConnectionStart);
							dbHelper.addLog(comLog);
						}
						break;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case Constants.MESSAGE_WHAT_CLIENT_CONNECTED:
				Log.d(Constants.TAG_ACT_TEST, "client connected");
				if (!goodDevices.contains(MAC)) goodDevices.add(MAC);
				// update main UI (current listview)
				deviceListAdapter.setDeviceAction(MAC, Constants.MESSAGE_WHAT_CLIENT_CONNECTED);
				deviceListAdapter.notifyDataSetChanged();
				Toast.makeText(context, "Client connected",Toast.LENGTH_SHORT).show();
				JSONObject dataTimestamp = new JSONObject();
				try {
					dataTimestamp.put(Constants.PACKET_TYPE, Constants.PACKET_TYPE_TIMESTAMP_DATA);
					dataTimestamp.put(Constants.PACKET_DATA, String.valueOf(System.currentTimeMillis()));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bluetoothHelper.writeObject(MAC, dataTimestamp);
				break;
			case Constants.MESSAGE_WHAT_CLIENT_CONNECTED_FAILED:
				Log.d(Constants.TAG_ACT_TEST, "client failed");
				if(goodDevices.contains(MAC)){
					goodDevices.remove(MAC);
				}
				deviceListAdapter.setDeviceAction(MAC, Constants.MESSAGE_WHAT_CLIENT_CONNECTED_FAILED);
				deviceListAdapter.notifyDataSetChanged();
				Toast.makeText(context, "Connection failed, retry",Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	}

	private btServiceHandler mHandler = new btServiceHandler();

	// Create a BroadcastReceiver for actions
	BroadcastReceiver BTFoundReceiver = new BTServiceBroadcastReceiver();

	class BTServiceBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.d(Constants.TAG_APPLICATION, "get a device : " + String.valueOf(device.getAddress()));
				/*
				 * -30dBm = Awesome
				 * -60dBm = Good
				 * -80dBm = OK
				 * -90dBm = Bad
				 */
				short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);
				int deviceIndex = -1;
				for(int i=0; i < deviceList.size(); i++){
					if (deviceList.get(i).getMAC().equals(device.getAddress())){
						deviceIndex = i;
					}
				}
				MyBluetoothDevice myBTDevice = new MyBluetoothDevice();
				if (deviceIndex < 0){ // device not exist
					BTDevice btDevice = new BTDevice(device);
					btDevice.setRssi(rssi);
					btDevice.setConnState(Constants.STATE_CLIENT_UNCONNECTED);
					BTConnectButtonOnClickListener onClickListener = new BTConnectButtonOnClickListener(btDevice, bluetoothHelper);
					btDevice.setOnClickListener(onClickListener);
					deviceIndex = deviceList.size();
					deviceList.add(btDevice);
					myBTDevice.setMac(btDevice.getMAC());
					myBTDevice.setName(btDevice.getName());
					myBTDevice.setRssi(btDevice.getRssi());
					myBTDevice.setState(btDevice.getConnState());
					myBTDevice.setCounter(btDevice.getRetryCounter());
				}
				else{ // device already found
					BTDevice btDevice = deviceList.get(deviceIndex);
					btDevice.setRssi(rssi);
					btDevice.setConnState(Constants.STATE_CLIENT_UNCONNECTED);
					BTConnectButtonOnClickListener onClickListener = new BTConnectButtonOnClickListener(btDevice, bluetoothHelper);
					btDevice.setOnClickListener(onClickListener);
					myBTDevice.setMac(btDevice.getMAC());
					myBTDevice.setName(btDevice.getName());
					myBTDevice.setRssi(btDevice.getRssi());
					myBTDevice.setState(Constants.STATE_CLIENT_OUTDATED);
					myBTDevice.setCounter(btDevice.getRetryCounter());
				}
				dbHelper.addDevice(myBTDevice);
				if(!devicesFound.contains(myBTDevice.getMac())){
					devicesFound.add(myBTDevice.getMac());
					ScanResultLog dbScanLog = new ScanResultLog();
					dbScanLog.setTimestamp(System.currentTimeMillis());
					dbScanLog.setDeviceId(dbHelper.getDevice(myBTDevice.getMac()).getId());
					dbScanLog.setRssi(rssi);
					dbScanLog.setScanDuration(SharedPreferencesUtil.loadSavedPreferences(context, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_SCAN_DURATION));
					dbScanLog.setBaselineTimestamp(scanStartTimestamp);
					dbHelper.addScanLog(dbScanLog);
				}
				deviceListAdapter.sortList();
				deviceListAdapter.notifyDataSetChanged();
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				mScanning = true;
				Log.d(Constants.TAG_APPLICATION, "Discovery process has been started: " + String.valueOf(System.currentTimeMillis()));
				if(System.currentTimeMillis() - scanStartTimestamp > SharedPreferencesUtil.loadSavedPreferences(context, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_SCAN_DURATION)){
					//a new scan has been started
					Log.d(Constants.TAG_APPLICATION, "Timestamp updated: " + String.valueOf(System.currentTimeMillis()));
					devicesFound = new ArrayList<String>();
					scanStartTimestamp = System.currentTimeMillis();
				}
				invalidateOptionsMenu();
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				mScanning = false;
				invalidateOptionsMenu();

				Log.d(Constants.TAG_APPLICATION, "Discovery process has been stopped: " + String.valueOf(System.currentTimeMillis()));

				for(BTDevice device : deviceList){
					if(goodDevices.contains(device.getMAC())){
						Log.d(Constants.TAG_ACT_TEST, "good list contains" + device.getMAC());
						device.resetRetryCounter();
						/**
						 * make decisions on whether to choose bluetooth to communicate OR to scan using WiFi-Direct
						 */
						//mBTService.connect(device.getRawDevice());
						device.setConnectionStartTime(System.currentTimeMillis());
					}
				}
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.btdevice_list, menu);
		if (!mScanning) {
			menu.findItem(R.id.action_stop).setVisible(false);
			menu.findItem(R.id.action_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.action_stop).setVisible(true);
			menu.findItem(R.id.action_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}

		menu.findItem(R.id.action_set_autoscan_start).setChecked(autoScanning);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id){
		case R.id.action_settings:
			break;
		case R.id.action_scan:
			Log.d(Constants.TAG_APPLICATION, "Click scan button");
			BluetoothComm.getObject().startScanService(true);
			break;
		case R.id.action_stop:
			Log.d(Constants.TAG_APPLICATION, "Click stop button");
			BluetoothComm.getObject().startScanService(false);
			break;
		case R.id.action_set_scaninterval:
			Log.d(Constants.TAG_APPLICATION, "Set scan interval");
			ScanIntervalSetup scanIntervalDialog = new ScanIntervalSetup(this);
			scanIntervalDialog.setTitle("SET SCAN INTERVAL");
			scanIntervalDialog.show();
			break;
		case R.id.action_set_scantime:
			Log.d(Constants.TAG_APPLICATION, "Set scan time");
			ScanDurationSetup scanDurationDialog = new ScanDurationSetup(this);
			scanDurationDialog.setTitle("SET SCAN DURATION");
			scanDurationDialog.show();
			break;
		case R.id.action_set_autoscan_start:
			if(item.isChecked()){ //autoscan will be stopped
				autoScanning = false;
				Log.d(Constants.TAG_ACT_TEST, "unchecked");
				item.setChecked(autoScanning);
				stopAutoScanTask();
			}
			else{
				autoScanning = true;
				Log.d(Constants.TAG_ACT_TEST, "checked");
				item.setChecked(autoScanning);
				startAutoScanTask();
			}

			break;
		default:
			break;
		}
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		switch (requestCode){
		case REQUEST_BT_ENABLE:
			if (resultCode == RESULT_OK) {
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is enabled by the user.");
				// start bluetooth utils
				bluetoothHelper = BluetoothComm.getObject(context, new Messenger(mHandler));
				bluetoothHelper.startServer();
				// clean up
				ScanningAlarm.stopScanning(context);
				Intent discoverableIntent = new
						Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, RESULT_BT_DISCOVERABLE_DURATION);
				startActivityForResult(discoverableIntent, REQUEST_BT_DISCOVERABLE);
			}
			else{
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is not enabled by the user.");
			}
			break;
		case REQUEST_BT_DISCOVERABLE:
			if (resultCode == RESULT_CANCELED){
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is not discoverable.");
			}
			else{
				Log.d(Constants.TAG_APPLICATION, "Bluetooth is discoverable by 300 seconds.");
			}
			break;
		default:
			break;
		}
	}

	private void startAutoScanTask() {
		new ScanningAlarm(context);
	}

	private void stopAutoScanTask() {
		ScanningAlarm.stopScanning(context);
	}

}
