package org.docheinstein.minimote.ui.controller

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimote.R
import org.docheinstein.minimote.database.hotkey.sw.SwHotkey
import org.docheinstein.minimote.databinding.ControllerBinding
import org.docheinstein.minimote.ui.swhotkeys.SwHotkeyView
import org.docheinstein.minimote.util.debug
import org.docheinstein.minimote.util.verbose
import org.docheinstein.minimote.util.warn

/**
 * Fragment representing the screen that allows the remote control of the server.
 * It's composed by several components that are able to detects events,
 * based on which a different type of packet is sent to the server.
 * Right now, the components are the following:
 * - Touchpad area: mouse movements and mouse clicks
 *      1 finger movement: mouse movement
 *      2 fingers movement: scroll
 *      1 finger click: left click
 *      2 fingers click: right click
 *      3 fingers click: middle click
 * - Touchpad buttons: mouse clicks (left, right)
 * - Keyboard: keys and texts
 * - Software hotkeys (buttons shown on the screen): hotkeys
 * - Hardware hotkeys (physical phone buttons): hotkeys
 */

@AndroidEntryPoint
class ControllerFragment : Fragment(), TouchpadAreaView.TouchpadListener, KeyboardEditText.KeyboardListener{

    private val viewModel: ControllerViewModel by viewModels()
    private lateinit var binding: ControllerBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        verbose("ControllerFragment.onCreateView()")

        binding = ControllerBinding.inflate(inflater, container, false)

        // Touchpad area
        binding.touchpadArea.listener = this

