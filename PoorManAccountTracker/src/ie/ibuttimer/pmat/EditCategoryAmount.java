/**
 * 
 */
package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.util.Constants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Class to provide common functionality for editing category amount
 * @author Ian Buttimer
 *
 */
public class EditCategoryAmount {

	/** Id of layout used by this dialog */
	public static final int LAYOUT_ID = R.layout.activity_edit_category_amount;
	/** Id of title string used by this dialog */
	public static final int TITLE_ID = R.string.title_activity_edit_category_amount;

	protected TextView textViewName;
	protected EditText editTextAmount;
	protected Button buttonCancel;
	protected Button buttonDelete;
	protected Button buttonSave;
	protected Button buttonAmt;

	protected int id;			// category id
	protected String presetAmt;	// preset amount
	
	/**
	 * 
	 */
	public EditCategoryAmount() {
		// nop
	}

	
	/**
	 * Perform initialisation required for a Fragment. Call from Fragment.onCreateView().
	 * @param v	- Inflated layout
	 */
	public void onCreateFragment(View v) {
		
		// get references to the activity views
        textViewName = (TextView)v.findViewById(R.id.editCategoryAmount_textViewName);
		editTextAmount = (EditText)v.findViewById(R.id.editCategoryAmount_editTextAmount);
		buttonCancel = (Button)v.findViewById(R.id.editCategoryAmount_buttonCancel);
		buttonDelete = (Button)v.findViewById(R.id.editCategoryAmount_buttonDelete);
		buttonSave = (Button)v.findViewById(R.id.editCategoryAmount_buttonSave);
		buttonAmt = (Button)v.findViewById(R.id.editCategoryAmount_buttonAmt);
	}

	
	/**
	 * Perform initialisation required for an Activity. Call from Activity.onCreate().
	 * @param a	- Inflated layout
	 */
	public void onCreateActivity(Activity a) {
		
		// get references to the activity views
        textViewName = (TextView)a.findViewById(R.id.editCategoryAmount_textViewName);
		editTextAmount = (EditText)a.findViewById(R.id.editCategoryAmount_editTextAmount);
		buttonCancel = (Button)a.findViewById(R.id.editCategoryAmount_buttonCancel);
		buttonDelete = (Button)a.findViewById(R.id.editCategoryAmount_buttonDelete);
		buttonSave = (Button)a.findViewById(R.id.editCategoryAmount_buttonSave);
		buttonAmt = (Button)a.findViewById(R.id.editCategoryAmount_buttonAmt);
	}
	
	
	/**
	 * Get the arguments for the layout
	 * @param args					- arguments 
	 * @param savedInstanceState	- If non-null, this is being re-constructed from a previous saved state as given here.
	 */
	public void getArguments(Bundle args, Bundle savedInstanceState) {
	
		// get arguments
		String name;
		double amount;
		Bundle b = (savedInstanceState == null ? args : savedInstanceState);
		
		int visibility;
		if ( b.containsKey(Constants.CATEGORY_AMOUNT_ALLOC) ) {
			amount = b.getDouble(Constants.CATEGORY_AMOUNT_ALLOC);
			presetAmt = Double.toString(amount);
			buttonAmt.setText(presetAmt);
			visibility = View.VISIBLE;
			
			buttonAmt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					editTextAmount.setText(presetAmt);
				}
				
			});
		}
		else {
			presetAmt = null;
			visibility = View.GONE;
		}
		buttonAmt.setVisibility(visibility);
		buttonAmt.setEnabled((visibility == View.VISIBLE));

		name = b.getString(Constants.CATEGORY_AMOUNT_EDIT_NAME);

		amount = b.getDouble(Constants.CATEGORY_AMOUNT_TO_EDIT);
		
		id = b.getInt(Constants.CATEGORY_AMOUNT_EDIT_ID);

		textViewName.setText(name);
		
		if ( amount != 0 )
			editTextAmount.setText(Double.toString(amount));
	}
	
	
    /**
	 * Called to save the current dynamic state, so it can later be reconstructed in a new instance of its process is restarted.
	 * @param outState	- Bundle in which to place your saved state. 
	 */
	public void onSaveInstanceState(Bundle outState) {

		outState.putString(Constants.CATEGORY_AMOUNT_EDIT_NAME, (String) textViewName.getText());

		outState.putDouble(Constants.CATEGORY_AMOUNT_TO_EDIT, getEnteredAmount());
		
		outState.putInt(Constants.CATEGORY_AMOUNT_EDIT_ID, id);
	}

	
	/**
	 * Get the amount entered
	 * @return
	 */
	private double getEnteredAmount() {
		String amountStr = editTextAmount.getText().toString();
		double amount = 0;
		
		if ( !TextUtils.isEmpty(amountStr) ) {
			try {
				amount = Double.parseDouble(amountStr);
			}
			catch ( NumberFormatException e ) {
				amount = 0;
			}
		}
		return amount;
	}

	
	/**
	 * Create an intent representing a save operation
	 * @return
	 */
	public Intent getSaveIntent() {
		// return the new amount
		Intent intent = new Intent();
		intent.putExtra(Constants.CATEGORY_AMOUNT_RESULT_AMOUNT, getEnteredAmount());
		intent.putExtra(Constants.CATEGORY_AMOUNT_RESULT, id);
		
		return intent;
	}

	
	/**
	 * Create an intent representing a delete operation
	 * @return
	 */
	public Intent getDeleteIntent() {
		// return ok result but no amount indicates delete category
		Intent intent = new Intent();
		intent.putExtra(Constants.CATEGORY_AMOUNT_RESULT, id);
		
		return intent;
	}

	

}
