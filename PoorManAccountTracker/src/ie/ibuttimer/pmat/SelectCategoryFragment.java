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
 * @author Ian Buttimer
 *
 */
public class SelectCategoryFragment extends EditFragment {

	/** Id of layout used by this fragment */
	public static final int LAYOUT_ID = SelectCategory.LAYOUT_ID;
	/** Id of title string used by this fragment */
	public static final int TITLE_ID = SelectCategory.TITLE_ID;

	private SelectCategory editor;

	/**
	 * 
	 */
	public SelectCategoryFragment() {
		// nop
	}

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
        
        editor = new SelectCategory(getDialog().getContext());
        
        editor.onCreateFragment(v);
        
		// get arguments
        editor.getArguments(getArguments(), savedInstanceState);

		// setup buttons
		setupSaveButton(editor.buttonSave);
		editor.setupClearButton();
		setupCancelButton(editor.buttonCancel);

        return v;
    }
    
    
    /* (non-Javadoc)
	 * @see android.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {

		editor.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	
	/**
	 * Setup the save button in this activity
	 */
	private void setupSaveButton(Button b) {
		
    	// update save button state
    	b.setEnabled( true /*allRequiredDataEntered()*/ );

		b.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// return the selected categories
				onEditDone(Activity.RESULT_OK, editor.getSaveIntent());
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
