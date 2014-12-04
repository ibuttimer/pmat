/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

/**
 * Extension of DialogFragment to display DatePickerDialog/TimePickerDialog
 * @author Ian Buttimer
 *
 */
public class SelectDateTimeFragment extends DialogFragment {

	// argument keys and values
	public static String DATE = "date";
	public static String TITLE = "title";
	public static String TYPE = "type";
	public static int SELECT_DATE = 1;
	public static int SELECT_TIME = 2;

	// default to a date picker
	private static int defaultType = SELECT_DATE; 
	private static int defaultTitle = R.string.select_date; 

	private GregorianCalendar date;
	private int titleID;
	private int type; 
	
	
	/**
	 * default constructor
	 */
	public SelectDateTimeFragment() {
		date = null;
		titleID = defaultTitle;
		type = defaultType;
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#setArguments(android.os.Bundle)
	 */
	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		setArgs(args);
	}

	
	/**
	 * Set local variables from a bundle
	 * @param args	- Bundle containing arguments
	 */
	private void setArgs(Bundle args) {
		if ( args.containsKey(TITLE) ) {
			titleID = args.getInt(TITLE, defaultTitle);
		}
		if ( args.containsKey(DATE) ) {
			GregorianCalendar dateArg = (GregorianCalendar) args.getSerializable(DATE);
			if ( dateArg != null )
				date = dateArg;
		}
		if ( args.containsKey(TYPE) ) {
			type = args.getInt(TYPE, defaultType);
		}
	}

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		if ( savedInstanceState != null )
			setArgs(savedInstanceState);			

		if ( date == null )
			date = new GregorianCalendar();

		// Create a new instance of DatePickerDialog/TimePickerDialog and return it
		Context context = getActivity();
		AlertDialog dialog;
		if ( type == SELECT_DATE ) {
			int year = date.get(Calendar.YEAR);
			int month = date.get(Calendar.MONTH);
			int day = date.get(Calendar.DAY_OF_MONTH);
			dialog = new DatePickerDialog(context, (DatePickerDialog.OnDateSetListener)context, year, month, day);
		}
		else {
			int hour = date.get(Calendar.HOUR_OF_DAY);
			int minute = date.get(Calendar.MINUTE);
			dialog = new TimePickerDialog(context, (TimePickerDialog.OnTimeSetListener)context, hour, minute, 
											android.text.format.DateFormat.is24HourFormat(context));
		}
		
		dialog.setTitle(titleID);
		return dialog; 
	}

	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(TITLE, titleID);
		outState.putSerializable(DATE, date);
	}
}
