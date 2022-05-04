package org.docheinstein.minimote.database.hotkey

import org.docheinstein.minimote.keys.MinimoteKeyType

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