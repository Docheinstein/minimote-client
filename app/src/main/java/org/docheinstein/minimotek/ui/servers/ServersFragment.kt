package org.docheinstein.minimotek.ui.servers

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.util.NetUtils
import org.docheinstein.minimotek.util.error
import org.docheinstein.minimotek.util.debug
import org.docheinstein.minimotek.util.info
import java.lang.NumberFormatException

class ServersFragment : Fragment() {

    private val viewModel: ServersViewModel by viewModels()

    // Add server dialog

    class AddServerDialog : DialogFragment() {
        companion object {
            const val FRAGMENT_TAG = "add_server_fragment"
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val dialogView = layoutInflater.inflate(R.layout.add_server_dialog, null)

                val uiAddress: EditText = dialogView.findViewById(R.id.uiAddress)
                val uiPort: EditText = dialogView.findViewById(R.id.uiPort)

                return AlertDialog.Builder(it)
                    .setView(dialogView)
                    .setTitle(R.string.add_server_dialog_title)
                    .setPositiveButton(R.string.add) onClick@{ _, _ ->
                        // Address validation
                        val serverAddress = uiAddress.text.toString()

                        if (!NetUtils.isValidIPv4(serverAddress)) {
                            error("Invalid IPv4: $serverAddress")
                            showInvalidAddressAlert()
                            return@onClick
                        }

                        // Port validation
                        val serverPort: Int

                        try {
                            serverPort = uiPort.text.toString().toInt()
                        } catch (e: NumberFormatException) {
                            error("Invalid port: ${uiPort.text}")
                            showInvalidPortAlert()
                            return@onClick
                        }

                        if (!NetUtils.isValidPort(serverPort)) {
                            error("Invalid port: $serverPort")
                            showInvalidPortAlert()
                            return@onClick
                        }

                        // Actually add the server
                        info("Adding server: $serverAddress:$serverPort")
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        debug("Aborting server addition")
                    }
                    .create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }

        private fun showInvalidAddressAlert() {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.add_server_failed_address_dialog_title)
                .setMessage(R.string.add_server_failed_address_dialog_message)
                .setPositiveButton(R.string.ok, null)
                .show();
        }

        private fun showInvalidPortAlert() {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.add_server_failed_port_dialog_title)
                .setMessage(R.string.add_server_failed_port_dialog_message)
                .setPositiveButton(R.string.ok, null)
                .show();
        }
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        debug("ServersFragment.onCreateView()")
        val root = inflater.inflate(R.layout.servers, container, false)
        root.findViewById<FloatingActionButton>(R.id.uiAddServerButton).setOnClickListener {
            debug("uiAddServerButton.onClick()")
            handleAddServerButtonClick();

        }
        root.findViewById<FloatingActionButton>(R.id.uiDiscoverServersButton).setOnClickListener {
            debug("uiDiscoverServersButton.onClick()")
            handleDiscoverServersButtonClick()

        }
        return root
    }

    private fun handleAddServerButtonClick() {
        val addServerDialog = AddServerDialog()
        addServerDialog.show(requireActivity().supportFragmentManager, AddServerDialog.FRAGMENT_TAG)
    }

    private fun handleDiscoverServersButtonClick() {

    }
}