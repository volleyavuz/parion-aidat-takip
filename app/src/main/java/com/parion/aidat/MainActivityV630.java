package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.30 - student-based attendance matrix; attendance date editing moved to Settings. */
public class MainActivityV630 extends MainActivityV629 {
    private static final int TEXT_630=Color.rgb(28,28,28), MUTED_630=Color.rgb(95,95,95), GOLD_630=Color.rgb(205,156,34);
    private final SimpleDateFormat ISO630=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    private final SimpleDateFormat TR630=new SimpleDateFormat("dd.MM",new Locale("tr","TR"));
    private PopupWindow popup630;

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->{patchSettingsIcon630(root);patchAttendanceGroupButtons630(root);});
    }

    private void patchSettingsIcon630(View v){
        if(v instanceof TextView){CharSequence d=v.getContentDescription();if(d!=null&&"Ayarlar".equalsIgnoreCase(d.toString())){v.setOnClickListener(this::showSettingsPopup630);return;}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchSettingsIcon630(g.getChildAt(i));}
    }

    private void showSettingsPopup630(View anchor){
        dismissPopup630();
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(8),dp(8),dp(8),dp(8));panel.setBackground(round630(Color.WHITE,Color.rgb(215,205,175),14,1));
        TextView groups=item630("GRUPLARI DÜZENLE");groups.setOnClickListener(v->{dismissPopup630();openGroups630();});panel.addView(groups,new LinearLayout.LayoutParams(dp(230),dp(48)));
        TextView att=item630("YOKLAMA AYARLARI");att.setOnClickListener(v->{dismissPopup630();showAttendanceSettings630();});panel.addView(att,new LinearLayout.LayoutParams(dp(230),dp(48)));
        popup630=new PopupWindow(panel,dp(246),WindowManager.LayoutParams.WRAP_CONTENT,true);popup630.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));popup630.setOutsideTouchable(true);popup630.setElevation(dp(8));
        int[] loc=new int[2];anchor.getLocationOnScreen(loc);panel.measure(View.MeasureSpec.makeMeasureSpec(dp(246),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));int h=Math.max(dp(112),panel.getMeasuredHeight());int x=Math.max(dp(8),getResources().getDisplayMetrics().widthPixels-dp(258));int y=Math.max(dp(8),loc[1]-h-dp(8));popup630.showAtLocation(root,Gravity.TOP|Gravity.START,x,y);
    }
    private TextView item630(String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(13);t.setTextColor(TEXT_630);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(14),0,dp(14),0);t.setClickable(true);return t;}
    private void dismissPopup630(){if(popup630!=null){try{popup630.dismiss();}catch(Exception ignored){}popup630=null;}}
    private void openGroups630(){try{Method m=MainActivityV625.class.getDeclaredMethod("showGroups625");m.setAccessible(true);m.invoke(this);}catch(Exception e){toast("Gruplar ekranı açılamadı.");}}

    private void patchAttendanceGroupButtons630(View v){
        if(!"ATTENDANCE_GROUPS_628".equals(page))return;
        if(v instanceof Button){Button b=(Button)v;String s=String.valueOf(b.getText()).trim();if(!s.isEmpty()&&!s.toUpperCase(new Locale("tr","TR")).contains("DIŞA AKTAR"))b.setOnClickListener(x->showAttendanceMatrix630(s));}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchAttendanceGroupButtons630(g.getChildAt(i));}
    }

    private void showAttendanceMatrix630(String group){
        if(!hasSchedule630(group)){new AlertDialog.Builder(this).setTitle("YOKLAMA PROGRAMI YOK").setMessage(group+" için önce Ayarlar > Yoklama Ayarları bölümünden haftalık antrenman günlerini belirleyin.").setPositiveButton("AYARLARA GİT",(d,w)->showGroupAttendanceSettings630(group)).setNegativeButton("KAPAT",null).show();return;}
        ensureCurrentMonthSessions630(group);
        page="ATTENDANCE_MATRIX_630:"+group;currentAthlete=-1;base(group+" • YOKLAMA",true);
        LinearLayout host=new LinearLayout(this);host.setOrientation(LinearLayout.VERTICAL);host.setPadding(dp(6),dp(8),dp(6),dp(16));
        TextView info=tv("İşaretli = geldi • İşaretsiz = gelmedi. Tarih başlığındaki ONAY kutusu işaretlenmeyen yoklamalar Devamsızlar hesabına katılmaz.",11,MUTED_630,false);info.setPadding(dp(6),0,dp(6),dp(8));host.addView(info);
        HorizontalScrollView hsv=new HorizontalScrollView(this);TableLayout table=new TableLayout(this);table.setStretchAllColumns(false);hsv.addView(table,new HorizontalScrollView.LayoutParams(-2,-2));
        ArrayList<Session630> sessions=currentMonthSessions630(group);if(sessions.isEmpty()){host.addView(tv("Bu ay için yoklama tarihi bulunmuyor. Ayarlar > Yoklama Ayarları bölümünü kontrol edin.",13,MUTED_630,true));return;}
        TableRow header=new TableRow(this);TextView athleteHead=cell630("SPORCU",150,true);header.addView(athleteHead);for(Session630 s:sessions)header.addView(dateHeader630(s));table.addView(header);
        Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear FROM athletes WHERE category=? COLLATE NOCASE AND status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",new String[]{group});
        while(a.moveToNext()){long athleteId=a.getLong(0);String name=a.getString(1);int by=a.getInt(2);TableRow row=new TableRow(this);row.addView(cell630((by>0?by+" • ":"")+name,150,false));for(Session630 s:sessions){ensureRecord630(s.id,athleteId);CheckBox cb=new CheckBox(this);cb.setGravity(Gravity.CENTER);cb.setChecked(recordPresent630(s.id,athleteId));cb.setEnabled(!s.cancelled);cb.setOnCheckedChangeListener((v,on)->setRecord630(s.id,athleteId,on));row.addView(cb,new TableRow.LayoutParams(dp(96),dp(52)));}table.addView(row);}a.close();
        host.addView(hsv,new LinearLayout.LayoutParams(-1,0,1));root.addView(host,Math.max(0,root.getChildCount()-1),new LinearLayout.LayoutParams(-1,0,1));
    }

    private View dateHeader630(Session630 s){LinearLayout col=new LinearLayout(this);col.setOrientation(LinearLayout.VERTICAL);col.setGravity(Gravity.CENTER);TextView d=cell630(dateLabel630(s.date),96,true);d.setGravity(Gravity.CENTER);CheckBox ok=new CheckBox(this);ok.setText("ONAY");ok.setTextSize(9);ok.setGravity(Gravity.CENTER);ok.setChecked(s.confirmed);ok.setEnabled(!s.cancelled);ok.setOnCheckedChangeListener((v,on)->setConfirmed630(s.id,on));col.addView(d,new LinearLayout.LayoutParams(dp(96),dp(36)));col.addView(ok,new LinearLayout.LayoutParams(dp(96),dp(40)));return col;}
    private TextView cell630(String s,int width,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(bold?11:10.5f);t.setTextColor(TEXT_630);t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(6),0,dp(6),0);t.setBackground(round630(Color.WHITE,Color.rgb(225,225,225),4,1));t.setSingleLine(false);t.setMaxLines(2);t.setLayoutParams(new TableRow.LayoutParams(dp(width),dp(52)));return t;}

    private void showAttendanceSettings630(){page="ATTENDANCE_SETTINGS_630";currentAthlete=-1;base("YOKLAMA AYARLARI",true);ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(12),dp(12),dp(12),dp(24));b.addView(tv("Grup programları ve yoklama tarihleri burada düzenlenir. Eski yoklamalar, program değişse bile kendi tarihlerinde korunur.",12,MUTED_630,false));for(String g:groupNames630()){Button x=btn(g+"\n"+scheduleSummary630(g));x.setAllCaps(false);x.setOnClickListener(v->showGroupAttendanceSettings630(g));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(66));lp.setMargins(0,dp(8),0,0);b.addView(x,lp);}}

    private void showGroupAttendanceSettings630(String group){page="ATTENDANCE_SETTINGS_GROUP_630:"+group;currentAthlete=-1;base(group+" • YOKLAMA AYARLARI",true);ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(12),dp(12),dp(12),dp(24));Button week=btn("HAFTALIK ANTRENMAN GÜNLERİ");week.setOnClickListener(v->showWeekdaySettings630(group));b.addView(week,new LinearLayout.LayoutParams(-1,dp(54)));Button add=btn("+ YOKLAMA TARİHİ EKLE");add.setOnClickListener(v->addSession630(group));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(54));ap.setMargins(0,dp(8),0,dp(10));b.addView(add,ap);ensureCurrentMonthSessions630(group);Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate,cancelled,confirmed FROM attendance_sessions WHERE groupName=? AND sessionDate>=date('now','-10 months') ORDER BY sessionDate DESC",new String[]{group});while(c.moveToNext()){long id=c.getLong(0);String date=c.getString(1);boolean cancelled=c.getInt(2)==1,confirmed=c.getInt(3)==1;Button r=btn((cancelled?"İPTAL • ":"")+dateTrFull630(date)+(confirmed?" • ONAYLI":""));r.setAllCaps(false);r.setOnClickListener(v->editSession630(group,id,date,cancelled));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(52));rp.setMargins(0,dp(6),0,0);b.addView(r,rp);}c.close();}

    private void showWeekdaySettings630(String group){String[] days={"Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi","Pazar"};boolean[] checked=currentWeekdays630(group);AlertDialog d=new AlertDialog.Builder(this).setTitle(group+" • ANTRENMAN GÜNLERİ").setMultiChoiceItems(days,checked,(x,w,on)->checked[w]=on).setPositiveButton("KAYDET",null).setNegativeButton("VAZGEÇ",null).create();d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(saveWeekdays630(group,checked)){d.dismiss();showGroupAttendanceSettings630(group);}}));d.show();}
    private boolean saveWeekdays630(String group,boolean[] sel){StringBuilder s=new StringBuilder();for(int i=0;i<7;i++)if(sel[i]){if(s.length()>0)s.append(',');s.append(i+1);}if(s.length()==0){toast("En az bir gün seçin.");return false;}ContentValues v=new ContentValues();v.put("groupName",group);v.put("effectiveFrom",ISO630.format(new Date()));v.put("weekdays",s.toString());db.getWritableDatabase().insert("attendance_schedule",null,v);ensureCurrentMonthSessions630(group);return true;}

    private void addSession630(String group){EditText e=new EditText(this);e.setHint("gg.aa.yyyy");e.setText(new SimpleDateFormat("dd.MM.yyyy",new Locale("tr","TR")).format(new Date()));new AlertDialog.Builder(this).setTitle("YOKLAMA TARİHİ EKLE").setView(e).setPositiveButton("EKLE",(d,w)->{String iso=parseDate630(e.getText().toString());if(iso==null){toast("Tarih geçersiz.");return;}ContentValues v=new ContentValues();v.put("groupName",group);v.put("sessionDate",iso);db.getWritableDatabase().insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);showGroupAttendanceSettings630(group);}).setNegativeButton("VAZGEÇ",null).show();}
    private void editSession630(String group,long id,String date,boolean cancelled){String[] opts={"TARİHİ DEĞİŞTİR",cancelled?"İPTALİ GERİ AL":"ANTRENMANI İPTAL ET"};new AlertDialog.Builder(this).setTitle(dateTrFull630(date)).setItems(opts,(d,w)->{if(w==1){ContentValues v=new ContentValues();v.put("cancelled",cancelled?0:1);if(!cancelled)v.put("confirmed",0);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});showGroupAttendanceSettings630(group);}else{EditText e=new EditText(this);e.setText(dateTrFull630(date));new AlertDialog.Builder(this).setTitle("YENİ TARİH").setView(e).setPositiveButton("KAYDET",(x,z)->{String iso=parseDate630(e.getText().toString());if(iso==null){toast("Tarih geçersiz.");return;}ContentValues v=new ContentValues();v.put("sessionDate",iso);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});showGroupAttendanceSettings630(group);}).setNegativeButton("VAZGEÇ",null).show();}}).show();}

    private boolean hasSchedule630(String g){Cursor c=db.getReadableDatabase().rawQuery("SELECT 1 FROM attendance_schedule WHERE groupName=? LIMIT 1",new String[]{g});boolean x=c.moveToFirst();c.close();return x;}
    private ArrayList<String> groupNames630(){ArrayList<String> out=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT name FROM app_groups ORDER BY sortOrder,id,name COLLATE NOCASE",null);while(c.moveToNext())out.add(c.getString(0));c.close();return out;}
    private boolean[] currentWeekdays630(String g){boolean[] a=new boolean[7];Cursor c=db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{g});if(c.moveToFirst()){String s=c.getString(0);if(s!=null)for(String z:s.split(",")){try{int n=Integer.parseInt(z.trim());if(n>=1&&n<=7)a[n-1]=true;}catch(Exception ignored){}}}c.close();return a;}
    private String scheduleSummary630(String g){boolean[] a=currentWeekdays630(g);String[] d={"Pzt","Sal","Çar","Per","Cum","Cmt","Paz"};StringBuilder s=new StringBuilder();for(int i=0;i<7;i++)if(a[i]){if(s.length()>0)s.append(" • ");s.append(d[i]);}return s.length()==0?"Program tanımlı değil":s.toString();}

    private void ensureCurrentMonthSessions630(String group){if(!hasSchedule630(group))return;Calendar c=Calendar.getInstance();int month=c.get(Calendar.MONTH);c.set(Calendar.DAY_OF_MONTH,1);while(c.get(Calendar.MONTH)==month){String date=ISO630.format(c.getTime());String wd=weekdaysForDate630(group,date);int n=weekNo630(c.get(Calendar.DAY_OF_WEEK));if(wd!=null&&containsDay630(wd,n)){ContentValues v=new ContentValues();v.put("groupName",group);v.put("sessionDate",date);db.getWritableDatabase().insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);}c.add(Calendar.DAY_OF_MONTH,1);}}
    private String weekdaysForDate630(String group,String date){Cursor c=db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? AND effectiveFrom<=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{group,date});String s=c.moveToFirst()?c.getString(0):null;c.close();return s;}
    private boolean containsDay630(String list,int n){for(String z:list.split(","))if(z.trim().equals(String.valueOf(n)))return true;return false;}
    private int weekNo630(int d){return d==Calendar.MONDAY?1:d==Calendar.TUESDAY?2:d==Calendar.WEDNESDAY?3:d==Calendar.THURSDAY?4:d==Calendar.FRIDAY?5:d==Calendar.SATURDAY?6:7;}

    private ArrayList<Session630> currentMonthSessions630(String group){ArrayList<Session630> out=new ArrayList<>();Calendar c=Calendar.getInstance();String ym=new SimpleDateFormat("yyyy-MM",Locale.US).format(c.getTime());Cursor q=db.getReadableDatabase().rawQuery("SELECT id,sessionDate,cancelled,confirmed FROM attendance_sessions WHERE groupName=? AND substr(sessionDate,1,7)=? AND cancelled=0 ORDER BY sessionDate",new String[]{group,ym});while(q.moveToNext())out.add(new Session630(q.getLong(0),q.getString(1),q.getInt(2)==1,q.getInt(3)==1));q.close();return out;}
    private void ensureRecord630(long sid,long aid){ContentValues v=new ContentValues();v.put("sessionId",sid);v.put("athleteId",aid);v.put("present",1);db.getWritableDatabase().insertWithOnConflict("attendance_records",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private boolean recordPresent630(long sid,long aid){Cursor c=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=?",new String[]{String.valueOf(sid),String.valueOf(aid)});boolean x=!c.moveToFirst()||c.getInt(0)==1;c.close();return x;}
    private void setRecord630(long sid,long aid,boolean on){ensureRecord630(sid,aid);ContentValues v=new ContentValues();v.put("present",on?1:0);db.getWritableDatabase().update("attendance_records",v,"sessionId=? AND athleteId=?",new String[]{String.valueOf(sid),String.valueOf(aid)});}
    private void setConfirmed630(long sid,boolean on){ContentValues v=new ContentValues();v.put("confirmed",on?1:0);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(sid)});}

    private String dateLabel630(String iso){try{return TR630.format(ISO630.parse(iso));}catch(Exception e){return iso;}}
    private String dateTrFull630(String iso){try{return new SimpleDateFormat("dd.MM.yyyy",new Locale("tr","TR")).format(ISO630.parse(iso));}catch(Exception e){return iso;}}
    private String parseDate630(String tr){try{return ISO630.format(new SimpleDateFormat("dd.MM.yyyy",new Locale("tr","TR")).parse(tr));}catch(Exception e){return null;}}
    private android.graphics.drawable.GradientDrawable round630(int fill,int stroke,int radius,int width){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}
    private static class Session630{long id;String date;boolean cancelled,confirmed;Session630(long i,String d,boolean c,boolean f){id=i;date=d;cancelled=c;confirmed=f;}}

    @Override void goBack(){if(page!=null&&page.startsWith("ATTENDANCE_MATRIX_630:")){invokeAttendanceGroups630();return;}if(page!=null&&page.startsWith("ATTENDANCE_SETTINGS_GROUP_630:")){showAttendanceSettings630();return;}if("ATTENDANCE_SETTINGS_630".equals(page)){showHome();return;}super.goBack();}
    private void invokeAttendanceGroups630(){try{Method m=MainActivityV628.class.getDeclaredMethod("showAttendanceGroups628");m.setAccessible(true);m.invoke(this);}catch(Exception e){showHome();}}
    @Override protected void onDestroy(){dismissPopup630();super.onDestroy();}
}
