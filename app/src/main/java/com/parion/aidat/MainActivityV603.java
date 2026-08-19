package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

/** v4.0.3 - personal-cycle overdue logic + exact profile reminder lists. */
public class MainActivityV603 extends MainActivityV602 {

    @Override void showHome(){
        super.showHome();
        patchOverdue603();
        patchSeason603(true);
        patchSeason603(false);
    }

    private void patchOverdue603(){
        ScrollView sv=firstScroll603(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout b=(LinearLayout)sv.getChildAt(0);View card=top603(b,"GECİKMİŞ");if(card==null)return;
        setNumber603(card,countOverdue603());card.setOnClickListener(v->showOverdue603());
    }

    private int countOverdue603(){
        int n=0;Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY id",null);
        while(c.moveToNext())if(overdueAmount603(c.getLong(0))>0)n++;c.close();return n;
    }

    /** Current personal card is NEVER overdue. Only cards strictly before currentCycleKey are checked. */
    private int overdueAmount603(long id){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return 0;}
        String start=s603(a,"startDate"),end=s603(a,"endDate"),restart=s603(a,"restartDate"),sib=s603(a,"sibling");a.close();
        if("BURSLU".equalsIgnoreCase(sib))return 0;
        Calendar now=Calendar.getInstance();int anchor=anchorDay(start),current=currentCycleKey(now,anchor);
        int first=registrationMonth(start,id,current);int reg=parseMonthKey(start);if(reg>0&&reg>first)first=reg;if(first>current)return 0;
        HashMap<Integer,PayRec> pays=paymentMap(id);int debt=0,guard=0;
        for(int key=first;key!=current&&guard++<240;key=shiftMonth(key,1)){
            int y=key/100,m=key%100;if(!activeAt(y,m,start,end,restart))continue;
            PayRec p=pays.get(key);if(p==null)p=new PayRec("",0);if("X".equals(p.marker))continue;
            int expected=expectedFeeAt(id,y,m,p);if(expected<=0)continue;
            debt+=Math.max(0,expected-p.amount);
        }
        return debt;
    }

    private void showOverdue603(){
        page="OVERDUE_603";base("GECİKMİŞ SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);
        while(c.moveToNext()){A x=a(c);int debt=overdueAmount603(x.id);if(debt<=0)continue;row(b,x,"ÖNCEKİ DÖNEMLERDEN KALAN BORÇ",debt);n++;}c.close();
        if(n==0)b.addView(tv("GÜNCEL KARTTAN ÖNCEKİ DÖNEMLERDEN BORCU BULUNAN SPORCU YOK.",14,Color.DKGRAY,true));
    }

    private void patchSeason603(boolean summer){
        String label=summer?"YAZIN ARANACAK":"KIŞIN ARANACAK";ScrollView sv=firstScroll603(root);if(sv==null||sv.getChildCount()==0||!(sv.getChildAt(0) instanceof LinearLayout))return;
        LinearLayout b=(LinearLayout)sv.getChildAt(0);View card=top603(b,label);if(card==null)return;
        setNumber603(card,countSeason603(summer));card.setOnClickListener(v->showSeason603(summer));
    }
    private int countSeason603(boolean summer){String col=summer?"summerCall":"winterCall";Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND "+col+"=1",null);int n=0;if(c.moveToFirst())n=c.getInt(0);c.close();return n;}
    private void showSeason603(boolean summer){
        page=summer?"SUMMER_CALL_603":"WINTER_CALL_603";base(summer?"YAZIN ARANACAK SPORCULAR":"KIŞIN ARANACAK SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);String col=summer?"summerCall":"winterCall";
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))='' AND "+col+"=1 ORDER BY name COLLATE NOCASE",null);int n=0;while(c.moveToNext()){row(b,a(c),null,0);n++;}c.close();if(n==0)b.addView(tv("BU HATIRLATMA İŞARETLİ SPORCU YOK.",14,Color.DKGRAY,true));
    }

    @Override void goBack(){if("OVERDUE_603".equals(page)||"SUMMER_CALL_603".equals(page)||"WINTER_CALL_603".equals(page)){showHome();return;}super.goBack();}

    private ScrollView firstScroll603(View v){if(v instanceof ScrollView)return(ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=firstScroll603(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private boolean has603(View v,String text){if(v instanceof TextView&&String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR")).contains(text.toUpperCase(new Locale("tr","TR"))))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(has603(g.getChildAt(i),text))return true;}return false;}
    private View top603(LinearLayout p,String text){for(int i=0;i<p.getChildCount();i++){View v=p.getChildAt(i);if(has603(v,text))return v;}return null;}
    private void setNumber603(View v,int n){ArrayList<TextView>x=new ArrayList<>();collect603(v,x);for(TextView t:x){if(String.valueOf(t.getText()).trim().matches("\\d+")){t.setText(String.valueOf(n));return;}}}
    private void collect603(View v,ArrayList<TextView>x){if(v instanceof TextView)x.add((TextView)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collect603(g.getChildAt(i),x);}}
    private String s603(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
}
