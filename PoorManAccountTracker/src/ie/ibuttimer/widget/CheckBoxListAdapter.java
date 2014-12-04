/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.util.Logger;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;

/**
 * @author Ian Buttimer
 *
 */
public class CheckBoxListAdapter<T> extends ArrayAdapter<T> {

	private int layoutId;				// id of layout containing CheckBox
	private int checkBoxViewResourceId;	// id of CheckBox in layout
	private boolean[] state;			// selected state of items in list 
	
	
	/**
	 * Constructor
	 * @param layoutResourceId		The resource ID for a layout file containing a layout to use when instantiating views 
	 * @param viewResourceId		The id of the CheckBox within the layout resource to be populated
	 * @param objects				The objects to represent in the ListView
	 * @param state					Initial state of the CheckBox for the objects
	 */
	public CheckBoxListAdapter(Context context, int layoutId, int checkBoxViewResourceId, List<T> objects, boolean[] state) {
		super(context, checkBoxViewResourceId, objects);
		this.layoutId = layoutId;
		this.checkBoxViewResourceId = checkBoxViewResourceId;
		
		this.state = new boolean[state.length];
		System.arraycopy(state, 0, this.state, 0, state.length);
	}

	
	/**
	 * Return the selected state of the items in the list 
	 * @return
	 */
	public boolean[] getSelections() {
		return state;
	}
	
	
	/**
	 * Clear all the selected check boxes
	 */
	public void clearSelections() {
		final int N = state.length;
		for ( int i = 0; i < N; ++i ) {
			state[i] = false;
		}

		notifyDataSetChanged();
	}
	
	
	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	private View getItemView(int position, View convertView, ViewGroup parent) {

		LinearLayout itemView;
		T item = getItem(position);

		if ( convertView == null ) {
			itemView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
			vi.inflate(layoutId, itemView, true);
		}
		else {
			itemView = (LinearLayout)convertView;
		}
	
		// set the text
		CheckBox checkBox = (CheckBox)itemView.findViewById(checkBoxViewResourceId);
		try {
			checkBox.setText( ((TextViewAdapterInterface) item).toDisplayString() );
		}
		catch ( ClassCastException e ) {
			String msg = item.getClass().getName() + " supplied object does not implement TextViewAdapterInterface";
			Logger.wtf(msg, e);
			throw ( e );
		}

		// set the state
		checkBox.setChecked(state[position]);
		
		// add listener to remember the state of the check box
		checkBox.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				
				CheckBox checkBox = (CheckBox) v;
				int position = (Integer) v.getTag();
				if(checkBox.isChecked())
					state[position] = true;
				else
					state[position] = false;
			}
		});   

		// store the position, so the listener can retrieve it and update the correct position in the state array 
		checkBox.setTag(position);
		
		return itemView;
	}

	
	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		return getItemView(position, convertView, parent);
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {

		return getItemView(position, convertView, parent);
	}

}
