package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.93 - HOME-only visual cleanup: remove legacy side rail and unify card surface language. */
public class MainActivityV693 extends MainActivityV692 {
    private static final int LINE693=Color.rgb(226,226,226);
    private static final int TEXT693=Color.rgb(38,38,38);

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::cleanupDashboard693,7000);
            root.postDelayed(this::cleanupDashboard693,7800);
        }
    }

    private void cleanupDashboard693(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll693(root);
        if(sv==null)return;
        removeLegacySideRail693(sv);
        if(sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        box.setPadding(dp(10),dp(8),dp(10),dp(26));
        normalizeDashboardCards693(box);
    }

    /** Remove only the narrow sibling rail that shares a horizontal HOME container with the ScrollView. */
    private void removeLegacySideRail693(ScrollView sv){
        View current=sv;
        while(current!=null&&current!=root&&current.getParent() instanceof ViewGroup){
            ViewGroup p=(ViewGroup)current.getParent();
            if(p instanceof LinearLayout && ((LinearLayout)p).getOrientation()==LinearLayout.HORIZONTAL && p.getChildCount()>1){
                View holder=current;
                for(int i=p.getChildCount()-1;i>=0;i--){
                    View c=p.getChildAt(i);
                    if(c==holder)continue;
                    int w=c.getWidth();
                    ViewGroup.LayoutParams raw=c.getLayoutParams();
                    int declared=raw==null?0:raw.width;
                    boolean narrow=(w>0&&w<=dp(120))||(declared>0&&declared<=dp(120));
                    if(narrow){
                        c.setVisibility(View.GONE);
                        if(raw!=null){raw.width=0;c.setLayoutParams(raw);}
                    }
                }
                ViewGroup.LayoutParams hr=holder.getLayoutParams();
                if(hr instanceof LinearLayout.LayoutParams){
                    LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)hr;
                    lp.width=0;lp.weight=1f;holder.setLayoutParams(lp);
                }else if(hr!=null){hr.width=ViewGroup.LayoutParams.MATCH_PARENT;holder.setLayoutParams(hr);}
                p.requestLayout();
                return;
            }
            current=(p instanceof View)?(View)p:null;
        }
    }

    private void normalizeDashboardCards693(LinearLayout box){
        String[] titles={
            "FOTOĞRAF EKSİK","FOTOGRAF EKSIK","KAYIT FORMU EKSİK","KAYIT FORMU EKSIK",
            "TİŞÖRT ALMAYAN","TISORT ALMAYAN","KIŞIN ARANACAK","KISIN ARANACAK",
            "YAZIN ARANACAK","DEVAMSIZLAR","YENİ BAŞLAYANLAR","YENI BASLAYANLAR",
            "BU AY BAŞLAYANLAR","BU AY BASLAYANLAR","GEÇEN AY BAŞLAYANLAR","GECEN AY BASLAYANLAR"
        };
        LinkedHashSet<View> cards=new LinkedHashSet<>();
        for(String title:titles){
            ArrayList<TextView> hits=new ArrayList<>();
            findTexts693(box,title,hits);
            for(TextView t:hits){
                View card=nearestClickable693(t,box);
                if(card==null)card=topChild693(box,t);
                if(card!=null&&card!=box)cards.add(card);
                styleCardText693(t,title);
            }
        }
        for(View card:cards)styleSurface693(card);
    }

    private void styleSurface693(View card){
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1),LINE693);
        card.setBackground(bg);
        card.setElevation(dp(1));
        if(card.getPaddingLeft()<dp(10)||card.getPaddingRight()<dp(10))
            card.setPadding(dp(12),Math.max(card.getPaddingTop(),dp(10)),dp(12),Math.max(card.getPaddingBottom(),dp(10)));
        ViewGroup.LayoutParams raw=card.getLayoutParams();
        if(raw instanceof LinearLayout.LayoutParams){
            LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)raw;
            lp.setMargins(Math.max(lp.leftMargin,dp(4)),lp.topMargin,Math.max(lp.rightMargin,dp(4)),Math.max(lp.bottomMargin,dp(7)));
            card.setLayoutParams(lp);
        }
    }

    private void styleCardText693(TextView t,String title){
        String n=norm693(String.valueOf(t.getText()));
        if(n.contains("DEVAMSIZLAR")||n.contains("ARANACAK")||n.contains("EKSİK")||n.contains("EKSIK")||n.contains("BAŞLAYAN")||n.contains("BASLAYAN")||n.contains("TİŞÖRT ALMAYAN")||n.contains("TISORT ALMAYAN")){
            t.setTextColor(TEXT693);
            t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            if(t.getTextSize()/getResources().getDisplayMetrics().scaledDensity>14f)t.setTextSize(12.5f);
            t.setGravity(Gravity.CENTER);
            t.setMaxLines(3);
        }
    }

    private View nearestClickable693(View v,View stop){
        View cur=v,best=null;
        while(cur!=null&&cur!=stop){
            if(cur.isClickable()||cur.hasOnClickListeners())best=cur;
            ViewParent p=cur.getParent();
            if(!(p instanceof View))break;
            cur=(View)p;
        }
        return best;
    }

    private void findTexts693(View v,String needle,ArrayList<TextView> out){
        if(v instanceof TextView){
            String n=norm693(String.valueOf(((TextView)v).getText()));
            if(n.contains(norm693(needle)))out.add((TextView)v);
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)findTexts693(g.getChildAt(i),needle,out);}
    }
    private View topChild693(LinearLayout box,View v){View cur=v;while(cur!=null&&cur.getParent() instanceof View&&cur.getParent()!=box)cur=(View)cur.getParent();return cur!=null&&cur.getParent()==box?cur:null;}
    private ScrollView findScroll693(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll693(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm693(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
