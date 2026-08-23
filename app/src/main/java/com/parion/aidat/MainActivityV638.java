package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import androidx.work.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** v4.0.38 - durable pending sync queue + WorkManager retry. */
public class MainActivityV638 extends MainActivityV637 {
    private static final String UNIQUE_NOW="parion-pending-sync";
    private static final String UNIQUE_PERIODIC="parion-periodic-sync";

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        ensurePending638();
        enqueueSnapshot638("APP_OPEN");
        scheduleSync638();
    }

    private void ensurePending638(){
        try{db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS pending_sync(id INTEGER PRIMARY KEY AUTOINCREMENT,kind TEXT NOT NULL,entity_key TEXT NOT NULL DEFAULT '',created_at INTEGER NOT NULL,UNIQUE(kind,entity_key))");}catch(Exception ignored){}
    }

    private void enqueueSnapshot638(String reason){
        ensurePending638();
        try{db.getWritableDatabase().execSQL("INSERT OR REPLACE INTO pending_sync(kind,entity_key,created_at) VALUES('LOCAL_SNAPSHOT','ALL',?)",new Object[]{System.currentTimeMillis()});}catch(Exception ignored){}
        try{
            Constraints c=new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest w=new OneTimeWorkRequest.Builder(ParionSyncWorker.class).setConstraints(c).setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).build();
            WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(UNIQUE_NOW,ExistingWorkPolicy.REPLACE,w);
        }catch(Exception ignored){}
    }

    private void scheduleSync638(){
        try{
            Constraints c=new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            PeriodicWorkRequest p=new PeriodicWorkRequest.Builder(ParionSyncWorker.class,15,TimeUnit.MINUTES).setConstraints(c).build();
            WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(UNIQUE_PERIODIC,ExistingPeriodicWorkPolicy.KEEP,p);
        }catch(Exception ignored){}
    }

    @Override void showProfile(long id){super.showProfile(id);enqueueSnapshot638("PROFILE");}

    @Override void showHome(){
        super.showHome();
        if(db==null||root==null)return;
        ensurePending638();
        int pending=0;try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM pending_sync",null);if(c.moveToFirst())pending=c.getInt(0);c.close();}catch(Exception ignored){}
        long last=getSharedPreferences("parion_cloud_session",MODE_PRIVATE).getLong("last_background_sync",0L);
        String lastText=last<=0?"henüz yok":new SimpleDateFormat("dd.MM HH:mm",new Locale("tr","TR")).format(new Date(last));
        TextView s=tv(pending>0?"☁ BULUT • "+pending+" DEĞİŞİKLİK BEKLİYOR • SON: "+lastText:"☁ BULUT GÜNCEL • SON: "+lastText,10,pending>0?Color.rgb(180,115,0):Color.rgb(0,125,70),true);
        s.setGravity(Gravity.CENTER);s.setPadding(0,2,0,4);s.setOnClickListener(v->{enqueueSnapshot638("MANUAL_STATUS");toast("Bulut eşitlemesi sıraya alındı.");});
        int pos=Math.min(1,root.getChildCount());root.addView(s,pos);
    }

    @Override protected void onStop(){
        enqueueSnapshot638("APP_STOP");
        super.onStop();
    }
}
