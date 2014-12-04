/**
 * 
 */
package ie.ibuttimer.pmat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Ian Buttimer
 *
 */
public class SelectTransferFragment extends EditFragment {

	/** Id of layout used by this fragment */
	public static final int LAYOUT_ID = SelectTransfer.LAYOUT_ID;
	/** Id of title string used by this fragment */
	public static final int TITLE_ID = SelectTransfer.TITLE_ID;

	private SelectTransfer editor;
	
	/**
	 * 
	 */
	public SelectTransferFragment() {
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
        
        editor = new SelectTransfer(getDialog().getContext());
        
        editor.onCreateFragment(v, new DismissFragment());

		// get arguments
        editor.getArguments(getArguments(), savedInstanceState);

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
	
	/* Implement the DismissDialogInterface interface to allow SelectTransfer to dismiss the fragment */
    
    private class DismissFragment implements DismissDialogInterface {

		@Override
		public void onDoDismiss(int result, Intent data) {
			onEditDone(result, data);
			dismiss();
		}

		@Override
		public void onDoDismiss(int result) {
			onEditDone(result);
			dismiss();
		}
    	
    }
}
