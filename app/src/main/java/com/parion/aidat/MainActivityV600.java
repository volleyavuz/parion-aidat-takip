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
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Parion v4.0.0
 * - cloud-first: no athlete/payment seed inside APK
 * - v62 UI/navigation chain is preserved through V416/V505
 * - full snapshot upload is permanently blocked
 * - athlete/payment/fee writes are per-record and hash based
 * - materials keep their row-based REST sync
 * - summer/winter call cards are shown on Home
 */
public class MainActivityV600 extends MainActivityV505 {
    private static final String V4_PREF="parion_v4_sync_state";
    private static final String V4_READY="baseline_ready";
    private final AtomicBoolean pushRunning600=new AtomicBoolean(false);
    private volatile boolean awaitingPull600=false;
    private volatile boolean sessionPullComplete600=false;
    private volatile boolean materialKick600=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
    }

    private SharedPreferences v4p(){return getSharedPreferences(V4_PREF,MODE_PRIVATE);}

    /**
     * V500 was intentionally read-only. V4 re-enables normal writes but keeps
     * the old whole-database snapshot endpoint blocked forever.
     */
    @Override HttpResult request(String method,String url,String body,String bearer)throws Exception{
        if(url!=null&&url.contains("/rpc/parion_sync_mobile_snapshot")){
            return new HttpResult(409,"{\"error\":\"PARION_V4_FULL_SNAPSHOT_DISABLED\"}");
        }
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
        c.setRequestMethod(method);c.setConnectTimeout(15000);c.setReadTimeout(25000);
        c.setRequestProperty("apikey",SUPABASE_KEY);c.setRequestProperty("Accept","application/json");
        if(bearer!=null&&!bearer.isEmpty())c.setRequestProperty("Authorization","Bearer "+bearer);
        if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream o=c.getOutputStream()){o.write(body.getBytes(StandardCharsets.UTF_8));}}
        int code=c.getResponseCode();InputStream in=code>=400?c.getErrorStream():c.getInputStream();String text=readAll(in);c.disconnect();return new HttpResult(code,text);
    }

    @Override void syncFromCloud(boolean announce){
        // If this device has local changes from a previous/offline session,
        // retry those small per-record writes before accepting a new cloud snapshot.
        if(v4p().getBoolean(V4_READY,false)&&!sessionPullComplete600&&!pushRunning600.get()){
            new Thread(()->{
                pushAllChanged600(false);
                runOnUiThread(()->{awaitingPull600=true;MainActivityV600.super.syncFromCloud(announce);});
            },"parion-v4-prepull-flush").start();
            return;
        }
        awaitingPull600=true;
        super.syncFromCloud(announce);
    }

    @Override void showCloudMenu(){
        String[] items={"ŞİMDİ SENKRONİZE ET","OTURUMU KAPAT"};
        new AlertDialog.Builder(this).setTitle("ONLINE HESAP • ÇİFT YÖNLÜ").setItems(items,(d,w)->{
            if(w==0)syncFromCloud(true);
            else{cloudPrefs.edit().clear().apply();sessionPullComplete600=false;showLogin();}
        }).show();
    }

    @Override void showHome(){
        super.showHome();
        removeReadOnlyBanner600(root);
        restoreHomeMediaActions600();
        addSeasonCards600();

        // V501 calls showHome after a successful cloud->local replace.
        if(awaitingPull600&&!syncing){
            awaitingPull600=false;sessionPullComplete600=true;
            baselineAll600();
            kickMaterialSync600();
        }else if(sessionPullComplete600&&v4p().getBoolean(V4_READY,false)){
            pushAllChangedAsync600(false);
        }

        TextView mode=tv("☁ ÇİFT YÖNLÜ • SADECE DEĞİŞEN VERİLER",11,Color.rgb(0,120,70),true);
        mode.setGravity(Gravity.CENTER);mode.setPadding(dp(6),dp(4),dp(6),dp(6));
        root.addView(mode,Math.min(1,root.getChildCount()));
    }

    @Override void showProfile(long id){
        super.showProfile(id);
        removeReadOnlyBanner600(root);
        restoreProfileMediaActions600(id);
        if(sessionPullComplete600&&v4p().getBoolean(V4_READY,false))pushChangedAthleteAsync600(id);
    }

    @Override void form(long id){
        super.form(id);
        removeReadOnlyBanner600(root);
        restoreFormMediaActions600(id);
        addSeasonControls600(id);
    }

    /** Re-enable the media listeners that V500 disabled during recovery mode. */
    private void restoreHomeMediaActions600(){
        invokePrivate600(MainActivityV405.class,"patchHomeMediaCards405",new Class[]{View.class},root);
        invokePrivate600(MainActivityV416.class,"patchMissingCard416",new Class[]{View.class},root);
    }
    private void restoreProfileMediaActions600(long id){
        invokePrivate600(MainActivityV416.class,"patchProfileForm416",new Class[]{View.class,long.class},root,id);
        invokePrivate600(MainActivityV412.class,"patchCloudFormButtons412",new Class[]{View.class,long.class},root,id);
    }
    private void restoreFormMediaActions600(long id){
        if(id>0)invokePrivate600(MainActivityV412.class,"patchPhotoButtons412",new Class[]{View.class,long.class},root,id);
    }
    private void invokePrivate600(Class<?> cls,String name,Class<?>[] sig,Object...args){
        try{Method m=cls.getDeclaredMethod(name,sig);m.setAccessible(true);m.invoke(this,args);}catch(Exception ignored){}
    }

    private void removeReadOnlyBanner600(View v){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=g.getChildCount()-1;i>=0;i--){
            View c=g.getChildAt(i);
            if(c instanceof TextView&&String.valueOf(((TextView)c).getText()).contains("BULUTA YAZMA KAPALI")){g.removeViewAt(i);continue;}
            removeReadOnlyBanner600(c);
        }
    }

    /** ---------------- Summer / winter call flags ---------------- */
    private void addSeasonCards600(){
        ScrollView sv=findScroll600(root);if(sv==null)return;LinearLayout b=box(sv);
        addSeasonCard600(b,true);
        addSeasonCard600(b,false);
    }
    private void addSeasonCard600(LinearLayout b,boolean summer){
        int n=countSeason600(summer);
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER);card.setPadding(dp(8),dp(9),dp(8),dp(9));card.setBackground(round(Color.WHITE,12));
        TextView number=tv(String.valueOf(n),24,n>0?Color.rgb(2,119,189):GREEN,true);number.setGravity(Gravity.CENTER);card.addView(number,new LinearLayout.LayoutParams(-1,dp(46)));
        TextView label=tv(summer?"YAZIN ARANACAK":"KIŞIN ARANACAK",12,Color.DKGRAY,true);label.setGravity(Gravity.CENTER);card.addView(label,new LinearLayout.LayoutParams(-1,dp(38)));
        card.setOnClickListener(v->showSeasonList600(summer));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,dp(7));b.addView(card,lp);
    }
    private int countSeason600(boolean summer){
        String col=summer?"summerCall":"winterCall";Cursor c=db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM athletes WHERE status='AKTİF' AND COALESCE("+col+",0)=1 AND TRIM(COALESCE(deletedAt,''))=''",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;
    }
    private void showSeasonList600(boolean summer){
        page=summer?"CALL_SUMMER_V4":"CALL_WINTER_V4";base(summer?"YAZIN ARANACAK":"KIŞIN ARANACAK",true);ScrollView sv=scroll();LinearLayout b=box(sv);
        String col=summer?"summerCall":"winterCall";Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE status='AKTİF' AND COALESCE("+col+",0)=1 AND TRIM(COALESCE(deletedAt,''))='' ORDER BY name COLLATE NOCASE",null);int n=0;
        while(c.moveToNext()){A x=a(c);row(b,x,null,0);n++;}c.close();if(n==0)b.addView(tv("Bu listede sporcu bulunmuyor.",14,Color.DKGRAY,true));
    }
    private void addSeasonControls600(long id){
        ScrollView sv=findScroll600(root);if(sv==null)return;LinearLayout b=box(sv);
        TextView h=tv("ARAMA PLANLAMASI",15,BLACK,true);h.setPadding(dp(4),dp(14),dp(4),dp(4));b.addView(h);
        if(id<0){
            TextView note=tv("YAZIN / KIŞIN ARANACAK işaretleri sporcu kaydı oluşturulduktan sonra düzenleme ekranından seçilebilir.",12,Color.DKGRAY,false);b.addView(note);return;
        }
        Cursor c=db.athlete(id);boolean summer=false,winter=false;if(c.moveToFirst()){int si=c.getColumnIndex("summerCall"),wi=c.getColumnIndex("winterCall");summer=si>=0&&c.getInt(si)!=0;winter=wi>=0&&c.getInt(wi)!=0;}c.close();
        CheckBox s=new CheckBox(this);s.setText("YAZIN ARANACAK");s.setChecked(summer);CheckBox w=new CheckBox(this);w.setText("KIŞIN ARANACAK");w.setChecked(winter);b.addView(s);b.addView(w);
        Button save=btn("ARAMA İŞARETLERİNİ KAYDET");save.setOnClickListener(v->{ContentValues cv=new ContentValues();cv.put("summerCall",s.isChecked()?1:0);cv.put("winterCall",w.isChecked()?1:0);db.getWritableDatabase().update("athletes",cv,"id=?",new String[]{String.valueOf(id)});toast("ARAMA İŞARETLERİ KAYDEDİLDİ.");pushChangedAthleteAsync600(id);});b.addView(save,new LinearLayout.LayoutParams(-1,dp(52)));
    }
    private ScrollView findScroll600(View v){if(v instanceof ScrollView)return (ScrollView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=findScroll600(g.getChildAt(i));if(s!=null)return s;}}return null;}

    /** ---------------- Material cloud merge ---------------- */
    private void kickMaterialSync600(){
        if(materialKick600)return;materialKick600=true;
        invokePrivate600(MainActivityV393.class,"syncMaterialsFromCloud",new Class[]{boolean.class},false);
        root.postDelayed(()->materialKick600=false,4000);
    }

    /** ---------------- Per-record delta sync ---------------- */
    private void baselineAll600(){
        try{
            SharedPreferences.Editor e=v4p().edit().clear();SQLiteDatabase d=db.getReadableDatabase();
            Cursor a=d.rawQuery("SELECT * FROM athletes",null);while(a.moveToNext()){long id=a.getLong(a.getColumnIndexOrThrow("id"));e.putString("a:"+id,hashAthlete600(a));}a.close();
            ArrayList<String> pkeys=new ArrayList<>();Cursor p=d.rawQuery("SELECT athleteId,year,month,marker,amount FROM payments",null);while(p.moveToNext()){String k=payKey600(p.getLong(0),p.getInt(1),p.getInt(2));pkeys.add(k);e.putString("p:"+k,hashPayment600(p));}p.close();
            ArrayList<String> fkeys=new ArrayList<>();Cursor f=d.rawQuery("SELECT athleteId,year,effectiveMonth,fee FROM fee_history",null);while(f.moveToNext()){String k=feeKey600(f.getLong(0),f.getInt(1),f.getInt(2));fkeys.add(k);e.putString("f:"+k,hashFee600(f));}f.close();
            e.putString("knownP",join600(pkeys)).putString("knownF",join600(fkeys)).putBoolean(V4_READY,true).putLong("baselineAt",System.currentTimeMillis()).apply();
        }catch(Exception ignored){}
    }

    private void pushAllChangedAsync600(boolean announce){
        if(!pushRunning600.compareAndSet(false,true))return;
        new Thread(()->{try{pushAllChangedBody600(announce);}finally{pushRunning600.set(false);}},"parion-v4-delta-all").start();
    }
    private void pushAllChanged600(boolean announce){
        if(!pushRunning600.compareAndSet(false,true))return;
        try{pushAllChangedBody600(announce);}finally{pushRunning600.set(false);}
    }
    private void pushAllChangedBody600(boolean announce){
        if(!v4p().getBoolean(V4_READY,false)||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty())return;
        int changed=0;SQLiteDatabase d=db.getReadableDatabase();
        try{
            Cursor a=d.rawQuery("SELECT * FROM athletes",null);while(a.moveToNext()){long id=a.getLong(a.getColumnIndexOrThrow("id"));String h=hashAthlete600(a);if(!h.equals(v4p().getString("a:"+id,""))&&pushAthleteCursor600(a)) {v4p().edit().putString("a:"+id,h).apply();changed++;}}a.close();

            HashSet<String> currentP=new HashSet<>();Cursor p=d.rawQuery("SELECT athleteId,year,month,marker,amount FROM payments",null);while(p.moveToNext()){long id=p.getLong(0);int y=p.getInt(1),m=p.getInt(2);String k=payKey600(id,y,m);currentP.add(k);String h=hashPayment600(p);if(!h.equals(v4p().getString("p:"+k,""))&&pushPayment600(id,y,m,p.getString(3),p.getInt(4))){v4p().edit().putString("p:"+k,h).apply();changed++;}}p.close();
            changed+=deleteMissingPayments600(currentP);

            HashSet<String> currentF=new HashSet<>();Cursor f=d.rawQuery("SELECT athleteId,year,effectiveMonth,fee FROM fee_history",null);while(f.moveToNext()){long id=f.getLong(0);int y=f.getInt(1),m=f.getInt(2);String k=feeKey600(id,y,m);currentF.add(k);String h=hashFee600(f);if(!h.equals(v4p().getString("f:"+k,""))&&pushFee600(id,y,m,f.getInt(3))){v4p().edit().putString("f:"+k,h).apply();changed++;}}f.close();
            changed+=deleteMissingFees600(currentF);

            v4p().edit().putString("knownP",join600(currentP)).putString("knownF",join600(currentF)).apply();
            if(announce){final int c=changed;runOnUiThread(()->toast(c==0?"DEĞİŞİKLİK YOK.":c+" DEĞİŞİKLİK BULUTA AKTARILDI."));}
        }catch(Exception ignored){}
    }

    private void pushChangedAthleteAsync600(long id){
        if(id<=0||!sessionPullComplete600||!v4p().getBoolean(V4_READY,false))return;
        new Thread(()->pushChangedAthlete600(id),"parion-v4-athlete-"+id).start();
    }
    private void pushChangedAthlete600(long id){
        try{Cursor a=db.athlete(id);if(a.moveToFirst()){String h=hashAthlete600(a);if(!h.equals(v4p().getString("a:"+id,""))&&pushAthleteCursor600(a))v4p().edit().putString("a:"+id,h).apply();}a.close();
            Cursor p=db.getReadableDatabase().rawQuery("SELECT athleteId,year,month,marker,amount FROM payments WHERE athleteId=?",new String[]{String.valueOf(id)});while(p.moveToNext()){int y=p.getInt(1),m=p.getInt(2);String k=payKey600(id,y,m),h=hashPayment600(p);if(!h.equals(v4p().getString("p:"+k,""))&&pushPayment600(id,y,m,p.getString(3),p.getInt(4)))v4p().edit().putString("p:"+k,h).apply();}p.close();
            Cursor f=db.getReadableDatabase().rawQuery("SELECT athleteId,year,effectiveMonth,fee FROM fee_history WHERE athleteId=?",new String[]{String.valueOf(id)});while(f.moveToNext()){int y=f.getInt(1),m=f.getInt(2);String k=feeKey600(id,y,m),h=hashFee600(f);if(!h.equals(v4p().getString("f:"+k,""))&&pushFee600(id,y,m,f.getInt(3)))v4p().edit().putString("f:"+k,h).apply();}f.close();
        }catch(Exception ignored){}
    }

    private boolean pushAthleteCursor600(Cursor c){
        try{
            JSONObject p=new JSONObject();long id=c.getLong(c.getColumnIndexOrThrow("id"));p.put("legacy_id",id);
            jcol600(p,"seq",c,"seq");jcol600(p,"birth_year",c,"birthYear");jcol600(p,"birth_date",c,"birthDate");jcol600(p,"name",c,"name");jcol600(p,"category",c,"category");jcol600(p,"status",c,"status");jcol600(p,"monthly_fee",c,"monthlyFee");jcol600(p,"sibling",c,"sibling");jcol600(p,"tshirt_qty",c,"tshirtQty");jcol600(p,"tshirt_paid",c,"tshirtPaid");jcol600(p,"tracksuit_qty",c,"tracksuitQty");jcol600(p,"tracksuit_paid",c,"tracksuitPaid");jcol600(p,"notes",c,"notes");jcol600(p,"phone",c,"phone");jcol600(p,"mother_name",c,"motherName");jcol600(p,"mother_phone",c,"motherPhone");jcol600(p,"father_name",c,"fatherName");jcol600(p,"father_phone",c,"fatherPhone");jcol600(p,"start_date",c,"startDate");jcol600(p,"end_date",c,"endDate");jcol600(p,"restart_date",c,"restartDate");jcol600(p,"tckn",c,"tckn");
            int si=c.getColumnIndex("summerCall"),wi=c.getColumnIndex("winterCall");p.put("summer_call",si>=0&&c.getInt(si)!=0);p.put("winter_call",wi>=0&&c.getInt(wi)!=0);
            HttpResult r=rpc600("parion_upsert_mobile_athlete",new JSONObject().put("p",p));return ok600(r);
        }catch(Exception e){return false;}
    }
    private boolean pushPayment600(long id,int y,int m,String marker,int amount){try{return ok600(rpc600("parion_upsert_mobile_payment",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m).put("p_marker",marker==null?"":marker).put("p_amount",amount)));}catch(Exception e){return false;}}
    private boolean pushFee600(long id,int y,int m,int fee){try{return ok600(rpc600("parion_upsert_mobile_fee",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m).put("p_fee",fee)));}catch(Exception e){return false;}}

    private int deleteMissingPayments600(Set<String> current){int n=0;for(String k:split600(v4p().getString("knownP",""))){if(current.contains(k))continue;String[] z=k.split(":");if(z.length!=3)continue;try{long id=Long.parseLong(z[0]);int y=Integer.parseInt(z[1]),m=Integer.parseInt(z[2]);if(ok600(rpc600("parion_delete_mobile_payment",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m)))){v4p().edit().remove("p:"+k).apply();n++;}}catch(Exception ignored){}}return n;}
    private int deleteMissingFees600(Set<String> current){int n=0;for(String k:split600(v4p().getString("knownF",""))){if(current.contains(k))continue;String[] z=k.split(":");if(z.length!=3)continue;try{long id=Long.parseLong(z[0]);int y=Integer.parseInt(z[1]),m=Integer.parseInt(z[2]);if(ok600(rpc600("parion_delete_mobile_fee",new JSONObject().put("p_legacy_id",id).put("p_year",y).put("p_month",m)))){v4p().edit().remove("f:"+k).apply();n++;}}catch(Exception ignored){}}return n;}

    private HttpResult rpc600(String name,JSONObject body)throws Exception{
        String token=cloudPrefs.getString("access_token","");HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/"+name,body.toString(),token);
        if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/"+name,body.toString(),token);}return r;
    }
    private boolean ok600(HttpResult r){return r!=null&&r.code>=200&&r.code<300;}

    private void jcol600(JSONObject o,String key,Cursor c,String col)throws Exception{int i=c.getColumnIndex(col);if(i<0||c.isNull(i)){o.put(key,"");return;}if(c.getType(i)==Cursor.FIELD_TYPE_INTEGER)o.put(key,c.getLong(i));else o.put(key,c.getString(i)==null?"":c.getString(i));}
    private String payKey600(long id,int y,int m){return id+":"+y+":"+m;}
    private String feeKey600(long id,int y,int m){return id+":"+y+":"+m;}
    private String hashAthlete600(Cursor c){StringBuilder s=new StringBuilder();String[] cols={"id","seq","birthYear","birthDate","name","category","status","monthlyFee","sibling","tshirtQty","tshirtPaid","tracksuitQty","tracksuitPaid","notes","phone","motherName","motherPhone","fatherName","fatherPhone","startDate","endDate","restartDate","tckn","summerCall","winterCall","deletedAt"};for(String col:cols){int i=c.getColumnIndex(col);s.append(col).append('=').append(i<0||c.isNull(i)?"":c.getString(i)).append('|');}return sha600(s.toString());}
    private String hashPayment600(Cursor c){return sha600(c.getLong(0)+"|"+c.getInt(1)+"|"+c.getInt(2)+"|"+(c.getString(3)==null?"":c.getString(3))+"|"+c.getInt(4));}
    private String hashFee600(Cursor c){return sha600(c.getLong(0)+"|"+c.getInt(1)+"|"+c.getInt(2)+"|"+c.getInt(3));}
    private String sha600(String s){try{byte[] d=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:d)b.append(String.format(Locale.US,"%02x",x&255));return b.toString();}catch(Exception e){return String.valueOf(s.hashCode());}}
    private String join600(Collection<String> xs){StringBuilder b=new StringBuilder();for(String x:xs){if(b.length()>0)b.append(',');b.append(x);}return b.toString();}
    private Set<String> split600(String s){HashSet<String>x=new HashSet<>();if(s==null||s.trim().isEmpty())return x;for(String k:s.split(","))if(!k.trim().isEmpty())x.add(k.trim());return x;}

    @Override void goBack(){if("CALL_SUMMER_V4".equals(page)||"CALL_WINTER_V4".equals(page)){showHome();return;}super.goBack();}
}
