package org.docheinstein.minimote.orientation

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