package com.parion.aidat;

import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;

/** v4.1.30 - restore persistent 4-icon bottom navigation; remove legacy left home rail. */
public class MainActivityV708 extends MainActivityV707 {

    @Override void base(String title, boolean back){
        super.base(title,back);
        if(root!=null)root.post(this::customizeBottomNav708);
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(()->{
            removeLeftRail708();
            customizeBottomNav708();
        });
    }

    private void customizeBottomNav708(){
        if(root==null||"LOGIN".equals(page))return;
        LinearLayout nav=findTaggedLinear708(root,"v626-bottom-nav");
        if(nav==null)nav=findTaggedLinear708(root,"v625-bottom-nav");
        if(nav==null)return;

        // Preserve the original Home and Settings views/listeners from V626 exactly.
        View home=findByDescription708(nav,"Anasayfa");
        View settings=findByDescription708(nav,"Ayarlar");
        if(home==null||settings==null)return;

        nav.removeAllViews();
        nav.setTag("v708-bottom-nav");
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8),dp(3),dp(8),dp(3));
        nav.setBackgroundColor(Color.rgb(250,248,241));

        ImageButton attendance=navImage708(android.R.drawable.ic_menu_agenda,"Yoklamalar");
        attendance.setOnClickListener(v->openAttendance708());
        ImageButton athletes=navImage708(android.R.drawable.ic_menu_myplaces,"Sporcular");
        athletes.setOnClickListener(v->showAthletes());

        // Requested order: Home | Attendance | Athletes | Settings.
        nav.addView(home,new LinearLayout.LayoutParams(0,dp(44),1));
        nav.addView(attendance,new LinearLayout.LayoutParams(0,dp(44),1));
        nav.addView(athletes,new LinearLayout.LayoutParams(0,dp(44),1));
        nav.addView(settings,new LinearLayout.LayoutParams(0,dp(44),1));
    }

    private ImageButton navImage708(int res,String desc){
        ImageButton b=new ImageButton(this);
        b.setImageResource(res);
        b.setContentDescription(desc);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setPadding(dp(10),dp(10),dp(10),dp(10));
        b.setColorFilter(Color.rgb(35,35,35));
        b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        b.setFocusable(true);b.setClickable(true);
        return b;
    }

    private void openAttendance708(){
        try{
            Method m=MainActivityV628.class.getDeclaredMethod("showAttendanceGroups628");
            m.setAccessible(true);m.invoke(this);
        }catch(Exception e){toast("Yoklamalar ekranı açılamadı.");}
    }

    /** Undo V627's horizontal shell and restore the dashboard ScrollView to full width. */
    private void removeLeftRail708(){
        if(root==null||!"HOME".equals(page))return;
        View shell=findTaggedView708(root,"v627-home-shell");
        if(!(shell instanceof LinearLayout))return;
        LinearLayout sh=(LinearLayout)shell;
        ScrollView sv=findScroll708(sh);if(sv==null)return;
        ViewParent p=sh.getParent();if(!(p instanceof ViewGroup))return;
        ViewGroup parent=(ViewGroup)p;int idx=parent.indexOfChild(sh);if(idx<0)return;
        ViewGroup.LayoutParams lp=sh.getLayoutParams();
        sh.removeView(sv);parent.removeView(sh);parent.addView(sv,Math.min(idx,parent.getChildCount()),lp);
    }

    private LinearLayout findTaggedLinear708(View v,String tag){View x=findTaggedView708(v,tag);return x instanceof LinearLayout?(LinearLayout)x:null;}
    private View findTaggedView708(View v,String tag){if(tag.equals(v.getTag()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findTaggedView708(g.getChildAt(i),tag);if(x!=null)return x;}}return null;}
    private View findByDescription708(View v,String desc){CharSequence c=v.getContentDescription();if(c!=null&&desc.equalsIgnoreCase(c.toString()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findByDescription708(g.getChildAt(i),desc);if(x!=null)return x;}}return null;}
    private ScrollView findScroll708(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll708(g.getChildAt(i));if(s!=null)return s;}}return null;}
}
