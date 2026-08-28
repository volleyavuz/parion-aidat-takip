package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.*;

/** v4.2.13 - delta-only sync menu, missing-photo back fix, durable profile photo resolve, cloud recent-payment mirror. */
public class MainActivityV737 extends MainActivityV736 {
    private final ScheduledExecutorService postSync737=Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService photo737=Executors.newSingleThreadExecutor();
    private final Set<Long> photoLookup737=java.util.Collections.newSetFromMap(new ConcurrentHashMap<Long,Boolean>());
    private volatile long syncGen737=0L;

    /** Snapshot buttons are intentionally removed: normal use is LWW delta; clean restore remains as disaster recovery. */
    @Override void showCloudMenu(){
        String[] items={"GÜVENLİ SENKRONİZE ET (DELTA)","BULUTTAN TEMİZ GERİ YÜKLE","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("SENKRONİZASYON").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else if(w==1)invokePrivate737(MainActivityV730.class,"confirmRestore730");
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    /** The custom missing-photo page was unknown to the legacy back router and could close the app. */
    @Override void goBack(){
        if("MISSING_PHOTOS_736".equals(page)){showHome();return;}
        super.goBack();
    }

    @Override public void onBackPressed(){
        if("MISSING_PHOTOS_736".equals(page)){showHome();return;}
        super.onBackPressed();
    }

    /** Make an already-known CLOUD: ref available before inherited profile rendering. */
    @Override void showProfile(long id){
        try{
            Cursor c=db.athlete(id);
            if(c.moveToFirst()){
                int pi=c.getColumnIndex("photo");
                String local=pi>=0&&!c.isNull(pi)?c.getString(pi):"";
                if(local!=null&&local.startsWith("CLOUD:")&&local.length()>6)photoMap413().put(id,local.substring(6));
            }
            c.close();
            String known=photoMap413().get(id);
            if(known!=null&&!known.trim().isEmpty()){
                ContentValues v=new ContentValues();v.put("photo","CLOUD:"+known);
                db.getWritableDatabase().update("athletes",v,"id=?",new String[]{String.valueOf(id)});
            }
        }catch(Exception ignored){}
        super.showProfile(id);
        if(!photoMap413().containsKey(id)&&photoLookup737.add(id))photo737.execute(()->resolveOnePhoto737(id));
    }

    private void resolveOnePhoto737(long id){
        try{
            HttpResult r=getAuthed("/rest/v1/athletes?legacy_id=eq."+id+"&select=photo_path&limit=1");
            if(r.code==401&&refreshSession())r=getAuthed("/rest/v1/athletes?legacy_id=eq."+id+"&select=photo_path&limit=1");
            if(r.code<200||r.code>=300)return;
            JSONArray a=new JSONArray(r.body);if(a.length()==0)return;
            String p=a.getJSONObject(0).optString("photo_path","").trim();
            if(p.isEmpty()||"null".equalsIgnoreCase(p))return;
            photoMap413().put(id,p);ContentValues v=new ContentValues();v.put("photo","CLOUD:"+p);
            db.getWritableDatabase().update("athletes",v,"id=?",new String[]{String.valueOf(id)});
            runOnUiThread(()->{if(currentAthlete==id)showProfile(id);});
        }catch(Exception ignored){}finally{photoLookup737.remove(id);}
    }

    /** After every real sync, rebuild Finance/Recent Payments from cloud payment updated_at ordering. */
    @Override void syncFromCloud(boolean announce){
        long gen=++syncGen737;
        super.syncFromCloud(announce);
        waitRecent737(gen,0,false,announce);
    }

    private void waitRecent737(long gen,int tries,boolean seenBusy,boolean announce){
        if(gen!=syncGen737)return;
        boolean busy=false;try{busy=syncing;}catch(Exception ignored){}
        boolean seen=seenBusy||busy;
        if(seen&&!busy){
            // V734 and V736 also perform post-pull mirrors. Run after them so payment_recent cannot be cleared afterwards.
            postSync737.schedule(()->{
                try{
                    int n=reconcileRecentPayments737();
                    runOnUiThread(()->{if(announce)toast("ERKEN ÖDEME GEÇMİŞİ SENKRONİZE • "+n+" KAYIT");});
                }catch(Exception e){runOnUiThread(()->toast("ERKEN ÖDEME GEÇMİŞİ SENKRONİZE EDİLEMEDİ."));}
            },2200L,TimeUnit.MILLISECONDS);
            return;
        }
        if(tries>=120)return;
        final boolean nextSeen=seen;
        postSync737.schedule(()->waitRecent737(gen,tries+1,nextSeen,announce),500L,TimeUnit.MILLISECONDS);
    }

    private int reconcileRecentPayments737()throws Exception{
        HttpResult r=getAuthed("/rest/v1/mobile_payment_recent_v413?select=legacy_id,year,month,amount,updated_at&order=updated_at.desc&limit=500");
        if(r.code==401&&refreshSession())r=getAuthed("/rest/v1/mobile_payment_recent_v413?select=legacy_id,year,month,amount,updated_at&order=updated_at.desc&limit=500");
        if(r.code<200||r.code>=300)throw new IllegalStateException("HTTP "+r.code);
        JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();int n=0;long base=System.currentTimeMillis();
        try{
            d.delete("payment_recent",null,null);
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);int y=o.optInt("year",0),m=o.optInt("month",0),amt=o.optInt("amount",0);
                if(id<=0||y<=0||m<1||m>12||amt<=0)continue;
                ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",y);v.put("month",m);v.put("amount",amt);
                // Preserve authoritative cloud ordering identically on every device.
                v.put("savedAt",base-i);
                d.insertWithOnConflict("payment_recent",null,v,SQLiteDatabase.CONFLICT_REPLACE);n++;
            }
            d.setTransactionSuccessful();
        }finally{d.endTransaction();}
        return n;
    }

    private void invokePrivate737(Class<?> owner,String name){
        try{Method m=owner.getDeclaredMethod(name);m.setAccessible(true);m.invoke(this);}catch(Exception e){toast("İŞLEM AÇILAMADI.");}
    }

    @Override protected void onDestroy(){postSync737.shutdownNow();photo737.shutdownNow();super.onDestroy();}
}
