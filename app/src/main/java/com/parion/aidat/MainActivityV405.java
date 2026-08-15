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
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

public class MainActivityV405 extends MainActivityV403 {
    private static final String SB_URL="https://ujjtsemybslznmzadzvk.supabase.co";
    private static final String SB_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqanRzZW15YnNsem5temFkenZrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY3MzIyMjIsImV4cCI6MjEwMjMwODIyMn0.qZPcYZwAjMJpc2yBB1bdTjA8YguFqr3UY85VuQGQRLE";
    private static final int REQ_CLOUD_FORM=4051, REQ_CLOUD_PHOTO=4052;
    private final ExecutorService mediaPool=Executors.newFixedThreadPool(4);
    private final android.util.LruCache<String,Bitmap> mediaCache=new android.util.LruCache<String,Bitmap>(96){@Override protected int sizeOf(String k,Bitmap b){return 1;}};
    private final ConcurrentHashMap<Long,String> photoPath=new ConcurrentHashMap<>(), formPath=new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,String> photoAlias=new ConcurrentHashMap<>();
    private volatile boolean cloudIndexReady=false;
    private long mediaTarget=-1; private boolean returnMissingPhoto=false, returnMissingForm=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().getDecorView().postDelayed(this::refreshCloudIndex405,250);
    }

    private void refreshCloudIndex405(){
        mediaPool.execute(()->{
            try{
                URL u=new URL(SB_URL+"/rest/v1/athletes?select=legacy_id,photo_path,registration_form_path");
                HttpURLConnection h=(HttpURLConnection)u.openConnection();auth405(h);h.setConnectTimeout(12000);h.setReadTimeout(20000);
                String text=read405(h);h.disconnect();JSONArray a=new JSONArray(text);
                photoPath.clear();formPath.clear();photoAlias.clear();
                for(int i=0;i<a.length();i++){
                    JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);if(id<=0)continue;
                    String pp=o.optString("photo_path","");String fp=o.optString("registration_form_path","");
                    if(!pp.isEmpty()&&!"null".equals(pp)){photoPath.put(id,pp);String base=pp.substring(pp.lastIndexOf('/')+1);photoAlias.put(base,pp);}
                    if(!fp.isEmpty()&&!"null".equals(fp))formPath.put(id,fp);
                    Cursor c=db.athlete(id);if(c.moveToFirst()){String local=v(c,"photo");if(local!=null&&!local.trim().isEmpty()&&!pp.isEmpty()&&!"null".equals(pp))photoAlias.put(local,pp);}c.close();
                }
                cloudIndexReady=true;
                runOnUiThread(()->{if("HOME".equals(page))showHome();});
            }catch(Exception ignored){}
        });
    }

    @Override void setAthletePhoto(ImageView v,String photo){
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        String key=photo==null?"":photo.trim();String remote="";
        if(key.startsWith("CLOUD:"))remote=key.substring(6);else remote=photoAlias.getOrDefault(key,"");
        if(remote.isEmpty()){
            if(key.startsWith("USER:")){super.setAthletePhoto(v,key);return;}
            v.setImageDrawable(new ColorDrawable(Color.rgb(225,225,225)));return;
        }
        loadRemoteBitmap405(v,"athlete-photos",remote,220);
    }

    private void loadRemoteBitmap405(ImageView v,String bucket,String path,int target){
        String ck=bucket+":"+path+":"+target;Bitmap cached=mediaCache.get(ck);if(cached!=null){v.setImageBitmap(cached);return;}
        v.setImageDrawable(new ColorDrawable(Color.rgb(225,225,225)));v.setTag(ck);
        mediaPool.execute(()->{Bitmap b=downloadBitmap405(bucket,path,target);if(b==null)return;mediaCache.put(ck,b);runOnUiThread(()->{if(ck.equals(String.valueOf(v.getTag())))v.setImageBitmap(b);});});
    }

    private Bitmap downloadBitmap405(String bucket,String path,int target){
        try{byte[] data=download405(bucket,path);BitmapFactory.Options bo=new BitmapFactory.Options();bo.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,bo);BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sample405(bo.outWidth,bo.outHeight,target);o.inPreferredConfig=Bitmap.Config.RGB_565;return BitmapFactory.decodeByteArray(data,0,data.length,o);}catch(Exception e){return null;}
    }
    private int sample405(int w,int h,int target){int s=1;while(w>0&&h>0&&(w/s>target*2||h/s>target*2))s*=2;return Math.max(1,s);}

    @Override void showHome(){
        super.showHome();
        patchHomeMediaCards405(root);
    }
    private void patchHomeMediaCards405(View v){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof TextView){String s=String.valueOf(((TextView)c).getText());
                if(s.contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")){View p=(View)c.getParent();setPreviousCount405(p,countMissingPhoto405());p.setOnClickListener(x->showMissingPhotos405());}
                if(s.contains("KAYIT FORMU OLMAYAN AKTİF SPORCULAR")){View p=(View)c.getParent();setPreviousCount405(p,countMissingForm405());p.setOnClickListener(x->showMissingForms405());}
            }
            patchHomeMediaCards405(c);
        }
    }
    private void setPreviousCount405(View p,int n){if(!(p instanceof ViewGroup))return;ViewGroup g=(ViewGroup)p;for(int i=0;i<g.getChildCount();i++){View x=g.getChildAt(i);if(x instanceof TextView){String s=String.valueOf(((TextView)x).getText()).trim();if(s.matches("\\d+")){((TextView)x).setText(String.valueOf(n));return;}}}}
    private int countMissingPhoto405(){int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(!photoPath.containsKey(x.id))n++;}c.close();return n;}
    private int countMissingForm405(){int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(!formPath.containsKey(x.id))n++;}c.close();return n;}

    private void showMissingPhotos405(){
        page="MISSING_PHOTOS_CLOUD";base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(photoPath.containsKey(x.id))continue;addMediaMissingRow405(b,x,true);n++;}c.close();if(n==0)b.addView(tv("Fotoğrafı olmayan aktif sporcu bulunmuyor.",14,GREEN,true));
    }
    private void showMissingForms405(){
        page="MISSING_FORMS_CLOUD";base("KAYIT FORMU OLMAYAN AKTİF SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(formPath.containsKey(x.id))continue;addMediaMissingRow405(b,x,false);n++;}c.close();if(n==0)b.addView(tv("Kayıt formu olmayan aktif sporcu bulunmuyor.",14,GREEN,true));
    }
    private void addMediaMissingRow405(LinearLayout b,A x,boolean photo){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);String pp=photoPath.get(x.id);if(pp!=null)loadRemoteBitmap405(av,"athlete-photos",pp,160);else av.setImageDrawable(new ColorDrawable(Color.LTGRAY));r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+(photo?" • FOTOĞRAF EKLE":" • KAYIT FORMU EKLE"),12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));r.setOnClickListener(v->{mediaTarget=x.id;if(photo){returnMissingPhoto=true;pickCloudImage405(REQ_CLOUD_PHOTO);}else{returnMissingForm=true;pickCloudImage405(REQ_CLOUD_FORM);}});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);
    }

    @Override void showProfile(long id){
        super.showProfile(id);
        patchProfileCloud405(root,id);
        // New records may still have a one-time USER: photo from the legacy form flow; promote it to cloud automatically.
        Cursor c=db.athlete(id);if(c.moveToFirst()){String p=v(c,"photo");if(p!=null&&p.startsWith("USER:")&&!photoPath.containsKey(id))promoteLocalPhoto405(id,p);}c.close();
    }
    private void patchProfileCloud405(View v,long id){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof Button){String s=String.valueOf(((Button)c).getText());
                if(s.contains("KAYIT FORMUNU GÖRÜNTÜLE")){((Button)c).setText(formPath.containsKey(id)?"KAYIT FORMUNU GÖRÜNTÜLE":"KAYIT FORMU EKLE");c.setOnClickListener(x->{if(formPath.containsKey(id))showCloudForm405(id);else{mediaTarget=id;pickCloudImage405(REQ_CLOUD_FORM);}});}
                else if(s.contains("KAYIT FORMU EKLE")||s.contains("KAYIT FORMUNU DEĞİŞTİR")){c.setOnClickListener(x->{mediaTarget=id;pickCloudImage405(REQ_CLOUD_FORM);});}
            }
            patchProfileCloud405(c,id);
        }
    }

    @Override void form(long id){
        super.form(id);
        if(id>0)patchPhotoControls405(root,id);
    }
    private void patchPhotoControls405(View v,long id){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof Button){String s=String.valueOf(((Button)c).getText());
                if(s.contains("FOTOĞRAF EKLE")||s.contains("FOTOĞRAFI DEĞİŞTİR"))c.setOnClickListener(x->{mediaTarget=id;pickCloudImage405(REQ_CLOUD_PHOTO);});
                else if(s.contains("FOTOĞRAFI SİL"))c.setOnClickListener(x->confirmDeleteCloudPhoto405(id));
            }
            patchPhotoControls405(c,id);
        }
    }

    private void pickCloudImage405(int req){Intent i;if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}else{i=new Intent(Intent.ACTION_PICK);i.setType("image/*");}startActivityForResult(i,req);}

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if((requestCode==REQ_CLOUD_FORM||requestCode==REQ_CLOUD_PHOTO)){
            long id=mediaTarget;mediaTarget=-1;
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0){Uri u=data.getData();if(requestCode==REQ_CLOUD_FORM)uploadFormFromGallery405(id,u);else uploadPhotoFromGallery405(id,u);}else finishMediaReturn405(id);
            return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void uploadFormFromGallery405(long id,Uri uri){mediaPool.execute(()->{try{byte[] jpg=optimizedJpeg405(uri,2400,82);String old=formPath.get(id);String path="user/"+id+"/form_"+System.currentTimeMillis()+".jpg";if(!upload405("registration-forms",path,"image/jpeg",jpg))throw new IOException();patchPath405(id,"registration_form_path",path);formPath.put(id,path);if(old!=null&&!old.equals(path))delete405("registration-forms",old);runOnUiThread(()->{toast("Kayıt formu buluta kaydedildi.");finishMediaReturn405(id);});}catch(Exception e){runOnUiThread(()->{toast("Kayıt formu yüklenemedi.");finishMediaReturn405(id);});}});}
    private void uploadPhotoFromGallery405(long id,Uri uri){mediaPool.execute(()->{try{byte[] jpg=optimizedJpeg405(uri,1600,84);String old=photoPath.get(id);String path="user/"+id+"/photo_"+System.currentTimeMillis()+".jpg";if(!upload405("athlete-photos",path,"image/jpeg",jpg))throw new IOException();patchPath405(id,"photo_path",path);photoPath.put(id,path);Cursor c=db.athlete(id);String alias="";if(c.moveToFirst())alias=v(c,"photo");c.close();if(alias!=null&&!alias.isEmpty())photoAlias.put(alias,path);setPendingPhotoReflection405("CLOUD:"+path);db.getWritableDatabase().execSQL("UPDATE athletes SET photo=? WHERE id=?",new Object[]{"CLOUD:"+path,id});if(old!=null&&!old.equals(path))delete405("athlete-photos",old);runOnUiThread(()->{toast("Sporcu fotoğrafı buluta kaydedildi.");finishMediaReturn405(id);});}catch(Exception e){runOnUiThread(()->{toast("Fotoğraf yüklenemedi.");finishMediaReturn405(id);});}});}
    private void promoteLocalPhoto405(long id,String ref){mediaPool.execute(()->{try{File f=new File(new File(getFilesDir(),"athlete_photos"),ref.substring(5));if(!f.isFile())return;byte[] data=java.nio.file.Files.readAllBytes(f.toPath());String path="user/"+id+"/photo_"+System.currentTimeMillis()+".jpg";if(upload405("athlete-photos",path,"image/jpeg",data)){patchPath405(id,"photo_path",path);photoPath.put(id,path);photoAlias.put(ref,path);}}catch(Exception ignored){}});}
    private void finishMediaReturn405(long id){boolean mp=returnMissingPhoto,mf=returnMissingForm;returnMissingPhoto=false;returnMissingForm=false;if(mp)showMissingPhotos405();else if(mf)showMissingForms405();else if(id>0)showProfile(id);else showHome();}

    private void confirmDeleteCloudPhoto405(long id){new AlertDialog.Builder(this).setTitle("FOTOĞRAFI SİL").setMessage("Sporcunun buluttaki fotoğrafı silinsin mi?").setPositiveButton("EVET, SİL",(d,w)->mediaPool.execute(()->{String old=photoPath.remove(id);if(old!=null)delete405("athlete-photos",old);patchPath405(id,"photo_path",null);db.getWritableDatabase().execSQL("UPDATE athletes SET photo='NONE' WHERE id=?",new Object[]{id});setPendingPhotoReflection405("NONE");runOnUiThread(()->{toast("Fotoğraf silindi.");form(id);});})).setNegativeButton("VAZGEÇ",null).show();}

    private void showCloudForm405(long id){String path=formPath.get(id);if(path==null){toast("Kayıt formu bulunamadı.");return;}mediaPool.execute(()->{try{byte[] data=download405("registration-forms",path);BitmapFactory.Options o=new BitmapFactory.Options();o.inPreferredConfig=Bitmap.Config.RGB_565;Bitmap b=BitmapFactory.decodeByteArray(data,0,data.length,o);if(b==null)throw new IOException();runOnUiThread(()->{ZoomImage403 iv=new ZoomImage403(this);iv.setImageBitmap(b);FrameLayout f=new FrameLayout(this);f.setBackgroundColor(Color.BLACK);f.addView(iv,new FrameLayout.LayoutParams(-1,dp(650)));new AlertDialog.Builder(this).setTitle("KAYIT FORMU").setView(f).setPositiveButton("KAPAT",null).show();});}catch(Exception e){runOnUiThread(()->toast("Kayıt formu açılamadı. İnternet bağlantısını kontrol edin."));}});}

    private byte[] optimizedJpeg405(Uri uri,int target,int quality)throws Exception{BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,b);}BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sample405(b.outWidth,b.outHeight,target);Bitmap bm;try(InputStream in=getContentResolver().openInputStream(uri)){bm=BitmapFactory.decodeStream(in,null,o);}if(bm==null)throw new IOException();ByteArrayOutputStream out=new ByteArrayOutputStream();bm.compress(Bitmap.CompressFormat.JPEG,quality,out);bm.recycle();return out.toByteArray();}

    private void setPendingPhotoReflection405(String value){try{Class<?> c=MainActivityV384.class;Field f=c.getDeclaredField("pendingPhoto");f.setAccessible(true);f.set(this,value);Field p=c.getDeclaredField("pendingPreview");p.setAccessible(true);Object x=p.get(this);if(x instanceof ImageView){runOnUiThread(()->{String path=value.startsWith("CLOUD:")?value.substring(6):"";if(!path.isEmpty())loadRemoteBitmap405((ImageView)x,"athlete-photos",path,500);else ((ImageView)x).setImageDrawable(new ColorDrawable(Color.LTGRAY));});}}catch(Exception ignored){}}

    private void patchPath405(long id,String field,String value){try{URL u=new URL(SB_URL+"/rest/v1/athletes?legacy_id=eq."+id);HttpURLConnection h=(HttpURLConnection)u.openConnection();auth405(h);h.setRequestMethod("PATCH");h.setDoOutput(true);h.setRequestProperty("Content-Type","application/json");h.setRequestProperty("Prefer","return=minimal");JSONObject o=new JSONObject();if(value==null)o.put(field,JSONObject.NULL);else o.put(field,value);try(OutputStream out=h.getOutputStream()){out.write(o.toString().getBytes(StandardCharsets.UTF_8));}h.getResponseCode();h.disconnect();}catch(Exception ignored){}}
    private boolean upload405(String bucket,String path,String mime,byte[] data)throws Exception{URL u=new URL(SB_URL+"/storage/v1/object/"+bucket+"/"+encode405(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();auth405(h);h.setRequestMethod("POST");h.setDoOutput(true);h.setRequestProperty("Content-Type",mime);h.setRequestProperty("x-upsert","true");try(OutputStream out=h.getOutputStream()){out.write(data);}int c=h.getResponseCode();h.disconnect();return c>=200&&c<300;}
    private byte[] download405(String bucket,String path)throws Exception{URL u=new URL(SB_URL+"/storage/v1/object/"+bucket+"/"+encode405(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();auth405(h);h.setConnectTimeout(12000);h.setReadTimeout(30000);if(h.getResponseCode()/100!=2)throw new IOException();ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=h.getInputStream()){byte[] b=new byte[16384];int n;while((n=in.read(b))>0)out.write(b,0,n);}h.disconnect();return out.toByteArray();}
    private void delete405(String bucket,String path){try{URL u=new URL(SB_URL+"/storage/v1/object/"+bucket+"/"+encode405(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();auth405(h);h.setRequestMethod("DELETE");h.getResponseCode();h.disconnect();}catch(Exception ignored){}}
    private void auth405(HttpURLConnection h){h.setRequestProperty("apikey",SB_KEY);h.setRequestProperty("Authorization","Bearer "+SB_KEY);}
    private String read405(HttpURLConnection h)throws Exception{InputStream in=h.getResponseCode()/100==2?h.getInputStream():h.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while(in!=null&&(n=in.read(b))>0)out.write(b,0,n);if(in!=null)in.close();return out.toString("UTF-8");}
    private String encode405(String p)throws Exception{StringBuilder b=new StringBuilder();String[] z=p.split("/");for(int i=0;i<z.length;i++){if(i>0)b.append('/');b.append(URLEncoder.encode(z[i],"UTF-8").replace("+","%20"));}return b.toString();}

    @Override void goBack(){if("MISSING_PHOTOS_CLOUD".equals(page)||"MISSING_FORMS_CLOUD".equals(page)){showHome();return;}super.goBack();}
    @Override protected void onDestroy(){mediaPool.shutdownNow();super.onDestroy();}
}
