package com.parion.aidat;

import android.view.*;
import android.widget.*;

public class MainActivityV432 extends MainActivityV431 {

    @Override void base(String title, boolean back){
        super.base(title,back);
        patchHeader432();
    }

    private void patchHeader432(){
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);

            // V431'in sağa eklediği logoyu kaldır.
            for(int i=bar.getChildCount()-1;i>=0;i--){
                View v=bar.getChildAt(i);
                if("PARION_HEADER_LOGO_431".equals(v.getTag())||"PARION_HOME_LOGO_432".equals(v.getTag()))bar.removeViewAt(i);
            }

            // Gerçek gömülü Parion simgesini sol üste yerleştir ve ana sayfa düğmesi yap.
            ImageView logo=new ImageView(this);
            logo.setTag("PARION_HOME_LOGO_432");
            logo.setImageResource(R.drawable.ic_launcher);
            logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            logo.setAdjustViewBounds(false);
            logo.setContentDescription("Ana Sayfa");
            logo.setClickable(true);
            logo.setFocusable(true);
            logo.setOnClickListener(v->showHome());
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(40),dp(40));
            lp.setMargins(dp(2),0,dp(5),0);
            bar.addView(logo,0,lp);
        }catch(Exception ignored){}
    }
}
