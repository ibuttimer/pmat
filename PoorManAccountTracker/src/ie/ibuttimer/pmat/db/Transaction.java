/**
 * 
 */
package ie.ibuttimer.pmat.db;

import ie.ibuttimer.pmat.db.SQLiteCommandFactory.SelectionArgs;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

/**
 * Class to represent a transaction as it appears in the database
 * @author Ian Buttimer
 *
 */
public class Transaction extends DatabaseObject {
	
	/* Note: variables have to protected to allow DatabaseObject.saveField() to access them */
	protected long transId;				// id
	protected int transOrigin;			// origin
	protected int transOriginID;		// origin id
	protected int transType;			// type
	protected long transSrcAccount;		// source account
	protected long transDestAccount;	// destination account
	protected GregorianCalendar transSendDate;	// send date/time
	protected GregorianCalendar transRecvDate;	// receive date/time
	protected int transStatus;			// status
	protected double transAmt;			// amount
	protected double transRate;			// rate
	protected long transPayee;			// payee
	protected String transRef;			// reference
	protected String transNote;			// note
	protected long transCategory;		// category
	protected String transMin;			// min amount
	protected String transMax;			// max amount
	protected long transParent;			// parent transaction
	protected int transFlags;			// flags

	protected static ArrayList<String> clsLongFields;
	protected static ArrayList<String> clsIntFields;
	protected static ArrayList<String> clsDateFields;
	protected static ArrayList<String> clsDoubleFields;
	protected static ArrayList<String> clsStringFields;

	static {
		clsLongFields = new ArrayList<String>();
		clsLongFields.add(DatabaseManager.TRANSACTION_ID);
		clsLongFields.add(DatabaseManager.TRANSACTION_SRC);
		clsLongFields.add(DatabaseManager.TRANSACTION_DEST);
		clsLongFields.add(DatabaseManager.TRANSACTION_PAYEE);
		clsLongFields.add(DatabaseManager.TRANSACTION_CATEGORY);
		clsLongFields.add(DatabaseManager.TRANSACTION_PARENT);
		
		clsIntFields = new ArrayList<String>();
		clsIntFields.add(DatabaseManager.TRANSACTION_ORIGIN);
		clsIntFields.add(DatabaseManager.TRANSACTION_ORIGIN_ID);
		clsIntFields.add(DatabaseManager.TRANSACTION_TYPE);
		clsIntFields.add(DatabaseManager.TRANSACTION_STATUS);
		clsIntFields.add(DatabaseManager.TRANSACTION_FLAGS);

		clsDateFields = new ArrayList<String>();
		clsDateFields.add(DatabaseManager.TRANSACTION_SENDDATE);
		clsDateFields.add(DatabaseManager.TRANSACTION_RECVDATE);

		clsDoubleFields = new ArrayList<String>();
		clsDoubleFields.add(DatabaseManager.TRANSACTION_AMOUNT);
		clsDoubleFields.add(DatabaseManager.TRANSACTION_RATE);

		clsStringFields = new ArrayList<String>();
		clsStringFields.add(DatabaseManager.TRANSACTION_REF);
		clsStringFields.add(DatabaseManager.TRANSACTION_NOTE);
		clsStringFields.add(DatabaseManager.TRANSACTION_MIN);
		clsStringFields.add(DatabaseManager.TRANSACTION_MAX);
	}
	private static final Uri uri = DatabaseManager.TRANSACTION_URI;
	private static final String idField = DatabaseManager.TRANSACTION_ID;
	
	
	/** Transaction status invalid */
	public static final int TRANSSTATUS_INVALID = 1;
	/** Transaction status initiated */
	public static final int TRANSSTATUS_INITIATED = 2;
	/** Transaction status in progress */
	public static final int TRANSSTATUS_INPROGRESS = 3;
	/** Transaction status complete */
	public static final int TRANSSTATUS_COMPLETE = 4;
	/** Transaction status cancelled */
	public static final int TRANSSTATUS_CANCELLED = 5;
	/** Transaction status postponed */
	public static final int TRANSSTATUS_POSTPONED = 6;
	
	/** Transaction type invalid */
	public static final int TRANSACTION_INVALID = 0;
	/** Credit transaction */
	public static final int TRANSACTION_CREDIT = 1;
	/** Debit transaction */
	public static final int TRANSACTION_DEBIT = 2;
	/** Transfer transaction */
	public static final int TRANSACTION_TRANSFER = 3;
	
