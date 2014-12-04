/**
 * 
 */
package ie.ibuttimer.pmat.db;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import ie.ibuttimer.widget.TextViewAdapterBase;
import ie.ibuttimer.widget.TextViewAdapterInterface;

/**
 * Class to represent text transaction templates as they are stored in the database.
 * @author Ian Buttimer
 *
 */
public class TextTemplate extends TextViewAdapterBase implements TextViewAdapterInterface, Parcelable {

	/* fields stored in super class:
	 * - texttrans id
	 * - texttrans name
	 *  */
	private long typeId;
	private long bankId;
	private String prototype;
	
	
	/**
	 * @param id
	 * @param text
	 */
	public TextTemplate(long id, String text) {
		super(id, text);
	}


	/**
	 * Retrieve all the text templates in the database
	 * @param cr			- Content Resolver to use
	 * @param selection		- A filter declaring which rows to return
	 * @param selectionArgs	- Values to replace ? in <code>selection</code>
	 * @return		an ArrayList of TextTemplate objects
	 */
	private static ArrayList<TextTemplate> loadTextTemplatesFromProvider(ContentResolver cr, String selection, String[] selectionArgs) {
		ArrayList<TextTemplate> templates = new ArrayList<TextTemplate>();
		Cursor c = cr.query(DatabaseManager.TEXTTRANS_URI, null, selection, selectionArgs, null);
		if (c.moveToFirst()) {
			int idIdx = c.getColumnIndex(DatabaseManager.TEXTTRANS_ID);
			int typeIdx = c.getColumnIndex(DatabaseManager.TEXTTRANS_TYPE);
			int nameIdx = c.getColumnIndex(DatabaseManager.TEXTTRANS_NAME);
			int bankIdx = c.getColumnIndex(DatabaseManager.TEXTTRANS_BANK);
			int protoIdx = c.getColumnIndex(DatabaseManager.TEXTTRANS_PROTO);
			do {
				// Extract the details.
				TextTemplate template = new TextTemplate(c.getInt(idIdx), c.getString(nameIdx));
				template.typeId = c.getLong(typeIdx);
				template.bankId = c.getLong(bankIdx);
				template.prototype = c.getString(protoIdx);

				templates.add( template );
			} while(c.moveToNext());
		}
		c.close();
		return templates;
	}

	
	/**
	 * Retrieve all the text templates in the database
	 * @param cr	- Content Resolver to use
	 * @return		an ArrayList of TextTemplate objects
	 */
	public static ArrayList<TextTemplate> loadTextTemplatesFromProvider(ContentResolver cr) {
		return loadTextTemplatesFromProvider(cr, null, null);	// return all records
	}

	
	/**
	 * Retrieve all the text templates in the database for the specified bank
	 * @param cr		- Content Resolver to use
	 * @param bankId	- Id of bank to retrieve templates for
	 * @return		an ArrayList of TextTemplate objects
	 */
	public static ArrayList<TextTemplate> loadTextTemplatesFromProvider(ContentResolver cr, int bankId) {
		String selection = "(" + DatabaseManager.TEXTTRANS_BANK + "=?)";
		String[] selectionArgs = new String[] {	Integer.toString(bankId) };
		return loadTextTemplatesFromProvider(cr, selection, selectionArgs);
	}

	/**
	 * Retrieve the text template from the database
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of template to retrieve
	 * @return		TextTemplate object
	 */
	public static TextTemplate loadFromProvider(ContentResolver cr, long id) {
		String selection = "(" + DatabaseManager.TEXTTRANS_ID + "=?)";
		String[] selectionArgs = new String[] {	Long.toString(id) };
		ArrayList<TextTemplate> templates = loadTextTemplatesFromProvider(cr, selection, selectionArgs);
		if ( templates.size() == 1 )
			return templates.get(0);
		else 
			return null;
	}

