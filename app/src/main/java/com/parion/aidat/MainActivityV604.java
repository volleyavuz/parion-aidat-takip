package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.4 - exact card binding + exact personal-cycle overdue. */
public class MainActivityV604 extends MainActivityV603 {

    @Override void showHome(){
        super.showHome();
        patchExactSeason604(true);
        patchExactSeason604(false);
        patchExactOverdue604();
    }

    private void patchExactSeason604(boolean summer){
        String label=summer?"YAZIN ARANACAK":"KIŞIN ARANACAK";
        View card=findCardByExactLabel604(root,label);if(card==null)return;
        setPrimaryValue604(card,String.valueOf(countSeason604(summer)));
        card.setOnClickListener(v->showSeason604(summer));
        card.setClickable(true);
    }

    private int countSeason604(boolean summer){
        String col=summer?"summerCall":"winterCall";
        Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND "+col+"=1",null);
        int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;
    }

    private void showSeason604(boolean summer){
        page=summer?"SUMMER_CALL_604":"WINTER_CALL_604";
        base(summer?"YAZIN ARANACAK SPORCULAR":"KIŞIN ARANACAK SPORCULAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);String col=summer?"summerCall":"winterCall";
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND "+col+"=1 ORDER BY name COLLATE NOCASE",null);
        int n=0;while(c.moveToNext()){row(b,a(c),null,0);n++;}c.close();
        if(n==0)b.addView(tv("BU HATIRLATMA İŞARETLİ SPORCU YOK.",14,Color.DKGRAY,true));
    }

    private void patchExactOverdue604(){
        View card=findCardByExactLabel604(root,"GECİKMİŞ");if(card==null)return;
        setPrimaryValue604(card,money(totalOverdue604()));
        card.setOnClickListener(v->showOverdue604());
        card.setClickable(true);
    }

    private int totalOverdue604(){
        int total=0;Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);
        while(c.moveToNext())total+=overdueAmount604(c.getLong(0));c.close();return total;
    }

    /** Uses the same currentKey rule as profile aidat cards. Current card is excluded completely. */
    private int overdueAmount604(long id){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return 0;}
        String start=s604(a,"startDate"),end=s604(a,"endDate"),restart=s604(a,"restartDate"),sib=s604(a,"sibling");a.close();
        if("BURSLU".equalsIgnoreCase(sib))return 0;
        Calendar now=Calendar.getInstance();int anchor=anchorDay(start);int currentKey=currentCycleKey(now,anchor);
        int first=registrationMonth(start,id,currentKey);int regKey=parseMonthKey(start);if(regKey>0&&regKey>first)first=regKey;if(first>=currentKey)return 0;
        HashMap<Integer,PayRec> pays=paymentMap(id);int debt=0;int guard=0;
        for(int key=first;key!=currentKey&&guard++<240;key=shiftMonth(key,1)){
            int y=key/100,m=key%100;if(!activeAt(y,m,start,end,restart))continue;
            PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);
            if("X".equals(r.marker))continue;
            int expected=expectedFeeAt(id,y,m,r);if(expected<=0)continue;
            debt+=Math.max(0,expected-r.amount);
        }
        return debt;
    }

    private void showOverdue604(){
        page="OVERDUE_604";base("GECİKMİŞ SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){
            A x=a(c);int debt=overdueAmount604(x.id);if(debt<=0)continue;
            row(b,x,"GÜNCEL KARTTAN ÖNCEKİ DÖNEM BORCU",debt);n++;
        }
        c.close();if(n==0)b.addView(tv("GÜNCEL AİDAT KARTINDAN ÖNCEKİ DÖNEMLERDEN BORCU BULUNAN SPORCU YOK.",14,Color.DKGRAY,true));
    }

    @Override void goBack(){
        if("SUMMER_CALL_604".equals(page)||"WINTER_CALL_604".equals(page)||"OVERDUE_604".equals(page)){showHome();return;}
        super.goBack();
    }

    private View findCardByExactLabel604(View v,String wanted){
        if(v instanceof TextView){
            String s=String.valueOf(((TextView)v).getText()).trim();
            if(s.equalsIgnoreCase(wanted)){
                ViewParent p=v.getParent();return p instanceof View?(View)p:null;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findCardByExactLabel604(g.getChildAt(i),wanted);if(r!=null)return r;}}
        return null;
    }

    private void setPrimaryValue604(View card,String value){
        if(!(card instanceof ViewGroup))return;ViewGroup g=(ViewGroup)card;
        for(int i=0;i<g.getChildCount();i++)if(g.getChildAt(i) instanceof TextView){TextView t=(TextView)g.getChildAt(i);String s=String.valueOf(t.getText()).trim();if(!s.equalsIgnoreCase("YAZIN ARANACAK")&&!s.equalsIgnoreCase("KIŞIN ARANACAK")&&!s.equalsIgnoreCase("GECİKMİŞ")){t.setText(value);return;}}
    }

    private String s604(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
}
