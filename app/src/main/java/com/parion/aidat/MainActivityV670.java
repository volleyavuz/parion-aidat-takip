package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.70 - safe duplicate winter cleanup directly on V668; bypasses broken V669. */
public class MainActivityV670 extends MainActivityV668 {
    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::patch670);
    }

    private void patch670(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll670(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        View keepRow=findTag670(box,"v663-call-row");
        ArrayList<View> winterTop=new ArrayList<>();
        collectWinterTop670(box,box,winterTop);
        for(int i=winterTop.size()-1;i>=0;i--){
            View top=winterTop.get(i);
            if(top==null||top==keepRow||isDescendant670(top,keepRow))continue;
            // Only remove a direct child of the scroll content; never remove an ancestor/container.
            if(top.getParent()==box){box.removeView(top);break;}
        }
    }

    private void collectWinterTop670(LinearLayout box,View v,ArrayList<View> out){
        if(v instanceof TextView){
            String n=norm670(String.valueOf(((TextView)v).getText()));
            if(n.equals("KIŞIN ARANACAK")||n.equals("KIŞIN ARANACAKLAR")||n.equals("KISIN ARANACAK")||n.equals("KISIN ARANACAKLAR")){
                View top=topChild670(box,v);if(top!=null&&!out.contains(top))out.add(top);
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectWinterTop670(box,g.getChildAt(i),out);}
    }

    private View topChild670(LinearLayout box,View v){
        View cur=v;
        while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();
        return cur!=null&&cur.getParent()==box?cur:null;
    }
    private boolean isDescendant670(View child,View ancestor){
        if(child==null||ancestor==null)return false;View cur=child;
        while(cur!=null){if(cur==ancestor)return true;ViewParent p=cur.getParent();cur=p instanceof View?(View)p:null;}return false;
    }
    private ScrollView findScroll670(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll670(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag670(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag670(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private String norm670(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
