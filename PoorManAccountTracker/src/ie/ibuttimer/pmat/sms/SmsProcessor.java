/**
 * 
 */
package ie.ibuttimer.pmat.sms;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import ie.ibuttimer.pmat.R;
import ie.ibuttimer.pmat.db.Account;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.Payee;
import ie.ibuttimer.pmat.db.Transaction;
import ie.ibuttimer.pmat.db.User;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.pmat.util.DateTimeFormat;
import ie.ibuttimer.pmat.util.KeyValueSplitter;
import ie.ibuttimer.pmat.util.Logger;
import ie.ibuttimer.pmat.util.TwoWayHashMap;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;


/**
 * Class to process SMS messages
 * @author Ian Buttimer
 *
 */
public class SmsProcessor {
	

	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String SENT_SMS = "ie.ibuttimer.pmat.SMS_SENT";
	public static final String DELIVERED_SMS = "ie.ibuttimer.pmat.SMS_DELIVERED";
	public static final String SMS_PAYLOAD = "SMS_PAYLOAD";


	/** Key to use to specify the transaction id in a key/value pair SMS.<br>
	 *  E.g. i=123 */
	public static final String SMSKEY_TRANSID = "i";
	/** Key to use to specify the transaction type in a key/value pair SMS.<br>
	 *  E.g. k={+/-/*} */
	public static final String SMSKEY_TRANSTYPE = "k";
	/** Key to use to specify the transaction account in a key/value pair SMS.<br>
	 *  E.g. a="account nickname" */
	public static final String SMSKEY_ACCOUNT = "a";
	/** Key to use to specify the transaction secondary account in a key/value pair SMS.<br>
	 *  E.g. b="account nickname" */
	public static final String SMSKEY_2ND_ACCOUNT = "b";
	/** Key to use to specify the transaction amount in a key/value pair SMS.<br>
	 *  E.g. #=250.21 */
	public static final String SMSKEY_AMOUNT = "#";
	/** Key to use to specify the transaction payee/payer in a key/value pair SMS.<br>
	 *  E.g. p="joe bloggs" */
	public static final String SMSKEY_PAYEE = "p";
	/** Key to use to specify the transaction sent date/time in a key/value pair SMS.<br>
	 *  E.g. s=DDMMYYhhmm */
	public static final String SMSKEY_SEND_DATE = "s";
	/** Key to use to specify the transaction receive date/time in a key/value pair SMS.<br>
	 *  E.g. r=DDMMYYhhmm */
	public static final String SMSKEY_RECV_DATE = "r";
	/** Key to use to specify the transaction status in a key/value pair SMS.<br>
	 *  E.g. t=1<br> 
	 *  <b>Note:</b> See Transaction.TRANSSTATUS_INVALID etc. for valid status values */
	public static final String SMSKEY_STATUS = "t";
	/** Key to use to specify the transaction reference in a key/value pair SMS.<br>
	 *  E.g. f=12 */
	public static final String SMSKEY_REF = "f";
	/** Key to use to specify the transaction note in a key/value pair SMS.<br>
	 *  E.g. n="this is a note" */
	public static final String SMSKEY_NOTE = "n";
	/** Key to use to specify the transaction category in a key/value pair SMS.<br>
	 *  E.g. c=71 */
	public static final String SMSKEY_CATEGORY = "c";

	
	/** Value to use to specify a credit transaction in a key/value pair SMS.<br>
	 *  E.g. k=+ */
	public static final String SMSKEY_TRANSTYPE_CREDIT = "+";
	/** Value to use to specify a debit transaction in a key/value pair SMS.<br>
	 *  E.g. k=- */
	public static final String SMSKEY_TRANSTYPE_DEBIT = "-";
	/** Value to use to specify a transfer transaction in a key/value pair SMS.<br>
	 *  E.g. k=* */
	public static final String SMSKEY_TRANSTYPE_TRANSFER = "*";

	
	/* A simple message takes the following forms:
	 * 1. Credit transaction - raw 
	 *    "+ <amt> <a/c> <payer>" where
	 *    	<a/c> 	: nickname of account to credit
	 *    	<amt> 	: amount to credit the account
	 *    	<payer>	: payer name
	 * 2. Debit transaction - raw
	 *    "- <amt> <a/c> <payee>" where
	 *    	<a/c> 	: nickname of account to debit
	 *    	<amt> 	: amount to debit the account
	 *    	<payee>	: payee name
	 * 3. Transfer transaction - raw
	 *    "* <amt> <src a/c> <dest a/c> <payee>" where
	 *    	<src a/c> 	: nickname of source account
	 *    	<amt> 		: amount to transfer
	 *    	<dest a/c>	: nickname of destination account
	 *    	<payee>		: payee name
	 *    
	 * 4. Credit transaction - plain text 
	 *    "get <amt> from <payer> to <a/c>"
	 *    "get <amt> to <a/c> from <payer>"
	 * 5. Debit transaction - plain text
	 *    "pay <amt> from <a/c> to <payee>"
	 *    "pay <amt> to <payee> from <a/c>"
	 * 6. Transfer transaction - plain text
	 *    "transfer <amt> from <src a/c> to <dest a/c> as <payee>"
	 *    "transfer <amt> to <dest a/c> from <src a/c> as <payee>"
	 * 
	 * and a key/value message  takes the forms: 
	 * 1. Credit transaction 
	 *    "k=+ a=<a/c> #=<amt> p=<payer> ..."
	 * 2. Debit transaction 
	 *    "k=- a=<a/c> #=<amt> p=<payee> ..."
	 * 3. Transfer transaction 
	 *    "k=* a=<src a/c> #=<amt> p=<dest a/c> ..."
	 */

