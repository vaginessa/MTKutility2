package adtdev.com.mtkutility2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;

public class myLibrary {
    public Activity mContext;

    public SharedPreferences sharedPrefs;
    public SharedPreferences appPrefs;
    public SharedPreferences.Editor sharedPrefEditor;
    public SharedPreferences.Editor appPrefEditor;

    private boolean OK;
    public boolean failed = false;
    public boolean aborting = false;
    boolean sendToLogCat = android.os.Debug.isDebuggerConnected();

    private int versionNumber;
    private int screenWidth;
    private int screenHeight;
    private int screenDPI;
    private String PMversion = "get package info xxfailed";

    public boolean logFileIsOpen = false;
    public File logPath;
    public File logFile;
    public File errFile;
    private String logPathName = "MTKutility";
    private String logFileName = "MTKutilityLog.txt";
    private String errFileName = "MTKutilityErr.txt";
    private static OutputStreamWriter logWriter;
    private FileOutputStream fOut;

    public myLibrary(Activity context) {
        mContext = context;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPrefEditor = sharedPrefs.edit();
        appPrefs = mContext.getSharedPreferences("otherprefs", Context.MODE_PRIVATE);
        appPrefEditor = appPrefs.edit();
    }//myLibrary()

    public void askForEmail() {
        logWrite(132, "myLibrary.askForEmail()");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        // set dialog message
        alertDialogBuilder
                .setMessage(mContext.getString(R.string.crashLog))
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                sendEmail(1);
                            }
                        });
        //show alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    } //askForEmail()

    public void buildCrashReport(Throwable ex) {
        logWrite(132, "myLibrary.buildCrashReport()");
        logWrite(132, "********** Stack **********\n");
        final Writer stringwriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringwriter);
        ex.printStackTrace(printWriter);
        String stacktrace = stringwriter.toString();
        printWriter.close();
        int len = stacktrace.length();
        logWrite(len, stringwriter.toString());
        logWrite(132, "**** End of current Report ***");
    }//buildCrashReport()

    public void goSleep(int mSec) {
        logWrite(132, "myLibrary.goSleep(" + Integer.toString(mSec) + ")");
        try {
            Thread.sleep(mSec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }//goSleep()

    public void logClose() {
        logWrite(132, "myLibrary.logClose()\n");
        if (logFileIsOpen) {
            try {
                logWriter.flush();
                logWriter.close();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            logFileIsOpen = false;
        }
    } //logClose()

    public boolean logOpen() {
        // Create log file objects for the the email method
        OK = true;
        logFileIsOpen = false;
        logPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), logPathName);
        // make sure MTKutility directory exists - create if it is missing
        if (!logPath.exists()) {
            OK = logPath.mkdirs();
        }

        if (!OK) {
            showToast("fatal error: create " + logPath + " failed");
            return false;
        }

        logFile = new File(logPath, logFileName);
        errFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), errFileName);

        // did previous app execute fail - ask user to send an email
        failed = appPrefs.getBoolean("appFailed", false);
        if (failed) {
            if (logFile.exists()) {
                // rename log file to preserve error log for email
                logFile.renameTo(errFile);
                askForEmail();
                setAppFailed(false);
            }
        }

        if (logFile.exists()) {
            logFile.delete();
//            goSleep(500);
        }

        try {
            logFile.createNewFile();
            fOut = new FileOutputStream(logFile);
            logFileIsOpen = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (logFileIsOpen) {
            logWriter = new OutputStreamWriter(fOut);
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            logWrite(132, "+++++++ log file opened " + currentDateTimeString + "\n");
            logWrite(132, "myLibrary.logOpen()");
            writePhoneInfo();
        }
        return true;
    }//logOpen()

    public void logWrite(int len, String dMsg) {
        if (!logFileIsOpen) {
            return;
        }

        if (!dMsg.contains("uncaught")) {
            if (dMsg.length() > len) {
                dMsg = dMsg.substring(0, len - 1) + " ...";
            }
        }
        if (sendToLogCat) {
            Log.d("#### ", dMsg);
        }

        try {
            logWriter.append(dMsg + "\n");
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//logWrite()

    public void sendEmail(int idx) {
        logWrite(132, "myLibrary.sendEmail()");
        if (idx == 0) {
            //abort email if file can't be processed
            if (!logFile.exists() || !logFile.canRead()) {
                return;
            } else {
                if (!errFile.exists() || !errFile.canRead()) {
                    return;
                }
            }
        }

        //create lookup table for email body text
        final int[] LOOKUP_TABLE = new int[]{
                R.string.emailtext,
                R.string.errortext};
        Uri uri;

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mContext.getString(R.string.myEmail)});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MTKutility2 log file");
        emailIntent.putExtra(Intent.EXTRA_TEXT, mContext.getString(LOOKUP_TABLE[idx]));
        if (idx == 0) {
            uri = Uri.fromFile(logFile);
        } else {
            uri = Uri.fromFile(errFile);
        }
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

/*		try {
            logWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
        mContext.startActivity(Intent.createChooser(emailIntent, mContext.getResources().getString(R.string.emailMsg)));
    }//sendEmail()

    public void setAppFailed(Boolean x) {
        logWrite(132, "myLibrary.setAppFailed()");
        appPrefEditor.putBoolean("appFailed", x);
        appPrefEditor.commit();
    }//setAppFailed()

    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    } //showToast()

    public void testException(int Int){
        // call with Int = 0
        int res = 1/Int;
    }//testException(int Int)

    private void writePhoneInfo() {
        logWrite(132, "myLibrary.writePhoneInfo()");
        //get screen size
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        screenDPI = metrics.densityDpi;
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            PMversion = pi.versionName;
            versionNumber = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            logWrite(132, "Could not get Version information for " + mContext.getPackageName());
        }

        logWrite(132, "************ DEVICE INFO ************");
        logWrite(132, "=== Brand:    " + Build.BRAND);
        logWrite(132, "=== Device:  " + Build.DEVICE);
        logWrite(132, "=== Model:   " + Build.MODEL);
        logWrite(132, "=== ID:          " + Build.ID);
        logWrite(132, "=== Product: " + Build.PRODUCT);
        logWrite(132, "=== Screen DPI:       " + screenDPI);
        logWrite(132, "=== Screen width:   " + screenWidth);
        logWrite(132, "=== Screen height: " + screenHeight);

        logWrite(132, "\n************ FIRMWARE ************");
        logWrite(132, "=== Android Version:       " + Build.VERSION.RELEASE);
        logWrite(132, "=== Android Increment: " + Build.VERSION.INCREMENTAL);
        logWrite(132, "=== Board: " + Build.BOARD + "\n");

    }//writePhoneInfo()
}


