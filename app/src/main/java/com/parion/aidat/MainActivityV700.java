package com.parion.aidat;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** v4.1.14 HOME callback batch + single setContentView HOME root. */
public class MainActivityV700 extends MainActivityV699 {
    static volatile long maxCallbackCost700=0L;
    static volatile long maxCallbackRequested700=0L;
    static volatile int maxCallbackSeq700=0;
    static volatile long totalCallbackCost700=0L;
    static volatile long totalCallbackQueue700=0L;
    static volatile int callbackRunCount700=0;

    static synchronized void resetCallbackStats700(){
        maxCallbackCost700=0L;
        maxCallbackRequested700=0L;
        maxCallbackSeq700=0;
        totalCallbackCost700=0L;
        totalCallbackQueue700=0L;
        callbackRunCount700=0;
    }

    static synchronized void recordCallback700(long cost,long queue,long requested,int seq){
        callbackRunCount700++;
        totalCallbackCost700+=cost;
        totalCallbackQueue700+=Math.max(0L,queue);
        if(cost>maxCallbackCost700){
            maxCallbackCost700=cost;
            maxCallbackRequested700=requested;
            maxCallbackSeq700=seq;
        }
    }

    @Override void base(String title, boolean back) {
        if (page == null || !"HOME".equalsIgnoreCase(page)) {
            super.base(title, back);
            return;
        }

        FastHomeRoot700 fast = new FastHomeRoot700(this);
        fast.setOrientation(LinearLayout.VERTICAL);
        fast.setBackgroundColor(BG);
        root = fast;
        setContentView(root);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(10),dp(8),dp(10),dp(8));
        bar.setBackgroundColor(BLACK);
        if(back){
            TextView b=tv("‹",36,Color.WHITE,true);
            b.setOnClickListener(v->goBack());
            bar.addView(b,new LinearLayout.LayoutParams(dp(52),dp(52)));
        }
        TextView t=tv(title,19,Color.rgb(212,175,55),true);
        bar.addView(t,new LinearLayout.LayoutParams(0,dp(56),1));
        root.addView(bar);
    }

    static final class FastHomeRoot700 extends LinearLayout {
        private static final String TAG="ParionHomeCallback";
        private int seq700=0;
        FastHomeRoot700(Context c){super(c);}

        @Override public boolean postDelayed(Runnable action,long delayMillis){
            if(action==null)return false;
            final int seq=++seq700;
            if(delayMillis==4550L||delayMillis==5200L||delayMillis==5700L||delayMillis==6400L||delayMillis==7700L){
                Log.i(TAG,"DROP #"+seq+" requested="+delayMillis+"ms class="+action.getClass().getName());
                return true;
            }
            final long requested=delayMillis;
            // Keep genuinely immediate UI posts unchanged. All inherited delayed HOME
            // cleanup callbacks are intentionally released in one queue window. Their
            // insertion order is preserved, but Android can traverse/layout once after
            // the batch instead of being forced across many 8 ms-spaced frames.
            final long actual = delayMillis<=120L ? delayMillis : 72L;
            final long scheduled=SystemClock.elapsedRealtime();
            Runnable measured=()->{
                long started=SystemClock.elapsedRealtime();
                try { action.run(); }
                finally {
                    long cost=SystemClock.elapsedRealtime()-started;
                    long queue=started-scheduled;
                    recordCallback700(cost,queue,requested,seq);
                    Log.i(TAG,"RUN #"+seq+" req="+requested+"ms actual="+actual+"ms queue="+queue+"ms cost="+cost+"ms class="+action.getClass().getName());
                }
            };
            return super.postDelayed(measured,actual);
        }
    }
}
