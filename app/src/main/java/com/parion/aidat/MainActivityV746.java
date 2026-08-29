package com.parion.aidat;

/** v4.2.25 - never run full canonical snapshot during Activity startup/resume.
 * Manual sync remains available; background/realtime invalidations do not block first paint.
 */
public class MainActivityV746 extends MainActivityV745 {
    @Override void syncFromCloud(boolean announce){
        // V741.onResume(), realtime signal and the 450ms pending pump all call with announce=false.
        // A full snapshot here can hold the inherited startup/loading layer for a long time.
        if(!announce) return;
        super.syncFromCloud(true);
    }
}
