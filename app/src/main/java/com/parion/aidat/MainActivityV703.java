package com.parion.aidat;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.exifinterface.media.ExifInterface;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import org.json.JSONObject;

/** v4.1.25 - registration forms are normalized to portrait both on upload and display. */
public class MainActivityV703 extends MainActivityV702 {
    private static final int REQ_CLOUD_FORM_703=4051;
    private final ExecutorService formExec703=Executors.newSingleThreadExecutor();

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode!=REQ_CLOUD_FORM_703){super.onActivityResult(requestCode,resultCode,data);return;}
        long id=consumeMediaTarget703();
        if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0){
            Uri uri=data.getData();
            toast("Kayıt formu yükleniyor...");
            formExec703.execute(()->uploadRegistrationForm703(id,uri));
        }else finishMediaReturn703(id);
    }

    @Override void showProfile(long id){
        super.showProfile(id);
        patchPortraitFormViewer703(root,id);
    }

    private void patchPortraitFormViewer703(View v,long id){
        if(!(v instanceof ViewGroup))return;
        ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof Button){
                String s=String.valueOf(((Button)c).getText());
                if(s.contains("KAYIT FORMUNU GÖRÜNTÜLE"))c.setOnClickListener(x->showPortraitForm703(id));
            }
            patchPortraitFormViewer703(c,id);
        }
    }

    private void uploadRegistrationForm703(long id,Uri uri){
        try{
            byte[] jpg=optimizedJpeg703(uri,2400,82);
            String path="user/"+id+"/form_"+System.currentTimeMillis()+".jpg";
            String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");
            if(token.isEmpty())throw new IOException("Oturum anahtarı bulunamadı. Lütfen oturumu yenileyin.");
            HttpUploadResult703 up=upload703(path,jpg,token);
            if(up.code<200||up.code>=300)throw new IOException("Storage HTTP "+up.code+"\n"+clean703(up.body));

            JSONObject body=new JSONObject().put("registration_form_path",path);
            HttpResult r=request("PATCH",SUPABASE_URL+"/rest/v1/athletes?legacy_id=eq."+id,body.toString(),token);
            if(r.code==401&&refreshSession()){
                token=cloudPrefs.getString("access_token","");
                r=request("PATCH",SUPABASE_URL+"/rest/v1/athletes?legacy_id=eq."+id,body.toString(),token);
            }
            if(r.code<200||r.code>=300)throw new IOException("Form yolu kaydedilemedi • HTTP "+r.code+"\n"+clean703(r.body));
            formMap413().put(id,path);
            runOnUiThread(()->{toast("Kayıt formu buluta kaydedildi.");finishMediaReturn703(id);});
        }catch(Exception e){
            String detail=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();
            runOnUiThread(()->showFormError703(detail,id));
        }
    }

    private HttpUploadResult703 upload703(String path,byte[] data,String token)throws Exception{
        URL u=new URL(SUPABASE_URL+"/storage/v1/object/registration-forms/"+encode703(path));
        HttpURLConnection h=(HttpURLConnection)u.openConnection();
        h.setRequestMethod("POST");h.setDoOutput(true);h.setConnectTimeout(15000);h.setReadTimeout(30000);
        h.setRequestProperty("apikey",SUPABASE_KEY);
        h.setRequestProperty("Authorization","Bearer "+token);
        h.setRequestProperty("Content-Type","image/jpeg");
        h.setRequestProperty("x-upsert","true");
        try(OutputStream out=h.getOutputStream()){out.write(data);}
        int code=h.getResponseCode();InputStream in=code>=400?h.getErrorStream():h.getInputStream();String text=read703(in);h.disconnect();
        return new HttpUploadResult703(code,text);
    }

    private byte[] optimizedJpeg703(Uri uri,int target,int quality)throws Exception{
        int orientation=ExifInterface.ORIENTATION_NORMAL;
        try(InputStream exifIn=getContentResolver().openInputStream(uri)){
            if(exifIn!=null){
                ExifInterface exif=new ExifInterface(exifIn);
                orientation=exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            }
        }catch(Exception ignored){}

        BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;
        try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,b);}
        int sample=1;while(b.outWidth>0&&b.outHeight>0&&(b.outWidth/sample>target*2||b.outHeight/sample>target*2))sample*=2;
        BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=Math.max(1,sample);
        Bitmap bm;try(InputStream in=getContentResolver().openInputStream(uri)){bm=BitmapFactory.decodeStream(in,null,o);}
        if(bm==null)throw new IOException("Seçilen görsel okunamadı.");

        Bitmap corrected=applyExifOrientation703(bm,orientation);
        if(corrected!=bm)bm.recycle();
        Bitmap portrait=forcePortrait703(corrected);
        if(portrait!=corrected)corrected.recycle();
        ByteArrayOutputStream out=new ByteArrayOutputStream();portrait.compress(Bitmap.CompressFormat.JPEG,quality,out);portrait.recycle();return out.toByteArray();
    }

    private Bitmap applyExifOrientation703(Bitmap src,int orientation){
        Matrix m=new Matrix();
        switch(orientation){
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: m.setScale(-1f,1f); break;
            case ExifInterface.ORIENTATION_ROTATE_180: m.setRotate(180f); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: m.setScale(1f,-1f); break;
            case ExifInterface.ORIENTATION_TRANSPOSE: m.setRotate(90f);m.postScale(-1f,1f); break;
            case ExifInterface.ORIENTATION_ROTATE_90: m.setRotate(90f); break;
            case ExifInterface.ORIENTATION_TRANSVERSE: m.setRotate(-90f);m.postScale(-1f,1f); break;
            case ExifInterface.ORIENTATION_ROTATE_270: m.setRotate(270f); break;
            default: return src;
        }
        try{return Bitmap.createBitmap(src,0,0,src.getWidth(),src.getHeight(),m,true);}catch(Exception e){return src;}
    }

    private Bitmap forcePortrait703(Bitmap src){
        if(src==null||src.getHeight()>=src.getWidth())return src;
        Matrix m=new Matrix();m.setRotate(90f);
        try{return Bitmap.createBitmap(src,0,0,src.getWidth(),src.getHeight(),m,true);}catch(Exception e){return src;}
    }

    private void showPortraitForm703(long id){
        String path=formMap413().get(id);
        if(path==null||path.trim().isEmpty()){toast("Kayıt formu bulunamadı.");return;}
        formExec703.execute(()->{
            Bitmap decoded=null;
            try{
                String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");
                if(token.isEmpty())throw new IOException("Oturum anahtarı bulunamadı.");
                byte[] data=downloadForm703(path,token);
                BitmapFactory.Options o=new BitmapFactory.Options();o.inPreferredConfig=Bitmap.Config.RGB_565;
                decoded=BitmapFactory.decodeByteArray(data,0,data.length,o);
                if(decoded==null)throw new IOException("Form görseli okunamadı.");
                Bitmap shown=forcePortrait703(decoded);
                if(shown!=decoded)decoded.recycle();
                final Bitmap result=shown;
                runOnUiThread(()->{
                    ZoomImage403 iv=new ZoomImage403(this);iv.setImageBitmap(result);
                    FrameLayout f=new FrameLayout(this);f.setBackgroundColor(Color.BLACK);f.addView(iv,new FrameLayout.LayoutParams(-1,dp(650)));
                    AlertDialog d=new AlertDialog.Builder(this).setTitle("KAYIT FORMU").setView(f).setPositiveButton("KAPAT",null).create();
                    d.setOnDismissListener(x->result.recycle());d.show();
                });
            }catch(Exception e){
                if(decoded!=null&&!decoded.isRecycled())decoded.recycle();
                runOnUiThread(()->toast("Kayıt formu açılamadı. İnternet bağlantısını kontrol edin."));
            }
        });
    }

    private byte[] downloadForm703(String path,String token)throws Exception{
        URL u=new URL(SUPABASE_URL+"/storage/v1/object/registration-forms/"+encode703(path));
        HttpURLConnection h=(HttpURLConnection)u.openConnection();h.setConnectTimeout(15000);h.setReadTimeout(30000);
        h.setRequestProperty("apikey",SUPABASE_KEY);h.setRequestProperty("Authorization","Bearer "+token);
        int code=h.getResponseCode();if(code<200||code>=300){String err=read703(h.getErrorStream());h.disconnect();throw new IOException("HTTP "+code+" "+err);}
        byte[] data=readBytes703(h.getInputStream());h.disconnect();return data;
    }

    private byte[] readBytes703(InputStream in)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream x=in){byte[] b=new byte[16384];int n;while((n=x.read(b))>0)out.write(b,0,n);}return out.toByteArray();}

    private void showFormError703(String detail,long id){
        String msg="Kayıt formu yüklenemedi.\n\n"+clean703(detail)+"\n\nBu pencere siz kapatana kadar ekranda kalır.";
        new AlertDialog.Builder(this).setTitle("KAYIT FORMU YÜKLEME HATASI").setMessage(msg)
            .setPositiveButton("TAMAM",(d,w)->finishMediaReturn703(id)).show();
    }

    private long consumeMediaTarget703(){
        try{Field f=MainActivityV405.class.getDeclaredField("mediaTarget");f.setAccessible(true);long id=f.getLong(this);f.setLong(this,-1L);return id;}catch(Exception e){return currentAthlete;}
    }
    private void finishMediaReturn703(long id){
        try{Method m=MainActivityV405.class.getDeclaredMethod("finishMediaReturn405",long.class);m.setAccessible(true);m.invoke(this,id);}catch(Exception e){if(id>0)showProfile(id);else showHome();}
    }
    private String encode703(String p)throws Exception{StringBuilder b=new StringBuilder();String[] z=p.split("/");for(int i=0;i<z.length;i++){if(i>0)b.append('/');b.append(URLEncoder.encode(z[i],"UTF-8").replace("+","%20"));}return b.toString();}
    private String read703(InputStream in)throws Exception{if(in==null)return "";ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream x=in){byte[] b=new byte[8192];int n;while((n=x.read(b))>0)out.write(b,0,n);}return out.toString("UTF-8");}
    private String clean703(String s){if(s==null||s.trim().isEmpty())return "Sunucudan ayrıntılı hata mesajı gelmedi.";return s.trim().replace("\\u0000","");}
    static class HttpUploadResult703{final int code;final String body;HttpUploadResult703(int c,String b){code=c;body=b;}}

    @Override protected void onDestroy(){formExec703.shutdownNow();super.onDestroy();}
}
