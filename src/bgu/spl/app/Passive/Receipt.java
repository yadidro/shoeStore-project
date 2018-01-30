package bgu.spl.app.Passive;
/***
 * The Receipt is passive object,
 * An object representing a receipt that should be sent to a client after buying a shoe (when the client’s PurchaseRequest completed).
 *
 */

public class Receipt {
	private String seller;
	private String customer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;

	/***
	 * This constructor of Receipt.
	 * @param seller -the name of the service which issued the receipt.
	 * @param customer- the name of the service this receipt issued to (the client name or “store”).
	 * @param shoeType -the shoe type bought.
	 * @param discount - indicating if the shoe was sold at a discounted price.
	 * @param issuedTick -tick in which this receipt was issued (upon completing the corresponding request).
	 * @param requestTick-tick in which the customer requested to buy the shoe.
	 * @param amountSold- the amount of shoes sold.
	 */
	public Receipt(String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick, int amountSold) {
		this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.issuedTick = issuedTick;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}

	/***
	 * 
	 * @return name of the service which issued the receipt.
	 */
	public String getSeller() {
		return seller;
	}

	/***
	 * 
	 * @return name of the service this receipt issued to (the client name or “store”).
	 */
	public String getCustomer() {
		return customer;
	}

	/***
	 * 
	 * @return the shoe type bought.
	 */
	public String getShoeType() {
		return shoeType;
	}

	/***
	 * 
	 * @return f the shoe was sold at a discounted price.
	 */
	public boolean isDiscount() {
		return discount;
	}

	/***
	 * 
	 * @return in which this receipt was issued (upon completing the corresponding request)
	 */
	public int getIssuedTick() {
		return issuedTick;
	}

	/***
	 * 
	 * @return in which the customer requested to buy the shoe.
	 */
	public int getRequestTick() {
		return requestTick;
	}

	/***
	 * 
	 * @return amount of shoes sold.
	 */
	public int getAmountSold() {
		return amountSold;
	}	
}