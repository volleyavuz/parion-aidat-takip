package com.parion.aidat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.FileObserver;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivityV443 extends MainActivityV442 {
    private ScheduledExecutorService queueWatch443;
    private volatile String lastSig443 = "";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        stopDbObserver443();
        resetAndCapLog443();
        startQueueWatch443();
    }

    @Override protected void onDestroy() {
        try { if (queueWatch443 != null) queueWatch443.shutdownNow(); } catch (Throwable ignored) {}
        super.onDestroy();
    }

    private SQLiteDatabase db443() {
        try {
            File f = getDatabasePath("parion_spor_okulu.db");
            if (!f.exists()) return null;
            return SQLiteDatabase.openDatabase(f.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        } catch (Throwable t) { return null; }
    }

    private void stopDbObserver443() {
        try {
            Class<?> c = MainActivityV442.class;
            Field f = c.getDeclaredField("dbObserver442");
            f.setAccessible(true);
            Object o = f.get(this);
            if (o instanceof FileObserver) ((FileObserver)o).stopWatching();
            f.set(this, null);
        } catch (Throwable ignored) {}
    }

    private void resetAndCapLog443() {
        SQLiteDatabase d = db443(); if (d == null) return;
        try {
            d.beginTransaction();
            boolean done = getSharedPreferences("parion_sync_v443", MODE_PRIVATE).getBoolean("log_reset_done", false);
            if (!done) {
                d.execSQL("DELETE FROM activity_log_local");
                getSharedPreferences("parion_sync_v443", MODE_PRIVATE).edit().putBoolean("log_reset_done", true).apply();
            }
            d.execSQL("DROP TRIGGER IF EXISTS cap_activity_log_50");
            d.execSQL("CREATE TRIGGER cap_activity_log_50 AFTER INSERT ON activity_log_local BEGIN DELETE FROM activity_log_local WHERE rowid NOT IN (SELECT rowid FROM activity_log_local ORDER BY rowid DESC LIMIT 50); END");
            d.execSQL("DELETE FROM activity_log_local WHERE rowid NOT IN (SELECT rowid FROM activity_log_local ORDER BY rowid DESC LIMIT 50)");
            d.setTransactionSuccessful();
        } catch (Throwable ignored) {
        } finally { try { d.endTransaction(); } catch (Throwable ignored) {} try { d.close(); } catch (Throwable ignored) {} }
    }

    private String queueSig443() {
        SQLiteDatabase d = db443(); if (d == null) return "";
        Cursor c = null;
        try {
            c = d.rawQuery("SELECT COUNT(*), COALESCE(MAX(rowid),0) FROM sync_queue", null);
            if (!c.moveToFirst()) return "0:0";
            return c.getLong(0) + ":" + c.getLong(1);
        } catch (Throwable t) { return ""; }
        finally { if (c != null) c.close(); d.close(); }
    }

    private void startQueueWatch443() {
        lastSig443 = queueSig443();
        queueWatch443 = Executors.newSingleThreadScheduledExecutor();
        queueWatch443.scheduleWithFixedDelay(() -> {
            try {
                String sig = queueSig443();
                if (sig.length() == 0 || sig.equals(lastSig443)) return;
                lastSig443 = sig;
                if (!sig.startsWith("0:")) invokeRunSync443();
            } catch (Throwable ignored) {}
        }, 3, 5, TimeUnit.SECONDS);
    }

    private void invokeRunSync443() {
        try {
            Method m = MainActivityV442.class.getDeclaredMethod("runEventSync442", boolean.class);
            m.setAccessible(true);
            m.invoke(this, false);
            lastSig443 = queueSig443();
        } catch (Throwable ignored) {}
    }
}
