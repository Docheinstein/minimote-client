package org.docheinstein.minimotek.database.hotkey

import org.docheinstein.minimotek.keys.MinimoteKeyType

data class Hotkey(
    var key: MinimoteKeyType,
    var shift: Boolean,
    var ctrl: Boolean,
    var alt: Boolean,
    var altgr: Boolean,
    var meta: Boolean
)