	/* offset applied to all transaction templates */
	private static final int TRANSACTION_TEMPLATE_OFFSET = 3;
	
	/** Credit transaction template */
	public static final int TRANSACTION_CREDIT_TEMPLATE = TRANSACTION_CREDIT + TRANSACTION_TEMPLATE_OFFSET;
	/** Debit transaction template */
	public static final int TRANSACTION_DEBIT_TEMPLATE = TRANSACTION_DEBIT + TRANSACTION_TEMPLATE_OFFSET;
	/** Transfer transaction template */
	public static final int TRANSACTION_TRANSFER_TEMPLATE = TRANSACTION_TRANSFER + TRANSACTION_TEMPLATE_OFFSET;

	/* offset applied to all transaction reversals */
	private static final int TRANSACTION_REVERSAL_OFFSET = 6;
	
	/** Credit transaction template */
	public static final int TRANSACTION_CREDIT_REVERSAL = TRANSACTION_CREDIT + TRANSACTION_REVERSAL_OFFSET;
	/** Debit transaction template */
	public static final int TRANSACTION_DEBIT_REVERSAL = TRANSACTION_DEBIT + TRANSACTION_REVERSAL_OFFSET;
	/** Transfer transaction template */
	public static final int TRANSACTION_TRANSFER_REVERSAL = TRANSACTION_TRANSFER + TRANSACTION_REVERSAL_OFFSET;

	/** Minimum valid transaction type id */
	public static final int TRANSACTION_TYPE_MIN = TRANSACTION_CREDIT;
	/** Maximum valid transaction type id */
	public static final int TRANSACTION_TYPE_MAX = TRANSACTION_TRANSFER_REVERSAL;

	
	/** No transaction flag */
	public static final int TRANSACTION_FLAG_NONE = 0;
	/** Estimated amount transaction flag */
	public static final int TRANSACTION_FLAG_ESTIMATE = 0x01;
	
	
	/**
	 * Constructor 
	 */
	public Transaction() {
		super(Transaction.class);
		setFields(clsLongFields, clsIntFields, clsDateFields, clsDoubleFields, clsStringFields);
	}


	/**
	 * Checks if the specified value represents a valid transaction status.
	 * @param status	Value to check
	 * @return			true if valid, false otherwise
	 */
	public static final boolean isValidStatus(int status) {
		if ( (status >= TRANSSTATUS_INVALID) && (status <= TRANSSTATUS_POSTPONED) )
			return true;
		else
			return false;
	}
	
	
	/**
	 * Check if the specified value represents a valid transaction. 
	 * @param trans		Value to check
	 * @return			true if valid, false otherwise
	 */
	public static boolean isValidTransaction(int trans) {
		return ((trans >= TRANSACTION_CREDIT) && (trans <= TRANSACTION_TRANSFER));
	}
	
	/**
	 * Check if the specified value represents a valid transaction template. 
	 * @param trans		Value to check
	 * @return			true if valid, false otherwise
	 */
	public static boolean isValidTransactionTemplate(int trans) {
		return ((trans >= TRANSACTION_CREDIT_TEMPLATE) && (trans <= TRANSACTION_TRANSFER_TEMPLATE));
	}
	
	/**
	 * Check if the specified value represents a valid transaction reversal. 
	 * @param trans		Value to check
	 * @return			true if valid, false otherwise
	 */
	public static boolean isValidTransactionReversal(int trans) {
		return ((trans >= TRANSACTION_CREDIT_REVERSAL) && (trans <= TRANSACTION_TRANSFER_REVERSAL));
	}
	
	/**
	 * Check if the specified value represents a valid transaction type. 
	 * @param trans		Value to check
	 * @return			true if valid, false otherwise
	 */
	public static boolean isValidTransactionType(int trans) {
		return (isValidTransaction(trans) || isValidTransactionReversal(trans) || isValidTransactionTemplate(trans));
	}
	
	/**
	 * Return the transaction value from the specified template. 
	 * @param type		Value to convert
	 * @return			Transaction value, or TRANSACTION_INVALID if error
	 */
	public static int getTransactionFromTemplate(int type) {
		if (isValidTransactionTemplate(type))
			return (type - TRANSACTION_TEMPLATE_OFFSET);
		else
			return TRANSACTION_INVALID;
	}

