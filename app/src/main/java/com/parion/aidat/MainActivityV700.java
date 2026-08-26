package com.parion.aidat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

/**
 * v4.1.00 - HOME performance layer.
 *
 * Historical dashboard fixes intentionally remain in the inheritance chain so the
 * v4.0.99 visual/functional result is preserved.  Their multi-second View.postDelayed
 * callbacks are compressed only while HOME is being built.  Callback order is kept,
 * but the whole stabilization window is reduced to a few hundred milliseconds.
 */
public class MainActivityV700 extends MainActivityV699 {

    @Override void base(String title, boolean back) {
        super.base(title, back);
        if (root == null || page == null || !"HOME".equalsIgnoreCase(page) || root instanceof FastHomeRoot700) return;

        LinearLayout old = root;
        FastHomeRoot700 fast = new FastHomeRoot700(this);
        fast.setOrientation(old.getOrientation());
        fast.setGravity(old.getGravity());
        fast.setPadding(old.getPaddingLeft(), old.getPaddingTop(), old.getPaddingRight(), old.getPaddingBottom());
        fast.setFitsSystemWindows(old.getFitsSystemWindows());
        fast.setTag(old.getTag());
        fast.setId(old.getId());
        Drawable bg = old.getBackground();
        if (bg != null) fast.setBackground(bg);

        while (old.getChildCount() > 0) {
            View child = old.getChildAt(0);
            old.removeViewAt(0);
            fast.addView(child);
        }

        root = fast;
        setContentView(fast);
    }

    /** HOME-only scheduler: preserve callback order, collapse legacy waits. */
    static final class FastHomeRoot700 extends LinearLayout {
        private long slot700 = 0L;
        FastHomeRoot700(Context c) { super(c); }

        @Override public boolean postDelayed(Runnable action, long delayMillis) {
            if (action == null) return false;
            if (delayMillis <= 120L) return super.postDelayed(action, delayMillis);

            // Give the first layout frame time to complete, then keep inherited patch order.
            long compact = 72L + Math.min(300L, slot700 * 10L);
            slot700++;
            return super.postDelayed(action, compact);
        }
    }
}
