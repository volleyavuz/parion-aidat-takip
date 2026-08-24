package com.parion.aidat;

import android.database.Cursor;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** v4.0.58 - freeze the single-pass dashboard after first construction so inherited legacy async patches cannot restyle it. */
public class MainActivityV658 extends MainActivityV657 {
    private final ExecutorService overdueExec658=Executors.newSingleThreadExecutor();
    private volatile int overdueGen658=0;
    private static final String FINAL_HOME_658="HOME_FINAL_658";

    @Override void showHome(){
        super.showHome();
        if(root==null)return;
        page=FINAL_HOME_658;
        final int gen=++overdueGen658;
        calculateOverdue658(gen);
    }

    private void calculateOverdue658(int gen){
        overdueExec658.execute(()->{
            int count=0,total=0;
            Cursor c=null;
            try{
                c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);
                while(c.moveToNext()){
                    int d=overdueAmount658(c.getLong(0));
                    if(d>0){count++;total+=d;}
                }
            }catch(Exception ignored){}finally{if(c!=null)c.close();}
            final int fc=count,ft=total;
            runOnUiThread(()->{
                if(gen!=overdueGen658||root==null||!FINAL_HOME_658.equals(page))return;
                View card=findOverdueCard658(root);
                if(card==null)return;
                TextView value=findTagged658(card,"value");
                TextView sub=findTagged658(card,"sub");
                if(value!=null)value.setText(money658(ft));
                if(sub!=null)sub.setText(fc+" sporcu");
            });
        });
    }

    private int overdueAmount658(long id){
        Cursor ac=db.athlete(id);if(!ac.moveToFirst()){ac.close();return 0;}
        String start=s658(ac,"startDate"),end=s658(ac,"endDate"),restart=s658(ac,"restartDate"),sib=s658(ac,"sibling");ac.close();
        if("BURSLU".equalsIgnoreCase(sib)||start==null||!start.matches("\\d{4}-\\d{2}-\\d{2}"))return 0;
        Calendar today=Calendar.getInstance();int anchor=anchorDay(start),todayKey=currentCycleKey(today,anchor);Calendar todayCycleStart=cycleDate(todayKey,anchor);
        int first=registrationMonth(start,id,todayKey),reg=parseMonthKey(start);if(reg>0&&reg>first)first=reg;if(first>=todayKey)return 0;
        HashMap<Integer,PayRec> pays=paymentMap(id);int debt=0,guard=0;
        for(int key=first;guard++<240;key=shiftMonth(key,1)){
            Calendar cycleStart=cycleDate(key,anchor);if(!cycleStart.before(todayCycleStart))break;
            int y=key/100,m=key%100;if(!activeAt(y,m,start,end,restart))continue;
            PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);if("X".equals(r.marker))continue;
            int expected=expectedFeeAt(id,y,m,r);if(expected<=0)continue;debt+=Math.max(0,expected-r.amount);
        }
        return debt;
    }

    private View findOverdueCard658(View v){
        if(v instanceof TextView){String n=norm658(String.valueOf(((TextView)v).getText()));if(n.equals("GECİKMİŞ")){View cur=v;while(cur!=null&&cur!=root){if(findTagged658(cur,"value")!=null&&findTagged658(cur,"sub")!=null)return cur;ViewParent p=cur.getParent();if(!(p instanceof View))break;cur=(View)p;}}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=findOverdueCard658(g.getChildAt(i));if(r!=null)return r;}}
        return null;
    }
    private TextView findTagged658(View v,String tag){if(v instanceof TextView&&tag.equals(v.getTag()))return(TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView t=findTagged658(g.getChildAt(i),tag);if(t!=null)return t;}}return null;}
    private String money658(long n){return String.format(new Locale("tr","TR"),"₺%,d",n).replace(',','.');}
    private String s658(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
    private String norm658(String s){return s==null?"":s.replace('\n',' ').replaceAll("\\s+"," ").trim().toUpperCase(new Locale("tr","TR"));}

    @Override void goBack(){
        if(FINAL_HOME_658.equals(page)){page="HOME";super.goBack();return;}
        super.goBack();
    }

    @Override protected void onDestroy(){overdueGen658++;overdueExec658.shutdownNow();super.onDestroy();}
}
