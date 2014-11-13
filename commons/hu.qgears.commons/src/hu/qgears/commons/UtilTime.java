package hu.qgears.commons;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Time measurement tool for
 * simple debugging - profiling purpose
 * @author rizsi
 *
 */
public class UtilTime {
	static Map<String, UtilTime> globalTimers = new HashMap<String, UtilTime>();
	private long start;
	private boolean disableLog=false;
	public boolean isDisableLog() {
		return disableLog;
	}
	public void setDisableLog(boolean disableLog) {
		this.disableLog = disableLog;
	}
	private UtilTime()
	{
		start=System.nanoTime();
	}
	public void printElapsed(String label)
	{
		long now=System.nanoTime();
		long elapsed=now-start;
		start=now;
		if(!disableLog)
		{
			System.out.println("Elapsed time - "+label+" "+elapsed/1000000);
			System.out.flush();
		}
	}
	public static UtilTime createTimer()
	{
		return new UtilTime();
	}
	
	public static UtilTime getTimer(String id){
		if(!globalTimers.containsKey(id)){
			UtilTime time = new UtilTime();
			globalTimers.put(id, time);
			return time;
		}else{
			return globalTimers.get(id);
		}
	}
	/**
	 * Timestamp that is:
	 *  * user readable
	 *  * string ordering orders the timestamps fine
	 * @return
	 */
	public static String createUserReadableTimeStamp() {
		Calendar c=Calendar.getInstance();
		
		return c.get(Calendar.YEAR)+
			UtilString.fillLeft(""+(c.get(Calendar.MONTH)+1),
						2, '0')+
			UtilString.fillLeft(""+(c.get(Calendar.DAY_OF_MONTH)),
						2, '0')+
			UtilString.fillLeft(""+(c.get(Calendar.HOUR_OF_DAY)),
						2, '0')+
			UtilString.fillLeft(""+(c.get(Calendar.MINUTE)),
								2, '0')+
			UtilString.fillLeft(""+(c.get(Calendar.SECOND)),
							2, '0')+
			UtilString.fillLeft(""+(c.get(Calendar.MILLISECOND)/100),
							2, '0');
	}
}
