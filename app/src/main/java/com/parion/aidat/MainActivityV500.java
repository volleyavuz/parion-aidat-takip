package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.IOException;
import java.util.*;

/**
 * Parion v3.9.0 recovery mode.
 *
 * Safety rules for this build:
 * 1) Cloud is the source of truth on sync: CLOUD -> LOCAL only.
 * 2) Auth POSTs are allowed, every other request() write is blocked locally.
 * 3) Athlete/payment/fee tables are rebuilt from Supabase mobile views.
 * 4) Cloud media paths are written locally as CLOUD:<path>, preventing legacy
 *    USER: photo auto-upload paths from firing after recovery.
 */
public class MainActivityV500 extends MainActivityV416 {
    private static final String RESTORE_PREF="parion_v390_recovery";
    private static final String RESTORE_AT="restore_at";
    private volatile boolean restoreReady500=false;
    private volatile boolean restoreRunning500=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        try{
            long at=getSharedPreferences(RESTORE_PREF,MODE_PRIVATE).getLong(RESTORE_AT,0L);
            restoreReady500=at>0;
        }catch(Exception ignored){}
    }

    /** Hard network safety gate. Login/refresh must remain POST-capable. */
    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        String m=method==null?"":method.toUpperCase(Locale.ROOT);
        if("GET".equals(m))return super.request(method,url,body,bearer);
        if(url!=null&&url.contains("/auth/v1/"))return super.request(method,url,body,bearer);
        return new HttpResult(403,"{\"error\":\"PARION_READ_ONLY_CLOUD_TO_LOCAL\"}");
    }

    /** Disable automatic orphan repair that could write athlete/media data to cloud. */
    @Override protected void repairNewAthleteMedia413(long id){ }
    @Override protected void ensureAthleteCloud413(long id,String token){ }

    @Override void syncFromCloud(boolean announce){
        if(restoreRunning500||syncing)return;
        if(cloudPrefs==null)return;
        final String token=cloudPrefs.getString("access_token","");
        if(token.isEmpty())return;

        // Auto-start sync is limited after a successful restore; manual refresh always runs.
        if(!announce){
            long at=getSharedPreferences(RESTORE_PREF,MODE_PRIVATE).getLong(RESTORE_AT,0L);
            if(at>0 && System.currentTimeMillis()-at < 6L*60L*60L*1000L){restoreReady500=true;return;}
        }

        restoreRunning500=true;syncing=true;
        if(announce)toast("BULUTTAN YERELE VERİLER ALINIYOR...");
        new Thread(()->{
            try{
                HttpResult ar=getAuthed("/rest/v1/mobile_athletes?select=*");
                if(ar.code==401&&refreshSession())ar=getAuthed("/rest/v1/mobile_athletes?select=*");
                if(ar.code<200||ar.code>=300)throw new IOException("SPORCU "+ar.code);

                HttpResult pr=getAuthed("/rest/v1/mobile_payments_legacy?select=*");
                if(pr.code<200||pr.code>=300)throw new IOException("ÖDEME "+pr.code);

                HttpResult fr=getAuthed("/rest/v1/mobile_fee_history?select=*");
                if(fr.code<200||fr.code>=300)throw new IOException("AİDAT "+fr.code);

                HttpResult mr=getAuthed("/rest/v1/athletes?select=legacy_id,photo_path,registration_form_path");
                if(mr.code<200||mr.code>=300)throw new IOException("MEDYA "+mr.code);

                JSONArray athletes=new JSONArray(ar.body);
                JSONArray payments=new JSONArray(pr.body);
                JSONArray fees=new JSONArray(fr.body);
                JSONArray media=new JSONArray(mr.body);
                int[] n=replaceLocalFromCloud500(athletes,payments,fees,media);

                long now=System.currentTimeMillis();
                getSharedPreferences(RESTORE_PREF,MODE_PRIVATE).edit().putLong(RESTORE_AT,now).apply();
                restoreReady500=true;
                runOnUiThread(()->{
                    restoreRunning500=false;syncing=false;
                    toast("BULUT → YEREL TAMAMLANDI: "+n[0]+" SPORCU • "+n[1]+" ÖDEME • "+n[2]+" AİDAT");
                    showHome();
                });
            }catch(Exception e){
                runOnUiThread(()->{
                    restoreRunning500=false;syncing=false;restoreReady500=false;
                    toast("BULUTTAN GERİ YÜKLEME HATASI: "+shortMsg(e));
                });
            }
        },"parion-cloud-to-local-500").start();
    }

    private int[] replaceLocalFromCloud500(JSONArray athletes,JSONArray payments,JSONArray fees,JSONArray media)throws Exception{
        HashMap<Long,String> photos=new HashMap<>();
        HashMap<Long,String> forms=new HashMap<>();
        for(int i=0;i<media.length();i++){
            JSONObject o=media.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;
            String p=o.optString("photo_path","");if("null".equalsIgnoreCase(p))p="";
            String f=o.optString("registration_form_path","");if("null".equalsIgnoreCase(f))f="";
            if(!p.isEmpty())photos.put(id,p);if(!f.isEmpty())forms.put(id,f);
        }

        SQLiteDatabase d=db.getWritableDatabase();
        d.beginTransaction();
        try{
            d.delete("payments",null,null);
            d.delete("fee_history",null,null);
            d.delete("athletes",null,null);

            int ac=0,pc=0,fc=0;
            for(int i=0;i<athletes.length();i++){
                JSONObject a=athletes.getJSONObject(i);long id=a.optLong("legacy_id",-1);if(id<=0)continue;
                ContentValues v=new ContentValues();v.put("id",id);
                put500(v,"seq",a,"seq");put500(v,"birthYear",a,"birth_year");put500(v,"name",a,"name");put500(v,"category",a,"category");put500(v,"status",a,"status");put500(v,"monthlyFee",a,"monthly_fee");put500(v,"sibling",a,"sibling");
                put500(v,"tshirtQty",a,"tshirt_qty");put500(v,"tshirtPaid",a,"tshirt_paid");put500(v,"tracksuitQty",a,"tracksuit_qty");put500(v,"tracksuitPaid",a,"tracksuit_paid");put500(v,"notes",a,"notes");put500(v,"phone",a,"phone");put500(v,"motherName",a,"mother_name");put500(v,"motherPhone",a,"mother_phone");put500(v,"fatherName",a,"father_name");put500(v,"fatherPhone",a,"father_phone");put500(v,"startDate",a,"start_date");put500(v,"endDate",a,"end_date");put500(v,"restartDate",a,"restart_date");
                if(hasColumn500(d,"birthDate"))put500(v,"birthDate",a,"birth_date");
                if(hasColumn500(d,"tckn"))put500(v,"tckn",a,"tckn");
                if(hasColumn500(d,"summerCall"))putBool500(v,"summerCall",a,"summer_call");
                if(hasColumn500(d,"winterCall"))putBool500(v,"winterCall",a,"winter_call");
                String pp=photos.get(id);if(pp==null||pp.isEmpty())pp=a.optString("photo","");
                v.put("photo",pp==null||pp.trim().isEmpty()?"NONE":"CLOUD:"+pp.trim());
                d.insertOrThrow("athletes",null,v);ac++;
            }

            for(int i=0;i<payments.length();i++){
                JSONObject p=payments.getJSONObject(i);long id=p.optLong("legacy_id",-1);int y=p.optInt("year",0),m=p.optInt("month",0);if(id<=0||y<=0||m<1||m>12)continue;
                ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",y);v.put("month",m);v.put("marker",p.optString("marker",""));v.put("amount",p.optInt("amount",0));
                d.insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE);pc++;
            }

            for(int i=0;i<fees.length();i++){
                JSONObject f=fees.getJSONObject(i);long id=f.optLong("legacy_id",-1);int y=f.optInt("year",0),m=f.optInt("month",0);if(id<=0||y<=0||m<1||m>12)continue;
                ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",y);v.put("effectiveMonth",m);v.put("fee",f.optInt("fee",0));
                d.insertWithOnConflict("fee_history",null,v,SQLiteDatabase.CONFLICT_REPLACE);fc++;
            }

            d.setTransactionSuccessful();
            // Keep V405/V416 media UI immediately consistent with the restored snapshot.
            photoMap413().clear();photoMap413().putAll(photos);
            formMap413().clear();formMap413().putAll(forms);
            return new int[]{ac,pc,fc};
        }finally{d.endTransaction();}
    }

    private boolean hasColumn500(SQLiteDatabase d,String col){
        Cursor c=null;try{c=d.rawQuery("PRAGMA table_info(athletes)",null);while(c.moveToNext())if(col.equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name"))))return true;}catch(Exception ignored){}finally{if(c!=null)c.close();}return false;
    }
    private void put500(ContentValues v,String col,JSONObject o,String key){if(o.isNull(key))return;Object x=o.opt(key);if(x instanceof Number)v.put(col,((Number)x).intValue());else v.put(col,o.optString(key,""));}
    private void putBool500(ContentValues v,String col,JSONObject o,String key){if(o.isNull(key))return;v.put(col,o.optBoolean(key,false)?1:0);}

    @Override void showCloudMenu(){
        String[] items={"BULUTTAN YERELE YENİLE","OTURUMU KAPAT"};
        new android.app.AlertDialog.Builder(this).setTitle("ONLINE HESAP • YAZMA KAPALI").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    @Override void showHome(){
        super.showHome();
        if(cloudPrefs==null)return;
        TextView mode=tv("☁ BULUT → YEREL • BULUTA YAZMA KAPALI",11,Color.rgb(120,70,0),true);
        mode.setGravity(Gravity.CENTER);mode.setPadding(dp(6),dp(4),dp(6),dp(6));
        root.addView(mode,Math.min(1,root.getChildCount()));
        protectWriteUi500(root);
    }

    @Override void showProfile(long id){
        if(!restoreReady500&&cloudPrefs!=null&&!cloudPrefs.getString("access_token","").isEmpty()){
            toast("ÖNCE BULUTTAN YERELE GERİ YÜKLEME TAMAMLANMALI.");syncFromCloud(true);return;
        }
        super.showProfile(id);protectWriteUi500(root);
    }

    @Override void form(long id){
        if(!restoreReady500&&cloudPrefs!=null&&!cloudPrefs.getString("access_token","").isEmpty()){
            toast("ÖNCE BULUTTAN YERELE GERİ YÜKLEME TAMAMLANMALI.");syncFromCloud(true);return;
        }
        super.form(id);protectWriteUi500(root);
    }

    private void protectWriteUi500(View v){
        if(v instanceof Button){
            Button b=(Button)v;String s=String.valueOf(b.getText()).toUpperCase(new Locale("tr","TR"));
            if((s.contains("FOTOĞRAF")||s.contains("KAYIT FORMU"))&&(s.contains("EKLE")||s.contains("DEĞİŞTİR")||s.contains("GÜNCELLE")||s.contains("SİL"))){
                b.setOnClickListener(x->toast("v3.9.0 KURTARMA MODU: BULUTA MEDYA YAZMA KAPALI."));
            }
        }
        if(v instanceof TextView){
            TextView t=(TextView)v;String s=String.valueOf(t.getText()).toUpperCase(new Locale("tr","TR"));
            if(s.contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")||s.contains("KAYIT FORMU OLMAYAN AKTİF SPORCULAR")){
                View p=(View)t.getParent();if(p!=null)p.setOnClickListener(x->toast("v3.9.0 KURTARMA MODU: BULUTA MEDYA YAZMA KAPALI."));
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)protectWriteUi500(g.getChildAt(i));}
    }
}
