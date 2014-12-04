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
public class AccountBalanceView extends RelativeLayout {

	private TextView textViewName;
	private TextView textViewBalance;
	
	
	/**
	 * @param context
	 */
	public AccountBalanceView(Context context) {
		this(context,null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public AccountBalanceView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initAccountBalanceView(context, attrs);
	}
	

	private void initAccountBalanceView(Context context, AttributeSet attrs) {

		String name = null;
		String balance = null;
		
		if ( attrs != null ) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AccountBalanceView, 0, 0);
			name = a.getString(R.styleable.AccountBalanceView_accountName);
			balance = a.getString(R.styleable.AccountBalanceView_accountBalance);

			a.recycle();
		}
		
		if ( name == null )
			name = "";
		if ( balance == null )
			balance = "0";
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.activity_main_account_view, this, true);

		textViewName = (TextView) findViewById(R.id.accountView_textViewName);
		textViewBalance = (TextView) findViewById(R.id.accountView_textViewBal);
		
		textViewName.setText(name);
		
		textViewBalance.setText(balance);
	}


	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public AccountBalanceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initAccountBalanceView(context, attrs);
	}

	/**
	 * @return the account name
	 */
	public String getAccountName() {
		return textViewName.getText().toString();
	}

	/**
	 * @param name the account name to set
	 */
	public void setAccountName(String name) {
		textViewName.setText(name);
	}

	/**
	 * @param colour the colour name to set
	 */
	public void setAccountNameColour(int colour) {
		textViewName.setTextColor(colour);
	}

	/**
	 * @return the account balance
	 */
	public String getAccountBalance() {
		return textViewBalance.getText().toString();
	}

	/**
	 * @param balance the account Balance to set
	 */
	public void setAccountBalance(String balance) {
		textViewBalance.setText(balance);
	}

	/**
	 * @param colour the colour name to set
	 */
	public void setAccountBalanceColour(int colour) {
		textViewBalance.setTextColor(colour);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AccountBalanceView [name=" + getAccountName()
				+ ", balance=" + getAccountBalance() + "]";
	}
	
	


}
