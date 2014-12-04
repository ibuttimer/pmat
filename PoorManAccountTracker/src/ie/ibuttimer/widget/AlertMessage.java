/**
 * 
 */
package ie.ibuttimer.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * @author Ian Buttimer
 *
 */
public class AlertMessage extends DialogFragment {
	
	public static final int BUTTON_LEFT = 0;
	public static final int BUTTON_MIDDLE = 1;
	public static final int BUTTON_RIGHT = 2;
	public static final int MAX_BUTTONS = 3;
	
	public static final String TITLE_ARG = "title";
	public static final String MESSAGE_ARG = "msg";
	public static final String BUTTON_LEFT_ARG = "left";
	public static final String BUTTON_MIDDLE_ARG = "mid";
	public static final String BUTTON_RIGHT_ARG = "right";
	public static final String RESPONSE_ARG = "resp";

	private String titleStr;
	private String msgStr;
	private String buttonStr[] = new String[MAX_BUTTONS];
	private long responseCode;
	
	
	/* (non-Javadoc)
	 * @see android.app.Fragment#setArguments(android.os.Bundle)
	 */
	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		
		if ( args.containsKey(TITLE_ARG) )
			titleStr = args.getString(TITLE_ARG);
		if ( args.containsKey(MESSAGE_ARG) )
			msgStr = args.getString(MESSAGE_ARG);
		if ( args.containsKey(BUTTON_LEFT_ARG) )
			buttonStr[BUTTON_LEFT] = args.getString(BUTTON_LEFT_ARG);
		if ( args.containsKey(BUTTON_MIDDLE_ARG) )
			buttonStr[BUTTON_MIDDLE] = args.getString(BUTTON_MIDDLE_ARG);
		if ( args.containsKey(BUTTON_RIGHT_ARG) )
			buttonStr[BUTTON_RIGHT] = args.getString(BUTTON_RIGHT_ARG);
		if ( args.containsKey(RESPONSE_ARG) )
			responseCode = args.getLong(RESPONSE_ARG);
	}
	
	/**
	 * @param titleStr the titleStr to set
	 */
	public AlertMessage setTitleStr(String titleStr) {
		this.titleStr = titleStr;
		return this;
	}

	/**
	 * @param msgStr the msgStr to set
	 */
	public AlertMessage setMsgStr(String msgStr) {
		this.msgStr = msgStr;
		return this;
	}

	
	/**
	 * @param which		- button to set, one of BUTTON_LEFT/BUTTON_MIDDLE/BUTTON_RIGHT
	 * @param buttonStr - the text to set
	 * @throws IllegalArgumentException
	 */
	public AlertMessage setButtonStr(int which, String buttonStr) throws IllegalArgumentException {
		if ( which >= BUTTON_LEFT && which < MAX_BUTTONS )
			this.buttonStr[which] = buttonStr;
		else
			throw new IllegalArgumentException("Invalid button " + which);
		return this;
	}



	private class DismissHandler implements DialogInterface.OnClickListener {

		AlertMessage alert;
		int button;
		/**
		 * @param alert
		 * @param button
		 */
		public DismissHandler(AlertMessage alert, int button) {
			super();
			this.alert = alert;
			this.button = button;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mListener.onAlertMessageClick(alert, button, responseCode);
			alert.dismiss();
		}
	}
	

	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
	    // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if ( !TextUtils.isEmpty(titleStr) )
        	builder.setTitle(titleStr);
        if ( !TextUtils.isEmpty(msgStr) )
        	builder.setMessage(msgStr);
        for ( int i = BUTTON_LEFT; i < MAX_BUTTONS; ++i ) {
            if ( !TextUtils.isEmpty(buttonStr[i]) ) {
            	switch ( i ) {
	            	case BUTTON_LEFT:
	            		builder.setNegativeButton(buttonStr[i], new DismissHandler(this, i));
	            		break;
	            	case BUTTON_MIDDLE:
	            		builder.setNeutralButton(buttonStr[i], new DismissHandler(this, i));
	            		break;
	            	case BUTTON_RIGHT:
	            		builder.setPositiveButton(buttonStr[i], new DismissHandler(this, i));
	            		break;
            	}
            }
        }
        // Create the AlertDialog object and return it
        return builder.create();
	}



	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface AlertMessageListener {
        /**
         * Callback method to return selection to activity
         * @param dialog
         * @param which		- button that was selected
         * @param response	- response code
         */
        public void onAlertMessageClick(DialogFragment dialog, int which, long response);
    }

	private AlertMessageListener mListener;

	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Verify that the host activity implements the callback interface
        try {
            // Instantiate the AlertMessageListener so we can send events to the host
            mListener = (AlertMessageListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AlertMessageListener" );
        }
	}



	
	
}
