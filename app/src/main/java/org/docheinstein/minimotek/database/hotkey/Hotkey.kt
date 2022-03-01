package org.docheinstein.minimotek.database.hotkey

import org.docheinstein.minimotek.keys.MinimoteKeyType

/**
 * Represents a combination of a [MinimoteKeyType] and eventually some modifiers.
 * e.g. Alt+Tab, Ctrl+C.
 */
data class Hotkey(
    var key: MinimoteKeyType,
    var shift: Boolean,
    var ctrl: Boolean,
    var alt: Boolean,
    var altgr: Boolean,
    var meta: Boolean
)