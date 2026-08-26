package com.parion.aidat;

/**
 * v4.1.13 - disable obsolete v4.0.60 dashboard cover.
 *
 * The old layer deliberately covered every HOME with a full-screen white PARION
 * loading view for 1500 ms after the dashboard had already been built. Current HOME
 * construction is fast and ANR-safe, so this visual gate only creates artificial delay.
 * No database, sync, navigation or dashboard-card logic is changed here.
 */
public class MainActivityV660 extends MainActivityV659 {
    @Override void showHome(){
        super.showHome();
    }
}
