package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.1.45 - remove standalone Early Payment card from HOME only. */
public class MainActivityV722 extends MainActivityV721 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::removeStandaloneEarly722,250);
            root.postDelayed(this::removeStandaloneEarly722,1500);
            root.postDelayed(this::removeStandaloneEarly722,5000);
            root.postDelayed(this::removeStandaloneEarly722,10000);
            root.postDelayed(this::removeStandaloneEarly722,15000);
        }
    }

    private void removeStandaloneEarly722(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll722(root);if(sv==null||sv.getChildCount()==0)return;
        View child=sv.getChildAt(0);if(!(child instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)child;
        for(int i=box.getChildCount()-1;i>=0;i--){
            View v=box.getChildAt(i);
            if("v718-finance-entry".equals(v.getTag()))continue;
            String t=norm722(allText722(v));
            if((t.contains("ERKEN ÖDEME GİR")||t.contains("EKSİK ÖDEME GİR"))&&!t.contains("FİNANS"))box.removeViewAt(i);
        }
    }

    private ScrollView findScroll722(View v){
        if(v instanceof ScrollView)return(ScrollView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll722(g.getChildAt(i));if(s!=null)return s;}}
        return null;
    }
    private String allText722(View v){StringBuilder b=new StringBuilder();collect722(v,b);return b.toString();}
    private void collect722(View v,StringBuilder b){if(v instanceof TextView)b.append(' ').append(((TextView)v).getText());if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collect722(g.getChildAt(i),b);}}
    private String norm722(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
