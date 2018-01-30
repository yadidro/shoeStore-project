package bgu.spl.app.Passive;
/***
 * The PurchaseSchedule is passive object,
 * An object which describes a schedule of a single client-purchase at a specific tick.
 *
 */
public class PurchaseSchedule {
	private String shoeType;
	private int tick;

	/***
	 * This constructor of PurchaseSchedule.
	 * @param shoeType that client wish to purchase.
	 * @param tick in which client wish to purchase.
	 */
	public PurchaseSchedule(String shoeType, int tick) {
		this.shoeType = shoeType;
		this.tick = tick;
	}

	/***
	 * 
	 * @return shoeType that client wish to purchase.
	 */
	public String getShoeType() {
		return shoeType;
	}
	/***
	 * 
	 * @return tick in which client wish to purchase.
	 */
	public int getTick() {
		return tick;
	}

}