	/**
	 * Return the transaction reversal value from the specified type. 
	 * @param type		Value to convert
	 * @return			Transaction value, or TRANSACTION_INVALID if error
	 */
	public static int getReversalFromTransaction(int type) {
		if (isValidTransaction(type))
			return (type + TRANSACTION_REVERSAL_OFFSET);
		else
			return TRANSACTION_INVALID;
	}

	/**
	 * Return the base transaction value from the specified transaction/template. 
	 * @param trans		Value to convert
	 * @return			Transaction value, or TRANSACTION_INVALID if error
	 */
	public static int getBaseTransaction(int trans) {
		if (isValidTransactionTemplate(trans))
			return (trans - TRANSACTION_TEMPLATE_OFFSET);
		else if (isValidTransactionReversal(trans))
			return (trans - TRANSACTION_REVERSAL_OFFSET);
		else if (isValidTransaction(trans))
			return trans;
		else
			return TRANSACTION_INVALID;
	}
	
	
	/**
	 * Delete a transaction from the database
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of Object to delete 
	 * @return		<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	public static boolean deleteTransactionFromProvider(ContentResolver cr, long id) {
		return deleteIdFromProvider(cr, uri, idField, id);
	}

	/**
	 * Delete a transaction from the database
	 * @param cr	- Content Resolver to use
	 * @param ids	- Ids of Objects to delete 
	 * @return		<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	public static boolean deleteTransactionFromProvider(ContentResolver cr, long[] ids) {
		return deleteIdFromProvider(cr, uri, idField, ids);
	}

	/**
	 * Retrieve the transaction in the database with the specified id
	 * @param cr		- Content Resolver to use
	 * @param transId	- Id of transaction to retrieve
	 * @return			a Transaction object
	 */
	public Transaction loadFromProvider(ContentResolver cr, long transId) {
		ArrayList<Transaction> transactions = loadIdFromProvider(cr, uri, idField, transId);
		if ( transactions.size() == 1 )
			return transactions.get(0);
		else
			return null;
	}

	/**
	 * Retrieve the object in the database with the specified uri id
	 * @param cr	- Content Resolver to use
	 * @param id	- Uri to use with the id of object to retrieve
	 * @return		a Transaction object
	 */
	public Transaction loadFromProvider(ContentResolver cr, Uri id) {
		ArrayList<Transaction> transactions = loadUriIdFromProvider(cr, uri, idField, id);
		if ( transactions.size() == 1 )
			return transactions.get(0);
		else
			return null;
	}

	/**
	 * Retrieve the transaction in the database with the specified id and all of its category transactions.
	 * @param cr		- Content Resolver to use
	 * @param transId	- Id of transaction to retrieve
	 * @return		an ArrayList of Transaction objects
	 */
	public ArrayList<Transaction> loadTransactionGroupFromProvider(ContentResolver cr, long transId) {
		SelectionArgs args = SQLiteCommandFactory.makeIdSelection(new String[] {
																	idField, DatabaseManager.TRANSACTION_PARENT
																	}, transId);
		return loadFromProvider(cr, uri, args.selection, args.selectionArgs);
	}
	
	/**
	 * Retrieve the transaction in the database with the specified id and all of its category transactions.
	 * @param cr		- Content Resolver to use
	 * @param transId	- Id of transaction to retrieve
	 * @return			<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	public boolean deleteTransactionGroupFromProvider(ContentResolver cr, long transId) {
		
		ArrayList<Transaction> transactions = loadTransactionGroupFromProvider(cr, transId);
		long[] ids = new long[transactions.size()];
		int i = 0;
		for ( Transaction trans : transactions ) {
			ids[i++] = trans.transId;
			if ( trans.transParent == DatabaseManager.TRANSACTION_PARENT_ID ) {
				// reverse parent effect on account balances
				ContentValues values = trans.createContentValuesFromTransaction();
				values.put(DatabaseManager.TRANSACTION_TYPE, getReversalFromTransaction(trans.transType));
				values.remove(DatabaseManager.TRANSACTION_ID);
				updateIdInProvider(cr, uri, idField, transId, values);
			}
		}
		return deleteTransactionFromProvider(cr, ids);
	}
	
	
	/**
	 * Retrieve the transaction in the database with the specified id and all of its category transactions.
	 * @param cr		- Content Resolver to use
	 * @return			<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	public boolean deleteTransactionGroupFromProvider(ContentResolver cr) {
		return deleteTransactionGroupFromProvider(cr, transId);
	}
	
	/**
	 * Update an object in the database
	 * @param cr		- Content Resolver to use
	 * @param uri		- URI to use
	 * @param idName	- name of id field
	 * @param id		- Id of object to delete 
	 * @param values	- ContentValues containing updated data
	 * @return			<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	public static boolean updateTransactionInProvider(ContentResolver cr, long id, ContentValues values) {
		return updateIdInProvider(cr, uri, idField, id, values);
	}
	

	/**
	 * Return the parent transaction from the transaction group.
	 * @param group	- group to find parent from
	 * @return		parent Transaction object
	 */
	public static Transaction getParentFromGroup(ArrayList<Transaction> group) {
		final int N = group.size();
		Transaction parent = null;
		for ( int i = 0; i < N; ++i ) {
			if ( group.get(i).transParent == DatabaseManager.TRANSACTION_PARENT_ID ) {
				parent = group.get(i);
				break;
			}
		}
		return parent;
	}


