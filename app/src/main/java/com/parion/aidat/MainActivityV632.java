package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.32 - compact visible dates, numbered/photo athlete column, profile shortcuts, past attendance editing, no future attendance columns. */
public class MainActivityV632 extends MainActivityV631 {
    private static final int TEXT=Color.rgb(28,28,28), MUTED=Color.rgb(92,92,92);
    private final SimpleDateFormat ISO632=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    private final SimpleDateFormat DAY632=new SimpleDateFormat("dd",new Locale("tr","TR"));
    private final SimpleDateFormat MON632=new SimpleDateFormat("MMM",new Locale("tr","TR"));
    private final SimpleDateFormat FULL632=new SimpleDateFormat("dd.MM.yyyy",new Locale("tr","TR"));

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->{
            patchAttendanceGroupButtons632(root);
            patchAttendanceSettings632(root);
        });
    }

    private void patchAttendanceGroupButtons632(View v){
        if(!"ATTENDANCE_GROUPS_631".equals(page))return;
        if(v instanceof Button){
            Button b=(Button)v;String text=String.valueOf(b.getText()).trim();
            if(!text.isEmpty()&&!text.toUpperCase(new Locale("tr","TR")).contains("DIŞA AKTAR")){
                b.setOnClickListener(x->showAttendanceMatrix632(text));
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchAttendanceGroupButtons632(g.getChildAt(i));}
    }

    private void patchAttendanceSettings632(View v){
        if(page==null||!page.startsWith("ATTENDANCE_SETTINGS_GROUP_630:"))return;
        String group=page.substring("ATTENDANCE_SETTINGS_GROUP_630:".length());
        ScrollView sv=findScroll632(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        if(findTag632(box,"v632-history")!=null)return;
        Button history=btn("GEÇMİŞ YOKLAMALARI DÜZENLE");history.setTag("v632-history");history.setOnClickListener(x->showPastAttendance632(group));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));lp.setMargins(0,dp(8),0,dp(8));
        int index=Math.min(2,box.getChildCount());box.addView(history,index,lp);
    }

    private void showAttendanceMatrix632(String group){
        if(!hasSchedule632(group)){toast("Önce Ayarlar > Yoklama Ayarları bölümünden antrenman günlerini belirleyin.");return;}
        ensureCurrentMonthPastSessions632(group);
        ArrayList<Session632> sessions=sessionsUntilToday632(group);
        page="ATTENDANCE_MATRIX_632:"+group;currentAthlete=-1;base(group+" • YOKLAMA",true);
        if(sessions.isEmpty()){
            ScrollView sv=scroll();LinearLayout b=box(sv);b.addView(tv("Bu ay bugün veya geçmişe ait yoklama tarihi bulunmuyor.",13,MUTED,true));return;
        }

        View content=findMainContent632();if(content!=null&&content.getParent() instanceof ViewGroup)((ViewGroup)content.getParent()).removeView(content);
        LinearLayout host=new LinearLayout(this);host.setOrientation(LinearLayout.VERTICAL);host.setPadding(dp(4),dp(4),dp(4),dp(6));
        TextView info=tv("İşaretli = geldi • İşaretsiz = gelmedi",9,MUTED,false);info.setPadding(dp(5),0,dp(5),dp(4));host.addView(info,new LinearLayout.LayoutParams(-1,-2));

        final int leftW=dp(196), dateW=dp(54), rowH=dp(50), headerH=dp(44);
        LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);
        TextView athleteHead=cell632("#   SPORCU",leftW,headerH,9.5f,true);header.addView(athleteHead);
        HorizontalScrollView headerHsv=new HorizontalScrollView(this);headerHsv.setHorizontalScrollBarEnabled(false);
        LinearLayout dates=new LinearLayout(this);dates.setOrientation(LinearLayout.HORIZONTAL);
        for(Session632 s:sessions)dates.addView(dateCell632(s,dateW,headerH));
        headerHsv.addView(dates,new HorizontalScrollView.LayoutParams(-2,headerH));header.addView(headerHsv,new LinearLayout.LayoutParams(0,headerH,1));host.addView(header);

        ScrollView vertical=new ScrollView(this);vertical.setFillViewport(true);vertical.setVerticalScrollBarEnabled(true);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout names=new LinearLayout(this);names.setOrientation(LinearLayout.VERTICAL);names.setBackgroundColor(Color.WHITE);
        HorizontalScrollView gridHsv=new HorizontalScrollView(this);gridHsv.setHorizontalScrollBarEnabled(true);
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);

        Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear,photo FROM athletes WHERE category=? COLLATE NOCASE AND status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",new String[]{group});
        int order=0;
        while(a.moveToNext()){
            order++;long athleteId=a.getLong(0);String name=a.getString(1);int birthYear=a.getInt(2);String photo=a.getString(3);
            names.addView(athleteCell632(order,athleteId,name,birthYear,photo,leftW,rowH));
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
            for(Session632 s:sessions){
                ensureRecord632(s.id,athleteId);
                CheckBox cb=new CheckBox(this);cb.setGravity(Gravity.CENTER);cb.setChecked(recordPresent632(s.id,athleteId));cb.setEnabled(!s.cancelled);cb.setContentDescription(name+" • "+s.date);
                cb.setOnCheckedChangeListener((v,on)->setRecord632(s.id,athleteId,on));row.addView(cb,new LinearLayout.LayoutParams(dateW,rowH));
            }
            grid.addView(row,new LinearLayout.LayoutParams(-2,rowH));
        }
        a.close();
        body.addView(names,new LinearLayout.LayoutParams(leftW,-2));gridHsv.addView(grid,new HorizontalScrollView.LayoutParams(-2,-2));body.addView(gridHsv,new LinearLayout.LayoutParams(0,-2,1));vertical.addView(body,new ScrollView.LayoutParams(-1,-2));host.addView(vertical,new LinearLayout.LayoutParams(-1,0,1));
        if(android.os.Build.VERSION.SDK_INT>=23){gridHsv.setOnScrollChangeListener((v,x,y,ox,oy)->headerHsv.scrollTo(x,0));headerHsv.setOnScrollChangeListener((v,x,y,ox,oy)->gridHsv.scrollTo(x,0));}
        root.addView(host,Math.max(0,root.getChildCount()-1),new LinearLayout.LayoutParams(-1,0,1));
    }

    private View athleteCell632(int order,long athleteId,String name,int birthYear,String photo,int width,int height){
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(3),dp(2),dp(4),dp(2));row.setBackground(roundStroke632(Color.WHITE,Color.rgb(225,225,225),3,1));
        TextView no=tv(String.valueOf(order),9,TEXT,true);no.setGravity(Gravity.CENTER);no.setPadding(0,0,0,0);row.addView(no,new LinearLayout.LayoutParams(dp(25),height));
        ImageView img=new ImageView(this);img.setScaleType(ImageView.ScaleType.CENTER_CROP);img.setImageResource(R.drawable.parion_logo);img.setContentDescription(name+" fotoğrafı");img.setOnClickListener(v->showProfile(athleteId));loadPhoto632(img,photo);
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(38),dp(38));ip.setMargins(0,0,dp(5),0);row.addView(img,ip);
        TextView nm=tv((birthYear>0?birthYear+" • ":"")+name,9.3f>0?9:9,TEXT,true);nm.setTextSize(9.3f);nm.setPadding(dp(2),0,dp(2),0);nm.setMaxLines(2);nm.setGravity(Gravity.CENTER_VERTICAL);nm.setOnClickListener(v->showProfile(athleteId));row.addView(nm,new LinearLayout.LayoutParams(0,height,1));
        row.setOnClickListener(v->showProfile(athleteId));row.setLayoutParams(new LinearLayout.LayoutParams(width,height));return row;
    }

    private void loadPhoto632(ImageView view,String photo){
        if(photo==null||photo.trim().isEmpty())return;String p=photo.trim();
        try{
            if(p.startsWith("http://")||p.startsWith("https://")){
                new Thread(()->{try{Bitmap bm=BitmapFactory.decodeStream(new URL(p).openStream());if(bm!=null)view.post(()->view.setImageBitmap(bm));}catch(Exception ignored){}}).start();
            }else if(p.startsWith("content:")||p.startsWith("file:")){view.setImageURI(Uri.parse(p));}
            else {File f=new File(p);if(f.exists())view.setImageURI(Uri.fromFile(f));}
        }catch(Exception ignored){}
    }

    private View dateCell632(Session632 s,int width,int height){
        LinearLayout col=new LinearLayout(this);col.setOrientation(LinearLayout.VERTICAL);col.setGravity(Gravity.CENTER);col.setPadding(0,0,0,0);col.setBackground(roundStroke632(Color.WHITE,Color.rgb(225,225,225),3,1));
        try{
            Date d=ISO632.parse(s.date);TextView day=tv(DAY632.format(d),12,TEXT,true);day.setGravity(Gravity.CENTER);day.setPadding(0,0,0,0);day.setIncludeFontPadding(false);
            TextView mon=tv(MON632.format(d).toUpperCase(new Locale("tr","TR")),7,MUTED,true);mon.setGravity(Gravity.CENTER);mon.setPadding(0,0,0,0);mon.setIncludeFontPadding(false);
            col.addView(day,new LinearLayout.LayoutParams(width,dp(25)));col.addView(mon,new LinearLayout.LayoutParams(width,dp(15)));
        }catch(Exception e){TextView t=tv(s.date,7,TEXT,true);t.setGravity(Gravity.CENTER);t.setPadding(0,0,0,0);col.addView(t,new LinearLayout.LayoutParams(width,height));}
        col.setLayoutParams(new LinearLayout.LayoutParams(width,height));return col;
    }

    private void showPastAttendance632(String group){
        page="ATTENDANCE_HISTORY_632:"+group;currentAthlete=-1;base(group+" • GEÇMİŞ YOKLAMALAR",true);
        ScrollView sv=scroll();LinearLayout box=box(sv);box.setPadding(dp(12),dp(10),dp(12),dp(24));
        TextView note=tv("Son 10 ay içindeki yoklama günlerini buradan değiştirebilir veya iptal edebilirsin.",11,MUTED,false);box.addView(note);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate,cancelled FROM attendance_sessions WHERE groupName=? AND sessionDate<=date('now') AND sessionDate>=date('now','-10 months') ORDER BY sessionDate DESC",new String[]{group});
        int count=0;while(c.moveToNext()){
            count++;long id=c.getLong(0);String date=c.getString(1);boolean cancelled=c.getInt(2)==1;
            Button b=btn((cancelled?"İPTAL • ":"")+dateTrFull632(date));b.setAllCaps(false);b.setOnClickListener(v->editPastSession632(group,id,date,cancelled));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));lp.setMargins(0,dp(6),0,0);box.addView(b,lp);
        }c.close();if(count==0)box.addView(tv("Düzenlenebilir geçmiş yoklama bulunmuyor.",12,MUTED,true));
    }

    private void editPastSession632(String group,long id,String date,boolean cancelled){
        String[] opts={"TARİHİ DEĞİŞTİR",cancelled?"İPTALİ GERİ AL":"ANTRENMANI İPTAL ET"};
        new AlertDialog.Builder(this).setTitle(dateTrFull632(date)).setItems(opts,(d,w)->{
            if(w==1){ContentValues v=new ContentValues();v.put("cancelled",cancelled?0:1);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});showPastAttendance632(group);}
            else {EditText e=new EditText(this);e.setText(dateTrFull632(date));new AlertDialog.Builder(this).setTitle("YENİ TARİH").setView(e).setPositiveButton("KAYDET",(x,z)->{String iso=parseDate632(e.getText().toString());if(iso==null){toast("Tarih geçersiz.");return;}ContentValues v=new ContentValues();v.put("sessionDate",iso);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});showPastAttendance632(group);}).setNegativeButton("VAZGEÇ",null).show();}
        }).show();
    }

    private boolean hasSchedule632(String group){Cursor c=db.getReadableDatabase().rawQuery("SELECT 1 FROM attendance_schedule WHERE groupName=? LIMIT 1",new String[]{group});boolean ok=c.moveToFirst();c.close();return ok;}
    private void ensureCurrentMonthPastSessions632(String group){
        Calendar now=Calendar.getInstance();String today=ISO632.format(now.getTime());Calendar c=Calendar.getInstance();int month=c.get(Calendar.MONTH);c.set(Calendar.DAY_OF_MONTH,1);
        while(c.get(Calendar.MONTH)==month){String date=ISO632.format(c.getTime());if(date.compareTo(today)>0)break;String wd=weekdaysForDate632(group,date);int n=weekNo632(c.get(Calendar.DAY_OF_WEEK));if(wd!=null&&containsDay632(wd,n)){ContentValues v=new ContentValues();v.put("groupName",group);v.put("sessionDate",date);db.getWritableDatabase().insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);}c.add(Calendar.DAY_OF_MONTH,1);}
    }
    private String weekdaysForDate632(String group,String date){Cursor c=db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? AND effectiveFrom<=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{group,date});String s=c.moveToFirst()?c.getString(0):null;c.close();return s;}
    private int weekNo632(int d){return d==Calendar.MONDAY?1:d==Calendar.TUESDAY?2:d==Calendar.WEDNESDAY?3:d==Calendar.THURSDAY?4:d==Calendar.FRIDAY?5:d==Calendar.SATURDAY?6:7;}
    private boolean containsDay632(String list,int n){for(String x:list.split(","))if(x.trim().equals(String.valueOf(n)))return true;return false;}
    private ArrayList<Session632> sessionsUntilToday632(String group){ArrayList<Session632> out=new ArrayList<>();String ym=new SimpleDateFormat("yyyy-MM",Locale.US).format(new Date());String today=ISO632.format(new Date());Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate,cancelled FROM attendance_sessions WHERE groupName=? AND substr(sessionDate,1,7)=? AND sessionDate<=? AND cancelled=0 ORDER BY sessionDate",new String[]{group,ym,today});while(c.moveToNext())out.add(new Session632(c.getLong(0),c.getString(1),c.getInt(2)==1));c.close();return out;}
    private static class Session632{long id;String date;boolean cancelled;Session632(long i,String d,boolean c){id=i;date=d;cancelled=c;}}

    private void ensureRecord632(long session,long athlete){ContentValues v=new ContentValues();v.put("sessionId",session);v.put("athleteId",athlete);v.put("present",1);db.getWritableDatabase().insertWithOnConflict("attendance_records",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private boolean recordPresent632(long session,long athlete){Cursor c=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=?",new String[]{String.valueOf(session),String.valueOf(athlete)});boolean yes=!c.moveToFirst()||c.getInt(0)==1;c.close();return yes;}
    private void setRecord632(long session,long athlete,boolean present){ensureRecord632(session,athlete);ContentValues v=new ContentValues();v.put("present",present?1:0);db.getWritableDatabase().update("attendance_records",v,"sessionId=? AND athleteId=?",new String[]{String.valueOf(session),String.valueOf(athlete)});}

    private String dateTrFull632(String iso){try{return FULL632.format(ISO632.parse(iso));}catch(Exception e){return iso;}}
    private String parseDate632(String tr){try{return ISO632.format(FULL632.parse(tr));}catch(Exception e){return null;}}
    private TextView cell632(String text,int w,int h,float sp,boolean bold){TextView t=new TextView(this);t.setText(text);t.setTextSize(sp);t.setTextColor(TEXT);t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(5),0,dp(5),0);t.setBackground(roundStroke632(Color.WHITE,Color.rgb(225,225,225),3,1));t.setMaxLines(2);t.setLayoutParams(new LinearLayout.LayoutParams(w,h));return t;}
    private android.graphics.drawable.GradientDrawable roundStroke632(int fill,int stroke,int radius,int width){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}
    private View findMainContent632(){for(int i=0;i<root.getChildCount();i++){View v=root.getChildAt(i);if(v instanceof ScrollView)return v;}return null;}
    private ScrollView findScroll632(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll632(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private View findTag632(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag632(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}

    @Override void goBack(){if(page!=null&&page.startsWith("ATTENDANCE_MATRIX_632:")){try{java.lang.reflect.Method m=MainActivityV631.class.getDeclaredMethod("showAttendanceGroups631");m.setAccessible(true);m.invoke(this);}catch(Exception e){showHome();}return;}if(page!=null&&page.startsWith("ATTENDANCE_HISTORY_632:")){String group=page.substring("ATTENDANCE_HISTORY_632:".length());try{java.lang.reflect.Method m=MainActivityV630.class.getDeclaredMethod("showGroupAttendanceSettings630",String.class);m.setAccessible(true);m.invoke(this,group);}catch(Exception e){showHome();}return;}super.goBack();}
}
