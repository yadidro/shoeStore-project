package bgu.spl.app.Passive;
/***
 * The Wish is passive object,
 * An object which indicate if wish of client was sent or not
 *
 */
public class Wish {
	
	private Boolean sent;
	/***
	 * This constructor of Wish.
	 */
	public Wish() {
		this.sent = false;
	}

	/***
	 * 
	 * @return if wish was sent.
	 */
	public synchronized boolean isSent() {
		return sent;
	}

	/***
	 * 
	 * @param sent- in case wish was sent update.
	 */
	public synchronized void setSent(boolean sent) {
		this.sent = sent;
	}


}