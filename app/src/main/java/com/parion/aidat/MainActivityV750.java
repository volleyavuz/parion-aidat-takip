package com.parion.aidat;

import android.view.View;

/**
 * v4.2.33 - atomic FINANCE header reveal.
 *
 * The finance detail screen is functionally correct, but inherited base()/header compatibility
 * layers briefly expose the legacy header (ANASAYFA pill / duplicate logo) before the current
 * header patch finishes. Hide only the FINANS root while those queued UI-only header patches
 * drain, then reveal the final header in one frame. No DB/sync/navigation behavior is changed.
 */
public class MainActivityV750 extends MainActivityV749 {
    private int financeGeneration750=0;

    @Override void base(String title, boolean back){
        super.base(title, back);
        if(root==null || title==null || !"FİNANS".equalsIgnoreCase(title.trim())) return;

        final int gen=++financeGeneration750;
        final View builtRoot=root;
        builtRoot.setAlpha(0f);

        // Drain inherited header/status compatibility callbacks, then reveal only the final state.
        builtRoot.post(() -> builtRoot.post(() -> builtRoot.postOnAnimation(() -> {
            if(gen!=financeGeneration750 || root!=builtRoot) return;
            builtRoot.setAlpha(1f);
        })));
    }
}
