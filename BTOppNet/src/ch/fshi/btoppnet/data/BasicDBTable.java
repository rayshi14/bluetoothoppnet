package ch.fshi.btoppnet.data;

public class BasicDBTable {
	//private variables
	protected long _id;

	// Empty constructor
	public BasicDBTable(){

	}
	
	public BasicDBTable(long id){
		this._id = id;
	}
	
	public void setId(int id){
		this._id = id;
	}
	
	public long getId(){
		return this._id;
	}
}
