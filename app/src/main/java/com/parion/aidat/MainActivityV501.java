package com.parion.aidat;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;
import org.json.*;
import java.io.IOException;
import java.util.*;

/** v3.9.1 - resilient CLOUD -> LOCAL restore. Cloud writes remain blocked by V500. */
public class MainActivityV501 extends MainActivityV500 {
    private volatile boolean restoreRunning501=false;

    @Override public void onCreate(Bundle b){ super.onCreate(b); }

    @Override void syncFromCloud(boolean announce){
        if(restoreRunning501 || syncing) return;
        if(cloudPrefs==null) return;
        final String token=cloudPrefs.getString("access_token","");
        if(token.isEmpty()) return;
        restoreRunning501=true; syncing=true;
        if(announce) toast("BULUT → YEREL: ANA VERİLER ALINIYOR...");
        new Thread(()->{
            String stage="BAŞLANGIÇ";
            try{
                stage="SPORCULAR";
                HttpResult ar=getAuthed("/rest/v1/mobile_athletes?select=*&order=legacy_id.asc");
                if(ar.code==401 && refreshSession()) ar=getAuthed("/rest/v1/mobile_athletes?select=*&order=legacy_id.asc");
                require2xx501(ar,"SPORCU");

                stage="ÖDEMELER";
                HttpResult pr=getAuthed("/rest/v1/mobile_payments_legacy?select=*&order=legacy_id.asc,year.asc,month.asc");
                require2xx501(pr,"ÖDEME");

                stage="AİDATLAR";
                HttpResult fr=getAuthed("/rest/v1/mobile_fee_history?select=*&order=legacy_id.asc,year.asc,month.asc");
                require2xx501(fr,"AİDAT");

                JSONArray athletes=new JSONArray(ar.body);
                JSONArray payments=new JSONArray(pr.body);
                JSONArray fees=new JSONArray(fr.body);

                stage="YEREL VERİTABANI";
                int[] counts=replaceCore501(athletes,payments,fees);

                // Media is deliberately non-fatal. Core data must survive a media/RLS/path issue.
                String mediaState="MEDYA BEKLİYOR";
                try{
                    HttpResult mr=getAuthed("/rest/v1/athletes?select=legacy_id,photo_path,registration_form_path&order=legacy_id.asc");
                    require2xx501(mr,"MEDYA");
                    applyMedia501(new JSONArray(mr.body));
                    mediaState="MEDYA TAMAM";
                }catch(Exception mediaError){
                    mediaState="MEDYA BEKLİYOR: "+short501(mediaError);
                }

                final String ms=mediaState;
                getSharedPreferences("parion_v390_recovery",MODE_PRIVATE).edit().putLong("restore_at",System.currentTimeMillis()).apply();
                runOnUiThread(()->{
                    restoreRunning501=false; syncing=false;
                    toast("BULUT → YEREL TAMAM: "+counts[0]+" SPORCU • "+counts[1]+" ÖDEME • "+counts[2]+" AİDAT • "+ms);
                    showHome();
                });
            }catch(Exception e){
                final String msg=stage+": "+short501(e);
                runOnUiThread(()->{
                    restoreRunning501=false; syncing=false;
                    Toast.makeText(this,"BULUT → YEREL HATA • "+msg,Toast.LENGTH_LONG).show();
                });
            }
        },"parion-cloud-to-local-501").start();
    }

    private void require2xx501(HttpResult r,String label)throws IOException{
        if(r==null) throw new IOException(label+" YANIT YOK");
        if(r.code<200||r.code>=300){
            String body=r.body==null?"":r.body.replace('\n',' ');
            if(body.length()>100) body=body.substring(0,100);
            throw new IOException(label+" HTTP "+r.code+(body.isEmpty()?"":" • "+body));
        }
    }

