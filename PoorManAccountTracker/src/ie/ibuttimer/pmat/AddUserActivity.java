package ie.ibuttimer.pmat;

import java.util.ArrayList;

import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.User;
import ie.ibuttimer.pmat.util.AudioPlayer;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.pmat.util.DeviceConfiguration;
import ie.ibuttimer.pmat.util.Logger;
import ie.ibuttimer.widget.AddPhoneNumber;
import ie.ibuttimer.widget.ButteredToast;
import ie.ibuttimer.widget.MultiTextViewAdapter;
import ie.ibuttimer.widget.MultiTextViewAdapterBase;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.provider.ContactsContract;

public class AddUserActivity extends ActionBarActivity {

	private static final int USER_NUMBER = 0;

	private AddPhoneNumber addNumberPhone;
	private ImageButton buttonContact;
	private EditText editTextName;
	private EditText editTextNumber;
	private TextView textViewExisting;
	private Button buttonCancel;
	private Button buttonSave;
	
	private User userOriginal;

	private ListView listViewUsers;
	MultiTextViewAdapter<MultiTextViewAdapterBase> usersAdapter;
	private ArrayList<MultiTextViewAdapterBase> userInfo;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_user);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.addUser_Container, new PlaceholderFragment()).commit();
		}
		
		userOriginal = null;
		getArguments(savedInstanceState);
	}

	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();

		// need to wait until the fragment has been created to get references to its views
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.addUser_Container);
		editTextName = (EditText) fragment.getView().findViewById(R.id.addUser_editTextName);
		addNumberPhone = (AddPhoneNumber) fragment.getView().findViewById(R.id.addUser_NumberPhone);
		editTextNumber = (EditText) addNumberPhone.findViewById(R.id.addPhoneNumber_editTextNumber);
		textViewExisting = (TextView) fragment.getView().findViewById(R.id.addUser_Existing);
		listViewUsers = (ListView) fragment.getView().findViewById(R.id.addUser_listView);
		buttonContact = (ImageButton) addNumberPhone.findViewById(R.id.addPhoneNumber_imageButtonContacts);
		buttonCancel = (Button) fragment.getView().findViewById(R.id.addUser_buttonCancel);
		buttonSave = (Button) fragment.getView().findViewById(R.id.addUser_buttonSave);
		
		// setup accounts list view
		userInfo = new ArrayList<MultiTextViewAdapterBase>();
		usersAdapter = new MultiTextViewAdapter<MultiTextViewAdapterBase>(this, 
									R.layout.add_user_user_summary_view, new int[] {
										R.id.addUser_userName,
										R.id.addUser_userPhone
									}, 
									userInfo, 0);
		listViewUsers.setAdapter(usersAdapter);

		listViewUsers.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

				AudioPlayer.playButtonClick(getApplicationContext());

				// launch account activity
				userOriginal = new User(getContentResolver(), (int)userInfo.get((int)id).getId()); 
				setActivityFields(userOriginal);
			}
		 });

		
		// setup the activity views
		setupLayout();
		setupContactButton();
		setupEditTexts();
		setupSaveButton();
		setupCancelButton();
		
		populateUsers();
	}


	/**
	 * Retrieve the arguments from the intent that started this activity 
	 * @param b
	 */
	private void getArguments(Bundle b) {

		if ( b == null )
			return;

		if ( b.containsKey(Constants.USER_ID_NUM) ) {
			int userId = b.getInt(Constants.USER_ID_NUM);

			// retrieve info from database
			userOriginal = new User().loadFromProvider(getContentResolver(), userId);
			if ( userOriginal != null ) {
				setActivityFields(userOriginal);

				setTitle(R.string.title_activity_edit_user);
			}
		}
	}

	
	/**
	 * Retrieve the list of existing users from the database
	 */
	private void populateUsers() {
		
		userInfo.clear();

		ArrayList<User> users = new User().loadFromProviderExcluding(getContentResolver(), User.DEFAULT_PHONE_NUMBER);
		for ( User usr : users ) {
			String[] strings = new String[] {
					usr.getUserName(),
					usr.getUserPhone(),
				};
			userInfo.add( new MultiTextViewAdapterBase(usr.getUserId(), strings) );
		}
		
		textViewExisting.setText(userInfo.size() > 0 ? R.string.add_user_existing : R.string.add_user_no_existing);

		usersAdapter.notifyDataSetChanged();
	}
	
	
	/**
	 * Set the EditText text
	 * @param editText
	 * @param text
	 */
	private void setEditText(EditText editText, String text) {
		
		if ( TextUtils.isEmpty(text) )
			text = "";
		editText.setText(text);
	}
	
	/**
	 * Set the activity fields based on the specified object
	 * @param bank
	 */
	private void setActivityFields(User user) {
		
		if ( user != null ) {
			setEditText(editTextName, user.getUserName());
			setEditText(editTextNumber, user.getUserPhone());
		}
		else {
			setEditText(editTextName, "");
			setEditText(editTextNumber, "");
		}
		setSaveButtonState();
	}

	/**
	 * Set the object fields based on the activity fields
	 * @param bank
	 */
	private void setUserFields(User user) {

		user.setUserName(editTextName.getText().toString());
		user.setUserPhone(editTextNumber.getText().toString());
	}


	
	
	
	/**
	 * Setup the layout of this activity
	 */
	private void setupLayout() {

		if ( DeviceConfiguration.isLargeScreen(getApplicationContext()) ) {
			int[] layouts = new int[] {
					R.id.addUser_layoutName,
			};
			for ( int i = layouts.length - 1; i >= 0; --i ) { 
				LinearLayout layout = (LinearLayout)findViewById(layouts[i]);
				layout.setOrientation(LinearLayout.HORIZONTAL);
			}
		}
	}



	/**
	 * Enable/disable optional fields based on selections made
	 */
	private void enableOptionalFields() {

		setSaveButtonState();
	}

	
	/**
	 * Do everything that needs to be done when the user moves focus
	 */
	private void handleFocusChange() {
		
		enableOptionalFields();
	}
	
	/**
	 * Setup the contacts buttons
	 */
	private void setupContactButton() {

		// add listener to launch select contact activity
		buttonContact.setTag(Integer.valueOf(USER_NUMBER));
		buttonContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				handleFocusChange();	// handle focus change

				Uri uri = SelectContactActivity.SELECT_CONTACT_PHONE_URI;
				Intent intent = new Intent(Intent.ACTION_PICK, uri);
				startActivityForResult(intent, ((Integer)v.getTag()).intValue());
			}
		});
	}

	/**
	 * Does necessary updates when a view loses focus
	 */
	private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if ( !hasFocus ) {
				handleFocusChange();	// handle focus change
			}
		}
	};

	private TextWatcher onTextChangeListener = new TextWatcher() {
        public void afterTextChanged(Editable s) {
			handleFocusChange();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
    };  
	
	/**
	 * Setup the edit texts in this activity
	 */
	private void setupEditTexts() {

		// add lost focus listener
		EditText[] editTextViews = new EditText[] {
				editTextName, editTextNumber
		};
		for ( int i = editTextViews.length - 1; i >= 0; --i ) {
			editTextViews[i].setOnFocusChangeListener(onFocusChangeListener);
			editTextViews[i].addTextChangedListener(onTextChangeListener);
		}
	}
	
	
	/**
	 * Show a toast
	 * @param resId
	 * @param duration
	 */
	private void showToast(int resId, int duration) {
		ButteredToast.makeText(this, R.string.add_user_unable_to_commit, Toast.LENGTH_LONG).show();
	}
	

	
	/**
	 * Setup the save button in this activity
	 */
	private void setupSaveButton() {
		
    	// update save button state
		setSaveButtonState();

		buttonSave.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {

				ContentValues values = new ContentValues(); 
				ContentResolver cr = getContentResolver();

				int resultCode = Activity.RESULT_CANCELED;

				if ( userOriginal != null ) {
					// check if there has been any changes
					User userEdit = (User) userOriginal.clone();
					setUserFields(userEdit);
					
					if ( !userEdit.equals(userOriginal) ) {
						// need to update
						for ( int i = 0; i < 2; ++i ) {
							String original = null;
							String edit = null;
							String field = null;
							switch ( i ) {
								case 0:
									original = userOriginal.getUserName();
									edit = userEdit.getUserName();
									field = DatabaseManager.USER_NAME;
									break;
								case 1:
									original = userOriginal.getUserPhone();
									edit = userEdit.getUserPhone();
									field = DatabaseManager.USER_PHONE;
									break;
							}
							if ( !original.equals(edit) )
								values.put(field, edit);
						}
						
						resultCode = Activity.RESULT_CANCELED;
						try {
							String selection = "(" + DatabaseManager.USER_ID + "=?)";
							String[] selectionArgs = new String[] {	Integer.toString((int) userOriginal.getUserId()) };

							int result = cr.update(DatabaseManager.USER_URI, values, selection, selectionArgs);
							if ( result > 0 )
								resultCode = Activity.RESULT_OK;
						}
						catch ( SQLException e ) {
							Logger.d("Unable to add " + values.toString());
						}
					}
				}
				else {
					// add the new user
					String str = editTextName.getText().toString();
					if ( !TextUtils.isEmpty(str) )
						values.put(DatabaseManager.USER_NAME, str);
					
					str = editTextNumber.getText().toString();
					if ( !TextUtils.isEmpty(str) )
						values.put(DatabaseManager.USER_PHONE, str);

					// TODO just for now add fake pass/challenge/response 
					values.put(DatabaseManager.USER_PASS, "pass");
					values.put(DatabaseManager.USER_CHALLENGE, "hey what's up?");
					values.put(DatabaseManager.USER_RESPONSE, "nothing");

					resultCode = Activity.RESULT_CANCELED;
					try {
						Uri result = cr.insert(DatabaseManager.USER_URI, values);
						if ( result != null )
							resultCode = Activity.RESULT_OK;
					}
					catch ( SQLException e ) {
						Logger.d("Unable to add " + values.toString());

						showToast(R.string.add_user_unable_to_commit, Toast.LENGTH_LONG);
					}
				}

				// update users
				userOriginal = null;
				setActivityFields( null );
				if ( resultCode == Activity.RESULT_OK )
					populateUsers();
				
				// return result
//				setResult(resultCode);
//				finish();
			}
		});
	}
	
	
	/**
	 * Verify if data has been entered in an EditText
	 * @param editText	- EditText to check
	 * @return	<code>true</code> or <code>false</code>.
	 */
	private boolean dataEntered(EditText editText) {
		return !TextUtils.isEmpty(editText.getText().toString());
	}

	/**
	 * Verify if all the required data has been entered
	 * @return	<code>true</code> is all the required data has been entered, <code>false</code> otherwise.
	 */
	private boolean allRequiredDataEntered() {

		boolean allOK = false;

		if ( dataEntered(editTextName) && dataEntered(editTextNumber) ) {
			allOK = true;
		}

		return allOK;
	}

	private void setSaveButtonState() {
    	// update button state
    	buttonSave.setEnabled( allRequiredDataEntered() );
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

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case USER_NUMBER:
				if (resultCode == Activity.RESULT_OK) {
					// set edit text to selected number
					Uri contactData = data.getData();
					editTextNumber.setText(contactData.getLastPathSegment());

					String name = data.getStringExtra(ContactsContract.Contacts.DISPLAY_NAME);
					if ( !TextUtils.isEmpty(name) )
						editTextName.setText(name);
				}
				break;
		}

	}

	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_user, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_add_user,
					container, false);
			return rootView;
		}
	}

}
