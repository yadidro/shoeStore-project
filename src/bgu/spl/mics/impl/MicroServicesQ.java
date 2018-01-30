package bgu.spl.mics.impl;

import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
/***
 * 
 * The MicroServicesQ is object that help us organize micro-service that handler request in  Round-Robin fashion.
 * the head of orderDiFacto is the microservice who will get the next request.
 * the head of orderByJoiningTime is the oldest microservice who got the request
 */
public class MicroServicesQ {
	private ConcurrentLinkedQueue<MicroService> orderDiFacto;
	private ConcurrentLinkedQueue<MicroService> orderByJoiningTime;
/***
 *  The contractor of MicroServicesQ.
 * @param _type is type of request that micro-service will handle.
 */
	@SuppressWarnings("rawtypes")
	public MicroServicesQ(Class<? extends Request> _type){
		orderDiFacto = new ConcurrentLinkedQueue<MicroService>();
		orderByJoiningTime = new ConcurrentLinkedQueue<MicroService>();
	}

	/***
	 * 
	 * @param m is added to list of micro-service that will handle this type of request.
	 */
	public synchronized void subscribe(MicroService m){
		if(orderByJoiningTime.isEmpty()){
			orderByJoiningTime.add(m);
			orderDiFacto.add(m);
		}
		else{
			if(! orderByJoiningTime.contains(m)){
				MicroService last = lastMicroByTime(); // last microservice that subscribed to request "? extends Request"
				orderByJoiningTime.add(m); //add m to orderByJoiningTime and make it the new last microservice
				addToOrderDiFactoAfterOldLast(last , m);//add MicroService m after MicroService last
			
			}
		}
	}
	/**
	 * search for oldLast m and add newLast after it.
	 * create new temporary queue. pop elements from DiFacto and move them to tempQ, until we popped oldLast.than add newlast and to tempQ and continue to move all the other elements.
	 * @param oldLast 	last micro-service that handled this type of request before exeuting this method.
	 * @param newLast 	new last will add after oldLast, and be recognized as the new last element that joined this queue.
	 */
	private synchronized void addToOrderDiFactoAfterOldLast(MicroService oldLast,MicroService newLast ) {
		

		ConcurrentLinkedQueue<MicroService> TempQ = new ConcurrentLinkedQueue<MicroService>();
		MicroService micro;
		while (!(orderDiFacto.isEmpty())){
			micro = orderDiFacto.poll();
			TempQ.add(micro);
			if (micro.getName() == oldLast.getName() ) 
				TempQ.add(newLast);
		}	
		while (!TempQ.isEmpty())
			orderDiFacto.add(TempQ.poll());
	}

	/***
	 * move each microservice to temporary q until last microservice is retreaved, than return all the element to the q;
	 * @return last element in list orderByJoiningTime
	 */
	public synchronized MicroService lastMicroByTime(){ 
		MicroService last = null;
		ConcurrentLinkedQueue<MicroService> tempQ = new ConcurrentLinkedQueue<MicroService>();
		while( !(orderByJoiningTime.isEmpty() )){
			last = orderByJoiningTime.poll();
			tempQ.add(last);
		}
		while( !(tempQ.isEmpty() )){
			orderByJoiningTime.add(tempQ.poll());
		}
		return last;
	}

	public boolean isEmpty() {
		return orderDiFacto.isEmpty();
	}

	public MicroService poll() {
		return orderDiFacto.poll();
	}

	public MicroService peek() {
		return orderDiFacto.peek();
	}

	public void addToTailOfOrderDiFacto(MicroService m){
		orderDiFacto.add(m);
	}

	public void remove(MicroService m) {
		orderDiFacto.remove(m);
		orderByJoiningTime.remove(m);
	}
/***
 * 	move each microservice to temporary q until last microservice is retreaved, than return all the element to the q;
 * @return last element in list orderDiFacto
 */
	public synchronized MicroService lastMicroInDiFacto(){ 
		MicroService last = null;
		ConcurrentLinkedQueue<MicroService> tempQ = new ConcurrentLinkedQueue<MicroService>();
		while( !(orderDiFacto.isEmpty() )){
			last = orderDiFacto.poll();
			tempQ.add(last);
		}
		while( !(tempQ.isEmpty() )){
			orderDiFacto.add(tempQ.poll());
		}
		return last;
	}

/**
 * return true if orderDiFacto contains m
 */
	public synchronized boolean contains(MicroService m){
		return orderDiFacto.contains(m);
	}

}
