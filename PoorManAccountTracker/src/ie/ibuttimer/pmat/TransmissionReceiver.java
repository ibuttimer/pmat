/**
 * 
 */
package ie.ibuttimer.pmat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ie.ibuttimer.pmat.sms.SmsProcessor;
import ie.ibuttimer.pmat.util.Logger;

/**
 * @author Ian Buttimer
 *
 */
public class TransmissionReceiver extends BroadcastReceiver {

	
	private SmsProcessor smsManager;
	
	/**
	 * 
	 */
	public TransmissionReceiver() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Logger.d("Received ACTION_BOOT_COMPLETED");
			smsManager = SmsProcessor.getInstance(context);
		}
		else if (intent.getAction().equals(SmsProcessor.SMS_RECEIVED)) {
			Logger.d("Received SMS_RECEIVED");
			smsManager = SmsProcessor.getInstance(context);
			smsManager.onReceive(context, intent);
		}
		else if (intent.getAction().equals(SmsProcessor.SENT_SMS)) {
			Logger.d("Received SENT_SMS: " + intent.getStringExtra(SmsProcessor.SMS_PAYLOAD));
		}
		else if (intent.getAction().equals(SmsProcessor.DELIVERED_SMS)) {
			Logger.d("Received DELIVERED_SMS: " + intent.getStringExtra(SmsProcessor.SMS_PAYLOAD));
		}
	}

}
