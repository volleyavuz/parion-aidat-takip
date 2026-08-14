package com.parion.aidat;

import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.*;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;

public class MainActivityV31 extends MainActivity {
    String selectedStatus="TÜMÜ", selectedCategory="TÜMÜ";

    @Override void showAthletes(){
        page="LIST"; base("SPORCULAR",true);
        LinearLayout panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(8),dp(8),dp(8),dp(6)); panel.setBackground(round(Color.WHITE,10));
        EditText q=new EditText(this); q.setHint("Sporcu adı ara"); panel.addView(q,new LinearLayout.LayoutParams(-1,dp(52)));
        LinearLayout row1=new LinearLayout(this); row1.setOrientation(LinearLayout.HORIZONTAL);
        Spinner st=new Spinner(this); String[] sts={"TÜM DURUMLAR","AKTİF","ARA VERDİ","BIRAKTI","ARANACAK","SAKATLANDI"}; st.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,sts));
        Spinner cat=new Spinner(this); String[] cats={"TÜM KATEGORİLER","SO 1","SO 2","SO 3","MİNİ VOLEYBOL","MİDİ","KÜÇÜK","YILDIZ","GENÇ"}; cat.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cats));
        row1.addView(st,new LinearLayout.LayoutParams(0,dp(52),1)); row1.addView(cat,new LinearLayout.LayoutParams(0,dp(52),1)); panel.addView(row1);
        LinearLayout row2=new LinearLayout(this); row2.setOrientation(LinearLayout.HORIZONTAL);
        EditText birth=new EditText(this); birth.setHint("Doğum yılı (örn. 2012)"); birth.setInputType(2);
        EditText notes=new EditText(this); notes.setHint("Özel notlarda ara");
        row2.addView(birth,new LinearLayout.LayoutParams(0,dp(52),1)); row2.addView(notes,new LinearLayout.LayoutParams(0,dp(52),1)); panel.addView(row2);
        root.addView(panel);
        ScrollView sv=scroll(); LinearLayout list=box(sv);
        Runnable load=()->loadAthletesAdvanced(list,q.getText().toString(),String.valueOf(st.getSelectedItem()),String.valueOf(cat.getSelectedItem()),birth.getText().toString(),notes.getText().toString());
        android.text.TextWatcher w=new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){load.run();}public void afterTextChanged(android.text.Editable e){}};
        q.addTextChangedListener(w); birth.addTextChangedListener(w); notes.addTextChangedListener(w);
        android.widget.AdapterView.OnItemSelectedListener l=new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){load.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}};
        st.setOnItemSelectedListener(l); cat.setOnItemSelectedListener(l); load.run();
    }

    void loadAthletesAdvanced(LinearLayout b,String q,String status,String category,String birth,String notes){
        b.removeAllViews(); String stat=(status==null||status.startsWith("TÜM"))?"TÜMÜ":status; Cursor c=db.athletes(q,stat); int shown=0;
        String noteNeed=fold(notes.trim()); int birthNeed=0; try{birthNeed=Integer.parseInt(birth.trim());}catch(Exception ignored){}
        while(c.moveToNext()){
            String cat=s(c,"category"), nt=s(c,"notes"); int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
            if(category!=null&&!category.startsWith("TÜM")&&!category.equalsIgnoreCase(cat))continue;
            if(birthNeed>0&&by!=birthNeed)continue;
            if(!noteNeed.isEmpty()&&!fold(nt).contains(noteNeed))continue;
            long id=c.getLong(c.getColumnIndexOrThrow("id")); String name=s(c,"name"),stat=s(c,"status"),photo=s(c,"photo");
            LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(8),dp(7),dp(8),dp(7)); row.setBackground(round(Color.WHITE,10));
            ImageView av=new ImageView(this); av.setScaleType(ImageView.ScaleType.CENTER_CROP); setAthletePhoto(av,photo); row.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));
            LinearLayout t=new LinearLayout(this); t.setOrientation(LinearLayout.VERTICAL); t.addView(tv((by>0?by+" • ":"")+name,15,BLACK,true)); t.addView(tv(cat+" • "+stat,12,statusColor(stat),false)); if(!nt.isEmpty())t.addView(tv(nt,11,Color.DKGRAY,false)); row.addView(t,new LinearLayout.LayoutParams(0,-2,1));
            final long athleteId=id; row.setOnClickListener(v->showProfile(athleteId)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(7)); b.addView(row,lp); shown++;
        } c.close();
        if(shown==0){TextView none=tv("FİLTRELERE UYGUN SPORCU BULUNAMADI",14,Color.DKGRAY,true); none.setGravity(Gravity.CENTER); b.addView(none);}
    }

    @Override void showProfile(long id){
        page="PROFILE"; currentAthlete=id; Cursor c=db.athlete(id); if(!c.moveToFirst()){c.close();showAthletes();return;}
        String name=s(c,"name"),photo=s(c,"photo"),cat=s(c,"category"),stat=s(c,"status"),notes=s(c,"notes"); int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
        base("SPORCU PROFİLİ",true); ScrollView sv=scroll(); LinearLayout b=box(sv);
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER_HORIZONTAL); card.setPadding(dp(12),dp(14),dp(12),dp(14)); card.setBackground(round(Color.WHITE,14));
        ImageView av=new ImageView(this); av.setScaleType(ImageView.ScaleType.CENTER_CROP); setAthletePhoto(av,photo); LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(150),dp(180)); ip.gravity=Gravity.CENTER_HORIZONTAL; card.addView(av,ip);
        TextView nm=tv(name,22,BLACK,true); nm.setGravity(Gravity.CENTER); card.addView(nm,new LinearLayout.LayoutParams(-1,-2));
        TextView cs=tv(cat+" • "+stat,14,statusColor(stat),true); cs.setGravity(Gravity.CENTER); card.addView(cs,new LinearLayout.LayoutParams(-1,-2));
        TextView bd=tv("Doğum yılı: "+(by>0?by:"—"),13,Color.DKGRAY,false); bd.setGravity(Gravity.CENTER); card.addView(bd); b.addView(card);
        b.addView(line("Aylık Aidat",money(c.getInt(c.getColumnIndexOrThrow("monthlyFee"))))); b.addView(line("Sporcu Tel",s(c,"phone"))); b.addView(line("Anne",join(s(c,"motherName"),s(c,"motherPhone")))); b.addView(line("Baba",join(s(c,"fatherName"),s(c,"fatherPhone")))); b.addView(line("Kardeş",s(c,"sibling"))); b.addView(line("İlk Kayıt",dateTr(s(c,"startDate")))); b.addView(line("Bitiş / Ara Verme",dateTr(s(c,"endDate")))); b.addView(line("Yeniden Başlama",dateTr(s(c,"restartDate")))); if(!notes.isEmpty())b.addView(line("Özel Not",notes));
        int fee=c.getInt(c.getColumnIndexOrThrow("monthlyFee")); String status=s(c,"status"),start=s(c,"startDate"),end=s(c,"endDate"),restart=s(c,"restartDate"),sib=s(c,"sibling"); c.close();
        TextView h=tv("2026 AİDAT HAREKETLERİ",16,BLACK,true); h.setPadding(dp(10),dp(20),dp(10),dp(8)); b.addView(h); HashMap<Integer,String[]> pays=new HashMap<>(); Cursor p=db.payments(id); while(p.moveToNext())pays.put(p.getInt(p.getColumnIndexOrThrow("month")),new String[]{s(p,"marker"),String.valueOf(p.getInt(p.getColumnIndexOrThrow("amount")))}); p.close();
        for(int m=1;m<=12;m++){String[] a=pays.get(m);String marker=a==null?"":a[0];int amount=a==null?0:Integer.parseInt(a[1]);b.addView(paymentRow(id,m,fee,sib,status,start,end,restart,marker,amount));}
        Button report=btn("YILLIK AİDAT RAPORUNU PAYLAŞ"); report.setOnClickListener(v->shareReport(id)); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(58));rp.setMargins(0,dp(16),0,dp(8));b.addView(report,rp);
    }

    void setAthletePhoto(ImageView v,String photo){
        v.setImageResource(R.drawable.parion_logo); if(photo==null||photo.trim().isEmpty())return;
        String[] paths={"photos/"+photo,photo}; for(String path:paths){try(InputStream in=getAssets().open(path)){Bitmap bm=BitmapFactory.decodeStream(in);if(bm!=null){v.setImageBitmap(bm);return;}}catch(Exception ignored){}}
    }
    String fold(String x){if(x==null)return "";String z=x.toUpperCase(new Locale("tr","TR"));z=Normalizer.normalize(z,Normalizer.Form.NFD).replaceAll("\\p{M}","");return z.replace('İ','I').replace('Ş','S').replace('Ğ','G').replace('Ü','U').replace('Ö','O').replace('Ç','C');}
}
