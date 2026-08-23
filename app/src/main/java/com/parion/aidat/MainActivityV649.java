package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.49 - stability reset: never move/reparent dashboard cards. */
public class MainActivityV649 extends MainActivityV643 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.postDelayed(this::patch649,900);
            root.postDelayed(this::patch649,1350);
        }
    }

    private void patch649(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        hideDelta649(root);
        hideAthletes649(root);
        styleDashboard649(root);
    }

    private void hideDelta649(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;
            String n=norm649(String.valueOf(t.getText()));
            if(n.contains("ÇİFT YÖNLÜ DELTA")||n.contains("CIFT YONLU DELTA")||n.contains("TOPLU SNAPSHOT KAPALI")){
                t.setVisibility(View.GONE);
                ViewGroup.LayoutParams lp=t.getLayoutParams();if(lp!=null){lp.height=0;t.setLayoutParams(lp);}return;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)hideDelta649(g.getChildAt(i));}
    }

    private void hideAthletes649(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String n=norm649(String.valueOf(t.getText()));
            if(n.equals("SPORCULAR")||n.equals("SPORCULAR ›")||n.equals("SPORCULAR >")){
                View target=nearestClickable649(t);
                if(target!=null){target.setVisibility(View.GONE);ViewGroup.LayoutParams lp=target.getLayoutParams();if(lp!=null){lp.height=0;target.setLayoutParams(lp);}}
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)hideAthletes649(g.getChildAt(i));}
    }

    private View nearestClickable649(View v){
        View cur=v,best=null;
        while(cur!=null&&cur!=root){
            if(cur.isClickable()||cur.hasOnClickListeners())best=cur;
            ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;
        }
        return best;
    }

    private void styleDashboard649(View v){
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            if(g instanceof LinearLayout && ((LinearLayout)g).getOrientation()==LinearLayout.HORIZONTAL){
                boolean hasMetric=containsMetric649(g);
                if(hasMetric){
                    for(int i=0;i<g.getChildCount();i++){
                        View c=g.getChildAt(i);
                        if(c instanceof ViewGroup){
                            GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(14));
                            c.setBackground(bg);c.setElevation(dp(1));
                        }
                    }
                }
            }
            for(int i=0;i<g.getChildCount();i++)styleDashboard649(g.getChildAt(i));
        }else if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();String n=norm649(s);
            if(isMetricTitle649(n)){t.setTextSize(11.5f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setTextColor(Color.rgb(48,48,48));}
            else if(isValue649(s)){t.setTextSize(20f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setTextColor(Color.rgb(24,24,24));}
        }
    }

    private boolean containsMetric649(View v){
        if(v instanceof TextView){String n=norm649(String.valueOf(((TextView)v).getText()));return isMetricTitle649(n);}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsMetric649(g.getChildAt(i)))return true;}
        return false;
    }

    private boolean isMetricTitle649(String n){
        return n.contains("AKTİF SPORCU")||n.contains("ARA VERDİ")||n.contains("AYLIK HEDEF")||n.contains("GECİKMİŞ")||n.contains("MALZEME BORCU")||n.contains("ÖDENMEMİŞ MALZEME")||n.contains("BU AY BAŞLAYAN")||n.contains("GEÇEN AY BAŞLAYAN")||n.contains("SON 3 AY")||n.contains("FOTOĞRAF EKSİK")||n.contains("KAYIT FORMU EKSİK")||n.contains("DEVAMSIZLAR");
    }

    private boolean isValue649(String s){String x=s.replace("₺","").replace("TL","").replace(".","").replace(",","").replace(" ","").replace("+","").replace("-","");return x.matches("\\d+");}
    private String norm649(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
