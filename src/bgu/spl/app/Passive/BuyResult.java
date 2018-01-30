package bgu.spl.app.Passive;
/**
 * 
 * The BuyResult is passive object,
 * it is an enum result that method take receive.
 * <p>
 *NOT_IN_STOCK: which indicates that there were no shoe of this type in stock (the store storage should not be changed in this case).
 * <p>
 *NOT_ON_DISCOUNT: which indicates that the "onlyDiscount" was true and there are no discounted shoes with the requested type.
 * <p>
 *REGULAR_PRICE: which means that the item was successfully taken (the amount of items of this type was reduced by one).
 * <p>
 *DISCOUNTED_PRICE: which means that was successfully taken in a discounted price (the of items of this type was reduced by one and the amount of discounted items reduced by one).
 *
 */
public enum BuyResult {
	NOT_IN_STOCK,NOT_ON_DISCOUNT,REGULAR_PRICE,DISCOUNTED_PRICE
}