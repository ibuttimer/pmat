/**
 * 
 */
package ie.ibuttimer.pmat.sms;

import ie.ibuttimer.pmat.util.DateTimeFormat;
import ie.ibuttimer.pmat.util.TwoWayHashMap;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * @author Ian Buttimer
 *
 */
public class SmsMessageFactory {


	/** Represents a data field that may be present. */
	private static final int SMS_DATA_FIELD_OPTIONAL = 1000;
	
	/** Represents an invalid field. */
	public static final int SMS_DATA_FIELD_INVALID = 0;
	/** Represents a fixed alphabetic data field, i.e. text that doesn't change and is always present. */
	public static final int SMS_DATA_FIELD_FIXED_ALPHA = 1;
	/** Represents a variable alphabetic data field, i.e. text that does change and is always present. */
	public static final int SMS_DATA_FIELD_VARIABLE_ALPHA = 2;
	/** Represents a fixed alphanumeric data field, i.e. text that doesn't change and is always present. */
	public static final int SMS_DATA_FIELD_FIXED_ALPHANUM = 3;
	/** Represents a variable alphanumeric data field, i.e. text that does change and is always present. */
	public static final int SMS_DATA_FIELD_VARIABLE_ALPHANUM = 4;
	/** Represents a punctuation data field that is always present. */
	public static final int SMS_DATA_FIELD_FIXED_PUNCTUATION = 5;
	/** Represents a variable punctuation data field, i.e. text that does change and is always present. */
	public static final int SMS_DATA_FIELD_VARIABLE_PUNCTUATION = 6;
	/** Represents a whitespace data field that is always present. */
	public static final int SMS_DATA_FIELD_FIXED_WHITE = 7;
	/** Represents a variable whitespace data field, i.e. text that does change and is always present. */
	public static final int SMS_DATA_FIELD_VARIABLE_WHITE = 8;
	/** Represents an amount data field that is always present. */
	public static final int SMS_DATA_FIELD_AMOUNT = 9;
	/** Represents an account data field that is always present. */
	public static final int SMS_DATA_FIELD_ACCOUNT = 10;
	/** Represents a time data field that is always present. */
	public static final int SMS_DATA_FIELD_TIME = 11;
	/** Represents a date data field that is always present. */
	public static final int SMS_DATA_FIELD_DATE = 12;

	
	/** Represents an input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_MARKER = "?";
	/** Represents the start of a variable name for an input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_VALUE_START = "{";
	/** Represents the end of a variable name for an input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_VALUE_END = "}";
	/** Represents the start of an input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_START = SMS_INPUT_FIELD_MARKER + SMS_INPUT_FIELD_VALUE_START;
	/** Represents the end of an input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_END = SMS_INPUT_FIELD_VALUE_END;
	
	/** Represents the variable name for an account input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_ACCOUNT_VALUE = "account";
	/** Represents the variable name for an amount input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_AMOUNT_VALUE = "amount";
	/** Represents the variable name for a source account input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_SOURCE_VALUE = "source";
	/** Represents the variable name for a destination account input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_DESTINATION_VALUE = "destination";
	/** Represents the variable name for a payee (or payer) input field in an sms template. Input fields take the form "?{field_name}" */
	public static final String SMS_INPUT_FIELD_PAYEE_VALUE = "payee";

	/** An invalid field in an sms template. */
	public static final int SMS_TEMPLATE_FIELD_INVALID = 0;
	/** A fixed text field in an sms template. */
	public static final int SMS_TEMPLATE_FIELD_TEXT = 1;
	/** An amount field in an sms template. */
	public static final int SMS_TEMPLATE_FIELD_AMOUNT = 2;
	/** An account field in an sms template. */
	public static final int SMS_TEMPLATE_FIELD_ACCOUNT = 3;
	/** A source account field in an sms template. */
	public static final int SMS_TEMPLATE_FIELD_SOURCE = 4;
	/** A destination account field in an sms template. */
	public static final int SMS_TEMPLATE_FIELD_DESTINATION = 5;
	/** A payee (or payer) field in an sms template. */
	public static final int SMS_TEMPLATE_FIELD_PAYEE = 6;

	
	/** map of SMS template fields to field types */
	private static TwoWayHashMap<String, Integer> fieldKeyMap = new TwoWayHashMap<String, Integer>();
	static {
		String[] keys = new String[] {
				SMS_INPUT_FIELD_ACCOUNT_VALUE,
				SMS_INPUT_FIELD_AMOUNT_VALUE,
				SMS_INPUT_FIELD_SOURCE_VALUE,
				SMS_INPUT_FIELD_DESTINATION_VALUE,
				SMS_INPUT_FIELD_PAYEE_VALUE,
		};
		int[] values = new int[] {
				SMS_TEMPLATE_FIELD_ACCOUNT,
				SMS_TEMPLATE_FIELD_AMOUNT,
				SMS_TEMPLATE_FIELD_SOURCE,
				SMS_TEMPLATE_FIELD_DESTINATION,
				SMS_TEMPLATE_FIELD_PAYEE,
		};
		for ( int i = keys.length - 1; i >= 0; --i )
			fieldKeyMap.put(keys[i], values[i]);
	}


