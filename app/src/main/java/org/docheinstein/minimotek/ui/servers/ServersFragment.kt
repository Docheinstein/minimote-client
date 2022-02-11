package org.docheinstein.minimotek.ui.servers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.docheinstein.minimotek.R
import org.docheinstein.minimotek.util.debug

class ServersFragment : Fragment() {

    private val viewModel: ServersViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        debug("ServersFragment.onCreateView")
        val root = inflater.inflate(R.layout.servers, container, false)
        return root
    }
}