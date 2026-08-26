package com.parion.aidat;

import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class MainActivityV703 extends MainActivityV702 {
    private final Handler homeLayoutHandler703 = new Handler(Looper.getMainLooper());
    private int generation703 = 0;

    @Override void showHome() {
        final boolean returningToHome = page != null && !"HOME".equalsIgnoreCase(page);
        final int generation = ++generation703;
        super.showHome();

        if (!returningToHome || !(root instanceof ViewGroup)) return;
        final ViewGroup homeRoot = (ViewGroup) root;
        final ViewTreeObserver vto = homeRoot.getViewTreeObserver();
        if (!vto.isAlive()) return;

        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            boolean done = false;
            @Override public boolean onPreDraw() {
                if (done) return true;
                done = true;
                ViewTreeObserver obs = homeRoot.getViewTreeObserver();
                if (obs.isAlive()) obs.removeOnPreDrawListener(this);
                if (generation != generation703 || root != homeRoot || !"HOME".equalsIgnoreCase(page)) return true;

                homeRoot.suppressLayout(true);
                homeLayoutHandler703.postDelayed(() -> {
                    if (generation != generation703 || root != homeRoot) return;
                    homeRoot.suppressLayout(false);
                    homeRoot.requestLayout();
                    homeRoot.invalidate();
                }, 260L);
                return true;
            }
        });
    }

    @Override protected void onDestroy() {
        generation703++;
        homeLayoutHandler703.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
