package org.docheinstein.minimotek

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.ui.*
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.buttons.ButtonType
import org.docheinstein.minimotek.buttons.ButtonEventBus
import org.docheinstein.minimotek.util.debug
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

    @Inject lateinit var buttonEventBus: ButtonEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        debug("MainActivity.onCreate()")
        setContentView(R.layout.main)

        // toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        navView = findViewById(R.id.navigation_view)
        navController = findNavController(R.id.navigation_controller)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_servers),
            drawer
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { ctrl, dest, args ->
            debug("Destination changed to: ${dest.id}")
        }

//        onBackPressedDispatcher.addCallback(this) {
//            // Handle the back button event
//            debug("OnBack")
//        }

        debug("MainActivity.onCreate() DONE")
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Automatically navigation to the fragment with the same id as the menu item's id
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }
//
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        debug("Detected keyDown for $keyCode")
//        var handled = false
//
//        val button = ButtonType.byKeyCode(keyCode)
//        if (button != null) {
//            handled = buttonEventBus.publish(button)
//        }
//
//        return if (handled) true else super.onKeyDown(keyCode, event)
//    }

    private fun navigateFromDrawerTo(dir: NavDirections) {
        navController.navigate(dir)
        drawer.closeDrawers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

//    override fun onNavigateUp(): Boolean {
//        debug("onNavigateUp")
//        return navController.navigateUp(appBarConfiguration) || super.onNavigateUp()
//    }

    override fun onSupportNavigateUp(): Boolean {
        debug("onSupportNavigateUp")
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}