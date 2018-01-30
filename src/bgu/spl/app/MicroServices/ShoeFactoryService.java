package bgu.spl.app.MicroServices;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;
import bgu.spl.app.Messages.ManufacturingOrderRequest;
import bgu.spl.app.Messages.TickBroadcast;
import bgu.spl.app.Passive.Receipt;
import bgu.spl.mics.MicroService;

/**
 * The ShoeFactoryService is active object that extend MicroService.
 * <p>
 * This micro-service describes a shoe factory that manufacture shoes for the store. 
 * This micro-service handles the ManufacturingOrderRequest it takes it exactly 1 tick to manufacture a single shoe.
 *
 */
public class ShoeFactoryService extends MicroService {
	private LinkedBlockingDeque<ManufacturingOrderRequest> queueOfOrders;
	private Boolean first;// use to indicate if order first in the queue.
	private int currentTick;
	private int done=0;// use to know if factory done with order.
	private CountDownLatch countDownLatchObject;
	final static Logger LOGGER=Logger.getLogger(ShoeFactoryService.class.getName());


	/***
	 * This constructor of ShoeFactoryService.
	 * @param name of the factory 
	 * @param latchObject we use it for countDownLatch in Runner. 
	 * @param duration is tick when every thing will shutdown.
	 */
	public ShoeFactoryService(String name,CountDownLatch latchObject, int duration) {
		super(name, duration);
		queueOfOrders=new LinkedBlockingDeque<ManufacturingOrderRequest>();	
		first=true;
		countDownLatchObject = latchObject;
	}

	@Override
	protected void initialize() {

		//if factory receive ManufacturingOrderRequest this request will be add to queue of orders . 
		subscribeRequest(ManufacturingOrderRequest.class, newOrder ->{
			queueOfOrders.add(newOrder); // add new order to queueOfOrders
		});

		/**
		 * Subscribes to TickBroadcast  in order to receive tick and when will achieve duration+1 will shutdown.
		 * Every tick factory pop order from queue of orders (if isn't empty).
		 * Factory check if it made all shoes in this order,
		 * if not factory produce one new shoe and update time that need to finish, then push this order back to queue.
		 * if factory done with this order, then factory complete the request with receipt and check if queue is empty.
		 * In case queue is empty update first=true (indicate that next order will be first )in propose that will produce this order in next tick that factory receive order
		 * Otherwise it pop next order, produce a new one shoe from this order and update time that need to finish, then push this order back to queue.
		 */
		subscribeBroadcast(TickBroadcast.class, broad->{	
			currentTick++;
			if(duration<currentTick){
				terminate();
			}
			if(!queueOfOrders.isEmpty()){

				ManufacturingOrderRequest order = queueOfOrders.pollFirst();

				// check if factory receive this order tick before , if so will update first in propose that will produce this order in currentTick
				if(order.get_requestTick() < currentTick)
					first=false;

				if(!first){	
					if(order.get_workTime()==done){
						complete(order,new Receipt( getName(), "manager", order.get_shoeType(),false, currentTick, order.get_requestTick(),
								order.get_amount()));
						if(queueOfOrders.isEmpty()){
							first=true;
						}
						else {
							order=queueOfOrders.pollFirst();
							order.update_workTime();
							queueOfOrders.addFirst(order);
						}

					}
					else{
						order.update_workTime();
						queueOfOrders.addFirst(order);
					}

				}
				else{
					first=false;
					queueOfOrders.addFirst(order);
				}
			}

		});
		countDownLatchObject.countDown();
	}
}