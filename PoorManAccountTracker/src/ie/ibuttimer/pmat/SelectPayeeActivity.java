/**
 * 
 */
package ie.ibuttimer.pmat;

import android.content.Intent;
import android.os.Bundle;

/**
 * @author Ian Buttimer
 */
public class SelectPayeeActivity extends BaseActivity implements DismissDialogInterface {

	private SelectPayee editor;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(SelectPayee.LAYOUT_ID);

        editor = new SelectPayee(this);

        editor.onCreateActivity(this);

		// get arguments
        editor.getArguments(getIntent().getExtras(), savedInstanceState);
	}
	
	
	@Override
	protected void onDestroy() {
	    editor.onDestroy();
	    super.onDestroy();
	}



	/* Implement the DismissDialogInterface to provide a mechanism for the editor (SelectPayee) to
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
