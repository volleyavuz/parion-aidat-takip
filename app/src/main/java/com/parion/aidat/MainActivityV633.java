package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.33 - previous-month attendance access, Jul/Aug one-time backfill, single-day delete. */
public class MainActivityV633 extends MainActivityV632 {
    private static final int TEXT=Color.rgb(28,28,28), MUTED=Color.rgb(92,92,92);
    private final SimpleDateFormat ISO=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    private final SimpleDateFormat FULL=new SimpleDateFormat("dd.MM.yyyy",new Locale("tr","TR"));
    private final SimpleDateFormat DAY=new SimpleDateFormat("dd",new Locale("tr","TR"));
    private final SimpleDateFormat MON=new SimpleDateFormat("MMM",new Locale("tr","TR"));
    private final SimpleDateFormat MONTH=new SimpleDateFormat("MMMM yyyy",new Locale("tr","TR"));

    @Override public void onCreate(android.os.Bundle b){super.onCreate(b);backfillOnce633();}

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->{patchGroupButtons633(root);patchHistoryButton633(root);});
    }

    private void patchGroupButtons633(View v){
        if(!"ATTENDANCE_GROUPS_631".equals(page))return;
        if(v instanceof Button){Button b=(Button)v;String s=String.valueOf(b.getText()).trim();if(!s.isEmpty()&&!s.toUpperCase(new Locale("tr","TR")).contains("DIŞA AKTAR"))b.setOnClickListener(x->chooseMonth633(s));}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchGroupButtons633(g.getChildAt(i));}
    }

    private void patchHistoryButton633(View v){
        if(page==null||!page.startsWith("ATTENDANCE_SETTINGS_GROUP_630:"))return;
        final String group=page.substring("ATTENDANCE_SETTINGS_GROUP_630:".length());
        if(v instanceof Button&&"GEÇMİŞ YOKLAMALARI DÜZENLE".equalsIgnoreCase(String.valueOf(((Button)v).getText()).trim())){v.setOnClickListener(x->showPast633(group));return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchHistoryButton633(g.getChildAt(i));}
    }

    private void chooseMonth633(String group){
        Calendar now=Calendar.getInstance();ArrayList<Calendar> months=new ArrayList<>();ArrayList<String> labels=new ArrayList<>();
        for(int i=0;i<10;i++){Calendar c=(Calendar)now.clone();c.set(Calendar.DAY_OF_MONTH,1);c.add(Calendar.MONTH,-i);months.add(c);labels.add(MONTH.format(c.getTime()));}
        new AlertDialog.Builder(this).setTitle(group+" • YOKLAMA AYI").setItems(labels.toArray(new String[0]),(d,w)->{if(w==0)invokeCurrent633(group);else showMonth633(group,months.get(w));}).show();
    }

    private void invokeCurrent633(String group){try{Method m=MainActivityV632.class.getDeclaredMethod("showAttendanceMatrix632",String.class);m.setAccessible(true);m.invoke(this,group);}catch(Exception e){toast("Yoklama açılamadı.");}}

    private void showMonth633(String group,Calendar month){
        ArrayList<Sess> sessions=sessions633(group,month);page="ATTENDANCE_MONTH_633:"+group;currentAthlete=-1;base(group+" • "+MONTH.format(month.getTime()).toUpperCase(new Locale("tr","TR")),true);
        ScrollView outer=new ScrollView(this);outer.setFillViewport(true);LinearLayout host=new LinearLayout(this);host.setOrientation(LinearLayout.VERTICAL);host.setPadding(dp(6),dp(6),dp(6),dp(16));outer.addView(host);
        if(sessions.isEmpty()){host.addView(tv("Bu ay için yoklama günü bulunmuyor.",12,MUTED,true));root.addView(outer,new LinearLayout.LayoutParams(-1,0,1));return;}
        TextView hint=tv("İşaretli = geldi • İşaretsiz = gelmedi",9,MUTED,false);host.addView(hint);
        HorizontalScrollView h=new HorizontalScrollView(this);LinearLayout table=new LinearLayout(this);table.setOrientation(LinearLayout.VERTICAL);h.addView(table);
        LinearLayout head=new LinearLayout(this);head.addView(cell633("#  SPORCU",dp(190),dp(46),9,true));for(Sess s:sessions)head.addView(dateCell633(s.date));table.addView(head);
        Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear,photo FROM athletes WHERE category=? COLLATE NOCASE AND status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",new String[]{group});int n=0;
        while(a.moveToNext()){n++;long id=a.getLong(0);String name=a.getString(1),photo=a.getString(3);int by=a.getInt(2);LinearLayout r=new LinearLayout(this);r.addView(athleteCell633(n,id,name,by,photo));for(Sess s:sessions){ensureRecord633(s.id,id);CheckBox cb=new CheckBox(this);cb.setGravity(Gravity.CENTER);cb.setChecked(present633(s.id,id));cb.setOnCheckedChangeListener((v,on)->setPresent633(s.id,id,on));r.addView(cb,new LinearLayout.LayoutParams(dp(54),dp(50)));}table.addView(r);}a.close();host.addView(h,new LinearLayout.LayoutParams(-1,-2));root.addView(outer,new LinearLayout.LayoutParams(-1,0,1));
    }

    private View athleteCell633(int n,long id,String name,int by,String photo){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);TextView no=tv(String.valueOf(n),9,TEXT,true);no.setGravity(Gravity.CENTER);no.setPadding(0,0,0,0);r.addView(no,new LinearLayout.LayoutParams(dp(24),dp(50)));ImageView im=new ImageView(this);im.setImageResource(R.drawable.parion_logo);im.setScaleType(ImageView.ScaleType.CENTER_CROP);loadLocal633(im,photo);im.setOnClickListener(v->showProfile(id));r.addView(im,new LinearLayout.LayoutParams(dp(36),dp(36)));TextView nm=tv((by>0?by+" • ":"")+name,9,TEXT,true);nm.setMaxLines(2);nm.setOnClickListener(v->showProfile(id));r.addView(nm,new LinearLayout.LayoutParams(0,dp(50),1));r.setOnClickListener(v->showProfile(id));r.setLayoutParams(new LinearLayout.LayoutParams(dp(190),dp(50)));return r;}
    private void loadLocal633(ImageView v,String p){if(p==null||p.trim().isEmpty())return;try{p=p.trim();if(p.startsWith("content:")||p.startsWith("file:"))v.setImageURI(Uri.parse(p));else{File f=new File(p);if(f.exists())v.setImageURI(Uri.fromFile(f));}}catch(Exception ignored){}}
    private View dateCell633(String iso){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);try{Date d=ISO.parse(iso);TextView day=tv(DAY.format(d),10,TEXT,true);day.setGravity(Gravity.CENTER);day.setPadding(0,0,0,0);TextView mon=tv(MON.format(d).toUpperCase(new Locale("tr","TR")),6,MUTED,true);mon.setGravity(Gravity.CENTER);mon.setPadding(0,0,0,0);c.addView(day,new LinearLayout.LayoutParams(dp(54),dp(28)));c.addView(mon,new LinearLayout.LayoutParams(dp(54),dp(16)));}catch(Exception ignored){}c.setLayoutParams(new LinearLayout.LayoutParams(dp(54),dp(46)));return c;}
    private TextView cell633(String s,int w,int h,int sp,boolean bold){TextView t=tv(s,sp,TEXT,bold);t.setGravity(Gravity.CENTER_VERTICAL);t.setLayoutParams(new LinearLayout.LayoutParams(w,h));return t;}

    private void showPast633(String group){page="ATTENDANCE_HISTORY_633:"+group;currentAthlete=-1;base(group+" • GEÇMİŞ YOKLAMALAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);b.addView(tv("Son 10 ay içindeki yoklama günlerini değiştirebilir, iptal edebilir veya tek tek silebilirsin.",11,MUTED,false));Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate,cancelled FROM attendance_sessions WHERE groupName=? AND sessionDate<=date('now') AND sessionDate>=date('now','-10 months') ORDER BY sessionDate DESC",new String[]{group});while(c.moveToNext()){long id=c.getLong(0);String date=c.getString(1);boolean cancelled=c.getInt(2)==1;Button x=btn((cancelled?"İPTAL • ":"")+tr633(date));x.setOnClickListener(v->edit633(group,id,date,cancelled));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));lp.setMargins(0,dp(6),0,0);b.addView(x,lp);}c.close();}
    private void edit633(String group,long id,String date,boolean cancelled){String[] o={"TARİHİ DEĞİŞTİR",cancelled?"İPTALİ GERİ AL":"ANTRENMANI İPTAL ET","BU YOKLAMA GÜNÜNÜ SİL"};new AlertDialog.Builder(this).setTitle(tr633(date)).setItems(o,(d,w)->{if(w==2){new AlertDialog.Builder(this).setTitle("YOKLAMA GÜNÜ SİLİNSİN Mİ?").setMessage(tr633(date)+" tarihli yoklama ve o güne ait işaretler silinecek.").setPositiveButton("SİL",(x,z)->{SQLiteDatabase q=db.getWritableDatabase();q.beginTransaction();try{q.delete("attendance_records","sessionId=?",new String[]{String.valueOf(id)});q.delete("attendance_sessions","id=?",new String[]{String.valueOf(id)});q.setTransactionSuccessful();}finally{q.endTransaction();}showPast633(group);}).setNegativeButton("VAZGEÇ",null).show();return;}if(w==1){ContentValues v=new ContentValues();v.put("cancelled",cancelled?0:1);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});showPast633(group);return;}EditText e=new EditText(this);e.setText(tr633(date));new AlertDialog.Builder(this).setTitle("YENİ TARİH").setView(e).setPositiveButton("KAYDET",(x,z)->{String iso=parse633(e.getText().toString());if(iso==null){toast("Tarih geçersiz.");return;}ContentValues v=new ContentValues();v.put("sessionDate",iso);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});showPast633(group);}).setNegativeButton("VAZGEÇ",null).show();}).show();}

    private void backfillOnce633(){SharedPreferences p=getSharedPreferences("attendance_v633",MODE_PRIVATE);if(p.getBoolean("jul_aug_2026",false))return;SQLiteDatabase d=db.getWritableDatabase();Cursor g=d.rawQuery("SELECT name FROM app_groups",null);while(g.moveToNext()){String group=g.getString(0),wd=latestWeekdays633(group);if(wd==null||wd.trim().isEmpty())continue;fill633(d,group,2026,Calendar.JULY,wd);fill633(d,group,2026,Calendar.AUGUST,wd);}g.close();p.edit().putBoolean("jul_aug_2026",true).apply();}
    private void fill633(SQLiteDatabase d,String group,int y,int m,String wd){Calendar c=Calendar.getInstance();c.clear();c.set(y,m,1);int mm=c.get(Calendar.MONTH);while(c.get(Calendar.MONTH)==mm){if(hasDay633(wd,week633(c.get(Calendar.DAY_OF_WEEK)))){ContentValues v=new ContentValues();v.put("groupName",group);v.put("sessionDate",ISO.format(c.getTime()));d.insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);}c.add(Calendar.DAY_OF_MONTH,1);}}
    private String latestWeekdays633(String g){Cursor c=db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{g});String s=c.moveToFirst()?c.getString(0):null;c.close();return s;}
    private int week633(int d){return d==Calendar.MONDAY?1:d==Calendar.TUESDAY?2:d==Calendar.WEDNESDAY?3:d==Calendar.THURSDAY?4:d==Calendar.FRIDAY?5:d==Calendar.SATURDAY?6:7;}
    private boolean hasDay633(String s,int n){for(String x:s.split(","))if(x.trim().equals(String.valueOf(n)))return true;return false;}

    private ArrayList<Sess> sessions633(String group,Calendar month){ArrayList<Sess> out=new ArrayList<>();String ym=new SimpleDateFormat("yyyy-MM",Locale.US).format(month.getTime()),today=ISO.format(new Date());Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate FROM attendance_sessions WHERE groupName=? AND substr(sessionDate,1,7)=? AND sessionDate<=? AND cancelled=0 ORDER BY sessionDate",new String[]{group,ym,today});while(c.moveToNext())out.add(new Sess(c.getLong(0),c.getString(1)));c.close();return out;}
    private static class Sess{long id;String date;Sess(long i,String d){id=i;date=d;}}
    private void ensureRecord633(long s,long a){ContentValues v=new ContentValues();v.put("sessionId",s);v.put("athleteId",a);v.put("present",1);db.getWritableDatabase().insertWithOnConflict("attendance_records",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private boolean present633(long s,long a){Cursor c=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=?",new String[]{String.valueOf(s),String.valueOf(a)});boolean x=!c.moveToFirst()||c.getInt(0)==1;c.close();return x;}
    private void setPresent633(long s,long a,boolean on){ensureRecord633(s,a);ContentValues v=new ContentValues();v.put("present",on?1:0);db.getWritableDatabase().update("attendance_records",v,"sessionId=? AND athleteId=?",new String[]{String.valueOf(s),String.valueOf(a)});}
    private String tr633(String s){try{return FULL.format(ISO.parse(s));}catch(Exception e){return s;}}
    private String parse633(String s){try{return ISO.format(FULL.parse(s));}catch(Exception e){return null;}}
}
