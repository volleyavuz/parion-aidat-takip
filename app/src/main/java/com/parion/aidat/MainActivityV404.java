package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.JSONObject;

public class MainActivityV404 extends MainActivityV403 {
    private static final String SB_URL="https://ujjtsemybslznmzadzvk.supabase.co";
    private static final String SB_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqanRzZW15YnNsem5temFkenZrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY3MzIyMjIsImV4cCI6MjEwMjMwODIyMn0.qZPcYZwAjMJpc2yBB1bdTjA8YguFqr3UY85VuQGQRLE";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().getDecorView().postDelayed(this::beginRegisteredMediaMigration404,3500);
    }

    private void beginRegisteredMediaMigration404(){
        final android.content.SharedPreferences sp=getSharedPreferences("cloud_media_404",MODE_PRIVATE);
        if(sp.getBoolean("done",false))return;
        Toast.makeText(this,"KAYITLI SPORCU MEDYALARI BULUTA AKTARILIYOR",Toast.LENGTH_LONG).show();
        new Thread(()->{
            int photos=0,forms=0,errors=0;
            try{photos=uploadReferencedPhotos404();}catch(Exception e){errors++;}
            try{forms=uploadReferencedForms404();}catch(Exception e){errors++;}
            final int p=photos,f=forms,er=errors;
            if(er==0)sp.edit().putBoolean("done",true).putInt("photos",p).putInt("forms",f).apply();
            runOnUiThread(()->Toast.makeText(this,er==0?("BULUT AKTARIMI TAMAMLANDI • "+p+" FOTOĞRAF • "+f+" FORM"):("BULUT AKTARIMI TAMAMLANAMADI • HATA: "+er),Toast.LENGTH_LONG).show());
        },"parion-cloud-media-registered-only").start();
    }

    private int uploadReferencedPhotos404() throws Exception{
        int ok=0; Set<String> done=new HashSet<>();
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,photo FROM athletes",null);
        while(c.moveToNext()){
            long id=c.getLong(0); String ref=c.getString(1); if(ref==null)continue; ref=ref.trim();
            if(ref.isEmpty()||"NONE".equalsIgnoreCase(ref)||ref.toUpperCase(new Locale("tr","TR")).contains("0000 BOS"))continue;
            String remote="athletes/"+id+"/profile.jpg";
            try{
                InputStream in=null;
                if(ref.startsWith("USER:")){
                    File f=new File(new File(getFilesDir(),"athlete_photos"),ref.substring(5)); if(f.isFile())in=new FileInputStream(f);
                }else{
                    try{in=getAssets().open("photos/"+ref);}catch(Exception ignored){}
                }
                if(in==null)continue;
                try(InputStream x=in){if(upload404("athlete-photos",remote,"image/jpeg",x)){ok++;patchAthlete404(id,"photo","REMOTE:"+remote);}}
            }catch(Exception ignored){}
        }
        c.close(); return ok;
    }

    private int uploadReferencedForms404() throws Exception{
        int ok=0;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,birthYear,name FROM athletes",null);
        while(c.moveToNext()){
            long id=c.getLong(0); int year=c.getInt(1); String name=c.getString(2);
            FormRef fr=findFormForAthlete404(id,year,name); if(fr==null)continue;
            String remote="athletes/"+id+"/registration.jpg";
            try(InputStream in=fr.open(this)){
                if(upload404("registration-forms",remote,"image/jpeg",in)){
                    ok++; patchAthlete404(id,"registration_form_path",remote);
                }
            }catch(Exception ignored){}
        }
        c.close(); return ok;
    }

    private FormRef findFormForAthlete404(long id,int year,String name){
        try{
            Cursor c=db.getReadableDatabase().rawQuery("SELECT fileRef FROM registration_forms WHERE athleteId=?",new String[]{String.valueOf(id)});
            if(c.moveToFirst()){
                String r=c.getString(0); c.close();
                if(r!=null&&r.startsWith("USER:")){
                    File f=new File(new File(getFilesDir(),"registration_forms"),r.substring(5)); if(f.isFile())return new FormRef(f);
                }
            }else c.close();
        }catch(Exception ignored){}
        String wanted=norm404(name);
        try(InputStream in=getAssets().open("forms/index.tsv");BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){
            String line;while((line=br.readLine())!=null){String[] z=line.split("\\t",5);if(z.length<3)continue;int y;try{y=Integer.parseInt(z[0]);}catch(Exception e){continue;}if(y==year&&norm404(z[1]).equals(wanted))return new FormRef("forms/bundled/"+z[2]);}
        }catch(Exception ignored){}
        return null;
    }

    private String norm404(String s){if(s==null)return "";String x=s.toUpperCase(new Locale("tr","TR")).replace('İ','I').replace('ı','I');x=java.text.Normalizer.normalize(x,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+","");return x.replaceAll("[^A-Z0-9]","");}

    private boolean upload404(String bucket,String path,String mime,InputStream in)throws Exception{
        URL u=new URL(SB_URL+"/storage/v1/object/"+bucket+"/"+encode404(path)); HttpURLConnection h=(HttpURLConnection)u.openConnection();
        h.setConnectTimeout(20000);h.setReadTimeout(60000);h.setRequestMethod("POST");h.setDoOutput(true);h.setRequestProperty("apikey",SB_KEY);h.setRequestProperty("Authorization","Bearer "+SB_KEY);h.setRequestProperty("Content-Type",mime);h.setRequestProperty("x-upsert","true");
        try(OutputStream out=h.getOutputStream()){byte[] b=new byte[32768];int n;while((n=in.read(b))>0)out.write(b,0,n);}int code=h.getResponseCode();h.disconnect();return code>=200&&code<300;
    }
    private String encode404(String path)throws Exception{StringBuilder b=new StringBuilder();String[] p=path.split("/");for(int i=0;i<p.length;i++){if(i>0)b.append('/');b.append(URLEncoder.encode(p[i],"UTF-8").replace("+","%20"));}return b.toString();}

    private void patchAthlete404(long legacyId,String field,String value){
        try{URL u=new URL(SB_URL+"/rest/v1/athletes?legacy_id=eq."+legacyId);HttpURLConnection h=(HttpURLConnection)u.openConnection();h.setConnectTimeout(12000);h.setReadTimeout(12000);h.setRequestMethod("PATCH");h.setDoOutput(true);h.setRequestProperty("apikey",SB_KEY);h.setRequestProperty("Authorization","Bearer "+SB_KEY);h.setRequestProperty("Content-Type","application/json");h.setRequestProperty("Prefer","return=minimal");byte[] body=new JSONObject().put(field,value).toString().getBytes(StandardCharsets.UTF_8);try(OutputStream out=h.getOutputStream()){out.write(body);}h.getResponseCode();h.disconnect();}catch(Exception ignored){}
    }

    static class FormRef{
        final File file;final String asset;FormRef(File f){file=f;asset=null;}FormRef(String a){file=null;asset=a;}
        InputStream open(MainActivityV404 a)throws Exception{return file!=null?new FileInputStream(file):a.getAssets().open(asset);}
    }
}
