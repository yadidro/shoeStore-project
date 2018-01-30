package bgu.spl.app.Messages;

import bgu.spl.app.Passive.Receipt;
import bgu.spl.mics.Request;

/**
 * The PurchaseOrderRequest is class that implements massage
 * Its a request that is sent when the a store client wish to buy a shoe.
 * 
 */
public class PurchaseOrderRequest implements Request<Receipt> {

	private String _ShoeType;
	private boolean _OnlyDiscount;
	private int _requestTick;
	private String _costumer;

	/***
	 * This constructor of PurchaseOrderRequest.
	 * @param ShoeType is type that client request to purchase
	 * @param OnlyDiscount indicate if client wish to buy a shoe on discount.
	 * @param costumer is name of client.
	 * @param requestTick is time when client asked to purchase this shoe type.
	 */
	public PurchaseOrderRequest(String ShoeType,boolean OnlyDiscount, String costumer,int requestTick){
		_ShoeType = ShoeType;
		_OnlyDiscount = OnlyDiscount;
		_costumer = costumer;
		_requestTick = requestTick;
	}

	/***
	 * 
	 * @return _ShoeType that was request to purchase.
	 */
	public String get_ShoeType() {
		return _ShoeType;
	}

	/***
	 * 
	 * @return if client wish to buy a shoe on discount.
	 */
	public boolean is_OnlyDiscount() {
		return _OnlyDiscount;
	}

	/***
	 * 
	 * @return time when client asked to purchase this type.
	 */
	public int getRequestTick(){
		return _requestTick;
	}

	/***
	 * 
	 * @return name of client.
	 */
	public String getCostumer(){
		return _costumer;
	}

}