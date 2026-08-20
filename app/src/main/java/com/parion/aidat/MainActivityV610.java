package com.parion.aidat;

import android.os.SystemClock;
import java.lang.reflect.Field;

/** v4.0.12 - hard ANR hotfix: automatic full cloud restore disabled; dashboard work collapsed per render. */
public class MainActivityV610 extends MainActivityV609 {
    private DashData dashCache610;
    private long dashCacheAt610=0L;

    /**
     * Full BULUT -> YEREL refresh is no longer allowed to start automatically from
     * onCreate/onResume. It rewrites the local SQLite tables and can block home UI
     * readers long enough to trigger ANR. Manual sync (announce=true) is preserved.
     */
    @Override void syncFromCloud(boolean announce){
        if(!announce) return;
        dashCache610=null;dashCacheAt610=0L;
        super.syncFromCloud(true);
    }

    /** V36 builds dashboard once and V398 asks for it again in the same render. Reuse it. */
    @Override DashData dashboardData(){
        long now=SystemClock.uptimeMillis();
        if(dashCache610!=null && now-dashCacheAt610<2500L) return dashCache610;
        DashData d=super.dashboardData();
        dashCache610=d;dashCacheAt610=now;
        return d;
    }

    @Override void showHome(){
        // Never suppress a requested home navigation, but reuse expensive dashboard data
        // within the same render pass.
        try{
            Field f=MainActivityV609.class.getDeclaredField("lastHomeBuild609");
            f.setAccessible(true);f.setLong(this,0L);
        }catch(Exception ignored){}
        super.showHome();
    }

    @Override void showProfile(long id){
        // Any profile/payment edit may change dashboard totals; force a fresh value next time.
        dashCache610=null;dashCacheAt610=0L;
        super.showProfile(id);
    }
}
