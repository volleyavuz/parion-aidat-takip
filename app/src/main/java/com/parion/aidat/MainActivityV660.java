package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * v4.1.17 - first-frame HOME reveal.
 * Keep the proven v4.0.60 visual shield only until the next render frame instead of
 * holding it for a fixed 1500 ms. This prevents stale/old HOME content from flashing
 * while removing the artificial startup delay. No DB, sync, navigation or card logic.
 */
public class MainActivityV660 extends MainActivityV659 {
    private View dashboardCover660;

    @Override void showHome(){
        super.showHome();
        showDashboardCover660();
    }

    private void showDashboardCover660(){
        if(root==null)return;
        ViewGroup decor=(ViewGroup)getWindow().getDecorView();
        if(dashboardCover660!=null){
            try{decor.removeView(dashboardCover660);}catch(Exception ignored){}
        }
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
        decor.addView(cover,new ViewGroup.LayoutParams(-1,-1));
        dashboardCover660=cover;

        // Remove at the next frame boundary: no fixed delay and no fade tail.
        Choreographer.getInstance().postFrameCallback(frameTimeNanos -> {
            if(dashboardCover660!=cover)return;
            try{decor.removeView(cover);}catch(Exception ignored){}
            if(dashboardCover660==cover)dashboardCover660=null;
        });
    }

    @Override protected void onDestroy(){
        dashboardCover660=null;
        super.onDestroy();
    }
}
