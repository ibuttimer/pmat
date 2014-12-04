package ie.ibuttimer.pmat;

import ie.ibuttimer.pmat.util.DateTimeFormat;

import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		TextView tv;
		Calendar buildDate = getBuildDate(this);
		DateTimeFormat fmt = new DateTimeFormat(this);
		
		PackageInfo pInfo;
		try {
			// version
			tv = (TextView)this.findViewById(R.id.aboutActivity_Version );
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			tv.setText( getString(R.string.about_version) + " " + pInfo.versionName + " " + fmt.formatShortDate(buildDate.getTime()) );
			
			// copyright
			tv = (TextView)this.findViewById(R.id.aboutActivity_Copyright);
			tv.setText( getString(R.string.about_copyright) + " " + getString(R.string.about_developer) + " " + buildDate.get(Calendar.YEAR) );
			// 
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// add credits
		tv = (TextView)this.findViewById(R.id.aboutActivity_Credits);
		StringBuffer credit = new StringBuffer( "\n" + getString(R.string.about_credits) + "\n" );
		
		String credits[] = getResources().getStringArray(R.array.about_credit_list);
		for ( int i = 0; i < credits.length; ++i ) {
			credit.append("- " + credits[i] + "\n");
		}
		tv.setText( credit.toString() );
	}
	
	
    /**
     * Return the application build date.
     * @param context	- application context
     * @return			build date as number of milliseconds since Jan. 1, 1970.
     */
    public long getBuildDateMs(Context context) {

        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();

            zf.close();
            
            return time;

        } catch (Exception e) {
        }

        return 0l;
    }

    /**
     * Return the application build date as a Calendar object.
     * @param context	- application context
     * @return
     */
    public Calendar getBuildDate(Context context) {

    	Calendar calendar = null;
    	long buildDateMs = getBuildDateMs(context);
		if ( buildDateMs > 0 ) {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(buildDateMs);
		}
        return calendar;
    }
    

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// no menu
		return false;
	}

}
