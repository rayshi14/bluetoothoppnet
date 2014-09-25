package ch.fshi.btoppnet.data;

public class ScanResultLog extends BasicDBTable{
	long _timestamp;
	short _tmp_rssi;
	long _fk_btdevice;
	int _scan_duration;
	long _scan_timestamp_baseline; // timestamp when this scan started

	public static final int COL_ID = 0;
	public static final int COL_TIMESTAMP = 1;
	public static final int COL_RSSI = 2;
	public static final int COL_FK_DEVICE = 3;
	public static final int COL_SCAN_DURATION = 4;
	public static final int COL_SCAN_BASELINE = 5;
	
	/**
	 * empty constructor
	 */
	public ScanResultLog(){
	}
	
	/**
	 * constructor
	 * @param id
	 * @param timestamp
	 * @param rssi
	 * @param id_btdevice
	 * @param scan_duration
	 */
	public ScanResultLog(long id, long timestamp, short rssi, long id_btdevice, int scan_duration, long scan_baseline){
		super(id);
		this._timestamp = timestamp;
		this._fk_btdevice = id_btdevice;
		this._tmp_rssi = rssi;
		this._scan_timestamp_baseline = scan_baseline;
	}
	/**
	 * constructor
	 * @param id
	 * @param rssi
	 * @param id_btdevice
	 */
	public ScanResultLog(long id, short rssi, int id_btdevice, int scan_duration, long scan_baseline){
		super(id);
		this._timestamp = System.currentTimeMillis();
		this._fk_btdevice = id_btdevice;
		this._tmp_rssi = rssi;
		this._scan_timestamp_baseline = scan_baseline;
	}

	// getting timestamp
	public long getTimestamp(){
		return this._timestamp;
	}

	// setting timestamp
	public void setTimestamp(long timestamp){
		this._timestamp = timestamp;
	}

	// getting baseline timestamp
	public long getBaselineTimestamp(){
		return this._scan_timestamp_baseline;
	}

	// setting baseline timestamp
	public void setBaselineTimestamp(long timestamp){
		this._scan_timestamp_baseline = timestamp;
	}
	
	// getting device id
	public long getDeviceId(){
		return this._fk_btdevice;
	}

	// setting device id
	public void setDeviceId(long deviceId){
		this._fk_btdevice = deviceId;
	}

	// getting device rssi
	public short getRssi(){
		return this._tmp_rssi;
	}

	// setting device rssi
	public void setRssi(short rssi){
		this._tmp_rssi = rssi;
	}

	// getting bt scan duration
	public int getScanDuration(){
		return this._scan_duration;
	}

	// setting bt scan duration
	public void setScanDuration(int duration){
		this._scan_duration = duration;
	}

}
