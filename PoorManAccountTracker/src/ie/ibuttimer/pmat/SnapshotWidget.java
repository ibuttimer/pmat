/**
 * 
 */
package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.db.AccountCurrency;
import ie.ibuttimer.pmat.db.DatabaseManager;
import ie.ibuttimer.pmat.db.SQLiteCommandFactory.SelectionArgs;
import ie.ibuttimer.pmat.util.Constants;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

/**
 * Account snapshot widget for the home screen.
 * @author Ian Buttimer
 *
 */
public class SnapshotWidget extends AppWidgetProvider {

	/**
	 * 
	 */
	public SnapshotWidget() {
	}

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onEnabled(android.content.Context)
	 */
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		updateSnapshot(context);
	}

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		updateSnapshot(context, appWidgetManager, appWidgetIds);
	}

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		if ( Constants.UPDATE_SNAPSHOT_ACTION.equals(intent.getAction()) ) {
			updateSnapshot(context);
		}
	}

	
	/**
	 * Update the widget contents
	 * @param context
	 * @param appWidgetManager
	 * @param appWidgetIds
	 */
	public void updateSnapshot(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		String accountNames = context.getResources().getString(R.string.snapshot_widget_no_accounts);
		String accountBal = new String();
		
		SelectionArgs args = PreferenceControl.getWidgetAccountsSelection(context);
		if ( args != null ) {
			Cursor c = context.getContentResolver().query(DatabaseManager.ACCOUNT_SNAPSHOT_URI, 
															null, args.selection, args.selectionArgs, null);
			if (c.moveToFirst()) {
				int nameIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_NAME);
				int balanceIdx = c.getColumnIndex(DatabaseManager.ACCOUNT_AVAILBAL);
				int currencyIdx = c.getColumnIndex(DatabaseManager.CURRENCY_CODE);

				accountNames = new String();

				do {
					// Extract the details.
					String name = c.getString(nameIdx);
					Double balance = c.getDouble(balanceIdx);
					String currency = c.getString(currencyIdx);
	
					if ( !accountNames.isEmpty() )
						accountNames = accountNames.concat("\n");
					accountNames = accountNames.concat(name + "  ");
					
					if ( !accountBal.isEmpty() )
						accountBal = accountBal.concat("\n");
					accountBal = accountBal.concat( AccountCurrency.formatDouble(currency, balance, true) );
					
				} while(c.moveToNext());
			}
			c.close();
		}

		// update the widget display
		final int N = appWidgetIds.length;
		for ( int i = 0; i < N; ++i ) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.snapshot_widget);
			views.setTextViewText(R.id.widgetTextViewAccounts, accountNames);
			views.setTextViewText(R.id.widgetTextViewBalance, accountBal);

            // Create an Intent to launch Activity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // attach an on-click listener
            views.setOnClickPendingIntent(R.id.widgetLayoutAccounts, pendingIntent);
            views.setOnClickPendingIntent(R.id.widgetTextViewAccounts, pendingIntent);
            views.setOnClickPendingIntent(R.id.widgetTextViewBalance, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	/**
	 * Update the widget contents
	 * @param context
	 */
	public void updateSnapshot(Context context) {
		
		ComponentName widget = new ComponentName(context, SnapshotWidget.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);
		updateSnapshot(context, appWidgetManager, appWidgetIds);
	}
	
	
}
