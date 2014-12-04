/**
 * 
 */
package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.db.Category;
import ie.ibuttimer.pmat.util.Constants;
import ie.ibuttimer.widget.CheckBoxListAdapter;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * Class to provide common functionality for selecting categories.
 * @author Ian Buttimer
 *
 */
public class SelectCategory {

	/** Id of layout used by this dialog */
	public static final int LAYOUT_ID = R.layout.activity_select_category;
	/** Id of title string used by this dialog */
	public static final int TITLE_ID = R.string.title_activity_select_category;
	
	private Context context;

	private ListView listViewCategories;

	private CheckBoxListAdapter<Category> categoryAdapter;

	private ArrayList<Category> categories;				// list of categories
	protected ArrayList<Category> selectedCategories;

	boolean[] selected;		// selected categories

	int selectedTypes;		// flag representing category type

	protected Button buttonCancel;
	protected Button buttonClear;
	protected Button buttonSave;
	
	/**
	 * 
	 */
	public SelectCategory(Context context) {
		this.context = context;
	}

	
	/**
	 * Perform initialisation required for a Fragment. Call from Fragment.onCreateView().
	 * @param v	- Inflated layout
	 */
	public void onCreateFragment(View v) {
		
		// get references to the activity views
		listViewCategories = (ListView)v.findViewById(R.id.selectCategory_listViewCategories);
		buttonCancel = (Button)v.findViewById(R.id.selectCategory_buttonCancel);
		buttonClear = (Button)v.findViewById(R.id.selectCategory_buttonClear);
		buttonSave = (Button)v.findViewById(R.id.selectCategory_buttonSave);
	}

	
	/**
	 * Perform initialisation required for an Activity. Call from Activity.onCreate().
	 * @param a	- Inflated layout
	 */
	public void onCreateActivity(Activity a) {
		
		// get references to the activity views
		listViewCategories = (ListView)a.findViewById(R.id.selectCategory_listViewCategories);
		buttonCancel = (Button)a.findViewById(R.id.selectCategory_buttonCancel);
		buttonClear = (Button)a.findViewById(R.id.selectCategory_buttonClear);
		buttonSave = (Button)a.findViewById(R.id.selectCategory_buttonSave);
	}

	
	/**
	 * Get the arguments for the layout
	 * @param args					- arguments 
	 * @param savedInstanceState	- If non-null, this is being re-constructed from a previous saved state as given here.
	 */
	public void getArguments(Bundle args, Bundle savedInstanceState) {

		// get arguments
		Bundle b = (savedInstanceState == null ? args : savedInstanceState);

		selectedTypes = b.getInt(Constants.CATEGORY_TYPE_SELECTED, Category.EXPENSE_CATEGORY);

		// list of individual categories and already selected categories
		categories = b.getParcelableArrayList(Constants.CATEGORY_ARRAYLIST);
		selectedCategories = b.getParcelableArrayList(Constants.CATEGORY_SELECTION_RESULT);

		// sort the lists
		Collections.sort(categories, new Category.CompareLevelPathName());
		
		selected = new boolean[categories.size()];
		for ( Category category : selectedCategories ) {
			int index;
			if ( (index = categories.indexOf(category)) >= 0 ) {
				selected[index] = true;
			}
		}

		setupCategoryListView();
	}
	
    /**
	 * Called to save the current dynamic state, so it can later be reconstructed in a new instance of its process is restarted.
	 * @param outState	- Bundle in which to place your saved state. 
	 */
	public void onSaveInstanceState(Bundle outState) {

		outState.putInt(Constants.CATEGORY_TYPE_SELECTED, selectedTypes);

		outState.putParcelableArrayList(Constants.CATEGORY_ARRAYLIST, categories);

		outState.putParcelableArrayList(Constants.CATEGORY_SELECTION_RESULT, selectedCategories);
	}

	
	
	
	/**
	 * Setup the category list views in this activity
	 */
	private void setupCategoryListView() {
		
		categoryAdapter = new CheckBoxListAdapter<Category>(context, 
				R.layout.checked_list_view_item, R.id.checkedListView_checkBox, categories, selected);
		listViewCategories.setAdapter(categoryAdapter);
	}

	
	/**
	 * Create an intent representing a save operation
	 * @return
	 */
	public Intent getSaveIntent() {
		// return the selected categories
		Intent intent = new Intent();
		
		selectedCategories = new ArrayList<Category>();

		// get results from the adapter
		selected = categoryAdapter.getSelections();

		// create the results to return
		for ( int i = 0; i < selected.length; ++i ) {
			if ( selected[i] )
				selectedCategories.add(categories.get(i));
		}
		
		intent.putParcelableArrayListExtra(Constants.CATEGORY_SELECTION_RESULT, selectedCategories);

		return intent;
	}

	/**
	 * Setup the clear button in this activity
	 */
	protected void setupClearButton() {
		
    	// update save button state
    	buttonClear.setEnabled(true);

    	buttonClear.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {

				// clear selected items
				categoryAdapter.clearSelections();
			}
			
		});
	}
	
	/**
	 * Verify if all the required data has been entered
	 * @return	<code>true</code> is all the required data has been entered, <code>false</code> otherwise.
	 */
	private boolean allRequiredDataEntered() {

		boolean allOK = false;

		// get results from the adapter
		selected = categoryAdapter.getSelections();
		for ( int i = 0; i < selected.length; ++i ) {
			if ( selected[i] ) {
				allOK = true;
				break;
			}
		}
		return allOK;
	}
	


}
