package ch.fshi.btoppnet;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import ch.fshi.btoppnet.util.Constants;
import ch.fshi.btoppnet.util.SharedPreferencesUtil;

public class ScanIntervalSetup extends Dialog {

	Button okButton;
	Button cancelButton;

	private int selectedIntervalId;
	private int selectedInterval;

	Context parentActivity;
	public class onRadioButtonClicked implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			// Is the button now checked?
			boolean checked = ((RadioButton) view).isChecked();

			// Check which radio button was clicked
			switch(view.getId()) {
			case R.id.radio_scan_interval_010:
				if (checked){
					selectedIntervalId = R.id.radio_scan_interval_010;
					selectedInterval = 10*1000;
				}
				break;
			case R.id.radio_scan_interval_020:
				if (checked){
					selectedIntervalId = R.id.radio_scan_interval_020;
					selectedInterval = 20*1000;
				}
				break;
			case R.id.radio_scan_interval_030:
				if (checked){
					selectedIntervalId = R.id.radio_scan_interval_030;
					selectedInterval = 30*1000;
				}
				break;
			case R.id.radio_scan_interval_1:
				if (checked){
					selectedIntervalId = R.id.radio_scan_interval_1;
					selectedInterval = 60*1000;
				}
				break;
			case R.id.radio_scan_interval_2:
				if (checked){
					selectedIntervalId = R.id.radio_scan_interval_2;
					selectedInterval = 120*1000;
				}
				break;
			case R.id.radio_scan_interval_5:
				if (checked){
					selectedIntervalId = R.id.radio_scan_interval_5;
					selectedInterval = 300*1000;
				}
				break;
			case R.id.radio_scan_interval_10:
				if (checked){
					selectedIntervalId = R.id.radio_scan_interval_10;
					selectedInterval = 600*1000;
				}
				break;
			case R.id.radio_scan_interval_15:
				if (checked){
					selectedIntervalId = R.id.radio_scan_interval_15;
					selectedInterval = 900*1000;
				}
				break;
			case R.id.radio_scan_interval_30:
				if (checked){
					selectedIntervalId = R.id.radio_scan_interval_30;
					selectedInterval = 1800*1000;
				}
				break;
			default:
				break;
			}
		}
	}

	public ScanIntervalSetup(Context context) {
		super(context);
		parentActivity = context;
		// TODO Auto-generated constructor stub
		setContentView(R.layout.dialog_set_scaninterval);
		okButton = (Button) findViewById(R.id.ok_button);
		okButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedPreferencesUtil.savePreferences(parentActivity, Constants.SP_RADIO_SCAN_INTERVAL_ID, selectedIntervalId);
				SharedPreferencesUtil.savePreferences(parentActivity, Constants.SP_RADIO_SCAN_INTERVAL, selectedInterval);

				ScanIntervalSetup.this.dismiss();
			}
		});
		cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ScanIntervalSetup.this.dismiss(); 
			}
		});

		// scan interval id refering to radio button id
		selectedIntervalId = SharedPreferencesUtil.loadSavedPreferences(parentActivity, Constants.SP_RADIO_SCAN_INTERVAL_ID, Constants.DEFAULT_SCAN_INTERVAL_ID);
		selectedInterval = SharedPreferencesUtil.loadSavedPreferences(parentActivity, Constants.SP_RADIO_SCAN_INTERVAL, Constants.DEFAULT_SCAN_INTERVAL);
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.scan_interval_radiogroup);

		int count = radioGroup.getChildCount();
		for (int i=0;i<count;i++) {
			View o = radioGroup.getChildAt(i);
			if (o instanceof RadioButton) {
				o.setOnClickListener(new onRadioButtonClicked());
				if( o.getId() == selectedIntervalId){
					((RadioButton) o).setChecked(true);
				}
			}
		}
	}

}