/**
 * 
 */
package ie.ibuttimer.widget;

import ie.ibuttimer.pmat.util.AudioPlayer;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

/**
 * Extension of the standard android Button widget to allow hooking into OnClickListener 
 * @author Ian Buttimer
 *
 */
public class AppButton extends Button {

	private OnClickListener setListener;	// listener set by app
	private Context context;
	
	/**
	 * @param context
	 */
	public AppButton(Context context) {
		super(context);
		init(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public AppButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public AppButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/**
	 * Initialise this object
	 * @param context
	 */
	private void init(Context context) {
		this.context = context;
		this.setOnClickListener(null);
	}
	
	
	/* (non-Javadoc)
	 * @see android.view.View#setOnClickListener(android.view.View.OnClickListener)
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		setListener = l;
		super.setOnClickListener(hookListener);
	}

	
	private OnClickListener hookListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			// play the button click sound
			AudioPlayer.playButtonClick(context);
			
			// call the listener the app set
			if ( setListener != null )
				setListener.onClick(v);
		}
	};
	
}
