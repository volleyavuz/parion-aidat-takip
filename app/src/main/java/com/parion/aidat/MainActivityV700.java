package com.parion.aidat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** v4.1.15 HOME callback aggregate + root phase profiler. */
public class MainActivityV700 extends MainActivityV699 {
    static volatile long maxCallbackCost700=0L;
    static volatile long maxCallbackRequested700=0L;
    static volatile int maxCallbackSeq700=0;
    static volatile long totalCallbackCost700=0L;
    static volatile long totalCallbackQueue700=0L;
    static volatile int callbackRunCount700=0;

    static synchronized void resetCallbackStats700(){maxCallbackCost700=0L;maxCallbackRequested700=0L;maxCallbackSeq700=0;totalCallbackCost700=0L;totalCallbackQueue700=0L;callbackRunCount700=0;}
    static synchronized void recordCallback700(long cost,long queue,long requested,int seq){callbackRunCount700++;totalCallbackCost700+=cost;totalCallbackQueue700+=Math.max(0L,queue);if(cost>maxCallbackCost700){maxCallbackCost700=cost;maxCallbackRequested700=requested;maxCallbackSeq700=seq;}}

    @Override void base(String title, boolean back) {
        if (page == null || !"HOME".equalsIgnoreCase(page)) {super.base(title, back);return;}
        FastHomeRoot700 fast = new FastHomeRoot700(this);
        fast.setOrientation(LinearLayout.VERTICAL);fast.setBackgroundColor(BG);root = fast;setContentView(root);
        LinearLayout bar = new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(10),dp(8),dp(10),dp(8));bar.setBackgroundColor(BLACK);
        if(back){TextView b=tv("‹",36,Color.WHITE,true);b.setOnClickListener(v->goBack());bar.addView(b,new LinearLayout.LayoutParams(dp(52),dp(52)));}
        TextView t=tv(title,19,Color.rgb(212,175,55),true);bar.addView(t,new LinearLayout.LayoutParams(0,dp(56),1));root.addView(bar);
    }

    static final class FastHomeRoot700 extends LinearLayout {
        private static final String TAG="ParionHomeCallback";
        private long slot700=0L;private int seq700=0;
        private long maxMeasure700=0,maxLayout700=0,maxDraw700=0;
        private boolean reportQueued700=false;
        FastHomeRoot700(Context c){super(c);}

        @Override protected void onMeasure(int w,int h){long t=SystemClock.elapsedRealtime();super.onMeasure(w,h);long d=SystemClock.elapsedRealtime()-t;if(d>maxMeasure700)maxMeasure700=d;Log.i(TAG,"ROOT measure="+d+"ms");}
        @Override protected void onLayout(boolean ch,int l,int t,int r,int b){long s=SystemClock.elapsedRealtime();super.onLayout(ch,l,t,r,b);long d=SystemClock.elapsedRealtime()-s;if(d>maxLayout700)maxLayout700=d;Log.i(TAG,"ROOT layout="+d+"ms");}
        @Override protected void dispatchDraw(Canvas c){long s=SystemClock.elapsedRealtime();super.dispatchDraw(c);long d=SystemClock.elapsedRealtime()-s;if(d>maxDraw700)maxDraw700=d;Log.i(TAG,"ROOT draw="+d+"ms");queueReport700();}
        private void queueReport700(){if(reportQueued700)return;reportQueued700=true;super.postDelayed(()->{String m="ROOT ölçüm "+maxMeasure700+" ms • yerleşim "+maxLayout700+" ms • çizim "+maxDraw700+" ms";Log.i(TAG,m);Toast.makeText(getContext(),m,Toast.LENGTH_LONG).show();},1800L);}

        @Override public boolean postDelayed(Runnable action,long delayMillis){
            if(action==null)return false;final int seq=++seq700;
            if(delayMillis==4550L||delayMillis==5200L||delayMillis==5700L||delayMillis==6400L||delayMillis==7700L){Log.i(TAG,"DROP #"+seq+" requested="+delayMillis+"ms class="+action.getClass().getName());return true;}
            final long requested=delayMillis;final long actual;if(delayMillis<=120L)actual=delayMillis;else{actual=72L+Math.min(220L,slot700*8L);slot700++;}
            final long scheduled=SystemClock.elapsedRealtime();Runnable measured=()->{long started=SystemClock.elapsedRealtime();try{action.run();}finally{long cost=SystemClock.elapsedRealtime()-started;long queue=started-scheduled;recordCallback700(cost,queue,requested,seq);Log.i(TAG,"RUN #"+seq+" req="+requested+"ms actual="+actual+"ms queue="+queue+"ms cost="+cost+"ms class="+action.getClass().getName());}};return super.postDelayed(measured,actual);
        }
    }
}
