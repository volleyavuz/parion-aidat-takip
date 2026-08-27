package com.parion.aidat;

import android.app.*;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.1.28 - payment DatePicker + absentee card based on 4 consecutive missed confirmed sessions. */
public class MainActivityV706 extends MainActivityV705 {

    @Override void showHome(){
        super.showHome();
        if(root==null||!"HOME".equals(page))return;
        root.post(this::replaceAbsenteeCard706);
    }

    @Override void goBack(){
        if("ABSENTEES_706".equals(page)){showHome();return;}
        super.goBack();
    }

    @Override void editPayment(long id,int month,int fee,String marker,int amount){
        final String[] opts={"ÖDEME GİR","ARA VERDİ (X)","FARKLI TUTAR (!)","KAYDI TEMİZLE"};
        new AlertDialog.Builder(this).setTitle(monthName(month)+" 2026").setItems(opts,(d,w)->{
            if(w==1){db.updatePayment(id,month,"X",0);deleteCollectionDate706(id,month);showProfile(id);return;}
            if(w==3){db.updatePayment(id,month,"",0);deleteCollectionDate706(id,month);showProfile(id);return;}

            LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(20),0,dp(20),0);
            EditText date=new EditText(this);date.setHint("Ödeme tarihi");date.setFocusable(false);date.setClickable(true);
            date.setText(isDate(marker)?dateTr(marker):new SimpleDateFormat("dd.MM.yyyy",TR).format(new Date()));
            date.setOnClickListener(v->openDatePicker706(date));
            EditText am=new EditText(this);am.setHint("Tutar");am.setInputType(2);am.setText(String.valueOf(amount>0?amount:fee));
            x.addView(date);x.addView(am);
            new AlertDialog.Builder(this).setTitle(w==2?"Farklı tutar":"Aidat ödemesi").setView(x)
                .setPositiveButton("KAYDET",(a,z)->{
                    String iso=toIso(date.getText().toString());int val=parseInt(am.getText().toString());
                    db.updatePayment(id,month,w==2?"!":iso,val);
                    if(val>0&&isDate(iso))saveCollectionDate706(id,month,iso);else deleteCollectionDate706(id,month);
                    showProfile(id);
                }).setNegativeButton("İPTAL",null).show();
        }).show();
    }

    private void openDatePicker706(EditText target){
        Calendar c=Calendar.getInstance();
        try{Date d=new SimpleDateFormat("dd.MM.yyyy",TR).parse(target.getText().toString());if(d!=null)c.setTime(d);}catch(Exception ignored){}
        DatePickerDialog dlg=new DatePickerDialog(this,(v,y,m,d)->target.setText(String.format(new Locale("tr","TR"),"%02d.%02d.%04d",d,m+1,y)),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private void ensureCollectionDate706(){try{db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS payment_collection_dates(athleteId INTEGER NOT NULL,year INTEGER NOT NULL,month INTEGER NOT NULL,paidDate TEXT NOT NULL,PRIMARY KEY(athleteId,year,month))");}catch(Exception ignored){}}
    private void saveCollectionDate706(long athleteId,int month,String paidDate){try{ensureCollectionDate706();ContentValues v=new ContentValues();v.put("athleteId",athleteId);v.put("year",2026);v.put("month",month);v.put("paidDate",paidDate);db.getWritableDatabase().insertWithOnConflict("payment_collection_dates",null,v,SQLiteDatabase.CONFLICT_REPLACE);}catch(Exception ignored){}}
    private void deleteCollectionDate706(long athleteId,int month){try{ensureCollectionDate706();db.getWritableDatabase().delete("payment_collection_dates","athleteId=? AND year=2026 AND month=?",new String[]{String.valueOf(athleteId),String.valueOf(month)});}catch(Exception ignored){}}

    private void replaceAbsenteeCard706(){
        LinearLayout box=findHomeBox706();if(box==null)return;
        for(int i=box.getChildCount()-1;i>=0;i--){View v=box.getChildAt(i);if(containsText706(v,"DEVAMSIZLAR"))box.removeViewAt(i);}
        int count=countAbsentees706();
        LinearLayout card=new LinearLayout(this);card.setTag("v706-absentees");card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(12),dp(12),dp(12));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),Color.rgb(196,63,63));card.setBackground(bg);card.setElevation(dp(2));
        TextView n=new TextView(this);n.setText(String.valueOf(count));n.setTextSize(28f);n.setTextColor(Color.rgb(196,63,63));n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);n.setGravity(Gravity.CENTER);card.addView(n);
        TextView t=new TextView(this);t.setText("DEVAMSIZLAR");t.setTextSize(11.5f);t.setTextColor(Color.rgb(45,45,45));t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER);card.addView(t);
        TextView s=new TextView(this);s.setText("Son 4 antrenmanın tamamına gelmeyenler");s.setTextSize(9.5f);s.setTextColor(Color.rgb(105,105,105));s.setGravity(Gravity.CENTER);card.addView(s);
        card.setClickable(true);card.setOnClickListener(v->showAbsentees706());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(116));lp.setMargins(dp(4),dp(10),dp(4),dp(8));box.addView(card,lp);
    }

    private LinearLayout findHomeBox706(){
        if(root==null)return null;ScrollView sv=findScroll706(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return null;return(LinearLayout)sv.getChildAt(0);
    }
    private ScrollView findScroll706(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll706(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private boolean containsText706(View v,String needle){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR")).contains(needle))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsText706(g.getChildAt(i),needle))return true;}return false;}

    private String absenteeSql706(){
        String last4="SELECT s2.id FROM attendance_sessions s2 WHERE s2.groupName=a.category COLLATE NOCASE AND s2.confirmed=1 AND s2.cancelled=0 AND s2.sessionDate<=date('now','localtime') ORDER BY s2.sessionDate DESC,s2.id DESC LIMIT 4";
        return "SELECT a.id,a.name,a.category FROM athletes a WHERE a.status='AKTİF' AND TRIM(COALESCE(a.deletedAt,''))='' " +
            "AND (SELECT COUNT(*) FROM attendance_sessions sx WHERE sx.groupName=a.category COLLATE NOCASE AND sx.confirmed=1 AND sx.cancelled=0 AND sx.sessionDate<=date('now','localtime'))>=4 " +
            "AND (SELECT COUNT(*) FROM attendance_records r JOIN attendance_sessions s ON s.id=r.sessionId WHERE r.athleteId=a.id AND r.present=0 AND s.id IN ("+last4+"))=4 " +
            "ORDER BY a.name COLLATE NOCASE";
    }
    private Cursor absentees706(){try{return db.getReadableDatabase().rawQuery(absenteeSql706(),null);}catch(Exception e){return db.getReadableDatabase().rawQuery("SELECT id,name,category FROM athletes WHERE 1=0",null);}}
    private int countAbsentees706(){Cursor c=null;int n=0;try{c=absentees706();while(c.moveToNext())n++;}finally{if(c!=null)c.close();}return n;}

    private void showAbsentees706(){
        page="ABSENTEES_706";base("DEVAMSIZLAR",true);ScrollView sv=scroll();LinearLayout list=box(sv);
        TextView info=new TextView(this);info.setText("Bugüne kadar onaylanmış ve iptal edilmemiş son 4 antrenmanın dördünde de yok yazılan aktif sporcular.");info.setTextSize(11.5f);info.setTextColor(Color.DKGRAY);info.setPadding(dp(6),dp(4),dp(6),dp(14));list.addView(info);
        Cursor c=null;int n=0;try{c=absentees706();while(c.moveToNext()){
            long id=c.getLong(0);String name=c.getString(1);String group=c.getString(2)==null?"":c.getString(2);
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(11),dp(12),dp(11));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(12));row.setBackground(bg);row.setElevation(dp(1));
            TextView nm=new TextView(this);nm.setText(name);nm.setTextSize(14f);nm.setTextColor(Color.rgb(30,30,30));nm.setTypeface(Typeface.DEFAULT,Typeface.BOLD);row.addView(nm);
            TextView sub=new TextView(this);sub.setText(group+" • Üst üste 4 antrenman yok");sub.setTextSize(10.5f);sub.setTextColor(Color.rgb(196,63,63));row.addView(sub);row.setOnClickListener(v->showProfile(id));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));list.addView(row,lp);n++;
        }}finally{if(c!=null)c.close();}
        if(n==0){TextView e=new TextView(this);e.setText("Üst üste 4 antrenmana gelmeyen aktif sporcu yok.");e.setTextSize(13f);e.setTextColor(Color.DKGRAY);e.setGravity(Gravity.CENTER);e.setPadding(dp(10),dp(28),dp(10),dp(28));list.addView(e);}
    }
}
