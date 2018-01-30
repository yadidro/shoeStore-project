package bgu.spl.app.Passive;
/**
 * 
 * The discountSchedule is passive object,
 * describe order of shoe type that client wish to buy in particular tick .
 */

public class discountSchedule {
	private String shoeType;
	private int amount;
	private int tick;

	/***
	 * This constructor of discountSchedule
	 * @param shoeType that client wish to buy
	 * @param amount is how much client wish to buy of this shoe type  
	 * @param tick is tick that client wish make this order
	 */
	public discountSchedule(String shoeType, int amount, int tick) {
		this.shoeType = shoeType;
		this.amount = amount;
		this.tick = tick;
	}
	/***
	 * 
	 * @return shoeType that client wish to buy
	 */
	public String getShoeType() {
		return shoeType;
	}

	/***
	 * 
	 * @return tick that client wish make this order
	 */
	public int getTick() {
		return tick;
	}

	/***
	 * 
	 * @return how much client wish to buy of this shoe type
	 */
	public int getAmount() {
		return amount;
	}


}