package org.docheinstein.minimotek.ui.controller

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.ControllerBinding
import org.docheinstein.minimotek.keys.MinimoteKeyType
import org.docheinstein.minimotek.ui.controller.touchpad.TouchpadPointerView
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn


@AndroidEntryPoint
class ControllerFragment : Fragment(), TouchpadPointerView.TouchpadListener, TextWatcher,
    View.OnKeyListener {

    private val viewModel: ControllerViewModel by viewModels()
    private lateinit var binding: ControllerBinding


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val address = ControllerFragmentArgs.fromBundle(requireArguments()).address
        val port = ControllerFragmentArgs.fromBundle(requireArguments()).port
        debug("ControllerFragment.onCreateView() for server = $address:$port")

        binding = ControllerBinding.inflate(inflater, container, false)

        // touchpad

        binding.touchpadPointer.listener = this

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

        // overlay

        binding.splash.setOnTouchListener { _, _ ->
            // prevent propagation of touch events if splash screen is atop
            true
        }

        // keyboard

        binding.keyboardButton.setOnClickListener {
            toggleSoftKeyboard()
        }

        // text watcher: for soft keyboard chars and backspace
        binding.keyboardTextPreview.addTextChangedListener(this)

        // key listener: for physical keyboards and special chars on soft keyboards
        binding.keyboardTextPreview.setOnKeyListener(this)

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
            .show();
    }

    private fun toggleSoftKeyboard() {
        debug("Toggling keyboard")
        if (binding.keyboardTextPreview.isVisible)
            hideKeyboard()
        else
            showKeyboard()
    }

    private fun hideKeyboard() {
        debug("Hiding keyboard")
            binding.keyboardTextPreview.isVisible = false

        if (requireActivity().currentFocus == null) {
            warn("No focused view?")
        }

        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.keyboardTextPreview.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun showKeyboard() {
        debug("Showing keyboard")
        binding.keyboardTextPreview.isVisible = true
        if (!binding.keyboardTextPreview.requestFocus()) {
            warn("Failed to acquire focus for keyboard text preview")
            return
        }

        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.keyboardTextPreview, InputMethodManager.SHOW_IMPLICIT)
    }

    // Touchpad events
    override fun onTouchpadDown(ev: MotionEvent) = viewModel.touchpadDown(ev)
    override fun onTouchpadUp(ev: MotionEvent) = viewModel.touchpadUp(ev)
    override fun onTouchpadPointerDown(ev: MotionEvent) = viewModel.touchpadPointerDown(ev)
    override fun onTouchpadPointerUp(ev: MotionEvent) = viewModel.touchpadPointerUp(ev)
    override fun onTouchpadMovement(ev: MotionEvent) = viewModel.touchpadMovement(ev)

    // Keyboard events
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable?) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s == null)
            return

        debug("onTextChanged: (str: $s, start: $start, before: $before, count: $count)")

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

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        debug("onKey: $keyCode")
        if (event == null)
            return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> { viewModel.keyDown(keyCode)}
            KeyEvent.ACTION_UP -> { viewModel.keyUp(keyCode) }
        }
        return true
    }
}