package com.parion.aidat;

import android.database.Cursor;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class MainActivityV392 extends MainActivityV391 {

    @Override void base(String title, boolean back){
        super.base(title,back);
        if(back) fixBackButton(root);
    }

    private void fixBackButton(View v){
        if(!(v instanceof ViewGroup)) return;
        ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View ch=g.getChildAt(i);
            if(ch instanceof TextView){
                TextView t=(TextView)ch;
                if("‹".contentEquals(t.getText())){
                    t.setText("←");
                    t.setTextSize(28);
                    t.setGravity(Gravity.CENTER);
                    t.setPadding(0,0,0,0);
                    ViewGroup.LayoutParams old=t.getLayoutParams();
                    if(old!=null){old.width=dp(56);old.height=dp(56);t.setLayoutParams(old);}
                    return;
                }
            }
            fixBackButton(ch);
        }
    }

    @Override void showHome(){
        super.showHome();
        refreshMissingPhotoCard(root);
    }

    private void refreshMissingPhotoCard(View v){
        if(!(v instanceof ViewGroup)) return;
        ViewGroup g=(ViewGroup)v;
        for(int i=0;i<g.getChildCount();i++){
            View ch=g.getChildAt(i);
            if(ch instanceof Button && String.valueOf(((Button)ch).getText()).contains("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR")){
                if(i>0 && g.getChildAt(i-1) instanceof TextView) ((TextView)g.getChildAt(i-1)).setText(String.valueOf(realMissingCount()));
                ch.setOnClickListener(x->showRealMissingPhotos());
                return;
            }
            refreshMissingPhotoCard(ch);
        }
    }

    private boolean usesDefaultPhoto(String photo){
        String p=photo==null?"":photo.trim();
        String up=p.toUpperCase(new Locale("tr","TR"));
        if(p.isEmpty() || "NONE".equalsIgnoreCase(p) || up.contains("0000 BOS")) return true;
        try{
            if(p.startsWith("USER:")){
                File f=new File(new File(getFilesDir(),"athlete_photos"),p.substring(5));
                return !f.isFile();
            }
            try(InputStream in=getAssets().open("photos/"+p)){ return false; }
        }catch(Exception e){ return true; }
    }

    private int realMissingCount(){
        int n=0;Cursor c=db.athletes("","AKTİF");
        while(c.moveToNext()){A x=a(c);if(usesDefaultPhoto(x.photo))n++;}c.close();return n;
    }

    private void showRealMissingPhotos(){
        page="LIST";base("FOTOĞRAFI OLMAYAN AKTİF SPORCULAR",true);ScrollView sv=scroll();LinearLayout b=box(sv);int n=0;
        Cursor c=db.athletes("","AKTİF");while(c.moveToNext()){A x=a(c);if(!usesDefaultPhoto(x.photo))continue;row(b,x,null,0);n++;}c.close();
        if(n==0)b.addView(tv("Varsayılan 0000 BOS görselini kullanan aktif sporcu bulunmuyor.",15,Color.DKGRAY,true));
    }

    @Override void showDeletedAthletes(){
        page="DELETED";base("SİLİNEN SPORCULAR",true);
        LinearLayout searchBox=new LinearLayout(this);searchBox.setPadding(dp(10),dp(8),dp(10),dp(4));
        EditText q=new EditText(this);q.setHint("Silinen sporcu adı ara");q.setSingleLine(true);searchBox.addView(q,new LinearLayout.LayoutParams(-1,dp(52)));root.addView(searchBox);
        TextView info=tv("Silinen kayıtlar 1 yıl boyunca burada tutulur. Süre dolunca sistem otomatik olarak kalıcı siler.",12,Color.DKGRAY,false);root.addView(info);
        ScrollView sv=scroll();LinearLayout b=box(sv);
        Runnable load=()->loadDeletedFiltered(b,q.getText().toString());
        q.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int x,int y){}public void onTextChanged(CharSequence s,int a,int x,int y){load.run();}public void afterTextChanged(android.text.Editable e){}});
        load.run();syncDeletedFromCloud(true);
    }

    private void loadDeletedFiltered(LinearLayout b,String query){
        b.removeAllViews();String q=query==null?"":query.trim().toUpperCase(new Locale("tr","TR"));
        Cursor c=db.getReadableDatabase().rawQuery("SELECT * FROM athletes WHERE TRIM(COALESCE(deletedAt,''))<>'' ORDER BY deletedAt DESC",null);int n=0;
        while(c.moveToNext()){
            String name=v(c,"name");if(!q.isEmpty()&&!name.toUpperCase(new Locale("tr","TR")).contains(q))continue;
            long id=c.getLong(c.getColumnIndexOrThrow("id"));String cat=v(c,"category"),photo=v(c,"photo"),at=v(c,"deletedAt");int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(8),dp(8),dp(8),dp(8));row.setBackground(round(Color.WHITE,10));
            ImageView av=new ImageView(this);av.setScaleType(ImageView.ScaleType.CENTER_CROP);setAthletePhoto(av,photo);row.addView(av,new LinearLayout.LayoutParams(dp(58),dp(58)));
            LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);text.addView(tv((by>0?by+" • ":"")+name,14,BLACK,true));text.addView(tv(cat+" • Silinme: "+deletedDateLabel(at),11,Color.DKGRAY,false));text.addView(tv(remainingLabel(at),11,RED,true));row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
            Button restore=btn("GERİ YÜKLE");restore.setTextSize(11);restore.setOnClickListener(v->confirmRestore(id,name));row.addView(restore,new LinearLayout.LayoutParams(dp(108),dp(48)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(7));b.addView(row,lp);n++;
        }c.close();
        if(n==0)b.addView(tv(q.isEmpty()?"Silinen sporcu bulunmuyor.":"Aramaya uygun silinen sporcu bulunamadı.",14,Color.DKGRAY,true));
    }
}
