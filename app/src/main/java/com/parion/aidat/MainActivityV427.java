package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivityV427 extends MainActivityV426 {
    private static final int OLD_PAID_GREEN=Color.rgb(198,239,206);

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        normalizeNotes427();
    }

    @Override void base(String title,boolean back){
        super.base(title,back);
        try{
            if(root==null||root.getChildCount()==0||!(root.getChildAt(0) instanceof LinearLayout))return;
            LinearLayout old=(LinearLayout)root.getChildAt(0);
            View backView=null,undoView=null;
            for(int i=0;i<old.getChildCount();i++){
                View v=old.getChildAt(i);
                if(v instanceof TextView){String s=String.valueOf(((TextView)v).getText());if("‹".equals(s))backView=v;else if(s.contains("GERİ AL"))undoView=v;}
            }
            old.removeAllViews();old.setGravity(Gravity.CENTER_VERTICAL);old.setPadding(dp(7),dp(5),dp(7),dp(5));old.setBackgroundColor(BLACK);
            if(back&&backView!=null)old.addView(backView,new LinearLayout.LayoutParams(dp(42),dp(50)));
            TextView logo=new TextView(this);logo.setText("");GradientDrawable lg=new GradientDrawable();lg.setColor(Color.TRANSPARENT);lg.setStroke(dp(1),GOLD);lg.setCornerRadius(dp(5));logo.setBackground(lg);LinearLayout.LayoutParams ll=new LinearLayout.LayoutParams(dp(38),dp(38));ll.setMargins(dp(3),0,dp(8),0);old.addView(logo,ll);
            LinearLayout texts=new LinearLayout(this);texts.setOrientation(LinearLayout.VERTICAL);texts.setGravity(Gravity.CENTER_VERTICAL);
            TextView brand=tv("PARİON SPOR KULÜBÜ SPORCU TAKİP SİSTEMİ",11,GOLD,true);brand.setPadding(0,0,0,0);brand.setMaxLines(2);texts.addView(brand,new LinearLayout.LayoutParams(-1,-2));
            if(title!=null&&!title.trim().isEmpty()&&!"PARION SPOR OKULU".equalsIgnoreCase(title.trim())){TextView sub=tv(title.toUpperCase(TR),9,Color.LTGRAY,false);sub.setPadding(0,1,0,0);sub.setMaxLines(1);texts.addView(sub,new LinearLayout.LayoutParams(-1,-2));}
            old.addView(texts,new LinearLayout.LayoutParams(0,dp(54),1));
            if(undoView!=null)old.addView(undoView,new LinearLayout.LayoutParams(dp(86),dp(48)));
        }catch(Exception ignored){}
    }

    @Override void addCycleProfileRow(LinearLayout b,long id,int key,int anchor,String start,String end,String restart,String sibling,HashMap<Integer,PayRec> pays,boolean future){
        int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeOnDue427(id,key,anchor,start,end,restart);
        String status,detail;int color;String period=cycleDateLabel(key,anchor)+" – "+cycleDateLabel(shiftMonth(key,1),anchor);
        if(future&&r.amount<=0&&!isDate(r.marker)&&!"X".equals(r.marker)){status="BEKLİYOR";color=Color.WHITE;detail=period+" • "+(expected>0?money(expected):"—");}
        else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;detail=period+" • ARA VERDİ";}
        else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=status.equals("ÖDENDİ")?OLD_PAID_GREEN:ORANGE;detail=period+" • "+money(r.amount)+" • "+status;}
        else if(isDate(r.marker)){status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?OLD_PAID_GREEN:ORANGE;detail=period+" • "+dateTr(r.marker)+" • "+money(r.amount)+" • "+status;}
        else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);detail=period+" • AKTİF DEĞİL";}
        else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);detail=period+" • BURSLU";}
        else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);detail=period+" • VERİ YOK";}
        else{status="ÖDEME DÖNEMİ";color=YELLOW;detail=period+" • "+money(expected)+" • ÖDEME DÖNEMİ";}
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(round(color,9));row.addView(tv(monthName(mo)+" "+yr,14,BLACK,true));row.addView(tv(detail,12,Color.DKGRAY,false));
        // Yönetici her görünen 2026 dönemini, sporcu bugün aktif olmasa bile düzeltebilir.
        if(yr==2026){final int mm=mo,fee=expected>0?expected:Math.max(0,currentMonthlyFee(id)),amt=r.amount;final String mk=r.marker;row.setClickable(true);row.setOnClickListener(v->editPayment(id,mm,fee,mk,amt));}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(row,lp);
    }

    private boolean activeOnDue427(long athleteId,int key,int anchor,String start,String end,String restart){
        Calendar due=cycleDate(key,anchor);String ds=String.format(Locale.US,"%04d-%02d-%02d",due.get(Calendar.YEAR),due.get(Calendar.MONTH)+1,due.get(Calendar.DAY_OF_MONTH));
        try{SQLiteDatabase d=db.getReadableDatabase();Cursor t=d.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name='membership_events'",null);boolean has=t.moveToFirst();t.close();if(has){Cursor c=d.rawQuery("SELECT eventType FROM membership_events WHERE athleteId=? AND eventDate<=? ORDER BY eventDate ASC,id ASC",new String[]{String.valueOf(athleteId),ds});boolean any=false,on=false;while(c.moveToNext()){any=true;String type=c.getString(0);if("START".equals(type)||"RESTART".equals(type))on=true;else if("LEAVE".equals(type))on=false;}c.close();if(any)return on;}}catch(Exception ignored){}
        if(start!=null&&start.matches("\\d{4}-\\d{2}-\\d{2}")&&ds.compareTo(start)<0)return false;if(restart!=null&&restart.matches("\\d{4}-\\d{2}-\\d{2}")&&ds.compareTo(restart)>=0&&(end==null||!end.matches("\\d{4}-\\d{2}-\\d{2}")||restart.compareTo(end)>0))return true;if(end!=null&&end.matches("\\d{4}-\\d{2}-\\d{2}")&&ds.compareTo(end)>=0)return false;return true;
    }

    private void normalizeNotes427(){
        try{SQLiteDatabase d=db.getWritableDatabase();ArrayList<long[]> ids=new ArrayList<>();ArrayList<String> vals=new ArrayList<>();Cursor c=d.rawQuery("SELECT id,notes FROM athletes WHERE TRIM(COALESCE(notes,''))<>''",null);while(c.moveToNext()){String old=c.getString(1),up=old==null?"":old.toUpperCase(TR);if(!up.equals(old)){ids.add(new long[]{c.getLong(0)});vals.add(up);}}c.close();if(ids.isEmpty())return;try{d.execSQL("UPDATE sync_control SET suppress=1 WHERE id=1");}catch(Exception ignored){}d.beginTransaction();for(int i=0;i<ids.size();i++){ContentValues v=new ContentValues();v.put("notes",vals.get(i));d.update("athletes",v,"id=?",new String[]{String.valueOf(ids.get(i)[0])});}d.setTransactionSuccessful();d.endTransaction();try{d.execSQL("UPDATE sync_control SET suppress=0 WHERE id=1");ContentValues q=new ContentValues();q.put("kind","SPORCU");q.put("entityId","NOT_NORMALIZE");d.insert("sync_queue",null,q);}catch(Exception ignored){} }catch(Exception ignored){try{db.getWritableDatabase().execSQL("UPDATE sync_control SET suppress=0 WHERE id=1");}catch(Exception ignored2){}}
    }
}
