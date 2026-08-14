package com.parion.aidat;

import android.database.Cursor;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;

public class MainActivityV31 extends MainActivity {
    @Override void showAthletes(){
        page="LIST"; base("SPORCULAR",true);
        LinearLayout panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(8),dp(8),dp(8),dp(6)); panel.setBackground(round(Color.WHITE,10));
        EditText q=new EditText(this); q.setHint("Sporcu adı ara"); panel.addView(q,new LinearLayout.LayoutParams(-1,dp(52)));

        LinearLayout row1=new LinearLayout(this); row1.setOrientation(LinearLayout.HORIZONTAL);
        Spinner st=new Spinner(this); String[] sts={"TÜM DURUMLAR","AKTİF","ARA VERDİ","BIRAKTI","ARANACAK","SAKATLANDI"}; st.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,sts));
        Spinner cat=new Spinner(this); String[] cats={"TÜM KATEGORİLER","SO 1","SO 2","SO 3","MİNİ VOLEYBOL","MİDİ","KÜÇÜK","YILDIZ","GENÇ"}; cat.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cats));
        row1.addView(st,new LinearLayout.LayoutParams(0,dp(52),1)); row1.addView(cat,new LinearLayout.LayoutParams(0,dp(52),1)); panel.addView(row1);

        EditText birth=new EditText(this); birth.setHint("Doğum yılı (örn. 2012)"); birth.setInputType(2); panel.addView(birth,new LinearLayout.LayoutParams(-1,dp(52)));
        Spinner note=new Spinner(this); ArrayList<String> noteItems=new ArrayList<>(); noteItems.add("TÜM ÖZEL NOTLAR"); noteItems.addAll(db.uniqueNotes()); note.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,noteItems)); panel.addView(note,new LinearLayout.LayoutParams(-1,dp(58)));
        root.addView(panel);

        ScrollView sv=scroll(); LinearLayout list=box(sv);
        Runnable load=()->loadAthletesAdvanced(list,q.getText().toString(),String.valueOf(st.getSelectedItem()),String.valueOf(cat.getSelectedItem()),birth.getText().toString(),String.valueOf(note.getSelectedItem()));
        android.text.TextWatcher w=new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){load.run();}public void afterTextChanged(android.text.Editable e){}};
        q.addTextChangedListener(w); birth.addTextChangedListener(w);
        android.widget.AdapterView.OnItemSelectedListener l=new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){load.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}};
        st.setOnItemSelectedListener(l); cat.setOnItemSelectedListener(l); note.setOnItemSelectedListener(l); load.run();
    }

    void loadAthletesAdvanced(LinearLayout b,String q,String status,String category,String birth,String selectedNote){
        b.removeAllViews(); String statusFilter=(status==null||status.startsWith("TÜM"))?"TÜMÜ":status; Cursor c=db.athletes(q,statusFilter); int shown=0;
        int birthNeed=0; try{birthNeed=Integer.parseInt(birth.trim());}catch(Exception ignored){}
        while(c.moveToNext()){
            String cat=s(c,"category"), nt=s(c,"notes"); int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
            if(category!=null&&!category.startsWith("TÜM")&&!category.equalsIgnoreCase(cat))continue;
            if(birthNeed>0&&by!=birthNeed)continue;
            if(selectedNote!=null&&!selectedNote.startsWith("TÜM")&&!selectedNote.trim().equals(nt==null?"":nt.trim()))continue;
            long id=c.getLong(c.getColumnIndexOrThrow("id")); String name=s(c,"name"),athleteStatus=s(c,"status"),photo=s(c,"photo");
            LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(8),dp(7),dp(8),dp(7)); row.setBackground(round(Color.WHITE,10));
            ImageView av=new ImageView(this); av.setScaleType(ImageView.ScaleType.CENTER_CROP); setAthletePhoto(av,photo); row.addView(av,new LinearLayout.LayoutParams(dp(60),dp(60)));
            LinearLayout t=new LinearLayout(this); t.setOrientation(LinearLayout.VERTICAL); t.addView(tv((by>0?by+" • ":"")+name,15,BLACK,true)); t.addView(tv(cat+" • "+athleteStatus,12,statusColor(athleteStatus),false)); if(nt!=null&&!nt.isEmpty())t.addView(tv(nt,11,Color.DKGRAY,false)); row.addView(t,new LinearLayout.LayoutParams(0,-2,1));
            final long athleteId=id; row.setOnClickListener(v->showProfile(athleteId)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(7)); b.addView(row,lp); shown++;
        } c.close();
        if(shown==0){TextView none=tv("FİLTRELERE UYGUN SPORCU BULUNAMADI",14,Color.DKGRAY,true); none.setGravity(Gravity.CENTER); b.addView(none);}
    }

    @Override void showProfile(long id){
        page="PROFILE"; currentAthlete=id; Cursor c=db.athlete(id); if(!c.moveToFirst()){c.close();showAthletes();return;}
        String name=s(c,"name"),photo=s(c,"photo"),cat=s(c,"category"),athleteStatus=s(c,"status"),notes=s(c,"notes"); int by=c.getInt(c.getColumnIndexOrThrow("birthYear"));
        String start=s(c,"startDate"),end=s(c,"endDate"),restart=s(c,"restartDate"),sib=s(c,"sibling");
        base("SPORCU PROFİLİ",true); ScrollView sv=scroll(); LinearLayout b=box(sv);
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER_HORIZONTAL); card.setPadding(dp(12),dp(14),dp(12),dp(14)); card.setBackground(round(Color.WHITE,14));
        ImageView av=new ImageView(this); av.setScaleType(ImageView.ScaleType.CENTER_CROP); setAthletePhoto(av,photo); LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(150),dp(180)); ip.gravity=Gravity.CENTER_HORIZONTAL; card.addView(av,ip);
        TextView nm=tv(name,22,BLACK,true); nm.setGravity(Gravity.CENTER); card.addView(nm,new LinearLayout.LayoutParams(-1,-2));
        TextView cs=tv(cat+" • "+athleteStatus,14,statusColor(athleteStatus),true); cs.setGravity(Gravity.CENTER); card.addView(cs,new LinearLayout.LayoutParams(-1,-2));
        TextView bd=tv("Doğum yılı: "+(by>0?by:"—"),13,Color.DKGRAY,false); bd.setGravity(Gravity.CENTER); card.addView(bd); b.addView(card);

        Calendar now=Calendar.getInstance(); int cm=now.get(Calendar.YEAR)==2026?now.get(Calendar.MONTH)+1:12; int currentFee=db.expectedFee(id,cm);
        b.addView(line("Güncel Aidat",money(currentFee))); b.addView(line("Sporcu Tel",s(c,"phone"))); b.addView(line("Anne",join(s(c,"motherName"),s(c,"motherPhone")))); b.addView(line("Baba",join(s(c,"fatherName"),s(c,"fatherPhone")))); b.addView(line("Kardeş",sib)); b.addView(line("İlk Kayıt",dateTr(start))); b.addView(line("Bitiş / Ara Verme",dateTr(end))); b.addView(line("Yeniden Başlama",dateTr(restart))); if(notes!=null&&!notes.isEmpty())b.addView(line("Özel Not",notes)); c.close();

        Button feeBtn=btn("AİDAT ÜCRETİ / GEÇERLİ AY DÜZENLE"); feeBtn.setOnClickListener(v->editFeePeriod(id)); LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(56)); fp.setMargins(0,dp(10),0,dp(8)); b.addView(feeBtn,fp);

        TextView h=tv("2026 AİDAT HAREKETLERİ",16,BLACK,true); h.setPadding(dp(10),dp(16),dp(10),dp(8)); b.addView(h); HashMap<Integer,String[]> pays=new HashMap<>(); Cursor p=db.payments(id); while(p.moveToNext())pays.put(p.getInt(p.getColumnIndexOrThrow("month")),new String[]{s(p,"marker"),String.valueOf(p.getInt(p.getColumnIndexOrThrow("amount")))}); p.close();
        for(int m=1;m<=12;m++){String[] a=pays.get(m);String marker=a==null?"":a[0];int amount=a==null?0:Integer.parseInt(a[1]);int expected=db.expectedFee(id,m);b.addView(paymentRow(id,m,expected,sib,athleteStatus,start,end,restart,marker,amount));}
        Button report=btn("YILLIK AİDAT RAPORUNU PAYLAŞ"); report.setOnClickListener(v->shareReport(id)); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(58));rp.setMargins(0,dp(16),0,dp(8));b.addView(report,rp);
    }

    void editFeePeriod(long id){
        LinearLayout x=new LinearLayout(this); x.setOrientation(LinearLayout.VERTICAL); x.setPadding(dp(20),dp(8),dp(20),0);
        Spinner month=new Spinner(this); ArrayList<String> months=new ArrayList<>(); for(int m=1;m<=12;m++)months.add(monthName(m)+" 2026'dan itibaren"); month.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,months));
        Calendar now=Calendar.getInstance(); if(now.get(Calendar.YEAR)==2026)month.setSelection(now.get(Calendar.MONTH));
        EditText fee=new EditText(this); fee.setHint("Yeni aylık aidat tutarı"); fee.setInputType(2); fee.setText(String.valueOf(db.expectedFee(id,month.getSelectedItemPosition()+1)));
        month.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long rowId){fee.setText(String.valueOf(db.expectedFee(id,pos+1)));}public void onNothingSelected(android.widget.AdapterView<?> p){}});
        x.addView(month);x.addView(fee);
        new android.app.AlertDialog.Builder(this).setTitle("AİDAT ÜCRETİ DEĞİŞİKLİĞİ").setMessage("Seçilen aydan itibaren yeni aidat geçerli olur. Önceki ayların tutarları değişmez.").setView(x).setPositiveButton("KAYDET",(d,w)->{int val=parseInt(fee.getText().toString());if(val>=0){db.setFeeFromMonth(id,month.getSelectedItemPosition()+1,val);showProfile(id);}}).setNegativeButton("İPTAL",null).show();
    }

    void setAthletePhoto(ImageView v,String photo){
        v.setImageResource(R.drawable.parion_logo); if(photo==null||photo.trim().isEmpty())return;
        String[] paths={"photos/"+photo,photo}; for(String path:paths){try(InputStream in=getAssets().open(path)){Bitmap bm=BitmapFactory.decodeStream(in);if(bm!=null){v.setImageBitmap(bm);return;}}catch(Exception ignored){}}
    }
    String fold(String x){if(x==null)return "";String z=x.toUpperCase(new Locale("tr","TR"));z=Normalizer.normalize(z,Normalizer.Form.NFD).replaceAll("\\p{M}","");return z.replace('İ','I').replace('Ş','S').replace('Ğ','G').replace('Ü','U').replace('Ö','O').replace('Ç','C');}
}
