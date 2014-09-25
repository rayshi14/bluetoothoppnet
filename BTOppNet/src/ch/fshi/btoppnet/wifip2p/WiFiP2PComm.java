package ch.fshi.btoppnet.wifip2p;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Messenger;

public class WiFiP2PComm {

	private Context mContext;
	private Messenger mMessenger;
	private WifiP2pManager mManager;
	private Channel mChannel;
	private WiFiP2PReceiver mReceiver;
	private IntentFilter mIntentFilter;
	
	private static WiFiP2PComm _obj;
	
	private WiFiP2PComm(){
	}

	public static WiFiP2PComm getObject(){
		_obj = new WiFiP2PComm();
		return _obj;
	}

	public void setContext(Context context){
		mContext = context;
	}
	
	public void setHandler(Messenger messenger){
		mMessenger = messenger;
	}

	public void startWiFiService(){
		mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
	    mChannel = mManager.initialize(mContext, mContext.getMainLooper(), null);
	    mReceiver = new WiFiP2PReceiver(mManager, mChannel, mContext);
	    mIntentFilter = new IntentFilter();
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	}
	
	
}
