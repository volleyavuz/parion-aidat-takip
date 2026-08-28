package com.parion.aidat;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivityV384 extends MainActivityV383 {
    private static final int REQ_PICK_PHOTO = 3840;
    private String pendingPhoto = "";
    private String originalPhoto = "";
    private ImageView pendingPreview;
    private Button pendingPhotoButton;
    private Button pendingDeleteButton;

    @Override void showProfile(long id) {
        super.showProfile(id);
        ImageView photo = firstImage(root);
        if (photo != null) {
            photo.setClickable(true);
            photo.setOnClickListener(v -> showZoomPhoto(photo.getDrawable()));
        }
    }

    @Override void setAthletePhoto(ImageView v, String photo) {
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (photo != null && photo.startsWith("USER:")) {
            File f = new File(new File(getFilesDir(), "athlete_photos"), photo.substring(5));
            if (f.isFile()) {
                Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bm != null) { v.setImageBitmap(bm); return; }
            }
        }
        if ("NONE".equals(photo)) {
            if (loadDefaultPhoto(v)) return;
        }
        super.setAthletePhoto(v, photo);
    }

    private boolean loadDefaultPhoto(ImageView v){
        try(InputStream in=getAssets().open("photos/0000 BOS.jpg")){
            Bitmap bm=BitmapFactory.decodeStream(in);if(bm!=null){v.setImageBitmap(bm);return true;}
        }catch(Exception ignored){}
        return false;
    }

    @Override int[] applyCloudCache(org.json.JSONArray athletes, org.json.JSONArray payments) throws Exception {
        HashMap<Long,String> local = new HashMap<>();
        Cursor c = db.getReadableDatabase().rawQuery("SELECT id,photo FROM athletes WHERE photo LIKE 'USER:%' OR photo='NONE'", null);
        while (c.moveToNext()) local.put(c.getLong(0), c.getString(1));
        c.close();
        int[] r = super.applyCloudCache(athletes, payments);
        SQLiteDatabase d = db.getWritableDatabase();
        for (Map.Entry<Long,String> e : local.entrySet()) {
            ContentValues cv = new ContentValues(); cv.put("photo", e.getValue());
            d.update("athletes", cv, "id=?", new String[]{String.valueOf(e.getKey())});
        }
        return r;
    }

    @Override void form(long id) {
        page="FORM";
        base(id<0?"YENİ SPORCU KAYDI":"SPORCU BİLGİLERİNİ DÜZENLE",true);
        ScrollView sv=scroll(); LinearLayout b=box(sv);
        Cursor c=id<0?null:db.athlete(id); if(c!=null)c.moveToFirst();

        originalPhoto = c==null ? "" : v(c,"photo");
        pendingPhoto = originalPhoto;
        LinearLayout photoBox = new LinearLayout(this); photoBox.setOrientation(LinearLayout.VERTICAL); photoBox.setGravity(Gravity.CENTER_HORIZONTAL); photoBox.setPadding(0,dp(6),0,dp(12));
        pendingPreview = new ImageView(this); pendingPreview.setScaleType(ImageView.ScaleType.CENTER_CROP); setAthletePhoto(pendingPreview,pendingPhoto);
        photoBox.addView(pendingPreview,new LinearLayout.LayoutParams(dp(150),dp(150)));
        pendingPhotoButton=btn(hasPhoto(pendingPhoto)?"FOTOĞRAFI DEĞİŞTİR":"FOTOĞRAF EKLE");
        LinearLayout.LayoutParams pbp=new LinearLayout.LayoutParams(-1,dp(52)); pbp.setMargins(0,dp(8),0,0); photoBox.addView(pendingPhotoButton,pbp);
        pendingDeleteButton=btn("FOTOĞRAFI SİL"); pendingDeleteButton.setBackground(round(Color.rgb(245,210,210),12));
        LinearLayout.LayoutParams dbp=new LinearLayout.LayoutParams(-1,dp(52)); dbp.setMargins(0,dp(6),0,0); photoBox.addView(pendingDeleteButton,dbp);
        updatePhotoButtons(); b.addView(photoBox);
        pendingPhotoButton.setOnClickListener(vv->pickPhoto());
        pendingDeleteButton.setOnClickListener(vv->confirmPhotoDelete());
        pendingPreview.setOnClickListener(vv->showZoomPhoto(pendingPreview.getDrawable()));

        String restartEndValue=id>0?db.restartEndDate(id):"";
        EditText name=f("AD SOYAD",v(c,"name")),bd=f("DOĞUM TARİHİ (gg.aa.yyyy)",birth(c)),cat=f("GRUP / TAKIM",v(c,"category")),fee=f("AYLIK AİDAT ₺",n(c,"monthlyFee")),phone=f("SPORCU TELEFON",v(c,"phone")),mn=f("ANNE ADI",v(c,"motherName")),mp=f("ANNE TELEFON",v(c,"motherPhone")),fn=f("BABA ADI",v(c,"fatherName")),fp=f("BABA TELEFON",v(c,"fatherPhone")),start=f("BAŞLANGIÇ TARİHİ",dt(c,"startDate")),end=f("BİTİŞ / ARA VERME",dt(c,"endDate")),restart=f("YENİDEN BAŞLAMA",dt(c,"restartDate")),restartEnd=f("YENİDEN BIRAKMA / ARA VERME TARİHİ",restartEndValue.isEmpty()?"":dateTr(restartEndValue)),notes=f("ÖZEL NOTLAR",v(c,"notes"));
        fee.setInputType(2); notes.setMinLines(3);
        Spinner status=sp(new String[]{"AKTİF","ARA VERDİ","BIRAKTI","ARANACAK","SAKATLANDI"},v(c,"status"));
        Spinner sibling=sp(new String[]{"TEK","VAR","BURSLU"},v(c,"sibling")); if(c!=null)c.close();
        View[] vs={name,bd,cat,status,fee,sibling,phone,mn,mp,fn,fp,start,end,restart,restartEnd,notes}; for(View z:vs)b.addView(z);
        Button save=btn(id<0?"KAYDI OLUŞTUR":"DEĞİŞİKLİKLERİ KAYDET"); b.addView(save,new LinearLayout.LayoutParams(-1,dp(60)));
        save.setOnClickListener(vv->{
            if(name.getText().toString().trim().isEmpty()){Toast.makeText(this,"Ad Soyad zorunludur.",Toast.LENGTH_SHORT).show();return;}
            ContentValues cv=new ContentValues(); String bi=iso(bd.getText().toString());
            cv.put("name",name.getText().toString().trim().toUpperCase(new Locale("tr","TR"))); cv.put("birthDate",bi); cv.put("birthYear",bi.length()>=4?parseInt(bi.substring(0,4)):0);
            cv.put("category",cat.getText().toString().trim().toUpperCase(new Locale("tr","TR"))); cv.put("status",String.valueOf(status.getSelectedItem())); cv.put("monthlyFee",parseInt(fee.getText().toString())); cv.put("sibling",String.valueOf(sibling.getSelectedItem()));
            cv.put("phone",phone.getText().toString().trim()); cv.put("motherName",mn.getText().toString().trim()); cv.put("motherPhone",mp.getText().toString().trim()); cv.put("fatherName",fn.getText().toString().trim()); cv.put("fatherPhone",fp.getText().toString().trim());
            cv.put("startDate",iso(start.getText().toString())); cv.put("endDate",iso(end.getText().toString())); cv.put("restartDate",iso(restart.getText().toString())); cv.put("notes",notes.getText().toString().trim()); cv.put("photo",pendingPhoto==null?"":pendingPhoto);
            SQLiteDatabase d=db.getWritableDatabase(); long saved=id;
            if(id<0){Cursor m=d.rawQuery("SELECT COALESCE(MAX(seq),0)+1 FROM athletes",null);m.moveToFirst();cv.put("seq",m.getInt(0));m.close();saved=d.insert("athletes",null,cv);} else d.update("athletes",cv,"id=?",new String[]{String.valueOf(id)});
            if(saved>0){db.setRestartEndDate(saved,iso(restartEnd.getText().toString()));deleteOldUserPhotoIfNeeded();Toast.makeText(this,"Kayıt kaydedildi.",Toast.LENGTH_SHORT).show();showProfile(saved);}
        });
    }

    private boolean hasPhoto(String p){return p!=null&&!p.trim().isEmpty()&&!"NONE".equals(p);}
    private void updatePhotoButtons(){if(pendingPhotoButton!=null)pendingPhotoButton.setText(hasPhoto(pendingPhoto)?"FOTOĞRAFI DEĞİŞTİR":"FOTOĞRAF EKLE");if(pendingDeleteButton!=null)pendingDeleteButton.setVisibility(hasPhoto(pendingPhoto)?View.VISIBLE:View.GONE);}
    private void confirmPhotoDelete(){
        if(!hasPhoto(pendingPhoto))return;
        new android.app.AlertDialog.Builder(this).setTitle("FOTOĞRAFI SİL").setMessage("Sporcunun fotoğrafı kaldırılsın mı? Değişiklik, kayıt kaydedildiğinde uygulanır.").setPositiveButton("EVET, SİL",(d,w)->{pendingPhoto="NONE";if(pendingPreview!=null)setAthletePhoto(pendingPreview,pendingPhoto);updatePhotoButtons();toast("Fotoğraf kaldırıldı. Değişikliği kaydetmeyi unutmayın.");}).setNegativeButton("VAZGEÇ",null).show();
    }
    private void deleteOldUserPhotoIfNeeded(){
        if(originalPhoto!=null&&originalPhoto.startsWith("USER:")&&!originalPhoto.equals(pendingPhoto)){
            File f=new File(new File(getFilesDir(),"athlete_photos"),originalPhoto.substring(5));if(f.isFile())f.delete();
        }
    }

    private void pickPhoto(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("image/*"); startActivityForResult(i,REQ_PICK_PHOTO);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=REQ_PICK_PHOTO||resultCode!=RESULT_OK||data==null||data.getData()==null)return;
        Uri u=data.getData();
        try{
            File dir=new File(getFilesDir(),"athlete_photos"); if(!dir.exists())dir.mkdirs();
            String fn="user_"+System.currentTimeMillis()+".img"; File out=new File(dir,fn);
            try(InputStream in=getContentResolver().openInputStream(u); OutputStream os=new FileOutputStream(out)){
                byte[] buf=new byte[8192]; int n; while(in!=null&&(n=in.read(buf))>0)os.write(buf,0,n);
            }
            Bitmap bm=BitmapFactory.decodeFile(out.getAbsolutePath()); if(bm==null){out.delete();toast("Fotoğraf okunamadı.");return;}
            pendingPhoto="USER:"+fn; if(pendingPreview!=null)pendingPreview.setImageBitmap(bm); updatePhotoButtons(); toast("Fotoğraf seçildi. Kaydet düğmesine basın.");
        }catch(Exception e){toast("Fotoğraf eklenemedi: "+shortMsg(e));}
    }

    private ImageView firstImage(View v){
        if(v instanceof ImageView)return (ImageView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ImageView x=firstImage(g.getChildAt(i));if(x!=null)return x;}}
        return null;
    }

    private void showZoomPhoto(Drawable drawable){
        if(drawable==null)return;
        Dialog d=new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen); d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout frame=new FrameLayout(this); frame.setBackgroundColor(Color.BLACK);
        ZoomImageView z=new ZoomImageView(this); z.setImageDrawable(drawable); frame.addView(z,new FrameLayout.LayoutParams(-1,-1));
        TextView close=tv("✕",28,Color.WHITE,true); close.setGravity(Gravity.CENTER); close.setBackgroundColor(0x66000000); FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(dp(56),dp(56),Gravity.TOP|Gravity.END); cp.setMargins(0,dp(18),dp(12),0); frame.addView(close,cp); close.setOnClickListener(v->d.dismiss());
        TextView hint=tv("İKİ PARMAKLA BÜYÜT / KÜÇÜLT",12,Color.WHITE,true); hint.setGravity(Gravity.CENTER); FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,dp(44),Gravity.BOTTOM); frame.addView(hint,hp);
        d.setContentView(frame); d.show();
    }

    static class ZoomImageView extends androidx.appcompat.widget.AppCompatImageView {
        final Matrix matrix=new Matrix(); float scale=1f; final ScaleGestureDetector detector;
        ZoomImageView(android.content.Context c){super(c);setScaleType(ScaleType.MATRIX);detector=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){@Override public boolean onScale(ScaleGestureDetector sd){float f=sd.getScaleFactor();float ns=Math.max(1f,Math.min(5f,scale*f));f=ns/scale;scale=ns;matrix.postScale(f,f,sd.getFocusX(),sd.getFocusY());setImageMatrix(matrix);return true;}});}
        @Override public boolean onTouchEvent(MotionEvent e){detector.onTouchEvent(e);return true;}
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);fit();}
        @Override public void setImageDrawable(Drawable d){super.setImageDrawable(d);post(this::fit);}
        void fit(){Drawable d=getDrawable();if(d==null||getWidth()==0||getHeight()==0)return;float dw=d.getIntrinsicWidth(),dh=d.getIntrinsicHeight();if(dw<=0||dh<=0)return;float s=Math.min((float)getWidth()/dw,(float)getHeight()/dh);float dx=(getWidth()-dw*s)/2f,dy=(getHeight()-dh*s)/2f;matrix.reset();matrix.postScale(s,s);matrix.postTranslate(dx,dy);setImageMatrix(matrix);scale=1f;}
    }
}
