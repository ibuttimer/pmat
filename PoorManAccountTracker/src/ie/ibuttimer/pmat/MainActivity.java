package ie.ibuttimer.pmat;

import java.util.ArrayList;

import ie.ibuttimer.pmat.db.AccountCurrency;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.DatabaseManager.CreationStep;
import ie.ibuttimer.pmat.util.AudioPlayer;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.widget.AccountBalanceView;
import ie.ibuttimer.widget.MultiTextViewAdapter;
import ie.ibuttimer.widget.MultiTextViewAdapterBase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The main activity class for the application.
 * @author Ian Buttimer
 *
 */
public class MainActivity extends BaseActivity {

	private ListView listViewAccounts;
	MultiTextViewAdapter<MultiTextViewAdapterBase> accountsAdapter;
	private ArrayList<MultiTextViewAdapterBase> accountInfo;

	/** Array of all account ids */
	private int[] accountIdArray;

	private AnimationDrawable juggleAnimation;
	private TextView textViewNoAccount;
	private AccountBalanceView balanceView;

	/** Class to provide control of shortcut buttons */
	private class ShortcutButtons {
		public int[] resourceIds;
		public int[] menuIds;
		public Button[] buttons;
		/**
		 * @param resource
		 * @param menu
		 */
		public ShortcutButtons(int[] resourceIds, int[] menuIds) {
			super();
			this.resourceIds = resourceIds;
			this.menuIds = menuIds;
		}
		/**
		 * Setup the buttons
		 * @param a
		 */
		public void setupButtons(Activity a) {
			final int N = resourceIds.length;
			buttons = new Button[N];
			for ( int i = 0; i < N; ++i ) {
				buttons[i] = (Button) a.findViewById(resourceIds[i]);
				buttons[i].setTag(Integer.valueOf( menuIds[i] ));
				buttons[i].setOnClickListener( shortcutListener );
			}
		}
		/**
		 * Return a button
		 * @param resource	- resource id of button to return
		 * @return
		 */
		public Button getButton(int resource) {
			Button button = null;
			for ( int i = resourceIds.length - 1; i >= 0; --i ) {
				if ( resourceIds[i] == resource ) {
					button = buttons[i];
					break;
				}
			}
			return button;
		}
		/**
		 * Set a button to be enabled/disabled 
		 * @param resource	- resource id of button to set
		 * @param enabled	- enabled state
		 */
		public void setEnabled(int resource, boolean enabled) {
			Button button = getButton(resource);
			if ( button !=  null )
				button.setEnabled(enabled);
		}
		/**
		 * Set all buttons to be enabled/disabled 
		 * @param enabled	- enabled state
		 */
		public void setEnabled(boolean enabled) {
			for ( int i = resourceIds.length - 1; i >= 0; --i )
				buttons[i].setEnabled(enabled);
		}
		/**
		 * Set all buttons to be enabled/disabled depending on whether that actions are allowed 
		 */
		public void setEnabledByAction() {
			for ( int i = resourceIds.length - 1; i >= 0; --i )
				buttons[i].setEnabled(isMenuActionAllowed(menuIds[i]));
		}
		/**
		 * Set the enabled state of all buttons to match that of the appropriate action in the a menu
		 * @param menu	- menu whose state to mirror
		 */
		public void mirrorMenu(Menu menu) {
			for ( int i = menuIds.length - 1; i >= 0; --i ) {
				MenuItem item = menu.findItem(menuIds[i]);
				boolean enabled = (item != null && item.isEnabled());
				buttons[i].setEnabled(enabled);
			}
		}
	}
	private final ShortcutButtons shortcuts = new ShortcutButtons(
										new int[] { R.id.main_buttonAddTransaction,
													R.id.main_buttonAddAccount,
													R.id.main_buttonAddBank,
													R.id.main_buttonAddSms,
													R.id.main_buttonAddUser,
										}, 
										dataBaseMenuActions
	);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		switch ( DatabaseManager.getDatabaseState() ) {
			case DATABASE_NON_EXISTENT:
			case DATABASE_EXISTS:
				// database needs creating/populating with default entries
				new PopulateDatabaseTask(DatabaseManager.POPULATE_URI,
											R.string.progress_initing_database).execute(DatabaseManager.POPULATE_URI_STR);
				break;
			case DATABASE_NEEDS_UPGRADE:
				// database needs upgrade
				new PopulateDatabaseTask(DatabaseManager.UPGRADE_URI,
											R.string.progress_upgrading_database).execute(DatabaseManager.UPGRADE_URI_STR);
				break;
			default:
				// database ok
				break;
		}
		
