package com.parion.aidat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Locale;

/** v4.0.40 - clean dashboard: remove athletes card and top online/cloud update cards. */
public class MainActivityV640 extends MainActivityV639 {
    @Override void showHome(){
        super.showHome();
        if(root!=null) cleanHome640(root);
    }

    private void cleanHome640(View v){
        if(root==null || page==null || !"HOME".equalsIgnoreCase(page)) return;
        removeMatching640(root);
    }

    private void removeMatching640(ViewGroup g){
        for(int i=g.getChildCount()-1;i>=0;i--){
            View c=g.getChildAt(i);
            if(containsHomeTarget640(c)){
                g.removeViewAt(i);
                continue;
            }
            if(c instanceof ViewGroup) removeMatching640((ViewGroup)c);
        }
    }

    private boolean containsHomeTarget640(View v){
        if(v instanceof TextView){
            String s=String.valueOf(((TextView)v).getText()).trim().toUpperCase(new Locale("tr","TR"));
            if("SPORCULAR".equals(s)) return true;
            if(s.contains("ONLINE") || s.contains("BULUT GÜNCEL") || s.contains("DEĞİŞİKLİK BEKLİYOR")) return true;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++) if(containsHomeTarget640(g.getChildAt(i))) return true;
        }
        return false;
    }
}
