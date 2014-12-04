package ie.ibuttimer.pmat.util;

import android.util.Log;

/**
 * Local application implementation of the android.util.Log class 
 * @author Ian Buttimer
 *
 */
public class Logger {

	private final static String TAG = "PMAT";

	/**
	 * Send a DEBUG log message.
	 * @param msg	The message you would like logged.
	 * @return
	 * @see android.util.Log#d(String, String)
	 */
	public static int  d(String msg) {
		return Log.d(TAG, msg);
	}
	 
	/**
	 * Send a DEBUG log message and log the exception.
	 * @param msg	The message you would like logged.
	 * @param tr	An exception to log.
	 * @return
	 * @see android.util.Log#d(String, String, Throwable)
	 */
	public static int  d(String msg, Throwable tr) {
		return Log.d(TAG, msg, tr);
	}
	 
	/**
	 * Send an ERROR log message.
	 * @param msg	The message you would like logged.
	 * @return
	 * @see android.util.Log#e(String, String)
	 */
	public static int  e(String msg) {
		return Log.e(TAG, msg);
	}
	 
	/**
	 * Send a ERROR log message and log the exception.
	 * @param msg	The message you would like logged.
	 * @param tr	An exception to log.
	 * @return
	 * @see android.util.Log#e(String, String, Throwable)
	 */
	public static int  e(String msg, Throwable tr) {
		return Log.e(TAG, msg, tr);
	}
	
	/**
	 * Handy function to get a loggable stack trace from a Throwable
	 * @param tr	An exception to log.
	 * @return
	 * @see android.util.Log#getStackTraceString(Throwable)
	 */
	public static String  getStackTraceString(Throwable tr) {
		return Log.getStackTraceString(tr);
	}
	 
	/**
	 * Send an INFO log message.
	 * @param msg	The message you would like logged.
	 * @return
	 * @see android.util.Log#i(String, String)
	 */
	public static int  i(String msg) {
		return Log.i(TAG, msg);
	}

	/**
	 * Send a INFO log message and log the exception.
	 * @param msg	The message you would like logged.
	 * @param tr	An exception to log.
	 * @return
	 * @see android.util.Log#i(String, String, Throwable)
	 */
	public static int  i(String msg, Throwable tr) {
		return Log.i(TAG, msg, tr);
	}
	 
	/**
	 * Checks to see whether or not a log for the specified tag is loggable at the specified level.
	 * @param level	The level to check.
	 * @return
	 * @see android.util.Log#isLoggable(String, int)
	 */
	public static boolean  isLoggable(int level) {
		return Log.isLoggable(TAG, level);
	}
	 
	/**
	 * Low-level logging call. 
	 * @param priority	The priority/type of this log message
	 * @param msg		The message you would like logged.
	 * @return
	 * @see android.util.Log#println(int, String, String)
	 */
	public static int  println(int priority, String msg) {
		return Log.println(priority, TAG, msg);
	}
	
	/**
	 * Send a VERBOSE log message.
	 * @param msg		The message you would like logged.
	 * @return
	 * @see android.util.Log#v(String, String)
	 */
	public static int  v(String msg) {
		return Log.v(TAG, msg);
	}
	 
	/**
	 * Send a VERBOSE log message and log the exception.
	 * @param msg	The message you would like logged.
	 * @param tr	An exception to log.
	 * @return
	 * @see android.util.Log#v(String, String, Throwable)
	 */
	public static int  v(String msg, Throwable tr) {
		return Log.v(TAG, msg, tr);
	}
	 
	/**
	 * Log the exception.
	 * @param tr	An exception to log.
	 * @return
	 * @see android.util.Log#w(String, Throwable)
	 */
	public static int  w(Throwable tr) {
		return Log.w(TAG, tr);
	}
	
	/**
	 * Send a WARN log message and log the exception.
	 * @param msg	The message you would like logged.
	 * @param tr	An exception to log.
	 * @return
	 * @see android.util.Log#w(String, String, Throwable)
	 */
	public static int  w(String msg, Throwable tr) {
		return Log.w(TAG, msg, tr);
	}
	 
	/**
	 * Send a WARN log message. 
	 * @param msg	The message you would like logged.
	 * @return
	 * @see android.util.Log#w(String, String)
	 */
	public static int  w(String msg) {
		return Log.wtf(TAG, msg);
	}
	
	/**
	 * What a Terrible Failure: Report an exception that should never happen.
	 * @param tr	An exception to log.
	 * @return
	 * @see android.util.Log#wtf(String, Throwable)
	 */
	public static int  wtf(Throwable tr) {
		return Log.wtf(TAG, tr);
	}
	 
	/**
	 * What a Terrible Failure: Report a condition that should never happen.
	 * @param msg	The message you would like logged.
	 * @return
	 * @see android.util.Log#wtf(String, String)
	 */
	public static int  wtf(String msg) {
		return Log.wtf(TAG, msg);
	}
	 
	/**
	 * What a Terrible Failure: Report an exception that should never happen.
	 * @param msg	The message you would like logged.
	 * @param tr	An exception to log.
	 * @return
	 * @see android.util.Log#wtf(String, String, Throwable)
	 */
	public static int  wtf(String msg, Throwable tr) {
		return Log.wtf(TAG, msg, tr);
	}

}
