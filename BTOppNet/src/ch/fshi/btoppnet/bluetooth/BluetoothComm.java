package ch.fshi.btoppnet.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import ch.fshi.btoppnet.util.Constants;
import ch.fshi.btoppnet.util.SharedPreferencesUtil;

public class BluetoothComm {

	private static final UUID MY_UUID = UUID.fromString("8113ac40-438f-11e1-b86c-0800200c9a66");

	private BluetoothAdapter mBluetoothAdapter;

	// server/client status
	private boolean serverRunning = false;

	Messenger mMessenger = null;

	private ArrayList<ConnectedThread> connections = new ArrayList<ConnectedThread>();

	// current connection state, only one server thread and one client thread
	private ServerThread mServerThread = null;
	private StringBuffer sbLock = new StringBuffer("BTService");

	private Handler timeoutHandler = new Handler();

	public static BluetoothComm _obj;

	private Context mContext;

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * @param context  The UI Activity Context
	 * @param handler  A Handler to send messages back to the UI Activity
	 */
	private BluetoothComm(Context context, Messenger messenger) {

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mMessenger = messenger;
		mContext = context;
	}

	public static BluetoothComm getObject(Context context, Messenger messenger){
		if(_obj == null){
			_obj = new BluetoothComm(context, messenger);
		}
		return _obj;
	}

	public static BluetoothComm getObject(){
		if(_obj == null){
			return null;
		}
		return _obj;
	}

	/**
	 * BT Server thread
	 * @author fshi
	 *
	 */
	private class ServerThread extends Thread {
		private final BluetoothServerSocket mServerSocket;

