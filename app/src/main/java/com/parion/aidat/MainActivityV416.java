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
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

public class MainActivityV416 extends MainActivityV415 {
    private static final int REQ_FORM_PROFILE_416=4161, REQ_FORM_MISSING_416=4162;
    private final ExecutorService form416=Executors.newSingleThreadExecutor();
    private long target416=-1;

    @Override void showProfile(long id){
        super.showProfile(id);
        patchProfileForm416(root,id);
        repairExistingForm416(id);
    }

    private void patchProfileForm416(View v,long id){
        if(v instanceof Button){
            Button b=(Button)v;String s=String.valueOf(b.getText()).toUpperCase(new Locale("tr","TR"));
            if(s.contains("KAYIT FORMU EKLE")||s.contains("KAYIT FORMUNU GÜNCELLE")||s.contains("KAYIT FORMUNU DEĞİŞTİR")){
                b.setOnClickListener(x->{target416=id;pick416(REQ_FORM_PROFILE_416);});
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchProfileForm416(g.getChildAt(i),id);}
    }

    @Override void showHome(){
        super.showHome();
        patchMissingCard416(root);
    }

    private void patchMissingCard416(View v){
        if(v instanceof TextView){
            String s=String.valueOf(((TextView)v).getText()).toUpperCase(new Locale("tr","TR"));
            if(s.contains("KAYIT FORMU OLMAYAN AKTİF SPORCULAR")){
                View p=(View)v.getParent();p.setOnClickListener(x->showMissing416());return;
            }
        }
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)patchMissingCard416(g.getChildAt(i));}
    }

    private void showMissing416(){
        page="MISSING_FORMS_416";base("KAYIT FORMU OLMAYAN AKTİF SPORCULAR",true);
        TextView info=tv("SPORCUYA DOKUNUN; GALERİDEN KAYIT FORMU GÖRSELİNİ SEÇİN.",12,Color.DKGRAY,false);info.setGravity(Gravity.CENTER);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){A x=a(c);if(formMap413().containsKey(x.id))continue;addMissingRow416(b,x);n++;}c.close();
        if(n==0)b.addView(tv("KAYIT FORMU OLMAYAN AKTİF SPORCU BULUNMUYOR.",14,GREEN,true));
    }

    private void addMissingRow416(LinearLayout b,A x){
        LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(8),dp(8),dp(8),dp(8));r.setBackground(round(Color.WHITE,10));
        ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,x.photo);r.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));
        LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv((x.by>0?x.by+" • ":"")+x.name,15,BLACK,true));t.addView(tv(x.cat+" • KAYIT FORMU EKLE",12,Color.DKGRAY,true));r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        r.setOnClickListener(v->{target416=x.id;pick416(REQ_FORM_MISSING_416);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(r,lp);
    }

    private void pick416(int req){Intent i;if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}else{i=new Intent(Intent.ACTION_PICK);i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");}startActivityForResult(i,req);}

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_FORM_PROFILE_416||requestCode==REQ_FORM_MISSING_416){
            long id=target416;target416=-1;boolean missing=requestCode==REQ_FORM_MISSING_416;
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0)upload416(id,data.getData(),missing);
            else if(missing)showMissing416(); else if(id>0)showProfile(id);
            return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    private void upload416(long id,Uri uri,boolean missing){
        form416.execute(()->{
            try{
                String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");if(token.isEmpty())throw new IOException("OTURUM YOK");
                ensureAthleteCloud413(id,token);
                byte[] jpg=jpeg414(uri,2400,82);String path="user/"+id+"/form_"+System.currentTimeMillis()+".jpg";
                int code=put414("registration-forms",path,jpg,token);if(code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");code=put414("registration-forms",path,jpg,token);}if(code<200||code>=300)throw new IOException("STORAGE "+code);
                JSONObject b=new JSONObject().put("p_legacy_id",id);
                HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_attach_latest_registration_form",b.toString(),token);
                if(r.code==401&&refreshSession()){token=cloudPrefs.getString("access_token","");r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_attach_latest_registration_form",b.toString(),token);}
                if(r.code<200||r.code>=300||r.body==null||r.body.trim().equals("\"\"")||r.body.trim().equals("null"))throw new IOException("FORM BAĞLAMA "+r.code);
                HttpResult verify=request("GET",SUPABASE_URL+"/rest/v1/athlete_media_index?legacy_id=eq."+id+"&select=registration_form_path",null,token);
                if(verify.code<200||verify.code>=300)throw new IOException("DOĞRULAMA "+verify.code);
                JSONArray a=new JSONArray(verify.body);if(a.length()==0)throw new IOException("FORM YOLU BULUNAMADI");String actual=a.getJSONObject(0).optString("registration_form_path","");if(actual.isEmpty())throw new IOException("FORM YOLU BOŞ");
                formMap413().put(id,actual);
                runOnUiThread(()->{toast("KAYIT FORMU BULUTA KAYDEDİLDİ.");if(missing)showMissing416();else showProfile(id);});
            }catch(Exception e){String m=e.getMessage();runOnUiThread(()->{toast("KAYIT FORMU YÜKLENEMEDİ: "+(m==null?"BAĞLANTI":m));if(missing)showMissing416();else showProfile(id);});}
        });
    }

    private void repairExistingForm416(long id){
        if(id<=0||formMap413().containsKey(id))return;
        form416.execute(()->{
            try{
                String token=cloudPrefs==null?"":cloudPrefs.getString("access_token","");if(token.isEmpty())return;
                HttpResult r=request("POST",SUPABASE_URL+"/rest/v1/rpc/parion_attach_latest_registration_form",new JSONObject().put("p_legacy_id",id).toString(),token);
                if(r.code<200||r.code>=300||r.body==null||r.body.trim().equals("\"\"")||r.body.trim().equals("null"))return;
                String p=new JSONTokener(r.body).nextValue().toString();if(!p.isEmpty())formMap413().put(id,p);
            }catch(Exception ignored){}
        });
    }

    @Override void goBack(){if("MISSING_FORMS_416".equals(page)){showHome();return;}super.goBack();}
    @Override protected void onDestroy(){form416.shutdownNow();super.onDestroy();}
}
