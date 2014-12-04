/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.PreferenceControl;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

/**
 * @author Ian Buttimer
 *
 */
public class AccountListPreference extends MultiSelectListPreference {

	/**
	 * @param context
	 */
	public AccountListPreference(Context context) {
		super(context);
		populateAccountList(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public AccountListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		populateAccountList(context);
	}
	
	
	/**
	 * Set the list entries and values based on the database content
	 * @param context
	 */
	void populateAccountList(Context context) {
	    List<CharSequence> entries = new ArrayList<CharSequence>();
	    List<CharSequence> entriesValues = new ArrayList<CharSequence>();

	    Cursor c = context.getContentResolver().query(DatabaseManager.ACCOUNT_BASIC_URI, new String[] { 
	    								DatabaseManager.ACCOUNT_ID, DatabaseManager.ACCOUNT_NAME }, 
	    								null, null, null);
	    if ( c.moveToFirst() ) {
	    	int idIdx = c.getColumnIndexOrThrow(DatabaseManager.ACCOUNT_ID);
	    	int nameIdx = c.getColumnIndexOrThrow(DatabaseManager.ACCOUNT_NAME);
	    	
	    	do {
		        long id = c.getLong(idIdx);
		        String name = c.getString(nameIdx);

		        entries.add(name);
		        entriesValues.add(Long.toString(id));
	    	}
	    	while (c.moveToNext());
	    }
	    c.close();

	    setEntries(entries.toArray(new CharSequence[]{}));
	    setEntryValues(entriesValues.toArray(new CharSequence[]{}));
	    
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
	}

	/* (non-Javadoc)
	 * @see android.preference.MultiSelectListPreference#onSetInitialValue(boolean, java.lang.Object)
	 */
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		super.onSetInitialValue(restoreValue, defaultValue);
		
		//If restorePersistedValue is true, you should restore the Preference value from the android.content.SharedPreferences. 
		// If restorePersistedValue is false, you should set the Preference value to defaultValue that is given (and possibly store to SharedPreferences if shouldPersist() is true). 
		if ( restoreValue ) {

			Object values = defaultValue;
			if ( shouldPersist() )
				values = PreferenceControl.getPreference(getContext(), getKey());
			setValues((Set<String>) values);
		}
	}

	/* (non-Javadoc)
	 * @see android.preference.MultiSelectListPreference#onDialogClosed(boolean)
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		
		super.onDialogClosed(positiveResult);

		if ( positiveResult )
			getContext().sendBroadcast(new Intent(Constants.UPDATE_SNAPSHOT_ACTION));
	}
}
