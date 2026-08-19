package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;

public class MainActivityV410 extends MainActivityV408 {
    private long editAthlete410=-1;
    private int profileScroll410=0;
    private boolean smoothRestore410=false;
    private volatile boolean push410=false;

    private ScrollView firstScroll410(View v){
        if(v instanceof ScrollView)return (ScrollView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=firstScroll410(g.getChildAt(i));if(s!=null)return s;}}
        return null;
    }

    @Override void form(long id){
        if(id>0&&"PROFILE".equals(page)){ScrollView s=firstScroll410(root);if(s!=null)profileScroll410=s.getScrollY();}
        editAthlete410=id;
        super.form(id);
    }

    @Override void showProfile(long id){
        boolean fromForm="FORM".equals(page)&&id>0;
        super.showProfile(id);
        if(fromForm||smoothRestore410){
            smoothRestore410=false;
            final ScrollView s=firstScroll410(root);
            if(s!=null){
                root.setAlpha(0f);
                s.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
                    @Override public boolean onPreDraw(){
                        if(s.getViewTreeObserver().isAlive())s.getViewTreeObserver().removeOnPreDrawListener(this);
                        s.scrollTo(0,profileScroll410);root.setAlpha(1f);return true;
                    }
                });
            }
        }
        pushOneAthlete410(id);
    }

    @Override void goBack(){
        if("FORM".equals(page)&&editAthlete410>0){long id=editAthlete410;editAthlete410=-1;smoothRestore410=true;showProfile(id);return;}
        super.goBack();
    }

    @Override void syncFromCloud(boolean announce){
        if(syncing||cloudPrefs==null)return;
        if(cloudPrefs.getString("access_token","").isEmpty())return;
        syncing=true;if(announce)toast("Senkronizasyon başladı...");
        new Thread(()->{
            try{
                String token=cloudPrefs.getString("access_token","");
                pushSnapshot410(token,-1);
                HttpResult ar=request("GET",SUPABASE_URL+"/rest/v1/mobile_athletes?select=*",null,token);
                if(ar.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");ar=request("GET",SUPABASE_URL+"/rest/v1/mobile_athletes?select=*",null,token);}
                if(ar.code<200||ar.code>=300)throw new IOException("Sporcu verisi alınamadı ("+ar.code+")");
                HttpResult pr=request("GET",SUPABASE_URL+"/rest/v1/mobile_payments_legacy?select=*",null,token);
                if(pr.code<200||pr.code>=300)throw new IOException("Ödeme verisi alınamadı ("+pr.code+")");
                HttpResult fr=request("GET",SUPABASE_URL+"/rest/v1/mobile_fee_history?select=*",null,token);
                if(fr.code<200||fr.code>=300)throw new IOException("Aidat geçmişi alınamadı ("+fr.code+")");
                int[] n=applyFullCloud410(new JSONArray(ar.body),new JSONArray(pr.body),new JSONArray(fr.body));
                runOnUiThread(()->{syncing=false;if(announce)toast("Bulut senkronizasyonu tamamlandı: "+n[0]+" sporcu, "+n[1]+" ödeme.");showHome();});
            }catch(Exception e){String m=e.getMessage();runOnUiThread(()->{syncing=false;if(announce)toast("Senkronizasyon hatası: "+(m==null?"bağlantı":m));});}
        },"parion-full-sync-410").start();
    }

    private void pushOneAthlete410(long id){
        if(id<=0||push410||cloudPrefs==null)return;final String token=cloudPrefs.getString("access_token","");if(token.isEmpty())return;
        push410=true;new Thread(()->{try{pushSnapshot410(token,id);}catch(Exception ignored){}finally{push410=false;}},"parion-athlete-push-410").start();
    }

    private void pushSnapshot410(String token,long onlyId)throws Exception{
        JSONArray athletes=new JSONArray(),payments=new JSONArray(),fees=new JSONArray();SQLiteDatabase d=db.getReadableDatabase();
        String where=onlyId>0?" WHERE id=?":"";String[] args=onlyId>0?new String[]{String.valueOf(onlyId)}:null;Cursor a;
        try{a=d.rawQuery("SELECT * FROM athletes"+where+(onlyId>0?"":" WHERE TRIM(COALESCE(deletedAt,''))=''"),args);}catch(Exception e){a=d.rawQuery("SELECT * FROM athletes"+where,args);}
        while(a.moveToNext()){
            JSONObject o=new JSONObject();long id=a.getLong(a.getColumnIndexOrThrow("id"));o.put("legacy_id",id);
            pc(o,"seq",a,"seq");pc(o,"birth_year",a,"birthYear");pc(o,"birth_date",a,"birthDate");pc(o,"name",a,"name");pc(o,"category",a,"category");pc(o,"status",a,"status");pc(o,"monthly_fee",a,"monthlyFee");pc(o,"sibling",a,"sibling");pc(o,"tshirt_qty",a,"tshirtQty");pc(o,"tshirt_paid",a,"tshirtPaid");pc(o,"tracksuit_qty",a,"tracksuitQty");pc(o,"tracksuit_paid",a,"tracksuitPaid");pc(o,"notes",a,"notes");pc(o,"phone",a,"phone");pc(o,"mother_name",a,"motherName");pc(o,"mother_phone",a,"motherPhone");pc(o,"father_name",a,"fatherName");pc(o,"father_phone",a,"fatherPhone");pc(o,"start_date",a,"startDate");pc(o,"end_date",a,"endDate");pc(o,"restart_date",a,"restartDate");pc(o,"tckn",a,"tckn");athletes.put(o);
        }a.close();
        Cursor p=d.rawQuery("SELECT athleteId,year,month,marker,amount FROM payments"+(onlyId>0?" WHERE athleteId=?":""),args);
        while(p.moveToNext()){JSONObject o=new JSONObject();o.put("legacy_id",p.getLong(0));o.put("year",p.getInt(1));o.put("month",p.getInt(2));o.put("marker",p.getString(3)==null?"":p.getString(3));o.put("amount",p.getInt(4));payments.put(o);}p.close();
        try{Cursor f=d.rawQuery("SELECT athleteId,year,effectiveMonth,fee FROM fee_history"+(onlyId>0?" WHERE athleteId=?":""),args);while(f.moveToNext()){JSONObject o=new JSONObject();o.put("legacy_id",f.getLong(0));o.put("year",f.getInt(1));o.put("month",f.getInt(2));o.put("fee",f.getInt(3));fees.put(o);}f.close();}catch(Exception ignored){}
        JSONObject body=new JSONObject().put("p_athletes",athletes).put("p_payments",payments).put("p_fees",fees);
        HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_mobile_snapshot",body.toString(),token);if(r.code<200||r.code>=300)throw new IOException("Buluta yazma başarısız ("+r.code+")");
    }

    private void pc(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));}

    private int[] applyFullCloud410(JSONArray athletes,JSONArray payments,JSONArray fees)throws Exception{
        SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
        try{
            for(int i=0;i<athletes.length();i++){
                JSONObject x=athletes.getJSONObject(i);long id=x.optLong("legacy_id",-1);if(id<=0)continue;Cursor c=d.rawQuery("SELECT 1 FROM athletes WHERE id=?",new String[]{String.valueOf(id)});boolean exists=c.moveToFirst();c.close();
                if(!exists){ContentValues v=new ContentValues();v.put("id",id);v.put("name",x.optString("name",""));v.put("status",x.optString("status","AKTİF"));v.put("category",x.optString("category",""));v.put("photo",x.optString("photo",""));d.insert("athletes",null,v);}
            }d.setTransactionSuccessful();
        }finally{d.endTransaction();}
        int[] base=super.applyCloudCache(athletes,payments);
        d.beginTransaction();try{
            for(int i=0;i<athletes.length();i++){JSONObject x=athletes.getJSONObject(i);long id=x.optLong("legacy_id",-1);if(id<=0)continue;ContentValues v=new ContentValues();if(x.has("tckn")&&!x.isNull("tckn"))v.put("tckn",x.optString("tckn",""));if(v.size()>0)d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});}
            d.delete("fee_history",null,null);for(int i=0;i<fees.length();i++){JSONObject f=fees.getJSONObject(i);ContentValues v=new ContentValues();v.put("athleteId",f.optLong("legacy_id"));v.put("year",f.optInt("year"));v.put("effectiveMonth",f.optInt("month"));v.put("fee",f.optInt("fee"));d.insertWithOnConflict("fee_history",null,v,SQLiteDatabase.CONFLICT_REPLACE);}d.setTransactionSuccessful();
        }finally{d.endTransaction();}
        return base;
    }
}
