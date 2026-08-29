package com.parion.aidat;

import android.os.Bundle;
import org.json.*;
import java.util.concurrent.*;

/** v4.2.24 - robust cloud media index. Never clears known media on failed refresh. */
public class MainActivityV745 extends MainActivityV744 {
    private final ScheduledExecutorService media745=Executors.newSingleThreadScheduledExecutor();
    private volatile boolean destroyed745=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        scheduleMedia745(150);
    }

    @Override protected void onResume(){
        super.onResume();
        scheduleMedia745(300);
    }

    @Override void syncFromCloud(boolean announce){
        super.syncFromCloud(announce);
        scheduleMedia745(1200);
        scheduleMedia745(3500);
    }

    private void scheduleMedia745(long delay){
        if(destroyed745)return;
        try{media745.schedule(this::refreshMedia745,delay,TimeUnit.MILLISECONDS);}catch(Exception ignored){}
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
            if(a.length()<200)return; // never replace a healthy index with an obviously partial response

            java.util.HashMap<Long,String> photos=new java.util.HashMap<>();
            java.util.HashMap<Long,String> forms=new java.util.HashMap<>();
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;
                String pp=o.isNull("photo_path")?"":o.optString("photo_path","").trim();
                String fp=o.isNull("registration_form_path")?"":o.optString("registration_form_path","").trim();
                if(!pp.isEmpty()&&!"null".equalsIgnoreCase(pp))photos.put(id,pp);
                if(!fp.isEmpty()&&!"null".equalsIgnoreCase(fp))forms.put(id,fp);
            }
            // Production currently has far more than a handful of forms. Guard against auth/RLS/partial-response regressions.
            if(forms.size()<150)return;
            photoMap413().clear();photoMap413().putAll(photos);
            formMap413().clear();formMap413().putAll(forms);
            if(db!=null){
                android.database.sqlite.SQLiteDatabase d=db.getWritableDatabase();
                for(java.util.Map.Entry<Long,String> e:photos.entrySet()){
                    d.execSQL("UPDATE athletes SET photo=? WHERE id=?",new Object[]{"CLOUD:"+e.getValue(),e.getKey()});
                }
            }
            runOnUiThread(()->{if(!destroyed745)showHome();});
        }catch(Exception ignored){
            // Deliberately keep the previous in-memory media index on failure.
        }
    }

    @Override protected void onDestroy(){
        destroyed745=true;media745.shutdownNow();super.onDestroy();
    }
}
