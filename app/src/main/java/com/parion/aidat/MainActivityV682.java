package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.82 - narrow dashboard cleanup: remove stray half text under Monthly Target and duplicate legacy Winter card. */
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
        removeStrayHalfUnderMonthlyTarget682(box);
        removeDuplicateWinter682(box);
    }

    private void removeStrayHalfUnderMonthlyTarget682(LinearLayout box){
        TextView monthly=findExact682(box,"AYLIK HEDEF");
        if(monthly==null)return;
        View monthlyTop=topChild682(box,monthly);
        if(monthlyTop==null)return;
        int idx=box.indexOfChild(monthlyTop);
        // The reported artifact is a standalone/legacy partial label immediately after the monthly target area.
        for(int i=Math.min(box.getChildCount()-1,idx+2);i>idx;i--){
            View candidate=box.getChildAt(i);
            if(candidate==null)continue;
            if(containsCoreCardTitle682(candidate))continue;
            String txt=allText682(candidate);
            String n=norm682(txt);
            if(n.equals("YARIM")||n.startsWith("YARIM ")||n.endsWith(" YARIM")||
               (candidate instanceof TextView && n.length()>0 && n.length()<=12)){
                box.removeViewAt(i);
            }
        }
    }

    private void removeDuplicateWinter682(LinearLayout box){
        View keep=findTag682(box,"v663-call-row");
        ArrayList<View> winters=new ArrayList<>();
        collectWinterTop682(box,box,winters);
        for(int i=winters.size()-1;i>=0;i--){
            View v=winters.get(i);
            if(v==null||v==keep||isDescendant682(v,keep))continue;
            // Preserve the current tagged call row; remove every remaining direct legacy winter card.
            if(v.getParent()==box)box.removeView(v);
        }
    }

    private boolean containsCoreCardTitle682(View v){
        String n=norm682(allText682(v));
        return n.contains("AYLIK HEDEF")||n.contains("AKTİF SPORCU")||n.contains("ARA VEREN")||
               n.contains("GECİKMİŞ")||n.contains("ÖDEME VADESİ")||n.contains("TİŞÖRT ALMAYAN")||
               n.contains("TISORT ALMAYAN")||n.contains("BU AY BAŞLAYAN")||n.contains("GEÇEN AY BAŞLAYAN")||
               n.contains("FOTOĞRAF EKSİK")||n.contains("KAYIT FORMU EKSİK")||n.contains("DEVAMSIZLAR")||
               n.contains("YAZIN ARANACAK")||n.contains("KIŞIN ARANACAK")||n.contains("KISIN ARANACAK");
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
    private String allText682(View v){
        StringBuilder b=new StringBuilder();collectText682(v,b);return b.toString();
    }
    private void collectText682(View v,StringBuilder b){
        if(v instanceof TextView)b.append(' ').append(((TextView)v).getText());
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectText682(g.getChildAt(i),b);}
    }
    private View topChild682(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private boolean isDescendant682(View child,View ancestor){if(child==null||ancestor==null)return false;View cur=child;while(cur!=null){if(cur==ancestor)return true;ViewParent p=cur.getParent();cur=p instanceof View?(View)p:null;}return false;}
    private ScrollView findScroll682(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll682(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag682(View v,String tag){if(v!=null&&tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag682(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private String norm682(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
