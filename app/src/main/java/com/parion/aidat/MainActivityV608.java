package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.8 - bind the REAL V62 overdue card and calculate only completed personal cycles before today. */
public class MainActivityV608 extends MainActivityV607 {
    private long overdueCacheAt608=0L;
    private int overdueCacheCount608=0;
    private int overdueCacheDebt608=0;

    @Override void showHome(){
        super.showHome();
        patchRealOverdue608();
    }

    private void patchRealOverdue608(){
        TextView label=findOverdueLabel608(root);
        if(label==null)return;
        View card=(label.getParent() instanceof View)?(View)label.getParent():null;
        if(card==null)return;
        ensureOverdueCache608();
        label.setText("GECİKMİŞ\n"+overdueCacheCount608+" SPORCU");
        setCardAmount608(card,money(overdueCacheDebt608),label);
        card.setOnClickListener(v->showOverdue608());
        card.setClickable(true);
    }

    /**
     * Home used to scan every athlete twice: once for count and once for debt.
     * Keep one short-lived result so a single home render performs only one scan.
     * The cache is intentionally only 2 seconds: it collapses duplicate work during
     * one render without hiding newly entered payment data on a later navigation.
     */
    private void ensureOverdueCache608(){
        long now=SystemClock.uptimeMillis();
        if(overdueCacheAt608>0L && now-overdueCacheAt608<2000L)return;
        int n=0,total=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);
        while(c.moveToNext()){
            int debt=overdueAmount608(c.getLong(0));
            if(debt>0){n++;total+=debt;}
        }
        c.close();
        overdueCacheCount608=n;
        overdueCacheDebt608=total;
        overdueCacheAt608=now;
    }

    private TextView findOverdueLabel608(View v){
        if(v instanceof TextView){
            String s=String.valueOf(((TextView)v).getText()).trim().toUpperCase(new Locale("tr","TR"));
            if(s.startsWith("GECİKMİŞ"))return (TextView)v;
        }
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                TextView r=findOverdueLabel608(g.getChildAt(i));if(r!=null)return r;
            }
        }
        return null;
    }

    private void setCardAmount608(View card,String value,TextView label){
        if(!(card instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)card;
        for(int i=0;i<g.getChildCount();i++){
            View x=g.getChildAt(i);
            if(x instanceof TextView && x!=label){((TextView)x).setText(value);return;}
        }
    }

    /**
     * No status filter. Every non-deleted athlete is evaluated.
     * The card containing TODAY is excluded. Future cards are excluded even if
     * they exist for early payment. Only completed personal cycles before the
     * start of today's cycle may create overdue debt.
     */
    private int overdueAmount608(long id){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return 0;}
        String start=s608(a,"startDate"),end=s608(a,"endDate"),restart=s608(a,"restartDate"),sib=s608(a,"sibling");
        a.close();
        if("BURSLU".equalsIgnoreCase(sib))return 0;
        if(start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return 0;

        Calendar today=Calendar.getInstance();
        int anchor=anchorDay(start);
        int todayKey=currentCycleKey(today,anchor);
        Calendar todayCycleStart=cycleDate(todayKey,anchor);

        int first=registrationMonth(start,id,todayKey);
        int reg=parseMonthKey(start);if(reg>0&&reg>first)first=reg;
        if(first>=todayKey)return 0;

        HashMap<Integer,PayRec> pays=paymentMap(id);
        int debt=0,guard=0;
        for(int key=first;guard++<240;key=shiftMonth(key,1)){
            Calendar cycleStart=cycleDate(key,anchor);
            if(!cycleStart.before(todayCycleStart))break;
            int y=key/100,m=key%100;
            if(!activeAt(y,m,start,end,restart))continue;
            PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);
            if("X".equals(r.marker))continue;
            int expected=expectedFeeAt(id,y,m,r);if(expected<=0)continue;
            debt+=Math.max(0,expected-r.amount);
        }
        return debt;
    }

    private void showOverdue608(){
        page="OVERDUE_608";base("GECİKMİŞ SPORCULAR",true);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){
            A x=a(c);int debt=overdueAmount608(x.id);if(debt<=0)continue;
            row(b,x,"BUGÜNKÜ DÖNEMDEN ÖNCE TAMAMLANMIŞ DÖNEM BORCU",debt);n++;
        }
        c.close();
        if(n==0)b.addView(tv("BUGÜNKÜ KİŞİSEL AİDAT DÖNEMİNDEN ÖNCE BORCU BULUNAN SPORCU YOK.",14,Color.DKGRAY,true));
    }

    @Override void goBack(){if("OVERDUE_608".equals(page)){showHome();return;}super.goBack();}

    private String s608(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
}
