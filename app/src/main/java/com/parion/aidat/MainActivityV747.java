package com.parion.aidat;

import android.os.Bundle;

/** v4.2.28 - single-owner startup lifecycle.
 *
 * The legacy inheritance chain calls showHome() from several parent onCreate() methods.
 * Because Java dispatches those calls to the newest override, the complete dashboard used
 * to be rebuilt repeatedly before Activity startup had even finished. This class blocks
 * every inherited startup-time showHome() call, then performs exactly one dashboard draw
 * after the full superclass onCreate chain returns.
 */
public class MainActivityV747 extends MainActivityV746 {
    private volatile boolean startupReady747=false;
    private long lastHome747=0L;

    @Override public void onCreate(Bundle b){
        startupReady747=false;
        super.onCreate(b);
        startupReady747=true;
        showHome();
    }

    @Override void showHome(){
        if(!startupReady747)return;
        long now=android.os.SystemClock.uptimeMillis();
        // Coalesce background media/index callbacks that arrive almost together.
        if(lastHome747>0L && now-lastHome747<900L && "HOME".equals(page))return;
        lastHome747=now;
        super.showHome();
    }
}
