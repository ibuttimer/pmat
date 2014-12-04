package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class SelectContactActivity extends BaseActivity {

	
	// widget variables and other related variables 
	private ExpandableListView listViewContacts;
	private TextView textViewNoContacts;
	private Button buttonCancel;
	private Button buttonAddNew;

    private ExpandableListAdapter mAdapter; 


    public static Uri SELECT_CONTACT_URI = Uri.parse("content://" + Constants.APP_BASE + "/contacts");
    public static Uri SELECT_CONTACT_PHONE_URI = Uri.parse(SELECT_CONTACT_URI.toString() + "/phone");

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_contact);
		
		// get references to the activity views
		listViewContacts = (ExpandableListView)this.findViewById(R.id.selectContact_expandableListViewContacts);
		buttonCancel = (Button)this.findViewById(R.id.selectContact_buttonCancel);
		buttonAddNew = (Button)this.findViewById(R.id.selectContact_buttonAddNew);
		textViewNoContacts = (TextView)this.findViewById(R.id.selectContact_textViewNoContacts);

		// get contact list
		populateContactList();

		// setup buttons
		setupCancelButton();
		setupAddNewButton();

		// setup list view 
		listViewContacts.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				HashMap<String, String> contact = (HashMap<String, String>) mAdapter.getGroup(groupPosition); 
				HashMap<String, String> number = (HashMap<String, String>) mAdapter.getChild(groupPosition, childPosition);
				
				
				
//            	curGroupMap.put(ContactsContract.Contacts.DISPLAY_NAME, name);

				// construct the result uri
				Uri outUri = Uri.parse(SELECT_CONTACT_PHONE_URI.toString() + "/" + number.get(Phone.NUMBER));
				Intent outData = new Intent();
				outData.setData(outUri);
				
				String name = contact.get(ContactsContract.Contacts.DISPLAY_NAME);
				if ( !TextUtils.isEmpty(name) )
					outData.putExtra(ContactsContract.Contacts.DISPLAY_NAME, name);
				
				setResult(Activity.RESULT_OK, outData);
				finish();
				
				return true;
			}
		});
	}
	
	
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		populateContactList();
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_contact, menu);
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
     * Populate the contact list.
     */
    private void populateContactList() {
        // Build adapter with contact entries
    	Cursor cursor = getContacts();
    	int noContacts;

        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
        if ( cursor.moveToFirst() ) {

        	noContacts = View.GONE;
        	
        	int idxId = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY);
        	int idxName = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
        	do { 
        		String lookupKey = cursor.getString(idxId);
        		String name = cursor.getString(idxName);

        		// add new group i.e. contact name
            	Map<String, String> curGroupMap = new HashMap<String, String>();
            	groupData.add(curGroupMap);
            	curGroupMap.put(ContactsContract.Contacts.DISPLAY_NAME, name);
            	curGroupMap.put(ContactsContract.Contacts.LOOKUP_KEY, lookupKey);

            	ArrayList<PhoneNumberDetails> numbers = lookupPhoneNumbers(lookupKey);
            	
        		// add children for the group i.e. contact phone numbers
        		List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            	if ( !numbers.isEmpty() ) {
            		final int N = numbers.size();
            		for ( int i = 0; i < N; ++ i ) {
	            		Map<String, String> curChildMap = new HashMap<String, String>();
	            		children.add(curChildMap);
	            		
	            		PhoneNumberDetails number = numbers.get(i);
	            		
	            		curChildMap.put(Phone.NUMBER, number.number);
	            		curChildMap.put(Phone.TYPE, number.type);
            		}
                	childData.add(children);
            	}
        	}
        	while (cursor.moveToNext());
        }
        else {
        	// no contacts
        	noContacts = View.VISIBLE;
        }

        textViewNoContacts.setVisibility(noContacts);

    	// Set up the adapter
    	mAdapter = new SimpleExpandableListAdapter(this,
    												groupData,
    												android.R.layout.simple_expandable_list_item_1,
    												new String[] { ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.LOOKUP_KEY },
    												new int[] { android.R.id.text1, android.R.id.text2 },
    												childData,
    												android.R.layout.simple_expandable_list_item_2,
    												new String[] { Phone.NUMBER, Phone.TYPE },
    												new int[] { android.R.id.text1, android.R.id.text2 }
    	);
    	listViewContacts.setAdapter(mAdapter);   
    }

	/**
	 * Setup the add new button in this activity
	 */
	private void setupAddNewButton() {
		
		buttonAddNew.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION,ContactsContract.Contacts.CONTENT_URI);
				startActivity(intent);
			}
		});
	}

	/**
	 * Setup the cancel button in this activity
	 */
	private void setupCancelButton() {
		
		buttonCancel.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
	}

	
	
    /**
     * Obtains the contact list.
     * @return A cursor for accessing the contact list.
     */
    private Cursor getContacts()
    {
        // Run query
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1'";
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return managedQuery(uri, projection, selection, null, sortOrder);
    }

    
    /**
     * Return all the phone numbers for the contact specified by <code>lookupKey</code>
     * @param lookupKey		Lookup key for the contact
     * @return				List of numbers
     */
    private ArrayList<PhoneNumberDetails> lookupPhoneNumbers(String lookupKey)
    {
    	ArrayList<PhoneNumberDetails> numbers = new ArrayList<PhoneNumberDetails>();

        String[] projection = new String[] {
        		Phone.NUMBER,
        		Phone.TYPE
        };

        Cursor phoneCursor = getBaseContext().getContentResolver().query(Phone.CONTENT_URI, 
        		projection, ContactsContract.Contacts.LOOKUP_KEY + " = ?", new String[] { lookupKey }, null);
        
        try
        {
            int numIdx = phoneCursor.getColumnIndex(Phone.NUMBER);
            int typeIdx = phoneCursor.getColumnIndex(Phone.TYPE);
            Resources res = getResources();
            String undefined = res.getString(R.string.add_bank_undefined); 
            while (phoneCursor.moveToNext())
            {
                String phoneNumber = phoneCursor.getString(numIdx);
                int type = phoneCursor.getInt(typeIdx);
                
                CharSequence phoneLabel = Phone.getTypeLabel(res, type, undefined);

                numbers.add( new PhoneNumberDetails(phoneNumber, phoneLabel.toString()) );
            }
        } 
        finally {
            phoneCursor.close();
        }

        return numbers;
    }

    /**
     * Utility class to store a phone number & number type
     * @author Ian Buttimer
     *
     */
    private class PhoneNumberDetails {
    	
    	String number;
    	String type;
    	
		/**
		 * @param number
		 * @param type
		 */
		public PhoneNumberDetails(String number, String type) {
			super();
			this.number = number;
			this.type = type;
		}

    }
    
}
