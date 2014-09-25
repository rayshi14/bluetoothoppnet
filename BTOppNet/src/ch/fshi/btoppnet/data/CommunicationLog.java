package ch.fshi.btoppnet.data;

public class CommunicationLog extends BasicDBTable{
	long _timestamp;
	short _tmp_rssi;
	long _fk_btdevice;
	long _delay;

	public static final int COL_ID = 0;
	public static final int COL_TIMESTAMP = 1;
	public static final int COL_RSSI = 2;
	public static final int COL_FK_DEVICE = 3;
	public static final int COL_DELAY = 4;
	
	/**
	 * empty constructor
	 */
	public CommunicationLog(){
	}
	
	/**
	 * constructor
	 * @param id
	 * @param timestamp
	 * @param rssi
	 * @param id_btdevice
	 * @param delay
	 */
	public CommunicationLog(long id, long timestamp, short rssi, long id_btdevice, long delay){
		super(id);
		this._timestamp = timestamp;
		this._fk_btdevice = id_btdevice;
		this._delay = delay;
		this._tmp_rssi = rssi;
	}
	/**
	 * constructor
	 * @param id
	 * @param rssi
	 * @param id_btdevice
	 * @param delay
	 */
	public CommunicationLog(long id, short rssi, int id_btdevice, long delay){
		super(id);
		this._timestamp = System.currentTimeMillis();
		this._fk_btdevice = id_btdevice;
		this._delay = delay;
		this._tmp_rssi = rssi;
	}

	// getting timestamp
	public long getTimestamp(){
		return this._timestamp;
	}

	// setting timestamp
	public void setTimestamp(long timestamp){
		this._timestamp = timestamp;
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

	// getting delay
	public long getDelay(){
		return this._delay;
	}

	// setting delay
	public void setDelay(long delay){
		this._delay = delay;
	}

}