	/** Key for the generated string in a Bundle. */
	public static final String SMS_TEMPLATE_GENERATED_STRING = "SMS_TEMPLATE_GENERATED_STRING";
	/** Key for the offsets of the individual fields within the generated string in a Bundle. */
	public static final String SMS_TEMPLATE_STRING_OFFSETS = "SMS_TEMPLATE_STRING_OFFSETS";

	
	
	
	
	
//	Text balance or bal to 51309
//	"balance"
//	Text statement or mini to 51309
//	"statement"
//	Text transfer + amount + account nickname to 51309
//	"transfer ?{amount} ?{account}"
//	Text pay + amount + bill nickname to 51309
//	"pay ?{amount} ?{account}"
//	Text topup + amount to 50309
//	"topup ?{amount}"
//	Text cash + recipient mobile number + mobile phone operator code to 51309
//	"cash ?{phone} TS"
	
	
	private static enum YearMonthDayOrder { YMD, MDY, DMY }; 
	private static YearMonthDayOrder yearMthDay = null;
	private static char[] yearMthDayMarkArray = new char[3];
	
	
	private int[] fields;			// field types e.g. SMS_TEMPLATE_FIELD_TEXT etc.
	private String[] fieldValues;	// field values

	
	/**
	 * Constructor 
	 * @param fields		- field types
	 * @param fieldValues	- field values
	 */
	public SmsMessageFactory(int[] fields, String[] fieldValues) {
		this.fields = fields;
		this.fieldValues = fieldValues;
	}

	
	/**
	 * Constructor
	 * @param template	- Template string
	 */
	public SmsMessageFactory(String template) {
		
		generateFields(template);
	}
	
	
	/**
	 * Tests if the specified base type is valid  
	 * @param type - base type to test
	 * @return		- true or false
	 */
	public static boolean isValidBaseFieldType(int type) {
		return ( (type >= SMS_DATA_FIELD_FIXED_ALPHA) && (type <= SMS_DATA_FIELD_DATE) );
	}
	
	/**
	 * Tests if the specified type is valid  
	 * @param type	- type to test
	 * @return		- true or false
	 */
	public static boolean isValidFieldType(int type) {
		if ( type > SMS_DATA_FIELD_OPTIONAL )
			type -= SMS_DATA_FIELD_OPTIONAL;
		return isValidBaseFieldType(type);
	}
	
	/**
	 * Returns the base type.  
	 * @param type	- type value to get base type of
	 * @return		- base type value
	 */
	public static int getBaseFieldType(int type) {
		int fieldType;
		if ( type > SMS_DATA_FIELD_OPTIONAL )
			type -= SMS_DATA_FIELD_OPTIONAL;
		if ( isValidBaseFieldType(type) )
			fieldType = type;
		else
			fieldType = SMS_DATA_FIELD_INVALID;
		return fieldType;
	}
	
	/**
	 * Returns the final type value. 
	 * @param type		- base type
	 * @param optional	- optional indicator
	 * @return			- final type value
	 */
	public static int getFieldType(int type, boolean optional) {
		int fieldType;
		if ( isValidBaseFieldType(type) )
			fieldType = type + (optional ? SMS_DATA_FIELD_OPTIONAL : 0);
		else
			fieldType = SMS_DATA_FIELD_INVALID;
		return fieldType;
	}

	
	/**
	 * Return the field types
	 * @return the fields
	 */
	public int[] getFields() {
		return fields;
	}


	/**
	 * Set the field types<br>
	 * <b>Note:</b> There are no range or validity checks done.
	 * @param fields the fields to set
	 */
	public void setFields(int[] fields) {
		this.fields = fields;
	}


