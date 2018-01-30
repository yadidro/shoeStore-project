package bgu.spl.app.Messages;

import bgu.spl.mics.Broadcast;

/**
 * The TickBroadcast is class that implements massage
 * Its a broadcast messages that is sent at every passed clock tick.
 */
public class TickBroadcast implements Broadcast {

	private  int _currentTick;

	/**
	 * This constructor of TickBroadcast.
	 * @param currentTick is time right now.
	 */
	public TickBroadcast(int currentTick){
		_currentTick = currentTick;
	}

	/**
	 * 
	 * @return time right now.
	 */
	public int getCurrentTick() {
		return _currentTick;
	}

}