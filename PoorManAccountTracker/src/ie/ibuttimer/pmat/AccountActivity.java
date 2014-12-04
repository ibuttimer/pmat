package ie.ibuttimer.pmat;

import java.util.Arrays;
import ie.ibuttimer.pmat.db.Account;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.Transaction;
import ie.ibuttimer.pmat.sms.SmsProcessor;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.widget.AlertMessage;
import ie.ibuttimer.widget.AlertMessage.AlertMessageListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AccountActivity extends BaseActivity implements AlertMessageListener {
	
	private Button buttonCancel;
	
	private ActionBar actionBar;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account);

		// Show the Up button in the action bar.
		setupActionBar();
		
		// get references to the activity views
		buttonCancel = (Button)this.findViewById(R.id.accountActivity_buttonCancel);
		actionBar = getActionBar();

		setupCancelButton();
		
		if ( savedInstanceState == null )
			savedInstanceState = getIntent().getExtras();

		getArguments(savedInstanceState);
	}


	/**
	 * Retrieve the arguments from the intent that started this activity 
	 * @param b
	 */
	private void getArguments(Bundle b) {

		if ( b == null )
			return;

		int accountId = b.getInt(Constants.ACCOUNT_ID_NUM);
		int[] accountIdArray = b.getIntArray(Constants.ACCOUNT_ID_ARRAY);
		int selected = accountId;	// selected a/c number

		boolean single = (accountId != 0);
		boolean array = (accountIdArray != null && accountIdArray.length > 0);
		
		if ( !single && !array )
			return;
		else if ( single && array ) {
			// check for duplicate
			for ( int i = 0; i < accountIdArray.length; ++i ) {
				if ( accountIdArray[i] == accountId ) {
					single = false;
					break;
				}
			}
			if ( single ) {
				// add to existing array
				accountIdArray = Arrays.copyOf(accountIdArray, accountIdArray.length + 1);
				accountIdArray[accountIdArray.length - 1] = accountId;
			}
		}
		else if ( single ) {
			// create array
			accountIdArray = new int[1];
			accountIdArray[0] = accountId;
		}
		else {
			// just pass array
			selected = accountIdArray[0];
		}

		setAccountDetails(accountIdArray, selected);
	}
	

	/**
	 * Creates tabs for the account ids specified 
	 * @param accountIdArray	- array of account ids
	 * @param selected			- index of selected tab
	 */
	private void setAccountDetails(int[] accountIdArray, int selected) {

		if ( accountIdArray == null || accountIdArray.length == 0 )
			return;

        // Hide Actionbar Icon
        actionBar.setDisplayShowHomeEnabled(false);
 
        // Hide Actionbar Title
        actionBar.setDisplayShowTitleEnabled(false);
 
        // Create Actionbar Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // create and add tabs for all accounts
        new LoadAccountInfoTask(accountIdArray, selected).execute(DatabaseManager.ACCOUNT_BASIC_URI);
	}
	
	
	/**
	 * AsyncTask to load account transaction details from the database
	 * @author Ian Buttimer
	 */
	private class LoadAccountInfoTask extends AsyncTask<Uri, ProgressUpdate, ActionBar.Tab[]> {
		
		int[] accountIdArray;
		int selected;
		
		/**
		 * Constructor
		 * @param accountIdArray	- array of account ids
		 * @param selected			- index of selected tab
		 */
		public LoadAccountInfoTask(int[] accountIdArray, int selected) {
			super();
			this.accountIdArray = accountIdArray;
			this.selected = selected;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		protected ActionBar.Tab[] doInBackground(Uri... uri) {

			final int N = accountIdArray.length;
			ActionBar.Tab[] result = new ActionBar.Tab[N];
			ContentResolver cr = getContentResolver();

			final int incPerUri = (MAX_PROGRESS - MIN_PROGRESS) / N;

			publishProgress(new ProgressUpdate(MIN_PROGRESS));

			int count = 0;
			for ( int u = 0; u < N; ++u ) {
				
				Account account = new Account(cr, accountIdArray[u]);
				
				int progress = incPerUri * u;
				
				if ( account.isValid() ) {
					publishProgress(new ProgressUpdate(progress, account.getAccountName()));

					result[count++] = createAccountTab(accountIdArray[u]);
				}
			}

			publishProgress(new ProgressUpdate(MAX_PROGRESS));

			return result;
	     }

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		protected void onPreExecute () {
			super.onPreExecute();

			// show progress dialog
			initProgress(R.string.progress_loading_accdetails);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute (ActionBar.Tab[] result) {
			super.onPostExecute(result);

	        // Add tabs to actionbar
			final int N = result.length;
			for ( int i = 0; i < N; ++i ) {
				
				int accId = ((AccountBasics)result[i].getTag()).accountId;	// a/c info is set as tag

		        actionBar.addTab(result[i], (accId == selected));
			}

			// clear progress dialog
			clearProgress();
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(java.lang.Object[])
		 */
		protected void onProgressUpdate(ProgressUpdate... progress) {
			super.onProgressUpdate(progress);

			// update progress dialog
			setProgressPercent(progress[0]);
		}
	 }

	
	
	
	/**
	 * Create a tab for the account id specified.<br>
	 * <b>Note:</b> the tab is NOT added to <code>actionBar</code>
	 * @param accountId	- Account ID
	 * @return			New tab 
	 */
	private ActionBar.Tab createAccountTab(int accountId) {

		ActionBar.Tab tab = null;
		Uri accUri = ContentUris.withAppendedId(DatabaseManager.ACCOUNT_BASIC_URI, accountId);
		Cursor c = getContentResolver().query(accUri, null, null, null, null);	// Return the required account
		if (c.moveToFirst()) {
			int nicknameIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_NICKNAME);
			int bankIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_BANK);

			do {
				// Extract the details and update activity
				String name = c.getString(nicknameIdx);
				int bankId = c.getInt(bankIdx);

				Bundle args = new Bundle();
				args.putInt(Constants.ACCOUNT_ID_NUM, accountId);
				
				Fragment fragmentTab = new AccountActivityTab();
				fragmentTab.setArguments(args);
				
		        // Set Tab Icon and Titles
				tab = actionBar.newTab().setText(name);
				tab.setTag(new AccountBasics(accountId, bankId));	// set a/c basics as tag

		        // Set Tab Listeners
				tab.setTabListener(new TabListener(fragmentTab));
			} while(c.moveToNext());
		}
		c.close();
		return tab;
	}
	
	/**
	 * Find the tab index of the specified account.
	 * @param accountId	- Account ID to find tab index of
	 * @return			tab index or -1 if not found
	 */
	private int getTabIndex(long accountId) {

		int index = -1;

		final int N = actionBar.getTabCount();

		for ( int i = 0; i < N; ++i ) {
			ActionBar.Tab tab = actionBar.getTabAt(i);
			if ( accountId == ((AccountBasics)tab.getTag()).accountId ) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	
	/**
	 * Setup the cancel button in this activity
	 */
	private void setupCancelButton() {
		
		buttonCancel.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.account, menu);
		return true;
	}
	


	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent i;
		AccountBasics accInfo = (AccountBasics)actionBar.getSelectedTab().getTag();
		int itemId = item.getItemId();

		switch (itemId) {
			case R.id.action_EditBank:
				i = new Intent(this, AddBankActivity.class);
				i.putExtra(Constants.BANK_ID_NUM, accInfo.bankId);
				item.setIntent(i);
				break;
			case R.id.action_AddTransaction:
				i = addEditTransaction(SHOW_ADD_TRANSACTION, accInfo.accountId);
				item.setIntent(i);
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	/**
	 * Create the intent to launch a add/edit transaction activity
	 * @param request	- request code for activity; SHOW_ADD_TRANSACTION or SHOW_EDIT_TRANSACTION
	 * @param id		- item id; request == SHOW_ADD_TRANSACTION => account, request == SHOW_EDIT_TRANSACTION => transaction
	 * @return
	 */
	private Intent addEditTransaction(int request, long id) {
		Intent i = new Intent(this, AddTransactionActivity.class);
		if ( request == SHOW_ADD_TRANSACTION )
			i.putExtra(Constants.ACCOUNT_ID_NUM, id);
		else
			i.putExtra(Constants.TRANSACTION_ID_NUM, id);
		return i;
	}
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case SHOW_ADD_TRANSACTION:
			case SHOW_EDIT_TRANSACTION:
				if (resultCode == Activity.RESULT_OK) {
					// extract account num
					if ( data != null ) {
						Bundle b = data.getExtras();
						if ( b != null && b.containsKey(Constants.ACCOUNT_ID_ARRAY) ) {
							long[] accounts = b.getLongArray(Constants.ACCOUNT_ID_ARRAY);

							if ( accounts != null ) {
								for ( int i = accounts.length - 1; i >= 0; --i ) {
									int index = getTabIndex(accounts[i]);
		
									ActionBar.Tab tab = createAccountTab((int) accounts[i]);
									if ( tab != null ) {
								        // Remove previous and add updated tab to actionbar
										if ( index >= 0 ) {
											actionBar.removeTabAt(index);
											actionBar.addTab(tab, index, true);
										}
										else {
											actionBar.addTab(tab, true);
										}
									}
								}
							}
						}
					}
				}
				break;
		}

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		sendBroadcast(new Intent(Constants.UPDATE_SNAPSHOT_ACTION));
		super.onPause();
	}
	
	
	private class AccountBasics {
		int accountId;
		int bankId;
		/**
		 * @param accountId
		 * @param bankId
		 */
		public AccountBasics(int accountId, int bankId) {
			super();
			this.accountId = accountId;
			this.bankId = bankId;
		}
	}

	
	/**
	 * @see ie.ibuttimer.widget.AlertMessage.AlertMessageListener#onAlertMessageClick(android.app.DialogFragment, int, long)
	 */
	@Override
	public void onAlertMessageClick(DialogFragment dialog, int which, long response) {

		/* this handles the selection from the edit or delete alert after clicking on a
		 * transaction details on the account tab.
		 * the response code is the transaction id */
		
		switch ( which ) {
			case AlertMessage.BUTTON_LEFT:	// edit
				Intent intent = addEditTransaction(SHOW_EDIT_TRANSACTION, response);
				startActivityForResult(intent, SHOW_EDIT_TRANSACTION);
				break;
			case AlertMessage.BUTTON_MIDDLE:	// delete
				ContentResolver cr = getContentResolver();
				Transaction trans = new Transaction().loadFromProvider(cr, response);
				if ( trans.deleteTransactionGroupFromProvider(cr, response) ) {
					Intent fakeRes = new Intent();	// intent to return updated a/c
					long[] accounts = trans.getAffectedAccountIds(cr);
					fakeRes.putExtra(Constants.ACCOUNT_ID_ARRAY, accounts);

					// update users about the delete
					SmsProcessor smsManager = SmsProcessor.getInstance(getApplicationContext());
					smsManager.updateUsers(new Transaction[] { trans }, SmsProcessor.SmsReport.SMS_DELETE_REPORT, cr);

					onActivityResult(SHOW_EDIT_TRANSACTION, Activity.RESULT_OK, fakeRes);
				}
				break;
			/* no need to do anything for 
			case AlertMessage.BUTTON_RIGHT:	// cancel
			as AlertMessage dismisses itself after this callback */
		}

	}
	
}
