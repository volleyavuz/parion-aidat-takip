package com.parion.aidat;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.JSONObject;

public class MainActivityV402 extends MainActivityV399 {
    private static final String SB_URL="https://ujjtsemybslznmzadzvk.supabase.co";
    private static final String SB_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqanRzZW15YnNsem5temFkenZrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY3MzIyMjIsImV4cCI6MjEwMjMwODIyMn0.qZPcYZwAjMJpc2yBB1bdTjA8YguFqr3UY85VuQGQRLE";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().getDecorView().postDelayed(this::startMediaMigration402,1200);
    }

    private void startMediaMigration402(){
        final android.content.SharedPreferences p=getSharedPreferences("media_migration_402",MODE_PRIVATE);
        if(p.getBoolean("done",false))return;
        Toast.makeText(this,"ONLINE MEDYA AKTARIMI ARKA PLANDA BAŞLADI",Toast.LENGTH_LONG).show();
        new Thread(()->{
            int photos=0,forms=0,errors=0;
            try{
                photos=migratePhotoAssets402();
            }catch(Exception e){errors++;}
            try{
                forms=migrateBundledForms402();
            }catch(Exception e){errors++;}
            if(errors==0){
                p.edit().putBoolean("done",true).putInt("photos",photos).putInt("forms",forms).apply();
            }
            final int fp=photos,ff=forms,fe=errors;
            runOnUiThread(()->Toast.makeText(this,fe==0?("ONLINE AKTARIM TAMAMLANDI • "+fp+" FOTOĞRAF • "+ff+" FORM"):("AKTARIM TAMAMLANAMADI • HATA: "+fe),Toast.LENGTH_LONG).show());
        },"parion-media-migration").start();
    }

    private int migratePhotoAssets402() throws Exception{
        String[] names=getAssets().list("photos"); if(names==null)return 0; int ok=0;
        for(String n:names){
            if(n==null||n.trim().isEmpty())continue;
            String low=n.toLowerCase(Locale.ROOT);
            if(!(low.endsWith(".jpg")||low.endsWith(".jpeg")||low.endsWith(".png")||low.endsWith(".webp")))continue;
            String mime=low.endsWith(".png")?"image/png":low.endsWith(".webp")?"image/webp":"image/jpeg";
            try(InputStream in=getAssets().open("photos/"+n)){
                if(upload402("athlete-photos","assets/"+n,mime,in))ok++;
            }
        }
        // Also migrate photos added later from the phone.
        File dir=new File(getFilesDir(),"athlete_photos"); File[] user=dir.listFiles();
        if(user!=null)for(File f:user)if(f.isFile()){
            try(InputStream in=new FileInputStream(f)){if(upload402("athlete-photos","user/"+f.getName(),"image/jpeg",in))ok++;}catch(Exception ignored){}
        }
        return ok;
    }

    private int migrateBundledForms402() throws Exception{
        int ok=0;
        try(InputStream idx=getAssets().open("forms/index.tsv");BufferedReader br=new BufferedReader(new InputStreamReader(idx,StandardCharsets.UTF_8))){
            String line;
            while((line=br.readLine())!=null){
                String[] z=line.split("\\t",5); if(z.length<5)continue;
                int year; try{year=Integer.parseInt(z[0]);}catch(Exception e){continue;}
                String wanted=z[1],asset=z[2],mime=z[3]; long localId=findAthlete402(year,wanted); if(localId<=0)continue;
                String remote="legacy/"+localId+"/"+asset;
                try(InputStream in=getAssets().open("forms/bundled/"+asset)){
                    if(upload402("registration-forms",remote,mime,in)){
                        ok++;
                        db.getWritableDatabase().execSQL("INSERT OR REPLACE INTO registration_forms(athleteId,fileRef,mimeType,originalName,updatedAt) VALUES(?,?,?,?,datetime('now'))",new Object[]{localId,"REMOTE:"+remote,mime,z[4]});
                        patchAthleteForm402(localId,remote);
                    }
                }
            }
        }
        // User-added forms already stored in app files.
        Cursor c=db.getReadableDatabase().rawQuery("SELECT athleteId,fileRef,mimeType FROM registration_forms WHERE fileRef LIKE 'USER:%'",null);
        while(c.moveToNext()){
            long id=c.getLong(0);String ref=c.getString(1),mime=c.getString(2);File f=new File(new File(getFilesDir(),"registration_forms"),ref.substring(5));
            if(!f.isFile())continue;String remote="legacy/"+id+"/user_"+f.getName();
            try(InputStream in=new FileInputStream(f)){if(upload402("registration-forms",remote,mime,in)){ok++;patchAthleteForm402(id,remote);}}
        }c.close();
        return ok;
    }

    private long findAthlete402(int year,String wanted){
        String nw=norm402(wanted);Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM athletes WHERE birthYear=?",new String[]{String.valueOf(year)});long best=-1;int bestD=99;
        while(c.moveToNext()){long id=c.getLong(0);String nn=norm402(c.getString(1));if(nn.equals(nw)){c.close();return id;}int d=edit402(nn,nw);if(d<bestD){bestD=d;best=id;}}
        c.close();return bestD<=2?best:-1;
    }
    private String norm402(String s){if(s==null)return "";String x=s.toUpperCase(new Locale("tr","TR")).replace('İ','I').replace('ı','I');x=java.text.Normalizer.normalize(x,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+","");return x.replaceAll("[^A-Z0-9]","");}
    private int edit402(String a,String b){int[] p=new int[b.length()+1],q=new int[b.length()+1];for(int j=0;j<=b.length();j++)p[j]=j;for(int i=1;i<=a.length();i++){q[0]=i;for(int j=1;j<=b.length();j++)q[j]=Math.min(Math.min(q[j-1]+1,p[j]+1),p[j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));int[] t=p;p=q;q=t;}return p[b.length()];}

    private boolean upload402(String bucket,String path,String mime,InputStream in) throws Exception{
        String enc=encodePath402(path);URL u=new URL(SB_URL+"/storage/v1/object/"+bucket+"/"+enc);HttpURLConnection h=(HttpURLConnection)u.openConnection();
        h.setConnectTimeout(20000);h.setReadTimeout(60000);h.setRequestMethod("POST");h.setDoOutput(true);h.setRequestProperty("apikey",SB_KEY);h.setRequestProperty("Authorization","Bearer "+SB_KEY);h.setRequestProperty("Content-Type",mime==null?"application/octet-stream":mime);h.setRequestProperty("x-upsert","true");
        try(OutputStream out=h.getOutputStream()){byte[] buf=new byte[32768];int n;while((n=in.read(buf))>0)out.write(buf,0,n);}int code=h.getResponseCode();h.disconnect();return code>=200&&code<300;
    }
    private String encodePath402(String path)throws Exception{StringBuilder b=new StringBuilder();String[] p=path.split("/");for(int i=0;i<p.length;i++){if(i>0)b.append('/');b.append(URLEncoder.encode(p[i],"UTF-8").replace("+","%20"));}return b.toString();}

    private void patchAthleteForm402(long legacyId,String remote){
        try{URL u=new URL(SB_URL+"/rest/v1/athletes?legacy_id=eq."+legacyId);HttpURLConnection h=(HttpURLConnection)u.openConnection();h.setConnectTimeout(12000);h.setReadTimeout(12000);h.setRequestMethod("PATCH");h.setDoOutput(true);h.setRequestProperty("apikey",SB_KEY);h.setRequestProperty("Authorization","Bearer "+SB_KEY);h.setRequestProperty("Content-Type","application/json");h.setRequestProperty("Prefer","return=minimal");byte[] body=new JSONObject().put("registration_form_path",remote).toString().getBytes(StandardCharsets.UTF_8);try(OutputStream out=h.getOutputStream()){out.write(body);}h.getResponseCode();h.disconnect();}catch(Exception ignored){}
    }
}
