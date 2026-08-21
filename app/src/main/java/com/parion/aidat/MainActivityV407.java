package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import org.json.*;

public class MainActivityV407 extends MainActivityV406 {
    private static final String SB_URL="https://ujjtsemybslznmzadzvk.supabase.co";
    private static final String SB_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqanRzZW15YnNsem5temFkenZrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY3MzIyMjIsImV4cCI6MjEwMjMwODIyMn0.qZPcYZwAjMJpc2yBB1bdTjA8YguFqr3UY85VuQGQRLE";
    private static final int REQ_PHOTO_407=4071, REQ_FORM_407=4072;
    private final ExecutorService cloud407=Executors.newFixedThreadPool(3);
    private long target407=-1; private boolean returnPhoto407=false, returnForm407=false;

    @SuppressWarnings("unchecked") private ConcurrentHashMap<Long,String> photoMap407(){try{Field f=MainActivityV405.class.getDeclaredField("photoPath");f.setAccessible(true);return (ConcurrentHashMap<Long,String>)f.get(this);}catch(Exception e){return new ConcurrentHashMap<>();}}
    @SuppressWarnings("unchecked") private ConcurrentHashMap<Long,String> formMap407(){try{Field f=MainActivityV405.class.getDeclaredField("formPath");f.setAccessible(true);return (ConcurrentHashMap<Long,String>)f.get(this);}catch(Exception e){return new ConcurrentHashMap<>();}}
    @SuppressWarnings("unchecked") private ConcurrentHashMap<String,String> aliasMap407(){try{Field f=MainActivityV405.class.getDeclaredField("photoAlias");f.setAccessible(true);return (ConcurrentHashMap<String,String>)f.get(this);}catch(Exception e){return new ConcurrentHashMap<>();}}

