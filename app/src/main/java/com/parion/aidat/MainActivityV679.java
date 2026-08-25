package com.parion.aidat;

import android.view.*;
import android.widget.*;

/** v4.0.79 - keep the original official club logo inside safe bounds on HOME. */
public class MainActivityV679 extends MainActivityV678 {

    @Override void showHome(){
        super.showHome();
        if(root!=null) root.post(this::fixHomeHeaderLogo679);
    }

    private void fixHomeHeaderLogo679(){
        if(root==null) return;
        TextView title=findTitle679(root,"PARION SPOR OKULU");
        if(title==null || !(title.getParent() instanceof ViewGroup)) return;
        ViewGroup bar=(ViewGroup)title.getParent();

        // Remove only direct header logo images so dashboard/content images remain untouched.
        for(int i=bar.getChildCount()-1;i>=0;i--){
            View v=bar.getChildAt(i);
            if(v instanceof ImageView) bar.removeViewAt(i);
        }

        ImageView logo=new ImageView(this);
        logo.setTag("official-home-header-logo-679");
        logo.setImageResource(R.drawable.parion_official_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setPadding(dp(3),dp(3),dp(3),dp(3));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(42),dp(42));
        lp.setMargins(dp(4),dp(3),dp(7),dp(3));
        bar.addView(logo,0,lp);
    }

    private TextView findTitle679(View v,String wanted){
        if(v instanceof TextView && wanted.equalsIgnoreCase(String.valueOf(((TextView)v).getText()).trim())) return (TextView)v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                TextView r=findTitle679(g.getChildAt(i),wanted);
                if(r!=null) return r;
            }
        }
        return null;
    }
}
