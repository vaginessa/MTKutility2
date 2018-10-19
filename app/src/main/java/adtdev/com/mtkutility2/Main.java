package adtdev.com.mtkutility2;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static myLibrary myLib;
    public static Main instace;

    private boolean OK;
    private Thread.UncaughtExceptionHandler appUEH;
    private boolean hasWrite = false;
    private static final int REQUEST_WRITE_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myLib = new myLibrary(this);
        setContext();

        //set custom exception handler as default handler
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }

        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create the navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

//      set the startup screen from the navigation drawer
        Fragment fragment = new HomeFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // set the toolbar title
        String title = getResources().getString(R.string.app_name) + " - " + getString(R.string.nav_home);
        getSupportActionBar().setTitle(title);

        checkFileWritePermisssion();
        if (hasWrite){
            OK = myLib.logOpen();
        } else {
            myLib.showToast(getString(R.string.nowrite));
//            exit_app();
        }
    }//onCreate(Bundle savedInstanceState)

    @Override
    public void onBackPressed() {
        // back is disabled to ensure bluetooth connection with GPS is closed properly
        Toast.makeText(this, R.string.noback, Toast.LENGTH_LONG).show();

//        myLib.testException(0);
    }//onBackPressed()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }//onCreateOptionsMenu(Menu menu)

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle
        // clicks on the Home/Up button, so long as you specify a parent activity in
        // AndroidManifest.xml
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected(MenuItem item)

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_Home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_GetLog) {
            fragment = new GetLogFragment();
        } else if (id == R.id.nav_MakeGPX) {
            fragment = new MakeGPXFragment();
        } else if (id == R.id.nav_GetEPO) {
            fragment = new GetEPOFragment();
        } else if (id == R.id.nav_CheckEPO) {
            fragment = new CheckEPOFragment();
        } else if (id == R.id.nav_UpdtAGPS) {
            fragment = new UpdtAGPSFragment();
        } else if (id == R.id.nav_Settings) {
            fragment = new SettingsFragment();
        } else if (id == R.id.nav_eMail) {
            fragment = new eMailFragment();
        } else if (id == R.id.nav_Help) {
            fragment = new HelpFragment();
        } else if (id == R.id.nav_About) {
            fragment = new AboutFragment();
        } else if (id == R.id.nav_Exit) {
            exit_app();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            String title = getResources().getString(R.string.app_name) + " - " +
                    item.toString();
            getSupportActionBar().setTitle(title);
        }

        return true;
    }//onNavigationItemSelected(MenuItem item)

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // The result of the popup opened with the requestPermissions() method
        // is in that method, you need to check that your application comes here
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasWrite = true;
            }
        }
    }//onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)

    private void checkFileWritePermisssion() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if(hasPermission){
            hasWrite = true;
        }else{
            // ask the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
    }//checkFileWritePermisssion()

    public void exit_app() {
        myLib.logWrite(132, "Main.exit_app()\n");
        // close navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        // close app
        super.onBackPressed();
    }//exit_app()

    public void testException(int Int){
        // call with Int = 0
        int res = 1/Int;
    }

    private void setContext(){
        myLib.mContext = this;
    }

    public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
        private Activity mContext;
        private myLibrary myLib;

        public CustomExceptionHandler(Activity context) {
            mContext = context;
        }

        public void uncaughtException(Thread t, Throwable ex) {
            myLib = Main.myLib;
            OK = true;
            myLib.aborting = true;

            if (!myLib.logFileIsOpen) {
                OK = myLib.logOpen();
            }
            if (OK) {
                myLib.logWrite(132, "\n**** CustomExceptionHandler.uncaughtException()\n");
                myLib.buildCrashReport(ex);
                myLib.setAppFailed(true);
                myLib.logClose();
            }

            Intent intent = new Intent(mContext, Main.class);
            mContext.startActivity(intent);

            // make sure we die, otherwise the app will hang ...
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(2);
        }
    }
}
