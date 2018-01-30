package bgu.spl.app.Messages;
/**
 * The ManufacturingOrderRequest is class that implements massage
 * Its a request that is sent when the the store manager want that a shoe factory will manufacture a shoe for the store.
 * 
 */

import bgu.spl.app.Passive.Receipt;
import bgu.spl.mics.Request;

public class ManufacturingOrderRequest implements Request <Receipt>{

	private String _shoeType;
	private int _amount;
	private int _requestTick;
	private int _workTime;

	/***
	 * This constructor of ManufacturingOrderRequest.
	 * @param shoeType is type that factory was request to produce .  
	 * @param amount of shoe that factory was request to produce.
	 * @param requestTick is time when manger asked to produce this request.
	 * @param _workTime is how much time factory need to produce this request.
	 */
	public ManufacturingOrderRequest(String shoeType,int amount, int  requestTick){
		_shoeType = shoeType;
		_amount = amount;
		_requestTick=requestTick;
		_workTime = amount;
	}

	/**
	 * 
	 * @return shoe type that factory was request to produce 
	 */
	public String get_shoeType() {
		return _shoeType;
	}


	/***
	 * 
	 * @return amount of shoe that was request to produce
	 */
	public int get_amount() {
		return _amount;
	}

	/**
	 * 
	 * @return  time when manger asked to produce this request
	 */
	public int get_requestTick() {
		return _requestTick;
	}

	/**
	 * 
	 * @return how much time left to factory to produce this request
	 */
	public int get_workTime() {
		return _workTime;
	}

	/**
	 * 
	 * @param update time that factory need to produce this request 
	 */
	public void update_workTime() {
		this._workTime =this._workTime-1;
	}



}
