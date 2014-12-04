package ie.ibuttimer.pmat;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Activity to select categories for a transaction.
 * @author Ian Buttimer
 */
public class SelectCategoryActivity extends BaseActivity {

	private SelectCategory editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(SelectCategory.LAYOUT_ID);

        editor = new SelectCategory(this);

        editor.onCreateActivity(this);

		// get arguments
        editor.getArguments(getIntent().getExtras(), savedInstanceState);

		setupSaveButton(editor.buttonSave);
		editor.setupClearButton();
		setupCancelButton(editor.buttonCancel);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_category, menu);
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
	 * Setup the save button in this activity
	 */
	private void setupSaveButton(Button b) {
		
    	// update save button state
    	b.setEnabled( true );

		b.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// return the selected categories
				setResult(Activity.RESULT_OK, editor.getSaveIntent());
				finish();
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
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
		
	}

}
