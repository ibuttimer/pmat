package ie.ibuttimer.pmat;

import java.util.ArrayList;
import java.util.HashMap;

import ie.ibuttimer.pmat.db.Bank;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.TextTemplate;
import ie.ibuttimer.pmat.sms.SmsMessageFactory;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.pmat.util.DeviceConfiguration;
import ie.ibuttimer.pmat.util.Logger;
import ie.ibuttimer.widget.SmsTemplateField;
import ie.ibuttimer.widget.TextViewAdapter;
import ie.ibuttimer.widget.TextViewAdapterBase;
import ie.ibuttimer.widget.TextViewAdapterInterface;
import ie.ibuttimer.widget.SmsTemplateField.OnRemoveSmsFieldListener;
import ie.ibuttimer.widget.SmsTemplateField.OnSmsFieldMoveSelectedListener;
import ie.ibuttimer.widget.SmsTemplateField.OnSmsFieldTypeSelectedListener;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class CreateSmsTemplateActivity extends BaseActivity {

	private EditText editTextName;
	private Spinner spinnerSmsType;
	private Spinner spinnerBank;
	private TextView textViewExample;
	private LinearLayout layoutFields;
	private Button buttonCancel;
	private Button buttonClear;
	private Button buttonSave;
	
	private ArrayList<Bank> banks = new ArrayList<Bank>();
	private Bank bankSelected;
	private String newBankName;
	private TextTemplate templateSelected;
	
	private ArrayList<GenericIdName> smsTypes = new ArrayList<GenericIdName>();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_sms_template);
		
		
		editTextName = (EditText)this.findViewById(R.id.createSmsTemplate_editTextName);
		spinnerSmsType = (Spinner)this.findViewById(R.id.createSmsTemplate_spinnerSmsType);
		spinnerBank = (Spinner)this.findViewById(R.id.createSmsTemplate_spinnerBank);
		TextView textViewBank = (TextView)this.findViewById(R.id.createSmsTemplate_textViewBank);
		
		textViewExample = (TextView)this.findViewById(R.id.createSmsTemplate_textViewExample);
		layoutFields = (LinearLayout)this.findViewById(R.id.createSmsTemplate_layoutFields);

		buttonClear = (Button)this.findViewById(R.id.createSmsTemplate_buttonClear);
		buttonCancel = (Button)this.findViewById(R.id.createSmsTemplate_buttonCancel);
		buttonSave = (Button)this.findViewById(R.id.createSmsTemplate_buttonSave);
		
		// setup text type list
		loadSmsTypesFromProvider();
		TextViewAdapter<GenericIdName> smsTypesAdapter = new TextViewAdapter<GenericIdName>(this, 
				R.layout.text_view_adapter_item, R.id.text_view_adapter_item_textViewItem, smsTypes);
		spinnerSmsType.setAdapter(smsTypesAdapter);

		
		
		if ( savedInstanceState == null )
			savedInstanceState = getIntent().getExtras();
		getArguments(savedInstanceState);

		// setup banks list
		if ( !TextUtils.isEmpty(newBankName) ) {
			// name of new bank supplied
			spinnerBank.setVisibility(View.GONE);
			textViewBank.setText(getResources().getString(R.string.createSmsTemplate_bank) + newBankName);
		}
		else {
			// bank is already selected or may be selected 
			banks = Bank.loadBanksFromProvider(getContentResolver());
			banks.add(0, new Bank(-1, getResources().getString(R.string.createSmsTemplate_templateSelectBank)) );

			TextViewAdapter<Bank> banksAdapter = new TextViewAdapter<Bank>(this, 
					R.layout.text_view_adapter_item, R.id.text_view_adapter_item_textViewItem, banks);
			spinnerBank.setAdapter(banksAdapter);
			
			textViewBank.setVisibility(View.GONE);
		}
		
		if ( bankSelected != null ) {
			// bank is already selected 
			long bankId = bankSelected.getId();
			for ( int i = banks.size() - 1; i >= 0; --i ) {
				Bank chk = banks.get(i);
				if ( chk.getId() == bankId ) {
					spinnerBank.setSelection(i);
					spinnerBank.setEnabled(false);
					break;
				}
			}
		}

		
		setTitle(templateSelected != null ? R.string.title_activity_edit_sms_template : 
												R.string.title_activity_create_sms_template);

		setupLayout();
		setupCancelButton();
		setupSaveButton();
		
		enableOptionalFields();
	}
	
	
	/**
	 * Retrieve the arguments from the intent that started this activity 
	 * @param b
	 */
	private void getArguments(Bundle b) {

		if ( b == null )
			return;

		if ( b.containsKey(Constants.BANK_ID_NUM) ) {
			// bank id supplied 
			int bankId = b.getInt(Constants.BANK_ID_NUM);

			// retrieve info from database
			bankSelected = Bank.loadBankFromProvider(getContentResolver(), bankId);
		}
		else if ( b.containsKey(Constants.BANK_ID_NAME) ) {
			// bank name supplied i.e. hasn't been created yet 
			newBankName = b.getString(Constants.BANK_ID_NAME);
		}
		
		if ( b.containsKey(DatabaseManager.TEXTTRANS_ID) ) {
			// text trans id supplied so retrieve info from database
			long id = b.getLong(DatabaseManager.TEXTTRANS_ID);
			templateSelected = TextTemplate.loadFromProvider(getContentResolver(), id);

			// fill in name
			editTextName.setText(templateSelected.getName());
			
			// set type
			long typeId = templateSelected.getTypeId();
			for ( int i = smsTypes.size() - 1; i >= 0; --i ) {
				GenericIdName type = smsTypes.get(i);
				if ( type.getId() == typeId ) {
					spinnerSmsType.setSelection(i);
					break;
				}
			}
			
			// add the fields
			SmsMessageFactory sms = new SmsMessageFactory( templateSelected.getPrototype() );

			int[] fields = sms.getFields();
			String[] fieldValues = sms.getFieldValues();
			final int N = fields.length;
			
			for ( int i = 0; i < N; ++i ) {
				SmsTemplateField fld = addNewTemplateField();
				
				HashMap<String,String> info = new HashMap<String,String>();
				
				info.put(SmsTemplateField.FIELD_TYPE_KEY, String.valueOf(fields[i]));
				if ( fields[i] == SmsMessageFactory.SMS_TEMPLATE_FIELD_TEXT )
					info.put(SmsTemplateField.FIELD_VALUE_KEY, fieldValues[i]);

				fld.setFieldInfo(info);
			}
		}
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_sms_template, menu);
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
	 * Add a new field
	 * @param v		View that was clicked
	 */
	public void addNewField(View v) {

		addNewTemplateField();
	}
	
	/**
	 * Add a new field
	 */
	public SmsTemplateField addNewTemplateField() {
		
		SmsTemplateField field = new SmsTemplateField( this );

		layoutFields.addView(field);
		field.setOnRemoveSmsFieldListener(removeFieldListener);
		field.setOnSmsFieldTypeSelectedListener(fieldTypeSelectedListener);
		field.setOnSmsFieldMoveSelectedListener(fieldMoveSelectedListener);

		enableOptionalFields();
		
		return field;
	}
	
	/**
	 * Remove all fields
	 * @param v		View that was clicked
	 */
	public void clearAllFields(View v) {
		
		layoutFields.removeAllViews();

		enableOptionalFields();
	}
	
	/**
	 * Sets the up/down button states
	 */
	private void setUpDownButtonState() {

		final int N = layoutFields.getChildCount();
		if ( N > 0 ) {
			for ( int i = 0; i < N; ++i ) {
				SmsTemplateField field = (SmsTemplateField) layoutFields.getChildAt(i);
				int index;
				if ( N == 1 )
					index = SmsTemplateField.INDEX_ONLY;
				else if ( i == 0 )
					index = SmsTemplateField.INDEX_FIRST;
				else if ( i == (N - 1) )
					index = SmsTemplateField.INDEX_LAST;
				else
					index = SmsTemplateField.INDEX_MIDDLE;
				field.setIndex(index);
			}
		}
	}

	
	/**
	 * Verify if all the required data has been entered
	 * @return	<code>true</code> is all the required data has been entered, <code>false</code> otherwise.
	 */
	private boolean allRequiredDataEntered() {

		boolean allOK = false;
		final int N = layoutFields.getChildCount();

		if ( N > 0 ) {
			
			if ( editTextName.getText().toString().length() > 0 ) {		// text entered?
				
				if ( spinnerSmsType.getSelectedItemPosition() > 0 ) {	// type other than 1st (i.e. prompt) selected?
				
					if ( (spinnerBank.getVisibility() == View.GONE) ||		// not selecting bank
							(spinnerBank.getSelectedItemPosition() > 0) ) {	// bank other than 1st (i.e. prompt) selected?
					
						allOK = true;
						for ( int i = 0; i < N; ++i ) {
							SmsTemplateField field = (SmsTemplateField) layoutFields.getChildAt(i);
							if ( field.isAllDataEntered() == false ) {
								allOK = false;
								break;
							}
						}
					}
				}
			}
		}

		return allOK;
	}

	
	/**
	 * Create a new SmsMessageFactory from the current activity fields
	 * @param amount	- Amount
	 * @param account	- Account name
	 * @return
	 */
	private SmsMessageFactory createSmsFactory(String amount, String account) {
		
		final int N = layoutFields.getChildCount();
		int[] fields = new int[N];
		String[] fieldValues = new String[N];

		for ( int i = 0; i < N; ++i ) {
			SmsTemplateField field = (SmsTemplateField) layoutFields.getChildAt(i);
			HashMap<String,String> fieldInfo = field.getFieldInfo();
			
			if ( fieldInfo.containsKey(SmsTemplateField.FIELD_TYPE_KEY) ) {
				fields[i] = Integer.parseInt( fieldInfo.get(SmsTemplateField.FIELD_TYPE_KEY) );
				
				switch ( fields[i] ) {
					case SmsMessageFactory.SMS_TEMPLATE_FIELD_TEXT:
						fieldValues[i] = fieldInfo.get(SmsTemplateField.FIELD_VALUE_KEY);
						break;
					case SmsMessageFactory.SMS_TEMPLATE_FIELD_AMOUNT:
						fieldValues[i] = amount;
						break;
					case SmsMessageFactory.SMS_TEMPLATE_FIELD_ACCOUNT:
						fieldValues[i] = account;
						break;
				}
			}
		}
		
    	return new SmsMessageFactory(fields, fieldValues);
	}
	
	
	/**
	 * Enable/disable optional fields based on selections made
	 */
	private void enableOptionalFields() {

		final int F = layoutFields.getChildCount();

		layoutFields.setVisibility(F > 0 ? View.VISIBLE : View.GONE);
		
    	// update clear button state
    	buttonClear.setEnabled( F > 0 );

    	// update save button state
    	boolean allOk = allRequiredDataEntered();
    	buttonSave.setEnabled( allOk );
    	
    	// update up/down button state
    	setUpDownButtonState();
    	
    	// update example
    	Resources r = getResources();
		String na = r.getString(R.string.createSmsTemplate_example_na);
    	if ( allOk ) {
	    	SmsMessageFactory sms = createSmsFactory("100", r.getString(R.string.createSmsTemplate_example_account));
    		int[] fields = sms.getFields();
    		final int N = fields.length;
    		int[] styles = new int[N];

			for ( int i = 0; i < N; ++i ) {
				switch ( fields[i] ) {
					case SmsMessageFactory.SMS_TEMPLATE_FIELD_TEXT:
						styles[i] = android.graphics.Typeface.NORMAL;
						break;
					case SmsMessageFactory.SMS_TEMPLATE_FIELD_AMOUNT:
						styles[i] = android.graphics.Typeface.ITALIC;
						break;
					case SmsMessageFactory.SMS_TEMPLATE_FIELD_ACCOUNT:
						styles[i] = android.graphics.Typeface.ITALIC;
						break;
				}
			}
			
	    	Bundle b = sms.generateSmsBundle();
	    	String smsText = b.getString(SmsMessageFactory.SMS_TEMPLATE_GENERATED_STRING);
	    	if ( smsText == null )
	    		smsText = na;
	    	
	    	SpannableString styledText = new SpannableString(smsText + " ");

	    	if ( b.containsKey(SmsMessageFactory.SMS_TEMPLATE_STRING_OFFSETS) ) {

	    		int[] offsets = b.getIntArray(SmsMessageFactory.SMS_TEMPLATE_STRING_OFFSETS);
				for ( int i = 0; i < N; ++i ) {
					 int end;
					 if ( (i + 1) < N )
						 end = offsets[i+1] - 1;
					 else
						 end = smsText.length();
					 styledText.setSpan(new StyleSpan(styles[i]), offsets[i], end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
	    	}
	    	textViewExample.setText(styledText, TextView.BufferType.SPANNABLE);
    	}
    	else
	    	textViewExample.setText(na);

    	
	}

    
	/**
	 * Setup the save button in this activity
	 */
	private void setupSaveButton() {
		
		buttonSave.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// add the new template
				ContentResolver cr = getContentResolver();
				ContentValues values = new ContentValues();								// basic transaction 
				
				// template name
				values.put(DatabaseManager.TEXTTRANS_NAME, editTextName.getText().toString().trim());
				
				// template type
				values.put(DatabaseManager.TEXTTRANS_TYPE, spinnerSmsType.getSelectedItemId());
				
				// bank
				if ( spinnerBank.getVisibility() != View.GONE )
					values.put(DatabaseManager.TEXTTRANS_BANK, spinnerBank.getSelectedItemId());

				// template prototype
				values.put(DatabaseManager.TEXTTRANS_PROTO, createSmsFactory(null, null).generateSmsTemplate().trim());

				if ( TextUtils.isEmpty(newBankName) ) {
					// adding/updating template for existing bank
					int result = Activity.RESULT_CANCELED;
					if ( templateSelected == null ) {
						try {
							Uri insResult = cr.insert(DatabaseManager.TEXTTRANS_URI, values);
							if ( insResult != null )
								result = Activity.RESULT_OK;
						}
						catch ( SQLException e ) {
							Logger.d("Unable to add " + values.toString());
						}
					}
					else {
						try {
							TextTemplate update = TextTemplate.createTextTemplateFromValues(values);
							update.setId(templateSelected.getId());
							if ( !update.equals(templateSelected) ) {
								if ( update.updateInProvider(getContentResolver(), values) )
									result = Activity.RESULT_OK;
							}
						}
						catch ( SQLException e ) {
							Logger.d("Unable to update " + values.toString());
						}
					}
					setResult(result);
				}
				else {
					/* adding template for a new bank, so return details so it can be added to database 
					 * after the bank has been created */
					Intent data = new Intent(Intent.ACTION_INSERT, DatabaseManager.TEXTTRANS_URI);
					data.putExtra(DatabaseManager.TEXTTRANS_TABLE, values);
					setResult(Activity.RESULT_OK, data);
				}
				finish();
			}
			
		});
		
	}

	
	/**
	 * Setup the layout of this activity
	 */
	private void setupLayout() {

		if ( DeviceConfiguration.isLargeScreen(getApplicationContext()) ) {
			int[] layouts = new int[] {
					R.id.createSmsTemplate_layoutName,
			};
			for ( int i = layouts.length - 1; i >= 0; --i ) { 
				LinearLayout layout = (LinearLayout)findViewById(layouts[i]);
				layout.setOrientation(LinearLayout.HORIZONTAL);
			}
		}
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

	
	private SmsTemplateField findParent(View v) {

		View viewRoot = v.getRootView();
		View viewParent = v;
		/// find the SmsTemplateField parent
		while ( viewParent != viewRoot ) {
			if ( viewParent instanceof SmsTemplateField  ) {
				return (SmsTemplateField) viewParent;
			}
			else
				viewParent = (View) viewParent.getParent();
		}
		return null;
	}
	
	
	/**
	 * Listener to remove individual fields
	 */
	private OnRemoveSmsFieldListener removeFieldListener = new OnRemoveSmsFieldListener() {

		@Override
		public void onRemoveSmsField(View v) {
			
			SmsTemplateField parent = findParent(v);
			if ( parent != null )
				layoutFields.removeView(parent);
		}
	};
	
    
	/**
	 * Listener called when field type is selected
	 */
	private OnSmsFieldTypeSelectedListener fieldTypeSelectedListener = new OnSmsFieldTypeSelectedListener() {

		@Override
		public void onFieldTypeSelected(View v) {
			enableOptionalFields();
		}
	};
	

	/**
	 * Listener called when field is moved
	 */
	private OnSmsFieldMoveSelectedListener fieldMoveSelectedListener = new OnSmsFieldMoveSelectedListener() {

		@Override
		public void onFieldMoveSelected(View v, int move) {

			int moveIndex;
			if ( move == SmsTemplateField.INDEX_MOVE_UP )
				moveIndex = -1;
			else if ( move == SmsTemplateField.INDEX_MOVE_DOWN )
				moveIndex = 1;
			else
				return;
			
			SmsTemplateField mover = findParent(v);
			if ( mover != null ) {
				int index = layoutFields.indexOfChild(mover);

				moveIndex += index;

				if ( (moveIndex >= 0) && (moveIndex < layoutFields.getChildCount()) ) {
					SmsTemplateField moving = (SmsTemplateField) layoutFields.getChildAt(moveIndex);
					
					HashMap<String,String> moverInfo = mover.getFieldInfo();
					HashMap<String,String> movingInfo = moving.getFieldInfo();

					mover.setFieldInfo(movingInfo);
					moving.setFieldInfo(moverInfo);
					
				}
			}
		}
	};


	/**
	 * Get list of sms types from ContentProvider
	 */
	private void loadSmsTypesFromProvider() { 
		 
		smsTypes.clear();		// Clear the existing array

		// add select prompt
		smsTypes.add( new GenericIdName(-1, getResources().getString(R.string.createSmsTemplate_templateSelectType)) );

		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(DatabaseManager.TEXTTYPES_URI, null, null, null, null);	// Return all the database text types
		if (c.moveToFirst()) {
			int idIdx = c.getColumnIndex(DatabaseManager.TEXTTYPES_ID);
			int nameIdx = c.getColumnIndex(DatabaseManager.TEXTTYPES_NAME);
			do {
				// Extract the details.
				int id = c.getInt(idIdx);
				String name = c.getString(nameIdx);

				smsTypes.add( new GenericIdName(id, name) );
			} while(c.moveToNext());
		}
		c.close();
	}
	

	private class GenericIdName extends TextViewAdapterBase implements TextViewAdapterInterface {

		/* fields stored in super class:
		 * - id
		 * - name
		 *  */

		/**
		 * @param id
		 * @param name
		 */
		public GenericIdName(int id, String name) {
			super(id, name);
		}
	}

}
