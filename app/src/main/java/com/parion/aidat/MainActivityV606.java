package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.6 - overdue begins only after a later active dues card exists. */
public class MainActivityV606 extends MainActivityV605 {

    @Override void showHome(){
        super.showHome();
        patchOverdue606();
    }

    private void patchOverdue606(){
        View card=findExact606(root,"GECİKMİŞ");
        if(card==null)return;
        setValue606(card,money(totalOverdue606()));
        card.setOnClickListener(v->showOverdue606());
        card.setClickable(true);
    }

    private int totalOverdue606(){
        int total=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);
        while(c.moveToNext()) total+=overdueAmount606(c.getLong(0));
        c.close();
        return total;
    }

    /**
     * The newest ACTIVE dues card is never overdue. Only active cards before it
     * can create overdue debt. This means a member with only the latest card
     * unpaid is not listed; when a later active card opens, that older unpaid
     * card becomes overdue.
     */
    private int overdueAmount606(long id){
        Cursor a=db.athlete(id);
        if(!a.moveToFirst()){a.close();return 0;}
        String start=s606(a,"startDate"),end=s606(a,"endDate"),restart=s606(a,"restartDate"),sib=s606(a,"sibling");
        a.close();
        if("BURSLU".equalsIgnoreCase(sib))return 0;

        Calendar now=Calendar.getInstance();
        int anchor=anchorDay(start);
        int currentKey=currentCycleKey(now,anchor);
        int first=registrationMonth(start,id,currentKey);
        int reg=parseMonthKey(start);
        if(reg>0&&reg>first)first=reg;
        if(first>currentKey)return 0;

        // Find the latest active card that has actually opened by today.
        int latestActive=-1, guard=0;
        for(int key=first;;key=shiftMonth(key,1)){
            int y=key/100,m=key%100;
            if(activeAt(y,m,start,end,restart)) latestActive=key;
            if(key==currentKey || guard++>240)break;
        }
        if(latestActive<0 || latestActive==first)return 0;

        HashMap<Integer,PayRec> pays=paymentMap(id);
        int debt=0;guard=0;
        for(int key=first;key!=latestActive&&guard++<240;key=shiftMonth(key,1)){
            int y=key/100,m=key%100;
            if(!activeAt(y,m,start,end,restart))continue;
            PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);
            if("X".equals(r.marker))continue;
            int expected=expectedFeeAt(id,y,m,r);if(expected<=0)continue;
            debt+=Math.max(0,expected-r.amount);
        }
        return debt;
    }

    private void showOverdue606(){
        page="OVERDUE_606";
        base("GECİKMİŞ SPORCULAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){
            A x=a(c);int debt=overdueAmount606(x.id);if(debt<=0)continue;
            row(b,x,"SON AKTİF KARTTAN ÖNCEKİ BORÇ",debt);n++;
        }
        c.close();
        if(n==0)b.addView(tv("SON AKTİF AİDAT KARTINDAN ÖNCE BORCU BULUNAN SPORCU YOK.",14,Color.DKGRAY,true));
    }

    @Override void goBack(){
        if("OVERDUE_606".equals(page)){showHome();return;}
        super.goBack();
    }

    private View findExact606(View v,String wanted){
        if(v instanceof TextView){
            String s=String.valueOf(((TextView)v).getText()).trim();
            if(s.equalsIgnoreCase(wanted)){
                ViewParent p=v.getParent();return p instanceof View?(View)p:null;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findExact606(g.getChildAt(i),wanted);if(r!=null)return r;}}
        return null;
    }

    private void setValue606(View card,String value){
        if(!(card instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)card;
        for(int i=0;i<g.getChildCount();i++)if(g.getChildAt(i) instanceof TextView){
            TextView t=(TextView)g.getChildAt(i);
            String s=String.valueOf(t.getText()).trim();
            if(!s.equalsIgnoreCase("GECİKMİŞ")){t.setText(value);return;}
        }
    }

    private String s606(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
}
