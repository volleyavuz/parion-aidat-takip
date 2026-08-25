package com.parion.aidat;

import android.view.*;
import android.widget.*;

/** v4.0.78 - complete official logo integration on HOME; keep original club artwork. */
public class MainActivityV678 extends MainActivityV677 {

    @Override void showHome(){
        super.showHome();
        if(root!=null) root.post(this::installHomeHeaderLogo678);
    }

    private void installHomeHeaderLogo678(){
        if(root==null || root.getChildCount()==0) return;
        View first=root.getChildAt(0);
        if(!(first instanceof ViewGroup)) return;
        ViewGroup bar=(ViewGroup)first;

        // HOME used a legacy header path in older layers. Only normalize direct header images;
        // dashboard/content imagery is left untouched.
        for(int i=bar.getChildCount()-1;i>=0;i--){
            View v=bar.getChildAt(i);
            if(v instanceof ImageView) bar.removeViewAt(i);
        }

        ImageView logo=new ImageView(this);
        logo.setTag("official-home-header-logo-678");
        logo.setImageResource(R.drawable.parion_official_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setAdjustViewBounds(true);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(48),dp(48));
        lp.setMargins(dp(6),dp(4),dp(6),dp(4));
        bar.addView(logo,0,lp);
    }
}
