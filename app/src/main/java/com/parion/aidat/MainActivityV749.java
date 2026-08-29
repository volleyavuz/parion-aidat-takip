package com.parion.aidat;

import android.os.Bundle;
import android.view.View;

/**
 * v4.2.32 - atomic final HOME reveal.
 *
 * The legacy dashboard chain still contains a handful of UI-only root.post(...) patches
 * (movement/follow-up/polish/header compatibility layers). They are kept for function
 * compatibility in this intermediate cleanup step, but their intermediate frames are never
 * exposed. HOME is rendered off-screen, queued compatibility patches drain, then the fully
 * finished current dashboard is revealed in one frame.
 *
 * This does not add a loading cover and does not touch DB/sync/navigation behavior.
 */
public class MainActivityV749 extends MainActivityV748 {
    private int homeGeneration749=0;

    @Override void showHome(){
        final int gen=++homeGeneration749;
        super.showHome();
        if(root==null)return;

        final View builtRoot=root;
        builtRoot.setAlpha(0f);

        // Drain callbacks posted by inherited dashboard-only compatibility layers.
        builtRoot.post(() -> builtRoot.post(() -> builtRoot.postOnAnimation(() -> {
            if(gen!=homeGeneration749 || root!=builtRoot)return;
            builtRoot.setAlpha(1f);
        })));
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
    }
}
