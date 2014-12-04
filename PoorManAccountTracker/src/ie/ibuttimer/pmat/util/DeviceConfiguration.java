/**
 * 
 */
package ie.ibuttimer.pmat.util;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Utility class providing access to various configuration related functions.
 * @author Ian Buttimer
 *
 */
public class DeviceConfiguration {

	/**
	 * 
	 */
	public DeviceConfiguration() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Helper method to determine if the device has a particular screen size. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isScreenSize(Context context, int size) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= size;
	}

	/**
	 * Helper method to determine if the device has an extra-large screen, i.e. at least 960dp x 720dp. For
	 * example, 10" tablets are extra-large.
	 */
	public static boolean isXLargeScreen(Context context) {
		return isScreenSize(context, Configuration.SCREENLAYOUT_SIZE_XLARGE);
	}
	
	/**
	 * Helper method to determine if the device has an large screen, i.e. at least 640dp x 480dp. For
	 * example, 10" tablets are extra-large.
	 */
	public static boolean isLargeScreen(Context context) {
		return isScreenSize(context, Configuration.SCREENLAYOUT_SIZE_LARGE);
	}
	
	/**
	 * Helper method to determine if the device has an large screen, i.e. at least 470dp x 320dp. For
	 * example, 10" tablets are extra-large.
	 */
	public static boolean isNormalScreen(Context context) {
		return isScreenSize(context, Configuration.SCREENLAYOUT_SIZE_NORMAL);
	}
	
	/**
	 * Helper method to determine if the device has an small screen, i.e. at least 426dp x 320dp. For
	 * example, 10" tablets are extra-large.
	 */
	public static boolean isSmallScreen(Context context) {
		return isScreenSize(context, Configuration.SCREENLAYOUT_SIZE_SMALL);
	}


}
