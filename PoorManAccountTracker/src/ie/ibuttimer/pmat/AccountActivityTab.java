/**
 * 
 */
package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.db.AccountCurrency;
import ie.ibuttimer.pmat.db.Category;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.Transaction;
import ie.ibuttimer.pmat.util.AudioPlayer;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.widget.AlertMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * This class is a tab which displays an accounts activity 
 * @author Ian Buttimer
 *
 */
public class AccountActivityTab extends Fragment {

	private int accountId;
	private String currencySymbol;
	private String currencyCode;
	private ExpandableListView transactions;
	private Spinner spinnerFrom;
	private Spinner spinnerSortBy;
	private TableRow noTransRow;
	
	private ContentResolver cr;

    private ExpandableListAdapter mAdapter; 
    
    private List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
    private List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

    private boolean attached;
    
	/**
	 * 
	 */
	public AccountActivityTab() {
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#setArguments(android.os.Bundle)
	 */
	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		
		accountId = args.getInt(Constants.ACCOUNT_ID_NUM, 0);
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.activity_account_tab, container, false);

		 cr = container.getContext().getContentResolver();
		 
		 transactions = (ExpandableListView)rootView.findViewById(R.id.accountActivity_expandableListView);
		 spinnerFrom = (Spinner)rootView.findViewById(R.id.accountActivity_spinnerFrom);
		 spinnerSortBy = (Spinner)rootView.findViewById(R.id.accountActivity_spinnerSortBy);
		 noTransRow = (TableRow)rootView.findViewById(R.id.accountActivity_tableRowNoTransactions);

		 // get arguments
		 getArguments(accountId, cr, rootView);

		 setupFromSpinner();
		 setupSortSpinner();
		 
		 populateTransactions(cr, rootView, true);

