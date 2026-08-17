package com.parion.aidat;

import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivityV431 extends MainActivityV430 {

    @Override void base(String title, boolean back){
        super.base(title,back);
        patchHeader431();
    }

    private void patchHeader431(){
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout bar=(LinearLayout)root.getChildAt(0);
            // Soldaki boş logo yer tutucusunu kaldır.
            for(int i=0;i<bar.getChildCount();i++){
                View v=bar.getChildAt(i);
                if(v instanceof TextView && String.valueOf(((TextView)v).getText()).trim().isEmpty()
                        && v.getLayoutParams()!=null && v.getLayoutParams().width<=dp(42)){
                    bar.removeViewAt(i);break;
                }
            }
            // Aynı başlığa ikinci kez logo eklenmesin.
            for(int i=0;i<bar.getChildCount();i++)if("PARION_HEADER_LOGO_431".equals(bar.getChildAt(i).getTag()))return;
            ImageView logo=new ImageView(this);logo.setTag("PARION_HEADER_LOGO_431");logo.setImageResource(R.drawable.parion_logo);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);logo.setAdjustViewBounds(true);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(38),dp(38));lp.setMargins(dp(5),0,dp(2),0);bar.addView(logo,lp);
        }catch(Exception ignored){}
    }

    @Override void showHome(){
        super.showHome();
        moveAthletesFirst431();
        moveDeletedIntoScroll431();
        simplifyRecentTitle431(root);
    }

    private void moveAthletesFirst431(){
        ScrollView sv=findScroll431(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);View athletes=null;
        for(int i=0;i<box.getChildCount();i++){View v=box.getChildAt(i);if(v instanceof Button&&"SPORCULAR".equalsIgnoreCase(String.valueOf(((Button)v).getText()).trim())){athletes=v;break;}}
        if(athletes==null)return;box.removeView(athletes);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.setMargins(0,dp(3),0,dp(7));box.addView(athletes,0,lp);
    }

    private void moveDeletedIntoScroll431(){
        try{
            ScrollView sv=findScroll431(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;LinearLayout box=(LinearLayout)sv.getChildAt(0);
            View deleted=findButton431(root,"SİLİNEN SPORCULAR");if(deleted==null)return;ViewParent p=deleted.getParent();if(p==box)box.removeView(deleted);else if(p instanceof ViewGroup)((ViewGroup)p).removeView(deleted);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));lp.setMargins(dp(8),dp(10),dp(8),dp(12));box.addView(deleted,lp);
        }catch(Exception ignored){}
    }

    private void simplifyRecentTitle431(View v){
        if(v instanceof TextView){TextView t=(TextView)v;String s=String.valueOf(t.getText()).toUpperCase(new Locale("tr","TR"));if(s.contains("SON İŞLEMLER")&&s.contains("İSTANBUL"))t.setText("SON İŞLEMLER");}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)simplifyRecentTitle431(g.getChildAt(i));}
    }

    private View findButton431(View v,String term){
        if(v instanceof Button){String s=String.valueOf(((Button)v).getText()).toUpperCase(new Locale("tr","TR"));if(s.contains(term))return v;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findButton431(g.getChildAt(i),term);if(x!=null)return x;}}
        return null;
    }
    private ScrollView findScroll431(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll431(g.getChildAt(i));if(s!=null)return s;}}return null;}
}