	/** min required entries in a simple credit/debit SMS and the order they MUST appear in */
	private static final ArrayList<String> rawCDSmsMinEntryInOrder = new ArrayList<String>();
	static {
		rawCDSmsMinEntryInOrder.add(SMSKEY_TRANSTYPE);
		rawCDSmsMinEntryInOrder.add(SMSKEY_AMOUNT);
		rawCDSmsMinEntryInOrder.add(SMSKEY_ACCOUNT);
		rawCDSmsMinEntryInOrder.add(SMSKEY_PAYEE);
	}
	/** min required entries in a simple transfer SMS and the order they MUST appear in<br>
	 * <b>NOTE:</b> To simplify code, first elements need to match <code>rawCDSmsMinEntryInOrder</code>.	 */
	private static final ArrayList<String> rawTSmsMinEntryInOrder = new ArrayList<String>();
	static {
		rawTSmsMinEntryInOrder.add(SMSKEY_TRANSTYPE);
		rawTSmsMinEntryInOrder.add(SMSKEY_AMOUNT);
		rawTSmsMinEntryInOrder.add(SMSKEY_ACCOUNT);
		rawTSmsMinEntryInOrder.add(SMSKEY_2ND_ACCOUNT);
		rawTSmsMinEntryInOrder.add(SMSKEY_PAYEE);
	}
	/** all possible SMS entries<br>
	 * <b>NOTE:</b> To simplify code, first elements need to match <code>rawCDSmsMinEntryInOrder</code>.	 */
	private static final ArrayList<String> allSmsEntries = new ArrayList<String>();
	static {
		final int N = rawTSmsMinEntryInOrder.size();
		for ( int i = 0; i < N; ++i )
			allSmsEntries.add(rawTSmsMinEntryInOrder.get(i));
		allSmsEntries.add(SMSKEY_TRANSID);
		allSmsEntries.add(SMSKEY_SEND_DATE);
		allSmsEntries.add(SMSKEY_RECV_DATE);
		allSmsEntries.add(SMSKEY_STATUS);
		allSmsEntries.add(SMSKEY_REF);
		allSmsEntries.add(SMSKEY_NOTE);
		allSmsEntries.add(SMSKEY_CATEGORY);
	}
	
	/** bit mask of min required entries in a simple credit/debit SMS */
	private static final BitSet minMaskCreditDebit = new BitSet();
	static {
		final int N = rawCDSmsMinEntryInOrder.size();
		for ( int i = 0; i < N; ++i )
			minMaskCreditDebit.set(allSmsEntries.indexOf( rawCDSmsMinEntryInOrder.get(i) ));
	}
	/** bit mask of min required entries in a simple transfer SMS */
	private static final BitSet minMaskTransfer = new BitSet();
	static {
		final int N = rawTSmsMinEntryInOrder.size();
		for ( int i = 0; i < N; ++i )
			minMaskTransfer.set(allSmsEntries.indexOf( rawTSmsMinEntryInOrder.get(i) ));
	}

	/** map of SMS keys to database columns */
	private static TwoWayHashMap<String, String> smsDatabaseKeyMap = new TwoWayHashMap<String, String>();
	static {
		String[][] fields = new String[][] {
				{ SMSKEY_TRANSTYPE,		DatabaseManager.TRANSACTION_TYPE },
				{ SMSKEY_AMOUNT,		DatabaseManager.TRANSACTION_AMOUNT },
				{ SMSKEY_ACCOUNT,		DatabaseManager.TRANSACTION_SRC },
				{ SMSKEY_2ND_ACCOUNT,	DatabaseManager.TRANSACTION_DEST },
				{ SMSKEY_PAYEE,			DatabaseManager.TRANSACTION_PAYEE },
				{ SMSKEY_TRANSID,		DatabaseManager.TRANSACTION_ID },
				{ SMSKEY_SEND_DATE,		DatabaseManager.TRANSACTION_SENDDATE },
				{ SMSKEY_RECV_DATE,		DatabaseManager.TRANSACTION_RECVDATE },
				{ SMSKEY_STATUS,		DatabaseManager.TRANSACTION_STATUS },
				{ SMSKEY_REF,			DatabaseManager.TRANSACTION_REF },
				{ SMSKEY_NOTE,			DatabaseManager.TRANSACTION_NOTE },
				{ SMSKEY_CATEGORY,		DatabaseManager.TRANSACTION_CATEGORY },
		};
		for ( int i = fields.length - 1; i >= 0; --i )
			smsDatabaseKeyMap.put(fields[i][0], fields[i][1]);
	}

	/** map of SMS transaction keys to database transaction types */
	private static TwoWayHashMap<String, Integer> transactionKeyMap = new TwoWayHashMap<String, Integer>();
	static {
		String[] keys = new String[] {
				SMSKEY_TRANSTYPE_CREDIT,		SMSKEY_TRANSTYPE_DEBIT,			SMSKEY_TRANSTYPE_TRANSFER
		};
		int[] values = new int[] {
				Transaction.TRANSACTION_CREDIT,	Transaction.TRANSACTION_DEBIT,	Transaction.TRANSACTION_TRANSFER
		};
		for ( int i = keys.length - 1; i >= 0; --i )
			transactionKeyMap.put(keys[i], values[i]);
	}
	