	/**
	 * Return the field values
	 * @return the fieldValues
	 */
	public String[] getFieldValues() {
		return fieldValues;
	}


	/**
	 * Set the field values<br>
	 * <b>Note:</b> There are no range or validity checks done.
	 * @param fieldValues the fieldValues to set
	 */
	public void setFieldValues(String[] fieldValues) {
		this.fieldValues = fieldValues;
	}


	/**
	 * Clear the field values of the specified types
	 * @param fieldTypes	- the types to process
	 * @param clear			- clear values for <code>fieldTypes</code> if <code>true</code>, or values excluding <code>fieldTypes</code> if <code>false</code>   
	 */
	private void clearFieldValues(int[] fieldTypes, boolean clear) {
		final int N = fieldTypes.length;
		final int M = fields.length;
		for ( int i = 0; i < N; ++i ) {
			for ( int j = 0; j < M; ++j ) {
				if ( clear && fields[j] == fieldTypes[i] )
					fieldValues[j] = null;
				else if ( !clear && fields[j] != fieldTypes[i] )
					fieldValues[j] = null;
			}
		}
	}

	/**
	 * Clear the field values of the specified types
	 * @param fieldTypes	- the types to clear the value for
	 */
	public void clearFieldValues(int[] fieldTypes) {
		clearFieldValues(fieldTypes, true);
	}

	/**
	 * Clear the field values of all types except the specified types
	 * @param fieldTypes	- the types to not clear the value for
	 */
	public void clearFieldValuesExcluding(int[] fieldTypes) {
		clearFieldValues(fieldTypes, false);
	}




	/* standard regex special chars as per the java regex engine */  
	private static final String SPECIAL_CHARS = "[]\\^$.|?*+(){}";
	private static final String ESCAPE_CHAR_STR = "\\";
	private static final char ESCAPE_CHAR = '\\';
	
	
	private static final String PUNCTUATION_MARKS = ".:;";	// '.', ':' & ';'
	private static final String TIME_MARK_STR = ":";
	private static final char TIME_MARK_CHAR = ':';
	
	
	/** Format character meaning day of week<br>
	 *  Examples for Sunday: DDD -> Sun, DDDDDD -> Sunday */
	private static final char DAY_OF_WEEK = 'D';	
	/** Format character meaning numeric month in year<br>
	 *  Examples for September: N -> 9 NN -> 09 */
	private static final char MONTH_IN_YEAR_NUM = 'N';	
	/** Format character meaning alphabetic month in year<br>
	 *  Examples for September: MMM -> Sep MMMMMMMMM -> September */
	private static final char MONTH_IN_YEAR_ALPHA = 'M';	
	/** Format character meaning am/pm<br>
	 *  Examples: a -> a or p aa -> am or pm */
	private static final char AM_PM = 'a';	
	/** Format character meaning hour in the day<br>
	 *  Examples for midnight: H -> 0 HH -> 00 */
	private static final char HOUR_IN_DAY = 'H';	
	/** Format character meaning minute in hour<br>
	 *  Examples for 7 minutes past the hour: m -> 7 mm -> 07 */
	private static final char MINUTE_IN_HOUR = 'm';	
	/** Format character meaning seconds in minute<br>
	 *  Examples for 7 seconds past the minute: s -> 7 ss -> 07 */
	private static final char SECOND_IN_MINUTE = 's';	
	/** Format character meaning year<br>
	 *  Examples for 2006 yy -> 06 yyyy -> 2006  */
	private static final char YEAR = 'y';	
	/** Format character meaning day of the month<br>
	 *  Examples for the 9th of the month: d -> 9 dd -> 09 */
	private static final char DAY_IN_MONTH = 'd';	
	/** Format character meaning exact match character */
	private static final char EXACT_MATCH = '*';	
	
	 

	/**
	 * Make an input field marker for an SMS template string
	 * @param field	- field name
	 * @return
	 */
	private String makeInputFieldName(String field) {
		return SMS_INPUT_FIELD_START + field + SMS_INPUT_FIELD_END;
	}
	
