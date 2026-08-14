package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivityV35 extends MainActivityV34 {
    @Override void showProfile(long id){
        page="PROFILE"; currentAthlete=id; Cursor c=db.athlete(id); if(!c.moveToFirst()){c.close();showAthletes();return;}
        String name=s(c,"name"),photo=s(c,"photo"),cat=s(c,"category"),athleteStatus=s(c,"status"),notes=s(c,"notes"); int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
        String start=s(c,"startDate"),end=s(c,"endDate"),restart=s(c,"restartDate"),sib=s(c,"sibling");
        base("SPORCU PROFİLİ",true); ScrollView sv=scroll(); LinearLayout b=box(sv);
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER_HORIZONTAL); card.setPadding(dp(12),dp(14),dp(12),dp(14)); card.setBackground(round(Color.WHITE,14));
        ImageView av=new ImageView(this); av.setScaleType(ImageView.ScaleType.CENTER_CROP); setAthletePhoto(av,photo); LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(150),dp(180)); ip.gravity=Gravity.CENTER_HORIZONTAL; card.addView(av,ip);
        TextView nm=tv(name,22,BLACK,true); nm.setGravity(Gravity.CENTER); card.addView(nm,new LinearLayout.LayoutParams(-1,-2));
        TextView cs=tv(cat+" • "+athleteStatus,14,statusColor(athleteStatus),true); cs.setGravity(Gravity.CENTER); card.addView(cs,new LinearLayout.LayoutParams(-1,-2));
        TextView bd=tv("Doğum yılı: "+(by>0?by:"—"),13,Color.DKGRAY,false); bd.setGravity(Gravity.CENTER); card.addView(bd); b.addView(card);

        Calendar now=Calendar.getInstance(); int anchor=anchorDay(start); int currentKey=currentCycleKey(now,anchor); int currentFee=expectedFeeAt(id,currentKey/100,currentKey%100,new PayRec("",0));
        b.addView(line("Güncel Aidat",money(currentFee))); b.addView(line("Sporcu Tel",s(c,"phone"))); b.addView(line("Anne",join(s(c,"motherName"),s(c,"motherPhone")))); b.addView(line("Baba",join(s(c,"fatherName"),s(c,"fatherPhone")))); b.addView(line("Kardeş",sib)); b.addView(line("İlk Kayıt",dateTr(start))); b.addView(line("Bitiş / Ara Verme",dateTr(end))); b.addView(line("Yeniden Başlama",dateTr(restart))); if(notes!=null&&!notes.isEmpty())b.addView(line("Özel Not",notes)); c.close();

        Button feeBtn=btn("AİDAT ÜCRETİ / GEÇERLİ AY DÜZENLE"); feeBtn.setOnClickListener(v->editFeePeriod(id)); LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(56)); fp.setMargins(0,dp(10),0,dp(8)); b.addView(feeBtn,fp);

        TextView h=tv("AİDAT DÖNEMLERİ",16,BLACK,true); h.setPadding(dp(10),dp(16),dp(10),dp(8)); b.addView(h);
        HashMap<Integer,PayRec> pays=new HashMap<>(); Cursor p=db.payments(id); while(p.moveToNext()){int y=p.getInt(p.getColumnIndexOrThrow("year")),m=p.getInt(p.getColumnIndexOrThrow("month"));pays.put(y*100+m,new PayRec(s(p,"marker"),p.getInt(p.getColumnIndexOrThrow("amount"))));} p.close();

        int startKey=registrationMonth(start,id,currentKey); int regKey=parseMonthKey(start); if(regKey>0&&regKey>startKey)startKey=regKey; if(startKey>currentKey)startKey=currentKey;
        for(int key=startKey;;key=shiftMonth(key,1)){
            addCycleProfileRow(b,id,key,anchor,start,end,restart,sib,pays,false);
            if(key==currentKey)break;
        }
        int waitingKey=shiftMonth(currentKey,1); addCycleProfileRow(b,id,waitingKey,anchor,start,end,restart,sib,pays,true);

        Button report=btn("AİDAT BİLANÇOSU / PAYLAŞ"); report.setOnClickListener(v->shareReport(id)); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(58));rp.setMargins(0,dp(16),0,dp(8));b.addView(report,rp);
    }

    void addCycleProfileRow(LinearLayout b,long id,int key,int anchor,String start,String end,String restart,String sibling,HashMap<Integer,PayRec> pays,boolean future){
        int yr=key/100,mo=key%100; PayRec r=pays.get(key); if(r==null)r=new PayRec("",0); int expected=expectedFeeAt(id,yr,mo,r); boolean active=activeAt(yr,mo,start,end,restart);
        String status; int color; String detail;
        String period=cycleDateLabel(key,anchor)+" – "+cycleDateLabel(shiftMonth(key,1),anchor);
        if(future){status="BEKLİYOR";color=Color.WHITE;detail=period+" • "+(expected>0?money(expected):"—");}
        else if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;detail=period+" • ARA VERDİ";}
        else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=ORANGE;detail=period+" • "+money(r.amount)+" • "+status;}
        else if(isDate(r.marker)){status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":"FAZLA ÖDEME"):"ÖDENDİ";color=status.equals("ÖDENDİ")?GREEN:ORANGE;detail=period+" • "+dateTr(r.marker)+" • "+money(r.amount)+" • "+status;}
        else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);detail=period+" • AKTİF DEĞİL";}
        else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);detail=period+" • BURSLU";}
        else if(expected==0&&yr<2026){status="VERİ YOK";color=Color.rgb(225,225,225);detail=period+" • VERİ YOK";}
        else {status="ÖDEME DÖNEMİ";color=YELLOW;detail=period+" • "+money(expected)+" • ÖDEME DÖNEMİ";}
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(round(color,9));
        row.addView(tv(monthName(mo)+" "+yr,14,BLACK,true)); row.addView(tv(detail,12,Color.DKGRAY,false));
        if(!future&&yr==2026){final int mm=mo;final int fee=expected;final String mk=r.marker;final int amt=r.amount;row.setOnClickListener(v->editPayment(id,mm,fee,mk,amt));}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(row,lp);
    }
}
