package com.parion.aidat;

import android.content.*;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * v4.2.2 safety worker.
 * Background whole-database writes are intentionally disabled.
 * Foreground MainActivityV727 performs conflict-checked per-athlete delta sync.
 */
public class ParionSyncWorker extends Worker {
    private static final String PREF="parion_cloud_session";
    private final Context ctx;
    public ParionSyncWorker(@NonNull Context c,@NonNull WorkerParameters p){super(c,p);ctx=c.getApplicationContext();}

    @NonNull @Override public Result doWork(){
        SharedPreferences prefs=ctx.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean("background_snapshot_disabled",true)
            .putLong("last_background_safety_check",System.currentTimeMillis())
            .apply();
        // Never call parion_sync_mobile_snapshot or parion_sync_attendance_snapshot here.
        return Result.success();
    }
}
