package com.parion.aidat;

import android.view.View;
import android.widget.ScrollView;

/** v4.0.43 - prevent old dashboard layers from flashing during startup/sync. */
public class MainActivityV643 extends MainActivityV642 {
    @Override void showHome(){
        super.showHome();
        if(root==null) return;

        ScrollView sv=findHomeScroll643(root);
        if(sv==null) return;

        sv.setAlpha(0f);
        sv.setVisibility(View.INVISIBLE);

        root.postDelayed(()->revealHome643(sv),1350);
    }

    private void revealHome643(ScrollView sv){
        if(root==null || page==null || !"HOME".equalsIgnoreCase(page)) return;
        sv.setAlpha(1f);
        sv.setVisibility(View.VISIBLE);
    }

    private ScrollView findHomeScroll643(View v){
        if(v instanceof ScrollView) return (ScrollView)v;
        if(v instanceof android.view.ViewGroup){
            android.view.ViewGroup g=(android.view.ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                ScrollView s=findHomeScroll643(g.getChildAt(i));
                if(s!=null) return s;
            }
        }
        return null;
    }
}