		// get references to view items
		listViewAccounts = (ListView)this.findViewById(R.id.main_listViewAccounts);

		// setup accounts list view
		accountInfo = new ArrayList<MultiTextViewAdapterBase>();
		accountsAdapter = new MultiTextViewAdapter<MultiTextViewAdapterBase>(this, 
									R.layout.activity_main_account_view, new int[] {
										R.id.accountView_textViewName,
										R.id.accountView_textViewBal
									}, 
									accountInfo, 0);
		listViewAccounts.setAdapter(accountsAdapter);
		
		listViewAccounts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

				AudioPlayer.playButtonClick(getApplicationContext());

				// launch account activity
				Intent i = new Intent(getBaseContext(), AccountActivity.class);

				i.putExtra(Constants.ACCOUNT_ID_NUM, (int)accountInfo.get((int)id).getId());
				if ( accountIdArray.length > 1 )
					i.putExtra(Constants.ACCOUNT_ID_ARRAY, accountIdArray);

				startActivityForResult(i, SHOW_ACCOUNT);
			}
		 });
		
		textViewNoAccount = (TextView)findViewById(R.id.main_textViewNoAccounts);
		balanceView = (AccountBalanceView)findViewById(R.id.main_accountBalanceHeader);
		textViewNoAccount.setVisibility(View.GONE);
		balanceView.setVisibility(View.GONE);
		
		// setup shortcut buttons
		shortcuts.setupButtons(this);
		shortcuts.setEnabled(false);
	}


	
	/**
	 * Listener for shortcut buttons
	 */
	OnClickListener shortcutListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// launch activity
			processMenuItem(((Integer)v.getTag()).intValue(), null);
		}
	};

	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();

		ImageView juggleImage = (ImageView) findViewById(R.id.main_imageView);
		if ( PreferenceControl.isAnimationEnabled(getApplicationContext()) ) {
			juggleImage.setVisibility(View.VISIBLE);
			juggleImage.setBackgroundResource(R.drawable.juggle);
			juggleAnimation = (AnimationDrawable) juggleImage.getBackground();
			juggleAnimation.start();
		}
		else {
			juggleAnimation = (AnimationDrawable) juggleImage.getBackground();
			if ( juggleAnimation != null )
				juggleAnimation.stop();
			juggleImage.setVisibility(View.GONE);
		}
	}





	/**
	 * Load the current account snapshots from the content provider
	 */
	private void loadAccountSnapshotFromProvider() { 

		// execute in an async task
		new LoadAccountSnapshotTask().execute(DatabaseManager.ACCOUNT_SNAPSHOT_URI);
	}

	
	/**
	 * AsyncTask to populate the database
	 * @author Ian Buttimer
	 */
	private class PopulateDatabaseTask extends AsyncTask<String, ProgressUpdate, View[]> {
		
		Uri uriBase;		// uri to call first
		int resourceId;		// resource id of message to display
		
		/**
		 * Constructor
		 * @param uri			- Uri to call
		 * @param resourceId	- resource id of string to display in progress dialog
		 */
		public PopulateDatabaseTask(Uri uriBase, int resourceId) {
			super();
			this.uriBase = uriBase;
			this.resourceId = resourceId;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		protected View[] doInBackground(String... method) {
			ContentResolver cr = getContentResolver();
			Resources r = getResources();
			View[] result = null;

			publishProgress(new ProgressUpdate(MIN_PROGRESS));

			Bundle b = cr.call(uriBase, method[0], null, null);
			if ( b != null ) {
				CreationStep[] steps = (CreationStep[]) b.getParcelableArray(DatabaseManager.DB_CONTROL_STEPS);
				if ( steps != null ) {
					final int N = steps.length;
					if ( N > 0 ) {
						final int incPerStep = (MAX_PROGRESS - MIN_PROGRESS) / N;
						
						for ( int u = 0; u < N; ++u ) {
							
							int progress = incPerStep * u;

							Uri callUri = Uri.parse( steps[u].uri );
							
							publishProgress(new ProgressUpdate(progress, r.getString(steps[u].resID)));

							cr.call(callUri, steps[u].uri, null, null);
						}
					}
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
			initProgress(resourceId);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute (View[] result) {
			super.onPostExecute(result);

			// clear progress dialog
			clearProgress();

			switch ( DatabaseManager.getDatabaseState() ) {
				case DATABASE_OK:
					loadAccountSnapshotFromProvider();

					// update shortcuts & menu state
					shortcuts.setEnabledByAction();
					invalidateOptionsMenu();
					break;
				default:
					// problem with database do nothing
					break;
			}

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
	 * AsyncTask to load the account snapshot from the database
	 * @author Ian Buttimer
	 */
	private class LoadAccountSnapshotTask extends AsyncTask<Uri, ProgressUpdate, MultiTextViewAdapterBase[]> {
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		protected MultiTextViewAdapterBase[] doInBackground(Uri... uri) {
			ContentResolver cr = getContentResolver();
			MultiTextViewAdapterBase[] result = null;

			final int incPerUri = (MAX_PROGRESS - MIN_PROGRESS) / uri.length;

			publishProgress(new ProgressUpdate(MIN_PROGRESS));

			for ( int u = 0; u < uri.length; ++u ) {
				
				int progress = incPerUri * u;
				publishProgress(new ProgressUpdate(progress));

				Cursor c = cr.query(uri[u], null, null, null, null);
				if (c.moveToFirst()) {
					int idIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_ID);
					int nameIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_NAME);
					int balanceIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_AVAILBAL);
					int currencyIdx = c.getColumnIndex(DatabaseManager.CURRENCY_CODE);
					
					int i = c.getCount();
					result = new MultiTextViewAdapterBase[i];

					int incPerRec = incPerUri / i;	// progress inc per record

					i = 0;
					do {

						// Escape early if cancel() is called
						if (isCancelled())
							break;

						// Extract the details.
						int id = c.getInt(idIdx);
						String name = c.getString(nameIdx);
						Double balance = c.getDouble(balanceIdx);
						String currency = c.getString(currencyIdx);
	
						String[] strings = new String[] {
							name,
							AccountCurrency.formatDouble(currency, balance, true)
						};
						result[i] = new MultiTextViewAdapterBase(id, strings);
						
						if ( balance < 0.0 ) {
							String colour = getResources().getString(R.color.sysRed);
							result[i].setColour(1, colour);
						}
						
						++i;

						progress += incPerRec;
						publishProgress(new ProgressUpdate(progress, name));
						
					} while(c.moveToNext());
				}
				c.close();
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
			initProgress(R.string.progress_loading_snapshot);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute (MultiTextViewAdapterBase[] result) {
			super.onPostExecute(result);

			if ( result != null ) {
				balanceView.setVisibility(View.VISIBLE);
				textViewNoAccount.setVisibility(View.GONE);

				// Clear the existing list and add results
				final int N = result.length;
	
				accountIdArray = new int[N];
	
				accountInfo.clear();
				for ( int i = 0; i < N; ++i ) {
					accountInfo.add(result[i]);
					accountIdArray[i] = (int) result[i].getId();
				}
				accountsAdapter.notifyDataSetChanged();
			}
			else {
				balanceView.setVisibility(View.GONE);
				textViewNoAccount.setVisibility(View.VISIBLE);
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	/* (non-Javadoc)
	 * @see ie.ibuttimer.pmat.BaseActivity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		boolean result = super.onPrepareOptionsMenu(menu);
		shortcuts.mirrorMenu(menu);

		return result;
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		return super.onOptionsItemSelected(item);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case SHOW_SETTINGS:
			case SHOW_ADD_BANK:
			case SHOW_ACCOUNT:
				if (resultCode == Activity.RESULT_OK) {
					;
				}
				break;
			case SHOW_ADD_ACCOUNT:
			case SHOW_ADD_TRANSACTION:
				if (resultCode == Activity.RESULT_OK) {
					// no need done in onResume()
//					loadAccountSnapshotFromProvider();
				}
				break;
		}
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		switch ( DatabaseManager.getDatabaseState() ) {
			case DATABASE_OK:
				loadAccountSnapshotFromProvider();
				break;
			default:
				// problem with database do nothing
				break;
		}
		
		// update shortcuts & menu state
		shortcuts.setEnabledByAction();
		invalidateOptionsMenu();

		IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UPDATE_SNAPSHOT_ACTION);
        registerReceiver(receiver, filter);
        super.onResume();
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		sendBroadcast(new Intent(Constants.UPDATE_SNAPSHOT_ACTION));
		super.onPause();
	}
	
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
    		if ( Constants.UPDATE_SNAPSHOT_ACTION.equals(intent.getAction()) ) {
    			loadAccountSnapshotFromProvider();
    		}
        }
    };

	
}
