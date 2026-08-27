package com.parion.aidat;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

/** v4.1.18 - production HOME path: keep first-frame reveal, remove diagnostic Frame/Toast profiler overhead. */
public class MainActivityV702 extends MainActivityV701 {
    private static final String TAG702 = "ParionHomePerf";
    private long activityStart702;

    @Override public void onCreate(Bundle b) {
        activityStart702 = SystemClock.elapsedRealtime();
        super.onCreate(b);
        Log.i(TAG702, "onCreate total=" + (SystemClock.elapsedRealtime()-activityStart702) + "ms");
    }

    @Override void showHome() {
        super.showHome();
    }
}
