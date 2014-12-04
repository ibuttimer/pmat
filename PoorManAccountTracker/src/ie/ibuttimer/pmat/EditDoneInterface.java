/**
 * 
 */
package ie.ibuttimer.pmat;

import android.content.Intent;

/**
 * Interface to return results, e.g from a Fragment to an activity 
 * @author Ian Buttimer
 *
 */
public interface EditDoneInterface {

	/**
	 * Call this to set the result that will return to the caller.
	 * @param request	- The integer request code originally supplied as part of the EditFragment argument bundle, allowing you to identify who this result came from.
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 * @param data		- The data to propagate back to the originating activity.
	 */
	public void onEditDone(int request, int result, Intent data);

	/**
	 * Call this to set the result that will return to the caller.
	 * @param request	- The integer request code originally supplied as part of the EditFragment argument bundle, allowing you to identify who this result came from.
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 */
	public void onEditDone(int request, int result);

	/**
	 * Call this to set the result that will return to the caller.
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 * @param data		- The data to propagate back to the originating activity.
	 */
	public void onEditDone(int result, Intent data);

	/**
	 * Call this to set the result that will return to the caller.
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 */
	public void onEditDone(int result);

}
