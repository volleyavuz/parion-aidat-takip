package com.parion.aidat;

import java.util.*;

/** v4.0.5 - aidat activity is determined by real personal cycle boundaries, not calendar months. */
public class MainActivityV605 extends MainActivityV604 {

    /** From the restart spell onward the athlete gets a NEW personal cycle anchor. */
    int cycleAnchor605(int key,String start,String restart){
        int rk=parseMonthKey(restart);
        if(rk>0&&key>=rk)return anchorDay(restart);
        return anchorDay(start);
    }

    int currentAnchor605(Calendar now,String start,String restart){
        Calendar resume=iso605(restart);
        if(resume!=null&&!now.before(resume))return anchorDay(restart);
        return anchorDay(start);
    }

    Calendar athleteCycleDate605(int key,String start,String restart){return cycleDate(key,cycleAnchor605(key,start,restart));}
    String athleteCycleDateLabel605(int key,String start,String restart){Calendar c=athleteCycleDate605(key,start,restart);return c.get(Calendar.DAY_OF_MONTH)+" "+monthName(c.get(Calendar.MONTH)+1)+" "+c.get(Calendar.YEAR);}

    boolean activeAt605(int year,int month,String start,String end,String restart,String restartEnd){
        int key=year*100+month;
        Calendar cycleStart=athleteCycleDate605(key,start,restart);
        Calendar firstStart=iso605(start),stop=iso605(end),resume=iso605(restart),secondStop=iso605(restartEnd);

        // Second active spell: its own day-of-month and its own optional stop date.
        if(resume!=null&&key>=parseMonthKey(restart)){
            if(cycleStart.before(resume))return false;
            return secondStop==null||cycleStart.before(secondStop);
        }

        if(firstStart!=null&&cycleStart.before(firstStart))return false;
        boolean hasStop=stop!=null&&end!=null&&!"DEVAM".equalsIgnoreCase(end);
        if(!hasStop)return true;
        return cycleStart.before(stop);
    }

    @Override boolean activeAt(int year,int month,String start,String end,String restart){
        return activeAt605(year,month,start,end,restart,"");
    }

    Calendar iso605(String iso){
        try{
            if(iso==null||!iso.matches("\\d{4}-\\d{2}-\\d{2}"))return null;
            Calendar c=Calendar.getInstance();c.clear();
            c.set(Integer.parseInt(iso.substring(0,4)),Integer.parseInt(iso.substring(5,7))-1,Integer.parseInt(iso.substring(8,10)));
            return c;
        }catch(Exception e){return null;}
    }
}
