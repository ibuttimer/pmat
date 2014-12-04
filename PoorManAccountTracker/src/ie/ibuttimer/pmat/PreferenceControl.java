/**
 * 
 */
package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.SQLiteCommandFactory;
import ie.ibuttimer.pmat.db.SQLiteCommandFactory.SelectionArgs;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.Preference;
import android.preference.PreferenceManager;

/**
 * This class provides an interface to the applications preference. 
 * @author Ian Buttimer
 *
 */
public class PreferenceControl {

	
	// preferences related constants
	public static final String PREF_SOUND = "pref_sound";
	public static final boolean prefDefaultSound = true;
	
	public static final String PREF_ANIMATION = "pref_animation";
	public static final boolean prefDefaultAnimation = true;

	public static final String PREF_DEVMODE = "pref_devmode";
	/**
	 * Development mode default value. For developer builds set this to true thereby enabling 
	 * developer optional setting. For release builds set this to false.
	 */
	public static final boolean prefDefaultDevMode = false;
	
	public static final String PREF_CREATE_ACCOUNTS = "pref_devmode_create_accounts";
	public static final boolean prefDefaultCreateAccounts = true;
	
	public static final String PREF_USE_FRAGEMNTS = "pref_use_fragments";
	public static final boolean prefDefaultUseFragments = true;

	public static final String PREF_TRANSACTION_RANGE = "pref_transaction_range";
	public static final int PREF_TRANSACTION_RANGE_1_DAY = 1;
	public static final int PREF_TRANSACTION_RANGE_2_DAY = 2;
	public static final int PREF_TRANSACTION_RANGE_3_DAY = 3;
	public static final int PREF_TRANSACTION_RANGE_4_DAY = 4;
	public static final int PREF_TRANSACTION_RANGE_5_DAY = 5;
	public static final int PREF_TRANSACTION_RANGE_1_WEEK = 7;
	public static final int PREF_TRANSACTION_RANGE_2_WEEK = 14;
	public static final int PREF_TRANSACTION_RANGE_3_WEEK = 21;
	private static final int PREF_TRANSACTION_RANGE_MONTH_VALUE = 100;
	public static final int PREF_TRANSACTION_RANGE_1_MONTH = PREF_TRANSACTION_RANGE_MONTH_VALUE;
	public static final int PREF_TRANSACTION_RANGE_2_MONTH = PREF_TRANSACTION_RANGE_MONTH_VALUE * 2;
	public static final int PREF_TRANSACTION_RANGE_3_MONTH = PREF_TRANSACTION_RANGE_MONTH_VALUE * 3;
	public static final int PREF_TRANSACTION_RANGE_4_MONTH = PREF_TRANSACTION_RANGE_MONTH_VALUE * 4;
	public static final int PREF_TRANSACTION_RANGE_5_MONTH = PREF_TRANSACTION_RANGE_MONTH_VALUE * 5;
	public static final int PREF_TRANSACTION_RANGE_6_MONTH = PREF_TRANSACTION_RANGE_MONTH_VALUE * 6;
	public static final int PREF_TRANSACTION_RANGE_ALL = 1000;
	public static final int prefDefaultTransactionRange = PREF_TRANSACTION_RANGE_1_WEEK;
	
	private static final int MSEC_PER_DAY = 24 * 60 * 60 * 1000;
	private static final int MTHS_PER_YEAR = Calendar.DECEMBER - Calendar.JANUARY + 1;
	
