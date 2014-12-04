/**
 * 
 */
package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.util.Constants;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;

/**
 * Abstract class providing Fragment framework for edit/select activities.<br>
 * Activities using this framework <b>must</b>:
 * <ul>
 * <li>implement the EditDoneInterface </li>
 * <li>add a Request Code integer to the argument bundle</li>
 * </ul>
 *
 * @author Ian Buttimer
 *
 */
public abstract class EditFragment extends DialogFragment {

	
	/** Key to identify the Request Code in the argument bundle */
	public static final String EDITFRAGMENT_REQUESTCODE = Constants.APP_BASE + ".RequestCode";

	private boolean haveRequestCode;
	private int requestCode;

	private EditDoneInterface mCallback;	// dialog callback

	/**
	 * 
	 */
	public EditFragment() {
		this.haveRequestCode = false;
	}

	
	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // set the request code
        Bundle b = getArguments();
        if ( b != null ) {
            if ( b.containsKey(EDITFRAGMENT_REQUESTCODE) ) {
            	int code = b.getInt(EDITFRAGMENT_REQUESTCODE);
        		setRequestCode(code);
            }
            else {
            	throw new IllegalArgumentException("Request code not part of argument bundle");
            }
        }
        else {
        	throw new IllegalArgumentException("No argument bundle.");
        }
	}

	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (EditDoneInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EditDoneInterface");
        }
  	}

	/**
	 * Set the request code for this object.
	 * @param requestCode	- request code originally allowing you to identify who this result came from.
	 */
	private void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
		this.haveRequestCode = true;
	}
	
	
	/**
	 * Call the EditDoneInterface.onEditDone() implementation matching the method signature 
	 * @param request	- The integer request code originally supplied as part of the EditFragment argument bundle, allowing you to identify who this result came from.
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 * @param data		- The data to propagate back to the originating activity.
	 */
	public void onEditDone(int request, int result, Intent data) {
		mCallback.onEditDone(request, result, data);
	}

	/**
	 * Call the EditDoneInterface.onEditDone() implementation matching the method signature 
	 * @param request	- The integer request code originally supplied as part of the EditFragment argument bundle, allowing you to identify who this result came from.
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 */
	public void onEditDone(int request, int result) {
		mCallback.onEditDone(request, result, null);
	}

	/**
	 * Call the EditDoneInterface.onEditDone() implementation matching the method signature 
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 * @param data		- The data to propagate back to the originating activity.
	 * @throws IllegalArgumentException if the Request Code has not been set for this object.
	 */
	public void onEditDone(int result, Intent data) {
		if ( haveRequestCode )
			onEditDone(requestCode, result, data);
		else
			throw new IllegalArgumentException("Request Code not set");
	}
	
	/**
	 * Call the EditDoneInterface.onEditDone() implementation matching the method signature 
	 * @param result	- The result code to propagate back to the originating activity, often RESULT_CANCELED or RESULT_OK
	 * @throws IllegalArgumentException if the Request Code has not been set for this object.
	 */
	public void onEditDone(int result) {
		onEditDone(result, null);
	}

	
}