    @Override void showProfile(long id){
        super.showProfile(id);
        patchProfile407(root,id);
    }
    private void patchProfile407(View v,long id){
        if(!(v instanceof ViewGroup))return; ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof Button){
                Button b=(Button)c; String s=String.valueOf(b.getText());
                if(s.contains("KAYIT FORM")){
                    if(formMap407().containsKey(id)){
                        b.setText("KAYIT FORMUNU GÖRÜNTÜLE");
                        b.setOnClickListener(x->showCloudForm407(id));
                    }else{
                        b.setText("KAYIT FORMU EKLE");
                        b.setOnClickListener(x->{target407=id;pick407(REQ_FORM_407);});
                    }
                }
            }
            patchProfile407(c,id);
        }
    }

    @Override void showHome(){
        super.showHome();
        patchHomeCards407(root);
    }
    private void patchHomeCards407(View v){
        if(!(v instanceof ViewGroup))return; ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof TextView){String s=String.valueOf(((TextView)c).getText());
                if(s.contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")){View p=(View)c.getParent();p.setOnClickListener(x->showMissingPhotos407());}
                else if(s.contains("KAYIT FORMU OLMAYAN AKTİF SPORCULAR")){View p=(View)c.getParent();p.setOnClickListener(x->showMissingForms407());}
            }
            patchHomeCards407(c);
        }
    }

    private void showMissingPhotos407(){
        page="MISSING_PHOTOS_407";base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);
        TextView info=tv("Sporcuya dokunun; galeriden fotoğrafı seçin. Kaydedilince tekrar bu listeye dönülür.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){A x=a(c);if(photoMap407().containsKey(x.id))continue;addMissingRow407(b,x,true);n++;}c.close();
        if(n==0)b.addView(tv("Fotoğrafı olmayan aktif sporcu bulunmuyor.",14,GREEN,true));
    }
    private void showMissingForms407(){
        page="MISSING_FORMS_407";base("KAYIT FORMU OLMAYAN AKTİF SPORCULAR",true);
        TextView info=tv("Sporcuya dokunun; galeriden kayıt formu görselini seçin. Kaydedilince tekrar bu listeye dönülür.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){A x=a(c);if(formMap407().containsKey(x.id))continue;addMissingRow407(b,x,false);n++;}c.close();
        if(n==0)b.addView(tv("Kayıt formu olmayan aktif sporcu bulunmuyor.",14,GREEN,true));
    }
    private void addMissingRow407(LinearLayout b,A x,boolean photo){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));
        ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,x.photo);r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));
        LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+(photo?" • FOTOĞRAF EKLE":" • KAYIT FORMU EKLE"),12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        r.setOnClickListener(v->{target407=x.id;if(photo){returnPhoto407=true;pick407(REQ_PHOTO_407);}else{returnForm407=true;pick407(REQ_FORM_407);}});
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);
    }

    @Override void form(long id){
        super.form(id);
        if(id>0)patchPhotoButtons407(root,id);
    }
    private void patchPhotoButtons407(View v,long id){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof Button){Button b=(Button)c;String s=String.valueOf(b.getText());
                if(s.contains("FOTOĞRAF EKLE")||s.contains("FOTOĞRAFI DEĞİŞTİR"))b.setOnClickListener(x->{target407=id;pick407(REQ_PHOTO_407);});
                else if(s.contains("FOTOĞRAFI SİL"))b.setOnClickListener(x->confirmDeletePhoto407(id));
            }
            patchPhotoButtons407(c,id);
        }
    }

    private void pick407(int req){Intent i;if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}else{i=new Intent(Intent.ACTION_PICK);i.setType("image/*");}startActivityForResult(i,req);}

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_PHOTO_407||requestCode==REQ_FORM_407){
            long id=target407;target407=-1;
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0){if(requestCode==REQ_PHOTO_407)uploadPhoto407(id,data.getData());else uploadForm407(id,data.getData());}
            else finish407(id);
            return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void uploadPhoto407(long id,Uri uri){cloud407.execute(()->{try{
        byte[] jpg=optimized407(uri,1600,84);String old=photoMap407().get(id);String path="user/"+id+"/photo_"+System.currentTimeMillis()+".jpg";
        if(!upload407("athlete-photos",path,jpg))throw new IOException("upload");if(!setPath407(id,"photo",path))throw new IOException("path");
        photoMap407().put(id,path);Cursor c=db.athlete(id);String local="";if(c.moveToFirst())local=v(c,"photo");c.close();if(local!=null&&!local.isEmpty())aliasMap407().put(local,path);db.getWritableDatabase().execSQL("UPDATE athletes SET photo=? WHERE id=?",new Object[]{"CLOUD:"+path,id});
        if(old!=null&&!old.equals(path))delete407("athlete-photos",old);
        runOnUiThread(()->{toast("Sporcu fotoğrafı buluta kaydedildi.");finish407(id);});
    }catch(Exception e){runOnUiThread(()->{toast("Fotoğraf yüklenemedi.");finish407(id);});}});}

    private void uploadForm407(long id,Uri uri){cloud407.execute(()->{try{
        byte[] jpg=optimized407(uri,2400,82);String old=formMap407().get(id);String path="user/"+id+"/form_"+System.currentTimeMillis()+".jpg";
        if(!upload407("registration-forms",path,jpg))throw new IOException("upload");if(!setPath407(id,"form",path))throw new IOException("path");
        formMap407().put(id,path);if(old!=null&&!old.equals(path))delete407("registration-forms",old);
        runOnUiThread(()->{toast("Kayıt formu buluta kaydedildi.");finish407(id);});
    }catch(Exception e){runOnUiThread(()->{toast("Kayıt formu yüklenemedi.");finish407(id);});}});}

    private void confirmDeletePhoto407(long id){new AlertDialog.Builder(this).setTitle("FOTOĞRAFI SİL").setMessage("Sporcunun buluttaki fotoğrafı silinsin mi?").setPositiveButton("EVET, SİL",(d,w)->cloud407.execute(()->{String old=photoMap407().remove(id);if(setPath407(id,"photo","")){if(old!=null)delete407("athlete-photos",old);db.getWritableDatabase().execSQL("UPDATE athletes SET photo='NONE' WHERE id=?",new Object[]{id});runOnUiThread(()->{toast("Fotoğraf silindi.");form(id);});}else runOnUiThread(()->toast("Fotoğraf silinemedi."));})).setNegativeButton("VAZGEÇ",null).show();}

    private void showCloudForm407(long id){String path=formMap407().get(id);if(path==null||path.isEmpty()){toast("Kayıt formu bulunamadı.");return;}cloud407.execute(()->{try{
        byte[] data=download407("registration-forms",path);BitmapFactory.Options o=new BitmapFactory.Options();o.inPreferredConfig=Bitmap.Config.RGB_565;Bitmap bm=BitmapFactory.decodeByteArray(data,0,data.length,o);if(bm==null)throw new IOException();
        runOnUiThread(()->{MainActivityV403.ZoomImage403 iv=new MainActivityV403.ZoomImage403(this);iv.setImageBitmap(bm);FrameLayout f=new FrameLayout(this);f.setBackgroundColor(Color.BLACK);f.addView(iv,new FrameLayout.LayoutParams(-1,dp(650)));new AlertDialog.Builder(this).setTitle("KAYIT FORMU").setView(f).setPositiveButton("KAPAT",null).show();});
    }catch(Exception e){runOnUiThread(()->toast("Kayıt formu açılamadı. İnternet bağlantısını kontrol edin."));}});}

    private void finish407(long id){boolean rp=returnPhoto407,rf=returnForm407;returnPhoto407=false;returnForm407=false;if(rp)showMissingPhotos407();else if(rf)showMissingForms407();else if(id>0)showProfile(id);else showHome();}

    private byte[] optimized407(Uri uri,int target,int quality)throws Exception{BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,b);}int s=1;while(b.outWidth>0&&b.outHeight>0&&(b.outWidth/s>target*2||b.outHeight/s>target*2))s*=2;BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=Math.max(1,s);Bitmap bm;try(InputStream in=getContentResolver().openInputStream(uri)){bm=BitmapFactory.decodeStream(in,null,o);}if(bm==null)throw new IOException();ByteArrayOutputStream out=new ByteArrayOutputStream();bm.compress(Bitmap.CompressFormat.JPEG,quality,out);bm.recycle();return out.toByteArray();}
    private boolean upload407(String bucket,String path,byte[] data)throws Exception{URL u=new URL(SB_URL+"/storage/v1/object/"+bucket+"/"+enc407(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();auth407(h);h.setRequestMethod("POST");h.setDoOutput(true);h.setRequestProperty("Content-Type","image/jpeg");h.setRequestProperty("x-upsert","true");try(OutputStream out=h.getOutputStream()){out.write(data);}int c=h.getResponseCode();h.disconnect();return c/100==2;}
    private byte[] download407(String bucket,String path)throws Exception{URL u=new URL(SB_URL+"/storage/v1/object/"+bucket+"/"+enc407(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();auth407(h);h.setConnectTimeout(12000);h.setReadTimeout(30000);if(h.getResponseCode()/100!=2)throw new IOException("HTTP "+h.getResponseCode());ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=h.getInputStream()){byte[] b=new byte[16384];int n;while((n=in.read(b))>0)out.write(b,0,n);}h.disconnect();return out.toByteArray();}
    private void delete407(String bucket,String path){try{URL u=new URL(SB_URL+"/storage/v1/object/"+bucket+"/"+enc407(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();auth407(h);h.setRequestMethod("DELETE");h.getResponseCode();h.disconnect();}catch(Exception ignored){}}
    private boolean setPath407(long id,String kind,String path){try{URL u=new URL(SB_URL+"/rest/v1/rpc/set_athlete_media_path");HttpURLConnection h=(HttpURLConnection)u.openConnection();auth407(h);h.setRequestMethod("POST");h.setDoOutput(true);h.setRequestProperty("Content-Type","application/json");JSONObject o=new JSONObject();o.put("p_legacy_id",id);o.put("p_kind",kind);o.put("p_path",path==null?"":path);try(OutputStream out=h.getOutputStream()){out.write(o.toString().getBytes(StandardCharsets.UTF_8));}int c=h.getResponseCode();String body=read407(h);h.disconnect();return c/100==2&&body.contains("true");}catch(Exception e){return false;}}
    private void auth407(HttpURLConnection h){h.setRequestProperty("apikey",SB_KEY);h.setRequestProperty("Authorization","Bearer "+SB_KEY);}
    private String read407(HttpURLConnection h)throws Exception{InputStream in=h.getResponseCode()/100==2?h.getInputStream():h.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while(in!=null&&(n=in.read(b))>0)out.write(b,0,n);if(in!=null)in.close();return out.toString("UTF-8");}
    private String enc407(String p)throws Exception{StringBuilder b=new StringBuilder();String[] z=p.split("/");for(int i=0;i<z.length;i++){if(i>0)b.append('/');b.append(URLEncoder.encode(z[i],"UTF-8").replace("+","%20"));}return b.toString();}

    @Override void goBack(){if("MISSING_PHOTOS_407".equals(page)||"MISSING_FORMS_407".equals(page)){showHome();return;}super.goBack();}
    @Override protected void onDestroy(){cloud407.shutdownNow();super.onDestroy();}
}
