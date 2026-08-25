package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.76 - keep attendance title/header chrome at the top like all other screens. */
public class MainActivityV676 extends MainActivityV675 {

    @Override void base(String title, boolean back){
        super.base(title, back);
        if(root!=null) root.post(() -> moveAttendanceHeaderToTop676(title));
    }

    private void moveAttendanceHeaderToTop676(String title){
        if(root==null || !isAttendance676(title) || root.getChildCount()<2) return;
        View header=findHeader676(root,title);
        if(header==null || header==root) return;
        View top=header;
        while(top.getParent() instanceof View && top.getParent()!=root) top=(View)top.getParent();
        if(top.getParent()!=root) return;
        int idx=root.indexOfChild(top);
        if(idx<=0) return;
        root.removeView(top);
        root.addView(top,0);
    }

    private View findHeader676(View v,String title){
        if(v instanceof TextView){
            String s=norm676(String.valueOf(((TextView)v).getText()));
            if(s.equals(norm676(title))) return v;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                View r=findHeader676(g.getChildAt(i),title);
                if(r!=null) return r;
            }
        }
        return null;
    }

    private boolean isAttendance676(String title){
        String p=page==null?"":page.toUpperCase(Locale.ROOT);
        return p.startsWith("ATTENDANCE_") || norm676(title).contains("YOKLAMA");
    }

    private String norm676(String s){
        return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));
    }
}
