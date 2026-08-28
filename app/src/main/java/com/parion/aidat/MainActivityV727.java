package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * v4.2.2 - Safe multi-device synchronization.
 * Rules:
 * 1) legacy whole-database snapshot writes are always blocked;
 * 2) automatic legacy one-athlete pushes are blocked unless they pass this class' conflict preflight;
 * 3) local unsynced athlete changes survive restarts via sync_state hash comparison;
 * 4) cloud-newer + local-dirty => conflict is preserved locally and neither side is overwritten;
 * 5) after local deltas are safely pushed, cloud is pulled before normal use continues.
 */
public class MainActivityV727 extends MainActivityV726 {
    private final ExecutorService safe727=Executors.newSingleThreadExecutor();
    private volatile boolean safeSync727=false;
    private volatile Thread allowedDeltaThread727=null;
    private volatile long restartPullAt727=0L;

    @Override public void onCreate(Bundle b){super.onCreate(b);}

    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        if(url!=null&&url.contains("/rpc/parion_sync_mobile_snapshot"))
            return new HttpResult(423,"{\"error\":\"SAFE_MULTI_DEVICE_SNAPSHOT_BLOCKED\"}");
        if(url!=null&&url.contains("/rpc/parion_sync_attendance_snapshot"))
            return new HttpResult(423,"{\"error\":\"SAFE_MULTI_DEVICE_ATTENDANCE_SNAPSHOT_BLOCKED\"}");
        if(url!=null&&url.contains("/rpc/parion_sync_one_athlete_delta_v4")&&Thread.currentThread()!=allowedDeltaThread727)
            return new HttpResult(423,"{\"error\":\"DELTA_REQUIRES_SAFE_PREFLIGHT\"}");
        return super.request(method,url,body,bearer);
    }

    @Override void syncFromCloud(boolean announce){
        if(safeSync727||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        if(db==null||db.count(null)==0){super.syncFromCloud(announce);return;}
        safeSync727=true;
        if(announce)runOnUiThread(()->toast("GÜVENLİ SENKRONİZASYON • DEĞİŞİKLİKLER KONTROL EDİLİYOR..."));
        safe727.execute(()->{
            ArrayList<Long> conflicts=new ArrayList<>();
            ArrayList<Long> dirty=new ArrayList<>();
            try{
                dirty=findDirty727();
                for(long id:dirty){
                    CloudStamp727 cs=cloudStamp727(id);
                    long baseline=lastSynced727(id);
                    boolean hasBaseline=!savedHash727(id).isEmpty();
                    if(cs.exists&&(!hasBaseline || (baseline>0&&cs.updatedAt>baseline+1500L))){conflicts.add(id);continue;}
                    if(!safePushAthlete727(id))throw new IllegalStateException("Sporcu "+id+" buluta doğrulanamadı");
                }
                if(!conflicts.isEmpty()){
                    final String names=conflictNames727(conflicts);
                    runOnUiThread(()->{
                        safeSync727=false;
                        new AlertDialog.Builder(this).setTitle("SENKRONİZASYON ÇAKIŞMASI")
                            .setMessage("Bu cihazda ve bulutta aynı kayıt üzerinde farklı değişiklikler bulundu. Veri kaybını önlemek için hiçbir taraf ezilmedi.\n\n"+names+"\n\nBu kayıtları kontrol edip tekrar senkronize edin.")
                            .setPositiveButton("TAMAM",null).show();
                    });
                    return;
                }
                if(!syncRestartEventsUp727())throw new IllegalStateException("Yeniden bırakma tarihleri buluta aktarılamadı");
                runOnUiThread(()->{
                    safeSync727=false;
                    super.syncFromCloud(announce);
                    if(!dirty.isEmpty())toast(dirty.size()+" YEREL DEĞİŞİKLİK GÜVENLE BULUTA AKTARILDI • BULUT VERİLERİ ALINIYOR");
                });
            }catch(Exception e){
                String m=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();
                runOnUiThread(()->{safeSync727=false;toast("GÜVENLİ SENKRONİZASYON DURDU • "+m);});
            }
        });
    }

    @Override void showHome(){
        super.showHome();
        if(db==null||db.count(null)==0||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        long now=System.currentTimeMillis();
        if(now-restartPullAt727>60_000L){restartPullAt727=now;safe727.execute(this::pullRestartEvents727);}
    }

    @Override void showCloudMenu(){
        int pending=0;try{pending=findDirty727().size();}catch(Exception ignored){}
        final int p=pending;
        String[] items={"ŞİMDİ GÜVENLİ SENKRONİZE ET","BEKLEYEN YEREL DEĞİŞİKLİK: "+p,"OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("GÜVENLİ ÇOKLU CİHAZ SENKRONİZASYONU").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else if(w==1)toast(p==0?"BEKLEYEN DEĞİŞİKLİK YOK.":p+" DEĞİŞİKLİK BULUTA GÖNDERİLMEYİ BEKLİYOR.");
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    private ArrayList<Long> findDirty727(){
        ArrayList<Long> out=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes",null);
        while(c.moveToNext()){long id=c.getLong(0);if(!hash727(id).equals(savedHash727(id)))out.add(id);}c.close();return out;
    }

    private String hash727(long id){
        StringBuilder b=new StringBuilder();SQLiteDatabase d=db.getReadableDatabase();
        Cursor a=d.rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});
        if(a.moveToFirst())for(int i=0;i<a.getColumnCount();i++){String n=a.getColumnName(i);if("photo".equalsIgnoreCase(n))continue;b.append(n).append('=').append(a.isNull(i)?"":a.getString(i)).append('|');}a.close();
        Cursor p=d.rawQuery("SELECT year,month,marker,amount FROM payments WHERE athleteId=? ORDER BY year,month",new String[]{String.valueOf(id)});while(p.moveToNext())b.append("P:").append(p.getInt(0)).append(':').append(p.getInt(1)).append(':').append(p.getString(2)).append(':').append(p.getInt(3)).append('|');p.close();
        Cursor f=d.rawQuery("SELECT year,effectiveMonth,fee FROM fee_history WHERE athleteId=? ORDER BY year,effectiveMonth",new String[]{String.valueOf(id)});while(f.moveToNext())b.append("F:").append(f.getInt(0)).append(':').append(f.getInt(1)).append(':').append(f.getInt(2)).append('|');f.close();
        return Integer.toHexString(b.toString().hashCode());
    }

    private String savedHash727(long id){Cursor c=db.getReadableDatabase().rawQuery("SELECT localHash FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});String h="";if(c.moveToFirst()&&!c.isNull(0))h=c.getString(0);c.close();return h==null?"":h;}
    private long lastSynced727(long id){Cursor c=db.getReadableDatabase().rawQuery("SELECT lastSyncedAt FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});long x=0;if(c.moveToFirst())x=c.getLong(0);c.close();return x;}
    private void markSynced727(long id,String cloudAt){ContentValues v=new ContentValues();v.put("entity","ATHLETE");v.put("entityKey",String.valueOf(id));v.put("localHash",hash727(id));v.put("cloudUpdatedAt",cloudAt==null?"":cloudAt);v.put("lastSyncedAt",System.currentTimeMillis());db.getWritableDatabase().insertWithOnConflict("sync_state",null,v,SQLiteDatabase.CONFLICT_REPLACE);}

    private CloudStamp727 cloudStamp727(long id)throws Exception{
        HttpResult r=getAuthed("/rest/v1/athletes?legacy_id=eq."+id+"&select=legacy_id,updated_at&limit=1");if(r.code==401&&refreshSession())r=getAuthed("/rest/v1/athletes?legacy_id=eq."+id+"&select=legacy_id,updated_at&limit=1");if(r.code<200||r.code>=300)throw new IllegalStateException("Bulut kontrolü HTTP "+r.code);
        JSONArray a=new JSONArray(r.body);if(a.length()==0)return new CloudStamp727(false,0L,"");String s=a.getJSONObject(0).optString("updated_at","");return new CloudStamp727(true,parseCloudTime727(s),s);
    }

    private boolean safePushAthlete727(long id)throws Exception{
        JSONObject body=body727(id);String token=cloudPrefs.getString("access_token","");allowedDeltaThread727=Thread.currentThread();HttpResult r;
        try{r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_delta_v4",body.toString(),token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_delta_v4",body.toString(),token);}}finally{allowedDeltaThread727=null;}
        if(r.code<200||r.code>=300)return false;CloudStamp727 after=cloudStamp727(id);if(!after.exists)return false;markSynced727(id,after.raw);return true;
    }

    private JSONObject body727(long id)throws Exception{
        SQLiteDatabase d=db.getReadableDatabase();Cursor a=d.rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});if(!a.moveToFirst()){a.close();throw new IllegalStateException("Yerel sporcu bulunamadı: "+id);}JSONObject x=new JSONObject();x.put("legacy_id",id);
        pc727(x,"seq",a,"seq");pc727(x,"birth_year",a,"birthYear");pc727(x,"birth_date",a,"birthDate");pc727(x,"name",a,"name");pc727(x,"category",a,"category");pc727(x,"status",a,"status");pc727(x,"monthly_fee",a,"monthlyFee");pc727(x,"sibling",a,"sibling");pc727(x,"tshirt_qty",a,"tshirtQty");pc727(x,"tshirt_paid",a,"tshirtPaid");pc727(x,"tracksuit_qty",a,"tracksuitQty");pc727(x,"tracksuit_paid",a,"tracksuitPaid");pc727(x,"notes",a,"notes");pc727(x,"phone",a,"phone");pc727(x,"mother_name",a,"motherName");pc727(x,"mother_phone",a,"motherPhone");pc727(x,"father_name",a,"fatherName");pc727(x,"father_phone",a,"fatherPhone");pc727(x,"start_date",a,"startDate");pc727(x,"end_date",a,"endDate");pc727(x,"restart_date",a,"restartDate");pc727(x,"tckn",a,"tckn");int si=a.getColumnIndex("summerCall"),wi=a.getColumnIndex("winterCall");x.put("summer_call",si>=0&&a.getInt(si)==1);x.put("winter_call",wi>=0&&a.getInt(wi)==1);a.close();
        JSONArray ps=new JSONArray();Cursor p=d.rawQuery("SELECT year,month,marker,amount FROM payments WHERE athleteId=? ORDER BY year,month",new String[]{String.valueOf(id)});while(p.moveToNext())ps.put(new JSONObject().put("legacy_id",id).put("year",p.getInt(0)).put("month",p.getInt(1)).put("marker",p.getString(2)==null?"":p.getString(2)).put("amount",p.getInt(3)));p.close();
        JSONArray fs=new JSONArray();Cursor f=d.rawQuery("SELECT year,effectiveMonth,fee FROM fee_history WHERE athleteId=? ORDER BY year,effectiveMonth",new String[]{String.valueOf(id)});while(f.moveToNext())fs.put(new JSONObject().put("legacy_id",id).put("year",f.getInt(0)).put("month",f.getInt(1)).put("fee",f.getInt(2)));f.close();
        return new JSONObject().put("p_legacy_id",id).put("p_athlete",x).put("p_payments",ps).put("p_fees",fs);
    }

    private void pc727(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));}

    private boolean syncRestartEventsUp727(){
        try{Cursor c=db.getReadableDatabase().rawQuery("SELECT athleteId,restartEndDate FROM athlete_restart_periods WHERE TRIM(COALESCE(restartEndDate,''))<>''",null);while(c.moveToNext()){long id=c.getLong(0);String date=c.getString(1);HttpResult g=getAuthed("/rest/v1/athlete_membership_events?legacy_id=eq."+id+"&event_type=eq.RESTART_END&select=id,event_date,updated_at&order=updated_at.desc&limit=1");if(g.code<200||g.code>=300){c.close();return false;}JSONArray a=new JSONArray(g.body);if(a.length()>0){String cloud=a.getJSONObject(0).optString("event_date","");if(!date.equals(cloud)){c.close();return false;}continue;}JSONObject j=new JSONObject().put("legacy_id",id).put("event_date",date).put("event_type","RESTART_END").put("note","SAFE MULTI DEVICE");String token=cloudPrefs.getString("access_token","");HttpResult p=request("POST",SUPABASE_URL+"/rest/v1/athlete_membership_events",j.toString(),token);if(p.code<200||p.code>=300){c.close();return false;}}c.close();return true;}catch(Exception e){return false;}
    }

    private void pullRestartEvents727(){
        try{HttpResult r=getAuthed("/rest/v1/athlete_membership_events?event_type=eq.RESTART_END&select=legacy_id,event_date,updated_at&order=updated_at.asc");if(r.code<200||r.code>=300)return;JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{d.delete("athlete_restart_periods",null,null);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);String date=o.optString("event_date","");if(id<=0||date.isEmpty())continue;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("restartEndDate",date);d.insertWithOnConflict("athlete_restart_periods",null,v,SQLiteDatabase.CONFLICT_REPLACE);}d.setTransactionSuccessful();}finally{d.endTransaction();}}catch(Exception ignored){}
    }

    private String conflictNames727(List<Long> ids){StringBuilder b=new StringBuilder();for(long id:ids){Cursor c=db.athlete(id);String n=String.valueOf(id);if(c.moveToFirst()){int i=c.getColumnIndex("name");if(i>=0&&!c.isNull(i))n=c.getString(i);}c.close();if(b.length()>0)b.append("\n");b.append("• ").append(n);}return b.toString();}

    private long parseCloudTime727(String raw){if(raw==null||raw.isEmpty())return 0L;String[] patterns={"yyyy-MM-dd'T'HH:mm:ss.SSSXXX","yyyy-MM-dd'T'HH:mm:ssXXX","yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"};for(String p:patterns)try{return new SimpleDateFormat(p,Locale.US).parse(raw).getTime();}catch(Exception ignored){}return 0L;}
    private static class CloudStamp727{final boolean exists;final long updatedAt;final String raw;CloudStamp727(boolean e,long u,String r){exists=e;updatedAt=u;raw=r;}}

    @Override protected void onDestroy(){safe727.shutdownNow();super.onDestroy();}
}
