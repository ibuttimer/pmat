package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.db.AccountCurrency;
import ie.ibuttimer.pmat.db.AccountType;
import ie.ibuttimer.pmat.db.Bank;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.util.DateTimeFormat;
import ie.ibuttimer.pmat.util.DeviceConfiguration;
import ie.ibuttimer.pmat.util.Logger;
import ie.ibuttimer.widget.AmountEditText;
import ie.ibuttimer.widget.SelectDateFragment;
import ie.ibuttimer.widget.TextViewAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.SQLException;

public class AddAccountActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

	// widget variables and other related variables 
	private EditText editTextName;
	
	private EditText editTextNickname;
	
	private GregorianCalendar accountDate = null;

	
	private Spinner spinnerType;
	private ArrayList<AccountType> accTypes = new ArrayList<AccountType>();
	private TextViewAdapter<AccountType> accTypeAdapter;
	private AccountType accType;
	
	private Spinner spinnerCurrency;
	private ArrayList<AccountCurrency> currencies = new ArrayList<AccountCurrency>();
	private TextViewAdapter<AccountCurrency> currencyAdapter;
	private AccountCurrency currentCurrency;
	
	private Spinner spinnerBank;
	private ArrayList<Bank> banks = new ArrayList<Bank>();
	private TextViewAdapter<Bank> bankAdapter;
	private Bank currentBank;

	private TextView textViewDate;
	
	private AmountEditText editTextInitialBal;

	private LinearLayout layoutCreditLimit;
	private AmountEditText editTextCreditLimit;

	private LinearLayout layoutOverdraftLimit;
	private AmountEditText editTextOverdraftLimit;
	
	private Button buttonSave;
	private Button buttonCancel;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_account);

		// Show the Up button in the action bar.
		setupActionBar();

		// get references to the activity views
		editTextName = (EditText)this.findViewById(R.id.addAccount_editTextName);
		editTextNickname = (EditText)this.findViewById(R.id.addAccount_editTextNickname);
		spinnerType = (Spinner)this.findViewById(R.id.addAccount_spinnerType);
		spinnerCurrency = (Spinner)this.findViewById(R.id.addAccount_spinnerCurrency);
		spinnerBank = (Spinner)this.findViewById(R.id.addAccount_spinnerBank);
		textViewDate = (TextView)this.findViewById(R.id.addAccount_textViewDate);
		editTextInitialBal = (AmountEditText)this.findViewById(R.id.addAccount_editTextInitialBal);
		editTextCreditLimit = (AmountEditText)this.findViewById(R.id.addAccount_editTextCreditLimit);
		layoutCreditLimit = (LinearLayout)this.findViewById(R.id.addAccount_layoutCredit);
		editTextOverdraftLimit  = (AmountEditText)this.findViewById(R.id.addAccount_editTextOverdraftLimit);
		layoutOverdraftLimit = (LinearLayout)this.findViewById(R.id.addAccount_layoutOverdraft);
		buttonSave = (Button)this.findViewById(R.id.addAccount_buttonSave);
		buttonCancel = (Button)this.findViewById(R.id.addAccount_buttonCancel);

		// setup the activity views
		setupLayout();
		setupAccountTypeSpinner();
		setupCurrencySpinner();
		setupBankSpinner();
		setupInitialBalanceEditText();
		setupCreditLimitEditText();
		setupOverdraftLimitEditText();
		
		setupDateButton();
		setupSaveButton();
		setupCancelButton();
	}

	
	/**
	 * Setup the account type spinner in this activity
	 */
	private void setupAccountTypeSpinner() {
		
		accTypes = AccountType.loadAccountTypesFromProvider(getContentResolver());

		accTypeAdapter = new TextViewAdapter<AccountType>(this, 
				R.layout.text_view_adapter_item, R.id.text_view_adapter_item_textViewItem, accTypes);
		spinnerType.setAdapter(accTypeAdapter);

		spinnerType.setOnFocusChangeListener( focusChangeListener );

		// create a listener to retrieve the selected currency
		spinnerType.setOnItemSelectedListener(new OnItemSelectedListener () {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// An item was selected, retrieve the selected item
				accType = (AccountType) parent.getItemAtPosition(position);

				// enable/disable optional fields 
				enableOptionalFields();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nop
			}
			
		});
		
		// get the initial currency selection
		accType = (AccountType) spinnerType.getSelectedItem();

		// enable/disable optional fields 
		enableOptionalFields();
	}
	
	
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if ( !hasFocus ) {
				// enable/disable optional fields 
				enableOptionalFields();
			}
		}
	};
	
	
	/**
	 * Setup the currency spinner in this activity
	 */
	private void setupCurrencySpinner() {

		// populate currency spinner
		currencies = AccountCurrency.loadCurrenciesFromProvider(getContentResolver());
		
		// sort list
		if ( currencies.size() > 0 )
			Collections.sort(currencies, new CompareAccountCurrency());

		currencyAdapter = new TextViewAdapter<AccountCurrency>(this, 
				R.layout.text_view_adapter_item, R.id.text_view_adapter_item_textViewItem, currencies);
		spinnerCurrency.setAdapter(currencyAdapter);
		
		spinnerCurrency.setOnFocusChangeListener( focusChangeListener );
		
		// create a listener to retrieve the selected currency
		spinnerCurrency.setOnItemSelectedListener(new OnItemSelectedListener () {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// An item was selected, retrieve the selected item
				currentCurrency = (AccountCurrency) parent.getItemAtPosition(position);
				
				// set minor units on amount entry fields 
				setAmountsMinorUnits(currentCurrency.getMinorUnits());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nop
			}
			
		});
		
		// get the initial currency selection
		currentCurrency = (AccountCurrency) spinnerCurrency.getSelectedItem();

		// set minor units on amount entry fields 
		setAmountsMinorUnits(currentCurrency.getMinorUnits());
	}


	/**
	 * Setup the bank spinner in this activity
	 */
	private void setupBankSpinner() {

		// populate currency spinner
		banks = Bank.loadBanksFromProvider(getContentResolver());

		bankAdapter = new TextViewAdapter<Bank>(this, 
				R.layout.text_view_adapter_item, R.id.text_view_adapter_item_textViewItem, banks);
		spinnerBank.setAdapter(bankAdapter);
		
		spinnerBank.setOnFocusChangeListener( focusChangeListener );
		
		// create a listener to retrieve the selected currency
		spinnerBank.setOnItemSelectedListener(new OnItemSelectedListener () {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// An item was selected, retrieve the selected item
				currentBank = (Bank) parent.getItemAtPosition(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nop
			}
			
		});
		
		// get the initial currency selection
		currentBank = (Bank) spinnerBank.getSelectedItem();
	}

	/**
	 * Set the minor units for amount entry fields
	 */
	private void setAmountsMinorUnits( int minorUnits ) {
		editTextInitialBal.setMinorUnits(minorUnits);
		editTextCreditLimit.setMinorUnits(minorUnits);
		editTextOverdraftLimit.setMinorUnits(minorUnits);
	}
	
	/**
	 * Setup the layout of this activity
	 */
	private void setupLayout() {

		if ( DeviceConfiguration.isLargeScreen(getApplicationContext()) ) {
			int[] layouts = new int[] {
					R.id.addAccount_layoutName,
					R.id.addAccount_layoutNickname,
					R.id.addAccount_layoutType,
					R.id.addAccount_layoutCurrency,
					R.id.addAccount_layoutBank,
					R.id.addAccount_layoutInitBal,
					R.id.addAccount_layoutCredit,
					R.id.addAccount_layoutOverdraft,
			};
			for ( int i = layouts.length - 1; i >= 0; --i ) { 
				LinearLayout layout = (LinearLayout)findViewById(layouts[i]);
				layout.setOrientation(LinearLayout.HORIZONTAL);
			}
		}
	}
	

	/**
	 * Setup the initial balance edit text in this activity
	 */
	private void setupInitialBalanceEditText() {

		// add a text changed listener to ensure that the correct number of minor units is entered
		editTextInitialBal.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	// update save button state
	        	buttonSave.setEnabled( allRequiredDataEntered() );
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
	}

	/**
	 * Setup the credit limit edit text in this activity
	 */
	private void setupCreditLimitEditText() {

		// add a text changed listener to ensure that the correct number of minor units is entered
		editTextCreditLimit.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	// update save button state
	        	buttonSave.setEnabled( allRequiredDataEntered() );
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
	}
	

	/**
	 * Setup the overdraft limit edit text in this activity
	 */
	private void setupOverdraftLimitEditText() {

		// add a text changed listener to ensure that the correct number of minor units is entered
		editTextOverdraftLimit.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	// update save button state
	        	buttonSave.setEnabled( allRequiredDataEntered() );
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
	}
	
	/**
	 * Setup the save button in this activity
	 */
	private void setupSaveButton() {
		
    	// update save button state
    	buttonSave.setEnabled( allRequiredDataEntered() );

		buttonSave.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// add the new account
				ContentResolver cr = getContentResolver();
				ContentValues values = new ContentValues();
				AccountType type = (AccountType)spinnerType.getSelectedItem();
				String balance = editTextInitialBal.getText().toString().trim();
				
				values.put(DatabaseManager.ACCOUNT_NAME, editTextName.getText().toString().trim());
				values.put(DatabaseManager.ACCOUNT_NICKNAME, editTextNickname.getText().toString().trim());
				values.put(DatabaseManager.ACCOUNT_TYPE, type.getId());
				values.put(DatabaseManager.ACCOUNT_CURRENCY, currentCurrency.getNumber());
				values.put(DatabaseManager.ACCOUNT_BANK, currentBank.getId());
				values.put(DatabaseManager.ACCOUNT_DATE, DatabaseManager.makeDatabaseTimestamp(getAccountDate()));
				values.put(DatabaseManager.ACCOUNT_INITBAL, balance);
				values.put(DatabaseManager.ACCOUNT_CURRENTBAL, balance);
				values.put(DatabaseManager.ACCOUNT_AVAILBAL, balance);

				switch ( accType.getLimit() ) {
					case AccountType.LIMIT_CREDIT:
						values.put(DatabaseManager.ACCOUNT_LIMIT, editTextCreditLimit.getText().toString().trim());
						break;
					case AccountType.LIMIT_OVERDRAFT:
						values.put(DatabaseManager.ACCOUNT_LIMIT, editTextOverdraftLimit.getText().toString().trim());
						break;
					default:
						break;
				}

				try {
					cr.insert(DatabaseManager.ACCOUNT_ACC_URI, values);

					setResult(Activity.RESULT_OK);
				}
				catch ( SQLException e ) {
					Logger.d("Unable to add " + values.toString());

					setResult(Activity.RESULT_CANCELED);
				}
				
				finish();
			}
			
		});
		
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
	 * Setup the date button in this activity
	 */
	private void setupDateButton() {
		
		OnClickListener dateListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new SelectDateFragment();
				Bundle b = new Bundle();
				b.putInt(SelectDateFragment.TITLE, R.string.addaccount_date);
				b.putSerializable(SelectDateFragment.DATE, getAccountDate());
				newFragment.setArguments(b);
				newFragment.show(getFragmentManager(), "datePicker");
			}
		};
		
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.addAccount_layoutDate);
		layout.setOnClickListener( dateListener );

		ImageButton buttonDate = (ImageButton)this.findViewById(R.id.addAccount_buttonDate);
		buttonDate.setOnClickListener( dateListener );

		textViewDate.setOnClickListener( dateListener );
	}
	
	/**
	 * Verify if all the required data has been entered
	 * @return	<code>true</code> is all the required data has been entered, <code>false</code> otherwise.
	 */
	private boolean allRequiredDataEntered() {

		boolean allOK = false;

		if ( (editTextName.getText().length() > 0) &&
				(editTextNickname.getText().length() > 0) && 
				(editTextInitialBal.getText().length() > 0) ) {
			switch ( accType.getLimit() ) {
				case AccountType.LIMIT_CREDIT:
					if ( editTextCreditLimit.getText().length() > 0 )
						allOK = true;
					break;
				case AccountType.LIMIT_OVERDRAFT:
					if ( editTextOverdraftLimit.getText().length() > 0 )
						allOK = true;
					break;
				default:
					allOK = true;
					break;
			}
		}

		return allOK;
	}
	
	/**
	 * Enable/disable optional fields based on selections made
	 */
	private void enableOptionalFields() {

		int creditVisibility;
		int overdraftVisibility;
		
		switch ( accType.getLimit() ) {
			case AccountType.LIMIT_CREDIT:
				creditVisibility = View.VISIBLE;
				overdraftVisibility = View.GONE;
				break;
			case AccountType.LIMIT_OVERDRAFT:
				creditVisibility = View.GONE;
				overdraftVisibility = View.VISIBLE;
				break;
			default:
				creditVisibility = View.GONE;
				overdraftVisibility = View.GONE;
				break;
		}
		layoutCreditLimit.setVisibility(creditVisibility);
		layoutOverdraftLimit.setVisibility(overdraftVisibility);
		
//		textViewCreditLimit.setVisibility(creditVisibility);
//		editTextCreditLimit.setVisibility(creditVisibility);
//		textViewOverdraftLimit.setVisibility(overdraftVisibility);
//		editTextOverdraftLimit.setVisibility(overdraftVisibility);

    	// update save button state
    	buttonSave.setEnabled( allRequiredDataEntered() );
	}
	
	
	/**
	 * Compare AccountCurrency objects based on follow order of precedence:<br>
	 * <ol>
	 * <li>default currency for current locale</li>
	 * <li>code alphabetic order</li>
	 * </ol> 
	 * @author Ian Buttimer
	 *
	 */
	private class CompareAccountCurrency implements Comparator<AccountCurrency> {

		// TODO add default currency to settings
		private String defaultCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();

		@Override
		public int compare(AccountCurrency lhs, AccountCurrency rhs) {

			if ( lhs.equals(rhs) )
				return 0;		// equal
			if ( defaultCode.compareToIgnoreCase(lhs.getCode()) == 0 )
				return (-1);	// lhs first as its the default currency
			if ( defaultCode.compareToIgnoreCase(rhs.getCode()) == 0 )
				return (1);		// rhs first as its the default currency
			// compare alphabetically
			return lhs.getCode().compareToIgnoreCase(rhs.getCode());
		}
			
	};

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_account, menu);
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
	 * Get the open date of this account
	 * @return
	 */
	public GregorianCalendar getAccountDate() {
		GregorianCalendar date;
		if ( accountDate == null ) {
			date = (GregorianCalendar) Calendar.getInstance();	// Use the current time as the default
			date.set(Calendar.HOUR_OF_DAY, 0);
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.SECOND, 0);
		}
		else
			date = (GregorianCalendar) accountDate.clone();
		return date;
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		if ( accountDate == null )
			accountDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
		else
			accountDate.set(year, monthOfYear, dayOfMonth);

		// update date display with selected date
		DateTimeFormat df = new DateTimeFormat(this);
		Date date = accountDate.getTime();
		textViewDate.setText(df.formatMediumDate(date));
	}

}
