/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * An extension of the standard EditText widget to restrict the number of digits after a decimal point
 * @author Ian Buttimer
 *
 */
public class AmountEditText extends EditText {

	private int minorUnits;				// number of minor units allowed
	private String minorUnitSeparator;	// minor unit separator

	/**
	 * @param context
	 */
	public AmountEditText(Context context) {
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public AmountEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	
		initEditTest( context, attrs );
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public AmountEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initEditTest( context, attrs );
	}

	
	/**
	 * Initialise the widget 
	 * @param context
	 * @param attrs
	 */
	private void initEditTest( Context context, AttributeSet attrs ) {
		
		if ( attrs != null ) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmountEditText, 0, 0);
			minorUnits = a.getInt(R.styleable.AmountEditText_valueMinorUnits, 0);
			minorUnitSeparator = a.getString(R.styleable.AmountEditText_valueMinorUnitSeparator);
		    a.recycle();
		}
		else
			minorUnits = 0;

		if ( minorUnitSeparator == null )
			minorUnitSeparator = ".";	// by default
		
		// add a text changed listener to ensure that the correct number of minor units is entered
		addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	enforceMinorUnits(s);
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
		
	}
	
	/**
	 * Ensure the minor units limit has not been exceeded
	 * @param s		Editable to process
	 */
	private void enforceMinorUnits(Editable s) {
    	int numUnits = getNumberOfMinorUnitsEntered(s.toString()) - minorUnits;
    	if ( numUnits > 0 ) {
			// too many minor units, drop the excess
			int pos = s.length() - numUnits;
			s.delete(pos, pos+1);
    	}
	}
	
	/**
	 * Return the number of minor units entered 
	 * @param text	String to check
	 * @return		Number of minor units
	 */
	public int getNumberOfMinorUnitsEntered(String text) {
		
    	int dotPos = text.indexOf(minorUnitSeparator);
    	if ( dotPos >= 0 )
    		return (text.length() - 1 - dotPos);
    	else
    		return 0;
	}
	
	/**
	 * Return the number of minor units entered 
	 * @return		Number of minor units
	 */
	public int getNumberOfMinorUnitsEntered() {
		
   		return getNumberOfMinorUnitsEntered( this.getText().toString() );
	}
	
	/**
	 * @return the minorUnits
	 */
	public int getMinorUnits() {
		return minorUnits;
	}

	/**
	 * @param minorUnits the minorUnits to set
	 */
	public void setMinorUnits(int minorUnits) {
		this.minorUnits = minorUnits;
		
		enforceMinorUnits( this.getText() );
	}

	/**
	 * @return the minorUnitSeparator
	 */
	public String getMinorUnitSeparator() {
		return minorUnitSeparator;
	}

	/**
	 * @param minorUnitSeparator the minorUnitSeparator to set
	 */
	public void setMinorUnitSeparator(String minorUnitSeparator) {
		String text = getText().toString().replace(this.minorUnitSeparator, minorUnitSeparator);
		this.minorUnitSeparator = minorUnitSeparator;
		setText(text);
	}

	
	public void setText(double amount) {
		String str = Double.toString(amount);
		setText(str);
	}
}
