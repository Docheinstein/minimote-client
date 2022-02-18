package org.docheinstein.minimotek.ui.controller

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.databinding.ControllerBinding
import org.docheinstein.minimotek.ui.controller.touchpad.TouchpadPointerView
import org.docheinstein.minimotek.util.debug


@AndroidEntryPoint
class ControllerFragment : Fragment(), TouchpadPointerView.TouchpadListener {

    private val viewModel: ControllerViewModel by viewModels()
    private lateinit var binding: ControllerBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val address = ControllerFragmentArgs.fromBundle(requireArguments()).address
        val port = ControllerFragmentArgs.fromBundle(requireArguments()).port
        debug("ControllerFragment.onCreateView() for serve = $address:$port")

        binding = ControllerBinding.inflate(inflater, container, false)
        binding.touchpadPointer.listener = this

        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            when(state) {
                ControllerViewModel.ConnectionState.Connected -> {

                }
                ControllerViewModel.ConnectionState.Disconnected -> {
                    debug("Quitting controller fragment since we are not connected")
                    showConnectionErrorAlert()
                    findNavController().navigateUp()
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

    override fun onTouchpadDown(ev: MotionEvent) {

    }

    override fun onTouchpadUp(ev: MotionEvent) {
        viewModel.touchpadUp(ev)
    }

    override fun onTouchpadPointerDown(ev: MotionEvent) {

    }

    override fun onTouchpadPointerUp(ev: MotionEvent) {
        viewModel.touchpadPointerUp(ev)
    }

    override fun onTouchpadMovement(ev: MotionEvent) {
        viewModel.touchpadMovement(ev)
    }


}