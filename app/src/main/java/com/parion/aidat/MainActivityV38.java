package com.parion.aidat;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivityV38 extends MainActivityV37 {
    static final String SUPABASE_URL="https://ujjtsemybslznmzadzvk.supabase.co";
    static final String SUPABASE_KEY="sb_publishable_tYGPzcWkdcxwjbBr3hnitg_ce7mVdfM";
    static final String PREF="parion_cloud_session";
    SharedPreferences cloudPrefs;
    volatile boolean syncing=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        cloudPrefs=getSharedPreferences(PREF,MODE_PRIVATE);
        String token=cloudPrefs.getString("access_token","");
        if(token.isEmpty()) showLogin();
        else {
            showHome();
            syncFromCloud(false);
        }
    }

    void showLogin(){
        page="LOGIN"; currentAthlete=-1; base("PARION • ONLINE GİRİŞ",false);
        ScrollView sv=scroll(); LinearLayout box=box(sv); box.setPadding(dp(18),dp(24),dp(18),dp(18));
        ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.parion_logo); logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE); box.addView(logo,new LinearLayout.LayoutParams(-1,dp(120)));
        TextView title=tv("PARION SPOR OKULU",22,BLACK,true); title.setGravity(Gravity.CENTER); box.addView(title);
        TextView info=tv("Merkezi veritabanına bağlanmak için hesabınızla giriş yapın.",13,Color.DKGRAY,false); info.setGravity(Gravity.CENTER); info.setPadding(0,dp(4),0,dp(14)); box.addView(info);
        EditText email=new EditText(this); email.setHint("E-posta"); email.setSingleLine(true); email.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); email.setText("volleyavuz@gmail.com"); box.addView(email,new LinearLayout.LayoutParams(-1,dp(56)));
        EditText pass=new EditText(this); pass.setHint("Şifre"); pass.setSingleLine(true); pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); box.addView(pass,new LinearLayout.LayoutParams(-1,dp(56)));
        Button login=btn("GİRİŞ YAP"); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58)); lp.setMargins(0,dp(12),0,dp(6)); box.addView(login,lp);
        Button signup=btn("İLK KAYIT / HESAP OLUŞTUR"); box.addView(signup,new LinearLayout.LayoutParams(-1,dp(56)));
        TextView note=tv("İlk kullanımda hesabınızı oluşturun. E-posta doğrulaması istenirse gelen kutunuzdaki bağlantıyı onayladıktan sonra giriş yapın.",12,Color.DKGRAY,false); note.setPadding(dp(4),dp(12),dp(4),0); box.addView(note);
        login.setOnClickListener(v->auth(email.getText().toString().trim(),pass.getText().toString(),false));
        signup.setOnClickListener(v->auth(email.getText().toString().trim(),pass.getText().toString(),true));
    }

    void auth(String email,String password,boolean signup){
        if(email.isEmpty()||password.length()<6){toast("Geçerli e-posta ve en az 6 karakter şifre girin.");return;}
        final android.app.AlertDialog wait=new android.app.AlertDialog.Builder(this).setMessage(signup?"Hesap oluşturuluyor...":"Giriş yapılıyor...").setCancelable(false).create(); wait.show();
        new Thread(()->{
            try{
                JSONObject body=new JSONObject(); body.put("email",email); body.put("password",password); body.put("data",new JSONObject().put("full_name","YAVUZ KAYA"));
                String endpoint=signup?"/auth/v1/signup":"/auth/v1/token?grant_type=password";
                HttpResult r=request("POST",SUPABASE_URL+endpoint,body.toString(),null);
                JSONObject j=safeObject(r.body);
                if(r.code>=200&&r.code<300){
                    String access=j.optString("access_token",""); String refresh=j.optString("refresh_token","");
                    if(!access.isEmpty()){
                        cloudPrefs.edit().putString("access_token",access).putString("refresh_token",refresh).putString("email",email).apply();
                        runOnUiThread(()->{wait.dismiss();toast("Giriş başarılı. Online veriler eşitleniyor.");showHome();syncFromCloud(true);});
                    }else runOnUiThread(()->{wait.dismiss();toast("Hesap oluşturuldu. E-posta doğrulamasından sonra GİRİŞ YAP seçeneğini kullanın.");});
                }else {String msg=j.optString("msg",j.optString("error_description",j.optString("message","İşlem başarısız")));runOnUiThread(()->{wait.dismiss();toast(msg);});}
            }catch(Exception e){runOnUiThread(()->{wait.dismiss();toast("Bağlantı hatası: "+shortMsg(e));});}
        }).start();
    }

    @Override void showHome(){
        super.showHome();
        if(cloudPrefs==null) return;
        TextView cloud=tv("☁ ONLINE • "+cloudPrefs.getString("email","").toUpperCase(new Locale("tr","TR")),11,Color.rgb(0,120,70),true);
        cloud.setGravity(Gravity.CENTER); cloud.setPadding(0,dp(2),0,dp(5));
        if(root.getChildCount()>0) root.addView(cloud,0); else root.addView(cloud);
        cloud.setOnClickListener(v->showCloudMenu());
    }

    void showCloudMenu(){
        String[] items={"ŞİMDİ SENKRONİZE ET","OTURUMU KAPAT"};
        new android.app.AlertDialog.Builder(this).setTitle("ONLINE HESAP").setItems(items,(d,w)->{if(w==0)syncFromCloud(true);else{cloudPrefs.edit().clear().apply();showLogin();}}).show();
    }

    void syncFromCloud(boolean announce){
        if(syncing)return; final String token=cloudPrefs.getString("access_token",""); if(token.isEmpty())return; syncing=true;
        if(announce)toast("Senkronizasyon başladı...");
        new Thread(()->{
            try{
                HttpResult ar=getAuthed("/rest/v1/mobile_athletes?select=*");
                if(ar.code==401 && refreshSession()) ar=getAuthed("/rest/v1/mobile_athletes?select=*");
                if(ar.code<200||ar.code>=300)throw new IOException("Sporcu verisi alınamadı ("+ar.code+")");
                JSONArray athletes=new JSONArray(ar.body);
                HttpResult pr=getAuthed("/rest/v1/mobile_payments_legacy?select=*&year=eq.2026");
                if(pr.code<200||pr.code>=300)throw new IOException("Ödeme verisi alınamadı ("+pr.code+")");
                JSONArray payments=new JSONArray(pr.body);
                int[] counts=applyCloudCache(athletes,payments);
                runOnUiThread(()->{syncing=false;if(announce)toast("Senkronizasyon tamamlandı: "+counts[0]+" sporcu, "+counts[1]+" ödeme.");showHome();});
            }catch(Exception e){runOnUiThread(()->{syncing=false;if(announce)toast("Senkronizasyon hatası: "+shortMsg(e));});}
        }).start();
    }

    int[] applyCloudCache(JSONArray athletes,JSONArray payments)throws Exception{
        SQLiteDatabase d=db.getWritableDatabase(); int ac=0,pc=0; d.beginTransaction();
        try{
            for(int i=0;i<athletes.length();i++){
                JSONObject a=athletes.getJSONObject(i); long id=a.optLong("legacy_id",-1); if(id<=0)continue;
                ContentValues v=new ContentValues();
                put(v,"seq",a,"seq");put(v,"birthYear",a,"birth_year");put(v,"birthDate",a,"birth_date");put(v,"name",a,"name");put(v,"category",a,"category");put(v,"status",a,"status");put(v,"monthlyFee",a,"monthly_fee");put(v,"sibling",a,"sibling");put(v,"tshirtQty",a,"tshirt_qty");put(v,"tshirtPaid",a,"tshirt_paid");put(v,"tracksuitQty",a,"tracksuit_qty");put(v,"tracksuitPaid",a,"tracksuit_paid");put(v,"notes",a,"notes");put(v,"phone",a,"phone");put(v,"motherName",a,"mother_name");put(v,"motherPhone",a,"mother_phone");put(v,"fatherName",a,"father_name");put(v,"fatherPhone",a,"father_phone");put(v,"startDate",a,"start_date");put(v,"endDate",a,"end_date");put(v,"restartDate",a,"restart_date");put(v,"photo",a,"photo");
                int n=d.update("athletes",v,"id=?",new String[]{String.valueOf(id)}); if(n>0)ac++;
            }
            for(int i=0;i<payments.length();i++){
                JSONObject p=payments.getJSONObject(i); long aid=p.optLong("legacy_id",-1); int y=p.optInt("year",0),m=p.optInt("month",0); if(aid<=0||y<=0||m<1||m>12)continue;
                ContentValues v=new ContentValues();v.put("athleteId",aid);v.put("year",y);v.put("month",m);v.put("marker",p.optString("marker",""));v.put("amount",p.optInt("amount",0));d.insertWithOnConflict("payments",null,v,SQLiteDatabase.CONFLICT_REPLACE);pc++;
            }
            d.setTransactionSuccessful();
        }finally{d.endTransaction();}
        return new int[]{ac,pc};
    }

    void put(ContentValues v,String col,JSONObject o,String key){if(o.isNull(key))return;Object x=o.opt(key);if(x instanceof Number)v.put(col,((Number)x).intValue());else v.put(col,o.optString(key,""));}

    boolean refreshSession(){
        try{
            String refresh=cloudPrefs.getString("refresh_token","");if(refresh.isEmpty())return false;
            JSONObject body=new JSONObject().put("refresh_token",refresh);
            HttpResult r=request("POST",SUPABASE_URL+"/auth/v1/token?grant_type=refresh_token",body.toString(),null);if(r.code<200||r.code>=300)return false;
            JSONObject j=new JSONObject(r.body);String access=j.optString("access_token","");if(access.isEmpty())return false;
            cloudPrefs.edit().putString("access_token",access).putString("refresh_token",j.optString("refresh_token",refresh)).apply();return true;
        }catch(Exception e){return false;}
    }

    HttpResult getAuthed(String path)throws Exception{return request("GET",SUPABASE_URL+path,null,cloudPrefs.getString("access_token",""));}
    HttpResult request(String method,String url,String body,String bearer)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestMethod(method);c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("apikey",SUPABASE_KEY);c.setRequestProperty("Accept","application/json");if(bearer!=null&&!bearer.isEmpty())c.setRequestProperty("Authorization","Bearer "+bearer);if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream o=c.getOutputStream()){o.write(body.getBytes(StandardCharsets.UTF_8));}}
        int code=c.getResponseCode();InputStream in=code>=400?c.getErrorStream():c.getInputStream();String text=readAll(in);c.disconnect();return new HttpResult(code,text);
    }
    String readAll(InputStream in)throws Exception{if(in==null)return "";ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] x=new byte[8192];int n;while((n=in.read(x))>0)b.write(x,0,n);in.close();return b.toString(StandardCharsets.UTF_8.name());}
    JSONObject safeObject(String s){try{return new JSONObject(s);}catch(Exception e){return new JSONObject();}}
    String shortMsg(Exception e){String x=e.getMessage();return x==null?e.getClass().getSimpleName():(x.length()>90?x.substring(0,90):x);}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    static class HttpResult{int code;String body;HttpResult(int c,String b){code=c;body=b;}}
}
