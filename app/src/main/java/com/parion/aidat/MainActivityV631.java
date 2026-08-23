package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.31 - group-level dashboard inclusion and vertically scrollable attendance matrix with frozen athlete column. */
public class MainActivityV631 extends MainActivityV630 {
    private static final int TEXT=Color.rgb(28,28,28), MUTED=Color.rgb(92,92,92), RED=Color.rgb(185,55,55);
    private final SimpleDateFormat ISO=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    private final SimpleDateFormat DAY=new SimpleDateFormat("dd",new Locale("tr","TR"));
    private final SimpleDateFormat MON=new SimpleDateFormat("MMM",new Locale("tr","TR"));

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        ensureGroupDashboardTable631();
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(()->{
            patchAttendanceRail631(root);
            replaceAbsenteesCard631();
        });
    }

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->patchAttendanceRail631(root));
    }

    private void ensureGroupDashboardTable631(){
        SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_dashboard_groups(groupName TEXT PRIMARY KEY,enabled INTEGER NOT NULL DEFAULT 1)");
        Cursor c=d.rawQuery("SELECT name FROM app_groups",null);
        while(c.moveToNext()){
            ContentValues v=new ContentValues();v.put("groupName",c.getString(0));v.put("enabled",1);
            d.insertWithOnConflict("attendance_dashboard_groups",null,v,SQLiteDatabase.CONFLICT_IGNORE);
        }
        c.close();
    }

    private void patchAttendanceRail631(View v){
        if(v instanceof ImageButton){
            CharSequence cd=v.getContentDescription();
            if(cd!=null&&"Yoklamalar".equalsIgnoreCase(cd.toString())){
                v.setOnClickListener(x->showAttendanceGroups631());return;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchAttendanceRail631(g.getChildAt(i));}
    }

    private void showAttendanceGroups631(){
        ensureGroupDashboardTable631();
        page="ATTENDANCE_GROUPS_631";currentAthlete=-1;base("YOKLAMALAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(12),dp(10),dp(12),dp(24));
        TextView note=tv("Soldaki onay: bu grup DEVAMSIZLAR dashboard hesabına dahil edilir. İşareti kaldırılan grup dashboard hesabına katılmaz.",11,MUTED,false);
        note.setPadding(dp(4),0,dp(4),dp(8));b.addView(note);
        Button export=btn("YOKLAMALARI DIŞA AKTAR");export.setOnClickListener(v->invoke628("chooseExport628",new Class<?>[0]));b.addView(export,new LinearLayout.LayoutParams(-1,dp(50)));
        Cursor c=db.getReadableDatabase().rawQuery("SELECT name FROM app_groups ORDER BY sortOrder,id,name COLLATE NOCASE",null);
        while(c.moveToNext()){
            String group=c.getString(0);
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(6),dp(4),dp(6),dp(4));row.setBackground(round631(Color.WHITE,Color.rgb(222,216,198),12,1));
            CheckBox include=new CheckBox(this);include.setChecked(groupEnabled631(group));include.setContentDescription(group+" dashboard dahil");include.setOnCheckedChangeListener((x,on)->setGroupEnabled631(group,on));
            row.addView(include,new LinearLayout.LayoutParams(dp(48),dp(50)));
            Button g=btn(group);g.setAllCaps(false);g.setOnClickListener(v->showAttendanceMatrix631(group));row.addView(g,new LinearLayout.LayoutParams(0,dp(50),1));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.setMargins(0,dp(7),0,0);b.addView(row,lp);
        }
        c.close();
    }

    private boolean groupEnabled631(String group){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT enabled FROM attendance_dashboard_groups WHERE groupName=?",new String[]{group});
        boolean on=!c.moveToFirst()||c.getInt(0)==1;c.close();return on;
    }
    private void setGroupEnabled631(String group,boolean on){ContentValues v=new ContentValues();v.put("groupName",group);v.put("enabled",on?1:0);db.getWritableDatabase().insertWithOnConflict("attendance_dashboard_groups",null,v,SQLiteDatabase.CONFLICT_REPLACE);}

    private void showAttendanceMatrix631(String group){
        if(!hasSchedule631(group)){Toast.makeText(this,"Önce Ayarlar > Yoklama Ayarları bölümünden antrenman günlerini belirleyin.",Toast.LENGTH_LONG).show();return;}
        ensureMonth631(group);
        ArrayList<Sess> sessions=sessions631(group);
        page="ATTENDANCE_MATRIX_631:"+group;currentAthlete=-1;base(group+" • YOKLAMA",true);
        if(sessions.isEmpty()){
            ScrollView sv=scroll();LinearLayout b=box(sv);b.addView(tv("Bu ay için yoklama tarihi bulunmuyor.",13,MUTED,true));return;
        }

        // Remove the default content ScrollView created by base; build a dedicated frozen-column matrix.
        View content=findMainContent631();if(content!=null&&content.getParent() instanceof ViewGroup)((ViewGroup)content.getParent()).removeView(content);
        LinearLayout host=new LinearLayout(this);host.setOrientation(LinearLayout.VERTICAL);host.setPadding(dp(6),dp(6),dp(6),dp(8));
        TextView info=tv("İşaretli = geldi • İşaretsiz = gelmedi",10,MUTED,false);info.setPadding(dp(6),0,dp(6),dp(5));host.addView(info,new LinearLayout.LayoutParams(-1,-2));

        // Header: fixed athlete title at left, horizontally scrollable compact dates at right.
        LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);
        TextView athleteHead=cell631("SPORCU",dp(142),dp(48),10.5f,true);header.addView(athleteHead);
        HorizontalScrollView headerH=new HorizontalScrollView(this);headerH.setHorizontalScrollBarEnabled(false);
        LinearLayout dateHeader=new LinearLayout(this);dateHeader.setOrientation(LinearLayout.HORIZONTAL);
        for(Sess s:sessions)dateHeader.addView(dateCell631(s));
        headerH.addView(dateHeader,new HorizontalScrollView.LayoutParams(-2,dp(48)));header.addView(headerH,new LinearLayout.LayoutParams(0,dp(48),1));host.addView(header);

        // Body: one vertical ScrollView keeps names and attendance rows synchronized vertically.
        ScrollView vertical=new ScrollView(this);vertical.setFillViewport(true);
        LinearLayout bodyRow=new LinearLayout(this);bodyRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout names=new LinearLayout(this);names.setOrientation(LinearLayout.VERTICAL);names.setBackgroundColor(Color.WHITE);
        HorizontalScrollView gridH=new HorizontalScrollView(this);gridH.setHorizontalScrollBarEnabled(true);
        LinearLayout gridRows=new LinearLayout(this);gridRows.setOrientation(LinearLayout.VERTICAL);

        Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear FROM athletes WHERE category=? COLLATE NOCASE AND status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",new String[]{group});
        while(a.moveToNext()){
            long athleteId=a.getLong(0);String name=a.getString(1);int by=a.getInt(2);
            names.addView(cell631((by>0?by+" • ":"")+name,dp(142),dp(46),10.5f,false));
            LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);
            for(Sess s:sessions){ensureRecord631(s.id,athleteId);CheckBox cb=new CheckBox(this);cb.setGravity(Gravity.CENTER);cb.setChecked(recordPresent631(s.id,athleteId));cb.setEnabled(!s.cancelled);cb.setOnCheckedChangeListener((v,on)->setRecord631(s.id,athleteId,on));r.addView(cb,new LinearLayout.LayoutParams(dp(62),dp(46)));}
            gridRows.addView(r,new LinearLayout.LayoutParams(-2,dp(46)));
        }
        a.close();
        bodyRow.addView(names,new LinearLayout.LayoutParams(dp(142),-2));gridH.addView(gridRows,new HorizontalScrollView.LayoutParams(-2,-2));bodyRow.addView(gridH,new LinearLayout.LayoutParams(0,-2,1));vertical.addView(bodyRow,new ScrollView.LayoutParams(-1,-2));host.addView(vertical,new LinearLayout.LayoutParams(-1,0,1));

        // Synchronize header horizontal position with body grid.
        if(android.os.Build.VERSION.SDK_INT>=23){gridH.setOnScrollChangeListener((v,x,y,ox,oy)->headerH.scrollTo(x,0));headerH.setOnScrollChangeListener((v,x,y,ox,oy)->gridH.scrollTo(x,0));}
        int idx=Math.max(0,root.getChildCount()-1);root.addView(host,idx,new LinearLayout.LayoutParams(-1,0,1));
    }

    private View dateCell631(Sess s){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);c.setBackground(round631(Color.WHITE,Color.rgb(225,225,225),3,1));
        try{Date d=ISO.parse(s.date);TextView day=tv(DAY.format(d),10,TEXT,true);day.setGravity(Gravity.CENTER);TextView mon=tv(MON.format(d).toUpperCase(new Locale("tr","TR")),8,MUTED,true);mon.setGravity(Gravity.CENTER);c.addView(day,new LinearLayout.LayoutParams(dp(62),dp(25)));c.addView(mon,new LinearLayout.LayoutParams(dp(62),dp(19)));}catch(Exception e){TextView t=tv(s.date,8,TEXT,true);t.setGravity(Gravity.CENTER);c.addView(t,new LinearLayout.LayoutParams(dp(62),dp(44)));}
        return new FrameWrap631(c,dp(62),dp(48)).view;
    }
    private static class FrameWrap631{View view;FrameWrap631(View v,int w,int h){v.setLayoutParams(new LinearLayout.LayoutParams(w,h));view=v;}}

    private TextView cell631(String text,int w,int h,float sp,boolean bold){TextView t=new TextView(this);t.setText(text);t.setTextSize(sp);t.setTextColor(TEXT);t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(5),0,dp(5),0);t.setBackground(round631(Color.WHITE,Color.rgb(225,225,225),3,1));t.setMaxLines(2);t.setLayoutParams(new LinearLayout.LayoutParams(w,h));return t;}

    private void replaceAbsenteesCard631(){
        ensureGroupDashboardTable631();ScrollView sv=findScroll631(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;LinearLayout box=(LinearLayout)sv.getChildAt(0);
        for(int i=box.getChildCount()-1;i>=0;i--){if(containsText631(box.getChildAt(i),"DEVAMSIZLAR"))box.removeViewAt(i);}
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(12),dp(12),dp(12));card.setBackground(round631(Color.WHITE,RED,14,1));card.addView(tv("DEVAMSIZLAR",12,TEXT,true));
        Cursor c=absentees631();int n=0;while(c.moveToNext()){long id=c.getLong(0);String name=c.getString(1);int days=c.getInt(2);TextView r=tv(name+" • "+days+" gündür gelmiyor",12,days>=30?Color.rgb(170,30,30):TEXT,true);r.setOnClickListener(v->showProfile(id));card.addView(r);n++;}c.close();if(n==0)card.addView(tv("Seçili gruplarda 15 gün ve üzeri devamsız sporcu yok.",11,MUTED,false));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(12),0,dp(8));box.addView(card,lp);
    }

    private Cursor absentees631(){
        String sql="SELECT a.id,a.name,CAST(julianday('now')-julianday(COALESCE(MAX(CASE WHEN r.present=1 THEN s.sessionDate END),MIN(s.sessionDate))) AS INTEGER) days " +
                "FROM athletes a JOIN attendance_dashboard_groups g ON g.groupName=a.category AND g.enabled=1 " +
                "JOIN attendance_sessions s ON s.groupName=a.category AND s.cancelled=0 AND s.sessionDate<=date('now') " +
                "LEFT JOIN attendance_records r ON r.sessionId=s.id AND r.athleteId=a.id " +
                "WHERE a.status='AKTİF' AND TRIM(COALESCE(a.deletedAt,''))='' GROUP BY a.id,a.name HAVING days>=15 ORDER BY days DESC,a.name";
        return db.getReadableDatabase().rawQuery(sql,null);
    }

    private boolean hasSchedule631(String g){Cursor c=db.getReadableDatabase().rawQuery("SELECT 1 FROM attendance_schedule WHERE groupName=? LIMIT 1",new String[]{g});boolean x=c.moveToFirst();c.close();return x;}
    private void ensureMonth631(String group){Calendar c=Calendar.getInstance();int m=c.get(Calendar.MONTH);c.set(Calendar.DAY_OF_MONTH,1);while(c.get(Calendar.MONTH)==m){String date=ISO.format(c.getTime());String wd=weekdays631(group,date);int n=week631(c.get(Calendar.DAY_OF_WEEK));if(wd!=null&&containsDay631(wd,n)){ContentValues v=new ContentValues();v.put("groupName",group);v.put("sessionDate",date);db.getWritableDatabase().insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);}c.add(Calendar.DAY_OF_MONTH,1);}}
    private String weekdays631(String group,String date){Cursor c=db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? AND effectiveFrom<=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{group,date});String s=c.moveToFirst()?c.getString(0):null;c.close();return s;}
    private int week631(int d){return d==Calendar.MONDAY?1:d==Calendar.TUESDAY?2:d==Calendar.WEDNESDAY?3:d==Calendar.THURSDAY?4:d==Calendar.FRIDAY?5:d==Calendar.SATURDAY?6:7;}
    private boolean containsDay631(String list,int n){for(String z:list.split(","))if(z.trim().equals(String.valueOf(n)))return true;return false;}
    private ArrayList<Sess> sessions631(String group){ArrayList<Sess> out=new ArrayList<>();String ym=new SimpleDateFormat("yyyy-MM",Locale.US).format(new Date());Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate,cancelled FROM attendance_sessions WHERE groupName=? AND substr(sessionDate,1,7)=? ORDER BY sessionDate",new String[]{group,ym});while(c.moveToNext())out.add(new Sess(c.getLong(0),c.getString(1),c.getInt(2)==1));c.close();return out;}
    private static class Sess{long id;String date;boolean cancelled;Sess(long i,String d,boolean c){id=i;date=d;cancelled=c;}}
    private void ensureRecord631(long s,long a){ContentValues v=new ContentValues();v.put("sessionId",s);v.put("athleteId",a);v.put("present",1);db.getWritableDatabase().insertWithOnConflict("attendance_records",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private boolean recordPresent631(long s,long a){Cursor c=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=?",new String[]{String.valueOf(s),String.valueOf(a)});boolean x=!c.moveToFirst()||c.getInt(0)==1;c.close();return x;}
    private void setRecord631(long s,long a,boolean on){ensureRecord631(s,a);ContentValues v=new ContentValues();v.put("present",on?1:0);db.getWritableDatabase().update("attendance_records",v,"sessionId=? AND athleteId=?",new String[]{String.valueOf(s),String.valueOf(a)});}

    private Object invoke628(String name,Class<?>[] types,Object... args){try{Method m=MainActivityV628.class.getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(this,args);}catch(Exception e){toast("İşlem açılamadı.");return null;}}
    private View findMainContent631(){for(int i=0;i<root.getChildCount();i++){View v=root.getChildAt(i);if(v instanceof ScrollView)return v;}return null;}
    private ScrollView findScroll631(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll631(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private boolean containsText631(View v,String needle){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR")).contains(needle))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsText631(g.getChildAt(i),needle))return true;}return false;}
    private GradientDrawable round631(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}

    @Override void goBack(){if(page!=null&&page.startsWith("ATTENDANCE_MATRIX_631:")){showAttendanceGroups631();return;}if("ATTENDANCE_GROUPS_631".equals(page)){showHome();return;}super.goBack();}
}
