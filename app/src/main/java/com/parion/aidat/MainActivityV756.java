package com.parion.aidat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * v4.3.8
 * Profile portrait bypasses photoAlias/setAthletePhoto placeholder flow entirely.
 * Exact athlete-id -> canonical photo_path is decoded once to Bitmap and then pinned directly.
 */
public class MainActivityV756 extends MainActivityV755 {
    private final ExecutorService directPhoto756 = Executors.newSingleThreadExecutor();
    private final Handler ui756 = new Handler(Looper.getMainLooper());
    private final ConcurrentHashMap<Long, Bitmap> bitmap756 = new ConcurrentHashMap<>();
    private long active756 = -1L;
    private boolean destroyed756 = false;

    private final Runnable pin756 = new Runnable() {
        @Override public void run() {
            if (destroyed756) return;
            long id = active756;
            if (id > 0 && currentAthlete == id && "PROFILE".equals(page) && root != null) {
                Bitmap b = bitmap756.get(id);
                if (b != null && !b.isRecycled()) {
                    ImageView target = findPortrait756(root);
                    if (target != null) {
                        target.setTag("PARION_DIRECT_PHOTO_" + id);
                        target.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        target.setImageBitmap(b);
                    }
                }
                ui756.postDelayed(this, 250L);
            }
        }
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
    }

    @Override void showProfile(long id) {
        active756 = id;
        super.showProfile(id);
        ui756.removeCallbacks(pin756);
        Bitmap cached = bitmap756.get(id);
        if (cached != null && !cached.isRecycled()) {
            apply756(id, cached);
            ui756.post(pin756);
            return;
        }
        String path = photoMap413().get(id);
        if (path == null || path.trim().isEmpty()) return;
        final String exact = path.trim();
        directPhoto756.execute(() -> {
            Bitmap decoded = decodeExact756(exact);
            if (decoded == null || decoded.isRecycled()) return;
            bitmap756.put(id, decoded);
            ui756.post(() -> {
                if (active756 == id && currentAthlete == id && "PROFILE".equals(page)) {
                    apply756(id, decoded);
                    ui756.removeCallbacks(pin756);
                    ui756.post(pin756);
                }
            });
        });
    }

    private Bitmap decodeExact756(String path) {
        try {
            Method m = MainActivityV405.class.getDeclaredMethod("downloadBitmap405", String.class, String.class, int.class);
            m.setAccessible(true);
            Object x = m.invoke(this, "athlete-photos", path, 420);
            return x instanceof Bitmap ? (Bitmap) x : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void apply756(long id, Bitmap b) {
        if (root == null || b == null || b.isRecycled()) return;
        ImageView target = findPortrait756(root);
        if (target == null) return;
        target.setTag("PARION_DIRECT_PHOTO_" + id);
        target.setScaleType(ImageView.ScaleType.CENTER_CROP);
        target.setImageBitmap(b);
    }

    private ImageView findPortrait756(View v) {
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
                ImageView x = findPortrait756(g.getChildAt(i));
                if (x != null) return x;
            }
        }
        return null;
    }

    @Override void showHome() {
        active756 = -1L;
        ui756.removeCallbacks(pin756);
        super.showHome();
    }

    @Override void showAthletes() {
        active756 = -1L;
        ui756.removeCallbacks(pin756);
        super.showAthletes();
    }

    @Override protected void onDestroy() {
        destroyed756 = true;
        ui756.removeCallbacksAndMessages(null);
        directPhoto756.shutdownNow();
        super.onDestroy();
    }
}
