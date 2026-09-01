package com.parion.aidat;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/** v4.3.6 - keep cloud athlete photo as the final profile render state. */
public class MainActivityV754 extends MainActivityV753 {
    private long pinnedProfile754 = -1L;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
    }

    @Override void showProfile(long id) {
        pinnedProfile754 = id;
        super.showProfile(id);
        // Several inherited profile/media patches are posted asynchronously. Re-apply the
        // canonical cloud photo after each legacy patch window so no placeholder can win.
        pinProfilePhoto754(id, 0L);
        pinProfilePhoto754(id, 180L);
        pinProfilePhoto754(id, 650L);
        pinProfilePhoto754(id, 1500L);
        pinProfilePhoto754(id, 3200L);
    }

    private void pinProfilePhoto754(long id, long delay) {
        if (root == null) return;
        root.postDelayed(() -> {
            if (id <= 0 || pinnedProfile754 != id || currentAthlete != id || !"PROFILE".equals(page) || root == null) return;
            String path = photoMap413().get(id);
            if (path == null || path.trim().isEmpty()) return;
            ImageView target = findProfilePhoto754(root);
            if (target == null) return;
            setAthletePhoto(target, "CLOUD:" + path.trim());
        }, delay);
    }

    private ImageView findProfilePhoto754(View v) {
        if (v instanceof ImageView) {
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp != null) {
                int w = lp.width, h = lp.height;
                // V31 profile portrait is 150x180dp. Allow later visual patches some tolerance.
                if (w >= dp(120) && w <= dp(210) && h >= dp(130) && h <= dp(230)) return (ImageView) v;
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                ImageView x = findProfilePhoto754(g.getChildAt(i));
                if (x != null) return x;
            }
        }
        return null;
    }

    @Override void showHome() { pinnedProfile754 = -1L; super.showHome(); }
    @Override void showAthletes() { pinnedProfile754 = -1L; super.showAthletes(); }
}