	/**
	 * Make an SMS field string
	 * @param index	- index of field to generate
	 * @param map	- map containing values to use
	 * @return
	 */
	private String generateSmsField(int index, HashMap<String, String> map) {

		String field = null;
		if ( (fields != null) && (fields.length > 0) && (index < fields.length)) {
			
			switch ( fields[index] ) {
				case SMS_TEMPLATE_FIELD_TEXT:
					field = fieldValues[index];
					break;
				default:
					if ( fieldValues[index] == null ) {
						String str = null;
						if ( map == null ) {
							// must be generating a template
							str = makeInputFieldName( getFieldText(fields[index]) );
						}
						else {
							// retrieve the value from the map
							String text = getFieldText( fields[index] );
							if ( text != null ) {
								str = map.get(text);
							}
						}
						fieldValues[index] = (str != null ? str : "");
					}
					field = fieldValues[index];
					break;
			}
		}
		
		return field;
	}
	
	/**
	 * Make the SMS text based on this template and using the specified values
	 * @param map	- map containing the values to fill in non-fixed fields
	 * @return
	 */
	public String generateSms(HashMap<String, String> map) {

		StringBuffer sb = new StringBuffer();

		if ( (fields != null) && (fields.length > 0)) {
			final int N = fields.length;
			
			for ( int i = 0; i < N; ++i ) {
				String field = generateSmsField(i, map);
				if ( field != null ) {
//					if ( i > 0 )
//						sb.append(" ");
					sb.append(field);
				}
			}
		}
		// remove any double spaces
//		for ( int i = sb.length() - 1; i >= 0; --i ) {
//			if ( sb.charAt(i) == ' ' ) {
//				int j = i;
//				while ( j > 0 ) {
//					if ( sb.charAt(j - 1) == ' ' )
//						--j;
//					else
//						break;
//				}
//				if ( i != j ) {
//					sb.delete(j, i);
//					i = j;
//				}
//			}
//		}
		return sb.toString().trim();
	}

