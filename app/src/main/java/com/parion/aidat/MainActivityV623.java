package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.23 - calmer, denser professional dashboard hierarchy. */
public class MainActivityV623 extends MainActivityV622 {
    private static final int GOLD=Color.rgb(205,156,34);
    private static final int GOLD_BG=Color.rgb(252,248,235);
    private static final int TEXT=Color.rgb(28,28,28);
    private static final int MUTED=Color.rgb(92,92,92);
    private static final int BLUE=Color.rgb(72,103,132);

    @Override void showHome(){super.showHome();root.post(this::polish623);}

    private void polish623(){
        ScrollView sv=findScroll623(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        box.setBackgroundColor(GOLD_BG);box.setPadding(dp(12),dp(8),dp(12),dp(30));
        header623(root);sections623(root);normalizeCards623(root);tshirt623(box);deleted623(box);spacing623(box);
    }

    private void header623(View v){
        if(v instanceof TextView){TextView t=(TextView)v;String u=norm623(String.valueOf(t.getText()));if(u.contains("PARİON SPORCU TAKİP UYGULAMASI")||u.contains("PARION SPORCU TAKİP UYGULAMASI")){
            t.setText("PARİON\nSPORCU TAKİP UYGULAMASI");t.setTextSize(15f);t.setTextColor(TEXT);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);t.setMaxLines(2);t.setLineSpacing(0,.92f);t.setPadding(dp(10),dp(5),dp(10),dp(5));
            if(t.getParent() instanceof View){View p=(View)t.getParent();p.setBackground(round623(Color.WHITE,GOLD,14,1));p.setPadding(dp(4),dp(3),dp(4),dp(3));}
        }}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)header623(g.getChildAt(i));}
    }

    private void sections623(View v){if(v instanceof TextView){TextView t=(TextView)v;String u=norm623(String.valueOf(t.getText()));if(u.startsWith("GENEL DURUM")||u.startsWith("FİNANS")||u.startsWith("TAKİP GEREKTİRENLER")){t.setTextSize(12.5f);t.setTextColor(TEXT);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(dp(3),dp(16),dp(3),dp(6));}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)sections623(g.getChildAt(i));}}

    private void normalizeCards623(View v){
        if(v instanceof TextView){TextView t=(TextView)v;String u=norm623(String.valueOf(t.getText()));
            if(isCardTitle623(u)){t.setTextSize(10f);t.setTextColor(TEXT);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setMaxLines(2);t.setGravity(Gravity.CENTER);}
            if(u.equals("SPORCULAR")||u.startsWith("SPORCULAR ")){t.setTextSize(14.5f);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)normalizeCards623(g.getChildAt(i));}
    }
    private boolean isCardTitle623(String u){return u.contains("AKTİF SPORCU")||u.contains("ARA VERDİ")||u.contains("AYLIK HEDEF")||u.contains("GECİKMİŞ")||u.contains("MALZEME BORCU")||u.contains("FOTOĞRAF EKSİK")||u.contains("KAYIT FORMU EKSİK")||u.contains("YAZIN ARANACAK")||u.contains("KIŞIN ARANACAK")||u.contains("BU AY BAŞLAYAN")||u.contains("GEÇEN AY BAŞLAYAN")||u.contains("ÖDEME YAPACAK")||u.contains("SON 3 AY İÇİNDE BIRAKAN")||u.equals("SON 3 AYDA")||u.contains("TİŞÖRT ALMAYAN");}

    private void tshirt623(LinearLayout box){
        View card=findTag623(box,"v621-tshirt-card");if(!(card instanceof ViewGroup))return;
        card.setBackground(round623(Color.WHITE,BLUE,16,1));card.setPadding(dp(10),dp(8),dp(10),dp(8));
        ArrayList<TextView> ts=new ArrayList<>();collect623(card,ts);for(TextView t:ts){String u=norm623(String.valueOf(t.getText()));if(u.contains("TİŞÖRT ALMAYAN")){t.setText("TİŞÖRT ALMAYAN\nAKTİF SPORCULAR");t.setTextSize(10f);t.setTextColor(TEXT);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setMaxLines(2);}else if(u.contains("TİŞÖRT ADEDİ")){t.setText("Tişört adedi 0 • Aktif");t.setTextSize(10.5f);t.setTextColor(MUTED);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}}
        ViewParent p=card.getParent();if(p instanceof LinearLayout){LinearLayout parent=(LinearLayout)p;ViewGroup.LayoutParams old=card.getLayoutParams();if(old instanceof LinearLayout.LayoutParams){LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)old;lp.height=dp(108);lp.setMargins(dp(3),dp(4),dp(3),dp(6));card.setLayoutParams(lp);}}
    }

    private void deleted623(LinearLayout box){TextView t=findText623(box,"SİLİNEN SPORCULAR");if(t==null)return;View c=card623(t);if(c==null)c=t;c.setAlpha(.82f);t.setTextSize(10.5f);t.setTextColor(MUTED);}

    private void spacing623(LinearLayout box){for(int i=0;i<box.getChildCount();i++){View v=box.getChildAt(i);ViewGroup.LayoutParams p=v.getLayoutParams();if(p instanceof LinearLayout.LayoutParams){LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)p;lp.topMargin=Math.max(lp.topMargin,dp(3));lp.bottomMargin=Math.max(lp.bottomMargin,dp(3));v.setLayoutParams(lp);}}}

    private View card623(View v){View cur=v,best=v;while(cur!=null&&cur!=root){if(cur.hasOnClickListeners()||cur.isClickable())best=cur;ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;}return best;}
    private TextView findText623(View v,String needle){if(v instanceof TextView&&norm623(String.valueOf(((TextView)v).getText())).contains(norm623(needle)))return (TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText623(g.getChildAt(i),needle);if(r!=null)return r;}}return null;}
    private View findTag623(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag623(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private void collect623(View v,List<TextView> out){if(v instanceof TextView)out.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collect623(g.getChildAt(i),out);}}
    private ScrollView findScroll623(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll623(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private String norm623(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
    private GradientDrawable round623(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}
}
