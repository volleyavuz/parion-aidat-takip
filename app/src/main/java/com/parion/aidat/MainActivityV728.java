package com.parion.aidat;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;

/** v4.2.3 - explicit disaster recovery: discard this device cache and rebuild only from cloud. */
public class MainActivityV728 extends MainActivityV727 {
    private volatile boolean restore728=false;

    @Override void showCloudMenu(){
        int n=0;try{n=findPending728();}catch(Exception ignored){}
        final int pending=n;
        String[] items={"ŞİMDİ GÜVENLİ SENKRONİZE ET","BULUTTAN TEMİZ GERİ YÜKLE","BEKLEYEN YEREL DEĞİŞİKLİK: "+pending,"OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("GÜVENLİ ÇOKLU CİHAZ SENKRONİZASYONU").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else if(w==1)confirmRestore728();
            else if(w==2)toast(pending==0?"BEKLEYEN DEĞİŞİKLİK YOK.":pending+" DEĞİŞİKLİK BEKLİYOR.");
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    private int findPending728(){
        int n=0;android.database.Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes",null);
        while(c.moveToNext()){
            long id=c.getLong(0);
            android.database.Cursor s=db.getReadableDatabase().rawQuery("SELECT localHash FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});
            if(!s.moveToFirst()||s.isNull(0)||s.getString(0).trim().isEmpty())n++;
            s.close();
        }
        c.close();return n;
    }

    private void confirmRestore728(){
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty()){toast("ÖNCE BULUT OTURUMU AÇILMALI.");showLogin();return;}
        new AlertDialog.Builder(this)
            .setTitle("BULUTTAN TEMİZ GERİ YÜKLE")
            .setMessage("Bu işlem BU CİHAZDAKİ yerel sporcu, ödeme ve ilgili önbellek verilerini siler ve buluttaki güncel verileri yeniden indirir.\n\nBu işlem sırasında bu cihazdan buluta hiçbir veri gönderilmez. Buluttaki kayıtlar silinmez veya değiştirilmez.\n\nBu cihazdaki gönderilmemiş değişiklikler varsa kaybolur. Devam edilsin mi?")
            .setNegativeButton("VAZGEÇ",null)
            .setPositiveButton("BULUTTAN GERİ YÜKLE",(d,w)->restoreFromCloud728())
            .show();
    }

    private void restoreFromCloud728(){
        if(restore728)return;restore728=true;
        toast("BULUTTAN TEMİZ GERİ YÜKLEME BAŞLADI • BU CİHAZ BULUTA YAZMAYACAK");
        try{
            SQLiteDatabase x=db.getWritableDatabase();x.beginTransaction();
            try{
                // Device-local cache only. Never touch cloud here.
                x.delete("payment_recent",null,null);
                x.delete("sync_state",null,null);
                x.delete("athlete_restart_periods",null,null);
                x.delete("fee_history",null,null);
                x.delete("payments",null,null);
                x.delete("athletes",null,null);
                x.setTransactionSuccessful();
            }finally{x.endTransaction();}
            // Empty-local path in V726/V727 is pull-only bootstrap; no delta or snapshot push can run.
            super.syncFromCloud(true);
            root.postDelayed(()->{restore728=false;if(db!=null&&db.count(null)>0){toast("BULUTTAN GERİ YÜKLEME TAMAMLANDI • "+db.count(null)+" SPORCU ALINDI");showHome();}else{toast("GERİ YÜKLEME TAMAMLANAMADI • TEKRAR DENEYİN");showHome();}},6500);
        }catch(Exception e){restore728=false;toast("GERİ YÜKLEME BAŞLATILAMADI • "+(e.getMessage()==null?"HATA":e.getMessage()));}
    }
}