	/**
	 * Generate the SMS text template represented by this object
	 * @return	String template
	 */
	public String generateSmsTemplate() {
		return generateSms(null);
	}
	
	
	/**
	 * Generate a Bundle containing:<br>
	 * <ul>
	 * <li>the SMS text represented by this object</li>
	 * <li>an int[] containing the starting offsets of each field which makes up the message</li>
	 * </ul>
	 * @return
	 */
	public Bundle generateSmsBundle() {

		Bundle b = new Bundle();
		
		if ( (fields != null) && (fields.length > 0) ) {
			
			StringBuffer sb = new StringBuffer();
			final int N = fields.length;
			int[] offsets = new int[N];
			
			for ( int i = 0; i < N; ++i ) {
				String field = generateSmsField(i, null);
				if ( field != null ) {
					if ( i > 0 )
						sb.append(" ");
					offsets[i] = sb.length();
					sb.append(field);
				}
				else
					offsets[i] = -1;
			}
			
			b.putString(SMS_TEMPLATE_GENERATED_STRING, sb.toString());
			b.putIntArray(SMS_TEMPLATE_STRING_OFFSETS, offsets);
		}
		
		return b;
	}
	
	
	/**
	 * Generate a regex string to match the specified type/template
	 * @param type
	 * @param template
	 * @return
	 */
	public static String generateRegexString(Context context, int type, String template) {
		
		StringBuffer sb = new StringBuffer();

		if ( isValidFieldType( type ) && !TextUtils.isEmpty(template) ) {
			int baseType = getBaseFieldType(type);
			
			switch ( baseType ) {
				case SMS_DATA_FIELD_FIXED_ALPHA:
				case SMS_DATA_FIELD_FIXED_ALPHANUM:
				case SMS_DATA_FIELD_FIXED_WHITE:
				case SMS_DATA_FIELD_FIXED_PUNCTUATION:
					sb.append(exactMatchRegex(template));	// exact pattern
					break;

				case SMS_DATA_FIELD_VARIABLE_ALPHA:
					sb.append("[[a-z][A-Z]\\s]*");		// zero or more of all alpha + whitespace
					break;
				case SMS_DATA_FIELD_VARIABLE_ALPHANUM:
					sb.append("[[a-z][A-Z][\\d]\\s]*");	// zero or more of all alpha + numeric + whitespace
					break;
				case SMS_DATA_FIELD_VARIABLE_PUNCTUATION:
					sb.append("[" + exactMatchRegex(PUNCTUATION_MARKS) + "]*");	// zero or more punctuation 
					break;
				case SMS_DATA_FIELD_VARIABLE_WHITE:
					sb.append("[\\s]*");				// zero or more of all whitespace
					break;
					
				case SMS_DATA_FIELD_AMOUNT:
					sb.append("[[\\d][,\\.]]*");		// zero or more of all numeric + numeric separators (',' & '.')
					break;
				case SMS_DATA_FIELD_ACCOUNT:
					break;
				case SMS_DATA_FIELD_TIME:
					sb.append("[[\\d][:]]*");			// zero or more of all numeric + time separators
					break;
				case SMS_DATA_FIELD_DATE: {
					DateFormatSymbols symbols = DateFormatSymbols.getInstance();  
					final int N = template.length();
					char[] mask = new char[N];
					String templateLwr = template.toLowerCase(Locale.US);
					char[] ymdMarks = getYearMonthDayOrderMarkArray(context);
					String[] regexArray;
					char[] separatorArray;
					int i;
					boolean gotYear = false;
					boolean gotMonth = false;
					boolean gotDay = false;
					Pattern pattern;
					Matcher matcher;

					// search for weekdays
					String[] days = symbols.getWeekdays();
					if ( setMaskChars(templateLwr, days, mask, DAY_OF_WEEK) == false ) {
						// search for short weekdays
						String[] shortDays = symbols.getShortWeekdays();
						setMaskChars(templateLwr, shortDays, mask, DAY_OF_WEEK);
					}

					// search for months
					String[] months = symbols.getMonths();
					gotMonth = setMaskChars(templateLwr, months, mask, MONTH_IN_YEAR_ALPHA);
					if ( gotMonth == false ) {
						// search for short months
						String[] shortMonths = symbols.getShortMonths();
						gotMonth = setMaskChars(templateLwr, shortMonths, mask, MONTH_IN_YEAR_ALPHA);
					}
					
					// search for am/pm
					String[] ampm = symbols.getAmPmStrings();
					setMaskChars(templateLwr, months, mask, AM_PM);
					
					// search for times
					regexArray = new String[] {
							"(\\d{1,2}):(\\d{2})",			// "h:mm" or "hh:mm" i.e. 2 groups; 1~2 digits & 2 digits separated by ':'
							"(\\d{1,2}):(\\d{2}):(\\d{2})",	// "h:mm:ss" or "hh:mm:ss" i.e. 3 groups; 1~2 digits, 2 digits & 2 digits separated by ':' 
					};
					separatorArray = new char[] {
							':', 
							':', 
					};
					for ( i = regexArray.length - 1; i >= 0; --i ) {
						if ( searchPattern(templateLwr, regexArray[i], separatorArray[i], new char[] { HOUR_IN_DAY, MINUTE_IN_HOUR, SECOND_IN_MINUTE }, mask) )
							break;
					}
					
					// search for dates
					if ( gotMonth == false ) {
						// haven't found alphabetic month so may be numeric
						regexArray = new String[] {
								/* "dd/NN/yy" or "NN/dd/yy" or "yy/NN/dd" type dates
								 * i.e. 3 groups; 1~2 digits, 2 digits & 1~2 digits separated by any char other than a number */
								"(\\d{1,2})[^\\d]{1}(\\d{1,2})[^\\d]{1}(\\d{1,2})",	
								/* "dd/NN/yyyy" or "NN/dd/yyyy" or "yyyy/NN/dd" type dates
								 * i.e. 3 groups; 1~4 digits, 2 digits & 1~4 digits separated by any char other than a number */
								"(\\d{1,4})[^\\d]{1}(\\d{1,2})[^\\d]{1}(\\d{1,4})",	
						};
						separatorArray = new char[] {
								0,	// no separator char, will inc field on group matches
								0,	// no separator char, will inc field on group matches
						};
						for ( i = regexArray.length - 1; i >= 0; --i ) {
							if ( searchPattern(templateLwr, regexArray[i], separatorArray[i], ymdMarks, mask) ) {
								gotYear = gotMonth = gotDay = true;
								break;
							}
						}
					}
					
					// search for years
					if ( gotYear == false ) {
						// haven't found year yet so may be a "dd MMMMMMM yyyy" type date
						pattern = Pattern.compile("\\d{4}");	// "yyyy"
						matcher = pattern.matcher(templateLwr);
						if ( matcher.find() ) {
							final int end = matcher.end();
							gotYear = true;
							for ( i = matcher.start(); i < end; ++i ) {
								mask[i] = YEAR;
							}
						}
					}
					
					// search for 2 digit days and years
					if ( !gotYear || !gotDay ) {
						pattern = Pattern.compile("\\d{1,2}");	// "d", "dd" or "yy"
						matcher = pattern.matcher(templateLwr);
						int[] startArray = new int[2]; 
						int[] endArray = new int[2]; 
						int matchCount = 0;
						while ( matcher.find() && (matchCount < startArray.length)) {
							// search for a max of 2 two digit entries, i.e. year & day
							int begin = matcher.start();
							if ( mask[begin] == 0 ) {
								// not previously matched so remember it
								startArray[matchCount] = begin;
								endArray[matchCount] = matcher.end();
								++matchCount;
							}
						}
						if ( matchCount > 0 ) {
							char[] chrArray = new char[2];
							
							if ( !gotYear && !gotDay ) {
								if ( getYearMonthDayOrder(context) == YearMonthDayOrder.YMD ) {
									// appears in order year, day
									chrArray[0] = YEAR;
									chrArray[1] = DAY_IN_MONTH;
								}
								else {
									// appears in order day, year
									chrArray[0] = DAY_IN_MONTH;
									chrArray[1] = YEAR;
								}
							}
							else if ( !gotDay ) {
								chrArray[0] = DAY_IN_MONTH;
							}
							else if ( !gotYear ) {
								chrArray[0] = YEAR;
							}
							
							for ( i = 0; i < matchCount; ++i ) {
								for ( int j = startArray[i]; j < endArray[i]; ++j )
									mask[j] = chrArray[i];
							}
						}
					}
					
					// scan mask and generate regex to match template
					for ( i = 0; i < N; ++i ) {
						char skipMark = 0;
						if ( mask[i] == EXACT_MATCH ) {
							sb.append( exactMatchRegex(mask[i]) );
						}
						else if ( mask[i] == DAY_IN_MONTH ) {
							sb.append("(\\d{1,2})");	// match 1~2 digit number
							skipMark = DAY_IN_MONTH;
						}
						else if ( mask[i] == MONTH_IN_YEAR_NUM ) {
							// numeric month
							sb.append("(\\d{1,2})");	// match 1~2 digit number
							skipMark = MONTH_IN_YEAR_NUM;
						}
						else if ( (mask[i] == MONTH_IN_YEAR_ALPHA) || (mask[i] == DAY_OF_WEEK) ) {
							// alphabetic month or day of week
							sb.append("([[a-z][A-Z]]+)");		// one or more of all alpha
							skipMark = mask[i];
						}
						else if ( mask[i] == YEAR ) {
							int size;
							for ( size = i; (size < N) && (mask[size] == YEAR); ++size )
								;
							size -= i;
							if ( size > 2 ) 
								sb.append("(\\d{2,4})");	// match 2~4 digit number
							else
								sb.append("(\\d{1,2})");	// match 1~2 digit number
							skipMark = YEAR;
						}
						else if ( mask[i] == AM_PM ) {
							// alphabetic month or day of week
							sb.append("([[a-z][A-Z]]{2})");		// 2 all alpha
							skipMark = AM_PM;
						}
						else if ( mask[i] == HOUR_IN_DAY ) {
							sb.append("(\\d{1,2})");	// match 1~2 digit number
							skipMark = HOUR_IN_DAY;
						}
						else if ( (mask[i] == MINUTE_IN_HOUR) || (mask[i] == SECOND_IN_MINUTE) ) {
							sb.append("(\\d{2})");		// match 2 digit number
							skipMark = mask[i];
						}
						else if ( Character.isWhitespace(template.charAt(i)) ){
							sb.append("[\\s]+");		// match 1 or more whitespace
							skipMark = ' ';
						}
						// else ignore it

						boolean skipWhite = Character.isWhitespace(skipMark);
						for ( int j = i + 1; j < N; ++j ) {
							if ( skipWhite && !Character.isWhitespace(mask[j]))
								break;
							else if ( mask[j] != skipMark )
								break;
							++i;
						}
					}
					break;
				}
			
			}
			
		}
		
		return sb.toString();
	}
	
	
	/**
	 * Get the field text which corresponds to the specified field type
	 * @param type	- field type
	 * @return
	 */
	private String getFieldText(int type) {
		// texts are the 'key'
		return fieldKeyMap.getKey(type);
	}

