package com.parion.aidat;

import android.content.ContentValues;
import android.os.Bundle;
import org.json.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** v4.2.28 startup-safe cloud media index.
 * - no duplicate 150/300 ms startup refreshes
 * - remote photo-path application is guarded so it never creates local pending_sync rows
 * - failed/partial refresh never clears a healthy in-memory index
 */
public class MainActivityV745 extends MainActivityV744 {
    private final ScheduledExecutorService media745=Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean queued745=new AtomicBoolean(false);
    private volatile boolean destroyed745=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        scheduleMedia745(1800);
    }

    @Override protected void onResume(){
        super.onResume();
        // Startup refresh is owned by onCreate. Avoid a second immediate refresh on first resume.
    }

    @Override void syncFromCloud(boolean announce){
        super.syncFromCloud(announce);
        scheduleMedia745(1800);
    }

    private void scheduleMedia745(long delay){
        if(destroyed745||!queued745.compareAndSet(false,true))return;
        try{media745.schedule(()->{queued745.set(false);refreshMedia745();},delay,TimeUnit.MILLISECONDS);}catch(Exception ignored){queued745.set(false);}
    }

    private void refreshMedia745(){
        if(destroyed745||cloudPrefs==null)return;
        try{
            String token=cloudPrefs.getString("access_token","");
            if(token.isEmpty())return;
            HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_media_index_v1","{}",token);
            if(r.code==401&&refreshSession()){
                token=cloudPrefs.getString("access_token","");
                r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_media_index_v1","{}",token);
            }
            if(r.code<200||r.code>=300)return;
            JSONArray a=new JSONArray(r.body);
            if(a.length()<200)return;

            java.util.HashMap<Long,String> photos=new java.util.HashMap<>();
            java.util.HashMap<Long,String> forms=new java.util.HashMap<>();
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;
                String pp=o.isNull("photo_path")?"":o.optString("photo_path","").trim();
                String fp=o.isNull("registration_form_path")?"":o.optString("registration_form_path","").trim();
                if(!pp.isEmpty()&&!"null".equalsIgnoreCase(pp))photos.put(id,pp);
                if(!fp.isEmpty()&&!"null".equalsIgnoreCase(fp))forms.put(id,fp);
            }
            if(forms.size()<150)return;

            photoMap413().clear();photoMap413().putAll(photos);
            formMap413().clear();formMap413().putAll(forms);

            if(db!=null){
                android.database.sqlite.SQLiteDatabase d=db.getWritableDatabase();
                d.beginTransaction();
                try{
                    ContentValues g=new ContentValues();g.put("applying_remote",1);d.update("sync_guard",g,"id=1",null);
                    for(java.util.Map.Entry<Long,String> e:photos.entrySet()){
                        d.execSQL("UPDATE athletes SET photo=? WHERE id=? AND COALESCE(photo,'')<>?",new Object[]{"CLOUD:"+e.getValue(),e.getKey(),"CLOUD:"+e.getValue()});
                    }
                    g.put("applying_remote",0);d.update("sync_guard",g,"id=1",null);
                    d.setTransactionSuccessful();
                }finally{
                    try{ContentValues g=new ContentValues();g.put("applying_remote",0);d.update("sync_guard",g,"id=1",null);}catch(Exception ignored){}
                    d.endTransaction();
                }
            }
            runOnUiThread(()->{if(!destroyed745&&"HOME".equals(page))showHome();});
        }catch(Exception ignored){
            // Keep previous media maps and local rows intact on any failure.
        }
    }

    @Override protected void onDestroy(){
        destroyed745=true;media745.shutdownNow();super.onDestroy();
    }
}
