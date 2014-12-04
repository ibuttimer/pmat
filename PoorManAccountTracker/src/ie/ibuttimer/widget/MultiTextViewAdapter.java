/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.R;
import ie.ibuttimer.pmat.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Adapter class which extends ArrayAdapter for use with a layout with multiple TextView's 
 * @author Ian Buttimer
 * @param <T>	Class of objects the adapter is to be used with
 *
 */
public class MultiTextViewAdapter<T> extends ArrayAdapter<T> {

	private int layoutId;		// id of layout containing TextView's
	private int[] textViewIds;	// ids of the TextViews in layout
	private int filterIdx;		// index of TextView to filter on 

	private ArrayList<T> baseList;			// copy of list for filtering
	private MultiTextViewAdapterFilter filter;	// filter for this adapter
	
	private Context context;

	/**
	 * Constructor
	 * @param context		- The current context.
	 * @param layoutId		- The resource ID for a layout file containing a layout to use when instantiating views.
	 * @param textViewIds	- The id's of the TextViews within the layout resource to be populated
	 * @param objects		- The objects to represent in the ListView.
	 * @param filterIdx		- Index of <code>textViewIds</code> to filter on  
	 */
	public MultiTextViewAdapter(Context context, int layoutId, int[] textViewIds, List<T> objects, int filterIdx) {
		super(context, layoutId, objects);
		this.layoutId = layoutId;
		this.textViewIds = Arrays.copyOf(textViewIds, textViewIds.length);
		this.baseList = new ArrayList<T>(objects);
		this.filterIdx = filterIdx;
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	private View getItemView(int position, View convertView, ViewGroup parent) {

		RelativeLayout itemView = null;
		T item = getItem(position);

		if ( convertView != null ) {
			if ( convertView instanceof RelativeLayout ) {
				itemView = (RelativeLayout)convertView;
				
				for ( int i = textViewIds.length - 1; i >= 0; --i ) {
					TextView textView = (TextView)itemView.findViewById(textViewIds[i]);
					if ( textView != null ) {
						textView.setTextColor(context.getResources().getColor(R.color.sysBlack));
					}
					else {
						itemView = null;
						break;
					}
				}
			}
		}
		if ( itemView == null ) {
			Context context = getContext();
			itemView = new RelativeLayout(context);
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater)context.getSystemService(inflater);
			vi.inflate(layoutId, itemView, true);
		}
		
		for ( int i = textViewIds.length - 1; i >= 0; --i ) {
			TextView textView = (TextView)itemView.findViewById(textViewIds[i]);
			try {
	        	MultiTextViewAdapterInterface iface = (MultiTextViewAdapterInterface) item;
				textView.setText( iface.toDisplayString(i) );
				String colour = iface.getColour(i);
				if ( colour != null ) {
					textView.setTextColor( Color.parseColor(colour) );
				}
			}
			catch ( ClassCastException e ) {
				String msg = item.getClass().getName() + " supplied object does not implement MultiTextViewAdapterInterface";
				Logger.wtf(msg, e);
				throw ( e );
			}
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
			filter = new MultiTextViewAdapterFilter();
		return filter;
	}


	/**
	 * Class to filter objects implementing DoubleTextViewAdapterInterface 
	 * @author Ian Buttimer
	 *
	 */
	public class MultiTextViewAdapterFilter extends Filter {

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
            		String name = ((MultiTextViewAdapterInterface) item).toDisplayString(filterIdx).toLowerCase(Locale.US);
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
