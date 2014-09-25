package ch.fshi.btoppnet;

import android.view.View;
import android.view.View.OnClickListener;
import ch.fshi.btoppnet.bluetooth.BluetoothComm;

public class BTConnectButtonOnClickListener implements OnClickListener {

	BTDevice btDevice;
	BluetoothComm btHelper;
	
	public BTConnectButtonOnClickListener(BTDevice btDevice, BluetoothComm btService){
		this.btDevice = btDevice;
		this.btHelper = btService;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		btHelper.connect(btDevice.getRawDevice());
		btDevice.setConnectionStartTime(System.currentTimeMillis());
	}
}