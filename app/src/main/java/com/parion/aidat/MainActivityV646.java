package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.46 - rebuild General Status and Finance card geometry after v4.0.45 clipping. */
public class MainActivityV646 extends MainActivityV645 {
    @Override void showHome(){
        super.showHome();
        if(root!=null){root.postDelayed(this::fix646,1650);root.postDelayed(this::fix646,2300);}
    }

    private void fix646(){
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        removeDelta646(root);
        ScrollView sv=findScroll646(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        rebuildSection646(box,"GENEL DURUM",new String[]{"AKTİF SPORCU","ARA VERDİ"});
        rebuildSection646(box,"FİNANS",new String[]{"AYLIK HEDEF","GECİKMİŞ","MALZEME BORCU"});
        styleHeadings646(box);
    }

    private void rebuildSection646(LinearLayout box,String section,String[] names){
        int start=findSection646(box,section);if(start<0)return;
        int end=findNextSection646(box,start+1);
        ArrayList<View> cards=new ArrayList<>();
        for(String name:names){View c=findTopCard646(box,start+1,end,name);if(c!=null&&!cards.contains(c))cards.add(c);}
        if(cards.isEmpty())return;
        for(View c:cards)if(c.getParent()==box)box.removeView(c);
        // remove old section rows that became empty / contained these cards
        for(int i=Math.min(end-1,box.getChildCount()-1);i>start;i--){View v=box.getChildAt(i);if(v instanceof LinearLayout && ((LinearLayout)v).getOrientation()==LinearLayout.HORIZONTAL)box.removeViewAt(i);}
        int at=start+1;
        for(int i=0;i<cards.size();i+=2){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.TOP);row.setPadding(0,0,0,dp(10));
            addMetric646(row,cards.get(i));
            if(i+1<cards.size())addMetric646(row,cards.get(i+1));else row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(1),1f));
            box.addView(row,Math.min(at++,box.getChildCount()));
        }
    }

    private void addMetric646(LinearLayout row,View card){
        if(card.getParent() instanceof ViewGroup)((ViewGroup)card.getParent()).removeView(card);
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));
        card.setBackground(bg);card.setElevation(dp(2));card.setPadding(dp(12),dp(14),dp(12),dp(14));card.setMinimumHeight(dp(126));
        ViewGroup.LayoutParams old=card.getLayoutParams();
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(136),1f);lp.setMargins(dp(4),0,dp(4),0);row.addView(card,lp);
        normalizeCard646(card);
    }

    private void normalizeCard646(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).trim();String n=norm646(s);
            t.setMaxLines(3);t.setEllipsize(null);t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(false);
            if(isTitle646(n)){t.setTextSize(11.5f);t.setTextColor(Color.rgb(55,55,55));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}
            else if(isNumeric646(s)){t.setTextSize(25f);t.setTextColor(Color.rgb(24,24,24));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setSingleLine(false);}
            else{t.setTextSize(10.5f);t.setTextColor(Color.rgb(95,95,95));}
            return;
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)normalizeCard646(g.getChildAt(i));}
    }

    private void styleHeadings646(LinearLayout box){
        for(int i=0;i<box.getChildCount();i++)if(box.getChildAt(i) instanceof TextView){TextView t=(TextView)box.getChildAt(i);String n=norm646(String.valueOf(t.getText()));if(n.startsWith("GENEL DURUM")||n.startsWith("FİNANS")){t.setTextSize(13f);t.setTextColor(Color.rgb(30,30,30));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setLineSpacing(dp(1),1f);}}
    }

    private void removeDelta646(View v){
        if(v instanceof TextView){TextView t=(TextView)v;String n=norm646(String.valueOf(t.getText()));if(n.contains("ÇİFT YÖNLÜ DELTA")||n.contains("TOPLU SNAPSHOT KAPALI")){t.setVisibility(View.GONE);ViewGroup.LayoutParams lp=t.getLayoutParams();if(lp!=null){lp.height=0;t.setLayoutParams(lp);}return;}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)removeDelta646(g.getChildAt(i));}
    }

    private int findSection646(LinearLayout box,String s){for(int i=0;i<box.getChildCount();i++)if(box.getChildAt(i) instanceof TextView&&norm646(String.valueOf(((TextView)box.getChildAt(i)).getText())).startsWith(norm646(s)))return i;return -1;}
    private int findNextSection646(LinearLayout box,int from){for(int i=from;i<box.getChildCount();i++)if(box.getChildAt(i) instanceof TextView){String n=norm646(String.valueOf(((TextView)box.getChildAt(i)).getText()));if(n.startsWith("GENEL DURUM")||n.startsWith("FİNANS")||n.startsWith("SPORCU HAREKETLERİ")||n.startsWith("TAKİP GEREKTİRENLER"))return i;}return box.getChildCount();}
    private View findTopCard646(LinearLayout box,int start,int end,String needle){for(int i=start;i<Math.min(end,box.getChildCount());i++){View top=box.getChildAt(i);if(findText646(top,needle)!=null)return top;}return null;}
    private TextView findText646(View v,String needle){if(v instanceof TextView&&norm646(String.valueOf(((TextView)v).getText())).contains(norm646(needle)))return (TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText646(g.getChildAt(i),needle);if(r!=null)return r;}}return null;}
    private boolean isTitle646(String n){return n.contains("AKTİF SPORCU")||n.contains("ARA VERDİ")||n.contains("AYLIK HEDEF")||n.contains("GECİKMİŞ")||n.contains("MALZEME BORCU")||n.contains("ÖDENMEMİŞ MALZEME");}
    private boolean isNumeric646(String s){String x=s.replace("₺","").replace("TL","").replace(".","").replace(",","").replace(" ","").replace("+","").replace("-","");return x.matches("\\d+");}
    private ScrollView findScroll646(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll646(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm646(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
