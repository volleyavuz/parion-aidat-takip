package com.parion.aidat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.FileObserver;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivityV442 extends MainActivityV441 {
    private final ExecutorService syncExec442 = Executors.newSingleThreadExecutor();
    private final AtomicBoolean syncBusy442 = new AtomicBoolean(false);
    private FileObserver dbObserver442;
    private ConnectivityManager cm442;
    private ConnectivityManager.NetworkCallback netCb442;
    private volatile long lastDbEvent442 = 0L;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        migrateSyncInfra442();
        recordLastLogin442();
        startDbObserver442();
        startConnectivity442();
        syncExec442.execute(() -> { if (isOnline442()) runEventSync442(true); });
    }

    @Override protected void onDestroy() {
        try { if (dbObserver442 != null) dbObserver442.stopWatching(); } catch (Throwable ignored) {}
        try { if (cm442 != null && netCb442 != null) cm442.unregisterNetworkCallback(netCb442); } catch (Throwable ignored) {}
        syncExec442.shutdownNow();
        super.onDestroy();
    }

    private SQLiteDatabase db442() {
        try {
            File f = getDatabasePath("parion_spor_okulu.db");
            if (!f.exists()) return null;
            return SQLiteDatabase.openDatabase(f.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        } catch (Throwable t) { return null; }
    }

    private void migrateSyncInfra442() {
        SQLiteDatabase d = db442(); if (d == null) return;
        try {
            d.beginTransaction();
            String[] drops = {"q_membership_events_ai","q_membership_events_au","q_membership_events_ad","q_athletes_au"};
            for (String n : drops) try { d.execSQL("DROP TRIGGER IF EXISTS " + n); } catch (Throwable ignored) {}
            d.execSQL("CREATE TRIGGER IF NOT EXISTS q_athletes_au_profile AFTER UPDATE ON athletes WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 AND OLD.photo IS NEW.photo BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('SPORCU',CAST(NEW.id AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','SPORCU',CAST(NEW.id AS TEXT),COALESCE(NEW.name,'SPORCU')); END");
            d.execSQL("CREATE TRIGGER IF NOT EXISTS q_athletes_au_photo AFTER UPDATE OF photo ON athletes WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 AND OLD.photo IS NOT NEW.photo BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('SPORCU',CAST(NEW.id AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','FOTOĞRAF',CAST(NEW.id AS TEXT),COALESCE(NEW.name,'SPORCU')); END");
            d.execSQL("CREATE TRIGGER IF NOT EXISTS q_registration_forms_ai_log AFTER INSERT ON registration_forms WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('YÜKLENDİ','KAYIT FORMU',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')); END");
            d.execSQL("CREATE TRIGGER IF NOT EXISTS q_registration_forms_au_log AFTER UPDATE ON registration_forms WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','KAYIT FORMU',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')); END");
            d.execSQL("CREATE TRIGGER IF NOT EXISTS q_registration_forms_ad_log AFTER DELETE ON registration_forms WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('SİLİNDİ','KAYIT FORMU',CAST(OLD.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=OLD.athleteId),'SPORCU')); END");
            d.execSQL("DELETE FROM activity_log_local WHERE entityType NOT IN ('SPORCU','ÖDEME','AİDAT','FOTOĞRAF','KAYIT FORMU','BAĞLANTI')");
            d.setTransactionSuccessful();
        } catch (Throwable ignored) {
        } finally { try { d.endTransaction(); } catch (Throwable ignored) {} try { d.close(); } catch (Throwable ignored) {} }
    }

    private void startDbObserver442() {
        try {
            final File dbFile = getDatabasePath("parion_spor_okulu.db");
            final File dir = dbFile.getParentFile();
            dbObserver442 = new FileObserver(dir, FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO | FileObserver.CREATE | FileObserver.MODIFY) {
                @Override public void onEvent(int event, String path) {
                    if (path == null || !path.startsWith("parion_spor_okulu.db")) return;
                    long now = System.currentTimeMillis();
                    if (now - lastDbEvent442 < 700) return;
                    lastDbEvent442 = now;
                    syncExec442.execute(() -> {
                        try { Thread.sleep(350); } catch (InterruptedException ignored) { return; }
                        if (isOnline442() && pendingCount442() > 0) runEventSync442(false);
                    });
                }
            };
            dbObserver442.startWatching();
        } catch (Throwable ignored) {}
    }

    private void startConnectivity442() {
        try {
            cm442 = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            netCb442 = new ConnectivityManager.NetworkCallback() {
                @Override public void onAvailable(Network network) {
                    setConnectionState442(true);
                    syncExec442.execute(() -> runEventSync442(true));
                }
                @Override public void onLost(Network network) {
                    if (!isOnline442()) setConnectionState442(false);
                }
            };
            cm442.registerDefaultNetworkCallback(netCb442);
            setConnectionState442(isOnline442());
        } catch (Throwable ignored) {}
    }

    private boolean isOnline442() {
        try {
            ConnectivityManager c = cm442 != null ? cm442 : (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network n = c.getActiveNetwork(); if (n == null) return false;
            NetworkCapabilities nc = c.getNetworkCapabilities(n);
            return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (Throwable t) { return false; }
    }

    private int pendingCount442() {
        SQLiteDatabase d = db442(); if (d == null) return 0;
        Cursor c = null;
        try { c = d.rawQuery("SELECT COUNT(*) FROM sync_queue", null); return c.moveToFirst() ? c.getInt(0) : 0; }
        catch (Throwable t) { return 0; }
        finally { if (c != null) c.close(); d.close(); }
    }

    private void runEventSync442(boolean pullFirst) {
        if (!isOnline442() || !syncBusy442.compareAndSet(false, true)) return;
        try {
            if (pullFirst) {
                invokeNoArg442("syncFromCloud");
                invokeNoArg442("beginRegisteredMediaMigration404");
            }
            if (pendingCount442() > 0) invokeNoArg442("startSingleAutoSync440");
            invokeNoArg442("beginRegisteredMediaMigration404");
        } finally { syncBusy442.set(false); }
    }

    private Object invokeNoArg442(String name) {
        Class<?> c = getClass();
        while (c != null) {
            try { Method m = c.getDeclaredMethod(name); m.setAccessible(true); return m.invoke(this); }
            catch (NoSuchMethodException e) { c = c.getSuperclass(); }
            catch (Throwable t) { return null; }
        }
        return null;
    }

    private String userEmail442() {
        try {
            android.content.SharedPreferences p = getSharedPreferences("parion_cloud_session", MODE_PRIVATE);
            String s = p.getString("user_email", "");
            if (s == null || s.trim().isEmpty()) s = p.getString("email", "");
            return s == null ? "" : s;
        } catch (Throwable t) { return ""; }
    }

    private void setConnectionState442(boolean online) {
        try {
            android.content.SharedPreferences p = getSharedPreferences("parion_sync_v442", MODE_PRIVATE);
            String next = online ? "ONLINE" : "OFFLINE";
            String prev = p.getString("network_state", "");
            if (next.equals(prev)) return;
            p.edit().putString("network_state", next).apply();
            SQLiteDatabase d = db442(); if (d == null) return;
            try {
                android.content.ContentValues v = new android.content.ContentValues();
                v.put("userEmail", userEmail442()); v.put("action", online ? "ÇEVRİMİÇİ" : "ÇEVRİMDIŞI");
                v.put("entityType", "BAĞLANTI"); v.put("entityId", ""); v.put("detail", online ? "ÇEVRİMİÇİ" : "ÇEVRİMDIŞI");
                d.insert("activity_log_local", null, v);
            } finally { d.close(); }
        } catch (Throwable ignored) {}
    }

    private void recordLastLogin442() {
        try {
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            getSharedPreferences("parion_sync_v442", MODE_PRIVATE).edit().putString("last_login_at", ts).apply();
        } catch (Throwable ignored) {}
    }
}
