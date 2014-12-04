package ie.ibuttimer.pmat.db;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import ie.ibuttimer.pmat.db.SQLiteCommandFactory.SelectionArgs;
import ie.ibuttimer.widget.TextViewAdapterBase;
import ie.ibuttimer.widget.TextViewAdapterInterface;



/**
 * Class to represent a currency as used by the application.
 * @author Ian Buttimer
 *
 */
public class AccountCurrency extends TextViewAdapterBase implements TextViewAdapterInterface {

	/* fields stored in super class:
	 * - currency name, e.g. "Euro"
	 * - currency numeric code (Note: not ISO 4217 numeric code)
	 *  */
	protected String code;				// currency ISO 4217 alphabetic code
	protected int minorUnits;			// currency default minor units
	private boolean setFields[];		// fields that have been set
	protected String symbol;			// currency symbol

	public static enum CurrencyFields {
		NAME_FIELD, CODE_FIELD, NUMBER_FIELD, MINORUNITS_FIELD, SYMBOL_FIELD
	};

	/** Represents an invalid currency numeric code. Codes need to be positive numbers. */
	public static final int INVALID_CODE = 0; 
	/** Represents an invalid number of minor units. Default minor units need to be zero or positive numbers. */
	public static final int INVALID_MINORUNITS = -1; 
	/** ISO 4217 representation for an invalid currency */
	public static final String NOT_A_CURRENCY_CODE = "XXX";
	/** ISO 4217 representation for an invalid currency */
	public static final int NOT_A_CURRENCY_NUMERIC_CODE = 999;
	
	private static final HashMap<String, String> currencyCodes;
	static {
		currencyCodes = new HashMap<String, String>();
		currencyCodes.put("BGN", "Bulgaria Lev");  
		currencyCodes.put("EUR", "Euro");  
		currencyCodes.put("CZK", "Czech Koruna");
		currencyCodes.put("DKK", "Danish Krone");
		currencyCodes.put("CHF", "Swiss Franc");
		currencyCodes.put("AUD", "Australian Dollar");
		currencyCodes.put("BWP", "Pula");
		currencyCodes.put("BZD", "Belize Dollar");
		currencyCodes.put("CAD", "Canadian Dollar");
		currencyCodes.put("GBP", "Pound Sterling");
		currencyCodes.put("HKD", "Hong Kong Dollar");
		currencyCodes.put("INR", "Indian Rupee");
		currencyCodes.put("JMD", "Jamaican Dollar");
		currencyCodes.put("USD", "US Dollar");
		currencyCodes.put("NAD", "Namibia Dollar");
		currencyCodes.put("NZD", "New Zealand Dollar");
		currencyCodes.put("PHP", "Philippine Peso");
		currencyCodes.put("PKR", "Pakistan Rupee");
		currencyCodes.put("SGD", "Singapore Dollar");
		currencyCodes.put("TTD", "Trinidad and Tobago Dollar");
		currencyCodes.put("ZAR", "Rand");
		currencyCodes.put("AFN", "Afghani");
		currencyCodes.put("IRR", "Iranian Rial");
		currencyCodes.put("JPY", "Yen");
		currencyCodes.put("KRW", "Won");
		currencyCodes.put("LTL", "Lithuanian Litas");
		currencyCodes.put("LVL", "Latvian Lats");
		currencyCodes.put("NOK", "Norwegian Krone");
		currencyCodes.put("PLN", "Zloty");
		currencyCodes.put("BRL", "Brazilian Real");
		currencyCodes.put("RUB", "Russian Ruble");
		currencyCodes.put("SEK", "Swedish Krona");
		currencyCodes.put("TRY", "Turkish Lira");
		currencyCodes.put("VND", "Dong");
		currencyCodes.put("CNY", "Yuan Renminbi");
		currencyCodes.put("TWD", "New Taiwan Dollar");
	};
	

