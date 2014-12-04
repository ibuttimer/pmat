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
public class CategoryAllocation extends RelativeLayout {

	private TextView textViewCategory;
	private TextView textViewAmount;
	
	private int minorUnits;
	
	/**
	 * @param context
	 */
	public CategoryAllocation(Context context) {
		this(context,null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CategoryAllocation(Context context, AttributeSet attrs) {
		super(context, attrs);

		String category = null;
		String amount = null;
		if ( attrs != null ) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CategoryAllocation, 0, 0);
			category = a.getString(R.styleable.CategoryAllocation_categoryText);
			amount = a.getString(R.styleable.CategoryAllocation_categoryText);
			a.recycle();
		}
		
		if ( category == null )
			category = "";
		if ( amount == null )
			amount = "0";
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.add_transaction_category_allocation_view, this, true);

		RelativeLayout layout = (RelativeLayout)getChildAt(0);

		textViewCategory = (TextView) layout.getChildAt(0);
		textViewAmount = (TextView) layout.getChildAt(1);

		textViewCategory.setText(category);
		textViewAmount.setText(amount);
	}

	
	/**
	 * Get the text displayed.
	 */
	public CharSequence getText () {
		return textViewCategory.getText();
	}
	
	/**
	 * Set the text displayed.
	 * @param text
	 */
	public void setText (CharSequence text) {
		textViewCategory.setText(text);
	}
	
	/**
	 * Set the amount displayed.
	 * @return
	 */
	public void setAmount(CharSequence text) {
		String amt = enforceMinorUnits(text.toString());
		textViewAmount.setText(amt);
	}

	/**
	 * Set the amount displayed.
	 * @return
	 */
	public void setAmount(int amount) {
		setAmount(String.valueOf(amount));
	}

	/**
	 * Set the amount displayed.
	 * @return
	 */
	public void setAmount(double amount) {
		setAmount(String.valueOf(amount));
	}

	
	private String enforceMinorUnits(String amount) {

		StringBuffer result = new StringBuffer(amount);
		int numUnits;
    	int dotPos = amount.indexOf(".");
    	if ( dotPos >= 0 )
    		numUnits = amount.length() - 1 - dotPos;
    	else
    		numUnits = 0;
		if ( (numUnits > 0) && (minorUnits > 0) ) {

			// update for too few
			while ( numUnits < minorUnits ) {
				result.append('0');
				++numUnits;
			}
			
			// update for too many
			if ( numUnits > minorUnits ) {
				int end = result.length();
				result.delete((end - (numUnits - minorUnits)), end);
			}
		}
		return result.toString();
	}
	
	
	/**
	 * Get the amount displayed.
	 */
	public double getAmount() {
		double amount;
		try {
			amount = Double.parseDouble(textViewAmount.getText().toString());
		}
		catch ( NumberFormatException e ) {
			amount = 0;
		}
		return amount;
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
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CategoryAllocation [textViewCategory=" + textViewCategory
				+ ", textViewAmount=" + textViewAmount + "]";
	}



}
