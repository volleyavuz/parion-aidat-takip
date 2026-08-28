package com.parion.aidat;

import android.database.Cursor;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.1.49 - each membership spell keeps its own personal dues anchor. */
public class MainActivityV725 extends MainActivityV724 {

    @Override void showProfile(long id){
        super.showProfile(id);
        if(root!=null)root.post(()->rebuildProfileCycles725(id));
    }

    private void rebuildProfileCycles725(long id){
        if(root==null||!"PROFILE".equals(page)||currentAthlete!=id)return;
        TextView heading=findText725(root,"AİDAT DÖNEMLERİ");
        if(heading==null||!(heading.getParent() instanceof LinearLayout))return;
        LinearLayout box=(LinearLayout)heading.getParent();
        int hi=box.indexOfChild(heading);if(hi<0)return;
        int endIndex=findShareIndex725(box,hi+1);if(endIndex<0)return;

        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String start=s725(a,"startDate"),end=s725(a,"endDate"),restart=s725(a,"restartDate"),sibling=s725(a,"sibling");a.close();
        if(start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return;

        // Remove only the old generated dues-period rows. Everything after the share
        // button (materials etc.) is deliberately left untouched.
        for(int i=endIndex-1;i>hi;i--)box.removeViewAt(i);

        HashMap<Integer,PayRec> pays=paymentMap(id);
        Calendar now=Calendar.getInstance();
        int currentAnchor=currentAnchor605(now,start,restart);
        int currentKey=currentCycleKey(now,currentAnchor);
        int startKey=registrationMonth(start,id,currentKey);
        int regKey=parseMonthKey(start);if(regKey>0&&regKey>startKey)startKey=regKey;
        if(startKey>currentKey)startKey=currentKey;

        int insert=hi+1,guard=0;
        for(int key=startKey;guard++<240;key=shiftMonth(key,1)){
            LinearLayout tmp=new LinearLayout(this);tmp.setOrientation(LinearLayout.VERTICAL);
            addCycleProfileRow(tmp,id,key,cycleAnchor605(key,start,restart),start,end,restart,sibling,pays,false);
            if(tmp.getChildCount()>0){View row=tmp.getChildAt(0);tmp.removeView(row);box.addView(row,insert++);}
            if(key==currentKey)break;
        }

        int waitingKey=shiftMonth(currentKey,1);
        LinearLayout tmp=new LinearLayout(this);tmp.setOrientation(LinearLayout.VERTICAL);
        addCycleProfileRow(tmp,id,waitingKey,cycleAnchor605(waitingKey,start,restart),start,end,restart,sibling,pays,true);
        if(tmp.getChildCount()>0){View row=tmp.getChildAt(0);tmp.removeView(row);box.addView(row,insert);}
    }

    private int findShareIndex725(LinearLayout box,int from){
        for(int i=from;i<box.getChildCount();i++){
            View v=box.getChildAt(i);String t=text725(v).toUpperCase(new Locale("tr","TR"));
            if(t.contains("ÖDEME BİLGİSİ PAYLAŞ")||t.contains("AİDAT BİLANÇOSU"))return i;
        }return -1;
    }

    private TextView findText725(View v,String exact){
        if(v instanceof TextView&&exact.equalsIgnoreCase(String.valueOf(((TextView)v).getText()).trim()))return(TextView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView r=findText725(g.getChildAt(i),exact);if(r!=null)return r;}}return null;
    }
    private String text725(View v){StringBuilder b=new StringBuilder();collect725(v,b);return b.toString();}
    private void collect725(View v,StringBuilder b){if(v instanceof TextView)b.append(' ').append(((TextView)v).getText());if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collect725(g.getChildAt(i),b);}}
    private String s725(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
}