	protected static ArrayList<String> clsLongFields;
	protected static ArrayList<String> clsIntFields;
	protected static ArrayList<String> clsDateFields;
	protected static ArrayList<String> clsDoubleFields;
	protected static ArrayList<String> clsStringFields;

	static {
		clsLongFields = new ArrayList<String>();
		clsLongFields.add(DatabaseManager.CURRENCY_ID);
		
		clsIntFields = new ArrayList<String>();
		clsIntFields.add(DatabaseManager.CURRENCY_MINORUNITS);

		clsDateFields = new ArrayList<String>();
		clsDateFields.add(DatabaseManager.TRANSACTION_SENDDATE);
		clsDateFields.add(DatabaseManager.TRANSACTION_RECVDATE);

		clsDoubleFields = new ArrayList<String>();

		clsStringFields = new ArrayList<String>();
		clsStringFields.add(DatabaseManager.CURRENCY_NAME);
		clsStringFields.add(DatabaseManager.CURRENCY_CODE);
		clsStringFields.add(DatabaseManager.CURRENCY_SYMBOL);
	}
	private static final Uri uri = DatabaseManager.CURRENCY_URI;
	private static final String idField = DatabaseManager.CURRENCY_ID;

	/**
	 * Constructor
	 * @param name			Name
	 * @param code			ISO 4217 alphabetic code
	 * @param number		Numeric code
	 * @param minorUnits	Default minor units
	 * @param symbol		Currency symbol
	 */
	public AccountCurrency(String name, String code, int number,
			int minorUnits, String symbol) {
		super(number, name);

		int size = CurrencyFields.values().length;
		this.setFields = new boolean[size]; 

		// set members using public methods to make sure setFields[] is correct  
		setName(name);
		setCode(code);
		setNumber(number);
		setMinorUnits(minorUnits);
		setSymbol(symbol);
	}

	/**
	 * Default constructor 
	 */
	public AccountCurrency() {
		this(null,null,INVALID_CODE,INVALID_MINORUNITS,null);
	}

	/**
	 * Set the name of the currency, e.g. "Euro".
	 * @param name	Currency name
	 */
	public void setName(String name) {
		super.setName(name);
		this.setFields[CurrencyFields.NAME_FIELD.ordinal()] = !TextUtils.isEmpty(name);
	}

	/**
	 * Get the alphabetic code for the currency, e.g. "EUR".
	 * @return		Alphabetic code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Set the alphabetic code for the currency, e.g. "EUR".
	 * @param code	Alphabetic code
	 */
	public void setCode(String code) {
		this.code = code;
		this.setFields[CurrencyFields.CODE_FIELD.ordinal()] = !TextUtils.isEmpty(code);
	}

	/**
	 * Get the numeric code for the currency, e.g. 978.
	 * @return		Numeric code
	 */
	public int getNumber() {
		return (int) getId();
	}

	/**
	 * Set the numeric code for the currency, e.g. 978.
	 * @param number	Numeric code
	 */
	public void setNumber(int number) {
		setId(number);
		this.setFields[CurrencyFields.NUMBER_FIELD.ordinal()] = ( number > 0 );	// zero or negative are not a valid numeric codes
	}

	/**
	 * Get the default minor units for the currency, e.g. 2.
	 * @return		Default minor units
	 */
	public int getMinorUnits() {
		return minorUnits;
	}

	/**
	 * Set the default minor units for the currency, e.g. 2.
	 * @param minorUnits	Default minor units
	 */
	public void setMinorUnits(int minorUnits) {
		this.minorUnits = minorUnits;
		this.setFields[CurrencyFields.MINORUNITS_FIELD.ordinal()] = ( minorUnits >= 0 );	// negative is not valid
	}

