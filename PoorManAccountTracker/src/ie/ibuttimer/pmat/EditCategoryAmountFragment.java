/**
 * 
 */
package ie.ibuttimer.pmat;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Fragment to edit category amount
 * @author Ian Buttimer
 *
 */
public class EditCategoryAmountFragment extends EditFragment {

	/** Id of layout used by this fragment */
	public static final int LAYOUT_ID = EditCategoryAmount.LAYOUT_ID;
	/** Id of title string used by this fragment */
	public static final int TITLE_ID = EditCategoryAmount.TITLE_ID;

	private EditCategoryAmount editor;

	
	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(LAYOUT_ID, container, false);
        
        getDialog().setTitle(TITLE_ID);
        
        editor = new EditCategoryAmount();
        
        editor.onCreateFragment(v);
        
		// get arguments
        editor.getArguments(getArguments(), savedInstanceState);

		// setup buttons
		setupCancelButton(editor.buttonCancel);
		setupDeleteButton(editor.buttonDelete);
		setupSaveButton(editor.buttonSave);

        return v;
    }
	
    /* (non-Javadoc)
	 * @see android.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {

		// TODO save EditFragment.EDITFRAGMENT_REQUESTCODE in bundle?
		
		editor.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	/**
	 * Setup the save button in this activity
	 */
	private void setupSaveButton(Button b) {
		
    	// update save button state
		b.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {

				// return the new amount
				onEditDone(Activity.RESULT_OK, editor.getSaveIntent());
				dismiss();
			}
		});
		
	}

	/**
	 * Setup the delete button in this activity
	 */
	private void setupDeleteButton(Button b) {
		
    	// update save button state
		b.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// return delete category
				onEditDone(Activity.RESULT_OK, editor.getDeleteIntent());
				dismiss();
			}
			
		});
		
	}

	/**
	 * Setup the cancel button in this activity
	 */
	private void setupCancelButton(Button b) {
		
		b.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				onEditDone(Activity.RESULT_CANCELED);
				dismiss();
			}
		});
	}


}
