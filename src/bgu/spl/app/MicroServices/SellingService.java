package bgu.spl.app.MicroServices;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import bgu.spl.app.Messages.PurchaseOrderRequest;
import bgu.spl.app.Passive.Store;
import bgu.spl.mics.MicroService;
import bgu.spl.app.Passive.BuyResult;
import bgu.spl.app.Passive.Receipt;
import bgu.spl.app.Messages.RestockRequest;
import bgu.spl.app.Messages.TickBroadcast;


public class SellingService extends MicroService {

	private static Store store;
	private int currentTick;
	private CountDownLatch countDownLatchObject;
	final static Logger LOGGER=Logger.getLogger(SellingService.class.getName());

	public SellingService(String name,CountDownLatch latchObject, int duration) {
		super(name, duration);
		store = Store.getInstance();
		countDownLatchObject = latchObject;
	}
	
	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, tick->{
			currentTick++;
			if(duration<currentTick)
				terminate();
		}); 

		subscribeRequest(PurchaseOrderRequest.class, OrderRequest -> {
			BuyResult resultOfBuying = store.take(OrderRequest.get_ShoeType(), OrderRequest.is_OnlyDiscount()); 
			if(resultOfBuying.equals(BuyResult.NOT_IN_STOCK)){
				sendRequest(new RestockRequest(OrderRequest.get_ShoeType()),isRestock -> {
					if(isRestock) {
						store.take(OrderRequest.get_ShoeType(), OrderRequest.is_OnlyDiscount());
						Receipt ReceiptAfterRestock = new Receipt(getName(),OrderRequest.getCostumer(),OrderRequest.get_ShoeType(),OrderRequest.is_OnlyDiscount(),currentTick,OrderRequest.getRequestTick(),1);
						store.file(ReceiptAfterRestock);
						complete(OrderRequest,ReceiptAfterRestock);
					}//	public Receipt(String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick, int amountSold) {
					else {
						complete(OrderRequest,null);
					}
				});
			}
			else if(resultOfBuying.equals(BuyResult.NOT_ON_DISCOUNT))
				complete(OrderRequest,null);
			else if (resultOfBuying.equals(BuyResult.DISCOUNTED_PRICE )){ 
				Receipt receipt= new Receipt(getName(),OrderRequest.getCostumer(), OrderRequest.get_ShoeType(), true,currentTick,OrderRequest.getRequestTick(),1);
				store.file(receipt);
				complete(OrderRequest,receipt);
			}			
			//regular price
			else { 
				Receipt receipt=new Receipt(getName(),OrderRequest.getCostumer(), OrderRequest.get_ShoeType(), OrderRequest.is_OnlyDiscount(),currentTick,OrderRequest.getRequestTick(),1);
				store.file(receipt);
				complete(OrderRequest,receipt);
			}
		});

		countDownLatchObject.countDown();
	}
}
