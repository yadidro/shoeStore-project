package bgu.spl.app.Passive;

import java.util.concurrent.ConcurrentLinkedQueue;
import bgu.spl.app.Messages.RestockRequest;

/**
 * 
 * The Order is passive object,
 * it contains details of restockRequests that manger send to factory 
 */
public class Order {
	private ConcurrentLinkedQueue<RestockRequest> restockRequests;
	private int sentToFactory;

	/***
	 *  This constructor of Order.
	 * @param restock.
	 * @param _sentToFactory.
	 */
	public Order(RestockRequest restock, int _sentToFactory) {
		restockRequests = new ConcurrentLinkedQueue<RestockRequest>() ;
		restockRequests.add(restock);
		sentToFactory = _sentToFactory;
	}

	/***
	 * 
	 * @return how much orders manger send to factory 
	 */
	synchronized public int getAmountOfRequsted() {
		return restockRequests.size();
	}

	/***
	 * 
	 * @param restock add to list of orders to factory 
	 */
	synchronized public void addRequest(RestockRequest restock) {
		restockRequests.add(restock);
	}
	synchronized public int getSentToFactory() {
		return sentToFactory;
	}
	synchronized public void setSentToFactory(int _sentToFactory) {
		sentToFactory = _sentToFactory;
	}

	synchronized public void addToFactory(int amountToAdd){//maybe delete
		sentToFactory = sentToFactory+ amountToAdd;
	}

	synchronized public RestockRequest poll(){
		return restockRequests.poll();
	}

}