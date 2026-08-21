package com.parion.aidat;

import android.content.*;
import android.database.Cursor;
import java.io.*;
import java.text.Normalizer;
import java.util.*;

public class MainActivityV400 extends MainActivityV399 {
    @Override public void onCreate(android.os.Bundle b){super.onCreate(b);importBundledForms400();}

    private void importBundledForms400(){
        android.content.SharedPreferences p=getSharedPreferences("registration_form_bundle",MODE_PRIVATE);
        if(p.getBoolean("v1_done",false))return;
        int matched=0;
        try(InputStream in=getAssets().open("forms/index.tsv");BufferedReader br=new BufferedReader(new InputStreamReader(in,"UTF-8"))){
            String line;
            while((line=br.readLine())!=null){
                String[] z=line.split("\\t",5);if(z.length<5)continue;
                int year;try{year=Integer.parseInt(z[0]);}catch(Exception e){continue;}
                String targetName=z[1],assetName=z[2],mime=z[3],original=z[4];
                long athlete=findAthlete400(year,targetName);if(athlete<=0)continue;
                Cursor c=db.getReadableDatabase().rawQuery("SELECT fileRef FROM registration_forms WHERE athleteId=?",new String[]{String.valueOf(athlete)});
                boolean already=c.moveToFirst()&&c.getString(0)!=null&&!c.getString(0).trim().isEmpty();c.close();
                if(already)continue;
                db.getWritableDatabase().execSQL("INSERT OR REPLACE INTO registration_forms(athleteId,fileRef,mimeType,originalName,updatedAt) VALUES(?,?,?,?,datetime('now'))",new Object[]{athlete,"ASSET:bundled/"+assetName,mime,original});
                matched++;
            }
            p.edit().putBoolean("v1_done",true).putInt("v1_matched",matched).apply();
        }catch(Exception ignored){}
    }

    private long findAthlete400(int year,String wanted){
        String nw=norm400(wanted);long best=-1;int bestDist=99;
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM athletes WHERE birthYear=?",new String[]{String.valueOf(year)});
        while(c.moveToNext()){
            long id=c.getLong(0);String name=c.getString(1);String nn=norm400(name);
            if(nn.equals(nw)){c.close();return id;}
            int d=edit400(nn,nw);if(d<bestDist){bestDist=d;best=id;}
        }c.close();
        // Only accept a very small spelling difference inside the same birth year.
        return bestDist<=2?best:-1;
    }
    private String norm400(String s){
        if(s==null)return "";String x=s.toUpperCase(new Locale("tr","TR")).replace('İ','I').replace('ı','I');
        x=Normalizer.normalize(x,Normalizer.Form.NFD).replaceAll("\\p{M}+","");return x.replaceAll("[^A-Z0-9]","");
    }
    private int edit400(String a,String b){int[] prev=new int[b.length()+1],cur=new int[b.length()+1];for(int j=0;j<=b.length();j++)prev[j]=j;for(int i=1;i<=a.length();i++){cur[0]=i;for(int j=1;j<=b.length();j++)cur[j]=Math.min(Math.min(cur[j-1]+1,prev[j]+1),prev[j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));int[] t=prev;prev=cur;cur=t;}return prev[b.length()];}
}
