package com.parion.aidat;

import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.concurrent.*;

public class MainActivityV394 extends MainActivityV393 {
    private static final int PAID_LIGHT_GREEN=Color.rgb(198,239,206);
    private final ExecutorService profilePhotoPool=Executors.newSingleThreadExecutor();
    private final android.util.LruCache<String,Bitmap> profilePhotoCache=new android.util.LruCache<String,Bitmap>(16){@Override protected int sizeOf(String k,Bitmap v){return 1;}};

    @Override int paymentColor(int m,int fee,String sibling,String start,String end,String restart,String marker,int amount){int c=super.paymentColor(m,fee,sibling,start,end,restart,marker,amount);return c==GREEN?PAID_LIGHT_GREEN:c;}

    @Override void showProfile(long id){
        page="PROFILE";currentAthlete=id;
        super.showProfile(id);
        ImageView photo=firstProfileImage(root);String key=profilePhotoKey(id);
        if(photo!=null&&!key.isEmpty())loadProfilePhotoAsync(photo,key);
    }

    @Override void setAthletePhoto(ImageView v,String photo){
        if("PROFILE".equals(page)){
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);v.setImageDrawable(new ColorDrawable(Color.rgb(230,230,230)));v.setTag(photo==null?"":photo);return;
        }
        super.setAthletePhoto(v,photo);
    }

    private String profilePhotoKey(long id){Cursor c=db.athlete(id);String p="";if(c.moveToFirst())p=v(c,"photo");c.close();return p==null?"":p;}
    private ImageView firstProfileImage(View v){if(v instanceof ImageView)return (ImageView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ImageView x=firstProfileImage(g.getChildAt(i));if(x!=null)return x;}}return null;}
    private void loadProfilePhotoAsync(ImageView view,String key){view.setTag(key);Bitmap cached=profilePhotoCache.get(key);if(cached!=null){view.setImageBitmap(cached);return;}profilePhotoPool.execute(()->{Bitmap bm=decodeProfileThumb(key);if(bm==null)return;profilePhotoCache.put(key,bm);runOnUiThread(()->{Object tag=view.getTag();if(tag!=null&&key.equals(String.valueOf(tag)))view.setImageBitmap(bm);});});}
    private Bitmap decodeProfileThumb(String photo){try{if(photo.startsWith("USER:")){File f=new File(new File(getFilesDir(),"athlete_photos"),photo.substring(5));if(!f.isFile())return decodeAssetProfile("0000 BOS.jpg");BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;BitmapFactory.decodeFile(f.getAbsolutePath(),b);BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sampleFor(b.outWidth,b.outHeight,512);o.inPreferredConfig=Bitmap.Config.RGB_565;return BitmapFactory.decodeFile(f.getAbsolutePath(),o);}String p=(photo.trim().isEmpty()||"NONE".equalsIgnoreCase(photo))?"0000 BOS.jpg":photo.trim();return decodeAssetProfile(p);}catch(Exception e){return null;}}
    private Bitmap decodeAssetProfile(String p){try{BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;try(InputStream in=getAssets().open("photos/"+p)){BitmapFactory.decodeStream(in,null,b);}BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sampleFor(b.outWidth,b.outHeight,512);o.inPreferredConfig=Bitmap.Config.RGB_565;try(InputStream in=getAssets().open("photos/"+p)){return BitmapFactory.decodeStream(in,null,o);}}catch(Exception e){if(!"0000 BOS.jpg".equals(p))return decodeAssetProfile("0000 BOS.jpg");return null;}}
    private int sampleFor(int w,int h,int target){int s=1;while(w>0&&h>0&&(w/s>target*2||h/s>target*2))s*=2;return Math.max(1,s);}

    @Override public void onBackPressed(){
        if("MATERIAL_PRICES".equals(page)&&currentAthlete>0){showProfile(currentAthlete);return;}
        if("MATERIAL_DEBTS".equals(page)){showHome();return;}
        if(currentAthlete>0&&!("HOME".equals(page)||"LIST".equals(page)||"FORM".equals(page)||"PROFILE".equals(page))){showProfile(currentAthlete);return;}
        super.onBackPressed();
    }

    @Override protected void onDestroy(){profilePhotoPool.shutdownNow();super.onDestroy();}
}
