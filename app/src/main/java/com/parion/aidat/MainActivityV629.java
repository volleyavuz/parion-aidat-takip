package com.parion.aidat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.29 - fixes attendance weekday multi-selection dialog. */
public class MainActivityV629 extends MainActivityV628 {
    private final SimpleDateFormat ISO629=new SimpleDateFormat("yyyy-MM-dd",Locale.US);

    @Override void base(String title,boolean back){
        super.base(title,back);
        if(root!=null)root.post(this::patchAttendanceScreens629);
    }

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(()->patchAttendanceRail629(root));
    }

    private void patchAttendanceScreens629(){
        if(page==null)return;
        if("ATTENDANCE_GROUPS_628".equals(page)) patchGroupButtons629(root);
        else if(page.startsWith("ATTENDANCE_GROUP_628:")){
            String group=page.substring("ATTENDANCE_GROUP_628:".length());
            patchProgramButton629(root,group);
        }
    }

    private void patchAttendanceRail629(View v){
        if(v instanceof ImageButton){
            CharSequence cd=v.getContentDescription();
            if(cd!=null&&"Yoklamalar".equalsIgnoreCase(cd.toString())){
                v.setOnClickListener(x->invokePrivate629("showAttendanceGroups628",new Class<?>[0]));
                return;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchAttendanceRail629(g.getChildAt(i));}
    }

    private void patchGroupButtons629(View v){
        if(v instanceof Button){
            Button b=(Button)v;String text=String.valueOf(b.getText()).trim();
            if(!text.isEmpty()&&!text.toUpperCase(new Locale("tr","TR")).contains("DIŞA AKTAR")){
                b.setOnClickListener(x->openGroupFixed629(text));
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchGroupButtons629(g.getChildAt(i));}
    }

    private void patchProgramButton629(View v,String group){
        if(v instanceof Button){Button b=(Button)v;if("PROGRAM".equalsIgnoreCase(String.valueOf(b.getText()).trim()))b.setOnClickListener(x->showWeekdayDialog629(group,false));}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchProgramButton629(g.getChildAt(i),group);}
    }

    private void openGroupFixed629(String group){
        if(!hasSchedule629(group)){showWeekdayDialog629(group,true);return;}
        invokePrivate629("openGroupAttendance628",new Class<?>[]{String.class},group);
    }

    private boolean hasSchedule629(String group){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT 1 FROM attendance_schedule WHERE groupName=? LIMIT 1",new String[]{group});
        boolean ok=c.moveToFirst();c.close();return ok;
    }

    private boolean[] currentWeekdays629(String group){
        boolean[] out=new boolean[7];
        Cursor c=db.getReadableDatabase().rawQuery("SELECT weekdays FROM attendance_schedule WHERE groupName=? ORDER BY effectiveFrom DESC,id DESC LIMIT 1",new String[]{group});
        if(c.moveToFirst()){
            String s=c.getString(0);
            if(s!=null)for(String z:s.split(",")){try{int n=Integer.parseInt(z.trim());if(n>=1&&n<=7)out[n-1]=true;}catch(Exception ignored){}}
        }
        c.close();return out;
    }

    private void showWeekdayDialog629(String group,boolean first){
        String[] days={"Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi","Pazar"};
        boolean[] checked=currentWeekdays629(group);
        AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle(group+" • ANTRENMAN GÜNLERİ")
                .setMultiChoiceItems(days,checked,(d,which,isChecked)->checked[which]=isChecked)
                .setPositiveButton("KAYDET",null)
                .setNegativeButton("VAZGEÇ",null)
                .create();
        dialog.setOnShowListener(x->{
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                if(saveSchedule629(group,checked))dialog.dismiss();
            });
        });
        dialog.show();
        toast(first?"Haftalık antrenman günlerini işaretleyip KAYDET'e basın.":"Yeni günler bugünden itibaren geçerli olur; eski yoklamalar değişmez.");
    }

    private boolean saveSchedule629(String group,boolean[] selected){
        StringBuilder s=new StringBuilder();
        for(int i=0;i<7;i++)if(selected[i]){if(s.length()>0)s.append(',');s.append(i+1);}
        if(s.length()==0){toast("En az bir antrenman günü seçin.");return false;}
        SQLiteDatabase d=db.getWritableDatabase();ContentValues v=new ContentValues();
        v.put("groupName",group);v.put("effectiveFrom",ISO629.format(new Date()));v.put("weekdays",s.toString());
        d.insert("attendance_schedule",null,v);
        invokePrivate629("ensureMonthSessions628",new Class<?>[]{String.class},group);
        invokePrivate629("openGroupAttendance628",new Class<?>[]{String.class},group);
        return true;
    }

    private Object invokePrivate629(String name,Class<?>[] types,Object... args){
        try{Method m=MainActivityV628.class.getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(this,args);}catch(Exception e){toast("Yoklama ekranı açılamadı.");return null;}
    }
}
