package bgu.spl.app.Passive;

import static org.junit.Assert.*;
import org.junit.Test;

public class StoreTest {

	private static Store store;		
	
	@Test// using get Method
	public void testLoad() {
		store = Store.getInstance();
		ShoeStorageInfo shoe = new ShoeStorageInfo( "blundstone", 5);
		ShoeStorageInfo[] shoes = {shoe};
		store.load(shoes);
		assertEquals(store.getShoe("blundstone"),shoe );
	}
	@Test// not using get Method, represents load method "clean" the store from previous shoes.
	public void testLoad2() {
		store = Store.getInstance();
		ShoeStorageInfo shoe1 = new ShoeStorageInfo( "kakamaika", 5);
		ShoeStorageInfo[] shoes = {shoe1};
		store.load(shoes);
		ShoeStorageInfo shoe2 = new ShoeStorageInfo( "sportShoe", 1);
		ShoeStorageInfo[] shoes2 = {shoe2};
		store.load(shoes2);
		
		assertTrue(store.take("kakamaika", false) == BuyResult.NOT_IN_STOCK );
	}

	@Test //taking a shoe without discount 
	public void testTake1() {
		store = Store.getInstance();
		ShoeStorageInfo shoe = new ShoeStorageInfo( "fakeShoe", 5);
		ShoeStorageInfo[] shoes = {shoe};
		store.load(shoes);
		assertEquals(store.take("fakeShoe", false),BuyResult.REGULAR_PRICE);
	}
	@Test //taking a shoe without discount will not result in returning DISCOUNTED_PRICE, in case this shoe wasnt discounted.
	public void testTake2() {
		store = Store.getInstance();
		ShoeStorageInfo shoe = new ShoeStorageInfo( "Diadora", 5);
		ShoeStorageInfo[] shoes = {shoe};
		store.load(shoes);
		assertFalse(store.take("Diadora", false) == BuyResult.DISCOUNTED_PRICE);
	}
	
	@Test // taking a shoe with discount will result in DISCOUNTED_PRICE
	public void testTake3() {
	store = Store.getInstance();
	ShoeStorageInfo shoe = new ShoeStorageInfo( "Adiddas", 1);
	shoe.AddDiscount(1);
	ShoeStorageInfo[] shoes = {shoe};
	store.load(shoes);
	assertTrue(store.take("Adiddas", true) == BuyResult.DISCOUNTED_PRICE);
}
	
	@Test // trying to take a shoe with discount, in case that there are no shoes on store will result in  NOT_ON_DISCOUNT
	public void testTake4() {
	store = Store.getInstance();
	ShoeStorageInfo shoe = new ShoeStorageInfo( "PUMA", 0);
	shoe.AddDiscount(1);
	ShoeStorageInfo[] shoes = {shoe};
	store.load(shoes);
	assertTrue(store.take("PUMA", true) == BuyResult.NOT_ON_DISCOUNT);
}
	
	@Test // trying to take a shoe without discount, in case that there are no shoes on store will result in  NOT_ON_DISCOUNT
	public void testTake5() {
	store = Store.getInstance();
	ShoeStorageInfo shoe = new ShoeStorageInfo( "KAKI", 0);
	ShoeStorageInfo[] shoes = {shoe};
	store.load(shoes);
	assertTrue(store.take("KAKI", false) == BuyResult.NOT_IN_STOCK);
}
	@Test //taking a shoe without discount will result in returning DISCOUNTED_PRICE, in case this shoe WAS discounted.
	public void testTake6() {
		store = Store.getInstance();
		ShoeStorageInfo shoe = new ShoeStorageInfo( "shoshanaShoe", 1);
		ShoeStorageInfo[] shoes = {shoe};
		store.load(shoes);
		store.addDiscount("shoshanaShoe",1);
		assertTrue(store.take("shoshanaShoe", false) == BuyResult.DISCOUNTED_PRICE);
	}
	@Test
	public void testAddDiscount1() {
		store = Store.getInstance();
		ShoeStorageInfo shoe = new ShoeStorageInfo( "sandals", 1);
		ShoeStorageInfo[] shoes = {shoe};
		store.load(shoes);
		store.addDiscount("sandals", 0);
		assertFalse(store.take("sandals", false) == BuyResult.DISCOUNTED_PRICE);
	}
	@Test
	public void testAddDiscount2() {
		store = Store.getInstance();
		ShoeStorageInfo shoe = new ShoeStorageInfo( "PaperShoe", 5);
		ShoeStorageInfo[] shoes = {shoe};
		store.load(shoes);
		store.addDiscount("PaperShoe",1);
		assertTrue(store.take("PaperShoe", true) == BuyResult.DISCOUNTED_PRICE);
	}
	@Test
	public void testAddDiscount3() {
		store = Store.getInstance();
		ShoeStorageInfo shoe = new ShoeStorageInfo( "PaperShoe", 5);
		ShoeStorageInfo[] shoes = {shoe};
		store.load(shoes);
		store.addDiscount("PaperShoe",1);
		assertTrue(store.take("PaperShoe", false) == BuyResult.DISCOUNTED_PRICE);
	}
	@Test 
	public void testAddDiscount4() {
		store = Store.getInstance();
		ShoeStorageInfo shoe = new ShoeStorageInfo( "MudShoe",0);
		ShoeStorageInfo[] shoes = {shoe};
		store.load(shoes);
		store.addDiscount("MudShoe",5);
		assertFalse(store.take("MudShoe", true) == BuyResult.DISCOUNTED_PRICE);
	}

	public void testFile() {
		store = Store.getInstance();
		Receipt receipt = new Receipt( "Moshe The Saleman",  "innocent costumer", "FakeNike",  false,  1, 1, 37);
		store.file(receipt);
		Receipt otherReceipt = store.getLastReceipt();
		assertEquals(receipt,otherReceipt);
	}

}
