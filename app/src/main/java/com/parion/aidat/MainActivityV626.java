package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;

/** v4.0.26 - compact icon-only bottom navigation and upward settings popup. */
public class MainActivityV626 extends MainActivityV625 {
    private static final int TEXT_626=Color.rgb(28,28,28);
    private PopupWindow settingsPopup626;

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(this::installBottomNav626);
    }

    private void installBottomNav626(){
        if(root==null||"LOGIN".equals(page))return;
        dismissSettingsPopup626();
        for(int i=root.getChildCount()-1;i>=0;i--){
            View v=root.getChildAt(i);
            if("v625-bottom-nav".equals(v.getTag())||"v626-bottom-nav".equals(v.getTag()))root.removeViewAt(i);
        }

        LinearLayout nav=new LinearLayout(this);
        nav.setTag("v626-bottom-nav");
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(12),dp(3),dp(12),dp(3));
        nav.setBackgroundColor(Color.rgb(250,248,241));

        TextView home=navIcon626("⌂");
        TextView settings=navIcon626("⚙");
        home.setContentDescription("Anasayfa");
        settings.setContentDescription("Ayarlar");
        home.setOnClickListener(v->showHome());
        settings.setOnClickListener(this::showSettingsPopup626);

        // User requested swapped positions: home on the left, settings on the right.
        nav.addView(home,new LinearLayout.LayoutParams(dp(44),dp(44)));
        View spacer=new View(this);
        nav.addView(spacer,new LinearLayout.LayoutParams(0,dp(1),1));
        nav.addView(settings,new LinearLayout.LayoutParams(dp(44),dp(44)));
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(50)));
    }

    private TextView navIcon626(String icon){
        TextView v=new TextView(this);
        v.setText(icon);
        v.setTextSize(25f);
        v.setTextColor(TEXT_626);
        v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        v.setGravity(Gravity.CENTER);
        v.setBackgroundColor(Color.TRANSPARENT);
        v.setPadding(0,0,0,0);
        v.setClickable(true);
        v.setFocusable(true);
        return v;
    }

    private void showSettingsPopup626(View anchor){
        dismissSettingsPopup626();
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(8),dp(8),dp(8),dp(8));
        panel.setBackground(roundStroke625Compat626(Color.WHITE,Color.rgb(215,205,175),14,1));

        TextView groups=popupItem626("GRUPLARI DÜZENLE");
        groups.setOnClickListener(v->{dismissSettingsPopup626();openGroups626();});
        panel.addView(groups,new LinearLayout.LayoutParams(dp(210),dp(48)));

        settingsPopup626=new PopupWindow(panel,dp(226),WindowManager.LayoutParams.WRAP_CONTENT,true);
        settingsPopup626.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        settingsPopup626.setOutsideTouchable(true);
        settingsPopup626.setElevation(dp(8));

        int[] loc=new int[2];anchor.getLocationOnScreen(loc);
        panel.measure(View.MeasureSpec.makeMeasureSpec(dp(226),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        int popupH=Math.max(dp(64),panel.getMeasuredHeight());
        int x=Math.max(dp(8),getResources().getDisplayMetrics().widthPixels-dp(238));
        int y=Math.max(dp(8),loc[1]-popupH-dp(8));
        settingsPopup626.showAtLocation(root,Gravity.TOP|Gravity.START,x,y);
    }

    private TextView popupItem626(String text){
        TextView t=new TextView(this);
        t.setText(text);
        t.setTextSize(13f);
        t.setTextColor(TEXT_626);
        t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(14),0,dp(14),0);
        t.setBackgroundColor(Color.TRANSPARENT);
        t.setClickable(true);
        return t;
    }

    /** Reuse the existing, already-tested V625 group manager without duplicating its data logic. */
    private void openGroups626(){
        try{
            Method m=MainActivityV625.class.getDeclaredMethod("showGroups625");
            m.setAccessible(true);
            m.invoke(this);
        }catch(Exception e){toast("Gruplar ekranı açılamadı.");}
    }

    private void dismissSettingsPopup626(){
        if(settingsPopup626!=null){try{settingsPopup626.dismiss();}catch(Exception ignored){}settingsPopup626=null;}
    }

    private android.graphics.drawable.GradientDrawable roundStroke625Compat626(int fill,int stroke,int radius,int width){
        android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();
        d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(width),stroke);return d;
    }

    @Override protected void onDestroy(){dismissSettingsPopup626();super.onDestroy();}
}
