package bgu.spl.app.MicroServices;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.app.Messages.NewDiscountedBroadcast;
import bgu.spl.app.Messages.PurchaseOrderRequest;
import bgu.spl.app.Messages.TickBroadcast;
import bgu.spl.app.Passive.PurchaseSchedule;
import bgu.spl.app.Passive.Wish;
import bgu.spl.mics.MicroService;
/**
 * The WebsiteClientService is active object that extend MicroService.
 * <p>
 * This micro service describes one client connected to the web-site.
 */
public class WebsiteClientService extends MicroService{
	private int currentTick;
	private ConcurrentHashMap<Integer,ConcurrentLinkedQueue<PurchaseSchedule>> purchaseScheduleList;
	private ConcurrentHashMap<String,Wish> _wishList;
	private CountDownLatch countDownLatchObject;
	final static Logger LOGGER=Logger.getLogger(WebsiteClientService.class.getName());

	/**
	 * The WebsiteClientService is active object that extend MicroService.
	 * <p>
	 * This micro service describes one client connected to the web-site.
	 */
	public WebsiteClientService(String name,List<PurchaseSchedule> PurchaseScheduler,Set<String> wishList,CountDownLatch latchObject, int duration) {
		super(name, duration);
		purchaseScheduleList=new ConcurrentHashMap<Integer,ConcurrentLinkedQueue<PurchaseSchedule>>();
		_wishList=new ConcurrentHashMap<String,Wish>();
		for (String wish1 : wishList){
			_wishList.put(wish1,new Wish());
		}
		countDownLatchObject=latchObject;

		// add purchases from input List to purchaseScheduleList
		for (PurchaseSchedule purchase : PurchaseScheduler){			
			if(!purchaseScheduleList.containsKey(purchase.getTick())){
				ConcurrentLinkedQueue<PurchaseSchedule> purchaseList=new ConcurrentLinkedQueue<PurchaseSchedule>();
				purchaseScheduleList.put(purchase.getTick(), purchaseList);
			}
			purchaseScheduleList.get(purchase.getTick()).add(purchase);
		}
	}


	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class,tick->{ 
			
			currentTick++;
			if(duration<currentTick){
				terminate();
		
			}
			/*
			 * Every tick that client receive we check if in this tick client has shoe type that he want to purchase
			 * if he dose then we send sendRequestd and request will be without discount(false)
			 * and then remove this tick from purchaseScheduleList
			 */
			if(purchaseScheduleList.containsKey(tick.getCurrentTick())) {
				 
				//pop all the "PurchaseSchedule" from the queue, and send PurchaseOrderRequest by its data.
				int sizeList=purchaseScheduleList.get(tick.getCurrentTick()).size();
				for(int i=0;i<sizeList;i++){
					PurchaseSchedule purchaseSchedule = purchaseScheduleList.get(tick.getCurrentTick()).poll();//poll first purchaseSchedule and create PurchaseOrderRequest by its data.
					PurchaseOrderRequest purchaseOrder = new PurchaseOrderRequest(purchaseSchedule.getShoeType(), false, getName(), currentTick);
					String type=purchaseOrder.get_ShoeType();
					purchaseScheduleList.get(tick.getCurrentTick()).add(purchaseSchedule); //return purchaseSchedule to list. delete it only when return.
					sendRequest(purchaseOrder,receipt->{

						if(receipt!=null){
							//search shoe to remove
							boolean found=false;
							PurchaseSchedule p;
							ConcurrentLinkedQueue<PurchaseSchedule> queueOfPurchaseSchedule = purchaseScheduleList.get(receipt.getRequestTick());

							synchronized(queueOfPurchaseSchedule){
								//checking twice to make sure queueOfPurchaseSchedule is really exist.
								if(!queueOfPurchaseSchedule.isEmpty() ){
									while(!found){
										p = queueOfPurchaseSchedule.poll();
										if(p.getShoeType() == receipt.getShoeType())
											found = true;
										else queueOfPurchaseSchedule.add(p);
									}
								}
								if(queueOfPurchaseSchedule.isEmpty()) purchaseScheduleList.remove(receipt.getRequestTick());
							}
						}
						else {
							for(PurchaseSchedule purchase:purchaseScheduleList.get(tick.getCurrentTick()) ){
								if(purchase.getShoeType().equals(type)){
									purchaseScheduleList.get(tick.getCurrentTick()).remove(purchase);
								}
								
							}
							
						}
					});

				}
			}
			if(purchaseScheduleList.isEmpty() && _wishList.isEmpty()){
				terminate();
			}
		});

		/*
		 * Subscribes to NewDiscountedBroadcast in order to receive message of discount 
		 * in case than in wish list the client has this type he will send request on  discount (setSent(true))
		 * if client succeed to purchase on discount this type will be remove from _wishList
		 */
		subscribeBroadcast(NewDiscountedBroadcast.class, discount->{
			if(_wishList.containsKey(discount.getShoeType())){
				if(!_wishList.get(discount.getShoeType()).isSent()){
					_wishList.get(discount.getShoeType()).setSent(true);
					sendRequest(new PurchaseOrderRequest(discount.getShoeType(),true, getName(), currentTick),receipt->{
						if(receipt != null) {
							_wishList.remove(discount.getShoeType());

						}
						else {
							_wishList.get(discount.getShoeType()).setSent(false);
						}
					});
				}
			}
		});

		
		countDownLatchObject.countDown();
	}

}
