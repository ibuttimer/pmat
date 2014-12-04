package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.db.Category;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.Payee;
import ie.ibuttimer.pmat.db.Transaction;
import ie.ibuttimer.pmat.db.Transfer;
import ie.ibuttimer.pmat.sms.SmsProcessor;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.pmat.util.DateTimeFormat;
import ie.ibuttimer.pmat.util.Logger;
import ie.ibuttimer.widget.AmountEditText;
import ie.ibuttimer.widget.CategoryAllocation;
import ie.ibuttimer.widget.SelectDateFragment;
import ie.ibuttimer.widget.SelectTimeFragment;
import ie.ibuttimer.widget.TextViewAdapter;
import ie.ibuttimer.widget.TextViewAdapterBase;
import ie.ibuttimer.widget.TextViewAdapterInterface;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;


/**
 * This class provides transaction add/edit functionality.
 * @author Ian Buttimer
 *
 */
public class AddTransactionActivity extends FragmentActivity implements DatePickerDialog.OnDateSetListener, 
													TimePickerDialog.OnTimeSetListener, EditDoneInterface {

	/*
	 * Some notes about variable names in this class:
	 * - in order to enable the use of reflection to save and retrieve object from/to Bundles, all variable of 
	 *   a common type need to start with a common tag, and the Bundle key should be the variable name.  
	 */
	
	private static final boolean alwaysLoadDataFromProvider = true;	// flag to determine whether data is always loaded from the database or from a Bundle when available	
	
	
	// widget variables and other related variables 
	private AmountEditText editTextAmount;

	// incoming trans => to account, outgoing/transfer trans => from account
	private Spinner spinnerAccount;
	private ArrayList<TransactionAccount> accounts = new ArrayList<TransactionAccount>();
	private TextViewAdapter<TransactionAccount> accAdapter;
	private TransactionAccount account;			// transaction from account
	
	// transfer trans => to account
	private Spinner spinnerToAccount;
	private TransactionAccount accountTo;	// transaction to account

	private static final String BUNDLE_ACCOUNT_TAG = "account";		// tag that all TransactionAccount related items stored in a Bundle should start with	
	private static final String BUNDLE_ACCOUNT = BUNDLE_ACCOUNT_TAG;
	private static final String BUNDLE_ACCOUNT_TO = BUNDLE_ACCOUNT_TAG + "To";
	/* The following will be retrieved from the database:
	 * 	- accounts
	 */

	
	private TransactionAccount startAccount;	// initially selected account
	
	
	private Transaction editTransactionParent;			// parent transaction to edit
	private ArrayList<Transaction> editTransactionCategories;	// category transaction group to edit

	ArrayList<Long> updatedAccounts;			// accounts that were updated by the transaction, i.e they'll need to refresh 

	
	private RelativeLayout layoutToAccount;
	private TextView textViewRate;

	private RadioGroup radioType;
	private int radioCheckedType;
	private static final String BUNDLE_RADIO_TAG = "radio";		// tag that all RadioGroup related items stored in a Bundle should start with	
	private static final String BUNDLE_RADIO_TYPE = BUNDLE_RADIO_TAG + "CheckedType";

	private ImageButton buttonPayee;
	private TextView textViewPayee;

	private LinearLayout layoutRate;
	private EditText editTextRate;

	private ArrayList<Category> categoriesIncome;		// list of all income categories
	private ArrayList<Category> categoriesExpense;		// list of all expense categories
	private ArrayList<Category> categoriesTransfer;		// list of all transfer categories
	private ArrayList<Category> categoriesImbalance;	// list of all imbalance categories
	private ArrayList<Category> categoriesUnassigned;	// list of all unassigned categories

	private Category categoryUnassigned;				// unassigned category for selected account
	private Category categoryImbalance;					// imbalance category for selected account
	private Category categorySplit;						// split category
	
	private ArrayList<TransactionCategory> categoriesSelected;

	private static final String BUNDLE_CATEGORY_TAG = "categor";		// tag that all Category related items stored in a Bundle should start with	
	private static final String BUNDLE_CATEGORIES_SELECTED = BUNDLE_CATEGORY_TAG + "iesSelected";
	/* The following will be retrieved from the database:
	 * 	- categoriesIncome, categoriesExpense, categoriesTransfer, categoriesImbalance, categoriesUnassigned
	 * The following are always set by class methods:
	 * 	- categoryUnassigned, categoryImbalance, categorySplit
	 */
	
	private RelativeLayout layoutCategoryMain;
	private ImageButton buttonSetCategory;
	private TextView textViewCategory;
	private ImageButton buttonUpdateAmount;
	
	private LinearLayout layoutCategories;
	
	private TextView textViewDate;
	
	private ArrayList<Payee> payees = new ArrayList<Payee>();
	private Payee payee;
	
	private static final String BUNDLE_PAYEE_TAG = "payee";		// tag that all Payee related items stored in a Bundle should start with	
	private static final String BUNDLE_PAYEE = BUNDLE_PAYEE_TAG;	
	/* The following will be retrieved from the database:
	 * 	- payees
	 */

	
	private ArrayList<Transfer> transfers = new ArrayList<Transfer>();
	private Transfer transfer;

	private static final String BUNDLE_TRANSFER_TAG = "transfer";		// tag that all Transfer related items stored in a Bundle should start with	
	private static final String BUNDLE_TRANSFER = BUNDLE_TRANSFER_TAG;	
	/* The following will be retrieved from the database:
	 * 	- transfers
	 */

	private ImageButton buttonTransfer;
	private TextView textViewTransfer;

	private EditText editTextRef;
	private EditText editTextNote;
	
	private Button buttonSave;
	private Button buttonSaveNew;
	private Button buttonCancel;
	
	// transaction variables
	private double transactionAmount = 0;
	private double transactionRate = 1.0;
	private GregorianCalendar transactionDate = null;
	private GregorianCalendar completeDate = null;
	
	// menu constants
	private static final int SHOW_SET_CATEGORY = 0;
	private static final int SHOW_SET_PAYEE = 1;
	private static final int SHOW_SET_TRANSFER = 2;
	private static final int SHOW_EDIT_CATEGORY = 3;


	// convenience lists to save data to Bundles
	private static final class SaveToBundle {
		String name;
		enum SaveType { SAVE_ARRAYLIST, SAVE_PARCELABLE, SAVE_INT };
		SaveType type;
		/**
		 * @param name
		 * @param type
		 */
		public SaveToBundle(String name, SaveType type) {
			this.name = name;
			this.type = type;
		}
	};
	private static final SaveToBundle[] saveItems = {
		new SaveToBundle(BUNDLE_CATEGORIES_SELECTED, SaveToBundle.SaveType.SAVE_ARRAYLIST),
		new SaveToBundle(BUNDLE_ACCOUNT, SaveToBundle.SaveType.SAVE_PARCELABLE),
		new SaveToBundle(BUNDLE_ACCOUNT_TO, SaveToBundle.SaveType.SAVE_PARCELABLE),
		new SaveToBundle(BUNDLE_PAYEE, SaveToBundle.SaveType.SAVE_PARCELABLE),
		new SaveToBundle(BUNDLE_TRANSFER, SaveToBundle.SaveType.SAVE_PARCELABLE),
		new SaveToBundle(BUNDLE_RADIO_TYPE, SaveToBundle.SaveType.SAVE_INT),
		/* Note: other items such as BUNDLE_CATEGORIES_INCOME etc. can be retrieved from the database, so
		 *       are not saved as per Android guidelines. */
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_transaction);
		
		// get references to the activity views
		editTextAmount = (AmountEditText)this.findViewById(R.id.addTransaction_editTextAmount);
		editTextRate = (EditText)this.findViewById(R.id.addTransaction_editTextRate);
		layoutRate = (LinearLayout) findViewById(R.id.addTransaction_layoutRate);
		textViewRate = (TextView)this.findViewById(R.id.addTransaction_textViewRate);
		spinnerAccount = (Spinner)this.findViewById(R.id.addTransaction_spinnerAccount);
		layoutToAccount = (RelativeLayout)this.findViewById(R.id.addTransaction_layoutToAccount);
		spinnerToAccount = (Spinner)this.findViewById(R.id.addTransaction_spinnerToAccount);
		radioType = (RadioGroup)this.findViewById(R.id.addTransaction_radioGroupType);
		buttonPayee = (ImageButton)this.findViewById(R.id.addTransaction_buttonSetPayee);
		textViewPayee = (TextView)this.findViewById(R.id.addTransaction_textViewPayee);
		buttonSetCategory = (ImageButton)this.findViewById(R.id.addTransaction_buttonSetCategory);
		textViewCategory = (TextView)this.findViewById(R.id.addTransaction_category_heading);
		buttonUpdateAmount = (ImageButton)this.findViewById(R.id.addTransaction_buttonUpdateAmount);
		layoutCategoryMain = (RelativeLayout) findViewById(R.id.addTransaction_layoutCategoryMain);
		layoutCategories = (LinearLayout) findViewById(R.id.addTransaction_layoutCategory);
		textViewDate = (TextView)this.findViewById(R.id.addTransaction_textViewDate);
		buttonTransfer = (ImageButton)this.findViewById(R.id.addTransaction_buttonTransfer);
		textViewTransfer = (TextView)this.findViewById(R.id.addTransaction_textViewTransfer);
		editTextRef = (EditText)this.findViewById(R.id.addTransaction_editTextRef);
		editTextNote = (EditText)this.findViewById(R.id.addTransaction_editTextNote);
		buttonSave = (Button)this.findViewById(R.id.addTransaction_buttonSave);
		buttonSaveNew = (Button)this.findViewById(R.id.addTransaction_buttonSaveAndNew);
		buttonCancel = (Button)this.findViewById(R.id.addTransaction_buttonCancel);

		// init variables
		transactionDate = getTransactionDate();		// default to now
		completeDate = null;
		
		updatedAccounts = new ArrayList<Long>();

		// get data from provider
		loadCategoriesFromProvider(savedInstanceState);
		loadAccountsFromProvider(savedInstanceState);
		loadPayeesFromProvider(savedInstanceState);
		loadTransfersFromProvider(savedInstanceState);

		if ( savedInstanceState == null )
			savedInstanceState = getIntent().getExtras();
		
		startAccount = null;				// default to no particular account 
		
		// default to create new transaction
		editTransactionParent = null;		
		editTransactionCategories = null;

		getArguments(savedInstanceState);

		
		// setup the activity views
		setupAmountEditText();
		setupRateEditText();
		setupAccountSpinner();

		setupRadioButtons(savedInstanceState);
		setupPayeeButton();
		setupSetCategoryButton();
		setupUpdateAmountButton();
		setupDateTimeButtons();
		setupTransferButton();
		setupSaveButtons();
		setupCancelButton();

		updateTransactionDateTime();
		displayPayee();
		displayNoteRef();
	}

	/**
	 * Retrieve the arguments from the intent that started this activity 
	 * @param b
	 */
	private void getArguments(Bundle b) {

		if ( b == null )
			return;

		if ( b.containsKey(Constants.ACCOUNT_ID_NUM) ) {
			long accountId = b.getLong(Constants.ACCOUNT_ID_NUM);

			for ( TransactionAccount acc : accounts ) {
				if ( acc.getId() == accountId ) {
					startAccount = acc;
					break;
				}
			}
		}
		else if ( b.containsKey(Constants.TRANSACTION_ID_NUM) ) {
			long transId = b.getLong(Constants.TRANSACTION_ID_NUM);

			// get transaction group to edit
			ArrayList<Transaction> transactionGroup = new Transaction().loadTransactionGroupFromProvider(getContentResolver(), transId);
			editTransactionParent = Transaction.getParentFromGroup(transactionGroup);
			editTransactionCategories = Transaction.getCategoriesFromGroup(transactionGroup);
			if ( transactionGroup != null && editTransactionParent == null ) {
				// no parent, something's wrong, so do nothing
				Logger.w("No parent found for transaction group: " + transactionGroup.toString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {

		Class<? extends AddTransactionActivity> c = getClass();
		for ( int i = saveItems.length - 1; i >= 0; --i ) {
			String key = saveItems[i].name;
			Field f;
			try {
				f = c.getDeclaredField(key);
			} catch (NoSuchFieldException  e) {
				Logger.w("Error saving '" + key + "' to bundle");
				e.printStackTrace();
				continue;
			}
			switch ( saveItems[i].type ) {
				case SAVE_ARRAYLIST:
					try {
						ArrayList<? extends Parcelable> array = (ArrayList<? extends Parcelable>) f.get(this);
						if ( array != null ) {
							outState.putParcelableArrayList(key, array);
							Logger.d("Added '" + key + "' to bundle");
						}
					} catch (Exception e) {
						Logger.w("Error saving '" + key + "' to bundle");
						e.printStackTrace();
					}
					break;
				case SAVE_PARCELABLE:
					try {
						Parcelable parcel = (Parcelable) f.get(this);
						if ( parcel != null ) {
							outState.putParcelable(key, parcel);
							Logger.d("Added '" + key + "' to bundle");
						}
					} catch (Exception e) {
						Logger.w("Error saving '" + key + "' to bundle");
						e.printStackTrace();
					}
					break;
				case SAVE_INT:
					try {
						int integer = (Integer) f.get(this);
						outState.putInt(key, integer);
						Logger.d("Added '" + key + "' to bundle");
					} catch (Exception e) {
						Logger.w("Error saving '" + key + "' to bundle");
						e.printStackTrace();
					}
					break;
				default:
					// ignore
					break;
			}
		}
		super.onSaveInstanceState(outState);
	}


	/**
	 * Retrieve items with the specified tag from the Bundle
	 * @param b		- Bundle to retrieve from
	 * @param tag	- tag of items to retrieve
	 * @return		Difference between number of tag items and items actually retrieved, e.g. < 0 => all except X items, == 0 => no items, > 0 => all items  
	 */
	private int retrieveFromBundle(Bundle b, String tag) {

		int tagCnt = 0;		// num of bundle tags matching specified tag
		int getCnt = 0;		// num of items retrieved
		if ( b != null ) {
			Class<? extends AddTransactionActivity> c = getClass();
			for ( int i = saveItems.length - 1; i >= 0; --i ) {
				String key = saveItems[i].name;
				if ( !TextUtils.isEmpty(key) && key.startsWith(tag) ) {
					++tagCnt;
					if ( b.containsKey(key) ) {
						Field f;
						try {
							f = c.getDeclaredField(key);
						} catch (NoSuchFieldException  e) {
							Logger.w("Error saving '" + key + "' to bundle");
							e.printStackTrace();
							continue;
						}
						switch ( saveItems[i].type ) {
							case SAVE_ARRAYLIST:
								try {
									f.set(this, b.getParcelableArrayList(key));
									++getCnt;
									Logger.d("Restored '" + key + "' from bundle");
								} catch (Exception e) {
									Logger.d("Error restoring " + key + " from bundle");
									e.printStackTrace();
								}
								break;
							case SAVE_PARCELABLE:
								try {
									f.set(this, b.getParcelable(key));
									++getCnt;
									Logger.d("Restored '" + key + "' from bundle");
								} catch (Exception e) {
									Logger.d("Error restoring " + key + " from bundle");
									e.printStackTrace();
								}
								break;
							case SAVE_INT:
								try {
									f.set(this, b.getInt(key));
									++getCnt;
									Logger.d("Restored '" + key + "' from bundle");
								} catch (Exception e) {
									Logger.d("Error restoring " + key + " from bundle");
									e.printStackTrace();
								}
								break;
							default:
								// ignore
								break;
						}
					}
					else
						Logger.d("'" + key + "' not in bundle");
				}
			}
		}
		int result = getCnt - tagCnt;
		if ( tagCnt == 0 )
			result = 0;	// no items
		else if ( result == 0 )
			result = tagCnt;	// all items
		return result;
	}
	
	/**
	 * Setup the amount edit text in this activity
	 */
	private void setupAmountEditText() {

		// in edit mode, amount text is set after the account has been set up so that minor units are correct  

		// add a text changed listener to ensure that the correct number of minor units is entered
		editTextAmount.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	// update save button state
	        	saveButtonControl();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 

		
		// not required as everything else calls handleFocusChange()
		// add a lost focus listener to update the category allocations 
//		editTextAmount.setOnFocusChangeListener(new OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if ( !hasFocus ) {
//					updateCategoryAmounts();
//				}
//			}
//		});
	}
	
	
	/**
	 * Setup the rate edit text in this activity
	 */
	private void setupRateEditText() {

		if ( editTransactionParent != null ) {
			// in edit mode, so retrieve original value
			transactionRate = editTransactionParent.getTransRate();
			editTextRate.setText(Double.toString(transactionRate));
		}

		// add a text changed listener to ensure that the correct number of minor units is entered
		editTextRate.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	// update save button state
	        	saveButtonControl();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 

		
		// not required as everything else calls handleFocusChange()
		// add a lost focus listener to update the category allocations 
//		editTextAmount.setOnFocusChangeListener(new OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if ( !hasFocus ) {
//					updateCategoryAmounts();
//				}
//			}
//		});
	}
	

	/**
	 * Setup the account spinners in this activity
	 */
	private void setupAccountSpinner() {

		accAdapter = new TextViewAdapter<TransactionAccount>(this, 
				R.layout.text_view_adapter_item, R.id.text_view_adapter_item_textViewItem, accounts);
		spinnerAccount.setAdapter(accAdapter);
		spinnerToAccount.setAdapter(accAdapter);

		// get the initial account selections
		long accountId = -1;
		long toAccountId = -1;
		if ( startAccount != null ) {
			// account to start with has been specified
			accountId = startAccount.getId();
		}
		if ( editTransactionParent != null ) {
			// in edit mode, so retrieve original value
			switch ( editTransactionParent.getTransType() ) {
				case Transaction.TRANSACTION_CREDIT:
					accountId = editTransactionParent.getTransDestAccount();
					break;
				case Transaction.TRANSACTION_DEBIT:
					accountId = editTransactionParent.getTransSrcAccount();
					break;
				case Transaction.TRANSACTION_TRANSFER:
					accountId = editTransactionParent.getTransSrcAccount();
					toAccountId = editTransactionParent.getTransDestAccount();
					break;
			}
		}
		if ( accountId > 0 || toAccountId > 0 ) {
			for ( int i = accounts.size() - 1; i >= 0; --i ) {
				long id = accounts.get(i).getId();
				if ( id == accountId ) {
					spinnerAccount.setSelection(i);
				}
				if ( id == toAccountId ) {
					spinnerToAccount.setSelection(i);
				}
			}
		}
		
		
		// create a listener to retrieve the selected account
		spinnerAccount.setOnItemSelectedListener(new OnItemSelectedListener () {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// An item was selected, retrieve the selected item
				changeAccount( (TransactionAccount) parent.getItemAtPosition(position) );

				handleFocusChange();	// handle focus change
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nop
			}
		});

		
		// create a listener to retrieve the selected account
		spinnerToAccount.setOnItemSelectedListener(new OnItemSelectedListener () {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// An item was selected, retrieve the selected item
				accountTo = (TransactionAccount) spinnerToAccount.getSelectedItem();

				handleFocusChange();	// handle focus change
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nop
			}
		});

		// set internal selection
		changeAccount( (TransactionAccount) spinnerAccount.getSelectedItem() );
		accountTo = (TransactionAccount) spinnerToAccount.getSelectedItem();

		// enable/disable optional fields 
		enableOptionalFields();
		
		
		// need to wait until the AmountEditText is setup before setting the amount text in edit mode
		if ( editTransactionParent != null ) {
			// in edit mode, so retrieve original value
			transactionAmount = editTransactionParent.getTransAmt();
			editTextAmount.setText(transactionAmount);
		}
	}
	
	
	/**
	 * Change the selected account & update the corresponding internal variables  
	 * @param newAccount
	 */
	private void changeAccount(TransactionAccount newAccount) {
		
		account = newAccount;

		// update the minor units on the amount entry
		editTextAmount.setMinorUnits(account.getMinorUnits());
		// TODO editTextAmount.setMinorUnitSeparator()

		// update the imbalance category
		Category prev = categoryImbalance;
		String code = newAccount.getCode();
		for ( Category category : categoriesImbalance ) {
			if ( code.compareToIgnoreCase(category.getName()) == 0 ) {
				categoryImbalance = category;	// remember imbalance for convenience
				break;
			}
		}
		if ( (prev!= null) && !prev.equals(categoryImbalance) )
			replaceCategoryInList(prev, categoryImbalance);

		// update the unassigned category
		prev = categoryUnassigned;
		for ( Category category : categoriesUnassigned ) {
			if ( code.compareToIgnoreCase(category.getName()) == 0 ) {
				categoryUnassigned = category;	// remember unassigned for convenience
				break;
			}
		}
		if ( (prev != null) && !prev.equals(categoryUnassigned) )
			replaceCategoryInList(prev, categoryUnassigned);
	}
	

	/**
	 * Setup the radio buttons button
	 */
	private void setupRadioButtons(Bundle savedInstanceState) {
		
		// only enable transfer if more than 1 account
		RadioButton radioButton = (RadioButton)this.findViewById(R.id.addTransaction_radioTransfer);
		boolean enabled = (accounts.size() <= 1 ? false : true);
		radioButton.setEnabled( enabled );
		radioButton.setVisibility( enabled ? View.VISIBLE : View.GONE );

		if ( retrieveFromBundle(savedInstanceState, BUNDLE_RADIO_TAG) <= 0 )
			radioCheckedType = R.id.addTransaction_radioCredit;	// set default as not in bundle
		
		if ( editTransactionParent != null ) {
			// in edit mode, so retrieve original value
			switch ( editTransactionParent.getTransType() ) {
				case Transaction.TRANSACTION_CREDIT:
					radioCheckedType = R.id.addTransaction_radioCredit;
					break;
				case Transaction.TRANSACTION_DEBIT:
					radioCheckedType = R.id.addTransaction_radioDebit;
					break;
				case Transaction.TRANSACTION_TRANSFER:
					radioCheckedType = R.id.addTransaction_radioTransfer;
					break;
			}
		}
		
		/* no default checked item set in layout, so set here to avoid OnCheckedChangeListener call 
		 * which clears categoriesSelected which may have been restored from bundle */ 
		radioType.check(radioCheckedType);
		
		// clear category selections when switching type
		radioType.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				int categoryVisibility = View.VISIBLE;

				categoriesSelected = null;	// clear categories
				
				radioCheckedType = checkedId;
				
				switch ( checkedId ) {
					case R.id.addTransaction_radioTransfer:
						categoryVisibility = View.GONE;
						break;
//					case R.id.addTransaction_radioCredit:
//					case R.id.addTransaction_radioDebit:
					default:
						// unassigned by default
						addCategoryToList(categoryUnassigned, transactionAmount);

						updateCategoryAmounts();
						break;
				}
				layoutCategoryMain.setVisibility(categoryVisibility);

				displaySelectedCategory();
			}
		});
	}
	
	
	/**
	 * Setup the payee button
	 */
	private void setupPayeeButton() {
		

		if ( editTransactionParent != null ) {
			// in edit mode, so retrieve original value
			Payee editPayee = Payee.loadFromProvider(getContentResolver(), editTransactionParent.getTransPayee());
			if ( editPayee != null ) {
				payee = editPayee;
				displayPayee();
			}
		}


		OnClickListener payeeListener =  new OnClickListener() {

			@Override
			public void onClick(View v) {

				handleFocusChange();	// handle focus change
				
				if ( PreferenceControl.isUseFragments(getApplicationContext()) ) {

				    Bundle b = new Bundle();
					b.putParcelableArrayList(Constants.PAYEE_ARRAYLIST, payees);

					launchFragment(SelectPayeeFragment.LAYOUT_ID, SelectPayeeFragment.class, b, SHOW_SET_PAYEE);
				}
				else {
					// launch select category activity
					Intent i = new Intent(getBaseContext(), SelectPayeeActivity.class);
					i.putParcelableArrayListExtra(Constants.PAYEE_ARRAYLIST, payees);

					startActivityForResult(i, SHOW_SET_PAYEE);
				}			
			}
		};

		RelativeLayout layout = (RelativeLayout)findViewById(R.id.addTransaction_layoutPayee);
		layout.setOnClickListener(payeeListener);
		textViewPayee.setOnClickListener(payeeListener);
		buttonPayee.setOnClickListener(payeeListener);
	}

	
	/**
	 * Display a fragment
	 * @param id			- fragment id, typically use layout id
	 * @param fragmentClass	- class of fragment to create 
	 * @param b				- argument bundle
	 * @param requestCode	- request code to return as part of fragment result
	 */
	private void launchFragment(int id, Class<?> fragmentClass, Bundle b, int requestCode) {
	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
	    FragmentManager fm = getFragmentManager();
	    FragmentTransaction ft = fm.beginTransaction();
	    Fragment prev = fm.findFragmentById(id);
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);
	    ft.commit();

	    b.putInt(EditFragment.EDITFRAGMENT_REQUESTCODE, requestCode);

	    // Create and show the dialog.
	    DialogFragment newFragment;
		try {
			newFragment = (DialogFragment) fragmentClass.newInstance();

		    newFragment.setArguments(b);
		    newFragment.show(fm, getString(id));
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Setup the categories button
	 */
	private void setupSetCategoryButton() {

		OnClickListener buttonListener = new OnClickListener() {

			@Override
			public void onClick(View v) {

				handleFocusChange();	// handle focus change
				
				ArrayList<Category> currentCategories = new ArrayList<Category>();
				for ( TransactionCategory transCat : categoriesSelected )
					currentCategories.add(transCat.getCategory());

				int selectedTypes;
				ArrayList<Category> categories;
				if ( radioType.getCheckedRadioButtonId() == R.id.addTransaction_radioCredit ) {
					selectedTypes = Category.INCOME_CATEGORY;
					categories = categoriesIncome;
				}
				else {
					selectedTypes = Category.EXPENSE_CATEGORY;
					categories = categoriesExpense;
				}

				if ( PreferenceControl.isUseFragments(getApplicationContext()) ) {
					
				    Bundle b = new Bundle();

					b.putParcelableArrayList(Constants.CATEGORY_SELECTION_RESULT, currentCategories);
					b.putInt(Constants.CATEGORY_TYPE_SELECTED, selectedTypes);
					b.putParcelableArrayList(Constants.CATEGORY_ARRAYLIST, categories);

					launchFragment(SelectCategoryFragment.LAYOUT_ID, SelectCategoryFragment.class, b, SHOW_SET_CATEGORY);
				}
				else {
					// launch select category activity
					Intent i = new Intent(getBaseContext(), SelectCategoryActivity.class);
//					i.putParcelableArrayListExtra(Constants.CATEGORY_INCOME_ARRAYLIST, categoriesIncome);
//					i.putParcelableArrayListExtra(Constants.CATEGORY_EXPENSE_ARRAYLIST, categoriesExpense);

					i.putParcelableArrayListExtra(Constants.CATEGORY_SELECTION_RESULT, currentCategories);
					i.putExtra(Constants.CATEGORY_TYPE_SELECTED, selectedTypes);
					i.putParcelableArrayListExtra(Constants.CATEGORY_ARRAYLIST, categories);

					startActivityForResult(i, SHOW_SET_CATEGORY);
				}			
			}
			
		};
		
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.addTransaction_layoutCategoryHeader);
		layout.setOnClickListener(buttonListener);
		buttonSetCategory.setOnClickListener(buttonListener);
		textViewCategory.setOnClickListener(buttonListener);

		if ( editTransactionParent != null ) {
			// in edit mode, so retrieve original value
			ArrayList<Transaction> categoryTrans; 
			if ( editTransactionCategories != null && !editTransactionCategories.isEmpty() )
				categoryTrans = editTransactionCategories; 
			else {
				categoryTrans = new ArrayList<Transaction>();
				categoryTrans.add(editTransactionParent);
			}

			ContentResolver cr = getContentResolver();
			for ( Transaction catTrans : categoryTrans ) {
				Category category = Category.loadFromProvider(cr, catTrans.getTransCategory());
				
				setCategoryInList(category, catTrans.getTransAmt());
			}
		}
		else {
			// unassigned by default
			addCategoryToList(categoryUnassigned, transactionAmount);
		}		
		displaySelectedCategory();
	}

	
	/**
	 * Setup the update amount button
	 */
	private void setupUpdateAmountButton() {

		buttonUpdateAmount.setEnabled(false);
		
		buttonUpdateAmount.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {

				handleFocusChange();	// handle focus change
				
				Double adjustment = (Double) buttonUpdateAmount.getTag();	// negative amount implies total needs to increase

				transactionAmount -= adjustment.doubleValue();
				
				editTextAmount.setText(Double.toString(transactionAmount));
				
				updateImbalanceAmount(0);	// remove imbalance

				displaySelectedCategory();
				enableOptionalFields();
			}
		});
	}

	
	/**
	 * Setup the date & time buttons in this activity
	 */
	private void setupDateTimeButtons() {
		
		ImageButton button = (ImageButton)this.findViewById(R.id.addTransaction_buttonDate);
		button.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new SelectDateFragment();
				Bundle b = new Bundle();
				b.putInt(SelectDateFragment.TITLE, R.string.addtransaction_date);
				b.putSerializable(SelectDateFragment.DATE, getTransactionDate());
				newFragment.setArguments(b);
				newFragment.show(getFragmentManager(), "datePicker");
			}
		});

		button = (ImageButton)this.findViewById(R.id.addTransaction_buttonTime);
		button.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new SelectTimeFragment();
				Bundle b = new Bundle();
				b.putInt(SelectDateFragment.TITLE, R.string.addtransaction_time);
				b.putSerializable(SelectDateFragment.DATE, getTransactionDate());
				newFragment.setArguments(b);
				newFragment.show(getFragmentManager(), "timePicker");
			}
		});
	}

	
	/**
	 * Setup the transfer button
	 */
	public void setupTransferButton() {

		if ( editTransactionParent != null ) {
			// in edit mode, so retrieve original value
			transactionDate = editTransactionParent.getTransSendDate();
			completeDate = editTransactionParent.getTransRecvDate();
			
			for ( Transfer xfer : transfers ) {
				if ( xfer.calcTransferCompleteDate(transactionDate).equals(completeDate) ) {
					transfer = xfer;
					setTransactionDate(transactionDate);
					break;
				}
			}
		}

		OnClickListener transferListener =  new OnClickListener() {

			@Override
			public void onClick(View v) {

				handleFocusChange();	// handle focus change
				
				if ( transactionDate == null ) {
					transactionDate = new GregorianCalendar();
					updateTransactionDateTime();
				}

				if ( PreferenceControl.isUseFragments(getApplicationContext()) ) {

					Bundle b = new Bundle();

					b.putParcelableArrayList(Constants.TRANSFER_ARRAYLIST, transfers);
					b.putSerializable(Constants.TRANSFER_START_DATE, transactionDate);
				    
					launchFragment(SelectTransferFragment.LAYOUT_ID, SelectTransferFragment.class, b, SHOW_SET_TRANSFER);
				}
				else {
					// launch select transfer activity
					Intent i = new Intent(getBaseContext(), SelectTransferActivity.class);
					i.putParcelableArrayListExtra(Constants.TRANSFER_ARRAYLIST, transfers);
					i.putExtra(Constants.TRANSFER_START_DATE, transactionDate);
					
					startActivityForResult(i, SHOW_SET_TRANSFER);
				}			
			}
			
		};

		RelativeLayout layout = (RelativeLayout)findViewById(R.id.addTransaction_layoutRecvDateTime);
		layout.setOnClickListener( transferListener );
		buttonTransfer.setOnClickListener( transferListener );
		textViewTransfer.setOnClickListener( transferListener );
	}


	/**
	 * Enable/disable optional fields based on radio button selections made
	 * @param view	- view clicked
	 */
	public void onRadioButtonClicked(View view) {
		enableOptionalFields();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case SHOW_SET_CATEGORY:
				if (resultCode == Activity.RESULT_OK) {
					ArrayList<Category> currentCategories = data.getParcelableArrayListExtra(Constants.CATEGORY_SELECTION_RESULT);
					updateCategoryList( currentCategories );
					displaySelectedCategory();
				}
				break;
			case SHOW_SET_PAYEE:
				if (resultCode == Activity.RESULT_OK) {
					
					payee = data.getParcelableExtra(Constants.PAYEE_SELECTION_RESULT);	// get selected payee
					if ( payee == null ) {
						// no selected result, so must be a new payee
						payee = data.getParcelableExtra(Constants.PAYEE_ADD_NEW_RESULT);	// get new payee
						if ( payee != null )
							addPayeeToProvider(payee);
					}
					displayPayee();
			    	// update save button state
					saveButtonControl();
				}
				break;
			case SHOW_SET_TRANSFER:
				if (resultCode == Activity.RESULT_OK) {

					transfer = data.getParcelableExtra(Constants.TRANSFER_SELECTION_RESULT);	// get selected transfer
					updateTransferTextView();
				}
				break;
			case SHOW_EDIT_CATEGORY:
				if (resultCode == Activity.RESULT_OK) {

					int id = data.getIntExtra(Constants.CATEGORY_AMOUNT_RESULT, 0);
					if ( data.hasExtra(Constants.CATEGORY_AMOUNT_RESULT_AMOUNT) ) {
						
						// update category amount
						double amount = data.getDoubleExtra(Constants.CATEGORY_AMOUNT_RESULT_AMOUNT, 0);
						updateCategoryInList(id, amount);
					}
					else {
						// remove category
						removeCategoryFromList(id);
					}
					updateCategoryAmounts();
					displaySelectedCategory();
					enableOptionalFields();
				}
				break;
		}

	}

	/**
	 * Control whether the save buttons are enabled or not
	 */
	private void saveButtonControl() {
		
		boolean allOk = allRequiredDataEntered();
		
    	// update save button state
    	buttonSave.setEnabled( allOk );
    	buttonSaveNew.setEnabled( allOk );
	}
	
	
	/**
	 * Finish the activity and return the list of updated accounts 
	 * @param result
	 */
	private void finishAddTransaction(int result) {
		
		if ( !updatedAccounts.isEmpty() ) {
			Intent intent = new Intent();	// intent to return updated a/c

			final int N = updatedAccounts.size();
			long[] accounts = new long[N];
			for ( int i = 0; i < N; ++i )
				accounts[i] = updatedAccounts.get(i);
			intent.putExtra(Constants.ACCOUNT_ID_ARRAY, accounts); 
			setResult(result, intent);
		}
		else
			setResult(result);
		finish();
	}
	
	
	/**
	 * Setup the save buttons in this activity
	 */
	private void setupSaveButtons() {
		
    	// update save button state
		saveButtonControl();

		// setup save button
		buttonSave.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				handleFocusChange();	// handle focus change

				// add the new transaction
				addTransactionToProvider();

				finishAddTransaction(Activity.RESULT_OK);
			}
		});
		
		// setup save & new button
		buttonSaveNew.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				handleFocusChange();	// handle focus change

				// add the new transaction
				addTransactionToProvider();
				
				// clear previous data
				editTextAmount.setText("");
				editTextRate.setText("");
				editTextRef.setText("");
				editTextNote.setText("");

				categoriesSelected = null;	// clear categories
				displaySelectedCategory();
				updateCategoryAmounts();
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
				finishAddTransaction(Activity.RESULT_CANCELED);
			}
		});
	}

	
	private OnClickListener editCategoryAmountListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			CategoryAllocation alloc = (CategoryAllocation)v;

			double unassigned = 0;
			for ( TransactionCategory transCat : categoriesSelected ) {
				if ( transCat.isSameCategory(categoryUnassigned) ) {
					unassigned = transCat.getAmount();
					break;
				}
			}

			
			if ( PreferenceControl.isUseFragments(getApplicationContext()) ) {
				
			    Bundle b = new Bundle();

			    b.putString(Constants.CATEGORY_AMOUNT_EDIT_NAME, (String) alloc.getText());
			    b.putDouble(Constants.CATEGORY_AMOUNT_TO_EDIT, alloc.getAmount());
			    b.putInt(Constants.CATEGORY_AMOUNT_EDIT_ID, ((Integer)alloc.getTag()).intValue());
			    if ( unassigned > 0 )
				    b.putDouble(Constants.CATEGORY_AMOUNT_ALLOC, unassigned);

				launchFragment(EditCategoryAmountFragment.LAYOUT_ID, EditCategoryAmountFragment.class, b, SHOW_EDIT_CATEGORY);
			}
			else {
				// launch edit category amount activity
				Intent i = new Intent(getBaseContext(), EditCategoryAmountActivity.class);
				i.putExtra(Constants.CATEGORY_AMOUNT_EDIT_NAME, alloc.getText());
				i.putExtra(Constants.CATEGORY_AMOUNT_TO_EDIT, alloc.getAmount());
				i.putExtra(Constants.CATEGORY_AMOUNT_EDIT_ID, ((Integer)alloc.getTag()).intValue());
			    if ( unassigned > 0 )
				    i.putExtra(Constants.CATEGORY_AMOUNT_ALLOC, unassigned);

				startActivityForResult(i, SHOW_EDIT_CATEGORY);
			}			
		}
	};
	
	
	/**
	 * Display selected payee name
	 */
	private void displayPayee() {
		String name;
		if ( payee != null )
			name = payee.getName();
		else
			name = (String) getResources().getText(R.string.addtransaction_select_payee);
		textViewPayee.setText( "  " + name );
	}
	
	
	private void displayNoteRef() {
		if ( editTransactionParent != null ) {
			// in edit mode, so retrieve original value
			editTextRef.setText(editTransactionParent.getTransRef());
			editTextNote.setText(editTransactionParent.getTransNote());
		}
	}
	
	/**
	 * Check if a category selection has been made
	 * @return
	 */
	private boolean isCategorySelected() {
		boolean result = false;
		if ( categoriesSelected != null ) {
			// selected if there's more than 1 or its not unassigned
			int size = categoriesSelected.size();
			if ( size > 1 )
				result = true;
			else if ( size == 1 )
				result = !categoriesSelected.get(0).isSameCategory(categoryUnassigned);
		}
		return result;
	}
	
	
	/**
	 * Display the selected categories
	 */
	private void displaySelectedCategory() {
		
		layoutCategories.removeAllViews();
		layoutCategories.invalidate();

		if ( categoriesSelected != null ) {

			boolean toFront = false;
			for ( TransactionCategory transCat : categoriesSelected ) {
			
				// display the allocations, if there's more than 1 or its not unassigned
				if ( (categoriesSelected.size() > 1) || !transCat.isSameCategory(categoryUnassigned) ) {
					CategoryAllocation alloc = new CategoryAllocation( getApplicationContext() );
					Category category = transCat.getCategory();
					
					alloc.setText(category.getAbsolutePath());

					alloc.setMinorUnits(account.getMinorUnits());

					double amount = transCat.getAmount();
//					if ( amount > 0)
						alloc.setAmount(amount);

					alloc.setOnClickListener(editCategoryAmountListener);
					alloc.setTag(Integer.valueOf((int)category.getId()));

					layoutCategories.addView(alloc);
					
					toFront = true;
				}
				// else don't display just unassigned
			}
			if ( toFront )
				layoutCategories.bringToFront();
		}

		int id = ( isCategorySelected() ? R.string.addtransaction_categories : R.string.addtransaction_select_categories);
		textViewCategory.setText((String) getResources().getText(id));
	}
	
	
	/**
	 * Update the category amounts
	 */
	private void updateCategoryAmounts() {
		
		double assignedAmount = 0;			// amount for all categories excluding imbalance & unassigned
		int zeroAmtCategories = 0;			// number of categories, excluding imbalance & unassigned, with zero amount
		Category zeroAmtCategory = null;
		boolean haveImbalance = false;
		boolean haveUnassigned = false;
		
		transactionAmount = getAmount(editTextAmount.getText().toString());	// trans total amount
		if ( rateRequired() )
			transactionRate = getAmount(editTextRate.getText().toString());	// trans rate
		else
			transactionRate = 1.0;		// default rate is 1.0

		if ( categoriesSelected != null ) {
			for ( TransactionCategory transCat : categoriesSelected ) {
				double amount = transCat.getAmount();
				boolean imbalance = transCat.isSameCategory(categoryImbalance);
				boolean unassigned = transCat.isSameCategory(categoryUnassigned);
				
				if ( imbalance )
					haveImbalance = true;
	
				if ( unassigned )
					haveUnassigned = true;
				
				if ( !imbalance && !unassigned )
					assignedAmount += amount;
				
				if ( amount == 0 ) {
					++zeroAmtCategories;
					if ( zeroAmtCategories == 1 )
						zeroAmtCategory = transCat.getCategory();
					else
						zeroAmtCategory = null;
				}
			}
		}
		
		if ( assignedAmount >= transactionAmount ) {
			// alloced categories amount >= transaction amount, remove unassigned & update imbalance
			updateImbalanceAmount(transactionAmount - assignedAmount);	// negative imbalance
			removeCategoryFromList(categoryUnassigned);
		}
		else {
			// alloced categories amount < transaction amount
			double diffAmount = transactionAmount - assignedAmount;

			if ( haveImbalance )
				updateImbalanceAmount(0);	// remove imbalance
			
			if ( zeroAmtCategories == 1 ) {
				// only one zero amount so alloc balance to that category
				addCategoryToList(zeroAmtCategory, diffAmount);
				removeCategoryFromList(categoryUnassigned);
			}
			else if ( !haveUnassigned ) {
				// add unassigned for balance
				addCategoryToList(categoryUnassigned, diffAmount);
			}
			else {
				// update unassigned for balance
				setCategoryInList(categoryUnassigned, diffAmount);
			}
		}
	}
	
	
	
	

	/**
	 * Update the imbalance category amount
	 */
	private void updateImbalanceAmount(double outstandingAmount) {
		
		if ( outstandingAmount != 0 ) {
			// still an amount outstanding, so add imbalance
			addCategoryToList(categoryImbalance, outstandingAmount);
			
			buttonUpdateAmount.setEnabled(true);
			buttonUpdateAmount.setVisibility(View.VISIBLE);
			buttonUpdateAmount.setTag(Double.valueOf(outstandingAmount));
		}
		else {
			// remove imbalance from list
			removeCategoryFromList(categoryImbalance);
			
			buttonUpdateAmount.setEnabled(false);
			buttonUpdateAmount.setVisibility(View.INVISIBLE);
			buttonUpdateAmount.setTag(null);
		}
	}

	
	/**
	 * Get the amount from the specified string.
	 * @param amountStr	- amount string
	 * @return			amount
	 */
	private double getAmount(String amountStr) {
		
		double newAmount = 0;

		if ( !TextUtils.isEmpty(amountStr) ) {
			try {
				// try to parse the entered text but if the hint is being displayed it'll fail
				newAmount = Double.parseDouble(amountStr);
			}
			catch ( NumberFormatException e ) {
				newAmount = 0;
			}
		}
		return newAmount;
	}
	

	/**
	 * Check if the specified category is in the list of selected categories 
	 * @return	<code>true</code> if the imbalance category is in the list, <code>false</code> otherwise
	 */
	private boolean isCategoryInList(Category category) {

		if ( categoriesSelected != null ) {
			for ( TransactionCategory transCat : categoriesSelected ) {
				if ( transCat.isSameCategory(category) )
					return true;
			}
		}
		return false;
	}

	
	/**
	 * Check if the imbalance category is in the list of selected categories 
	 * @return	<code>true</code> if the imbalance category is in the list, <code>false</code> otherwise
	 */
	private boolean isImbalanceCategoryInList() {
		return isCategoryInList(categoryImbalance);
	}

	
	/**
	 * Update the specified category/amount in the selected list.
	 * @param category
	 * @param amount
	 */
	private int UPDATE_CATEGORY_ADD = 0;
	private int UPDATE_CATEGORY_SET = 1;
	private void updateCategoryInList(Category category, double amount, int op) {
		
		TransactionCategory listItem = null;
		
		if ( categoriesSelected == null ) {
			// unassigned by default
			categoriesSelected = new ArrayList<TransactionCategory>();
		}
		else {
			// is category already present?
			for ( TransactionCategory transCat : categoriesSelected ) {
				if ( transCat.isSameCategory(category) ) {
					listItem = transCat;
					break;
				}
			}
		}

		if ( listItem == null )
			categoriesSelected.add( new TransactionCategory(category, amount) );
		else if ( op == UPDATE_CATEGORY_ADD )
			listItem.addAmount(amount);		// already present, so update amount
		else if ( op == UPDATE_CATEGORY_SET )
			listItem.setAmount(amount);		// already present, so set amount
	}
	

	/**
	 * Add the specified category/amount to the selected list. If it has previously been added, just the amount is updated.
	 * @param category
	 * @param amount
	 */
	private void addCategoryToList(Category category, double amount) {
		
		updateCategoryInList(category, amount, UPDATE_CATEGORY_ADD);
	}
	

	/**
	 * Set the amount for the specified category in the selected list.
	 * @param category
	 * @param amount
	 */
	private void setCategoryInList(Category category, double amount) {
		
		updateCategoryInList(category, amount, UPDATE_CATEGORY_SET);
	}
	
	/**
	 * Remove the specified category from the selected list.
	 * @param category
	 */
	private void removeCategoryFromList(Category category) {
		
		removeCategoryFromList((int)category.getId());
	}
	
	
	/**
	 * Remove the specified category from the selected list.
	 * @param id		Category id
	 */
	private void removeCategoryFromList(int id) {
		
		if ( categoriesSelected != null ) {
			for ( int i = categoriesSelected.size() - 1; i >= 0; --i ) {
				TransactionCategory transCat = categoriesSelected.get(i);

				if ( transCat.getCategory().getId() == id )
					categoriesSelected.remove(i);
			}
		}
	}
	
	
	/**
	 * Remove the specified category from the selected list.
	 * @param id		Category id
	 * @param amount	Amount
	 */
	private void updateCategoryInList(int id, double amount) {
		
		if ( categoriesSelected != null ) {
			final int N = categoriesSelected.size();
			for ( int i = 0; i < N; ++i ) {
				TransactionCategory transCat = categoriesSelected.get(i);

				if ( transCat.getCategory().getId() == id ) {
					transCat.setAmount(amount);
					break;
				}
			}
		}
	}
	
	
	/**
	 * Replace the specified category from the selected list.
	 * @param from	Category to be replaced
	 * @param to	Category to be added
	 */
	private void replaceCategoryInList(Category from, Category to) {
		
		if ( categoriesSelected != null ) {
			final int N = categoriesSelected.size();
			for ( int i = 0; i < N; ++i ) {
				TransactionCategory transCat = categoriesSelected.get(i);

				if ( transCat.isSameCategory(from) ) {
					transCat.setCategory(to);
				}
			}
		}
	}

	
	/**
	 * Update the selected categories list, removing items not in <code>currentCategories</code> and adding any new categories.  
	 * @param currentCategories
	 */
	private void updateCategoryList(ArrayList<Category> currentCategories) {
		
		ArrayList<TransactionCategory> newList = new ArrayList<TransactionCategory>();
		double unassignedAmt = 0;
		double categoriesAmt = 0;	// amount allocated to categories, excluding unassigned

		// add categories previously selected
		if ( categoriesSelected != null ) {
			for ( TransactionCategory transCat : categoriesSelected ) {
				Category category = transCat.getCategory();
				double amount = transCat.getAmount();
	
				if ( currentCategories.contains(category) ) {
					// keeping category, so add to new list
					newList.add(new TransactionCategory(category, amount));
					categoriesAmt += amount;
				}
				else {
					// dropping category, so need to reassign amount
					unassignedAmt += amount;
				}
			}
		}

		// add categories not previously selected
		for ( Category category : currentCategories ) {
			boolean found = false;
			for ( TransactionCategory transCat : newList ) {
				if ( transCat.isSameCategory(category) )
					found = true;
			}
			if ( !found ) {
				double categoryAmount = 0;
				if ( (unassignedAmt == transactionAmount) && (currentCategories.size() == 1) ) {
					// only one category so assign all the money to it
					categoryAmount = unassignedAmt;
					unassignedAmt = 0;
				}

				TransactionCategory newTransCat = new TransactionCategory(category, categoryAmount);
				
				newList.add(newTransCat);
				categoriesAmt += categoryAmount;
			}
		}

		// split unassigned amount between the other categories with zero amount 
		if ( unassignedAmt != 0 ) {
//			if ( splitUnassigned(unassignedAmt, newList) == 0 ) {
				newList.add(new TransactionCategory(categoryUnassigned, unassignedAmt));	// no split so add unassigned
				categoriesAmt += unassignedAmt;
//			}
		}
		
		// update imbalance as necessary
		double outstandingAmount = categoriesAmt - transactionAmount;
		updateImbalanceAmount( outstandingAmount );

		categoriesSelected = newList;
	}
	
	
	/**
	 * Split the specified amount equally between all categories with zero amounts
	 * @param amount
	 * @return			The number of categories it was split between
	 */
	private int splitUnassigned(double amount, ArrayList<TransactionCategory> list) {

		// calc the number to split between
		int splits = 0;
		for ( TransactionCategory transCat : list ) {
			if ( transCat.getAmount() == 0 )
				++splits;
		}
		
		if ( splits > 0 ) {

			double[] allocs = null;
	
			// calc the split for each
			if ( splits > 1 ) {
				BigDecimal bd = new BigDecimal(amount/splits);
				BigDecimal rounded = bd.setScale(2, BigDecimal.ROUND_FLOOR);
				double split = rounded.doubleValue();
				double alloced = 0;
				
				allocs = new double[splits];
				for ( int i = 0; i < splits; ++i ) {
					allocs[i] = split;
					alloced += split;
				}
				if ( alloced < amount )
					allocs[0] += (amount - alloced);	// assign outstanding amount
			}
			else if ( splits == 1 ) {
				allocs = new double[1];
				allocs[0] = amount;
			}
			
			if ( allocs != null ) {
				int i = 0;
				for ( TransactionCategory transCat : list ) {
					if ( transCat.getAmount() == 0 )
						transCat.setAmount(allocs[i++]);
				}
			}
		}		
		return splits;
	}
	
	
	
	/**
	 * Verify if all the required data has been entered
	 * @return	<code>true</code> is all the required data has been entered, <code>false</code> otherwise.
	 */
	private boolean allRequiredDataEntered() {

		boolean allOK = false;

		if ( (!TextUtils.isEmpty(editTextAmount.getText()))  &&
				(payee != null) &&
				(!buttonUpdateAmount.isEnabled()) &&
				rateDataEntered() ) {
			allOK = true;
		}

		return allOK;
	}
	
	
	/**
	 * Check if the rate data is required
	 * @return	<b>true</b> if required, <b>false</b> otherwise
	 */
	private boolean rateRequired() {
		// both the layout and the EditText must be visible
		return (layoutRate.getVisibility() == View.VISIBLE) && 
				(editTextRate.getVisibility() == View.VISIBLE);
	}
	
	
	/**
	 * Check if the rate data has been entered
	 * @return	<b>true</b> if entered or not required, <b>false</b> otherwise
	 */
	private boolean rateDataEntered() {

		boolean rateOK = false;

		if ( !rateRequired() ) {
			rateOK = true;	// no rate required, so ignore
		}
		else {
			if ( getAmount(editTextRate.getText().toString()) > 0.0 )
				rateOK = true;
		}

		return rateOK;
	}
	
	
	/**
	 * Do everything that needs to be done when the user moves focus
	 */
	private void handleFocusChange() {
		
		if ( editTextAmount.isFocused() || editTextRate.isFocused() ) {
			// amount/rate has focus so update categories as a click on a button/spinner doesn't change focus
			updateCategoryAmounts();
			displaySelectedCategory();
		}

		enableOptionalFields();
	}
	
	
	
	/**
	 * Enable/disable optional fields based on selections made
	 */
	private void enableOptionalFields() {

		// update 'to account' visibility
		int accountToVisibility;

		switch ( radioCheckedType ) {
			case R.id.addTransaction_radioTransfer:
				accountToVisibility = View.VISIBLE;	// visible for transfer
				break;
//			case R.id.addTransaction_radioCredit:
//			case R.id.addTransaction_radioDebit:
			default:
				accountToVisibility = View.GONE;
				break;
		}
		layoutToAccount.setVisibility(accountToVisibility);

		// update conversion rate visibility
		int rateVisibility = accountToVisibility;
		if ( rateVisibility == View.VISIBLE ) {
			
			if ( accountTo == null || account == null || accountTo.getCode().equals(account.getCode()) )
				rateVisibility = View.GONE;
			else {
				String text = getResources().getString(R.string.addtransaction_rate) + " (" +
						account.getSymbol() + "-" + accountTo.getSymbol() + ")";
				textViewRate.setText(text);
			}
		}
		layoutRate.setVisibility(rateVisibility);

    	// update save button state
		saveButtonControl();
	}
	
	/**
	 * Get list of account types from ContentProvider
	 */
	private void loadAccountsFromProvider(Bundle savedInstanceState) { 
		 
		accounts.clear();		// Clear the existing array
		
		retrieveFromBundle(savedInstanceState, BUNDLE_ACCOUNT_TAG);

		if ( accounts.isEmpty() ) {
			ContentResolver cr = getContentResolver();
			Cursor c = cr.query(DatabaseManager.ACCOUNT_ADDTRANS_URI, null, null, null, null);	// Return all the database accounts
			if (c.moveToFirst()) {
				int idIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_ID);
				int nameIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_NAME);
				int nicknameIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_NICKNAME);
				int unitsIdx = c.getColumnIndex(DatabaseManager.CURRENCY_MINORUNITS);
				int codeIdx = c.getColumnIndex(DatabaseManager.CURRENCY_CODE);
				int symbolIdx = c.getColumnIndex(DatabaseManager.CURRENCY_SYMBOL);
				do {
					// Extract the details.
					int id = c.getInt(idIdx);
					String name = c.getString(nameIdx);
					String nickname = c.getString(nicknameIdx);
					int minorUnits = c.getInt(unitsIdx);
					String code = c.getString(codeIdx);
					String symbol = c.getString(symbolIdx);
	
					accounts.add( new TransactionAccount(id, name, nickname, minorUnits, code, symbol) );
				} while(c.moveToNext());
			}
			c.close();
		}

		if ( account != null && accounts.contains(account) )
			changeAccount(account);
		else
			account = null;
	}
	
	/**
	 * Get list of categories from ContentProvider
	 */
	private void loadCategoriesFromProvider(Bundle savedInstanceState) { 

		int retrieved = retrieveFromBundle(savedInstanceState, BUNDLE_CATEGORY_TAG);
		
		if ( alwaysLoadDataFromProvider )
			retrieved = -1;		// force get data from database
			
		if ( retrieved <= 0 ) {
			// all or some of required not retrieved from bundle, so get everything new
			ArrayList<Category> categories = new ArrayList<Category>();

			// Clear the existing arrays
			if ( categoriesIncome != null )
				categoriesIncome.clear();
			if ( categoriesExpense != null )
				categoriesExpense.clear();
			if ( categoriesTransfer != null )
				categoriesTransfer.clear();
			if ( categoriesUnassigned != null )
				categoriesUnassigned.clear();
			if ( categoriesImbalance != null )
				categoriesImbalance.clear();
		
			// query database for all categories
			categories = Category.loadCategoriesFromProvider(getContentResolver());
			if (!categories.isEmpty()) {
				
				// sort list 
				Collections.sort(categories, new Category.CompareTypeLevelPathName());
				
				// generate sub lists
				final int N = categories.size();
				int type = Category.NO_FLAGS_CATEGORY;
				int subStart = 0;
				for ( int i = 0; i < N; ++i ) {
		
					Category category = categories.get(i);
		
					if ( type == Category.NO_FLAGS_CATEGORY )
						type = category.getTypeFlag();

					if ( (category.getTypeFlag() != type) || ((i + 1) == N) ) {
						// type has changed, get the previous type sub list
						int subEnd = i;
						// loop a 2nd time if there's a single item at the end to save
						for ( int loopCnt = ((subEnd + 1) == N ? 2 : 1); loopCnt > 0; --loopCnt ) {
							switch ( type ) {
								case Category.EXPENSE_CATEGORY:
									categoriesExpense = new ArrayList<Category>(categories.subList(subStart, subEnd));
									break;
								case Category.INCOME_CATEGORY:
									categoriesIncome = new ArrayList<Category>(categories.subList(subStart, subEnd));
									break;
								case Category.TRANSFER_CATEGORY:
									categoriesTransfer = new ArrayList<Category>(categories.subList(subStart, subEnd));
									break;
								case Category.IMBALANCE_CATEGORY:
									categoriesImbalance = new ArrayList<Category>(categories.subList(subStart, subEnd));
									break;
								case Category.SPLIT_CATEGORY: {
									// special case should be only one, but handle properly anyway
									ArrayList<Category> categoriesSplit = new ArrayList<Category>(categories.subList(subStart, subEnd));
									for ( Category split : categoriesSplit ) {
										if ( split.isRootTypeFlag() ) {
											categorySplit = split;	// remember split for convenience
											break;
										}
									}
									break;
								}
								case Category.UNASSIGNED_CATEGORY:
									categoriesUnassigned = new ArrayList<Category>(categories.subList(subStart, subEnd));
									break;
							}
							subStart = subEnd;
							type = category.getTypeFlag();
							if ( (subStart + 1) == N )
								subEnd = N;	// to save single item at end
						}						
					}
				}
			}
		}
	}
	
	/**
	 * Get list of payees from ContentProvider
	 */
	private void loadPayeesFromProvider(Bundle savedInstanceState) { 
		 
		payees.clear();		// Clear the existing array
		
		retrieveFromBundle(savedInstanceState, BUNDLE_PAYEE_TAG);

		if ( payees.isEmpty() ) {
			payees = Payee.loadPayeesFromProvider(getContentResolver());
		}

		if ( payee != null && !payees.contains(payee) )
			payee = null;
	}
	
	/**
	 * Get list of transfers from ContentProvider
	 */
	private void loadTransfersFromProvider(Bundle savedInstanceState) { 
		 
		transfers.clear();		// Clear the existing array
		
		retrieveFromBundle(savedInstanceState, BUNDLE_TRANSFER_TAG);

		if ( transfers.isEmpty() ) {
			transfers = Transfer.loadTransfersFromProvider(getContentResolver());
		}

		if ( transfer != null && !transfers.contains(payee) )
			transfer = null;
	}
	
	/**
	 * Add a new payee to the list of payees in the ContentProvider
	 */
	private void addPayeeToProvider(Payee newPayee) { 

		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		
		// add to database
		values.put(DatabaseManager.PAYEE_NAME, newPayee.getName());
		Uri result = cr.insert(DatabaseManager.PAYEE_URI, values);
		
		// update with id from database 
		newPayee.setId(Integer.parseInt(result.getLastPathSegment()));

		payees.add( newPayee );
	}

	/**
	 * Add the transaction to the database
	 */
	private void addTransactionToProvider() {
		
		// add the new transaction
		ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();								// basic transaction 
		ArrayList<ContentValues> addTransactions = new ArrayList<ContentValues>();	// list of all transactions to be added, i.e. parent followed by children

		// transaction type, accounts
		long destAccount = -1;
		long srcAccount = -1;
		int transType;
		switch ( radioType.getCheckedRadioButtonId() ) {
			case R.id.addTransaction_radioCredit:
				destAccount = account.getId();
				transType = Transaction.TRANSACTION_CREDIT;
				break;
			case R.id.addTransaction_radioDebit:
				srcAccount = account.getId();
				transType = Transaction.TRANSACTION_DEBIT;
				break;
//			case R.id.addTransaction_radioTransfer:
			default:
				srcAccount = account.getId();
				destAccount = accountTo.getId();
				transType = Transaction.TRANSACTION_TRANSFER;
				break;
		}
		if ( destAccount > 0 )
			values.put(DatabaseManager.TRANSACTION_DEST, destAccount);
		if ( srcAccount > 0 )
			values.put(DatabaseManager.TRANSACTION_SRC, srcAccount);
		
		values.put(DatabaseManager.TRANSACTION_TYPE, transType);
		
		// transaction payee
		values.put(DatabaseManager.TRANSACTION_PAYEE, payee.getId());
		
		// transaction date (DatabaseManager will use CURRENT_TIMESTAMP if field isn't present)
		if ( transactionDate != null ) {
			values.put(DatabaseManager.TRANSACTION_SENDDATE, DatabaseManager.makeDatabaseTimestamp(transactionDate));
			
			if ( completeDate == null )
				completeDate = transactionDate;		// complete not set, so use transaction date
		}

		// transaction complete date (DatabaseManager will use CURRENT_TIMESTAMP if field isn't present)
		if ( completeDate != null ) {
			values.put(DatabaseManager.TRANSACTION_RECVDATE, DatabaseManager.makeDatabaseTimestamp(completeDate));
		}

		// transaction status
		int transactionStatus = Transaction.TRANSSTATUS_COMPLETE;	// assume immediate transfer, so mark complete
		if ( transactionDate != null && transactionDate.compareTo(completeDate) != 0 )
			transactionStatus = Transaction.TRANSSTATUS_INPROGRESS;	// transfer in progress
		values.put(DatabaseManager.TRANSACTION_STATUS, transactionStatus);
		
		
		// TODO estimate processing
//				values.put(DatabaseManager.TRANSACTION_FLAGS, Transaction.TRANSACTION_FLAG_ESTIMATE);
//				values.put(DatabaseManager.TRANSACTION_MIN, DatabaseManager.makeDatabaseMonetaryEstimate(double amount));
//				values.put(DatabaseManager.TRANSACTION_MAX, DatabaseManager.makeDatabasePercentEstimate(double amount));
		
		// transaction reference
		String miscString = editTextRef.getText().toString();
		if ( !TextUtils.isEmpty(miscString) )
			values.put(DatabaseManager.TRANSACTION_REF, miscString);

		// transaction note
		miscString = editTextNote.getText().toString();
		if ( !TextUtils.isEmpty(miscString) )
			values.put(DatabaseManager.TRANSACTION_NOTE, miscString);

		// transaction amount for single category transaction or multi-category parent transaction is the total amount
		values.put(DatabaseManager.TRANSACTION_AMOUNT, transactionAmount);

		// transaction rate
		if ( transactionRate > 0.0 )
			values.put(DatabaseManager.TRANSACTION_RATE, transactionRate);

		// transaction categories & amounts
		if ( (categoriesSelected == null) || (categoriesSelected.size() == 0) ) {
			// nothing selected, so alloc all to unassigned
			values.put(DatabaseManager.TRANSACTION_CATEGORY, categoryUnassigned.getId());
			addTransactions.add(values);	// add to list of transactions to commit
		}
		else if ( categoriesSelected.size() == 1 ) {
			// alloc all to selected category
			values.put(DatabaseManager.TRANSACTION_CATEGORY, categoriesSelected.get(0).getCategory().getId());
			addTransactions.add(values);	// add to list of transactions to commit
		}
		else {
			// multiple categories
			
			// add parent transaction to list
			values.put(DatabaseManager.TRANSACTION_CATEGORY, categorySplit.getId());
			addTransactions.add(values);	// add to list of transactions to commit

			// generate child transactions & add to list
			double unallocedAmount = transactionAmount;
			for ( TransactionCategory transCat : categoriesSelected ) {
				// add categories ignoring unassigned as everything left over will be assigned to it below
				if ( !transCat.isSameCategory(categoryUnassigned) ) {
					double amount = transCat.getAmount();
					if ( amount > 0 ) {
						// reduce unassigned amount
						addTransactions.add(getCategoryValues(values, transCat.getId(), amount));	// add to list of transactions to commit

						unallocedAmount -= amount;
					}
					// else ignore categories with no amount
				}
			}
			
			// add everything left as unassigned
			if ( unallocedAmount > 0 ) {
				addTransactions.add(getCategoryValues(values, categoryUnassigned.getId(), unallocedAmount));	// add to list of transactions to commit
			}
		}
		

		long parentId = DatabaseManager.TRANSACTION_PARENT_ID;	// no parent by default
		Uri oldTransParent = null;		// uri of the parent trans of old transaction group
		Uri newTransParent = null;		// uri of the parent trans of a new transaction group

		if ( editTransactionParent != null ) {
			// in edit mode
			ArrayList<ContentValues> updateTransactions = new ArrayList<ContentValues>();	// category transactions to update after edit
			ArrayList<Long> affectedAccounts = new ArrayList<Long>(); 

			// check if parent has changed
			try {
				Transaction newTransactionParent = (Transaction) editTransactionParent.clone();

				parentId = editTransactionParent.getTransId();
				
				// update parent with any changes
				values = addTransactions.get(0);
				newTransactionParent.updateTransactionFromValues(values);
				if ( !newTransactionParent.equals(editTransactionParent) ) {
					// parent has changed so add to update list
					values.put(DatabaseManager.TRANSACTION_ID, parentId);
					updateTransactions.add(values);

					// uri for old transaction parent
					oldTransParent = ContentUris.withAppendedId(DatabaseManager.TRANSACTION_URI, parentId);
				}
				// remove from addTransactions list, either unchanged or added to update list
				addTransactions.remove(values);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}

			String[] accCols = new String[] {
					DatabaseManager.TRANSACTION_DEST,
					DatabaseManager.TRANSACTION_SRC
			};

			// check if categories have changed
			if ( editTransactionCategories != null ) {

				ArrayList<Transaction> delCategories = (ArrayList<Transaction>) editTransactionCategories.clone();

				for ( int i = addTransactions.size() - 1; i >= 0; --i ) {
					values = addTransactions.get(i);
					long category = values.getAsLong(DatabaseManager.TRANSACTION_CATEGORY);
					for ( Transaction transCat : editTransactionCategories ) {
						if ( transCat.getTransCategory() == category ) {
							// already there, check if changed
							double amount = values.getAsDouble(DatabaseManager.TRANSACTION_AMOUNT);
							if ( transCat.getTransAmt() != amount ) {
								// changed so add to updateTransactions list
								values.put(DatabaseManager.TRANSACTION_ID, transCat.getTransId());
								updateTransactions.add(values);
							}
							// remove from addTransactions list, either unchanged or added to update list
							addTransactions.remove(i);
							
							delCategories.remove(transCat);	// remove from del list
						}
					}
				}

				// delete any categories that are not needed
				final int R = delCategories.size();
				if ( R > 0 ) {
					// need to delete some of the previous categories
					long[] ids = new long[R];
					for ( int i = 0; i < R; ++i )
						ids[i] = delCategories.get(i).getTransId();
					if ( Transaction.deleteTransactionFromProvider(cr, ids) ) {
						for ( int j = accCols.length - 1; j >= 0; --j ) {
							if ( values.containsKey(accCols[j]) ) {
								affectedAccounts.add( values.getAsLong(accCols[j]) );
							}
						}
					}
				}
			}
			
			// update any categories that need to be
			for ( int i = updateTransactions.size() - 1; i >= 0; --i ) {
				values = updateTransactions.get(i);
				long id = values.getAsLong(DatabaseManager.TRANSACTION_ID);	// get id
				values.remove(DatabaseManager.TRANSACTION_ID);				// remove id as it not changing
				if ( Transaction.updateTransactionInProvider(cr, id, values) ) {
					for ( int j = accCols.length - 1; j >= 0; --j ) {
						if ( values.containsKey(accCols[j]) ) {
							affectedAccounts.add( values.getAsLong(accCols[j]) );
						}
					}
				}
			}

			for ( Long num : affectedAccounts ) {
				if ( !updatedAccounts.contains(num) )
					updatedAccounts.add(num);
			}
		}
		

		// add the transaction list to the database
		final int N = addTransactions.size();
		for ( int i = 0; i < N; ++i ) {
			values = addTransactions.get(i);

			// update the child transaction with its parent id if necessary
			if ( parentId != DatabaseManager.TRANSACTION_PARENT_ID ) {
				values.put(DatabaseManager.TRANSACTION_PARENT, parentId);
			}

			Uri result = cr.insert(DatabaseManager.TRANSACTION_URI, values);
			if ( result != null ) {
				// add account id to list of updated accounts
				long[] transAcc = new long[] {
						destAccount, srcAccount
				};
				for ( int j = transAcc.length - 1; j >= 0; --j ) {
					if ( transAcc[j] > 0 ) {
						Long num = Long.valueOf(transAcc[j]);
						if ( !updatedAccounts.contains(num) )
							updatedAccounts.add(num);
					}
				}
			}
			
			// retrieve the parent transaction id if necessary
			if ( parentId == DatabaseManager.TRANSACTION_PARENT_ID ) {
				parentId = ContentUris.parseId(result);
				
				newTransParent = result;	// uri for new transaction parent
			}
		}
		
		// update users
		boolean haveOld = (oldTransParent != null);
		boolean haveNew = (newTransParent != null);
		if ( haveOld || haveNew ) {
			SmsProcessor.SmsReport reportType;
			Uri[] uri;
			if ( haveOld && haveNew ) {
				uri = new Uri[] { oldTransParent, newTransParent };
				reportType = SmsProcessor.SmsReport.SMS_UPDATE_REPORT;
			}
			else if ( haveNew ) {
				uri = new Uri[] { newTransParent };
				reportType = SmsProcessor.SmsReport.SMS_NEW_REPORT;
			}
			else {
				uri = new Uri[] { oldTransParent };
				reportType = SmsProcessor.SmsReport.SMS_DELETE_REPORT;
			}
			updateUsers(uri, reportType);
		}
	}

	
	/**
	 * Update users with the transaction details
	 * @param uri			- Uri of transactions to update users about
	 * @param reportType	- type of update to send
	 */
	private void updateUsers(Uri[] uri, SmsProcessor.SmsReport reportType) {
		SmsProcessor smsManager = SmsProcessor.getInstance(getApplicationContext());
		smsManager.updateUsers(uri, reportType, getContentResolver());
	}
	
	
	
	/**
	 * Return a new ContentValues updated with the specified category id & amount.
	 * @param values		- Base ContentValues to use
	 * @param categoryId	- category id
	 * @param amount		- amount
	 * @return
	 */
	private ContentValues getCategoryValues(ContentValues values, long categoryId, double amount) {
		ContentValues categoryValues = new ContentValues(values);
		categoryValues.put(DatabaseManager.TRANSACTION_CATEGORY, categoryId);
		categoryValues.put(DatabaseManager.TRANSACTION_AMOUNT, amount);
		return categoryValues;
	}
	
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		sendBroadcast(new Intent(Constants.UPDATE_SNAPSHOT_ACTION));
		super.onPause();
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_transaction, menu);
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
	 * Class to store the account related information required by the AddTransaction activity.<br>
	 * This avoids using Account objects which would require more overhead.
	 * @author Ian Buttimer
	 *
	 */
	private static class TransactionAccount extends TextViewAdapterBase implements TextViewAdapterInterface, Parcelable {
		
		/* fields stored in super class:
		 * - account id
		 * - account name
		 *  */
		protected String nickname;	// nickname
		protected int minorUnits;	// minor units
		protected String code;		// currency code
		protected String symbol;	// currency symbol
		
		
		/**
		 * @param id			- account id
		 * @param name			- account name
		 * @param nickname		- account nickname
		 * @param minorUnits	- currency minor units
		 * @param code			- currency code
		 * @param symbol		- currency symbol
		 */
		public TransactionAccount(int id, String name, String nickname,
				int minorUnits, String code, String symbol) {
			super(id, name);
			this.nickname = nickname;
			this.minorUnits = minorUnits;
			this.code = code;
			this.symbol = symbol;
		}

		/**
		 * @return the account nickname
		 */
		public String getNickname() {
			return nickname;
		}

		/**
		 * @return the currency minorUnits
		 */
		public int getMinorUnits() {
			return minorUnits;
		}

		/**
		 * @return the currency code
		 */
		public String getCode() {
			return code;
		}

		/**
		 * @return the currency symbol
		 */
		public String getSymbol() {
			return symbol;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getClass().getSimpleName() + " [nickname=" + nickname + ", minorUnits="
					+ minorUnits + ", code=" + code + ", symbol=" + symbol
					+ ", toString()=" + super.toString() + "]";
		}


		/* Parcelable interface - related functions */
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int writeflags) {
			super.writeToParcel(dest, writeflags);
			dest.writeString(nickname);	// nickname
			dest.writeInt(minorUnits);	// minor units
			dest.writeString(code);		// currency code
			dest.writeString(symbol);	// currency symbol
		}
		
		public static final Parcelable.Creator<TransactionAccount> CREATOR = new Parcelable.Creator<TransactionAccount>() {
			public TransactionAccount createFromParcel(Parcel in) {
				return new TransactionAccount(in);
			}
			
			public TransactionAccount[] newArray(int size) {
				return new TransactionAccount[size];
			}
		};
		
		private TransactionAccount(Parcel in) {
			super(in);
			nickname = in.readString();	// nickname
			minorUnits = in.readInt();	// minor units
			code = in.readString();		// currency code
			symbol = in.readString();	// currency symbol
		}

	}

	/**
	 * Class to store a category related information required by the AddTransaction activity.<br>
	 * @author Ian Buttimer
	 *
	 */
	public static class TransactionCategory implements TextViewAdapterInterface, Parcelable {
		
		protected Category category;	// category info
		protected double amount;		// amount assigned to category

		/**
		 * @param category
		 * @param selected
		 */
		public TransactionCategory(Category category, double amount) {
			super();
			this.category = category;
			this.amount = amount;
		}

		/**
		 * @param category
		 * @param selected
		 */
		public TransactionCategory(Category category) {
			this(category, 0);
		}

		/**
		 * @return the category
		 */
		public Category getCategory() {
			return category;
		}

		/**
		 * @param category the category to set
		 */
		public void setCategory(Category category) {
			this.category = category;
		}

		/**
		 * @return the amount
		 */
		public double getAmount() {
			return amount;
		}

		/**
		 * @param amount the amount to set
		 */
		public void setAmount(double amount) {
			this.amount = amount;
		}

		/**
		 * @param amount the amount to add
		 */
		public void addAmount(double amount) {
			this.amount += amount;
		}

		/**
		 * Checks if the specified category is the same as the category of this object
		 * @param category
		 * @return
		 */
		public boolean isSameCategory(Category category) {
			return this.category.equals(category);
		}
		
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TransactionCategory [category=" + category + ", amount="
					+ amount + "]";
		}


		public static class CompareLevelPathName implements Comparator<TransactionCategory> {

			@Override
			public int compare(TransactionCategory lhs, TransactionCategory rhs) {

				return new Category.CompareLevelPathName().compare(lhs.getCategory(), rhs.getCategory());
			}
		};


		/* Parcelable interface - related functions */
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int writeflags) {
			
			dest.writeParcelable(category, writeflags);
			dest.writeDouble(amount);
		}
		
		public static final Parcelable.Creator<TransactionCategory> CREATOR = new Parcelable.Creator<TransactionCategory>() {
			public TransactionCategory createFromParcel(Parcel in) {
				return new TransactionCategory(in);
			}
			
			public TransactionCategory[] newArray(int size) {
				return new TransactionCategory[size];
			}
		};
		
		private TransactionCategory(Parcel in) {
			category = in.readParcelable(null);
			amount = in.readDouble();
		}

		/* TextViewAdapterInterface interface related functions */
		
		@Override
		public String toDisplayString() {
			return category.toDisplayString() + "\n" + amount;
		}

		@Override
		public long getId() {
			return category.getId();
		}

		@Override
		public void setPrefix(String prefix) {
			category.setPrefix(prefix);
		}

		@Override
		public void setId(long id) {
			// nop
		}

		@Override
		public void setName(String name) {
			// nop
		}

		@Override
		public String getName() {
			return category.getName();
		}

		@Override
		public String getPrefix() {
			return category.getPrefix();
		}
	}

	
	/**
	 * Get the date of this transaction
	 * @return
	 */
	public GregorianCalendar getTransactionDate() {
		if ( transactionDate == null )
			return (GregorianCalendar) Calendar.getInstance();	// Use the current time as the default
		else
			return (GregorianCalendar) transactionDate.clone(); 
	}
	
	
	private static final int IGNORE_DATE_FIELD = -1;
	private void setTransactionDate(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute) {

		if ( transactionDate == null ) {
			if ( year != IGNORE_DATE_FIELD )
				transactionDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
			else
				transactionDate = new GregorianCalendar();
		}
		if ( year != IGNORE_DATE_FIELD )
			transactionDate.set(year, monthOfYear, dayOfMonth);
		if ( hourOfDay != IGNORE_DATE_FIELD ) {
			transactionDate.set(GregorianCalendar.HOUR_OF_DAY, hourOfDay);
			transactionDate.set(GregorianCalendar.MINUTE, minute);
		}

		updateTransactionDateTime();
	}

	private void setTransactionDate(GregorianCalendar cal) {
		setTransactionDate(cal.get(GregorianCalendar.YEAR), 
							cal.get(GregorianCalendar.MONTH), 
							cal.get(GregorianCalendar.DAY_OF_MONTH),
							cal.get(GregorianCalendar.HOUR_OF_DAY), 
							cal.get(GregorianCalendar.MINUTE));
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		setTransactionDate(year, monthOfYear, dayOfMonth, IGNORE_DATE_FIELD, IGNORE_DATE_FIELD);
	}


	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		setTransactionDate(IGNORE_DATE_FIELD, IGNORE_DATE_FIELD, IGNORE_DATE_FIELD, hourOfDay, minute);
	}

	
	/**
	 * Update the transaction date/time display
	 */
	private void updateTransactionDateTime() {
	
		// update date display with selected date
		DateTimeFormat df = new DateTimeFormat(this);
		Date date = transactionDate.getTime();
		textViewDate.setText(df.formatMediumDateTime(date));

		updateTransferTextView();
	}
	
	
	/**
	 * Update the transfer complete date text
	 */
	private void updateTransferTextView() {
		
		String str = null;
		final String indent = "  ";
		Resources r = getResources();
		
		// update date button with selected date
		if ( transfer != null ) {
			
			completeDate = (GregorianCalendar) transfer.calcTransferCompleteDate(transactionDate);
			if ( completeDate != null ) {
				
				DateTimeFormat df = new DateTimeFormat(this, DateTimeFormat.MEDIUM, DateTimeFormat.FORMAT_DATE_TIME);
				str = r.getText(R.string.addtransaction_transfer) + " - " + transfer.getName() + "\n" +
						indent + r.getText(R.string.addtransaction_recv_date) + " - " + df.format(completeDate.getTime());
			}
		}
		if ( str == null )
			str = (String) r.getText(R.string.addtransaction_transfer_end_date_not_set);
		textViewTransfer.setText(indent + str);
	}

	
	
	@Override
	public void onEditDone(int result, Intent intent) {
		onActivityResult(SHOW_EDIT_CATEGORY, result, intent);
	}


	@Override
	public void onEditDone(int result) {
		onActivityResult(SHOW_EDIT_CATEGORY, result, null);
	}


	@Override
	public void onEditDone(int request, int result, Intent data) {
		onActivityResult(request, result, data);
	}


	@Override
	public void onEditDone(int request, int result) {
		onActivityResult(request, result, null);
	}

}

