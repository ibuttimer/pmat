/**
 * 
 */
package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.db.Payee;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.widget.TextViewAdapter;
import ie.ibuttimer.widget.TextViewAdapter.TextViewAdapterFilter;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This class provides the Payee selection logic for both activity-based and fragment-based selection methods.
 * @author Ian Buttimer
 *
 */
public class SelectPayee {

	/** Id of layout used by this dialog */
	public static final int LAYOUT_ID = R.layout.add_transaction_select_payee_view;
	/** Id of title string used by this dialog */
	public static final int TITLE_ID = R.string.addtransaction_select_payee;
	
	private Context context;

	private ArrayList<Payee> payees;				// arraylist provided to adapter (contents change depending on filtering)
	private ArrayList<Payee> payeeArgument;			// copy of original arguments provided 
	private TextViewAdapter<Payee> payeeAdapter;

	private TextViewAdapter<Payee>.TextViewAdapterFilter filter;
	
	private ListView listView;
	private EditText filterText;
	private ImageButton buttonAddNew;
	private TextView instructText;
	
	private DismissDialogInterface finishCallback;	// selection finished callback

	/**
	 * 
	 */
	public SelectPayee(Context context) {
		this.context = context;
	}

	
	/**
	 * Perform initialisation required for a Fragment. Call from Fragment.onCreateView().
	 * @param v	- Inflated layout
	 */
	public void onCreateFragment(View v, DismissDialogInterface i) {
		
		// get references to the activity views
		listView = (ListView)v.findViewById(R.id.selectPayee_listView);
		filterText = (EditText)v.findViewById(R.id.selectPayee_editTextSearchBox);
		buttonAddNew = (ImageButton)v.findViewById(R.id.selectPayee_buttonAddNew);
		instructText = (TextView)v.findViewById(R.id.selectPayee_instruction);
		
		finishCallback = i;
	}

	
	/**
	 * Perform initialisation required for an Activity. Call from Activity.onCreate().
	 * @param a	- Inflated layout
	 */
	public void onCreateActivity(Activity a) {
		
		// get references to the activity views
		listView = (ListView)a.findViewById(R.id.selectPayee_listView);
		filterText = (EditText)a.findViewById(R.id.selectPayee_editTextSearchBox);
		buttonAddNew = (ImageButton)a.findViewById(R.id.selectPayee_buttonAddNew);
		instructText = (TextView)a.findViewById(R.id.selectPayee_instruction);
		
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            finishCallback = (DismissDialogInterface) a;
        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString()
                    + " must implement DismissDialogInterface");
        }

	}

	/**
	 * Get the arguments for the layout
	 * @param args					- arguments 
	 * @param savedInstanceState	- If non-null, this is being re-constructed from a previous saved state as given here.
	 */
	public void getArguments(Bundle args, Bundle savedInstanceState) {

		// get arguments
		Bundle b = (savedInstanceState == null ? args : savedInstanceState);

		payees = b.getParcelableArrayList(Constants.PAYEE_ARRAYLIST);
		payeeArgument = clonePayees();

		
		// set instruction
		instructText.setText( payees.size() > 0 ? R.string.addtransaction_select_or_add_payee : R.string.addtransaction_add_new_payee );
		
		buttonAddNew.setEnabled(false);
		
		// create the adapter to display the payees 
		payeeAdapter = new TextViewAdapter<Payee>(context, 
				R.layout.text_view_adapter_item, R.id.text_view_adapter_item_textViewItem, payees);
		listView.setAdapter(payeeAdapter);
		
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				// return the selected payee
				Intent intent = new Intent();
				Payee payee = (Payee) parent.getItemAtPosition(pos);
				
				intent.putExtra(Constants.PAYEE_SELECTION_RESULT, payee);

				finishCallback.onDoDismiss(Activity.RESULT_OK, intent);
			}
		});
		
		
		// setup filtering on the search box
		filter = (TextViewAdapterFilter) payeeAdapter.getFilter();
		filterText.addTextChangedListener(filterWatacher);
		
		// setup the add new button
		buttonAddNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// return the new payee
				Intent intent = new Intent();
				Payee payee = new Payee(filterText.getText().toString());
				
				intent.putExtra(Constants.PAYEE_ADD_NEW_RESULT, payee);

				finishCallback.onDoDismiss(Activity.RESULT_OK, intent);
			}
		});
		
		enableOptionalItems();
	}

    /**
	 * Called to save the current dynamic state, so it can later be reconstructed in a new instance of its process is restarted.
	 * @param outState	- Bundle in which to place your saved state. 
	 */
	public void onSaveInstanceState(Bundle outState) {

//		outState.putInt(Constants.CATEGORY_TYPE_SELECTED, selectedTypes);
//
//		outState.putParcelableArrayList(Constants.CATEGORY_ARRAYLIST, categories);
//
//		outState.putParcelableArrayList(Constants.CATEGORY_SELECTION_RESULT, selectedCategories);
	}

	
	/**
	 * Utility function to provide a clone of <code>payees</code> and avoid compiler warnings.
	 * @return	Type-correct clone of <code>payees</code>
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<Payee> clonePayees() {
		return (ArrayList<Payee>) payees.clone();
	}
	
	/**
	 * Enable optional items
	 */
	private void enableOptionalItems() {

		boolean enabled = false;

		// enable add new button it the search box text doesn't match a pre-existing payee 
		if ( filterText.length() > 0 ) {
			String entered = filterText.getText().toString();
			enabled = true;
			for ( Payee payee : payeeArgument ) {
				if ( entered.compareToIgnoreCase(payee.getName()) == 0 ) {
					// already exists
					enabled = false;
					break;
				}
			}
		}

		buttonAddNew.setEnabled(enabled);
	}
	
	
	private TextWatcher filterWatacher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			enableOptionalItems();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// no op
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			filter.filter(s);	// filter list of payees
		}
	};
	
	protected void onDestroy() {
	    filterText.removeTextChangedListener(filterWatacher);
	}
	
}
