/**
 * 
 */
package ie.ibuttimer.pmat.db;

import ie.ibuttimer.widget.TextViewAdapterBase;
import ie.ibuttimer.widget.TextViewAdapterInterface;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * Class to represent a transfer 
 * @author Ian Buttimer
 *
 */
public class Transfer extends TextViewAdapterBase implements TextViewAdapterInterface, Parcelable {

	/* fields stored in super class:
	 * - transfer id
	 * - transfer name
	 *  */
	private int delay;				// delay in hours
	private float amountFee;		// fixed fee amount
	private float minAmount;		// min fee amount
	private float maxAmount;		// max fee amount
	private float percentFee;		// fixed fee percent
	private float minPercent;		// min fee percent
	private float maxPercent;		// max fee percent
	
	/**
	 * Constructor
	 * @param id			ID
	 * @param name			Display name
	 * @param delay			Delay in hours
	 * @param amountFee		Fixed fee amount
	 * @param minAmount		Minimum fee amount
	 * @param maxAmount		Maximum fee amount
	 * @param percentFee	Fixed percent fee
	 * @param minPercent	Minimum fee percent
	 * @param maxPercent	Maximum fee percent
	 */
	public Transfer(int id, String name, int delay, float amountFee,
			float minAmount, float maxAmount, float percentFee,
			float minPercent, float maxPercent) {
		super(id, name);
		this.delay = delay;
		this.amountFee = amountFee;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.percentFee = percentFee;
		this.minPercent = minPercent;
		this.maxPercent = maxPercent;
	}
	
	/**
	 * Constructor
	 * @param id			ID
	 * @param name			Display name
	 * @param delay			Delay in hours
	 */
	public Transfer(int id, String name, int delay) {
		this(id,name,delay,0,0,0,0,0,0);
	}

	
	/**
	 * @return the delay
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	/**
	 * @return the amountFee
	 */
	public float getAmountFee() {
		return amountFee;
	}

	/**
	 * @param amountFee the amountFee to set
	 */
	public void setAmountFee(float amountFee) {
		this.amountFee = amountFee;
	}

	/**
	 * @return the minAmount
	 */
	public float getMinAmount() {
		return minAmount;
	}

	/**
	 * @param minAmount the minAmount to set
	 */
	public void setMinAmount(float minAmount) {
		this.minAmount = minAmount;
	}

	/**
	 * @return the maxAmount
	 */
	public float getMaxAmount() {
		return maxAmount;
	}

	/**
	 * @param maxAmount the maxAmount to set
	 */
	public void setMaxAmount(float maxAmount) {
		this.maxAmount = maxAmount;
	}

	/**
	 * @return the percentFee
	 */
	public float getPercentFee() {
		return percentFee;
	}

	/**
	 * @param percentFee the percentFee to set
	 */
	public void setPercentFee(float percentFee) {
		this.percentFee = percentFee;
	}

	/**
	 * @return the minPercent
	 */
	public float getMinPercent() {
		return minPercent;
	}

	/**
	 * @param minPercent the minPercent to set
	 */
	public void setMinPercent(float minPercent) {
		this.minPercent = minPercent;
	}

	/**
	 * @return the maxPercent
	 */
	public float getMaxPercent() {
		return maxPercent;
	}