	/**
	 * Get the field type which corresponds tot he specified field text
	 * @param text	- field text
	 * @return
	 */
	private int getFieldType(String text) {
		// types are the 'value'
		Integer type = fieldKeyMap.getValue(text);
		if ( type == null )
			type = Integer.valueOf(SMS_TEMPLATE_FIELD_INVALID);
		return type.intValue();
	}
	
	/**
	 * Generate the field list from the specified template. 
	 * @param template
	 */
	private void generateFields( String template ) {

		Pattern p = Pattern.compile(exactMatchRegex(SMS_INPUT_FIELD_END));
		String[] splitStart = template.split(exactMatchRegex(SMS_INPUT_FIELD_START));
		final int N = splitStart.length;
		ArrayList<Integer> fieldType = new ArrayList<Integer>(); 
		ArrayList<String> fieldValue = new ArrayList<String>(); 

		if ( N >= 1 ) {
			// contains value fields
			for ( int i = 0; i < N; ++i ) {
				if ( TextUtils.isEmpty(splitStart[i]) )
					continue;	// nothing before the marker so skip

//				splitStart[i] = splitStart[i].trim();

				Matcher m = p.matcher(splitStart[i]);
				boolean found = m.find();

				String[] splitEnd = splitStart[i].split(exactMatchRegex(SMS_INPUT_FIELD_END));

				int fixedTextIdx = -1;
				int splits = splitEnd.length;
				switch ( splits ) {
					case 0:	// nothing to do
						break;
					case 1:
						if ( !found ) {
							// no end marker, so all fixed text
							fixedTextIdx = 0;
							break;
						}
						// else nothing after the marker, so fall thru to add field 
					default:
						int type = getFieldType(splitEnd[0].toLowerCase(Locale.US));
						fieldType.add(Integer.valueOf(type));
						fieldValue.add(splitEnd[0]);
						if ( found && splits > 1 ) {
							// add trailing fixed text; Note should only ever be a max of 2 in split
							fixedTextIdx = 1;
						}
						break;
				}
				if ( fixedTextIdx >= 0 ) {
					// add fixed text field, if exists
					fieldType.add(Integer.valueOf(SMS_TEMPLATE_FIELD_TEXT));
					fieldValue.add(splitEnd[fixedTextIdx]);
				}
			}
			
			final int T = fieldType.size();
			fields = new int[T];
			fieldValues = new String[T];
			Object[] typeArray = fieldType.toArray();
			String[] valArray = fieldValue.toArray(new String[T]);
			for ( int j = 0; j < T; ++j ) {
				fields[j] = ((Integer) typeArray[j]).intValue();
				if ( fields[j] == SMS_TEMPLATE_FIELD_TEXT )
					fieldValues[j] = valArray[j];	// only save fixed text values
			}
		}
		else {
			fields = null;
			fieldValues = null;
		}
	}
	
	
	
