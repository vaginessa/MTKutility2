package adtdev.com.mtkutility2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
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

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static myLibrary myLib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myLib = new myLibrary(this);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

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
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).commit();

        // set the toolbar title
        int id = fragment.getId();
        String title = getResources().getString(R.string.app_name) + " - " +
                getString(R.string.nav_home);
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        // back is disabled to ensure bluetooth connection with GPS is closed properly
        Toast.makeText(this, R.string.noback, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
    }

    public boolean exit_app() {
        // close navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        // close app
        finish();
        return true;
    }
}
