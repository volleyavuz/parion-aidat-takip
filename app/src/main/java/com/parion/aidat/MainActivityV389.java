package com.parion.aidat;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class MainActivityV389 extends MainActivityV386 {

    @Override public void onCreate(android.os.Bundle b){
        super.onCreate(b);
        ensureTcknColumn();
        normalizeLocalPhones();
    }

    private void ensureTcknColumn(){
        try{
            Cursor c=db.getReadableDatabase().rawQuery("PRAGMA table_info(athletes)",null); boolean ok=false;
            while(c.moveToNext()) if("tckn".equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name")))) ok=true;
            c.close();
            if(!ok) db.getWritableDatabase().execSQL("ALTER TABLE athletes ADD COLUMN tckn TEXT");
        }catch(Exception ignored){}
    }

    private void normalizeLocalPhones(){
        try{
            SQLiteDatabase d=db.getWritableDatabase();
            d.execSQL("UPDATE athletes SET phone='0'||phone WHERE phone GLOB '5?????????' AND length(phone)=10");
            d.execSQL("UPDATE athletes SET motherPhone='0'||motherPhone WHERE motherPhone GLOB '5?????????' AND length(motherPhone)=10");
            d.execSQL("UPDATE athletes SET fatherPhone='0'||fatherPhone WHERE fatherPhone GLOB '5?????????' AND length(fatherPhone)=10");
        }catch(Exception ignored){}
    }

    @Override void showHome(){
        super.showHome();
        int n=countMissingActivePhotos();
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(12),dp(10),dp(12),dp(10)); card.setBackground(round(Color.WHITE,12));
        TextView count=tv(String.valueOf(n),26,BLACK,true); count.setGravity(Gravity.CENTER); card.addView(count,new LinearLayout.LayoutParams(-1,dp(42)));
        Button b=btn("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR"); b.setOnClickListener(v->showMissingActivePhotos()); card.addView(b,new LinearLayout.LayoutParams(-1,dp(54)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(dp(8),dp(8),dp(8),dp(8)); root.addView(card,lp);
    }

    private boolean missingPhoto(String p){
        if(p==null) return true;
        String raw=p.trim();
        String x=raw.toUpperCase(new Locale("tr","TR"));
        if(x.isEmpty() || "NONE".equals(x) || x.contains("0000 BOS")) return true;
        try{
            if(raw.startsWith("USER:")){
                File f=new File(new File(getFilesDir(),"athlete_photos"),raw.substring(5));
                return !f.isFile();
            }
            try(InputStream in=getAssets().open("photos/"+raw)){ return false; }
        }catch(Exception ignored){ return true; }
    }

    private int countMissingActivePhotos(){
        int n=0; Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){ A x=a(c); if(missingPhoto(x.photo)) n++; }
        c.close(); return n;
    }

    private void showMissingActivePhotos(){
        page="LIST"; base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);
        ScrollView sv=scroll(); LinearLayout b=box(sv); int n=0;
        Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){ A x=a(c); if(!missingPhoto(x.photo)) continue; row(b,x,null,0); n++; }
        c.close();
        if(n==0) b.addView(tv("Varsayılan fotoğrafı kullanan aktif sporcu bulunmuyor.",15,Color.DKGRAY,true));
    }

    @Override void form(long id){
        super.form(id);
        configureFormInputs(root);
    }

    private void configureFormInputs(View v){
        if(v instanceof EditText){ configureField((EditText)v); return; }
        if(v instanceof ViewGroup){ ViewGroup g=(ViewGroup)v; for(int i=0;i<g.getChildCount();i++) configureFormInputs(g.getChildAt(i)); }
    }

    private void configureField(EditText e){
        String h=e.getHint()==null?"":e.getHint().toString().toUpperCase(new Locale("tr","TR"));
        if(h.contains("TELEFON")){
            String x=normalizePhone(e.getText().toString()); if(x.isEmpty()) x="05"; e.setText(x); e.setSelection(e.length());
            e.setInputType(InputType.TYPE_CLASS_PHONE); e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)}); return;
        }
        if(h.contains("TARİH") || h.contains("BAŞLANGIÇ") || h.contains("BİTİŞ") || h.contains("YENİDEN BAŞLAMA")){
            e.setInputType(InputType.TYPE_NULL); e.setFocusable(false); e.setClickable(true); e.setOnClickListener(v->openDatePicker(e)); return;
        }
        if(h.contains("AİDAT") || h.contains("TUTAR") || h.contains("ADET")){
            e.setInputType(InputType.TYPE_CLASS_NUMBER); return;
        }
        if(h.contains("AD SOYAD") || h.contains("ANNE ADI") || h.contains("BABA ADI") || h.contains("GRUP") || h.contains("TAKIM")){
            e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS); return;
        }
    }

    private String normalizePhone(String raw){
        String x=raw==null?"":raw.replaceAll("[^0-9]","");
        if(x.startsWith("90") && x.length()==12) x="0"+x.substring(2);
        if(x.length()==10 && x.startsWith("5")) x="0"+x;
        if(x.length()>11) x=x.substring(0,11);
        return x;
    }

    private void openDatePicker(EditText e){
        Calendar c=Calendar.getInstance();
        String s=e.getText().toString().trim();
        try{ String[] p=s.split("\\."); if(p.length==3){c.set(Calendar.DAY_OF_MONTH,Integer.parseInt(p[0]));c.set(Calendar.MONTH,Integer.parseInt(p[1])-1);c.set(Calendar.YEAR,Integer.parseInt(p[2]));} }catch(Exception ignored){}
        new DatePickerDialog(this,(v,y,m,d)->e.setText(String.format(Locale.US,"%02d.%02d.%04d",d,m+1,y)),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
    }
}
