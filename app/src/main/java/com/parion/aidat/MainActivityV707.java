package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.1.29 - robust absentee engine: last 4 confirmed sessions, athlete-active dates only. */
public class MainActivityV707 extends MainActivityV706 {
    static class Ab707 { long id; String name, group; ArrayList<String> dates=new ArrayList<>(); }

    @Override void showHome(){
        super.showHome();
        if(root==null||!"HOME".equals(page))return;
        root.post(this::replaceAbsenteeCard707);
    }

    @Override void goBack(){
        if("ABSENTEES_707".equals(page)){showHome();return;}
        super.goBack();
    }

    private void replaceAbsenteeCard707(){
        LinearLayout box=findHomeBox707();if(box==null)return;
        for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(containsText707(v,"DEVAMSIZLAR"))box.removeViewAt(i);}
        ArrayList<Ab707> list=absentees707();
        LinearLayout card=new LinearLayout(this);card.setTag("v707-absentees");card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(12),dp(12),dp(12));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),Color.rgb(196,63,63));card.setBackground(bg);card.setElevation(dp(2));
        TextView n=new TextView(this);n.setText(String.valueOf(list.size()));n.setTextSize(28f);n.setTextColor(Color.rgb(196,63,63));n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);n.setGravity(Gravity.CENTER);card.addView(n);
        TextView t=new TextView(this);t.setText("DEVAMSIZLAR");t.setTextSize(11.5f);t.setTextColor(Color.rgb(45,45,45));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER);card.addView(t);
        TextView s=new TextView(this);s.setText("Son 4 onaylı antrenmanın tamamına gelmeyenler");s.setTextSize(9.5f);s.setTextColor(Color.rgb(105,105,105));s.setGravity(Gravity.CENTER);card.addView(s);
        card.setClickable(true);card.setOnClickListener(v->showAbsentees707());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(116));lp.setMargins(dp(4),dp(10),dp(4),dp(8));box.addView(card,lp);
    }

    private ArrayList<Ab707> absentees707(){
        ArrayList<Ab707> out=new ArrayList<>();Cursor a=null;
        try{
            a=db.getReadableDatabase().rawQuery("SELECT id,name,category,startDate,endDate,restartDate FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' AND TRIM(COALESCE(category,''))<>'' ORDER BY name COLLATE NOCASE",null);
            while(a.moveToNext()){
                long id=a.getLong(0);String name=val707(a,1),group=val707(a,2),start=val707(a,3),end=val707(a,4),restart=val707(a,5);
                ArrayList<Long> sess=new ArrayList<>();ArrayList<String> dates=new ArrayList<>();Cursor s=null;
                try{
                    s=db.getReadableDatabase().rawQuery("SELECT id,sessionDate FROM attendance_sessions WHERE groupName=? COLLATE NOCASE AND confirmed=1 AND cancelled=0 AND sessionDate<=date('now','localtime') ORDER BY sessionDate DESC,id DESC",new String[]{group});
                    while(s.moveToNext()&&sess.size()<4){String d=val707(s,1);if(activeOn707(d,start,end,restart)){sess.add(s.getLong(0));dates.add(d);}}
                }finally{if(s!=null)s.close();}
                if(sess.size()<4)continue;
                boolean allAbsent=true;
                for(long sid:sess){Cursor r=null;try{r=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=? LIMIT 1",new String[]{String.valueOf(sid),String.valueOf(id)});if(!r.moveToFirst()||r.getInt(0)!=0){allAbsent=false;break;}}finally{if(r!=null)r.close();}}
                if(allAbsent){Ab707 x=new Ab707();x.id=id;x.name=name;x.group=group;x.dates.addAll(dates);out.add(x);}
            }
        }catch(Exception ignored){}finally{if(a!=null)a.close();}
        return out;
    }

    private boolean activeOn707(String date,String start,String end,String restart){
        if(date==null||date.length()<10)return false;
        if(start!=null&&!start.isEmpty()&&date.compareTo(start)<0)return false;
        if(end==null||end.isEmpty()||"DEVAM".equalsIgnoreCase(end))return true;
        if(date.compareTo(end)<=0)return true;
        return restart!=null&&!restart.isEmpty()&&date.compareTo(restart)>=0;
    }

    private void showAbsentees707(){
        page="ABSENTEES_707";base("DEVAMSIZLAR",true);ScrollView sv=scroll();LinearLayout list=box(sv);
        TextView info=new TextView(this);info.setText("Sporcunun aktif olduğu tarihlerde, bugüne kadar onaylanmış ve iptal edilmemiş son 4 antrenmanın dördünde de 'gelmedi' olarak işaretlenen aktif sporcular. Onaysız yoklamalar hesaba katılmaz.");info.setTextSize(11.5f);info.setTextColor(Color.DKGRAY);info.setPadding(dp(6),dp(4),dp(6),dp(14));list.addView(info);
        ArrayList<Ab707> rows=absentees707();
        for(Ab707 x:rows){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(11),dp(12),dp(11));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(12));row.setBackground(bg);row.setElevation(dp(1));
            TextView nm=new TextView(this);nm.setText(x.name);nm.setTextSize(14f);nm.setTextColor(Color.rgb(30,30,30));nm.setTypeface(Typeface.DEFAULT,Typeface.BOLD);row.addView(nm);
            TextView sub=new TextView(this);sub.setText(x.group+" • Üst üste 4 antrenman yok");sub.setTextSize(10.5f);sub.setTextColor(Color.rgb(196,63,63));row.addView(sub);
            TextView ds=new TextView(this);ds.setText("Yoklama tarihleri: "+joinDates707(x.dates));ds.setTextSize(9.5f);ds.setTextColor(Color.GRAY);row.addView(ds);
            row.setOnClickListener(v->showProfile(x.id));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));list.addView(row,lp);
        }
        if(rows.isEmpty()){TextView e=new TextView(this);e.setText("Üst üste 4 onaylı antrenmana gelmeyen aktif sporcu yok.");e.setTextSize(13f);e.setTextColor(Color.DKGRAY);e.setGravity(Gravity.CENTER);e.setPadding(dp(10),dp(28),dp(10),dp(28));list.addView(e);}
    }

    private String joinDates707(ArrayList<String> x){ArrayList<String> out=new ArrayList<>();for(String d:x)out.add(dateTr(d));return android.text.TextUtils.join(" • ",out);}
    private String val707(Cursor c,int i){String x=c.getString(i);return x==null?"":x;}
    private LinearLayout findHomeBox707(){if(root==null)return null;ScrollView sv=findScroll707(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return null;return(LinearLayout)sv.getChildAt(0);}
    private ScrollView findScroll707(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll707(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private boolean containsText707(View v,String needle){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR")).contains(needle))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsText707(g.getChildAt(i),needle))return true;}return false;}
}
