package com.parion.aidat;

import android.os.*;
import java.lang.reflect.Field;

/** v4.0.10 - ANR hotfix: defer automatic cloud restore until UI is settled and disable V609 home redraw gate. */
public class MainActivityV610 extends MainActivityV609 {
    private final Handler main610 = new Handler(Looper.getMainLooper());
    private boolean autoQueued610=false;
    private final Runnable autoPull610 = () -> {
        autoQueued610=false;
        MainActivityV610.super.syncFromCloud(false);
    };

    @Override void syncFromCloud(boolean announce){
        if(announce){
            main610.removeCallbacks(autoPull610);
            autoQueued610=false;
            super.syncFromCloud(true);
            return;
        }
        if(autoQueued610) return;
        autoQueued610=true;
        main610.postDelayed(autoPull610, 8000L);
    }

    @Override void showHome(){
        // V609's 3.5 s early-return gate can suppress lifecycle redraws. Force normal rendering.
        try{
            Field f=MainActivityV609.class.getDeclaredField("lastHomeBuild609");
            f.setAccessible(true);f.setLong(this,0L);
        }catch(Exception ignored){}
        super.showHome();
    }

    @Override protected void onDestroy(){
        main610.removeCallbacks(autoPull610);
        super.onDestroy();
    }
}
