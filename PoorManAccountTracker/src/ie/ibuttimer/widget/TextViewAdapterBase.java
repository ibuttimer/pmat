/**
 * 
 */
package ie.ibuttimer.widget;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Ian Buttimer
 *
 */
public class TextViewAdapterBase implements TextViewAdapterInterface, Parcelable {

	private long id;
	private String name;

	protected String prefix = "";

	/**
	 * Constructor
	 * @param id	- id which represents the object. 
	 * @param name	- text which the object represents 
	 */
	public TextViewAdapterBase() {
		this(0, null);
	}

	/**
	 * Constructor
	 * @param id	- id which represents the object. 
	 * @param text	- text which the object represents 
	 */
	public TextViewAdapterBase(long id, String text) {
		this.id = id;
		this.name = text;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", name=" + name
				+ ", prefix=" + prefix + "]";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((prefix == null) ? 0 : prefix
						.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TextViewAdapterBase))
			return false;
		TextViewAdapterBase other = (TextViewAdapterBase) obj;
		if (prefix == null) {
			if (other.prefix != null)
				return false;
		} else if (!prefix.equals(other.prefix))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	/* Implement TextViewAdapterInterface */
	
	@Override
	public String toDisplayString() {
		return prefix + name;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}


	/* Implement Parcelable interface */
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(prefix);
	}
	
	public static final Parcelable.Creator<TextViewAdapterBase> CREATOR = new Parcelable.Creator<TextViewAdapterBase>() {
		public TextViewAdapterBase createFromParcel(Parcel in) {
			return new TextViewAdapterBase(in);
		}
		
		public TextViewAdapterBase[] newArray(int size) {
			return new TextViewAdapterBase[size];
		}
	};

	protected TextViewAdapterBase(Parcel in) {
		id = in.readLong();
		name = in.readString();
		prefix = in.readString();
	}


}
