/**
 * 
 */
package ie.ibuttimer.pmat.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.text.TextUtils;

/**
 * @author Ian Buttimer
 *
 */
public class DateTimeFormat extends android.text.format.DateFormat {

	/** The format style constant defining the short style, i.e. completely numeric, such as 12.1.52 or 3:30pm */
	public static final int SHORT = java.text.DateFormat.SHORT;
	/** The format style constant defining the medium style, such as Jan 12, 1952 */
	public static final int MEDIUM = java.text.DateFormat.MEDIUM;
	/** The format style constant defining the long style, such as January 12, 1952 or 3:30:32pm */
	public static final int LONG = java.text.DateFormat.LONG;
	
	/** Format date field */
	public static final int FORMAT_DATE = 1;
	/** Format time field */
	public static final int FORMAT_TIME = 2;
	/** Format date & time field */
	public static final int FORMAT_DATE_TIME = FORMAT_DATE | FORMAT_TIME;
	
	private Context context;	// user context
	private int style;			// format style; SHORT/MEDIUM/LONG
	private int fields;			// required fields; FORMAT_DATE/FORMAT_TIME/FORMAT_DATE_TIME
	

	/**
	 * @param context
	 * @param style
	 * @param fields
	 */
	public DateTimeFormat(Context context, int style, int fields) {
		super();
		this.context = context;
		this.style = style;
		this.fields = fields;
	}

	/**
	 * @param context
	 * @param style
	 */
	public DateTimeFormat(Context context, int style) {
		this(context, style, FORMAT_DATE_TIME); 
	}

	/**
	 * @param context
	 */
	public DateTimeFormat(Context context) {
		this(context, SHORT); 
	}
	
	
	
	/**
	 * @return the style
	 */
	public int getStyle() {
		return style;
	}

	/**
	 * @param style the style to set
	 */
	public void setStyle(int style) {
		this.style = style;
	}

	/**
	 * @return the fields
	 */
	public int getFields() {
		return fields;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(int fields) {
		this.fields = fields;
	}

	/**
	 * Return a formatted date according to the current locale, the user's date-order & 12-/24-hour clock preference.
	 * @param date		Date to format
	 * @param style		Format style; SHORT/MEDIUM/LONG
	 * @param fields	Required fields; FORMAT_DATE/FORMAT_TIME/FORMAT_DATE_TIME
	 * @return			Formatted string
	 */
	public String format(Date date, int style, int fields) {

		StringBuffer sb = new StringBuffer();
		
		if ( (fields & FORMAT_DATE_TIME) == 0 )
			fields = FORMAT_DATE_TIME;	// be nice, give everything

		// get date format
		if ( (fields & FORMAT_DATE) != 0 ) {
			java.text.DateFormat df; 
			switch ( style ) {
				case SHORT:
					df = android.text.format.DateFormat.getDateFormat(context);
					break;
				case MEDIUM:
					df = android.text.format.DateFormat.getMediumDateFormat(context);
					break;
//				case LONG:
				default:
					df = android.text.format.DateFormat.getLongDateFormat(context);
					break;
			}
			sb.append(df.format(date));
		}

		// get time format
		if ( (fields & FORMAT_TIME) != 0 ) {
			java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);
			if ( sb.length() > 0 )
				sb.append(" ");
			sb.append(tf.format(date));
		}
		
		return sb.toString();
	}

	
	/**
	 * Return the current date/time formatted according to the current locale, the user's date-order & 12-/24-hour clock preference.
	 * @return			Formatted string
	 */
	public String formatCurrent() {
		return format(Calendar.getInstance().getTime(), style, fields);
	}

	
	/**
	 * Return a formatted date/time according to the current locale, the user's date-order & 12-/24-hour clock preference.
	 * @return			Formatted string
	 */
	public String format( Date date ) {
		return format(date, style, fields);
	}

	
	/**
	 * Return a formatted time (such as 10:21 pm or 22:21) according to the current locale and the user's 12-/24-hour clock preference.
	 * @param date	Date to format
	 * @return		Formatted string
	 */
	public String formatTime( Date date ) {
		return format(date, SHORT, FORMAT_TIME);
	}

	/**
	 * Return a short format date (such as 12/31/1999) according to the current locale and the user's date-order preference.
	 * @param date	Date to format
	 * @return		Formatted string
	 */
	public String formatShortDate( Date date ) {
		return format(date, SHORT, FORMAT_DATE);
	}

