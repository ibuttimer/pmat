/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Ian Buttimer
 *
 */
public class UserSummary extends RelativeLayout {

	private TextView textViewName;
	private TextView textViewNumber;
	
	/**
	 * @param context
	 */
	public UserSummary(Context context) {
		this(context,null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public UserSummary(Context context, AttributeSet attrs) {
		super(context, attrs);

		String name = null;
		String number = null;
		if ( attrs != null ) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AddUser, 0, 0);
			name = a.getString(R.styleable.AddUser_userName);
			number = a.getString(R.styleable.AddUser_userNumber);
			a.recycle();
		}
		
		if ( name == null )
			name = "";
		if ( number == null )
			number = "0";
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.add_user_user_summary_view, this, true);

		RelativeLayout layout = (RelativeLayout)getChildAt(0);

		textViewName = (TextView) layout.findViewById(R.id.addUser_userName);
		textViewNumber = (TextView) layout.findViewById(R.id.addUser_userPhone);

		textViewName.setText(name);
		textViewNumber.setText(number);
	}

	
	/**
	 * Get the name displayed.
	 */
	public CharSequence getName () {
		return textViewName.getText();
	}
	
	/**
	 * Set the name displayed.
	 * @param text
	 */
	public void setName (CharSequence text) {
		textViewName.setText(text);
	}
	
	/**
	 * Get the number displayed.
	 */
	public CharSequence getNumber () {
		return textViewNumber.getText();
	}
	
	/**
	 * Set the number displayed.
	 * @return
	 */
	public void setNumber(CharSequence text) {
		textViewNumber.setText(text);
	}



}
