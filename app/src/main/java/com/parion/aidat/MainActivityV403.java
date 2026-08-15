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
import java.util.*;
import java.util.concurrent.*;

public class MainActivityV403 extends MainActivityV399 {
    private static final int REQ_FORM_IMG=4031;
    private long formTarget=-1;
    private boolean formReturnMissing=false;
    private final ExecutorService thumbPool=Executors.newFixedThreadPool(2);
    private final android.util.LruCache<String,Bitmap> thumbs=new android.util.LruCache<String,Bitmap>(80){@Override protected int sizeOf(String k,Bitmap b){return 1;}};

    @Override public void onCreate(Bundle b){super.onCreate(b);}

    @Override void setAthletePhoto(ImageView v,String photo){
        if("PROFILE".equals(page)){super.setAthletePhoto(v,photo);return;}
        final String key=photo==null?"":photo;
        Bitmap cached=thumbs.get(key);if(cached!=null){v.setImageBitmap(cached);return;}
        v.setImageDrawable(new ColorDrawable(Color.rgb(232,232,232)));v.setTag(key);
        thumbPool.execute(()->{Bitmap bm=decodeThumb403(key,180);if(bm==null)return;thumbs.put(key,bm);runOnUiThread(()->{Object tag=v.getTag();if(tag!=null&&key.equals(String.valueOf(tag)))v.setImageBitmap(bm);});});
    }

    private Bitmap decodeThumb403(String photo,int target){
        try{
            if(photo!=null&&photo.startsWith("USER:")){
                File f=new File(new File(getFilesDir(),"athlete_photos"),photo.substring(5));if(!f.isFile())return decodeAsset403("0000 BOS.jpg",target);
                BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;BitmapFactory.decodeFile(f.getAbsolutePath(),b);
                BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sample403(b.outWidth,b.outHeight,target);o.inPreferredConfig=Bitmap.Config.RGB_565;return BitmapFactory.decodeFile(f.getAbsolutePath(),o);
            }
            String p=(photo==null||photo.trim().isEmpty()||"NONE".equalsIgnoreCase(photo))?"0000 BOS.jpg":photo.trim();return decodeAsset403(p,target);
        }catch(Exception e){return null;}
    }
    private Bitmap decodeAsset403(String p,int target){try{BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;try(InputStream in=getAssets().open("photos/"+p)){BitmapFactory.decodeStream(in,null,b);}BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sample403(b.outWidth,b.outHeight,target);o.inPreferredConfig=Bitmap.Config.RGB_565;try(InputStream in=getAssets().open("photos/"+p)){return BitmapFactory.decodeStream(in,null,o);}}catch(Exception e){if(!"0000 BOS.jpg".equals(p))return decodeAsset403("0000 BOS.jpg",target);return null;}}
    private int sample403(int w,int h,int target){int s=1;while(w>0&&h>0&&(w/s>target*2||h/s>target*2))s*=2;return Math.max(1,s);}

