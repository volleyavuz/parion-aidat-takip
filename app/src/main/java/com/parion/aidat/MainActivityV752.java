package com.parion.aidat;

import android.database.Cursor;
import android.os.Bundle;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * v4.3.0 - Universal Last-Write-Wins router.
 *
 * Supabase is the single authority. Every local write carries:
 *   entity kind + entity key + client edit time (ms) + stable device id.
 * The server accepts only the newest version. Older packets may arrive later,
 * but can never overwrite a newer value.
 */
public class MainActivityV752 extends MainActivityV751 {
    private final ScheduledExecutorService fast752=Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService athlete752=Executors.newSingleThreadExecutor();
    private final AtomicBoolean kick752=new AtomicBoolean(false);
    private final AtomicBoolean athletePass752=new AtomicBoolean(false);
    private volatile boolean resumed752=false;
    private String device752;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        device752=deviceId752();
        fast752.scheduleWithFixedDelay(()->{
            if(!resumed752||db==null||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
            try{
                if(pendingCount741()>0){
                    normalizePendingTimes752();
                    if(kick752.compareAndSet(false,true)){
                        try{syncFromCloud(false);}finally{fast752.schedule(()->kick752.set(false),90,TimeUnit.MILLISECONDS);}
                    }
                }
            }catch(Exception ignored){}
        },80,90,TimeUnit.MILLISECONDS);
    }

    @Override protected void onResume(){resumed752=true;super.onResume();}
    @Override protected void onPause(){resumed752=false;super.onPause();}

