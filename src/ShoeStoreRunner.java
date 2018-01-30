
import java.io.FileReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import bgu.spl.app.MicroServices.ManagementService;
import bgu.spl.app.MicroServices.SellingService;
import bgu.spl.app.MicroServices.ShoeFactoryService;
import bgu.spl.app.MicroServices.TimeService;
import bgu.spl.app.MicroServices.WebsiteClientService;
import bgu.spl.app.Passive.PurchaseSchedule;
import bgu.spl.app.Passive.ShoeStorageInfo;
import bgu.spl.app.Passive.Store;
import bgu.spl.app.Passive.discountSchedule;


@SuppressWarnings("restriction")
public class ShoeStoreRunner {

	public static void main(String[] args) throws InterruptedException {
		ConcurrentLinkedQueue <Thread> listOfThreads = new ConcurrentLinkedQueue <Thread>(); // add each thread of to this list and run it afterwards
		CountDownLatch CountDownToStartTimer; 
		Store store = Store.getInstance();
		
		try{
			// read the JSON file
			FileReader reader = new FileReader(args[0]);
			JSONParser jsonParser = new JSONParser();
			//read initialstorage or service
			Object obj = (JSONObject) jsonParser.parse(reader);
			JSONObject initialStorageOrServices = (JSONObject)obj;
			

			/**
			----------READ INITIAL STORAGE INFO----------
			 */

			// get an array from the JSON object
			JSONArray initialStorage= (JSONArray)initialStorageOrServices.get("initialStorage");
			ShoeStorageInfo[] shoes = new ShoeStorageInfo[initialStorage.size()];

			// take the elements of the JSON array
			for(int i=0;i<initialStorage.size();i++){
				JSONObject shoe = (JSONObject) initialStorage.get(i);
				String shoeType = (String) shoe.get("shoeType");
				int amount = (int)(long)shoe.get("amount");
				shoes[i] = new ShoeStorageInfo(shoeType,amount);
			}

			//add shoes to store
			store.load(shoes);

			/**
			----------READ SERVICES----------
			 */
			
			JSONObject services = (JSONObject)initialStorageOrServices.get("services");
			
			
			//----------TIMER----------
			
			//parse timer, don't run it yet
			JSONObject timeService = (JSONObject)services.get("time");
			int speed = (int)(long)timeService.get("speed");
			int duration = (int)(long)timeService.get("duration");
			
			
			//--------MANAGER------------
			
			List<discountSchedule> discountScheduler = new LinkedList<discountSchedule>();
			JSONObject managerservice = (JSONObject)services.get("manager");
			JSONArray discountSchedule = (JSONArray)managerservice.get("discountSchedule");
			for(int i=0;i<discountSchedule.size();i++){
				JSONObject discount = (JSONObject) discountSchedule.get(i);
				String shoeType = (String)discount.get("shoeType");
				int amount = (int)(long)discount.get("amount");
				int tick = (int)(long)discount.get("tick");
				discountScheduler.add(new discountSchedule(shoeType, amount, tick));
			}

			//get number of factories, number of sellers and number of customers for the countDownLatch
			
			int numberOfFactories = (int)(long)services.get("factories");
			int numberOfSellers = (int)(long)services.get("sellers");
			JSONArray customers = (JSONArray)services.get("customers");
			CountDownToStartTimer = new CountDownLatch (customers.size()+numberOfFactories+numberOfSellers+2); // countdownLatch!!!
			
			//----------FACTORIES----------

			for(int i=0;i<numberOfFactories;i++){
				ShoeFactoryService factory = new ShoeFactoryService("factory "+i, CountDownToStartTimer,duration);
				Thread factoryService = new Thread(factory);
				listOfThreads.add(factoryService);
			}

			//------SELLERS---------- 
			
			for(int i=0;i<numberOfSellers;i++){
				SellingService seller = new SellingService("seller "+i, CountDownToStartTimer,duration);
				Thread sellingService = new Thread(seller);
				listOfThreads.add(sellingService);
			}

			//-----CLIENTS-----------
			
			for(int r=0;r<customers.size();r++) {
				Set<String> wishList = new HashSet<String>(); //best implementation for set
				JSONObject customer = (JSONObject) customers.get(r);
				String ClientName = (String)customer.get("name");
				JSONArray wishes = (JSONArray)customer.get("wishList");
				for(int j=0;j< wishes.size();j++){
					wishList.add((String)wishes.get(j));
				}
				JSONArray purchases = (JSONArray)customer.get("purchaseSchedule");
				List<PurchaseSchedule> PurchaseScheduler  = new LinkedList<PurchaseSchedule>();
				for(int k=0;k<purchases.size();k++){

					JSONObject shoe = (JSONObject) purchases.get(k);
					String ShoeType = (String)shoe.get("shoeType");
					int tick = (int)(long)shoe.get("tick");
					PurchaseScheduler.add(new PurchaseSchedule(ShoeType,tick));
				}
				//create client Thread 
				//System.out.println("shoeRunner: number of wishes for "+ClientName+" is "+wishes.size());
				WebsiteClientService nameOfClient = new WebsiteClientService(ClientName,PurchaseScheduler,wishList, CountDownToStartTimer,duration);
				Thread  clientService=new Thread(nameOfClient);
				listOfThreads.add(clientService);
			}
			
			
			//create new thread of manager and run it
			ManagementService manager = new ManagementService("manager",discountScheduler, CountDownToStartTimer,duration);
			Thread managerService = new Thread(manager);
			listOfThreads.add(managerService);
			//create timer thread, don't run it yet until 
			
			TimeService timer = new TimeService("timer",speed,duration, CountDownToStartTimer);
			Thread timerService = new Thread(timer);
			listOfThreads.add(timerService);
			
			
			//startAllThreads:
			for(Thread t:listOfThreads){
				t.start();
			}
			
			//  when all threads end to run store will print 
			for(Thread t:listOfThreads)
				t.join();
			store.print();
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
	}
}