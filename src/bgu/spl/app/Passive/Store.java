package bgu.spl.app.Passive;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.app.Passive.BuyResult;

public class Store {
	private ConcurrentHashMap<String,ShoeStorageInfo> _storage;
	private ConcurrentLinkedQueue<Receipt> receipts;

	/**
	 * 
	 * create new static instance of Store in case Store
	 *
	 */
	private static class SingletonHolder {
		private static Store instance = new Store();
	}
	/**
	 * constructor for Store. 
	 */
	private Store(){
		_storage = new ConcurrentHashMap<String,ShoeStorageInfo>();
		receipts = new ConcurrentLinkedQueue<Receipt>();
	}
	/**
	 * 
	 * @return the only instance of singleton Store
	 */
	public static Store getInstance() {
		return SingletonHolder.instance;
	}
	/**
	 * 
	 * @param storage	 array which contains data about each shoe, parsed from Json file.
	 */
	public void load(ShoeStorageInfo[] storage){
		if(_storage.size() > 0)  _storage = new ConcurrentHashMap<String,ShoeStorageInfo>();
		for(int i=0;i<storage.length; i++)
			_storage.put(storage[i].getShoeType(), storage[i]);
	}
	/**
	 * 	change amount on storage and/or discounted amount of the requested shoeType according to request.
	 *  if the shoe is NOT_ON_DISCOUNT or NOT_IN_STOCK: do nothing.
	 *  else: update amount on storage and/or discounted amount.
	 * @param shoeType name of the shoe to take.
	 * @param onlyDiscount 	"true" if costumer requested to buy this shoe only if it was discounted. "false" otherwise. 
	 * @return	enum which indicates the statue of the shoe(NOT_ON_DISCOUNT / NOT_IN_STOCK / REGULAR_PRICE / DISCOUNTED_PRICE.
	 */
	public BuyResult take(String shoeType, boolean onlyDiscount){
		if(!_storage.containsKey(shoeType)){
			if(onlyDiscount) return BuyResult.NOT_ON_DISCOUNT;
			else return BuyResult.NOT_IN_STOCK;
		}

		else return _storage.get(shoeType).take(onlyDiscount);
	}
	/**
	 * adds the given amount to the ShoeStorageInfo of the given shoeType.
	 * @param shoeType	specific shoeType to add amountOnStorage
	 * @param amount	amount to add to this specific shoeType.
	 */
	public void add(String shoeType,int amount){
		if(!_storage.containsKey(shoeType)){
			_storage.put(shoeType, new ShoeStorageInfo(shoeType,amount));
		}
		synchronized(_storage.get(shoeType)){
			_storage.get(shoeType).AddAmount(amount);
		}
	}
	/**
	 * adds the given amount to the corresponding ShoeStorageInfo's discounted amount field
	 * @param shoeType specific shoeType to add discount
	 * @param amount amount of shoes which will be discounted.
	 */
	public void addDiscount(String shoeType, int amount){
		if(_storage.get(shoeType) != null){
			_storage.get(shoeType).AddDiscount(amount);
		}
	}
	/**
	 * save the given receipt in the store
	 * @param receipt
	 */
	public void file(Receipt receipt){
		receipts.add(receipt);
	}
	/**
	 * for each item in the stock - prints its name, amount and discountedAmount
	 * for each receipt filed in the store - prints all its fields
	 */
	public void print(){
		ConcurrentHashMap<String,ShoeStorageInfo> temp=new ConcurrentHashMap<String,ShoeStorageInfo>();
		ConcurrentLinkedQueue<Receipt> tempQ = new ConcurrentLinkedQueue<Receipt>();
		// will copy the _storage and then print this copy
		synchronized(_storage){
			for (String key : _storage.keySet()) {
				temp.put(key, _storage.get(key));
			}
		}
		System.out.println();
		System.out.println();

		System.out.println("---STORE---");

		for (String key : temp.keySet()) {
			System.out.println("----------");
			System.out.println("shoe type: "+temp.get(key).getShoeType());
			System.out.println("amount on store: "+temp.get(key).getAmountOnStorage());
			System.out.println();
		}
		//will print receipt
		synchronized(receipts){

			for(Receipt r:receipts){
				tempQ.add(r);
			}

		}
		System.out.println("---RECEIPTS---");
		int num=1;
		for(Receipt r:tempQ){
			System.out.println();
			System.out.println("______receipt number :"+num+"_______");
			System.out.println();
			System.out.println("name of seller:  "+r.getSeller());
			System.out.println("name of customer: "+r.getCustomer());
			System.out.println("shoe type: "+r.getShoeType());

			System.out.print("sale ");
			if(r.isDiscount()){
				System.out.println("was in discount");
			}
			else System.out.println("wasnt in discount");
			System.out.println("IssuedTick: "+r.getIssuedTick());
			System.out.println("RequestTick: "+r.getRequestTick());
			System.out.println("numer of shoes which sold: "+r.getAmountSold());	
			num++;
			System.out.println();

		}


	}

	/** Getters - used only for testing **/
	public ShoeStorageInfo getShoe( String shoeType ) {
		return _storage.get(shoeType);
	}
	public Receipt getLastReceipt() {
		return receipts.peek();
	}
}
