package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.7 - overdue is based strictly on TODAY's personal cycle; future/early-payment cards are ignored. */
public class MainActivityV607 extends MainActivityV606 {

    @Override void showHome(){
        super.showHome();
        patchOverdue607();
    }

    private void patchOverdue607(){
        View card=findExact607(root,"GECİKMİŞ");
        if(card==null)return;
        setValue607(card,money(totalOverdue607()));
        card.setOnClickListener(v->showOverdue607());
        card.setClickable(true);
    }

    private int totalOverdue607(){
        int total=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);
        while(c.moveToNext()) total+=overdueAmount607(c.getLong(0));
        c.close();
        return total;
    }

    /**
     * Reference point is ONLY today's date. The personal cycle containing today
     * is the current cycle and is never overdue. Any future card, including an
     * early-payment card, is completely ignored. Only active cycles strictly
     * before today's current cycle can create overdue debt.
     */
    private int overdueAmount607(long id){
        Cursor a=db.athlete(id);
        if(!a.moveToFirst()){a.close();return 0;}
        String start=s607(a,"startDate"),end=s607(a,"endDate"),restart=s607(a,"restartDate"),sib=s607(a,"sibling");
        a.close();
        if("BURSLU".equalsIgnoreCase(sib))return 0;

        Calendar now=Calendar.getInstance();
        int anchor=anchorDay(start);
        int currentKey=currentCycleKey(now,anchor);
        int first=registrationMonth(start,id,currentKey);
        int reg=parseMonthKey(start);
        if(reg>0&&reg>first)first=reg;
        if(first>=currentKey)return 0;

        HashMap<Integer,PayRec> pays=paymentMap(id);
        int debt=0,guard=0;
        for(int key=first;key!=currentKey&&guard++<240;key=shiftMonth(key,1)){
            int y=key/100,m=key%100;
            if(!activeAt(y,m,start,end,restart))continue;
            PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);
            if("X".equals(r.marker))continue;
            int expected=expectedFeeAt(id,y,m,r);if(expected<=0)continue;
            debt+=Math.max(0,expected-r.amount);
        }
        return debt;
    }

    private void showOverdue607(){
        page="OVERDUE_607";
        base("GECİKMİŞ SPORCULAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){
            A x=a(c);int debt=overdueAmount607(x.id);if(debt<=0)continue;
            row(b,x,"BUGÜNKÜ DÖNEMDEN ÖNCEKİ BORÇ",debt);n++;
        }
        c.close();
        if(n==0)b.addView(tv("BUGÜNKÜ AİDAT DÖNEMİNDEN ÖNCE BORCU BULUNAN SPORCU YOK.",14,Color.DKGRAY,true));
    }

    @Override void goBack(){
        if("OVERDUE_607".equals(page)){showHome();return;}
        super.goBack();
    }

    private View findExact607(View v,String wanted){
        if(v instanceof TextView){
            String s=String.valueOf(((TextView)v).getText()).trim();
            if(s.equalsIgnoreCase(wanted)){
                ViewParent p=v.getParent();return p instanceof View?(View)p:null;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findExact607(g.getChildAt(i),wanted);if(r!=null)return r;}}
        return null;
    }

    private void setValue607(View card,String value){
        if(!(card instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)card;
        for(int i=0;i<g.getChildCount();i++)if(g.getChildAt(i) instanceof TextView){
            TextView t=(TextView)g.getChildAt(i);
            String s=String.valueOf(t.getText()).trim();
            if(!s.equalsIgnoreCase("GECİKMİŞ")){t.setText(value);return;}
        }
    }

    private String s607(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
}
