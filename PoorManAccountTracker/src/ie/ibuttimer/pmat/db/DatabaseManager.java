package ie.ibuttimer.pmat.db;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import ie.ibuttimer.pmat.PreferenceControl;
import ie.ibuttimer.pmat.R;
import ie.ibuttimer.pmat.db.SQLiteCommandFactory.FieldDefinition;
import ie.ibuttimer.pmat.util.DateTimeFormat;
import ie.ibuttimer.pmat.util.KeyValueSplitter;
import ie.ibuttimer.pmat.util.Logger;
import ie.ibuttimer.pmat.db.AccountType;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class DatabaseManager extends ContentProvider {

	private static final String DATABASE_NAME = "pmat.db";
	private static final int DATABASE_VERSION = 1;
	
	private SQLiteDatabase db;
	
	private static int defaultBankId;	// id of default bank
	
	public enum DatabaseState {
		DATABASE_NON_EXISTENT, DATABASE_EXISTS, DATABASE_NEEDS_UPGRADE, DATABASE_OK
	};
	private static DatabaseState dbState;

	private static final String DEFAULT_FIELD_MARKER = ";";											// default marker to separate fields in database default entries
	private static final String DEFAULT_KEYVAL_MARKER = KeyValueSplitter.DEFAULT_KV_ASSIGN_REGEX;	// default marker to separate key/value pairs in database default entries
	private static final String DEFAULT_UNIFIER_MARKER = KeyValueSplitter.DEFAULT_UNIFIER_REGEX;	// default marker to unify fields in database default entries

	/* User table definition
	 * 1. id		: id column
	 * 2. phone		: phone number column
	 * 3. name		: name column
	 * 4. pass		: password column
	 * 5. challenge	: challenge column
	 * 6. response	: challenge response column
	 */
	public static final String USER_TABLE = "userTable";
	public static final String USER_ID = "userId";
	public static final String USER_PHONE = "userPhone";
	public static final String USER_NAME = "userName";
	public static final String USER_PASS = "userPass";
	public static final String USER_CHALLENGE = "userChallenge";
	public static final String USER_RESPONSE = "userResponse";
	private static FieldDefinition userColumns[] = {
		new FieldDefinition(USER_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(USER_PHONE, FieldDefinition.INTEGER_UNIQUE),
		new FieldDefinition(USER_NAME, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(USER_PASS, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(USER_CHALLENGE, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(USER_RESPONSE, FieldDefinition.TEXT_NOT_NULL),
	};
	
	/* Account type table definition
	 * 1. id		: id column
	 * 2. name		: display name column
	 * 3. limit		: credit/overdraft limit applies column
	 */
	public static final String ACCTYPE_TABLE = "accTypeTable";
	public static final String ACCTYPE_ID = "accTypeId";
	public static final String ACCTYPE_NAME = "accTypeName";
	public static final String ACCTYPE_LIMIT = "accTypeLimit";
	private static FieldDefinition accTypeColumns[] = {
		new FieldDefinition(ACCTYPE_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(ACCTYPE_NAME, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(ACCTYPE_LIMIT, FieldDefinition.INTEGER_VALUE),
	};
	// key/value fields for default account type entries, e.g. <item>name=Current;overdraft=yes</item>
	public static final String LIMIT_NAME_KEY = "name";
	public static final String LIMIT_CREDIT_KEY = "credit";
	public static final String LIMIT_OVERDRAFT_KEY = "overdraft";
	public static final String YES_VALUE = "yes";
	public static final String NO_VALUE = "no";

	/* Currency table definition
	 * 1. id			: id column
	 * 2. name			: display name column
	 * 3. code			: alphabetic code column
	 * 4. minor units	: minor units column
	 * 5. symbol		: currency symbol column
	 */
	public static final String CURRENCY_TABLE 		= "currencyTable";		// currency table name
	public static final String CURRENCY_ID 			= "currencyId";			// id (Note: this is NOT the ISO 4217 numeric code e.g. euro is 978, but this field is "EUR".hashCode()) 
	public static final String CURRENCY_NAME 		= "currencyName";		// display name, e.g. "Euro"
	public static final String CURRENCY_CODE 		= "currencyCode";		// alphabetic code, e.g. "EUR"
	public static final String CURRENCY_MINORUNITS 	= "currencyMinorUnits";	// minor units, e.g. 2
	public static final String CURRENCY_SYMBOL 		= "currencySymbol";		// symbol, e.g. "€"
	private static FieldDefinition currencyColumns[] = {
		new FieldDefinition(CURRENCY_ID, FieldDefinition.INTEGER_UNIQUE),
		new FieldDefinition(CURRENCY_NAME, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(CURRENCY_CODE, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(CURRENCY_MINORUNITS, FieldDefinition.INTEGER_VALUE),
		new FieldDefinition(CURRENCY_SYMBOL, FieldDefinition.TEXT_NOT_NULL),
	};
	
	/* Financial institution table definition
	 * 1. id							: id column
	 * 2. name							: display name column
	 * 3. address						: address column
	 * 4. country						: country column
	 * 5. local service number			: local customer service number column
	 * 6. abroad service number			: abroad customer service number column
	 * 7. phone banking number			: phone banking number column
	 * 8. abroad phone banking number	: phone banking number column
	 * 9. text banking number			: text banking number column
	 */
	public static final String BANK_TABLE = "bankTable";							// financial institution table name
	public static final String BANK_ID = "bankId";									// id
	public static final String BANK_NAME = "bankName";								// name
	public static final String BANK_ADDR = "bankAddr";								// address
	public static final String BANK_COUNTRY = "bankCountry";						// country
	public static final String BANK_LOCAL_SERVICE_NUM = "bankLocalServiceNum";		// local customer service number
	public static final String BANK_AWAY_SERVICE_NUM = "bankAwayServiceNum";		// abroad customer service number
	public static final String BANK_PHONE_BANK_NUM = "bankPhoneBankNum";			// phone banking number
	public static final String BANK_AWAY_PHONE_BANK_NUM = "bankAwayPhoneBankNum";	// abroad phone banking number
	public static final String BANK_TEXT_BANK_NUM = "bankTextBankNum";				// text banking number
	private static FieldDefinition bankColumns[] = {
		new FieldDefinition(BANK_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(BANK_NAME, FieldDefinition.TEXT_DEFAULT, ""),
		new FieldDefinition(BANK_ADDR, FieldDefinition.TEXT_DEFAULT, ""),
		new FieldDefinition(BANK_COUNTRY, FieldDefinition.TEXT_DEFAULT, ""),
		new FieldDefinition(BANK_LOCAL_SERVICE_NUM, FieldDefinition.TEXT_DEFAULT, ""),
		new FieldDefinition(BANK_AWAY_SERVICE_NUM, FieldDefinition.TEXT_DEFAULT, ""),
		new FieldDefinition(BANK_PHONE_BANK_NUM, FieldDefinition.TEXT_DEFAULT, ""),
		new FieldDefinition(BANK_AWAY_PHONE_BANK_NUM, FieldDefinition.TEXT_DEFAULT, ""),
		new FieldDefinition(BANK_TEXT_BANK_NUM, FieldDefinition.TEXT_DEFAULT, ""),
	};
	// key/value fields for default bank entries, e.g. <item>name=Non-bank;default=yes</item>
	public static final String BANK_DEFAULT_KEY = "default";
	public static final String BANK_NAME_KEY = "name";
	public static final String BANK_ADDR_KEY = "addr";
	public static final String BANK_LOCALE_KEY = "locale";
	public static final String BANK_CSLOCAL_KEY = "cslocal";
	public static final String BANK_CSAWAY_KEY = "csaway";
	public static final String BANK_PHONE_KEY = "phone";
	public static final String BANK_PHONE_AWAY_KEY = "phoneaway";
	public static final String BANK_TEXT_KEY = "text";

	/* Text banking transaction types table definition
	 * 1. id				: id column
	 * 2. name				: display name column
	 */
	public static final String TEXTTYPES_TABLE = "textTypesTable";				// text banking transactions table
	public static final String TEXTTYPES_ID = "textTypesId";					// id
	public static final String TEXTTYPES_NAME = "textTypesName";				// name
	private static FieldDefinition textTypesColumns[] = {
		new FieldDefinition(TEXTTYPES_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(TEXTTYPES_NAME, FieldDefinition.TEXT_NOT_NULL),
	};

	/* Text banking transactions table definition
	 * 1. id				: id column
	 * 2. type				: type column, refers to text banking transactions table
	 * 3. name				: display name column
	 * 4. bank				: bank column, refers to text financial institution table
	 * 5. prototype			: text prototype column
	 */
	public static final String TEXTTRANS_TABLE = "textTransTable";				// text banking transaction prototypes table
	public static final String TEXTTRANS_ID = "textTransId";					// id
	public static final String TEXTTRANS_TYPE = "textTransType";				// type
	public static final String TEXTTRANS_NAME = "textTransName";				// name
	public static final String TEXTTRANS_BANK = "textTransBank";				// bank
	public static final String TEXTTRANS_PROTO = "textTransProto";				// prototype
	private static FieldDefinition textTransColumns[] = {
		new FieldDefinition(TEXTTRANS_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(TEXTTRANS_TYPE, FieldDefinition.INTEGER_REFERENCES, TEXTTYPES_TABLE, new String[] { TEXTTYPES_ID }),
		new FieldDefinition(TEXTTRANS_NAME, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(TEXTTRANS_BANK, FieldDefinition.INTEGER_REFERENCES, BANK_TABLE, new String[] { BANK_ID }),
		new FieldDefinition(TEXTTRANS_PROTO, FieldDefinition.TEXT_NOT_NULL),
	};

	/* Transfer table definition
	 * 1. id				: id column
	 * 2. name				: display name column
	 * 3. delay				: delay column, i.e. length of time it takes for the money to appear in the destination account
	 * 4. fixed fee amount	: fixed fee amount column, i.e. monetary fee amount that always applies
	 * 5. min fee amount	: min fee amount column, i.e. min monetary fee amount
	 * 6. max fee amount	: max fee amount column, i.e. max monetary fee amount
	 * 7. fixed fee percent	: fixed fee percent column, i.e. percent fee that always applies
	 * 8. min fee percent	: min fee percent column, i.e. min percent fee
	 * 9. max fee percent	: max fee percent column, i.e. max percent fee
	 */
	public static final String TRANSFER_TABLE = "transferTable";				// transfer table name
	public static final String TRANSFER_ID = "transferId";						// id
	public static final String TRANSFER_NAME = "transferName";					// display name
	public static final String TRANSFER_DELAY = "transferDelay";				// delay in hours
	public static final String TRANSFER_AMTFEE = "transferAmtFee";				// fixed fee amount
	public static final String TRANSFER_MINAMT = "transferMinAmt";				// min fee amount
	public static final String TRANSFER_MAXAMT = "transferMaxAmt";				// max fee amount
	public static final String TRANSFER_PERCENTFEE = "transferPercentFee";		// fixed fee percent
	public static final String TRANSFER_MINPERCENT = "transferMinPercent";		// min fee percent
	public static final String TRANSFER_MAXPERCENT = "transferMaxPercent";		// max fee percent
	private static FieldDefinition transferColumns[] = {
		new FieldDefinition(TRANSFER_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(TRANSFER_NAME, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(TRANSFER_DELAY, FieldDefinition.INTEGER_DEFAULT, "0"),
		new FieldDefinition(TRANSFER_AMTFEE, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(TRANSFER_MINAMT, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(TRANSFER_MAXAMT, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(TRANSFER_PERCENTFEE, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(TRANSFER_MINPERCENT, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(TRANSFER_MAXPERCENT, FieldDefinition.REAL_DEFAULT, "0.0"),
	};
	
	/* Account table definition
	 *  1. id					: id column
	 *  2. name					: display name column
	 *  3. nickname				: nickname column
	 *  4. type					: account type column refers to account type table
	 *  5. bank					: bank column refers to bank table
	 *  6. currency				: account currency column refers to currency table
	 *  7. initial balance		: initial balance column
	 *  8. date					: date account was opened column
	 *  9. current balance		: current balance column
	 * 10. available balance	: available balance column
	 * 11. limit				: limit column, for credit cards this will be the credit limit, and for current accounts its the overdraft limit
	 * 12. number				: account number column
	 */
	public static final String ACCOUNT_TABLE = "accountTable";					// account table name
	public static final String ACCOUNT_ID = "accountId";						// id
	public static final String ACCOUNT_NAME = "accountName";					// display name
	public static final String ACCOUNT_NICKNAME = "accountNickname";			// nickname, used for text banking
	public static final String ACCOUNT_TYPE = "accountType";					// type
	public static final String ACCOUNT_BANK = "accountBank";					// bank
	public static final String ACCOUNT_CURRENCY = "accountCurrency";			// currency
	public static final String ACCOUNT_INITBAL = "accountInitBal";				// initial balance
	public static final String ACCOUNT_DATE = "accountDate";					// date account was opened in UTC
	public static final String ACCOUNT_CURRENTBAL = "accountCurrentBal";		// current balance
	public static final String ACCOUNT_AVAILBAL = "accountAvailBal";			// available balance
	public static final String ACCOUNT_LIMIT = "accountLimit";					// limit
	public static final String ACCOUNT_NUMBER = "accountNumber";				// account number
	private static FieldDefinition accountColumns[] = {
		new FieldDefinition(ACCOUNT_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(ACCOUNT_NAME, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(ACCOUNT_NICKNAME, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(ACCOUNT_TYPE, FieldDefinition.INTEGER_REFERENCES, ACCTYPE_TABLE, new String[] { ACCTYPE_ID }),
		new FieldDefinition(ACCOUNT_BANK, FieldDefinition.INTEGER_REFERENCES, BANK_TABLE, new String[] { BANK_ID }),
		new FieldDefinition(ACCOUNT_CURRENCY, FieldDefinition.INTEGER_REFERENCES, CURRENCY_TABLE, new String[] { CURRENCY_ID }),
		new FieldDefinition(ACCOUNT_INITBAL, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(ACCOUNT_DATE, FieldDefinition.TEXT_DEFAULT, "CURRENT_TIMESTAMP"),
		new FieldDefinition(ACCOUNT_CURRENTBAL, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(ACCOUNT_AVAILBAL, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(ACCOUNT_LIMIT, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(ACCOUNT_NUMBER, FieldDefinition.TEXT_DEFAULT, ""),
	};
	// key/value fields for default account entries, e.g. <item>name=Cash;nickname=cash;type=Cash</item>
	public static final String ACCOUNT_NAME_KEY = "name";
	public static final String ACCOUNT_NICKNAME_KEY = "nickname";
	public static final String ACCOUNT_TYPE_KEY = "type";
	public static final String ACCOUNT_CURRENCY_KEY = "currency";
	public static final String ACCOUNT_BANK_KEY = "bank";
	public static final String ACCOUNT_INITBAL_KEY = "init";
	public static final String ACCOUNT_DATE_KEY = "date";
	public static final String ACCOUNT_CURRENTBAL_KEY = "current";
	public static final String ACCOUNT_LIMIT_KEY = "limit";
	public static final String ACCOUNT_NUMBER_KEY = "num";

	
	/* Payee table definition
	 * 1. id				: id column
	 * 2. name				: name column
	 */
	public static final String PAYEE_TABLE = "payeeTable";						// payee table name
	public static final String PAYEE_ID = "payeeId";							// id
	public static final String PAYEE_NAME = "payeeName";						// name
	private static FieldDefinition payeeColumns[] = {
		new FieldDefinition(PAYEE_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(PAYEE_NAME, FieldDefinition.TEXT_NOT_NULL_COLLATE_NOCASE),
	};


	/* Categories table definition
	 * 1. id				: id column
	 * 2. name				: display name column
	 * 3. parent			: parent column, refers to categories table 
	 * 4. level				: level column 
	 * 5. path				: path column
	 * 6. flags				: flags column 
	 */
	public static final String CATEGORY_TABLE = "categoryTable";				// category table name
	public static final String CATEGORY_ID = "categoryId";						// id
	public static final String CATEGORY_NAME = "categoryName";					// category name
	public static final String CATEGORY_PARENT = "categoryParent";				// parent
	public static final String CATEGORY_LEVEL = "categoryLevel";				// level
	public static final String CATEGORY_PATH = "categoryPath";					// path to category
	public static final String CATEGORY_FLAGS = "categoryFlags";				// flags
	private static FieldDefinition categoryColumns[] = {
		new FieldDefinition(CATEGORY_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(CATEGORY_NAME, FieldDefinition.TEXT_NOT_NULL),
		new FieldDefinition(CATEGORY_PARENT, FieldDefinition.INTEGER_REFERENCES, CATEGORY_TABLE, new String[] { CATEGORY_ID }),
		new FieldDefinition(CATEGORY_LEVEL, FieldDefinition.INTEGER_DEFAULT, "0"),
		new FieldDefinition(CATEGORY_PATH, FieldDefinition.TEXT_DEFAULT, ""),
		new FieldDefinition(CATEGORY_FLAGS, FieldDefinition.INTEGER_DEFAULT, "0"),
	};
	// Special case items are preceded by "#sysXXX"
    private static final String DB_DEFAULT_PREFIX = "#sys";	// prefix for unassigned item in database defaults array
    private static final String DB_DEFAULT_UNASSIGNED_PREFIX = DB_DEFAULT_PREFIX + "Unassigned";	// prefix for unassigned item in database defaults array
    private static final String DB_DEFAULT_TRANSFER_PREFIX = DB_DEFAULT_PREFIX + "Transfer";		// prefix for transfer item in database defaults array
    private static final String DB_DEFAULT_INCOME_PREFIX = DB_DEFAULT_PREFIX + "Income";			// prefix for income item in database defaults array
    private static final String DB_DEFAULT_EXPENSE_PREFIX = DB_DEFAULT_PREFIX + "Expense";			// prefix for expense item in database defaults array
    private static final String DB_DEFAULT_IMBALANCE_PREFIX = DB_DEFAULT_PREFIX + "Imbalance";		// prefix for imbalance item in database defaults array
    private static final String DB_DEFAULT_SPLIT_PREFIX = DB_DEFAULT_PREFIX + "Split";				// prefix for split item in database defaults array

    
	/* Transaction table definition
	 *  1. id				: id column
	 *  2. origin			: origin column, this will be the phone number the transaction came from or 0 for this device
	 *  3. origin id		: origin id column, this is the transaction id (i.e. column 1) from the origin device database
	 *  4. type				: type column, i.e. credit/debit/transfer
	 *  5. src account		: source account column for debit/transfer, refers to account table
	 *  6. dest account		: destination account column for credit/transfer, refers to account table
	 *  7. send date/time	: send date/time column, i.e. date & time transaction occurred in the form "YYYY-MM-DD HH:MM:SS"
	 *  8. receive date/time: receive date/time column, i.e. date & time transaction was/will be finalised in the form "YYYY-MM-DD HH:MM:SS"
	 *  9. status			: status column, refer to Transaction class for possible values
	 * 10. amount			: amount column
	 * 11. rate				: rate column
	 * 12. payee			: payee/payer column, refers to payee table
	 * 13. ref				: reference column
	 * 14. note				: note column
	 * 15. category			: category column, refers to categories table
	 * 16. min				: min amount column, i.e. none/monetary/percent min, e.g. "#10.2" is monetary min 10.2
	 * 17. max				: max amount column, i.e. none/monetary/percent max, e.g. "%1.5" is percent max 1.5
	 * 18. parent 			: parent transaction column, refers to transactions table but is not a constraint
	 * 19. flags 			: flags column
	 */
	public static final String TRANSACTION_TABLE = "transTable";				// transaction table name
	public static final String TRANSACTION_ID = "transId";						// id
	public static final String TRANSACTION_ORIGIN = "transOrigin";				// origin
	public static final String TRANSACTION_ORIGIN_ID = "transOriginID";			// origin id
	public static final String TRANSACTION_TYPE = "transType";					// type
	public static final String TRANSACTION_SRC = "transSrcAccount";				// source account
	public static final String TRANSACTION_DEST = "transDestAccount";			// destination account
	public static final String TRANSACTION_SENDDATE = "transSendDate";			// send date/time in UTC
	public static final String TRANSACTION_RECVDATE = "transRecvDate";			// receive date/time in UTC
	public static final String TRANSACTION_STATUS = "transStatus";				// status,
	public static final String TRANSACTION_AMOUNT = "transAmt";					// amount
	public static final String TRANSACTION_RATE = "transRate";					// rate
	public static final String TRANSACTION_PAYEE = "transPayee";				// payee
	public static final String TRANSACTION_REF = "transRef";					// reference
	public static final String TRANSACTION_NOTE = "transNote";					// note
	public static final String TRANSACTION_CATEGORY = "transCategory";			// category
	public static final String TRANSACTION_MIN = "transMin";					// min amount
	public static final String TRANSACTION_MAX = "transMax";					// max amount
	public static final String TRANSACTION_PARENT = "transParent";				// parent transaction
	public static final String TRANSACTION_FLAGS = "transFlags";				// flags
	
	/** Value of parent field in a parent transaction, i.e. if the parent field is other than this its a child transaction */
	public static final int TRANSACTION_PARENT_ID = 0; 
	public static final int TRANSACTION_RATE_DFLT = 1; 

	private static FieldDefinition transactionColumns[] = {
		new FieldDefinition(TRANSACTION_ID, FieldDefinition.INTEGER_PRIMARY_KEY_ASC),
		new FieldDefinition(TRANSACTION_ORIGIN, FieldDefinition.INTEGER_DEFAULT, "0"),	// default origin this device
		new FieldDefinition(TRANSACTION_ORIGIN_ID, FieldDefinition.INTEGER_NOT_NULL),
		new FieldDefinition(TRANSACTION_TYPE, FieldDefinition.INTEGER_CHECK_BETWEEN_CONSTRAINT, 
							new String[] { Integer.toString(Transaction.TRANSACTION_TYPE_MIN), Integer.toString(Transaction.TRANSACTION_TYPE_MAX) }),
		new FieldDefinition(TRANSACTION_SRC, FieldDefinition.INTEGER_REFERENCES, ACCOUNT_TABLE, new String[] { ACCOUNT_ID }),
		new FieldDefinition(TRANSACTION_DEST, FieldDefinition.INTEGER_REFERENCES, ACCOUNT_TABLE, new String[] { ACCOUNT_ID }),
		new FieldDefinition(TRANSACTION_SENDDATE, FieldDefinition.TEXT_DEFAULT, "CURRENT_TIMESTAMP"),
		new FieldDefinition(TRANSACTION_RECVDATE, FieldDefinition.TEXT_DEFAULT, "CURRENT_TIMESTAMP"),
		new FieldDefinition(TRANSACTION_STATUS, FieldDefinition.INTEGER_DEFAULT, Integer.toString(Transaction.TRANSSTATUS_INVALID)),
		new FieldDefinition(TRANSACTION_AMOUNT, FieldDefinition.REAL_DEFAULT, "0.0"),
		new FieldDefinition(TRANSACTION_RATE, FieldDefinition.REAL_DEFAULT, Float.toString(TRANSACTION_RATE_DFLT)),
		new FieldDefinition(TRANSACTION_PAYEE, FieldDefinition.INTEGER_REFERENCES, PAYEE_TABLE, new String[] { PAYEE_ID }),
		new FieldDefinition(TRANSACTION_REF, FieldDefinition.TEXT_VALUE),
		new FieldDefinition(TRANSACTION_NOTE, FieldDefinition.TEXT_VALUE),
		new FieldDefinition(TRANSACTION_CATEGORY, FieldDefinition.INTEGER_REFERENCES, CATEGORY_TABLE, new String[] { CATEGORY_ID }),
		new FieldDefinition(TRANSACTION_MIN, FieldDefinition.TEXT_VALUE),
		new FieldDefinition(TRANSACTION_MAX, FieldDefinition.TEXT_VALUE),
		new FieldDefinition(TRANSACTION_PARENT, FieldDefinition.INTEGER_DEFAULT, String.valueOf(TRANSACTION_PARENT_ID) ),
		new FieldDefinition(TRANSACTION_FLAGS, FieldDefinition.INTEGER_DEFAULT, "0" ),
		// constraints must come at the end
		new FieldDefinition(null, FieldDefinition.UNIQUE_CONSTRAINT, null, new String[] { TRANSACTION_ORIGIN, TRANSACTION_ORIGIN_ID }),
	};
	private static final String TRANSACTION_LIMIT_MONETARY = "#";	// monetary limit marker
	private static final String TRANSACTION_LIMIT_PERCENT = "%";	// percent limit marker

	
	private static class TableDefinition {
		public String tableName;
		public FieldDefinition[] columnsDef;
		/**
		 * @param tableName
		 * @param userColumns
		 */
		public TableDefinition(String tableName, FieldDefinition[] columnsDef) {
			super();
			this.tableName = tableName;
			this.columnsDef = columnsDef;
		}
		
	}
	private static class TableList {
		/* table list in order of creation! */
		private TableDefinition[] tableList = new TableDefinition[] {
			new TableDefinition(USER_TABLE, userColumns),
			new TableDefinition(ACCTYPE_TABLE, accTypeColumns),
			new TableDefinition(CURRENCY_TABLE, currencyColumns),
			new TableDefinition(BANK_TABLE, bankColumns),
			new TableDefinition(TEXTTYPES_TABLE, textTypesColumns),
			new TableDefinition(TEXTTRANS_TABLE, textTransColumns),
			new TableDefinition(TRANSFER_TABLE, transferColumns),
			new TableDefinition(ACCOUNT_TABLE, accountColumns),
			new TableDefinition(PAYEE_TABLE, payeeColumns),
			new TableDefinition(CATEGORY_TABLE, categoryColumns),
			new TableDefinition(TRANSACTION_TABLE, transactionColumns),
		};
		public TableDefinition[] getTableCreationList() {
			return tableList;
		}
		public TableDefinition[] getTableDropList() {
			TableDefinition[] list = new TableDefinition[tableList.length];
			for ( int i = tableList.length - 1, j = 0; i >= 0; --i, ++j )
				list[j] = tableList[i];
			return list;
		}
	}
	
	

	/* URI related definitions */
	public static final String CONTENT_URI_BASE = "ie.ibuttimer.provider.pmat";
	
	/**
	 * Make a Uri string with the specified path.<br>
	 * <b>Note:</b> The scheme and authority are fixed, i.e. &ltscheme&gt://&ltauthority&gt&ltpath&gt 
	 * @param path	- path for uri
	 * @return		Full Uri string
	 */
	public static String makeUriString(String path) {
		return "content://" + CONTENT_URI_BASE + "/" + path;
	}
	
	/**
	 * Make a Uri string with the specified path and segment.<br>
	 * <b>Note:</b> The scheme and authority are fixed, i.e. &ltscheme&gt://&ltauthority&gt&ltpath&gt&ltid&gt 
	 * @param path		- path for uri
	 * @param segment	- segment to append to uri 
	 * @return			Full Uri string
	 */
	public static String makeUriString(String path, String segment) {
		return makeUriString(path) + "/" + segment;
	}
	
	/**
	 * Make a Uri string with the specified path and id.<br>
	 * <b>Note:</b> The scheme and authority are fixed, i.e. &ltscheme&gt://&ltauthority&gt&ltpath&gt&ltid&gt 
	 * @param path	- path for uri
	 * @param id	- id to append to uri 
	 * @return		Full Uri string
	 */
	public static String makeUriString(String path, int id) {
		return makeUriString(path, String.valueOf(id));
	}
	
	/**
	 * Make a Uri with the specified path.<br>
	 * <b>Note:</b> The scheme and authority are fixed, i.e. &ltscheme&gt://&ltauthority&gt&ltpath&gt&ltid&gt 
	 * @param path	- path for uri
	 * @return		Uri object
	 */
	public static Uri makeUri(String path) {
		return Uri.parse( makeUriString(path) );
	}
	
	/**
	 * Make a Uri with the specified path and segment.<br>
	 * <b>Note:</b> The scheme and authority are fixed, i.e. &ltscheme&gt://&ltauthority&gt&ltpath&gt&ltid&gt 
	 * @param path		- path for uri
	 * @param segment	- segment to append to uri 
	 * @return		Uri object
	 */
	public static Uri makeUri(String path, String segment) {
		return Uri.parse( makeUriString(path, segment) );
	}

	/**
	 * Make a Uri with the specified path and id.<br>
	 * <b>Note:</b> The scheme and authority are fixed, i.e. &ltscheme&gt://&ltauthority&gt&ltpath&gt&ltid&gt 
	 * @param path	- path for uri
	 * @param id	- id to append to uri 
	 * @return		Uri object
	 */
	public static Uri makeUri(String path, int id) {
		return Uri.parse( makeUriString(path, id) );
	}
	
	/*
	 * An absolute hierarchical URI reference follows the pattern: 
	 * 	<scheme>://<authority><absolute path>?<query>#<fragment> 
	 * 
	 * Relative URI references (which are always hierarchical) follow one of two patterns: 
	 * 	<relative or absolute path>?<query>#<fragment> or //<authority><absolute path>?<query>#<fragment> 
	 * 
	 * An opaque URI follows this pattern: 
	 * 	<scheme>:<opaque part>#<fragment> 
	 */
	
	
	public static final String CURRENCY_URI_PATH = "currency";
	/** URI for currencies */
	public static final Uri CURRENCY_URI = makeUri(CURRENCY_URI_PATH);
	
	public static final String BANKS_URI_PATH = "banks";
	/** URI for banks */
	public static final Uri BANKS_URI = makeUri(BANKS_URI_PATH);
	
	public static final String ACCTYPE_URI_PATH = "account_types";
	/** URI for account types */
	public static final Uri ACCTYPE_URI = makeUri(ACCTYPE_URI_PATH);
	
	public static final String TEXTTRANS_URI_PATH = "text_trans";
	/** URI for sms transactions */
	public static final Uri TEXTTRANS_URI = makeUri(TEXTTRANS_URI_PATH);
	
	public static final String TEXTTYPES_URI_PATH = "text_types";
	/** URI for sms types */
	public static final Uri TEXTTYPES_URI = makeUri(TEXTTYPES_URI_PATH);
	
	public static final String TRANSFER_URI_PATH = "transfers";
	/** URI for transfers */
	public static final Uri TRANSFER_URI = makeUri(TRANSFER_URI_PATH);

	public static final String ACCOUNT_URI_PATH = "accounts";
	public static final String ACCOUNT_ACC_URI_PATH = ACCOUNT_URI_PATH + "/account";
	public static final String ACCOUNT_SNAP_URI_PATH = ACCOUNT_URI_PATH + "/snapshot";
	public static final String ACCOUNT_BASIC_URI_PATH = ACCOUNT_URI_PATH + "/basic";
	public static final String ACCOUNT_CURR_CATEGORY_URI_PATH = ACCOUNT_URI_PATH + "/currcat";
	public static final String ACCOUNT_NICKNAME_URI_PATH = ACCOUNT_ACC_URI_PATH + "/nickname";
	public static final String ACCOUNT_ADDTRANS_URI_PATH = ACCOUNT_URI_PATH + "/addtransaction";
	/** URI for all accounts */
	public static final Uri ACCOUNT_ACC_URI = makeUri(ACCOUNT_ACC_URI_PATH);
	/** URI for all account snapshot */
	public static final Uri ACCOUNT_SNAPSHOT_URI = makeUri(ACCOUNT_SNAP_URI_PATH);
	/** URI for all account basic info */
	public static final Uri ACCOUNT_BASIC_URI = makeUri(ACCOUNT_BASIC_URI_PATH);
	/** URI for all account category info */
	public static final Uri ACCOUNT_CURR_CATEGORY_URI = makeUri(ACCOUNT_CURR_CATEGORY_URI_PATH);
	/** URI for single account based on nickname */
	public static final Uri ACCOUNT_NICKNAME_URI = makeUri(ACCOUNT_NICKNAME_URI_PATH);
	/** URI for 'add transaction' related information */
	public static final Uri ACCOUNT_ADDTRANS_URI = makeUri(ACCOUNT_ADDTRANS_URI_PATH);
	
	public static final String PAYEE_URI_PATH = "payees";
	public static final String PAYEE_NAME_URI_PATH = PAYEE_URI_PATH + "/name";
	/** URI for all payees */
	public static final Uri PAYEE_URI = makeUri(PAYEE_URI_PATH);
	/** URI for single named payees */
	public static final Uri PAYEE_NAME_URI = makeUri(PAYEE_NAME_URI_PATH);

	public static final String CATEGORY_URI_PATH = "category";
	public static final Uri CATEGORY_URI = makeUri(CATEGORY_URI_PATH);

	public static final String TRANSACTION_URI_PATH = "transactions";
	public static final String TRANSACTION_INFO_URI_PATH = TRANSACTION_URI_PATH + "/info";
	/** URI for all transactions */
	public static final Uri TRANSACTION_URI = makeUri(TRANSACTION_URI_PATH);
	public static final Uri TRANSACTION_INFO_URI = makeUri(TRANSACTION_INFO_URI_PATH);

	public static final String USER_URI_PATH = "users";
	/** URI for all users */
	public static final Uri USER_URI = makeUri(USER_URI_PATH);
	
	public static final String DB_CONTROL_STEPS = "dbctrl_steps";
	public static final String DB_CONTROL_URI_PATH = "dbctrl";
	public static final String POPULATE_URI_PATH = DB_CONTROL_URI_PATH + "/populate";
	public static final String UPGRADE_URI_PATH = DB_CONTROL_URI_PATH + "/upgrade";
	public static final String COUNT_ROWS_URI_PATH = DB_CONTROL_URI_PATH + "/countrows";

	/** URI for database actions */
	public static final String POPULATE_URI_STR = makeUriString(POPULATE_URI_PATH);
	public static final Uri POPULATE_URI = Uri.parse( POPULATE_URI_STR );
	public static final String UPGRADE_URI_STR = makeUriString(UPGRADE_URI_PATH);
	public static final Uri UPGRADE_URI = Uri.parse( UPGRADE_URI_STR );
	public static final String COUNT_ROWS_URI_STR = makeUriString(COUNT_ROWS_URI_PATH);
	public static final Uri COUNT_ROWS_URI = Uri.parse( COUNT_ROWS_URI_STR );

	
	
	// Create the constants used to differentiate between the different URI requests.
	private static final int BY_ID_OFFSET 					= 1000;		// offset applied to all by id uri's
	private static final int BY_SEGEMENT_OFFSET 			= 2000;		// offset applied to all by last segment uri's
	
	private static final int CURRENCY_URI_ID 				= 1;									// all currencies
	private static final int CURRENCY_BY_ID_URI_ID 			= CURRENCY_URI_ID + BY_ID_OFFSET;		// single currency by id
	
	private static final int ACCTYPE_URI_ID 				= 10;									// all account types
	private static final int ACCTYPE_BY_ID_URI_ID 			= ACCTYPE_URI_ID + BY_ID_OFFSET;		// single account type by id
	
	private static final int BANKS_URI_ID					= 20;									// all banks
	private static final int BANKS_URI_ID_BY_ID 			= BANKS_URI_ID + BY_ID_OFFSET;			// single bank by id
	
	private static final int ACCOUNT_ACC_URI_ID				= 30;									// all accounts
	private static final int ACCOUNT_ACC_URI_ID_BY_ID 		= ACCOUNT_ACC_URI_ID + BY_ID_OFFSET;	// single account by id
	private static final int ACCOUNT_ACC_URI_ID_BY_NICKNAME	= ACCOUNT_ACC_URI_ID + BY_SEGEMENT_OFFSET;	// single account by nickname
	private static final int ACCOUNT_ADDTRANS_URI_ID		= ACCOUNT_ACC_URI_ID + 2;				// info needed by AddTransaction activity for all accounts
	private static final int ACCOUNT_ADDTRANS_URI_ID_BY_ID	= ACCOUNT_ADDTRANS_URI_ID + BY_ID_OFFSET;	// info needed by AddTransaction activity for single account

	private static final int ACCOUNT_SNAP_URI_ID			= 100;									// snapshot of all accounts
	private static final int ACCOUNT_SNAP_URI_ID_BY_ID 		= ACCOUNT_SNAP_URI_ID + BY_ID_OFFSET;	// snapshot of single account

	private static final int ACCOUNT_BASIC_URI_ID			= 110;									// basic info of all accounts
	private static final int ACCOUNT_BASIC_URI_ID_BY_ID 	= ACCOUNT_BASIC_URI_ID + BY_ID_OFFSET;	// basic info of single account
	private static final int ACCOUNT_CURR_CATEGORY_URI_ID	= ACCOUNT_BASIC_URI_ID + 1;				// basic info about account categories
	private static final int ACCOUNT_CURR_CATEGORY_URI_ID_BY_ID =
													ACCOUNT_CURR_CATEGORY_URI_ID  + BY_ID_OFFSET;	// basic info about account categories
	
	private static final int PAYEE_URI_ID					= 150;									// all payees
	private static final int PAYEE_URI_ID_BY_ID 			= PAYEE_URI_ID + BY_ID_OFFSET;			// single payee by id
	private static final int PAYEE_URI_ID_BY_NAME			= PAYEE_URI_ID + BY_SEGEMENT_OFFSET;	// single payee by name

	private static final int CATEGORY_URI_ID				= 170;									// get all categories
	private static final int CATEGORY_URI_ID_BY_ID 			= CATEGORY_URI_ID + BY_ID_OFFSET;		// get single category
	
	private static final int TRANSFER_URI_ID				= 180;									// get all transfers
	private static final int TRANSFER_URI_ID_BY_ID 			= TRANSFER_URI_ID + BY_ID_OFFSET;		// get single transfer
	
	private static final int TEXTTYPES_URI_ID				= 190;									// get all sms text types
	private static final int TEXTTYPES_URI_ID_BY_ID 		= TEXTTYPES_URI_ID + BY_ID_OFFSET;		// get single sms text types
	
	private static final int TEXTTRANS_URI_ID				= 200;									// get all sms text transaction templates
	private static final int TEXTTRANS_URI_ID_BY_ID 		= TEXTTRANS_URI_ID + BY_ID_OFFSET;		// get single sms text transaction templates
	
	private static final int TRANSACTION_URI_ID				= 210;									// get all transactions
	private static final int TRANSACTION_URI_ID_BY_ID 		= TRANSACTION_URI_ID + BY_ID_OFFSET;	// get single transaction
	private static final int TRANSACTION_INFO_URI_ID		= TRANSACTION_URI_ID + 1;				// get all transactions with info
	private static final int TRANSACTION_INFO_URI_ID_BY_ID	= TRANSACTION_INFO_URI_ID + BY_ID_OFFSET;	// get single transaction with info

	private static final int USER_URI_ID					= 220;									// get all users
	private static final int USER_URI_ID_BY_ID 				= USER_URI_ID + BY_ID_OFFSET;			// get single user

	private static final int POPULATE_URI_ID				= 230;									// populate the database
	private static final int POPULATE_STEP_URI_ID			= POPULATE_URI_ID + BY_ID_OFFSET;		// populate the database step
	private static final int UPGRADE_URI_ID					= POPULATE_URI_ID + 1;					// upgrade the database
	private static final int UPGRADE_STEP_URI_ID			= UPGRADE_URI_ID + BY_ID_OFFSET;		// upgrade the database step
	private static final int COUNT_ROWS_URI_ID				= POPULATE_URI_ID + 2;					// count the rows in a table

	
	
	private static final UriMatcher uriMatcher;
	private static final TableList dbTables;
	static {
		/* the token "*" that matches any text, or the token "#" that matches only numbers */

		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		uriMatcher.addURI(CONTENT_URI_BASE, CURRENCY_URI_PATH, 						CURRENCY_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, CURRENCY_URI_PATH + "/#", 				CURRENCY_BY_ID_URI_ID);

		uriMatcher.addURI(CONTENT_URI_BASE, ACCTYPE_URI_PATH, 						ACCTYPE_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCTYPE_URI_PATH + "/#", 				ACCTYPE_BY_ID_URI_ID);
		
		uriMatcher.addURI(CONTENT_URI_BASE, BANKS_URI_PATH, 						BANKS_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, BANKS_URI_PATH + "/#", 					BANKS_URI_ID_BY_ID);

		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_ACC_URI_PATH, 					ACCOUNT_ACC_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_ACC_URI_PATH + "/#", 			ACCOUNT_ACC_URI_ID_BY_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_NICKNAME_URI_PATH + "/*",		ACCOUNT_ACC_URI_ID_BY_NICKNAME);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_SNAP_URI_PATH, 					ACCOUNT_SNAP_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_SNAP_URI_PATH + "/#", 			ACCOUNT_SNAP_URI_ID_BY_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_BASIC_URI_PATH, 				ACCOUNT_BASIC_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_BASIC_URI_PATH + "/#", 			ACCOUNT_BASIC_URI_ID_BY_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_CURR_CATEGORY_URI_PATH, 		ACCOUNT_CURR_CATEGORY_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_CURR_CATEGORY_URI_PATH + "/#", 	ACCOUNT_CURR_CATEGORY_URI_ID_BY_ID);
		
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_ADDTRANS_URI_PATH, 				ACCOUNT_ADDTRANS_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, ACCOUNT_ADDTRANS_URI_PATH + "/#", 		ACCOUNT_ADDTRANS_URI_ID_BY_ID);
		
		uriMatcher.addURI(CONTENT_URI_BASE, PAYEE_URI_PATH, 						PAYEE_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, PAYEE_URI_PATH + "/#", 					PAYEE_URI_ID_BY_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, PAYEE_NAME_URI_PATH + "/*",				PAYEE_URI_ID_BY_NAME);

		uriMatcher.addURI(CONTENT_URI_BASE, CATEGORY_URI_PATH, 						CATEGORY_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, CATEGORY_URI_PATH + "/#", 				CATEGORY_URI_ID_BY_ID);
		
		uriMatcher.addURI(CONTENT_URI_BASE, TRANSFER_URI_PATH,						TRANSFER_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, TRANSFER_URI_PATH + "/#", 				TRANSFER_URI_ID_BY_ID);
		
		uriMatcher.addURI(CONTENT_URI_BASE, TEXTTYPES_URI_PATH, 					TEXTTYPES_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, TEXTTYPES_URI_PATH + "/#", 				TEXTTYPES_URI_ID_BY_ID);
		
		uriMatcher.addURI(CONTENT_URI_BASE, TEXTTRANS_URI_PATH, 					TEXTTRANS_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, TEXTTRANS_URI_PATH + "/#", 				TEXTTRANS_URI_ID_BY_ID);
		
		uriMatcher.addURI(CONTENT_URI_BASE, TRANSACTION_URI_PATH, 					TRANSACTION_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, TRANSACTION_URI_PATH + "/#", 			TRANSACTION_URI_ID_BY_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, TRANSACTION_INFO_URI_PATH, 				TRANSACTION_INFO_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, TRANSACTION_INFO_URI_PATH + "/#", 		TRANSACTION_INFO_URI_ID_BY_ID);

		uriMatcher.addURI(CONTENT_URI_BASE, USER_URI_PATH, 							USER_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, USER_URI_PATH + "/#", 					USER_URI_ID_BY_ID);

		uriMatcher.addURI(CONTENT_URI_BASE, POPULATE_URI_PATH, 						POPULATE_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, POPULATE_URI_PATH + "/#", 				POPULATE_STEP_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, UPGRADE_URI_PATH, 						UPGRADE_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, UPGRADE_URI_PATH + "/#", 				UPGRADE_STEP_URI_ID);
		uriMatcher.addURI(CONTENT_URI_BASE, COUNT_ROWS_URI_PATH, 					COUNT_ROWS_URI_ID);
		
		dbTables = new TableList();
	}
	
	
	
	/**
	 * Default constructor
	 */
	public DatabaseManager() {
		setDatabaseState(DatabaseState.DATABASE_OK);	// assume db ok
	}


	/**
	 * Decode the specified uri.
	 * @param uri	- Uri
	 * @return		UriDecode object
	 */
	private UriDecode decodeUri(Uri uri, String selection, String[] selectionArgs) {
		String table = null;
		String column = null;
		String segmentSelection;
		Uri contentUri;
		int id = uriMatcher.match(uri);
		if ( (id / BY_ID_OFFSET) > 0 || (id / BY_SEGEMENT_OFFSET) > 0 )
			segmentSelection = uri.getLastPathSegment();
		else
			segmentSelection = "";

		switch (id) {
			// currency table related queries
			case CURRENCY_BY_ID_URI_ID:
				segmentSelection = getWhereChunk( null, CURRENCY_ID, segmentSelection);
				// fall through to set table
			case CURRENCY_URI_ID:
				table = CURRENCY_TABLE;
				contentUri = CURRENCY_URI;
				break;
	
			// banks table related queries
			case BANKS_URI_ID_BY_ID:
				segmentSelection = getWhereChunk( null, BANK_ID, segmentSelection);
				// fall through to set table
			case BANKS_URI_ID:
				table = BANK_TABLE;
				contentUri = BANKS_URI;
				break;
	
			// account type table related queries
			case ACCTYPE_BY_ID_URI_ID:
				segmentSelection = getWhereChunk( null, ACCTYPE_ID, segmentSelection);
				// fall through to set table
			case ACCTYPE_URI_ID:
				table = ACCTYPE_TABLE;
				contentUri = ACCTYPE_URI;
				break;
	
			// account type table related queries
			case ACCOUNT_ACC_URI_ID_BY_NICKNAME:
				column = ACCOUNT_NICKNAME;
				// fall through to update selection string
			case ACCOUNT_ACC_URI_ID_BY_ID:
				if ( column == null )
					column = ACCOUNT_ID;
				segmentSelection = getWhereChunk( null, column, segmentSelection);
				// fall through to set table
			case ACCOUNT_ACC_URI_ID:
				table = ACCOUNT_TABLE;
				contentUri = ACCOUNT_ACC_URI;
				break;
			// the following are more complicated multi-table queries so just return id, segmentSelection & uri
			case ACCOUNT_SNAP_URI_ID_BY_ID:
			case ACCOUNT_SNAP_URI_ID:
				contentUri = ACCOUNT_SNAPSHOT_URI;
				break;
			case ACCOUNT_BASIC_URI_ID_BY_ID:
			case ACCOUNT_BASIC_URI_ID:
				contentUri = ACCOUNT_BASIC_URI;
				break;
			case ACCOUNT_ADDTRANS_URI_ID_BY_ID:
			case ACCOUNT_ADDTRANS_URI_ID:
				contentUri = ACCOUNT_ADDTRANS_URI;
				break;
			case ACCOUNT_CURR_CATEGORY_URI_ID_BY_ID:
			case ACCOUNT_CURR_CATEGORY_URI_ID:
				contentUri = ACCOUNT_CURR_CATEGORY_URI;
				break;
				
			// payee table related queries
			case PAYEE_URI_ID_BY_NAME:
				column = PAYEE_NAME;
				// fall through to update selection string
			case PAYEE_URI_ID_BY_ID:
				if ( column == null )
					column = PAYEE_ID;
				segmentSelection = getWhereChunk( null, column, segmentSelection);
				// fall through to set table
			case PAYEE_URI_ID:
				table = PAYEE_TABLE;
				contentUri = PAYEE_URI;
				break;
	
			// category table related queries
			case CATEGORY_URI_ID_BY_ID:
				segmentSelection = getWhereChunk( null, CATEGORY_ID, segmentSelection);
				// fall through to set table
			case CATEGORY_URI_ID:
				table = CATEGORY_TABLE;
				contentUri = CATEGORY_URI;
				break;
	
			// transfer table related queries
			case TRANSFER_URI_ID_BY_ID:
				segmentSelection = getWhereChunk( null, TRANSFER_ID, segmentSelection);
				// fall through to set table
			case TRANSFER_URI_ID:
				table = TRANSFER_TABLE;
				contentUri = TRANSFER_URI;
				break;
	
			// sms text type related queries
			case TEXTTYPES_URI_ID_BY_ID:
				segmentSelection = getWhereChunk( null, TEXTTYPES_ID, segmentSelection);
				// fall through to set table
			case TEXTTYPES_URI_ID:
				table = TEXTTYPES_TABLE;
				contentUri = TEXTTYPES_URI;
				break;
	
			// sms texts related queries
			case TEXTTRANS_URI_ID_BY_ID:
				segmentSelection = getWhereChunk( null, TEXTTRANS_ID, segmentSelection);
				// fall through to set table
			case TEXTTRANS_URI_ID:
				table = TEXTTRANS_TABLE;
				contentUri = TEXTTRANS_URI;
				break;

			// user related queries
			case USER_URI_ID_BY_ID:
				segmentSelection = getWhereChunk( null, USER_ID, segmentSelection);
				// fall through to set table
			case USER_URI_ID:
				table = USER_TABLE;
				contentUri = USER_URI;
				break;

			// transaction table related queries
			case TRANSACTION_URI_ID_BY_ID:
				segmentSelection = getWhereChunk( null, TRANSACTION_ID, segmentSelection);
				// fall through to set table
			case TRANSACTION_URI_ID:
				table = TRANSACTION_TABLE;
				contentUri = TRANSACTION_URI;
				break;
				// the following are more complicated multi-table queries so just return id, segmentSelection & uri
			case TRANSACTION_INFO_URI_ID_BY_ID:
			case TRANSACTION_INFO_URI_ID:
				contentUri = TRANSACTION_INFO_URI;
				break;

			default:
				return null;
		}

		boolean seg = !TextUtils.isEmpty(segmentSelection);
		boolean sel = !TextUtils.isEmpty(selection);
		if ( seg || sel ) {
			if ( seg && sel )
				segmentSelection += " AND (" + selection + ")";	// prepend the id test to the selection
			else if ( sel )
				segmentSelection = selection;					// use selection
			// else use segmentSelection
		}
		else {
			segmentSelection = null;	// no selection criteria at all
		}
		return new UriDecode(table, segmentSelection, selectionArgs, id, contentUri );
	}
	
	private class UriDecode {
		String table;
		String selection;
		String[] selectionArgs;
		int id;
		Uri contentUri;
		/**
		 * @param table
		 * @param selection
		 * @param selectionArgs
		 * @param id
		 * @param contentUri
		 */
		public UriDecode(String table, String selection, String[] selectionArgs, int id, Uri contentUri) {
			super();
			this.table = table;
			this.selection = selection;
			this.selectionArgs = selectionArgs;
			this.id = id;
			this.contentUri = contentUri;
		}
	}
	

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		UriDecode decode = decodeUri(uri, selection, selectionArgs);

		if (decode == null) {
			Logger.d(this.getClass().getName() + " unrecognised uri " + uri.toString() );
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return db.delete(decode.table, decode.selection, decode.selectionArgs);
	}



	@Override
	public String getType(Uri uri) {
		int id = uriMatcher.match(uri);
		if( id == -1 )
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		else if ( (id / BY_ID_OFFSET) > 0 )
			return "vnd.android.cursor.item/vnd.ibuttimer.pmat";
		else
			return "vnd.android.cursor.dir/vnd.ibuttimer.pmat";
	}


	/**
	 * Update the current and/or available balances for the account, based on transaction status and amount  
	 * @param accountID		Account to update
	 * @param amount		Transaction amount
	 * @param status		Transaction status
	 * @transactionType		Transaction type
	 * @return				Number of rows updated
	 */
	private int updateAccountWithTransaction(String accountID, double amount, int status, int transactionType) {

		String[] tables = new String[] { ACCOUNT_TABLE, ACCTYPE_TABLE	};
		String[] joinColumns = new String[] { ACCOUNT_TYPE, ACCTYPE_ID	};
		String[][] columns = new String[][] {
				new String[] { ACCOUNT_CURRENTBAL, ACCOUNT_AVAILBAL, ACCOUNT_LIMIT	},
				new String[] { ACCTYPE_LIMIT	},
		};
		String[] projection = SQLiteCommandFactory.getProjection(tables, columns);
		
		// update the relevant account
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		Cursor c;
		String selection = ACCOUNT_ID + "=" + accountID;
		int count = 0;		// number of rows updated

		qb.appendWhere(selection);
//		qb.setTables(ACCOUNT_TABLE);

		qb.setTables( SQLiteCommandFactory.getInnerJoinCommand(tables, joinColumns) );

		
		// get current values
		c = qb.query(db, projection, null, null, null, null, null);

		if ( c.moveToFirst() ) {

			ContentValues accValues = new ContentValues();
			double available = c.getDouble( c.getColumnIndex(ACCOUNT_AVAILBAL) );
			double current = c.getDouble( c.getColumnIndex(ACCOUNT_CURRENTBAL) );
			int type = c.getInt( c.getColumnIndex(ACCTYPE_LIMIT) );
			double newAvailable = available; 
			double newCurrent = current;
			
			switch ( type ) {
				case AccountType.LIMIT_CREDIT:
					// credit limit so available is limit less current balance +/- amount
					if ( transactionType == Transaction.TRANSACTION_DEBIT && amount < 0.0 )
						amount *= -1.0;		// ensure positive as debit's increase current and decrease available  
					else if ( transactionType == Transaction.TRANSACTION_CREDIT && amount > 0.0 ) 
						amount *= -1.0;		// ensure negative as credit's decrease current and increase available  
					switch ( status )  {
						case Transaction.TRANSSTATUS_COMPLETE:
							newCurrent = current + amount;
							// fall thru to update available
						case Transaction.TRANSSTATUS_INPROGRESS:
							newAvailable = available - amount;
							break;
					}
					break;
//				case AccountType.LIMIT_OVERDRAFT:
				default:
					// available is current available balance +/- amount
					if ( transactionType == Transaction.TRANSACTION_DEBIT && amount > 0.0 )
						amount *= -1.0;		// ensure negative as debit's decrease current and decrease available  
					else if ( transactionType == Transaction.TRANSACTION_CREDIT && amount < 0.0 ) 
						amount *= -1.0;		// ensure positive as credit's increase current and increase available  
					switch ( status )  {
						case Transaction.TRANSSTATUS_COMPLETE:
							newCurrent = current + amount;
							// fall thru to update available
						case Transaction.TRANSSTATUS_INPROGRESS:
							newAvailable = available + amount;
							break;
					}
					break;
			}
			
			if ( newCurrent != current )
				accValues.put(ACCOUNT_CURRENTBAL, newCurrent);
			if ( newAvailable != available )
				accValues.put(ACCOUNT_AVAILBAL, newAvailable);

			if ( accValues.size() > 0 ) {
				count = db.update(ACCOUNT_TABLE, accValues, selection, null);
			}
		}
		c.close();
		
		return count;
	}

	/**
	 * Update the relevant account balances
	 * @param values
	 */
	private void updateAccountBalances(ContentValues values) {
		
		Long parent = values.getAsLong(TRANSACTION_PARENT);
		if ( parent == null || parent.longValue() == TRANSACTION_PARENT_ID ) {
			
			// parent transaction so update balances

			int transactionType = getAsInteger(values, TRANSACTION_TYPE, Transaction.TRANSACTION_INVALID);
			double amount = getAsDouble(values, TRANSACTION_AMOUNT, 0.0);
			int status = getAsInteger(values, TRANSACTION_STATUS, Transaction.TRANSSTATUS_COMPLETE);
			String debitAccount = null;
			String creditAccount = null;
			double debitRate = getAsDouble(values, TRANSACTION_RATE, 0.0);
			double creditRate = debitRate;

			// update the relevant account(s)
			int baseTransactionType = Transaction.getBaseTransaction(transactionType);
			switch ( baseTransactionType ) {
				case Transaction.TRANSACTION_TRANSFER:
					// for a transfer transaction rate applies to the credit a/c only
					if ( debitRate > 0.0 )
						debitRate = 1.0;
					// fall thru
				case Transaction.TRANSACTION_CREDIT:
					creditAccount = values.getAsString(TRANSACTION_DEST);
					if ( baseTransactionType == Transaction.TRANSACTION_CREDIT )
						break;	// only need credit a/c for a credit
					// fall thru to set debit account
				case Transaction.TRANSACTION_DEBIT:
					debitAccount = values.getAsString(TRANSACTION_SRC);
					break;
				default:
					break;
			}					
			
			if ( Transaction.isValidTransactionReversal(transactionType) ) {
				// reversing transaction so swap debit & credit accounts
				String temp = debitAccount;
				debitAccount = creditAccount;
				creditAccount = temp;
			}

			// don't update accounts with templates
			if ( !Transaction.isValidTransactionTemplate(transactionType) ) {
				if ( debitAccount != null ) {
					double accAmount = amount;
					if ( debitRate > 0.0 )
						accAmount *= debitRate;
					updateAccountWithTransaction(debitAccount, accAmount, status, Transaction.TRANSACTION_DEBIT);
				}
				if ( creditAccount != null ) {
					double accAmount = amount;
					if ( creditRate > 0.0 )
						accAmount *= creditRate;
					updateAccountWithTransaction(creditAccount, accAmount, status, Transaction.TRANSACTION_CREDIT);
				}
			}
		}			
	}

	
	
	
	/**
	 * Gets a value and converts it to an integer.
	 * @param values		- ContentValues to get value from
	 * @param key			- the value to get
	 * @param defaultVal	- value to return if <code>key</code> not found.
	 * @return				the integer value, or <code>defaultVal</code> if the value is missing or cannot be converted 
	 */
	private int getAsInteger(ContentValues values, String key, int defaultVal) {
		int result = 0;
		if ( values.containsKey(key) )
			result = values.getAsInteger(key);
		else
			result = defaultVal;
		return result;
	}
	
	/**
	 * Gets a value and converts it to a long.
	 * @param values		- ContentValues to get value from
	 * @param key			- the value to get
	 * @param defaultVal	- value to return if <code>key</code> not found.
	 * @return				the long value, or <code>defaultVal</code> if the value is missing or cannot be converted 
	 */
	private long getAsLong(ContentValues values, String key, long defaultVal) {
		long result = 0;
		if ( values.containsKey(key) )
			result = values.getAsLong(key);
		else
			result = defaultVal;
		return result;
	}
	
	/**
	 * Gets a value and converts it to a double.
	 * @param values		- ContentValues to get value from
	 * @param key			- the value to get
	 * @param defaultVal	- value to return if <code>key</code> not found.
	 * @return				the double value, or <code>defaultVal</code> if the value is missing or cannot be converted 
	 */
	private double getAsDouble(ContentValues values, String key, double defaultVal) {
		double result = 0;
		if ( values.containsKey(key) )
			result = values.getAsDouble(key);
		else
			result = defaultVal;
		return result;
	}
	
	/**
	 * Add a transaction to the database
	 * @param uri
	 * @param values
	 * @return
	 */
	private Uri insertTransaction(Uri uri, ContentValues values) {
		
		int id = uriMatcher.match(uri);
		long origin;
		int transactionType = Transaction.TRANSACTION_INVALID;

		switch (id) {
			// transaction table related queries
			case TRANSACTION_URI_ID:
				// update the origin id if necessary 
				origin = getAsLong(values, TRANSACTION_ORIGIN, 0);	// default origin is this device
				if ( (origin == 0) || !values.containsKey(TRANSACTION_ORIGIN_ID) ) {
					/* origin is this device or no origin id included,
					 * so update origin id field to next rowid for transaction table, 
					 * i.e. the id this insert will get */
					Cursor c = db.rawQuery("SELECT MAX(ROWID) FROM " + TRANSACTION_TABLE , null);
					if ( c.moveToFirst() )
						origin = c.getLong(0) + 1;
					
					if ( values.containsKey(TRANSACTION_ORIGIN_ID) )
						values.remove(TRANSACTION_ORIGIN_ID);
					values.put(TRANSACTION_ORIGIN_ID, origin);
				}
				
				// check the transaction type and update the source/destination account
				if ( values.containsKey(TRANSACTION_TYPE) ) {
					transactionType = getAsInteger(values, TRANSACTION_TYPE, Transaction.TRANSACTION_INVALID);
					switch ( Transaction.getBaseTransaction(transactionType) ) {
						case Transaction.TRANSACTION_CREDIT:
							// set source to be the same as destination just to pass database constraint
							if ( values.containsKey(TRANSACTION_DEST) )
								values.put(TRANSACTION_SRC, values.getAsInteger(TRANSACTION_DEST));
							break;
						case Transaction.TRANSACTION_DEBIT:
							// set destination to be the same as source just to pass database constraint
							if ( values.containsKey(TRANSACTION_SRC) )
								values.put(TRANSACTION_DEST, values.getAsInteger(TRANSACTION_SRC));
							break;
//						case Transaction.TRANSACTION_TRANSFER:
						default:
							break;
					}					
				}
				
				// fall thru
			case TRANSACTION_URI_ID_BY_ID:
				// check a category is assigned
				if ( !values.containsKey(TRANSACTION_CATEGORY) ) {
					// find unassigned category for the account currency & use it
					Cursor c = query(ACCOUNT_CURR_CATEGORY_URI, null, 
							SQLiteCommandFactory.makeFieldSelection(new String[] {
									ACCOUNT_TABLE,
									null,
									CATEGORY_TABLE,
									null,
							}, 
							new String[] {
									ACCOUNT_ID,
									Integer.toString(values.getAsInteger(TRANSACTION_SRC)),
									CATEGORY_FLAGS,
									Integer.toString(Category.UNASSIGNED_CATEGORY),
							}), 
							null, null);
					if ( c.moveToFirst() ) {
						int idx = c.getColumnIndexOrThrow(CATEGORY_ID);
						values.put(TRANSACTION_CATEGORY, c.getInt(idx));
					}
					c.close();
				}
				break;

			default:
				Logger.d(this.getClass().getName() + " unrecognised uri " + uri.toString() );
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		// Insert the new row, will return the row number if successful.
		long rowID = db.insert(TRANSACTION_TABLE, null, values);

		// Return a URI to the newly inserted row on success.
		if (rowID > 0) {
			
			updateAccountBalances(values);
			
			Uri returnUri = ContentUris.withAppendedId(TRANSACTION_URI, rowID);
			getContext().getContentResolver().notifyChange(returnUri, null);
			return returnUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}


	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		UriDecode decode = decodeUri(uri, null, null);

		if (decode == null) {
			Logger.d(this.getClass().getName() + " unrecognised uri " + uri.toString() );
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		switch (decode.id) {
			// transaction table related queries
			case TRANSACTION_URI_ID_BY_ID:
			case TRANSACTION_URI_ID:
				// inserting a transaction effects multiple tables
				return insertTransaction(uri, values);
		}
		
		// Insert the new row, will return the row number if successful.
		long rowID = db.insert(decode.table, null, values);

		// Return a URI to the newly inserted row on success.
		if (rowID > 0) { 
			Uri returnUri = ContentUris.withAppendedId(decode.contentUri, rowID);
			getContext().getContentResolver().notifyChange(returnUri, null);

			Logger.d(getClass().getName() + " added row " + rowID + " as [" + values.toString() + "]");

			return returnUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}



	@Override
	public boolean onCreate() {
		DatabaseManagerHelper dbHelper = new DatabaseManagerHelper(getContext(),
														DATABASE_NAME, null, DATABASE_VERSION);
		try {
			db = dbHelper.getWritableDatabase();
		}
		catch ( SQLiteException e ){
			db = dbHelper.getReadableDatabase();
		}
		return (db == null) ? false : true;
	}


	/**
	 * @return the dbState
	 */
	public static DatabaseState getDatabaseState() {
		return ctrlDatabaseState(CtrlDatabaseState.DB_STATE_GET, null);
	}

	/**
	 * @param dbState the dbState to set
	 */
	private static void setDatabaseState(DatabaseState dbState) {
		ctrlDatabaseState(CtrlDatabaseState.DB_STATE_SET, dbState);
	}

	enum CtrlDatabaseState { DB_STATE_GET, DB_STATE_SET }; 
	/**
	 * Control access to <code>dbState</code>
	 * @param action 
	 * @param dbState the dbState to set
	 */
	private static synchronized DatabaseState ctrlDatabaseState(CtrlDatabaseState action, DatabaseState dbState) {
		if ( action == CtrlDatabaseState.DB_STATE_SET )
			DatabaseManager.dbState = dbState;
		return DatabaseManager.dbState;
	}


	/**
	 * Create an SQL WHERE statement
	 * @param table	- table name; if null ignored
	 * @param field	- field name
	 * @param value	- required field value
	 * @return		formatted WHERE statement
	 */
	private String getWhereChunk( String table, String field, String value) {
		if ( !TextUtils.isEmpty(table) )
			return table + "." + field + "=" + value;
		else
			return field + "=" + value;
	}
	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		Cursor c;
		String[] tables = null;
		String[][] multiTables = null;
		String[] joinColumns = null;
		String[][] multiJoinColumns = null;
		String[][] columns = null;

		UriDecode decode = decodeUri(uri, selection, selectionArgs);

		if (decode == null) {
			Logger.d(this.getClass().getName() + " unrecognised uri " + uri.toString() );
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		switch (decode.id) {
			// multi-table related queries
			case ACCOUNT_SNAP_URI_ID_BY_ID:
			case ACCOUNT_BASIC_URI_ID_BY_ID:
			case ACCOUNT_ADDTRANS_URI_ID_BY_ID:
				qb.appendWhere( getWhereChunk( ACCOUNT_TABLE, ACCOUNT_ID, decode.selection) );
				// fall through to generate raw sql statement
			case ACCOUNT_SNAP_URI_ID:
			case ACCOUNT_BASIC_URI_ID:
			case ACCOUNT_ADDTRANS_URI_ID:
				tables = new String[] { ACCOUNT_TABLE, CURRENCY_TABLE	};
				joinColumns = new String[] { ACCOUNT_CURRENCY, CURRENCY_ID	};
				
				if ( projection == null ) {
					// no projection supplied so use default
					switch ( decode.id % BY_ID_OFFSET ) {
						case ACCOUNT_SNAP_URI_ID:
							columns = new String[][] {
									new String[] { ACCOUNT_ID, ACCOUNT_NAME, ACCOUNT_AVAILBAL	},
									new String[] { CURRENCY_CODE, CURRENCY_SYMBOL	},
							};
							break;
						case ACCOUNT_BASIC_URI_ID:
							columns = new String[][] {
									new String[] { ACCOUNT_ID, ACCOUNT_NAME, ACCOUNT_NICKNAME, ACCOUNT_CURRENTBAL, ACCOUNT_AVAILBAL, ACCOUNT_BANK	},
									new String[] { CURRENCY_CODE, CURRENCY_SYMBOL },
							};
							break;
						case ACCOUNT_ADDTRANS_URI_ID:
							columns = new String[][] {
									new String[] { ACCOUNT_ID, ACCOUNT_NAME, ACCOUNT_NICKNAME, 	},
									new String[] { CURRENCY_MINORUNITS, CURRENCY_CODE, CURRENCY_SYMBOL	},
							};
							break;
					}
					projection = SQLiteCommandFactory.getProjection(tables, columns);
				}
				qb.setTables( SQLiteCommandFactory.getInnerJoinCommand(tables, joinColumns) );
				break;
			case ACCOUNT_CURR_CATEGORY_URI_ID_BY_ID:
				qb.appendWhere( getWhereChunk( ACCOUNT_TABLE, ACCOUNT_ID, decode.selection) );
				// fall through to set table
			case ACCOUNT_CURR_CATEGORY_URI_ID:
				multiTables = new String[][] { 
						{ ACCOUNT_TABLE, CURRENCY_TABLE		},	// 1st join
						{ CURRENCY_TABLE, CATEGORY_TABLE	},	// 2nd join
				};
				multiJoinColumns = new String[][] { 
						{ ACCOUNT_CURRENCY, CURRENCY_ID	},		// 1st join
						{ CURRENCY_CODE, CATEGORY_NAME	},		// 2nd join
				};
				
				if ( projection == null ) {
					// no projection supplied so use default
					tables = new String[] { ACCOUNT_TABLE, CURRENCY_TABLE, CATEGORY_TABLE	};
					columns = new String[][] {
							new String[] { ACCOUNT_ID	},
							new String[] { CURRENCY_ID, CURRENCY_CODE	},
							new String[] { CATEGORY_ID, CATEGORY_NAME	},
					};
					projection = SQLiteCommandFactory.getProjection(tables, columns);
				}
				qb.setTables( SQLiteCommandFactory.getMultiJoinCommand(multiTables, multiJoinColumns) );
				break;

			case TRANSACTION_INFO_URI_ID_BY_ID:
				qb.appendWhere( getWhereChunk( TRANSACTION_TABLE, TRANSACTION_ID, decode.selection) );
				// fall through to generate raw sql statement
			case TRANSACTION_INFO_URI_ID:
				multiTables = new String[][] { 
						{ TRANSACTION_TABLE, PAYEE_TABLE	},	// 1st join
						{ TRANSACTION_TABLE, CATEGORY_TABLE	},	// 2nd join
				};
				multiJoinColumns = new String[][] { 
						{ TRANSACTION_PAYEE, PAYEE_ID	},		// 1st join
						{ TRANSACTION_CATEGORY, CATEGORY_ID	},	// 2nd join
				};
				
				if ( projection == null ) {
					// no projection supplied so use default
					tables = new String[] { TRANSACTION_TABLE, PAYEE_TABLE, CATEGORY_TABLE	};
					columns = new String[][] {
							null,		// all columns
							new String[] { PAYEE_NAME	},
							new String[] { CATEGORY_PATH, CATEGORY_NAME	},
					};
					projection = SQLiteCommandFactory.getProjection(tables, columns);
				}
				qb.setTables( SQLiteCommandFactory.getMultiJoinCommand(multiTables, multiJoinColumns) );
				break;

			default:
				// basic query
				if ( !TextUtils.isEmpty(decode.selection) && decode.selectionArgs == null ) {
					// have a selection but no args, could be from original uri or selection arg, add as where
					qb.appendWhere( decode.selection );
				}
				qb.setTables(decode.table);
				break;
		}
		
		// Apply the query to the underlying database.
		c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		// Register the contexts ContentResolver to be notified if the cursor result set changes.
		c.setNotificationUri(getContext().getContentResolver(), uri);

		// Return a cursor to the query result.
		return c;
	}


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		UriDecode decode = decodeUri(uri, selection, selectionArgs);

		if (decode == null) {
			Logger.d(this.getClass().getName() + " unrecognised uri " + uri.toString() );
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		switch (decode.id) {
			// transaction table related queries
			case TRANSACTION_URI_ID_BY_ID:
			case TRANSACTION_URI_ID:
				boolean hasAmt = values.containsKey(TRANSACTION_AMOUNT);
				boolean hasType = values.containsKey(TRANSACTION_TYPE);
				boolean hasSrc = values.containsKey(TRANSACTION_SRC);
				boolean hasDest = values.containsKey(TRANSACTION_DEST);
				if ( hasAmt || hasType || hasSrc || hasDest ) {
					/* need to check if the amount/type/src/dest in a parent transaction is being updated
					 * and if so update the balance */
					Cursor c = query(uri, new String[] { TRANSACTION_PARENT, TRANSACTION_TYPE, 
															TRANSACTION_AMOUNT, TRANSACTION_STATUS, 
															TRANSACTION_DEST, TRANSACTION_SRC }, 
												selection, selectionArgs, null);
					if ( c.moveToFirst() ) {
						int parentIdx = c.getColumnIndexOrThrow(TRANSACTION_PARENT);
						int typeIdx = c.getColumnIndexOrThrow(TRANSACTION_TYPE);
						int amtIdx = c.getColumnIndexOrThrow(TRANSACTION_AMOUNT);
						int statIdx = c.getColumnIndexOrThrow(TRANSACTION_STATUS);
						int destIdx = c.getColumnIndexOrThrow(TRANSACTION_DEST);
						int srcIdx = c.getColumnIndexOrThrow(TRANSACTION_SRC);
						do {
							long parentId = c.getLong(parentIdx);
							if ( parentId == TRANSACTION_PARENT_ID ) {
								boolean revOriginal = false;	// reverse original 
								boolean applyValues = false;	// apply values as supplied
								boolean done = false;

								int oldType = c.getInt(typeIdx);
								double oldAmt = c.getDouble(amtIdx);
								long oldSrc = c.getLong(srcIdx);
								long oldDest = c.getLong(destIdx);
								
								if ( hasType ) {
									// check if reversal of existing trans
									int newType = values.getAsInteger(TRANSACTION_TYPE);
									if ( Transaction.isValidTransactionReversal(newType) ) {
										// reversal info in values so just apply as new as NOTHING other than type should have changed
										applyValues = true;
										done = true;	// mark as done
									}
									else if ( Transaction.isValidTransaction(newType) && newType != oldType ) {
										// trans type has changed so need to reverse original and apply new
										revOriginal = applyValues = done = true;
									}
								}
								if ( !done && hasAmt) {
									// check amount adjustment
									double newAmt = values.getAsDouble(TRANSACTION_AMOUNT);
									if ( newAmt != oldAmt ) {
										// amount has changed so need to reverse original and apply new
										revOriginal = applyValues = done = true;
									}
								}
								if ( !done && (hasSrc || hasDest) ) {
									// check account changes
									long newSrc = (hasSrc ? values.getAsLong(TRANSACTION_SRC) : oldSrc);
									long newDest = (hasDest ? values.getAsLong(TRANSACTION_DEST) : oldDest);
									if ( newSrc != oldSrc || newDest != oldDest ) {
										// amount has changed so need to reverse original and apply new
										revOriginal = applyValues = done = true;
									}
								}

								if ( revOriginal ) {
									// reverse original transaction in account balances
									ContentValues update = new ContentValues();
									update.put(TRANSACTION_PARENT, parentId);
									update.put(TRANSACTION_TYPE, Transaction.getReversalFromTransaction(oldType));
									update.put(TRANSACTION_AMOUNT, oldAmt);
									update.put(TRANSACTION_STATUS, c.getInt(statIdx));
									update.put(TRANSACTION_DEST, oldDest);
									update.put(TRANSACTION_SRC, oldSrc);
									updateAccountBalances(update);
								}
								if ( applyValues ) {
									// apply new changes to account balances
									updateAccountBalances(values);
								}
							}
						}
						while ( c.moveToNext() );
					}
					c.close();
				}
				// else looks like it a non-balance effecting change so just apply it
				break;
		}

		int count = db.update(decode.table, values, decode.selection, decode.selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null); 
		return count;
	}
	
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#call(java.lang.String, java.lang.String, android.os.Bundle)
	 */
	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		
		Uri callUri = Uri.parse( method );
		int step = 0;
		int id = uriMatcher.match(callUri);
		if ( (id / BY_ID_OFFSET) > 0 )
			step = Integer.decode( callUri.getLastPathSegment() );
		Bundle result = null;

		switch (id) {
			case POPULATE_URI_ID:
				result = populateDefaults();
				break;
			case UPGRADE_URI_ID:
				result = upgrade();
				break;
			case POPULATE_STEP_URI_ID:
			case UPGRADE_STEP_URI_ID:
				doDatabaseOperation(step);
				break;
			case COUNT_ROWS_URI_ID:
				SQLiteStatement statement = db.compileStatement(SQLiteCommandFactory.getRowCount(arg));
			    long count = statement.simpleQueryForLong();
				result = new Bundle();
				result.putLong(arg, count);
				break;
			default:
				result = super.call(method, arg, extras);
				break;
		}
		return result; 
	}



	@Override
	public void shutdown() {
		if ( db.isOpen() )
			db.close();
	}

	
	/**
	 * Return <code>calendar</code> formatted as a time stamp as used by DatabaseManager 
	 * @param calendar	Calendar to format
	 * @return			Time stamp
	 */
	public static String makeDatabaseTimestamp(Calendar calendar) {
		
		return makeDatabaseTimestamp( calendar.getTime() );
	}
	
	/* see SimpleDateFormat formatters. Note: 'HH' has range 0~23 */ 
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String TIMESTAMP_FORMAT_SHORT = "yyyy-MM-dd";
	private static final char TIMESTAMP_DATE_CHAR = '-';
	private static final char TIMESTAMP_TIME_CHAR = ':';
	
	/**
	 * Return <code>date</code> formatted as a time stamp as used by DatabaseManager 
	 * @param date	Date to format
	 * @return		Time stamp
	 */
	public static String makeDatabaseTimestamp(Date date) {
		
		return DateTimeFormat.makeTimestamp(date, TIMESTAMP_FORMAT, "UTC", Locale.US);

//		
//		SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.US);
//
//		if ( date == null )
//			date = Calendar.getInstance().getTime();	// use current date/time
//
//		// get UTC offset for user timezone
//		Calendar cal = Calendar.getInstance();
//		int utcOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
//
//		// UTC time
//		date = new Date(date.getTime() + utcOffset);
//
//		String timestamp = sdf.format( date );
//		return timestamp;
	}

	
//	/**
//	 * Adapt calendar to client time zone.
//	 * @param calendar - adapting calendar
//	 * @param timeZone - client time zone
//	 * @return adapt calendar to client time zone
//	 */
//	public static Calendar convertCalendar(final Calendar calendar, final TimeZone timeZone) {
//	    Calendar ret = new GregorianCalendar(timeZone);
//	    ret.setTimeInMillis(calendar.getTimeInMillis() +
//	            timeZone.getOffset(calendar.getTimeInMillis()) -
//	            TimeZone.getDefault().getOffset(calendar.getTimeInMillis()));
//	    ret.getTime();
//	    return ret;
//	}
//	
	/**
	 * Parse <code>dateStr</code> returning a Date object representing the date & time 
	 * @param dateStr	String to parse
	 * @return			Date object
	 */
	public static Date parseDatabaseTimestamp(String dateStr) {
		
		return DateTimeFormat.parseTimestamp(dateStr, new String[] { TIMESTAMP_FORMAT_SHORT, TIMESTAMP_FORMAT },
												"UTC", Locale.US);
//		
//		SimpleDateFormat sdf;
//		Date date;
//
//		if ( dateStr.length() == TIMESTAMP_FORMAT_SHORT.length() )
//			sdf = new SimpleDateFormat(TIMESTAMP_FORMAT_SHORT, Locale.US);
//		else
//			sdf = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.US);
//		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//		try {
//			date = sdf.parse(dateStr);
//		} catch (ParseException e) {
//			e.printStackTrace();
//			date = null;
//		}
//		return date;
	}

	/**
	 * Return <code>amount</code> as an monetary estimate as used by DatabaseManager
	 * @param amount	Amount to format
	 * @return			Estimate string
	 */
	public static String makeDatabaseMonetaryEstimate(double amount) {
		
		StringBuffer sb = new StringBuffer(TRANSACTION_LIMIT_MONETARY);
		sb.append(amount);
		return sb.toString();
	}
	
	/**
	 * Return <code>amount</code> as an monetary estimate as used by DatabaseManager
	 * @param percent	Percentage to format
	 * @return			Estimate string
	 */
	public static String makeDatabasePercentEstimate(float percent) {
		
		StringBuffer sb = new StringBuffer(TRANSACTION_LIMIT_PERCENT);
		sb.append(percent);
		return sb.toString();
	}
	
	
	private enum CreationStepId {
		CREATE_DB,
		CREATE_DROPTABLES,
		CREATE_ADDTABLES, 
		CREATE_USERS, 
		CREATE_ACCTYPES, 
		CREATE_CURRENCIES, 
		CREATE_BANKS, 
		CREATE_TEXTTRANSTYPES, 
		CREATE_TRANSFERS, 
		CREATE_CATEGORIES, 
		CREATE_ACCOUNTS,
		CREATE_COMPLETE,
	}
	public static class CreationStep implements Parcelable {
		public String uri;
		public int resID;
		/**
		 * @param step
		 * @param resID
		 */
		public CreationStep(String uri, int resID) {
			super();
			this.uri = uri;
			this.resID = resID;
		}
		
		/* Parcelable interface - related functions */
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int writeflags) {
			dest.writeString(uri);
			dest.writeInt(resID);
		}
		
		public static final Parcelable.Creator<CreationStep> CREATOR = new Parcelable.Creator<CreationStep>() {
			public CreationStep createFromParcel(Parcel in) {
				return new CreationStep(in);
			}
			
			public CreationStep[] newArray(int size) {
				return new CreationStep[size];
			}
		};
		
		private CreationStep(Parcel in) {
			uri = in.readString();
			resID = in.readInt();
		}
	}
    
    
	private enum dbOperation { POPULATE_OP, UPGRATE_OP }; 
	/**
	 * Make a list of URI strings of the steps required to carry out the requested operation
	 * @param op	- database operation to carry out
	 * @return		Bundle containing a String[] of the steps
	 */
	private Bundle makeStepList(dbOperation op) {

		CreationStepId[] steps = CreationStepId.values();
		CreationStep[] stepStrs = new CreationStep[steps.length];
		String basePath = ( op == dbOperation.UPGRATE_OP ? UPGRADE_URI_PATH : POPULATE_URI_PATH );
		int idx = 0;
		for ( int i = 0; i < steps.length; ++i ) {
			boolean add = true;	// default always add
			
			if ( steps[i].equals(CreationStepId.CREATE_DROPTABLES) )
				add = (op == dbOperation.UPGRATE_OP);						// only drop tables for upgrade
			else if ( steps[i].equals(CreationStepId.CREATE_ACCOUNTS) )
				add = PreferenceControl.isCreateAccounts( getContext() );	// only create accounts if option enabled
			else if ( steps[i].equals(CreationStepId.CREATE_DB) )
				add = false;												// no need to add create step its already done
			// else everything else required

			if ( add ) {
				int resID;
				switch ( steps[i] ) {
					case CREATE_DB:				resID = R.string.create_database;			break;
					case CREATE_DROPTABLES:		resID = R.string.create_db_drop_tables;		break;
					case CREATE_ADDTABLES:		resID = R.string.create_db_add_tables;		break;
					case CREATE_USERS: 			resID = R.string.create_db_add_users;		break;
					case CREATE_ACCTYPES: 		resID = R.string.create_db_add_account_types;	break;
					case CREATE_CURRENCIES:		resID = R.string.create_db_add_currencies;	break;
					case CREATE_BANKS: 			resID = R.string.create_db_add_banks;		break;
					case CREATE_TEXTTRANSTYPES: resID = R.string.create_db_add_sms_types;	break;
					case CREATE_TRANSFERS: 		resID = R.string.create_db_add_transfers;	break;
					case CREATE_CATEGORIES: 	resID = R.string.create_db_add_categories;	break;
					case CREATE_ACCOUNTS:		resID = R.string.create_db_add_accounts;	break;
					default:					resID = R.string.please_wait;				break;
				}
				stepStrs[idx++] = new CreationStep(makeUriString(basePath, steps[i].ordinal()), resID);
			}
		}
		Bundle b = new Bundle();
		b.putParcelableArray(DB_CONTROL_STEPS, Arrays.copyOf(stepStrs, idx));
		return b;
	}
	
	
	/**
	 * Populate a new database with the default entries
	 */
	private Bundle populateDefaults()	{

		Logger.w("Populating database defaults");

		return makeStepList(dbOperation.POPULATE_OP);
	}

	
	/**
	 * Upgrade an old database
	 */
	public Bundle upgrade() { 
		Logger.w("Upgrading database; removing old data");

		return makeStepList(dbOperation.UPGRATE_OP);
	}
	
	
	private ArrayList<AccountCurrency> currenciesForInit = null;	// temporarily needed during database initialisation
	
	/**
	 * Populate a new database with the default entries
	 */
	private Bundle doDatabaseOperation(int step) {

		Bundle b = null;
		Resources r = getContext().getResources();

		if ( step == CreationStepId.CREATE_DROPTABLES.ordinal() ) {
			// remove old tables
			TableDefinition[] tables = dbTables.getTableDropList();
			final int N = tables.length;
			for ( int i = 0; i < N; ++i )
				db.execSQL(SQLiteCommandFactory.getDropTable(tables[i].tableName, FieldDefinition.IF_EXISTS));
		}
		else if ( step == CreationStepId.CREATE_ADDTABLES.ordinal() ) {
			// generate database tables
			TableDefinition[] tables = dbTables.getTableCreationList();
			final int N = tables.length;
			for ( int i = 0; i < N; ++i )
				db.execSQL(SQLiteCommandFactory.getCreateTable(tables[i].tableName, tables[i].columnsDef));
		}
		else if ( step == CreationStepId.CREATE_USERS.ordinal() ) {
			// add default users to database
			addDefaultUsers(db, r);
		}
		else if ( step == CreationStepId.CREATE_ACCTYPES.ordinal() ) {
			// add default account types to database
			addDefaultAccountTypes(db, r);
		}
		else if ( step == CreationStepId.CREATE_CURRENCIES.ordinal() ) {
			// add default currencies to database
			currenciesForInit = addDefaultCurrencies(db);
		}
		else if ( step == CreationStepId.CREATE_BANKS.ordinal() ) {
			// add default banks to database
			addDefaultBanks(db, r);
		}
		else if ( step == CreationStepId.CREATE_TEXTTRANSTYPES.ordinal() ) {
			// add default text transaction types to database
			addDefaultTextTransactionTypes(db, r);
		}
		else if ( step == CreationStepId.CREATE_TRANSFERS.ordinal() ) {
			// add default transfers to database
			addDefaultTransfers(db, r);
		}
		else if ( step == CreationStepId.CREATE_CATEGORIES.ordinal() ) {
			// add default categories to database
			addDefaultCategories(db, r, currenciesForInit);
			currenciesForInit = null;	// release object no longer required
		}
		else if ( step == CreationStepId.CREATE_ACCOUNTS.ordinal() ) {
			// add default accounts to database
			addDefaultAccounts(db, r);
		}
		else if ( step == CreationStepId.CREATE_COMPLETE.ordinal() ) {
			// all done
			setDatabaseState(DatabaseState.DATABASE_OK);	// db populated
		}
		
		return b;
	}

	
	
	/**
	 * Add default users to database, (there's only one yourself)
	 * @param db	Database
	 * @param r		Resources
	 */
	private void addDefaultUsers(SQLiteDatabase db, Resources r) {

		ContentValues values = new ContentValues();
		/* since there is no reliable method of retrieving your phone number (due to number porting etc.)
		 * the default user's phone number is zero, we'll always know other user's phone numbers as it'll come in
		 * on the SMS. */
		values.put(USER_PHONE, User.DEFAULT_PHONE_NUMBER);
		values.put(USER_NAME, "me");
		values.put(USER_PASS, "pass");
		values.put(USER_CHALLENGE, "hey what's up?");
		values.put(USER_RESPONSE, "nothing");
		if ( db.insert(USER_TABLE, null, values) == -1 )
			Logger.d(this.getClass().getName() + " unable to insert " + values.toString());
	}
	
	
	/**
	 * Add default account types to database
	 * @param db	Database
	 * @param r		Resources
	 */
	private void addDefaultAccountTypes(SQLiteDatabase db, Resources r) {

		String accTypes[] = r.getStringArray(R.array.account_types);
		KeyValueSplitter splitter = new KeyValueSplitter(null, DEFAULT_FIELD_MARKER, DEFAULT_KEYVAL_MARKER, DEFAULT_UNIFIER_MARKER, null );
		for ( int i = 0; i < accTypes.length; ++i) {

			// get column values and add a new row to the table 
			// e.g. "name=Current;overdraft=yes"

			ContentValues values = new ContentValues();
			int limit = AccountType.LIMIT_NONE;
			
			// split fields on ';', key/value pairs on '='
			splitter.setSource(accTypes[i]);
			LinkedHashMap<String, String> map = splitter.splitString();
			
			if ( map.size() > 0 ) {

				if ( map.containsKey(LIMIT_NAME_KEY) )
					values.put(ACCTYPE_NAME, map.get(LIMIT_NAME_KEY));
				if ( map.containsKey(LIMIT_CREDIT_KEY) ) {
					String value = map.get(LIMIT_CREDIT_KEY);
					if ( value.compareToIgnoreCase(YES_VALUE) == 0 )
						limit = AccountType.LIMIT_CREDIT;
					// else default to no credit limit
				}
				if ( map.containsKey(LIMIT_OVERDRAFT_KEY) ) {
					String value = map.get(LIMIT_OVERDRAFT_KEY);
					if ( value.compareToIgnoreCase(YES_VALUE) == 0 )
						limit = AccountType.LIMIT_OVERDRAFT;
					// else default to no overdraft limit
				}
			}

			if ( values.containsKey(ACCTYPE_NAME) ) {
				values.put(ACCTYPE_LIMIT, limit);
				if ( db.insert(ACCTYPE_TABLE, null, values) == -1 )
					Logger.d(this.getClass().getName() + " unable to insert " + values.toString());
			}
			else
				Logger.d(this.getClass().getName() + " malformed default account type " + accTypes[i]);
		}
	}
	
	
	/**
	 * Add default banks to database
	 * @param db	Database
	 * @param r		Resources
	 */
	private void addDefaultBanks(SQLiteDatabase db, Resources r) {
		String banks[] = r.getStringArray(R.array.banks);
		KeyValueSplitter splitter = new KeyValueSplitter(null, DEFAULT_FIELD_MARKER, DEFAULT_KEYVAL_MARKER, DEFAULT_UNIFIER_MARKER, null );
		final String[][] keyField = new String[][] {
				{ BANK_NAME_KEY, 		BANK_NAME },	
				{ BANK_ADDR_KEY, 		BANK_ADDR },	
				{ BANK_CSLOCAL_KEY,		BANK_LOCAL_SERVICE_NUM },	
				{ BANK_CSAWAY_KEY, 		BANK_AWAY_SERVICE_NUM },	
				{ BANK_PHONE_KEY, 		BANK_PHONE_BANK_NUM },
				{ BANK_PHONE_AWAY_KEY,	BANK_AWAY_PHONE_BANK_NUM },
				{ BANK_TEXT_KEY,		BANK_TEXT_BANK_NUM },	
		};
		final int keyCol = 0;
		final int fieldCol = 1;
		for ( int i = 0; i < banks.length; ++i) {
			ContentValues values = new ContentValues();

			// get column values and add a new row to the table 
			// split fields on ';', key/value pairs on '='
			splitter.setSource(banks[i]);
			LinkedHashMap<String, String> map = splitter.splitString();
			
			if ( map.size() > 0 ) {

				// determine the locale for the bank
				if ( map.containsKey(BANK_LOCALE_KEY) ) {
					String loc = map.get(BANK_LOCALE_KEY);
					if ( loc.length() >= 5 ) {
						/* ISO 3166 alpha-2 codes are 2 chars & locales "ll_cc", where l=language and c=country */
						Locale l = new Locale(loc.substring(0, 2), loc.substring(3, 5));
						values.put(BANK_COUNTRY, l.getDisplayCountry());
					}
				}

				// get all the string fields
				for ( int j = 0; j < keyField.length; ++j ) {
					if ( map.containsKey(keyField[j][keyCol]) )
						values.put(keyField[j][fieldCol], map.get(keyField[j][keyCol]));
				}
			}

			if ( values.containsKey(BANK_NAME) ) {
				if ( db.insert(BANK_TABLE, null, values) == -1 ) {
					Logger.d(this.getClass().getName() + " unable to insert " + values.toString());
				}
				else {
					if ( map.containsKey(BANK_DEFAULT_KEY) ) {
						String value = map.get(BANK_DEFAULT_KEY);
						if ( value.compareToIgnoreCase(YES_VALUE) == 0 ) {
							// query database for bank id
							String[] columns = new String[] { BANK_ID };
							String selection = BANK_NAME + " = \"" + map.get(BANK_NAME_KEY) + "\"";
							Cursor c = db.query(BANK_TABLE, 
									columns, selection, null, null, null, null);
							if (c.moveToFirst()) {
								int idIdx = c.getColumnIndex(columns[0]);
								defaultBankId = c.getInt(idIdx);
							}
							c.close();
						}
					}
				}
			}
			else
				Logger.d(this.getClass().getName() + " malformed default bank " + banks[i]);
		}
	}
	

	/**
	 * Add default currencies to database
	 * @param db	Database
	 */
	private ArrayList<AccountCurrency> addDefaultCurrencies(SQLiteDatabase db) {

		ArrayList<AccountCurrency> currencies = getCurrencyList();
		Iterator<AccountCurrency> iterator = currencies.iterator();
		while ( iterator.hasNext() ) {
			AccountCurrency currency = (AccountCurrency)iterator.next();
			ContentValues values = new ContentValues();
			
			// get column values and add a new row to the table 
			values.put(CURRENCY_ID, currency.getNumber());
			values.put(CURRENCY_NAME, currency.getName());
			values.put(CURRENCY_CODE, currency.getCode());
			values.put(CURRENCY_MINORUNITS, currency.getMinorUnits());
			values.put(CURRENCY_SYMBOL, currency.getSymbol());
			if ( db.insert(CURRENCY_TABLE, null, values) == -1 )
				Logger.d(this.getClass().getName() + " unable to insert " + values.toString());
		}
		return currencies;
	}
	
	/**
	 * Add default transfers to database
	 * @param db	Database
	 * @param r		Resources
	 */
	private void addDefaultTransfers(SQLiteDatabase db, Resources r) {
		String transfers[] = r.getStringArray(R.array.transfers);
		for ( int i = 0; i < transfers.length; ++i) {
			ContentValues values = new ContentValues();

			// get column values and add a new row to the table 
			values.put(TRANSFER_NAME, transfers[i]);
			if ( db.insert(TRANSFER_TABLE, null, values) == -1 )
				Logger.d(this.getClass().getName() + " unable to insert " + values.toString());
		}
	}
	
	/**
	 * Add default text banking transaction types to database
	 * @param db	Database
	 * @param r		Resources
	 */
	private void addDefaultTextTransactionTypes(SQLiteDatabase db, Resources r) {
		String transfers[] = r.getStringArray(R.array.text_bank_trans_types);
		for ( int i = 0; i < transfers.length; ++i) {
			ContentValues values = new ContentValues();

			// get column values and add a new row to the table 
			values.put(TEXTTYPES_NAME, transfers[i]);
			if ( db.insert(TEXTTYPES_TABLE, null, values) == -1 )
				Logger.d(this.getClass().getName() + " unable to insert " + values.toString());
		}
	}
	
	/**
	 * Add default transaction categories to database. Any missing higher level categories will also be added.
	 * @param db			Database
	 * @param r				Resources
	 * @param currencies	List of currencies
	 */
	private void addDefaultCategories(SQLiteDatabase db, Resources r, ArrayList<AccountCurrency> currencies) {

		String categories[] = r.getStringArray(R.array.category_types);
		ArrayList<Category> list = new ArrayList<Category>();
		int i, j, k;

		/* could use Uri to do this but it would probably work out pretty much as complicated */
		
		for ( i = 0; i < categories.length; ++i) {
			
			String[] branch = Category.splitPath(categories[i]);	// split path of this item
			int flags = Category.NO_FLAGS_CATEGORY;

			// check for special items
			if ( branch.length > 1 && branch[0].startsWith(DB_DEFAULT_PREFIX)) {
				if ( branch[0].compareToIgnoreCase(DB_DEFAULT_UNASSIGNED_PREFIX) == 0 )
					flags = Category.UNASSIGNED_CATEGORY_ROOT;
				else if ( branch[0].compareToIgnoreCase(DB_DEFAULT_TRANSFER_PREFIX) == 0 )
					flags = Category.TRANSFER_CATEGORY_ROOT;
				else if ( branch[0].compareToIgnoreCase(DB_DEFAULT_INCOME_PREFIX) == 0 )
					flags = Category.INCOME_CATEGORY_ROOT;
				else if ( branch[0].compareToIgnoreCase(DB_DEFAULT_EXPENSE_PREFIX) == 0 )
					flags = Category.EXPENSE_CATEGORY_ROOT;
				else if ( branch[0].compareToIgnoreCase(DB_DEFAULT_IMBALANCE_PREFIX) == 0 )
					flags = Category.IMBALANCE_CATEGORY_ROOT;
				else if ( branch[0].compareToIgnoreCase(DB_DEFAULT_SPLIT_PREFIX) == 0 )
					flags = Category.SPLIT_CATEGORY_ROOT;

				if ( flags != Category.NO_FLAGS_CATEGORY )
					branch = Arrays.copyOfRange(branch,1,branch.length);	// drop the special marker
			}

			// for each element of the path
			final int B = branch.length;
			for ( j = 0; j < B; ++j ) {
				Category item = new Category(branch[j], (Category.ROOT_LEVEL_CATEGORY + j), 
						Category.makePath(branch, j), flags);	// item with correct name, level, path & flags (if a root item)
				
				// check if item is already in list
				final int N = list.size();
				for ( k = 0; k < N; ++k ) {
					if ( item.isSameLevelNameAndPath(list.get(k)) )
						break;
				}
				if ( k >= N ) {
					// need to add to list
					list.add(item);
				}
				}
			
			// add the individual categories for root categories that have individual categories for each currency
			switch ( flags ) {
				case Category.IMBALANCE_CATEGORY_ROOT:
				case Category.UNASSIGNED_CATEGORY_ROOT: {
					String[] newBranches = Arrays.copyOfRange(branch,0,branch.length + 1);	// add space for currency code
					final int Z = newBranches.length - 1;

					flags = Category.getTypeFlag(flags);

					for ( AccountCurrency currency: currencies ) {
						newBranches[Z] = currency.getCode();
						Category item = new Category(newBranches[Z], (Category.ROOT_LEVEL_CATEGORY + j), 
								Category.makePath(newBranches, j), flags);	// item with correct name, level, path & flags (if a root item)
						list.add(item);
					}
					break;
				}
				default:
					break;
			}
		}

		// add to the database
		if ( list.size() > 0 ) {
			// sort list into ascending order
			Collections.sort(list, new Category.CompareLevelPathName());
			Category[] array = (Category[]) list.toArray(new Category[list.size()]);
			
			/* have an array of 
			 * "a"
			 * "a.b"
			 * "a.b.c"
			 * "b"
			 * etc.
			 */
			
			final int N = array.length;
			for ( i = 0; i < N; ++i ) {
				ContentValues values = new ContentValues();

				// add category name 
				values.put(CATEGORY_NAME, array[i].getName());
				
				// find parent category
				Category parentCategory = null;
				if ( array[i].getLevel() != Category.ROOT_LEVEL_CATEGORY ) {
					// find parent path of the item
					String path = array[i].getPath(); 
					// search previous items to see if parent is there
					for ( j = 0; j < i; ++j ) {
						if ( path.compareTo(array[j].getAbsolutePath()) == 0 ) {
							parentCategory = array[j];
							break;
						}
					}
				}
				if ( parentCategory != null ) {
					array[i].setParent( (int) parentCategory.getId() );		// update the item with it's parent
					array[i].setPath( parentCategory.getAbsolutePath() );	// update the item with it's parent path
					array[i].setFlags( parentCategory.getTypeFlag() );	// update the item flags
				}

				// add category parent 
				values.put(CATEGORY_PARENT, array[i].getParent());

				// add parent path 
				values.put(CATEGORY_PATH, array[i].getPath());

				// add category flags
				values.put(CATEGORY_FLAGS, array[i].getFlags());
				
				// add a new row to the table 
				long id = db.insert(CATEGORY_TABLE, null, values);
				if ( id == -1 )
					Logger.d(this.getClass().getName() + " unable to insert " + values.toString());
				else {
					// update the item with it's id
					array[i].setId((int) id);
				}
			}
		}
	}
	
	
	
	private void setAccountBalances(ContentValues values) {
		
	}
	

	/**
	 * Add default accounts to database
	 * @param db			Database
	 * @param r				Resources
	 */
	private void addDefaultAccounts(SQLiteDatabase db, Resources r) {

		String accounts[] = r.getStringArray(R.array.test_accounts);
		KeyValueSplitter splitter = new KeyValueSplitter(null, DEFAULT_FIELD_MARKER, DEFAULT_KEYVAL_MARKER, DEFAULT_UNIFIER_MARKER, null );

		// values that may be put directly into database
		final String[][] putDirect = new String[][] {
				// key						table column
				{ ACCOUNT_NAME_KEY, 		ACCOUNT_NAME },
				{ ACCOUNT_NICKNAME_KEY,		ACCOUNT_NICKNAME },
				{ ACCOUNT_NUMBER_KEY,		ACCOUNT_NUMBER },
		};
		final int directN = putDirect.length;
		// values that need to be parsed as doubles to ensure correct
		final String[][] putDouble = new String[][] {
				// key						table column
				{ ACCOUNT_INITBAL_KEY,		ACCOUNT_INITBAL },
				{ ACCOUNT_CURRENTBAL_KEY,	ACCOUNT_CURRENTBAL },
				{ ACCOUNT_LIMIT_KEY,		ACCOUNT_LIMIT },
		};
		final int initBalIdx = 0;
		final int currentBalIdx = 1;
		final int limitIdx = 2;
		final int doublesN = putDouble.length;
		double[] amounts = new double[doublesN];

		for ( int i = 0; i < accounts.length; ++i) {

			// get column values and add a new row to the table 
			// e.g. "name=Cash;nickname=cash;type=Cash"

			ContentValues values = new ContentValues();

			// split fields on ';', key/value pairs on '='
			splitter.setSource(accounts[i]);
			LinkedHashMap<String, String> map = splitter.splitString();

			if ( map.size() > 0 ) {

				// values that may be put directly into database
				for ( int j = 0; j < directN; ++j ) {
					if ( map.containsKey(putDirect[j][0]) )
						values.put(putDirect[j][1], map.get(putDirect[j][0]));
				}
				
				// account type
				int accountLimit = AccountType.LIMIT_NONE;	// type of limit on the account
				if ( map.containsKey(ACCOUNT_TYPE_KEY) ) {
					// query database for account type id
					String[] columns = new String[] { ACCTYPE_ID, ACCTYPE_LIMIT };
					String selection = ACCTYPE_NAME + " = \"" + map.get(ACCOUNT_TYPE_KEY) + "\"";
					Cursor c = db.query(ACCTYPE_TABLE, 
							columns, selection, null, null, null, null);
					if (c.moveToFirst()) {
						int idx = c.getColumnIndex(columns[0]);
						values.put(ACCOUNT_TYPE, c.getInt(idx));

						idx = c.getColumnIndex(columns[1]);
						accountLimit = c.getInt(idx);
					}
					c.close();
				}
				else {
					Logger.d(this.getClass().getName() + " malformed default account; missing " + ACCOUNT_TYPE_KEY + " " + map.values().toString());
					continue;
				}
				
				// account currency
				if ( map.containsKey(ACCOUNT_CURRENCY_KEY) ) {
					// query database for currency id
					String[] columns = new String[] { CURRENCY_ID };
					String selection = CURRENCY_CODE + " = \"" + map.get(ACCOUNT_CURRENCY_KEY) + "\"";
					Cursor c = db.query(CURRENCY_TABLE, columns, selection, null, null, null, null);
					if (c.moveToFirst()) {
						int idIdx = c.getColumnIndex(columns[0]);
						values.put(ACCOUNT_CURRENCY, c.getInt(idIdx));
					}
					c.close();
				}
				else {
					Logger.d(this.getClass().getName() + " malformed default account; missing " + ACCOUNT_CURRENCY_KEY + " " + map.values().toString());
					continue;
				}
				
				// bank
				if ( map.containsKey(ACCOUNT_BANK_KEY) ) {
					// query database for account type id
					String[] columns = new String[] { BANK_ID };
					String selection = BANK_NAME + " = \"" + map.get(ACCOUNT_BANK_KEY) + "\"";
					Cursor c = db.query(BANK_TABLE, columns, selection, null, null, null, null);
					if (c.moveToFirst()) {
						int idIdx = c.getColumnIndex(columns[0]);
						values.put(ACCOUNT_BANK, c.getInt(idIdx));
					}
					c.close();
				}
				else {
					values.put(ACCOUNT_BANK, defaultBankId);
				}

				// date
				if ( map.containsKey(ACCOUNT_DATE_KEY) ) {
					String dateStr = map.get(ACCOUNT_DATE_KEY);
					dateStr = dateStr.replace('.', TIMESTAMP_DATE_CHAR);
					Date date = parseDatabaseTimestamp(dateStr);
					values.put(ACCOUNT_DATE, DatabaseManager.makeDatabaseTimestamp(date));
				}

				// values that need to be parsed as doubles to ensure correct
				for ( int j = 0; j < doublesN; ++j ) {
					amounts[j] = 0;
					if ( map.containsKey(putDouble[j][0]) ) {
						String value = map.get(putDouble[j][0]);

						if ( !TextUtils.isEmpty(value) ) {
							try {
								// try to parse the entered text
								amounts[j] = Double.parseDouble(value);
								values.put(putDouble[j][1], amounts[j]);
							}
							catch ( NumberFormatException e ) {
								Logger.d(this.getClass().getName() + " malformed default account; " + putDouble[j][0] + " " + map.values().toString());
							}
						}
					}
				}

				// add additional required fields
				boolean initBal = values.containsKey(ACCOUNT_INITBAL);
				boolean curBal = values.containsKey(ACCOUNT_CURRENTBAL);
				if ( initBal || curBal ) {
					if ( initBal && !curBal ) {
						// set current to init
						amounts[currentBalIdx] = amounts[initBalIdx];
						values.put(ACCOUNT_CURRENTBAL, amounts[currentBalIdx]);
						curBal = true;
					}
					if ( curBal ) {
						if ( !initBal ) {
							// set init to current
							amounts[initBalIdx] = amounts[currentBalIdx];
							values.put(ACCOUNT_INITBAL, amounts[initBalIdx]);
						}
						
						// set available
						double availBal = 0;
						switch ( accountLimit )  {
							case AccountType.LIMIT_CREDIT:
								// available is credit limit less current bal
								availBal = amounts[limitIdx] - amounts[currentBalIdx];
								break;
//							case AccountType.LIMIT_OVERDRAFT:
//							case AccountType.LIMIT_NONE:
							default:
								// available is current
								availBal = amounts[currentBalIdx];
								break;
						}
						values.put(ACCOUNT_AVAILBAL, availBal);
					}
				}

				if ( db.insert(ACCOUNT_TABLE, null, values) == -1 )
					Logger.d(this.getClass().getName() + " unable to insert " + values.toString());

			}
		}
	}

	/**
	 * Get a list of the ISO 4217 currencies from the locale's available on the device
	 * @return
	 */
	public ArrayList<AccountCurrency> getCurrencyList() {
		
		ArrayList<AccountCurrency> currencies = new ArrayList<AccountCurrency>();
		AccountCurrency currency;
	
		Locale[] locales = Locale.getAvailableLocales();

		for ( int i = 0; i < locales.length; ++i ) {
			DecimalFormatSymbols format = new DecimalFormatSymbols(locales[i]);
			Currency sysCurrency = format.getCurrency();
			String code = sysCurrency.getCurrencyCode();
			
			// only add valid symbols
			if ( !TextUtils.isEmpty(code) ) {
				if  ( code.compareToIgnoreCase(AccountCurrency.NOT_A_CURRENCY_CODE) != 0 ) {
					
					code = code.toUpperCase(Locale.US);
					currency = new AccountCurrency(
							AccountCurrency.getCurrencyName(code), 
							code, 
							code.hashCode(),
							sysCurrency.getDefaultFractionDigits(), 
							sysCurrency.getSymbol());

					if ( !currencies.contains(currency) ) {
						currencies.add(currency);

						Logger.d(currency.toString());
					}
				}
			}
		}

		return currencies;
	}

	
	/**
	 * Helper class for opening, creating, and managing database version control.
	 * Due to the time taken to populate default entries the work is actually done form an AsyncTask, only 
	 * the database state is currently set in this class.
	 * @author Ian Buttimer
	 *
	 */
	private static class DatabaseManagerHelper extends SQLiteOpenHelper {
		
		public DatabaseManagerHelper(Context context, String name, CursorFactory factory, int version) { 
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db)	{

			Logger.w("Creating new database version " + DATABASE_VERSION);

			setDatabaseState(DatabaseState.DATABASE_EXISTS);	// empty db exists
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
			Logger.w("Upgrading database from version " + oldVersion + " to "
							+ newVersion + ", which will destroy all old data");

			setDatabaseState(DatabaseState.DATABASE_NEEDS_UPGRADE);	// old db exists
		}
	}

}
