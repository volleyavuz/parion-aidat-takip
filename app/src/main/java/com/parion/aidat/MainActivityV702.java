package com.parion.aidat;

import android.os.SystemClock;
import android.view.ViewGroup;

/**
 * v4.1.02 - fast HOME return cache.
 *
 * The first HOME build still uses the complete v4.1.01 chain so the visual result and
 * dashboard behavior are preserved. For a short navigation window, returning to HOME
 * re-attaches the already-built final root instead of replaying the legacy HOME chain.
 * This removes the visible intermediate/loading HOME on quick in-app navigation.
 *
 * Cache lifetime is intentionally short (20s): after edits/long navigation the HOME is
 * rebuilt from source so dashboard values do not remain stale.
 */
public class MainActivityV702 extends MainActivityV701 {
    private ViewGroup cachedHome702;
    private long cachedAt702 = 0L;
    private boolean buildingHome702 = false;
    private static final long HOME_CACHE_MS_702 = 20000L;

    @Override void showHome() {
        long now = SystemClock.elapsedRealtime();
        if (!buildingHome702 && cachedHome702 != null && now - cachedAt702 <= HOME_CACHE_MS_702) {
            page = "HOME";
            currentAthlete = -1;
            root = (android.widget.LinearLayout) cachedHome702;
            setContentView(root);
            return;
        }

        buildingHome702 = true;
        try {
            super.showHome();
        } finally {
            buildingHome702 = false;
        }

        if (root != null) {
            cachedHome702 = root;
            cachedAt702 = SystemClock.elapsedRealtime();
            // Legacy HOME patches are compressed by v4.1.00 and finish in a few hundred ms.
            // Refresh the cache timestamp after they settle, without rebuilding HOME.
            root.postDelayed(() -> {
                if (root != null && "HOME".equalsIgnoreCase(page)) {
                    cachedHome702 = root;
                    cachedAt702 = SystemClock.elapsedRealtime();
                }
            }, 450L);
        }
    }
}
