/**
 * 
 */
package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.db.DatabaseManager.DatabaseState;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.help.HelpListActivity;
import ie.ibuttimer.pmat.util.Logger;
import ie.ibuttimer.widget.ButteredToast;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The class is the base class for activities in the application.
 * @author Ian Buttimer
 *
 */
public class BaseActivity extends Activity {

	// menu constants
	static final int NO_RESULT = -1;
	static final int EDIT_MENUITEM = 100;
	static final int SHOW_SETTINGS = 0;
	static final int SHOW_ADD_ACCOUNT = 1;
	static final int SHOW_ADD_TRANSACTION = 2;
	static final int SHOW_EDIT_TRANSACTION = EDIT_MENUITEM + SHOW_ADD_TRANSACTION;
	static final int SHOW_ADD_BANK = 3;
	static final int SHOW_EDIT_BANK = EDIT_MENUITEM + SHOW_ADD_BANK;
	static final int SHOW_ADD_USER = 4;
	static final int SHOW_EDIT_USER = EDIT_MENUITEM + SHOW_ADD_USER;
	static final int SHOW_ADD_SMS_TEMPLATE = 5;
	static final int SHOW_ACCOUNT = 6;
	static final int SHOW_HELP = 7;

	protected static final int[] dataBaseMenuActions = new int[] {
			R.id.action_AddTransaction,
			R.id.action_AddAccount,
			R.id.action_AddBank,
			R.id.action_CreateSmsTemplate,
			R.id.action_AddUser,
	};

	protected ProgressDialog progressDialog;

	protected static final int MIN_PROGRESS = 0; 
	protected static final int MAX_PROGRESS = 100; 

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	
	/**
	 * Determine if a menu action is currently allowed
	 * @param action	- menu action to check
	 * @return			<code>true</code> if allowed, <code>false</code> otherwise
	 */
	protected boolean isMenuActionAllowed(int action) {
		boolean allowed = true;

		for ( int i = dataBaseMenuActions.length - 1; i >= 0; --i ) {
			if ( action == dataBaseMenuActions[i] ) {
				boolean dbOk = (DatabaseManager.getDatabaseState() == DatabaseState.DATABASE_OK);
				allowed = dbOk;
				switch ( action ) {
					case R.id.action_AddTransaction:
						if ( allowed ) {
							Bundle b = getContentResolver().call(DatabaseManager.COUNT_ROWS_URI, 
									DatabaseManager.COUNT_ROWS_URI_STR, 
									DatabaseManager.ACCOUNT_TABLE, null);
							if ( b != null && b.containsKey(DatabaseManager.ACCOUNT_TABLE) )
								allowed = (b.getLong(DatabaseManager.ACCOUNT_TABLE) > 0);
						}
						break;
					default:
						break;
				}
			}
		}
		return allowed;
	}
	


	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		for ( int i = dataBaseMenuActions.length - 1; i >= 0; --i ) {
			MenuItem item = menu.findItem(dataBaseMenuActions[i]);
			if ( item != null )
				item.setEnabled( isMenuActionAllowed(dataBaseMenuActions[i]) );
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		if ( processMenuItem(item.getItemId(), item.getIntent()) )
			return true;
		else
			return onOptionsItemSelected(item);
	}

	
	/**
	 * Process menu items
	 * @param itemId	- id of item to process
	 * @return			<code>true</code> if item processed, <code>false</code> otherwise
	 */
	public boolean processMenuItem(int itemId, Intent intent) {
		// Handle item selection
		Class<?> cls = null;
		int result = NO_RESULT;
		switch (itemId) {
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
			case R.id.action_settings:
				cls = SettingsActivity.class;
				result = SHOW_SETTINGS;
				break;
			case R.id.action_AddAccount:
				cls = AddAccountActivity.class;
				result = SHOW_ADD_ACCOUNT;
				break;
			case R.id.action_AddTransaction:
				cls = AddTransactionActivity.class;
				result = SHOW_ADD_TRANSACTION;
				break;
			case R.id.action_AddBank:
			case R.id.action_EditBank:
				cls = AddBankActivity.class;
				result = SHOW_EDIT_BANK;
				result = (itemId == R.id.action_AddBank ? SHOW_ADD_BANK : SHOW_EDIT_BANK);
				break;
			case R.id.action_CreateSmsTemplate:
				cls = CreateSmsTemplateActivity.class;
				result = SHOW_ADD_SMS_TEMPLATE;
				break;
			case R.id.action_AddUser:
				cls = AddUserActivity.class;
				result = SHOW_ADD_USER;
				break;
			case R.id.action_About:
				cls = AboutActivity.class;
				break;
			case R.id.action_help:
				cls = HelpListActivity.class;
				break;
			default:
				return false;
		}

		// By default there is no Intent associated with a menu item
		if ( intent == null )
			intent = new Intent(this, cls);
		// else intent was set elsewhere so use it

		if ( result != NO_RESULT )
			startActivityForResult(intent, result);
		else
			startActivity(intent);
		return true;
	}
	
	
	/**
	 * Display a standard toast that just contains a text view with the text from a resource.
	 * @param resId			- The resource id of the string resource to use. Can be formatted text.
	 * @param duration		- How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 * @param formatArgs	- Any arguments required to complete the message string
	 */
	public void showToast(int resId, int duration, Object... formatArgs) {
		ButteredToast.makeText(this, resId, duration, formatArgs).show();
	}

