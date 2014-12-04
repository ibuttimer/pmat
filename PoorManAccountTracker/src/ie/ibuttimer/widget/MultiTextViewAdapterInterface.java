/**
 * 
 */
package ie.ibuttimer.widget;

/**
 * Interface to be implemented by classes which use the MultiTextViewAdapter.
 * @author Ian Buttimer
 *
 */
public interface MultiTextViewAdapterInterface extends TextViewAdapterInterface {

	/**
	 * Generate a string suitable for display in the TextViewAdapter. 
	 * @param index	- Index of the TextView get the display string for
	 * @return	String to display
	 */
	public String toDisplayString(int index);

	/**
	 * Sets a prefix to prepend to the display string.<br>
	 * <b>Note:</b> The implementing class need to define local variables to save the prefix if required. 
	 * @param index		- Index of the TextView to set the prefix for
	 * @param prefix	- Prefix string to set
	 */
	public void setPrefix(int index, String prefix);
	
	/**
	 * Sets the text colour.<br>
	 * <b>Note:</b> The implementing class need to define local variables to save the colours if required. 
	 * @param index		- Index of the TextView to set the colour for
	 * @param colour	- Colour to set, see {@link android.graphics.Color#parseColor(String)} for supported formats. Use <code>null</code> to reset to default.
	 */
	public void setColour(int index, String colour);
	
	/**
	 * Clears the text colour.<br>
	 * <b>Note:</b> The implementing class need to define local variables to save the colours if required. 
	 */
	public void clearColours();
	
	/**
	 * Sets the text colour.<br>
	 * @param index		- Index of the TextView to set the colour for
	 * @see				#setColour(int, String)
	 */
	public String getColour(int index);
}
