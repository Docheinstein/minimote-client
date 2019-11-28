package org.docheinstein.minimote.commons;

public class Conf {
    public static class Connection {
        public static int DEFAULT_PORT = 50500;
    }

    public static class Discovery {
        public static int TIMEOUT = 8000;
    }

    public static class Controller {
        public static final int MAX_MOVEMENT_SAMPLING_RATE = 60; // Hz
        public static final int MIN_MS_BETWEEN_MOVEMENT_SAMPLES = 1000 / MAX_MOVEMENT_SAMPLING_RATE; // ms

        // Scroll
        public static final int MAX_SCROLL_SAMPLING_RATE = 30; // Hz
        public static final int MIN_MS_BETWEEN_SCROLL_SAMPLES = 1000 / MAX_SCROLL_SAMPLING_RATE; // ms
        public static final int MIN_SCROLL_POS_DELTA_THRESHOLD = 25; // points

        // Click
        public static final int MAX_TOUCHPAD_CLICK_MS_THRESHOLD = 250; // ms
        public static final int MAX_TOUCHPAD_CLICK_POS_THRESHOLD = 25; // points
    }
}
