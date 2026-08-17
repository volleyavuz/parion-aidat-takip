package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

public class MainActivityV417 extends MainActivityV416 {
    private static final int REQ_FORM_PROFILE_417=4171;
    private static final int REQ_FORM_MISSING_417=4172;
    private static final int REQ_PHOTO_PROFILE_417=4173;
    private static final int REQ_PHOTO_MISSING_417=4174;
    private final ExecutorService media417=Executors.newSingleThreadExecutor();
    private long target417=-1;

    @Override void showProfile(long id){super.showProfile(id);patchProfileForm417(root,id);}
    @Override void form(long id){super.form(id);if(id>0)patchPhotoControls417(root,id);}
    @Override void showHome(){super.showHome();patchHomeCards417(root);}

    private void patchProfileForm417(View v,long id){
        if(v instanceof Button){Button b=(Button)v;String s=String.valueOf(b.getText()).toUpperCase(new Locale("tr","TR"));if(s.contains("KAYIT FORMU EKLE")||s.contains("KAYIT FORMUNU GÜNCELLE")||s.contains("KAYIT FORMUNU DEĞİŞTİR"))b.setOnClickListener(x->{target417=id;pick417(REQ_FORM_PROFILE_417);});}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchProfileForm417(g.getChildAt(i),id);}
    }
    private void patchPhotoControls417(View v,long id){
        if(v instanceof Button){Button b=(Button)v;String s=String.valueOf(b.getText()).toUpperCase(new Locale("tr","TR"));if(s.contains("FOTOĞRAF EKLE")||s.contains("FOTOĞRAFI DEĞİŞTİR"))b.setOnClickListener(x->{target417=id;pick417(REQ_PHOTO_PROFILE_417);});}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchPhotoControls417(g.getChildAt(i),id);}
    }
    private void patchHomeCards417(View v){
        if(v instanceof TextView){String s=String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR"));if(s.contains("KAYIT FORMU OLMAYAN AKTİF SPORCULAR")){View p=(View)v.getParent();p.setOnClickListener(x->showMissingForms417());}else if(s.contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")){View p=(View)v.getParent();p.setOnClickListener(x->showMissingPhotos417());}}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchHomeCards417(g.getChildAt(i));}
    }

    private void showMissingForms417(){page="MISSING_FORMS_417";base("KAYIT FORMU OLMAYAN AKTİF SPORCULAR",true);TextView info=tv("SPORCUYA DOKUNUN; GALERİDEN KAYIT FORMU GÖRSELİNİ SEÇİN.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(formMap413().containsKey(x.id))continue;addMissingRow417(b,x,false);n++;}c.close();if(n==0)b.addView(tv("KAYIT FORMU OLMAYAN AKTİF SPORCU BULUNMUYOR.",14,GREEN,true));}
    private void showMissingPhotos417(){page="MISSING_PHOTOS_417";base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);TextView info=tv("SPORCUYA DOKUNUN; GALERİDEN FOTOĞRAFI SEÇİN.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(hasActualPhoto417(x))continue;addMissingRow417(b,x,true);n++;}c.close();if(n==0)b.addView(tv("FOTOĞRAFI OLMAYAN AKTİF SPORCU BULUNMUYOR.",14,GREEN,true));}
    private boolean hasActualPhoto417(A x){if(photoMap413().containsKey(x.id))return true;String p=x.photo==null?"":x.photo.trim();return (p.startsWith("CLOUD:")||p.startsWith("USER:"))&&!p.toUpperCase(Locale.ROOT).contains("0000 BOS");}
    private void addMissingRow417(LinearLayout b,A x,boolean photo){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,x.photo);r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+(photo?" • FOTOĞRAF EKLE":" • KAYIT FORMU EKLE"),12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));r.setOnClickListener(v->{target417=x.id;pick417(photo?REQ_PHOTO_MISSING_417:REQ_FORM_MISSING_417);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);}

    private void pick417(int req){Intent i;if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}else{i=new Intent(Intent.ACTION_PICK);i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");}startActivityForResult(i,req);}

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_FORM_PROFILE_417||requestCode==REQ_FORM_MISSING_417||requestCode==REQ_PHOTO_PROFILE_417||requestCode==REQ_PHOTO_MISSING_417){long id=target417;target417=-1;boolean isForm=requestCode==REQ_FORM_PROFILE_417||requestCode==REQ_FORM_MISSING_417;boolean missing=requestCode==REQ_FORM_MISSING_417||requestCode==REQ_PHOTO_MISSING_417;if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0)uploadMedia417(id,data.getData(),isForm,missing);else returnAfter417(id,isForm,missing);return;}super.onActivityResult(requestCode,resultCode,data);
    }

    private void uploadMedia417(long id,Uri uri,boolean isForm,boolean missing){
        toast(isForm?"KAYIT FORMU YÜKLENİYOR...":"SPORCU FOTOĞRAFI YÜKLENİYOR...");
        media417.execute(()->{try{
            String token=sessionToken417();if(token.isEmpty())throw new IOException("OTURUM YOK");ensureAthleteCloud413(id,token);
            byte[] jpg=jpeg414(uri,isForm?2400:1600,isForm?82:84);if(jpg==null||jpg.length==0)throw new IOException("GÖRSEL HAZIRLANAMADI");
            String bucket=isForm?"registration-forms":"athlete-photos";String path="user/"+id+"/"+(isForm?"form_":"photo_")+System.currentTimeMillis()+".jpg";
            UploadResult417 up=put417(bucket,path,jpg,token);if(up.code==401&&refreshSession()){token=sessionToken417();up=put417(bucket,path,jpg,token);}if(up.code<200||up.code>=300)throw new IOException("STORAGE "+up.code+cleanError417(up.body));
            String actual=bindAndVerify417(id,isForm?"form":"photo",path,token);if(!path.equals(actual))throw new IOException("BULUT DOĞRULAMA UYUŞMADI");
            if(isForm)formMap413().put(id,path);else{photoMap413().put(id,path);db.getWritableDatabase().execSQL("UPDATE athletes SET photo=? WHERE id=?",new Object[]{"CLOUD:"+path,id});}
            runOnUiThread(()->{toast(isForm?"KAYIT FORMU BULUTA KAYDEDİLDİ.":"SPORCU FOTOĞRAFI BULUTA KAYDEDİLDİ.");returnAfter417(id,isForm,missing);});
        }catch(Exception e){String m=e.getMessage();runOnUiThread(()->{toast((isForm?"KAYIT FORMU":"FOTOĞRAF")+" YÜKLENEMEDİ: "+(m==null?"BAĞLANTI":m));returnAfter417(id,isForm,missing);});}});
    }

    private String sessionToken417(){return cloudPrefs==null?"":cloudPrefs.getString("access_token","");}
    private String bindAndVerify417(long id,String kind,String path,String token)throws Exception{
        JSONObject body=new JSONObject().put("p_legacy_id",id).put("p_kind",kind).put("p_path",path);HttpResult set=request("POST",SUPABASE_URL+"/rest/v1/rpc/set_athlete_media_path",body.toString(),token);if(set.code==401&&refreshSession()){token=sessionToken417();set=request("POST",SUPABASE_URL+"/rest/v1/rpc/set_athlete_media_path",body.toString(),token);}if(set.code<200||set.code>=300||set.body==null||!set.body.contains("true"))throw new IOException("MEDYA BAĞLAMA "+set.code);
        String field="photo".equals(kind)?"photo_path":"registration_form_path";HttpResult verify=request("GET",SUPABASE_URL+"/rest/v1/athlete_media_index?legacy_id=eq."+id+"&select="+field,null,token);if(verify.code==401&&refreshSession()){token=sessionToken417();verify=request("GET",SUPABASE_URL+"/rest/v1/athlete_media_index?legacy_id=eq."+id+"&select="+field,null,token);}if(verify.code<200||verify.code>=300)throw new IOException("DOĞRULAMA "+verify.code);JSONArray arr=new JSONArray(verify.body);if(arr.length()==0)throw new IOException("MEDYA KAYDI BULUNAMADI");String actual=arr.getJSONObject(0).optString(field,"");if(actual==null||actual.trim().isEmpty())throw new IOException("MEDYA YOLU BOŞ");return actual;
    }
    private UploadResult417 put417(String bucket,String path,byte[] data,String token)throws Exception{
        URL u=new URL(SUPABASE_URL+"/storage/v1/object/"+bucket+"/"+enc417(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();h.setRequestMethod("POST");h.setDoOutput(true);h.setConnectTimeout(15000);h.setReadTimeout(30000);h.setRequestProperty("apikey",SUPABASE_KEY);h.setRequestProperty("Authorization","Bearer "+token);h.setRequestProperty("Content-Type","image/jpeg");h.setFixedLengthStreamingMode(data.length);try(OutputStream o=h.getOutputStream()){o.write(data);o.flush();}int code=h.getResponseCode();InputStream in=code>=200&&code<300?h.getInputStream():h.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();if(in!=null){try(InputStream x=in){byte[] b=new byte[4096];int n;while((n=x.read(b))>0)out.write(b,0,n);}}String body=out.toString(StandardCharsets.UTF_8.name());h.disconnect();return new UploadResult417(code,body);
    }
    private String enc417(String p)throws Exception{StringBuilder b=new StringBuilder();for(String z:p.split("/")){if(b.length()>0)b.append('/');b.append(URLEncoder.encode(z,"UTF-8").replace("+","%20"));}return b.toString();}
    private String cleanError417(String s){if(s==null||s.trim().isEmpty())return "";try{JSONObject o=new JSONObject(s);String m=o.optString("message",o.optString("error",o.optString("code","")));return m.isEmpty()?"":" - "+m;}catch(Exception e){String x=s.replace('\n',' ').trim();if(x.length()>90)x=x.substring(0,90);return x.isEmpty()?"":" - "+x;}}
    private void returnAfter417(long id,boolean isForm,boolean missing){if(missing){if(isForm)showMissingForms417();else showMissingPhotos417();}else if(isForm)showProfile(id);else form(id);}
    @Override void goBack(){if("MISSING_FORMS_417".equals(page)||"MISSING_PHOTOS_417".equals(page)){showHome();return;}super.goBack();}
    @Override protected void onDestroy(){media417.shutdownNow();super.onDestroy();}
    private static class UploadResult417{final int code;final String body;UploadResult417(int code,String body){this.code=code;this.body=body;}}
}
