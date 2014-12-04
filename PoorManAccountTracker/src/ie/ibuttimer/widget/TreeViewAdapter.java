/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.util.Logger;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;

/**
 * @author Ian Buttimer
 *
 */
public class TreeViewAdapter<T> extends BaseExpandableListAdapter {

	public ArrayList<T> groupItem;
	public ArrayList<ArrayList<T>> childItem;
	public LayoutInflater inflater;
	public int groupLayout;
	public int groupResource;
	public int groupResource2;
	public int childLayout;
	public int childResource;

	public TreeViewAdapter(ArrayList<T> groupItem, ArrayList<ArrayList<T>> childItem, LayoutInflater inflater, 
			int groupLayout, int groupResource, int groupResource2, int childLayout, int childResource) {
		this.groupItem = groupItem;
		this.childItem = childItem;
		this.inflater = inflater;
		this.groupLayout = groupLayout;
		this.groupResource = groupResource;
		this.groupResource2 = groupResource2;
		this.childLayout = childLayout;
		this.childResource = childResource;
	}


	
	

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChild(int, int)
	 */
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childItem.get(groupPosition).get(childPosition);
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildId(int, int)
	 */
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return ((TextViewAdapterInterface) getChild(groupPosition, childPosition)).getId();
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		ArrayList<T> tempChild = childItem.get(groupPosition);
		Object item = tempChild.get(childPosition);
		
		if (convertView == null) {
		   convertView = inflater.inflate(childLayout, null);
		}
		
		
		TextView text = (TextView) convertView.findViewById(childResource);
		try {
			text.setText( ((TextViewAdapterInterface) item).toDisplayString() );
		}
		catch ( ClassCastException e ) {
			String msg = item.getClass().getName() + " supplied object does not implement TextViewAdapterInterface";
			Logger.wtf(msg, e);
			throw ( e );
		}

		return convertView;
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
	 */
	@Override
	public int getChildrenCount(int groupPosition) {
		return (childItem.get(groupPosition)).size();
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroup(int)
	 */
	@Override
	public Object getGroup(int groupPosition) {
		return groupItem.get(groupPosition);
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupCount()
	 */
	@Override
	public int getGroupCount() {
		return groupItem.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupId(int)
	 */
	@Override
	public long getGroupId(int groupPosition) {
		return ((TextViewAdapterInterface) groupItem.get(groupPosition)).getId();
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
		   convertView = inflater.inflate(groupLayout, null);
		}
		
		CheckedTextView text = (CheckedTextView) convertView.findViewById(groupResource);
		CheckBox box = (CheckBox) convertView.findViewById(groupResource2);

		Object item = groupItem.get(groupPosition);
		try {
			String dstr = ((TextViewAdapterInterface) item).toDisplayString();
			box.setText( dstr );
			text.setChecked(isExpanded);
		}
		catch ( ClassCastException e ) {
			String msg = item.getClass().getName() + " supplied object does not implement TextViewAdapterInterface";
			Logger.wtf(msg, e);
			throw ( e );
		}

		return convertView;

	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#hasStableIds()
	 */
	@Override
	public boolean hasStableIds() {
		return false;
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
	 */
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

}
