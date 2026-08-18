package com.parion.aidat;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

public class MainActivityV435 extends MainActivityV431 {
    private boolean fromAthletes435=false;

    @Override void base(String title, boolean back){
        super.base(title,back);
        addSafeHomeLogo435();
    }

    private void addSafeHomeLogo435(){
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);
            // Eski header logo/yer tutucularını temizle.
            for(int i=bar.getChildCount()-1;i>=0;i--){
                View v=bar.getChildAt(i);Object tag=v.getTag();
                if("PARION_HEADER_LOGO_431".equals(tag)||"PARION_HOME_LOGO_435".equals(tag))bar.removeViewAt(i);
            }
            ImageView logo=new ImageView(this);
            logo.setTag("PARION_HOME_LOGO_435");
            try{logo.setImageBitmap(ClubLogoAsset.bitmap());}catch(Throwable ignored){logo.setImageResource(R.drawable.ic_launcher);}
            logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            logo.setContentDescription("Ana Sayfa");
            logo.setClickable(true);logo.setFocusable(true);logo.setOnClickListener(v->showHome());
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(42),dp(42));lp.setMargins(dp(2),0,dp(6),0);
            bar.addView(logo,0,lp);
        }catch(Throwable ignored){}
    }

    @Override void showAthletes(){
        fromAthletes435=false;
        super.showAthletes();
    }

    @Override void showProfile(long id){
        boolean cameFromList="LIST".equals(page);
        if(cameFromList)fromAthletes435=true;
        super.showProfile(id);
        attachAthletePhoto435(id);
    }

    private void attachAthletePhoto435(long id){
        try{
            Cursor c=db.athlete(id);String photo="";if(c.moveToFirst())photo=s(c,"photo");c.close();
            ScrollView sv=findScroll435(root);if(sv==null)return;
            ImageView athlete=findFirstImage435(sv);if(athlete==null)return;
            final String ph=photo;athlete.setClickable(true);athlete.setOnClickListener(v->showFullPhoto435(ph));
        }catch(Throwable ignored){}
    }

    private void showFullPhoto435(String photo){
        try{
            Dialog d=new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            ImageView im=new ImageView(this);im.setBackgroundColor(Color.BLACK);im.setScaleType(ImageView.ScaleType.FIT_CENTER);
            setAthletePhoto(im,photo);im.setOnClickListener(v->d.dismiss());d.setContentView(im);d.show();
        }catch(Throwable ignored){}
    }

    @Override void goBack(){
        if("PROFILE".equals(page)&&fromAthletes435){fromAthletes435=false;super.showAthletes();return;}
        if("LIST".equals(page)){showHome();return;}
        super.goBack();
    }

    @Override void showHome(){fromAthletes435=false;super.showHome();}

    private ScrollView findScroll435(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll435(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private ImageView findFirstImage435(View v){if(v instanceof ImageView)return (ImageView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ImageView im=findFirstImage435(g.getChildAt(i));if(im!=null)return im;}}return null;}
}
