package org.docheinstein.minimote;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.servers.ServersFragmentDirections;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private NavController uiNavigationFragment;
    private DrawerLayout uiDrawer;
    private NavigationView uiNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        uiNavigationFragment = Navigation.findNavController(this, R.id.uiNavigationFragment);
        uiDrawer = findViewById(R.id.uiDrawer);
        uiNavigationView = findViewById(R.id.uiNavigationView);

        AppBarConfiguration toolbarConfiguration =
                new AppBarConfiguration.Builder(uiNavigationFragment.getGraph())
                        .setDrawerLayout(uiDrawer)
                        .build();


        final Toolbar toolbar = findViewById(R.id.uiToolbar);
        setSupportActionBar(toolbar);

        NavigationUI.setupWithNavController(toolbar, uiNavigationFragment, toolbarConfiguration);

        uiNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Log.v(TAG, "Selected drawer menu item: " + menuItem);
                switch (menuItem.getItemId()) {
                    case R.id.uiServersMenuItem:
                        navigateFromDrawerTo(NavDirections.actionServers());
                        break;
                    case R.id.uiHotkeysMenuItem:
                        navigateFromDrawerTo(NavDirections.actionHotkeys());
                        break;
                    case R.id.uiSettingsMenuItem:
                        navigateFromDrawerTo(NavDirections.actionSettings());
                        break;
                }

                return false;
            }
        });

        DB.init(this);

        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final MinimoteServerEntity autoConnectServer = DB.getInstance().minimoteServerDao().getAutoConnectionRequired();
                if (autoConnectServer == null)  {
                    Log.d(TAG, "No servers with the auto connection flag, doing nothing");
                    return;
                }

                // Attempt connection
                Log.d(TAG, "Auto connection required for server " + autoConnectServer + ", attempting so");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ServersFragmentDirections.ActionController action =
                                ServersFragmentDirections.actionController(
                                        autoConnectServer.address,
                                        autoConnectServer.port);
                        uiNavigationFragment.navigate(action);
                    }
                });

            }
        });
    }

    private void navigateFromDrawerTo(androidx.navigation.NavDirections  dir) {
        uiNavigationFragment.navigate(dir);
        uiDrawer.closeDrawers();
    }
}