	/**
	 * Get the symbol for the currency, e.g. "€".
	 * @return		Currency symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Set the symbol for the currency, e.g. "€".
	 * @param name	Currency name
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
		this.setFields[CurrencyFields.SYMBOL_FIELD.ordinal()] = !TextUtils.isEmpty(symbol);
	}
	
	/**
	 * Set the specified field of the currency.<br>
	 * <b>Note:</b> In the case of the ENTITIES_FIELD this function performs an addEntity(String)
	 * @param field		Field to set
	 * @data data		Data 
	 */
	public void setFieldValue(CurrencyFields field, String data) {
		if ( field != null ) {
			switch ( field ) {
				case NAME_FIELD:
					setName(data);
					break;
				case CODE_FIELD:
					setCode(data);
					break;
				case NUMBER_FIELD:
				case MINORUNITS_FIELD: {
					int value;
					try {
						value = Integer.parseInt(data, 10);
					}
					catch ( NumberFormatException e ) {
						value = (field == CurrencyFields.NUMBER_FIELD ? INVALID_CODE : INVALID_MINORUNITS);
					}
					if ( field == CurrencyFields.NUMBER_FIELD )
						setNumber(value);
					else
						setMinorUnits(value);
					break;
				}
				case SYMBOL_FIELD:
					setSymbol(data);
					break;
			}
		}
	}
	
	/**
	 * Check if all the specified fields in this object are set.
	 * @param fields	Fields to check
	 * @return			true if all specified fields are set, false otherwise
	 */
	public boolean areFieldsSet(CurrencyFields fields[]) {
		
		if ((fields == null) || (fields.length == 0) )
			return false;
		
		for ( int i = 0; i < fields.length; ++i ) {
			if ( !this.setFields[ fields[i].ordinal() ] )
				return false;
		}
		return true;
	}
	
	/**
	 * Check if all the fields in this object are set.
	 * @return		true if all set, false otherwise
	 */
	public boolean areAllFieldsSet() {
		
		for ( int i = 0; i < setFields.length; ++i ) {
			if ( !this.setFields[i] )
				return false;
		}
		return true;
	}
	

	/**
	 * Return the currency name of the specified ISO 4217 currency code
	 * @param	code	currency ISO 4217 alphabetic code
	 * @return	Currency name or ISO 4217 alphabetic code if no name available
	 */
	public static String getCurrencyName(String code) {
		
		String name = currencyCodes.get(code);
		if ( name == null )
			name = code;
		return name;
	}
	
	
	/**
	 * Formats the specified double using the rules of the current locale number format and the currency 
	 * symbol for the specified ISO 4217 currency code.
	 * @param code		ISO 4217 currency code
	 * @param value		Value to format
	 * @param pattern	Format pattern as specified by {@link java.text.DecimalFormat}
	 * @return			Formatted value string
	 */
	public static String formatDouble(String code, double value, String pattern) {

		// user locale's symbols updated with the symbol for the account currency 
		DecimalFormatSymbols symbolGen = new DecimalFormatSymbols();
		Currency currency = Currency.getInstance(code);
		symbolGen.setCurrencySymbol(currency.getSymbol());
		
		// formatter for user's locale updated with the symbol with the account currency 
		DecimalFormat formatter = new DecimalFormat(pattern, symbolGen); 

		return formatter.format(value);
	}
	
	/**
	 * Formats the specified double using the rules of the current locale number format and the currency 
	 * symbol for the specified ISO 4217 currency code.
	 * @param code		ISO 4217 currency code
	 * @param value		Value to format
	 * @param symbol	Prefix currency symbol; true or false
	 * @return			Formatted value string, e.g. "1,123.00" or "€1,123.00"
	 */
	public static String formatDouble(String code, double value, boolean symbol) {

		// user locale's symbols updated with the symbol for the account currency 
		Currency currency = Currency.getInstance(code);
		StringBuffer sb = new StringBuffer();

		if ( symbol )
			sb.append("\u00A4");	// currency sign
		sb.append("###,###,###,###,##0");
		
		int minorUnits = currency.getDefaultFractionDigits();
		if ( minorUnits > 0 ) {
			// add the minor units format
			char[] minorFmt = new char[minorUnits + 1];
			Arrays.fill(minorFmt, '0');
			minorFmt[0] = '.';
			sb.append(minorFmt);
		}
		
		return formatDouble(code, value, sb.toString());
	}
	
