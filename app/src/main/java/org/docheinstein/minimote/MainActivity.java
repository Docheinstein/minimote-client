package org.docheinstein.minimote;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;

import org.docheinstein.minimote.lifecycle.AppLifecycleManager;
import org.docheinstein.minimote.ui.base.MinimoteFragment;
import org.docheinstein.minimote.ui.controller.MinimoteControllerFragment;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.utils.ResUtils;


public class MainActivity
        extends AppCompatActivity
        implements MinimoteFragment.MinimoteFragmentOwner {

    private static final String TAG = "MainActivity";

    private NavController uiNavigationFragment;
    private DrawerLayout uiDrawer;
    private NavigationView uiNavigationView;

    private Fragment mTopFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.v(TAG, "MainActivity.onCreate");

        ProcessLifecycleOwner.get().getLifecycle().addObserver(AppLifecycleManager.getInstance());

        Bundle extras = new Bundle();

        // auto connect just the first time (on app start up)
//        extras.putBoolean(
//                ServersFragment.AUTO_CONNECT_EXTRA,
//                attemptAutoConnect.getAndSet(false));

        uiNavigationFragment = Navigation.findNavController(this, R.id.uiNavigationFragment);
        NavGraph g = uiNavigationFragment.getGraph();
        uiNavigationFragment.setGraph(g, extras);

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
                    case R.id.uiHwHotkeysMenuItem:
                        navigateFromDrawerTo(NavDirections.actionHwHotkeys());
                        break;
                    case R.id.uiSettingsMenuItem:
                        navigateFromDrawerTo(NavDirections.actionSettings());
                        break;
                }

                return false;
            }
        });


        DB.init(this);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        super.onKeyDown(keyCode, event);
        Log.d(TAG, "Key down: " + keyCode);

        if (mTopFragment instanceof MinimoteControllerFragment) {
            return ((MinimoteControllerFragment) mTopFragment).onMediaButton(keyCode, event);
        }

        return false;
    }

    @Override
    public void onFragmentResult(Fragment from, Bundle args) {
        Log.d(TAG, "Received fragment result");
        if (from instanceof MinimoteControllerFragment) {
            int connectivityResult = args.getInt(MinimoteControllerFragment.RESULT_KEY_CONNECTIVITY);
            if (connectivityResult == MinimoteControllerFragment.RESULT_VALUE_CONNECTIVITY_ERROR) {
                Log.w(TAG, "Connection error occurred");
                showConnectionWithServerFailedAlert(args.getString(MinimoteControllerFragment.RESULT_KEY_SERVER_ADDRESS));
            }
        }
    }

    @Override
    public void onFragmentResumed(Fragment f) {
        Log.d(TAG, "Fragment resumed: " + f);

        mTopFragment = f;
    }

    private void showConnectionWithServerFailedAlert(String serverAddress) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.connection_failed_dialog_title)
                .setMessage(String.format(
                        ResUtils.getString(R.string.connection_failed_dialog_message, this),
                        serverAddress)
                )
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void navigateFromDrawerTo(androidx.navigation.NavDirections  dir) {
        uiNavigationFragment.navigate(dir);
        uiDrawer.closeDrawers();
    }
}
