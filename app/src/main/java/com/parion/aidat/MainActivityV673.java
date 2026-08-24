package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** v4.0.73 - Devamsızlar: ignore X/pause and inactive date ranges; count only confirmed sessions. */
public class MainActivityV673 extends MainActivityV672 {
    private final SimpleDateFormat ISO673=new SimpleDateFormat("yyyy-MM-dd",Locale.US);

    @Override void showHome(){
        super.showHome();
        if(root!=null)root.post(this::replaceAbsentees673);
    }

    private void replaceAbsentees673(){
        if(root==null||!"HOME".equalsIgnoreCase(page))return;
        ScrollView sv=findScroll673(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)sv.getChildAt(0);
        for(int i=box.getChildCount()-1;i>=0;i--)if(contains673(box.getChildAt(i),"DEVAMSIZLAR"))box.removeViewAt(i);
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(12),dp(12),dp(12));card.setBackground(round(Color.WHITE,14));
        card.addView(tv("DEVAMSIZLAR",12,Color.rgb(35,35,35),true));
        ArrayList<Abs673> list=calculate673();
        for(Abs673 a:list){TextView r=tv(a.name+" • "+a.days+" gündür gelmiyor",12,a.days>=30?Color.rgb(170,30,30):Color.rgb(35,35,35),true);r.setOnClickListener(v->showProfile(a.id));card.addView(r);}
        if(list.isEmpty())card.addView(tv("Seçili gruplarda 15 gün ve üzeri devamsız sporcu yok.",11,Color.rgb(95,95,95),false));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(12),0,dp(8));box.addView(card,lp);
    }

    private ArrayList<Abs673> calculate673(){
        ArrayList<Abs673> out=new ArrayList<>();Date today=day673(new Date());String todayIso=ISO673.format(today);int curYm=ym673(todayIso);
        Cursor a=db.getReadableDatabase().rawQuery("SELECT a.id,a.name,a.startDate,a.endDate,a.restartDate FROM athletes a JOIN attendance_dashboard_groups g ON g.groupName=a.category AND g.enabled=1 WHERE a.status='AKTİF' AND TRIM(COALESCE(a.deletedAt,''))=''",null);
        while(a.moveToNext()){
            long id=a.getLong(0);String name=a.getString(1),start=a.getString(2),end=a.getString(3),restart=a.getString(4);
            HashSet<Integer> xMonths=new HashSet<>();int latestPastX=0;Cursor xp=db.getReadableDatabase().rawQuery("SELECT year,month FROM payments WHERE athleteId=? AND UPPER(TRIM(COALESCE(marker,'')))='X'",new String[]{String.valueOf(id)});while(xp.moveToNext()){int ym=xp.getInt(0)*100+xp.getInt(1);xMonths.add(ym);if(ym<curYm&&ym>latestPastX)latestPastX=ym;}xp.close();
            if(xMonths.contains(curYm))continue;
            Date segment=parse673(start);Date rd=parse673(restart),ed=parse673(end);
            if(segment!=null&&today.before(segment))continue;
            if(ed!=null){if(rd==null&&today.after(ed))continue;if(rd!=null&&today.after(ed)&&today.before(rd))continue;}
            if(rd!=null&&!today.before(rd)&&(segment==null||rd.after(segment)))segment=rd;
            if(latestPastX>0){Date afterX=parse673(nextMonthStart673(latestPastX));if(afterX!=null&&(segment==null||afterX.after(segment)))segment=afterX;}
            String seg=segment==null?"0000-01-01":ISO673.format(segment);
            Cursor s=db.getReadableDatabase().rawQuery("SELECT s.sessionDate,r.present FROM attendance_sessions s LEFT JOIN attendance_records r ON r.sessionId=s.id AND r.athleteId=? WHERE s.groupName=(SELECT category FROM athletes WHERE id=?) AND s.cancelled=0 AND s.confirmed=1 AND s.sessionDate>=? AND s.sessionDate<=? ORDER BY s.sessionDate",new String[]{String.valueOf(id),String.valueOf(id),seg,todayIso});
            Date first=null,lastPresent=null;while(s.moveToNext()){String d=s.getString(0);if(xMonths.contains(ym673(d)))continue;Date dd=parse673(d);if(dd==null)continue;if(first==null)first=dd;boolean present=s.isNull(1)||s.getInt(1)==1;if(present)lastPresent=dd;}s.close();
            if(first==null)continue;Date anchor=lastPresent!=null?lastPresent:first;int days=(int)((today.getTime()-anchor.getTime())/86400000L);if(days>=15)out.add(new Abs673(id,name,days));
        }a.close();Collections.sort(out,(x,y)->x.days==y.days?x.name.compareToIgnoreCase(y.name):Integer.compare(y.days,x.days));return out;
    }

    private Date parse673(String s){try{return s==null||s.trim().isEmpty()?null:day673(ISO673.parse(s.trim()));}catch(Exception e){return null;}}
    private Date day673(Date d){Calendar c=Calendar.getInstance();c.setTime(d);c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c.getTime();}
    private int ym673(String iso){try{return Integer.parseInt(iso.substring(0,4))*100+Integer.parseInt(iso.substring(5,7));}catch(Exception e){return 0;}}
    private String nextMonthStart673(int ym){int y=ym/100,m=ym%100;m++;if(m==13){m=1;y++;}return String.format(Locale.US,"%04d-%02d-01",y,m);}
    private ScrollView findScroll673(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll673(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private boolean contains673(View v,String n){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR")).contains(n))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(contains673(g.getChildAt(i),n))return true;}return false;}
    private static class Abs673{long id;String name;int days;Abs673(long i,String n,int d){id=i;name=n;days=d;}}
}