	public static final String PREF_TRANSACTION_SORTBY = "pref_transaction_sortby";
	private static final int PREF_TRANSACTION_SORTBY_ASC = 123;
	private static final int PREF_TRANSACTION_SORTBY_DSC = 321;
	private static final int PREF_TRANSACTION_SORTBY_DATE = 44;
	private static final int PREF_TRANSACTION_SORTBY_AMOUNT = 41;
	private static final int PREF_TRANSACTION_SORTBY_PAYEE = 50;
	private static final int PREF_TRANSACTION_SORTBY_FACTOR = 1000;
	public static final int PREF_TRANSACTION_SORTBY_DATE_ASC = PREF_TRANSACTION_SORTBY_DATE * PREF_TRANSACTION_SORTBY_FACTOR + PREF_TRANSACTION_SORTBY_ASC;
	public static final int PREF_TRANSACTION_SORTBY_DATE_DSC = PREF_TRANSACTION_SORTBY_DATE * PREF_TRANSACTION_SORTBY_FACTOR + PREF_TRANSACTION_SORTBY_DSC;
	public static final int PREF_TRANSACTION_SORTBY_AMOUNT_ASC = PREF_TRANSACTION_SORTBY_AMOUNT * PREF_TRANSACTION_SORTBY_FACTOR + PREF_TRANSACTION_SORTBY_ASC;
	public static final int PREF_TRANSACTION_SORTBY_AMOUNT_DSC = PREF_TRANSACTION_SORTBY_AMOUNT * PREF_TRANSACTION_SORTBY_FACTOR + PREF_TRANSACTION_SORTBY_DSC;
	public static final int PREF_TRANSACTION_SORTBY_PAYEE_ASC = PREF_TRANSACTION_SORTBY_PAYEE * PREF_TRANSACTION_SORTBY_FACTOR + PREF_TRANSACTION_SORTBY_ASC;
	public static final int PREF_TRANSACTION_SORTBY_PAYEE_DSC = PREF_TRANSACTION_SORTBY_PAYEE * PREF_TRANSACTION_SORTBY_FACTOR + PREF_TRANSACTION_SORTBY_DSC;
	public static final int prefDefaultTransactionSortBy = PREF_TRANSACTION_SORTBY_DATE_DSC;

	public static final String PREF_WIDGET_ACCOUNTS = "pref_widget_accounts";

	
	private static SharedPreferences preferences = null;