    @Override void showProfile(long id){
        super.showProfile(id);
        replaceFormButtons403(root,id,false);
    }
    @Override void showHome(){
        super.showHome();
        replaceMissingFormCard403(root);
    }
    private void replaceMissingFormCard403(View v){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof TextView&&String.valueOf(((TextView)c).getText()).contains("KAYIT FORMU OLMAYAN AKTİF SPORCULAR")){
                View parent=(View)c.getParent();parent.setOnClickListener(x->showMissingForms403());return;
            }
            replaceMissingFormCard403(c);
        }
    }
    private void replaceFormButtons403(View v,long id,boolean missing){
        if(!(v instanceof ViewGroup))return;ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View c=g.getChildAt(i);
            if(c instanceof Button){String s=String.valueOf(((Button)c).getText());
                if(s.contains("KAYIT FORMUNU GÖRÜNTÜLE"))c.setOnClickListener(x->showForm403(id));
                else if(s.contains("KAYIT FORMU EKLE")||s.contains("KAYIT FORMUNU DEĞİŞTİR"))c.setOnClickListener(x->pickForm403(id,missing));
            }
            replaceFormButtons403(c,id,missing);
        }
    }

    private void showMissingForms403(){
        page="MISSING_FORMS";base("KAYIT FORMU OLMAYAN AKTİF SPORCULAR",true);
        TextView info=tv("Sporcuya dokunun; galeriden kayıt formu görselini seçin. İşlem tamamlanınca bu listeye dönülür.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(hasForm403(x.id))continue;addMissingRow403(b,x);n++;}c.close();if(n==0)b.addView(tv("Kayıt formu olmayan aktif sporcu bulunmuyor.",14,GREEN,true));
    }
    private boolean hasForm403(long id){Cursor c=db.getReadableDatabase().rawQuery("SELECT fileRef FROM registration_forms WHERE athleteId=?",new String[]{String.valueOf(id)});boolean ok=c.moveToFirst()&&c.getString(0)!=null&&!c.getString(0).trim().isEmpty();c.close();if(ok)return true;return bundledForm403(id)!=null;}
    private void addMissingRow403(LinearLayout b,A x){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,x.photo);r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+" • KAYIT FORMU EKLE",12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));r.setOnClickListener(v->pickForm403(x.id,true));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);}

    private void pickForm403(long id,boolean missing){formTarget=id;formReturnMissing=missing;Intent i;if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}else{i=new Intent(Intent.ACTION_PICK);i.setType("image/*");}startActivityForResult(i,REQ_FORM_IMG);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_FORM_IMG){long id=formTarget;boolean missing=formReturnMissing;formTarget=-1;formReturnMissing=false;if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0)saveFormImage403(data.getData(),id);if(missing)showMissingForms403();else if(id>0)showProfile(id);return;}super.onActivityResult(requestCode,resultCode,data);
    }
    private void saveFormImage403(Uri uri,long id){
        try{String mime=getContentResolver().getType(uri);if(mime==null||!mime.startsWith("image/")){toast("Yalnız JPEG veya PNG görsel seçilebilir.");return;}BitmapFactory.Options b=new BitmapFactory.Options();b.inJustDecodeBounds=true;try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,b);}BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sample403(b.outWidth,b.outHeight,2200);Bitmap bm;try(InputStream in=getContentResolver().openInputStream(uri)){bm=BitmapFactory.decodeStream(in,null,o);}if(bm==null){toast("Görsel okunamadı.");return;}File dir=new File(getFilesDir(),"registration_forms");if(!dir.exists())dir.mkdirs();String fn="form_"+id+"_"+System.currentTimeMillis()+".jpg";File f=new File(dir,fn);try(OutputStream out=new FileOutputStream(f)){bm.compress(Bitmap.CompressFormat.JPEG,82,out);}bm.recycle();db.getWritableDatabase().execSQL("INSERT OR REPLACE INTO registration_forms(athleteId,fileRef,mimeType,originalName,updatedAt) VALUES(?,?,?,?,datetime('now'))",new Object[]{id,"USER:"+fn,"image/jpeg",fn});toast("Kayıt formu kaydedildi.");}catch(Exception e){toast("Kayıt formu eklenemedi.");}
    }

    private String bundledForm403(long id){
        try{Cursor a=db.athlete(id);if(!a.moveToFirst()){a.close();return null;}int year=a.getInt(a.getColumnIndexOrThrow("birthYear"));String name=v(a,"name");a.close();String wanted=norm403(name);try(InputStream in=getAssets().open("forms/index.tsv");BufferedReader br=new BufferedReader(new InputStreamReader(in,"UTF-8"))){String line;while((line=br.readLine())!=null){String[] z=line.split("\\t",5);if(z.length<5)continue;if(Integer.parseInt(z[0])==year&&norm403(z[1]).equals(wanted))return z[2];}}}catch(Exception ignored){}return null;
    }
    private String norm403(String s){if(s==null)return "";String x=s.toUpperCase(new Locale("tr","TR")).replace('İ','I').replace('ı','I');x=java.text.Normalizer.normalize(x,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+","");return x.replaceAll("[^A-Z0-9]","");}
    private void showForm403(long id){
        try{InputStream in=null;Cursor c=db.getReadableDatabase().rawQuery("SELECT fileRef FROM registration_forms WHERE athleteId=?",new String[]{String.valueOf(id)});if(c.moveToFirst()){String r=c.getString(0);if(r!=null&&r.startsWith("USER:"))in=new FileInputStream(new File(new File(getFilesDir(),"registration_forms"),r.substring(5)));}c.close();if(in==null){String b=bundledForm403(id);if(b!=null)in=getAssets().open("forms/bundled/"+b);}if(in==null){toast("Kayıt formu bulunamadı.");return;}BitmapFactory.Options o=new BitmapFactory.Options();o.inPreferredConfig=Bitmap.Config.RGB_565;Bitmap bm=BitmapFactory.decodeStream(in,null,o);in.close();if(bm==null){toast("Kayıt formu açılamadı.");return;}ZoomImage403 iv=new ZoomImage403(this);iv.setImageBitmap(bm);FrameLayout frame=new FrameLayout(this);frame.setBackgroundColor(Color.BLACK);frame.addView(iv,new FrameLayout.LayoutParams(-1,dp(650)));new AlertDialog.Builder(this).setTitle("KAYIT FORMU").setView(frame).setPositiveButton("KAPAT",null).show();}catch(Exception e){toast("Kayıt formu açılamadı.");}
    }

    static class ZoomImage403 extends androidx.appcompat.widget.AppCompatImageView{
        private final ScaleGestureDetector scale;private float factor=1f;private float lastX,lastY,tx,ty;private boolean drag=false;
        ZoomImage403(Context c){super(c);setScaleType(ScaleType.MATRIX);scale=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){@Override public boolean onScale(ScaleGestureDetector d){factor=Math.max(1f,Math.min(5f,factor*d.getScaleFactor()));apply();return true;}});}
        private void apply(){Drawable dr=getDrawable();if(dr==null)return;float vw=getWidth(),vh=getHeight(),iw=dr.getIntrinsicWidth(),ih=dr.getIntrinsicHeight();if(vw<=0||vh<=0||iw<=0||ih<=0)return;float base=Math.min(vw/iw,vh/ih),s=base*factor;float maxX=Math.max(0,(iw*s-vw)/2),maxY=Math.max(0,(ih*s-vh)/2);tx=Math.max(-maxX,Math.min(maxX,tx));ty=Math.max(-maxY,Math.min(maxY,ty));Matrix m=new Matrix();m.postScale(s,s);m.postTranslate((vw-iw*s)/2+tx,(vh-ih*s)/2+ty);setImageMatrix(m);}
        @Override protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);post(this::apply);}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){scale.onTouchEvent(e);if(e.getPointerCount()==1&&!scale.isInProgress()){switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:lastX=e.getX();lastY=e.getY();drag=true;break;case MotionEvent.ACTION_MOVE:if(drag&&factor>1f){tx+=e.getX()-lastX;ty+=e.getY()-lastY;lastX=e.getX();lastY=e.getY();apply();}break;case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:drag=false;break;}}return true;}
    }

    @Override void goBack(){if("MISSING_FORMS".equals(page)){showHome();return;}super.goBack();}
    @Override protected void onDestroy(){thumbPool.shutdownNow();super.onDestroy();}
}
