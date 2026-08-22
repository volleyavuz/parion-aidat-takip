package com.parion.aidat;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

/**
 * v4.0.16 - navigation + dashboard readability.
 *
 * Navigation:
 * - A profile remembers the exact screen/root it was opened from.
 * - Back from PROFILE restores that screen instead of always jumping to the generic athlete list.
 * - Existing page-specific goBack handlers remain intact for dashboard cards/forms/material screens.
 *
 * UI:
 * - Long dashboard card labels are shortened without changing their click listeners.
 * - Long header titles become two-line/readable instead of being clipped.
 */
public class MainActivityV616 extends MainActivityV615 {
    private View profileSourceRoot616=null;
    private String profileSourcePage616=null;
    private long profileSourceAthlete616=-1;
    private boolean restoringProfileSource616=false;

    @Override void showProfile(long id){
        // Capture only when ENTERING a profile. Profile refreshes after payment/media edits
        // must not overwrite the original source screen.
        if(!restoringProfileSource616 && !"PROFILE".equals(page) && root!=null){
            profileSourceRoot616=root;
            profileSourcePage616=page;
            profileSourceAthlete616=currentAthlete;
        }
        super.showProfile(id);
        improveLongHeader616();
    }

    @Override void showHome(){
        profileSourceRoot616=null;
        profileSourcePage616=null;
        profileSourceAthlete616=-1;
        super.showHome();
        patchDashboard616(root);
        improveLongHeader616();
    }

    @Override void base(String title,boolean back){
        super.base(title,back);
        improveLongHeader616();
    }

    @Override void goBack(){
        if("PROFILE".equals(page) && profileSourceRoot616!=null && profileSourcePage616!=null){
            restoringProfileSource616=true;
            try{
                View old=profileSourceRoot616;
                String oldPage=profileSourcePage616;
                long oldAthlete=profileSourceAthlete616;
                profileSourceRoot616=null;
                profileSourcePage616=null;
                profileSourceAthlete616=-1;
                setContentView(old);
                root=(LinearLayout)old;
                page=oldPage;
                currentAthlete=oldAthlete;
            }finally{
                restoringProfileSource616=false;
            }
            return;
        }
        // Keep the complete inherited navigation chain for every other page.
        super.goBack();
    }

    private void patchDashboard616(View v){
        if(v==null)return;
        if(v instanceof TextView){
            TextView t=(TextView)v;
            String raw=String.valueOf(t.getText()).trim();
            String u=raw.toUpperCase(new Locale("tr","TR"));
            String neo=null;
            if(u.equals("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")) neo="FOTOĞRAF\nEKSİK";
            else if(u.equals("KAYIT FORMU OLMAYAN AKTİF SPORCULAR")) neo="KAYIT FORMU\nEKSİK";
            else if(u.equals("ÖDENMEMİŞ MALZEME")) neo="MALZEME\nBORCU";
            else if(u.equals("AYLIK HEDEF CİRO")) neo="AYLIK\nHEDEF";
            else if(u.equals("YAZIN ARANACAK")) neo="YAZIN\nARANACAK";
            else if(u.equals("KIŞIN ARANACAK")) neo="KIŞIN\nARANACAK";
            if(neo!=null){
                t.setText(neo);
                t.setSingleLine(false);
                t.setMaxLines(2);
                t.setGravity(Gravity.CENTER);
                t.setTextSize(11f);
                t.setPadding(dp(5),dp(4),dp(5),dp(4));
                t.setLineSpacing(0f,0.92f);
            }
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)patchDashboard616(g.getChildAt(i));
        }
    }

    private void improveLongHeader616(){
        if(root==null||root.getChildCount()==0)return;
        View first=root.getChildAt(0);
        if(!(first instanceof ViewGroup))return;
        ViewGroup bar=(ViewGroup)first;
        for(int i=0;i<bar.getChildCount();i++){
            View x=bar.getChildAt(i);
            if(!(x instanceof TextView))continue;
            TextView t=(TextView)x;
            String s=String.valueOf(t.getText()).trim();
            if("‹".equals(s)||s.isEmpty())continue;
            t.setSingleLine(false);
            t.setMaxLines(2);
            t.setEllipsize(null);
            t.setGravity(Gravity.CENTER_VERTICAL);
            if(s.length()>26)t.setTextSize(15f);
            else if(s.length()>18)t.setTextSize(17f);
        }
    }
}
