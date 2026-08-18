package com.parion.aidat;

import android.os.Bundle;
import android.os.Handler;
import java.lang.reflect.Field;

/**
 * Final ANR guard: V421 and V436 both owned self-repeating main-thread Handlers.
 * V440 replaced the V421 loop with one background scheduler, but V436 still kept
 * its own safeSync436 Handler alive. Disable both inherited UI-thread loops.
 */
public class MainActivityV441 extends MainActivityV440 {

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        disableAllInheritedUiSync441();
    }

    @Override void showHome(){
        super.showHome();
        disableAllInheritedUiSync441();
    }

    @Override void showProfile(long id){
        super.showProfile(id);
        disableAllInheritedUiSync441();
    }

    @Override void form(long id){
        super.form(id);
        disableAllInheritedUiSync441();
    }

    private void disableAllInheritedUiSync441(){
        disableHandler441(MainActivityV421.class,"syncHandler421","alive421");
        disableHandler441(MainActivityV436.class,"safeSync436","alive436");
    }

    private void disableHandler441(Class<?> owner,String handlerField,String aliveField){
        try{
            Field alive=owner.getDeclaredField(aliveField);
            alive.setAccessible(true);
            alive.setBoolean(this,false);
        }catch(Throwable ignored){}
        try{
            Field hf=owner.getDeclaredField(handlerField);
            hf.setAccessible(true);
            Object h=hf.get(this);
            if(h instanceof Handler)((Handler)h).removeCallbacksAndMessages(null);
        }catch(Throwable ignored){}
    }

    @Override protected void onDestroy(){
        disableAllInheritedUiSync441();
        super.onDestroy();
    }
}
