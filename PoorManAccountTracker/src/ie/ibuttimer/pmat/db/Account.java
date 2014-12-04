/**
 * 
 */
package ie.ibuttimer.pmat.db;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

/**
 * Class to represent an account as it appears in the database
 * @author Ian Buttimer
 *
 */
public class Account extends DatabaseObject {

	/* Note: variables have to protected to allow DatabaseObject.saveField() to access them */
	protected long accountId;				// id
	protected String accountName;			// display name
	protected String accountNickname;		// nickname, used for text banking
	protected long accountType;				// type
	protected long accountBank;				// bank
	protected long accountCurrency;			// currency
	protected double accountInitBal;		// initial balance
	protected GregorianCalendar accountDate;// date account was opened
	protected double accountCurrentBal;		// current balance
	protected double accountAvailBal;		// available balance
	protected double accountLimit;			// limit
	protected String accountNumber;			// account number

	protected static ArrayList<String> clsLongFields;
	protected static ArrayList<String> clsIntFields;
	protected static ArrayList<String> clsDateFields;
	protected static ArrayList<String> clsDoubleFields;
	protected static ArrayList<String> clsStringFields;

	
	static {
		clsLongFields = new ArrayList<String>();
		clsLongFields.add(DatabaseManager.ACCOUNT_ID);
		clsLongFields.add(DatabaseManager.ACCOUNT_TYPE);
		clsLongFields.add(DatabaseManager.ACCOUNT_BANK);
		clsLongFields.add(DatabaseManager.ACCOUNT_CURRENCY);
		
		clsIntFields = new ArrayList<String>();

		clsDateFields = new ArrayList<String>();
		clsDateFields.add(DatabaseManager.ACCOUNT_DATE);

		clsDoubleFields = new ArrayList<String>();
		clsDoubleFields.add(DatabaseManager.ACCOUNT_INITBAL);
		clsDoubleFields.add(DatabaseManager.ACCOUNT_CURRENTBAL);
		clsDoubleFields.add(DatabaseManager.ACCOUNT_AVAILBAL);
		clsDoubleFields.add(DatabaseManager.ACCOUNT_LIMIT);

		clsStringFields = new ArrayList<String>();
		clsStringFields.add(DatabaseManager.ACCOUNT_NAME);
		clsStringFields.add(DatabaseManager.ACCOUNT_NICKNAME);
		clsStringFields.add(DatabaseManager.ACCOUNT_NUMBER);
	}
	private static final Uri uri = DatabaseManager.ACCOUNT_ACC_URI;

	
	/**
	 * Constructor 
	 */
	public Account() {
		super(Account.class);
		setFields(clsLongFields, clsIntFields, clsDateFields, clsDoubleFields, clsStringFields);
	}
	
	/**
	 * Constructor for the account in the database with the specified id
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of account to retrieve
	 */
	public Account(ContentResolver cr, long id) {
		this();
		Account db = loadFromProvider(cr, id);
		if ( db != null ) {
			ContentValues values = toContentValues(db);
			this.updateFromValues(values);
		}
	}

	/**
	 * Delete an account from the database
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of Object to delete 
	 * @return		<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	public static boolean deleteAccountFromProvider(ContentResolver cr, long id) {
		return deleteIdFromProvider(cr, uri, DatabaseManager.ACCOUNT_ID, id);
	}

	/**
	 * Retrieve the account in the database with the specified id
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of account to retrieve
	 * @return		an Account object
	 */
	public Account loadFromProvider(ContentResolver cr, long id) {
		ArrayList<Account> list = loadIdFromProvider(cr, uri, DatabaseManager.ACCOUNT_ID, id);
		if ( list.size() == 1 )
			return list.get(0);
		else
			return null;
	}

	/**
	 * Retrieve all the objects in the database
	 * @param cr			- Content Resolver to use
	 * @return				an ArrayList of objects
	 */
	public ArrayList<Account> loadAllFromProvider(ContentResolver cr) {
		return loadFromProvider(cr, uri, (String)null, (String[])null);
	}

	/**
	 * Retrieve the account currency in the database with the specified id
	 * @param cr	- Content Resolver to use
	 * @return		an Account object
	 */
	public AccountCurrency getAccountCurrency(ContentResolver cr) {
		return AccountCurrency.loadCurrencyFromProvider(cr, accountCurrency);
	}

