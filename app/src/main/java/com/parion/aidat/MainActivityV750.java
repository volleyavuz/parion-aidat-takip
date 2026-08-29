package com.parion.aidat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import org.json.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** v4.2.36 - full entity delta sync with direct Realtime/pending routing. */
public class MainActivityV750 extends MainActivityV749 {
    private int financeGeneration750=0;
    private final ExecutorService delta750=Executors.newSingleThreadExecutor();
    private final AtomicBoolean running750=new AtomicBoolean(false);
    private static final String[] DOMAIN_TABLES={"athletes","payments","fee_periods","mobile_attendance_schedule","mobile_attendance_sessions","mobile_attendance_records","material_products","material_transactions","athlete_membership_events"};

    @Override public void onCreate(Bundle b){super.onCreate(b);setupExtraSync750();}
    @Override protected boolean useDirectSync741(){return true;}
    @Override void base(String title, boolean back){super.base(title,back);if(root==null||title==null||!"FİNANS".equalsIgnoreCase(title.trim()))return;final int gen=++financeGeneration750;final View builtRoot=root;builtRoot.setAlpha(0f);builtRoot.post(()->builtRoot.post(()->builtRoot.postOnAnimation(()->{if(gen==financeGeneration750&&root==builtRoot)builtRoot.setAlpha(1f);})));}
    @Override protected int pendingCount741(){return pendingKindCount750(null);}
    @Override void syncFromCloud(boolean announce){if(db==null||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;delta750.execute(()->deltaPass750(announce));}

    private void setupExtraSync750(){
        if(db==null)return;SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_schedule(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,effectiveFrom TEXT NOT NULL,weekdays TEXT NOT NULL)");
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_sessions(id INTEGER PRIMARY KEY AUTOINCREMENT,groupName TEXT NOT NULL,sessionDate TEXT NOT NULL,cancelled INTEGER NOT NULL DEFAULT 0,confirmed INTEGER NOT NULL DEFAULT 0,UNIQUE(groupName,sessionDate))");
        d.execSQL("CREATE TABLE IF NOT EXISTS attendance_records(sessionId INTEGER NOT NULL,athleteId INTEGER NOT NULL,present INTEGER NOT NULL DEFAULT 1,PRIMARY KEY(sessionId,athleteId))");
        String w=" WHEN (SELECT applying_remote FROM sync_guard WHERE id=1)=0 ",n="CAST(strftime('%s','now') AS INTEGER)*1000";
        trigger750(d,"x750_as_i","attendance_schedule","INSERT","'ATTENDANCE','ALL'",w,n);trigger750(d,"x750_as_u","attendance_schedule","UPDATE","'ATTENDANCE','ALL'",w,n);trigger750(d,"x750_as_d","attendance_schedule","DELETE","'ATTENDANCE','ALL'",w,n);
        trigger750(d,"x750_ase_i","attendance_sessions","INSERT","'ATTENDANCE','ALL'",w,n);trigger750(d,"x750_ase_u","attendance_sessions","UPDATE","'ATTENDANCE','ALL'",w,n);trigger750(d,"x750_ase_d","attendance_sessions","DELETE","'ATTENDANCE','ALL'",w,n);
        trigger750(d,"x750_ar_i","attendance_records","INSERT","'ATTENDANCE','ALL'",w,n);trigger750(d,"x750_ar_u","attendance_records","UPDATE","'ATTENDANCE','ALL'",w,n);trigger750(d,"x750_ar_d","attendance_records","DELETE","'ATTENDANCE','ALL'",w,n);
        trigger750(d,"x750_mp_i","material_products","INSERT","'MATERIAL','ALL'",w,n);trigger750(d,"x750_mp_u","material_products","UPDATE","'MATERIAL','ALL'",w,n);trigger750(d,"x750_mp_d","material_products","DELETE","'MATERIAL','ALL'",w,n);
        trigger750(d,"x750_mt_i","material_transactions","INSERT","'MATERIAL','ALL'",w,n);trigger750(d,"x750_mt_u","material_transactions","UPDATE","'MATERIAL','ALL'",w,n);trigger750(d,"x750_mt_d","material_transactions","DELETE","'MATERIAL','ALL'",w,n);
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x750_me_i AFTER INSERT ON athlete_restart_periods"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('MEMBERSHIP',NEW.athleteId,"+n+"); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x750_me_u AFTER UPDATE ON athlete_restart_periods"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('MEMBERSHIP',NEW.athleteId,"+n+"); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS x750_me_d AFTER DELETE ON athlete_restart_periods"+w+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('MEMBERSHIP',OLD.athleteId,"+n+"); END");
        if(!cloudPrefs.getBoolean("membership_seeded_750",false)){Cursor c=d.rawQuery("SELECT athleteId FROM athlete_restart_periods",null);while(c.moveToNext())enqueueKind750("MEMBERSHIP",String.valueOf(c.getLong(0)));c.close();cloudPrefs.edit().putBoolean("membership_seeded_750",true).apply();}
    }
    private void trigger750(SQLiteDatabase d,String name,String table,String event,String vals,String when,String now){d.execSQL("CREATE TRIGGER IF NOT EXISTS "+name+" AFTER "+event+" ON "+table+when+"BEGIN INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES("+vals+","+now+"); END");}
    private void enqueueKind750(String kind,String key){ContentValues v=new ContentValues();v.put("kind",kind);v.put("entity_key",key);v.put("created_at",System.currentTimeMillis());db.getWritableDatabase().insertWithOnConflict("pending_sync",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    private int pendingKindCount750(String kind){try{Cursor c=kind==null?db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM pending_sync",null):db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM pending_sync WHERE kind=?",new String[]{kind});int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}catch(Exception e){return 0;}}

    private void deltaPass750(boolean announce){
        if(!running750.compareAndSet(false,true))return;
        try{
            boolean attPending=pendingKindCount750("ATTENDANCE")>0,matPending=pendingKindCount750("MATERIAL")>0,memPending=pendingKindCount750("MEMBERSHIP")>0,athPending=pendingKindCount750("ATHLETE")>0;
            int pushed=invokeInt740("flushPending740");if(pendingKindCount750("ATHLETE")>0)throw new Exception("SPORCU DEĞİŞİKLİKLERİ BULUTA AKTARILAMADI");
            if(attPending)syncAttendance750();if(matPending)pushMaterial750();if(memPending)pushMembership750();
            JSONObject domains=fetchDomains750();
            boolean athleteChanged=announce||athPending||domainChanged750(domains,"athletes")||domainChanged750(domains,"payments")||domainChanged750(domains,"fee_periods");
            boolean attendanceChanged=announce||attPending||domainChanged750(domains,"mobile_attendance_schedule")||domainChanged750(domains,"mobile_attendance_sessions")||domainChanged750(domains,"mobile_attendance_records");
            boolean materialChanged=announce||matPending||domainChanged750(domains,"material_products")||domainChanged750(domains,"material_transactions");
            boolean membershipChanged=announce||memPending||domainChanged750(domains,"athlete_membership_events");
            int athleteDelta=athleteChanged?syncAthleteDelta750():0;if(attendanceChanged&&!attPending)syncAttendance750();if(materialChanged)pullMaterial750();if(membershipChanged)pullMembership750();saveDomains750(domains);
            if(announce){final int p=pushed,a=athleteDelta;runOnUiThread(()->{toast("EŞZAMANLAMA TAMAM • "+p+" SPORCU GÖNDERİLDİ • "+a+" SPORCU DELTA • TÜM VERİ TÜRLERİ EŞİTLENDİ");showHome();});}else if(athleteDelta>0||attendanceChanged||materialChanged||membershipChanged)runOnUiThread(this::showHome);
        }catch(Exception e){if(announce){String m=rootCause750(e);runOnUiThread(()->toast("EŞZAMANLAMA DURDU • YEREL VERİ KORUNDU • "+m));}}finally{running750.set(false);}
    }

    private int syncAthleteDelta750()throws Exception{
        String since=cloudPrefs.getString("delta_cursor_750","");JSONObject s=fetchDelta750(since);JSONArray aa=arr750(s,"athletes"),pp=arr750(s,"payments"),ff=arr750(s,"fees"),dd=arr750(s,"deleted"),ids=arr750(s,"changed_ids");String cursor=s.optString("cursor_at","");if(cursor.isEmpty())throw new Exception("DELTA CURSOR YOK");
        HashMap<Long,JSONObject> athletes=new HashMap<>();HashMap<Long,JSONArray> pay=new HashMap<>(),fee=new HashMap<>();HashMap<Long,String> deleted=new HashMap<>();
        for(int i=0;i<aa.length();i++){JSONObject o=aa.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)athletes.put(id,o);}for(int i=0;i<pp.length();i++){JSONObject o=pp.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)pay.computeIfAbsent(id,k->new JSONArray()).put(o);}for(int i=0;i<ff.length();i++){JSONObject o=ff.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)fee.computeIfAbsent(id,k->new JSONArray()).put(o);}for(int i=0;i<dd.length();i++){JSONObject o=dd.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id>0)deleted.put(id,o.optString("deleted_at",""));}
        SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,true);for(int i=0;i<ids.length();i++){long id=ids.optLong(i,-1);if(id<=0)continue;JSONObject a=athletes.get(id);if(a!=null){invoke740("mergeAthlete740",new Class<?>[]{SQLiteDatabase.class,JSONObject.class,JSONObject.class},d,a,null);d.delete("payments","athleteId=?",new String[]{String.valueOf(id)});JSONArray px=pay.get(id);if(px!=null)for(int j=0;j<px.length();j++)invoke740("insertPayment740",new Class<?>[]{SQLiteDatabase.class,JSONObject.class},d,px.getJSONObject(j));d.delete("fee_history","athleteId=?",new String[]{String.valueOf(id)});JSONArray fx=fee.get(id);if(fx!=null)for(int j=0;j<fx.length();j++)invoke740("insertFee740",new Class<?>[]{SQLiteDatabase.class,JSONObject.class},d,fx.getJSONObject(j));invoke740("mark740",new Class<?>[]{SQLiteDatabase.class,long.class,String.class},d,id,"");}String at=deleted.get(id);if(at!=null&&!at.isEmpty()){ContentValues v=new ContentValues();v.put("deletedAt",at);d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});}}invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);d.setTransactionSuccessful();}finally{try{invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);}catch(Exception ignored){}d.endTransaction();}cloudPrefs.edit().putString("delta_cursor_750",cursor).apply();return s.optInt("changed_count",ids.length());
    }

    private void syncAttendance750()throws Exception{
        SQLiteDatabase d=db.getWritableDatabase();invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,true);try{Method m=MainActivityV731.class.getDeclaredMethod("syncAttendanceDelta731",boolean.class);m.setAccessible(true);m.invoke(this,false);}finally{invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);}Method b=MainActivityV731.class.getDeclaredMethod("buildAttendancePayload731",boolean.class);b.setAccessible(true);JSONObject left=(JSONObject)b.invoke(this,true);int dirty=left.getJSONArray("p_schedule").length()+left.getJSONArray("p_sessions").length()+left.getJSONArray("p_records").length();if(dirty>0)throw new Exception("YOKLAMA DEĞİŞİKLİĞİ/ÇAKIŞMASI BEKLİYOR");d.delete("pending_sync","kind='ATTENDANCE'",null);
    }

    private void pushMaterial750()throws Exception{
        long stamp=pendingStamp750("MATERIAL");JSONArray p=new JSONArray(),t=new JSONArray();SQLiteDatabase rdb=db.getReadableDatabase();Cursor a=rdb.rawQuery("SELECT name,currentPrice,active FROM material_products ORDER BY name",null);while(a.moveToNext())p.put(new JSONObject().put("name",a.getString(0)).put("current_price",a.getInt(1)).put("active",a.getInt(2)==1));a.close();Cursor c=rdb.rawQuery("SELECT id,cloudId,athleteId,product,qty,unitPrice,total,paidAmount,issuedDate,paymentDate,note FROM material_transactions ORDER BY id",null);while(c.moveToNext())t.put(new JSONObject().put("local_id",c.getLong(0)).put("cloud_id",nz750(c.getString(1))).put("athlete_id",c.getLong(2)).put("product",c.getString(3)).put("qty",c.getInt(4)).put("unit_price",c.getInt(5)).put("total",c.getInt(6)).put("paid_amount",c.getInt(7)).put("issued_date",c.getString(8)).put("payment_date",nz750(c.getString(9))).put("note",nz750(c.getString(10))));c.close();JSONObject out=postRpc750("parion_sync_material_snapshot_v1",new JSONObject().put("p_products",p).put("p_transactions",t));JSONArray map=arr750(out,"id_map");SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,true);for(int i=0;i<map.length();i++){JSONObject x=map.getJSONObject(i);ContentValues v=new ContentValues();v.put("cloudId",x.optString("cloud_id",""));d.update("material_transactions",v,"id=?",new String[]{String.valueOf(x.optLong("local_id",-1))});}invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);d.delete("pending_sync","kind='MATERIAL' AND created_at<=?",new String[]{String.valueOf(stamp)});d.setTransactionSuccessful();}finally{try{invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);}catch(Exception ignored){}d.endTransaction();}
    }

    private void pullMaterial750()throws Exception{
        if(pendingKindCount750("MATERIAL")>0)return;JSONArray p=new JSONArray(getAuthed750("/rest/v1/material_products?select=id,name,current_price,active&order=name.asc").body),t=new JSONArray(getAuthed750("/rest/v1/material_transactions?select=id,athlete_legacy_id,product_name,quantity,unit_price,total_amount,paid_amount,issued_at,payment_date,note&order=issued_at.asc,id.asc").body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{if(pendingKindCount750("MATERIAL")>0)return;invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,true);d.delete("material_transactions",null,null);d.delete("material_products",null,null);for(int i=0;i<p.length();i++){JSONObject o=p.getJSONObject(i);ContentValues v=new ContentValues();v.put("name",o.optString("name",""));v.put("currentPrice",o.optInt("current_price",0));v.put("active",o.optBoolean("active",true)?1:0);v.put("cloudId",o.optString("id",""));d.insertWithOnConflict("material_products",null,v,SQLiteDatabase.CONFLICT_REPLACE);}for(int i=0;i<t.length();i++){JSONObject o=t.getJSONObject(i);ContentValues v=new ContentValues();v.put("cloudId",o.optString("id",""));v.put("athleteId",o.optLong("athlete_legacy_id",-1));v.put("product",o.optString("product_name",""));v.put("qty",o.optInt("quantity",0));v.put("unitPrice",o.optInt("unit_price",0));v.put("total",o.optInt("total_amount",0));v.put("paidAmount",o.optInt("paid_amount",0));v.put("issuedDate",o.optString("issued_at",""));if(!o.isNull("payment_date"))v.put("paymentDate",o.optString("payment_date",""));v.put("note",o.optString("note",""));d.insertWithOnConflict("material_transactions",null,v,SQLiteDatabase.CONFLICT_REPLACE);}invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);d.setTransactionSuccessful();}finally{try{invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);}catch(Exception ignored){}d.endTransaction();}
    }

    private void pushMembership750()throws Exception{
        long stamp=pendingStamp750("MEMBERSHIP");JSONArray a=new JSONArray();Cursor c=db.getReadableDatabase().rawQuery("SELECT athleteId,restartEndDate FROM athlete_restart_periods ORDER BY athleteId",null);while(c.moveToNext())a.put(new JSONObject().put("legacy_id",c.getLong(0)).put("restart_end_date",nz750(c.getString(1))));c.close();postRpc750("parion_sync_membership_restart_v1",new JSONObject().put("p_periods",a));db.getWritableDatabase().delete("pending_sync","kind='MEMBERSHIP' AND created_at<=?",new String[]{String.valueOf(stamp)});
    }
    private void pullMembership750()throws Exception{
        if(pendingKindCount750("MEMBERSHIP")>0)return;HttpResult r=getAuthed750("/rest/v1/athlete_membership_events?event_type=eq.RESTART_END&select=legacy_id,event_date,note&order=legacy_id.asc,event_date.desc");JSONArray a=new JSONArray(r.body);HashSet<Long> seen=new HashSet<>();SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{if(pendingKindCount750("MEMBERSHIP")>0)return;invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,true);d.delete("athlete_restart_periods",null,null);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0||!seen.add(id))continue;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("restartEndDate",o.optString("event_date",""));d.insertWithOnConflict("athlete_restart_periods",null,v,SQLiteDatabase.CONFLICT_REPLACE);}invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);d.setTransactionSuccessful();}finally{try{invoke740("guard740",new Class<?>[]{SQLiteDatabase.class,boolean.class},d,false);}catch(Exception ignored){}d.endTransaction();}
    }

    private JSONObject fetchDomains750()throws Exception{return postRpc750("parion_sync_domain_revisions_v1",new JSONObject());}
    private boolean domainChanged750(JSONObject x,String table){long now=x.optLong(table,0),old=cloudPrefs.getLong("domain_rev_750_"+table,-1);return old<0||now!=old;}
    private void saveDomains750(JSONObject x){android.content.SharedPreferences.Editor e=cloudPrefs.edit();for(String t:DOMAIN_TABLES)e.putLong("domain_rev_750_"+t,x.optLong(t,0));e.apply();}
    private long pendingStamp750(String kind){try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COALESCE(MAX(created_at),0) FROM pending_sync WHERE kind=?",new String[]{kind});long x=0;if(c.moveToFirst())x=c.getLong(0);c.close();return x;}catch(Exception e){return 0;}}
    private JSONArray arr750(JSONObject o,String k){JSONArray a=o.optJSONArray(k);return a==null?new JSONArray():a;}
    private String nz750(String s){return s==null?"":s;}
    private HttpResult getAuthed750(String path)throws Exception{String token=cloudPrefs.getString("access_token","");HttpResult r=request("GET",SUPABASE_URL+path,null,token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("GET",SUPABASE_URL+path,null,token);}if(r.code<200||r.code>=300)throw new Exception("BULUT OKUMA HTTP "+r.code);return r;}
    private JSONObject postRpc750(String name,JSONObject body)throws Exception{String token=cloudPrefs.getString("access_token","");HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/"+name,body.toString(),token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/"+name,body.toString(),token);}if(r.code<200||r.code>=300)throw new Exception(name+" HTTP "+r.code);if(r.body==null||r.body.trim().isEmpty())return new JSONObject();String s=r.body.trim();if(s.startsWith("[")){JSONArray a=new JSONArray(s);return a.length()>0&&a.opt(0) instanceof JSONObject?a.getJSONObject(0):new JSONObject();}return new JSONObject(s);}
    private JSONObject fetchDelta750(String since)throws Exception{String body=(since==null||since.isEmpty())?"{\"p_since\":null}":new JSONObject().put("p_since",since).toString();String token=cloudPrefs.getString("access_token","");HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_delta_snapshot_v1",body,token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_delta_snapshot_v1",body,token);}if(r.code<200||r.code>=300)throw new Exception("BULUT HTTP "+r.code);return new JSONObject(r.body);}
    private String rootCause750(Throwable t){Throwable x=t;while(x.getCause()!=null)x=x.getCause();String m=x.getMessage();return m==null?x.getClass().getSimpleName():m;}
    @Override protected void onDestroy(){delta750.shutdownNow();super.onDestroy();}
}
