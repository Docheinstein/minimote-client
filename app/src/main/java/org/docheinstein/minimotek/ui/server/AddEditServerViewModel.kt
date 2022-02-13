package org.docheinstein.minimotek.ui.server

import androidx.lifecycle.ViewModel
import org.docheinstein.minimotek.util.debug

class AddEditServerViewModel : ViewModel() {
    enum class Purpose {
        ADD,
        EDIT
    }

    var purpose: Purpose? = null

    init {
        debug("AddEditServerViewModel.init()")
    }

}