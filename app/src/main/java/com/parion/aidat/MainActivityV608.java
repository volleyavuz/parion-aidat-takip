package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** v4.0.8 - bind the REAL V62 overdue card and calculate only completed personal cycles before today. */
public class MainActivityV608 extends MainActivityV607 {
    private long overdueCacheAt608=0L;
    private int overdueCacheCount608=0;
    private int overdueCacheDebt608=0;
    private final ExecutorService overdueExec608=Executors.newSingleThreadExecutor();
    private volatile int overdueGeneration608=0;

    @Override void showHome(){
        super.showHome();
        patchRealOverdueAsync608();
    }

    private void patchRealOverdueAsync608(){
        final TextView label=findOverdueLabel608(root);
        if(label==null)return;
        final View card=(label.getParent() instanceof View)?(View)label.getParent():null;
        if(card==null)return;
        final int generation=++overdueGeneration608;
        label.setText("GECİKMİŞ\nHESAPLANIYOR");
        setCardAmount608(card,"…",label);
        card.setOnClickListener(v->showOverdue608());
        card.setClickable(true);
        overdueExec608.execute(()->{
            ensureOverdueCache608();
            final int count=overdueCacheCount608;
            final int debt=overdueCacheDebt608;
            runOnUiThread(()->{
                if(generation!=overdueGeneration608)return;
                if(!"HOME".equals(page) || root==null)return;
                TextView current=findOverdueLabel608(root);
                if(current==null)return;
                View currentCard=(current.getParent() instanceof View)?(View)current.getParent():null;
                if(currentCard==null)return;
                current.setText("GECİKMİŞ\n"+count+" SPORCU");
                setCardAmount608(currentCard,money(debt),current);
                currentCard.setOnClickListener(v->showOverdue608());
                currentCard.setClickable(true);
            });
        });
    }

    private synchronized void ensureOverdueCache608(){
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

    @Override protected void onDestroy(){
        overdueGeneration608++;
        overdueExec608.shutdownNow();
        super.onDestroy();
    }

    private String s608(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
}
