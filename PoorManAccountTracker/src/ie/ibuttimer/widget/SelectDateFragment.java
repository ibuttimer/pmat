/**
 * 
 */
package ie.ibuttimer.widget;

import android.os.Bundle;

/**
 * Extension of DialogFragment to display DatePickerDialog
 * @author Ian Buttimer
 *
 */
public class SelectDateFragment extends SelectDateTimeFragment {

	/**
	 * default constructor
	 */
	public SelectDateFragment() {
		super();
	}

	/* (non-Javadoc)
	 * @see ie.ibuttimer.widget.SelectDateTimeFragment#setArguments(android.os.Bundle)
	 */
	@Override
	public void setArguments(Bundle args) {
		args.putInt(TYPE, SelectDateTimeFragment.SELECT_DATE);
		super.setArguments(args);
	}
}
