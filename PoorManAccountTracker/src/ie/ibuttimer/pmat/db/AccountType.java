/**
 * 
 */
package ie.ibuttimer.pmat.db;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.Cursor;
import ie.ibuttimer.widget.TextViewAdapterBase;
import ie.ibuttimer.widget.TextViewAdapterInterface;

/**
 * Class to represent account types as they are stored in the database.
 * @author Ian Buttimer
 *
 */
public class AccountType extends TextViewAdapterBase implements TextViewAdapterInterface {

	/* fields stored in super class:
	 * - account type id
	 * - account type name
	 *  */
	private int limit;				// credit/overdraft limit applies

	public static final int LIMIT_NONE = 0;			// no limit applies to the account
	public static final int LIMIT_CREDIT = 1;		// credit limit applies to the account
	public static final int LIMIT_OVERDRAFT = 2;	// overdraft limit applies to the account

	
	/**
	 * Constructor
	 * @param id	- Id to represent this account type
	 * @param name	- Name of account type
	 * @param limit	- type of limit that applies to this account type; one of LIMIT_NONE, LIMIT_CREDIT or LIMIT_OVERDRAFT  
	 */
	public AccountType(int id, String name, int limit) {
		super(id, name);
		this.limit = limit;
	}

	/**
	 * Constructor
	 * @param id	Id 
	 * @param name	Display name
	 */
	public AccountType(int id, String name) {
		this(id,name,LIMIT_NONE);
	}
	
	/**
	 * Default constructor
	 */
	public AccountType() {
		this(0, "");
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	
	/**
	 * Retrieve all the account types in the database
	 * @param cr	- Content Resolver to use
	 * @return		an ArrayList of AccountType objects
	 */
	public static ArrayList<AccountType> loadAccountTypesFromProvider(ContentResolver cr) {
		ArrayList<AccountType> accTypes = new ArrayList<AccountType>();
		Cursor c = cr.query(DatabaseManager.ACCTYPE_URI, null, null, null, null);	// Return all the database account types
		if (c.moveToFirst()) {
			int idIdx = c.getColumnIndex(DatabaseManager.ACCTYPE_ID);
			int nameIdx = c.getColumnIndex(DatabaseManager.ACCTYPE_NAME);
			int limitIdx = c.getColumnIndex(DatabaseManager.ACCTYPE_LIMIT);
			do {
				// Extract the details.
				int id = c.getInt(idIdx);
				String name = c.getString(nameIdx);
				int limit = c.getInt(limitIdx);

				accTypes.add( new AccountType(id, name, limit) );
			} while(c.moveToNext());
		}
		c.close();
		return accTypes;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [limit=" + limit + ", toString()="
				+ super.toString() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + limit;
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
		if (!(obj instanceof AccountType))
			return false;
		AccountType other = (AccountType) obj;
		if (limit != other.limit)
			return false;
		return true;
	}
	
}
