package com.parion.aidat;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

/**
 * v4.1.08 - HOME transition cleanup.
 * Keep v4.1.07 behavior/data intact, but stop exposing the legacy white/grey
 * intermediate window while HOME is being rebuilt. No View caching/reparenting.
 */
public class MainActivityV703 extends MainActivityV702 {
    private boolean firstHome703 = true;

    @Override public void onCreate(Bundle b) {
        Window w = getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.rgb(250,248,239)));
        }
        super.onCreate(b);
    }

    @Override void showHome() {
        // Do not cache or move the old dashboard root. The complete legacy/final HOME
        // pipeline still runs exactly as in v4.1.07; only avoid an extra transition
        // animation/window flash during in-app HOME navigation.
        Window w = getWindow();
        if (!firstHome703 && w != null) {
            w.setWindowAnimations(0);
        }
        super.showHome();
        firstHome703 = false;
    }
}
