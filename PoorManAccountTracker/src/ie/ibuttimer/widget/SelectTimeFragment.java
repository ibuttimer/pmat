/**
 * 
 */
package ie.ibuttimer.widget;

import android.os.Bundle;

/**
 * Extension of DialogFragment to display TimePickerDialog
 * @author Ian Buttimer
 *
 */
public class SelectTimeFragment extends SelectDateTimeFragment {

	/**
	 * default constructor
	 */
	public SelectTimeFragment() {
		super();
	}

	/* (non-Javadoc)
	 * @see ie.ibuttimer.widget.SelectDateTimeFragment#setArguments(android.os.Bundle)
	 */
	@Override
	public void setArguments(Bundle args) {
		args.putInt(TYPE, SelectDateTimeFragment.SELECT_TIME);
		super.setArguments(args);
	}
}
