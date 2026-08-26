package com.parion.aidat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;
import android.widget.Toast;

/** v4.1.11 HOME timing + callback + frame profiler. No data mutation. */
public class MainActivityV702 extends MainActivityV701 {
    private static final String TAG702 = "ParionHomePerf";
    private final Handler perfHandler702 = new Handler(Looper.getMainLooper());
    private long activityStart702;
    private int homeCall702 = 0;

    @Override public void onCreate(Bundle b) {
        activityStart702 = SystemClock.elapsedRealtime();
        super.onCreate(b);
        Log.i(TAG702, "onCreate total=" + (SystemClock.elapsedRealtime()-activityStart702) + "ms");
    }

    @Override void showHome() {
        final int call = ++homeCall702;
        resetCallbackStats700();
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

        final long frameProbeStart = SystemClock.elapsedRealtime();
        Choreographer.getInstance().postFrameCallback(frameTimeNanos -> {
            long firstFrameMs = SystemClock.elapsedRealtime() - frameProbeStart;
            final long secondStart = SystemClock.elapsedRealtime();
            Choreographer.getInstance().postFrameCallback(frame2 -> {
                long nextFrameMs = SystemClock.elapsedRealtime() - secondStart;
                String fm = "FRAME ilk " + firstFrameMs + " ms • sonraki " + nextFrameMs + " ms";
                Log.i(TAG702, "call=" + call + " firstFrame=" + firstFrameMs + "ms nextFrame=" + nextFrameMs + "ms");
                Toast.makeText(this, fm, Toast.LENGTH_LONG).show();
            });
        });

        perfHandler702.postDelayed(() -> {
            int count=callbackRunCount700;
            long total=totalCallbackCost700;
            long max=maxCallbackCost700;
            long req=maxCallbackRequested700;
            int seq=maxCallbackSeq700;
            String cb = "CB " + count + " adet • toplam " + total + " ms • max " + max + " ms";
            Log.i(TAG702, cb + " • maxReq=" + req + "ms • #" + seq + " • queueToplam=" + totalCallbackQueue700 + "ms");
            Toast.makeText(this, cb, Toast.LENGTH_LONG).show();
        }, 2600L);
    }
}
