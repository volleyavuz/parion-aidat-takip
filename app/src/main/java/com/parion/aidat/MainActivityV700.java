package com.parion.aidat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

/** v4.1.06 HOME performance/callback diagnostic layer. */
public class MainActivityV700 extends MainActivityV699 {
    @Override void base(String title, boolean back) {
        super.base(title, back);
        if (root == null || page == null || !"HOME".equalsIgnoreCase(page) || root instanceof FastHomeRoot700) return;
        LinearLayout old = root;
        FastHomeRoot700 fast = new FastHomeRoot700(this);
        fast.setOrientation(old.getOrientation()); fast.setGravity(old.getGravity());
        fast.setPadding(old.getPaddingLeft(), old.getPaddingTop(), old.getPaddingRight(), old.getPaddingBottom());
        fast.setFitsSystemWindows(old.getFitsSystemWindows()); fast.setTag(old.getTag()); fast.setId(old.getId());
        Drawable bg = old.getBackground(); if (bg != null) fast.setBackground(bg);
        while (old.getChildCount() > 0) { View child=old.getChildAt(0); old.removeViewAt(0); fast.addView(child); }
        root=fast; setContentView(fast);
    }

    static final class FastHomeRoot700 extends LinearLayout {
        private static final String TAG="ParionHomeCallback";
        private long slot700=0L;
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
            long actual;
            if(delayMillis<=120L) actual=delayMillis;
            else { actual=72L+Math.min(220L,slot700*8L); slot700++; }
            final long scheduled=SystemClock.elapsedRealtime();
            Runnable measured=()->{
                long started=SystemClock.elapsedRealtime();
                try { action.run(); }
                finally {
                    long cost=SystemClock.elapsedRealtime()-started;
                    long queue=started-scheduled;
                    Log.i(TAG,"RUN #"+seq+" req="+requested+"ms actual="+actual+"ms queue="+queue+"ms cost="+cost+"ms class="+action.getClass().getName());
                }
            };
            return super.postDelayed(measured,actual);
        }
    }
}
