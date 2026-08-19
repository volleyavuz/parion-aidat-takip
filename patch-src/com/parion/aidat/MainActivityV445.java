package com.parion.aidat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivityV445 extends MainActivityV444 {
    private static final String BASE="https://ujjtsemybslznmzadzvk.supabase.co";
    private static final String ANON="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqanRzZW15YnNsem5temFkenZrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY3MzIyMjIsImV4cCI6MjEwMjMwODIyMn0.qZPcYZwAjMJpc2yBB1bdTjA8YguFqr3UY85VuQGQRLE";
    private final ScheduledExecutorService sync445=Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean busy445=new AtomicBoolean(false);

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        sync445.execute(this::installPreciseTriggers445);
        sync445.scheduleWithFixedDelay(this::tick445,3,10,TimeUnit.SECONDS);
    }
    @Override protected void onDestroy(){
        try{sync445.shutdownNow();}catch(Throwable ignored){}
        super.onDestroy();
    }

    private SQLiteDatabase db445(){
        try{return SQLiteDatabase.openDatabase(getDatabasePath("parion_spor_okulu.db").getPath(),null,SQLiteDatabase.OPEN_READWRITE);}catch(Throwable t){return null;}
    }
    private void installPreciseTriggers445(){
        SQLiteDatabase d=db445(); if(d==null)return;
        try{
            d.beginTransaction();
            String[] n={"q_payments_ai","q_payments_au","q_payments_ad","q_fee_history_ai","q_fee_history_au","q_fee_history_ad"};
            for(String x:n)d.execSQL("DROP TRIGGER IF EXISTS "+x);
            d.execSQL("CREATE TRIGGER q_payments_ai AFTER INSERT ON payments WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('ÖDEME',NEW.athleteId||'|'||NEW.year||'|'||NEW.month||'|U'); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('EKLENDİ','ÖDEME',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||NEW.month||'/'||NEW.year); END");
            d.execSQL("CREATE TRIGGER q_payments_au AFTER UPDATE ON payments WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('ÖDEME',NEW.athleteId||'|'||NEW.year||'|'||NEW.month||'|U'); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','ÖDEME',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||NEW.month||'/'||NEW.year); END");
            d.execSQL("CREATE TRIGGER q_payments_ad AFTER DELETE ON payments WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('ÖDEME',OLD.athleteId||'|'||OLD.year||'|'||OLD.month||'|D'); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('SİLİNDİ','ÖDEME',CAST(OLD.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=OLD.athleteId),'SPORCU')||' • '||OLD.month||'/'||OLD.year); END");
            d.execSQL("CREATE TRIGGER q_fee_history_ai AFTER INSERT ON fee_history WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('AİDAT',NEW.athleteId||'|'||NEW.year||'|'||NEW.effectiveMonth||'|U'); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('EKLENDİ','AİDAT',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||NEW.effectiveMonth||'/'||NEW.year); END");
            d.execSQL("CREATE TRIGGER q_fee_history_au AFTER UPDATE ON fee_history WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('AİDAT',NEW.athleteId||'|'||NEW.year||'|'||NEW.effectiveMonth||'|U'); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','AİDAT',CAST(NEW.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=NEW.athleteId),'SPORCU')||' • '||NEW.effectiveMonth||'/'||NEW.year); END");
            d.execSQL("CREATE TRIGGER q_fee_history_ad AFTER DELETE ON fee_history WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('AİDAT',OLD.athleteId||'|'||OLD.year||'|'||OLD.effectiveMonth||'|D'); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('SİLİNDİ','AİDAT',CAST(OLD.athleteId AS TEXT),COALESCE((SELECT name FROM athletes WHERE id=OLD.athleteId),'SPORCU')||' • '||OLD.effectiveMonth||'/'||OLD.year); END");
            d.setTransactionSuccessful();
        }catch(Throwable ignored){}finally{try{d.endTransaction();}catch(Throwable ignored){} try{d.close();}catch(Throwable ignored){}}
    }
    private void tick445(){
        if(!online445()||!busy445.compareAndSet(false,true))return;
        try{for(int i=0;i<12;i++){QueueRow q=next445(); if(q==null)break; boolean ok=process445(q); if(!ok)break; deleteQueue445(q.id); markLog445(q);}}finally{busy445.set(false);}
    }
    private boolean online445(){
        try{ConnectivityManager c=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE); if(c==null||c.getActiveNetwork()==null)return false; NetworkCapabilities n=c.getNetworkCapabilities(c.getActiveNetwork()); return n!=null&&n.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);}catch(Throwable t){return false;}
    }
    private static class QueueRow{long id;String kind,eid;QueueRow(long i,String k,String e){id=i;kind=k;eid=e;}}
    private QueueRow next445(){
        SQLiteDatabase d=db445(); Cursor c=null; try{if(d==null)return null;c=d.rawQuery("SELECT id,kind,entityId FROM sync_queue ORDER BY id LIMIT 1",null);return c.moveToFirst()?new QueueRow(c.getLong(0),c.getString(1),c.getString(2)):null;}catch(Throwable t){return null;}finally{if(c!=null)c.close();if(d!=null)d.close();}
    }
    private void deleteQueue445(long id){SQLiteDatabase d=db445();try{if(d!=null)d.delete("sync_queue","id=?",new String[]{String.valueOf(id)});}catch(Throwable ignored){}finally{if(d!=null)d.close();}}
    private void markLog445(QueueRow q){
        SQLiteDatabase d=db445(); if(d==null)return; try{String athlete=q.eid==null?"":q.eid.split("\\|")[0]; String type="SPORCU".equals(q.kind)?"SPORCU":q.kind; d.execSQL("UPDATE activity_log_local SET cloudSynced=1 WHERE cloudSynced=0 AND entityId=? AND entityType=?",new Object[]{athlete,type});}catch(Throwable ignored){}finally{d.close();}
    }
    private boolean process445(QueueRow q){
        try{
            if(q.kind==null)return true;
            if(q.kind.equals("ÜYELİK"))return true;
            if(q.kind.equals("SPORCU"))return syncAthlete445(Long.parseLong(q.eid));
            if(q.kind.equals("ÖDEME"))return syncPayment445(q.eid);
            if(q.kind.equals("AİDAT"))return syncFee445(q.eid);
            return true;
        }catch(Throwable t){return false;}
    }
    private boolean syncAthlete445(long id)throws Exception{
        SQLiteDatabase d=db445(); Cursor c=null;
        try{
            if(d==null)return false;c=d.rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});
            if(!c.moveToFirst())return rpc445("parion_delete_mobile_athlete",new JSONObject().put("p_legacy_id",id));
            String deleted=s445(c,"deletedAt",""); if(deleted.trim().length()>0)return rpc445("parion_delete_mobile_athlete",new JSONObject().put("p_legacy_id",id));
            JSONObject p=new JSONObject(); p.put("legacy_id",id); p.put("seq",i445(c,"seq",0)); p.put("name",s445(c,"name","")); p.put("birth_year",i445(c,"birthYear",0)); p.put("birth_date",s445(c,"birthDate","")); p.put("category",s445(c,"category","")); p.put("status",s445(c,"status","AKTİF")); p.put("monthly_fee",i445(c,"monthlyFee",0)); p.put("sibling",s445(c,"sibling","")); p.put("phone",s445(c,"phone","")); p.put("mother_name",s445(c,"motherName","")); p.put("mother_phone",s445(c,"motherPhone","")); p.put("father_name",s445(c,"fatherName","")); p.put("father_phone",s445(c,"fatherPhone","")); p.put("start_date",s445(c,"startDate","")); p.put("end_date",s445(c,"endDate","")); p.put("restart_date",s445(c,"restartDate","")); p.put("notes",s445(c,"notes","")); p.put("tshirt_qty",i445(c,"tshirtQty",0)); p.put("tshirt_paid",i445(c,"tshirtPaid",0)); p.put("tracksuit_qty",i445(c,"tracksuitQty",0)); p.put("tracksuit_paid",i445(c,"tracksuitPaid",0)); p.put("tckn",s445(c,"tckn","")); p.put("summer_call",i445(c,"summerCall",0)!=0); p.put("winter_call",i445(c,"winterCall",0)!=0);
            return rpc445("parion_upsert_mobile_athlete",new JSONObject().put("p",p));
        }finally{if(c!=null)c.close();if(d!=null)d.close();}
    }
    private boolean syncPayment445(String eid)throws Exception{
        String[] a=eid.split("\\|"); if(a.length<4)return syncAllPayments445(Long.parseLong(a[0])); long id=Long.parseLong(a[0]);int y=Integer.parseInt(a[1]),m=Integer.parseInt(a[2]); if("D".equals(a[3]))return rpc445("parion_delete_mobile_payment",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m));
        SQLiteDatabase d=db445();Cursor c=null;try{c=d.rawQuery("SELECT marker,amount FROM payments WHERE athleteId=? AND year=? AND month=?",new String[]{""+id,""+y,""+m}); if(!c.moveToFirst())return rpc445("parion_delete_mobile_payment",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m)); return rpc445("parion_upsert_mobile_payment",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m).put("p_marker",nz(c.getString(0))).put("p_amount",c.getInt(1)));}finally{if(c!=null)c.close();if(d!=null)d.close();}
    }
    private boolean syncFee445(String eid)throws Exception{
        String[] a=eid.split("\\|"); if(a.length<4)return syncAllFees445(Long.parseLong(a[0])); long id=Long.parseLong(a[0]);int y=Integer.parseInt(a[1]),m=Integer.parseInt(a[2]); if("D".equals(a[3]))return rpc445("parion_delete_mobile_fee",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m));
        SQLiteDatabase d=db445();Cursor c=null;try{c=d.rawQuery("SELECT fee FROM fee_history WHERE athleteId=? AND year=? AND effectiveMonth=?",new String[]{""+id,""+y,""+m}); if(!c.moveToFirst())return rpc445("parion_delete_mobile_fee",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m)); return rpc445("parion_upsert_mobile_fee",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m).put("p_fee",c.getInt(0)));}finally{if(c!=null)c.close();if(d!=null)d.close();}
    }
    private boolean syncAllPayments445(long id)throws Exception{SQLiteDatabase d=db445();Cursor c=null;try{c=d.rawQuery("SELECT year,month,marker,amount FROM payments WHERE athleteId=?",new String[]{""+id});while(c.moveToNext())if(!rpc445("parion_upsert_mobile_payment",new JSONObject().put("p_legacy_id",id).put("p_year",c.getInt(0)).put("p_month",c.getInt(1)).put("p_marker",nz(c.getString(2))).put("p_amount",c.getInt(3))))return false;return true;}finally{if(c!=null)c.close();if(d!=null)d.close();}}
    private boolean syncAllFees445(long id)throws Exception{SQLiteDatabase d=db445();Cursor c=null;try{c=d.rawQuery("SELECT year,effectiveMonth,fee FROM fee_history WHERE athleteId=?",new String[]{""+id});while(c.moveToNext())if(!rpc445("parion_upsert_mobile_fee",new JSONObject().put("p_legacy_id",id).put("p_year",c.getInt(0)).put("p_month",c.getInt(1)).put("p_fee",c.getInt(2))))return false;return true;}finally{if(c!=null)c.close();if(d!=null)d.close();}}
    private boolean rpc445(String fn,JSONObject body)throws Exception{
        HttpURLConnection h=(HttpURLConnection)new URL(BASE+"/rest/v1/rpc/"+fn).openConnection(); h.setConnectTimeout(6000);h.setReadTimeout(8000);h.setRequestMethod("POST");h.setDoOutput(true);h.setRequestProperty("Content-Type","application/json");h.setRequestProperty("apikey",ANON);h.setRequestProperty("Authorization","Bearer "+token445()); byte[] b=body.toString().getBytes(StandardCharsets.UTF_8); try(OutputStream o=h.getOutputStream()){o.write(b);} int code=h.getResponseCode(); try{InputStream in=code>=200&&code<300?h.getInputStream():h.getErrorStream(); if(in!=null){byte[] buf=new byte[512];while(in.read(buf)!=-1){}}}catch(Throwable ignored){} h.disconnect(); return code>=200&&code<300;
    }
    private String token445(){try{android.content.SharedPreferences p=getSharedPreferences("parion_cloud_session",MODE_PRIVATE);String t=p.getString("access_token","");return t==null||t.trim().isEmpty()?ANON:t;}catch(Throwable t){return ANON;}}
    private static String nz(String s){return s==null?"":s;}
    private static String s445(Cursor c,String n,String def){try{int i=c.getColumnIndex(n);return i<0||c.isNull(i)?def:nz(c.getString(i));}catch(Throwable t){return def;}}
    private static int i445(Cursor c,String n,int def){try{int i=c.getColumnIndex(n);return i<0||c.isNull(i)?def:c.getInt(i);}catch(Throwable t){return def;}}
}
