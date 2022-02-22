package org.docheinstein.minimotek.ui.hotkeys

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.docheinstein.minimotek.databinding.HotkeysBinding

@AndroidEntryPoint
class HotkeysFragment : Fragment() {
    private val viewModel: HotkeysViewModel by viewModels()
    private lateinit var binding: HotkeysBinding
}