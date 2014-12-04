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
 * Class to represent banks as they are stored in the database.
 * @author Ian Buttimer
 *
 */
public class Bank extends TextViewAdapterBase implements TextViewAdapterInterface, Parcelable, Cloneable {

	/* fields stored in super class:
	 * - bank id
	 * - bank name
	 *  */
	private String address;			// address
	private String country;			// country
	private String localServiceNum;	// local customer service number
	private String awayServiceNum;	// abroad customer service number
	private String phoneBankNum;	// phone banking number
	private String textBankNum;		// text banking number
	
	// convenience variables
	private static int banksUriIdIdx = -1;
	private static int banksUriNameIdx = -1;
	private static int banksUriAddrIdx = -1;
	private static int banksUriCountryIdx = -1;
	private static int banksUriLocalIdx = -1;
	private static int banksUriAwayIdx = -1;
	private static int banksUriPhoneIdx = -1;
	private static int banksUriTextIdx = -1;


	/**
	 * @param id
	 * @param text
	 */
	public Bank(long id, String text) {
		super(id, text);
	}

	
	/**
	 * Retrieve all the banks in the database
	 * @param cr	- Content Resolver to use
	 * @return		an ArrayList of Bank objects
	 */
	public static ArrayList<Bank> loadBanksFromProvider(ContentResolver cr) {
		ArrayList<Bank> banks = new ArrayList<Bank>();
		Cursor c = cr.query(DatabaseManager.BANKS_URI, null, null, null, null);	// Return all the database banks
		if (c.moveToFirst()) {
			do {
				// Extract the details.
				Bank bank = getBankFromBanksUriCursor( c );
				if ( bank != null )	
					banks.add( bank );
			} while(c.moveToNext());
		}
		c.close();
		return banks;
	}

