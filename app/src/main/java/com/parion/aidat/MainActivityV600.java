package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * Parion v4.0.0
 * - no bundled athlete/payment seed data
 * - cloud is bootstrap source on first install
 * - full bulk cloud->local refresh includes materials
 * - local edits push only ONE athlete at a time
 * - material/media writes are enabled again
 * - legacy parion_sync_mobile_snapshot bulk write is permanently blocked
 */
public class MainActivityV600 extends MainActivityV505 {
    private final ExecutorService sync600=Executors.newSingleThreadExecutor();
    private volatile boolean restore600=false;
    private volatile boolean delta600=false;
    private boolean seasonPending600=false;
    private long seasonTarget600=-2;
    private boolean pendingSummer600=false,pendingWinter600=false;
    private static final String V4_PREF="parion_v4_sync";
    private static final String LAST_PULL="last_pull";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        if(cloudPrefs!=null&&!cloudPrefs.getString("access_token","").isEmpty()){
            long last=getSharedPreferences(V4_PREF,MODE_PRIVATE).getLong(LAST_PULL,0L);
            if(db.count(null)==0 || System.currentTimeMillis()-last>5L*60L*1000L) syncFromCloud(false);
        }
    }

    @Override protected void onResume(){
        super.onResume();
        if(cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        long last=getSharedPreferences(V4_PREF,MODE_PRIVATE).getLong(LAST_PULL,0L);
        if(System.currentTimeMillis()-last>5L*60L*1000L) syncFromCloud(false);
    }

    /** Allow normal record/media writes again, but never the old whole-database snapshot RPC. */
    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        String m=method==null?"":method.toUpperCase(Locale.ROOT);
        if("GET".equals(m) || (url!=null&&url.contains("/auth/v1/"))) return super.request(method,url,body,bearer);
        if(url!=null&&url.contains("/rpc/parion_sync_mobile_snapshot"))
            return new HttpResult(409,"{\"error\":\"V4_BULK_SNAPSHOT_DISABLED\"}");
        return directRequest600(method,url,body,bearer,null);
    }

    private HttpResult directRequest600(String method,String url,String body,String bearer,String prefer)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
        c.setRequestMethod(method);c.setConnectTimeout(15000);c.setReadTimeout(25000);
        c.setRequestProperty("apikey",SUPABASE_KEY);c.setRequestProperty("Accept","application/json");
        if(bearer!=null&&!bearer.isEmpty())c.setRequestProperty("Authorization","Bearer "+bearer);
        if(prefer!=null)c.setRequestProperty("Prefer",prefer);
        if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream o=c.getOutputStream()){o.write(body.getBytes(StandardCharsets.UTF_8));}}
        int code=c.getResponseCode();InputStream in=code>=400?c.getErrorStream():c.getInputStream();String text=read600(in);c.disconnect();return new HttpResult(code,text);
    }
    private String read600(InputStream in)throws Exception{if(in==null)return "";ByteArrayOutputStream b=new ByteArrayOutputStream();try(InputStream x=in){byte[] p=new byte[8192];int n;while((n=x.read(p))>0)b.write(p,0,n);}return b.toString("UTF-8");}

    @Override void syncFromCloud(boolean announce){
        if(restore600||syncing||cloudPrefs==null)return;
        if(cloudPrefs.getString("access_token","").isEmpty())return;
        restore600=true;syncing=true;if(announce)toast("BULUT → YEREL SENKRONİZASYON BAŞLADI...");
        sync600.execute(()->{
            String stage="BAŞLANGIÇ";
            try{
                stage="SPORCULAR";HttpResult ar=getAuthed("/rest/v1/mobile_athletes?select=*&order=legacy_id.asc");
                if(ar.code==401&&refreshSession())ar=getAuthed("/rest/v1/mobile_athletes?select=*&order=legacy_id.asc");need600(ar,"SPORCULAR");
                stage="ÖDEMELER";HttpResult pr=getAuthed("/rest/v1/mobile_payments_legacy?select=*&order=legacy_id.asc,year.asc,month.asc");need600(pr,"ÖDEMELER");
                stage="AİDATLAR";HttpResult fr=getAuthed("/rest/v1/mobile_fee_history?select=*&order=legacy_id.asc,year.asc,month.asc");need600(fr,"AİDATLAR");
                stage="MEDYA";HttpResult mr=getAuthed("/rest/v1/athletes?select=legacy_id,photo_path,registration_form_path,deleted_at&order=legacy_id.asc");need600(mr,"MEDYA");
                stage="MALZEME ÜRÜNLERİ";HttpResult mpr=getAuthed("/rest/v1/material_products?select=*&order=name.asc");need600(mpr,"MALZEME ÜRÜNLERİ");
                stage="MALZEME HAREKETLERİ";HttpResult mtr=getAuthed("/rest/v1/material_transactions?select=*&order=issued_at.asc,created_at.asc");need600(mtr,"MALZEME HAREKETLERİ");

                JSONArray athletes=new JSONArray(ar.body),payments=new JSONArray(pr.body),fees=new JSONArray(fr.body),media=new JSONArray(mr.body),products=new JSONArray(mpr.body),materials=new JSONArray(mtr.body);
                stage="YEREL VERİTABANI";int[] n=replaceAll600(athletes,payments,fees,media,products,materials);
                getSharedPreferences(V4_PREF,MODE_PRIVATE).edit().putLong(LAST_PULL,System.currentTimeMillis()).apply();
                runOnUiThread(()->{restore600=false;syncing=false;if(announce)toast("SENKRONİZASYON TAMAM: "+n[0]+" SPORCU • "+n[1]+" ÖDEME • "+n[2]+" AİDAT • "+n[3]+" MALZEME");showHome();});
            }catch(Exception e){String msg=stage+": "+short600(e);runOnUiThread(()->{restore600=false;syncing=false;Toast.makeText(this,"SENKRONİZASYON HATASI • "+msg,Toast.LENGTH_LONG).show();});}
        });
    }

    private void need600(HttpResult r,String name)throws IOException{if(r==null)throw new IOException(name+" YANIT YOK");if(r.code<200||r.code>=300)throw new IOException(name+" HTTP "+r.code+" "+(r.body==null?"":r.body.substring(0,Math.min(100,r.body.length()))));}

    private int[] replaceAll600(JSONArray athletes,JSONArray payments,JSONArray fees,JSONArray media,JSONArray products,JSONArray materials)throws Exception{
        HashMap<Long,String> photos=new HashMap<>(),forms=new HashMap<>(),deleted=new HashMap<>();
        for(int i=0;i<media.length();i++){JSONObject o=media.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;String p=clean600(o,"photo_path"),f=clean600(o,"registration_form_path"),del=clean600(o,"deleted_at");if(!p.isEmpty())photos.put(id,p);if(!f.isEmpty())forms.put(id,f);if(!del.isEmpty())deleted.put(id,del);}
        SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
        int ac=0,pc=0,fc=0,mc=0;
        try{
            d.delete("payments",null,null);d.delete("fee_history",null,null);d.delete("athletes",null,null);d.delete("material_transactions",null,null);d.delete("material_products",null,null);
            for(int i=0;i<athletes.length();i++){
                JSONObject a=athletes.getJSONObject(i);long id=a.optLong("legacy_id",-1);if(id<=0)continue;ContentValues v=new ContentValues();v.put("id",id);
                putInt600(v,"seq",a,"seq");putInt600(v,"birthYear",a,"birth_year");putText600(v,"birthDate",a,"birth_date");putText600(v,"name",a,"name");putText600(v,"category",a,"category");putText600(v,"status",a,"status");putInt600(v,"monthlyFee",a,"monthly_fee");putText600(v,"sibling",a,"sibling");
                putInt600(v,"tshirtQty",a,"tshirt_qty");putInt600(v,"tshirtPaid",a,"tshirt_paid");putInt600(v,"tracksuitQty",a,"tracksuit_qty");putInt600(v,"tracksuitPaid",a,"tracksuit_paid");putText600(v,"notes",a,"notes");putText600(v,"phone",a,"phone");putText600(v,"motherName",a,"mother_name");putText600(v,"motherPhone",a,"mother_phone");putText600(v,"fatherName",a,"father_name");putText600(v,"fatherPhone",a,"father_phone");putText600(v,"startDate",a,"start_date");putText600(v,"endDate",a,"end_date");putText600(v,"restartDate",a,"restart_date");putText600(v,"tckn",a,"tckn");
                v.put("summerCall",a.optBoolean("summer_call",false)?1:0);v.put("winterCall",a.optBoolean("winter_call",false)?1:0);String p=photos.get(id);v.put("photo",p==null||p.isEmpty()?"NONE":"CLOUD:"+p);String del=deleted.get(id);if(del!=null)v.put("deletedAt",del);
                d.insertOrThrow("athletes",null,v);ac++;
            }
            for(int i=0;i<payments.length();i++){JSONObject p=payments.getJSONObject(i);long id=p.optLong("legacy_id",-1);int y=p.optInt("year"),m=p.optInt("month");if(id<=0||m<1||m>12)continue;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",y);v.put("month",m);v.put("marker",p.optString("marker",""));v.put("amount",p.optInt("amount",0));d.insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE);pc++;}
            for(int i=0;i<fees.length();i++){JSONObject f=fees.getJSONObject(i);long id=f.optLong("legacy_id",-1);int y=f.optInt("year"),m=f.optInt("month");if(id<=0||m<1||m>12)continue;ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",y);v.put("effectiveMonth",m);v.put("fee",f.optInt("fee",0));d.insertWithOnConflict("fee_history",null,v,SQLiteDatabase.CONFLICT_REPLACE);fc++;}
            for(int i=0;i<products.length();i++){JSONObject o=products.getJSONObject(i);ContentValues v=new ContentValues();v.put("name",o.optString("name",""));v.put("currentPrice",o.optInt("current_price",0));v.put("active",o.optBoolean("active",true)?1:0);v.put("cloudId",o.optString("id",""));d.insertWithOnConflict("material_products",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
            for(int i=0;i<materials.length();i++){JSONObject o=materials.getJSONObject(i);long aid=o.optLong("athlete_legacy_id",-1);if(aid<=0)continue;ContentValues v=new ContentValues();v.put("cloudId",o.optString("id",""));v.put("athleteId",aid);v.put("product",o.optString("product_name",""));v.put("qty",o.optInt("quantity",0));v.put("unitPrice",o.optInt("unit_price",0));v.put("total",o.optInt("total_amount",0));v.put("paidAmount",o.optInt("paid_amount",0));v.put("issuedDate",clean600(o,"issued_at"));v.put("paymentDate",clean600(o,"payment_date"));v.put("note",clean600(o,"note"));d.insertWithOnConflict("material_transactions",null,v,SQLiteDatabase.CONFLICT_REPLACE);mc++;}
            d.setTransactionSuccessful();
        }finally{d.endTransaction();}
        photoMap413().clear();photoMap413().putAll(photos);formMap413().clear();formMap413().putAll(forms);
        markAllSynced600();return new int[]{ac,pc,fc,mc};
    }

    private String clean600(JSONObject o,String k){if(!o.has(k)||o.isNull(k))return "";String s=o.optString(k,"");return "null".equalsIgnoreCase(s)?"":s;}
    private void putText600(ContentValues v,String col,JSONObject o,String key){if(o.has(key)&&!o.isNull(key))v.put(col,o.optString(key,""));}
    private void putInt600(ContentValues v,String col,JSONObject o,String key){if(o.has(key)&&!o.isNull(key))v.put(col,o.optInt(key,0));}

    private void markAllSynced600(){
        SQLiteDatabase d=db.getWritableDatabase();Cursor c=d.rawQuery("SELECT id FROM athletes",null);long now=System.currentTimeMillis();d.beginTransaction();try{while(c.moveToNext()){long id=c.getLong(0);ContentValues v=new ContentValues();v.put("entity","ATHLETE");v.put("entityKey",String.valueOf(id));v.put("localHash",athleteHash600(id));v.put("lastSyncedAt",now);d.insertWithOnConflict("sync_state",null,v,SQLiteDatabase.CONFLICT_REPLACE);}d.setTransactionSuccessful();}finally{c.close();d.endTransaction();}}

    private String athleteHash600(long id){
        StringBuilder b=new StringBuilder();SQLiteDatabase d=db.getReadableDatabase();Cursor a=d.rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});if(a.moveToFirst())for(int i=0;i<a.getColumnCount();i++){String n=a.getColumnName(i);if("photo".equalsIgnoreCase(n))continue;b.append(n).append('=').append(a.isNull(i)?"":a.getString(i)).append('|');}a.close();
        Cursor p=d.rawQuery("SELECT year,month,marker,amount FROM payments WHERE athleteId=? ORDER BY year,month",new String[]{String.valueOf(id)});while(p.moveToNext())b.append("P:").append(p.getInt(0)).append(':').append(p.getInt(1)).append(':').append(p.getString(2)).append(':').append(p.getInt(3)).append('|');p.close();
        Cursor f=d.rawQuery("SELECT year,effectiveMonth,fee FROM fee_history WHERE athleteId=? ORDER BY year,effectiveMonth",new String[]{String.valueOf(id)});while(f.moveToNext())b.append("F:").append(f.getInt(0)).append(':').append(f.getInt(1)).append(':').append(f.getInt(2)).append('|');f.close();
        return Integer.toHexString(b.toString().hashCode());
    }

    private String savedHash600(long id){Cursor c=db.getReadableDatabase().rawQuery("SELECT localHash FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});String h="";if(c.moveToFirst())h=c.getString(0)==null?"":c.getString(0);c.close();return h;}
    private void saveHash600(long id,String h){ContentValues v=new ContentValues();v.put("entity","ATHLETE");v.put("entityKey",String.valueOf(id));v.put("localHash",h);v.put("lastSyncedAt",System.currentTimeMillis());db.getWritableDatabase().insertWithOnConflict("sync_state",null,v,SQLiteDatabase.CONFLICT_REPLACE);}

    private void queueDelta600(long id){
        if(id<=0||delta600||restore600||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;String now=athleteHash600(id);if(now.equals(savedHash600(id)))return;
        delta600=true;sync600.execute(()->{try{JSONObject body=oneAthleteBody600(id);String token=cloudPrefs.getString("access_token","");HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_delta_v4",body.toString(),token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_delta_v4",body.toString(),token);}if(r.code>=200&&r.code<300)saveHash600(id,athleteHash600(id));else runOnUiThread(()->toast("DEĞİŞİKLİK BULUTA GÖNDERİLEMEDİ • HTTP "+r.code));}catch(Exception e){runOnUiThread(()->toast("DEĞİŞİKLİK BULUTTA BEKLİYOR."));}finally{delta600=false;}});
    }

    private JSONObject oneAthleteBody600(long id)throws Exception{
        SQLiteDatabase d=db.getReadableDatabase();Cursor c=d.rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});if(!c.moveToFirst()){c.close();throw new IOException("sporcu yok");}
        JSONObject a=new JSONObject();a.put("legacy_id",id);putCur600(a,"seq",c,"seq");putCur600(a,"birth_year",c,"birthYear");putCur600(a,"birth_date",c,"birthDate");putCur600(a,"name",c,"name");putCur600(a,"category",c,"category");putCur600(a,"status",c,"status");putCur600(a,"monthly_fee",c,"monthlyFee");putCur600(a,"sibling",c,"sibling");putCur600(a,"tshirt_qty",c,"tshirtQty");putCur600(a,"tshirt_paid",c,"tshirtPaid");putCur600(a,"tracksuit_qty",c,"tracksuitQty");putCur600(a,"tracksuit_paid",c,"tracksuitPaid");putCur600(a,"notes",c,"notes");putCur600(a,"phone",c,"phone");putCur600(a,"mother_name",c,"motherName");putCur600(a,"mother_phone",c,"motherPhone");putCur600(a,"father_name",c,"fatherName");putCur600(a,"father_phone",c,"fatherPhone");putCur600(a,"start_date",c,"startDate");putCur600(a,"end_date",c,"endDate");putCur600(a,"restart_date",c,"restartDate");putCur600(a,"tckn",c,"tckn");a.put("summer_call",getInt600(c,"summerCall")!=0);a.put("winter_call",getInt600(c,"winterCall")!=0);c.close();
        JSONArray ps=new JSONArray();Cursor p=d.rawQuery("SELECT year,month,marker,amount FROM payments WHERE athleteId=?",new String[]{String.valueOf(id)});while(p.moveToNext())ps.put(new JSONObject().put("legacy_id",id).put("year",p.getInt(0)).put("month",p.getInt(1)).put("marker",p.getString(2)==null?"":p.getString(2)).put("amount",p.getInt(3)));p.close();
        JSONArray fs=new JSONArray();Cursor f=d.rawQuery("SELECT year,effectiveMonth,fee FROM fee_history WHERE athleteId=?",new String[]{String.valueOf(id)});while(f.moveToNext())fs.put(new JSONObject().put("legacy_id",id).put("year",f.getInt(0)).put("month",f.getInt(1)).put("fee",f.getInt(2)));f.close();
        return new JSONObject().put("p_legacy_id",id).put("p_athlete",a).put("p_payments",ps).put("p_fees",fs);
    }
    private int getInt600(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?0:c.getInt(i);}
    private void putCur600(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));}

    @Override void form(long id){
        seasonPending600=true;seasonTarget600=id;pendingSummer600=false;pendingWinter600=false;
        if(id>0){Cursor c=db.athlete(id);if(c.moveToFirst()){pendingSummer600=getInt600(c,"summerCall")!=0;pendingWinter600=getInt600(c,"winterCall")!=0;}c.close();}
        super.form(id);addSeasonChecks600(root);
        restoreWritableForm600(id);
    }

    private void addSeasonChecks600(View v){
        Button save=findSave600(v);if(save==null||!(save.getParent() instanceof LinearLayout))return;LinearLayout p=(LinearLayout)save.getParent();
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(6),dp(8),dp(6),dp(8));box.setBackground(round(Color.rgb(248,248,248),10));
        TextView h=tv("ARAMA HATIRLATMALARI",13,BLACK,true);box.addView(h);
        CheckBox summer=new CheckBox(this);summer.setText("YAZIN ARANACAK");summer.setChecked(pendingSummer600);summer.setOnCheckedChangeListener((b,x)->pendingSummer600=x);box.addView(summer);
        CheckBox winter=new CheckBox(this);winter.setText("KIŞIN ARANACAK");winter.setChecked(pendingWinter600);winter.setOnCheckedChangeListener((b,x)->pendingWinter600=x);box.addView(winter);
        int pos=p.indexOfChild(save);p.addView(box,Math.max(0,pos),new LinearLayout.LayoutParams(-1,-2));
    }
    private Button findSave600(View v){if(v instanceof Button){String s=String.valueOf(((Button)v).getText()).toUpperCase(new Locale("tr","TR"));if(s.contains("KAYDI OLUŞTUR")||s.contains("DEĞİŞİKLİKLERİ KAYDET"))return (Button)v;}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){Button b=findSave600(g.getChildAt(i));if(b!=null)return b;}}return null;}

    @Override void showProfile(long id){
        String from=page;
        if("FORM".equals(from)&&seasonPending600&&(seasonTarget600==id||seasonTarget600<0)){
            ContentValues v=new ContentValues();v.put("summerCall",pendingSummer600?1:0);v.put("winterCall",pendingWinter600?1:0);db.getWritableDatabase().update("athletes",v,"id=?",new String[]{String.valueOf(id)});seasonPending600=false;seasonTarget600=-2;
        }
        super.showProfile(id);restoreWritableProfile600(id);queueDelta600(id);
    }

    @Override void goBack(){if("FORM".equals(page)){seasonPending600=false;seasonTarget600=-2;}super.goBack();}

    @Override void showHome(){
        super.showHome();removeReadOnlyLabels600(root);restoreWritableHome600();addSeasonCards600();
    }

    private void addSeasonCards600(){
        ScrollView sv=firstScroll600(root);if(sv==null)return;LinearLayout b=box(sv);int summer=countSeason600("summerCall"),winter=countSeason600("winterCall");
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
        View sc=seasonCard600("YAZIN ARANACAK",summer,Color.rgb(245,166,35),true);View wc=seasonCard600("KIŞIN ARANACAK",winter,Color.rgb(70,130,180),false);
        LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(0,dp(105),1);a.setMargins(0,dp(8),dp(4),dp(8));LinearLayout.LayoutParams z=new LinearLayout.LayoutParams(0,dp(105),1);z.setMargins(dp(4),dp(8),0,dp(8));row.addView(sc,a);row.addView(wc,z);b.addView(row);
    }
    private int countSeason600(String col){Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' AND "+col+"=1",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}
    private View seasonCard600(String label,int n,int color,boolean summer){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);x.setPadding(dp(5),dp(7),dp(5),dp(7));x.setBackground(round(Color.WHITE,12));TextView c=tv(String.valueOf(n),24,color,true);c.setGravity(Gravity.CENTER);TextView l=tv(label,11,Color.DKGRAY,true);l.setGravity(Gravity.CENTER);x.addView(c);x.addView(l);x.setOnClickListener(v->showSeason600(summer));return x;}
    private void showSeason600(boolean summer){page=summer?"SUMMER_CALL_600":"WINTER_CALL_600";base(summer?"YAZIN ARANACAK SPORCULAR":"KIŞIN ARANACAK SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);String col=summer?"summerCall":"winterCall";Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE status='AKTİF' AND TRIM(COALESCE(deletedAt,''))='' AND "+col+"=1 ORDER BY name COLLATE NOCASE",null);int n=0;while(c.moveToNext()){row(b,a(c),null,0);n++;}c.close();if(n==0)b.addView(tv("BU LİSTEDE SPORCU YOK.",14,Color.DKGRAY,true));}

    @Override void showCloudMenu(){String[] items={"ŞİMDİ SENKRONİZE ET","OTURUMU KAPAT"};new AlertDialog.Builder(this).setTitle("ONLINE HESAP • ÇİFT YÖNLÜ DELTA").setItems(items,(d,w)->{if(w==0){pushDirtyAll600();}else{cloudPrefs.edit().clear().apply();showLogin();}}).show();}
    private void pushDirtyAll600(){if(delta600||restore600)return;toast("DEĞİŞİKLİKLER KONTROL EDİLİYOR...");sync600.execute(()->{try{Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);while(c.moveToNext()){long id=c.getLong(0);String h=athleteHash600(id);if(!h.equals(savedHash600(id)))pushOneBlocking600(id,h);}c.close();runOnUiThread(()->syncFromCloud(true));}catch(Exception e){runOnUiThread(()->toast("SENKRONİZASYON BAŞLATILAMADI."));}});}
    private void pushOneBlocking600(long id,String hash)throws Exception{JSONObject body=oneAthleteBody600(id);String token=cloudPrefs.getString("access_token","");HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_delta_v4",body.toString(),token);if(r.code>=200&&r.code<300)saveHash600(id,hash);}

    private ScrollView firstScroll600(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=firstScroll600(g.getChildAt(i));if(s!=null)return s;}}return null;}
    private void removeReadOnlyLabels600(View v){if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;for(int i=g.getChildCount()-1;i>=0;i--){View x=g.getChildAt(i);if(x instanceof TextView&&String.valueOf(((TextView)x).getText()).contains("BULUTA YAZMA KAPALI")){g.removeViewAt(i);continue;}removeReadOnlyLabels600(x);}}

    private void restoreWritableHome600(){try{Method m=MainActivityV407.class.getDeclaredMethod("patchHomeCards407",View.class);m.setAccessible(true);m.invoke(this,root);}catch(Exception ignored){}try{Method m=MainActivityV416.class.getDeclaredMethod("patchMissingCard416",View.class);m.setAccessible(true);m.invoke(this,root);}catch(Exception ignored){}}
    private void restoreWritableProfile600(long id){try{Method m=MainActivityV416.class.getDeclaredMethod("patchProfileForm416",View.class,long.class);m.setAccessible(true);m.invoke(this,root,id);}catch(Exception ignored){}}
    private void restoreWritableForm600(long id){if(id<=0)return;try{Method m=MainActivityV412.class.getDeclaredMethod("patchPhotoButtons412",View.class,long.class);m.setAccessible(true);m.invoke(this,root,id);}catch(Exception ignored){}}

    private String short600(Exception e){String s=e.getMessage();if(s==null||s.trim().isEmpty())s=e.getClass().getSimpleName();s=s.replace('\n',' ');return s.length()>160?s.substring(0,160):s;}

    @Override protected void onDestroy(){sync600.shutdownNow();super.onDestroy();}
}
