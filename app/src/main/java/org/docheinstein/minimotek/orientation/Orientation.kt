package org.docheinstein.minimotek.orientation

/** Phone orientation */
enum class Orientation {
    Portrait,
    Landscape;

    operator fun not(): Orientation {
        return when (this) {
            Portrait -> Landscape
            Landscape -> Portrait
        }
    }
}