	/**
	 * 
	 */
	public PreferenceControl() {
		// nop
	}
	
	
	/**
	 * Initialise class 
	 * @param context	- application context
	 */
	private static void init(Context context) {
		if ( preferences == null && context != null)
			preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	/**
	 * Check if sound is enabled.
	 * @param context	- application context
	 * @return			<code>true</code> if sound is enabled, <code>false</code> otherwise
	 */
	public static boolean isSoundEnabled(Context context) {
		init(context);
		return preferences.getBoolean(PREF_SOUND, prefDefaultSound);
	}

	/**
	 * Check if animation is enabled.
	 * @param context	- application context
	 * @return			<code>true</code> if animation is enabled, <code>false</code> otherwise
	 */
	public static boolean isAnimationEnabled(Context context) {
		init(context);
		return preferences.getBoolean(PREF_ANIMATION, prefDefaultAnimation);
	}

	/**
	 * Check if developer mode is enabled.
	 * @param context	- application context
	 * @return			<code>true</code> if developer mode is enabled, <code>false</code> otherwise
	 */
	public static boolean isDevMode(Context context) {
		init(context);
		return preferences.getBoolean(PREF_DEVMODE, prefDefaultDevMode);
	}
	
	/**
	 * Check if default accounts should be created for a new database.
	 * @param context	- application context
	 * @return			<code>true</code> if accounts should be created, <code>false</code> otherwise
	 */
	public static boolean isCreateAccounts(Context context) {
		init(context);
		return isDevMode(context) &&
				preferences.getBoolean(PREF_CREATE_ACCOUNTS, prefDefaultCreateAccounts);
	}
	
	/**
	 * Check if fragments should be used.
	 * @param context	- application context
	 * @return			<code>true</code> if fragments should be used, <code>false</code> otherwise
	 */
	public static boolean isUseFragments(Context context) {
		init(context);
		return preferences.getBoolean(PREF_USE_FRAGEMNTS, prefDefaultUseFragments);
	}

	/**
	 * Get the default transaction display range.
	 * @param context	- application context
	 * @return			Range setting; one of <code>PREF_TRANSACTION_RANGE_1_DAY</code> etc.
	 */
	public static int getDefaultTransactionRange(Context context) {
		init(context);
		return Integer.valueOf(preferences.getString(PREF_TRANSACTION_RANGE, Integer.toString(prefDefaultTransactionRange)));
	}

	/**
	 * Get the start of the default transaction display range from the current date/time.
	 * @param context	- application context
	 * @return			Calendar object representing the first valid date in the range or <code>null</code> if range is unlimited
	 */
	public static Calendar getTransactionRangeStartDate(Context context) {
		return getTransactionRangeStartDate(context, Calendar.getInstance());
	}

	/**
	 * Get the start of a transaction display range from the current date/time.
	 * @param context	- application context
	 * @param range		- range to get start of; one of <code>PREF_TRANSACTION_RANGE_1_DAY</code> etc.
	 * @return			Calendar object representing the first valid date in the range or <code>null</code> if range is unlimited
	 */
	public static Calendar getTransactionRangeStartDate(Context context, int range) {
		return getTransactionRangeStartDate(context, range, Calendar.getInstance());
	}

	/**
	 * Get the start of the default transaction display range from the specified date/time.
	 * @param context	- application context
	 * @param end		- end of range
	 * @return			Calendar object representing the first valid date in the range or <code>null</code> if range is unlimited
	 */
	public static Calendar getTransactionRangeStartDate(Context context, Calendar end) {
		return getTransactionRangeStartDate(context, getDefaultTransactionRange(context), end);
	}

	/**
	 * Get the start of a transaction display range from the specified date/time.
	 * @param context	- application context
	 * @param range		- range to get start of; one of <code>PREF_TRANSACTION_RANGE_1_DAY</code> etc.
	 * @param end		- end of range
	 * @return			Calendar object representing the first valid date in the range or <code>null</code> if range is unlimited
	 */
	public static Calendar getTransactionRangeStartDate(Context context, int range, Calendar end) {
		Calendar start = (Calendar) end.clone();
		
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		
		long mSec = start.getTimeInMillis();
		
		if ( range < PREF_TRANSACTION_RANGE_1_MONTH ) {
			// specific number of days
			mSec -= (MSEC_PER_DAY * range);
			start.setTimeInMillis(mSec);
		}
		else if ( range <= PREF_TRANSACTION_RANGE_6_MONTH ) {
			// specific number of months
			int months = range / PREF_TRANSACTION_RANGE_MONTH_VALUE;
			int day = start.get(Calendar.DAY_OF_MONTH);
			int mth = start.get(Calendar.MONTH);
			int yr = start.get(Calendar.YEAR);
			
			if ( months >= MTHS_PER_YEAR ) {
				yr -= (months / MTHS_PER_YEAR);
				months %= MTHS_PER_YEAR;
			}
			/* Note: using the fact that Calendar.JANUARY etc. are increasing sequential */
			mth -= months;
			if ( mth < Calendar.JANUARY ) {
				// year wrap
				--yr;
				mth = Calendar.JANUARY - mth;	// num of months before jan
				mth = MTHS_PER_YEAR - mth + Calendar.JANUARY;
			}

			start.set(yr, mth, 1);	// set year & month
			int maxDay = start.getActualMaximum(Calendar.DAY_OF_MONTH);	// max day in month	
			start.set(Calendar.DAY_OF_MONTH, (day > maxDay ? maxDay : day));
		}
		else {
			// all transactions
			start = null;
		}
		return start;
	}
	
	/**
	 * Get the default transaction display sort order.
	 * @param context	- application context
	 * @return			Range setting; one of <code>PREF_TRANSACTION_RANGE_1_DAY</code> etc.
	 */
	public static int getDefaultTransactionSortOrder(Context context) {
		init(context);
		return Integer.valueOf(preferences.getString(PREF_TRANSACTION_SORTBY, Integer.toString(prefDefaultTransactionSortBy)));
	}

	/**
	 * Get the accounts to be displayed in the widget
	 * @param context	- application context
	 * @return			Array of string of the account ids
	 */
	public static Set<String> getWidgetAccounts(Context context) {
		init(context);
		Set<String> accounts = preferences.getStringSet(PREF_WIDGET_ACCOUNTS, null);

		if ( accounts == null || accounts.size() == 0 ) {
			// no setting so just return the first few accounts
			Cursor c = context.getContentResolver().query(DatabaseManager.ACCOUNT_BASIC_URI, new String[] { 
															DatabaseManager.ACCOUNT_ID
															}, 
															null, null, null);
			final int N = c.getCount();
			if ( N > 0 ) {
				accounts = new HashSet<String>();

				c.moveToFirst();
				int idIdx = c.getColumnIndexOrThrow(DatabaseManager.ACCOUNT_ID);

				for ( int i = 0; i < N; ++i ) {
					accounts.add(Long.toString( c.getLong(idIdx) ));
					c.moveToNext();
				}
			}
			c.close();
		}

		return accounts;
	}

	/**
	 * Get the accounts to be displayed in the widget
	 * @param context	- application context
	 * @return			SelectionArgs object with database query selection
	 */
	public static SelectionArgs getWidgetAccountsSelection(Context context) {
		init(context);
		Set<String> accounts = preferences.getStringSet(PREF_WIDGET_ACCOUNTS, null);
		long[] ids = null;

		if ( accounts != null && accounts.size() > 0) {
			final int N = accounts.size();
			ids = new long[N];
			String[] strIds = accounts.toArray(new String[N]);
			for ( int i = 0; i < N; ++i )
				ids[i] = Long.valueOf(strIds[i]);
		}
		else {
			// no setting so just return all the accounts
			Cursor c = context.getContentResolver().query(DatabaseManager.ACCOUNT_BASIC_URI, new String[] { 
															DatabaseManager.ACCOUNT_ID
															}, 
															null, null, null);
			final int N = c.getCount();
			if ( N > 0 ) {
				ids = new long[N];
				
				c.moveToFirst();
				int idIdx = c.getColumnIndexOrThrow(DatabaseManager.ACCOUNT_ID);

				for ( int i = 0; i < N; ++i ) {
					ids[i] = c.getLong(idIdx);
					c.moveToNext();
				}
			}
			c.close();
		}

		SelectionArgs args;
		if ( ids != null && ids.length > 0 )
			args = SQLiteCommandFactory.makeIdSelection(DatabaseManager.ACCOUNT_ID, ids);
		else
			args = null;
		return args;
	}

	
	/**
	 * Return the value of the preference with the specified key.
	 * @param context
	 * @param key		- preference key
	 * @return
	 */
	public static Object getPreference(Context context, String key) {
		Object value;
		
		if ( key.equals(PREF_TRANSACTION_RANGE) )
			value = Integer.valueOf( getDefaultTransactionRange(context) );
		else if ( key.equals(PREF_TRANSACTION_SORTBY) )
			value = Integer.valueOf( getDefaultTransactionSortOrder(context) );
		else if ( key.equals(PREF_WIDGET_ACCOUNTS) )
			value = getWidgetAccounts(context);
		else if ( key.equals(PREF_SOUND) )
			value = Boolean.valueOf( isSoundEnabled(context) );
		else if ( key.equals(PREF_ANIMATION) )
			value = Boolean.valueOf( isAnimationEnabled(context) );
		else if ( key.equals(PREF_DEVMODE) )
			value = Boolean.valueOf( isDevMode(context) );
		else if ( key.equals(PREF_CREATE_ACCOUNTS) )
			value = Boolean.valueOf( isCreateAccounts(context) );
		else if ( key.equals(PREF_USE_FRAGEMNTS) )
			value = Boolean.valueOf( isUseFragments(context) );
		else
			value = null;
		return value;
	}

	/**
	 * Return the value of the specified preference.
	 * @param context
	 * @param pref		- preference key
	 * @return
	 */
	public static Object getPreference(Context context, Preference pref) {
		return getPreference(context, pref.getKey());
	}
	
}
