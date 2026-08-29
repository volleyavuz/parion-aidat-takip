package com.parion.aidat;

import android.os.Bundle;

/**
 * v4.2.29 media rollback.
 *
 * The v4.2.24 duplicate cloud media RPC client is disabled here. MainActivityV405 already
 * owns photo/form indexing and upload/display behavior. The duplicate engine also wrote
 * CLOUD: photo paths into athletes, which interacted with sync triggers and startup.
 *
 * Keep this compatibility class in the inheritance chain, but make it intentionally inert.
 * Media indexing/upload/display remains owned by the proven V405 implementation.
 */
public class MainActivityV745 extends MainActivityV744 {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
    }

    @Override protected void onResume(){
        super.onResume();
    }

    @Override void syncFromCloud(boolean announce){
        super.syncFromCloud(announce);
    }
}
