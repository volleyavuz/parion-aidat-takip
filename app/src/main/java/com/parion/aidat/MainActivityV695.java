package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.95 - final attendance workflow: unchecked-by-default, effective-dated groups, one-month landscape PDF. */
public class MainActivityV695 extends MainActivityV694 {
    private static final int TEXT695=Color.rgb(28,28,28), MUTED695=Color.rgb(92,92,92);
    private final SimpleDateFormat ISO695=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    private final SimpleDateFormat DAY695=new SimpleDateFormat("dd.MM",new Locale("tr","TR"));
    private final SimpleDateFormat FULL695=new SimpleDateFormat("dd.MM.yyyy",new Locale("tr","TR"));

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        ensureAttendanceFinal695();
        reconcileGroupHistory695();
    }

    @Override protected void onResume(){
        super.onResume();
        ensureAttendanceFinal695();
        reconcileGroupHistory695();
        if(root!=null)root.postDelayed(()->patchAttendanceUi695(root),180);
    }

    @Override void showHome(){
        ensureAttendanceFinal695();
        reconcileGroupHistory695();
        super.showHome();
        if(root!=null)root.postDelayed(()->patchAttendanceUi695(root),220);
    }

    @Override void showProfile(long id){
        ensureAttendanceFinal695();
        reconcileGroupHistory695();
        super.showProfile(id);
    }

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.postDelayed(()->patchAttendanceUi695(root),220);
    }

    private void ensureAttendanceFinal695(){
        SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_schedule(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,effectiveFrom TEXT NOT NULL,weekdays TEXT NOT NULL)");
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_sessions(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,sessionDate TEXT NOT NULL,cancelled INTEGER NOT NULL DEFAULT 0,confirmed INTEGER NOT NULL DEFAULT 0,UNIQUE(groupName,sessionDate))");
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_records(sessionId INTEGER NOT NULL,athleteId INTEGER NOT NULL,present INTEGER NOT NULL DEFAULT 0,PRIMARY KEY(sessionId,athleteId))");
        d.execSQL("CREATE TABLE IF NOT EXISTS athlete_group_history(id INTEGER PRIMARY KEY AUTOINCREMENT,athleteId INTEGER NOT NULL,groupName TEXT NOT NULL,validFrom TEXT NOT NULL,validTo TEXT)");
        d.execSQL("CREATE INDEX IF NOT EXISTS idx_group_history_athlete ON athlete_group_history(athleteId,validFrom,validTo)");
        d.execSQL("CREATE INDEX IF NOT EXISTS idx_group_history_group ON athlete_group_history(groupName,validFrom,validTo)");
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_meta(k TEXT PRIMARY KEY,v TEXT)");
        Cursor m=d.rawQuery("SELECT 1 FROM attendance_meta WHERE k='unchecked_default_v695'",null);
        boolean done=m.moveToFirst();m.close();
        if(!done){
            // Old code auto-created 'present=1'. Only unconfirmed sessions are reset; confirmed history is preserved.
            d.execSQL("UPDATE attendance_records SET present=0 WHERE sessionId IN (SELECT id FROM attendance_sessions WHERE confirmed=0)");
            ContentValues v=new ContentValues();v.put("k","unchecked_default_v695");v.put("v",ISO695.format(new Date()));d.insertWithOnConflict("attendance_meta",null,v,SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    /** Bootstrap current membership once, then close/open rows whenever the athlete's current group changes. */
    private void reconcileGroupHistory695(){
        SQLiteDatabase d=db.getWritableDatabase();String today=ISO695.format(new Date());
        Cursor a=d.rawQuery("SELECT id,TRIM(COALESCE(category,'')),TRIM(COALESCE(startDate,'')) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);
        while(a.moveToNext()){
            long aid=a.getLong(0);String current=a.getString(1)==null?"":a.getString(1).trim();String start=a.getString(2)==null?"":a.getString(2).trim();
            Cursor h=d.rawQuery("SELECT id,groupName FROM athlete_group_history WHERE athleteId=? AND validTo IS NULL ORDER BY validFrom DESC,id DESC LIMIT 1",new String[]{String.valueOf(aid)});
            if(!h.moveToFirst()){
                h.close();
                if(!current.isEmpty()){
                    ContentValues v=new ContentValues();v.put("athleteId",aid);v.put("groupName",current);v.put("validFrom",isIso695(start)?start:today);d.insert("athlete_group_history",null,v);
                }
                continue;
            }
            long hid=h.getLong(0);String old=h.getString(1)==null?"":h.getString(1).trim();h.close();
            if(!old.equalsIgnoreCase(current)){
                ContentValues close=new ContentValues();close.put("validTo",today);d.update("athlete_group_history",close,"id=?",new String[]{String.valueOf(hid)});
                if(!current.isEmpty()){
                    ContentValues v=new ContentValues();v.put("athleteId",aid);v.put("groupName",current);v.put("validFrom",today);d.insert("athlete_group_history",null,v);
                }
            }
        }a.close();
    }

    private boolean isIso695(String s){return s!=null&&s.matches("\\d{4}-\\d{2}-\\d{2}");}

    private void patchAttendanceUi695(View v){
        if(v==null)return;
        if(v instanceof ImageButton){CharSequence cd=v.getContentDescription();if(cd!=null&&"Yoklamalar".equalsIgnoreCase(cd.toString()))v.setOnClickListener(x->invokeAttendanceGroups695());}
        if(v instanceof TextView){CharSequence cd=v.getContentDescription();if(cd!=null&&"Yoklamalar".equalsIgnoreCase(cd.toString()))v.setOnClickListener(x->invokeAttendanceGroups695());}
        if("ATTENDANCE_GROUPS_628".equals(page)&&v instanceof Button){
            Button b=(Button)v;String s=String.valueOf(b.getText()).trim();String n=s.toUpperCase(new Locale("tr","TR"));
            if(n.contains("DIŞA AKTAR")||n.contains("DİŞA AKTAR")){b.setText("YOKLAMALARI PDF OLARAK AKTAR");b.setOnClickListener(x->chooseMonthForPdf695());}
            else if(!s.isEmpty())b.setOnClickListener(x->showAttendanceMatrix695(s));
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchAttendanceUi695(g.getChildAt(i));}
    }

    private void invokeAttendanceGroups695(){
        ensureAttendanceFinal695();reconcileGroupHistory695();
        try{Method m=MainActivityV628.class.getDeclaredMethod("showAttendanceGroups628");m.setAccessible(true);m.invoke(this);if(root!=null)root.postDelayed(()->patchAttendanceUi695(root),260);}catch(Exception e){toast("Yoklamalar açılamadı.");}
    }

    private void showAttendanceMatrix695(String group){
        ensureAttendanceFinal695();reconcileGroupHistory695();ensureCurrentMonthSessions695(group);
        page="ATTENDANCE_MATRIX_695:"+group;currentAthlete=-1;base(group+" • YOKLAMA",true);
        LinearLayout host=new LinearLayout(this);host.setOrientation(LinearLayout.VERTICAL);host.setPadding(dp(6),dp(8),dp(6),dp(16));
        TextView info=tv("İşaretli = geldi • İşaretsiz = gelmedi. Sporcular artık otomatik olarak geldi işaretlenmez. ONAY kutusu işaretlenmeyen tarihler Devamsızlar hesabına katılmaz.",11,MUTED695,false);info.setPadding(dp(6),0,dp(6),dp(8));host.addView(info);
        ArrayList<Session695> sessions=currentMonthSessions695(group);
        if(sessions.isEmpty()){host.addView(tv("Bu ay için yoklama tarihi bulunmuyor. Ayarlar > Yoklama Ayarları bölümünü kontrol edin.",13,MUTED695,true));root.addView(host,Math.max(0,root.getChildCount()-1),new LinearLayout.LayoutParams(-1,0,1));return;}
        HorizontalScrollView hsv=new HorizontalScrollView(this);TableLayout table=new TableLayout(this);table.setStretchAllColumns(false);hsv.addView(table,new HorizontalScrollView.LayoutParams(-2,-2));
        TableRow header=new TableRow(this);header.addView(cell695("SPORCU",150,true));for(Session695 s:sessions)header.addView(dateHeader695(group,s));table.addView(header);
        ArrayList<Athlete695> athletes=monthRoster695(group,sessions);
        for(Athlete695 a:athletes){TableRow row=new TableRow(this);row.addView(cell695((a.birthYear>0?a.birthYear+" • ":"")+a.name,150,false));for(Session695 s:sessions){boolean member=isMemberOn695(a.id,group,s.date,s.id);CheckBox cb=new CheckBox(this);cb.setGravity(Gravity.CENTER);cb.setEnabled(member&&!s.cancelled);cb.setChecked(member&&recordPresent695(s.id,a.id));if(member)ensureAbsentRecord695(s.id,a.id);cb.setOnCheckedChangeListener((v,on)->{if(v.isPressed())setRecord695(s.id,a.id,on);});row.addView(cb,new TableRow.LayoutParams(dp(96),dp(52)));}table.addView(row);}
        host.addView(hsv,new LinearLayout.LayoutParams(-1,0,1));root.addView(host,Math.max(0,root.getChildCount()-1),new LinearLayout.LayoutParams(-1,0,1));
    }

    private View dateHeader695(String group,Session695 s){
        LinearLayout col=new LinearLayout(this);col.setOrientation(LinearLayout.VERTICAL);col.setGravity(Gravity.CENTER);TextView d=cell695(dateLabel695(s.date),96,true);d.setGravity(Gravity.CENTER);CheckBox ok=new CheckBox(this);ok.setText("ONAY");ok.setTextSize(9);ok.setGravity(Gravity.CENTER);ok.setChecked(s.confirmed);ok.setEnabled(!s.cancelled);ok.setOnCheckedChangeListener((v,on)->{setConfirmed695(s.id,on);if(on)materializeRosterAsAbsent695(group,s);});col.addView(d,new LinearLayout.LayoutParams(dp(96),dp(36)));col.addView(ok,new LinearLayout.LayoutParams(dp(96),dp(40)));return col;
    }

    private TextView cell695(String s,int width,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(bold?11:10.5f);t.setTextColor(TEXT695);t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(6),0,dp(6),0);t.setBackground(round695(Color.WHITE,Color.rgb(225,225,225),4,1));t.setSingleLine(false);t.setMaxLines(2);t.setLayoutParams(new TableRow.LayoutParams(dp(width),dp(52)));return t;}

    private void ensureCurrentMonthSessions695(String group){
        Calendar c=Calendar.getInstance();String ym=new SimpleDateFormat("yyyy-MM",Locale.US).format(c.getTime());ensureSessionsForMonth695(group,ym);
    }
    private void ensureSessionsForMonth695(String group,String ym){
        int y,m;try{String[] p=ym.split("-");y=Integer.parseInt(p[0]);m=Integer.parseInt(p[1])-1;}catch(Exception e){return;}Calendar c=Calendar.getInstance();c.set(y,m,1,12,0,0);int month=m;while(c.get(Calendar.MONTH)==month){String date=ISO695.format(c.getTime());String wd=weekdaysForDate695(group,date);int n=weekNo695(c.get(Calendar.DAY_OF_WEEK));if(wd!=null&&containsDay695(wd,n)){ContentValues v=new ContentValues();v.put("groupName",group);v.put("sessionDate",date);db.getWritableDatabase().insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);}c.add(Calendar.DAY_OF_MONTH,1);}
    }
    private String weekdaysForDate695(String group,String date){Cursor c=db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? AND effectiveFrom<=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{group,date});String s=c.moveToFirst()?c.getString(0):null;c.close();return s;}
    private boolean containsDay695(String list,int n){if(list==null)return false;for(String z:list.split(","))if(z.trim().equals(String.valueOf(n)))return true;return false;}
    private int weekNo695(int d){return d==Calendar.MONDAY?1:d==Calendar.TUESDAY?2:d==Calendar.WEDNESDAY?3:d==Calendar.THURSDAY?4:d==Calendar.FRIDAY?5:d==Calendar.SATURDAY?6:7;}
    private ArrayList<Session695> currentMonthSessions695(String group){String ym=new SimpleDateFormat("yyyy-MM",Locale.US).format(new Date());return sessionsForMonth695(group,ym);}
    private ArrayList<Session695> sessionsForMonth695(String group,String ym){ArrayList<Session695> out=new ArrayList<>();Cursor q=db.getReadableDatabase().rawQuery("SELECT id,sessionDate,cancelled,confirmed FROM attendance_sessions WHERE groupName=? AND substr(sessionDate,1,7)=? ORDER BY sessionDate",new String[]{group,ym});while(q.moveToNext())out.add(new Session695(q.getLong(0),q.getString(1),q.getInt(2)==1,q.getInt(3)==1));q.close();return out;}

    private ArrayList<Athlete695> monthRoster695(String group,ArrayList<Session695> sessions){
        LinkedHashMap<Long,Athlete695> out=new LinkedHashMap<>();
        for(Session695 s:sessions){Cursor c=rosterCursor695(group,s.date,s.id);while(c.moveToNext()){long id=c.getLong(0);if(!out.containsKey(id))out.put(id,new Athlete695(id,c.getString(1),c.getInt(2)));}c.close();}
        ArrayList<Athlete695> list=new ArrayList<>(out.values());Collections.sort(list,(x,y)->x.name.compareToIgnoreCase(y.name));return list;
    }
    private Cursor rosterCursor695(String group,String date,long sid){
        String sql="SELECT DISTINCT a.id,a.name,a.birthYear FROM athletes a WHERE TRIM(COALESCE(a.deletedAt,''))='' AND a.status='AKTİF' AND (EXISTS(SELECT 1 FROM athlete_group_history h WHERE h.athleteId=a.id AND h.groupName=? COLLATE NOCASE AND h.validFrom<=? AND (h.validTo IS NULL OR ?<h.validTo)) OR EXISTS(SELECT 1 FROM attendance_records r JOIN attendance_sessions s ON s.id=r.sessionId WHERE r.athleteId=a.id AND s.id=? AND s.groupName=? COLLATE NOCASE)) ORDER BY a.name COLLATE NOCASE";
        return db.getReadableDatabase().rawQuery(sql,new String[]{group,date,date,String.valueOf(sid),group});
    }
    private boolean isMemberOn695(long aid,String group,String date,long sid){Cursor c=db.getReadableDatabase().rawQuery("SELECT 1 WHERE EXISTS(SELECT 1 FROM athlete_group_history h WHERE h.athleteId=? AND h.groupName=? COLLATE NOCASE AND h.validFrom<=? AND (h.validTo IS NULL OR ?<h.validTo)) OR EXISTS(SELECT 1 FROM attendance_records r JOIN attendance_sessions s ON s.id=r.sessionId WHERE r.athleteId=? AND s.id=? AND s.groupName=? COLLATE NOCASE) LIMIT 1",new String[]{String.valueOf(aid),group,date,date,String.valueOf(aid),String.valueOf(sid),group});boolean x=c.moveToFirst();c.close();return x;}

    private void ensureAbsentRecord695(long sid,long aid){ContentValues v=new ContentValues();v.put("sessionId",sid);v.put("athleteId",aid);v.put("present",0);db.getWritableDatabase().insertWithOnConflict("attendance_records",null,v,SQLiteDatabase.CONFLICT_IGNORE);}
    private boolean recordPresent695(long sid,long aid){Cursor c=db.getReadableDatabase().rawQuery("SELECT present FROM attendance_records WHERE sessionId=? AND athleteId=?",new String[]{String.valueOf(sid),String.valueOf(aid)});boolean x=c.moveToFirst()&&c.getInt(0)==1;c.close();return x;}
    private void setRecord695(long sid,long aid,boolean on){ensureAbsentRecord695(sid,aid);ContentValues v=new ContentValues();v.put("present",on?1:0);db.getWritableDatabase().update("attendance_records",v,"sessionId=? AND athleteId=?",new String[]{String.valueOf(sid),String.valueOf(aid)});}
    private void setConfirmed695(long sid,boolean on){ContentValues v=new ContentValues();v.put("confirmed",on?1:0);db.getWritableDatabase().update("attendance_sessions",v,"id=?",new String[]{String.valueOf(sid)});}
    private void materializeRosterAsAbsent695(String group,Session695 s){Cursor c=rosterCursor695(group,s.date,s.id);while(c.moveToNext())ensureAbsentRecord695(s.id,c.getLong(0));c.close();}

    private void chooseMonthForPdf695(){
        ensureAttendanceFinal695();reconcileGroupHistory695();ArrayList<String> yms=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT DISTINCT substr(sessionDate,1,7) ym FROM attendance_sessions WHERE length(sessionDate)>=7 ORDER BY ym DESC",null);while(c.moveToNext())if(c.getString(0)!=null)yms.add(c.getString(0));c.close();String now=new SimpleDateFormat("yyyy-MM",Locale.US).format(new Date());if(!yms.contains(now))yms.add(0,now);if(yms.isEmpty()){toast("Dışa aktarılacak yoklama ayı bulunamadı.");return;}String[] labels=new String[yms.size()];for(int i=0;i<yms.size();i++)labels[i]=monthLabel695(yms.get(i));new AlertDialog.Builder(this).setTitle("PDF İÇİN AY SEÇİN").setItems(labels,(d,w)->exportPdfMonth695(yms.get(w))).show();
    }

    private void exportPdfMonth695(String ym){
        try{
            ArrayList<String> groups=groupNames695();for(String g:groups)ensureSessionsForMonth695(g,ym);
            PdfDocument pdf=new PdfDocument();int pageNo=0;boolean any=false;
            final int PW=842,PH=595,M=24,TOP=68,BOTTOM=28,NAMEW=180,ROWH=22;
            Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);text.setColor(Color.BLACK);Paint line=new Paint(Paint.ANTI_ALIAS_FLAG);line.setStyle(Paint.Style.STROKE);line.setStrokeWidth(0.7f);line.setColor(Color.rgb(175,175,175));
            for(String group:groups){ArrayList<Session695> all=sessionsForMonth695(group,ym);if(all.isEmpty())continue;ArrayList<Athlete695> athletes=monthRoster695(group,all);if(athletes.isEmpty())continue;any=true;
                int usable=PW-2*M-NAMEW;int maxCols=Math.max(1,usable/42);for(int cs=0;cs<all.size();cs+=maxCols){int ce=Math.min(all.size(),cs+maxCols);ArrayList<Session695> sessions=new ArrayList<>(all.subList(cs,ce));float dateW=(PW-2f*M-NAMEW)/sessions.size();int rowsPerPage=Math.max(1,(PH-TOP-BOTTOM-ROWH)/ROWH);for(int rs=0;rs<athletes.size();rs+=rowsPerPage){int re=Math.min(athletes.size(),rs+rowsPerPage);pageNo++;PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(PW,PH,pageNo).create());Canvas cv=page.getCanvas();
                    text.setTypeface(Typeface.DEFAULT_BOLD);text.setTextSize(15);cv.drawText("PARİON YOKLAMA RAPORU",M,25,text);text.setTextSize(11);cv.drawText(group+" • "+monthLabel695(ym),M,43,text);text.setTypeface(Typeface.DEFAULT);text.setTextSize(8);cv.drawText("G = Geldi   - = Gelmedi   ? = Yoklama onaysız   İ = Antrenman iptal",M,57,text);
                    float y=TOP;drawCell695(cv,line,text,M,y,NAMEW,ROWH,"SPORCU",true,9);for(int j=0;j<sessions.size();j++)drawCell695(cv,line,text,M+NAMEW+j*dateW,y,dateW,ROWH,dateLabel695(sessions.get(j).date),true,8);y+=ROWH;
                    for(int r=rs;r<re;r++){Athlete695 a=athletes.get(r);drawCell695(cv,line,text,M,y,NAMEW,ROWH,(a.birthYear>0?a.birthYear+" • ":"")+a.name,false,7.7f);for(int j=0;j<sessions.size();j++){Session695 s=sessions.get(j);String mark="";if(isMemberOn695(a.id,group,s.date,s.id)){if(s.cancelled)mark="İ";else if(!s.confirmed)mark="?";else mark=recordPresent695(s.id,a.id)?"G":"-";}drawCell695(cv,line,text,M+NAMEW+j*dateW,y,dateW,ROWH,mark,false,9);}y+=ROWH;}
                    pdf.finishPage(page);
                }}
            }
            if(!any){pdf.close();toast(monthLabel695(ym)+" için yoklama bulunamadı.");return;}
            String file="Parion_Yoklama_"+ym.replace('-','_')+".pdf";ContentValues v=new ContentValues();v.put(MediaStore.Downloads.DISPLAY_NAME,file);v.put(MediaStore.Downloads.MIME_TYPE,"application/pdf");Uri u=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);if(u==null)throw new Exception("download uri");OutputStream o=getContentResolver().openOutputStream(u);pdf.writeTo(o);if(o!=null)o.close();pdf.close();toast(monthLabel695(ym)+" yoklamaları PDF olarak dışa aktarıldı.");
        }catch(Exception e){toast("PDF oluşturulamadı.");}
    }

    private void drawCell695(Canvas c,Paint line,Paint text,float x,float y,float w,float h,String s,boolean bold,float size){c.drawRect(x,y,x+w,y+h,line);text.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);text.setTextSize(size);text.setColor(Color.BLACK);String z=s==null?"":s;float tw=text.measureText(z);while(tw>w-6&&z.length()>3){z=z.substring(0,z.length()-2)+"…";tw=text.measureText(z);}float tx=x+Math.max(3,(w-tw)/2);Paint.FontMetrics fm=text.getFontMetrics();float ty=y+(h-(fm.descent+fm.ascent))/2;c.drawText(z,tx,ty,text);}
    private ArrayList<String> groupNames695(){ArrayList<String> out=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT name FROM app_groups ORDER BY sortOrder,id,name COLLATE NOCASE",null);while(c.moveToNext())out.add(c.getString(0));c.close();return out;}
    private String monthLabel695(String ym){try{String[] p=ym.split("-");int m=Integer.parseInt(p[1]);String[] a={"OCAK","ŞUBAT","MART","NİSAN","MAYIS","HAZİRAN","TEMMUZ","AĞUSTOS","EYLÜL","EKİM","KASIM","ARALIK"};return a[m-1]+" "+p[0];}catch(Exception e){return ym;}}
    private String dateLabel695(String iso){try{return DAY695.format(ISO695.parse(iso));}catch(Exception e){return iso;}}
    private GradientDrawable round695(int fill,int stroke,int radius,int width){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;}

    @Override void goBack(){if(page!=null&&page.startsWith("ATTENDANCE_MATRIX_695:")){invokeAttendanceGroups695();return;}super.goBack();}

    private static class Session695{long id;String date;boolean cancelled,confirmed;Session695(long i,String d,boolean c,boolean f){id=i;date=d;cancelled=c;confirmed=f;}}
    private static class Athlete695{long id;String name;int birthYear;Athlete695(long i,String n,int b){id=i;name=n==null?"":n;birthYear=b;}}
}
