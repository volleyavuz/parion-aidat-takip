package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.1.26 - recent payments dashboard card. */
public class MainActivityV704 extends MainActivityV703 {
    @Override void showHome(){
        super.showHome();
        if(root==null||page==null||!"HOME".equalsIgnoreCase(page))return;
        LinearLayout fresh=findFresh704(root);
        if(fresh==null)return;
        removeOldRecent704(fresh);
        View card=recentCard704();
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(dp(4),0,dp(4),dp(9));
        fresh.addView(card,lp);
    }

    private LinearLayout findFresh704(View v){
        if(v instanceof LinearLayout&&"v657-fresh".equals(v.getTag()))return(LinearLayout)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){LinearLayout x=findFresh704(g.getChildAt(i));if(x!=null)return x;}}
        return null;
    }
    private void removeOldRecent704(LinearLayout fresh){for(int i=fresh.getChildCount()-1;i>=0;i--){View v=fresh.getChildAt(i);if("v704-recent".equals(v.getTag()))fresh.removeViewAt(i);}}

    private View recentCard704(){
        LinearLayout card=new LinearLayout(this);card.setTag("v704-recent");card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(13),dp(12),dp(13),dp(10));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));card.setBackground(bg);card.setElevation(dp(2));

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=new TextView(this);title.setText("SON ÖDEMELER");title.setTextSize(12f);title.setTextColor(Color.rgb(45,45,45));title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);head.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView more=new TextView(this);more.setText("SON 20 ›");more.setTextSize(10.5f);more.setTextColor(Color.rgb(205,156,34));more.setTypeface(Typeface.DEFAULT,Typeface.BOLD);head.addView(more);card.addView(head);

        Cursor c=null;int n=0;
        try{
            c=db.recentPayments(3);
            while(c.moveToNext()){
                String name=c.getString(c.getColumnIndexOrThrow("name"));int amount=c.getInt(c.getColumnIndexOrThrow("amount"));
                card.addView(paymentLine704(name,amount,n));n++;
            }
        }catch(Exception ignored){}finally{if(c!=null)c.close();}
        if(n==0){TextView empty=new TextView(this);empty.setText("Bu sürümde henüz yeni ödeme kaydedilmedi.");empty.setTextSize(11f);empty.setTextColor(Color.rgb(115,115,115));empty.setPadding(0,dp(13),0,dp(10));card.addView(empty);}
        card.setClickable(true);card.setOnClickListener(v->showRecentPayments704());return card;
    }

    private View paymentLine704(String name,int amount,int index){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(9),0,dp(9));
        if(index>0){GradientDrawable line=new GradientDrawable();line.setColor(Color.rgb(238,238,238));row.setBackground(line);}
        TextView nm=new TextView(this);nm.setText(name);nm.setTextSize(13.5f);nm.setTextColor(Color.rgb(35,35,35));nm.setTypeface(Typeface.DEFAULT,Typeface.BOLD);nm.setSingleLine(true);row.addView(nm,new LinearLayout.LayoutParams(0,-2,1));
        TextView am=new TextView(this);am.setText(money704(amount));am.setTextSize(13.5f);am.setTextColor(Color.rgb(39,134,82));am.setTypeface(Typeface.DEFAULT,Typeface.BOLD);am.setGravity(Gravity.END);row.addView(am,new LinearLayout.LayoutParams(-2,-2));return row;
    }

    private void showRecentPayments704(){
        page="RECENT_PAYMENTS";base("SON ÖDEMELER",true);ScrollView sv=scroll();LinearLayout box=box(sv);
        TextView info=new TextView(this);info.setText("Son 20 kayıt • Sıralama ödeme tarihine değil, uygulamada KAYDET'e basılan zamana göredir.");info.setTextSize(11.5f);info.setTextColor(Color.DKGRAY);info.setPadding(dp(6),dp(4),dp(6),dp(12));box.addView(info);
        Cursor c=null;int n=0;try{c=db.recentPayments(20);while(c.moveToNext()){
            String name=c.getString(c.getColumnIndexOrThrow("name"));int amount=c.getInt(c.getColumnIndexOrThrow("amount"));long saved=c.getLong(c.getColumnIndexOrThrow("savedAt"));long athleteId=c.getLong(c.getColumnIndexOrThrow("athleteId"));
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(12));row.setBackground(bg);row.setElevation(dp(1));
            LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView nm=new TextView(this);nm.setText(name);nm.setTextSize(14f);nm.setTextColor(Color.rgb(30,30,30));nm.setTypeface(Typeface.DEFAULT,Typeface.BOLD);top.addView(nm,new LinearLayout.LayoutParams(0,-2,1));TextView am=new TextView(this);am.setText(money704(amount));am.setTextSize(14f);am.setTextColor(Color.rgb(39,134,82));am.setTypeface(Typeface.DEFAULT,Typeface.BOLD);top.addView(am);row.addView(top);
            TextView when=new TextView(this);when.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm",new Locale("tr","TR")).format(new Date(saved)));when.setTextSize(10.5f);when.setTextColor(Color.GRAY);row.addView(when);row.setOnClickListener(v->showProfile(athleteId));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));box.addView(row,lp);n++;
        }}catch(Exception ignored){}finally{if(c!=null)c.close();}
        if(n==0){TextView e=new TextView(this);e.setText("Bu sürümde henüz yeni ödeme kaydedilmedi.");e.setTextSize(13f);e.setTextColor(Color.DKGRAY);e.setGravity(Gravity.CENTER);e.setPadding(dp(10),dp(28),dp(10),dp(28));box.addView(e);}
    }

    private String money704(long n){return String.format(new Locale("tr","TR"),"₺%,d",n).replace(',','.');}
}
