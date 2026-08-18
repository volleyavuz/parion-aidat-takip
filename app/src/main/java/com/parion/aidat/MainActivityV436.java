package com.parion.aidat;

import android.view.*;
import android.widget.*;

public class MainActivityV436 extends MainActivityV435 {
    @Override void base(String title, boolean back){
        super.base(title,back);
        applySelectedLogo436();
    }

    private void applySelectedLogo436(){
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);
            for(int i=bar.getChildCount()-1;i>=0;i--){
                View v=bar.getChildAt(i);Object tag=v.getTag();
                if("PARION_HEADER_LOGO_431".equals(tag)||"PARION_HOME_LOGO_435".equals(tag)||"PARION_SELECTED_LOGO_436".equals(tag))bar.removeViewAt(i);
            }
            ImageView logo=new ImageView(this);
            logo.setTag("PARION_SELECTED_LOGO_436");
            logo.setImageResource(R.drawable.parion_app_icon);
            logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            logo.setContentDescription("Ana Sayfa");
            logo.setClickable(true);logo.setFocusable(true);
            logo.setOnClickListener(v->showHome());
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(44),dp(44));
            lp.setMargins(dp(2),0,dp(7),0);
            bar.addView(logo,0,lp);
        }catch(Throwable ignored){}
    }
}
