package com.parion.aidat;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/** v4.4.1 financial-year UI. Profile month cards are never shown for years before 2026. */
public class MainActivityV757 extends MainActivityV756 {
    @Override public void onCreate(Bundle b){ super.onCreate(b); }

    @Override void showProfile(long id){
        super.showProfile(id);
        applyFinancialYearUi757(root,id);
    }

    private void applyFinancialYearUi757(View v,long athleteId){
        if(v==null)return;
        if(v instanceof TextView){
            TextView t=(TextView)v;
            String s=String.valueOf(t.getText());
            if(s.contains("2026 AİDAT HAREKETLERİ") || s.matches("\\d{4} AİDAT HAREKETLERİ")){
                int year=db.activeFinancialYear();
                t.setText(year+" AİDAT HAREKETLERİ  ▾");
                t.setTextColor(BLACK);
                t.setOnClickListener(x->chooseFinancialYear757(athleteId));
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)applyFinancialYearUi757(g.getChildAt(i),athleteId);}
    }

    private void chooseFinancialYear757(long athleteId){
        int now=Math.max(FinancialYear.MIN_YEAR,Calendar.getInstance().get(Calendar.YEAR));
        int max=Math.max(now,db.activeFinancialYear());
        ArrayList<Integer> years=new ArrayList<>();
        for(int y=FinancialYear.MIN_YEAR;y<=max;y++)years.add(y);
        String[] labels=new String[years.size()];
        for(int i=0;i<years.size();i++)labels[i]=years.get(i)+" Aidat Yılı";
        new AlertDialog.Builder(this).setTitle("Aktif Aidat Yılı").setItems(labels,(d,w)->{
            db.setActiveFinancialYear(years.get(w));
            showProfile(athleteId);
        }).setNegativeButton("İPTAL",null).show();
    }

    @Override int paymentColor(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){
        int year=db.activeFinancialYear();
        if("X".equals(marker))return GRAY;
        if("!".equals(marker)||"!!".equals(marker)||amount>0&&fee>0&&amount!=fee)return ORANGE;
        if(isDate(marker))return GREEN;
        if("BURSLU".equals(sibling)||fee==0)return Color.rgb(225,225,225);
        if(!activeMonth(m,start,end,restart))return Color.rgb(230,230,230);
        Calendar now=Calendar.getInstance();int y=now.get(Calendar.YEAR),cm=now.get(Calendar.MONTH)+1;
        if(year>y || (year==y&&m>cm))return Color.WHITE;
        if(year==y&&m==cm)return YELLOW;
        return RED;
    }

    @Override String paymentText(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){
        int year=db.activeFinancialYear();
        if("X".equals(marker))return "ARA VERDİ";
        if("!".equals(marker)||"!!".equals(marker))return money(amount)+" • "+(amount<fee?"EKSİK ÖDEME":amount>fee?"FAZLA ÖDEME":"ÖDENDİ");
        if(isDate(marker))return dateTr(marker)+" • "+money(amount)+(fee>0&&amount!=fee?amount<fee?" • EKSİK":" • FAZLA":" • ÖDENDİ");
        if("BURSLU".equals(sibling)||fee==0)return "BURSLU";
        if(!activeMonth(m,start,end,restart))return "AKTİF DEĞİL";
        Calendar now=Calendar.getInstance();int cy=now.get(Calendar.YEAR),cm=now.get(Calendar.MONTH)+1;
        if(year<cy || (year==cy&&m<cm))return "ÖDEME GECİKTİ";
        if(year==cy&&m==cm)return "ÖDEME DÖNEMİ";
        return "BEKLİYOR";
    }

    @Override boolean activeMonth(int m,String start,String end,String restart){
        int year=db.activeFinancialYear();
        if(year<FinancialYear.MIN_YEAR)return false;
        String first=String.format(Locale.US,"%04d-%02d-01",year,m);
        String last=String.format(Locale.US,"%04d-%02d-31",year,m);
        if(!start.isEmpty()&&start.compareTo(last)>0)return false;
        if(end.isEmpty()||"DEVAM".equalsIgnoreCase(end))return true;
        if(end.compareTo(first)>=0)return true;
        return !restart.isEmpty()&&restart.compareTo(last)<=0;
    }

    @Override void editPayment(long id,int month,int fee,String marker,int amount){
        // Base editor now writes through DbHelper to the selected financial year.
        super.editPayment(id,month,fee,marker,amount);
    }
}
