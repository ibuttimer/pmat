/**
 * 
 */
package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.db.Transfer;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.pmat.util.DateTimeFormat;
import ie.ibuttimer.widget.TextViewAdapter;
import ie.ibuttimer.widget.TextViewAdapterBase;
import ie.ibuttimer.widget.TextViewAdapterInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Ian Buttimer
 *
 */
public class SelectTransfer {

	/** Id of layout used by this dialog */
	public static final int LAYOUT_ID = R.layout.activity_select_transfer;
	/** Id of title string used by this dialog */
	public static final int TITLE_ID = R.string.title_activity_select_transfer;

	private Context context;
	
	private TextView textViewSendDate;
	private ListView listViewTransfers;

	private ArrayList<TransferInfo> transferInfos;
	private TextViewAdapter<TransferInfo> transferAdapter;

	private DismissDialogInterface finishCallback;	// selection finished callback
	
	
	
	/**
	 * 
	 */
	public SelectTransfer(Context context) {
		this.context = context;
	}

	/**
	 * Perform initialisation required for a Fragment. Call from Fragment.onCreateView().
	 * @param v	- Inflated layout
	 */
	public void onCreateFragment(View v, DismissDialogInterface i) {
		
		// get references to the activity views
		textViewSendDate = (TextView)v.findViewById(R.id.selectTransfer_textViewSendDate);
		listViewTransfers = (ListView)v.findViewById(R.id.selectTransfer_listViewTransfers);
		
		finishCallback = i;
	}

	
	/**
	 * Perform initialisation required for an Activity. Call from Activity.onCreate().
	 * @param a	- Inflated layout
	 */
	public void onCreateActivity(Activity a) {
		
		// get references to the activity views
		textViewSendDate = (TextView)a.findViewById(R.id.selectTransfer_textViewSendDate);
		listViewTransfers = (ListView)a.findViewById(R.id.selectTransfer_listViewTransfers);
		
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

		ArrayList<Transfer> transfers = b.getParcelableArrayList(Constants.TRANSFER_ARRAYLIST);
		DateTimeFormat df = new DateTimeFormat(context, DateTimeFormat.MEDIUM, DateTimeFormat.FORMAT_DATE_TIME);
		
		// get start date
		GregorianCalendar calendar = (GregorianCalendar) b.getSerializable(Constants.TRANSFER_START_DATE);
		if ( calendar == null )
			calendar = (GregorianCalendar) GregorianCalendar.getInstance();
		Date date = calendar.getTime();

		// setup send date
		textViewSendDate.setText(df.format(date));

		
		// get transfers
		transferInfos = new ArrayList<TransferInfo>();
		if ( transfers != null ) {
			for ( Transfer transfer : transfers ) {
				
				String completeDate = df.format(transfer.calcTransferCompleteDate(calendar).getTime());
				
				transferInfos.add(new TransferInfo(transfer, completeDate));
			}
		}
		
		// setup transfer list
		transferAdapter = new TextViewAdapter<TransferInfo>(context, 
				R.layout.text_view_adapter_item, R.id.text_view_adapter_item_textViewItem, transferInfos);
		listViewTransfers.setAdapter(transferAdapter);

		listViewTransfers.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// return the selected transfer
				Intent intent = new Intent();
				TransferInfo transferInfo = (TransferInfo) transferAdapter.getItem(position);
				
				intent.putExtra(Constants.TRANSFER_SELECTION_RESULT, transferInfo.getTransfer());

				finishCallback.onDoDismiss(Activity.RESULT_OK, intent);
			}
			
		});

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
	 * Create an intent representing a save operation
	 * @return
	 */
	public Intent getSaveIntent(int position) {
		// return the selected transfer
		Intent intent = new Intent();
		TransferInfo transferInfo = (TransferInfo) transferAdapter.getItem(position);
		
		intent.putExtra(Constants.TRANSFER_SELECTION_RESULT, transferInfo.getTransfer());

		return intent;
	}


	/**
	 * Class with all the info required to select a transfer 
	 * @author Ian Buttimer
	 *
	 */
	private class TransferInfo extends TextViewAdapterBase implements TextViewAdapterInterface {
		
		private Transfer transfer;		// id
		private String date;			// date
		
		/**
		 * @param id
		 * @param name
		 */
		public TransferInfo(Transfer transfer, String date) {
			super(transfer.getId(), transfer.getName());
			this.transfer = transfer;
			this.date = date;
		}

		/**
		 * @return the transfer
		 */
		public Transfer getTransfer() {
			return transfer;
		}
		
		
		/* TextViewAdapterInterface interface related functions */
		
		@Override
		public String toDisplayString() {
			return super.toDisplayString() + " (" + date + ")";
		};
	}

}