		public ServerThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.STR_APPLICATION_NAME, MY_UUID);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			serverRunning = true;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				Log.d(Constants.TAG_APPLICATION, "BT server waiting for incoming connections");
				try {
					socket = mServerSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				// If a connection was accepted
				if (socket != null && socket.isConnected()) {
					if(mBluetoothAdapter.isDiscovering()){
						mBluetoothAdapter.cancelDiscovery();
					}
					// Do work to manage the connection (in a separate thread)
					Log.d(Constants.TAG_APPLICATION, "Connected as a server");//manageConnectedSocket(socket);
					// start a new thread to handling data exchange
					connected(socket, socket.getRemoteDevice(), false);
				}
			}
			try {
				mServerSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverRunning = false;
			return;
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				Log.d(Constants.TAG_ACT_TEST, "server thread is stopped");
				mServerSocket.close();
			} catch (IOException e) { }
		}
	}


	/**
	 * Client thread to handle issued connection command
	 * @author fshi
	 *
	 */
	private class ClientThread extends Thread {
		private final BluetoothSocket mClientSocket;
		private final BluetoothDevice mBTDevice;
		private boolean clientConnected = false;
		private StringBuffer sb;

		public ClientThread(BluetoothDevice device, StringBuffer sb) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mBTDevice = device;
			this.sb=sb;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server code
				tmp = mBTDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) { }
			mClientSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			if(mBluetoothAdapter.isDiscovering()){
				mBluetoothAdapter.cancelDiscovery();
			}
			// timestamp before connection
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				// stop the connection after 5 seconds
				synchronized (sb){
					timeoutHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							Log.d(Constants.TAG_ACT_TEST, "post delay " + String.valueOf(clientConnected));
							if(!clientConnected){
								cancel();
								Message msg=Message.obtain();
								msg.what = Constants.MESSAGE_WHAT_CLIENT_CONNECTED_FAILED;
								// send necessary info to the handler
								Bundle b = new Bundle();
								b.putString(Constants.MESSAGE_DATA_DEVICE_MAC, mBTDevice.getAddress());
								msg.setData(b);
								try {
									mMessenger.send(msg);
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}, Constants.BT_CLIENT_TIMEOUT);
					mClientSocket.connect();
					clientConnected = true;
					Log.d(Constants.TAG_APPLICATION, "Connected as a client");//manageConnectedSocket(socket);
					// Do work to manage the connection (in a separate thread)
					// start a new thread to handling data exchange
					connected(mClientSocket, mClientSocket.getRemoteDevice(), true);
				}
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mClientSocket.close();
				} catch (IOException closeException) { 
				}
			}

			// Do work to manage the connection (in a separate thread)
			return;
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				Log.d(Constants.TAG_ACT_TEST, "client thread is stopped");
				mClientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * @param socket  The BluetoothSocket on which the connection was made
	 * @param device  The BluetoothDevice that has been connected
	 */
	private synchronized void connected(BluetoothSocket socket, BluetoothDevice device, boolean iAmClient) {
		ConnectedThread newConn = new ConnectedThread(socket);
		newConn.start();
		connections.add(newConn);
		// Send the info of the connected device back to the UI Activity
		Message msg=Message.obtain();
		if(iAmClient){
			msg.what = Constants.MESSAGE_WHAT_CLIENT_CONNECTED;
		}else{
			msg.what = Constants.MESSAGE_WHAT_SERVER_CONNECTED;
		}
		// send necessary info to the handler
		Bundle b = new Bundle();
		b.putString(Constants.MESSAGE_DATA_DEVICE_MAC, device.getAddress());
		msg.setData(b);
		try {
			mMessenger.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Connected Thread for handling established connections
	 * @author fshi
	 *
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mConnectedSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mConnectedSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = mConnectedSocket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public String getMac(){
			return mConnectedSocket.getRemoteDevice().getAddress();
		}

		public void run() {
			Object buffer;

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					ObjectInputStream in = new ObjectInputStream(mmInStream);
					buffer = in.readObject();
					// Send the obtained bytes to the UI activity
					try {
						// Send the obtained bytes to the UI Activity
						Message msg=Message.obtain();
						Bundle b = new Bundle();
						b.putString(Constants.MESSAGE_DATA, buffer.toString());
						b.putString(Constants.MESSAGE_DATA_DEVICE_MAC, this.getMac());
						msg.what = Constants.MESSAGE_WHAT_DATA;
						msg.setData(b);
						mMessenger.send(msg);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
					stopConnection(getMac());
					break;
					// stop the connected thread
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					stopConnection(getMac());
					break;
				}
			}
		}

		public void writeObject(JSONObject json) {
			try {
				Log.d(Constants.TAG_ACT_TEST, String.valueOf(mmOutStream));
				ObjectOutputStream out = new ObjectOutputStream(mmOutStream);
				out.writeObject(json.toString());
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(Constants.TAG_ACT_TEST, "output stream error");
				e.printStackTrace();
				stopConnection(getMac());
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				Log.d(Constants.TAG_ACT_TEST, "connected thread " + getMac() + " is stopped");
				mmInStream.close();
				mmOutStream.close();
				mConnectedSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// used to managed connections
	public synchronized void stopConnection(String MAC){
		for(ConnectedThread connection : connections){
			if(connection.getMac().equals(MAC)){
				connection.cancel();
				Log.d(Constants.TAG_ACT_TEST, "remove connection" + connection.getMac());
				connections.remove(connection);
				break;
			}
		}
		Log.d(Constants.TAG_ACT_TEST, String.valueOf(connections.size()));
	}

	// used to send data to a dest
	public synchronized void writeObject(String MAC, JSONObject data){
		for(ConnectedThread connection : connections){
			if(connection.getMac().equals(MAC)){
				connection.writeObject(data);
				break;
			}
		}
	}
	/**
	 * Start listening
	 * @return
	 */
	public synchronized void startServer(){
		stopServer();
		mServerThread = new ServerThread();             
		mServerThread.start();
	}

	/**
	 * stop listening
	 * @return
	 */
	private void stopServer(){
		if(serverRunning){
			mServerThread.cancel();
		}
	}

	/**
	 * connect to a BT device
	 * @return
	 */
	public synchronized void connect(BluetoothDevice btDevice){
		// Start the thread to connect with the given device
		Log.d(Constants.TAG_ACT_TEST, "connect to :" + btDevice.getAddress());	
		ClientThread clientThread = new ClientThread(btDevice, sbLock);
		clientThread.start();
	}

	/**
	 * stop all working thread
	 */
	public void close() {
		// clean up
		stopServer();
		return;
	}
	/**
	 * get the number of active connections
	 * @return
	 */
	private int getActiveConnectionsCount(){
		return connections.size();
	}

	/**
	 * start scan service
	 * @param scanStart
	 */
	public void startScanService(boolean scanStart) {
		if (scanStart){ // if command is to start scanning
			if (mBluetoothAdapter.isDiscovering()){ // if scan is already started, stop the current scanning
				mBluetoothAdapter.cancelDiscovery();
			}

			if(getActiveConnectionsCount() == 0){
				mBluetoothAdapter.startDiscovery();

				// set device state to outdated, update db and notify list change
				Message msg=Message.obtain();
				msg.what = Constants.MESSAGE_WHAT_SCAN_STARTED;

				// send necessary info to the handler
				try {
					mMessenger.send(msg);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Cancel the discovery process after SCAN_INTERVAL
				final Handler discoveryHandler = new Handler();
				discoveryHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mBluetoothAdapter.isDiscovering()){
							mBluetoothAdapter.cancelDiscovery();
						}
					}
				}, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_SCAN_DURATION));
			}
		}
		else{ // if command is to stop scanning
			mBluetoothAdapter.cancelDiscovery();
		}
	}
}