	/**
	 * Return the category transactions from the transaction group.
	 * @param group	- group to find category transactions from
	 * @return		an ArrayList of Transaction objects
	 */
	public static ArrayList<Transaction> getCategoriesFromGroup(ArrayList<Transaction> group) {
		ArrayList<Transaction> categories = (ArrayList<Transaction>) group.clone();
		ArrayList<Transaction> parent = new ArrayList<Transaction>();
		for ( int i = categories.size() - 1; i >= 0; --i ) {
			if ( group.get(i).transParent == DatabaseManager.TRANSACTION_PARENT_ID ) {
				parent.add(group.get(i));
			}
		}
		if ( parent.size() > 0 )
			categories.removeAll(parent);
		return categories;
	}


	
	
	/**
	 * @return the transId
	 */
	public long getTransId() {
		return transId;
	}


	/**
	 * @param transId the transId to set
	 */
	public void setTransId(long transId) {
		this.transId = transId;
	}


	/**
	 * @return the transOrigin
	 */
	public int getTransOrigin() {
		return transOrigin;
	}


	/**
	 * @param transOrigin the transOrigin to set
	 */
	public void setTransOrigin(int transOrigin) {
		this.transOrigin = transOrigin;
	}


	/**
	 * @return the transOriginID
	 */
	public int getTransOriginID() {
		return transOriginID;
	}


	/**
	 * @param transOriginID the transOriginID to set
	 */
	public void setTransOriginID(int transOriginID) {
		this.transOriginID = transOriginID;
	}


	/**
	 * @return the transType
	 */
	public int getTransType() {
		return transType;
	}


	/**
	 * @param transType the transType to set
	 */
	public void setTransType(int transType) {
		this.transType = transType;
	}


	/**
	 * @return the transSrcAccount
	 */
	public long getTransSrcAccount() {
		return transSrcAccount;
	}


	/**
	 * @param transSrcAccount the transSrcAccount to set
	 */
	public void setTransSrcAccount(long transSrcAccount) {
		this.transSrcAccount = transSrcAccount;
	}


	/**
	 * @return the transDestAccount
	 */
	public long getTransDestAccount() {
		return transDestAccount;
	}


	/**
	 * @param transDestAccount the transDestAccount to set
	 */
	public void setTransDestAccount(long transDestAccount) {
		this.transDestAccount = transDestAccount;
	}


	/**
	 * @return the transSendDate
	 */
	public GregorianCalendar getTransSendDate() {
		return transSendDate;
	}


	/**
	 * @param transSendDate the transSendDate to set
	 */
	public void setTransSendDate(GregorianCalendar transSendDate) {
		this.transSendDate = transSendDate;
	}


	/**
	 * @return the transRecvDate
	 */
	public GregorianCalendar getTransRecvDate() {
		return transRecvDate;
	}


	/**
	 * @param transRecvDate the transRecvDate to set
	 */
	public void setTransRecvDate(GregorianCalendar transRecvDate) {
		this.transRecvDate = transRecvDate;
	}


	/**
	 * @return the transStatus
	 */
	public int getTransStatus() {
		return transStatus;
	}


	/**
	 * @param transStatus the transStatus to set
	 */
	public void setTransStatus(int transStatus) {
		this.transStatus = transStatus;
	}


	/**
	 * @return the transAmt
	 */
	public double getTransAmt() {
		return transAmt;
	}


	/**
	 * @param transAmt the transAmt to set
	 */
	public void setTransAmt(double transAmt) {
		this.transAmt = transAmt;
	}