	public static final String SMS_DMYHM_TEMPLATE = "ddMMyyHHmm";	// day in month/month in year/year/hour in day (0-23)/minute in hour
	public static final String SMS_DMY_TEMPLATE = "ddMMyy";			// day in month/month in year/year
	public static final String SMS_HM_TEMPLATE = "HHmm";			// hour in day (0-23)/minute in hour
	public static final String[] SMS_DATE_FORMATS = new String[] {
		SMS_DMYHM_TEMPLATE, SMS_DMY_TEMPLATE, SMS_HM_TEMPLATE
	};
	
	
	private static final int PRESENT = 0x1;
	private static final int REQUIRED = 0x2;
	private static final int PRESENT_REQUIRED = PRESENT | REQUIRED;

	private Resources resrc;
	private Context context;
	private static SmsProcessor instance = null;	// singleton instance variable
	

	/**
	 * Constructor
	 */
	private SmsProcessor() {
		super();
	}


	/**
	 * Get a singleton instance of the SmsProcessor class
	 * @return
	 */
	public static SmsProcessor getInstance(Context context) {
		if ( instance == null )
			instance = new SmsProcessor();
		instance.setContext(context);
		return instance;
	}

	private void setContext(Context context) {
		this.context = context;
		resrc = context.getResources();
	}

