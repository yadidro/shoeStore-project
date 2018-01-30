package bgu.spl.app.Messages;

import bgu.spl.mics.Request;

/**
 * The RestockRequest is class that implements massage
 * Its a a request that is sent by the selling service to the store manager 
 * so that he will know that he need to order new shoes from a factory.
 * 
 */

public class RestockRequest implements Request<Boolean> {

	private  String _ShoeType;

	/***
	 * This constructor of RestockRequest.
	 * @param ShoeType is type of shoe that was request to be restock.
	 */
	public RestockRequest(String ShoeType){
		_ShoeType = ShoeType;
	}

	/***
	 * 
	 * @return type of shoe that was request to be restock.
	 */
	public String get_ShoeType() {
		return _ShoeType;
	}

}