package com.parion.aidat;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class DbHelper426 extends DbHelper {
    public DbHelper426(android.content.Context c){ super(c); }

    @Override public void updatePayment(long athleteId,int month,String marker,int amount){
        SQLiteDatabase d=getWritableDatabase();
        ContentValues v=new ContentValues();v.put("marker",marker);v.put("amount",amount);
        int n=d.update("payments",v,"athleteId=? AND year=? AND month=?",new String[]{String.valueOf(athleteId),"2026",String.valueOf(month)});
        if(n==0){v.put("athleteId",athleteId);v.put("year",2026);v.put("month",month);d.insert("payments",null,v);}
    }

    @Override public void setFeeFromMonth(long athleteId,int month,int fee){
        SQLiteDatabase d=getWritableDatabase();
        ContentValues v=new ContentValues();v.put("fee",fee);
        int n=d.update("fee_history",v,"athleteId=? AND year=? AND effectiveMonth=?",new String[]{String.valueOf(athleteId),"2026",String.valueOf(month)});
        if(n==0){v.put("athleteId",athleteId);v.put("year",2026);v.put("effectiveMonth",month);d.insert("fee_history",null,v);}
        java.util.Calendar now=java.util.Calendar.getInstance();
        if(now.get(java.util.Calendar.YEAR)>2026 || (now.get(java.util.Calendar.YEAR)==2026 && month<=now.get(java.util.Calendar.MONTH)+1)){
            ContentValues a=new ContentValues();a.put("monthlyFee",fee);d.update("athletes",a,"id=?",new String[]{String.valueOf(athleteId)});
        }
    }
}
