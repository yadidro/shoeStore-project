package bgu.spl.app.Messages;

import bgu.spl.mics.Broadcast;

/**
 * The NewDiscountedBroadcast is class that implements massage
 * Its a broadcast message that is sent when the manager of the store decides to have a sale on a specific shoe.
 * 
 */

public class NewDiscountedBroadcast implements Broadcast {
	private String shoeType;
	
	/***
	 * This constructor of NewDiscountedBroadcast
	 * @param shoeType is type of shoe that manger decided to give discount
	 */
	public NewDiscountedBroadcast(String shoeType) {
		this.shoeType = shoeType;
	}
	/***
	 * 
	 * @return shoe type that has discount
	 */
	public String getShoeType() {
		return shoeType;
	}

	
	
	

}