	/**
	 * Search <code>template</code> for any of the strings in <code>strings</code> and if found set the appropriate locations in <code>mask</code> with <code>chr</code>. 
	 * @param template	String to search
	 * @param strings	String to search for
	 * @param mask		Array to mask <code>template</code>
	 * @param chr		Mash char to use
	 */
	private static boolean setMaskChars(String template, String[] strings, char[] mask, char chr) {

		String templateLwr = template.toLowerCase(Locale.US);
		boolean result = false;

		for ( int i = strings.length - 1; i >= 0; --i ) {
			int start;
			if ( !TextUtils.isEmpty(strings[i]) ) {
				start = templateLwr.indexOf(strings[i].toLowerCase(Locale.US));
				if ( start >= 0 ) {
					result = true;
					for ( int j = start + strings[i].length() - 1; j >= start; --j )
						mask[j] = chr;
				}
			}
		}
		return result;
	}
	
	
	/**
	 * Search <code>template</code> with the regular expression <code>regex</code> and if found set the appropriate locations in 
	 * <code>mask</code> with the appropriate mark from <code>markOrder</code>. 
	 * 
	 * @param template		String to search
	 * @param regex			Regular expressions to use for matching 
	 * @param separatorChar	Character which separates groups within <code>regex</code>
	 * @param markOrder		Marks representing each group to fill into <code>mask</code>
	 * @param mask			Array to mask <code>template</code>
	 * @return
	 */
	private static boolean searchPattern(String template, String regex, char separatorChar, char[] markOrder, char[] mask) {
		
		Pattern pattern = Pattern.compile(regex);		// pattern to match
		Matcher matcher = pattern.matcher(template);
		boolean found = false;
		if ( matcher.find() ) {
			final int end = matcher.end();
			int i;
			int field = 0;
			char[] newMark = new char[mask.length];
			boolean conflict = false;
			int groupStart;

			for ( i = groupStart = matcher.start(); i < end; ++i ) {

				if ( separatorChar == 0 ) {
					if ( i == matcher.end(field + 1) ) {	// note: end(0) is special case representing the whole pattern
						// first char after end of group
						++field;								// inc field for next group
						groupStart = matcher.start(field + 1);	// set group start index
					}
				}
				else {
					if ( template.charAt(i) == separatorChar ) {
						++field;				// inc field for next group
						groupStart = i + 1;		// set group start index
					}
				}
				
				if ( i >= groupStart ) {
					if ( mask[i] != 0 ) {
						conflict = true;		// mask already set, so forget it
						break;
					}
					
					newMark[i] = markOrder[field];		// within match for group, so set new mask
				}
				else
					newMark[i] = EXACT_MATCH;
			}

			if ( !conflict ) {
				// no conflict between previous mask and new bits set, so update the mask
				for ( i = mask.length - 1; i >= 0; --i ) {
					if ( newMark[i] != 0 )
						mask[i] = newMark[i];
				}
			}
			
			found = true;
		}
		return found;
	}
	
	
	
	
	/**
	 * Get the year/month/day order for the user default locale
	 * @return
	 */
	private static YearMonthDayOrder getYearMonthDayOrder(Context context) {
		
		if ( yearMthDay == null ) {
			// need to use the application context as java.text.DateFormat will return Locale.US format by default
			DateTimeFormat df = new DateTimeFormat(context, DateTimeFormat.SHORT, DateTimeFormat.FORMAT_DATE);
			GregorianCalendar calendar = new GregorianCalendar(1970, GregorianCalendar.FEBRUARY, 1);	// 1st Feb 1970
			String str = df.format( calendar.getTime() );
			
			int yr = str.indexOf("1970");
			if ( yr < 0 )
				yr = str.indexOf("70");
			int mth = str.indexOf("2");
			int day = str.indexOf("1");
			
			if ( (day < yr) && (day < mth) )
				yearMthDay = YearMonthDayOrder.DMY;
			else if ( (mth < day) && (day < yr) )
				yearMthDay = YearMonthDayOrder.MDY;
			else 
				yearMthDay = YearMonthDayOrder.YMD;
		}
		return yearMthDay;
	}
	
