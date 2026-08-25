package com.parion.aidat;

import android.view.*;
import android.widget.*;

/** v4.0.80 - deterministic HOME header logo sizing/position, independent of title text. */
public class MainActivityV680 extends MainActivityV679 {

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(this::forceHomeHeaderLogo680);
            root.postDelayed(this::forceHomeHeaderLogo680,80);
        }
    }

    private void forceHomeHeaderLogo680(){
        if(root==null || root.getChildCount()==0) return;
        View top=root.getChildAt(0);
        if(!(top instanceof ViewGroup)) return;
        ViewGroup bar=(ViewGroup)top;

        for(int i=bar.getChildCount()-1;i>=0;i--){
            if(bar.getChildAt(i) instanceof ImageView) bar.removeViewAt(i);
        }

        ImageView logo=new ImageView(this);
        logo.setTag("official-home-header-logo-680");
        logo.setImageResource(R.drawable.parion_official_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setPadding(dp(4),dp(4),dp(4),dp(4));

        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(38),dp(38));
        lp.setMargins(dp(6),dp(5),dp(8),dp(5));
        bar.addView(logo,0,lp);
    }
}