	/**
	 * @return the transRate
	 */
	public double getTransRate() {
		return transRate;
	}


	/**
	 * @param transRate the transRate to set
	 */
	public void setTransRate(double transRate) {
		this.transRate = transRate;
	}


	/**
	 * @return the transPayee
	 */
	public long getTransPayee() {
		return transPayee;
	}


	/**
	 * @param transPayee the transPayee to set
	 */
	public void setTransPayee(long transPayee) {
		this.transPayee = transPayee;
	}


	/**
	 * @return the transRef
	 */
	public String getTransRef() {
		return transRef;
	}


	/**
	 * @param transRef the transRef to set
	 */
	public void setTransRef(String transRef) {
		this.transRef = transRef;
	}


	/**
	 * @return the transNote
	 */
	public String getTransNote() {
		return transNote;
	}


	/**
	 * @param transNote the transNote to set
	 */
	public void setTransNote(String transNote) {
		this.transNote = transNote;
	}


	/**
	 * @return the transCategory
	 */
	public long getTransCategory() {
		return transCategory;
	}


	/**
	 * @param transCategory the transCategory to set
	 */
	public void setTransCategory(long transCategory) {
		this.transCategory = transCategory;
	}


	/**
	 * @return the transMin
	 */
	public String getTransMin() {
		return transMin;
	}


	/**
	 * @param transMin the transMin to set
	 */
	public void setTransMin(String transMin) {
		this.transMin = transMin;
	}


	/**
	 * @return the transMax
	 */
	public String getTransMax() {
		return transMax;
	}


	/**
	 * @param transMax the transMax to set
	 */
	public void setTransMax(String transMax) {
		this.transMax = transMax;
	}


	/**
	 * @return the transParent
	 */
	public long getTransParent() {
		return transParent;
	}


	/**
	 * @param transParent the transParent to set
	 */
	public void setTransParent(long transParent) {
		this.transParent = transParent;
	}


	/**
	 * @return the transFlags
	 */
	public int getTransFlags() {
		return transFlags;
	}


	/**
	 * @param transFlags the transFlags to set
	 */
	public void setTransFlags(int transFlags) {
		this.transFlags = transFlags;
	}

	
	/**
	 * Update this object from the content data
	 * @param trans		- Object to update or if null a new object will be created
	 * @param values	- ContentValues to extract data from
	 * @return			Updated object
	 */
	public Transaction updateTransactionFromValues(ContentValues values) {
		return updateFromValues(this, values);
	}
	
	/**
	 * Create a ContentValues to represent the object
	 * @param obj		- Object to convert to a ContentValues 
	 * @return 			ContentValues 
	 */
	public ContentValues createContentValuesFromTransaction(Transaction obj) {
		return toContentValues(obj);
	}

	/**
	 * Create a ContentValues to represent the object
	 * @param obj		- Object to convert to a ContentValues 
	 * @return 			ContentValues 
	 */
	public ContentValues createContentValuesFromTransaction() {
		return toContentValues(this);
	}

	
	/**
	 * Get the accounts affected by this transaction 
	 * @param cr	- ContentResolver
	 * @return		map of affected accounts
	 */
	public HashMap<String, Account> getAffectedAccounts(ContentResolver cr) {
		HashMap<String, Account> map = new HashMap<String, Account>(); 
		Account loader = new Account();
		switch ( transType ) {
			case Transaction.TRANSACTION_DEBIT:		
			case Transaction.TRANSACTION_TRANSFER:
				map.put(DatabaseManager.TRANSACTION_SRC, loader.loadFromProvider(cr, transSrcAccount));
				break;
		}
		switch ( transType ) {
			case Transaction.TRANSACTION_CREDIT:
			case Transaction.TRANSACTION_TRANSFER:
				map.put(DatabaseManager.TRANSACTION_DEST, loader.loadFromProvider(cr, transDestAccount));
				break;
		}
		return map;
	}
	
