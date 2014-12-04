/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Adapter class which extends ArrayAdapter for use with a simple TextView layout 
 * @author Ian Buttimer
 * @param <T>	Class of objects the adapter is to be used with
 *
 */
public class TextViewAdapter<T> extends ArrayAdapter<T> {

	private int layoutId;			// id of layout containing TextView
	private int textViewResourceId;	// id of TextView in layout
	
	private ArrayList<T> baseList;			// copy of list for filtering
	private TextViewAdapterFilter filter;	// filter for this adapter

	/**
	 * Constructor
	 * @param context				The current context.
	 * @param resource				The resource ID for a layout file containing a layout to use when instantiating views.
	 * @param textViewResourceId	The id of the TextView within the layout resource to be populated
	 * @param objects				The objects to represent in the ListView.
	 */
	public TextViewAdapter(Context context, int layoutId, int textViewResourceId, List<T> objects) {
		super(context, layoutId, textViewResourceId, objects);
		this.layoutId = layoutId;
		this.textViewResourceId = textViewResourceId;
		this.baseList = new ArrayList<T>(objects);
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	private View getItemView(int position, View convertView, ViewGroup parent) {

		RelativeLayout itemView;
		T item = getItem(position);

		if ( convertView == null ) {
			itemView = new RelativeLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
			vi.inflate(layoutId, itemView, true);
		}
		else {
			itemView = (RelativeLayout)convertView;
		}
		
		TextView textView = (TextView)itemView.findViewById(textViewResourceId);
		try {
			textView.setText( ((TextViewAdapterInterface) item).toDisplayString() );
		}
		catch ( ClassCastException e ) {
			String msg = item.getClass().getName() + " supplied object does not implement TextViewAdapterInterface";
			Logger.wtf(msg, e);
			throw ( e );
		}

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

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public Filter getFilter() {
		if ( filter == null )
			filter = new TextViewAdapterFilter();
		return filter;
	}


	/**
	 * Class to filter objects implementing TextViewAdapterInterface 
	 * @author Ian Buttimer
	 *
	 */
	public class TextViewAdapterFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
            String prefix = constraint.toString().toLowerCase(Locale.US);
        	ArrayList<T> list = new ArrayList<T>();
            
            if ( TextUtils.isEmpty(prefix) ) {
            	// no prefix, return all
            	list.addAll((Collection<? extends T>) baseList);
            }
            else {
            	// filter on prefix
            	final int N = baseList.size();
            	
            	for ( int i = 0; i < N; ++i ) {
            		T item = (T) baseList.get(i);
            		String name = ((TextViewAdapterInterface) item).toDisplayString().toLowerCase(Locale.US);
            		
            		if ( name.startsWith(prefix) )
            			list.add(item);
            	}
            }
        	results.count = list.size();
        	results.values = list;
            
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {

			@SuppressWarnings("unchecked")
			ArrayList<T> filteredList = (ArrayList<T>) results.values;

			clear();
			final int N = filteredList.size();
			for ( int i = 0; i < N; ++i )
				add(filteredList.get(i));
		}
		
	}
	

}
