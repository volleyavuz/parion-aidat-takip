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

public class MainActivityV415 extends MainActivityV414 {
    private static final int REQ_MISSING_FORM_415=4151;
    private long missingFormTarget415=-1;
    private final ExecutorService upload415=Executors.newSingleThreadExecutor();

    @Override void showHome(){
        super.showHome();
        patchHome415(root);
        restoreRealLogo415(root);
    }

    private void patchHome415(View v){
        if(v instanceof TextView){
            String s=String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR"));
            if(s.contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")){View p=(View)v.getParent();p.setOnClickListener(x->showMissingPhotos415());}
            if(s.contains("KAYIT FORMU OLMAYAN AKTİF SPORCULAR")){View p=(View)v.getParent();p.setOnClickListener(x->showMissingForms415());}
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchHome415(g.getChildAt(i));}
    }

    private void restoreRealLogo415(View v){
        try{
            Bitmap bm=null;try(InputStream in=getAssets().open("parion_logo.png")){bm=BitmapFactory.decodeStream(in);}if(bm==null)return;
            ImageView target=findLargeImage415(v);if(target!=null){target.setImageBitmap(bm);target.setScaleType(ImageView.ScaleType.CENTER_INSIDE);}
        }catch(Exception ignored){}
    }
    private ImageView findLargeImage415(View v){
        if(v instanceof ImageView&&v.getLayoutParams()!=null&&v.getLayoutParams().height>=dp(90))return (ImageView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ImageView r=findLargeImage415(g.getChildAt(i));if(r!=null)return r;}}return null;
    }

    private boolean hasActualPhoto415(A x){
        if(photoMap413().containsKey(x.id))return true;
        String p=x.photo==null?"":x.photo.trim();
        return (p.startsWith("CLOUD:")||p.startsWith("USER:"))&&!p.toUpperCase(Locale.ROOT).contains("0000 BOS");
    }
    private void showMissingPhotos415(){
        page="MISSING_PHOTOS_415";base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){A x=a(c);if(hasActualPhoto415(x))continue;addPhotoRow415(b,x);n++;}c.close();if(n==0)b.addView(tv("FOTOĞRAFI OLMAYAN AKTİF SPORCU BULUNMUYOR.",14,GREEN,true));
    }
    private void addPhotoRow415(LinearLayout b,A x){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,x.photo);r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+" • FOTOĞRAF EKLE",12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));r.setOnClickListener(v->{missingPhotoTarget414=x.id;pickPhoto414();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);
    }

    private void showMissingForms415(){
        page="MISSING_FORMS_415";base("KAYIT FORMU OLMAYAN AKTİF SPORCULAR",true);TextView info=tv("SPORCUYA DOKUNUN; GALERİDEN KAYIT FORMU GÖRSELİNİ SEÇİN.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){A x=a(c);if(formMap413().containsKey(x.id))continue;addFormRow415(b,x);n++;}c.close();if(n==0)b.addView(tv("KAYIT FORMU OLMAYAN AKTİF SPORCU BULUNMUYOR.",14,GREEN,true));
    }
    private void addFormRow415(LinearLayout b,A x){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,x.photo);r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+" • KAYIT FORMU EKLE",12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));r.setOnClickListener(v->{missingFormTarget415=x.id;pickForm415();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);
    }
    private void pickForm415(){Intent i;if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}else{i=new Intent(Intent.ACTION_PICK);i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");}startActivityForResult(i,REQ_MISSING_FORM_415);}

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_MISSING_FORM_415){long id=missingFormTarget415;missingFormTarget415=-1;if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0)uploadMissingForm415(id,data.getData());else showMissingForms415();return;}super.onActivityResult(requestCode,resultCode,data);
    }
    private void uploadMissingForm415(long id,Uri uri){upload415.execute(()->{try{String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");if(token.isEmpty())throw new IOException("OTURUM YOK");ensureAthleteCloud413(id,token);byte[] jpg=jpeg414(uri,2400,82);String path="user/"+id+"/form_"+System.currentTimeMillis()+".jpg";int code=put414("registration-forms",path,jpg,token);if(code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");code=put414("registration-forms",path,jpg,token);}if(code<200||code>=300)throw new IOException("STORAGE "+code);HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/set_athlete_media_path",new JSONObject().put("p_legacy_id",id).put("p_kind","form").put("p_path",path).toString(),token);if(r.code<200||r.code>=300||!r.body.contains("true"))throw new IOException("PATH "+r.code);formMap413().put(id,path);runOnUiThread(()->{toast("KAYIT FORMU BULUTA KAYDEDİLDİ.");showMissingForms415();});}catch(Exception e){runOnUiThread(()->{toast("KAYIT FORMU YÜKLENEMEDİ: "+(e.getMessage()==null?"BAĞLANTI":e.getMessage()));showMissingForms415();});}});}

    @Override void goBack(){if("MISSING_PHOTOS_415".equals(page)||"MISSING_FORMS_415".equals(page)){showHome();return;}super.goBack();}
    @Override protected void onDestroy(){upload415.shutdownNow();super.onDestroy();}
}
