package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.69 - remove the duplicate legacy winter follow-up card only. */
public class MainActivityV669 extends MainActivityV668 {
    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::patch669);
    }

    private void patch669(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        View keepRow=findTag669(root,"v663-call-row");
        removeExtraWinter669(root,keepRow);
    }

    private void removeExtraWinter669(View v,View keepRow){
        if(!(v instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){
            View c=g.getChildAt(i);
            if(c==keepRow)continue;
            if(containsWinter669(c)){
                if(isInside669(c,keepRow))continue;
                if(isLegacyWinterCard669(c)){
                    g.removeViewAt(i);
                    continue;
                }
            }
            removeExtraWinter669(c,keepRow);
        }
    }

    private boolean isLegacyWinterCard669(View v){
        if(v==null)return false;
        if(v instanceof TextView){String n=norm669(String.valueOf(((TextView)v).getText()));return n.equals("KIŞIN ARANACAK")||n.equals("KIŞIN ARANACAKLAR")||n.equals("KISIN ARANACAK")||n.equals("KISIN ARANACAKLAR");}
        if(v instanceof LinearLayout){
            LinearLayout l=(LinearLayout)v;
            return l.getOrientation()==LinearLayout.VERTICAL && containsWinter669(v);
        }
        return false;
    }

    private boolean containsWinter669(View v){
        if(v instanceof TextView){String n=norm669(String.valueOf(((TextView)v).getText()));return n.equals("KIŞIN ARANACAK")||n.equals("KIŞIN ARANACAKLAR")||n.equals("KISIN ARANACAK")||n.equals("KISIN ARANACAKLAR");}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsWinter669(g.getChildAt(i)))return true;}
        return false;
    }

    private boolean isInside669(View child,View ancestor){
        if(child==null||ancestor==null)return false;
        View cur=child;
        while(cur!=null){if(cur==ancestor)return true;ViewParent p=cur.getParent();cur=p instanceof View?(View)p:null;}
        return false;
    }

    private View findTag669(View v,String tag){
        if(v!=null&&tag.equals(v.getTag()))return v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag669(g.getChildAt(i),tag);if(r!=null)return r;}}
        return null;
    }

    private String norm669(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
