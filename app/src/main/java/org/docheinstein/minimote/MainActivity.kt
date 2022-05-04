package org.docheinstein.minimote

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.*
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimote.buttons.ButtonType
import org.docheinstein.minimote.buttons.ButtonEventBus
import org.docheinstein.minimote.databinding.MainBinding
import org.docheinstein.minimote.orientation.Orientation
import org.docheinstein.minimote.orientation.OrientationEventBus
import org.docheinstein.minimote.util.debug
import org.docheinstein.minimote.util.verbose
import org.docheinstein.minimote.util.warn
import javax.inject.Inject

/** Main activity. */
/*
 * This is the only activity of the app, all the UI belongs to Fragments.
 * The transitions between fragment are handled using the android navigation component
 * (https://developer.android.com/guide/navigation).
 *
 * The only responsibilities of this activity, apart from setting
 * the navigation view and the drawer, are publish the following events
 * - orientation state change (cause the activity is the first UI component to see the change)
 * - button events (cause only the activity gets notified)
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var buttonEventBus: ButtonEventBus
    @Inject lateinit var orientationEventBus: OrientationEventBus

    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    private lateinit var binding: MainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verbose("MainActivity.onCreate()")

        // Publish the current orientation state
        // Doing this on MainActivity.onCreate() ensure that every other UI component
        // (created after this activity) will see the same consistent orientation state
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> orientationEventBus.publish(Orientation.Portrait)
            Configuration.ORIENTATION_LANDSCAPE -> orientationEventBus.publish(Orientation.Landscape)
            else -> warn("Unknown orientation")
        }

        binding = MainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        navController = findNavController(R.id.navigation_controller)
        appBarConfig = AppBarConfiguration(setOf(R.id.nav_servers), binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfig)
        binding.navigationView.setupWithNavController(navController)

        verbose("MainActivity.onCreate() DONE")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /* Automatically navigate to the fragment with the same id as the menu item's id.
         * The requirement for this to work is having the ids of the drawer equal
         * to the ids in the nav graph: doing so allows the framework to handle
         * everything correctly, even the selected item menu of the drawer */
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        debug("Detected keyDown for $keyCode")

        var handled = false

        val button = ButtonType.byKeyCode(keyCode)
        if (button != null)
            // Publish the button event
            handled = buttonEventBus.publish(button)

        return handled || super.onKeyDown(keyCode, event)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }
}