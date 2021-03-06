package info.fshi.oppnetdemo1.data;

import info.fshi.oppnetdemo1.utils.Constants;

import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class QueueGenerationAlarm extends BroadcastReceiver {
	// Class constants
	static final String TAG = "QueueGenerationAlarm"; /** for logging */   
	private static WakeLock wakeLock;
	private static final String WAKE_LOCK = "DataAlarmWakeLock";

	private static PendingIntent alarmIntent;

	private static long interval;

	/**
	 * This constructor is called the alarm manager.
	 */
	public QueueGenerationAlarm(){}

	/**
	 * Starts the alarm, need to give it a user defined bluetooth controller (define handler e.g.)
	 */

	public QueueGenerationAlarm(Context context) {          
		scheduleQueueGeneration(context, System.currentTimeMillis());
	}
	/**
	 * Acquire the Wake Lock
	 * @param context
	 */
	public static void getWakeLock(Context context){

		releaseWakeLock();

		PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , WAKE_LOCK); 
		wakeLock.acquire();
	}

	public static void releaseWakeLock(){
		if(wakeLock != null)
			if(wakeLock.isHeld())
				wakeLock.release();
	}


	/**
	 * Stop the scheduled alarm
	 * @param context
	 */
	public static void stopGenerating(Context context) {
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		if(alarmMgr != null){
			Intent intent = new Intent(context, QueueGenerationAlarm.class);
			alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmMgr.cancel(alarmIntent);
		}
		releaseWakeLock();
	}

	/**
	 * Schedules a queue generation event
	 * @param time after how many milliseconds (0 for immediately)?
	 */
	public void scheduleQueueGeneration(Context context, long time) {

		Log.d(TAG, "generate new data in " + Long.toString( time ));
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, QueueGenerationAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if(alarmMgr != null){
			alarmMgr.cancel(alarmIntent);
		}
		interval = Constants.DATA_RATE;
		
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time, interval, alarmIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// start scan
		Log.d(TAG, "generate data at " + String.valueOf(System.currentTimeMillis()));
		
		QueueManager.getInstance(context).generateData();
		
		// schedule a new event
		Random r = new Random();
		scheduleQueueGeneration(context, System.currentTimeMillis() + interval + (r.nextInt(2000 - 1000) + 1000) * 10);
	}
}
