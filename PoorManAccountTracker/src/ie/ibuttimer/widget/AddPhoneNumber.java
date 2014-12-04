/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Ian Buttimer
 *
 */
public class AddPhoneNumber extends RelativeLayout {

	private TextView textViewTitle;
	private EditText editTextNumber;

	/**
	 * @param context
	 */
	public AddPhoneNumber(Context context) {
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public AddPhoneNumber(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAddNumberView(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public AddPhoneNumber(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAddNumberView(context, attrs);
	}

	
	/**
	 * Initialise the views
	 * @param context
	 * @param attrs
	 */
	private void initAddNumberView(Context context, AttributeSet attrs) {

		String title = null;
		String hint = null;
		
		if ( attrs != null ) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AddPhoneNumber, 0, 0);
			title = a.getString(R.styleable.AddPhoneNumber_numberTitle);
			hint = a.getString(R.styleable.AddPhoneNumber_numberHint);

			a.recycle();
		}
		
		if ( title == null )
			title = "";
		if ( hint == null )
			hint = "";
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.activity_add_phone_number, this, true);
		
		textViewTitle = (TextView) findViewById(R.id.addPhoneNumber_textViewTitle);
		editTextNumber = (EditText) findViewById(R.id.addPhoneNumber_editTextNumber);
		
		textViewTitle.setText(title);
		
		editTextNumber.setHint(hint);
	}

}
