package com.parion.aidat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import org.json.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** v4.2.38 - precise attendance entity delta layered over the stable V750 sync engine. */
public class MainActivityV751 extends MainActivityV750 {
    private final ExecutorService sync751=Executors.newSingleThreadExecutor();
    private final AtomicBoolean running751=new AtomicBoolean(false);
    private static final String[] ATT_DOMAINS={"mobile_attendance_schedule","mobile_attendance_sessions","mobile_attendance_records"};

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        setupAttendance751();
    }

    @Override protected int pendingCount741(){
        try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM pending_sync",null);int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}catch(Exception e){return 0;}
    }

    @Override void syncFromCloud(boolean announce){
        if(db==null||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        sync751.execute(()->pass751(announce));
    }

    private void setupAttendance751(){
        SQLiteDatabase d=db.getWritableDatabase();
        try{d.execSQL("UPDATE sync_guard SET applying_remote=1 WHERE id=1");}catch(Exception ignored){}
        try{
            d.execSQL("DROP TRIGGER IF EXISTS x750_as_i");d.execSQL("DROP TRIGGER IF EXISTS x750_as_u");d.execSQL("DROP TRIGGER IF EXISTS x750_as_d");
            d.execSQL("DROP TRIGGER IF EXISTS x750_ase_i");d.execSQL("DROP TRIGGER IF EXISTS x750_ase_u");d.execSQL("DROP TRIGGER IF EXISTS x750_ase_d");
            d.execSQL("DROP TRIGGER IF EXISTS x750_ar_i");d.execSQL("DROP TRIGGER IF EXISTS x750_ar_u");d.execSQL("DROP TRIGGER IF EXISTS x750_ar_d");
            d.delete("pending_sync","kind='ATTENDANCE'",null);
            d.delete("sync_state","entity LIKE 'ATT_%'",null);
            d.execSQL("DELETE FROM attendance_schedule WHERE id NOT IN (SELECT MIN(id) FROM attendance_schedule GROUP BY groupName,effectiveFrom)");
            d.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS ux_attendance_schedule_key ON attendance_schedule(groupName,effectiveFrom)");
        }finally{try{d.execSQL("UPDATE sync_guard SET applying_remote=0 WHERE id=1");}catch(Exception ignored){}}
        String w=" WHEN (SELECT applying_remote FROM sync_guard WHERE id=1)=0 ";
        String n="CAST(strftime('%s','now') AS INTEGER)*1000";
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x751_as_i AFTER INSERT ON attendance_schedule"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATT_SCHEDULE',NEW.groupName||'|'||NEW.effectiveFrom,"+n+"); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x751_as_u AFTER UPDATE ON attendance_schedule"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATT_SCHEDULE',NEW.groupName||'|'||NEW.effectiveFrom,"+n+"); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x751_as_d AFTER DELETE ON attendance_schedule"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATT_SCHEDULE',OLD.groupName||'|'||OLD.effectiveFrom,"+n+"); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x751_ase_i AFTER INSERT ON attendance_sessions"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATT_SESSION',NEW.groupName||'|'||NEW.sessionDate,"+n+"); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x751_ase_u AFTER UPDATE ON attendance_sessions"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATT_SESSION',NEW.groupName||'|'||NEW.sessionDate,"+n+"); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x751_ase_d AFTER DELETE ON attendance_sessions"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('ATT_SESSION',OLD.groupName||'|'||OLD.sessionDate,"+n+"); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x751_ar_i AFTER INSERT ON attendance_records"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) SELECT 'ATT_RECORD',s.groupName||'|'||s.sessionDate||'|'||NEW.athleteId,"+n+" FROM attendance_sessions s WHERE s.id=NEW.sessionId; END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x751_ar_u AFTER UPDATE ON attendance_records"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) SELECT 'ATT_RECORD',s.groupName||'|'||s.sessionDate||'|'||NEW.athleteId,"+n+" FROM attendance_sessions s WHERE s.id=NEW.sessionId; END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x751_ar_d AFTER DELETE ON attendance_records"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) SELECT 'ATT_RECORD',s.groupName||'|'||s.sessionDate||'|'||OLD.athleteId,"+n+" FROM attendance_sessions s WHERE s.id=OLD.sessionId; END");
        if(!cloudPrefs.getBoolean("attendance_entity_migrated_751",false)){
            cloudPrefs.edit().putBoolean("attendance_entity_migrated_751",true).apply();
            sync751.execute(()->{try{pullAttendance751();JSONObject r=domainRevisions751();saveAttendanceRevs751(r);mirrorAttendanceRevs751(r);}catch(Exception ignored){}});
        }
    }

    private void pass751(boolean announce){
        if(!running751.compareAndSet(false,true))return;
        try{
            flushAttendance751();
            JSONObject rev=domainRevisions751();
            boolean changed=false;
            for(String t:ATT_DOMAINS){long now=rev.optLong(t,0),old=cloudPrefs.getLong("att_rev_751_"+t,-1);if(old<0||now!=old)changed=true;}
            if(changed&&!hasAttendancePending751())pullAttendance751();
            saveAttendanceRevs751(rev);mirrorAttendanceRevs751(rev);
        }catch(Exception e){if(announce){String m=root751(e);runOnUiThread(()->toast("YOKLAMA SENKRONİZASYONU DURDU • "+m));}}
        finally{running751.set(false);}
        // Keep the proven V750 athlete/payment/material/membership delta engine.
        super.syncFromCloud(announce);
    }

    private boolean hasAttendancePending751(){
        try{Cursor c=db.getReadableDatabase().rawQuery("SELECT 1 FROM pending_sync WHERE kind IN ('ATT_SCHEDULE','ATT_SESSION','ATT_RECORD') LIMIT 1",null);boolean x=c.moveToFirst();c.close();return x;}catch(Exception e){return false;}
    }

    private void flushAttendance751()throws Exception{
        for(int round=0;round<200;round++){
            Cursor c=db.getReadableDatabase().rawQuery("SELECT kind,entity_key,created_at FROM pending_sync WHERE kind IN ('ATT_SCHEDULE','ATT_SESSION','ATT_RECORD') ORDER BY created_at ASC LIMIT 1",null);
            if(!c.moveToFirst()){c.close();break;}
            String kind=c.getString(0),key=c.getString(1);long stamp=c.getLong(2);c.close();
            JSONObject payload=new JSONObject();boolean exists=false;String[] p=key.split("\\|",-1);SQLiteDatabase r=db.getReadableDatabase();
            if("ATT_SCHEDULE".equals(kind)&&p.length==2){Cursor x=r.rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? AND effectiveFrom=? LIMIT 1",new String[]{p[0],p[1]});if(x.moveToFirst()){exists=true;payload.put("weekdays",x.getString(0));}x.close();}
            else if("ATT_SESSION".equals(kind)&&p.length==2){Cursor x=r.rawQuery("SELECT cancelled,confirmed FROM attendance_sessions WHERE groupName=? AND sessionDate=? LIMIT 1",new String[]{p[0],p[1]});if(x.moveToFirst()){exists=true;payload.put("cancelled",x.getInt(0)==1).put("confirmed",x.getInt(1)==1);}x.close();}
            else if("ATT_RECORD".equals(kind)&&p.length==3){Cursor x=r.rawQuery("SELECT ar.present FROM attendance_records ar JOIN attendance_sessions s ON s.id=ar.sessionId WHERE s.groupName=? AND s.sessionDate=? AND ar.athleteId=? LIMIT 1",new String[]{p[0],p[1],p[2]});if(x.moveToFirst()){exists=true;payload.put("present",x.getInt(0)==1);}x.close();}
            else{db.getWritableDatabase().delete("pending_sync","kind=? AND entity_key=?",new String[]{kind,key});continue;}
            postRpc751("parion_sync_attendance_entity_v1",new JSONObject().put("p_kind",kind).put("p_key",key).put("p_payload",payload).put("p_exists",exists));
            db.getWritableDatabase().delete("pending_sync","kind=? AND entity_key=? AND created_at<=?",new String[]{kind,key,String.valueOf(stamp)});
        }
    }

    private void pullAttendance751()throws Exception{
        if(hasAttendancePending751())return;
        JSONArray sch=new JSONArray(get751("/rest/v1/mobile_attendance_schedule?select=group_name,effective_from,weekdays&order=effective_from.asc"));
        JSONArray ses=new JSONArray(get751("/rest/v1/mobile_attendance_sessions?select=group_name,session_date,cancelled,confirmed&order=session_date.asc"));
        JSONArray rec=new JSONArray(get751("/rest/v1/mobile_attendance_records?select=group_name,session_date,athlete_id,present&order=session_date.asc"));
        SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
        try{
            if(hasAttendancePending751())return;
            d.execSQL("UPDATE sync_guard SET applying_remote=1 WHERE id=1");
            d.delete("attendance_records",null,null);d.delete("attendance_sessions",null,null);d.delete("attendance_schedule",null,null);
            for(int i=0;i<sch.length();i++){JSONObject o=sch.getJSONObject(i);ContentValues v=new ContentValues();v.put("groupName",o.optString("group_name",""));v.put("effectiveFrom",o.optString("effective_from",""));v.put("weekdays",o.optString("weekdays",""));d.insertWithOnConflict("attendance_schedule",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
            for(int i=0;i<ses.length();i++){JSONObject o=ses.getJSONObject(i);ContentValues v=new ContentValues();v.put("groupName",o.optString("group_name",""));v.put("sessionDate",o.optString("session_date",""));v.put("cancelled",o.optBoolean("cancelled",false)?1:0);v.put("confirmed",o.optBoolean("confirmed",false)?1:0);d.insertWithOnConflict("attendance_sessions",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
            for(int i=0;i<rec.length();i++){JSONObject o=rec.getJSONObject(i);String g=o.optString("group_name",""),dt=o.optString("session_date","");Cursor s=d.rawQuery("SELECT id FROM attendance_sessions WHERE groupName=? AND sessionDate=? LIMIT 1",new String[]{g,dt});if(!s.moveToFirst()){s.close();continue;}long sid=s.getLong(0);s.close();ContentValues v=new ContentValues();v.put("sessionId",sid);v.put("athleteId",o.optLong("athlete_id",-1));v.put("present",o.optBoolean("present",true)?1:0);d.insertWithOnConflict("attendance_records",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
            d.delete("sync_state","entity LIKE 'ATT_%'",null);
            d.execSQL("UPDATE sync_guard SET applying_remote=0 WHERE id=1");d.setTransactionSuccessful();
        }finally{try{d.execSQL("UPDATE sync_guard SET applying_remote=0 WHERE id=1");}catch(Exception ignored){}d.endTransaction();}
    }

    private JSONObject domainRevisions751()throws Exception{return postRpc751("parion_sync_domain_revisions_v1",new JSONObject());}
    private void saveAttendanceRevs751(JSONObject r){android.content.SharedPreferences.Editor e=cloudPrefs.edit();for(String t:ATT_DOMAINS)e.putLong("att_rev_751_"+t,r.optLong(t,0));e.apply();}
    private void mirrorAttendanceRevs751(JSONObject r){android.content.SharedPreferences.Editor e=cloudPrefs.edit();for(String t:ATT_DOMAINS)e.putLong("domain_rev_750_"+t,r.optLong(t,0));e.apply();}
    private String get751(String path)throws Exception{String token=cloudPrefs.getString("access_token","");HttpResult r=request("GET",SUPABASE_URL+path,null,token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("GET",SUPABASE_URL+path,null,token);}if(r.code<200||r.code>=300)throw new Exception("ATT GET HTTP "+r.code);return r.body==null?"[]":r.body;}
    private JSONObject postRpc751(String name,JSONObject body)throws Exception{String token=cloudPrefs.getString("access_token","");HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/"+name,body.toString(),token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/"+name,body.toString(),token);}if(r.code<200||r.code>=300)throw new Exception(name+" HTTP "+r.code);String s=r.body==null?"":r.body.trim();if(s.isEmpty())return new JSONObject();if(s.startsWith("[")){JSONArray a=new JSONArray(s);return a.length()>0&&a.opt(0) instanceof JSONObject?a.getJSONObject(0):new JSONObject();}return new JSONObject(s);}
    private String root751(Throwable t){Throwable x=t;while(x.getCause()!=null)x=x.getCause();return x.getMessage()==null?x.getClass().getSimpleName():x.getMessage();}
    @Override protected void onDestroy(){sync751.shutdownNow();super.onDestroy();}
}
