package org.docheinstein.minimotek.ui.controller

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.ControllerBinding
import org.docheinstein.minimotek.ui.controller.keyboard.KeyboardEditText
import org.docheinstein.minimotek.ui.controller.touchpad.TouchpadAreaView
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn


@AndroidEntryPoint
class ControllerFragment : Fragment(), TouchpadAreaView.TouchpadListener, KeyboardEditText.KeyboardListener{

    private val viewModel: ControllerViewModel by viewModels()
    private lateinit var binding: ControllerBinding


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val address = ControllerFragmentArgs.fromBundle(requireArguments()).address
        val port = ControllerFragmentArgs.fromBundle(requireArguments()).port
        debug("ControllerFragment.onCreateView() for server = $address:$port")

        binding = ControllerBinding.inflate(inflater, container, false)

        // Touchpad
        binding.touchpadArea.listener = this

        binding.touchpadLeftButton.setOnTouchListener { v, event ->
            when(event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> { viewModel.leftDown() }
                MotionEvent.ACTION_UP -> { viewModel.leftUp()}
            }
            true
        }

        binding.touchpadRightButton.setOnTouchListener { v, event ->
            when(event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> { viewModel.rightDown() }
                MotionEvent.ACTION_UP -> { viewModel.rightUp()}
            }
            true
        }

        // Overlay

        binding.splash.setOnTouchListener { _, _ ->
            // prevent propagation of touch events if splash screen is atop
            true
        }

        // Keyboard
        binding.keyboardText.listener = this

        // Widgets
        binding.keyboardWidget.setOnClickListener {
            viewModel.toggleKeyboard()
        }

        binding.touchpadButtonsWidget.setOnClickListener {
            viewModel.toggleTouchpadButtons()
        }

        // Detect widgets state

        // IMPORTANT
        // use distinctUntilChanged to prevent emission of the same state,
        // since it would lead to recursive call loop
        // (since keyboard is closed also from UIHH
        viewModel.keyboard.distinctUntilChanged().observe(viewLifecycleOwner) { enabled ->
            binding.keyboardWidget.setHighlight(enabled)
            binding.keyboardText.setKeyboardOpen(requireActivity(), enabled)
        }

        viewModel.touchpadButtons.observe(viewLifecycleOwner) { enabled ->
            binding.touchpadButtonsWidget.setHighlight(enabled)
            binding.touchpadButtonsContainer.isVisible = enabled
        }


        // Detect connection state change
        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            debug("UI notified about new connection state: $state")
            when (state) {
                ControllerViewModel.ConnectionState.Connecting -> {
                    // show overlay
                    binding.overlay.isVisible = true
                    binding.overlay.alpha = 1.0f

                }
                ControllerViewModel.ConnectionState.Connected -> {
                    if (binding.overlay.isVisible) {
                        binding.overlay.animate().alpha(0.0f).setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {}
                            override fun onAnimationCancel(animation: Animator?) {}
                            override fun onAnimationRepeat(animation: Animator?) {}
                            override fun onAnimationEnd(animation: Animator?) {
                                // hide the overlay so that it won't consume touch events anymore
                                binding.overlay.isVisible = false
                            }
                        })
                    }
                }
                ControllerViewModel.ConnectionState.Disconnected -> {
                    debug("Quitting controller fragment since we are not connected")
                    showConnectionErrorAlert()
                    findNavController().navigateUp()
                }
                else -> {
                    warn("Unknown connection state: $state")
                }
            }
        }


        return binding.root
    }

    private fun showConnectionErrorAlert() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.connection_error_dialog_title)
            .setMessage(getString(R.string.connection_error_dialog_message,
                viewModel.serverAddress, viewModel.serverPort))
            .setPositiveButton(R.string.ok, null)
            .show()
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
        debug("onTextChanged: (str = $s, start = $start, before = $before, count = $count)")

        if (count > before) {
            // insertion
            val c = s[start + before]
            debug("+ $c")
            viewModel.write(c)
        } else if (before > count) {
            // deletion
            debug("<-")
            viewModel.keyClick(KeyEvent.KEYCODE_DEL)
        }
    }

    override fun onKeyboardKey(keyCode: Int, event: KeyEvent) {
        debug("onKey: $keyCode")

        when (event.action) {
            KeyEvent.ACTION_DOWN -> { viewModel.keyDown(keyCode)}
            KeyEvent.ACTION_UP -> { viewModel.keyUp(keyCode) }
        }
    }
}