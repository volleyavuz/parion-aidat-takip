package com.parion.aidat;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import java.io.File;

/**
 * v3.8.66 stability layer.
 * Deliberately extends the stable V441 directly, bypassing V442/V443 automatic sync loops.
 * Resets the visible activity log once for this version and permanently caps it at 50 rows.
 */
public class MainActivityV444 extends MainActivityV441 {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        resetAndCapLog444();
    }

    private SQLiteDatabase db444() {
        try {
            File f = getDatabasePath("parion_spor_okulu.db");
            if (!f.exists()) return null;
            return SQLiteDatabase.openDatabase(f.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        } catch (Throwable t) { return null; }
    }

    private void resetAndCapLog444() {
        SQLiteDatabase d = db444();
        if (d == null) return;
        boolean ok = false;
        try {
            d.beginTransaction();
            boolean done = getSharedPreferences("parion_sync_v444", MODE_PRIVATE)
                    .getBoolean("log_reset_v3866_done", false);
            if (!done) {
                d.execSQL("DELETE FROM activity_log_local");
            }
            d.execSQL("DROP TRIGGER IF EXISTS cap_activity_log_50");
            d.execSQL("CREATE TRIGGER cap_activity_log_50 AFTER INSERT ON activity_log_local " +
                    "BEGIN DELETE FROM activity_log_local WHERE id NOT IN " +
                    "(SELECT id FROM activity_log_local ORDER BY id DESC LIMIT 50); END");
            d.execSQL("DELETE FROM activity_log_local WHERE id NOT IN " +
                    "(SELECT id FROM activity_log_local ORDER BY id DESC LIMIT 50)");
            d.setTransactionSuccessful();
            ok = true;
        } catch (Throwable ignored) {
        } finally {
            try { d.endTransaction(); } catch (Throwable ignored) {}
            try { d.close(); } catch (Throwable ignored) {}
        }
        if (ok) {
            try {
                getSharedPreferences("parion_sync_v444", MODE_PRIVATE).edit()
                        .putBoolean("log_reset_v3866_done", true).commit();
            } catch (Throwable ignored) {}
        }
    }
}
