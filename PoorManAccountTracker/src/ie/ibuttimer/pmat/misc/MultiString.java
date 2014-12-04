/**
 * 
 */
package ie.ibuttimer.pmat.misc;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Ian Buttimer
 *
 */
public class MultiString implements Parcelable {

	protected String[] strings;
	
	/**
	 * @param count
	 */
	public MultiString(int count) {
		this.strings = new String[count];
	}

	/**
	 * @param strings
	 */
	public MultiString(String[] strings) {
		this.strings = strings;
	}

	/**
	 * @return the strings
	 */
	public String[] getStrings() {
		return strings;
	}

	/**
	 * @param strings the strings to set
	 */
	public void setStrings(String[] strings) {
		this.strings = strings;
	}
	
	/**
	 * @return number of strings
	 */
	public int getCount() {
		return strings.length;
	}

	/**
	 * @param index		- index of string to return
	 * @return the string
	 */
	public String getString(int index) {
		return strings[index];
	}

	/**
	 * @param index		- index of string to set
	 * @param string	- the strings to set
	 */
	public void setString(int index, String string) {
		strings[index] = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(strings);
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
		if (!(obj instanceof MultiString))
			return false;
		MultiString other = (MultiString) obj;
		if (!Arrays.equals(strings, other.strings))
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
		dest.writeStringArray(strings);
		
	}
	
	public static final Parcelable.Creator<MultiString> CREATOR = new Parcelable.Creator<MultiString>() {
		public MultiString createFromParcel(Parcel in) {
			return new MultiString(in);
		}
		
		public MultiString[] newArray(int size) {
			return new MultiString[size];
		}
	};

	protected MultiString(Parcel in) {
		in.readStringArray(strings);
	}

}