	/**
	 * Return the specified field as a string.<br>
	 * @param field		- field to retrieve
	 * @param cr		- ContentResolver to use
	 * @return			string or null if field doesn't
	 * @see 			ie.ibuttimer.pmat.db.DatabaseObject#fieldToString(String)
	 */
	public String fieldToString(String field, ContentResolver cr) {
		// DatabaseObject just formats doubles as double, so override to return as currency
		String str;
		if ( clsDoubleFields.contains(field) ) {
			AccountCurrency currency = getAccountCurrency(cr);
			str = currency.formatDouble((Double) getField(field));
		}
		else
			str = super.fieldToString(field);
		return str;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(accountAvailBal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (accountBank ^ (accountBank >>> 32));
		result = prime * result
				+ (int) (accountCurrency ^ (accountCurrency >>> 32));
		temp = Double.doubleToLongBits(accountCurrentBal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((accountDate == null) ? 0 : accountDate.hashCode());
		result = prime * result + (int) (accountId ^ (accountId >>> 32));
		temp = Double.doubleToLongBits(accountInitBal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(accountLimit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((accountName == null) ? 0 : accountName.hashCode());
		result = prime * result
				+ ((accountNickname == null) ? 0 : accountNickname.hashCode());
		result = prime * result
				+ ((accountNumber == null) ? 0 : accountNumber.hashCode());
		result = prime * result + (int) (accountType ^ (accountType >>> 32));
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
		if (!(obj instanceof Account))
			return false;
		Account other = (Account) obj;
		if (Double.doubleToLongBits(accountAvailBal) != Double
				.doubleToLongBits(other.accountAvailBal))
			return false;
		if (accountBank != other.accountBank)
			return false;
		if (accountCurrency != other.accountCurrency)
			return false;
		if (Double.doubleToLongBits(accountCurrentBal) != Double
				.doubleToLongBits(other.accountCurrentBal))
			return false;
		if (accountDate == null) {
			if (other.accountDate != null)
				return false;
		} else if (!accountDate.equals(other.accountDate))
			return false;
		if (accountId != other.accountId)
			return false;
		if (Double.doubleToLongBits(accountInitBal) != Double
				.doubleToLongBits(other.accountInitBal))
			return false;
		if (Double.doubleToLongBits(accountLimit) != Double
				.doubleToLongBits(other.accountLimit))
			return false;
		if (accountName == null) {
			if (other.accountName != null)
				return false;
		} else if (!accountName.equals(other.accountName))
			return false;
		if (accountNickname == null) {
			if (other.accountNickname != null)
				return false;
		} else if (!accountNickname.equals(other.accountNickname))
			return false;
		if (accountNumber == null) {
			if (other.accountNumber != null)
				return false;
		} else if (!accountNumber.equals(other.accountNumber))
			return false;
		if (accountType != other.accountType)
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Account [accountId=" + accountId + ", accountName="
				+ accountName + ", accountNickname=" + accountNickname
				+ ", accountType=" + accountType + ", accountBank="
				+ accountBank + ", accountCurrency=" + accountCurrency
				+ ", accountInitBal=" + accountInitBal + ", accountDate="
				+ accountDate + ", accountCurrentBal=" + accountCurrentBal
				+ ", accountAvailBal=" + accountAvailBal + ", accountLimit="
				+ accountLimit + ", accountNumber=" + accountNumber + "]";
	}

	/**
	 * @return the accountId
	 */
	public long getAccountId() {
		return accountId;
	}

	/**
	 * @param accountId the accountId to set
	 */
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	/**
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	/**
	 * @return the accountNickname
	 */
	public String getAccountNickname() {
		return accountNickname;
	}

	/**
	 * @param accountNickname the accountNickname to set
	 */
	public void setAccountNickname(String accountNickname) {
		this.accountNickname = accountNickname;
	}

	/**
	 * @return the accountType
	 */
	public long getAccountType() {
		return accountType;
	}

	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(long accountType) {
		this.accountType = accountType;
	}

	/**
	 * @return the accountBank
	 */
	public long getAccountBank() {
		return accountBank;
	}

	/**
	 * @param accountBank the accountBank to set
	 */
	public void setAccountBank(long accountBank) {
		this.accountBank = accountBank;
	}

	/**
	 * @return the accountCurrency
	 */
	public long getAccountCurrency() {
		return accountCurrency;
	}

	/**
	 * @param accountCurrency the accountCurrency to set
	 */
	public void setAccountCurrency(long accountCurrency) {
		this.accountCurrency = accountCurrency;
	}

	/**
	 * @return the accountInitBal
	 */
	public double getAccountInitBal() {
		return accountInitBal;
	}

	/**
	 * @param accountInitBal the accountInitBal to set
	 */
	public void setAccountInitBal(double accountInitBal) {
		this.accountInitBal = accountInitBal;
	}

	/**
	 * @return the accountDate
	 */
	public GregorianCalendar getAccountDate() {
		return accountDate;
	}

	/**
	 * @param accountDate the accountDate to set
	 */
	public void setAccountDate(GregorianCalendar accountDate) {
		this.accountDate = accountDate;
	}

	/**
	 * @return the accountCurrentBal
	 */
	public double getAccountCurrentBal() {
		return accountCurrentBal;
	}

	/**
	 * @param accountCurrentBal the accountCurrentBal to set
	 */
	public void setAccountCurrentBal(double accountCurrentBal) {
		this.accountCurrentBal = accountCurrentBal;
	}

	/**
	 * @return the accountAvailBal
	 */
	public double getAccountAvailBal() {
		return accountAvailBal;
	}

	/**
	 * @param accountAvailBal the accountAvailBal to set
	 */
	public void setAccountAvailBal(double accountAvailBal) {
		this.accountAvailBal = accountAvailBal;
	}

	/**
	 * @return the accountLimit
	 */
	public double getAccountLimit() {
		return accountLimit;
	}

	/**
	 * @param accountLimit the accountLimit to set
	 */
	public void setAccountLimit(double accountLimit) {
		this.accountLimit = accountLimit;
	}

	/**
	 * @return the accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}


}
