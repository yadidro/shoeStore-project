package bgu.spl.mics.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.IllegalStateException;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

/**
 * create new static instance of MessageBus
 */
public class MessageBusImpl implements MessageBus{

	private ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>> messageQueues; 
	private ConcurrentHashMap<Class<? extends Broadcast>,ConcurrentLinkedQueue<MicroService>> subscribeToBroadcast; 
	@SuppressWarnings("rawtypes")
	private ConcurrentHashMap<Class<? extends Request>,MicroServicesQ> subscribeToRequest;
	private ConcurrentHashMap<Request<?>,MicroService> Requesters;
	private ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Class<? extends Broadcast>>> BroadcastsByMicroServices; 
	@SuppressWarnings("rawtypes")
	private ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Class<? extends Request>>> RequestsByMicroServices;

	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	/**
	 * The constructor of MessageBusImpl.
	 */
	@SuppressWarnings("rawtypes")
	private MessageBusImpl(){
		messageQueues = new ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>>();
		subscribeToBroadcast = new ConcurrentHashMap<Class<? extends Broadcast>,ConcurrentLinkedQueue<MicroService>>();
		subscribeToRequest = new ConcurrentHashMap<Class<? extends Request>,MicroServicesQ>();
		Requesters = new ConcurrentHashMap<Request<?>,MicroService>();
		BroadcastsByMicroServices = new ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Class<? extends Broadcast>>>();
		RequestsByMicroServices = new ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Class<? extends Request>>>();
	}

	/**
	 * 
	 * @return the only instance of singleton MessageBusImpl.
	 */
	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		synchronized(subscribeToRequest){
			if(!subscribeToRequest.containsKey(type)){ // if this kind of request doesn't exist
				MicroServicesQ ReqToAdd= new MicroServicesQ(type); //create new list which will contains all the microservice which subscribed to it.
				ReqToAdd.subscribe(m);
				subscribeToRequest.put(type, ReqToAdd);
			}
			else{

				MicroServicesQ q = subscribeToRequest.get(type);
				q.subscribe(m);
			}
			//add this request to RequestsByMicroServices so we will be able to delete it when necrassary
			if (RequestsByMicroServices.containsKey(m))
				(RequestsByMicroServices.get(m)).add(type);//add request name to this MicroService list
			else{ // means this micro doesn't have requests, so we need to create new list
				ConcurrentLinkedQueue<Class<? extends Request>> tempList = new ConcurrentLinkedQueue<Class<? extends Request>>();
				tempList.add(type);
				RequestsByMicroServices.put(m, tempList);
			}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized(subscribeToBroadcast){
			//add microservice to BroadcastTable of speciefic broadcast
			if(!subscribeToBroadcast.containsKey(type)){ // if this kind of broadcast never been used before,			
				ConcurrentLinkedQueue<MicroService> BroadToAdd= new ConcurrentLinkedQueue<MicroService>(); //create new list which contains all the microservice which subscribed to it.
				subscribeToBroadcast.put(type, BroadToAdd);
			}
			subscribeToBroadcast.get(type).add(m);

			//add this broadcast to RequestsByMicroServices so we will be able to delete it when necrassary
			if (BroadcastsByMicroServices.containsKey(m)) //this micro has already has broadcasts
				(BroadcastsByMicroServices.get(m)).add(type);//add broadcast name to this MicroService list
			else{ // means this micro doesn't have broadcast, so we need to create new list
				ConcurrentLinkedQueue<Class<? extends Broadcast>> tempList = new ConcurrentLinkedQueue<Class<? extends Broadcast>>();
				tempList.add(type);
				BroadcastsByMicroServices.put(m, tempList);
			}
		}
	}

	@Override
	public synchronized <T> void complete(Request<T> r, T result) {

		RequestCompleted<T> completed = new RequestCompleted<T>(r,result);
		messageQueues.get(Requesters.get(r)).add(completed);
		Requesters.remove(r);

	}

	@Override
	public synchronized void sendBroadcast(Broadcast b) {
		//pop each microservice in the queue(of microservices sbscribed to this brodcast), add the broadcast to his MessageQueue and return the microservice to the queue.
		ConcurrentLinkedQueue<MicroService> listOfMicroservice = subscribeToBroadcast.get(b.getClass());
		if(listOfMicroservice != null){
			for(int i=0; i<listOfMicroservice.size();i++){
				MicroService tempMicroService = listOfMicroservice.remove();
				messageQueues.get(tempMicroService).add(b); //add b to messageQueue as Message. somehow need to be popped as Broadcast afterwards
				listOfMicroservice.add(tempMicroService);
			}
		}
	}

	@Override 
	public synchronized boolean sendRequest(Request<?> r, MicroService requester) {
		MicroServicesQ listOfMicroservice = subscribeToRequest.get(r.getClass()); // get list of microservices who are interested in this speciefic request
		if (listOfMicroservice !=null) { // return false if no-one asked for this kind of request
			//pop first element in the queue, add the request to his MessageQueue, and return it to the tail of the queue.
			MicroService tempMicroService = listOfMicroservice.poll(); //pop first MicroService in the DiFacto queue
			if(messageQueues.get(tempMicroService) == null) System.out.println("map_of_micro_queue.get(tempMicroService) is null!");
			messageQueues.get(tempMicroService).add(r);
			listOfMicroservice.addToTailOfOrderDiFacto(tempMicroService); //relocate the microservice in DiFactoList
			//add to map_of_who_send_request:
			synchronized(Requesters){
				Requesters.put(r, requester);
			}
			return true;
		}
		return false; 

	}
	@Override
	public synchronized void register(MicroService m) {
		LinkedBlockingQueue<Message> MessageQ=new LinkedBlockingQueue<Message>();
		messageQueues.put(m,MessageQ);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public synchronized void unregister(MicroService m) {
		//remove from SubscribeToBroadcast
		ConcurrentLinkedQueue<Class<? extends Broadcast>> ListOfBroadcastOf_m =  BroadcastsByMicroServices.get(m);
		if(ListOfBroadcastOf_m!=null){
			while(!ListOfBroadcastOf_m.isEmpty()){ // remove from map_of_subscribe_broadcast
				Class<? extends Broadcast> DeleteFromBroadcast = ListOfBroadcastOf_m.poll();
				(subscribeToBroadcast.get(DeleteFromBroadcast)).remove(m);
			}
		}
		BroadcastsByMicroServices.remove(m);

		//remove from subscribeToRequest 
		ConcurrentLinkedQueue<Class<? extends Request>> RequestsOfMicroSerivice =  RequestsByMicroServices.get(m);
		if(RequestsOfMicroSerivice!=null){
			while(!RequestsOfMicroSerivice.isEmpty()){ 
				Class<? extends Request> DeleteFromRequest = RequestsOfMicroSerivice.remove();
				(subscribeToRequest.get(DeleteFromRequest)).remove(m); // remove from map_of_subscribe_request
				Requesters.remove(DeleteFromRequest);//remove from requesters map, if MessageName isn't there, it does nothing.
				if(Requesters.containsKey(DeleteFromRequest)) System.out.println("MessageBus: something is wrong");
			}
		}
		RequestsByMicroServices.remove(m);

		//remove from MessageQueues
		messageQueues.remove(m);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized(m){
			BlockingQueue<Message> QueueToTakeMessage = messageQueues.get(m); // return null if m isn't a key
			if (QueueToTakeMessage == null) throw new IllegalStateException(m.getName()+" is not registered");
			Message l=QueueToTakeMessage.take();
			return l;
		}
	}

	public synchronized boolean isRegisterd(MicroService m){
		return messageQueues.containsKey(m); 
	}

}