package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.45 - first dashboard visual pass: General Status + Finance cards only. */
public class MainActivityV645 extends MainActivityV644 {
    @Override void showHome(){
        super.showHome();
        if(root!=null)root.postDelayed(this::style645,1500);
    }

    private void style645(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll645(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        styleSection645(box,"GENEL DURUM");
        styleSection645(box,"FİNANS");
    }

    private void styleSection645(LinearLayout box,String section){
        int start=-1,end=box.getChildCount();
        for(int i=0;i<box.getChildCount();i++){
            View v=box.getChildAt(i);
            if(v instanceof TextView){String n=norm645(String.valueOf(((TextView)v).getText()));if(n.startsWith(norm645(section))){start=i;break;}}
        }
        if(start<0)return;
        for(int i=start+1;i<box.getChildCount();i++){
            View v=box.getChildAt(i);if(v instanceof TextView){String n=norm645(String.valueOf(((TextView)v).getText()));if(n.startsWith("GENEL DURUM")||n.startsWith("FİNANS")||n.startsWith("SPORCU HAREKETLERİ")||n.startsWith("TAKİP GEREKTİRENLER")){end=i;break;}}
        }
        TextView heading=(TextView)box.getChildAt(start);heading.setTextSize(13.5f);heading.setTextColor(Color.rgb(25,25,25));heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);heading.setLineSpacing(dp(2),1.0f);heading.setPadding(dp(5),dp(18),dp(5),dp(9));
        for(int i=start+1;i<end;i++){View row=box.getChildAt(i);if(row instanceof ViewGroup)styleRow645((ViewGroup)row);}
    }

    private void styleRow645(ViewGroup row){
        for(int i=0;i<row.getChildCount();i++){
            View card=row.getChildAt(i);if(isSpacer645(card))continue;
            GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(15));card.setBackground(bg);card.setElevation(dp(2));card.setPadding(dp(14),dp(13),dp(14),dp(13));card.setMinimumHeight(dp(102));
            styleTexts645(card);
        }
    }

    private void styleTexts645(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();if(s.isEmpty())return;
            if(isPrimaryValue645(s)){t.setTextSize(27f);t.setTextColor(Color.rgb(20,20,20));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setIncludeFontPadding(false);}
            else if(isTitle645(s)){t.setTextSize(12.5f);t.setTextColor(Color.rgb(45,45,45));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setLineSpacing(dp(1),1.0f);}
            else{t.setTextSize(11f);t.setTextColor(Color.rgb(92,92,92));t.setLineSpacing(dp(1),1.0f);}
            return;
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)styleTexts645(g.getChildAt(i));}
    }

    private boolean isTitle645(String s){String n=norm645(s);return n.contains("AKTİF SPORCU")||n.contains("ARA VERDİ")||n.contains("AYLIK HEDEF")||n.contains("GECİKMİŞ")||n.contains("MALZEME BORCU")||n.contains("ÖDENMEMİŞ MALZEME");}
    private boolean isPrimaryValue645(String s){String x=s.replace("₺","").replace("TL","").replace(".","").replace(",","").replace(" ","").replace("+","").replace("-","");return x.matches("\\d+");}
    private boolean isSpacer645(View v){return !(v instanceof ViewGroup)&&!(v instanceof TextView)&&v.getMinimumHeight()<=dp(2);}
    private ScrollView findScroll645(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll645(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm645(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
