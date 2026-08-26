package com.parion.aidat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

/** v4.1.05 HOME performance layer. */
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

    /**
     * HOME-only scheduler. V691 and V692 are superseded by V694 but historically leave four
     * delayed full-tree rebuilds behind. Drop only those exact obsolete waits. V694's final
     * deterministic season-card pass is kept once; its duplicate retry is dropped.
     */
    static final class FastHomeRoot700 extends LinearLayout {
        private long slot700=0L;
        FastHomeRoot700(Context c){super(c);}
        @Override public boolean postDelayed(Runnable action,long delayMillis){
            if(action==null)return false;
            if(delayMillis==4550L||delayMillis==5200L||delayMillis==5700L||delayMillis==6400L||delayMillis==7700L){
                return true;
            }
            if(delayMillis<=120L)return super.postDelayed(action,delayMillis);
            long compact=72L+Math.min(220L,slot700*8L); slot700++;
            return super.postDelayed(action,compact);
        }
    }
}
