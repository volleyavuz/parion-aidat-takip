package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;

/** v4.2.7 - manual snapshot controls + normal conflict-checked attendance delta + exit-save prompt. */
public class MainActivityV731 extends MainActivityV730 {
    private final ExecutorService sync731=Executors.newSingleThreadExecutor();
    private volatile boolean attendanceRunning731=false;
    private volatile boolean forceExit731=false;

    @Override void showCloudMenu(){
        String[] items={"GÜVENLİ SENKRONİZE ET (DELTA)","BULUTA YÜKLE (SNAPSHOT)","BULUTTAN ÇEK (SNAPSHOT)","BULUTTAN TEMİZ GERİ YÜKLE","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("SENKRONİZASYON").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else if(w==1)confirmSnapshotUpload731();
            else if(w==2)confirmSnapshotPull731();
            else if(w==3)super.showCloudMenu();
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    @Override void syncFromCloud(boolean announce){
        super.syncFromCloud(announce);
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty()||db==null)return;
        if(!attendanceRunning731){attendanceRunning731=true;sync731.execute(()->{try{syncAttendanceDelta731(announce);}finally{attendanceRunning731=false;}});}
    }

    private void confirmSnapshotUpload731(){
        new AlertDialog.Builder(this).setTitle("BULUTA YÜKLE • SNAPSHOT")
            .setMessage("Bu cihazdaki sporcu, ödeme, aidat ve yoklama verilerinin TAM KOPYASI buluta gönderilir. Yoklama snapshotı buluttaki mevcut yoklama kopyasını bu cihazdaki kopyayla değiştirir.\n\nBu işlem yalnızca bilinçli manuel kullanım içindir. Devam edilsin mi?")
            .setNegativeButton("VAZGEÇ",null).setPositiveButton("BULUTA YÜKLE",(d,w)->snapshotUpload731()).show();
    }

    private void snapshotUpload731(){
        if(!hasSession731()){toast("BULUT OTURUMU YOK.");return;}
        toast("SNAPSHOT BULUTA YÜKLENİYOR...");
        sync731.execute(()->{
            try{
                JSONObject mobile=buildMobileSnapshot731();
                HttpResult a=postRpc731("parion_manual_mobile_snapshot_v427",mobile);
                if(a.code<200||a.code>=300)throw new IllegalStateException("Sporcu snapshot HTTP "+a.code);
                JSONObject att=buildAttendancePayload731(false);
                HttpResult b=postRpc731("parion_manual_attendance_snapshot_v427",att);
                if(b.code<200||b.code>=300)throw new IllegalStateException("Yoklama snapshot HTTP "+b.code);
                markAthleteBaselines731();pullAttendance731(true);
                runOnUiThread(()->toast("BULUTA SNAPSHOT YÜKLEME TAMAMLANDI"));
            }catch(Exception e){runOnUiThread(()->toast("SNAPSHOT YÜKLENEMEDİ • "+msg731(e)));}
        });
    }

    private void confirmSnapshotPull731(){
        new AlertDialog.Builder(this).setTitle("BULUTTAN ÇEK • SNAPSHOT")
            .setMessage("Bulut ana kaynak kabul edilir. Bu cihazdaki yerel sporcu, ödeme, aidat ve yoklama kopyası temizlenip buluttan yeniden oluşturulur.\n\nBu cihazdaki gönderilmemiş değişiklikler kaybolur. Buluta veri gönderilmez. Devam edilsin mi?")
            .setNegativeButton("VAZGEÇ",null).setPositiveButton("BULUTTAN ÇEK",(d,w)->snapshotPull731()).show();
    }

    private void snapshotPull731(){
        if(!hasSession731()){toast("BULUT OTURUMU YOK.");return;}
        toast("BULUT SNAPSHOT'I BU CİHAZA ALINIYOR...");
        try{
            SQLiteDatabase x=db.getWritableDatabase();x.beginTransaction();
            try{
                x.delete("payment_recent",null,null);x.delete("sync_state",null,null);x.delete("athlete_restart_periods",null,null);
                x.delete("fee_history",null,null);x.delete("payments",null,null);x.delete("athletes",null,null);
                x.delete("attendance_records",null,null);x.delete("attendance_sessions",null,null);x.delete("attendance_schedule",null,null);
                x.setTransactionSuccessful();
            }finally{x.endTransaction();}
            super.syncFromCloud(true); // empty-local path is pull-only
            sync731.execute(()->{try{pullAttendance731(true);waitAndMarkAthletes731();runOnUiThread(()->{toast("BULUT SNAPSHOT'I ALINDI");showHome();});}catch(Exception e){runOnUiThread(()->toast("BULUTTAN SNAPSHOT ALINAMADI • "+msg731(e)));}});
        }catch(Exception e){toast("BULUTTAN SNAPSHOT BAŞLATILAMADI • "+msg731(e));}
    }

    private void syncAttendanceDelta731(boolean announce){
        try{
            JSONObject body=buildAttendancePayload731(true);
            int dirty=body.getJSONArray("p_schedule").length()+body.getJSONArray("p_sessions").length()+body.getJSONArray("p_records").length();
            if(dirty>0){
                HttpResult r=postRpc731("parion_sync_attendance_delta_v3",body);
                if(r.code<200||r.code>=300)throw new IllegalStateException("Yoklama delta HTTP "+r.code);
                JSONObject out=new JSONObject(r.body);JSONArray conflicts=out.optJSONArray("conflicts");
                if(conflicts!=null&&conflicts.length()>0){String text=attendanceConflictText731(conflicts);runOnUiThread(()->new AlertDialog.Builder(this).setTitle("YOKLAMA SENKRONİZASYON ÇAKIŞMASI").setMessage("Aynı yoklama kayıtları başka bir cihazda da değişmiş. Veri ezilmedi.\n\n"+text).setPositiveButton("TAMAM",null).show());return;}
            }
            pullAttendance731(false);
            if(announce&&dirty>0)runOnUiThread(()->toast(dirty+" YOKLAMA DEĞİŞİKLİĞİ BULUTLA EŞİTLENDİ"));
        }catch(Exception e){if(announce)runOnUiThread(()->toast("YOKLAMA SENKRONİZASYONU DURDU • "+msg731(e)));}
    }

    private JSONObject buildMobileSnapshot731()throws Exception{
        JSONArray aa=new JSONArray(),pp=new JSONArray(),ff=new JSONArray();SQLiteDatabase d=db.getReadableDatabase();
        Cursor a=d.rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY id",null);
        while(a.moveToNext())aa.put(athleteJson731(a));a.close();
        Cursor p=d.rawQuery("SELECT athleteId,year,month,marker,amount FROM payments ORDER BY athleteId,year,month",null);
        while(p.moveToNext())pp.put(new JSONObject().put("legacy_id",p.getLong(0)).put("year",p.getInt(1)).put("month",p.getInt(2)).put("marker",nz731(p.getString(3))).put("amount",p.getInt(4)));p.close();
        Cursor f=d.rawQuery("SELECT athleteId,year,effectiveMonth,fee FROM fee_history ORDER BY athleteId,year,effectiveMonth",null);
        while(f.moveToNext())ff.put(new JSONObject().put("legacy_id",f.getLong(0)).put("year",f.getInt(1)).put("month",f.getInt(2)).put("fee",f.getInt(3)));f.close();
        return new JSONObject().put("p_athletes",aa).put("p_payments",pp).put("p_fees",ff);
    }

    private JSONObject athleteJson731(Cursor a)throws Exception{
        JSONObject x=new JSONObject();long id=a.getLong(a.getColumnIndexOrThrow("id"));x.put("legacy_id",id);
        String[][] m={{"seq","seq"},{"birth_year","birthYear"},{"birth_date","birthDate"},{"name","name"},{"category","category"},{"status","status"},{"monthly_fee","monthlyFee"},{"sibling","sibling"},{"tshirt_qty","tshirtQty"},{"tshirt_paid","tshirtPaid"},{"tracksuit_qty","tracksuitQty"},{"tracksuit_paid","tracksuitPaid"},{"notes","notes"},{"phone","phone"},{"mother_name","motherName"},{"mother_phone","motherPhone"},{"father_name","fatherName"},{"father_phone","fatherPhone"},{"start_date","startDate"},{"end_date","endDate"},{"restart_date","restartDate"},{"tckn","tckn"},{"photo_path","photo"}};
        for(String[] z:m){int i=a.getColumnIndex(z[1]);if(i<0||a.isNull(i))x.put(z[0],"");else if(a.getType(i)==Cursor.FIELD_TYPE_INTEGER)x.put(z[0],a.getLong(i));else x.put(z[0],nz731(a.getString(i)));}
        int si=a.getColumnIndex("summerCall"),wi=a.getColumnIndex("winterCall");x.put("summer_call",si>=0&&a.getInt(si)==1);x.put("winter_call",wi>=0&&a.getInt(wi)==1);return x;
    }

    private JSONObject buildAttendancePayload731(boolean onlyDirty)throws Exception{
        JSONArray sch=new JSONArray(),ses=new JSONArray(),rec=new JSONArray();SQLiteDatabase d=db.getReadableDatabase();
        ensureAttendanceTables731(d);
        Cursor s=d.rawQuery("SELECT groupName,effectiveFrom,weekdays FROM attendance_schedule ORDER BY groupName,effectiveFrom",null);
        while(s.moveToNext()){String g=s.getString(0),date=s.getString(1),w=nz731(s.getString(2)),key=g+"|"+date,h=hash731(w);if(!onlyDirty||dirtyState731("ATT_SCHEDULE",key,h))sch.put(new JSONObject().put("group_name",g).put("effective_from",date).put("weekdays",w).put("base_updated_at",cloudBase731("ATT_SCHEDULE",key)));}s.close();
        Cursor q=d.rawQuery("SELECT groupName,sessionDate,cancelled,confirmed FROM attendance_sessions ORDER BY groupName,sessionDate",null);
        while(q.moveToNext()){String g=q.getString(0),date=q.getString(1),key=g+"|"+date;boolean ca=q.getInt(2)==1,co=q.getInt(3)==1;String h=hash731(ca+"|"+co);if(!onlyDirty||dirtyState731("ATT_SESSION",key,h))ses.put(new JSONObject().put("group_name",g).put("session_date",date).put("cancelled",ca).put("confirmed",co).put("base_updated_at",cloudBase731("ATT_SESSION",key)));}q.close();
        Cursor r=d.rawQuery("SELECT s.groupName,s.sessionDate,r.athleteId,r.present FROM attendance_records r JOIN attendance_sessions s ON s.id=r.sessionId ORDER BY s.groupName,s.sessionDate,r.athleteId",null);
        while(r.moveToNext()){String g=r.getString(0),date=r.getString(1);long aid=r.getLong(2);boolean pr=r.getInt(3)==1;String key=g+"|"+date+"|"+aid,h=hash731(String.valueOf(pr));if(!onlyDirty||dirtyState731("ATT_RECORD",key,h))rec.put(new JSONObject().put("group_name",g).put("session_date",date).put("athlete_id",aid).put("present",pr).put("base_updated_at",cloudBase731("ATT_RECORD",key)));}r.close();
        return new JSONObject().put("p_schedule",sch).put("p_sessions",ses).put("p_records",rec);
    }

    private void pullAttendance731(boolean replace)throws Exception{
        HttpResult a=getAuthed("/rest/v1/mobile_attendance_schedule?select=group_name,effective_from,weekdays,updated_at&order=effective_from.asc");
        HttpResult b=getAuthed("/rest/v1/mobile_attendance_sessions?select=group_name,session_date,cancelled,confirmed,updated_at&order=session_date.asc");
        HttpResult c=getAuthed("/rest/v1/mobile_attendance_records?select=group_name,session_date,athlete_id,present,updated_at&order=session_date.asc");
        if(a.code<200||a.code>=300||b.code<200||b.code>=300||c.code<200||c.code>=300)throw new IllegalStateException("Yoklama bulut okuma hatası");
        JSONArray sa=new JSONArray(a.body),se=new JSONArray(b.body),re=new JSONArray(c.body);SQLiteDatabase d=db.getWritableDatabase();ensureAttendanceTables731(d);d.beginTransaction();
        try{
            if(replace){d.delete("attendance_records",null,null);d.delete("attendance_sessions",null,null);d.delete("attendance_schedule",null,null);d.delete("sync_state","entity LIKE 'ATT_%'",null);}
            for(int i=0;i<sa.length();i++){JSONObject o=sa.getJSONObject(i);String g=o.optString("group_name",""),dt=o.optString("effective_from",""),w=o.optString("weekdays","");ContentValues v=new ContentValues();v.put("groupName",g);v.put("effectiveFrom",dt);v.put("weekdays",w);d.insertWithOnConflict("attendance_schedule",null,v,SQLiteDatabase.CONFLICT_IGNORE);markState731(d,"ATT_SCHEDULE",g+"|"+dt,hash731(w),o.optString("updated_at",""));}
            for(int i=0;i<se.length();i++){JSONObject o=se.getJSONObject(i);String g=o.optString("group_name",""),dt=o.optString("session_date","");ContentValues v=new ContentValues();v.put("groupName",g);v.put("sessionDate",dt);v.put("cancelled",o.optBoolean("cancelled",false)?1:0);v.put("confirmed",o.optBoolean("confirmed",false)?1:0);d.insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_IGNORE);d.update("attendance_sessions",v,"groupName=? AND sessionDate=?",new String[]{g,dt});markState731(d,"ATT_SESSION",g+"|"+dt,hash731(o.optBoolean("cancelled",false)+"|"+o.optBoolean("confirmed",false)),o.optString("updated_at",""));}
            for(int i=0;i<re.length();i++){JSONObject o=re.getJSONObject(i);String g=o.optString("group_name",""),dt=o.optString("session_date","");long aid=o.optLong("athlete_id",-1);Cursor z=d.rawQuery("SELECT id FROM attendance_sessions WHERE groupName=? AND sessionDate=? LIMIT 1",new String[]{g,dt});if(!z.moveToFirst()){z.close();continue;}long sid=z.getLong(0);z.close();boolean pr=o.optBoolean("present",true);ContentValues v=new ContentValues();v.put("sessionId",sid);v.put("athleteId",aid);v.put("present",pr?1:0);d.insertWithOnConflict("attendance_records",null,v,SQLiteDatabase.CONFLICT_REPLACE);markState731(d,"ATT_RECORD",g+"|"+dt+"|"+aid,hash731(String.valueOf(pr)),o.optString("updated_at",""));}
            d.setTransactionSuccessful();
        }finally{d.endTransaction();}
    }

