package com.parion.aidat;

import android.view.*;
import android.widget.*;

/** v4.0.77 - official club logo in the left side of every app header. */
public class MainActivityV677 extends MainActivityV676 {

    @Override void base(String title, boolean back){
        super.base(title, back);
        if(root!=null) root.post(this::installOfficialHeaderLogo677);
    }

    private void installOfficialHeaderLogo677(){
        if(root==null || root.getChildCount()==0) return;
        View first=root.getChildAt(0);
        if(!(first instanceof ViewGroup)) return;
        ViewGroup bar=(ViewGroup)first;

        // Remove only legacy direct header image marks. Content/profile images are untouched.
        for(int i=bar.getChildCount()-1;i>=0;i--){
            View v=bar.getChildAt(i);
            if(v instanceof ImageView) bar.removeViewAt(i);
        }

        ImageView logo=new ImageView(this);
        logo.setTag("official-header-logo-677");
        logo.setImageResource(R.drawable.parion_official_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setAdjustViewBounds(true);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(48),dp(48));
        lp.setMargins(dp(6),dp(4),dp(6),dp(4));
        bar.addView(logo,0,lp);
    }
}
