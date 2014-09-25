package ch.fshi.btoppnet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import ch.fshi.btoppnet.data.BTOppNetDBHelper;
import ch.fshi.btoppnet.data.CommunicationLog;
import ch.fshi.btoppnet.data.MyBluetoothDevice;
import ch.fshi.btoppnet.data.ScanResultLog;
import ch.fshi.btoppnet.util.Constants;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle.GridStyle;
import com.jjoe64.graphview.LineGraphView;

public class BTDeviceDetailActivity extends Activity {

	BTOppNetDBHelper dbHelper;
	// init example series data
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.btdevice_detail);
		dbHelper = new BTOppNetDBHelper(this);
		Intent intent = getIntent();
		String Mac = intent.getStringExtra(Constants.INTENT_EXTRA_DEVICE_MAC);
		MyBluetoothDevice device = dbHelper.getDevice(Mac);
		List<CommunicationLog> logsList = dbHelper.getAllLogsByDeviceId(device.getId());
		List<ScanResultLog> scanLogsList = dbHelper.getAllScanLogsByDeviceId(device.getId());

		
		int maxIndex = scanLogsList.size();
		int scanLogIndex = 0;
		int eventInterval = 60;
		
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Locale.UK.getCountry()));
		cal.setTime(new Date(scanLogsList.get(0).getTimestamp()));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long startTime = cal.getTimeInMillis();
		Log.d(Constants.TAG_ACT_TEST, String.valueOf(startTime));
		long endTime = scanLogsList.get(scanLogsList.size()-1).getTimestamp();
		Log.d(Constants.TAG_ACT_TEST, String.valueOf(endTime - startTime));
		int daySpan = (int) ((endTime - startTime) / 24 / 1000 / 3600);
		int numEvent = 24*60;
		ArrayList<GraphViewData[]> scanLogsListDataSeriesArray = new ArrayList<GraphViewData[]>();
		ArrayList<Date> seriesDate = new ArrayList<Date>();
		Log.d(Constants.TAG_ACT_TEST, "Day span " + String.valueOf(daySpan));
		for(int i=0; i<=daySpan; i++){
			GraphViewData[] scanData = new GraphViewData[numEvent];
			Log.d(Constants.TAG_ACT_TEST, String.valueOf(i));
			long startOfDay = startTime + (long) (i)*24*3600*1000;
			Log.d(Constants.TAG_ACT_TEST, "start of day " + String.valueOf(startOfDay));
			seriesDate.add(new Date(startOfDay));
			for(int j=0; j<numEvent; j++){
				scanData[j] = new GraphViewData(j, 0);
			}
			scanLogsListDataSeriesArray.add(scanData);
		}
		
		ArrayList<Integer> dayIndexArray = new ArrayList<Integer>();
		
		for(ScanResultLog scanLog : scanLogsList){
			Log.d(Constants.TAG_ACT_TEST, "scan time " + String.valueOf(scanLog.getTimestamp()));
			int dayIndex = (int) ((scanLog.getTimestamp() - startTime)/24/3600/1000);
			long dayTimestamp = scanLog.getTimestamp() - startTime - dayIndex * 24 * 3600 * 1000;
			int dayTimeIndex = (int) dayTimestamp/1000/60;
			if(! dayIndexArray.contains(dayIndex)){
				dayIndexArray.add(dayIndex);
			}
			scanLogsListDataSeriesArray.get(dayIndex)[dayTimeIndex] = new GraphViewData(dayTimeIndex, 1);
		}
		
		GraphView graphView = new LineGraphView(
				this // context
				, "Scan History" // heading
				);

		DateFormat formatter = new SimpleDateFormat("MMM dd", Locale.UK);
		int[] color = {Color.RED, Color.BLUE, Color.CYAN, Color.YELLOW, Color.GREEN, Color.MAGENTA};

		int colorIndex = 0;
		Log.d(Constants.TAG_ACT_TEST, "Day " + String.valueOf(dayIndexArray.size()));
		for(int index : dayIndexArray){
			GraphViewData[] dataSeries = scanLogsListDataSeriesArray.get(index);
			
			Log.d(Constants.TAG_ACT_TEST, String.valueOf(seriesDate.get(index).getTime()));
			GraphViewSeries scanLogsSeries = new GraphViewSeries(formatter.format(seriesDate.get(index)), new GraphViewSeriesStyle(color[colorIndex % 6], 2), dataSeries);
			graphView.addSeries(scanLogsSeries); // data
			colorIndex++;
		}
		String[] xLabels = new String[12];
		for(int i=0; i<12; i++){
			xLabels[i] = String.valueOf(2*i) + ":00";
		}

		graphView.setBackgroundColor(Color.BLACK);
		graphView.getGraphViewStyle().setGridStyle(GridStyle.VERTICAL);
		graphView.getGraphViewStyle().setNumVerticalLabels(3);
		graphView.setHorizontalLabels(xLabels);
		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.TOP);
		graphView.setLegendWidth(200);
		graphView.setVerticalLabels(new String[] {"yes", "no"});
		LinearLayout logsLayout = (LinearLayout) findViewById(R.id.device_detail);
		logsLayout.addView(graphView);
	}
}
