/**
 * 
 */
package ie.ibuttimer.widget;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A base class for any classes requiring the MultiTextViewAdapterInterface
 * @author Ian Buttimer
 */
public class MultiTextViewAdapterBase extends TextViewAdapterBase implements MultiTextViewAdapterInterface, Parcelable {

	private String texts[];
	private String prefixs[];
	private String colours[];

	/**
	 * Constructor
	 * @param id		- id representing this item
	 * @param strings	- strings (name & balance) to display in list item
	 */
	public MultiTextViewAdapterBase(long id, String[] strings) {
		super(id, null);	// text field in super is not used
		this.texts = strings;
		final int N = strings.length;
		colours = new String[N];
		prefixs = new String[N];
		for ( int i = 0; i < N; ++i )
			prefixs[i] = "";
	}

	/**
	 * Constructor
	 * @param id		- id representing this item
	 * @param strings	- strings (name & balance) to display in list item
	 */
	public MultiTextViewAdapterBase(String[] strings) {
		this(0, strings);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " [texts=" + Arrays.toString(texts)
				+ ", prefixs=" + Arrays.toString(prefixs) + ", colours="
				+ Arrays.toString(colours) + "]";
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(colours);
		result = prime * result + Arrays.hashCode(prefixs);
		result = prime * result + Arrays.hashCode(texts);
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
		if (!(obj instanceof MultiTextViewAdapterBase))
			return false;
		MultiTextViewAdapterBase other = (MultiTextViewAdapterBase) obj;
		if (!Arrays.equals(colours, other.colours))
			return false;
		if (!Arrays.equals(prefixs, other.prefixs))
			return false;
		if (!Arrays.equals(texts, other.texts))
			return false;
		return true;
	}


	/* Implement MultiTextViewAdapterInterface interface */
	
	@Override
	public String toDisplayString(int index) {
		return prefixs[index] + texts[index];
	}

	@Override
	public void setPrefix(int index, String prefix) {
		prefixs[index] = prefix;
	}

	@Override
	public void setColour(int index, String colour) {
		colours[index] = colour;
	}

	@Override
	public void clearColours() {
		final int N = colours.length;
		for ( int i = 0; i < N; ++i )
			colours[i] = null;
	}

	@Override
	public String getColour(int index) {
		return colours[index];
	}


	/* Implement Parcelable interface */
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeStringArray(texts);
		dest.writeStringArray(prefixs);
		dest.writeStringArray(colours);
	}
	
	public static final Parcelable.Creator<MultiTextViewAdapterBase> CREATOR = new Parcelable.Creator<MultiTextViewAdapterBase>() {
		public MultiTextViewAdapterBase createFromParcel(Parcel in) {
			return new MultiTextViewAdapterBase(in);
		}
		
		public MultiTextViewAdapterBase[] newArray(int size) {
			return new MultiTextViewAdapterBase[size];
		}
	};

	protected MultiTextViewAdapterBase(Parcel in) {
		super(in);
		in.readStringArray(texts);
		in.readStringArray(prefixs);
		in.readStringArray(colours);
	}

}
