package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.22 - active-only tshirt zero list and readable tshirt card subtitle. */
public class MainActivityV622 extends MainActivityV621 {
    private static final int GOLD622=Color.rgb(205,156,34);
    private static final int TEXT622=Color.rgb(28,28,28);
    private static final int MUTED622=Color.rgb(72,72,72);

    @Override void showHome(){
        super.showHome();
        root.post(this::patch622);
    }

    private void patch622(){
        View card=findTag622(root,"v621-tshirt-card");
        if(!(card instanceof ViewGroup)) return;
        ViewGroup g=(ViewGroup)card;
        int activeZero=countActiveNoTshirt622();
        ArrayList<TextView> texts=new ArrayList<>();collectText622(g,texts);
        TextView number=null, subtitle=null;
        for(TextView t:texts){
            String s=String.valueOf(t.getText()).trim();
            if(s.matches("[0-9]+")){number=t;}
            if(norm622(s).contains("TİŞÖRT SAYISI")){subtitle=t;}
        }
        if(number!=null){number.setText(String.valueOf(activeZero));number.setTextSize(28f);number.setTextColor(GOLD622);number.setTypeface(Typeface.DEFAULT,Typeface.BOLD);}
        if(subtitle!=null){subtitle.setText("AKTİF • Tişört adedi: 0");subtitle.setTextSize(11.5f);subtitle.setTextColor(TEXT622);subtitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD);subtitle.setMaxLines(2);subtitle.setGravity(Gravity.CENTER);subtitle.setPadding(dp(4),dp(3),dp(4),dp(3));}
        card.setOnClickListener(v->showActiveNoTshirt622());
        card.setClickable(true);
    }

    private int countActiveNoTshirt622(){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status='AKTİF' AND COALESCE(tshirtQty,0)=0 AND TRIM(COALESCE(deletedAt,''))=''",null);
        int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;
    }

    private void showActiveNoTshirt622(){
        page="ACTIVE_NO_TSHIRT_622";base("TİŞÖRT ALMAYAN AKTİF SPORCULAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear,status FROM athletes WHERE status='AKTİF' AND COALESCE(tshirtQty,0)=0 AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){
            final long id=c.getLong(0);String name=c.getString(1);int by=c.getInt(2);
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));row.setBackground(round622(Color.WHITE,GOLD622,12,1));row.setClickable(true);row.setOnClickListener(v->showProfile(id));
            TextView a=text622(name,14f,TEXT622,true);a.setGravity(Gravity.START);row.addView(a);
            TextView d=text622((by>0?by+" • ":"")+"AKTİF • Tişört adedi: 0",11f,MUTED622,true);d.setGravity(Gravity.START);row.addView(d);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);n++;
        }
        c.close();
        if(n==0)b.addView(text622("Tişört adedi 0 olan aktif sporcu bulunmuyor.",13f,MUTED622,true));
    }

    @Override void goBack(){if("ACTIVE_NO_TSHIRT_622".equals(page)){showHome();return;}super.goBack();}

    private TextView text622(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private void collectText622(View v,List<TextView> out){if(v instanceof TextView)out.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectText622(g.getChildAt(i),out);}}
    private View findTag622(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag622(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private String norm622(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
    private GradientDrawable round622(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}
}
