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
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import org.docheinstein.minimote.edit.EditServerFragmentArgs;
import org.docheinstein.minimote.servers.ServersFragmentDirections;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private NavController uiNavi;
    private DrawerLayout uiDrawer;
    private NavigationView uiNavigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        uiNavi = Navigation.findNavController(this, R.id.uiNavi);
        uiDrawer = findViewById(R.id.uiDrawer);
        uiNavigation = findViewById(R.id.uiNavigation);

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(uiNavi.getGraph())
                        .setDrawerLayout(uiDrawer)
                        .build();


        final Toolbar toolbar = findViewById(R.id.uiToolbar);
        setSupportActionBar(toolbar);

//        NavigationUI.setupActionBarWithNavController(this, uiNavi, appBarConfiguration);
        NavigationUI.setupWithNavController(toolbar, uiNavi, appBarConfiguration);

        uiNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Log.v(TAG, "Selected menu item with id: " + menuItem.getItemId());
                switch (menuItem.getItemId()) {
                    case R.id.uiSettingsMenuItem:
                        uiNavi.navigate(NavDirections.actionSettings());
                        uiDrawer.closeDrawers();
                        break;
                    case R.id.uiServersMenuItem:
                        uiNavi.navigate(NavDirections.actionServers());
                        uiDrawer.closeDrawers();
                        break;
                    case R.id.uiHotkeysMenuItem:
                        uiNavi.navigate(NavDirections.actionHotkeys());
                        uiDrawer.closeDrawers();
                        break;
                }

                return false;
            }
        });
    }
}
