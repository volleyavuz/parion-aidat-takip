package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.*;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivityV421 extends MainActivityV420 {
    private final Handler syncHandler421=new Handler(Looper.getMainLooper());
    private final AtomicBoolean autoSyncRunning421=new AtomicBoolean(false);
    private volatile boolean alive421=true;
    private static final long AUTO_MS421=12000L;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        ensureSyncInfra421();
        scheduleAuto421(2500L);
    }

    @Override protected void onDestroy(){alive421=false;syncHandler421.removeCallbacksAndMessages(null);super.onDestroy();}

    private void ensureSyncInfra421(){
        SQLiteDatabase d=db.getWritableDatabase();
        d.execSQL("CREATE TABLE IF NOT EXISTS sync_control(id INTEGER PRIMARY KEY CHECK(id=1),suppress INTEGER NOT NULL DEFAULT 0)");
        d.execSQL("INSERT OR IGNORE INTO sync_control(id,suppress) VALUES(1,0)");
        d.execSQL("CREATE TABLE IF NOT EXISTS sync_queue(id INTEGER PRIMARY KEY AUTOINCREMENT,kind TEXT NOT NULL,entityId TEXT NOT NULL DEFAULT '',createdAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        d.execSQL("CREATE TABLE IF NOT EXISTS activity_log_local(id INTEGER PRIMARY KEY AUTOINCREMENT,userEmail TEXT NOT NULL DEFAULT '',action TEXT NOT NULL,entityType TEXT NOT NULL DEFAULT '',entityId TEXT NOT NULL DEFAULT '',detail TEXT NOT NULL DEFAULT '',createdAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,cloudSynced INTEGER NOT NULL DEFAULT 0)");
        createTriggers421(d,"athletes","SPORCU");
        createTriggers421(d,"payments","ÖDEME");
        createTriggers421(d,"fee_history","AİDAT");
        if(tableExists421(d,"membership_events"))createTriggers421(d,"membership_events","ÜYELİK");
        if(tableExists421(d,"material_products"))createTriggers421(d,"material_products","MALZEME");
        if(tableExists421(d,"material_transactions"))createTriggers421(d,"material_transactions","MALZEME_HAREKETİ");
    }

    private boolean tableExists421(SQLiteDatabase d,String t){Cursor c=d.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?",new String[]{t});boolean ok=c.moveToFirst();c.close();return ok;}

    private void createTriggers421(SQLiteDatabase d,String table,String label){
        String safe=table.replaceAll("[^A-Za-z0-9_]","");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS q_"+safe+"_ai AFTER INSERT ON "+safe+" WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('"+label+"',CAST(NEW.rowid AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('EKLENDİ','"+label+"',CAST(NEW.rowid AS TEXT),'Yeni kayıt'); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS q_"+safe+"_au AFTER UPDATE ON "+safe+" WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('"+label+"',CAST(NEW.rowid AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('GÜNCELLENDİ','"+label+"',CAST(NEW.rowid AS TEXT),'Kayıt güncellendi'); END");
        d.execSQL("CREATE TRIGGER IF NOT EXISTS q_"+safe+"_ad AFTER DELETE ON "+safe+" WHEN (SELECT suppress FROM sync_control WHERE id=1)=0 BEGIN INSERT INTO sync_queue(kind,entityId) VALUES('"+label+"',CAST(OLD.rowid AS TEXT)); INSERT INTO activity_log_local(action,entityType,entityId,detail) VALUES('SİLİNDİ','"+label+"',CAST(OLD.rowid AS TEXT),'Kayıt silindi'); END");
    }

    private void setSuppress421(boolean on){try{db.getWritableDatabase().execSQL("UPDATE sync_control SET suppress=? WHERE id=1",new Object[]{on?1:0});}catch(Exception ignored){}}

    private int pending421(){try{Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM sync_queue",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}catch(Exception e){return 0;}}

    private void scheduleAuto421(long delay){if(!alive421)return;syncHandler421.postDelayed(()->{if(!alive421)return;if(pending421()>0)autoPush421(false,null);scheduleAuto421(AUTO_MS421);},delay);}

    @Override void showHome(){super.showHome();ensureSyncInfra421();appendRecentActivity421();if(pending421()>0)scheduleAuto421(800L);}

    @Override void showProfile(long id){super.showProfile(id);if(pending421()>0)scheduleAuto421(700L);}

    @Override void form(long id){super.form(id);if(pending421()>0)scheduleAuto421(700L);}

    @Override void showCloudMenu(){
        ensureSyncInfra421();
        int pending=pending421();
        String pullAt=cloudPrefs.getString("sync_last_pull_at","HENÜZ YOK");
        String pushAt=cloudPrefs.getString("sync_last_push_at","HENÜZ YOK");
        String autoAt=cloudPrefs.getString("sync_last_auto_at","HENÜZ YOK");
        String msg="OTOMATİK SENKRONİZASYON: AÇIK\nBEKLEYEN KAYIT: "+pending+"\n\nSON OTOMATİK GÖNDERME\n"+autoAt+"\n\nSON BULUTTAN GÜNCELLEME\n"+pullAt+"\n\nSON BULUTA GÖNDERME\n"+pushAt;
        String[] items={"TÜMÜNÜ ŞİMDİ SENKRONİZE ET","BULUTTAN GÜNCELLE","YEREL DEĞİŞİKLİKLERİ BULUTA GÖNDER","İŞLEM GÜNLÜĞÜ","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("ONLINE HESAP").setMessage(msg).setItems(items,(d,w)->{
            if(w==0)autoPush421(true,()->syncFromCloud(true));
            else if(w==1)syncFromCloud(true);
            else if(w==2)autoPush421(true,null);
            else if(w==3)showActivity421();
            else{cloudPrefs.edit().clear().apply();showLogin();}
        }).show();
    }

    @Override void syncFromCloud(boolean announce){pull421(announce);}

    private void pull421(boolean announce){
        if(autoSyncRunning421.get())return;String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");if(token.isEmpty())return;
        autoSyncRunning421.set(true);if(announce)toast("BULUTTAN GÜNCELLENİYOR...");
        new Thread(()->{try{
            String[] t={token};HttpResult ar=getRetry421("/rest/v1/mobile_athletes?select=*",t),pr=getRetry421("/rest/v1/mobile_payments_legacy?select=*",t),fr=getRetry421("/rest/v1/mobile_fee_history?select=*",t);
            if(ar.code/100!=2||pr.code/100!=2||fr.code/100!=2)throw new IOException("Bulut okuma hatası: "+ar.code+"/"+pr.code+"/"+fr.code);
            setSuppress421(true);
            try{applyCloudCache(new JSONArray(ar.body),new JSONArray(pr.body));SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();try{d.delete("fee_history",null,null);JSONArray f=new JSONArray(fr.body);for(int i=0;i<f.length();i++){JSONObject x=f.getJSONObject(i);ContentValues v=new ContentValues();v.put("athleteId",x.optLong("legacy_id"));v.put("year",x.optInt("year"));v.put("effectiveMonth",x.optInt("month"));v.put("fee",x.optInt("fee"));d.insertWithOnConflict("fee_history",null,v,SQLiteDatabase.CONFLICT_REPLACE);}d.setTransactionSuccessful();}finally{d.endTransaction();}}finally{setSuppress421(false);}
            String at=now421();cloudPrefs.edit().putString("sync_last_pull_at",at).apply();
            runOnUiThread(()->{autoSyncRunning421.set(false);if(announce)new AlertDialog.Builder(this).setTitle("BULUTTAN GÜNCELLEME TAMAMLANDI").setMessage("Güncel bulut verileri telefona alındı.\nBekleyen yerel kayıt: "+pending421()).setPositiveButton("TAMAM",null).show();showHome();});
        }catch(Exception e){String m=e.getMessage();runOnUiThread(()->{autoSyncRunning421.set(false);if(announce)new AlertDialog.Builder(this).setTitle("SENKRONİZASYON HATASI").setMessage(m==null?"Bağlantı hatası":m).setPositiveButton("TAMAM",null).show();});}},"pull-421").start();
    }

    private HttpResult getRetry421(String path,String[] t)throws Exception{HttpResult r=request("GET",SUPABASE_URL+path,null,t[0]);if(r.code==401&&refreshSession()){t[0]=cloudPrefs.getString("access_token","");r=request("GET",SUPABASE_URL+path,null,t[0]);}return r;}

    private void autoPush421(boolean announce,Runnable after){
        if(autoSyncRunning421.get())return;String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");if(token.isEmpty())return;
        autoSyncRunning421.set(true);if(announce)toast("BULUTA GÖNDERİLİYOR...");
        new Thread(()->{try{
            JSONObject body=buildSnapshot421();HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_mobile_snapshot",body.toString(),token);if(r.code==401&&refreshSession())r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_mobile_snapshot",body.toString(),cloudPrefs.getString("access_token",""));if(r.code/100!=2)throw new IOException("Buluta yazma başarısız ("+r.code+") "+error421(r.body));
            pushMembership421();pushAudit421();
            db.getWritableDatabase().delete("sync_queue",null,null);
            String at=now421();SharedPreferences.Editor ed=cloudPrefs.edit().putString("sync_last_push_at",at).putString("sync_last_auto_at",at);ed.apply();
            runOnUiThread(()->{autoSyncRunning421.set(false);if(announce)new AlertDialog.Builder(this).setTitle("SENKRONİZASYON TAMAMLANDI").setMessage("Yerel kayıtlar buluta gönderildi ve doğrulandı.\nBekleyen kayıt: "+pending421()).setPositiveButton("TAMAM",null).show();if(after!=null)after.run();});
        }catch(Exception e){String m=e.getMessage();runOnUiThread(()->{autoSyncRunning421.set(false);if(announce)new AlertDialog.Builder(this).setTitle("BULUTA GÖNDERME HATASI").setMessage((m==null?"Bağlantı hatası":m)+"\n\nKayıtlar silinmedi. Bekleyen kuyrukta tutuluyor: "+pending421()).setPositiveButton("TAMAM",null).show();});}},"push-421").start();
    }

    private JSONObject buildSnapshot421()throws Exception{
        JSONArray athletes=new JSONArray(),payments=new JSONArray(),fees=new JSONArray();SQLiteDatabase d=db.getReadableDatabase();Cursor a;
        try{a=d.rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))=''",null);}catch(Exception e){a=d.rawQuery("SELECT * FROM athletes",null);}
        while(a.moveToNext()){JSONObject o=new JSONObject();long id=a.getLong(a.getColumnIndexOrThrow("id"));o.put("legacy_id",id);put421(o,"seq",a,"seq");put421(o,"birth_year",a,"birthYear");date421(o,"birth_date",a,"birthDate");put421(o,"name",a,"name");put421(o,"category",a,"category");put421(o,"status",a,"status");put421(o,"monthly_fee",a,"monthlyFee");put421(o,"sibling",a,"sibling");put421(o,"tshirt_qty",a,"tshirtQty");put421(o,"tshirt_paid",a,"tshirtPaid");put421(o,"tracksuit_qty",a,"tracksuitQty");put421(o,"tracksuit_paid",a,"tracksuitPaid");put421(o,"notes",a,"notes");put421(o,"phone",a,"phone");put421(o,"mother_name",a,"motherName");put421(o,"mother_phone",a,"motherPhone");put421(o,"father_name",a,"fatherName");put421(o,"father_phone",a,"fatherPhone");date421(o,"start_date",a,"startDate");date421(o,"end_date",a,"endDate");date421(o,"restart_date",a,"restartDate");put421(o,"tckn",a,"tckn");athletes.put(o);}a.close();
        Cursor p=d.rawQuery("SELECT athleteId,year,month,marker,amount FROM payments",null);while(p.moveToNext())payments.put(new JSONObject().put("legacy_id",p.getLong(0)).put("year",p.getInt(1)).put("month",p.getInt(2)).put("marker",p.getString(3)==null?"":p.getString(3)).put("amount",p.getInt(4)));p.close();
        try{Cursor f=d.rawQuery("SELECT athleteId,year,effectiveMonth,fee FROM fee_history",null);while(f.moveToNext())fees.put(new JSONObject().put("legacy_id",f.getLong(0)).put("year",f.getInt(1)).put("month",f.getInt(2)).put("fee",f.getInt(3)));f.close();}catch(Exception ignored){}
        return new JSONObject().put("p_athletes",athletes).put("p_payments",payments).put("p_fees",fees);
    }

    private void put421(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));}
    private void date421(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);String s=i<0||c.isNull(i)?"":c.getString(i);s=s==null?"":s.trim();if("DEVAM".equalsIgnoreCase(s)||!s.matches("\\d{4}-\\d{2}-\\d{2}"))s="";o.put(key,s);}

    private void pushMembership421(){
        try{if(!tableExists421(db.getReadableDatabase(),"membership_events"))return;Cursor c=db.getReadableDatabase().rawQuery("SELECT id,athleteId,eventDate,eventType,note FROM membership_events WHERE cloudId IS NULL OR TRIM(cloudId)=''",null);while(c.moveToNext()){long lid=c.getLong(0),aid=c.getLong(1);String dt=c.getString(2),tp=c.getString(3),nt=c.getString(4);if("LEGACY".equals(nt))continue;JSONObject o=new JSONObject().put("legacy_id",aid).put("event_date",dt).put("event_type",tp).put("note",nt==null?"":nt);String token=cloudPrefs.getString("access_token","");HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/athlete_membership_events",o.toString(),token);if(r.code/100==2||r.code==409){ContentValues v=new ContentValues();v.put("cloudId","SYNCED:"+aid+":"+dt+":"+tp);db.getWritableDatabase().update("membership_events",v,"id=?",new String[]{String.valueOf(lid)});}}c.close();}catch(Exception ignored){}
    }

    private void pushAudit421(){
        try{String email=currentEmail421();Cursor c=db.getReadableDatabase().rawQuery("SELECT id,action,entityType,entityId,detail,createdAt FROM activity_log_local WHERE cloudSynced=0 ORDER BY id",null);while(c.moveToNext()){long id=c.getLong(0);JSONObject o=new JSONObject().put("user_email",email).put("action",c.getString(1)).put("entity_type",c.getString(2)).put("entity_id",c.getString(3)).put("detail",c.getString(4)).put("client_created_at",sqliteTime421(c.getString(5)));HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/app_activity_log",o.toString(),cloudPrefs.getString("access_token",""));if(r.code/100==2){ContentValues v=new ContentValues();v.put("cloudSynced",1);v.put("userEmail",email);db.getWritableDatabase().update("activity_log_local",v,"id=?",new String[]{String.valueOf(id)});}}c.close();}catch(Exception ignored){}
    }

    private String sqliteTime421(String x){if(x==null||x.isEmpty())return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX",Locale.US).format(new Date());return x.replace(' ','T')+"+00:00";}
    private String currentEmail421(){try{String t=cloudPrefs.getString("access_token","");String[] p=t.split("\\.");if(p.length>1){byte[] b=Base64.decode(p[1],Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);JSONObject o=new JSONObject(new String(b,StandardCharsets.UTF_8));return o.optString("email","");}}catch(Exception ignored){}return "";}
    private String now421(){return new SimpleDateFormat("dd.MM.yyyy HH:mm",new Locale("tr","TR")).format(new Date());}
    private String error421(String s){if(s==null)return "";try{return new JSONObject(s).optString("message",s);}catch(Exception e){return s.length()>180?s.substring(0,180):s;}}

    @Override void addCycleProfileRow(LinearLayout b,long id,int key,int anchor,String start,String end,String restart,String sibling,HashMap<Integer,PayRec> pays,boolean future){
        int yr=key/100,mo=key%100;PayRec r=pays.get(key);if(r==null)r=new PayRec("",0);int expected=expectedFeeAt(id,yr,mo,r);boolean active=activeByEvents421(id,yr,mo,start,end,restart);String status,detail;int color;String period=cycleDateLabel(key,anchor)+" – "+cycleDateLabel(shiftMonth(key,1),anchor);
        if("X".equals(r.marker)){status="ARA VERDİ";color=GRAY;detail=period+" • ARA VERDİ";}
        else if("!".equals(r.marker)||"!!".equals(r.marker)){status=expected>0?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"FARKLI TUTAR";color=ORANGE;detail=period+" • "+money(r.amount)+" • "+status;}
        else if(isDate(r.marker)){status=expected>0&&r.amount!=expected?(r.amount<expected?"EKSİK ÖDEME":r.amount>expected?"FAZLA ÖDEME":"ÖDENDİ"):"ÖDENDİ";color=status.equals("ÖDENDİ")?GREEN:ORANGE;detail=period+" • "+dateTr(r.marker)+" • "+money(r.amount)+" • "+status;}
        else if(!active){status="AKTİF DEĞİL";color=Color.rgb(225,225,225);detail=period+" • AKTİF DEĞİL";expected=0;}
        else if("BURSLU".equalsIgnoreCase(sibling)||expected==0&&yr>=2026&&currentMonthlyFee(id)==0){status="BURSLU";color=Color.rgb(225,225,225);detail=period+" • BURSLU";expected=0;}
        else if(future){status="ERKEN ÖDEME AÇIK";color=Color.WHITE;detail=period+" • "+money(expected)+" • ERKEN ÖDEME YAPILABİLİR";}
        else{status="ÖDEME DÖNEMİ";color=YELLOW;detail=period+" • "+money(expected)+" • ÖDEME DÖNEMİ";}
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(10),dp(8),dp(10),dp(8));row.setBackground(round(color,9));row.addView(tv(monthName(mo)+" "+yr,14,BLACK,true));row.addView(tv(detail,12,Color.DKGRAY,false));
        if(active&&expected>0){final int yy=yr,mm=mo,fee=expected,amt=r.amount;final String mk=r.marker;row.setOnClickListener(v->editPayment421(id,yy,mm,fee,mk,amt));}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(row,lp);
    }

    private boolean activeByEvents421(long id,int year,int month,String start,String end,String restart){try{if(tableExists421(db.getReadableDatabase(),"membership_events")){int anchor=anchorDay(start);Calendar due=cycleDate(year*100+month,anchor);String ds=String.format(Locale.US,"%04d-%02d-%02d",due.get(Calendar.YEAR),due.get(Calendar.MONTH)+1,due.get(Calendar.DAY_OF_MONTH));Cursor c=db.getReadableDatabase().rawQuery("SELECT eventType FROM membership_events WHERE athleteId=? AND eventDate<=? ORDER BY eventDate,id",new String[]{String.valueOf(id),ds});boolean any=false,on=false;while(c.moveToNext()){any=true;String t=c.getString(0);if("START".equals(t)||"RESTART".equals(t))on=true;else if("LEAVE".equals(t))on=false;}c.close();if(any)return on;}}catch(Exception ignored){}return activeAt(year,month,start,end,restart);}

    private void editPayment421(long id,int year,int month,int fee,String marker,int amount){
        final String[] opts={"ÖDEME GİR","ARA VERDİ (X)","FARKLI TUTAR (!)","KAYDI TEMİZLE"};
        new AlertDialog.Builder(this).setTitle(monthName(month)+" "+year).setItems(opts,(d,w)->{
            if(w==1){savePayment421(id,year,month,"X",0);return;}if(w==3){savePayment421(id,year,month,"",0);return;}
            LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(20),dp(6),dp(20),0);Button date=btn("ÖDEME TARİHİ SEÇ");final String[] iso={today421()};if(marker!=null&&marker.matches("\\d{4}-\\d{2}-\\d{2}"))iso[0]=marker;date.setText("ÖDEME TARİHİ: "+dateTr(iso[0]));date.setOnClickListener(v->pickDate421(date,iso));EditText am=new EditText(this);am.setHint("TUTAR ₺");am.setInputType(2);am.setText(String.valueOf(amount>0?amount:fee));x.addView(date);x.addView(am);
            new AlertDialog.Builder(this).setTitle(w==2?"FARKLI TUTAR":"AİDAT ÖDEMESİ").setView(x).setPositiveButton("KAYDET",(a,z)->savePayment421(id,year,month,w==2?"!":iso[0],parseInt(am.getText().toString()))).setNegativeButton("VAZGEÇ",null).show();
        }).show();
    }

    private void savePayment421(long id,int year,int month,String marker,int amount){ContentValues v=new ContentValues();v.put("athleteId",id);v.put("year",year);v.put("month",month);v.put("marker",marker);v.put("amount",amount);db.getWritableDatabase().insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE);logDetail421("ÖDEME KAYDI",String.valueOf(id),monthName(month)+" "+year+" • "+(marker.isEmpty()?"TEMİZLENDİ":marker)+" • "+money(amount));showProfile(id);scheduleAuto421(400L);}
    private String today421(){Calendar c=Calendar.getInstance();return String.format(Locale.US,"%04d-%02d-%02d",c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH));}
    private void pickDate421(Button b,String[] iso){Calendar c=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,d)->{iso[0]=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);b.setText("ÖDEME TARİHİ: "+dateTr(iso[0]));},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}

    private void logDetail421(String action,String entity,String detail){try{ContentValues v=new ContentValues();v.put("userEmail",currentEmail421());v.put("action",action);v.put("entityType","UYGULAMA");v.put("entityId",entity);v.put("detail",detail);db.getWritableDatabase().insert("activity_log_local",null,v);}catch(Exception ignored){}}

    private void appendRecentActivity421(){ScrollView s=findScroll421(root);if(s==null||s.getChildCount()==0||!(s.getChildAt(0) instanceof LinearLayout))return;LinearLayout b=(LinearLayout)s.getChildAt(0);TextView h=tv("SON İŞLEMLER",15,BLACK,true);h.setPadding(dp(8),dp(18),dp(8),dp(6));b.addView(h);Cursor c=db.getReadableDatabase().rawQuery("SELECT action,entityType,detail,createdAt,cloudSynced FROM activity_log_local ORDER BY id DESC LIMIT 6",null);int n=0;while(c.moveToNext()){String text=c.getString(3)+" • "+c.getString(0)+" • "+c.getString(1);String det=c.getString(2);if(det!=null&&!det.isEmpty())text+="\n"+det;text+=" • "+(c.getInt(4)==1?"BULUTTA":"BEKLİYOR");TextView t=tv(text,11,Color.DKGRAY,false);t.setPadding(dp(8),dp(5),dp(8),dp(5));b.addView(t);n++;}c.close();if(n==0)b.addView(tv("Henüz kullanıcı işlemi yok.",11,Color.DKGRAY,false));Button all=btn("TÜM İŞLEM GÜNLÜĞÜ");all.setOnClickListener(v->showActivity421());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.setMargins(0,dp(5),0,dp(10));b.addView(all,lp);}
    private ScrollView findScroll421(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll421(g.getChildAt(i));if(s!=null)return s;}}return null;}

    private void showActivity421(){page="ACTIVITY421";base("İŞLEM GÜNLÜĞÜ",true);ScrollView s=scroll();LinearLayout b=box(s);Cursor c=db.getReadableDatabase().rawQuery("SELECT userEmail,action,entityType,entityId,detail,createdAt,cloudSynced FROM activity_log_local ORDER BY id DESC LIMIT 300",null);while(c.moveToNext()){String who=c.getString(0);if(who==null||who.isEmpty())who=currentEmail421();String x=c.getString(5)+"\n"+who+"\n"+c.getString(1)+" • "+c.getString(2)+(c.getString(4).isEmpty()?"":"\n"+c.getString(4))+"\n"+(c.getInt(6)==1?"BULUTA AKTARILDI":"BULUTA AKTARILMAYI BEKLİYOR");TextView t=tv(x,12,Color.DKGRAY,false);t.setPadding(dp(10),dp(8),dp(10),dp(8));t.setBackground(round(Color.WHITE,9));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(6));b.addView(t,lp);}c.close();}

    @Override void goBack(){if("ACTIVITY421".equals(page)){showHome();return;}super.goBack();}
}