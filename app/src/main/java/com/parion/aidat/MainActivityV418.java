package com.parion.aidat;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.io.*;
import java.util.*;

public class MainActivityV418 extends MainActivityV417 {
    private volatile boolean sync418=false;

    @Override void showCloudMenu(){
        String[] items={"BULUTTAN GÜNCELLE","YEREL DEĞİŞİKLİKLERİ BULUTA GÖNDER","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("ONLINE HESAP").setItems(items,(d,w)->{
            if(w==0)pullOnly418(true);
            else if(w==1)confirmPush418();
            else {cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    @Override void syncFromCloud(boolean announce){
        // Otomatik ve manuel normal senkronizasyon artık sadece buluttan güvenli yenileme yapar.
        pullOnly418(announce);
    }

    private void pullOnly418(boolean announce){
        if(sync418||cloudPrefs==null)return;
        String token=cloudPrefs.getString("access_token","");if(token.isEmpty())return;
        sync418=true; syncing=true;
        if(announce)toast("BULUTTAN GÜNCELLENİYOR...");
        new Thread(()->{
            try{
                String[] holder={token};
                HttpResult ar=getRetry418("/rest/v1/mobile_athletes?select=*",holder);
                if(ar.code<200||ar.code>=300)throw new IOException("SPORCU VERİSİ ALINAMADI ("+ar.code+")");
                HttpResult pr=getRetry418("/rest/v1/mobile_payments_legacy?select=*",holder);
                if(pr.code<200||pr.code>=300)throw new IOException("ÖDEME VERİSİ ALINAMADI ("+pr.code+")");
                HttpResult fr=getRetry418("/rest/v1/mobile_fee_history?select=*",holder);
                if(fr.code<200||fr.code>=300)throw new IOException("AİDAT GEÇMİŞİ ALINAMADI ("+fr.code+")");
                int[] n=applyPull418(new JSONArray(ar.body),new JSONArray(pr.body),new JSONArray(fr.body));
                runOnUiThread(()->{sync418=false;syncing=false;if(announce)showSyncResult418("BULUTTAN GÜNCELLEME TAMAMLANDI",n[0]+" SPORCU KONTROL EDİLDİ\n"+n[1]+" ÖDEME KAYDI GÜNCELLENDİ\nAİDAT GEÇMİŞİ GÜNCELLENDİ");showHome();});
            }catch(Exception e){String m=e.getMessage();runOnUiThread(()->{sync418=false;syncing=false;if(announce)showSyncResult418("SENKRONİZASYON HATASI",m==null?"BAĞLANTI HATASI":m);});}
        },"parion-pull-418").start();
    }

    private HttpResult getRetry418(String path,String[] token)throws Exception{
        HttpResult r=request("GET",SUPABASE_URL+path,null,token[0]);
        if(r.code==401&&refreshSession()){token[0]=cloudPrefs.getString("access_token","");r=request("GET",SUPABASE_URL+path,null,token[0]);}
        return r;
    }

    private int[] applyPull418(JSONArray athletes,JSONArray payments,JSONArray fees)throws Exception{
        int[] base=applyCloudCache(athletes,payments);
        SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
        try{
            for(int i=0;i<athletes.length();i++){
                JSONObject x=athletes.getJSONObject(i);long id=x.optLong("legacy_id",-1);if(id<=0)continue;
                if(x.has("tckn")&&!x.isNull("tckn")){ContentValues v=new ContentValues();v.put("tckn",x.optString("tckn",""));d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});}
            }
            try{d.delete("fee_history",null,null);for(int i=0;i<fees.length();i++){JSONObject f=fees.getJSONObject(i);ContentValues v=new ContentValues();v.put("athleteId",f.optLong("legacy_id"));v.put("year",f.optInt("year"));v.put("effectiveMonth",f.optInt("month"));v.put("fee",f.optInt("fee"));d.insertWithOnConflict("fee_history",null,v,SQLiteDatabase.CONFLICT_REPLACE);}}catch(Exception ignored){}
            d.setTransactionSuccessful();
        }finally{d.endTransaction();}
        return base;
    }

    private void confirmPush418(){
        new AlertDialog.Builder(this).setTitle("BULUTA GÖNDER").setMessage("Telefondaki sporcu, ödeme ve aidat geçmişi buluta gönderilecek. Mevcut bulut kayıtları silinmez; aynı kayıtlar güncellenir. Devam edilsin mi?").setPositiveButton("EVET, GÖNDER",(d,w)->pushOnly418()).setNegativeButton("VAZGEÇ",null).show();
    }

    private void pushOnly418(){
        if(sync418||cloudPrefs==null)return;String token=cloudPrefs.getString("access_token","");if(token.isEmpty())return;
        sync418=true;syncing=true;toast("YEREL DEĞİŞİKLİKLER BULUTA GÖNDERİLİYOR...");
        new Thread(()->{
            try{
                JSONObject payload=buildSnapshot418();
                HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_mobile_snapshot",payload.toString(),token);
                if(r.code==401&&refreshSession())r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_mobile_snapshot",payload.toString(),cloudPrefs.getString("access_token",""));
                if(r.code<200||r.code>=300)throw new IOException("BULUTA YAZMA BAŞARISIZ ("+r.code+")"+errorBody418(r.body));
                String body=r.body==null?"":r.body;runOnUiThread(()->{sync418=false;syncing=false;showSyncResult418("BULUTA GÖNDERME TAMAMLANDI",summary418(body));});
            }catch(Exception e){String m=e.getMessage();runOnUiThread(()->{sync418=false;syncing=false;showSyncResult418("BULUTA GÖNDERME HATASI",m==null?"BAĞLANTI HATASI":m);});}
        },"parion-push-418").start();
    }

    private JSONObject buildSnapshot418()throws Exception{
        JSONArray athletes=new JSONArray(),payments=new JSONArray(),fees=new JSONArray();SQLiteDatabase d=db.getReadableDatabase();Cursor a;
        try{a=d.rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);}catch(Exception e){a=d.rawQuery("SELECT * FROM athletes",null);}
        while(a.moveToNext()){
            JSONObject o=new JSONObject();long id=a.getLong(a.getColumnIndexOrThrow("id"));o.put("legacy_id",id);
            copy418(o,"seq",a,"seq");copy418(o,"birth_year",a,"birthYear");date418(o,"birth_date",a,"birthDate");copy418(o,"name",a,"name");copy418(o,"category",a,"category");copy418(o,"status",a,"status");copy418(o,"monthly_fee",a,"monthlyFee");copy418(o,"sibling",a,"sibling");copy418(o,"tshirt_qty",a,"tshirtQty");copy418(o,"tshirt_paid",a,"tshirtPaid");copy418(o,"tracksuit_qty",a,"tracksuitQty");copy418(o,"tracksuit_paid",a,"tracksuitPaid");copy418(o,"notes",a,"notes");copy418(o,"phone",a,"phone");copy418(o,"mother_name",a,"motherName");copy418(o,"mother_phone",a,"motherPhone");copy418(o,"father_name",a,"fatherName");copy418(o,"father_phone",a,"fatherPhone");date418(o,"start_date",a,"startDate");date418(o,"end_date",a,"endDate");date418(o,"restart_date",a,"restartDate");copy418(o,"tckn",a,"tckn");athletes.put(o);
        }a.close();
        Cursor p=d.rawQuery("SELECT athleteId,year,month,marker,amount FROM payments",null);while(p.moveToNext()){JSONObject o=new JSONObject();o.put("legacy_id",p.getLong(0));o.put("year",p.getInt(1));o.put("month",p.getInt(2));o.put("marker",p.getString(3)==null?"":p.getString(3));o.put("amount",p.getInt(4));payments.put(o);}p.close();
        try{Cursor f=d.rawQuery("SELECT athleteId,year,effectiveMonth,fee FROM fee_history",null);while(f.moveToNext()){JSONObject o=new JSONObject();o.put("legacy_id",f.getLong(0));o.put("year",f.getInt(1));o.put("month",f.getInt(2));o.put("fee",f.getInt(3));fees.put(o);}f.close();}catch(Exception ignored){}
        return new JSONObject().put("p_athletes",athletes).put("p_payments",payments).put("p_fees",fees);
    }