	/**
	 * Display a standard toast that just contains a text view with the text from a resource.
	 * @param resId			- The resource id of the string resource to use. Can be formatted text.
	 * @param duration		- How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 */
	public void showToast(int resId, int duration) {
		showToast(resId, duration, (Object)null);
	}

	/**
	 * Display a standard toast that just contains a text view with the text from a resource.
	 * @param text			- The text to show. Can be formatted text.
	 * @param duration		- How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 * @param formatArgs	- Any arguments required to complete the message string
	 */
	public void showToast(CharSequence text, int duration, Object... formatArgs) {
		ButteredToast.makeText(this, text, duration, formatArgs).show();
	}
	
	/**
	 * Display a standard toast that just contains a text view with the text from a resource.
	 * @param text			- The text to show. Can be formatted text.
	 * @param duration		- How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 */
	public void showToast(CharSequence text, int duration) {
		showToast(text, duration, (Object)null);
	}

	/**
	 * Initialise and display the progress dialog
	 */
	public void initProgress(int resourceId) {
		Resources r = getResources();
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		String title = (String) r.getText(resourceId);
		progressDialog.setTitle(title);
		progressDialog.setMessage(getResources().getText(R.string.please_wait));
		progressDialog.setCancelable(true);
		progressDialog.setIndeterminate(false);
		progressDialog.setMax(MAX_PROGRESS);
		progressDialog.setProgress(MIN_PROGRESS);
		progressDialog.setProgressNumberFormat(null);
		progressDialog.show();
		Logger.d(getClass().getName() + " show " + title );
	}

	/**
	 * Update the progress dialog
	 * @param integer	- progress percent to set
	 */
	public void setProgressPercent(ProgressUpdate progress) {
		String msg;
		if ( !TextUtils.isEmpty(progress.message) ) 
			msg = progress.message;
		else
			msg = (String) getResources().getText(progress.msgResource);
		if ( progressDialog != null ) {
			progressDialog.setProgress(progress.progress);
			progressDialog.setMessage(msg);
		}
		Logger.d(getClass().getName() + " progress " + progress.progress + " " + msg);
	}

	/**
	 * Clear the progress dialog
	 */
	public void clearProgress() {
		// clear progress dialog
		if ( progressDialog != null ) {
			progressDialog.cancel();
			progressDialog = null;
			Logger.d(getClass().getName() + " clear progress");
		}
	}

	
	/**
	 * Class to provide progress dialog updates
	 * @author Ian Buttimer
	 */
	public class ProgressUpdate {
		int progress;
		String message;
		int msgResource;
		/**
		 * @param progress
		 * @param message
		 */
		public ProgressUpdate(int progress, String message) {
			super();
			this.progress = progress;
			this.message = message;
			this.msgResource = R.string.please_wait;
		}
		/**
		 * @param progress
		 * @param message
		 * @param msgResource
		 */
		public ProgressUpdate(int progress, int msgResource) {
			super();
			this.progress = progress;
			this.msgResource = msgResource;
			this.message = null;
		}
		/**
		 * @param progress
		 */
		public ProgressUpdate(int progress) {
			this(progress, null);
		}
	}
	
}
