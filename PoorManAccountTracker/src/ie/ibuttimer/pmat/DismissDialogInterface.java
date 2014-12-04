/**
 * 
 */
package ie.ibuttimer.pmat;

import android.content.Intent;

/**
 * @author Ian Buttimer
 *
 */
public interface DismissDialogInterface {
	
	/**
	 * Call this to set the result that will return to the caller.
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 * @param data		- The data to propagate back to the originating activity.
	 */
	public void onDoDismiss(int result, Intent data);

	/**
	 * Call this to set the result that will return to the caller.
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 */
	public void onDoDismiss(int result);


}
