package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import bgu.spl.app.Messages.PurchaseOrderRequest;
import bgu.spl.app.Messages.RestockRequest;
import bgu.spl.app.Messages.TickBroadcast;
import bgu.spl.app.MicroServices.ManagementService;
import bgu.spl.app.MicroServices.SellingService;
import bgu.spl.app.MicroServices.ShoeFactoryService;
import bgu.spl.app.MicroServices.WebsiteClientService;
import bgu.spl.app.Passive.PurchaseSchedule;
import bgu.spl.app.Passive.Receipt;
import bgu.spl.app.Passive.discountSchedule;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.RequestCompleted;

public class MessageBusImplTest {
	private Set<String> wishList = new HashSet<String>();
	private List<discountSchedule> listOfDiscount = new LinkedList<discountSchedule>();
	private List<PurchaseSchedule> listOfPurchases = new LinkedList<PurchaseSchedule>();	
	private MessageBusImpl bus = MessageBusImpl.getInstance(); 
			//public WebsiteClientService(String name,List<PurchaseSchedule> PurchaseScheduler,Set<String> wishList,CountDownLatch latchObject, int duration) {

			
	@Test
	public  void testSubscribeRequest() throws InterruptedException {
		MicroService seller = new SellingService("SalesMan",new CountDownLatch(1),  1);
		MicroService BOSS = new ManagementService("boss",listOfDiscount,new CountDownLatch(1),  1);
		bus.register(seller);
		bus.register(BOSS);
		RestockRequest restockReq = new RestockRequest("StrongShoe");
		bus.subscribeRequest(restockReq.getClass(), BOSS);
		bus.sendRequest(restockReq, seller);
		Message message = bus.awaitMessage(BOSS);
		bus.unregister(BOSS);
		bus.unregister(seller);
		assertEquals(restockReq,message);
	}
	@Test
	public void testSubscribeBroadcast() throws InterruptedException {
		MicroService Mordor = new SellingService("Mordor",new CountDownLatch(1),  1);
		bus.register(Mordor);
		TickBroadcast tick = new TickBroadcast(15);
		bus.subscribeBroadcast(tick.getClass(), Mordor);
		bus.sendBroadcast(tick);
		Message tickMessage = bus.awaitMessage(Mordor);
		assertEquals(tickMessage,tick);
	}
	//client send request to seller, seller complete request with receipt.
	@SuppressWarnings("rawtypes")
	@Test
	public  void testComplete() throws InterruptedException {

		MicroService CrazySeller = new SellingService("SalesMan",new CountDownLatch(1),  1);
		MicroService superClient = new WebsiteClientService("client", listOfPurchases , wishList, new CountDownLatch(1), 1);
		bus.register(CrazySeller);
		bus.register(superClient);
		
		PurchaseOrderRequest purchase = new PurchaseOrderRequest("FrodoShoes",false, "Mordor",1);
		bus.subscribeRequest(purchase.getClass(), CrazySeller);
		bus.sendRequest(purchase, superClient);
		
		Receipt receiptOfBuying = new Receipt("SalesMan", "Mordor", "FrodoShoes", false,  1, 1, 1);
		bus.complete(purchase, receiptOfBuying);
		Message reqCompleted = bus.awaitMessage(superClient);
		bus.unregister(superClient);
		bus.unregister(CrazySeller);
		assertEquals(receiptOfBuying, ((RequestCompleted)reqCompleted).getResult());
	}

	@Test  //check that broadcast sent is getting to each MicroService that subscribed to it.
	public void testSendBroadcast() throws InterruptedException {
		TickBroadcast tickTock = new TickBroadcast(15);
		MicroService Werthaimer = new SellingService("Werthaimer",new CountDownLatch(1),  1);
		bus.register(Werthaimer);
		bus.subscribeBroadcast(tickTock.getClass(), Werthaimer);
		
		MicroService Bosit = new ManagementService("Bosit",listOfDiscount,new CountDownLatch(1),  1);
		bus.register(Bosit);
		bus.subscribeBroadcast(tickTock.getClass(), Bosit);
		
		MicroService BlackFactory = new ShoeFactoryService( "BlackFactory",new CountDownLatch(1), 1);
		bus.register(BlackFactory);
		bus.subscribeBroadcast(tickTock.getClass(), BlackFactory);
		
		MicroService StupidClient = new WebsiteClientService("StupidClient", listOfPurchases , wishList, new CountDownLatch(1), 1);
		bus.register(StupidClient);
		bus.subscribeBroadcast(tickTock.getClass(), StupidClient);
		bus.sendBroadcast(tickTock);
		
		Message sellerMessage = bus.awaitMessage(Werthaimer);
		Message managerMessage = bus.awaitMessage(Bosit);
		Message factoryMessage = bus.awaitMessage(BlackFactory);
		Message clientMessage = bus.awaitMessage(StupidClient);
		
		bus.unregister(Werthaimer);
		bus.unregister(StupidClient);
		bus.unregister(BlackFactory);
		bus.unregister(Bosit);
		
		assertTrue(sellerMessage == managerMessage && managerMessage == factoryMessage && factoryMessage == clientMessage);
	}

	@Test //test roundRobin, and confirm that the request that was sent is similar to request that was polled from the messageQueue.
	public  void testSendRequest() throws InterruptedException {
		MicroService seller1 = new SellingService("SalesMan1",new CountDownLatch(1),  1);
		MicroService seller2 = new SellingService("SalesMan2",new CountDownLatch(1),  1);
		MicroService KakaBoss = new ManagementService("boss",listOfDiscount,new CountDownLatch(1),  1);
		
		bus.register(seller1);
		bus.register(seller2);
		bus.register(KakaBoss);
		
		RestockRequest restockReq1 = new RestockRequest("NallBait1");
		RestockRequest restockReq2 = new RestockRequest("NallBait2");
		bus.subscribeRequest(restockReq1.getClass(), KakaBoss);
		
		bus.sendRequest(restockReq1, seller1);
		bus.sendRequest(restockReq2, seller2);
		Message resMessage1 = bus.awaitMessage(KakaBoss);
		Message resMessage2 = bus.awaitMessage(KakaBoss);
		
		bus.unregister(KakaBoss);
		bus.unregister(seller2);
		bus.unregister(seller1);
		
		assertTrue(restockReq1 == resMessage1 && restockReq2 == resMessage2);
	}

	@Test
	public void testRegister() {
		MicroService Bibi = new ManagementService("SellingLies",listOfDiscount,new CountDownLatch(1),  1);
		assertFalse(bus.isRegisterd(Bibi));
		bus.register(Bibi);
		assertTrue(bus.isRegisterd(Bibi));
		bus.unregister(Bibi);
	}

	@Test
	public void testUnregister() {
		MicroService fac = new ShoeFactoryService( "fac",new CountDownLatch(1), 1);
		bus.register(fac);
		assertTrue(bus.isRegisterd(fac));
		bus.unregister(fac);
		assertFalse(bus.isRegisterd(fac));	
	}

	@Test 
	/**
	 * checks that a message that was sent, is delivered to the right address.
	 */
		
	public void testAwaitMessage() throws InterruptedException {
		MicroService Buji = new ManagementService("ZifZif",listOfDiscount,new CountDownLatch(1),  1);

		bus.register(Buji);
		TickBroadcast tickTack = new TickBroadcast(37);
		bus.subscribeBroadcast(tickTack.getClass(), Buji);
		bus.sendBroadcast(tickTack);
		Message msg = bus.awaitMessage(Buji);
		assertEquals(tickTack,msg);
	}

}
