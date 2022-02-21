package org.docheinstein.minimotek

// DB
const val AUTO_ID = 0L

// Discover
const val DISCOVER_PORT = 50500
const val ESTIMATED_DISCOVER_TIME = 8000 // ms

// Click
const val CLICK_AREA = 25 // points

// Movement
const val MOVEMENT_SAMPLE_RATE = 60 // Hz
const val MOVEMENT_MIN_TIME_BETWEEN_SAMPLES = 1000 / MOVEMENT_SAMPLE_RATE // ms
const val MAX_MOVEMENT_ID = 256 // 8 bytes -> 2^8 == 256

// Scroll
const val SCROLL_SAMPLE_RATE = 30 // Hz
const val SCROLL_MIN_TIME_BETWEEN_SAMPLES = 1000 / SCROLL_SAMPLE_RATE // ms
const val SCROLL_DELTA_FOR_TICK = 25 // points

// Connection
const val CONNECTION_KEEP_ALIVE_TIME = 10000