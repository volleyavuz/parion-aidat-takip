package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.28 - attendance scheduling, confirmed attendance, absentee dashboard and export. */
public class MainActivityV628 extends MainActivityV627 {
    private static final int GOLD_628=Color.rgb(205,156,34), TEXT_628=Color.rgb(28,28,28), MUTED_628=Color.rgb(92,92,92);
    private final SimpleDateFormat ISO628=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    private final SimpleDateFormat TRDATE628=new SimpleDateFormat("dd.MM.yyyy",new Locale("tr","TR"));

    @Override public void onCreate(android.os.Bundle b){super.onCreate(b);ensureAttendance628();cleanupAttendance628();}

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(()->{
            removeHomeAthletesCard628(root);
            hookAttendanceRail628(root);
            addAbsenteeDashboard628();
        });
    }

    private void ensureAttendance628(){
        if(db==null)return;SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_schedules(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,effectiveFrom TEXT NOT NULL,weekMask INTEGER NOT NULL,createdAt TEXT NOT NULL,UNIQUE(groupName,effectiveFrom))");
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_sessions(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,sessionDate TEXT NOT NULL,confirmed INTEGER NOT NULL DEFAULT 0,cancelled INTEGER NOT NULL DEFAULT 0,createdAt TEXT NOT NULL,UNIQUE(groupName,sessionDate))");
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_entries(id INTEGER PRIMARY KEY AUTOINCREMENT,sessionId INTEGER NOT NULL,athleteId INTEGER NOT NULL,present INTEGER NOT NULL DEFAULT 1,UNIQUE(sessionId,athleteId))");
        d.execSQL("CREATE INDEX IF NOT EXISTS idx_att_sessions_group_date ON attendance_sessions(groupName,sessionDate)");
        d.execSQL("CREATE INDEX IF NOT EXISTS idx_att_entries_athlete ON attendance_entries(athleteId)");
    }

    private void cleanupAttendance628(){
        ensureAttendance628();SQLiteDatabase d=db.getWritableDatabase();
        Calendar c=Calendar.getInstance();c.add(Calendar.MONTH,-10);String cut=ISO628.format(c.getTime());
        d.execSQL("DELETE FROM attendance_entries WHERE sessionId IN (SELECT id FROM attendance_sessions WHERE sessionDate<?)",new Object[]{cut});
        d.delete("attendance_sessions","sessionDate<?",new String[]{cut});
        d.delete("attendance_schedules","effectiveFrom<? AND id NOT IN (SELECT MAX(id) FROM attendance_schedules GROUP BY groupName)",new String[]{cut});
    }

    private void hookAttendanceRail628(View v){
        if(v instanceof ImageButton){ImageButton b=(ImageButton)v;CharSequence d=b.getContentDescription();if(d!=null&&"Yoklamalar".equalsIgnoreCase(d.toString()))b.setOnClickListener(x->showAttendanceGroups628());}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)hookAttendanceRail628(g.getChildAt(i));}
    }

    private void removeHomeAthletesCard628(View v){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){
            View c=g.getChildAt(i);
            if(containsExactText628(c,"SPORCULAR")){
                if(c instanceof Button || c.isClickable() || hasClickable628(c)){g.removeViewAt(i);continue;}
            }
            removeHomeAthletesCard628(c);
        }
    }
    private boolean containsExactText628(View v,String s){if(v instanceof TextView&&s.equalsIgnoreCase(String.valueOf(((TextView)v).getText()).trim()))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(containsExactText628(g.getChildAt(i),s))return true;}return false;}
    private boolean hasClickable628(View v){if(v.isClickable()||v.hasOnClickListeners())return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(hasClickable628(g.getChildAt(i)))return true;}return false;}

    private ArrayList<String> groups628(){
        ensureAttendance628();ArrayList<String> out=new ArrayList<>();SQLiteDatabase d=db.getReadableDatabase();
        try{Cursor c=d.rawQuery("SELECT name FROM app_groups ORDER BY sortOrder,id,name COLLATE NOCASE",null);while(c.moveToNext())out.add(c.getString(0));c.close();}catch(Exception ignored){}
        if(out.isEmpty()){Cursor c=d.rawQuery("SELECT DISTINCT TRIM(category) FROM athletes WHERE TRIM(COALESCE(category,''))<>'' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY 1 COLLATE NOCASE",null);while(c.moveToNext())out.add(c.getString(0));c.close();}
        return out;
    }

    private void showAttendanceGroups628(){
        ensureAttendance628();cleanupAttendance628();page="ATT_GROUPS_628";currentAthlete=-1;base("YOKLAMALAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(12),dp(12),dp(12),dp(24));
        TextView info=tv("Bir grup seçin. İlk kullanımda grubun haftalık antrenman günleri sorulur; yalnızca seçilen günlerin yoklamaları oluşturulur.",12,MUTED_628,false);b.addView(info);
        for(String g:groups628()){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(9),dp(10),dp(9));row.setBackground(roundStroke628(Color.WHITE,Color.rgb(225,225,225),12,1));
            LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv(g,14,TEXT_628,true));t.addView(tv(groupActiveCount628(g)+" aktif sporcu",11,MUTED_628,false));row.addView(t,new LinearLayout.LayoutParams(0,-2,1));
            TextView chevron=tv("›",28,GOLD_628,true);row.addView(chevron,new LinearLayout.LayoutParams(dp(38),-1));row.setOnClickListener(v->openGroupAttendance628(g));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(8));b.addView(row,lp);
        }
        Button export=btn("YOKLAMALARI DIŞA AKTAR");export.setOnClickListener(v->chooseExport628());LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,dp(56));ep.setMargins(0,dp(10),0,0);b.addView(export,ep);
    }

    private int groupActiveCount628(String g){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE category=? COLLATE NOCASE AND status='AKTİF' AND TRIM(COALESCE(deletedAt,''))=''",new String[]{g});int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}

    private void openGroupAttendance628(String group){
        if(!hasSchedule628(group)){askSchedule628(group,true);return;}generateCurrentMonthSessions628(group);showGroupSessions628(group);
    }

    private boolean hasSchedule628(String group){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM attendance_schedules WHERE groupName=? COLLATE NOCASE",new String[]{group});boolean x=c.moveToFirst()&&c.getInt(0)>0;c.close();return x;}

    private void askSchedule628(String group,boolean first){
        final String[] days={"Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi","Pazar"};final boolean[] checked=new boolean[7];
        int current=currentWeekMask628(group,new Date());for(int i=0;i<7;i++)checked[i]=(current&(1<<i))!=0;
        new AlertDialog.Builder(this).setTitle(group+" • ANTRENMAN GÜNLERİ").setMessage("Grubun haftalık antrenman yaptığı günleri seçin. Program daha sonra değişirse yeni günler bugünden itibaren geçerli olur; eski yoklamalar değişmez.").setMultiChoiceItems(days,checked,(d,w,is)->checked[w]=is).setPositiveButton("KAYDET",(d,w)->{
            int mask=0;for(int i=0;i<7;i++)if(checked[i])mask|=(1<<i);if(mask==0){toast("En az bir antrenman günü seçmelisiniz.");return;}saveSchedule628(group,mask,first);generateCurrentMonthSessions628(group);showGroupSessions628(group);
        }).setNegativeButton("VAZGEÇ",null).show();
    }

    private int currentWeekMask628(String group,Date when){String date=ISO628.format(when);Cursor c=db.getReadableDatabase().rawQuery("SELECT weekMask FROM attendance_schedules WHERE groupName=? COLLATE NOCASE AND effectiveFrom<=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{group,date});int m=0;if(c.moveToFirst())m=c.getInt(0);c.close();return m;}

    private void saveSchedule628(String group,int mask,boolean first){
        SQLiteDatabase d=db.getWritableDatabase();Calendar now=Calendar.getInstance();String eff;
        if(first){now.set(Calendar.DAY_OF_MONTH,1);eff=ISO628.format(now.getTime());}else eff=ISO628.format(new Date());
        ContentValues v=new ContentValues();v.put("groupName",group);v.put("effectiveFrom",eff);v.put("weekMask",mask);v.put("createdAt",ISO628.format(new Date()));d.insertWithOnConflict("attendance_schedules",null,v,SQLiteDatabase.CONFLICT_REPLACE);
        if(!first){d.execSQL("UPDATE attendance_sessions SET cancelled=1 WHERE groupName=? COLLATE NOCASE AND sessionDate>=? AND confirmed=0",new Object[]{group,eff});}
    }

    private void generateCurrentMonthSessions628(String group){
        SQLiteDatabase d=db.getWritableDatabase();Calendar c=Calendar.getInstance();int y=c.get(Calendar.YEAR),m=c.get(Calendar.MONTH);c.set(Calendar.DAY_OF_MONTH,1);int max=c.getActualMaximum(Calendar.DAY_OF_MONTH);
        for(int day=1;day<=max;day++){c.set(Calendar.DAY_OF_MONTH,day);int mask=currentWeekMask628(group,c.getTime());if(mask==0)continue;int idx=(c.get(Calendar.DAY_OF_WEEK)+5)%7;if((mask&(1<<idx))==0)continue;String ds=ISO628.format(c.getTime());ContentValues v=new ContentValues();v.put("groupName",group);v.put("sessionDate",ds);v.put("createdAt",ISO628.format(new Date()));d.insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    }

    private void showGroupSessions628(String group){
        page="ATT_SESSIONS_628|"+group;currentAthlete=-1;base(group+" • YOKLAMA",true);ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(12),dp(10),dp(12),dp(24));
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER_VERTICAL);Button program=miniButton628("PROGRAM GÜNLERİ");Button add=miniButton628("+ ÖZEL GÜN");program.setOnClickListener(v->askSchedule628(group,false));add.setOnClickListener(v->addCustomSession628(group));actions.addView(program,new LinearLayout.LayoutParams(0,dp(48),1));actions.addView(add,new LinearLayout.LayoutParams(0,dp(48),1));b.addView(actions);
        TextView hint=tv("✓ işaretli yoklamalar dashboard hesaplarına dahil edilir. İptal edilen veya onaylanmamış yoklamalar devamsızlık hesabına katılmaz.",11,MUTED_628,false);hint.setPadding(dp(4),dp(8),dp(4),dp(10));b.addView(hint);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate,confirmed,cancelled FROM attendance_sessions WHERE groupName=? COLLATE NOCASE AND sessionDate>=date('now','-10 months') ORDER BY sessionDate DESC",new String[]{group});
        while(c.moveToNext()){long id=c.getLong(0);String date=c.getString(1);boolean confirmed=c.getInt(2)==1,cancelled=c.getInt(3)==1;addSessionRow628(b,group,id,date,confirmed,cancelled);}c.close();
    }

    private void addSessionRow628(LinearLayout b,String group,long id,String date,boolean confirmed,boolean cancelled){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(8),dp(6),dp(6),dp(6));row.setBackground(roundStroke628(Color.WHITE,cancelled?Color.LTGRAY:(confirmed?Color.rgb(9,242,153):Color.rgb(225,225,225)),12,1));
        CheckBox ok=new CheckBox(this);ok.setChecked(confirmed);ok.setEnabled(!cancelled);ok.setContentDescription("Yoklama onayı");ok.setOnCheckedChangeListener((v,is)->setSessionConfirmed628(id,is));row.addView(ok,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout texts=new LinearLayout(this);texts.setOrientation(LinearLayout.VERTICAL);texts.addView(tv(prettyDate628(date),13,TEXT_628,true));texts.addView(tv(cancelled?"İPTAL":(confirmed?"ONAYLI":"ONAY BEKLİYOR"),10,cancelled?MUTED_628:(confirmed?Color.rgb(0,130,75):Color.rgb(165,100,0)),true));row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
        Button edit=miniButton628("DÜZENLE");edit.setOnClickListener(v->editSession628(group,id,date,cancelled));row.addView(edit,new LinearLayout.LayoutParams(dp(88),dp(42)));row.setOnClickListener(v->showAttendanceSheet628(group,id,date));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);
    }

    private void setSessionConfirmed628(long id,boolean is){ContentValues v=new ContentValues();v.put("confirmed",is?1:0);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});if(is)ensureEntriesForSession628(id);}

    private void ensureEntriesForSession628(long sessionId){
        SQLiteDatabase d=db.getWritableDatabase();Cursor s=d.rawQuery("SELECT groupName FROM attendance_sessions WHERE id=?",new String[]{String.valueOf(sessionId)});if(!s.moveToFirst()){s.close();return;}String group=s.getString(0);s.close();
        Cursor a=d.rawQuery("SELECT id FROM athletes WHERE category=? COLLATE NOCASE AND status='AKTİF' AND TRIM(COALESCE(deletedAt,''))=''",new String[]{group});while(a.moveToNext()){ContentValues v=new ContentValues();v.put("sessionId",sessionId);v.put("athleteId",a.getLong(0));v.put("present",1);d.insertWithOnConflict("attendance_entries",null,v,SQLiteDatabase.CONFLICT_IGNORE);}a.close();
    }

    private void showAttendanceSheet628(String group,long sessionId,String date){
        ensureEntriesForSession628(sessionId);page="ATT_SHEET_628|"+group+"|"+sessionId;base(group+" • "+prettyDate628(date),true);ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(12),dp(8),dp(12),dp(24));
        Cursor ss=db.getReadableDatabase().rawQuery("SELECT confirmed,cancelled FROM attendance_sessions WHERE id=?",new String[]{String.valueOf(sessionId)});boolean conf=false,cancel=false;if(ss.moveToFirst()){conf=ss.getInt(0)==1;cancel=ss.getInt(1)==1;}ss.close();
        CheckBox master=new CheckBox(this);master.setText("BU YOKLAMAYI ONAYLA • Dashboard'a dahil et");master.setTextSize(12);master.setTypeface(Typeface.DEFAULT,Typeface.BOLD);master.setChecked(conf);master.setEnabled(!cancel);b.addView(master,new LinearLayout.LayoutParams(-1,dp(52)));
        ArrayList<CheckBox> checks=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT a.id,a.name,a.birthYear,COALESCE(e.present,1) present FROM athletes a LEFT JOIN attendance_entries e ON e.athleteId=a.id AND e.sessionId=? WHERE a.category=? COLLATE NOCASE AND a.status='AKTİF' AND TRIM(COALESCE(a.deletedAt,''))='' ORDER BY a.name COLLATE NOCASE",new String[]{String.valueOf(sessionId),group});
        while(c.moveToNext()){long athleteId=c.getLong(0);String name=c.getString(1);int by=c.getInt(2);boolean present=c.getInt(3)==1;CheckBox cb=new CheckBox(this);cb.setText((by>0?by+" • ":"")+name);cb.setTextSize(13);cb.setChecked(present);cb.setTag(athleteId);cb.setPadding(dp(6),dp(3),dp(6),dp(3));b.addView(cb,new LinearLayout.LayoutParams(-1,dp(48)));checks.add(cb);}c.close();
        Button save=btn("YOKLAMAYI KAYDET");save.setEnabled(!cancel);save.setOnClickListener(v->{SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{for(CheckBox cb:checks){ContentValues x=new ContentValues();x.put("sessionId",sessionId);x.put("athleteId",(Long)cb.getTag());x.put("present",cb.isChecked()?1:0);d.insertWithOnConflict("attendance_entries",null,x,SQLiteDatabase.CONFLICT_REPLACE);}ContentValues svv=new ContentValues();svv.put("confirmed",master.isChecked()?1:0);d.update("attendance_sessions",svv,"id=?",new String[]{String.valueOf(sessionId)});d.setTransactionSuccessful();}finally{d.endTransaction();}toast("Yoklama kaydedildi.");showGroupSessions628(group);});LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(58));sp.setMargins(0,dp(12),0,0);b.addView(save,sp);
    }

    private void addCustomSession628(String group){DatePickerDialog d=new DatePickerDialog(this,(v,y,m,day)->{Calendar c=Calendar.getInstance();c.set(y,m,day);String ds=ISO628.format(c.getTime());ContentValues x=new ContentValues();x.put("groupName",group);x.put("sessionDate",ds);x.put("createdAt",ISO628.format(new Date()));long id=db.getWritableDatabase().insertWithOnConflict("attendance_sessions",null,x,SQLiteDatabase.CONFLICT_IGNORE);toast(id<0?"Bu tarih zaten listede.":"Yoklama günü eklendi.");showGroupSessions628(group);},Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH));d.show();}

    private void editSession628(String group,long id,String oldDate,boolean cancelled){
        String[] opts=cancelled?new String[]{"İPTALİ KALDIR","TARİHİ DEĞİŞTİR"}:new String[]{"ANTRENMANI İPTAL ET","TARİHİ DEĞİŞTİR"};
        new AlertDialog.Builder(this).setTitle(prettyDate628(oldDate)).setItems(opts,(d,w)->{if(w==0){ContentValues v=new ContentValues();v.put("cancelled",cancelled?0:1);if(!cancelled)v.put("confirmed",0);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});showGroupSessions628(group);}else{Calendar c=parseIsoCal628(oldDate);new DatePickerDialog(this,(vv,y,m,day)->{Calendar n=Calendar.getInstance();n.set(y,m,day);ContentValues v=new ContentValues();v.put("sessionDate",ISO628.format(n.getTime()));int count=db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});toast(count>0?"Tarih güncellendi.":"Tarih güncellenemedi.");showGroupSessions628(group);},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}}).show();
    }

    private void addAbsenteeDashboard628(){
        if(root==null||!"HOME".equals(page)||findTag628(root,"v628-absentee-card")!=null)return;ScrollView sv=findScroll628(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;LinearLayout box=(LinearLayout)sv.getChildAt(0);
        ArrayList<Absent628> abs=absentees628();LinearLayout card=new LinearLayout(this);card.setTag("v628-absentee-card");card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(10),dp(12),dp(10));card.setBackground(roundStroke628(Color.WHITE,abs.isEmpty()?Color.rgb(220,210,180):Color.rgb(205,75,75),14,1));
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.addView(tv("DEVAMSIZLAR",12,TEXT_628,true),new LinearLayout.LayoutParams(0,-2,1));TextView n=tv(String.valueOf(abs.size()),22,abs.isEmpty()?GOLD_628:Color.rgb(190,45,45),true);head.addView(n);card.addView(head);
        if(abs.isEmpty())card.addView(tv("Son 15 günde düzenli yoklama verisine göre devamsız sporcu yok.",10,MUTED_628,false));else{int lim=Math.min(5,abs.size());for(int i=0;i<lim;i++){Absent628 a=abs.get(i);TextView r=tv(a.name+"  •  "+a.days+" gündür gelmiyor",11,TEXT_628,true);r.setPadding(dp(4),dp(5),dp(4),dp(5));final long id=a.id;r.setOnClickListener(v->showProfile(id));card.addView(r);}if(abs.size()>5){TextView more=tv("Tümünü Gör ("+abs.size()+") ›",11,GOLD_628,true);more.setOnClickListener(v->showAbsentees628());card.addView(more);}}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(10),0,dp(8));box.addView(card,lp);
    }

    private static class Absent628{long id;String name;int days;Absent628(long i,String n,int d){id=i;name=n;days=d;}}
    private ArrayList<Absent628> absentees628(){
        ensureAttendance628();ArrayList<Absent628> out=new ArrayList<>();String today=ISO628.format(new Date());SQLiteDatabase d=db.getReadableDatabase();
        String sql="SELECT a.id,a.name,MAX(CASE WHEN e.present=1 THEN s.sessionDate END) lastPresent,MIN(s.sessionDate) firstTracked FROM athletes a JOIN attendance_entries e ON e.athleteId=a.id JOIN attendance_sessions s ON s.id=e.sessionId AND s.confirmed=1 AND s.cancelled=0 AND s.sessionDate<=? WHERE a.status='AKTİF' AND TRIM(COALESCE(a.deletedAt,''))='' GROUP BY a.id,a.name";
        Cursor c=d.rawQuery(sql,new String[]{today});while(c.moveToNext()){long id=c.getLong(0);String name=c.getString(1),last=c.getString(2),first=c.getString(3);String ref=(last==null||last.isEmpty())?first:last;if(ref==null||ref.isEmpty())continue;int days=daysBetween628(ref,today);if(days>=15)out.add(new Absent628(id,name,days));}c.close();Collections.sort(out,(a,b)->Integer.compare(b.days,a.days));return out;
    }

    private void showAbsentees628(){page="ABSENTEES_628";base("DEVAMSIZLAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);ArrayList<Absent628> list=absentees628();for(Absent628 a:list){TextView r=tv(a.name+"\n"+a.days+" gündür gelmiyor",13,TEXT_628,true);r.setBackground(roundStroke628(Color.WHITE,Color.rgb(230,190,190),12,1));r.setPadding(dp(12),dp(9),dp(12),dp(9));final long id=a.id;r.setOnClickListener(v->showProfile(id));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);}}

    private void chooseExport628(){
        final String[] periods={"Son 1 ay","Son 3 ay","Son 6 ay","Son 10 ay"};new AlertDialog.Builder(this).setTitle("ZAMAN ARALIĞI").setItems(periods,(d,w)->{int months=new int[]{1,3,6,10}[w];String[] types={"PDF","EXCEL (CSV)"};new AlertDialog.Builder(this).setTitle("DOSYA FORMATI").setItems(types,(dd,t)->{if(t==0)exportPdf628(months);else exportCsv628(months);}).show();}).show();
    }

    private Cursor exportCursor628(int months){Calendar c=Calendar.getInstance();c.add(Calendar.MONTH,-months);String cut=ISO628.format(c.getTime());return db.getReadableDatabase().rawQuery("SELECT s.groupName,s.sessionDate,a.name,a.birthYear,e.present FROM attendance_sessions s JOIN attendance_entries e ON e.sessionId=s.id JOIN athletes a ON a.id=e.athleteId WHERE s.confirmed=1 AND s.cancelled=0 AND s.sessionDate>=? ORDER BY s.groupName,s.sessionDate,a.name COLLATE NOCASE",new String[]{cut});}

    private void exportCsv628(int months){
        try{String name="Parion_Yoklama_"+months+"ay_"+new SimpleDateFormat("yyyyMMdd_HHmm",Locale.US).format(new Date())+".csv";Uri uri=createDownloadUri628(name,"text/csv");if(uri==null){toast("Dosya oluşturulamadı.");return;}OutputStream os=getContentResolver().openOutputStream(uri);os.write("Grup;Tarih;Doğum Yılı;Sporcu;Durum\n".getBytes("UTF-8"));Cursor c=exportCursor628(months);while(c.moveToNext()){String line=safeCsv628(c.getString(0))+";"+prettyDate628(c.getString(1))+";"+c.getInt(3)+";"+safeCsv628(c.getString(2))+";"+(c.getInt(4)==1?"GELDİ":"GELMEDİ")+"\n";os.write(line.getBytes("UTF-8"));}c.close();os.close();shareUri628(uri,"text/csv","Parion yoklama Excel çıktısı");}catch(Exception e){toast("Excel çıktısı oluşturulamadı.");}
    }

    private void exportPdf628(int months){
        try{String name="Parion_Yoklama_"+months+"ay_"+new SimpleDateFormat("yyyyMMdd_HHmm",Locale.US).format(new Date())+".pdf";Uri uri=createDownloadUri628(name,"application/pdf");if(uri==null){toast("Dosya oluşturulamadı.");return;}PdfDocument doc=new PdfDocument();Paint p=new Paint(1);int pageNo=1,y=0;PdfDocument.Page page=null;Canvas canvas=null;Cursor c=exportCursor628(months);String current="";while(c.moveToNext()){String line=c.getString(0)+" • "+prettyDate628(c.getString(1))+" • "+c.getInt(3)+" • "+c.getString(2)+" • "+(c.getInt(4)==1?"GELDİ":"GELMEDİ");if(page==null||y>790){if(page!=null)doc.finishPage(page);page=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo++).create());canvas=page.getCanvas();canvas.drawColor(Color.WHITE);p.setColor(Color.BLACK);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(16);canvas.drawText("PARİON YOKLAMA RAPORU • SON "+months+" AY",28,34,p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(9);y=58;}canvas.drawText(trimPdf628(line,105),28,y,p);y+=14;}c.close();if(page!=null)doc.finishPage(page);OutputStream os=getContentResolver().openOutputStream(uri);doc.writeTo(os);os.close();doc.close();shareUri628(uri,"application/pdf","Parion yoklama PDF çıktısı");}catch(Exception e){toast("PDF oluşturulamadı.");}
    }

    private Uri createDownloadUri628(String name,String mime){if(Build.VERSION.SDK_INT>=29){ContentValues v=new ContentValues();v.put(MediaStore.Downloads.DISPLAY_NAME,name);v.put(MediaStore.Downloads.MIME_TYPE,mime);v.put(MediaStore.Downloads.RELATIVE_PATH,"Download/Parion");return getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);}return null;}
    private void shareUri628(Uri uri,String mime,String title){Intent i=new Intent(Intent.ACTION_SEND);i.setType(mime);i.putExtra(Intent.EXTRA_STREAM,uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,title));}

    private String safeCsv628(String s){if(s==null)return "";return '"'+s.replace("\"","\"\"")+'"';}
    private String trimPdf628(String s,int max){return s==null?"":(s.length()<=max?s:s.substring(0,max-1)+"…");}
    private String prettyDate628(String iso){try{return TRDATE628.format(ISO628.parse(iso));}catch(Exception e){return iso==null?"":iso;}}
    private Calendar parseIsoCal628(String iso){Calendar c=Calendar.getInstance();try{c.setTime(ISO628.parse(iso));}catch(Exception ignored){}return c;}
    private int daysBetween628(String a,String b){try{return (int)Math.floor((ISO628.parse(b).getTime()-ISO628.parse(a).getTime())/86400000d);}catch(Exception e){return 0;}}
    private Button miniButton628(String s){Button b=new Button(this);b.setText(s);b.setTextSize(10.5f);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(roundStroke628(Color.WHITE,GOLD_628,10,1));return b;}
    private GradientDrawable roundStroke628(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}
    private View findTag628(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findTag628(g.getChildAt(i),tag);if(r!=null)return r;}}return null;}
    private ScrollView findScroll628(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll628(g.getChildAt(i));if(s!=null)return s;}}return null;}

    @Override void goBack(){
        if(page!=null&&page.startsWith("ATT_SHEET_628|")){String[] p=page.split("\\|");if(p.length>1){showGroupSessions628(p[1]);return;}}
        if(page!=null&&page.startsWith("ATT_SESSIONS_628|")){showAttendanceGroups628();return;}
        if("ATT_GROUPS_628".equals(page)||"ABSENTEES_628".equals(page)){showHome();return;}
        super.goBack();
    }
}