	/**
	 * Process an intent broadcast
	 * @param context	- The Context in which the receiver is running.
	 * @param intent	- The Intent being received.
	 */
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(SMS_RECEIVED)) {

			setContext(context);
			
			Bundle bundle = intent.getExtras();
			if (bundle != null) { 
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++)
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				
				// get marker ("PMAT:") for simple sms i.e. just account/amount/payee
				String simpleMarker = resrc.getString(R.string.simple_sms_marker);
				int simpleLen = simpleMarker.length();
				// get marker ("PMAT/") for key/value sms i.e. key/value pairs possibly specifying all data
				String keyValMarker = resrc.getString(R.string.key_value_sms_marker);
				int keyValLen = keyValMarker.length();

				for (SmsMessage message : messages) {

					String msgBody = message.getMessageBody().trim();
					int msgLen = msgBody.length();
					LinkedHashMap<String, String> map = null;

					// split fields on spaces, it follows a specific order i.e. no key/value pairs but has default order
					KeyValueSplitter simpleSplitter = new KeyValueSplitter(null, " ", 
							null, KeyValueSplitter.DEFAULT_UNIFIER_REGEX, null );
					// split fields on spaces & key/value on '=' but no default order
					KeyValueSplitter keyValSplitter = new KeyValueSplitter(null, " ", 
							KeyValueSplitter.DEFAULT_KV_ASSIGN_REGEX, KeyValueSplitter.DEFAULT_UNIFIER_REGEX, null );
					
					int transactionType = Transaction.TRANSACTION_INVALID;


					// process as a simple message (e.g. "<a/c> <amt> <payee>") if possible
					if ( msgLen > simpleLen ) {
						if ( simpleMarker.compareToIgnoreCase(msgBody.substring(0, simpleLen)) == 0 ) {

							String payload = msgBody.substring(simpleLen, msgLen).trim();

							Logger.d("Received SMS [" + payload + "]");

							// convert a plain text sms to a key/value one
							String convertedPayload = convertPlainToKeyValue( payload );
							if ( convertedPayload != null ) {

								Logger.d("Converted SMS [" + convertedPayload + "]");

								keyValSplitter.setSource(convertedPayload);
								map = keyValSplitter.splitString();
							}
							else {
								// split simple payload
								simpleSplitter.setSource(payload);
								map = simpleSplitter.splitString();
							}
						}
					}

					// process as a key/value message  (e.g. "a=<a/c> #=<amt> p=<payee> ...") if possible
					if ( (map == null) && (msgLen > keyValLen) ) {

						if ( keyValMarker.compareToIgnoreCase(msgBody.substring(0, keyValLen)) == 0 ) {

							String payload = msgBody.substring(keyValLen, msgLen).trim();

							Logger.d("Received k/v SMS [" + payload + "]");

							// split key/val payload
							keyValSplitter.setSource(payload);
							map = keyValSplitter.splitString();
						}
					}
					
					// process the values extracted from the sms
					if ( map != null ) {
						if ( map.size() > 0 ) {

							ContentValues values = new ContentValues();
							String str;
							BitSet fields = new BitSet();
							BitSet minReqs = null;

							// add origin data
							String from = message.getOriginatingAddress();
							if ( from.startsWith("+") )
								from = from.substring(1);	// drop leading '+' for international number
							values.put(DatabaseManager.TRANSACTION_ORIGIN, Long.valueOf(from).longValue());

							// add transaction id data
							addStringField(map, SMSKEY_TRANSID, values, DatabaseManager.TRANSACTION_ORIGIN_ID, fields);

							// add transaction type data
							if ( (str = getValueFromMap(map, SMSKEY_TRANSTYPE)) == null )		// get using raw key name
								str = getValueFromMap(map, KeyValueSplitter.makeDefaultKey(0));	// get using default key name
							if ( str != null ) {
								transactionType = transactionKeyMap.getValue(str);

								// set min required mask
								switch ( transactionType ) {
									case Transaction.TRANSACTION_CREDIT:
									case Transaction.TRANSACTION_DEBIT:
										minReqs = minMaskCreditDebit;
										break;
									case Transaction.TRANSACTION_TRANSFER:
										minReqs = minMaskTransfer;
										break;
								}
								
								// add to ContentValues
								if ( transactionType != Transaction.TRANSACTION_INVALID ) {
									values.put(DatabaseManager.TRANSACTION_TYPE, transactionType);
									fields.set(allSmsEntries.indexOf( SMSKEY_TRANSTYPE ));		// set bit in mask
								}
								// else not valid so ignore
							}
							
							// add amount data
							addStringField(map, SMSKEY_AMOUNT, values, DatabaseManager.TRANSACTION_AMOUNT, fields);
							
							// add account data
							if ( (str = getValueFromMap(map, SMSKEY_ACCOUNT)) == null )	{		// get using raw key name
								// get using default key name
								str = getValueFromMap(map, KeyValueSplitter.makeDefaultKey( getDefaultKeyIndex(SMSKEY_ACCOUNT,transactionType) ));
							}
							if ( str != null ) {
								int accIdx = findAccount(str);
								if ( accIdx != -1 ) {
									String accField;
									switch ( transactionType ) {
										case Transaction.TRANSACTION_CREDIT:
											accField = DatabaseManager.TRANSACTION_DEST;
											break;
										case Transaction.TRANSACTION_DEBIT:
										case Transaction.TRANSACTION_TRANSFER:
											accField = DatabaseManager.TRANSACTION_SRC;
											break;
										default:
											accField = null;
											break;
									}

									// add to ContentValues
									if ( accField != null ) {
										values.put(accField, accIdx);
										fields.set(allSmsEntries.indexOf( SMSKEY_ACCOUNT ));		// set bit in mask
									}
								}
								// else not a valid account so ignore
							}
							
							// add secondary account data
							if ( (str = getValueFromMap(map, SMSKEY_2ND_ACCOUNT)) == null )	{		// get using raw key name
								// get using default key name
								str = getValueFromMap(map, KeyValueSplitter.makeDefaultKey( getDefaultKeyIndex(SMSKEY_2ND_ACCOUNT,transactionType) ));
							}
							if ( str != null ) {
								int accIdx = findAccount(str);
								if ( accIdx != -1 ) {
									String accField;
									switch ( transactionType ) {
										case Transaction.TRANSACTION_TRANSFER:
											accField = DatabaseManager.TRANSACTION_SRC;
											break;
										default:
											accField = null;
											break;
									}

									// add to ContentValues
									if ( accField != null ) {
										values.put(accField, accIdx);
										fields.set(allSmsEntries.indexOf( SMSKEY_2ND_ACCOUNT ));		// set bit in mask
									}
								}
								// else not a valid account so ignore
							}
							
							// add payee/payer data
							if ( (str = getValueFromMap(map, SMSKEY_PAYEE)) == null )	{		// get using raw key name
								// get using default key name
								str = getValueFromMap(map, KeyValueSplitter.makeDefaultKey( getDefaultKeyIndex(SMSKEY_PAYEE,transactionType) ));
							}
							if ( str != null ) {
								int index = findPayee(str);
								if ( index == -1 )
									index = addPayee(str);	// payee/payer not found, so add a new one
								if ( index != -1 ) {
									values.put(DatabaseManager.TRANSACTION_PAYEE, index);
									fields.set(allSmsEntries.indexOf( SMSKEY_PAYEE ));		// set bit in mask
								}
								// else payee/payer not found so ignore
							}
							
							// add send timestamp data
							addDateField(map, SMSKEY_SEND_DATE, values, DatabaseManager.TRANSACTION_SENDDATE, fields);
							
							// add receive timestamp data
							addDateField(map, SMSKEY_RECV_DATE, values, DatabaseManager.TRANSACTION_RECVDATE, fields);
							
							// add transaction status
							if ( (str = getValueFromMap(map, SMSKEY_STATUS)) != null ) {
								int status = Integer.parseInt(str);
								if ( Transaction.isValidStatus(status) ) {
									values.put(DatabaseManager.TRANSACTION_STATUS, status);
									fields.set(allSmsEntries.indexOf( SMSKEY_STATUS ));		// set bit in mask
								}
								// else invalid so ignore
							}
							
							// add transaction reference
							addStringField(map, SMSKEY_REF, values, DatabaseManager.TRANSACTION_REF, fields);
							
							// add transaction note
							addStringField(map, SMSKEY_NOTE, values, DatabaseManager.TRANSACTION_NOTE, fields);
							
							// add transaction category
							if ( (str = getValueFromMap(map, SMSKEY_CATEGORY)) != null ) {
								if ( doesCategoryExist(str) ) {
									values.put(DatabaseManager.TRANSACTION_CATEGORY, str);
									fields.set(allSmsEntries.indexOf( SMSKEY_CATEGORY ));		// set bit in mask
								}
								// else category not found so ignore
							}

							// if we have all the necessary fields so add the transaction
							fields.and(minReqs);
							if ( fields.equals(minReqs) ) {
								// if no status included, assume its complete
								if ( !fields.get(allSmsEntries.indexOf(SMSKEY_STATUS)) )
									values.put(DatabaseManager.TRANSACTION_STATUS, Transaction.TRANSSTATUS_COMPLETE);

								try {
									Uri result = context.getContentResolver().insert(DatabaseManager.TRANSACTION_URI, values);
									if ( result != null ) {
										Logger.d("Added " + values.toString());
										context.sendBroadcast(new Intent(Constants.UPDATE_SNAPSHOT_ACTION));
									}
								}
								catch ( SQLException e ) {
									Logger.d("Unable to add " + values.toString());
								}
							}
							else
								Logger.d("Empty key/val map");
						}
						else
							Logger.d("Empty key/val map");
					}
					else
						Logger.d("No key/val map");
				}
			}
		}
	}


	/**
	 * Get the the index of the minimum required entry list
	 * @param key
	 * @param transactionType
	 * @return
	 */
	private int getDefaultKeyIndex( String key, int transactionType ) {

		int index;
		switch ( transactionType ) {
			case Transaction.TRANSACTION_CREDIT:
			case Transaction.TRANSACTION_DEBIT:
				index = rawCDSmsMinEntryInOrder.indexOf(key);
				break;
			case Transaction.TRANSACTION_TRANSFER:
				index = rawTSmsMinEntryInOrder.indexOf(key);
				break;
			default:
				index = -1;
				break;
		}
		return index;
	}
	
	
	/**
	 * Convert transaction type value to a SMS raw transaction type 
	 * @param type	Transaction type value, e.g. Transaction.TRANSACTION_CREDIT
	 * @return		raw transaction type, e.g. "+"
	 */
	private String transactionTypeToSmsCommand( int type ) {
		String transactionType;
		if ( type == Transaction.TRANSACTION_CREDIT )
			transactionType = SMSKEY_TRANSTYPE_CREDIT;
		else if ( type == Transaction.TRANSACTION_DEBIT )
			transactionType = SMSKEY_TRANSTYPE_DEBIT;
		else if ( type == Transaction.TRANSACTION_TRANSFER )
			transactionType = SMSKEY_TRANSTYPE_TRANSFER;
		else
			transactionType = "";
		return transactionType;
	}
	
	/**
	 * Convert an SMS raw transaction type to a transaction 
	 * @param str	raw transaction type, e.g. "+"
	 * @return		Transaction type value, e.g. Transaction.TRANSACTION_CREDIT
	 */
	private int smsCommandToTransactionType( String str ) {
		int transactionType;
		if ( str.compareTo(SMSKEY_TRANSTYPE_CREDIT) == 0 )
			transactionType = Transaction.TRANSACTION_CREDIT;
		else if ( str.compareTo(SMSKEY_TRANSTYPE_DEBIT) == 0 )
			transactionType = Transaction.TRANSACTION_DEBIT;
		else if ( str.compareTo(SMSKEY_TRANSTYPE_TRANSFER) == 0 )
			transactionType = Transaction.TRANSACTION_TRANSFER;
		else
			transactionType = Transaction.TRANSACTION_INVALID;
		return transactionType;
	}
	
	
	/**
	 * Make a key/value assignment text
	 * @param key		key name
	 * @param value		value
	 * @return
	 */
	private static String makeKeyValueAssignment(String key, String value) {
		return key + KeyValueSplitter.DEFAULT_KV_ASSIGN + makeUnifiedValue(value);
	}

	/**
	 * Make a unified value text
	 * @param value		value
	 * @return
	 */
	private static String makeUnifiedValue(String value) {
		if ( value.indexOf(' ') >= 0 ) {
			// contains a space, so unify text
			value = KeyValueSplitter.DEFAULT_UNIFIER + value + KeyValueSplitter.DEFAULT_UNIFIER;
		}
		return value;
	}

	
	private static final int[][] plainSmsCmdTransType = new int[][] {
		// command							transaction type					plain text reports
		{ R.string.sms_credit_command, 		Transaction.TRANSACTION_CREDIT,		R.string.sms_credit_report },
		{ R.string.sms_debit_command, 		Transaction.TRANSACTION_DEBIT,		R.string.sms_debit_report },		
		{ R.string.sms_transfer_command,	Transaction.TRANSACTION_TRANSFER,	R.string.sms_transfer_report },
	};
	private static final int plainSmsCmdIdx = 0;
	private static final int transTypeIdx = 1;
	private static final int plainReportIdx = 2;
	
	/**
	 * Convert a plain text transaction sms to a key/value transaction sms
	 * @param msg	Message to convert
	 * @return		Raw transaction message string
	 */
	private String convertPlainToKeyValue( String msg ) {

		String str = msg.trim().toLowerCase(Locale.US);
		String result = null;
		int transactionType = Transaction.TRANSACTION_INVALID;

		// check if its a plain text sms
		for ( int i = plainSmsCmdTransType.length - 1; i >= 0; -- i ) {
			if ( str.startsWith( resrc.getString(plainSmsCmdTransType[i][plainSmsCmdIdx]).toLowerCase(Locale.US) ) ) {
				transactionType = plainSmsCmdTransType[i][transTypeIdx];
				break;
			}
		}

		if ( transactionType != Transaction.TRANSACTION_INVALID ) {
			// its a plain text sms, so split it into words
			LinkedHashMap<String, String> map = KeyValueSplitter.splitKeyValueString(msg, " ", null, KeyValueSplitter.DEFAULT_UNIFIER_REGEX, null);
			final int N = map.size();
			
			// add transaction type
			StringBuffer sb = new StringBuffer(makeKeyValueAssignment(SMSKEY_TRANSTYPE, transactionKeyMap.getKey(Integer.valueOf(transactionType))));

			/* scan words for additional fields
			 * the basic form is "<trans type> <amt> from <a/c> to <a/c> as <payee>"
			 * it must start with "<trans type> <amt>" but the "from <a/c>", "to <a/c>", "as <payee>" field positions can be alternated
			 */
			String field = SMSKEY_AMOUNT;
			String toCmd = resrc.getString(R.string.sms_to_command);
			String fromCmd = resrc.getString(R.string.sms_from_command);
			String asCmd = resrc.getString(R.string.sms_as_command);
			for ( int i = 1; i < N; ++ i) {
				str = map.get(KeyValueSplitter.makeDefaultKey(i));	// get words based on default key names
				if ( i % 2 == 0 ) {
					// command
					if ( str.compareToIgnoreCase( toCmd ) == 0 ) {
						switch ( transactionType ) {
							case Transaction.TRANSACTION_CREDIT:	// e.g. "get <amt> from <payer> to <a/c>"
								field = SMSKEY_ACCOUNT;
								break;
							case Transaction.TRANSACTION_DEBIT:		// e.g. "pay <amt> from <a/c> to <payee>"
								field = SMSKEY_PAYEE;
								break;
							case Transaction.TRANSACTION_TRANSFER:	// e.g. "transfer <amt> from <src a/c> to <dest a/c> as <payee>"
								field = SMSKEY_2ND_ACCOUNT;
								break;
						}
					}
					else if ( str.compareToIgnoreCase( fromCmd ) == 0 ) {
						switch ( transactionType ) {
							case Transaction.TRANSACTION_CREDIT:	// e.g. "get <amt> from <payer> to <a/c>"
								field = SMSKEY_PAYEE;
								break;
							case Transaction.TRANSACTION_DEBIT:		// e.g. "pay <amt> from <a/c> to <payee>"
							case Transaction.TRANSACTION_TRANSFER:	// e.g. "transfer <amt> from <src a/c> to <dest a/c> as <payee>"
								field = SMSKEY_ACCOUNT;
								break;
						}
					}
					else if ( str.compareToIgnoreCase( asCmd ) == 0 ) {
						switch ( transactionType ) {
							case Transaction.TRANSACTION_TRANSFER:	// e.g. "transfer <amt> from <src a/c> to <dest a/c> as <payee>"
								field = SMSKEY_PAYEE;
								break;
						}
					}
				}
				else {
					// value
					sb.append(" " + makeKeyValueAssignment(field, str));
				}
			}
			
			result = sb.toString();
		}

		return result;
	}
	
	
	/**
	 * Convert a transaction to a key/value transaction sms
	 * @param msg	Message to convert
	 * @return		Raw transaction message string
	 */
	private String convertTransactionToKeyValue( Transaction trans ) {

		StringBuffer sb = new StringBuffer();
		final int N = allSmsEntries.size();
		for ( int i = 0; i < N; ++i ) {
			String smsKey = allSmsEntries.get(i);					// sms is the 'key' in smsDatabaseKeyMap
			String dbCol = (String) smsDatabaseKeyMap.get(smsKey);	// db is the 'value' in smsDatabaseKeyMap
			
			String smsValue;

			boolean sendDate = smsKey.equals(SMSKEY_SEND_DATE);
			boolean recvDate = smsKey.equals(SMSKEY_RECV_DATE);
			if ( sendDate || recvDate ) {
				// field is returned as a time stamp
				Calendar cal = (sendDate ? trans.getTransSendDate() : trans.getTransRecvDate());
				smsValue = DateTimeFormat.makeTimestamp(cal.getTime(), SMS_DMYHM_TEMPLATE, "UTC", Locale.US);
			}
			else {
				smsValue = trans.fieldToString(dbCol);
			}

			if ( !TextUtils.isEmpty(smsValue) ) {
				if ( i > 0 )
					sb.append(" ");
				sb.append(makeKeyValueAssignment(smsKey, smsValue));
			}
		}
		
		return sb.toString();
	}


	/**
	 * Convert a transaction to a plain text sms
	 * @param trans	- transaction to convert
	 * @return		Raw transaction message string
	 */
	private String convertTransactionToPlainTextSms( Transaction trans ) {

		String report = null;
		int transactionType = trans.getTransType();

		// get the transaction summary template
		for ( int i = plainSmsCmdTransType.length - 1; i >= 0; -- i ) {
			if ( transactionType == plainSmsCmdTransType[i][transTypeIdx] ) {
				report = resrc.getString(plainSmsCmdTransType[i][plainReportIdx]);
				break;
			}
		}

		if ( report != null ) {
		
			ContentResolver cr = context.getContentResolver();
			HashMap<String, String> map = new HashMap<String, String>();
			
			// create the map with all the required transaction info
			HashMap<String, Account> mapAccounts = trans.getAffectedAccounts(cr);
			String nameDest;
			if ( mapAccounts.containsKey(DatabaseManager.TRANSACTION_DEST) )
				nameDest = mapAccounts.get(DatabaseManager.TRANSACTION_DEST).getAccountName();
			else
				nameDest = "";
			String nameSrc;
			if ( mapAccounts.containsKey(DatabaseManager.TRANSACTION_SRC) )
				nameSrc = mapAccounts.get(DatabaseManager.TRANSACTION_SRC).getAccountName();
			else
				nameSrc = "";

			switch ( transactionType ) {
				case Transaction.TRANSACTION_CREDIT:
					map.put(SmsMessageFactory.SMS_INPUT_FIELD_DESTINATION_VALUE, nameDest);
					map.put(SmsMessageFactory.SMS_INPUT_FIELD_ACCOUNT_VALUE, nameDest);
					break;
				case Transaction.TRANSACTION_DEBIT:		
				case Transaction.TRANSACTION_TRANSFER:
					map.put(SmsMessageFactory.SMS_INPUT_FIELD_SOURCE_VALUE, nameSrc);
					map.put(SmsMessageFactory.SMS_INPUT_FIELD_ACCOUNT_VALUE, nameSrc);
					if ( transactionType == Transaction.TRANSACTION_TRANSFER ) {
						map.put(SmsMessageFactory.SMS_INPUT_FIELD_DESTINATION_VALUE, nameDest);
					}
					break;
			}

			map.put(SmsMessageFactory.SMS_INPUT_FIELD_AMOUNT_VALUE, trans.fieldToString(DatabaseManager.TRANSACTION_AMOUNT, context.getContentResolver()));
			
			Payee payee= Payee.loadFromProvider(cr, trans.getTransPayee());
			if ( payee != null )
				map.put(SmsMessageFactory.SMS_INPUT_FIELD_PAYEE_VALUE, payee.getName());

			// generate report
			SmsMessageFactory factory = new SmsMessageFactory(report);
			report = factory.generateSms(map);
		}

		return report;
	}

	/**
	 * Generate a balance plain text sms
	 * @param accounts	- accounts to include in sms
	 * @return			message string
	 */
	private String generateBalancePlainTextSms( Account[] accounts ) {

		StringBuffer sb = new StringBuffer();
		String template;
		final int N = accounts.length;
		
		if ( N == 1 )
			template = resrc.getString(R.string.sms_balance_report);
		else if ( N > 1 ) { 
			sb.append(resrc.getString(R.string.sms_balance_snap_head));
			template = resrc.getString(R.string.sms_balance_snap_item);
		}
		else
			return "";
		
		SmsMessageFactory factory = new SmsMessageFactory(template);
		int[] fieldTypes = new int[] { SmsMessageFactory.SMS_TEMPLATE_FIELD_TEXT };
		HashMap<String, String> map = new HashMap<String, String>();
		for ( int i = 0; i < N; ++i ) {
			factory.clearFieldValuesExcluding(fieldTypes);

			// create the map with all the required info
			if ( accounts[i] != null ) {
				map.put(SmsMessageFactory.SMS_INPUT_FIELD_ACCOUNT_VALUE, accounts[i].getAccountName());
				map.put(SmsMessageFactory.SMS_INPUT_FIELD_AMOUNT_VALUE, 
						accounts[i].fieldToString(DatabaseManager.ACCOUNT_AVAILBAL, context.getContentResolver()));

				sb.append( factory.generateSms(map) );
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Convert and add a date/time entry from <code>map</code> to <code>values</code>.<br>
	 * The following forms are supported:
	 * <ul>
	 * <li>DDMMYYhhmm:<br>	date and time</li>
	 * <li>DDMMYY:<br>		date, time defaults to 00:00</li>
	 * <li>hhmm:<br>		time, date defaults to current date</li>
	 * </ul>
	 * @param map		LinkedHashMap to extract field from
	 * @param key		Key in LinkedHashMap to extract
	 * @param values	ContentValues to add to
	 * @param field		Field to add as in ContentValues
	 * @param mask		BitSet to record entry in
	 * @return			PRESENT if added, 0 otherwise
	 */
	private int addDateField(LinkedHashMap<String, String> map, String key, ContentValues values, String field, BitSet mask) {
		
		int result = 0;
		String timestamp = getValueFromMap(map, key);
		if ( timestamp != null ) {
			
			Date date = DateTimeFormat.parseTimestamp(timestamp, SMS_DATE_FORMATS, null, Locale.US);

			if ( date != null ) {
				timestamp = DatabaseManager.makeDatabaseTimestamp(date);
				values.put(field, timestamp);			// add to ContentValues
				mask.set(allSmsEntries.indexOf( key ));	// set bit in mask
				result = PRESENT;
			}
		}
		return result;
	}

	
	/**
	 * Add a string entry from <code>map</code> to <code>values</code>.<br>
	 * @param map		LinkedHashMap to extract field from
	 * @param key		Key in LinkedHashMap to extract
	 * @param values	ContentValues to add to
	 * @param field		Field to add as in ContentValues
	 * @param mask		BitSet to record entry in
	 * @return			PRESENT if added, 0 otherwise
	 */
	private int addStringField(LinkedHashMap<String, String> map, String key, ContentValues values, String field, BitSet mask) {
		
		int result = 0;
		String str = getValueFromMap(map, key);
		if ( str != null ) {
			values.put(field, str);						// add to ContentValues
			mask.set(allSmsEntries.indexOf( key ));		// set bit in mask
			result = PRESENT;
		}
		return result;
	}

	
	/**
	 * Get the value of the specified key from the map.
	 * @param map		LinkedHashMap to extract field from
	 * @param key		Key in LinkedHashMap to extract
	 * @return			Value as a String or null if key not present
	 */
	private String getValueFromMap(LinkedHashMap<String, String> map, String key) {
		
		String str = null;
		if (map.containsKey(key))
			str = map.get(key).trim().replace(KeyValueSplitter.DEFAULT_UNIFIER, "");
		return str;
	}
	
	/**
	 * Retrieve an entry id from the database
	 * @param uriPath	- basic URI path
	 * @param idCol		- Id column name in database
	 * @param segment	- segment to append to basic URI
	 * @return
	 */
	private int findId(String uriPath, String idCol, String segment) {
		
		String[] projection = {	idCol };
		int id = -1;
		Uri uri = DatabaseManager.makeUri(uriPath, "\"" + segment + "\"");
		
		Cursor c = context.getContentResolver().query(uri, projection, null, null, null);	// Return the info
		if (c.moveToFirst()) {
			int idIdx = c.getColumnIndex(idCol);
			id = c.getInt(idIdx);
		}
		c.close();

		return id;
	}
	
	/**
	 * Find the ID of the specified account
	 * @param nickname
	 * @return
	 */
	private int findAccount(String nickname) {
		return findId(DatabaseManager.ACCOUNT_NICKNAME_URI_PATH, DatabaseManager.ACCOUNT_ID, nickname);
	}
	
	/**
	 * Find the ID of the specified payee
	 * @param name	Payee to find
	 * @return		ID
	 */
	private int findPayee(String name) {
		return findId(DatabaseManager.PAYEE_NAME_URI_PATH, DatabaseManager.PAYEE_ID, name);
	}
	
	/**
	 * Add a new payee to the database
	 * @param name	Name of payee
	 * @return		ID of new row added
	 */
	private int addPayee(String name) {
		
		ContentValues values = new ContentValues();
		values.put(DatabaseManager.PAYEE_NAME, name);
		Uri result = context.getContentResolver().insert(DatabaseManager.PAYEE_URI, values);
		return Integer.parseInt(result.getLastPathSegment());
	}


	/**
	 * Check if the specified category ID exists
	 * @param categoryId	ID to search for
	 * @return				true if it exists, false otherwise
	 */
	private boolean doesCategoryExist(String categoryId) {
		
		String[] projection = {
				DatabaseManager.CATEGORY_ID,
				DatabaseManager.CATEGORY_NAME,
		};
		Cursor c = context.getContentResolver().query(ContentUris.withAppendedId(DatabaseManager.CATEGORY_URI, 
								Integer.decode(categoryId)), projection, null, null, null);	// Return the category
		boolean result = c.moveToFirst();
		c.close();

		return result;
	}
	
	
	/**
	 * Send a message to the specified phone number
	 * @param phoneNumber	- phone number to send to
	 * @param message		- message to send
	 */
	public void sendSMS(String phoneNumber, String message) {
		
		SmsManager sms = SmsManager.getDefault();
		int flags = PendingIntent.FLAG_ONE_SHOT + PendingIntent.FLAG_UPDATE_CURRENT;
		Intent intent = new Intent(SENT_SMS).putExtra(SMS_PAYLOAD, message);
		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, intent, flags);
		intent = new Intent(DELIVERED_SMS).putExtra(SMS_PAYLOAD, message);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, intent, flags);
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

	
	/**
	 * Send a message to the specified phone number
	 * @param phoneNumber	- phone number to send to
	 * @param message		- message to send
	 */
	public void sendSMS(int phoneNumber, String message) {
		sendSMS(String.valueOf(phoneNumber), message);
	}

	
	public enum SmsReport { SMS_NEW_REPORT, SMS_UPDATE_REPORT, SMS_DELETE_REPORT, SMS_BALANCE_REPORT };

	/**
	 * Send a plain text SMS to the specified number
	 * @param phoneNumber	- phone number to send to
	 * @param trans			- transactions to include in message
	 * @param type			- message type
	 */
	public void sendPlainTextSMS(String phoneNumber, Transaction[] trans, SmsReport type) {
		
		StringBuffer sb = new StringBuffer();
		ContentResolver cr = context.getContentResolver();
		ArrayList<Account> accounts = new ArrayList<Account>();
		final int N = trans.length - 1;
		
		switch ( type ) {
			case SMS_NEW_REPORT:	// new trans
				for ( int i = 0; i <= N; ++i )
					sb.append(convertTransactionToPlainTextSms(trans[i]));
				break;
			case SMS_UPDATE_REPORT:	// updated trans, so should have old & new
				if ( N >= 0 ) 
					sb.append(resrc.getString(R.string.sms_trans_update_head));
				for ( int i = 0; i <= N; ++i ) {
					sb.append(convertTransactionToPlainTextSms(trans[i]));
					if ( N >= 0 ) 
						sb.append(resrc.getString(R.string.sms_trans_update_link));
				}
				break;
			case SMS_DELETE_REPORT:	// deleted trans
				if ( N >= 0 ) 
					sb.append(resrc.getString(R.string.sms_trans_remove_head));
				for ( int i = 0; i <= N; ++i )
					sb.append(convertTransactionToPlainTextSms(trans[i]));
				break;
			default:
				break;
		}

		// add balance(s)
		for ( int i = 0; i <= N; ++i ) {
			HashMap<String, Account> mapAccounts = trans[i].getAffectedAccounts(cr);
			for ( Account acc : mapAccounts.values() ) {
				if ( !accounts.contains(acc) )
					accounts.add(acc);
			}
		}
		if ( accounts.size() > 0 ) {
			sb.append(" ");
			sb.append(generateBalancePlainTextSms( accounts.toArray(new Account[accounts.size()])));
		}

		if ( sb.length() > 0 ) {
			sendSMS(phoneNumber, sb.toString());
		}
	}

	/**
	 * Send a plain text SMS to the specified number
	 * @param phoneNumber	- phone number to send to
	 * @param trans			- transactions to include in message
	 * @param type			- message type
	 */
	public void sendSMS(int phoneNumber, Transaction[] trans, SmsReport type) {
		sendPlainTextSMS(String.valueOf(phoneNumber), trans, type);
	}

	
	/**
	 * Update users with the transactions details
	 * @param trans			- transactions to update users about
	 * @param reportType	- type of update to send
	 */
	public void updateUsers(Transaction[] trans, SmsReport reportType, ContentResolver cr) {
		ArrayList<User> users = new User().loadFromProviderExcluding(cr, User.DEFAULT_PHONE_NUMBER);
		for ( User usr : users )
			sendPlainTextSMS(usr.getUserPhone(), trans, reportType);
	}

	/**
	 * Update users with the transactions details
	 * @param uri			- Uri of transactions to update users about
	 * @param reportType	- type of update to send
	 */
	public void updateUsers(Uri[] uri, SmsReport reportType, ContentResolver cr) {
		final int N = uri.length;
		Transaction[] trans = new Transaction[N];
		Transaction loader = new Transaction();

		for ( int i = 0; i < N; ++i )
			trans[i] = loader.loadFromProvider(cr, uri[i]);

		updateUsers(trans, reportType, cr);
	}

}