	/**
	 * Create a bank object representing the current row in the cursor.
	 * @param c	- Cursor containing row 
	 * @return	Bank object
	 */
	private static Bank getBankFromBanksUriCursor( Cursor c ) {
	
		if ( banksUriIdIdx < 0 ) {
			banksUriIdIdx = c.getColumnIndex(DatabaseManager.BANK_ID);
			banksUriNameIdx = c.getColumnIndex(DatabaseManager.BANK_NAME);
			banksUriAddrIdx = c.getColumnIndex(DatabaseManager.BANK_ADDR);
			banksUriCountryIdx = c.getColumnIndex(DatabaseManager.BANK_COUNTRY);
			banksUriLocalIdx = c.getColumnIndex(DatabaseManager.BANK_LOCAL_SERVICE_NUM);
			banksUriAwayIdx = c.getColumnIndex(DatabaseManager.BANK_AWAY_SERVICE_NUM);
			banksUriPhoneIdx = c.getColumnIndex(DatabaseManager.BANK_PHONE_BANK_NUM);
			banksUriTextIdx = c.getColumnIndex(DatabaseManager.BANK_TEXT_BANK_NUM);
		}

		// Extract the details.
		Bank bank = new Bank(c.getInt(banksUriIdIdx), c.getString(banksUriNameIdx));
		bank.address = c.getString(banksUriAddrIdx);
		bank.country = c.getString(banksUriCountryIdx);
		bank.localServiceNum = c.getString(banksUriLocalIdx);
		bank.awayServiceNum = c.getString(banksUriAwayIdx);
		bank.phoneBankNum = c.getString(banksUriPhoneIdx);
		bank.textBankNum = c.getString(banksUriTextIdx);

		return bank;
	}
	
	
	/**
	 * Retrieve the bank with the specified id from the database
	 * @param cr	- Content Resolver to use
	 * @param id	- Bank ID to search for
	 * @return		an ArrayList of Bank objects
	 */
	public static Bank loadBankFromProvider(ContentResolver cr, int id) {
		Bank bank = null;
		String selection = "(" + DatabaseManager.BANK_ID + "=?)";
		String[] selectionArgs = new String[] {	Integer.toString(id) };
		Cursor c = cr.query(DatabaseManager.BANKS_URI, null, selection, selectionArgs, null);	// Return the banks
		if (c.moveToFirst()) {
			// Extract the details.
			bank = getBankFromBanksUriCursor( c );
		}
		c.close();

		return bank;
	}


	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}


	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}


	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}


	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}


	/**
	 * @return the localServiceNum
	 */
	public String getLocalServiceNum() {
		return localServiceNum;
	}


	/**
	 * @param localServiceNum the localServiceNum to set
	 */
	public void setLocalServiceNum(String localServiceNum) {
		this.localServiceNum = localServiceNum;
	}


	/**
	 * @return the awayServiceNum
	 */
	public String getAwayServiceNum() {
		return awayServiceNum;
	}


	/**
	 * @param awayServiceNum the awayServiceNum to set
	 */
	public void setAwayServiceNum(String awayServiceNum) {
		this.awayServiceNum = awayServiceNum;
	}


	/**
	 * @return the phoneBankNum
	 */
	public String getPhoneBankNum() {
		return phoneBankNum;
	}


	/**
	 * @param phoneBankNum the phoneBankNum to set
	 */
	public void setPhoneBankNum(String phoneBankNum) {
		this.phoneBankNum = phoneBankNum;
	}


	/**
	 * @return the textBankNum
	 */
	public String getTextBankNum() {
		return textBankNum;
	}


	/**
	 * @param textBankNum the textBankNum to set
	 */
	public void setTextBankNum(String textBankNum) {
		this.textBankNum = textBankNum;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Bank [address=" + address + ", country=" + country
				+ ", localServiceNum=" + localServiceNum + ", awayServiceNum="
				+ awayServiceNum + ", phoneBankNum=" + phoneBankNum
				+ ", textBankNum=" + textBankNum + ", toString()="
				+ super.toString() + "]";
	}


	@Override
	public Object clone () {
		Bank bank = new Bank(getId(), getName());
		bank.address = address;
		bank.country = country;
		bank.localServiceNum = localServiceNum;
		bank.awayServiceNum = awayServiceNum;
		bank.phoneBankNum = phoneBankNum;
		bank.textBankNum = textBankNum;
		return bank;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result
				+ ((awayServiceNum == null) ? 0 : awayServiceNum.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result
				+ ((localServiceNum == null) ? 0 : localServiceNum.hashCode());
		result = prime * result
				+ ((phoneBankNum == null) ? 0 : phoneBankNum.hashCode());
		result = prime * result
				+ ((textBankNum == null) ? 0 : textBankNum.hashCode());
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
		if (!(obj instanceof Bank))
			return false;
		Bank other = (Bank) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (awayServiceNum == null) {
			if (other.awayServiceNum != null)
				return false;
		} else if (!awayServiceNum.equals(other.awayServiceNum))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (localServiceNum == null) {
			if (other.localServiceNum != null)
				return false;
		} else if (!localServiceNum.equals(other.localServiceNum))
			return false;
		if (phoneBankNum == null) {
			if (other.phoneBankNum != null)
				return false;
		} else if (!phoneBankNum.equals(other.phoneBankNum))
			return false;
		if (textBankNum == null) {
			if (other.textBankNum != null)
				return false;
		} else if (!textBankNum.equals(other.textBankNum))
			return false;
		return true;
	}


	/* Implement Parcelable interface */

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(address);			// address
		dest.writeString(country);			// country
		dest.writeString(localServiceNum);	// local customer service number
		dest.writeString(awayServiceNum);	// abroad customer service number
		dest.writeString(phoneBankNum);		// phone banking number
		dest.writeString(textBankNum);		// text banking number
	}
	
	public static final Parcelable.Creator<Bank> CREATOR = new Parcelable.Creator<Bank>() {
		public Bank createFromParcel(Parcel in) {
			return new Bank(in);
		}
		
		public Bank[] newArray(int size) {
			return new Bank[size];
		}
	};

	/**
	 * @param in
	 */
	protected Bank(Parcel in) {
		super(in);
		address = in.readString();			// address
		country = in.readString();			// country
		localServiceNum = in.readString();	// local customer service number
		awayServiceNum = in.readString();	// abroad customer service number
		phoneBankNum = in.readString();		// phone banking number
		textBankNum = in.readString();		// text banking number
	}
	
	
}