		 return rootView;
	}

	
	/**
	 * Setup the display range spinner
	 */
	private void setupFromSpinner() {
		
		setupSimpleSpinner(spinnerFrom, R.array.pref_transaction_range_titles, 
				R.array.pref_transaction_range_values, PreferenceControl.getDefaultTransactionRange(getActivity()));
	}
	
	/**
	 * Setup the sort by spinner
	 */
	private void setupSortSpinner() {
		
		setupSimpleSpinner(spinnerSortBy, R.array.pref_transaction_sortby_titles, 
				R.array.pref_transaction_sortby_values, PreferenceControl.getDefaultTransactionSortOrder(getActivity()));
	}
	
	/**
	 * Setup simple spinner based on preferences
	 * @param s			- spinner to setup
     * @param titleId	- Resource id of display items string array
     * @param valueId	- Resource id of value items string array
	 * @param dfltValue	- Default preference value
	 */
	private void setupSimpleSpinner(Spinner s, int titleId, int valueId, int dfltValue) {
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		 ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				 													titleId, android.R.layout.simple_spinner_item);
		 // Specify the layout to use when the list of choices appears
		 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		 // Apply the adapter to the spinner
		 s.setAdapter(adapter);

		 // set the app default range
		 setSelectedValue(s, titleId, valueId, dfltValue);

		 s.setOnItemSelectedListener( new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				 populateTransactions(cr, parent, false);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nop
			}
		 });
	}

	
	/**
	 * Retrieve the arguments from the intent that started this activity 
	 * @param accountId	- account id
	 * @param cr		- Content resolver
	 * @param rootView	- View to add info too
	 */
	private void getArguments(int accountId, ContentResolver cr, View rootView) {
		
		final int[] tvIds = {
				R.id.accountActivity_textViewName,
				R.id.accountActivity_textViewNickname,
				R.id.accountActivity_textViewCurrentBal,
				R.id.accountActivity_textViewAvailableBal,
		};
		
		if ( accountId > 0 ) {
			Uri accUri = ContentUris.withAppendedId(DatabaseManager.ACCOUNT_BASIC_URI, accountId);
			Cursor c = cr.query(accUri, null, null, null, null);	// Return the required account
			if (c.moveToFirst()) {
				int[] indices = new int[tvIds.length];
				indices[0] = c.getColumnIndex(DatabaseManager.ACCOUNT_NAME);
				indices[1] = c.getColumnIndex(DatabaseManager.ACCOUNT_NICKNAME);
				indices[2] = c.getColumnIndex(DatabaseManager.ACCOUNT_CURRENTBAL);
				indices[3] = c.getColumnIndex(DatabaseManager.ACCOUNT_AVAILBAL);

				int currencyIdx = c.getColumnIndex(DatabaseManager.CURRENCY_SYMBOL);
				int codeIdx = c.getColumnIndex(DatabaseManager.CURRENCY_CODE);

				do {
					// Extract the details and update activity
					for ( int i = tvIds.length - 1; i >= 0; --i ) {
						TextView tv = (TextView)rootView.findViewById(tvIds[i]);
						StringBuffer sb = new StringBuffer("  ");
						String field;
						
						switch ( tvIds[i] ) {
							case R.id.accountActivity_textViewCurrentBal:
							case R.id.accountActivity_textViewAvailableBal:
								currencySymbol = c.getString(currencyIdx);
								currencyCode = c.getString(codeIdx);
								
								sb.append(currencySymbol);
								
								Double amt = c.getDouble(indices[i]);
								field = AccountCurrency.formatDouble(currencyCode, amt);
								break;
							default:
								field = c.getString(indices[i]);
								break;
						}
						sb.append(field);

						tv.setText(sb.toString());
					}
				} while(c.moveToNext());
			}
			c.close();
		}
		else {
			// no account so nothing to display
			for ( int i = tvIds.length - 1; i >= 0; --i ) {
				TextView tv = (TextView)rootView.findViewById(tvIds[i]);
				tv.setText(R.string.not_available);
			}
		}
	}

	
	private String DATE_FORMAT_DM = "dd MMM";		// short format for when all transactions are the same year as current
	private String DATE_FORMAT_DMY = "dd MMM yy";	// medium format for when any transactions are not the same year as current
	/**
	 * Make an individual transaction date string.
	 * @param dateStr	- Transaction date
	 * @param dateFmt	- date format to use
	 * @return			Formatted string
	 */
	private String makeTransactionDateString(String dateStr, String dateFmt) {
		Date date = DatabaseManager.parseDatabaseTimestamp(dateStr);
		SimpleDateFormat sdf = new SimpleDateFormat(dateFmt, Locale.US);
		return sdf.format(date);
	}

	/**
	 * Make an individual transaction amount string.
	 * @param amount	- Transaction amount
	 * @param type		- Transaction type, one of Transaction.TRANSACTION_CREDIT etc.
	 * @return			Formatted string
	 */
	private String makeTransactionAmountString(Double amount, int type) {
		String amt = currencySymbol + AccountCurrency.formatDouble(currencyCode, amount);
		switch ( type ) {
			case Transaction.TRANSACTION_DEBIT:
				amt += " (-)";
				break;
			case Transaction.TRANSACTION_CREDIT:
				amt += " (+)";
				break;
		}
		return amt;
	}

	// fields for transaction detail widgets
	private final static int MAIN_LAYOUT = R.layout.activity_account_transaction_summary_view;
	private final static int[] MAIN_VIEWS = new int[] { 
		R.id.accountTransactionDetail_textViewSummaryDate,
		R.id.accountTransactionDetail_textViewSummaryPayee,
		R.id.accountTransactionDetail_textViewSummaryAmount,
	};
	private static final String[] mainDetails = new String[] { 
		DatabaseManager.TRANSACTION_SENDDATE,
		DatabaseManager.TRANSACTION_PAYEE,
		DatabaseManager.TRANSACTION_AMOUNT,
		// following items are for info passing not display
		DatabaseManager.TRANSACTION_ID,
		AlertMessage.TITLE_ARG,
	};

	private final static int EXPANDED_LAYOUT = R.layout.activity_account_transaction_detail_view;
	private final static int[] EXPANDED_VIEWS = new int[] {
		R.id.accountTransactionDetail_textViewCategoryName,
		R.id.accountTransactionDetail_textViewCategoryAmt,
		R.id.accountTransactionDetail_textViewRef,
		R.id.accountTransactionDetail_textViewNote,
	};
	private static final String[] expandedDetails = new String[] {
		DatabaseManager.TRANSACTION_CATEGORY,
		DatabaseManager.TRANSACTION_AMOUNT,
		DatabaseManager.TRANSACTION_REF,
		DatabaseManager.TRANSACTION_NOTE,
	};
	private final int expandedDetailsLen = expandedDetails.length;

	private final String ACCOUNT_CRITERIA = "account";
	private final String DATE_CRITERIA = "date";
	private final String PARENT_CRITERIA = "parent";
	private static class SelectionDetails {
		String selection;
		int numArgs;
		/**
		 * @param selection
		 * @param numArgs
		 */
		public SelectionDetails(String selection) {
			this.selection = selection;
			String[] splits = selection.split("[?]");
			this.numArgs = splits.length - 1;
		}
		
	}
	private static final int MAX_SELECTIONS_ARGS;
	private static final SelectionDetails[] selections;
	private final static int ACCOUNT_CRITERIA_IDX = 0;
	private final static int DATE_CRITERIA_IDX = 1;
	private final static int PARENT_CRITERIA_IDX = 2;
	private final static int NUM_SELECTIONS = 3;
	static {
		selections = new SelectionDetails[NUM_SELECTIONS];
		selections[ACCOUNT_CRITERIA_IDX] = new SelectionDetails("(" + DatabaseManager.TRANSACTION_SRC + "=? OR " + DatabaseManager.TRANSACTION_DEST + "=?)");
		selections[DATE_CRITERIA_IDX] = new SelectionDetails("(" + DatabaseManager.TRANSACTION_SENDDATE + ">=?)");
		selections[PARENT_CRITERIA_IDX] = new SelectionDetails("(" + DatabaseManager.TRANSACTION_PARENT + "=?)");
		int selectionArgs = 0;
		for ( int i = selections.length - 1; i >= 0; --i )
			selectionArgs += selections[i].numArgs;
		MAX_SELECTIONS_ARGS = selectionArgs;
	}
	private final String SELECTION_STR = "selStr";
	private final String SELECTION_ARG = "selArg";

	/**
	 * Get the selection criteria for a query
	 * @param in	- Bundle containing which criteria to use
	 * @return
	 */
	private Bundle getQueryCriteria(Bundle in) {
		
		String[] fields = new String[selections.length];
		int args = 0;
		int sels = 0;

		StringBuffer selection = new StringBuffer();
		String[] selectionArgs = new String[MAX_SELECTIONS_ARGS];

		if ( in.containsKey(ACCOUNT_CRITERIA) ) {
			String accountId = Integer.toString( in.getInt(ACCOUNT_CRITERIA) );
			
			fields[sels++] = selections[ACCOUNT_CRITERIA_IDX].selection;
			selectionArgs[args++] = accountId;
			selectionArgs[args++] = accountId;
		}
		if ( in.containsKey(DATE_CRITERIA) ) {
			Calendar date = (Calendar) in.getSerializable(DATE_CRITERIA);
			
			fields[sels++]= selections[DATE_CRITERIA_IDX].selection;
			selectionArgs[args++] = DatabaseManager.makeDatabaseTimestamp(date);
		}
		if ( in.containsKey(PARENT_CRITERIA) ) {
			String parentId = Integer.toString( in.getInt(PARENT_CRITERIA) );

			fields[sels++]= selections[PARENT_CRITERIA_IDX].selection;
			selectionArgs[args++] = parentId;
		}
		
		for ( int i = 0; i < sels; ++i ) {
			if ( selection.length() > 0 )
				selection.append(" AND ");
			selection.append(fields[i]);
		}
		Bundle out = new Bundle();
		out.putString(SELECTION_STR, selection.toString());
		out.putStringArray(SELECTION_ARG, Arrays.copyOf(selectionArgs, args));

		return out;
	}
	
	
    /**
     * Populate the transaction list.
     */
    private void populateTransactions(ContentResolver cr, View view, boolean create) {

    	int noTransVisibility = View.VISIBLE;	// display no trans msg by default
    	
        // Build adapter with contact entries
		if ( accountId > 0 ) {
			
			Bundle criteria = new Bundle();
			
			criteria.putInt(ACCOUNT_CRITERIA, accountId);
			criteria.putInt(PARENT_CRITERIA, DatabaseManager.TRANSACTION_PARENT_ID);	// parents only

			Calendar start = PreferenceControl.getTransactionRangeStartDate(getActivity(), getDisplayRange());
			if ( start != null )
				criteria.putSerializable(DATE_CRITERIA, start);	// add start date to selection criteria
			
			Bundle dbSelection = getQueryCriteria(criteria);

			String selection = dbSelection.getString(SELECTION_STR);
			String[] selectionArgs = dbSelection.getStringArray(SELECTION_ARG);

			// Return the required transactions
			Cursor c = cr.query(DatabaseManager.TRANSACTION_INFO_URI, null, selection, selectionArgs, getDisplaySortOrder());

	        groupData.clear();
	        childData.clear();
	        if ( c.getCount() > 0 ) {
	        
				final int senddateIdx = c.getColumnIndexOrThrow(DatabaseManager.TRANSACTION_SENDDATE);
				final int payeeIdx = c.getColumnIndexOrThrow(DatabaseManager.PAYEE_NAME);
				final int amountIdx = c.getColumnIndexOrThrow(DatabaseManager.TRANSACTION_AMOUNT);
				final int categoryPathIdx = c.getColumnIndexOrThrow(DatabaseManager.CATEGORY_PATH);
				final int categoryIdx = c.getColumnIndexOrThrow(DatabaseManager.CATEGORY_NAME);
				final int idIdx = c.getColumnIndexOrThrow(DatabaseManager.TRANSACTION_ID);
				final int refIdx = c.getColumnIndexOrThrow(DatabaseManager.TRANSACTION_REF);
				final int noteIdx = c.getColumnIndexOrThrow(DatabaseManager.TRANSACTION_NOTE);
				final int typeIdx = c.getColumnIndexOrThrow(DatabaseManager.TRANSACTION_TYPE);
				final int srcIdx = c.getColumnIndexOrThrow(DatabaseManager.TRANSACTION_SRC);
				final int destIdx = c.getColumnIndexOrThrow(DatabaseManager.TRANSACTION_DEST);
				
				noTransVisibility = View.GONE;	// have trans so don't display no trans msg
	        
				// Note: need to be same order as expandedDetails
        		final int[] detailIndices = {
        			categoryIdx,
        			amountIdx,
    				refIdx,
    				noteIdx,
        		};

				// scan to check if all are the same year, and find longest payee
				String dateFmt = DATE_FORMAT_DM;
				if (c.moveToFirst()) {
					Calendar cal = Calendar.getInstance();
					int year = cal.get(Calendar.YEAR);
					do {
						Date d = DatabaseManager.parseDatabaseTimestamp(c.getString(senddateIdx));
						cal.setTime(d);
						int yr = cal.get(Calendar.YEAR);
						if ( year != yr )
							dateFmt = DATE_FORMAT_DMY;
					} while(c.moveToNext());
				}

				// populate details
				if (c.moveToFirst()) {
					do {
		        		// add new group i.e. transaction
		            	Map<String, String> curGroupMap = new HashMap<String, String>();
		            	groupData.add(curGroupMap);
	
	            		ArrayList<String> categories = new ArrayList<String>();
	            		ArrayList<String> amounts = new ArrayList<String>();

		            	Double amount = c.getDouble(amountIdx);
		            	int transType = c.getInt(typeIdx);
		            	int amtType = transType;
		            	
		            	if ( amtType == Transaction.TRANSACTION_TRANSFER ) {
		            		int[] accIds = new int[] {
		            				srcIdx, destIdx
		            		};
		            		amtType = Transaction.TRANSACTION_INVALID;
		            		String srcAcc = null;
		            		String destAcc = null;
		            		String[] projection = new String[] { DatabaseManager.ACCOUNT_NAME	};
		            		
		            		for ( int i = accIds.length - 1; i >= 0; --i ) {
			            		int accId = c.getInt(accIds[i]);
			            		
			            		if ( accId == accountId ) {
			            			amtType = (accIds[i] == srcIdx ? Transaction.TRANSACTION_DEBIT : Transaction.TRANSACTION_CREDIT);
			            		}
			            		
			        			Cursor acc = cr.query(DatabaseManager.ACCOUNT_ACC_URI, projection, 
										DatabaseManager.ACCOUNT_ID + "=" + accId, null, null);
					    		if ( acc.moveToFirst() ) {
					    			if ( accIds[i] == srcIdx )
					    				srcAcc = acc.getString(0);
					    			else
					    				destAcc = acc.getString(0);
					    		}
					    		acc.close();
		            		}

			    			int resId;
			    			String dispAcc;
			    			switch ( amtType ) {
			    				case Transaction.TRANSACTION_DEBIT:
			    					resId = R.string.account_transfer_to;
			    					dispAcc = destAcc;
			    					break;
			    				case Transaction.TRANSACTION_CREDIT:
			    					resId = R.string.account_transfer_from;
			    					dispAcc = srcAcc;
			    					break;
			    				default:
			    					resId = -1;
			    					dispAcc = null;
			    					break;
			    			}
			    			if ( resId > 0 && dispAcc != null ) {
			    				categories.add(getResources().getString(resId) + " " + dispAcc);
		        				amounts.add(makeTransactionAmountString(amount, Transaction.TRANSACTION_INVALID));
			    			}
		            	}

		            	// populate main details
		            	String date = makeTransactionDateString(c.getString(senddateIdx), dateFmt);
		            	String payee = c.getString(payeeIdx);
		            	String amt = makeTransactionAmountString(amount, amtType);
		            	int transId = c.getInt(idIdx);
		            	curGroupMap.put(DatabaseManager.TRANSACTION_SENDDATE, date);
		            	curGroupMap.put(DatabaseManager.TRANSACTION_PAYEE, payee);
		            	curGroupMap.put(DatabaseManager.TRANSACTION_AMOUNT, amt);

		            	curGroupMap.put(DatabaseManager.TRANSACTION_ID, Integer.toString(transId));
		            	curGroupMap.put(AlertMessage.TITLE_ARG, date + " " + payee + " " + amt);
	
		        		// add children for the group i.e. transaction details
		        		List<Map<String, String>> children = new ArrayList<Map<String, String>>();
		        		
	            		Map<String, String> curChildMap = new HashMap<String, String>();
	            		children.add(curChildMap);
	
		            	// populate expanded details
	            		
	        			// Return the required transactions
		            	if ( transType != Transaction.TRANSACTION_TRANSFER ) {
	            		
		            		criteria = new Bundle();
		        			criteria.putInt(PARENT_CRITERIA, transId);	// parent is current trans
		        			dbSelection = getQueryCriteria(criteria);
	
		        			selection = dbSelection.getString(SELECTION_STR);
		        			selectionArgs = dbSelection.getStringArray(SELECTION_ARG);
		        			Cursor subc = cr.query(DatabaseManager.TRANSACTION_INFO_URI, null, selection, selectionArgs, null);
		        			if ( subc.moveToFirst() ) {
		        				// splits
		        				final int amtIdx = subc.getColumnIndexOrThrow(DatabaseManager.TRANSACTION_AMOUNT);
		        				final int catPathIdx = subc.getColumnIndexOrThrow(DatabaseManager.CATEGORY_PATH);
		        				final int catIdx = subc.getColumnIndexOrThrow(DatabaseManager.CATEGORY_NAME);
		        				do {
			        				categories.add(Category.makePath(new String[] {
			        						subc.getString(catPathIdx),
			        						subc.getString(catIdx),
			        				}));
			        				amounts.add(makeTransactionAmountString(subc.getDouble(amtIdx), Transaction.TRANSACTION_INVALID));
		        				}
		        				while ( subc.moveToNext() );
		        			}
		        			else {
		        				// no splits
		        				categories.add(Category.makePath(new String[] {
		        						c.getString(categoryPathIdx),
		        						c.getString(categoryIdx),
		        				}));
		        				amounts.add(makeTransactionAmountString(amount, Transaction.TRANSACTION_INVALID));
		        			}
		        			subc.close();
		            	}

	        			StringBuffer sbCategory = new StringBuffer(); 
	        			StringBuffer sbAmount = new StringBuffer(); 
	        			int N = categories.size();
	        			for ( int i = 0; i < N; ++i ) {
	        				if ( i > 0 ) {
	        					sbCategory.append('\n');
	        					sbAmount.append('\n');
	        				}
        					sbCategory.append(categories.get(i));
        					sbAmount.append(amounts.get(i));
	        			}

                		curChildMap.put(DatabaseManager.TRANSACTION_CATEGORY, sbCategory.toString());
                		curChildMap.put(DatabaseManager.TRANSACTION_AMOUNT, sbAmount.toString());
	            		for ( int i = 0; i < expandedDetailsLen; ++i ) {
	            			if ( curChildMap.containsKey(expandedDetails[i]) )
	            				continue;
	                		String info = c.getString(detailIndices[i]);
	                		if ( !TextUtils.isEmpty(info) )
	                    		curChildMap.put(expandedDetails[i], info);
	            		}

	                	childData.add(children);
	
					} while(c.moveToNext());
				}
	        }
			c.close();
			
			if ( mAdapter == null || create ) {
		    	// Set up the adapter
		    	mAdapter = new AccountExpandableListAdapter(getActivity(),
		    												groupData,
		    												MAIN_LAYOUT,
		    												mainDetails,
		    												MAIN_VIEWS,
		    												childData,
		    												EXPANDED_LAYOUT,
		    												expandedDetails,
		    												EXPANDED_VIEWS
		    	);

		    	transactions.setAdapter(mAdapter);
			}
			else {
				// refresh data
				((BaseExpandableListAdapter) mAdapter).notifyDataSetChanged();
			}
		}

		noTransRow.setVisibility(noTransVisibility);
    }
    
    
    private class AccountExpandableListAdapter extends SimpleExpandableListAdapter {

    	private List<? extends Map<String, ?>> groupData;
    	
		/**
		 * @param context		- The context where the ExpandableListView associated with this SimpleExpandableListAdapter is running
		 * @param groupData		- A List of Maps. Each entry in the List corresponds to one group in the list. The Maps contain the data for each group, and should include all the entries specified in "groupFrom"
		 * @param groupLayout	- resource identifier of a view layout that defines the views for a group. The layout file should include at least those named views defined in "groupTo"
		 * @param groupFrom		- A list of keys that will be fetched from the Map associated with each group.
		 * @param groupTo		- The group views that should display column in the "groupFrom" parameter. These should all be TextViews. The first N views in this list are given the values of the first N columns in the groupFrom parameter.
		 * @param childData		- A List of List of Maps. Each entry in the outer List corresponds to a group (index by group position), each entry in the inner List corresponds to a child within the group (index by child position), and the Map corresponds to the data for a child (index by values in the childFrom array). The Map contains the data for each child, and should include all the entries specified in "childFrom"
		 * @param childLayout	- resource identifier of a view layout that defines the views for a child. The layout file should include at least those named views defined in "childTo"
		 * @param childFrom		- A list of keys that will be fetched from the Map associated with each child.
		 * @param childTo		- The child views that should display column in the "childFrom" parameter. These should all be TextViews. The first N views in this list are given the values of the first N columns in the childFrom parameter.
		 */
		public AccountExpandableListAdapter(Context context,
				List<? extends Map<String, ?>> groupData, int groupLayout,
				String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, String[] childFrom, int[] childTo) {
			super(context, groupData, groupLayout, groupFrom, groupTo, childData,
					childLayout, childFrom, childTo);
			this.groupData = groupData;
		}

		
		/* (non-Javadoc)
		 * @see android.widget.SimpleExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View v = super.getChildView(groupPosition, childPosition, isLastChild,
					convertView, parent);

			Map<String, String> map = (Map<String, String>) groupData.get(groupPosition);
			if ( map.containsKey(DatabaseManager.TRANSACTION_ID) ) {
				
				Bundle b = new Bundle();
				b.putLong(AlertMessage.RESPONSE_ARG, Long.valueOf(map.get(DatabaseManager.TRANSACTION_ID)));
				b.putString(AlertMessage.TITLE_ARG, map.get(AlertMessage.TITLE_ARG));
				
				v.setTag(b);

				v.setOnClickListener(transactionClickListener);
			}

			return v;
		}
    }
    
    
    /* (non-Javadoc)
	 * @see android.app.Fragment#onDetach()
	 */
	@Override
	public void onDetach() {
		attached = false;
		super.onDetach();
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		attached = true;
		super.onAttach(activity);
	}

	/**
     * Get the value based on the spinner selection
     * @param s			- Spinner to get selection from
     * @param titleId	- Resource id of display items string array
     * @param valueId	- Resource id of value items string array
     * @return			Integer representing selected item value, or <code>null</code> if no selection
     */
    private Integer getSelectedValue(Spinner s, int titleId, int valueId) {
    	
    	Integer selection = null;
    	
    	if ( !isRemoving() && attached ) {
        	int idx;
    		Resources r = getActivity().getResources();
	    	String[] titles = r.getStringArray(titleId);
	    	String[] values = r.getStringArray(valueId);
	    	String sel = (String) s.getSelectedItem();
	    	for ( idx = titles.length - 1; idx >= 0; --idx ) {
	    		if ( titles[idx].equals((sel)) )
	    			break;
	    	}
	    	if ( idx >= 0 && values.length > idx )
	    		selection = Integer.valueOf(values[idx]);
    	}
    	return selection;
    }
    
    /**
     * Set the spinner selection to match the required value.
     * @param s			- Spinner to get selection from
     * @param titleId	- Resource id of display items string array
     * @param valueId	- Resource id of value items string array
     * @param setValue	- value to set spinner to represent
     * @return			Set position or -1 if unable to set
     */
    private int setSelectedValue(Spinner s, int titleId, int valueId, int setValue) {

    	int idx = -1;
    	if ( !isRemoving() && attached ) {
	    	Resources r = getActivity().getResources();
	    	String[] titles = r.getStringArray(titleId);
	    	String[] values = r.getStringArray(valueId);
	    	
	    	for ( idx = values.length - 1; idx >= 0; --idx ) {
	    		if ( Integer.valueOf(values[idx]) == setValue )
	    			break;
	    	}
	    	if ( idx >= 0 && titles.length > idx ) {
	    		s.setSelection(idx, false);
	    	}
    	}
    	return idx;
    }
    
    /**
     * Get the display range based on the spinner selection
     * @return	Range setting; one of <code>PREF_TRANSACTION_RANGE_1_DAY</code> etc.
     */
    private int getDisplayRange() {
    	
    	Integer value = getSelectedValue(spinnerFrom, R.array.pref_transaction_range_titles,
    													R.array.pref_transaction_range_values);
    	if ( value == null )
    		value = PreferenceControl.getDefaultTransactionRange(getActivity());
    	return value.intValue();
    }
    
    /**
     * Get the display sort order based on the spinner selection
     * @return	Sort order string for ContentResolver.query() call
     */
    private String getDisplaySortOrder() {
    	
    	String order;
    	Integer value = getSelectedValue(spinnerSortBy, R.array.pref_transaction_sortby_titles,
						R.array.pref_transaction_sortby_values);
		if ( value == null )
			value = PreferenceControl.getDefaultTransactionSortOrder(getActivity());
		switch ( value.intValue() ) {
			case PreferenceControl.PREF_TRANSACTION_SORTBY_AMOUNT_ASC:
			case PreferenceControl.PREF_TRANSACTION_SORTBY_AMOUNT_DSC:
				order = DatabaseManager.TRANSACTION_AMOUNT;
				break;
			case PreferenceControl.PREF_TRANSACTION_SORTBY_PAYEE_ASC:
			case PreferenceControl.PREF_TRANSACTION_SORTBY_PAYEE_DSC:
				order = DatabaseManager.PAYEE_NAME;
				break;
//			case PreferenceControl.PREF_TRANSACTION_SORTBY_DATE_ASC:
//			case PreferenceControl.PREF_TRANSACTION_SORTBY_DATE_DSC:
			default:
				order = DatabaseManager.TRANSACTION_SENDDATE;
				break;
		}
		switch ( value.intValue() ) {
			case PreferenceControl.PREF_TRANSACTION_SORTBY_AMOUNT_ASC:
			case PreferenceControl.PREF_TRANSACTION_SORTBY_PAYEE_ASC:
			case PreferenceControl.PREF_TRANSACTION_SORTBY_DATE_ASC:
				order += " ASC";
				break;
			default:
				order += " DESC";
				break;
		}
		return order;
    }

	/**
	 * Click listener to display edit or delete alert
	 */
	private OnClickListener transactionClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Context c = getActivity();
			AudioPlayer.playButtonClick(c);

			Bundle b = (Bundle) v.getTag();
			Resources r = c.getResources();
			
//			AlertMessage.TITLE_ARG & AlertMessage.RESPONSE_ARG previously added to bundle in AccountExpandableListAdapter 
			b.putString(AlertMessage.MESSAGE_ARG, r.getString(R.string.account_transactionEditDelete));
			b.putString(AlertMessage.BUTTON_LEFT_ARG, r.getString(R.string.account_transactionEdit));
			b.putString(AlertMessage.BUTTON_MIDDLE_ARG, r.getString(R.string.account_transactionDelete));
			b.putString(AlertMessage.BUTTON_RIGHT_ARG, r.getString(android.R.string.cancel));

			AlertMessage msg = new AlertMessage();
			msg.setArguments(b);
			msg.show(getFragmentManager(), "editOrDelete");
		}
	};

	
}
