package ch.fshi.btoppnet;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import ch.fshi.btoppnet.util.Constants;
import ch.fshi.btoppnet.util.SharedPreferencesUtil;

public class ScanDurationSetup extends Dialog {

	Button okButton;
	Button cancelButton;

	private int selectedDurationId = 0;
	private int selectedDuration = 0;

	Context parentActivity;
	public class onRadioButtonClicked implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			// Is the button now checked?
			boolean checked = ((RadioButton) view).isChecked();

			// Check which radio button was clicked
			switch(view.getId()) {
			case R.id.radio_scan_duration_1:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_1;
					selectedDuration = 1000;
				}
				break;
			case R.id.radio_scan_duration_2:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_2;
					selectedDuration = 2000;
				}
				break;
			case R.id.radio_scan_duration_3:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_3;
					selectedDuration = 3000;
				}
				break;
			case R.id.radio_scan_duration_4:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_4;
					selectedDuration = 4000;
				}
				break;
			case R.id.radio_scan_duration_5:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_5;
					selectedDuration = 5000;
				}
				break;
			case R.id.radio_scan_duration_6:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_6;
					selectedDuration = 6000;
				}
				break;
			case R.id.radio_scan_duration_7:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_7;
					selectedDuration = 7000;
				}
				break;

			case R.id.radio_scan_duration_8:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_8;
					selectedDuration = 8000;
				}
				break;
			case R.id.radio_scan_duration_9:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_9;
					selectedDuration = 9000;
				}
				break;

			case R.id.radio_scan_duration_10:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_10;
					selectedDuration = 10000;
				}
				break;
			case R.id.radio_scan_duration_11:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_11;
					selectedDuration = 11000;
				}
				break;
			case R.id.radio_scan_duration_12:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_12;
					selectedDuration = 12000;
				}
				break;
			case R.id.radio_scan_duration_13:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_13;
					selectedDuration = 13000;
				}
				break;
			case R.id.radio_scan_duration_100:
				if (checked){
					selectedDurationId = R.id.radio_scan_duration_100;
					selectedDuration = 15000;
				}
				break;
			default:
				break;
			}
		}
	}

	public ScanDurationSetup(Context context) {
		super(context);
		parentActivity = context;
		// TODO Auto-generated constructor stub
		setContentView(R.layout.dialog_set_scanduration);
		okButton = (Button) findViewById(R.id.ok_button);
		okButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// not click on anything but pressed confirmation
				if(selectedDurationId > 0){
					SharedPreferencesUtil.savePreferences(parentActivity, Constants.SP_RADIO_SCAN_DURATION_ID, selectedDurationId);
					SharedPreferencesUtil.savePreferences(parentActivity, Constants.SP_RADIO_SCAN_DURATION, selectedDuration);
				}
				ScanDurationSetup.this.dismiss();
			}
		});
		cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ScanDurationSetup.this.dismiss(); 
			}
		});

		// scan duration id refering to radio button id
		selectedDurationId = SharedPreferencesUtil.loadSavedPreferences(parentActivity, Constants.SP_RADIO_SCAN_DURATION_ID, Constants.DEFAULT_SCAN_DURATION_ID);
		selectedDuration = SharedPreferencesUtil.loadSavedPreferences(parentActivity, Constants.SP_RADIO_SCAN_DURATION, Constants.DEFAULT_SCAN_DURATION);
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.scan_duration_radiogroup);

		int count = radioGroup.getChildCount();
		for (int i=0;i<count;i++) {
			View o = radioGroup.getChildAt(i);
			if (o instanceof RadioButton) {
				o.setOnClickListener(new onRadioButtonClicked());
				if( o.getId() == selectedDurationId){
					((RadioButton) o).setChecked(true);
				}
			}
		}
	}

}