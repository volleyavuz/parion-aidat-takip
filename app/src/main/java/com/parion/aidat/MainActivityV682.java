package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.82 - narrow dashboard cleanup: hide clipped Monthly Target subtitle and remove duplicate legacy Winter card. */
public class MainActivityV682 extends MainActivityV681 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::cleanup682);
            root.postDelayed(this::cleanup682,180);
        }
    }

    private void cleanup682(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll682(root);
        if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        cleanMonthlyTarget682(box);
        removeDuplicateWinter682(box);
    }

    private void cleanMonthlyTarget682(LinearLayout box){
        TextView monthly=findExact682(box,"AYLIK HEDEF");
        if(monthly==null)return;
        View card=nearestCard682(monthly);
        if(!(card instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)card;
        // V657 metric cards tag their bottom explanatory line as "sub". On the full-width
        // Monthly Target card that line can be clipped by the fixed card height, so remove only it.
        View sub=findTag682(g,"sub");
        if(sub!=null){
            sub.setVisibility(View.GONE);
            ViewGroup.LayoutParams lp=sub.getLayoutParams();
            if(lp!=null){lp.height=0;sub.setLayoutParams(lp);}
        }
    }

    private void removeDuplicateWinter682(LinearLayout box){
        View keep=findTag682(box,"v663-call-row");
        ArrayList<View> winters=new ArrayList<>();
        collectWinterTop682(box,box,winters);
        for(int i=winters.size()-1;i>=0;i--){
            View v=winters.get(i);
            if(v==null||v==keep||isDescendant682(v,keep))continue;
            if(v.getParent()==box)box.removeView(v);
        }
    }

    private View nearestCard682(View v){
        View cur=v,best=null;
        while(cur!=null&&cur!=root){
            if(cur.hasOnClickListeners()||cur.isClickable())best=cur;
            ViewParent p=cur.getParent();
            if(!(p instanceof View))break;
            cur=(View)p;
        }
        if(best!=null)return best;
        cur=v;
        while(cur!=null&&cur!=root){
            ViewParent p=cur.getParent();
            if(!(p instanceof View))break;
            cur=(View)p;
            if(cur instanceof LinearLayout)return cur;
        }
        return null;
    }

    private void collectWinterTop682(LinearLayout box,View v,ArrayList<View> out){
        if(v instanceof TextView){
            String n=norm682(String.valueOf(((TextView)v).getText()));
            if(n.equals("KIŞIN ARANACAK")||n.equals("KIŞIN ARANACAKLAR")||n.equals("KISIN ARANACAK")||n.equals("KISIN ARANACAKLAR")){
                View top=topChild682(box,v);if(top!=null&&!out.contains(top))out.add(top);
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectWinterTop682(box,g.getChildAt(i),out);}
    }

    private TextView findExact682(View v,String wanted){
        if(v instanceof TextView&&norm682(String.valueOf(((TextView)v).getText())).equals(norm682(wanted)))return(TextView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findExact682(g.getChildAt(i),wanted);if(r!=null)return r;}}
        return null;
    }
    private View topChild682(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private boolean isDescendant682(View child,View ancestor){if(child==null||ancestor==null)return false;View cur=child;while(cur!=null){if(cur==ancestor)return true;ViewParent p=cur.getParent();cur=p instanceof View?(View)p:null;}return false;}
    private ScrollView findScroll682(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll682(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag682(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag682(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private String norm682(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
