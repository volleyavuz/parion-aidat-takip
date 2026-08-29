package com.parion.aidat;

import android.os.Bundle;

/**
 * v4.2.29 media rollback.
 *
 * parion_media_index_v1 was introduced in v4.2.24 as a second media-index engine while
 * MainActivityV405 already owned photo/form indexing. The duplicate engine also wrote
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
