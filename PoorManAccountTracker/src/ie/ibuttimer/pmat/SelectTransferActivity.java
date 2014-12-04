package ie.ibuttimer.pmat;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Activity to select transfer type for a transaction.
 * @author Ian Buttimer
 *
 */
public class SelectTransferActivity extends BaseActivity implements DismissDialogInterface {

	private SelectTransfer editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(SelectTransfer.LAYOUT_ID);
		
        editor = new SelectTransfer(this);

        editor.onCreateActivity(this);

		// get arguments
        editor.getArguments(getIntent().getExtras(), savedInstanceState);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_transfer, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see ie.ibuttimer.pmat.BaseActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}


	/* Implement the DismissDialogInterface to provide a mechanism for the editor (SelectTransfer) to
	 * set the activity result and finish.
	 */

	@Override
	public void onDoDismiss(int result, Intent data) {
		setResult(result, data);
		finish();
	}


	@Override
	public void onDoDismiss(int result) {
		setResult(result);
		finish();
	}

}
