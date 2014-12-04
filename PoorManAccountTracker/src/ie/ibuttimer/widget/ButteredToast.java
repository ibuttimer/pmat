/**
 * 
 */
package ie.ibuttimer.widget;

import java.util.Formatter;

import android.content.Context;
import android.widget.Toast;

/**
 * An extension of the standard Toast class giving some extra formatting options
 * @author Ian Buttimer
 *
 */
public class ButteredToast extends Toast {

	/**
	 * @param context
	 */
	public ButteredToast(Context context) {
		super(context);
	}
	
	
	/**
	 * Make a standard toast that just contains a text view with the text from a resource.
	 * @param context		- The context to use. Usually your Application or Activity object.
	 * @param resId			- The resource id of the string resource to use. Can be formatted text.
	 * @param duration		- How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 * @param formatArgs	- Any arguments required to complete the message string
	 * @return
	 */
	public static Toast makeText(Context context, int resId, int duration, Object... formatArgs) {
		
		Toast toast = Toast.makeText(context, resId, duration);
		if ( formatArgs != null ) {
			String msg = context.getString(resId, formatArgs);
			toast.setText(msg);
		}
		toast.setDuration(duration);
		return toast;
	}
	
	/**
	 * Make a standard toast that just contains a text view with the text from a resource.
	 * @param context		- The context to use. Usually your Application or Activity object.
	 * @param resId			- The resource id of the string resource to use. Can be formatted text.
	 * @param duration		- How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 * @param formatArgs	- Any arguments required to complete the message string
	 * @return
	 */
	public static Toast makeText(Context context, CharSequence text, int duration, Object... formatArgs) {
		
		Toast toast = Toast.makeText(context, text, duration);
		if ( formatArgs != null ) {
			Formatter fmt = new Formatter();
			toast.setText(fmt.format(text.toString(), formatArgs).out().toString());
			fmt.close();
		}
		toast.setDuration(duration);
		return toast;
	}

}
