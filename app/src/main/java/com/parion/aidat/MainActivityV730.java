package com.parion.aidat;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;

/** v4.2.6 - cloud-only restore waits for real data arrival instead of a fixed 6.5 second timer. */
public class MainActivityV730 extends MainActivityV729 {
    private volatile boolean restore730=false;
    private long restoreStarted730=0L;
    private static final long RESTORE_TIMEOUT_730=90000L;

    @Override void showCloudMenu(){
        int n=0;try{n=findPending730();}catch(Exception ignored){}
        final int pending=n;
        String[] items={"ŞİMDİ GÜVENLİ SENKRONİZE ET","BULUTTAN TEMİZ GERİ YÜKLE","BEKLEYEN YEREL DEĞİŞİKLİK: "+pending,"OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("GÜVENLİ ÇOKLU CİHAZ SENKRONİZASYONU").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else if(w==1)confirmRestore730();
            else if(w==2)toast(pending==0?"BEKLEYEN DEĞİŞİKLİK YOK.":pending+" DEĞİŞİKLİK BEKLİYOR.");
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    private int findPending730(){
        int n=0;android.database.Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes",null);
        while(c.moveToNext()){
            long id=c.getLong(0);
            android.database.Cursor s=db.getReadableDatabase().rawQuery("SELECT localHash FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});
            if(!s.moveToFirst()||s.isNull(0)||s.getString(0).trim().isEmpty())n++;
            s.close();
        }
        c.close();return n;
    }

    private void confirmRestore730(){
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty()){toast("ÖNCE BULUT OTURUMU AÇILMALI.");showLogin();return;}
        new AlertDialog.Builder(this)
            .setTitle("BULUTTAN TEMİZ GERİ YÜKLE")
            .setMessage("Bu cihazdaki yerel sporcu, ödeme ve senkronizasyon önbelleği temizlenir; ardından yalnızca buluttaki güncel veriler yeniden indirilir.\n\nİşlem boyunca bu cihazdan buluta veri gönderilmez. Buluttaki kayıtlar değiştirilmez.\n\nBu cihazdaki gönderilmemiş yerel değişiklikler kaybolur. Devam edilsin mi?")
            .setNegativeButton("VAZGEÇ",null)
            .setPositiveButton("BULUTTAN GERİ YÜKLE",(d,w)->restoreFromCloud730())
            .show();
    }

    private void restoreFromCloud730(){
        if(restore730)return;
        restore730=true;restoreStarted730=System.currentTimeMillis();
        toast("BULUTTAN TEMİZ GERİ YÜKLEME BAŞLADI • BULUTA YAZMA KAPALI");
        try{
            SQLiteDatabase x=db.getWritableDatabase();x.beginTransaction();
            try{
                x.delete("payment_recent",null,null);
                x.delete("sync_state",null,null);
                x.delete("athlete_restart_periods",null,null);
                x.delete("fee_history",null,null);
                x.delete("payments",null,null);
                x.delete("athletes",null,null);
                x.setTransactionSuccessful();
            }finally{x.endTransaction();}
            // Exactly one pull request. Empty-local safety in V726/V727 blocks all push paths.
            syncFromCloud(true);
            pollRestore730();
        }catch(Exception e){restore730=false;toast("GERİ YÜKLEME BAŞLATILAMADI • "+(e.getMessage()==null?"HATA":e.getMessage()));showHome();}
    }

    private void pollRestore730(){
        if(!restore730||root==null)return;
        root.postDelayed(()->{
            if(!restore730)return;
            int count=0;try{count=db==null?0:db.count(null);}catch(Exception ignored){}
            if(count>0){
                restore730=false;
                toast("BULUTTAN GERİ YÜKLEME TAMAMLANDI • "+count+" SPORCU ALINDI");
                showHome();
                return;
            }
            if(System.currentTimeMillis()-restoreStarted730>=RESTORE_TIMEOUT_730){
                restore730=false;
                toast("BULUTTAN VERİ ALINAMADI • YEREL VERİ BOŞ VE BULUTA YAZMA YAPILMADI");
                showHome();
                return;
            }
            pollRestore730();
        },1000L);
    }
}
