package com.parion.aidat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Printer;
import android.view.Choreographer;
import android.widget.Toast;

/** v4.1.15 HOME timing + callback + frame + looper + traversal profiler. No data mutation. */
public class MainActivityV702 extends MainActivityV701 {
    private static final String TAG702 = "ParionHomePerf";
    private final Handler perfHandler702 = new Handler(Looper.getMainLooper());
    private long activityStart702;
    private int homeCall702 = 0;
    private int looperGeneration702 = 0;

    private static final class LoopStats702 {
        long started=0L; String label=""; long maxMs=0L; String maxLabel=""; int over16=0; int dispatchCount=0;
    }

    @Override public void onCreate(Bundle b) {
        activityStart702 = SystemClock.elapsedRealtime();
        super.onCreate(b);
        Log.i(TAG702, "onCreate total=" + (SystemClock.elapsedRealtime()-activityStart702) + "ms");
    }

    private void startLooperProbe702(final int call){
        final int generation=++looperGeneration702;
        final LoopStats702 s=new LoopStats702();
        Printer p=x->{
            if(generation!=looperGeneration702 || x==null)return;
            long now=SystemClock.elapsedRealtime();
            if(x.startsWith(">>>>> Dispatching")){s.started=now;s.label=x;s.dispatchCount++;}
            else if(x.startsWith("<<<<< Finished") && s.started>0L){
                long ms=now-s.started;if(ms>16L)s.over16++;if(ms>s.maxMs){s.maxMs=ms;s.maxLabel=s.label;}s.started=0L;
            }
        };
        Looper.getMainLooper().setMessageLogging(p);
        perfHandler702.postDelayed(()->{
            if(generation!=looperGeneration702)return;
            Looper.getMainLooper().setMessageLogging(null);
            String kind=s.maxLabel;
            if(kind.contains("Choreographer"))kind="Choreographer"; else if(kind.contains("ViewRoot"))kind="ViewRoot"; else if(kind.contains("Handler"))kind="Handler"; else if(kind.length()>38)kind=kind.substring(0,38);
            String lm="LOOPER max "+s.maxMs+" ms • >16ms "+s.over16+" • "+kind;
            Log.i(TAG702,"call="+call+" "+lm+" dispatch="+s.dispatchCount+" raw="+s.maxLabel);
            Toast.makeText(this,lm,Toast.LENGTH_LONG).show();
        },2300L);
    }

    @Override void showHome() {
        final int call = ++homeCall702;
        resetCallbackStats700();
        final long t0 = SystemClock.elapsedRealtime();
        super.showHome();
        final long syncMs = SystemClock.elapsedRealtime() - t0;
        if (syncMs < 80L) return;

        startLooperProbe702(call);
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
            if(root instanceof FastHomeRoot700){
                String tr=((FastHomeRoot700)root).traversalStats700();
                Log.i(TAG702,"call="+call+" ROOT "+tr);
                Toast.makeText(this,"ROOT "+tr,Toast.LENGTH_LONG).show();
            }
        },1900L);

        perfHandler702.postDelayed(() -> {
            int count=callbackRunCount700; long total=totalCallbackCost700; long max=maxCallbackCost700; long req=maxCallbackRequested700; int seq=maxCallbackSeq700;
            String cb = "CB " + count + " adet • toplam " + total + " ms • max " + max + " ms";
            Log.i(TAG702, cb + " • maxReq=" + req + "ms • #" + seq + " • queueToplam=" + totalCallbackQueue700 + "ms");
            Toast.makeText(this, cb, Toast.LENGTH_LONG).show();
        }, 2800L);
    }
}
