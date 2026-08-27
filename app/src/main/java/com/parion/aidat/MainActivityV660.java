package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * v4.1.20 - HOME transition shield that survives setContentView().
 *
 * The legacy HOME chain still rebuilds old dashboard layers before V657 replaces them.
 * A normal decor child is removed by setContentView(), so v4.1.19 could still expose
 * the old yellow dashboard. This version places the PARION shield in DecorView's overlay,
 * which survives content replacement, and removes it only after the final HOME call has
 * returned and the next frame is ready. No DB, sync, navigation or card logic changes.
 */
public class MainActivityV660 extends MainActivityV659 {
    private View dashboardCover660;
    private ViewGroup dashboardDecor660;

    @Override void showHome(){
        showDashboardCover660();
        try {
            super.showHome();
        } finally {
            removeDashboardCoverNextFrame660();
        }
    }

    private void showDashboardCover660(){
        ViewGroup decor=(ViewGroup)getWindow().getDecorView();
        removeDashboardCoverNow660();

        FrameLayout cover=new FrameLayout(this);
        cover.setBackgroundColor(Color.WHITE);
        cover.setClickable(true);

        LinearLayout center=new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER);
        center.setPadding(dp(24),dp(24),dp(24),dp(24));

        TextView title=new TextView(this);
        title.setText("PARİON");
        title.setTextColor(Color.rgb(28,28,28));
        title.setTextSize(22f);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        center.addView(title,new LinearLayout.LayoutParams(-1,-2));

        TextView sub=new TextView(this);
        sub.setText("SPORCU TAKİP UYGULAMASI");
        sub.setTextColor(Color.rgb(95,95,95));
        sub.setTextSize(11.5f);
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);
        sp.setMargins(0,dp(5),0,dp(14));
        center.addView(sub,sp);

        ProgressBar p=new ProgressBar(this);
        center.addView(p,new LinearLayout.LayoutParams(dp(28),dp(28)));
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);
        cover.addView(center,cp);

        int w=decor.getWidth()>0?decor.getWidth():getResources().getDisplayMetrics().widthPixels;
        int h=decor.getHeight()>0?decor.getHeight():getResources().getDisplayMetrics().heightPixels;
        int ws=View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY);
        int hs=View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY);
        cover.measure(ws,hs);
        cover.layout(0,0,w,h);

        ViewGroupOverlay overlay=decor.getOverlay();
        overlay.add(cover);
        dashboardCover660=cover;
        dashboardDecor660=decor;
    }

    private void removeDashboardCoverNextFrame660(){
        final View cover=dashboardCover660;
        final ViewGroup decor=dashboardDecor660;
        if(cover==null||decor==null)return;
        Choreographer.getInstance().postFrameCallback(frameTimeNanos -> {
            if(dashboardCover660!=cover)return;
            try{decor.getOverlay().remove(cover);}catch(Exception ignored){}
            if(dashboardCover660==cover){dashboardCover660=null;dashboardDecor660=null;}
        });
    }

    private void removeDashboardCoverNow660(){
        if(dashboardCover660!=null&&dashboardDecor660!=null){
            try{dashboardDecor660.getOverlay().remove(dashboardCover660);}catch(Exception ignored){}
        }
        dashboardCover660=null;
        dashboardDecor660=null;
    }

    @Override protected void onDestroy(){
        removeDashboardCoverNow660();
        super.onDestroy();
    }
}
