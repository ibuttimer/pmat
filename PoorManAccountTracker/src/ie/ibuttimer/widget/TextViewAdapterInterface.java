/**
 * 
 */
package ie.ibuttimer.widget;

/**
 * Interface to be implemented by classes which use the TextViewAdapter.
 * @author Ian Buttimer
 *
 */
public interface TextViewAdapterInterface {

	/**
	 * Generate a string suitable for display in the TextViewAdapter. 
	 * @return	String to display
	 */
	public String toDisplayString();

	/**
	 * Return an id which represents the object. 
	 * @return	ID
	 */
	public long getId();

	/**
	 * Set an id which represents the object. 
	 * @param id	- ID to set
	 */
	public void setId(long id);

	/**
	 * Sets the object name
	 * @param name	- Name to set
	 */
	public void setName(String name);

	/**
	 * Gets the object name
	 * @return	Name
	 */
	public String getName();

	/**
	 * Gets the prefix to prepend to the display string.
	 * @return	Prefix
	 */
	public String getPrefix();

	/**
	 * Sets a prefix to prepend to the display string.<br>
	 * <b>Note:</b> The implementing class need to define a local variable to save the prefix if required. 
	 */
	public void setPrefix(String prefix);
}
