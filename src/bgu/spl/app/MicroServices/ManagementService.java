package bgu.spl.app.MicroServices;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.app.Messages.TickBroadcast;
import bgu.spl.app.Messages.RestockRequest;
import bgu.spl.app.Messages.ManufacturingOrderRequest;
import bgu.spl.app.Messages.NewDiscountedBroadcast;
import bgu.spl.app.Passive.Order;
import bgu.spl.app.Passive.Store;
import bgu.spl.app.Passive.discountSchedule;
import bgu.spl.mics.MicroService;
/**
 *The ManagementService is active object that extend MicroService
 *This micro-service can add discount to shoes in the store and send NewDiscountBroadcast to notify clients about them.
 *In addition, the ManagementService handles RestockRequests that is being sent by the SellingService.
 *it will send a ManufacturingOrderRequest to ShoeFactoryService.
 */
public class ManagementService extends MicroService{
	private ConcurrentHashMap<Integer,ConcurrentLinkedQueue<discountSchedule>> _listOfDiscounted;
	private static Store store;
	private ConcurrentHashMap<String,Order> orderToFactory;
	private CountDownLatch countDownLatchObject;
	private int currentTick;
	private int empty=0;// use to compare if number of shoe is bigger then 0
	final static Logger LOGGER=Logger.getLogger(ManagementService.class.getName());

	/***
	 *  This constructor of ManagementService
	 * @param name of manger
	 * @param listOfDiscount is list that contain: shoeType and tick that manger will give discount to this type.
	 * @param latchObject we use it for countDownLatch in Runner. 
	 * @param duration is tick when every thing will shutdown.
	 */
	public ManagementService(String name, List<discountSchedule> listOfDiscount,CountDownLatch latchObject, int duration ) { // not sure we're alowed to do it. need to get LINKEDLIST without generic!
		super("manager", duration);
		_listOfDiscounted = new ConcurrentHashMap<Integer,ConcurrentLinkedQueue<discountSchedule>>();

		for (discountSchedule discount : listOfDiscount) {
			if(!_listOfDiscounted.containsKey(discount.getTick())){
				ConcurrentLinkedQueue<discountSchedule> discount_Schedule=new ConcurrentLinkedQueue<discountSchedule>();
				_listOfDiscounted.put(discount.getTick(), discount_Schedule);
			}
			_listOfDiscounted.get(discount.getTick()).add( discount);
		}
		countDownLatchObject = latchObject;

		store = Store.getInstance();
		orderToFactory=new ConcurrentHashMap<String,Order>();
	}

	@Override
	protected synchronized void initialize() {
		subscribeBroadcast(TickBroadcast.class, tickBroadcast->{
			currentTick++;
			if(duration<currentTick){
				terminate();
			}
			
			if(_listOfDiscounted.containsKey(tickBroadcast.getCurrentTick())){
				for (discountSchedule discount : _listOfDiscounted.get(tickBroadcast.getCurrentTick())) {
					synchronized(this){
						sendBroadcast(new NewDiscountedBroadcast(discount.getShoeType()));
						synchronized(store){
							store.addDiscount(discount.getShoeType(), discount.getAmount());
						}
					}
				}
			}
		});

		/**
		 * what to do when getting RestockRequest:
		 * 
		 */
		subscribeRequest(RestockRequest.class, restock -> { 
			synchronized(this){
		
				/**
				 * if manager until now didn't receive restock request for this type then he will make a new one order to factory 
				 * when he receive order from factory he will update the store,  file the receipt , and a sell will be finish .  
				 */
				if( ! orderToFactory.containsKey( restock.get_ShoeType() ) ) {
					boolean success=true;// use for indicated if manger success to make order to factory
					orderToFactory.put(restock.get_ShoeType(), new Order( restock, (currentTick%5) + 1 ) );
					success=sendRequest(new ManufacturingOrderRequest(restock.get_ShoeType(),(currentTick%5)+1,currentTick),receipt->{
						//what to do when getting requestCompleted of ManufacturingOrderRequest
						if(receipt == null) System.out.println("receipt is null!!!!!!!!!");
						if(receipt.getShoeType()== null)System.out.println("receipt.getShoeType() is null!!!!!!!!!");
						store.add(receipt.getShoeType(), receipt.getAmountSold());
						store.file(receipt);
						int amountToSell = receipt.getAmountSold();
						while(orderToFactory.get(receipt.getShoeType()).getAmountOfRequsted()>empty && amountToSell >empty){
							//System.out.println("ManagamentService1: complete restock request for "+receipt.getCustomer());
							complete(orderToFactory.get(receipt.getShoeType()).poll(),true);
						}
						orderToFactory.get(restock.get_ShoeType()).setSentToFactory(orderToFactory.get(restock.get_ShoeType()).getSentToFactory()-receipt.getAmountSold());

					});
					if (!success){
						complete(restock,false);
						
					}
				}
				/**
				 * if he receive before restock request for this type then and he will  wait to get the order
				 * but before he will check if amount of order is enough,
				 * in case is not enough he will add a new one  order of this shoe type .
				 * when he receive order from factory he will update the store,  file the receipt , and a sell will be finish .
				 */
				else{

					String ShoeToRestock = restock.get_ShoeType();
					Order order = orderToFactory.get(restock.get_ShoeType());
					int AmountOfRequsted = order.getAmountOfRequsted();
					int SentToFactory = order.getSentToFactory();

					if(AmountOfRequsted+1 <= SentToFactory){ //if AmountOfRequsted +1 is smaller then SentToFactory -> just update order(AmountOfRequsted +1)
						orderToFactory.get(ShoeToRestock).addRequest(restock);
					}
					// if AmountOfRequsted +1 is bigger then SentToFactory -> update order and send Request
					else {
						int amountToAddToFactory = (currentTick%5)+1;
						boolean success=true;// use for indicated if manger success to make order to factory
						orderToFactory.get(ShoeToRestock).addToFactory(amountToAddToFactory);
						orderToFactory.get(ShoeToRestock).addRequest(restock);
						success=sendRequest(new ManufacturingOrderRequest(restock.get_ShoeType(),(currentTick%5)+1,currentTick),receipt->{//what to do when getting requestCompleted of ManufacturingOrderRequest
							store.add(receipt.getShoeType(), receipt.getAmountSold());
							store.file(receipt);
							int amountToSell = receipt.getAmountSold();

							while(orderToFactory.get(receipt.getShoeType()).getAmountOfRequsted()>empty && amountToSell > empty){
								complete(orderToFactory.get(receipt.getShoeType()).poll(),true);
							}
							orderToFactory.get(restock.get_ShoeType()).setSentToFactory(orderToFactory.get(restock.get_ShoeType()).getSentToFactory()-receipt.getAmountSold());

						});	
						if (!success){
							complete(restock,false);
							
						}
					}
				}
			}
		});
		countDownLatchObject.countDown();
	}
}