        // Touchpad buttons
        binding.touchpadLeftButton.setOnTouchListener { _, event ->
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    debug("Touchpad left down")
                    viewModel.leftDown()
                }
                MotionEvent.ACTION_UP -> {
                    debug("Touchpad left up")
                    viewModel.leftUp()
                }
            }
            false // return false to make drawable selector work
        }

        binding.touchpadRightButton.setOnTouchListener { _, event ->
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    debug("Touchpad right down")
                    viewModel.rightDown()
                }
                MotionEvent.ACTION_UP -> {
                    debug("Touchpad right up")
                    viewModel.rightUp()
                }
            }
            false // return false to make drawable selector work
        }

        // Splash Overlay
        binding.splash.setOnTouchListener { _, _ ->
            true // prevent propagation of touch events if splash screen is atop
        }

        // Keyboard
        binding.keyboardText.listener = this

        // Widgets
        binding.keyboardWidget.setOnClickListener {
            debug("Toggling keyboard widget")
            viewModel.toggleKeyboard()
        }
        binding.touchpadButtonsWidget.setOnClickListener {
            debug("Toggling touchpad buttons widget")
            viewModel.toggleTouchpadButtons()
        }
        binding.hotkeysWidget.setOnClickListener {
            debug("Toggling hotkeys widget")
            viewModel.toggleHotkeys()
        }

        // Observe widgets state change
        // (we have to follow this line, instead of updating widgets directly from the UI,
        // mostly for keep the state in the viewModel in order to restore the same state
        // on orientation changes).
        // distinctUntilChanged must be used in order to prevent emission of the same state,
        // since it would lead to recursive call loop (this would happen because keyboard is
        // closed also from UI)
        viewModel.isKeyboardWidgetEnabled.distinctUntilChanged().observe(viewLifecycleOwner) { enabled ->
            binding.keyboardWidget.highlighted = enabled
            binding.keyboardText.setKeyboardOpen(requireActivity(), enabled)
        }

        viewModel.isTouchpadButtonsWidgetEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.touchpadButtonsWidget.highlighted = enabled
            binding.touchpadButtonsContainer.isVisible = enabled
        }

        viewModel.isHotkeysWidgetEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.hotkeysWidget.highlighted = enabled
            binding.hotkeysContainer.isVisible = enabled
        }

        // Observe connection state change
        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            debug("UI notified about new connection state: $state")
            when (state) {
                ControllerViewModel.ConnectionState.Connecting -> {
                    // Show overlay (Connecting...)
                    // No UI components can be used while the splash screen is atop
                    binding.splashOverlay.isVisible = true
                    binding.splashOverlay.alpha = 1.0f

                }
                ControllerViewModel.ConnectionState.Connected -> {
                    // Fade out the splash overlay
                    if (binding.splashOverlay.isVisible) {
                        binding.splashOverlay.animate().alpha(0.0f).setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {}
                            override fun onAnimationCancel(animation: Animator?) {}
                            override fun onAnimationRepeat(animation: Animator?) {}
                            override fun onAnimationEnd(animation: Animator?) {
                                // Hide the overlay after the fade
                                // so that it won't consume touch events anymore
                                binding.splashOverlay.isVisible = false
                            }
                        })
                    }
                }
                ControllerViewModel.ConnectionState.Disconnected -> {
                    // Close the fragment since we are not connected anymore
                    debug("Quitting controller fragment since we are not connected anymore")
                    showConnectionErrorAlert()
                    findNavController().navigateUp()
                }
                else -> warn("Unknown connection state: $state")
            }
        }

        // Observe hotkeys for current orientation
        viewModel.currentOrientationHotkeys.observe(viewLifecycleOwner) { hotkeys ->
            updateHotkeys(hotkeys)
        }

        return binding.root
    }

    private fun updateHotkeys(hotkeys: List<SwHotkey>) {
        debug("Updating hotkeys with ${hotkeys.size} hotkeys")
        binding.hotkeysContainer.removeAllViews()

        for (hotkey in hotkeys) {
            val hotkeyView = SwHotkeyView(requireContext(), hotkey = SwHotkeyView.Hotkey.fromSwHotkey(hotkey))
            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            lp.leftMargin = hotkey.x
            lp.topMargin = hotkey.y
            hotkeyView.layoutParams = lp

            hotkeyView.setOnClickListener { _ ->
                debug("Clicked on hotkey $hotkey")
                viewModel.hotkey(hotkey)
            }

            binding.hotkeysContainer.addView(hotkeyView)
        }
    }

    // Touchpad events
    override fun onTouchpadDown(ev: MotionEvent) = viewModel.touchpadDown(ev)
    override fun onTouchpadUp(ev: MotionEvent) = viewModel.touchpadUp(ev)
    override fun onTouchpadPointerDown(ev: MotionEvent) = viewModel.touchpadPointerDown(ev)
    override fun onTouchpadPointerUp(ev: MotionEvent) = viewModel.touchpadPointerUp(ev)
    override fun onTouchpadMovement(ev: MotionEvent) = viewModel.touchpadMovement(ev)

    // Keyboard events
    override fun onKeyboardShown() {
        debug("Keyboard shown")
        viewModel.openKeyboard()
    }

    override fun onKeyboardHidden() {
        debug("Keyboard hidden")
        viewModel.closeKeyboard()
    }

    override fun onKeyboardText(s: CharSequence, start: Int, before: Int, count: Int) {
        debug("Keyboard text: (str = $s, start = $start, before = $before, count = $count)")

        if (count > before) {
            // Insertion
            val c = s[start + before]
            debug("+ $c")
            viewModel.write(c)
        } else if (before > count) {
            // Deletion
            debug("<-")
            viewModel.keyClick(KeyEvent.KEYCODE_DEL)
        }
    }

    override fun onKeyboardKey(keyCode: Int, event: KeyEvent): Boolean {
        debug("Key pressed: $keyCode")

        return when (event.action) {
            KeyEvent.ACTION_DOWN -> viewModel.keyDown(keyCode)
            KeyEvent.ACTION_UP -> viewModel.keyUp(keyCode)
            else -> false
        }
    }

    private fun showConnectionErrorAlert() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.connection_error_dialog_title)
            .setMessage(getString(R.string.connection_error_dialog_message,
                viewModel.serverAddress, viewModel.serverPort))
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}