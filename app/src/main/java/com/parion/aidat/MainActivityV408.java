package com.parion.aidat;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

public class MainActivityV408 extends MainActivityV407 {
    private static final int REQ_FORM_UPDATE_408=4081;
    private static final String SB_URL_408="https://ujjtsemybslznmzadzvk.supabase.co";
    private static final String SB_KEY_408="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqanRzZW15YnNsem5temFkenZrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY3MzIyMjIsImV4cCI6MjEwMjMwODIyMn0.qZPcYZwAjMJpc2yBB1bdTjA8YguFqr3UY85VuQGQRLE";

    private int listScrollY408=0, profileScrollY408=0;
    private boolean restoreList408=false, restoreProfile408=false;
    private long editingAthlete408=-1, formUpdateTarget408=-1;

    private ScrollView firstScroll408(View v){
        if(v instanceof ScrollView)return (ScrollView)v;
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){ScrollView s=firstScroll408(g.getChildAt(i));if(s!=null)return s;}}
        return null;
    }

    @Override void showProfile(long id){
        String from=page;
        if("LIST".equals(from)){
            ScrollView s=firstScroll408(root);if(s!=null)listScrollY408=s.getScrollY();
        }
        super.showProfile(id);
        ensureRegistrationFormUpdate408(id);
        if(restoreProfile408){
            restoreProfile408=false;
            getWindow().getDecorView().postDelayed(()->{ScrollView s=firstScroll408(root);if(s!=null)s.scrollTo(0,profileScrollY408);},100);
        }
    }

    @Override void showAthletes(){
        super.showAthletes();
        if(restoreList408){
            restoreList408=false;
            getWindow().getDecorView().postDelayed(()->{ScrollView s=firstScroll408(root);if(s!=null)s.scrollTo(0,listScrollY408);},240);
        }
    }

    @Override void form(long id){
        if(id>0&&"PROFILE".equals(page)){
            ScrollView s=firstScroll408(root);if(s!=null)profileScrollY408=s.getScrollY();
        }
        editingAthlete408=id;
        super.form(id);
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Long,String> formMap408(){
        try{Field f=MainActivityV405.class.getDeclaredField("formPath");f.setAccessible(true);return (ConcurrentHashMap<Long,String>)f.get(this);}catch(Exception e){return new ConcurrentHashMap<>();}
    }

    private void ensureRegistrationFormUpdate408(long id){
        if(!formMap408().containsKey(id))return;
        ArrayList<Button> buttons=new ArrayList<>();collectFormButtons408(root,buttons);
        Button view=null,update=null;
        for(Button b:buttons){String t=String.valueOf(b.getText()).toUpperCase(new Locale("tr","TR"));if(t.contains("GÜNCELLE")||t.contains("DEĞİŞTİR"))update=b;else if(t.contains("GÖRÜNTÜLE"))view=b;}
        if(view==null&&!buttons.isEmpty())view=buttons.get(0);
        if(update!=null){update.setText("KAYIT FORMUNU GÜNCELLE");update.setOnClickListener(v->pickFormUpdate408(id));return;}
        if(view==null)return;
        ViewParent vp=view.getParent();if(!(vp instanceof ViewGroup))return;ViewGroup parent=(ViewGroup)vp;
        Button b=btn("KAYIT FORMUNU GÜNCELLE");b.setOnClickListener(v->pickFormUpdate408(id));
        int pos=parent.indexOfChild(view);ViewGroup.LayoutParams src=view.getLayoutParams();
        ViewGroup.LayoutParams lp;
        if(src instanceof LinearLayout.LayoutParams){LinearLayout.LayoutParams x=new LinearLayout.LayoutParams(src.width,src.height);x.setMargins(0,dp(5),0,0);lp=x;}else lp=new ViewGroup.LayoutParams(-1,dp(54));
        parent.addView(b,Math.min(pos+1,parent.getChildCount()),lp);
    }
    private void collectFormButtons408(View v,ArrayList<Button> out){
        if(v instanceof Button&&String.valueOf(((Button)v).getText()).toUpperCase(new Locale("tr","TR")).contains("KAYIT FORM"))out.add((Button)v);
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectFormButtons408(g.getChildAt(i),out);}
    }
    private void pickFormUpdate408(long id){
        formUpdateTarget408=id;
        Intent i;
        if(Build.VERSION.SDK_INT>=33){i=new Intent(MediaStore.ACTION_PICK_IMAGES);i.setType("image/*");}
        else{i=new Intent(Intent.ACTION_PICK);i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");}
        startActivityForResult(i,REQ_FORM_UPDATE_408);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQ_FORM_UPDATE_408){
            long id=formUpdateTarget408;formUpdateTarget408=-1;
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&id>0){
                try{Method m=MainActivityV407.class.getDeclaredMethod("uploadForm407",long.class,Uri.class);m.setAccessible(true);m.invoke(this,id,data.getData());}
                catch(Exception e){toast("Kayıt formu güncellenemedi.");showProfile(id);}
            }else if(id>0)showProfile(id);
            return;
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    @Override void base(String title,boolean back){
        super.base(title,back);
        if("SİLİNEN SPORCULAR".equals(title))getWindow().getDecorView().postDelayed(this::patchDeletedRestoreButtons408,80);
    }

    private void patchDeletedRestoreButtons408(){
        ArrayList<Button> buttons=new ArrayList<>();collectRestoreButtons408(root,buttons);if(buttons.isEmpty())return;
        ArrayList<Long> ids=new ArrayList<>();ArrayList<String> names=new ArrayList<>();
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM athletes WHERE TRIM(COALESCE(deletedAt,''))<>'' ORDER BY deletedAt DESC",null);
        while(c.moveToNext()){ids.add(c.getLong(0));names.add(c.getString(1));}c.close();
        int n=Math.min(buttons.size(),ids.size());
        for(int i=0;i<n;i++){final long id=ids.get(i);final String name=names.get(i);buttons.get(i).setOnClickListener(v->new AlertDialog.Builder(this).setTitle("SPORCUYU GERİ YÜKLE").setMessage(name+" aktif sporculara geri alınsın mı?").setPositiveButton("GERİ YÜKLE",(d,w)->restoreAthlete408(id)).setNegativeButton("VAZGEÇ",null).show());}
    }
    private void collectRestoreButtons408(View v,ArrayList<Button> out){
        if(v instanceof Button&&"GERİ YÜKLE".equalsIgnoreCase(String.valueOf(((Button)v).getText()).trim()))out.add((Button)v);
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)collectRestoreButtons408(g.getChildAt(i),out);}
    }
    private void restoreAthlete408(long id){
        new Thread(()->{
            boolean cloud=restoreCloud408(id);
            if(cloud){
                ContentValues cv=new ContentValues();cv.put("deletedAt","");cv.put("deletedPrevStatus","");cv.put("status","AKTİF");db.getWritableDatabase().update("athletes",cv,"id=?",new String[]{String.valueOf(id)});
            }
            runOnUiThread(()->{
                if(!cloud){toast("Sporcu geri yüklenemedi. İnternet bağlantısını kontrol edin.");return;}
                toast("Sporcu aktif sporculara geri yüklendi.");
                try{Method m=MainActivityV390.class.getDeclaredMethod("showDeletedAthletes");m.setAccessible(true);m.invoke(this);}catch(Exception e){showHome();}
            });
        },"restore-athlete-408").start();
    }
    private boolean restoreCloud408(long id){
        try{
            URL u=new URL(SB_URL_408+"/rest/v1/rpc/restore_athlete_from_trash");HttpURLConnection h=(HttpURLConnection)u.openConnection();
            h.setRequestMethod("POST");h.setDoOutput(true);h.setConnectTimeout(12000);h.setReadTimeout(18000);h.setRequestProperty("apikey",SB_KEY_408);h.setRequestProperty("Authorization","Bearer "+SB_KEY_408);h.setRequestProperty("Content-Type","application/json");
            byte[] body=new JSONObject().put("p_legacy_id",id).toString().getBytes(StandardCharsets.UTF_8);try(OutputStream out=h.getOutputStream()){out.write(body);}int code=h.getResponseCode();InputStream in=code/100==2?h.getInputStream():h.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[1024];int n;while(in!=null&&(n=in.read(b))>0)out.write(b,0,n);if(in!=null)in.close();h.disconnect();return code/100==2&&out.toString("UTF-8").contains("true");
        }catch(Exception e){return false;}
    }

    @Override void goBack(){
        if("FORM".equals(page)&&editingAthlete408>0){long id=editingAthlete408;editingAthlete408=-1;restoreProfile408=true;showProfile(id);return;}
        if("PROFILE".equals(page)&&currentAthlete>0){restoreList408=true;showAthletes();return;}
        super.goBack();
    }

    @Override public void onBackPressed(){goBack();}
}
