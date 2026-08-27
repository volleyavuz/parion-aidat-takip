package com.parion.aidat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.lang.reflect.Method;
import java.util.*;

/** v4.1.34 - restore V631/V635/V637 attendance flow; only patch group button colors. */
public class MainActivityV712 extends MainActivityV711 {

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(()->{
            hookModernAttendance712(root);
            if("ATTENDANCE_GROUPS_631".equals(page))styleAttendanceGroups712(root);
        });
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null){
            root.post(()->{hookModernAttendance712(root);root.post(()->hookModernAttendance712(root));});
        }
    }

    private void hookModernAttendance712(View v){
        if(v==null)return;
        CharSequence d=v.getContentDescription();
        if(d!=null&&"Yoklamalar".equalsIgnoreCase(d.toString())){
            v.setOnClickListener(x->openModernAttendance712());
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)hookModernAttendance712(g.getChildAt(i));}
    }

    private void openModernAttendance712(){
        try{
            Method m=MainActivityV631.class.getDeclaredMethod("showAttendanceGroups631");
            m.setAccessible(true);m.invoke(this);
        }catch(Exception e){toast("Yoklamalar ekranı açılamadı.");}
    }

    private void styleAttendanceGroups712(View v){
        if(v instanceof Button){
            Button b=(Button)v;String raw=String.valueOf(b.getText()).trim();String n=norm712(raw);
            if(!n.isEmpty()&&!n.contains("DIŞA AKTAR")){
                Integer fill=groupColor712(n);
                if(fill!=null){
                    GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(12));g.setStroke(dp(1),darken712(fill));
                    b.setBackground(g);b.setTextColor(Color.rgb(35,35,35));b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);
                }
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)styleAttendanceGroups712(g.getChildAt(i));}
    }

    /** Fixed category mapping; unlike v4.1.33 this never depends on DB sort order. */
    private Integer groupColor712(String n){
        if(n.equals("SO 1")||n.equals("SPOR OKULU 1")) return Color.rgb(217,234,247);
        if(n.equals("SO 2")||n.equals("SPOR OKULU 2")) return Color.rgb(224,239,214);
        if(n.equals("SO 3")||n.equals("SPOR OKULU 3")) return Color.rgb(255,239,204);
        if(n.contains("MİNİ")||n.contains("MINI")) return Color.rgb(249,222,230);
        if(n.contains("MİDİ")||n.contains("MIDI")) return Color.rgb(231,221,245);
        if(n.contains("KÜÇÜK")||n.contains("KUCUK")) return Color.rgb(218,237,233);
        if(n.contains("YILDIZ")) return Color.rgb(244,226,207);
        if(n.contains("GENÇ")||n.contains("GENC")) return Color.rgb(225,229,242);
        return Color.rgb(238,235,225);
    }

    private int darken712(int c){return Color.rgb(Math.max(0,Color.red(c)-28),Math.max(0,Color.green(c)-28),Math.max(0,Color.blue(c)-28));}
    private String norm712(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}
}