	/**
	 * @param maxPercent the maxPercent to set
	 */
	public void setMaxPercent(float maxPercent) {
		this.maxPercent = maxPercent;
	}

	
	/**
	 * Retrieve all the transfers in the database
	 * @param cr			- Content Resolver to use
	 * @param selection		- A filter declaring which rows to return
	 * @param selectionArgs	- Values to replace ? in <code>selection</code>
	 * @return		an ArrayList of Transfer objects
	 */
	private static ArrayList<Transfer> loadTransfersFromProvider(ContentResolver cr, String selection, String[] selectionArgs) {
		ArrayList<Transfer> transfers = new ArrayList<Transfer>();
		Cursor c = cr.query(DatabaseManager.TRANSFER_URI, null, selection, selectionArgs, null);
		if (c.moveToFirst()) {
			int idIdx = c.getColumnIndex(DatabaseManager.TRANSFER_ID);
			int nameIdx = c.getColumnIndex(DatabaseManager.TRANSFER_NAME);
			int delayIdx = c.getColumnIndex(DatabaseManager.TRANSFER_DELAY);
			int amtFeeIdx = c.getColumnIndex(DatabaseManager.TRANSFER_AMTFEE);
			int minAmtIdx = c.getColumnIndex(DatabaseManager.TRANSFER_MINAMT);
			int maxAmtIdx = c.getColumnIndex(DatabaseManager.TRANSFER_MAXAMT);
			int percentFeeIdx = c.getColumnIndex(DatabaseManager.TRANSFER_PERCENTFEE);
			int minPercentIdx = c.getColumnIndex(DatabaseManager.TRANSFER_MINPERCENT);
			int maxPercentIdx = c.getColumnIndex(DatabaseManager.TRANSFER_MAXPERCENT);
			do {
				// Extract the details.
				Transfer transfer = new Transfer(c.getInt(idIdx),c.getString(nameIdx),c.getInt(delayIdx));
				transfer.amountFee = c.getFloat(amtFeeIdx);
				transfer.minAmount = c.getFloat(minAmtIdx);
				transfer.maxAmount = c.getFloat(maxAmtIdx);
				transfer.percentFee = c.getFloat(percentFeeIdx);
				transfer.minPercent = c.getFloat(minPercentIdx);
				transfer.maxPercent = c.getFloat(maxPercentIdx);

				transfers.add( transfer );
			} while(c.moveToNext());
		}
		c.close();
		return transfers;
	}

	
	/**
	 * Retrieve all the transfers in the database
	 * @param cr	- Content Resolver to use
	 * @return		an ArrayList of Transfer objects
	 */
	public static ArrayList<Transfer> loadTransfersFromProvider(ContentResolver cr) {
		return loadTransfersFromProvider(cr, null, null);	// return all records
	}

	
	/**
	 * Calculate the complete date for this transfer based on <code>sendDate</code>.
	 * @param sendDate	Date transfer executed
	 * @return			Date transfer completed
	 */
	public GregorianCalendar calcTransferCompleteDate( GregorianCalendar sendDate ) {
		
		GregorianCalendar competeDate = (GregorianCalendar) sendDate.clone();
		
		// TODO add working day/hours fields to transfers 
		
		// for now assume things only happen Monday-Friday
		for ( int hours = delay; hours > 0; ) {
			switch ( competeDate.get(GregorianCalendar.DAY_OF_WEEK) ) {
				case GregorianCalendar.SATURDAY:
				case GregorianCalendar.SUNDAY:
					// non-working day
					competeDate.roll(GregorianCalendar.DAY_OF_YEAR, 1);
					break;
				default:
					int decrement;
					if ( hours >= 24 ) {
						decrement = 24;
						competeDate.roll(GregorianCalendar.DAY_OF_YEAR, 1);
					}
					else {
						decrement = hours;
						competeDate.roll(GregorianCalendar.HOUR_OF_DAY, decrement);
					}
					hours -= decrement;
					break;
			}
		}
		return competeDate;
	}
	
	
	
	/* Parcelable interface - related functions */
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [delay=" + delay + ", amountFee=" + amountFee
				+ ", minAmount=" + minAmount + ", maxAmount=" + maxAmount
				+ ", percentFee=" + percentFee + ", minPercent=" + minPercent
				+ ", maxPercent=" + maxPercent + ", toString()=" + super.toString()
				+ "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int writeflags) {
		super.writeToParcel(dest, writeflags);
		dest.writeInt(delay);
		dest.writeFloat(amountFee);
		dest.writeFloat(minAmount);
		dest.writeFloat(maxAmount);
		dest.writeFloat(percentFee);
		dest.writeFloat(minPercent);
		dest.writeFloat(maxPercent);
	}
	
	public static final Parcelable.Creator<Transfer> CREATOR = new Parcelable.Creator<Transfer>() {
		public Transfer createFromParcel(Parcel in) {
			return new Transfer(in);
		}
		
		public Transfer[] newArray(int size) {
			return new Transfer[size];
		}
	};
	
	private Transfer(Parcel in) {
		super(in);
		delay = in.readInt();
		amountFee = in.readFloat();
		minAmount = in.readFloat();
		maxAmount = in.readFloat();
		percentFee = in.readFloat();
		minPercent = in.readFloat();
		maxPercent = in.readFloat();
	}

}


