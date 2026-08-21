package com.parion.aidat;

/**
 * v4.0.13 - ANR hard stop.
 * Full CLOUD -> LOCAL restore is MANUAL ONLY.
 * All automatic lifecycle/background syncFromCloud(false) calls are ignored.
 * Manual syncFromCloud(true) remains fully functional.
 */
public class MainActivityV613 extends MainActivityV612 {
    @Override void syncFromCloud(boolean announce){
        if(!announce) return;
        super.syncFromCloud(true);
    }
}
