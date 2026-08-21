package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.*;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.*;

public class MainActivityV401 extends MainActivityV399 {
    private final ExecutorService formImportExecutor=Executors.newSingleThreadExecutor();

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        // The application must become interactive first. Importing hundreds of form mappings
        // synchronously during onCreate caused long startup stalls on phones.
        if(root!=null) root.postDelayed(this::startBundledFormImport401,900);
        else startBundledFormImport401();
    }

    private void startBundledFormImport401(){
        SharedPreferences p=getSharedPreferences("registration_form_bundle",MODE_PRIVATE);
        if(p.getBoolean("v2_done",false))return;
        formImportExecutor.execute(()->importBundledForms401(p));
    }

    private void importBundledForms401(SharedPreferences prefs){
        int matched=0;SQLiteDatabase w=null;boolean ok=false;
        try{
            w=db.getWritableDatabase();
            w.beginTransaction();
            try(InputStream in=getAssets().open("forms/index.tsv");BufferedReader br=new BufferedReader(new InputStreamReader(in,"UTF-8"))){
                String line;
                while((line=br.readLine())!=null){
                    String[] z=line.split("\\t",5);if(z.length<5)continue;
                    int year;try{year=Integer.parseInt(z[0]);}catch(Exception e){continue;}
                    String targetName=z[1],assetName=z[2],mime=z[3],original=z[4];
                    long athlete=findAthlete401(year,targetName);if(athlete<=0)continue;
                    Cursor c=w.rawQuery("SELECT fileRef FROM registration_forms WHERE athleteId=?",new String[]{String.valueOf(athlete)});
                    boolean already=c.moveToFirst()&&c.getString(0)!=null&&!c.getString(0).trim().isEmpty();c.close();
                    if(already)continue;
                    w.execSQL("INSERT OR REPLACE INTO registration_forms(athleteId,fileRef,mimeType,originalName,updatedAt) VALUES(?,?,?,?,datetime('now'))",new Object[]{athlete,"ASSET:bundled/"+assetName,mime,original});
                    matched++;
                }
            }
            w.setTransactionSuccessful();ok=true;
        }catch(Exception e){
            android.util.Log.e("ParionForms","Bundled form import failed",e);
        }finally{
            if(w!=null&&w.inTransaction())try{w.endTransaction();}catch(Exception ignored){}
        }
        if(ok){
            prefs.edit().putBoolean("v2_done",true).putInt("v2_matched",matched).apply();
            final int imported=matched;
            runOnUiThread(()->{if("HOME".equals(page))showHome(); if(imported>0)toast(imported+" kayıt formu eşleştirildi.");});
        }
    }

    private long findAthlete401(int year,String wanted){
        String nw=norm401(wanted);long best=-1;int bestDist=99;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM athletes WHERE birthYear=?",new String[]{String.valueOf(year)});
        while(c.moveToNext()){
            long id=c.getLong(0);String nn=norm401(c.getString(1));
            if(nn.equals(nw)){c.close();return id;}
            int d=edit401(nn,nw);if(d<bestDist){bestDist=d;best=id;}
        }
        c.close();return bestDist<=2?best:-1;
    }
    private String norm401(String s){
        if(s==null)return "";String x=s.toUpperCase(new Locale("tr","TR")).replace('İ','I').replace('ı','I');
        x=Normalizer.normalize(x,Normalizer.Form.NFD).replaceAll("\\p{M}+","");return x.replaceAll("[^A-Z0-9]","");
    }
    private int edit401(String a,String b){
        int[] prev=new int[b.length()+1],cur=new int[b.length()+1];for(int j=0;j<=b.length();j++)prev[j]=j;
        for(int i=1;i<=a.length();i++){cur[0]=i;for(int j=1;j<=b.length();j++)cur[j]=Math.min(Math.min(cur[j-1]+1,prev[j]+1),prev[j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));int[] t=prev;prev=cur;cur=t;}
        return prev[b.length()];
    }

    @Override protected void onDestroy(){formImportExecutor.shutdownNow();super.onDestroy();}
}
