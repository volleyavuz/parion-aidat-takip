package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.47 - preserve v4.0.44 card hierarchy/click listeners; visual-only pass for General + Finance. */
public class MainActivityV647 extends MainActivityV644 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){root.postDelayed(this::style647,1500);root.postDelayed(this::style647,2200);}
    }

    private void style647(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        hideDelta647(root);
        ScrollView sv=findScroll647(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        styleSection647(box,"GENEL DURUM");
        styleSection647(box,"FİNANS");
    }

    private void styleSection647(LinearLayout box,String section){
        int start=-1,end=box.getChildCount();
        for(int i=0;i<box.getChildCount();i++){
            View v=box.getChildAt(i);
            if(v instanceof TextView){String n=norm647(String.valueOf(((TextView)v).getText()));if(n.startsWith(norm647(section))){start=i;break;}}
        }
        if(start<0)return;
        for(int i=start+1;i<box.getChildCount();i++){
            View v=box.getChildAt(i);
            if(v instanceof TextView){String n=norm647(String.valueOf(((TextView)v).getText()));if(n.startsWith("GENEL DURUM")||n.startsWith("FİNANS")||n.startsWith("SPORCU HAREKETLERİ")||n.startsWith("TAKİP GEREKTİRENLER")){end=i;break;}}
        }
        TextView heading=(TextView)box.getChildAt(start);
        heading.setTextSize(13f);heading.setTextColor(Color.rgb(28,28,28));heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        heading.setLineSpacing(dp(1),1f);heading.setPadding(dp(4),dp(16),dp(4),dp(8));
        for(int i=start+1;i<end;i++){
            View row=box.getChildAt(i);
            if(row instanceof LinearLayout && ((LinearLayout)row).getOrientation()==LinearLayout.HORIZONTAL)styleRow647((LinearLayout)row);
        }
    }

    private void styleRow647(LinearLayout row){
        row.setGravity(Gravity.TOP);
        for(int i=0;i<row.getChildCount();i++){
            View card=row.getChildAt(i);
            if(!(card instanceof ViewGroup))continue;
            GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(15));
            card.setBackground(bg);card.setElevation(dp(2));card.setMinimumHeight(dp(118));
            ViewGroup.LayoutParams p=card.getLayoutParams();
            if(p instanceof LinearLayout.LayoutParams){
                LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)p;lp.height=ViewGroup.LayoutParams.WRAP_CONTENT;lp.setMargins(dp(4),0,dp(4),0);card.setLayoutParams(lp);
            }
            styleTexts647(card);
        }
    }

    private void styleTexts647(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();if(s.isEmpty())return;String n=norm647(s);
            if(isTitle647(n)){t.setTextSize(11.5f);t.setTextColor(Color.rgb(48,48,48));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}
            else if(isNumeric647(s)){t.setTextSize(22f);t.setTextColor(Color.rgb(24,24,24));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}
            else{t.setTextSize(10.5f);t.setTextColor(Color.rgb(92,92,92));}
            t.setLineSpacing(dp(1),1f);return;
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)styleTexts647(g.getChildAt(i));}
    }

    private boolean isTitle647(String n){return n.contains("AKTİF SPORCU")||n.contains("ARA VERDİ")||n.contains("AYLIK HEDEF")||n.contains("GECİKMİŞ")||n.contains("MALZEME BORCU")||n.contains("ÖDENMEMİŞ MALZEME");}
    private boolean isNumeric647(String s){String x=s.replace("₺","").replace("TL","").replace(".","").replace(",","").replace(" ","").replace("+","").replace("-","");return x.matches("\\d+");}
    private void hideDelta647(View v){
        if(v instanceof TextView){TextView t=(TextView)v;String n=norm647(String.valueOf(t.getText()));if(n.contains("ÇİFT YÖNLÜ DELTA")||n.contains("TOPLU SNAPSHOT KAPALI")){t.setVisibility(View.GONE);ViewGroup.LayoutParams lp=t.getLayoutParams();if(lp!=null){lp.height=0;t.setLayoutParams(lp);}return;}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)hideDelta647(g.getChildAt(i));}
    }
    private ScrollView findScroll647(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll647(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm647(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