	/**
	 * Return a short format date (such as Dec. 31, 1999) according to the current locale and the user's date-order preference.
	 * @param date	Date to format
	 * @return		Formatted string
	 */
	public String formatMediumDate( Date date ) {
		return format(date, MEDIUM, FORMAT_DATE);
	}
	
	/**
	 * Return a short format date (such as December 31, 1999) according to the current locale and the user's date-order preference.
	 * @param date	Date to format
	 * @return		Formatted string
	 */
	public String formatLongDate( Date date ) {
		return format(date, LONG, FORMAT_DATE);
	}

	/**
	 * Return a short format date (such as 12/31/1999 10:21) according to the current locale, the user's date-order & 12-/24-hour clock preference.
	 * @param date	Date to format
	 * @return		Formatted string
	 */
	public String formatShortDateTime( Date date ) {
		return format(date, SHORT, FORMAT_DATE_TIME);
	}

	/**
	 * Return a medium format date (such as Dec. 31, 1999 10:21) according to the current locale and the user's date-order & 12-/24-hour clock preference.
	 * @param date	Date to format
	 * @return		Formatted string
	 */
	public String formatMediumDateTime( Date date ) {
		return format(date, MEDIUM, FORMAT_DATE_TIME);
	}
	
	/**
	 * Return a short format date (such as December 31, 1999 10:21) according to the current locale and the user's date-order & 12-/24-hour clock preference.
	 * @param date	Date to format
	 * @return		Formatted string
	 */
	public String formatLongDateTime( Date date ) {
		return format(date, LONG, FORMAT_DATE_TIME);
	}
	
	
	/**
	 * Return <code>date</code> formatted as a time stamp 
	 * @param date		- Date to format
	 * @param format	- desired format using SimpleDateFormat formatters
	 * @param tz		- timezone designator, e.g. "UTC", if null default timezone is used
	 * @param loc		- locale, if null default locale is used
	 * @return		Time stamp
	 * @see java.text.SimpleDateFormat.SimpleDateFormat
	 */
	public static String makeTimestamp(Date date, String format, String tz, Locale loc) {
		
		SimpleDateFormat sdf;
		if ( loc != null )
			sdf = new SimpleDateFormat(format, loc);
		else {
			sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
			sdf.applyPattern(format);
		}

		if ( date == null )
			date = Calendar.getInstance().getTime();	// use current date/time

		// get offset for timezone
		int tzOffset = 0;
		if ( !TextUtils.isEmpty(tz) ) {
			TimeZone zone = TimeZone.getTimeZone(tz);
			tzOffset = zone.getOffset(date.getTime());
			if ( zone.inDaylightTime(date) )
				tzOffset += zone.getDSTSavings();
		}

		// timezone time
		date = new Date(date.getTime() + tzOffset);

		String timestamp = sdf.format( date );
		return timestamp;
	}

	/**
	 * Return <code>calendar</code> formatted as a time stamp 
	 * @param calendar	- Calendar to format
	 * @param format	- desired format using SimpleDateFormat formatters
	 * @param tz		- timezone designator, e.g. "UTC", if null default timezone is used
	 * @param loc		- locale, if null default locale is used
	 * @return		Time stamp
	 * @see java.text.SimpleDateFormat.SimpleDateFormat
	 */
	public static String makeDatabaseTimestamp(Calendar calendar, String format, String tz, Locale loc) {
		return makeTimestamp( calendar.getTime(), format, tz, loc );
	}

	/**
	 * Parse <code>dateStr</code> returning a Date object representing the date & time 
	 * @param dateStr	- String to parse
	 * @param tz		- timezone designator, e.g. "UTC", if null default timezone is used
	 * @param loc		- locale, if null default locale is used
	 * @return			Date object
	 */
	public static Date parseTimestamp(String dateStr, String[] formats, String tz, Locale loc) {
		
		SimpleDateFormat sdf = null;
		Date date;

		if ( formats != null ) {
			for ( int i = formats.length - 1; i >= 0; --i ) {
				if ( dateStr.length() == formats[i].length() ) {
					if ( loc != null )
						sdf = new SimpleDateFormat(formats[i], loc);
					else {
						sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
						sdf.applyPattern(formats[i]);
					}
					break;
				}
			}
		}
		if ( sdf == null )
			sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		
		if ( tz != null )
			sdf.setTimeZone(TimeZone.getTimeZone(tz));

		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			date = null;
		}
		return date;
	}

	
}
