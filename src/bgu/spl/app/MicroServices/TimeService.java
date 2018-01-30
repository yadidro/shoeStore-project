package bgu.spl.app.MicroServices;

import bgu.spl.mics.MicroService;

import bgu.spl.app.Messages.TickBroadcast;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * The TimeService is active object that extend MicroService.
 * <p>
This micro-service is our global system timer (handles the clock ticks in the system). 
It is responsible for counting how much clock ticks passed since the begging of its execution and notifying every 
other microservice (thats interested) about it using the TickBroadcast.
 */
public class TimeService extends MicroService {
	private Timer timer;
	private int currentTick;
	private int _speed;
	private CountDownLatch countDownLatchObject;
	final static Logger LOGGER=Logger.getLogger(TimeService.class.getName());

	/**
	 * This constructor of TimeService.
	 * @param name is TimeService.
	 * @param speed is number of milliseconds each clock tick takes.
	 * @param latchObject we use it for countDownLatch in Runner. 
	 * @param duration is tick when every thing will shutdown.
	 */
	public TimeService(String name, int speed,int duration,CountDownLatch latchObject) {
		super("timer", duration);
		_speed = speed;
		timer = new Timer();
		currentTick = 0;
		countDownLatchObject = latchObject;
	}

	@Override

	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, broad->{	
			if(duration<currentTick){
				timer.cancel();
				terminate();
			}
		});
		countDownLatchObject.countDown();
		try {
			/*
			 *  Waiting for the clients to bring the counter to 0.
			 *  Note that we call the await function of CountDownLatch and not to the wait function of Object.
			 */
			countDownLatchObject.await();
		} catch (InterruptedException e)
		{
			return;
		}
		this.timer.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				if(currentTick<=duration){
					currentTick++;
					sendBroadcast(new TickBroadcast(currentTick));
				}		
			}
		}, 0, _speed);
	}
}

