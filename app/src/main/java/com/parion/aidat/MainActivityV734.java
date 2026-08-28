package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.lang.reflect.Method;
import java.util.concurrent.*;

/** v4.2.10 - do not report pull success until cloud payments are mirrored and verified locally. */
public class MainActivityV734 extends MainActivityV733 {
    private final ScheduledExecutorService verify734=Executors.newSingleThreadScheduledExecutor();
    private volatile long pullGeneration734=0L;

    @Override void syncFromCloud(boolean announce){
        long gen=++pullGeneration734;
        super.syncFromCloud(announce);
        waitForRealPull734(gen,false,announce,0,false);
    }

    @Override void showCloudMenu(){
        String[] items={"GÜVENLİ SENKRONİZE ET (DELTA)","BULUTA YÜKLE (SNAPSHOT)","BULUTTAN ÇEK (SNAPSHOT)","BULUTTAN TEMİZ GERİ YÜKLE","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("SENKRONİZASYON").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else if(w==1)invokePrivate734(MainActivityV731.class,"confirmSnapshotUpload731");
            else if(w==2)confirmSnapshotPull734();
            else if(w==3)invokePrivate734(MainActivityV730.class,"confirmRestore730");
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    private void confirmSnapshotPull734(){
        new AlertDialog.Builder(this).setTitle("BULUTTAN ÇEK • SNAPSHOT")
            .setMessage("Bulut ana kaynak kabul edilir. Bu cihazdaki yerel sporcu, ödeme, aidat ve yoklama kopyası temizlenip buluttan yeniden oluşturulur.\n\nÖdeme verileri ayrıca buluttan tekrar okunup doğrulanmadan işlem tamamlandı sayılmaz.\n\nBu cihazdaki gönderilmemiş değişiklikler kaybolur. Devam edilsin mi?")
            .setNegativeButton("VAZGEÇ",null).setPositiveButton("BULUTTAN ÇEK",(d,w)->snapshotPull734()).show();
    }

    private void snapshotPull734(){
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty()){toast("BULUT OTURUMU YOK.");return;}
        toast("BULUT SNAPSHOT'I ALINIYOR • ÖDEMELER DOĞRULANACAK...");
        try{
            SQLiteDatabase x=db.getWritableDatabase();x.beginTransaction();
            try{
                x.delete("payment_recent",null,null);x.delete("sync_state",null,null);x.delete("athlete_restart_periods",null,null);
                x.delete("fee_history",null,null);x.delete("payments",null,null);x.delete("athletes",null,null);
                try{x.delete("attendance_records",null,null);x.delete("attendance_sessions",null,null);x.delete("attendance_schedule",null,null);}catch(Exception ignored){}
                x.setTransactionSuccessful();
            }finally{x.endTransaction();}
            long gen=++pullGeneration734;
            super.syncFromCloud(false);
            waitForRealPull734(gen,true,true,0,false);
        }catch(Exception e){toast("BULUTTAN SNAPSHOT BAŞLATILAMADI • "+short734(e));}
    }

    /** Wait until the inherited asynchronous cloud pull actually starts and then finishes. */
    private void waitForRealPull734(long gen,boolean snapshot,boolean announce,int tries,boolean seenBusy){
        if(gen!=pullGeneration734)return;
        boolean busy=false;try{busy=syncing;}catch(Exception ignored){}
        boolean seen=seenBusy||busy;
        if(seen&&!busy){
            verify734.execute(()->{
                try{
                    int count=replacePaymentsFromCloud734();
                    runOnUiThread(()->{
                        if(snapshot)toast("BULUT SNAPSHOT'I DOĞRULANDI • "+count+" ÖDEME KAYDI");
                        else if(announce)toast("BULUT ÖDEMELERİ DOĞRULANDI • "+count+" KAYIT");
                        showHome();
                    });
                }catch(Exception e){runOnUiThread(()->toast("ÖDEME SENKRONİZASYONU DOĞRULANAMADI • "+short734(e)));}
            });
            return;
        }
        if(tries>=120){if(snapshot)runOnUiThread(()->toast("BULUTTAN ÇEK TAMAMLANAMADI • ÖDEME DOĞRULAMASI BAŞLAMADI"));return;}
        final boolean nextSeen=seen;
        verify734.schedule(()->waitForRealPull734(gen,snapshot,announce,tries+1,nextSeen),500,TimeUnit.MILLISECONDS);
    }

    private int replacePaymentsFromCloud734()throws Exception{
        HttpResult r=getAuthed("/rest/v1/mobile_payments_legacy?select=legacy_id,year,month,marker,amount&order=legacy_id.asc,year.asc,month.asc");
        if(r.code==401&&refreshSession())r=getAuthed("/rest/v1/mobile_payments_legacy?select=legacy_id,year,month,marker,amount&order=legacy_id.asc,year.asc,month.asc");
        if(r.code<200||r.code>=300)throw new IllegalStateException("HTTP "+r.code);
        JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();int n=0;
        try{
            d.delete("payments",null,null);
            d.delete("payment_recent",null,null);
            for(int i=0;i<a.length();i++){
                JSONObject p=a.getJSONObject(i);long id=p.optLong("legacy_id",-1);int y=p.optInt("year",0),m=p.optInt("month",0);if(id<=0||y<=0||m<1||m>12)continue;
                ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",y);v.put("month",m);v.put("marker",p.optString("marker",""));v.put("amount",p.optInt("amount",0));
                d.insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE);n++;
            }
            d.setTransactionSuccessful();
        }finally{d.endTransaction();}
        return n;
    }

    private void invokePrivate734(Class<?> owner,String name){
        try{Method m=owner.getDeclaredMethod(name);m.setAccessible(true);m.invoke(this);}catch(Exception e){toast("İŞLEM AÇILAMADI • "+short734(e));}
    }
    private String short734(Exception e){String s=e.getMessage();return s==null||s.trim().isEmpty()?e.getClass().getSimpleName():s;}

    @Override protected void onDestroy(){verify734.shutdownNow();super.onDestroy();}
}
