package ie.ibuttimer.pmat;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Activity to edit a category amount
 * @author Ian Buttimer
 */
public class EditCategoryAmountActivity extends BaseActivity {

	private EditCategoryAmount editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_category_amount);
		
        editor = new EditCategoryAmount();
        
        editor.onCreateActivity(this);
        
		// get arguments
        editor.getArguments(getIntent().getExtras(), savedInstanceState);

		// setup buttons
		setupCancelButton(editor.buttonCancel);
		setupDeleteButton(editor.buttonDelete);
		setupSaveButton(editor.buttonSave);
	}

	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {

		editor.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_category_amount, menu);
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
		b.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {

				// return the new amount
				setResult(Activity.RESULT_OK, editor.getSaveIntent());
				finish();
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
				setResult(Activity.RESULT_OK, editor.getDeleteIntent());
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
