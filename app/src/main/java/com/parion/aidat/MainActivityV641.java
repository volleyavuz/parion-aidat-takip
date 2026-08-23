package com.parion.aidat;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/** v4.0.41 - first dashboard cleanup: remove delta banner, athletes card and colored card borders. */
public class MainActivityV641 extends MainActivityV640 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::cleanHome641);
            root.postDelayed(this::cleanHome641,180);
        }
    }

    private void cleanHome641(){
        if(root==null || page==null || !"HOME".equalsIgnoreCase(page)) return;
        ScrollView scroll=findHomeScroll641(root);
        if(scroll==null) return;
        hideTargetTexts641(scroll);
        removeCardStrokes641(scroll);
    }

    private void hideTargetTexts641(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;
            String s=String.valueOf(t.getText()).trim().toUpperCase(new Locale("tr","TR"));
            if("SPORCULAR".equals(s) || s.startsWith("ÇİFT YÖNLÜ DELTA") || s.startsWith("CIFT YONLU DELTA")){
                ViewParent p=t.getParent();
                if(p instanceof ViewGroup && p!=root){
                    ViewGroup pg=(ViewGroup)p;
                    if(pg.getChildCount()<=3) pg.setVisibility(View.GONE);
                    else t.setVisibility(View.GONE);
                }else t.setVisibility(View.GONE);
                return;
            }
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++) hideTargetTexts641(g.getChildAt(i));
        }
    }

    private void removeCardStrokes641(View v){
        if(v instanceof ViewGroup && !(v instanceof ScrollView)){
            Drawable bg=v.getBackground();
            if(bg instanceof GradientDrawable){
                ((GradientDrawable)bg).setStroke(0,Color.TRANSPARENT);
                v.invalidate();
            }
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++) removeCardStrokes641(g.getChildAt(i));
        }
    }

    private ScrollView findHomeScroll641(View v){
        if(v instanceof ScrollView) return (ScrollView)v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                ScrollView s=findHomeScroll641(g.getChildAt(i));
                if(s!=null) return s;
            }
        }
        return null;
    }
}
