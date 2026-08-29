package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
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
            addAbsenteesCard628();
        });
    }

    private void ensureAttendance628(){
        SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_schedule(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,effectiveFrom TEXT NOT NULL,weekdays TEXT NOT NULL)");
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_sessions(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,sessionDate TEXT NOT NULL,cancelled INTEGER NOT NULL DEFAULT 0,confirmed INTEGER NOT NULL DEFAULT 0,UNIQUE(groupName,sessionDate))");
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_records(sessionId INTEGER NOT NULL,athleteId INTEGER NOT NULL,present INTEGER NOT NULL DEFAULT 1,PRIMARY KEY(sessionId,athleteId))");
    }
    private void cleanupAttendance628(){
        ensureAttendance628();Calendar c=Calendar.getInstance();c.add(Calendar.MONTH,-10);String cutoff=ISO628.format(c.getTime());SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("DELETE FROM attendance_records WHERE sessionId IN (SELECT id FROM attendance_sessions WHERE sessionDate<?)",new Object[]{cutoff});
        d.delete("attendance_sessions","sessionDate<?",new String[]{cutoff});
    }

    private void hookAttendanceRail628(View v){
        if(v instanceof ImageButton){ImageButton b=(ImageButton)v;CharSequence cd=b.getContentDescription();if(cd!=null&&"Yoklamalar".equalsIgnoreCase(cd.toString())){b.setOnClickListener(x->showAttendanceGroups628());return;}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)hookAttendanceRail628(g.getChildAt(i));}
    }
    private void removeHomeAthletesCard628(View v){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){View c=g.getChildAt(i);if(c instanceof TextView){String t=String.valueOf(((TextView)c).getText()).trim();if("SPORCULAR".equalsIgnoreCase(t)){View kill=nearestClickable628(c);ViewParent p=kill.getParent();if(p instanceof ViewGroup){((ViewGroup)p).removeView(kill);continue;}}}removeHomeAthletesCard628(c);}
    }
    private View nearestClickable628(View v){View cur=v,best=v;while(cur!=null&&cur!=root){if(cur.isClickable()||cur.hasOnClickListeners())best=cur;ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;}return best;}

    private void showAttendanceGroups628(){
        ensureAttendance628();page="ATTENDANCE_GROUPS_628";currentAthlete=-1;base("YOKLAMALAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(12),dp(12),dp(12),dp(24));
        Button export=btn("YOKLAMALARI DIŞA AKTAR");export.setOnClickListener(v->chooseExport628());b.addView(export,new LinearLayout.LayoutParams(-1,dp(54)));
        ArrayList<String> gs=attendanceGroups628();if(gs.isEmpty()){b.addView(tv("Tanımlı grup bulunamadı.",14,MUTED_628,true));return;}for(String g:gs){Button x=btn(g);x.setOnClickListener(v->openGroupAttendance628(g));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));lp.setMargins(0,dp(8),0,0);b.addView(x,lp);}
    }
    private ArrayList<String> attendanceGroups628(){ArrayList<String> a=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT name FROM app_groups ORDER BY sortOrder,id,name COLLATE NOCASE",null);while(c.moveToNext())a.add(c.getString(0));c.close();return a;}

    private void openGroupAttendance628(String group){
        if(!hasSchedule628(group)){askSchedule628(group,true);return;}
        page="ATTENDANCE_GROUP_628:"+group;base(group+" • YOKLAMA",true);ScrollView sv=scroll();LinearLayout b=box(sv);b.setPadding(dp(12),dp(12),dp(12),dp(24));
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);Button program=btn("PROGRAM");Button add=btn("+ GÜN EKLE");program.setOnClickListener(v->askSchedule628(group,false));add.setOnClickListener(v->addManualSession628(group));actions.addView(program,new LinearLayout.LayoutParams(0,dp(50),1));actions.addView(add,new LinearLayout.LayoutParams(0,dp(50),1));b.addView(actions);
        ensureMonthSessions628(group);Cursor c=db.getReadableDatabase().rawQuery("SELECT id,sessionDate,cancelled,confirmed FROM attendance_sessions WHERE groupName=? AND sessionDate>=date('now','-10 months') ORDER BY sessionDate DESC",new String[]{group});
        while(c.moveToNext()){long id=c.getLong(0);String date=c.getString(1);boolean cancelled=c.getInt(2)==1,confirmed=c.getInt(3)==1;LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(8),dp(7),dp(8),dp(7));row.setBackground(roundStroke628(Color.WHITE,Color.LTGRAY,12,1));CheckBox ok=new CheckBox(this);ok.setChecked(confirmed);ok.setText(cancelled?"İPTAL • "+dateTr628(date):dateTr628(date));ok.setEnabled(!cancelled);ok.setOnCheckedChangeListener((x,v)->setSessionConfirmed628(id,v));row.addView(ok,new LinearLayout.LayoutParams(0,-2,1));Button edit=mini628("DÜZENLE");edit.setOnClickListener(v->editSession628(group,id,date,cancelled));row.addView(edit,new LinearLayout.LayoutParams(dp(90),dp(42)));row.setOnClickListener(v->{if(!cancelled)showSession628(group,id,date);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,0);b.addView(row,lp);}c.close();
    }

    private boolean hasSchedule628(String g){Cursor c=db.getReadableDatabase().rawQuery("SELECT 1 FROM attendance_schedule WHERE groupName=? LIMIT 1",new String[]{g});boolean x=c.moveToFirst();c.close();return x;}
    private void askSchedule628(String group,boolean first){
        String[] days={"Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi","Pazar"};boolean[] checked=currentWeekdays628(group);new AlertDialog.Builder(this).setTitle(group+" • ANTRENMAN GÜNLERİ").setMultiChoiceItems(days,checked,(d,w,on)->checked[w]=on).setMessage(first?"Bu grubun haftalık antrenman günlerini seçin. Sonraki aylarda bu programa göre yoklama günleri hazırlanır.":"Yeni program bugünden itibaren geçerli olur. Eski yoklama tarihleri değişmez.").setPositiveButton("KAYDET",(d,w)->saveSchedule628(group,checked)).setNegativeButton("VAZGEÇ",null).show();
    }
    private boolean[] currentWeekdays628(String g){boolean[] a=new boolean[7];Cursor c=db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{g});if(c.moveToFirst()){String s=c.getString(0);if(s!=null)for(String z:s.split(",")){try{int n=Integer.parseInt(z);if(n>=1&&n<=7)a[n-1]=true;}catch(Exception ignored){}}}c.close();return a;}
    private void saveSchedule628(String group,boolean[] selected){StringBuilder s=new StringBuilder();for(int i=0;i<7;i++)if(selected[i]){if(s.length()>0)s.append(',');s.append(i+1);}if(s.length()==0){toast("En az bir gün seçin.");return;}ContentValues v=new ContentValues();v.put("groupName",group);v.put("effectiveFrom",ISO628.format(new Date()));v.put("weekdays",s.toString());db.getWritableDatabase().insert("attendance_schedule",null,v);ensureMonthSessions628(group);openGroupAttendance628(group);}

    private void ensureMonthSessions628(String group){Calendar c=Calendar.getInstance();int y=c.get(Calendar.YEAR),m=c.get(Calendar.MONTH);c.set(Calendar.DAY_OF_MONTH,1);while(c.get(Calendar.MONTH)==m){String date=ISO628.format(c.getTime());String weekdays=weekdaysForDate628(group,date);if(weekdays!=null&&weekdays.contains(String.valueOf(androidWeekTo628(c.get(Calendar.DAY_OF_WEEK))))){ContentValues v=new ContentValues();v.put("groupName",group);v.put("sessionDate",date);db.getWritableDatabase().insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);}c.add(Calendar.DAY_OF_MONTH,1);}}
    private int androidWeekTo628(int d){return d==Calendar.MONDAY?1:d==Calendar.TUESDAY?2:d==Calendar.WEDNESDAY?3:d==Calendar.THURSDAY?4:d==Calendar.FRIDAY?5:d==Calendar.SATURDAY?6:7;}
    private String weekdaysForDate628(String group,String date){Cursor c=db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? AND effectiveFrom<=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{group,date});String s=c.moveToFirst()?c.getString(0):null;c.close();return s;}

    private void showSession628(String group,long sid,String date){page="ATTENDANCE_SESSION_628:"+group+":"+sid;base(group+" • "+dateTr628(date),true);ScrollView sv=scroll();LinearLayout b=box(sv);Cursor a=db.getReadableDatabase().rawQuery("SELECT id,name,birthYear FROM athletes WHERE category=? COLLATE NOCASE AND status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",new String[]{group});while(a.moveToNext()){long id=a.getLong(0);String name=a.getString(1);int by=a.getInt(2);boolean present=recordPresent628(sid,id);CheckBox cb=new CheckBox(this);cb.setChecked(present);cb.setText((by>0?by+" • ":"")+name);cb.setTextSize(14);cb.setPadding(dp(8),dp(6),dp(8),dp(6));cb.setOnCheckedChangeListener((v,on)->setRecord628(sid,id,on));b.addView(cb,new LinearLayout.LayoutParams(-1,dp(52)));ensureRecord628(sid,id);}a.close();TextView note=tv("İşaretli = geldi • İşaretsiz = gelmedi. Bu yoklamanın dashboard hesabına katılması için önceki ekrandaki yoklama onay kutusunu işaretleyin.",11,MUTED_628,false);b.addView(note);}
    private void ensureRecord628(long s,long a){ContentValues v=new ContentValues();v.put("sessionId",s);v.put("athleteId",a);v.put("present",1);db.getWritableDatabase().insertWithOnConflict("attendance_records",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private boolean recordPresent628(long s,long a){Cursor c=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=?",new String[]{String.valueOf(s),String.valueOf(a)});boolean x=!c.moveToFirst()||c.getInt(0)==1;c.close();return x;}
    private void enqueueAttendancePending628(String kind,String key){
        if(db==null||kind==null||key==null||key.trim().isEmpty())return;
        try{
            SQLiteDatabase d=db.getWritableDatabase();
            d.execSQL("CREATE TABLE IF NOT EXISTS pending_sync(kind TEXT NOT NULL,entity_key TEXT NOT NULL,created_at INTEGER NOT NULL,PRIMARY KEY(kind,entity_key))");
            ContentValues p=new ContentValues();p.put("kind",kind);p.put("entity_key",key);p.put("created_at",System.currentTimeMillis());
            d.insertWithOnConflict("pending_sync",null,p,SQLiteDatabase.CONFLICT_REPLACE);
        }catch(Exception ignored){}
    }
    private String sessionKey628(long sid){
        Cursor c=null;try{c=db.getReadableDatabase().rawQuery("SELECT groupName,sessionDate FROM attendance_sessions WHERE id=? LIMIT 1",new String[]{String.valueOf(sid)});if(c.moveToFirst())return c.getString(0)+"|"+c.getString(1);}catch(Exception ignored){}finally{if(c!=null)c.close();}return "";
    }
    private void setRecord628(long s,long a,boolean on){ensureRecord628(s,a);ContentValues v=new ContentValues();v.put("present",on?1:0);db.getWritableDatabase().update("attendance_records",v,"sessionId=? AND athleteId=?",new String[]{String.valueOf(s),String.valueOf(a)});String k=sessionKey628(s);if(!k.isEmpty())enqueueAttendancePending628("ATT_RECORD",k+"|"+a);}
    private void setSessionConfirmed628(long id,boolean on){ContentValues v=new ContentValues();v.put("confirmed",on?1:0);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});String k=sessionKey628(id);if(!k.isEmpty())enqueueAttendancePending628("ATT_SESSION",k);}

    private void addManualSession628(String group){EditText e=new EditText(this);e.setHint("gg.aa.yyyy");e.setText(TRDATE628.format(new Date()));new AlertDialog.Builder(this).setTitle("YOKLAMA GÜNÜ EKLE").setView(e).setPositiveButton("EKLE",(d,w)->{String iso=toIso628(e.getText().toString());if(iso==null){toast("Tarih geçersiz.");return;}ContentValues v=new ContentValues();v.put("groupName",group);v.put("sessionDate",iso);db.getWritableDatabase().insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);openGroupAttendance628(group);}).setNegativeButton("VAZGEÇ",null).show();}
    private void editSession628(String group,long id,String date,boolean cancelled){String[] opts={"TARİHİ DEĞİŞTİR",cancelled?"İPTALİ GERİ AL":"ANTRENMANI İPTAL ET"};new AlertDialog.Builder(this).setTitle(dateTr628(date)).setItems(opts,(d,w)->{if(w==1){ContentValues v=new ContentValues();v.put("cancelled",cancelled?0:1);if(!cancelled)v.put("confirmed",0);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});openGroupAttendance628(group);}else{EditText e=new EditText(this);e.setText(dateTr628(date));new AlertDialog.Builder(this).setTitle("YENİ TARİH").setView(e).setPositiveButton("KAYDET",(x,z)->{String iso=toIso628(e.getText().toString());if(iso==null)return;ContentValues v=new ContentValues();v.put("sessionDate",iso);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(id)});openGroupAttendance628(group);}).setNegativeButton("VAZGEÇ",null).show();}}).show();}

    private void addAbsenteesCard628(){ScrollView sv=findScroll628(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;LinearLayout b=(LinearLayout)sv.getChildAt(0);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(12),dp(12),dp(12));card.setBackground(roundStroke628(Color.WHITE,Color.rgb(190,60,60),14,1));TextView h=tv("DEVAMSIZLAR",12,TEXT_628,true);card.addView(h);Cursor c=absentees628();int n=0;while(c.moveToNext()){long id=c.getLong(0);String name=c.getString(1);int days=c.getInt(2);TextView r=tv(name+" • "+days+" gündür gelmiyor",12,days>=30?Color.rgb(170,30,30):TEXT_628,true);r.setOnClickListener(v->showProfile(id));card.addView(r);n++;}c.close();if(n==0)card.addView(tv("15 gün ve üzeri devamsız sporcu yok.",11,MUTED_628,false));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(12),0,dp(8));b.addView(card,lp);}
    private Cursor absentees628(){String sql="SELECT a.id,a.name,CAST(julianday('now')-julianday(MAX(CASE WHEN r.present=1 AND s.confirmed=1 AND s.cancelled=0 THEN s.sessionDate END)) AS INTEGER) days FROM athletes a JOIN attendance_records r ON r.athleteId=a.id JOIN attendance_sessions s ON s.id=r.sessionId WHERE a.status='AKTİF' AND TRIM(COALESCE(a.deletedAt,''))='' GROUP BY a.id,a.name HAVING days>=15 ORDER BY days DESC,a.name";return db.getReadableDatabase().rawQuery(sql,null);}

    private void chooseExport628(){String[] range={"Son 1 ay","Son 3 ay","Son 6 ay","Son 10 ay"};new AlertDialog.Builder(this).setTitle("ZAMAN ARALIĞI").setItems(range,(d,w)->chooseFormat628(new int[]{1,3,6,10}[w])).show();}
    private void chooseFormat628(int months){String[] fmt={"PDF","EXCEL / CSV"};new AlertDialog.Builder(this).setTitle("DIŞA AKTARMA BİÇİMİ").setItems(fmt,(d,w)->{if(w==0)exportPdf628(months);else exportCsv628(months);}).show();}
    private Cursor exportCursor628(int months){return db.getReadableDatabase().rawQuery("SELECT s.sessionDate,s.groupName,a.name,r.present,s.confirmed,s.cancelled FROM attendance_sessions s LEFT JOIN attendance_records r ON r.sessionId=s.id LEFT JOIN athletes a ON a.id=r.athleteId WHERE s.sessionDate>=date('now','-"+months+" months') ORDER BY s.sessionDate,s.groupName,a.name",null);}
    private void exportCsv628(int months){try{String name="Parion_Yoklama_"+months+"Ay.csv";ContentValues v=new ContentValues();v.put(MediaStore.Downloads.DISPLAY_NAME,name);v.put(MediaStore.Downloads.MIME_TYPE,"text/csv");Uri u=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);OutputStream o=getContentResolver().openOutputStream(u);o.write("Tarih;Grup;Sporcu;Durum;Yoklama Onaylı;İptal\n".getBytes("UTF-8"));Cursor c=exportCursor628(months);while(c.moveToNext()){String line=dateTr628(c.getString(0))+";"+safe628(c.getString(1))+";"+safe628(c.getString(2))+";"+(c.getInt(3)==1?"Geldi":"Gelmedi")+";"+(c.getInt(4)==1?"Evet":"Hayır")+";"+(c.getInt(5)==1?"Evet":"Hayır")+"\n";o.write(line.getBytes("UTF-8"));}c.close();o.close();toast("Excel/CSV dışa aktarıldı.");}catch(Exception e){toast("Dışa aktarma başarısız.");}}
    private void exportPdf628(int months){try{PdfDocument pdf=new PdfDocument();Cursor c=exportCursor628(months);int pageNo=1,y=70;PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(842,1191,pageNo).create());Canvas canvas=page.getCanvas();Paint p=new Paint(1);p.setTextSize(18);p.setTypeface(Typeface.DEFAULT_BOLD);canvas.drawText("PARİON YOKLAMA RAPORU • SON "+months+" AY",40,40,p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(10);while(c.moveToNext()){if(y>1140){pdf.finishPage(page);pageNo++;page=pdf.startPage(new PdfDocument.PageInfo.Builder(842,1191,pageNo).create());canvas=page.getCanvas();y=45;}String line=dateTr628(c.getString(0))+"  "+safe628(c.getString(1))+"  "+safe628(c.getString(2))+"  "+(c.getInt(3)==1?"GELDİ":"GELMEDİ")+(c.getInt(4)==1?"  ✓":"  [ONAYSIZ]")+(c.getInt(5)==1?"  İPTAL":"");canvas.drawText(line,40,y,p);y+=17;}c.close();pdf.finishPage(page);String name="Parion_Yoklama_"+months+"Ay.pdf";ContentValues v=new ContentValues();v.put(MediaStore.Downloads.DISPLAY_NAME,name);v.put(MediaStore.Downloads.MIME_TYPE,"application/pdf");Uri u=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);OutputStream o=getContentResolver().openOutputStream(u);pdf.writeTo(o);o.close();pdf.close();toast("PDF dışa aktarıldı.");}catch(Exception e){toast("PDF oluşturulamadı.");}}

    private Button mini628(String s){Button b=new Button(this);b.setText(s);b.setTextSize(10);b.setAllCaps(false);return b;}
    private String dateTr628(String iso){try{return TRDATE628.format(ISO628.parse(iso));}catch(Exception e){return iso;}}
    private String toIso628(String tr){try{return ISO628.format(TRDATE628.parse(tr));}catch(Exception e){return null;}}
    private String safe628(String s){return s==null?"":s.replace(';',',');}
    private ScrollView findScroll628(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll628(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private GradientDrawable roundStroke628(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}

    @Override void goBack(){if(page!=null&&page.startsWith("ATTENDANCE_SESSION_628:")){String[] p=page.split(":");openGroupAttendance628(p.length>1?p[1]:"");return;}if(page!=null&&page.startsWith("ATTENDANCE_GROUP_628:")){showAttendanceGroups628();return;}if("ATTENDANCE_GROUPS_628".equals(page)){showHome();return;}super.goBack();}
}
