package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.*;

/** v4.2.14 base: keep v4.2.12 delta chain untouched; recent payments refresh independently. */
public class MainActivityV737 extends MainActivityV736 {
    private final ScheduledExecutorService live737=Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService photo737=Executors.newSingleThreadExecutor();
    private final Set<Long> photoLookup737=java.util.Collections.newSetFromMap(new ConcurrentHashMap<Long,Boolean>());
    private volatile boolean liveEnabled737=false;

    @Override void showCloudMenu(){
        String[] items={"GÜVENLİ SENKRONİZE ET (DELTA)","BULUTTAN TEMİZ GERİ YÜKLE","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("SENKRONİZASYON").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else if(w==1)invokePrivate737(MainActivityV730.class,"confirmRestore730");
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    @Override void goBack(){if("MISSING_PHOTOS_736".equals(page)){showHome();return;}super.goBack();}
    @Override public void onBackPressed(){if("MISSING_PHOTOS_736".equals(page)){showHome();return;}super.onBackPressed();}

    @Override void showProfile(long id){
        try{Cursor c=db.athlete(id);if(c.moveToFirst()){int pi=c.getColumnIndex("photo");String local=pi>=0&&!c.isNull(pi)?c.getString(pi):"";if(local!=null&&local.startsWith("CLOUD:")&&local.length()>6)photoMap413().put(id,local.substring(6));}c.close();}catch(Exception ignored){}
        super.showProfile(id);
        if(!photoMap413().containsKey(id)&&photoLookup737.add(id))photo737.execute(()->resolveOnePhoto737(id));
    }

    private void resolveOnePhoto737(long id){
        try{HttpResult r=getAuthed("/rest/v1/athletes?legacy_id=eq."+id+"&select=photo_path&limit=1");if(r.code==401&&refreshSession())r=getAuthed("/rest/v1/athletes?legacy_id=eq."+id+"&select=photo_path&limit=1");if(r.code<200||r.code>=300)return;JSONArray a=new JSONArray(r.body);if(a.length()==0)return;String p=a.getJSONObject(0).optString("photo_path","").trim();if(p.isEmpty()||"null".equalsIgnoreCase(p))return;photoMap413().put(id,p);ContentValues v=new ContentValues();v.put("photo","CLOUD:"+p);db.getWritableDatabase().update("athletes",v,"id=?",new String[]{String.valueOf(id)});runOnUiThread(()->{if(currentAthlete==id)showProfile(id);});}catch(Exception ignored){}finally{photoLookup737.remove(id);}
    }

    @Override protected void onResume(){super.onResume();liveEnabled737=true;scheduleLive737(300L);}
    @Override protected void onPause(){liveEnabled737=false;super.onPause();}

    private void scheduleLive737(long delay){
        live737.schedule(()->{if(!liveEnabled737)return;try{reconcileRecentPayments737();}catch(Exception ignored){}if(liveEnabled737)scheduleLive737(4000L);},delay,TimeUnit.MILLISECONDS);
    }

    protected int reconcileRecentPayments737()throws Exception{
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty()||db==null)return 0;
        HttpResult r=getAuthed("/rest/v1/mobile_payment_recent_v413?select=legacy_id,year,month,amount,updated_at&order=updated_at.desc&limit=500");
        if(r.code==401&&refreshSession())r=getAuthed("/rest/v1/mobile_payment_recent_v413?select=legacy_id,year,month,amount,updated_at&order=updated_at.desc&limit=500");
        if(r.code<200||r.code>=300)throw new IllegalStateException("HTTP "+r.code);
        JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();int n=0;long base=System.currentTimeMillis();
        try{d.delete("payment_recent",null,null);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);int y=o.optInt("year",0),m=o.optInt("month",0),amt=o.optInt("amount",0);if(id<=0||y<=0||m<1||m>12||amt<=0)continue;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",y);v.put("month",m);v.put("amount",amt);v.put("savedAt",base-i);d.insertWithOnConflict("payment_recent",null,v,SQLiteDatabase.CONFLICT_REPLACE);n++;}d.setTransactionSuccessful();}finally{d.endTransaction();}
        return n;
    }

    private void invokePrivate737(Class<?> owner,String name){try{Method m=owner.getDeclaredMethod(name);m.setAccessible(true);m.invoke(this);}catch(Exception e){toast("İŞLEM AÇILAMADI.");}}
    @Override protected void onDestroy(){liveEnabled737=false;live737.shutdownNow();photo737.shutdownNow();super.onDestroy();}
}
