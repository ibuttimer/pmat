/**
 * 
 */
package ie.ibuttimer.widget;

import java.util.HashMap;

import ie.ibuttimer.pmat.R;
import ie.ibuttimer.pmat.sms.SmsMessageFactory;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

/**
 * @author Ian Buttimer
 *
 */
public class SmsTemplateField extends RelativeLayout {
	
	private Spinner spinnerType;
	private ImageButton buttonRemove;
	private ImageButton buttonUp;
	private ImageButton buttonDown;
	private EditText editTextText;
	
	private int index;
	
	private int[] typeValues;
	private int selectedType = SmsMessageFactory.SMS_TEMPLATE_FIELD_INVALID;
	
	public static final String FIELD_TYPE_KEY = "FIELD_TYPE";
	public static final String FIELD_VALUE_KEY = "FIELD_VALUE";
	
	public static final int INDEX_FIRST = 0; 
	public static final int INDEX_MIDDLE = 1; 
	public static final int INDEX_LAST = 2; 
	public static final int INDEX_ONLY = 3;
	
	public static final int INDEX_MOVE_UP = 0; 
	public static final int INDEX_MOVE_DOWN = 1; 

	/**
	 * @param context
	 */
	public SmsTemplateField(Context context) {
		this(context,null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public SmsTemplateField(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.create_sms_template_field_view, this, true);

		index = 0;
		
		
		RelativeLayout layoutRelative = (RelativeLayout)getChildAt(0);

		spinnerType = (Spinner) layoutRelative.getChildAt(0);
		LinearLayout layoutLinear = (LinearLayout)layoutRelative.getChildAt(1);
		editTextText = (EditText) layoutRelative.getChildAt(2);
		
		buttonRemove = (ImageButton) layoutLinear.getChildAt(0);
		buttonUp = (ImageButton) layoutLinear.getChildAt(1);
		buttonDown = (ImageButton) layoutLinear.getChildAt(2);

		
		setupTypeSpinner(context);

		// setup remove button
		buttonRemove.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		    	onRemoveSmsFieldListener.onRemoveSmsField(v);
			}
		});
		
		// setup up button
		buttonUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onSmsFieldMoveSelectedListener.onFieldMoveSelected(v, INDEX_MOVE_UP);
			}
		});
		
		// setup down button
		buttonDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onSmsFieldMoveSelectedListener.onFieldMoveSelected(v, INDEX_MOVE_DOWN);
			}
		});

	}


	
	
	public void setIndex(int index) {

		switch ( index ) {
			case INDEX_FIRST: 
			case INDEX_MIDDLE: 
			case INDEX_LAST: 
				this.index = index;
				buttonUp.setEnabled(index != INDEX_FIRST);
				buttonDown.setEnabled(index != INDEX_LAST);
				break;
			case INDEX_ONLY:
				this.index = index;
				buttonUp.setEnabled(false);
				buttonDown.setEnabled(false);
				break;
		}
	}
	
	
	
	/**
	 * Return the field information
	 * @return	HashMap with type & value keys
	 */
	public HashMap<String,String> getFieldInfo() {
		
		HashMap<String,String> values = new HashMap<String,String>();
		
		values.put(FIELD_TYPE_KEY, String.valueOf(selectedType));
		if ( selectedType == SmsMessageFactory.SMS_TEMPLATE_FIELD_TEXT )
			values.put(FIELD_VALUE_KEY, editTextText.getText().toString());
		
		return values;
	}
	

	/**
	 * Set the field information
	 * @param info	HashMap with type & value keys
	 */
	public void setFieldInfo(HashMap<String,String> info) {

		if ( info.containsKey(FIELD_TYPE_KEY) ) {
			int index = Integer.parseInt( info.get(FIELD_TYPE_KEY) );
			int i;
			for ( i = typeValues.length - 1; i >= 0; --i ) {
				if ( index == typeValues[i] )
					break;
			}
			if ( i >= 0 ) {
				spinnerType.setSelection(i);
				
				if ( info.containsKey(FIELD_VALUE_KEY) )
					editTextText.setText( info.get(FIELD_VALUE_KEY) );
			}
			
		}
	}

	
	/**
	 * Sets the selected type
	 * @param index		Index of the type in the value array
	 */
	private void setSelectedType(int index) {
		selectedType = typeValues[index];
	}
	
	
	/**
	 * Setup the field type spinner
	 * @param context
	 */
	private void setupTypeSpinner(Context context) {
		
		// Populate the type spinner 
		ArrayAdapter<CharSequence> typeAdapter;
		typeAdapter = ArrayAdapter.createFromResource(context, R.array.smstemplate_field_types,
									android.R.layout.simple_spinner_item);
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerType.setAdapter(typeAdapter);

		// Get the option values from the arrays.
		Resources r = context.getResources();
		typeValues = r.getIntArray(R.array.smstemplate_field_type_values);

		spinnerType.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// An item was selected, retrieve the selected item
				setSelectedType(position);

				// enable/disable optional fields 
				enableOptionalFields();
				
				onSmsFieldTypeSelectedListener.onFieldTypeSelected(view);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	/**
	 * Enable/disable optional fields based on selections made
	 */
	private void enableOptionalFields() {

		int textVisibility;
		if ( selectedType == SmsMessageFactory.SMS_TEMPLATE_FIELD_TEXT )
			textVisibility = View.VISIBLE;
		else
			textVisibility = View.GONE;
		editTextText.setVisibility(textVisibility);
	}

	
	/**
	 * Checks if all the required data has been entered
	 * @return
	 */
	public boolean isAllDataEntered() {
		boolean result = false;
		
		switch ( selectedType ) {
			case SmsMessageFactory.SMS_TEMPLATE_FIELD_TEXT:
				if ( editTextText.getText().length() <= 0 )
					break;	// no text entered, so not ready
				// fall thru to return ready
			case SmsMessageFactory.SMS_TEMPLATE_FIELD_AMOUNT:
			case SmsMessageFactory.SMS_TEMPLATE_FIELD_ACCOUNT:
				result = true;
				break;
			default:
				// not ready
				break;
		}
		return result;
	}

	
    /**
     * Interface to facilitate removal of SMS field 
     * @author Ian Buttimer
     *
     */
    public interface OnRemoveSmsFieldListener {
        public void onRemoveSmsField(View v);
    }

	private OnRemoveSmsFieldListener onRemoveSmsFieldListener;

    /**
     * Set the remove SMS field listener
     * @param listener
     */
    public void setOnRemoveSmsFieldListener(OnRemoveSmsFieldListener listener) {
    	onRemoveSmsFieldListener = listener;
    }

    /**
     * Interface to facilitate callback when SMS field type is selected
     * @author Ian Buttimer
     *
     */
    public interface OnSmsFieldTypeSelectedListener {
        public void onFieldTypeSelected(View v);
    }

	private OnSmsFieldTypeSelectedListener onSmsFieldTypeSelectedListener;

    /**
     * Set the remove SMS field listener
     * @param listener
     */
    public void setOnSmsFieldTypeSelectedListener(OnSmsFieldTypeSelectedListener listener) {
    	onSmsFieldTypeSelectedListener = listener;
    }
    
    /**
     * Interface to facilitate callback when SMS field move is selected
     * @author Ian Buttimer
     *
     */
    public interface OnSmsFieldMoveSelectedListener {
        public void onFieldMoveSelected(View v, int move);
    }

	private OnSmsFieldMoveSelectedListener onSmsFieldMoveSelectedListener;

    /**
     * Set the remove SMS field listener
     * @param listener
     */
    public void setOnSmsFieldMoveSelectedListener(OnSmsFieldMoveSelectedListener listener) {
    	onSmsFieldMoveSelectedListener = listener;
    }
}
