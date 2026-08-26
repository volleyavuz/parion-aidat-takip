package com.parion.aidat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/** v4.1.06 HOME timing + callback profiler. No data mutation. */
public class MainActivityV702 extends MainActivityV701 {
    private static final String TAG702 = "ParionHomePerf";
    private final Handler perfHandler702 = new Handler(Looper.getMainLooper());
    private long activityStart702;
    private int homeCall702 = 0;

    @Override public void onCreate(Bundle b) {
        activityStart702 = SystemClock.elapsedRealtime();
        super.onCreate(b);
        long createMs = SystemClock.elapsedRealtime() - activityStart702;
        Log.i(TAG702, "onCreate total=" + createMs + "ms");
    }

    @Override void showHome() {
        final int call = ++homeCall702;
        final long t0 = SystemClock.elapsedRealtime();
        super.showHome();
        final long syncMs = SystemClock.elapsedRealtime() - t0;
        if (syncMs < 80L) return;
        final long queuedAt = SystemClock.elapsedRealtime();
        perfHandler702.post(() -> {
            long uiLagMs = SystemClock.elapsedRealtime() - queuedAt;
            String msg = "HOME " + syncMs + " ms • UI bekleme " + uiLagMs + " ms";
            Log.i(TAG702, "call=" + call + " sync=" + syncMs + "ms uiLag=" + uiLagMs + "ms");
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }
}
