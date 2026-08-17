package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import java.util.*;

public class MainActivityV422 extends MainActivityV421 {
    private static final int OLD_PAID_GREEN=Color.rgb(198,239,206);

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        rebuildAuditTriggers422();
    }

    @Override int paymentColor(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){
        int c=super.paymentColor(m,fee,sibling,start,end,restart,marker,amount);
        return (c==GREEN || c==Color.rgb(9,242,153)) ? OLD_PAID_GREEN : c;
    }

    @Override void addCycleProfileRow(android.widget.LinearLayout b,long id,int key,int anchor,String start,String end,String restart,String sibling,HashMap<Integer,PayRec> pays,boolean future){
        int yr=key/100,mo=key%100; PayRec r=pays.get(key); if(r==null)r=new PayRec("",0); int expected=expectedFeeAt(id,yr,mo,r); boolean active=activeOnDue422(id,key,anchor,start,end,restart);
        String status; int color; String detail;
        String period=cycleDateLabel(key,anchor)+" – "+cycleDateLabel(shiftMonth(key,1),anchor);
        if(future&&r.amount<=0&&!isDate(r.marker)&&!"X".equals(r.marker)){status="BEKLİYOR";color=Color.WHITE;detail=period+" • "+(expected>0?money(expected):"—");}
        else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;detail=period+" • ARA VERDİ";}
        else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=status.equals("ÖDENDİ")?OLD_PAID_GREEN:ORANGE;detail=period+" • "+money(r.amount)+" • "+status;}
        else if(isDate(r.marker)){status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?OLD_PAID_GREEN:ORANGE;detail=period+" • "+dateTr(r.marker)+" • "+money(r.amount)+" • "+status;}
        else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);detail=period+" • AKTİF DEĞİL";expected=0;}
        else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);detail=period+" • BURSLU";expected=0;}
        else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);detail=period+" • VERİ YOK";}
        else{status="ÖDEME DÖNEMİ";color=YELLOW;detail=period+" • "+money(expected)+" • ÖDEME DÖNEMİ";}
        android.widget.LinearLayout row=new android.widget.LinearLayout(this);row.setOrientation(android.widget.LinearLayout.VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(round(color,9));row.addView(tv(monthName(mo)+" "+yr,14,BLACK,true));row.addView(tv(detail,12,Color.DKGRAY,false));
        if(yr==2026&&active&&expected>0){final int mm=mo,fee=expected,amt=r.amount;final String mk=r.marker;row.setOnClickListener(v->editPayment(id,mm,fee,mk,amt));}
        android.widget.LinearLayout.LayoutParams lp=new android.widget.LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(row,lp);
    }

    private boolean activeOnDue422(long athleteId,int key,int anchor,String start,String end,String restart){
        Calendar due=cycleDate(key,anchor);String ds=String.format(Locale.US,"%04d-%02d-%02d",due.get(Calendar.YEAR),due.get(Calendar.MONTH)+1,due.get(Calendar.DAY_OF_MONTH));
        try{
            SQLiteDatabase d=db.getReadableDatabase();Cursor t=d.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name='membership_events'",null);boolean has=t.moveToFirst();t.close();
            if(has){Cursor c=d.rawQuery("SELECT eventType FROM membership_events WHERE athleteId=? AND eventDate<=? ORDER BY eventDate ASC,id ASC",new String[]{String.valueOf(athleteId),ds});boolean any=false,active=false;while(c.moveToNext()){any=true;String type=c.getString(0);if("START".equals(type)||"RESTART".equals(type))active=true;else if("LEAVE".equals(type))active=false;}c.close();if(any)return active;}
        }catch(Exception ignored){}
        if(isIso422(start)&&ds.compareTo(start)<0)return false;
        if(isIso422(restart)&&ds.compareTo(restart)>=0){if(isIso422(end)&&restart.compareTo(end)>0)return true;}
        if(isIso422(end)&&ds.compareTo(end)>=0)return false;
        return true;
    }
    private boolean isIso422(String s){return s!=null&&s.matches("\\d{4}-\\d{2}-\\d{2}");}

    @Override DashData dashboardData(){
        DashData d=new DashData();Calendar now=Calendar.getInstance();int cy=now.get(Calendar.YEAR),cm=now.get(Calendar.MONTH)+1,currentCalendarKey=cy*100+cm;Calendar monthStart=(Calendar)now.clone();monthStart.set(Calendar.DAY_OF_MONTH,1);zeroTime(monthStart);
        Cursor c=db.athletes("","TÜMÜ");while(c.moveToNext()){
            long id=c.getLong(c.getColumnIndexOrThrow("id"));String name=s(c,"name"),cat=s(c,"category"),photo=s(c,"photo"),start=s(c,"startDate"),end=s(c,"endDate"),restart=s(c,"restartDate"),sib=s(c,"sibling");int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));int anchor=anchorDay(start);HashMap<Integer,PayRec> pays=paymentMap(id);
            int paidMonth=0;for(Map.Entry<Integer,PayRec> e:pays.entrySet()){PayRec pr=e.getValue();if(pr.amount<=0)continue;if(isDate(pr.marker)){Calendar pd=parseIsoCal(pr.marker);if(pd!=null&&pd.get(Calendar.YEAR)==cy&&pd.get(Calendar.MONTH)+1==cm&&!pd.after(now))paidMonth+=pr.amount;}else if(e.getKey()==currentCalendarKey)paidMonth+=pr.amount;}
            if(paidMonth>0){DashItem z=item(id,name,cat,photo,by);z.amount=paidMonth;z.detail="Bu ay tahsil edilen: "+money(paidMonth);d.collected.put(id,z);d.collectedTotal+=paidMonth;}
            int currentKey=currentCycleKey(now,anchor);Calendar due=cycleDate(currentKey,anchor);boolean active=activeOnDue422(id,currentKey,anchor,start,end,restart);PayRec cr=pays.get(currentKey);if(cr==null)cr=new PayRec("",0);int exp=expectedFeeAt(id,currentKey/100,currentKey%100,cr);if("BURSLU".equalsIgnoreCase(sib))exp=0;int remain=Math.max(0,exp-cr.amount);
            if(active&&remain>0){if(!due.after(now)){DashItem z=item(id,name,cat,photo,by);z.amount=remain;z.detail="Vade: "+cycleDateLabel(currentKey,anchor)+" • Beklenen: "+money(remain);d.expected.put(id,z);d.expectedTotal+=remain;}else if(due.get(Calendar.YEAR)==cy&&due.get(Calendar.MONTH)+1==cm){DashItem z=item(id,name,cat,photo,by);z.amount=remain;z.detail="Vade: "+cycleDateLabel(currentKey,anchor)+" • Gelecek: "+money(remain);d.upcoming.put(id,z);d.upcomingTotal+=remain;}}
            int reg=parseMonthKey(start);if(reg==0)reg=202601;int lastCompleted=shiftMonth(currentKey,-1),late=0;
            for(int k=Math.max(reg,202601);k<=lastCompleted;k=shiftMonth(k,1)){if(k/100!=2026)continue;if(!activeOnDue422(id,k,anchor,start,end,restart))continue;PayRec pr=pays.get(k);if(pr==null)pr=new PayRec("",0);if("X".equals(pr.marker))continue;int ex=expectedFeeAt(id,k/100,k%100,pr);if(ex<=0||"BURSLU".equalsIgnoreCase(sib))continue;late+=Math.max(0,ex-pr.amount);if(k==lastCompleted)break;}
            if(late>0){DashItem z=item(id,name,cat,photo,by);z.amount=late;z.detail="Önceki dönemlerden gecikmiş: "+money(late);d.overdue.put(id,z);d.overdueTotal+=late;}
        }c.close();return d;
    }

    private void rebuildAuditTriggers422(){
        try{
            SQLiteDatabase d=db.getWritableDatabase();
            String[] names={"q_athletes_ai","q_athletes_au","q_athletes_ad","q_payments_ai","q_payments_au","q_payments_ad","q_fee_history_ai","q_fee_history_au","q_fee_history_ad","q_membership_events_ai","q_membership_events_au","q_membership_events_ad"};
            for(String n:names)d.execSQL("DROP TRIGGER IF EXISTS "+n);
            d.execSQL("CREATE TRIGGER q_athletes_ai AFTER INSERT ON athletes WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('SPORCU',CAST(NEW.id AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('EKLENDİ','SPORCU',CAST(NEW.id AS TEXT),COALESCE(NEW.name,'SPORCU')); END");
            d.execSQL("CREATE TRIGGER q_athletes_au AFTER UPDATE ON athletes WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('SPORCU',CAST(NEW.id AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','SPORCU',CAST(NEW.id AS TEXT),COALESCE(NEW.name,'SPORCU')); END");
            d.execSQL("CREATE TRIGGER q_athletes_ad AFTER DELETE ON athletes WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('SPORCU',CAST(OLD.id AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('SİLİNDİ','SPORCU',CAST(OLD.id AS TEXT),COALESCE(OLD.name,'SPORCU')); END");
            d.execSQL("CREATE TRIGGER q_payments_ai AFTER INSERT ON payments WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('ÖDEME',CAST(NEW.athleteId AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('EKLENDİ','ÖDEME',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||NEW.month||'/'||NEW.year); END");
            d.execSQL("CREATE TRIGGER q_payments_au AFTER UPDATE ON payments WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('ÖDEME',CAST(NEW.athleteId AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','ÖDEME',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||NEW.month||'/'||NEW.year); END");
            d.execSQL("CREATE TRIGGER q_payments_ad AFTER DELETE ON payments WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('ÖDEME',CAST(OLD.athleteId AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('SİLİNDİ','ÖDEME',CAST(OLD.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=OLD.athleteId),'SPORCU')||' • '||OLD.month||'/'||OLD.year); END");
            d.execSQL("CREATE TRIGGER q_fee_history_ai AFTER INSERT ON fee_history WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('AİDAT',CAST(NEW.athleteId AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('EKLENDİ','AİDAT',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||NEW.effectiveMonth||'/'||NEW.year); END");
            d.execSQL("CREATE TRIGGER q_fee_history_au AFTER UPDATE ON fee_history WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('AİDAT',CAST(NEW.athleteId AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','AİDAT',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||NEW.effectiveMonth||'/'||NEW.year); END");
            d.execSQL("CREATE TRIGGER q_fee_history_ad AFTER DELETE ON fee_history WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('AİDAT',CAST(OLD.athleteId AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('SİLİNDİ','AİDAT',CAST(OLD.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=OLD.athleteId),'SPORCU')||' • '||OLD.effectiveMonth||'/'||OLD.year); END");
            Cursor x=d.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name='membership_events'",null);boolean has=x.moveToFirst();x.close();if(has){
                d.execSQL("CREATE TRIGGER q_membership_events_ai AFTER INSERT ON membership_events WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('ÜYELİK',CAST(NEW.athleteId AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('EKLENDİ','ÜYELİK',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||COALESCE(NEW.note,NEW.eventType)||' • '||NEW.eventDate); END");
                d.execSQL("CREATE TRIGGER q_membership_events_au AFTER UPDATE ON membership_events WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('ÜYELİK',CAST(NEW.athleteId AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','ÜYELİK',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||COALESCE(NEW.note,NEW.eventType)||' • '||NEW.eventDate); END");
                d.execSQL("CREATE TRIGGER q_membership_events_ad AFTER DELETE ON membership_events WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('ÜYELİK',CAST(OLD.athleteId AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('SİLİNDİ','ÜYELİK',CAST(OLD.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=OLD.athleteId),'SPORCU')||' • '||COALESCE(OLD.note,OLD.eventType)||' • '||OLD.eventDate); END");
            }
        }catch(Exception ignored){}
    }
}
