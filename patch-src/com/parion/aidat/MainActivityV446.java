package com.parion.aidat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * v3.8.68 clean-install bridge.
 * - stays on V444 stable/ANR-free base
 * - performs cloud restore only once after login on a background worker
 * - no repeating cloud polling loop
 * - retries restore only for a short bounded window after first install/login
 * - reuses the app's already-working syncFromCloud implementation so media/path logic stays compatible
 */
public class MainActivityV446 extends MainActivityV444 {
    private final ScheduledExecutorService restore446 = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService events446 = Executors.newSingleThreadExecutor();
    private final AtomicBoolean restoreBusy446 = new AtomicBoolean(false);
    private ConnectivityManager.NetworkCallback networkCallback446;
    private volatile int restoreAttempts446 = 0;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        registerNetwork446();
        // Bounded bootstrap window only: every 15 s, max 20 attempts (~5 minutes), then stops.
        restore446.scheduleWithFixedDelay(this::tryOneTimeRestore446, 2, 15, TimeUnit.SECONDS);
    }

    @Override protected void onResume() {
        super.onResume();
        // Event based: app returned to foreground; if first restore is still pending, try once.
        events446.execute(this::tryOneTimeRestore446);
    }

    @Override protected void onDestroy() {
        try { restore446.shutdownNow(); } catch (Throwable ignored) {}
        try { events446.shutdownNow(); } catch (Throwable ignored) {}
        try {
            ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm!=null && networkCallback446!=null) cm.unregisterNetworkCallback(networkCallback446);
        } catch (Throwable ignored) {}
        super.onDestroy();
    }

    private void registerNetwork446() {
        try {
            final ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm==null) return;
            networkCallback446=new ConnectivityManager.NetworkCallback(){
                @Override public void onAvailable(Network network){
                    try { events446.execute(MainActivityV446.this::tryOneTimeRestore446); } catch(Throwable ignored) {}
                }
            };
            NetworkRequest req=new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            cm.registerNetworkCallback(req, networkCallback446);
        } catch (Throwable ignored) {}
    }

    private boolean restoreDone446() {
        try { return getSharedPreferences("parion_clean_restore_446",MODE_PRIVATE).getBoolean("done",false); }
        catch(Throwable t){ return false; }
    }

    private void setRestoreDone446() {
        try { getSharedPreferences("parion_clean_restore_446",MODE_PRIVATE).edit().putBoolean("done",true).commit(); }
        catch(Throwable ignored) {}
    }

    private boolean hasSession446() {
        try {
            String t=getSharedPreferences("parion_cloud_session",MODE_PRIVATE).getString("access_token","");
            return t!=null && !t.trim().isEmpty();
        } catch(Throwable t){ return false; }
    }

    private boolean online446() {
        try {
            ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm==null || cm.getActiveNetwork()==null) return false;
            NetworkCapabilities n=cm.getNetworkCapabilities(cm.getActiveNetwork());
            return n!=null && n.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch(Throwable t){ return false; }
    }

    private int athleteCount446() {
        SQLiteDatabase d=null; Cursor c=null;
        try {
            File f=getDatabasePath("parion_spor_okulu.db");
            if(!f.exists()) return 0;
            d=SQLiteDatabase.openDatabase(f.getPath(),null,SQLiteDatabase.OPEN_READONLY);
            c=d.rawQuery("SELECT COUNT(*) FROM athletes",null);
            return c.moveToFirst()?c.getInt(0):0;
        } catch(Throwable t){ return 0; }
        finally { try{if(c!=null)c.close();}catch(Throwable ignored){} try{if(d!=null)d.close();}catch(Throwable ignored){} }
    }

    private void tryOneTimeRestore446() {
        if(restoreDone446()) { try{restore446.shutdown();}catch(Throwable ignored){} return; }
        if(restoreAttempts446>=20) { try{restore446.shutdown();}catch(Throwable ignored){} return; }
        if(!hasSession446() || !online446()) return;
        if(!restoreBusy446.compareAndSet(false,true)) return;
        restoreAttempts446++;
        try {
            // If cloud data has already been restored, do not pull it again.
            if(athleteCount446()>=200) { setRestoreDone446(); return; }
            Method target=null;
            Class<?> c=getClass();
            while(c!=null && target==null){
                for(Method m:c.getDeclaredMethods()){
                    if("syncFromCloud".equals(m.getName()) && m.getParameterTypes().length==0){ target=m; break; }
                }
                c=c.getSuperclass();
            }
            if(target==null) return;
            target.setAccessible(true);
            target.invoke(this);
            // Give the existing async cloud import a short window, then verify local rows.
            try{Thread.sleep(4000);}catch(InterruptedException ignored){Thread.currentThread().interrupt();}
            if(athleteCount446()>=200) setRestoreDone446();
        } catch(Throwable ignored) {
        } finally { restoreBusy446.set(false); }
    }
}
