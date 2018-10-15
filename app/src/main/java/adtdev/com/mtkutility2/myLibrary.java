package adtdev.com.mtkutility2;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class myLibrary {
    Context lContext;

    public myLibrary(Activity context) {
        lContext = context;

//        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//        sharedPrefEditor = sharedPrefs.edit();
//        appPrefs = mContext.getSharedPreferences("otherprefs", Context.MODE_PRIVATE);
//        appPrefEditor = appPrefs.edit();
    }

    public void showToast(String msg){
        Toast.makeText(lContext, msg, Toast.LENGTH_LONG).show();
    }
}
