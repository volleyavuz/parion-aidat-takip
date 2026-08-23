package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.19 - professional dashboard hierarchy and compact cohort previews. */
public class MainActivityV619 extends MainActivityV618 {
    private static final int GOLD=Color.rgb(205,156,34);
    private static final int GOLD_BG=Color.rgb(251,246,226);
    private static final int GREEN=Color.rgb(39,134,82);
    private static final int RED=Color.rgb(196,63,63);
    private static final int BLUE=Color.rgb(72,103,132);
    private static final int TEXT=Color.rgb(34,34,34);
    private static final int MUTED=Color.rgb(112,112,112);

    @Override void showHome(){
        super.showHome();
        root.post(this::polish619);
    }

    private void polish619(){
        ScrollView sv=findScroll619(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        box.setBackgroundColor(GOLD_BG);box.setPadding(dp(12),dp(10),dp(12),dp(28));
        compactHeader619(root);
        polishSections619(box);
        polishAthletes619(box);
        emphasizeTarget619(box);
        simplifyPalette619(box);
        compactCohortLists619(box);
        polishNetMetric619(box);
        normalizeSpacing619(box);
    }

    private void compactHeader619(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String u=norm619(String.valueOf(t.getText()));
            if(u.contains("PARİON SPORCU TAKİP UYGULAMASI")||u.contains("PARION SPORCU TAKİP UYGULAMASI")){
                String s="PARİON\nSporcu Takip Uygulaması";SpannableString sp=new SpannableString(s);
                sp.setSpan(new RelativeSizeSpan(1.22f),0,6,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sp.setSpan(new RelativeSizeSpan(.78f),7,s.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                t.setText(sp);t.setTextSize(14f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setTextColor(TEXT);t.setMaxLines(2);t.setLineSpacing(0,.94f);
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)compactHeader619(g.getChildAt(i));}
    }

    private void polishSections619(View v){
        if(v instanceof TextView){
            TextView t=(TextView)v;String u=norm619(String.valueOf(t.getText()));
            if(u.startsWith("GENEL DURUM")||u.startsWith("FİNANS")||u.startsWith("TAKİP GEREKTİRENLER")){
                t.setTextSize(13f);t.setTextColor(TEXT);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(dp(4),dp(20),dp(4),dp(7));
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)polishSections619(g.getChildAt(i));}
    }

    private void polishAthletes619(LinearLayout box){
        TextView label=findText619(box,"SPORCULAR");if(label==null)return;View card=card619(label);if(card==null)return;
        card.setBackground(round619(Color.WHITE,GOLD,16,1));card.setPadding(dp(16),dp(10),dp(16),dp(10));
        if(label instanceof Button){Button b=(Button)label;b.setText("Sporcular                                      ›");b.setAllCaps(false);b.setTextSize(15f);b.setTextColor(TEXT);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}
        else {label.setText("Sporcular   ›");label.setTextSize(15f);label.setTextColor(TEXT);label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}
    }

    private void emphasizeTarget619(LinearLayout box){
        TextView label=findText619(box,"AYLIK HEDEF");if(label==null)return;View card=card619(label);if(card==null)return;
        card.setBackground(round619(Color.WHITE,GOLD,18,2));card.setPadding(dp(14),dp(12),dp(14),dp(12));
        label.setTextSize(11f);label.setTextColor(MUTED);label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        ArrayList<TextView> all=new ArrayList<>();collectText619(card,all);
        for(TextView t:all){String s=String.valueOf(t.getText()).trim();if(s.contains("₺")&&s.replaceAll("[^0-9]","").length()>0){t.setTextSize(31f);t.setTextColor(TEXT);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}if(norm619(s).contains("AKTİF SPORCU")){t.setTextSize(10f);t.setTextColor(GOLD);}}
    }

    private void simplifyPalette619(LinearLayout box){
        recolorCard619(box,"KAYIT FORMU EKSİK",BLUE);
        recolorCard619(box,"FOTOĞRAF EKSİK",BLUE);
        recolorCard619(box,"KIŞIN ARANACAK",BLUE);
        recolorCard619(box,"YAZIN ARANACAK",GOLD);
        recolorCard619(box,"MALZEME BORCU",GOLD);
        recolorCard619(box,"GECİKMİŞ",RED);
        recolorCard619(box,"AKTİF SPORCU",GREEN);
    }
    private void recolorCard619(View root,String needle,int accent){TextView t=findText619(root,needle);if(t==null)return;View c=card619(t);if(c==null)return;c.setBackground(round619(Color.WHITE,accent,16,1));}

    private void compactCohortLists619(LinearLayout box){
        compactList619(box,"BU AY BAŞLAYAN SPORCULAR");
        compactList619(box,"GEÇEN AY BAŞLAYAN SPORCULAR");
        compactList619(box,"SON 3 AY İÇİNDE BIRAKANLAR");
    }
    private void compactList619(View root,String title){
        TextView h=findText619(root,title);if(h==null||!(h.getParent() instanceof LinearLayout))return;LinearLayout card=(LinearLayout)h.getParent();
        if(findTag619(card,"v619-more")!=null)return;
        ArrayList<View> people=new ArrayList<>();for(int i=0;i<card.getChildCount();i++){View x=card.getChildAt(i);if(x==h||"v619-more".equals(x.getTag()))continue;if(x instanceof TextView){String s=String.valueOf(((TextView)x).getText());if(s.startsWith("• "))people.add(x);}}
        if(people.size()<=3)return;for(int i=3;i<people.size();i++)people.get(i).setVisibility(View.GONE);
        TextView more=new TextView(this);more.setTag("v619-more");more.setText("Tümünü Gör ("+people.size()+")  ›");more.setTextSize(10f);more.setTextColor(GOLD);more.setTypeface(Typeface.DEFAULT,Typeface.BOLD);more.setGravity(Gravity.CENTER);more.setPadding(dp(4),dp(8),dp(4),dp(4));
        more.setOnClickListener(v->{boolean expand=people.get(3).getVisibility()!=View.VISIBLE;for(int i=3;i<people.size();i++)people.get(i).setVisibility(expand?View.VISIBLE:View.GONE);more.setText(expand?"Daralt  ‹":"Tümünü Gör ("+people.size()+")  ›");});card.addView(more);
    }

    private void polishNetMetric619(LinearLayout box){
        TextView title=findText619(box,"SON 3 AYDA");if(title==null||!(title.getParent() instanceof ViewGroup))return;ViewGroup card=(ViewGroup)title.getParent();ArrayList<TextView> a=new ArrayList<>();collectText619(card,a);
        for(TextView t:a){String s=String.valueOf(t.getText()).trim();if(s.matches("[+-]?[0-9]+")){int n=0;try{n=Integer.parseInt(s);}catch(Exception ignored){}t.setText((n>0?"+":"")+n+" Net Sporcu");t.setTextSize(24f);t.setTextColor(n>0?GREEN:n<0?RED:GOLD);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}else if(norm619(s).startsWith("SON 3 AYDA")){t.setText("Son 3 ayda");t.setTextSize(10.5f);}}
    }

    private void normalizeSpacing619(LinearLayout box){for(int i=0;i<box.getChildCount();i++){View v=box.getChildAt(i);ViewGroup.LayoutParams p=v.getLayoutParams();if(p instanceof LinearLayout.LayoutParams){LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)p;if(lp.topMargin<dp(3))lp.topMargin=dp(3);if(lp.bottomMargin<dp(3))lp.bottomMargin=dp(3);v.setLayoutParams(lp);}}}

    private View card619(View v){View cur=v,best=v;while(cur!=null&&cur!=root){if(cur.hasOnClickListeners()||cur.isClickable())best=cur;ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;}return best;}
    private TextView findText619(View v,String needle){if(v instanceof TextView&&norm619(String.valueOf(((TextView)v).getText())).contains(norm619(needle)))return (TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText619(g.getChildAt(i),needle);if(r!=null)return r;}}return null;}
    private View findTag619(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag619(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private void collectText619(View v,List<TextView> out){if(v instanceof TextView)out.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectText619(g.getChildAt(i),out);}}
    private ScrollView findScroll619(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll619(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm619(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
    private GradientDrawable round619(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}
}