    private void copy418(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));}
    private void date418(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}String s=c.getString(i);o.put(key,safeDate418(s));}
    private String safeDate418(String raw){String s=raw==null?"":raw.trim();if(s.isEmpty()||"DEVAM".equalsIgnoreCase(s)||"-".equals(s)||"—".equals(s))return "";if(s.matches("\\d{4}-\\d{2}-\\d{2}"))return s;if(s.matches("\\d{2}\\.\\d{2}\\.\\d{4}")){String[] p=s.split("\\.");return p[2]+"-"+p[1]+"-"+p[0];}return "";}
    private String errorBody418(String s){if(s==null||s.trim().isEmpty())return "";try{JSONObject o=new JSONObject(s);String m=o.optString("message",o.optString("error_description",o.optString("hint","")));return m.isEmpty()?"":"\n"+m;}catch(Exception e){return "";}}
    private String summary418(String s){try{JSONObject o=new JSONObject(s);return o.optInt("athletes",0)+" SPORCU\n"+o.optInt("payments",0)+" ÖDEME\n"+o.optInt("fees",0)+" AİDAT GEÇMİŞİ KAYDI BULUTA İŞLENDİ.";}catch(Exception e){return "YEREL VERİLER BULUTA BAŞARIYLA GÖNDERİLDİ.";}}
    private void showSyncResult418(String title,String message){new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("TAMAM",null).show();}
}