	/**
	 * Get the accounts affected by this transaction 
	 * @param cr	- ContentResolver
	 * @return		map of affected accounts
	 */
	public long[] getAffectedAccountIds(ContentResolver cr) {
		HashMap<String, Account> accounts = getAffectedAccounts(cr);
		long[] ids = new long[accounts.size()];
		int i = 0;
		if ( accounts.containsKey(DatabaseManager.TRANSACTION_DEST) )
			ids[i++] = accounts.get(DatabaseManager.TRANSACTION_DEST).getAccountId();
		if ( accounts.containsKey(DatabaseManager.TRANSACTION_SRC) )
			ids[i++] = accounts.get(DatabaseManager.TRANSACTION_SRC).getAccountId();
		return ids;
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
		if ( field.equals(DatabaseManager.TRANSACTION_AMOUNT) ) {
			Account account = new Account().loadFromProvider(cr, 
									(transType == Transaction.TRANSACTION_CREDIT ? transDestAccount : transSrcAccount));
			AccountCurrency currency = account.getAccountCurrency(cr);
			str = currency.formatDouble(transAmt);
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
		temp = Double.doubleToLongBits(transAmt);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ (int) (transCategory ^ (transCategory >>> 32));
		result = prime * result
				+ (int) (transDestAccount ^ (transDestAccount >>> 32));
		result = prime * result + transFlags;
		result = prime * result + (int) (transId ^ (transId >>> 32));
		result = prime * result
				+ ((transMax == null) ? 0 : transMax.hashCode());
		result = prime * result
				+ ((transMin == null) ? 0 : transMin.hashCode());
		result = prime * result
				+ ((transNote == null) ? 0 : transNote.hashCode());
		result = prime * result + transOrigin;
		result = prime * result + transOriginID;
		result = prime * result + (int) (transParent ^ (transParent >>> 32));
		result = prime * result + (int) (transPayee ^ (transPayee >>> 32));
		temp = Double.doubleToLongBits(transRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((transRecvDate == null) ? 0 : transRecvDate.hashCode());
		result = prime * result
				+ ((transRef == null) ? 0 : transRef.hashCode());
		result = prime * result
				+ ((transSendDate == null) ? 0 : transSendDate.hashCode());
		result = prime * result
				+ (int) (transSrcAccount ^ (transSrcAccount >>> 32));
		result = prime * result + transStatus;
		result = prime * result + transType;
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
		if (!(obj instanceof Transaction))
			return false;
		Transaction other = (Transaction) obj;
		if (Double.doubleToLongBits(transAmt) != Double
				.doubleToLongBits(other.transAmt))
			return false;
		if (transCategory != other.transCategory)
			return false;
		if (transDestAccount != other.transDestAccount)
			return false;
		if (transFlags != other.transFlags)
			return false;
		if (transId != other.transId)
			return false;
		if (transMax == null) {
			if (other.transMax != null)
				return false;
		} else if (!transMax.equals(other.transMax))
			return false;
		if (transMin == null) {
			if (other.transMin != null)
				return false;
		} else if (!transMin.equals(other.transMin))
			return false;
		if (transNote == null) {
			if (other.transNote != null)
				return false;
		} else if (!transNote.equals(other.transNote))
			return false;
		if (transOrigin != other.transOrigin)
			return false;
		if (transOriginID != other.transOriginID)
			return false;
		if (transParent != other.transParent)
			return false;
		if (transPayee != other.transPayee)
			return false;
		if (Double.doubleToLongBits(transRate) != Double
				.doubleToLongBits(other.transRate))
			return false;
		if (transRecvDate == null) {
			if (other.transRecvDate != null)
				return false;
		} else if (!transRecvDate.equals(other.transRecvDate))
			return false;
		if (transRef == null) {
			if (other.transRef != null)
				return false;
		} else if (!transRef.equals(other.transRef))
			return false;
		if (transSendDate == null) {
			if (other.transSendDate != null)
				return false;
		} else if (!transSendDate.equals(other.transSendDate))
			return false;
		if (transSrcAccount != other.transSrcAccount)
			return false;
		if (transStatus != other.transStatus)
			return false;
		if (transType != other.transType)
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Transaction [transId=" + transId + ", transOrigin="
				+ transOrigin + ", transOriginID=" + transOriginID
				+ ", transType=" + transType + ", transSrcAccount="
				+ transSrcAccount + ", transDestAccount=" + transDestAccount
				+ ", transSendDate=" + transSendDate + ", transRecvDate="
				+ transRecvDate + ", transStatus=" + transStatus
				+ ", transAmt=" + transAmt + ", transRate=" + transRate
				+ ", transPayee=" + transPayee + ", transRef=" + transRef
				+ ", transNote=" + transNote + ", transCategory="
				+ transCategory + ", transMin=" + transMin + ", transMax="
				+ transMax + ", transParent=" + transParent + ", transFlags="
				+ transFlags + "]";
	}
}
