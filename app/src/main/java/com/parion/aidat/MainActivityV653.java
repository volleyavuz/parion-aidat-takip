package com.parion.aidat;

import android.view.*;
import android.widget.*;

/** v4.0.53 - hide dashboard content until all delayed home layout passes finish. */
public class MainActivityV653 extends MainActivityV652 {
    private int revealGen653=0;

    @Override void showHome(){
        super.showHome();
        final int gen=++revealGen653;
        if(root==null)return;
        root.post(()->{
            ScrollView sv=findScroll653(root);
            if(sv!=null){sv.animate().cancel();sv.setAlpha(0f);sv.setVisibility(View.INVISIBLE);}
        });
        root.postDelayed(()->reveal653(gen),2550);
    }

    private void reveal653(int gen){
        if(gen!=revealGen653||root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll653(root);if(sv==null)return;
        sv.setVisibility(View.VISIBLE);sv.setAlpha(0f);
        sv.animate().alpha(1f).setDuration(160).start();
    }

    private ScrollView findScroll653(View v){
        if(v instanceof ScrollView)return (ScrollView)v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                ScrollView s=findScroll653(g.getChildAt(i));if(s!=null)return s;
            }
        }
        return null;
    }
}
