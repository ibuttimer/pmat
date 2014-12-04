/**
 * 
 */
package ie.ibuttimer.pmat.db;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import ie.ibuttimer.widget.TextViewAdapterBase;
import ie.ibuttimer.widget.TextViewAdapterInterface;

/**
 * Class to represent a Payee as stored in the database
 * @author Ian Buttimer
 */
public class Payee extends TextViewAdapterBase implements TextViewAdapterInterface, Parcelable {
	
	/**
	 * Constructor
	 * @param id	- ID to represent the payee
	 * @param name	- Payee name
	 */
	public Payee(int id, String name) {
		super(id, name );
	}

	/**
	 * Constructor
	 * @param name	- Payee name
	 */
	public Payee(String name) {
		this(0, name);
	}
	
	
	/**
	 * Retrieve all the payees in the database
	 * @param cr			- Content Resolver to use
	 * @param selection		- A filter declaring which rows to return
	 * @param selectionArgs	- Values to replace ? in <code>selection</code>
	 * @return		an ArrayList of Payee objects
	 */
	private static ArrayList<Payee> loadPayeesFromProvider(ContentResolver cr, String selection, String[] selectionArgs) {
		ArrayList<Payee> payees = new ArrayList<Payee>();
		Cursor c = cr.query(DatabaseManager.PAYEE_URI, null, selection, selectionArgs, null);
		if (c.moveToFirst()) {
			int idIdx = c.getColumnIndex(DatabaseManager.PAYEE_ID);
			int nameIdx = c.getColumnIndex(DatabaseManager.PAYEE_NAME);
			do {
				payees.add( new Payee(c.getInt(idIdx), c.getString(nameIdx)) );
			} while(c.moveToNext());
		}
		c.close();
		return payees;
	}

	
	/**
	 * Retrieve all the payees in the database
	 * @param cr	- Content Resolver to use
	 * @return		an ArrayList of Payee objects
	 */
	public static ArrayList<Payee> loadPayeesFromProvider(ContentResolver cr) {
		return loadPayeesFromProvider(cr, null, null);	// return all records
	}
	
	/**
	 * Retrieve the payee with the specified id from the database
	 * @param cr	- Content Resolver to use
	 * @payeeId		- payee id
	 * @return		a Payee object
	 */
	public static Payee loadFromProvider(ContentResolver cr, long payeeId) {
		String selection = "(" + DatabaseManager.PAYEE_ID + "=?)";
		String[] selectionArgs = new String[] {	Long.toString(payeeId) };
		ArrayList<Payee> payees = loadPayeesFromProvider(cr, selection, selectionArgs);	// return all records
		if ( payees.size() == 1 )
			return payees.get(0);
		else 
			return null;
	}
	
	/* Implement Parcelable interface */
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(getId());
		dest.writeString(getName());
		dest.writeString(getPrefix());
	}
	
	public static final Parcelable.Creator<Payee> CREATOR = new Parcelable.Creator<Payee>() {
		public Payee createFromParcel(Parcel in) {
			return new Payee(in);
		}
		
		public Payee[] newArray(int size) {
			return new Payee[size];
		}
	};

	protected Payee(Parcel in) {
		setId(in.readLong());
		setName(in.readString());
		setPrefix(in.readString());
	}

}
