package com.parion.aidat;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;

/** v4.1.31 - persistent bottom nav on dashboard + normalized icon sizing/colors. */
public class MainActivityV709 extends MainActivityV708 {
    private static final int NAV_BG_709=Color.rgb(250,248,241);
    private static final int NAV_FG_709=Color.rgb(42,42,42);
    private static final int NAV_DIV_709=Color.rgb(224,214,184);

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(this::installBottomNav709);
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(()->{
                installBottomNav709();
                root.post(this::installBottomNav709);
            });
        }
    }

    private void installBottomNav709(){
        if(root==null||"LOGIN".equals(page))return;
        removeLegacyBottomNav709(root);

        LinearLayout nav=new LinearLayout(this);
        nav.setTag("v709-bottom-nav");
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8),dp(3),dp(8),dp(3));
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(NAV_BG_709);bg.setStroke(dp(1),NAV_DIV_709);
        nav.setBackground(bg);nav.setElevation(dp(6));

        ImageButton home=imageIcon709(R.drawable.ic_nav_home,"Anasayfa");
        ImageButton attendance=imageIcon709(android.R.drawable.ic_menu_agenda,"Yoklamalar");
        ImageButton athletes=imageIcon709(android.R.drawable.ic_menu_myplaces,"Sporcular");
        ImageButton settings=imageIcon709(android.R.drawable.ic_menu_preferences,"Ayarlar");
        home.setImageAlpha(148);

        home.setOnClickListener(v->showHome());
        attendance.setOnClickListener(v->openAttendance709());
        athletes.setOnClickListener(v->showAthletes());
        settings.setOnClickListener(this::openSettings709);

        LinearLayout.LayoutParams item=new LinearLayout.LayoutParams(0,dp(48),1);
        nav.addView(home,new LinearLayout.LayoutParams(item));
        nav.addView(attendance,new LinearLayout.LayoutParams(item));
        nav.addView(athletes,new LinearLayout.LayoutParams(item));
        nav.addView(settings,new LinearLayout.LayoutParams(item));

        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(54)));
        nav.bringToFront();
    }

    private ImageButton imageIcon709(int res,String desc){
        ImageButton b=new ImageButton(this);
        b.setImageResource(res);b.setContentDescription(desc);
        b.setBackgroundColor(Color.TRANSPARENT);b.setColorFilter(NAV_FG_709);
        b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        b.setPadding(dp(12),dp(12),dp(12),dp(12));
        b.setClickable(true);b.setFocusable(true);
        return b;
    }

    private void removeLegacyBottomNav709(View v){
        if(!(v instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){
            View c=g.getChildAt(i);Object tag=c.getTag();
            if("v625-bottom-nav".equals(tag)||"v626-bottom-nav".equals(tag)||"v708-bottom-nav".equals(tag)||"v709-bottom-nav".equals(tag)){g.removeViewAt(i);continue;}
            removeLegacyBottomNav709(c);
        }
    }

    private void openAttendance709(){
        try{Method m=MainActivityV628.class.getDeclaredMethod("showAttendanceGroups628");m.setAccessible(true);m.invoke(this);}
        catch(Exception e){toast("Yoklamalar ekranı açılamadı.");}
    }

    private void openSettings709(View anchor){
        try{Method m=MainActivityV626.class.getDeclaredMethod("showSettingsPopup626",View.class);m.setAccessible(true);m.invoke(this,anchor);}
        catch(Exception e){toast("Ayarlar açılamadı.");}
    }
}
