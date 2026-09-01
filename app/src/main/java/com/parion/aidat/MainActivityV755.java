package com.parion.aidat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * v4.3.7
 * Keep the active profile portrait bound to athlete-id -> canonical cloud path.
 * This intentionally ignores athlete name/text so month-name collisions (e.g. EYLÜL)
 * cannot affect portrait rendering.
 */
public class MainActivityV755 extends MainActivityV754 {
    private final Handler photoGuard755 = new Handler(Looper.getMainLooper());
    private long watchedAthlete755 = -1L;
    private boolean destroyed755 = false;

    private final Runnable guard755 = new Runnable() {
        @Override public void run() {
            if (destroyed755) return;
            long id = watchedAthlete755;
            if (id > 0 && currentAthlete == id && "PROFILE".equals(page) && root != null) {
                String path = photoMap413().get(id);
                if (path != null && !path.trim().isEmpty()) {
                    ImageView target = findProfilePhoto755(root);
                    if (target != null) setAthletePhoto(target, "CLOUD:" + path.trim());
                }
                photoGuard755.postDelayed(this, 350L);
            }
        }
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
    }

    @Override void showProfile(long id) {
        watchedAthlete755 = id;
        super.showProfile(id);
        photoGuard755.removeCallbacks(guard755);
        photoGuard755.post(guard755);
    }

    private ImageView findProfilePhoto755(View v) {
        if (v instanceof ImageView) {
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp != null) {
                int w = lp.width, h = lp.height;
                if (w >= dp(120) && w <= dp(210) && h >= dp(130) && h <= dp(230)) return (ImageView) v;
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                ImageView x = findProfilePhoto755(g.getChildAt(i));
                if (x != null) return x;
            }
        }
        return null;
    }

    @Override void showHome() {
        watchedAthlete755 = -1L;
        photoGuard755.removeCallbacks(guard755);
        super.showHome();
    }

    @Override void showAthletes() {
        watchedAthlete755 = -1L;
        photoGuard755.removeCallbacks(guard755);
        super.showAthletes();
    }

    @Override protected void onDestroy() {
        destroyed755 = true;
        photoGuard755.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
