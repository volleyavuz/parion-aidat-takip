package com.parion.aidat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ANR guard for the legacy V421 automatic sync loop.
 *
 * V421 starts a new self-repeating Handler callback from onCreate, showHome,
 * showProfile and form. After normal navigation many 12-second loops coexist.
 * Disable that legacy loop and replace it with exactly one background scheduler.
 */
public class MainActivityV440 extends MainActivityV439 {
    private ScheduledExecutorService syncScheduler440;
    private volatile boolean destroyed440=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        disableLegacyAutoSync440();
        startSingleAutoSync440();
    }

    private void disableLegacyAutoSync440(){
        try{
            Field alive=MainActivityV421.class.getDeclaredField("alive421");
            alive.setAccessible(true);
            alive.setBoolean(this,false);

            Field hf=MainActivityV421.class.getDeclaredField("syncHandler421");
            hf.setAccessible(true);
            Object h=hf.get(this);
            if(h instanceof Handler)((Handler)h).removeCallbacksAndMessages(null);
        }catch(Throwable ignored){}
    }

    private void startSingleAutoSync440(){
        if(syncScheduler440!=null)return;
        syncScheduler440=Executors.newSingleThreadScheduledExecutor(r->{
            Thread t=new Thread(r,"parion-autosync-440");
            t.setDaemon(true);
            return t;
        });
        syncScheduler440.scheduleWithFixedDelay(()->{
            if(destroyed440)return;
            try{
                if(pending440()<=0)return;
                invokeAutoPush440();
            }catch(Throwable ignored){}
        },3,15,TimeUnit.SECONDS);
    }

    private int pending440(){
        Cursor c=null;
        try{
            SQLiteDatabase d=db.getReadableDatabase();
            c=d.rawQuery("SELECT COUNT(*) FROM sync_queue",null);
            return c.moveToFirst()?c.getInt(0):0;
        }catch(Throwable e){return 0;}
        finally{if(c!=null)c.close();}
    }

    private void invokeAutoPush440(){
        try{
            Method m=MainActivityV421.class.getDeclaredMethod("autoPush421",boolean.class,Runnable.class);
            m.setAccessible(true);
            m.invoke(this,false,null);
        }catch(Throwable ignored){}
    }

    @Override protected void onDestroy(){
        destroyed440=true;
        if(syncScheduler440!=null){
            syncScheduler440.shutdownNow();
            syncScheduler440=null;
        }
        disableLegacyAutoSync440();
        super.onDestroy();
    }
}