	/**
	 * Create a TextTemplate from the content data
	 * @param values	- ContentValues to extract data from
	 * @return
	 */
	public static TextTemplate createTextTemplateFromValues(ContentValues values) {
		TextTemplate template = new TextTemplate(0, values.getAsString(DatabaseManager.TEXTTRANS_NAME));
		template.typeId = values.getAsLong(DatabaseManager.TEXTTRANS_TYPE);
		if ( values.containsKey(DatabaseManager.TEXTTRANS_BANK) )
			template.bankId = values.getAsLong(DatabaseManager.TEXTTRANS_BANK);
		template.prototype = values.getAsString(DatabaseManager.TEXTTRANS_PROTO);
		return template;
	}
	
	
	/**
	 * Delete a TextTemplate from the database
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of Object to delete 
	 * @return		<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	public static boolean deleteTextTemplateFromProvider(ContentResolver cr, long id) {
		String selection = "(" + DatabaseManager.TEXTTRANS_ID + "=?)";
		String[] selectionArgs = new String[] {	Long.toString(id) };
		return (cr.delete(DatabaseManager.TEXTTRANS_URI, selection, selectionArgs) > 0);
	}
	
	/**
	 * Delete this object from the database
	 * @param cr	- Content Resolver to use
	 * @return		<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	public boolean deleteFromProvider(ContentResolver cr) {
		return deleteTextTemplateFromProvider(cr, super.getId());
	}

	
	/**
	 * Update a TextTemplate in the database
	 * @param cr		- Content Resolver to use
	 * @param id		- Id of Object to update
	 * @param values	- Values to update 
	 * @return		<code>true</code> if successfully updated, <code>false</code> otherwise
	 */
	public static boolean updateTextTemplateInProvider(ContentResolver cr, long id, ContentValues values) {
		String selection = "(" + DatabaseManager.TEXTTRANS_ID + "=?)";
		String[] selectionArgs = new String[] {	Long.toString(id) };
		return (cr.update(DatabaseManager.TEXTTRANS_URI, values, selection, selectionArgs) > 0);
	}
	
	/**
	 * Update this object in the database
	 * @param cr		- Content Resolver to use
	 * @param values	- Values to update 
	 * @return		<code>true</code> if successfully updated, <code>false</code> otherwise
	 */
	public boolean updateInProvider(ContentResolver cr, ContentValues values) {
		return updateTextTemplateInProvider(cr, getId(), values);
	}

	
	/**
	 * @return the typeId
	 */
	public long getTypeId() {
		return typeId;
	}


	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}


	/**
	 * @return the bankId
	 */
	public long getBankId() {
		return bankId;
	}


	/**
	 * @param bankId the bankId to set
	 */
	public void setBankId(long bankId) {
		this.bankId = bankId;
	}


	/**
	 * @return the prototype
	 */
	public String getPrototype() {
		return prototype;
	}


	/**
	 * @param prototype the prototype to set
	 */
	public void setPrototype(String prototype) {
		this.prototype = prototype;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (bankId ^ (bankId >>> 32));
		result = prime * result
				+ ((prototype == null) ? 0 : prototype.hashCode());
		result = prime * result + (int) (typeId ^ (typeId >>> 32));
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TextTemplate [typeId=" + typeId + ", bankId=" + bankId
				+ ", prototype=" + prototype + ", toString()="
				+ super.toString() + "]";
	}


	@Override
	public Object clone () {
		TextTemplate template = new TextTemplate(getId(), getName());
		template.typeId = typeId;
		template.bankId = bankId;
		template.prototype = prototype;
		return template;
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
		if (!(obj instanceof TextTemplate))
			return false;
		TextTemplate other = (TextTemplate) obj;
		if (bankId != other.bankId)
			return false;
		if (prototype == null) {
			if (other.prototype != null)
				return false;
		} else if (!prototype.equals(other.prototype))
			return false;
		if (typeId != other.typeId)
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
		dest.writeLong(typeId);
		dest.writeLong(bankId);
		dest.writeString(prototype);
	}
	
	public static final Parcelable.Creator<TextTemplate> CREATOR = new Parcelable.Creator<TextTemplate>() {
		public TextTemplate createFromParcel(Parcel in) {
			return new TextTemplate(in);
		}
		
		public TextTemplate[] newArray(int size) {
			return new TextTemplate[size];
		}
	};

	/**
	 * @param in
	 */
	protected TextTemplate(Parcel in) {
		super(in);
		typeId = in.readLong();
		bankId = in.readLong();
		prototype = in.readString();
	}

}
