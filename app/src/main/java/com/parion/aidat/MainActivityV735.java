package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;
import org.json.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** v4.2.11 - LAST WRITE WINS sync + durable cloud photo refs + EXIF-normalized photo upload. */
public class MainActivityV735 extends MainActivityV734 {
    private final ExecutorService lww735=Executors.newSingleThreadExecutor();
    private final ExecutorService media735=Executors.newSingleThreadExecutor();
    private volatile boolean lwwRunning735=false;
    private volatile boolean repairingPhotos735=false;
    private static final int REQ_CLOUD_PHOTO_735=4052;

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        media735.execute(this::repairPhotoRefs735);
    }

    /**
     * Normal sync policy: only locally dirty athletes are pushed. No conflict dialog.
     * A stale but untouched device is not dirty and therefore cannot overwrite newer cloud data.
     * If both devices truly edited the same athlete, the last successful cloud write wins.
     */
    @Override void syncFromCloud(boolean announce){
        if(lwwRunning735||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty()||db==null){
            if(!lwwRunning735)super.syncFromCloud(announce);
            return;
        }
        if(db.count(null)==0){super.syncFromCloud(announce);return;}
        lwwRunning735=true;
        if(announce)toast("SENKRONİZASYON • EN GÜNCEL DEĞİŞİKLİK KAZANIR...");
        lww735.execute(()->{
            try{
                ArrayList<Long> dirty=findDirty735();
                for(long id:dirty)pushAthleteLww735(id);
                runOnUiThread(()->{
                    lwwRunning735=false;
                    // Every dirty record has now been baselined; inherited safe engine therefore
                    // performs pull/attendance sync without producing an athlete conflict popup.
                    MainActivityV735.super.syncFromCloud(announce);
                    media735.execute(()->{try{Thread.sleep(1800L);}catch(Exception ignored){}repairPhotoRefs735();});
                });
            }catch(Exception e){
                String m=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();
                runOnUiThread(()->{lwwRunning735=false;toast("SENKRONİZASYON DURDU • "+m);});
            }
        });
    }

    private ArrayList<Long> findDirty735(){
        ArrayList<Long> out=new ArrayList<>();Cursor c=db.getReadableDatabase().rawQuery("SELECT id FROM athletes",null);
        while(c.moveToNext()){long id=c.getLong(0);if(!hash735(id).equals(saved735(id)))out.add(id);}c.close();return out;
    }
    private String saved735(long id){Cursor c=db.getReadableDatabase().rawQuery("SELECT localHash FROM sync_state WHERE entity='ATHLETE' AND entityKey=?",new String[]{String.valueOf(id)});String x="";if(c.moveToFirst()&&!c.isNull(0))x=c.getString(0);c.close();return x==null?"":x;}
    private String hash735(long id){
        StringBuilder b=new StringBuilder();SQLiteDatabase d=db.getReadableDatabase();
        Cursor a=d.rawQuery("SELECT * FROM athletes WHERE id=?",new String[]{String.valueOf(id)});if(a.moveToFirst())for(int i=0;i<a.getColumnCount();i++){String n=a.getColumnName(i);if("photo".equalsIgnoreCase(n))continue;b.append(n).append('=').append(a.isNull(i)?"":a.getString(i)).append('|');}a.close();
        Cursor p=d.rawQuery("SELECT year,month,marker,amount FROM payments WHERE athleteId=? ORDER BY year,month",new String[]{String.valueOf(id)});while(p.moveToNext())b.append("P:").append(p.getInt(0)).append(':').append(p.getInt(1)).append(':').append(p.getString(2)).append(':').append(p.getInt(3)).append('|');p.close();
        Cursor f=d.rawQuery("SELECT year,effectiveMonth,fee FROM fee_history WHERE athleteId=? ORDER BY year,effectiveMonth",new String[]{String.valueOf(id)});while(f.moveToNext())b.append("F:").append(f.getInt(0)).append(':').append(f.getInt(1)).append(':').append(f.getInt(2)).append('|');f.close();
        return Integer.toHexString(b.toString().hashCode());
    }
    private JSONObject body735(long id)throws Exception{
        Method m=MainActivityV727.class.getDeclaredMethod("body727",long.class);m.setAccessible(true);return (JSONObject)m.invoke(this,id);
    }
    private void pushAthleteLww735(long id)throws Exception{
        JSONObject body=body735(id);String token=cloudPrefs.getString("access_token","");
        HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_lww_v411",body.toString(),token);
        if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_sync_one_athlete_lww_v411",body.toString(),token);}
        if(r.code<200||r.code>=300)throw new IOException("Sporcu "+id+" HTTP "+r.code);
        ContentValues v=new ContentValues();v.put("entity","ATHLETE");v.put("entityKey",String.valueOf(id));v.put("localHash",hash735(id));v.put("lastSyncedAt",System.currentTimeMillis());
        db.getWritableDatabase().insertWithOnConflict("sync_state",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }

    /** Cloud media is authoritative for photo references, but an empty cloud path never erases a local photo. */
    private void repairPhotoRefs735(){
        if(repairingPhotos735||cloudPrefs==null||cloudPrefs.getString("access_token","").isEmpty()||db==null)return;
        repairingPhotos735=true;
        try{
            HttpResult r=getAuthed("/rest/v1/athletes?select=legacy_id,photo_path&photo_path=not.is.null&order=legacy_id.asc");
            if(r.code==401&&refreshSession())r=getAuthed("/rest/v1/athletes?select=legacy_id,photo_path&photo_path=not.is.null&order=legacy_id.asc");
            if(r.code<200||r.code>=300)return;
            JSONArray a=new JSONArray(r.body);SQLiteDatabase d=db.getWritableDatabase();d.beginTransaction();
            try{
                for(int i=0;i<a.length();i++){
                    JSONObject o=a.getJSONObject(i);long id=o.optLong("legacy_id",-1);String p=o.optString("photo_path","").trim();if(id<=0||p.isEmpty()||"null".equalsIgnoreCase(p))continue;
                    ContentValues v=new ContentValues();v.put("photo","CLOUD:"+p);d.update("athletes",v,"id=?",new String[]{String.valueOf(id)});photoMap413().put(id,p);
                }
                d.setTransactionSuccessful();
            }finally{d.endTransaction();}
        }catch(Exception ignored){}finally{repairingPhotos735=false;}
    }

    @Override void showProfile(long id){
        // Repair mapping before the inherited profile renderer uses the local photo field whenever possible.
        String p=photoMap413().get(id);if(p!=null&&!p.trim().isEmpty()){
            try{ContentValues v=new ContentValues();v.put("photo","CLOUD:"+p);db.getWritableDatabase().update("athletes",v,"id=?",new String[]{String.valueOf(id)});}catch(Exception ignored){}
        }
        super.showProfile(id);
        if(p==null)media735.execute(this::repairPhotoRefs735);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_CLOUD_PHOTO_735){
            long id=mediaTargetReflection735();setMediaTargetReflection735(-1L);
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0){
                Uri uri=data.getData();media735.execute(()->uploadNormalizedPhoto735(id,uri));
            }else if(id>0)runOnUiThread(()->showProfile(id));
            return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private long mediaTargetReflection735(){try{Field f=MainActivityV405.class.getDeclaredField("mediaTarget");f.setAccessible(true);return f.getLong(this);}catch(Exception e){return -1L;}}
    private void setMediaTargetReflection735(long x){try{Field f=MainActivityV405.class.getDeclaredField("mediaTarget");f.setAccessible(true);f.setLong(this,x);}catch(Exception ignored){}}

    private void uploadNormalizedPhoto735(long id,Uri uri){
        try{
            byte[] jpg=normalizedJpeg735(uri,1600,84);
            String old=photoMap413().get(id);String path="user/"+id+"/photo_"+System.currentTimeMillis()+".jpg";
            if(!uploadBinary735("athlete-photos",path,jpg))throw new IOException("storage");
            patchPhotoPath735(id,path);photoMap413().put(id,path);
            ContentValues v=new ContentValues();v.put("photo","CLOUD:"+path);db.getWritableDatabase().update("athletes",v,"id=?",new String[]{String.valueOf(id)});
            if(old!=null&&!old.equals(path))deleteBinary735("athlete-photos",old);
            runOnUiThread(()->{toast("Sporcu fotoğrafı doğru yönde buluta kaydedildi.");showProfile(id);});
        }catch(Exception e){runOnUiThread(()->{toast("Fotoğraf yüklenemedi.");showProfile(id);});}
    }

    private byte[] normalizedJpeg735(Uri uri,int target,int quality)throws Exception{
        int orientation=ExifInterface.ORIENTATION_NORMAL;
        try(InputStream in=getContentResolver().openInputStream(uri)){if(in!=null)orientation=new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);}
        BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,b);}
        BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sample735(b.outWidth,b.outHeight,target);Bitmap bm;try(InputStream in=getContentResolver().openInputStream(uri)){bm=BitmapFactory.decodeStream(in,null,o);}if(bm==null)throw new IOException("bitmap");
        Matrix m=new Matrix();
        switch(orientation){
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:m.setScale(-1,1);break;
            case ExifInterface.ORIENTATION_ROTATE_180:m.setRotate(180);break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:m.setRotate(180);m.postScale(-1,1);break;
            case ExifInterface.ORIENTATION_TRANSPOSE:m.setRotate(90);m.postScale(-1,1);break;
            case ExifInterface.ORIENTATION_ROTATE_90:m.setRotate(90);break;
            case ExifInterface.ORIENTATION_TRANSVERSE:m.setRotate(-90);m.postScale(-1,1);break;
            case ExifInterface.ORIENTATION_ROTATE_270:m.setRotate(-90);break;
            default:break;
        }
        Bitmap out=bm;if(!m.isIdentity()){out=Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight(),m,true);if(out!=bm)bm.recycle();}
        ByteArrayOutputStream bos=new ByteArrayOutputStream();out.compress(Bitmap.CompressFormat.JPEG,quality,bos);out.recycle();return bos.toByteArray();
    }
    private int sample735(int w,int h,int target){int s=1;while(w>0&&h>0&&(w/s>target*2||h/s>target*2))s*=2;return Math.max(1,s);}

    private boolean uploadBinary735(String bucket,String path,byte[] data)throws Exception{
        String token=cloudPrefs.getString("access_token","");URL u=new URL(SUPABASE_URL+"/storage/v1/object/"+bucket+"/"+encode735(path));HttpURLConnection h=(HttpURLConnection)u.openConnection();h.setRequestMethod("POST");h.setDoOutput(true);h.setConnectTimeout(15000);h.setReadTimeout(30000);h.setRequestProperty("apikey",SUPABASE_KEY);h.setRequestProperty("Authorization","Bearer "+token);h.setRequestProperty("Content-Type","image/jpeg");h.setRequestProperty("x-upsert","true");try(OutputStream out=h.getOutputStream()){out.write(data);}int c=h.getResponseCode();h.disconnect();return c>=200&&c<300;
    }
    private void deleteBinary735(String bucket,String path){try{String token=cloudPrefs.getString("access_token","");request("DELETE",SUPABASE_URL+"/storage/v1/object/"+bucket+"/"+encode735(path),null,token);}catch(Exception ignored){}}
    private void patchPhotoPath735(long id,String path)throws Exception{
        String token=cloudPrefs.getString("access_token","");JSONObject j=new JSONObject().put("photo_path",path);HttpResult r=request("PATCH",SUPABASE_URL+"/rest/v1/athletes?legacy_id=eq."+id,j.toString(),token);if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("PATCH",SUPABASE_URL+"/rest/v1/athletes?legacy_id=eq."+id,j.toString(),token);}if(r.code<200||r.code>=300)throw new IOException("photo path HTTP "+r.code);
    }
    private String encode735(String s)throws Exception{return URLEncoder.encode(s,"UTF-8").replace("%2F","/");}

    @Override protected void onDestroy(){lww735.shutdownNow();media735.shutdownNow();super.onDestroy();}
}
