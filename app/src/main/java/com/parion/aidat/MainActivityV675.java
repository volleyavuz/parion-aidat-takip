package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.75 - attendance screens: remove redundant Home/Back controls and normalize title sizing. */
public class MainActivityV675 extends MainActivityV674 {

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->patchAttendanceChrome675(title));
    }

    private void patchAttendanceChrome675(String title){
        if(root==null||!isAttendance675(title))return;

        // Attendance already has the fixed app navigation. Remove legacy local navigation
        // controls wherever an older layer inserted them (top or bottom bars).
        removeLegacyNav675(root);

        // Normalize every occurrence of the current screen title so a duplicated/legacy
        // title cannot remain oversized or clipped at the bottom of the screen.
        normalizeTitles675(root,title);

        // Collapse empty navigation rows left behind after removing Home/Back controls.
        collapseEmptyBars675(root);
    }

    private boolean isAttendance675(String title){
        String p=page==null?"":page.toUpperCase(Locale.ROOT);
        String t=norm675(title);
        return p.startsWith("ATTENDANCE_")||t.contains("YOKLAMA");
    }

    private void removeLegacyNav675(ViewGroup g){
        for(int i=g.getChildCount()-1;i>=0;i--){
            View v=g.getChildAt(i);
            if(v instanceof TextView){
                String s=norm675(String.valueOf(((TextView)v).getText()));
                if(s.contains("ANASAYFA")||s.equals("GERİ")||s.equals("‹")||s.equals("<")){
                    g.removeViewAt(i);
                    continue;
                }
            }
            if(v instanceof ViewGroup)removeLegacyNav675((ViewGroup)v);
        }
    }

    private void normalizeTitles675(View v,String wanted){
        if(v instanceof TextView){
            TextView t=(TextView)v;
            String s=norm675(String.valueOf(t.getText()));
            String w=norm675(wanted);
            if(!w.isEmpty()&&s.equals(w)){
                int len=s.length();
                t.setSingleLine(false);
                t.setMaxLines(2);
                t.setEllipsize(null);
                t.setTextSize(len>30?12.5f:len>22?13.5f:14.5f);
                t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                t.setTextColor(Color.rgb(212,175,55));
                t.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
                t.setPadding(dp(8),dp(3),dp(8),dp(3));
                ViewGroup.LayoutParams lp=t.getLayoutParams();
                if(lp!=null){lp.height=ViewGroup.LayoutParams.WRAP_CONTENT;t.setLayoutParams(lp);}
                t.setMinHeight(dp(44));
            }
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)normalizeTitles675(g.getChildAt(i),wanted);
        }
    }

    private void collapseEmptyBars675(ViewGroup g){
        for(int i=g.getChildCount()-1;i>=0;i--){
            View v=g.getChildAt(i);
            if(v instanceof ViewGroup){
                ViewGroup child=(ViewGroup)v;
                collapseEmptyBars675(child);
                if(child.getChildCount()==0 && child!=root){
                    ViewGroup.LayoutParams lp=child.getLayoutParams();
                    if(lp!=null && lp.height>0 && lp.height<=dp(80)){
                        lp.height=0;child.setLayoutParams(lp);child.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private String norm675(String s){
        return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));
    }
}
