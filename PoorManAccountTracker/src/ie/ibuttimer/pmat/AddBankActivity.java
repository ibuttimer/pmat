package ie.ibuttimer.pmat;

import java.util.ArrayList;

import ie.ibuttimer.pmat.db.Bank;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.TextTemplate;
import ie.ibuttimer.pmat.util.AudioPlayer;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.pmat.util.DeviceConfiguration;
import ie.ibuttimer.pmat.util.Logger;
import ie.ibuttimer.widget.AddPhoneNumber;
import ie.ibuttimer.widget.AlertMessage;
import ie.ibuttimer.widget.AlertMessage.AlertMessageListener;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddBankActivity extends BaseActivity implements AlertMessageListener {

	/* indices of items in arrays:
	 * contact numbers need to come first followed by strings just for programming convenience
	 */
	public static final int PHONE_BANK_IDX = 0;
	public static final int TEXT_BANK_IDX = 1;
	public static final int LOCAL_NUM_IDX = 2;
	public static final int AWAY_NUM_IDX = 3;
	public static final int NUM_PICK_CONTACTS = 4;
	
	public static final int BANK_NAME_IDX = 4;
	public static final int BANK_ADDR_IDX = 5;
	public static final int NUM_EDITTEXTS = 6;
	
	private static final int ADD_TEMPLATE_REQ = 10;
	private static final int EDIT_TEMPLATE_REQ = 11;
	
	private static final int[] editTextIds = new int[] {
			R.id.addBank_addBankNumberPhone,
			R.id.addBank_addBankNumberText,
			R.id.addBank_addBankNumberLocal,
			R.id.addBank_addBankNumberAway,
			
			R.id.addBank_editTextName,
			R.id.addBank_editTextAddress,
	};
	private static final String[] addNumDbFields = new String[] {
			DatabaseManager.BANK_PHONE_BANK_NUM,
			DatabaseManager.BANK_TEXT_BANK_NUM,
			DatabaseManager.BANK_LOCAL_SERVICE_NUM,
			DatabaseManager.BANK_AWAY_SERVICE_NUM,

			DatabaseManager.BANK_NAME,
			DatabaseManager.BANK_ADDR,
	};

	// widget variables and other related variables
	private AddPhoneNumber[] addNumberPhone = new AddPhoneNumber[NUM_PICK_CONTACTS];
	private ImageButton[] buttonContacts = new ImageButton[NUM_PICK_CONTACTS];
	private EditText[] editTextViews = new EditText[NUM_EDITTEXTS];
	private Button buttonCancel;
	private Button buttonSave;
	private ImageButton buttonAddTemplate;

	private Bank bankOriginal = null;

	private LinearLayout layoutTemplates;
	private TextView textViewTemplates;


	private ArrayList<Intent> postSaveIntents = new ArrayList<Intent>();
	
	private ArrayList<TextTemplate> templates = new ArrayList<TextTemplate>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_bank);
		
		// get references to the activity views
		int i;
		for ( i = 0; i < NUM_PICK_CONTACTS; ++i ) {
			addNumberPhone[i] = (AddPhoneNumber) findViewById(editTextIds[i]);
			editTextViews[i] = (EditText)addNumberPhone[i].findViewById(R.id.addPhoneNumber_editTextNumber);
			buttonContacts[i] = (ImageButton)addNumberPhone[i].findViewById(R.id.addPhoneNumber_imageButtonContacts);
		}
		while ( i < NUM_EDITTEXTS ) {
			editTextViews[i] = (EditText)findViewById(editTextIds[i]);
			++i;
		}
		buttonCancel = (Button)this.findViewById(R.id.addBank_buttonCancel);
		buttonSave = (Button)this.findViewById(R.id.addBank_buttonSave);
		buttonAddTemplate = (ImageButton)this.findViewById(R.id.addBank_buttonAddTemplate);
		
		layoutTemplates = (LinearLayout) findViewById(R.id.addBank_listViewTemplates);
		textViewTemplates = (TextView) findViewById(R.id.addBank_textViewTemplate);

		if ( savedInstanceState == null )
			savedInstanceState = getIntent().getExtras();
		
		bankOriginal = null;	// default to new mode
		getArguments(savedInstanceState);
		
		// add templates list
		loadTemplates(null);
		displayTemplates();
		
		// setup the activity views
		setupLayout();
		setupContactButtons();
		setupEditTexts();
		setupTemplateButton();
		setupSaveButton();
		setupCancelButton();
	}

	
	/**
	 * Retrieve the arguments from the intent that started this activity 
	 * @param b
	 */
	private void getArguments(Bundle b) {

		if ( b == null )
			return;

		if ( b.containsKey(Constants.BANK_ID_NUM) ) {
			int bankId = b.getInt(Constants.BANK_ID_NUM);

			// retrieve info from database
			bankOriginal = Bank.loadBankFromProvider(getContentResolver(), bankId);
			if ( bankOriginal != null ) {
				setActivityFields(bankOriginal);

				setTitle(R.string.title_activity_edit_bank);
			}
		}
	}

	
	/**
	 * Load text templates from the database
	 * @param add	- Optional TextTemplate to add to list
	 */
	private void loadTemplates(TextTemplate add) {
		
		if ( bankOriginal != null ) {
			templates = TextTemplate.loadTextTemplatesFromProvider(getContentResolver(), (int) bankOriginal.getId());
		}
		if ( add != null )
			templates.add(add);
		displayTemplates();
	}

	/**
	 * Display the template header text
	 */
	private void displayTemplateHeader() {
		textViewTemplates.setText(templates.size() > 0 ? R.string.add_bank_templates : R.string.add_bank_no_templates);
	}
	
	/**
	 * Display the template list
	 */
	private void displayTemplates() {
		
		layoutTemplates.removeAllViews();
		final int N = templates.size();
		
		displayTemplateHeader();

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for ( int i = 0; i < N; i++ ) {
			TextTemplate template = templates.get(i);
			
			inflater.inflate(R.layout.text_view_adapter_item, layoutTemplates, true);
			View v = layoutTemplates.getChildAt(i);
			
			v.setTag(Integer.valueOf(i));	// set tag as item index
			
			TextView tv = (TextView) v.findViewById(R.id.text_view_adapter_item_textViewItem);
			tv.setText(template.getName());
			
			v.setOnClickListener(templateClickListener);
		}
	}
	
	/**
	 * Delete text template from the database
	 * @param index	- index of TextTemplate to delete from list
	 */
	private void deleteTemplate(int index) {
		if ( index >= 0  && index < templates.size() ) {
			if ( bankOriginal != null ) {
				// bank exists already, so text trans needs to be removed from database
				templates.get(index).deleteFromProvider(getContentResolver());
			}

			templates.remove(index);
			
			layoutTemplates.removeViewAt(index);
			
			displayTemplateHeader();
		}
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
	private void setActivityFields(Bank bank) {
		
		setEditText(editTextViews[BANK_NAME_IDX], bank.getName());
		setEditText(editTextViews[BANK_ADDR_IDX], bank.getAddress());
		setEditText(editTextViews[PHONE_BANK_IDX], bank.getPhoneBankNum());
		setEditText(editTextViews[TEXT_BANK_IDX], bank.getTextBankNum());
		setEditText(editTextViews[LOCAL_NUM_IDX], bank.getLocalServiceNum());
		setEditText(editTextViews[AWAY_NUM_IDX], bank.getAwayServiceNum());
	}
	
	/**
	 * Set the object fields based on the activity fields
	 * @param bank
	 */
	private void setBankFields(Bank bank) {

		bank.setName(editTextViews[BANK_NAME_IDX].getText().toString());
		bank.setAddress(editTextViews[BANK_ADDR_IDX].getText().toString());
		bank.setPhoneBankNum(editTextViews[PHONE_BANK_IDX].getText().toString());
		bank.setTextBankNum(editTextViews[TEXT_BANK_IDX].getText().toString());
		bank.setLocalServiceNum(editTextViews[LOCAL_NUM_IDX].getText().toString());
		bank.setAwayServiceNum(editTextViews[AWAY_NUM_IDX].getText().toString());
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_bank, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see ie.ibuttimer.pmat.BaseActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	
	
	/**
	 * Verify if data has been entered in an EditText
	 * @param index	- Index of EditText
	 * @return	<code>true</code> or <code>false</code>.
	 */
	private boolean dataEntered(int index) {
		return (editTextViews[index].getText().length() > 0);
	}

	
	/**
	 * Verify if all the required data has been entered
	 * @return	<code>true</code> is all the required data has been entered, <code>false</code> otherwise.
	 */
	private boolean allRequiredDataEntered() {

		boolean allOK = false;

		if ( dataEntered(BANK_NAME_IDX) ) {
			allOK = true;
		}

		return allOK;
	}

	

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case PHONE_BANK_IDX:
			case TEXT_BANK_IDX:
			case LOCAL_NUM_IDX:
			case AWAY_NUM_IDX:
				if (resultCode == Activity.RESULT_OK) {
					// set edit text to selected number
					Uri contactData = data.getData();
					editTextViews[requestCode].setText(contactData.getLastPathSegment());
				}
				break;
			case ADD_TEMPLATE_REQ:
			case EDIT_TEMPLATE_REQ:
				if (resultCode == Activity.RESULT_OK) {
					// save to implement after bank has been created
					TextTemplate add = null;
					if ( data != null ) {
						postSaveIntents.add(data);
						
						/* add new template to list
						 * NOTE: its still only a local object it hasn't been added to the database
						 */
						ContentValues values = getAddTemplateValues(data);
						if ( values != null ) {
							add = TextTemplate.createTextTemplateFromValues(values);
						}
					}
					loadTemplates(add);
				}
				break;
		}

	}


	/**
	 * Enable/disable optional fields based on selections made
	 */
	private void enableOptionalFields() {

		setSaveButtonState();
		setTemplateButtonState();
	}

	
	/**
	 * Do everything that needs to be done when the user moves focus
	 */
	private void handleFocusChange() {
		
		enableOptionalFields();
	}

	
	/**
	 * Setup the layout of this activity
	 */
	private void setupLayout() {

		if ( DeviceConfiguration.isLargeScreen(getApplicationContext()) ) {
			int[] layouts = new int[] {
					R.id.addBank_layoutName,
			};
			for ( int i = layouts.length - 1; i >= 0; --i ) { 
				LinearLayout layout = (LinearLayout)findViewById(layouts[i]);
				layout.setOrientation(LinearLayout.HORIZONTAL);
			}
		}
	}

	
	/**
	 * Launch the select contacts activity 
	 * @param mode	Mode to return with activity result
	 */
	OnClickListener launchSelectContactListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			handleFocusChange();	// handle focus change

			Uri uri = SelectContactActivity.SELECT_CONTACT_PHONE_URI;
			Intent intent = new Intent(Intent.ACTION_PICK, uri);
			startActivityForResult(intent, ((Integer)v.getTag()).intValue());
		}
	};

	
	/**
	 * Setup the contacts buttons
	 */
	private void setupContactButtons() {

		// add listener to launch select contact activity
		for ( int i = 0 ; i < NUM_PICK_CONTACTS; ++i ) {
			buttonContacts[i].setTag(Integer.valueOf(i));
			buttonContacts[i].setOnClickListener(launchSelectContactListener);
		}
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
		for ( int i = 0; i < NUM_EDITTEXTS; ++i ) {
			editTextViews[i].setOnFocusChangeListener(onFocusChangeListener);
			editTextViews[i].addTextChangedListener(onTextChangeListener);
		}
	}
	
	
	/**
	 * Check is the intent represents an add text template action
	 * @param intent	- Intent to check
	 * @return
	 */
	private Uri isAddTemplateIntent(Intent intent) {
		Uri uri = intent.getData();
		
		if ( !uri.equals(DatabaseManager.TEXTTRANS_URI) ) {
			uri = null;
		}
		return uri;
	}
	
	/**
	 * Return the content values for an add text template action
	 * @param intent	- Intent to get vales from
	 * @return
	 */
	private ContentValues getAddTemplateValues(Intent intent) {
		ContentValues values = null; 
		Uri uri = isAddTemplateIntent(intent);
		if ( uri != null ) {
			values = intent.getParcelableExtra(DatabaseManager.TEXTTRANS_TABLE);
		}
		return values;
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

				if ( bankOriginal != null ) {
					// check if there has been any changes
					Bank bankEdit = (Bank) bankOriginal.clone();
					setBankFields(bankEdit);
					
					if ( !bankEdit.equals(bankOriginal) ) {
						// need to update
						for ( int i = 0; i < NUM_EDITTEXTS; ++i ) {
							String original = null;
							String edit = null;
							switch ( i ) {
								case PHONE_BANK_IDX:
									original = bankOriginal.getPhoneBankNum();
									edit = bankEdit.getPhoneBankNum();
									break;
								case TEXT_BANK_IDX:
									original = bankOriginal.getTextBankNum();
									edit = bankEdit.getTextBankNum();
									break;
								case LOCAL_NUM_IDX:
									original = bankOriginal.getLocalServiceNum();
									edit = bankEdit.getLocalServiceNum();
									break;
								case AWAY_NUM_IDX:
									original = bankOriginal.getAwayServiceNum();
									edit = bankEdit.getAwayServiceNum();
									break;
								case BANK_NAME_IDX:
									original = bankOriginal.getName();
									edit = bankEdit.getName();
									break;
								case BANK_ADDR_IDX:
									original = bankOriginal.getAddress();
									edit = bankEdit.getAddress();
									break;
							}
							if ( !original.equals(edit) )
								values.put(addNumDbFields[i], edit);
						}
						
						resultCode = Activity.RESULT_CANCELED;
						try {
							String selection = "(" + DatabaseManager.BANK_ID + "=?)";
							String[] selectionArgs = new String[] {	Integer.toString((int) bankOriginal.getId()) };

							int result = cr.update(DatabaseManager.BANKS_URI, values, selection, selectionArgs);
							if ( result > 0 )
								resultCode = Activity.RESULT_OK;
						}
						catch ( SQLException e ) {
							Logger.d("Unable to add " + values.toString());
						}

					}
				}
				else {
					// add the new bank
					for ( int i = 0; i < NUM_EDITTEXTS; ++i ) {
						String bankString = editTextViews[i].getText().toString();
						if ( !TextUtils.isEmpty(bankString) )
							values.put(addNumDbFields[i], bankString);
					}
					
					resultCode = Activity.RESULT_CANCELED;
					try {
						Uri result = cr.insert(DatabaseManager.BANKS_URI, values);
						if ( result != null )
							resultCode = Activity.RESULT_OK;
					}
					catch ( SQLException e ) {
						Logger.d("Unable to add " + values.toString());
					}
				}

				// do any post save actions
				if ( resultCode == Activity.RESULT_OK ) {
					final int N = postSaveIntents.size();
					for ( int i = 0; i < N; ++i ) {
						Intent intent = postSaveIntents.get(i);
						Uri uri = isAddTemplateIntent(intent);
						
						if ( uri != null ) {
							// add an sms template to the database
							values = getAddTemplateValues(intent);
							if ( values != null ) {
								try {
									Uri result = cr.insert(uri, values);
									if ( result == null )
										resultCode = Activity.RESULT_CANCELED;
								}
								catch ( SQLException e ) {
									Logger.d("Unable to add " + values.toString());
								}
							}
						}
					}
				}

				setResult(resultCode);
				finish();
			}
		});
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

	/**
	 * Setup the template button in this activity
	 */
	private void setupTemplateButton() {
		
    	// update button state
		setTemplateButtonState();
    	
		OnClickListener addTemplateListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				createEditTemplate( ADD_TEMPLATE_REQ, -1 );
			}
		};
		
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.addBank_layoutTemplateHeading);
		layout.setOnClickListener( addTemplateListener );

		buttonAddTemplate.setOnClickListener(addTemplateListener);
	}
	
	/**
	 * Set correct template button state
	 */
	private void setTemplateButtonState() {
    	// update button state
		buttonAddTemplate.setEnabled(dataEntered(BANK_NAME_IDX) && dataEntered(TEXT_BANK_IDX));
	}

	/**
	 * Launch the text template create/edit activity
	 * @param request		- request id
	 * @param templateId	- template id if editing
	 */
	private void createEditTemplate( int request, long templateId ) {
		Intent intent = new Intent(getBaseContext(), CreateSmsTemplateActivity.class);

		if ( bankOriginal != null )
			intent.putExtra(Constants.BANK_ID_NUM, (int)bankOriginal.getId());
		else
			intent.putExtra(Constants.BANK_ID_NAME, editTextViews[BANK_NAME_IDX].getText().toString());
		if( templateId > 0 )
			intent.putExtra(DatabaseManager.TEXTTRANS_ID, templateId);

		startActivityForResult(intent, request);
	}
	

	/**
	 * Click listener to display edit or delete alert
	 */
	private OnClickListener templateClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Context c = getApplicationContext();
			AudioPlayer.playButtonClick(c);

			int idx = ((Integer)v.getTag()).intValue();
			TextTemplate template = templates.get(idx);

			Bundle b = new Bundle();
			Resources r = c.getResources();
			b.putString(AlertMessage.TITLE_ARG, template.getName());
			b.putString(AlertMessage.MESSAGE_ARG, r.getString(R.string.add_bank_templateEditDelete));
			b.putString(AlertMessage.BUTTON_LEFT_ARG, r.getString(R.string.add_bank_templateEdit));
			b.putString(AlertMessage.BUTTON_RIGHT_ARG, r.getString(R.string.add_bank_templateDelete));
			b.putInt(AlertMessage.RESPONSE_ARG, idx);
			AlertMessage msg = new AlertMessage();
			msg.setArguments(b);
			msg.show(getFragmentManager(), "editOrDelete");
		}
	};
	
	
	/**
	 * @see ie.ibuttimer.widget.AlertMessage.AlertMessageListener#onAlertMessageClick(android.app.DialogFragment, int, long)
	 */
	@Override
	public void onAlertMessageClick(DialogFragment dialog, int which, long response) {

		switch ( which ) {
			case AlertMessage.BUTTON_LEFT:	// edit
				TextTemplate template = templates.get((int) response);
				createEditTemplate( EDIT_TEMPLATE_REQ, template.getId() );
				break;
			case AlertMessage.BUTTON_RIGHT:	// delete
				deleteTemplate((int) response);
				break;
		}
	}
	
}