    private void ensureAttendanceTables731(SQLiteDatabase d){d.execSQL("CREATE TABLE IF NOT EXISTS attendance_schedule(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,effectiveFrom TEXT NOT NULL,weekdays TEXT NOT NULL)");d.execSQL("CREATE TABLE IF NOT EXISTS attendance_sessions(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,sessionDate TEXT NOT NULL,cancelled INTEGER NOT NULL DEFAULT 0,confirmed INTEGER NOT NULL DEFAULT 0,UNIQUE(groupName,sessionDate))");d.execSQL("CREATE TABLE IF NOT EXISTS attendance_records(sessionId INTEGER NOT NULL,athleteId INTEGER NOT NULL,present INTEGER NOT NULL DEFAULT 1,PRIMARY KEY(sessionId,athleteId))");}

    private HttpResult postRpc731(String name,JSONObject body)throws Exception{String token=cloudPrefs.getString("access_token","");HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/"+name,body.toString(),token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/"+name,body.toString(),token);}return r;}

    private boolean dirtyState731(String entity,String key,String hash){Cursor c=db.getReadableDatabase().rawQuery("SELECT localHash FROM sync_state WHERE entity=? AND entityKey=?",new String[]{entity,key});String old="";if(c.moveToFirst()&&!c.isNull(0))old=c.getString(0);c.close();return !hash.equals(old==null?"":old);}
    private String cloudBase731(String entity,String key){Cursor c=db.getReadableDatabase().rawQuery("SELECT cloudUpdatedAt FROM sync_state WHERE entity=? AND entityKey=?",new String[]{entity,key});String x="";if(c.moveToFirst()&&!c.isNull(0))x=c.getString(0);c.close();return x==null?"":x;}
    private void markState731(SQLiteDatabase d,String entity,String key,String hash,String cloud){ContentValues v=new ContentValues();v.put("entity",entity);v.put("entityKey",key);v.put("localHash",hash);v.put("cloudUpdatedAt",cloud);v.put("lastSyncedAt",System.currentTimeMillis());d.insertWithOnConflict("sync_state",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    private String hash731(String x){return Integer.toHexString((x==null?"":x).hashCode());}

    private boolean hasSession731(){return cloudPrefs!=null&&!cloudPrefs.getString("access_token","").isEmpty();}
    private String nz731(String x){return x==null?"":x;}
    private String msg731(Exception e){return e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();}
    private String attendanceConflictText731(JSONArray a){StringBuilder b=new StringBuilder();for(int i=0;i<a.length()&&i<12;i++){if(b.length()>0)b.append("\n");b.append("• ").append(a.optString(i));}if(a.length()>12)b.append("\n• +").append(a.length()-12).append(" kayıt");return b.toString();}

    private void markAthleteBaselines731()throws Exception{
        HttpResult r=getAuthed("/rest/v1/athletes?select=legacy_id,updated_at&legacy_id=not.is.null");if(r.code<200||r.code>=300)return;JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;Cursor x=d.rawQuery("SELECT 1 FROM athletes WHERE id=?",new String[]{String.valueOf(id)});boolean has=x.moveToFirst();x.close();if(has)markState731(d,"ATHLETE",String.valueOf(id),athleteHash731(id),o.optString("updated_at",""));}}
    private void waitAndMarkAthletes731()throws Exception{long end=System.currentTimeMillis()+90000L;while(System.currentTimeMillis()<end){if(db.count(null)>0){markAthleteBaselines731();return;}Thread.sleep(800L);}throw new IllegalStateException("Sporcu verisi zaman aşımına uğradı");}
    private String athleteHash731(long id){StringBuilder b=new StringBuilder();SQLiteDatabase d=db.getReadableDatabase();Cursor a=d.rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});if(a.moveToFirst())for(int i=0;i<a.getColumnCount();i++){String n=a.getColumnName(i);if("photo".equalsIgnoreCase(n))continue;b.append(n).append('=').append(a.isNull(i)?"":a.getString(i)).append('|');}a.close();Cursor p=d.rawQuery("SELECT year,month,marker,amount FROM payments WHERE athleteId=? ORDER BY year,month",new String[]{String.valueOf(id)});while(p.moveToNext())b.append("P:").append(p.getInt(0)).append(':').append(p.getInt(1)).append(':').append(nz731(p.getString(2))).append(':').append(p.getInt(3)).append('|');p.close();Cursor f=d.rawQuery("SELECT year,effectiveMonth,fee FROM fee_history WHERE athleteId=? ORDER BY year,effectiveMonth",new String[]{String.valueOf(id)});while(f.moveToNext())b.append("F:").append(f.getInt(0)).append(':').append(f.getInt(1)).append(':').append(f.getInt(2)).append('|');f.close();return Integer.toHexString(b.toString().hashCode());}

    private boolean hasLocalChanges731(){
        try{SQLiteDatabase d=db.getReadableDatabase();Cursor a=d.rawQuery("SELECT id FROM athletes",null);while(a.moveToNext()){long id=a.getLong(0);Cursor s=d.rawQuery("SELECT localHash FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});String h="";if(s.moveToFirst()&&!s.isNull(0))h=s.getString(0);s.close();if(!athleteHash731(id).equals(h==null?"":h)){a.close();return true;}}a.close();JSONObject x=buildAttendancePayload731(true);return x.getJSONArray("p_schedule").length()+x.getJSONArray("p_sessions").length()+x.getJSONArray("p_records").length()>0;}catch(Exception e){return true;}
    }

    @Override public void finishAffinity(){
        if(forceExit731){super.finishAffinity();return;}
        if(!hasLocalChanges731()){super.finishAffinity();return;}
        new AlertDialog.Builder(this).setTitle("DEĞİŞİKLİKLER BULUTA KAYDEDİLSİN Mİ?")
            .setMessage("Bu cihazda henüz buluta gönderilmemiş değişiklikler var. Çıkmadan önce güvenli delta senkronizasyonu yapılsın mı?")
            .setPositiveButton("EVET",(d,w)->syncThenExit731()).setNegativeButton("HAYIR",(d,w)->{forceExit731=true;super.finishAffinity();}).setNeutralButton("İPTAL",null).show();
    }

    private void syncThenExit731(){
        syncFromCloud(true);long start=System.currentTimeMillis();pollExit731(start);
    }
    private void pollExit731(long start){if(root==null)return;root.postDelayed(()->{if(!hasLocalChanges731()){forceExit731=true;super.finishAffinity();return;}if(System.currentTimeMillis()-start>20000L){toast("SENKRONİZASYON TAMAMLANMADI • UYGULAMA AÇIK BIRAKILDI");return;}pollExit731(start);},1000L);}

    @Override protected void onDestroy(){sync731.shutdownNow();super.onDestroy();}
}
