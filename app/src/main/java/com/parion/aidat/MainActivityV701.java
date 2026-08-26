package com.parion.aidat;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

/**
 * v4.1.01 - render HOME only once during Activity startup and skip the expensive
 * legacy dashboard calculation that is immediately replaced by the final V657 dashboard.
 * v4.0.99 remains the untouched recovery baseline.
 */
public class MainActivityV701 extends MainActivityV700 {
    private boolean creating701 = false;
    private boolean finalHomeBuild701 = false;

    @Override public void onCreate(Bundle b) {
        creating701 = true;
        super.onCreate(b);
        creating701 = false;
        // Ancestor onCreate implementations historically request HOME several times.
        // Build the real screen exactly once, after all synchronous initialization is done.
        showHome();
    }

    @Override void showHome() {
        if (creating701) {
            // Keep a harmless lightweight content root available for old onCreate layers
            // that expect root to exist, without running any dashboard queries/rendering.
            page = "HOME";
            currentAthlete = -1;
            if (root == null) {
                root = new LinearLayout(this);
                root.setOrientation(LinearLayout.VERTICAL);
                root.setBackgroundColor(Color.rgb(246,246,246));
                setContentView(root);
            }
            return;
        }

        finalHomeBuild701 = true;
        try {
            super.showHome();
        } finally {
            finalHomeBuild701 = false;
        }
    }

    /**
     * V36's original dashboardData() walks every athlete and then every payment row.
     * V657 hides/replaces those legacy finance cards in the same showHome call, so doing
     * that work is pure startup/navigation cost. Return an empty shell only during the
     * final HOME construction; V657 then builds the visible dashboard from its own queries.
     */
    @Override DashData dashboardData() {
        if (finalHomeBuild701) return new DashData();
        return super.dashboardData();
    }
}