    @Override void syncFromCloud(boolean announce){
        if(db==null||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        athlete752.execute(()->{
            if(!athletePass752.compareAndSet(false,true))return;
            try{normalizePendingTimes752();flushAthleteLww752();}
            catch(Exception e){if(announce){String m=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();runOnUiThread(()->toast("LWW SENKRONİZASYONU DURDU • "+m));}}
            finally{athletePass752.set(false);}
            super.syncFromCloud(announce);
        });
    }

    private String deviceId752(){
        android.content.SharedPreferences p=getSharedPreferences("parion_lww_device_v430",0);
        String x=p.getString("device_id","");
        if(x==null||x.trim().isEmpty()){x=UUID.randomUUID().toString();p.edit().putString("device_id",x).apply();}
        return x;
    }

    /** Old SQLite triggers used second precision. Upgrade fresh pending writes to millisecond precision. */
    private void normalizePendingTimes752(){
        long now=System.currentTimeMillis();
        try{db.getWritableDatabase().execSQL("UPDATE pending_sync SET created_at=? WHERE created_at%1000=0 AND created_at>=?",new Object[]{now,now-2500L});}catch(Exception ignored){}
    }

    private long stamp752(String kind,String key){
        Cursor c=null;
        try{
            if(key==null){c=db.getReadableDatabase().rawQuery("SELECT MAX(created_at) FROM pending_sync WHERE kind=?",new String[]{kind});}
            else{c=db.getReadableDatabase().rawQuery("SELECT created_at FROM pending_sync WHERE kind=? AND entity_key=? LIMIT 1",new String[]{kind,key});}
            if(c.moveToFirst()&&!c.isNull(0)){long v=c.getLong(0);if(v>0)return v;}
        }catch(Exception ignored){}finally{if(c!=null)c.close();}
        return System.currentTimeMillis();
    }

    private void flushAthleteLww752()throws Exception{
        for(int round=0;round<100;round++){
            Cursor c=db.getReadableDatabase().rawQuery("SELECT entity_key,created_at FROM pending_sync WHERE kind='ATHLETE' ORDER BY created_at ASC LIMIT 1",null);
            if(!c.moveToFirst()){c.close();break;}
            String key=c.getString(0);long stamp=c.getLong(1);c.close();
            long id;try{id=Long.parseLong(key);}catch(Exception e){db.getWritableDatabase().delete("pending_sync","kind='ATHLETE' AND entity_key=?",new String[]{key});continue;}
            Method m=MainActivityV740.class.getDeclaredMethod("body740",long.class);m.setAccessible(true);JSONObject body=(JSONObject)m.invoke(this,id);
            body.put("p_version_ms",stamp).put("p_device_id",device752);
            String token=cloudPrefs.getString("access_token","");
            HttpResult r=super.request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_lww_v430",body.toString(),token);
            if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=super.request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_lww_v430",body.toString(),token);}
            if(r.code<200||r.code>=300)throw new Exception("SPORCU "+id+" LWW HTTP "+r.code);
            if(r.body!=null&&r.body.contains("\"applied\":false"))forceAthletePull752();
            db.getWritableDatabase().delete("pending_sync","kind='ATHLETE' AND entity_key=? AND created_at<=?",new String[]{key,String.valueOf(stamp)});
        }
    }

    private void forceAthletePull752(){
        try{cloudPrefs.edit().putLong("domain_rev_750_athletes",-1).putLong("domain_rev_750_payments",-1).putLong("domain_rev_750_fee_periods",-1).apply();}catch(Exception ignored){}
    }

    private void forceAttendancePull752(){
        try{cloudPrefs.edit()
            .putLong("att_rev_751_mobile_attendance_schedule",-1)
            .putLong("att_rev_751_mobile_attendance_sessions",-1)
            .putLong("att_rev_751_mobile_attendance_records",-1)
            .putLong("domain_rev_750_mobile_attendance_schedule",-1)
            .putLong("domain_rev_750_mobile_attendance_sessions",-1)
            .putLong("domain_rev_750_mobile_attendance_records",-1).apply();
            fast752.schedule(()->syncFromCloud(false),120,TimeUnit.MILLISECONDS);
        }catch(Exception ignored){}
    }

    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        if(url==null||body==null||!"POST".equalsIgnoreCase(method))return super.request(method,url,body,bearer);
        String routed=url;
        JSONObject j;
        try{j=new JSONObject(body);}catch(Exception e){return super.request(method,url,body,bearer);}
        String kind=null,key=null;

        if(url.contains("/rpc/parion_sync_one_athlete_lww_v411")){
            kind="ATHLETE";key=String.valueOf(j.optLong("p_legacy_id",-1));
            j.put("p_version_ms",stamp752(kind,key));j.put("p_device_id",device752);
            routed=url.replace("parion_sync_one_athlete_lww_v411","parion_sync_one_athlete_lww_v430");
        }else if(url.contains("/rpc/parion_sync_attendance_entity_v1")){
            kind=j.optString("p_kind","");key=j.optString("p_key","");
            j.put("p_version_ms",stamp752(kind,key));j.put("p_device_id",device752);
            routed=url.replace("parion_sync_attendance_entity_v1","parion_sync_attendance_lww_v2");
        }else if(url.contains("/rpc/parion_sync_material_snapshot_v1")){
            kind="MATERIAL";key="ALL";
            j.put("p_version_ms",stamp752(kind,null));j.put("p_device_id",device752);
            routed=url.replace("parion_sync_material_snapshot_v1","parion_sync_material_lww_v430");
        }else if(url.contains("/rpc/parion_sync_restart_ends_v1")){
            kind="MEMBERSHIP";key="ALL";
            j.put("p_version_ms",stamp752(kind,null));j.put("p_device_id",device752);
            routed=url.replace("parion_sync_restart_ends_v1","parion_sync_restart_ends_lww_v430");
        }

        HttpResult r=super.request(method,routed,j.toString(),bearer);
        if(r!=null&&r.code>=200&&r.code<300&&r.body!=null&&r.body.contains("\"applied\":false")){
            if(kind!=null&&kind.startsWith("ATT_"))forceAttendancePull752();
            else if("ATHLETE".equals(kind))forceAthletePull752();
        }
        return r;
    }

    @Override protected void onDestroy(){resumed752=false;fast752.shutdownNow();athlete752.shutdownNow();super.onDestroy();}
}
