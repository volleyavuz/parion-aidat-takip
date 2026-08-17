package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivityV420 extends MainActivityV419 {
    private volatile boolean sync420=false;
    private final ArrayList<String> warnings420=new ArrayList<>();

    @Override void showCloudMenu(){
        String pullAt=cloudPrefs.getString("sync_last_pull_at","HENÜZ YOK");
        String pullSum=cloudPrefs.getString("sync_last_pull_summary","—");
        String pushAt=cloudPrefs.getString("sync_last_push_at","HENÜZ YOK");
        String pushSum=cloudPrefs.getString("sync_last_push_summary","—");
        String msg="SON BULUTTAN GÜNCELLEME\n"+pullAt+"\n"+pullSum+"\n\nSON BULUTA GÖNDERME\n"+pushAt+"\n"+pushSum;
        String[] items={"BULUTTAN GÜNCELLE","YEREL DEĞİŞİKLİKLERİ BULUTA GÖNDER","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("ONLINE HESAP").setMessage(msg).setItems(items,(d,w)->{if(w==0)pull420(true);else if(w==1)confirmPush420();else{cloudPrefs.edit().clear().apply();showLogin();}}).show();
    }

    @Override void syncFromCloud(boolean announce){pull420(announce);}

    private void pull420(boolean announce){
        if(sync420||cloudPrefs==null)return;String token=cloudPrefs.getString("access_token","");if(token.isEmpty())return;sync420=true;syncing=true;if(announce)toast("BULUTTAN GÜNCELLENİYOR...");
        new Thread(()->{try{
            String[] t={token};HttpResult ar=get420("/rest/v1/mobile_athletes?select=*",t);if(ar.code<200||ar.code>=300)throw new IOException("SPORCU VERİSİ ALINAMADI ("+ar.code+")");
            HttpResult pr=get420("/rest/v1/mobile_payments_legacy?select=*",t);if(pr.code<200||pr.code>=300)throw new IOException("ÖDEME VERİSİ ALINAMADI ("+pr.code+")");
            HttpResult fr=get420("/rest/v1/mobile_fee_history?select=*",t);if(fr.code<200||fr.code>=300)throw new IOException("AİDAT GEÇMİŞİ ALINAMADI ("+fr.code+")");
            JSONArray athletes=new JSONArray(ar.body),payments=new JSONArray(pr.body),fees=new JSONArray(fr.body);int[] n=applyPull420(athletes,payments,fees);String sum=n[0]+" SPORCU • "+n[1]+" ÖDEME • "+fees.length()+" AİDAT GEÇMİŞİ";saveStatus420(true,sum);
            runOnUiThread(()->{sync420=false;syncing=false;if(announce)new AlertDialog.Builder(this).setTitle("BULUTTAN GÜNCELLEME TAMAMLANDI").setMessage(sum).setPositiveButton("TAMAM",null).show();showHome();});
        }catch(Exception e){String m=e.getMessage();runOnUiThread(()->{sync420=false;syncing=false;if(announce)new AlertDialog.Builder(this).setTitle("SENKRONİZASYON HATASI").setMessage(m==null?"BAĞLANTI HATASI":m).setPositiveButton("TAMAM",null).show();});}},"parion-pull-420").start();
    }

    private HttpResult get420(String path,String[] token)throws Exception{HttpResult r=request("GET",SUPABASE_URL+path,null,token[0]);if(r.code==401&&refreshSession()){token[0]=cloudPrefs.getString("access_token","");r=request("GET",SUPABASE_URL+path,null,token[0]);}return r;}

    private int[] applyPull420(JSONArray athletes,JSONArray payments,JSONArray fees)throws Exception{
        int[] base=applyCloudCache(athletes,payments);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{
            for(int i=0;i<athletes.length();i++){JSONObject x=athletes.getJSONObject(i);long id=x.optLong("legacy_id",-1);if(id<=0)continue;if(x.has("tckn")&&!x.isNull("tckn")){ContentValues v=new ContentValues();v.put("tckn",x.optString("tckn",""));d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});}}
            try{d.delete("fee_history",null,null);for(int i=0;i<fees.length();i++){JSONObject f=fees.getJSONObject(i);ContentValues v=new ContentValues();v.put("athleteId",f.optLong("legacy_id"));v.put("year",f.optInt("year"));v.put("effectiveMonth",f.optInt("month"));v.put("fee",f.optInt("fee"));d.insertWithOnConflict("fee_history",null,v,SQLiteDatabase.CONFLICT_REPLACE);}}catch(Exception ignored){}d.setTransactionSuccessful();
        }finally{d.endTransaction();}return base;
    }

    private void confirmPush420(){
        int[] counts=localCounts420();new AlertDialog.Builder(this).setTitle("BULUTA GÖNDER").setMessage("Telefondaki güncel kayıtlar doğrulanıp buluta gönderilecek.\n\n"+counts[0]+" sporcu\n"+counts[1]+" ödeme\n"+counts[2]+" aidat geçmişi\n\nMevcut bulut kayıtları silinmez; eşleşen kayıtlar güncellenir.").setPositiveButton("EVET, GÖNDER",(d,w)->push420()).setNegativeButton("VAZGEÇ",null).show();
    }

    private int[] localCounts420(){int[] x={0,0,0};try{Cursor a=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);a.moveToFirst();x[0]=a.getInt(0);a.close();Cursor p=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM payments",null);p.moveToFirst();x[1]=p.getInt(0);p.close();Cursor f=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM fee_history",null);f.moveToFirst();x[2]=f.getInt(0);f.close();}catch(Exception ignored){}return x;}

    private void push420(){
        if(sync420||cloudPrefs==null)return;String token=cloudPrefs.getString("access_token","");if(token.isEmpty())return;sync420=true;syncing=true;toast("YEREL DEĞİŞİKLİKLER BULUTA GÖNDERİLİYOR...");
        new Thread(()->{try{
            JSONObject payload=build420();HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_mobile_snapshot",payload.toString(),token);if(r.code==401&&refreshSession())r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_mobile_snapshot",payload.toString(),cloudPrefs.getString("access_token",""));if(r.code<200||r.code>=300)throw new IOException("BULUTA YAZMA BAŞARISIZ ("+r.code+")"+err420(r.body));
            int[] c=localCounts420();String sum=c[0]+" SPORCU • "+c[1]+" ÖDEME • "+c[2]+" AİDAT GEÇMİŞİ";saveStatus420(false,sum);String msg=sum+"\nBULUTA BAŞARIYLA GÖNDERİLDİ."+(warnings420.isEmpty()?"":"\n\nVERİ DOĞRULAMA NOTLARI:\n"+joinWarnings420());
            runOnUiThread(()->{sync420=false;syncing=false;new AlertDialog.Builder(this).setTitle("BULUTA GÖNDERME TAMAMLANDI").setMessage(msg).setPositiveButton("TAMAM",null).show();});
        }catch(Exception e){String m=e.getMessage();runOnUiThread(()->{sync420=false;syncing=false;new AlertDialog.Builder(this).setTitle("BULUTA GÖNDERME HATASI").setMessage(m==null?"BAĞLANTI HATASI":m).setPositiveButton("TAMAM",null).show();});}},"parion-push-420").start();
    }

    private JSONObject build420()throws Exception{
        warnings420.clear();JSONArray athletes=new JSONArray(),payments=new JSONArray(),fees=new JSONArray();SQLiteDatabase d=db.getReadableDatabase();Cursor a;
        try{a=d.rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);}catch(Exception e){a=d.rawQuery("SELECT * FROM athletes",null);}
        while(a.moveToNext()){
            JSONObject o=new JSONObject();long id=a.getLong(a.getColumnIndexOrThrow("id"));String name=col420(a,"name");o.put("legacy_id",id);copy420(o,"seq",a,"seq");copy420(o,"birth_year",a,"birthYear");date420(o,"birth_date",a,"birthDate",name);copy420(o,"name",a,"name");copy420(o,"category",a,"category");copy420(o,"status",a,"status");copy420(o,"monthly_fee",a,"monthlyFee");copy420(o,"sibling",a,"sibling");copy420(o,"tshirt_qty",a,"tshirtQty");copy420(o,"tshirt_paid",a,"tshirtPaid");copy420(o,"tracksuit_qty",a,"tracksuitQty");copy420(o,"tracksuit_paid",a,"tracksuitPaid");copy420(o,"notes",a,"notes");copy420(o,"phone",a,"phone");copy420(o,"mother_name",a,"motherName");copy420(o,"mother_phone",a,"motherPhone");copy420(o,"father_name",a,"fatherName");copy420(o,"father_phone",a,"fatherPhone");date420(o,"start_date",a,"startDate",name);date420(o,"end_date",a,"endDate",name);date420(o,"restart_date",a,"restartDate",name);copy420(o,"tckn",a,"tckn");athletes.put(o);
        }a.close();
        Cursor p=d.rawQuery("SELECT athleteId,year,month,marker,amount FROM payments",null);while(p.moveToNext()){JSONObject o=new JSONObject().put("legacy_id",p.getLong(0)).put("year",p.getInt(1)).put("month",p.getInt(2)).put("marker",p.getString(3)==null?"":p.getString(3)).put("amount",p.getInt(4));payments.put(o);}p.close();
        try{Cursor f=d.rawQuery("SELECT athleteId,year,effectiveMonth,fee FROM fee_history",null);while(f.moveToNext())fees.put(new JSONObject().put("legacy_id",f.getLong(0)).put("year",f.getInt(1)).put("month",f.getInt(2)).put("fee",f.getInt(3)));f.close();}catch(Exception ignored){}
        return new JSONObject().put("p_athletes",athletes).put("p_payments",payments).put("p_fees",fees);
    }

    private String col420(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}
    private void copy420(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));}
    private void date420(JSONObject o,String key,Cursor c,String col,String name)throws Exception{int i=c.getColumnIndex(col);String raw=i<0||c.isNull(i)?"":c.getString(i);String s=normalizeDate420(raw);if(raw!=null&&!raw.trim().isEmpty()&&!"DEVAM".equalsIgnoreCase(raw.trim())&&s.isEmpty())warnings420.add(name+" • "+col+": "+raw);o.put(key,s);}
    private String normalizeDate420(String raw){String s=raw==null?"":raw.trim();if(s.isEmpty()||"DEVAM".equalsIgnoreCase(s)||"-".equals(s)||"—".equals(s))return "";if(s.matches("\\d{4}-\\d{2}-\\d{2}"))return s;if(s.matches("\\d{2}\\.\\d{2}\\.\\d{4}")){String[] p=s.split("\\.");return p[2]+"-"+p[1]+"-"+p[0];}return "";}
    private String joinWarnings420(){StringBuilder b=new StringBuilder();int n=Math.min(8,warnings420.size());for(int i=0;i<n;i++)b.append("• ").append(warnings420.get(i)).append('\n');if(warnings420.size()>n)b.append("• +").append(warnings420.size()-n).append(" EK UYARI");return b.toString().trim();}
    private String err420(String s){if(s==null||s.trim().isEmpty())return "";try{JSONObject o=new JSONObject(s);String m=o.optString("message",o.optString("error_description",o.optString("hint","")));return m.isEmpty()?"":"\n"+m;}catch(Exception e){return "";}}
    private void saveStatus420(boolean pull,String summary){String at=new SimpleDateFormat("dd.MM.yyyy HH:mm",new Locale("tr","TR")).format(new Date());cloudPrefs.edit().putString(pull?"sync_last_pull_at":"sync_last_push_at",at).putString(pull?"sync_last_pull_summary":"sync_last_push_summary",summary).apply();}
}
