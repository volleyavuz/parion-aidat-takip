package com.parion.aidat;

import android.database.Cursor;
import android.graphics.*;
import java.util.*;

public class MainActivityV34 extends MainActivityV33 {
    @Override void createRollingReport(long id,int monthCount,String filterName){
        Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return;}
        String name=s(a,"name"),category=s(a,"category"),sibling=s(a,"sibling"),start=s(a,"startDate"),end=s(a,"endDate"),restart=s(a,"restartDate");
        int by=a.getInt(a.getColumnIndexOrThrow("birthYear"));a.close();

        HashMap<Integer,PayRec> pays=new HashMap<>();Cursor p=db.payments(id);while(p.moveToNext()){
            int yy=p.getInt(p.getColumnIndexOrThrow("year")),mm=p.getInt(p.getColumnIndexOrThrow("month"));
            pays.put(yy*100+mm,new PayRec(s(p,"marker"),p.getInt(p.getColumnIndexOrThrow("amount"))));
        }p.close();

        Calendar now=Calendar.getInstance();int anchor=anchorDay(start);int currentKey=currentCycleKey(now,anchor);int waitingKey=shiftMonth(currentKey,1);
        int startKey;
        if(monthCount>0)startKey=shiftMonth(currentKey,-(monthCount-1));
        else startKey=registrationMonth(start,id,currentKey);
        int regKey=parseMonthKey(start);if(regKey>0&&regKey>startKey)startKey=regKey;
        if(startKey>currentKey)startKey=currentKey;

        ArrayList<Integer> months=new ArrayList<>();for(int k=startKey;;k=shiftMonth(k,1)){months.add(k);if(k==currentKey)break;if(months.size()>240)break;}months.add(waitingKey);
        int rows=months.size();int W=1400,rowH=86,H=450+rows*rowH+260;Bitmap bm=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.WHITE);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);

        q.setTypeface(Typeface.DEFAULT_BOLD);q.setColor(BLACK);q.setTextSize(52);c.drawText("PARION VOLEYBOL AKADEMİSİ",60,78,q);
        q.setColor(GOLD);q.setTextSize(38);c.drawText("AİDAT BİLANÇOSU",60,132,q);
        q.setColor(BLACK);q.setTextSize(28);c.drawText("Sporcu: "+name,60,192,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(23);c.drawText("Doğum Yılı: "+by+"   Grup: "+category+"   Filtre: "+filterName,60,232,q);
        c.drawText("Dönem: "+keyLabel(startKey)+" – "+keyLabel(waitingKey),60,268,q);
        q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);q.setColor(Color.DKGRAY);c.drawText("AY",60,330,q);c.drawText("ÖDEME TARİHİ",300,330,q);c.drawText("BEKLENEN",610,330,q);c.drawText("ÖDENEN",830,330,q);c.drawText("DURUM",1050,330,q);
        q.setStrokeWidth(2);c.drawLine(60,350,W-60,350,q);

        int y=380,totalPaid=0,totalExpected=0;
        for(int key:months){int yr=key/100,mo=key%100;boolean future=(key==waitingKey);PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeAt(yr,mo,start,end,restart);String status;int color;String date="—";
            if(future){status="BEKLİYOR";color=Color.WHITE;}
            else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;expected=0;}
            else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=ORANGE;}
            else if(isDate(r.marker)){date=dateTr(r.marker);status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?GREEN:ORANGE;}
            else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);expected=0;}
            else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);expected=0;}
            else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);}
            else {status="ÖDEME DÖNEMİ";color=YELLOW;}

            if(!future&&expected>0&&active&&!"X".equals(r.marker))totalExpected+=expected;if(!future&&r.amount>0)totalPaid+=r.amount;
            q.setColor(color);c.drawRoundRect(48,y-38,W-48,y+34,16,16,q);q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText(monthName(mo)+" "+yr,60,y,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(21);c.drawText(date,300,y,q);c.drawText(expected>0?money(expected):"—",610,y,q);c.drawText(!future&&r.amount>0?money(r.amount):"—",830,y,q);drawFit(c,q,status,1050,y,280);y+=rowH;
        }

        q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(27);c.drawText("TOPLAM BEKLENEN: "+money(totalExpected),60,y+35,q);c.drawText("TOPLAM ÖDENEN: "+money(totalPaid),520,y+35,q);int fark=totalPaid-totalExpected;q.setColor(fark<0?RED:fark>0?ORANGE:GREEN);c.drawText("FARK: "+(fark>0?"+":"")+money(fark),980,y+35,q);

        String from=cycleDateLabel(waitingKey,anchor),to=cycleDateLabel(shiftMonth(waitingKey,1),anchor);
        q.setColor(BLACK);q.setTypeface(Typeface.DEFAULT_BOLD);q.setTextSize(22);c.drawText("Sayın Velimiz,",60,y+92,q);q.setTypeface(Typeface.DEFAULT);q.setTextSize(20);c.drawText(monthName(waitingKey%100)+" ayı aidat ödeme aralığınız "+from+" – "+to+"'dir.",60,y+126,q);c.drawText("Aidat takibiniz sporcumuzun kulübe başlangıç gününe göre aylık olarak hesaplanmaktadır.",60,y+158,q);
        saveAndShare(bm,name,filterName);
    }

    int anchorDay(String start){try{if(start!=null&&start.matches("\\d{4}-\\d{2}-\\d{2}")){int d=Integer.parseInt(start.substring(8,10));if(d>=1&&d<=31)return d;}}catch(Exception ignored){}return 1;}
    int currentCycleKey(Calendar now,int anchor){int key=now.get(Calendar.YEAR)*100+now.get(Calendar.MONTH)+1;Calendar due=cycleDate(key,anchor);return now.before(due)?shiftMonth(key,-1):key;}
    Calendar cycleDate(int key,int anchor){Calendar c=Calendar.getInstance();c.clear();int yr=key/100,mo=key%100;c.set(yr,mo-1,1);int max=c.getActualMaximum(Calendar.DAY_OF_MONTH);c.set(Calendar.DAY_OF_MONTH,Math.min(anchor,max));return c;}
    String cycleDateLabel(int key,int anchor){Calendar c=cycleDate(key,anchor);return c.get(Calendar.DAY_OF_MONTH)+" "+monthName(c.get(Calendar.MONTH)+1)+" "+c.get(Calendar.YEAR);}
}