    private int[] replaceCore501(JSONArray athletes,JSONArray payments,JSONArray fees)throws Exception{
        SQLiteDatabase d=db.getWritableDatabase();
        d.beginTransaction();
        try{
            d.delete("payments",null,null);
            d.delete("fee_history",null,null);
            d.delete("athletes",null,null);
            int ac=0,pc=0,fc=0;
            for(int i=0;i<athletes.length();i++){
                JSONObject a=athletes.getJSONObject(i);
                long id=a.optLong("legacy_id",-1); if(id<=0) continue;
                ContentValues v=new ContentValues();
                v.put("id",id);
                putInt501(v,"seq",a,"seq"); putInt501(v,"birthYear",a,"birth_year");
                putText501(v,"name",a,"name"); putText501(v,"category",a,"category"); putText501(v,"status",a,"status");
                putInt501(v,"monthlyFee",a,"monthly_fee"); putText501(v,"sibling",a,"sibling");
                putInt501(v,"tshirtQty",a,"tshirt_qty"); putInt501(v,"tshirtPaid",a,"tshirt_paid");
                putInt501(v,"tracksuitQty",a,"tracksuit_qty"); putInt501(v,"tracksuitPaid",a,"tracksuit_paid");
                putText501(v,"notes",a,"notes"); putText501(v,"phone",a,"phone");
                putText501(v,"motherName",a,"mother_name"); putText501(v,"motherPhone",a,"mother_phone");
                putText501(v,"fatherName",a,"father_name"); putText501(v,"fatherPhone",a,"father_phone");
                putText501(v,"startDate",a,"start_date"); putText501(v,"endDate",a,"end_date"); putText501(v,"restartDate",a,"restart_date");
                String photo=a.optString("photo",""); if("null".equalsIgnoreCase(photo)) photo="";
                v.put("photo",photo.isEmpty()?"NONE":(photo.startsWith("CLOUD:")?photo:"CLOUD:"+photo));
                long res=d.insert("athletes",null,v);
                if(res<0) throw new IOException("SPORCU SQLITE INSERT id="+id);
                ac++;
            }
            for(int i=0;i<payments.length();i++){
                JSONObject p=payments.getJSONObject(i); long id=p.optLong("legacy_id",-1); int y=p.optInt("year",0),m=p.optInt("month",0);
                if(id<=0||y<=0||m<1||m>12) continue;
                ContentValues v=new ContentValues(); v.put("athleteId",id);v.put("year",y);v.put("month",m);v.put("marker",p.optString("marker",""));v.put("amount",p.optInt("amount",0));
                if(d.insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE)<0) throw new IOException("ÖDEME SQLITE id="+id+" m="+m);
                pc++;
            }
            for(int i=0;i<fees.length();i++){
                JSONObject f=fees.getJSONObject(i); long id=f.optLong("legacy_id",-1); int y=f.optInt("year",0),m=f.optInt("month",0);
                if(id<=0||y<=0||m<1||m>12) continue;
                ContentValues v=new ContentValues(); v.put("athleteId",id);v.put("year",y);v.put("effectiveMonth",m);v.put("fee",f.optInt("fee",0));
                if(d.insertWithOnConflict("fee_history",null,v,SQLiteDatabase.CONFLICT_REPLACE)<0) throw new IOException("AİDAT SQLITE id="+id+" m="+m);
                fc++;
            }
            d.setTransactionSuccessful();
            return new int[]{ac,pc,fc};
        }finally{ d.endTransaction(); }
    }

    private void applyMedia501(JSONArray media)throws Exception{
        HashMap<Long,String> photos=new HashMap<>(), forms=new HashMap<>();
        SQLiteDatabase d=db.getWritableDatabase();
        d.beginTransaction();
        try{
            for(int i=0;i<media.length();i++){
                JSONObject o=media.getJSONObject(i); long id=o.optLong("legacy_id",-1); if(id<=0) continue;
                String p=o.optString("photo_path",""); if("null".equalsIgnoreCase(p)) p="";
                String f=o.optString("registration_form_path",""); if("null".equalsIgnoreCase(f)) f="";
                if(!p.isEmpty()){
                    photos.put(id,p); ContentValues cv=new ContentValues(); cv.put("photo","CLOUD:"+p); d.update("athletes",cv,"id=?",new String[]{String.valueOf(id)});
                }
                if(!f.isEmpty()) forms.put(id,f);
            }
            d.setTransactionSuccessful();
        }finally{ d.endTransaction(); }
        photoMap413().clear(); photoMap413().putAll(photos);
        formMap413().clear(); formMap413().putAll(forms);
    }

    private void putText501(ContentValues v,String col,JSONObject o,String key){ if(!o.has(key)||o.isNull(key)) return; v.put(col,o.optString(key,"")); }
    private void putInt501(ContentValues v,String col,JSONObject o,String key){ if(!o.has(key)||o.isNull(key)) return; v.put(col,o.optInt(key,0)); }
    private String short501(Exception e){ String s=e.getMessage(); if(s==null||s.trim().isEmpty()) s=e.getClass().getSimpleName(); s=s.replace('\n',' '); return s.length()>150?s.substring(0,150):s; }
}
