package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;

/**
 * Performance guard: cloud athlete thumbnails are only requested when their ImageView
 * is actually visible on screen. This prevents a ScrollView containing hundreds of
 * athletes from starting hundreds of Storage downloads/bitmap decodes at once.
 */
public class MainActivityV438 extends MainActivityV436 {
    private boolean profileRender438=false;

    @Override void showProfile(long id){
        profileRender438=true;
        try{ super.showProfile(id); }
        finally{ profileRender438=false; }
    }

    @Override void setAthletePhoto(ImageView v,String photo){
        if(profileRender438){
            super.setAthletePhoto(v,photo);
            return;
        }
        String p=photo==null?"":photo.trim();
        // Local one-off images are cheap and require no network.
        if(p.startsWith("USER:")){
            super.setAthletePhoto(v,p);
            return;
        }
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        v.setImageDrawable(new ColorDrawable(Color.rgb(225,225,225)));
        final String ref=p;
        // Wait until the view is attached, then only load if it is on-screen.
        v.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){
            boolean done=false;
            @Override public void onViewAttachedToWindow(View view){
                if(done)return;
                v.postDelayed(()->{
                    if(done||isFinishing()||isDestroyed())return;
                    Rect r=new Rect();
                    if(v.isShown() && v.getGlobalVisibleRect(r) && r.width()>0 && r.height()>0){
                        done=true;
                        v.removeOnAttachStateChangeListener(this);
                        MainActivityV438.super.setAthletePhoto(v,ref);
                    }
                },250L);
            }
            @Override public void onViewDetachedFromWindow(View view){}
        });
    }
}
