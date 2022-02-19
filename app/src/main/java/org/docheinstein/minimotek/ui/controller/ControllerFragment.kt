package org.docheinstein.minimotek.ui.controller

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.ControllerBinding
import org.docheinstein.minimotek.ui.controller.touchpad.TouchpadPointerView
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.warn


@AndroidEntryPoint
class ControllerFragment : Fragment(), TouchpadPointerView.TouchpadListener {

    private val viewModel: ControllerViewModel by viewModels()
    private lateinit var binding: ControllerBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val address = ControllerFragmentArgs.fromBundle(requireArguments()).address
        val port = ControllerFragmentArgs.fromBundle(requireArguments()).port
        debug("ControllerFragment.onCreateView() for server = $address:$port")

        binding = ControllerBinding.inflate(inflater, container, false)
        binding.touchpadPointer.listener = this

        binding.touchpadLeftButton.setOnClickListener {
            viewModel.leftClick()
        }
        binding.touchpadRightButton.setOnClickListener {
            viewModel.rightClick()
        }

        binding.splash.setOnTouchListener @SuppressLint("ClickableViewAccessibility") { _, _ ->
            // prevent propagation of touch events
            true
        }

        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ControllerViewModel.ConnectionState.Connecting -> {
                    // show ovelray
                    binding.overlay.isVisible = true
                    binding.overlay.alpha = 1.0f

                }
                ControllerViewModel.ConnectionState.Connected -> {
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

    override fun onTouchpadDown(ev: MotionEvent) = viewModel.touchpadDown(ev)
    override fun onTouchpadUp(ev: MotionEvent) = viewModel.touchpadUp(ev)
    override fun onTouchpadPointerDown(ev: MotionEvent) = viewModel.touchpadPointerDown(ev)
    override fun onTouchpadPointerUp(ev: MotionEvent) = viewModel.touchpadPointerUp(ev)
    override fun onTouchpadMovement(ev: MotionEvent) = viewModel.touchpadMovement(ev)


}