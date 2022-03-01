package org.docheinstein.minimotek

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application */
/* Required for Hilt to work (https://developer.android.com/training/dependency-injection/hilt-android) */
@HiltAndroidApp
class MinimoteApp : Application()