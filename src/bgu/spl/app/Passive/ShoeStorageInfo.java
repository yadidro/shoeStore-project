package bgu.spl.app.Passive;

import bgu.spl.app.Passive.BuyResult;

/***
 * The ShoeStorageInfo is passive object,
 * An object which represents information about a single type of shoe in the store (e.g., red-sneakers,blue-sandals, etc.).
 *
 */
public class ShoeStorageInfo {

	private String _shoeType;
	private int _amountOnStorage;
	private int _discountedAmount;
	private int _amountSold;

	/***
	 * This constructor of ShoeStorageInfo.
	 * @param shoeType - the type of the shoe (e.g., red-sneakers, blue-sandals, etc.) that this storage info regards.
	 * @param amountOnStorage -the number of shoes of shoeType currently on the storage.
	 */
	public ShoeStorageInfo(String shoeType, int amountOnStorage) {
		_shoeType = shoeType;
		_amountOnStorage = amountOnStorage;
		_discountedAmount = 0;
		_amountSold = 0;
	}
	/***
	 * 
	 *  add number _discountedAmountToAdd to shoes of this type to discount.
	 */
	public synchronized void AddDiscount(int _discountedAmountToAdd){
		if( _discountedAmountToAdd + _discountedAmount > _amountOnStorage)
			_discountedAmount = _amountOnStorage;
		else _discountedAmount = _discountedAmount + _discountedAmountToAdd;
	}
	/***
	 * add number amountToAdd to shoes of this type to store.
	 */
	public synchronized void AddAmount(int amountToAdd){
		_amountOnStorage = _amountOnStorage + amountToAdd;
	}

	/***
	 * 
	 * @return number of shoes of shoeType currently on the storage.
	 */
	public synchronized int getAmountOnStorage(){
		return _amountOnStorage;
	}

	/***
	 * 
	 * @return number of shoes of shoeType currently on the discount.
	 */
	public synchronized int getDiscountedAmount(){
		return _discountedAmount;
	}
	/***
	 * 
	 * @return _shoeType
	 */
	public synchronized String getShoeType(){
		return _shoeType;
	}

	/***
	 * 
	 * @return amount that was sold of this shoe type.
	 */
	public synchronized int getAmountSold(){
		return _amountSold;
	}

	/***
	 *This method will attempt to take a single showType from the store.
	 *It receives the shoeType to take and a boolean - onlyDiscount which indicates that the caller wish to take the item 
	 *only if it is in discount. 
	 *@return BuyResult
	 *<p>
	 *read BuyResult.
	 */
	public synchronized BuyResult take(boolean onlyDiscount){

		if(onlyDiscount && getDiscountedAmount()==0){
			return BuyResult.NOT_ON_DISCOUNT; //selling service will send complete message with result 'null'
		}
		if(getAmountOnStorage()==0){ //if no such shoeType in storage(added after forum question) or amount =0
			return BuyResult.NOT_IN_STOCK; //selling service will send RestockReq to manager
		}
		if(getAmountOnStorage()>0){
			_amountOnStorage--;
			if(getDiscountedAmount()>0){
				_discountedAmount--;
				return BuyResult.DISCOUNTED_PRICE;
			}
			else return BuyResult.REGULAR_PRICE;
		}
		return BuyResult.NOT_IN_STOCK; //will never be sent like this! only from the 'if'
	}


}