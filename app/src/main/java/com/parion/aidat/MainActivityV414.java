package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

public class MainActivityV414 extends MainActivityV413 {
    private static final int REQ_MISSING_PHOTO_414=4141;
    private long missingPhotoTarget414=-1;
    private final ExecutorService upload414=Executors.newSingleThreadExecutor();

    @Override void showHome(){
        super.showHome();
        patchMissingPhotoCard414(root);
    }

    private void patchMissingPhotoCard414(View v){
        if(v instanceof TextView){
            String s=String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR"));
            if(s.contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")){
                View p=(View)v.getParent();
                p.setOnClickListener(x->showMissingPhotos414());
                return;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchMissingPhotoCard414(g.getChildAt(i));}
    }

    private void showMissingPhotos414(){
        page="MISSING_PHOTOS_414";base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);
        TextView info=tv("SPORCUYA DOKUNUN; GALERİDEN FOTOĞRAFI SEÇİN. YÜKLEME TAMAMLANINCA BU LİSTEYE GERİ DÖNÜLÜR.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){A x=a(c);if(photoMap413().containsKey(x.id))continue;addMissingRow414(b,x);n++;}c.close();
        if(n==0)b.addView(tv("FOTOĞRAFI OLMAYAN AKTİF SPORCU BULUNMUYOR.",14,GREEN,true));
    }

    private void addMissingRow414(LinearLayout b,A x){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));
        ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,x.photo);r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));
        LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+" • FOTOĞRAF EKLE",12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        r.setOnClickListener(v->{missingPhotoTarget414=x.id;pickPhoto414();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);
    }

    private void pickPhoto414(){Intent i;if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}else{i=new Intent(Intent.ACTION_PICK);i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");}startActivityForResult(i,REQ_MISSING_PHOTO_414);}

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_MISSING_PHOTO_414){
            long id=missingPhotoTarget414;missingPhotoTarget414=-1;
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0)uploadMissingPhoto414(id,data.getData());
            else showMissingPhotos414();
            return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void uploadMissingPhoto414(long id,Uri uri){
        upload414.execute(()->{
            try{
                String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");if(token.isEmpty())throw new IOException("OTURUM YOK");
                ensureAthleteCloud413(id,token);
                byte[] jpg=jpeg414(uri,1600,84);String path="user/"+id+"/photo_"+System.currentTimeMillis()+".jpg";
                int code=put414("athlete-photos",path,jpg,token);if(code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");code=put414("athlete-photos",path,jpg,token);}if(code<200||code>=300)throw new IOException("STORAGE "+code);
                HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/set_athlete_media_path",new JSONObject().put("p_legacy_id",id).put("p_kind","photo").put("p_path",path).toString(),token);
                if(r.code<200||r.code>=300||!r.body.contains("true"))throw new IOException("PATH "+r.code);
                photoMap413().put(id,path);db.getWritableDatabase().execSQL("UPDATE athletes SET photo=? WHERE id=?",new Object[]{"CLOUD:"+path,id});
                runOnUiThread(()->{toast("SPORCU FOTOĞRAFI BULUTA KAYDEDİLDİ.");showMissingPhotos414();});
            }catch(Exception e){runOnUiThread(()->{toast("FOTOĞRAF YÜKLENEMEDİ: "+(e.getMessage()==null?"BAĞLANTI":e.getMessage()));showMissingPhotos414();});}
        });
    }

    private int put414(String bucket,String path,byte[] data,String token)throws Exception{
        URL u=new URL(SUPABASE_URL+"/storage/v1/object/"+bucket+"/"+enc414(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();h.setRequestMethod("POST");h.setDoOutput(true);h.setConnectTimeout(15000);h.setReadTimeout(30000);h.setRequestProperty("apikey",SUPABASE_KEY);h.setRequestProperty("Authorization","Bearer "+token);h.setRequestProperty("Content-Type","image/jpeg");h.setRequestProperty("x-upsert","true");try(OutputStream o=h.getOutputStream()){o.write(data);}int c=h.getResponseCode();h.disconnect();return c;
    }
    private String enc414(String p)throws Exception{StringBuilder b=new StringBuilder();for(String z:p.split("/")){if(b.length()>0)b.append('/');b.append(URLEncoder.encode(z,"UTF-8").replace("+","%20"));}return b.toString();}
    private byte[] jpeg414(Uri uri,int target,int quality)throws Exception{BitmapFactory.Options bo=new BitmapFactory.Options();bo.inJustDecodeBounds=true;try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,bo);}int s=1;while(bo.outWidth>0&&bo.outHeight>0&&(bo.outWidth/s>target*2||bo.outHeight/s>target*2))s*=2;BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=Math.max(1,s);Bitmap bm;try(InputStream in=getContentResolver().openInputStream(uri)){bm=BitmapFactory.decodeStream(in,null,o);}if(bm==null)throw new IOException("GÖRSEL OKUNAMADI");int w=bm.getWidth(),h=bm.getHeight();if(Math.max(w,h)>target){float sc=(float)target/Math.max(w,h);Bitmap sm=Bitmap.createScaledBitmap(bm,Math.max(1,Math.round(w*sc)),Math.max(1,Math.round(h*sc)),true);if(sm!=bm)bm.recycle();bm=sm;}ByteArrayOutputStream out=new ByteArrayOutputStream();bm.compress(Bitmap.CompressFormat.JPEG,quality,out);bm.recycle();return out.toByteArray();}

    @Override void goBack(){if("MISSING_PHOTOS_414".equals(page)){showHome();return;}super.goBack();}
    @Override protected void onDestroy(){upload414.shutdownNow();super.onDestroy();}
}
