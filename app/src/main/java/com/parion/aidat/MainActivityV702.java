package com.parion.aidat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * v4.1.04 diagnostic only.
 * Keeps v4.1.01/v4.1.03 behaviour unchanged and measures the real synchronous HOME build.
 * No view caching, no navigation changes, no database mutation.
 */
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
        getSharedPreferences("parion_perf_702", MODE_PRIVATE).edit()
                .putLong("last_oncreate_ms", createMs).apply();
    }

    @Override void showHome() {
        final int call = ++homeCall702;
        final long t0 = SystemClock.elapsedRealtime();
        super.showHome();
        final long syncMs = SystemClock.elapsedRealtime() - t0;

        // Ancestor startup layers can request lightweight HOME calls. Only report a real/expensive render.
        if (syncMs < 80L) return;

        final long queuedAt = SystemClock.elapsedRealtime();
        perfHandler702.post(() -> {
            long uiLagMs = SystemClock.elapsedRealtime() - queuedAt;
            long sinceActivityStart = activityStart702 > 0
                    ? SystemClock.elapsedRealtime() - activityStart702 : -1L;
            String msg = "HOME " + syncMs + " ms • UI bekleme " + uiLagMs + " ms";
            Log.i(TAG702, "call=" + call + " sync=" + syncMs + "ms uiLag=" + uiLagMs
                    + "ms sinceActivityStart=" + sinceActivityStart + "ms");
            getSharedPreferences("parion_perf_702", MODE_PRIVATE).edit()
                    .putLong("last_home_sync_ms", syncMs)
                    .putLong("last_home_ui_lag_ms", uiLagMs)
                    .putLong("last_home_since_start_ms", sinceActivityStart)
                    .putInt("last_home_call", call)
                    .apply();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }
}
