package com.parion.aidat;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import java.io.File;

/** Stable full-release layer on top of the real v3.8.8 APK.
 *  - blocks legacy automatic syncFromCloud on launch
 *  - caps local activity log at 50 rows
 */
public class MainActivityV387 extends MainActivityV386 {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        capLog387();
    }

    @Override void syncFromCloud() {
        // Deliberately disabled. Cloud bulk snapshot sync is also revoked server-side.
    }

    private SQLiteDatabase db387() {
        try {
            File f = getDatabasePath("parion_spor_okulu.db");
            if (!f.exists()) return null;
            return SQLiteDatabase.openDatabase(f.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        } catch (Throwable t) { return null; }
    }

    private void capLog387() {
        SQLiteDatabase d = db387();
        if (d == null) return;
        try {
            d.beginTransaction();
            d.execSQL("DROP TRIGGER IF EXISTS cap_activity_log_50");
            d.execSQL("CREATE TRIGGER cap_activity_log_50 AFTER INSERT ON activity_log_local BEGIN DELETE FROM activity_log_local WHERE id NOT IN (SELECT id FROM activity_log_local ORDER BY id DESC LIMIT 50); END");
            d.execSQL("DELETE FROM activity_log_local WHERE id NOT IN (SELECT id FROM activity_log_local ORDER BY id DESC LIMIT 50)");
            d.setTransactionSuccessful();
        } catch (Throwable ignored) {
        } finally {
            try { d.endTransaction(); } catch (Throwable ignored) {}
            try { d.close(); } catch (Throwable ignored) {}
        }
    }
}