	/**
	 * Formats the specified double using the rules of the current locale number format and the currency 
	 * symbol for the specified ISO 4217 currency code.
	 * @param code		ISO 4217 currency code
	 * @param value		Value to format
	 * @return			Formatted value string, e.g. "1,123.00"
	 */
	public static String formatDouble(String code, double value) {

		return formatDouble(code, value, false);
	}
	
	/**
	 * Formats the specified double using the rules of the current locale number format and the currency 
	 * symbol for this currency.
	 * @param value		Value to format
	 * @return			Formatted value string
	 */
	public String formatDouble(double value) {

		return formatDouble(code, value);
	}

	
	
	/**
	 * Retrieve currencies in the database
	 * @param cr			- Content Resolver to use
	 * @param selection		- selection A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given URI.
	 * @param selectionArgs	- You may include ?s in selection, which will be replaced by the values from selectionArgs, in the order that they appear in the selection. The values will be bound as Strings. 
	 * @return				an ArrayList of AccountCurrency objects
	 */
	public static ArrayList<AccountCurrency> loadCurrenciesFromProvider(ContentResolver cr, String selection, String[] selectionArgs) {
		ArrayList<AccountCurrency> currencies = new ArrayList<AccountCurrency>();
		Cursor c = cr.query(uri, null, selection, selectionArgs, null);	// Return all the database currencies
		if (c.moveToFirst()) {
			int nameIdx = c.getColumnIndex(DatabaseManager.CURRENCY_NAME);
			int codeIdx = c.getColumnIndex(DatabaseManager.CURRENCY_CODE);
			int numIdx = c.getColumnIndex(DatabaseManager.CURRENCY_ID);
			int unitsIdx = c.getColumnIndex(DatabaseManager.CURRENCY_MINORUNITS);
			int symbolIdx = c.getColumnIndex(DatabaseManager.CURRENCY_SYMBOL);
			do {
				// Extract the details.
				String name = c.getString(nameIdx);
				String code = c.getString(codeIdx);
				int number = c.getInt(numIdx);
				int minorUnits = c.getInt(unitsIdx);
				String symbol = c.getString(symbolIdx);

				currencies.add( new AccountCurrency(name, code, number, minorUnits, symbol) );
			} while(c.moveToNext());
		}
		c.close();
		return currencies;
	}

	
	/**
	 * Retrieve all the currencies in the database
	 * @param cr	- Content Resolver to use
	 * @return		an ArrayList of AccountCurrency objects
	 */
	public static ArrayList<AccountCurrency> loadCurrenciesFromProvider(ContentResolver cr) {
		return loadCurrenciesFromProvider(cr, null, null);
	}

	/**
	 * Retrieve the currency with the specified id in the database
	 * @param cr	- Content Resolver to use
	 * @param id	- id of currency to retrieve, i.e. the database id
	 * @return		an ArrayList of AccountCurrency objects
	 */
	public static AccountCurrency loadCurrencyFromProvider(ContentResolver cr, long id) {
		SelectionArgs args = SQLiteCommandFactory.makeIdSelection(idField, id);
		ArrayList<AccountCurrency> list = loadCurrenciesFromProvider(cr, args.selection, args.selectionArgs);
		if ( list.size() == 1 )
			return list.get(0);
		else
			return null;
	}
	
	
	/* Implement TextViewAdapterInterface */

	
	@Override
	public String toDisplayString() {
		return getPrefix() + this.code + " - " + getName();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " [code=" + code + ", minorUnits=" + minorUnits + ", symbol=" + symbol
				+ "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + minorUnits;
		result = prime * result + Arrays.hashCode(setFields);
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AccountCurrency))
			return false;
		AccountCurrency other = (AccountCurrency) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (minorUnits != other.minorUnits)
			return false;
		if (!Arrays.equals(setFields, other.setFields))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	
}