	/**
	 * Get the year/month/day order for the user default locale
	 * @return
	 */
	private static char[] getYearMonthDayOrderMarkArray(Context context) {
		
		YearMonthDayOrder ymdOrder = getYearMonthDayOrder(context);
		
		if ( yearMthDayMarkArray[0] == 0 ) {
			if ( ymdOrder == YearMonthDayOrder.DMY ) {
				yearMthDayMarkArray[0] = DAY_IN_MONTH;
				yearMthDayMarkArray[1] = MONTH_IN_YEAR_NUM;
				yearMthDayMarkArray[2] = YEAR;
			}
			else if ( ymdOrder == YearMonthDayOrder.MDY ) {
				yearMthDayMarkArray[0] = MONTH_IN_YEAR_NUM;
				yearMthDayMarkArray[1] = DAY_IN_MONTH;
				yearMthDayMarkArray[2] = YEAR;
			}
			else if ( ymdOrder == YearMonthDayOrder.YMD ) {
				yearMthDayMarkArray[0] = YEAR;
				yearMthDayMarkArray[1] = MONTH_IN_YEAR_NUM;
				yearMthDayMarkArray[2] = DAY_IN_MONTH;
			}
		}
		return yearMthDayMarkArray;
	}

	
	/**
	 * Create an exact match regular expression for the specified string.
	 * @param template
	 * @return
	 */
	private static String exactMatchRegex( String template ) {
		StringBuffer sb = new StringBuffer();
		final int N = template.length();
		for ( int i = 0; i < N; ++i ) {
			sb.append( exactMatchRegex(template.charAt(i)) );
		}
		return sb.toString();
	}
	
	/**
	 * Return the string representing the specified character for an exact match regular expression.
	 * @param chr
	 * @return
	 */
	private static String exactMatchRegex( char chr ) {
		String result = String.valueOf(chr);
		if ( SPECIAL_CHARS.contains(result) )
			return new String(ESCAPE_CHAR_STR + result);
		else
			return result;
	}
	
}
